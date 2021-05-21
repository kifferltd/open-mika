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

import java.awt.*;
import java.util.*;

public class PixelGrabber implements ImageConsumer {

  private int                status;
  private int                hints;
  private ColorModel         colorModel = null;
  private Hashtable          properties;
  private ImageProducer      producer;
  private int                sx, sy, sw, sh, off, scansize;
  private int[]              pixels;
  private boolean            rgb;
  
  public PixelGrabber(Image img, int x, int y, int w, int h, int[] pix, int off, int scansize) {
    this(img.getSource(), x, y, w, h, pix, off, scansize);
  }

  public PixelGrabber(ImageProducer ip, int x, int y, int w, int h, int[] pix, int off, int scansize) {
    sx = x;
    sy = y;
    sw = w;
    sh = h;
    pixels = pix;
    this.off = off;
    this.scansize = scansize;
    producer = ip;
    rgb = true;
  }

  public PixelGrabber(Image img, int x, int y, int w, int h, boolean forceRGB) {
    sx = x;
    sy = y;
    sw = w;
    sh = h;
    pixels = null;
    off = 0;
    scansize = w;
    producer = img.getSource();
    rgb = forceRGB;
  }

  public synchronized void abortGrabbing() {
  }

  public synchronized ColorModel getColorModel() {
    if(rgb) {
      return ColorModel.getRGBdefault();
    }
    else {
      return colorModel;
    }
  }

  public synchronized int getHeight() {
    return sh;
  }

  public synchronized int getWidth() {
    return sw;
  }

  public synchronized Object getPixels() {
    return pixels;
  }

  public synchronized int getStatus() {
    return status;
  }

  public synchronized boolean grabPixels() throws InterruptedException {
    startGrabbing();
    while(status != STATICIMAGEDONE) {
      Thread.sleep(1);
    }
    return true;
  }

  public synchronized boolean grabPixels(long ms) throws InterruptedException {
    startGrabbing();
    while(status != STATICIMAGEDONE) {
      Thread.sleep(1);
    }
    return true;
  }

  public synchronized void startGrabbing() {
    producer.startProduction(this); 
  }

  /*
  ** The following are from the ImageConsumer interface.
  */

  public void imageComplete(int status) {
    this.status = status;
    if(status == STATICIMAGEDONE) {
    }
  }
  
  public void setColorModel(ColorModel model) {
    colorModel = model;
  }
  
  public void setDimensions(int w, int h) {
    if(sw < 0) sw = w;
    if(sh < 0) sh = h;
    pixels = new int[w * h];
  }
  
  public void setHints(int hints) {
    this.hints = hints;
  }

  private int convertRGB(int red, int green, int blue) {
    return (0xFF << 24) | (red << 16) | (green << 8) | blue;
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scansize) {
    int ox = sx - x;
    int oy = sy - y;
    int nw = sw;
    int nh = sh;
    int ss = this.scansize;
    if(ox < 0) { nw += ox; ox = 0; }
    if(oy < 0) { nh += oy; oy = 0; }
    if((sx + sw) > (x + w)) { nw -= (sw - (x + w - sx)); } 
    if((sy + sh) > (y + h)) { nh -= (sh - (y + h - sy)); } 

    if(nw <= 0 || nh <= 0) return;

    if(rgb) {
      for(int j=0; j < nh; j++) {
        for(int i=0; i < nw; i++) {
          byte pixel = pixels[(j + oy) * scansize + (i + ox) + offset];
          this.pixels[(j + y) * ss + (i + x)] = convertRGB(model.getRed(pixel), model.getGreen(pixel), model.getBlue(pixel));
        }
      }
    }
    else {
      for(int j=0; j < nh; j++) {
        for(int i=0; i < nw; i++) {
          this.pixels[(j + y) * ss + (i + x)] = pixels[(j + oy) * scansize + (i + ox) + offset];
        }
      }
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scansize) {
    int ox = sx - x;
    int oy = sy - y;
    int nw = sw;
    int nh = sh;
    int ss = this.scansize;
    if(ox < 0) { nw += ox; ox = 0; }
    if(oy < 0) { nh += oy; oy = 0; }
    if((sx + sw) > (x + w)) { nw -= (sw - (x + w - sx)); } 
    if((sy + sh) > (y + h)) { nh -= (sh - (y + h - sy)); } 

    if(nw <= 0 || nh <= 0) return;

    if(rgb) {
      for(int j=0; j < nh; j++) {
        for(int i=0; i < nw; i++) {
          int pixel = pixels[(j + oy) * scansize + (i + ox) + offset];
          this.pixels[(j + y) * ss + (i + x)] = convertRGB(model.getRed(pixel), model.getGreen(pixel), model.getBlue(pixel));
        }
      }
    }
    else {
      for(int j=0; j < nh; j++) {
        for(int i=0; i < nw; i++) {
          this.pixels[(j + y) * ss + (i + x)] = pixels[(j + oy) * scansize + (i + ox) + offset];
        }
      }
    }
  }

  public void setProperties(Hashtable properties) {
    this.properties = properties;
  }

  public String toString() {
    return "[PixelGrabber] status: " + status + "  hints: " + hints + "  colormodel: " + colorModel + "  width: " + sw + "  height: " + sh + "  producer: " + producer + "  pixels: " + pixels;
  }

}
