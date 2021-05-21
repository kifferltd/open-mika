/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.util.*;

public class PNGImageSource implements ImageProducer {

  private Vector         consumers;
  private Hashtable      properties;
  protected int[]        pixels;
  protected int          width;
  protected int          height;
  private ColorModel     colorModel;

  private native void readImage(byte[] data); 

  public PNGImageSource(byte[] data) {
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
