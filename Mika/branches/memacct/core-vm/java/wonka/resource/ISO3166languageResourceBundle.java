/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

package wonka.resource;

import java.util.ResourceBundle;
import java.util.Properties;
import java.util.Enumeration;
import java.util.MissingResourceException;

public class ISO3166languageResourceBundle extends ResourceBundle {


     private Properties ISO3languageCodes;	

/**
** the constructor build a hashtable with String keys and TimeZoneResource Objects as value <br>
** the keys are the TimeZoneIDs and the TimeZoneResource can be used to create (Simple)TimeZone Objects
**
*/
     public ISO3166languageResourceBundle() {
	  ISO3languageCodes = new Properties();
          ISO3languageCodes.put("fr", "fra");
          ISO3languageCodes.put("en", "eng");
          ISO3languageCodes.put("zh", "chs");
          ISO3languageCodes.put("de", "deu");
          ISO3languageCodes.put("it", "ita");
          ISO3languageCodes.put("ja", "jpn");
          ISO3languageCodes.put("ko", "kor");
          ISO3languageCodes.put("en", "eng");
	  ISO3languageCodes.put("nl", "nld");
	  //many more to come ...
	  try {
	    ISO3languageCodes.load(getClass().getResourceAsStream("/ISO3languageCodes.properties"));
	  } catch (Exception e) {
	  }
     }

// required implementation of abstract methods of ResourceBundle

     protected Object handleGetObject(String key) throws MissingResourceException {
      	Object o = ISO3languageCodes.get(key);
      	if (o != null) {
      		return o;
      	}
      	throw new MissingResourceException("Oops, resource not found","ISO3166languageResourceBundle","key");
     }

     public Enumeration getKeys() {
     	return ISO3languageCodes.keys();
     }

}
