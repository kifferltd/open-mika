/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.util.*;

public class GIFImageSource implements ImageProducer {

  private Vector         consumers;
  private Hashtable      properties;
  private ColorModel     colorModel;
  protected Vector       frames;
  protected byte[]       pixels;
  protected int          height;
  protected int          width;
  protected int          next_event;
  private GIFFrame       current_frame;
  private int            counter = 0;
  private byte           background;

  private static int     NEXT_FRAME_NONE = 0;
  private static int     NEXT_FRAME_REPAINT = 0;
  private static int     NEXT_FRAME_UPDATE = 0x80000000;
  
  private native void readImage(byte[] data); 

  public GIFImageSource(byte[] data) {
    readImage(data);
    consumers = new Vector();
  }

  public void addConsumer(ImageConsumer consumer) {
    if(!consumers.contains(consumer))
      consumers.add(consumer);
  }
  
  public boolean isConsumer(ImageConsumer consumer) {
    return consumers.contains(consumer);
  }
  
  public void removeConsumer(ImageConsumer consumer) {
    consumers.remove(consumer);
  }
  
  public void requestTopDownLeftRightResend(ImageConsumer consumer) {
  }

  private native void copyFrame(GIFFrame frame);

  private int getNextFrame() {
    boolean newpixels = false;
    int result = NEXT_FRAME_REPAINT;
    
    if(pixels == null) {
      pixels = new byte[width * height];
      newpixels = true;
    }

    if(frames == null) {
      return NEXT_FRAME_NONE;
    }

    current_frame = (GIFFrame)frames.elementAt(counter);

    if(newpixels) {
      if(current_frame.transparent != -1) Arrays.fill(pixels, (byte)current_frame.transparent);
    }

    switch(current_frame.disposal) {
      case 0:  /* Do nothing */
        break;
      case 1:  /* Leave frame in place */
        break;
      case 2:  /* Resore background color */
        if(current_frame.transparent != -1) {
          Arrays.fill(pixels, (byte)current_frame.transparent);
        }
        else {
          Arrays.fill(pixels, (byte)background);
        }
        result = NEXT_FRAME_UPDATE;
        break;
      case 3:  /* Restore original background */
        result = NEXT_FRAME_UPDATE;
        break;
    }

    copyFrame(current_frame);
    
    counter++;
    if(counter == frames.size()) {
      counter = 0;
    }
   
    return result;
  }
  
  public synchronized void startProduction(ImageConsumer consumer) {
    
    if(frames.size() > 1) {
      GIFAnimator gifAnimator = GIFAnimator.getInstance();
      gifAnimator.registerGIF(this, consumer);
      //gifAnimator.startAnimator();
      //return;
    }
    
    getNextFrame();
    
    if(consumer != null) {
      addConsumer(consumer);
    }
    
    ColorModel model = current_frame.colorModel != null ? current_frame.colorModel : colorModel;
    
    Iterator iter = consumers.iterator();
    
    byte newpixels[] = new byte[width * height];

    while(iter.hasNext()) {
      consumer = (ImageConsumer)iter.next();
      consumer.setDimensions(width, height);
      consumer.setProperties(properties);
      consumer.setColorModel(model);

      System.arraycopy(pixels, 0, newpixels, 0, width * height);
      consumer.setPixels(0, 0, width, height, model, newpixels, 0, width); 
      consumer.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
    consumers = new Vector();
  }

  public synchronized boolean produceNextFrame(WeakHashMap consumers, int current_tick) {
    boolean result = false;
    int update = getNextFrame();
    ImageConsumer consumer;
   
    next_event = current_tick + current_frame.delay / 4;

    Iterator iter = consumers.keySet().iterator();
    byte newpixels[] = new byte[width];
      
    while(iter.hasNext()) {
      consumer = (ImageConsumer)iter.next();
      if(consumer != null) {
        result = true;
        consumer.setDimensions(width, height);
        consumer.setProperties(properties);
        consumer.setColorModel(colorModel);
        for(int i=0; i < height; i++) {
          System.arraycopy(pixels, i * width, newpixels, 0, width);
          consumer.setPixels(0, i, width, 1, colorModel, newpixels, 0, width); 
        }
        consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE | update);
      }
    }
    return result;
  }

  private void addFrame(GIFFrame frame) {
    if(frames == null) {
      frames = new Vector();
    }
    frames.add(frame);
  }

  boolean isAnimated() {
    return (frames.size() > 1);
  }  
}

