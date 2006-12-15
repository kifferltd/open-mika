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


package java.awt;

import java.awt.image.*;

public abstract class Image {

  public static final int SCALE_DEFAULT = 1;
  public static final int SCALE_FAST = 2;
  public static final int SCALE_SMOOTH = 4;
  public static final int SCALE_REPLICATE = 8;
  public static final int SCALE_AREA_AVERAGING = 16;

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public static final Object UndefinedProperty = new Object();

  /**
   * @status not implemented
   * @remark not implemented
   */
  public abstract void flush();

  /**
   * @status implemented
   * @remark not compliant with specification; instead of throwing a 'ClassCastException' a null pointer 
   * is returned if the image is not an off-screen image.
   */
  public abstract Graphics getGraphics();
  
  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public abstract int getHeight(ImageObserver observer);

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public abstract ImageProducer getSource();

  /**
   * @status implemented
   * @remark specifications are obscure...
   */
  public abstract Object getProperty(String name, ImageObserver observer);

  /**
   * @status implemented
   * @remark compliant with specifications
   */
  public Image getScaledInstance(int w, int h, int hints) {
    if(w < 0 && h >= 0) w = (getWidth(null) * h) / getHeight(null); 
    else if(h < 0 && w >= 0) h = (getHeight(null) * w) / getWidth(null); 
    else if(w < 0 && h < 0) {
      w = getWidth(null);
      h = getHeight(null);
    }

    // return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(this.getSource(), new ReplicateScaleFilter(w, h)));

    Image img = new Canvas().createImage(w, h);
    Graphics g = img.getGraphics();
    g.drawImage(this, 0, 0, w, h, null);
    return img;
  }

  /**
   * @status implemented
   * @remark compliant with the specifications
   */
  public abstract int getWidth(ImageObserver observer);

}
