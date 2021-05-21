/**************************************************************************
* Copyright (c) 2012 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions the names of other   *
*    contributors may be used to endorse or promote products derived from *
*    this software without specific prior written  permission.            *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER  CONTRIBUTORS    *
* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,     *
* OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT    *
* OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR      *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  LIABILITY,  *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

package java.awt;

import wonka.vm.SecurityConfiguration;
import java.security.AccessController;

/*
** Package-visible class used to get system properties with full privileges.
*/

class GetSystemProperty implements java.security.PrivilegedAction {

  static final Boolean HEADLESS;
  static final Boolean INCREMENTAL_IMAGE_UPDATE;
  static final String  TOOLKIT;

  private String key;
  private String dflt;
  private String value;

  /**
   ** Our static initialiser fetches the java.awt.headless and 
   ** awt.image.incrementaldraw system properties.
   */
  static {
    GetSystemProperty gsp;
    GetSystemProperty idraw;
    GetSystemProperty tk;
    String idrawPropertyValue;
    String gspPropertyValue;
    String tkPropertyValue;

    if (SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      gsp = new GetSystemProperty("awt.headless","\n");
      idraw = new GetSystemProperty("awt.image.incrementaldraw","\n");
      tk = new GetSystemProperty("awt.toolkit","\n");
      AccessController.doPrivileged(gsp);
      AccessController.doPrivileged(idraw);
      AccessController.doPrivileged(tk);
      gspPropertyValue = gsp.get();
      idrawPropertyValue = idraw.get();
      tkPropertyValue = tk.get();

      gsp = null;
      idraw = null;
      tk = null;
    }
    else {
      gspPropertyValue = System.getProperty("awt.headless", "false");
      idrawPropertyValue = System.getProperty("awt.image.incrementaldraw", "false");
      tkPropertyValue = System.getProperty("awt.toolkit");
    }
    HEADLESS = Boolean.valueOf(gspPropertyValue);
    INCREMENTAL_IMAGE_UPDATE = Boolean.valueOf(idrawPropertyValue);
    TOOLKIT = tkPropertyValue;
  }

  /**
   ** Constructor which takes a key and a default value.
   */
  public GetSystemProperty(String key, String dflt) {
    this.key = key;
    this.dflt = dflt;
  }

  /**
   ** Constructor which just takes a key.
   */
  public GetSystemProperty(String key) {
    this.key = key;
  }

  /**
   ** This gets called by doPrivileged().
   */
  public Object run() {
    if (dflt == null) {
      value = System.getProperty(key);
    }
    else {
      value = System.getProperty(key, dflt);
    }

    return null;
  }

  /**
   ** Return the value to the caller.
   */
  public String get() {
    return value;
  }

}
