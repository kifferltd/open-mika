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

import java.util.Hashtable;

public class ImageFilter implements ImageConsumer, Cloneable {

  protected  ImageConsumer consumer;

  public ImageFilter() {
  }
  
  public ImageFilter getFilterInstance(ImageConsumer ic) {
    ImageFilter filter = null;
    try {
      filter = (ImageFilter)clone();
      filter.consumer = ic;
    } catch(Exception e) { }
    return filter;
  }
  
  public void imageComplete(int status) {
    if(consumer != null) consumer.imageComplete(status);
  }
  
  public void resendTopDownLeftRight(ImageProducer ip) {
  }
  
  public void setColorModel(ColorModel model) {
    if(consumer != null) consumer.setColorModel(model);
  }
  
  public void setDimensions(int width, int height) {
    if(consumer != null) consumer.setDimensions(width, height);
  }
  
  public void setHints(int hints) {
    if(consumer != null) consumer.setHints(hints);
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    if(consumer != null) consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    if(consumer != null) consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
  }
  
  public void setProperties(Hashtable props) {
    if(consumer != null) consumer.setProperties(props);
  }
  
}

