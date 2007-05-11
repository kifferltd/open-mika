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

public abstract class ColorModel {

  protected int pixel_bits;
  private static ColorModel RGBdefault;

  public ColorModel(int b) {
    pixel_bits = b;
  }
  
  public abstract int getAlpha(int pixelValue);
  public abstract int getRed(int pixelValue);
  public abstract int getGreen(int pixelValue);
  public abstract int getBlue(int pixelValue);

  public int getPixelSize() {
    return pixel_bits;
  }

  public int getRGB(int pixelValue) {
    return (getAlpha(pixelValue) << 24) | (getRed(pixelValue) << 16) | (getGreen(pixelValue) << 8) | getBlue(pixelValue);
  }
  
  public static ColorModel getRGBdefault() {
    if(RGBdefault == null) RGBdefault = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);
    return RGBdefault;
  }

  public void finalize() {
  }
  
}
