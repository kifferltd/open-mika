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

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Image extends java.awt.Image {

  private Component component;
  private String filename;
  protected ImageProducer producer;
  Hashtable props;
  char[] pixels;
  private char[] alpha;
  protected ImageConsumer consumer;
  protected int flags;
  int width = -1;
  int height = -1;
  private int key = 0;
  WeakHashMap observers;

  static native int convertRGB(int red, int green, int blue);
  native void setPixel(int x, int y, int scanline, int pix);
  native void do_conversion(byte[] src, char[] dst, IndexColorModel model, int x, int y, int w, int h, int offset, int scansize);
  native void do_conversion(int[] src, char[] dst, DirectColorModel model, int x, int y, int w, int h, int offset, int scansize);

  protected class Consumer implements ImageConsumer {

    protected Consumer() { }
    
    public void setColorModel(ColorModel model) { }
    public void setHints(int hints) { }
    
    public void imageComplete(int status) { 
      if((status & 0xffff) == STATICIMAGEDONE) {
        createImage();
        pixels = null;
        flags |= ImageObserver.ALLBITS; 
      }
      else {
        createImage();
        pixels = null;
        if((status & 0x80000000) != 0) {
          flags |= 0x80000000;
        }
        else {
          flags &= 0x7FFFFFFF;
        }
        if(!imageUpdate(ImageObserver.FRAMEBITS, 0, 0, 0, 0)) {
          consumer = null;
        }
      }
    }
    
    public void setProperties(Hashtable properties) { 
      flags |= ImageObserver.PROPERTIES;
      props = properties;
    }

    public void setDimensions(int w, int h) { 
      width = w;
      height = h;
      flags |= ImageObserver.WIDTH | ImageObserver.HEIGHT;
      pixels = new char[w * h];
    }

    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pix, int offset, int scansize) {
      if(model instanceof IndexColorModel) {
        do_conversion(pix, pixels, (IndexColorModel)model, x, y, w, h, offset, scansize);
      }
      else {
        for(int j=0; j<h; j++) {
          for(int i=0; i<w; i++) {
            byte pixel = pix[j * scansize + i + offset];
            setPixel(i, j, width, convertRGB(model.getRed(pixel), model.getGreen(pixel), model.getBlue(pixel)));
          }
        }
      }
      pix = null;
    }

    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int offset, int scansize) {
      if(model instanceof DirectColorModel) {
        do_conversion(pix, pixels, (DirectColorModel)model, x, y, w, h, offset, scansize);
      } 
      else {
        for(int j=0; j<h; j++) {
          for(int i=0; i<w; i++) {
            int pixel = pix[j * scansize + i + offset];
            setPixel(i, j, width, convertRGB(model.getRed(pixel), model.getGreen(pixel), model.getBlue(pixel)));
          }
        }
      }
      pix = null;
    }
  }

  Image() {
  }

  public Image(ImageProducer producer) {
    this.consumer = new Consumer();
    this.producer = producer;
    producer.startProduction(consumer);
  }

  native void createImage();

  public int getWidth(ImageObserver observer) {
    if(observer != null) {
      observer.imageUpdate(this, flags, 0, 0, width, height);
    }
    return width;
  }
  
  public int getHeight(ImageObserver observer) {
    if(observer != null) {
      observer.imageUpdate(this, flags, 0, 0, width, height);
    }
    return height;
  }
  
  native public java.awt.Graphics getGraphics();
  
  public Object getProperty(String name, ImageObserver observer) {
    Object result;
    if(observer != null) {
      observer.imageUpdate(this, flags, 0, 0, width, height);
    }
    if(props != null) {
      result = props.get(name);
      if(result == null) result = UndefinedProperty;
    } else {
      result = UndefinedProperty;
    }
    return result;
  }
  
  public ImageProducer getSource() {
    return (producer != null ? producer : new BufferImageSource(this));
  }
  
  public void flush() {
    producer = null;
    pixels = null;
    createImage();
    if(key != 0) {
      com.acunia.wonka.rudolph.Toolkit.t.imageCache.remove(key);
      key = 0;
    }
  }

  void setKey(int key) {
    this.key = key;
  }

  protected void finalize() {
    if(key != 0) {
      try {
        com.acunia.wonka.rudolph.Toolkit.t.imageCache.remove(key);
      }
      catch(Exception e) {
      }
    }
    finalize0();
  }

  private native void finalize0();

  void addObserver(ImageObserver observer) {
    if(observer != null) {
      if(observers == null) observers = new WeakHashMap();
      if(!observers.containsKey(observer)) {
        observers.put(observer, null);
      }
      if(producer instanceof GIFImageSource) {
        if(((GIFImageSource)producer).isAnimated()) {
          GIFAnimator.getInstance().startAnimator();
        }
      }
    }
  }
  
  synchronized boolean imageUpdate(int flags, int x, int y, int w, int h) {
    boolean result = false;
    if(observers != null) {
      Iterator iter = observers.keySet().iterator();
      WeakHashMap newObs = new WeakHashMap();

      while(iter.hasNext()) {
        ImageObserver obs = (ImageObserver)iter.next();
        if(obs != null) {
          if(obs.imageUpdate(this, flags, x, y, w, h)) {
            newObs.put(obs, null);
            result = true;
          }
        }
      }
      observers = newObs;
    }
    return result;
  }

}

