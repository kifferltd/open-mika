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

import java.awt.Image;

public interface ImageObserver {

  public static final int WIDTH = 1;
  public static final int HEIGHT = 2;
  public static final int PROPERTIES = 4;
  public static final int SOMEBITS = 8;
  public static final int FRAMEBITS = 16;
  public static final int ALLBITS = 32;
  public static final int ERROR = 64;
  public static final int ABORT = 128;
 
  public boolean imageUpdate(Image img, int infofloat, int x, int y, int width, int height);
}
