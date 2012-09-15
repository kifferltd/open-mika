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

import java.util.Arrays;

public class IndexColorModel extends ColorModel {

  private byte[] reds;
  private byte[] greens;
  private byte[] blues;
  private byte[] alphas;
  private int[] internal;

  private int size;
  private int bits;
  private int trans;

  public IndexColorModel(int nbits, int size, byte[] reds, byte[] greens, byte[] blues) {
    this(nbits, size, reds, greens, blues, -1);
  }

  public IndexColorModel(int nbits, int size, byte[] reds, byte[] greens, byte[] blues, int trans) {
    super(nbits);
    byte[] alphas = new byte[size];
    Arrays.fill(alphas, (byte)255);
    setupArrays(size, reds, greens, blues, alphas);
    this.trans = trans;
    this.size = size;
    setup_internal();
  }

  public IndexColorModel(int nbits, int size, byte[] reds, byte[] greens, byte[] blues, byte[] alphas) {
    super(nbits);
    setupArrays(size, reds, greens, blues, alphas);
    this.trans = -1;
    this.size = size;
    setup_internal();
  }

  public IndexColorModel(int nbits, int size, byte[] cmap, int start, boolean hasAlpha) {
    this(nbits, size, cmap, start, hasAlpha, -1);
  }
  
  public IndexColorModel(int nbits, int size, byte[] cmap, int start, boolean hasAlpha, int trans) {
    super(nbits);
    this.trans = -1;
    this.size = size;
    this.reds = new byte[size];
    this.greens = new byte[size];
    this.blues = new byte[size];
    this.alphas = new byte[size];
    int j = start;
    if(hasAlpha) {
      for(int i=0; i<size; i++) {
        reds[i] = cmap[j++];
        greens[i] = cmap[j++];
        blues[i] = cmap[j++];
        alphas[i] = cmap[j++];
      }
    }
    else {
      for(int i=0; i<size; i++) {
        reds[i] = cmap[j++];
        greens[i] = cmap[j++];
        blues[i] = cmap[j++];
      }
      Arrays.fill(alphas, (byte)255);
    }
    setup_internal();
  }
  
  private void setupArrays(int size, byte[] reds, byte[] greens, byte[] blues, byte[] alphas) {
    this.reds = new byte[size];
    this.greens = new byte[size];
    this.blues = new byte[size];
    this.alphas = new byte[size];
    System.arraycopy(reds, 0, this.reds, 0, size);
    System.arraycopy(greens, 0, this.greens, 0, size);
    System.arraycopy(blues, 0, this.blues, 0, size);
    System.arraycopy(alphas, 0, this.alphas, 0, size);
  }

  final public int getAlpha(int pixelValue) {
    return alphas[pixelValue];
  }
  
  final public void getAlphas(byte[] a) {
    for(int i=0; i<size; i++) {
      a[i] = alphas[i];
    }
  }
  
  final public int getRed(int pixelValue) {
    pixelValue &= (1 << pixel_bits) - 1;
    return reds[pixelValue] & 0xFF;
  }
  
  final public void getReds(byte[] r) {
    for(int i=0; i<size; i++) {
      r[i] = reds[i];
    }
  }
  
  final public int getGreen(int pixelValue) {
    pixelValue &= (1 << pixel_bits) - 1;
    return greens[pixelValue] & 0xFF;
  }

  final public void getGreens(byte[] g) {
    for(int i=0; i<size; i++) {
      g[i] = greens[i];
    }
  }

  final public int getBlue(int pixelValue) {
    pixelValue &= (1 << pixel_bits) - 1;
    return blues[pixelValue] & 0xFF;
  }

  final public void getBlues(byte[] b) {
    for(int i=0; i<size; i++) {
      b[i] = blues[i];
    }
  }

  final public int getMapSize() {
    return size;
  }

  final public int getTransparentPixel() {
    return trans;
  }

  public final int getRGB(int pixelValue) {
    return super.getRGB(pixelValue);
  }

  private native void fill_internal();

  private void setup_internal() {
    if(internal == null) {
      internal = new int[size];
    }
    fill_internal();
  }

}

