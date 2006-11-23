/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package com.acunia.resource;

import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.MissingResourceException;

public class TimeZoneDisplayNameResourceBundle extends ResourceBundle {

  private static Hashtable timeZonesNames=null;	

  private synchronized static void createZoneNames(){
  	if (timeZonesNames==null) {
      ResourceBundle resource = new com.acunia.resource.DateFormatSymbolBundle();
      String[][] zoneStrings = (String[][])resource.getObject("zones");
  	  int length = zoneStrings.length;
  	  timeZonesNames = new Hashtable(length*2);
  	  for(int k = 0 ; k < length ; k++){
  	    timeZonesNames.put(zoneStrings[k][0], zoneStrings[k]);
  	  }
    }
  }

  /**
  ** the constructor build a hashtable with String keys and TimeZoneResource Objects as value <br>
  ** the keys are the TimeZoneIDs and the TimeZoneResource can be used to create (Simple)TimeZone Objects
  **
  */
  public TimeZoneDisplayNameResourceBundle() {
  	if(timeZonesNames==null) {
	    createZoneNames();
	  }
  }

  //required implementation of abstract methods of ResourceBundle
  protected Object handleGetObject(String key) throws MissingResourceException {
   	Object o = timeZonesNames.get(key);
   	if (o != null) {
   		return o;
   	}
   	throw new MissingResourceException("Oops, resource not found","TimeZoneDisplayResourceBundle","key");
  }

  public Enumeration getKeys() {
  	return timeZonesNames.keys();
  }
}