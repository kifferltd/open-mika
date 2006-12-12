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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.net;

import java.net.ContentHandler;
import java.net.ContentHandlerFactory;

public class DefaultContentHandlerFactory implements ContentHandlerFactory {

  private static Class ich;

  public ContentHandler createContentHandler(String contentType) {
    if(contentType.startsWith("image/")) {
      if (ich == null) {
        try {
          ich = Class.forName("com.acunia.wonka.net.handlers.ImageContentHandler");
        }
        catch (ClassNotFoundException cnfe) {
          // No ImageContentHandler - probably non-AWT build
          return null;
        }
      }

      try {
        return (ContentHandler)ich.newInstance();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}

