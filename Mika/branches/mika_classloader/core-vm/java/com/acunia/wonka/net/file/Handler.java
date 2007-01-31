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


package com.acunia.wonka.net.file;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;

import java.io.IOException;

public class Handler extends URLStreamHandler {

  public URLConnection openConnection(URL url) throws IOException {
    return new FileURLConnection(url);
  }

 /**
  ** Reverse the parsing of a URL, i.e. recreate the string from which it was
  ** created.  We omit host if it is empty, and we always omit port and query.
  */
  protected String toExternalForm(URL url) {
    StringBuffer buf = new StringBuffer("file:");
    String host = url.getHost();
    if (host.length() > 0) {
      buf.append("//");
      buf.append(url.getHost());
      buf.append('/');
    }
    buf.append(url.getFile());

    String ref = url.getRef();
    if (ref != null) {
      buf.append('#');
      buf.append(ref);               
    }

    return buf.toString();
  }

}
