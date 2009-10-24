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

public class ReplicateScaleFilter extends ImageFilter {

  protected int destHeight;
  protected int destWidth;
  protected int srcHeight;
  protected int srcWidth;
  
  protected Object outpixbuf;
  
  protected int[] srccols;
  protected int[] srcrows;

  public ReplicateScaleFilter(int width, int height) {
    destHeight = height;
    destWidth = width;
  }
  
  public void setDimensions(int w, int h) {
    super.setDimensions(destWidth, destHeight);
    srcHeight = h;
    srcWidth = w;

    srccols = new int[srcWidth + 1];
    srcrows = new int[srcHeight + 1];

    for(int i=0; i<=srcWidth; i++) {
      srccols[i] = i * destWidth / srcWidth;
    }

    for(int i=0; i<=srcHeight; i++) {
      srcrows[i] = i * destHeight / srcHeight;
    }
    
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    int nw = srccols[x + w] - srccols[x];
    int nh = srcrows[y + h] - srcrows[y];
    byte[] newpixels = new byte[nw * nh];
    
    for(int i=0; i<h; i++) {
      for(int j=0; j<w; j++) {
        for(int k=0; k<(srcrows[y + i + 1] - srcrows[y + i]); k++) {
          for(int l=0; l<(srccols[x + j + 1] - srccols[x + j]); l++) {
            newpixels[(srcrows[y + i] - srcrows[y] + k) * nw + (srccols[x + j] - srccols[x] + l)] = pixels[i * scansize + j + off];
          }
        }
      }
    }
    super.setPixels(srccols[x], srcrows[y], nw, nh, model, newpixels, 0, nw);
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    int nw = srccols[x + w] - srccols[x];
    int nh = srcrows[y + h] - srcrows[y];
    int[] newpixels = new int[nw * nh];

    for(int i=0; i<h; i++) {
      for(int j=0; j<w; j++) {
        for(int k=0; k<(srcrows[y + i + 1] - srcrows[y + i]); k++) {
          for(int l=0; l<(srccols[x + j + 1] - srccols[x + j]); l++) {
            newpixels[(srcrows[y + i] - srcrows[y] + k) * nw + (srccols[x + j] - srccols[x] + l)] = pixels[i * scansize + j + off];
          }
        }
      }
    }
    super.setPixels(srccols[x], srcrows[y], nw, nh, model, newpixels, 0, nw);
  }

  public void setProperties(Hashtable props) {
    super.setProperties(props);
  }
  
}

