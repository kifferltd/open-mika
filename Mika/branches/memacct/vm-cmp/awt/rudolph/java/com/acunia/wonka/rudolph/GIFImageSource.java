/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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

