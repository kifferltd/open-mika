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

package java.awt.image;

import java.util.*;

public class MemoryImageSource implements ImageProducer {

  private Vector         consumers = new Vector();
  private Hashtable      properties;
  private int            width;
  private int            height;
  private int            offset;
  private int            scansize;
  private int[]          pixels_int = null;
  private byte[]         pixels_byte = null;
  private ColorModel     colorModel;
  private boolean        animated = false;
  
  public MemoryImageSource(int width, int height, int[] pixels, int offset, int scansize) {
    this(width, height, pixels, offset, scansize, null);
  }

  public MemoryImageSource(int width, int height, int[] pixels, int offset, int scansize, Hashtable props) {
    this.width = width;
    this.height = height;
    this.offset = offset;
    this.scansize = scansize;
    properties = props;
    pixels_int = pixels;
    colorModel = ColorModel.getRGBdefault();
  }

  public MemoryImageSource(int width, int height, ColorModel cm, byte[] pixels, int offset, int scansize) {
    this(width, height, cm, pixels, offset, scansize, null);
  }

  public MemoryImageSource(int width, int height, ColorModel cm, int[] pixels, int offset, int scansize) {
    this(width, height, cm, pixels, offset, scansize, null);
  }

  public MemoryImageSource(int width, int height, ColorModel cm, byte[] pixels, int offset, int scansize, Hashtable props) {
    this.width = width;
    this.height = height;
    this.offset = offset;
    this.scansize = scansize;
    properties = props;
    pixels_byte = pixels;
    colorModel = cm;
  }

  public MemoryImageSource(int width, int height, ColorModel cm, int[] pixels, int offset, int scansize, Hashtable props) {
    this.width = width;
    this.height = height;
    this.offset = offset;
    this.scansize = scansize;
    properties = props;
    pixels_int = pixels;
    colorModel = cm;
  }

  public void newPixels() {
    if(animated) {
      startProduction(null);
    }
  }

  public synchronized void newPixels(byte[] pixels, ColorModel cm, int offset, int scansize) {
    if(animated) {
      this.pixels_int = null;
      this.pixels_byte = pixels;
      this.colorModel = cm;
      this.offset = offset;
      this.scansize = scansize;
      newPixels();
    }
  }

  public synchronized void newPixels(int[] pixels, ColorModel cm, int offset, int scansize) {
    if(animated) {
      this.pixels_int = pixels;
      this.pixels_byte = null;
      this.colorModel = cm;
      this.offset = offset;
      this.scansize = scansize;
      newPixels();
    }
  }

  public synchronized void newPixels(int x, int y, int width, int height) {
    if(animated) {
      newPixels(x, y, width, height, true);
    }
  }

  public synchronized void newPixels(int x, int y, int width, int height, boolean frameNotify) {
    if(animated) {
      
      Iterator iter = consumers.iterator();
      while(iter.hasNext()) {
        ImageConsumer consumer = (ImageConsumer)iter.next();
        consumer.setDimensions(width, height);
        consumer.setProperties(properties);
        consumer.setColorModel(colorModel);
        if(pixels_int != null) {
          int newpixels[] = new int[width];
          for(int i=0; i < height; i++) {
            for(int j=0; j < width; j++) newpixels[j] = pixels_int[(i + y) * this.width + j + x];
            consumer.setPixels(0, i, width, 1, colorModel, newpixels, 0, width);    
          } 
        } 
        else if(pixels_byte != null) {
          byte newpixels[] = new byte[width];
          for(int i=0; i < height; i++) {
            for(int j=0; j < width; j++) newpixels[j] = pixels_byte[(i + y) * this.width + j + x];
            consumer.setPixels(0, i, width, 1, colorModel, newpixels, 0, width);    
          } 
        }
        consumer.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
      }
    }
  }

  public synchronized void setAnimated(boolean animated) {
    this.animated = animated;
  }

  public synchronized void setFullBufferUpdates(boolean full) {
    System.out.println("[MemoryImageSource] setFullBufferUpdates not yet supported...");
  }

  public void addConsumer(ImageConsumer ic) {
    if(!consumers.contains(ic)) {
      consumers.add(ic);
    }
  }
  
  public boolean isConsumer(ImageConsumer ic) {
    return consumers.contains(ic);
  }
  
  public void removeConsumer(ImageConsumer ic) {
    consumers.remove(ic);
  }
  
  public void requestTopDownLeftRightResend(ImageConsumer ic) {
  }
  
  public synchronized void startProduction(ImageConsumer consumer) {
    if(consumer != null) {
      addConsumer(consumer);
    }
    Iterator iter = consumers.iterator();
    while(iter.hasNext()) {
      consumer = (ImageConsumer)iter.next();
      // System.out.println("  consumer: " + consumer);
      consumer.setDimensions(width, height);
      consumer.setProperties(properties);
      consumer.setColorModel(colorModel);
      if(pixels_int != null) {
        int newpixels[] = (int[])pixels_int.clone();
        consumer.setPixels(0, 0, width, height, colorModel, newpixels, offset, scansize);    
      } 
      else if(pixels_byte != null) {
        byte newpixels[] = (byte[])pixels_byte.clone();
        consumer.setPixels(0, 0, width, height, colorModel, newpixels, offset, scansize);    
        /*
        byte newpixels[] = new byte[width];
        for(int i=0; i < height; i++) {
          for(int j=0; j < width; j++) newpixels[j] = pixels_byte[i * width + j];
          consumer.setPixels(0, i, width, 1, colorModel, newpixels, 0, width);    
        } 
        */
      }
      consumer.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
  }
  
}

