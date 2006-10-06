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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: GetSystemProperty.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.net;

import wonka.vm.SecurityConfiguration;
import java.security.AccessController;

/*
** Package-visible class used to get system properties with full privileges.
*/

class GetSystemProperty implements java.security.PrivilegedAction {

  static final String IMPL_PREFIX;
  static final String PROTOCOL_HANDLER_PKGS;

  private String key;
  private String dflt;
  private String value;

  /**
   ** Our static initialiser fetches the system property which
   ** everybody in this package seems to need ...
   */
  static {
    GetSystemProperty gsp;

    if (SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      gsp = new GetSystemProperty("impl.prefix","Plain");
      AccessController.doPrivileged(gsp);
      IMPL_PREFIX = gsp.get();

      gsp = new GetSystemProperty("java.protocol.handler.pkgs","com.acunia.wonka.net");
      AccessController.doPrivileged(gsp);
      PROTOCOL_HANDLER_PKGS = gsp.get();
    }
    else {
      IMPL_PREFIX = System.getProperty("impl.prefix", "Plain");
      PROTOCOL_HANDLER_PKGS = System.getProperty("java.protocol.handler.pkgs", "com.acunia.wonka.net");
    }
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
