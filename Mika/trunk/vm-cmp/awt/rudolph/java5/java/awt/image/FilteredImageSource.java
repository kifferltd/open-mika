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

public class FilteredImageSource implements ImageProducer {

  private ImageProducer  producer;
  private ImageFilter    filter;
  private Vector         consumers;
  private Hashtable      properties;

  public FilteredImageSource(ImageProducer producer, ImageFilter filter) {
    this.producer = producer;
    this.filter = filter;
    consumers = new Vector();
  }

  public void addConsumer(ImageConsumer ic) {
    consumers.add(ic);
  }
  
  public boolean isConsumer(ImageConsumer ic) {
    return consumers.contains(ic);
  }
  
  public void removeConsumer(ImageConsumer ic) {
    consumers.remove(ic);
  }
  
  public void requestTopDownLeftRightResend(ImageConsumer ic) {
  }
  
  public synchronized void startProduction(ImageConsumer ic) {
    if(ic != null) {
      addConsumer(ic);
    }
    Iterator iter = consumers.iterator();
    while(iter.hasNext()) {
      producer.startProduction( (ImageConsumer)filter.getFilterInstance( (ImageConsumer)(iter.next()) ) );
    }
    consumers = new Vector();
  }
  
}

