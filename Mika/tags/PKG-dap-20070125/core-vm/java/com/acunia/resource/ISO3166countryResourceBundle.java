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

public class ISO3166countryResourceBundle extends ResourceBundle {


     private Properties ISO3countryCodes;	

/**
** the constructor build a hashtable with String keys and TimeZoneResource Objects as value <br>
** the keys are the TimeZoneIDs and the TimeZoneResource can be used to create (Simple)TimeZone Objects
**
*/
     public ISO3166countryResourceBundle() {
	  ISO3countryCodes = new Properties();
          ISO3countryCodes.put("CA", "CAN");
          ISO3countryCodes.put("CN", "CHN");
          ISO3countryCodes.put("US", "USA");
          ISO3countryCodes.put("DE", "DEU");
          ISO3countryCodes.put("IT", "ITA");
          ISO3countryCodes.put("BE", "BEL");
          ISO3countryCodes.put("JP", "JPN");
          ISO3countryCodes.put("FR", "FRA");
          ISO3countryCodes.put("KR", "KOR");
          ISO3countryCodes.put("GB", "GBR");
          ISO3countryCodes.put("TW", "TWN");
	  //many more to come ...
     }

// required implementation of abstract methods of ResourceBundle

     protected Object handleGetObject(String key) throws MissingResourceException {
      	Object o =  ISO3countryCodes.get(key);
      	if (o != null) return o;
      	throw new MissingResourceException("Oops resource not found","ISO3166countryResourceBundle",key);
     }

     public Enumeration getKeys() {
     	return ISO3countryCodes.keys();
     }

}