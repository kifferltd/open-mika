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

public class CropImageFilter extends ImageFilter {

  private int sx;
  private int sy;
  private int sw;
  private int sh;

  public CropImageFilter(int x, int y, int width, int height) {
    sx = x;
    sy = y;
    sw = width;
    sh = height;
  }

  public void setDimensions(int width, int height) {
    super.setDimensions(sw, sh);
  }

  public void setProperties(Hashtable properties) {
    super.setProperties(properties);
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    int ox = sx - x;
    int oy = sy - y;
    int nw = sw;
    int nh = sh;
    if(ox < 0) { nw += ox; ox = 0; }
    if(oy < 0) { nh += oy; oy = 0; }
    if((sx + sw) > (x + w)) { nw -= (sw - (x + w - sx)); } 
    if((sy + sh) > (y + h)) { nh -= (sh - (y + h - sy)); } 

    if((nw > 0) && (nh > 0)) {
      byte[] newpixels = new byte[nw * nh];
      for(int i=0; i<nh; i++) {
        for(int j=0; j<nw; j++) {
          newpixels[i * nw + j] = pixels[(i + oy) * scansize + (j + ox) + off];
        }
      }
      super.setPixels((x - sx > 0 ? x - sx : 0) , (y - sy > 0 ? y - sy : 0), nw, nh, model, newpixels, 0, nw);
    }
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    int ox = sx - x;
    int oy = sy - y;
    int nw = sw;
    int nh = sh;
    if(ox < 0) { nw += ox; ox = 0;}
    if(oy < 0) { nh += oy; oy = 0;}
    if((sx + sw) > (x + w)) { nw -= (sw - (x + w - sx)); } 
    if((sy + sh) > (y + h)) { nh -= (sh - (y + h - sy)); } 

    if((nw > 0) && (nh > 0)) {
      int[] newpixels = new int[nw * nh];
      for(int i=0; i<nh; i++) {
        for(int j=0; j<nw; j++) {
          newpixels[i * nw + j] = pixels[(i + oy) * scansize + (j + ox) + off];
        }
      }
      super.setPixels((x - sx > 0 ? x - sx : 0) , (y - sy > 0 ? y - sy : 0), nw, nh, model, newpixels, 0, nw);
    }
  }

}

