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

public class DirectColorModel extends ColorModel {

  private int rmask;
  private int gmask;
  private int bmask;
  private int amask;

  private int rmaskbits;
  private int gmaskbits;
  private int bmaskbits;
  private int amaskbits;

  private int rmaskpos;
  private int gmaskpos;
  private int bmaskpos;
  private int amaskpos;

  public DirectColorModel(int bits, int rmask, int gmask, int bmask) {
    this(bits, rmask, gmask, bmask, 0);
  }
  
  public DirectColorModel(int bits, int rmask, int gmask, int bmask, int amask) {
    super(bits);
    this.rmask = rmask;
    this.gmask = gmask;
    this.bmask = bmask;
    this.amask = amask;

    if(rmask != 0) {
      rmaskpos = getPos(rmask);
      rmaskbits = getBits(rmask, rmaskpos);
    }
    
    if(gmask != 0) {
      gmaskpos = getPos(gmask);
      gmaskbits = getBits(gmask, gmaskpos);
    }
    
    if(bmask != 0) {
      bmaskpos = getPos(bmask);
      bmaskbits = getBits(bmask, bmaskpos);
    }
    
    if(amask != 0) {
      amaskpos = getPos(amask);
      amaskbits = getBits(amask, amaskpos);
    }
    
  }

  private int getPos(int val) {
    int result = 0;
    while((val & 1) == 0) {
      val = val >>> 1;
      result++;
    }
    return result;
  }

  private int getBits(int val, int pos) {
    int result = 0;
    val = val >>> pos;
    while((val & 1) == 1) {
      val = val >>> 1;
      result++;
    }
    return result;
  }
  
  final public int getAlpha(int pixelValue) {
    if(amask == 0) return 255;
    return (((pixelValue & amask) >>> amaskpos) << 8) >>> amaskbits;
  }
  
  final public int getAlphaMask() {
    return amask;
  }
  
  final public int getRed(int pixelValue) {
    if(rmask == 0) return 0;
    return (((pixelValue & rmask) >>> rmaskpos) << 8) >>> rmaskbits;
  }
  
  final public int getRedMask() {
    return rmask;
  }
  
  final public int getGreen(int pixelValue) {
    if(gmask == 0) return 0;
    return (((pixelValue & gmask) >>> gmaskpos) << 8) >>> gmaskbits;
  }

  final public int getGreenMask() {
    return gmask;
  }

  final public int getBlue(int pixelValue) {
    if(bmask == 0) return 0;
    return (((pixelValue & bmask) >>> bmaskpos) << 8) >>> bmaskbits;
  }

  final public int getBlueMask() {
    return bmask;
  }

  public final int getRGB(int pixelValue) {
    return (getAlpha(pixelValue) << 24) | (getRed(pixelValue) << 16) | (getGreen(pixelValue) << 8) | (getBlue(pixelValue));
  }
  
}
