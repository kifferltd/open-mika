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

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.util.*;

public class JPEGImageSource implements ImageProducer {

  private Vector         consumers;
  private Hashtable      properties;
  protected int[]        pixels;
  protected int          width;
  protected int          height;
  private ColorModel     colorModel;

  private native void readImage(byte[] data); 

  public JPEGImageSource(byte[] data) {
    readImage(data);
    colorModel = new DirectColorModel(32, 0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000);
    consumers = new Vector();
  }

  public void addConsumer(ImageConsumer consumer) {
    consumers.add(consumer);
  }
  
  public boolean isConsumer(ImageConsumer consumer) {
    return consumers.contains(consumer);
  }
  
  public void removeConsumer(ImageConsumer consumer) {
    consumers.remove(consumer);
  }
  
  public void requestTopDownLeftRightResend(ImageConsumer consumer) {
  }
  
  public synchronized void startProduction(ImageConsumer consumer) {
    if(consumer != null) {
      addConsumer(consumer);
    }
    Iterator iter = consumers.iterator();
    while(iter.hasNext()) {
      consumer = (ImageConsumer)iter.next();
      consumer.setDimensions(width, height);
      consumer.setProperties(properties);
      consumer.setColorModel(colorModel);
      int newpixels[] = (int[])pixels.clone();
      consumer.setPixels(0, 0, width, height, colorModel, newpixels, 0, width);    
      consumer.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
    consumers = new Vector();
  }
  
}

