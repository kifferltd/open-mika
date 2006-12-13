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

public interface ImageConsumer {

  public static final int IMAGEERROR        = 0x00000001;
  public static final int SINGLEFRAMEDONE   = 0x00000002;
  public static final int STATICIMAGEDONE   = 0x00000008;
  public static final int IMAGEABORTED      = 0x00000004;
  
  public static final int RANDOMPIXELORDER  = 0x00000001;
  public static final int TOPDOWNLEFTRIGHT  = 0x00000002;
  public static final int COMPLETESCANLINES = 0x00000004;
  public static final int SINGLEPASS        = 0x00000008;
  public static final int SINGLEFRAME       = 0x00000010;
  
  public void imageComplete(int status);
  
  public void setColorModel(ColorModel model);
  
  public void setDimensions(int w, int h);
  
  public void setHints(int hints);
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scansize);

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scansize);

  public void setProperties(java.util.Hashtable properties);  

}
