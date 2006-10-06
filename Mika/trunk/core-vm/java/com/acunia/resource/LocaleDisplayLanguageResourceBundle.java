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
import java.util.Properties;
import java.util.Enumeration;
import java.util.MissingResourceException;

public class LocaleDisplayLanguageResourceBundle extends ResourceBundle {


     private Properties language;	

/**
** the constructor build a hashtable with String keys and TimeZoneResource Objects as value <br>
** the keys are the TimeZoneIDs and the TimeZoneResource can be used to create (Simple)TimeZone Objects
**
*/
     public LocaleDisplayLanguageResourceBundle() {
	  language = new Properties();
          language.put("fr", "French");
          language.put("en", "English");
          language.put("zh", "Chinese");
          language.put("nl", "Dutch");
          language.put("de", "German");
          language.put("it", "Italian");
          language.put("ja", "Japanese");
          language.put("ko", "Korean");
	  //many more to come ...
     }

// required implementation of abstract methods of ResourceBundle

     protected Object handleGetObject(String key) throws MissingResourceException {
      	Object o = language.get(key);
      	if (o != null) {
      		return o;
      	}
      	throw new MissingResourceException("Oops, resource not found","LocaleDisplayLanguageResourceBundle","key");
     }


     public Enumeration getKeys() {
     	return language.keys();
     }

}