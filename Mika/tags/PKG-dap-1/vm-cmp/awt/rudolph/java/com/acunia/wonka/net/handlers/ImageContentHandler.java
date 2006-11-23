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

package com.acunia.wonka.net.handlers;

import java.net.*;
import java.io.*;

public class ImageContentHandler extends ContentHandler {

  public Object getContent(URLConnection urlconn) throws IOException {
    String type = urlconn.getContentType();
    byte[] bytes;
    
    try {
      int len = urlconn.getContentLength();
      if(len > -1){
        bytes = new byte[len];
        InputStream in = urlconn.getInputStream();
        int rd = in.read(bytes,0,len);
        while(rd < len){
          int b = in.read(bytes,rd,len -rd);
          if(b == -1){
            byte[] old = bytes;
            bytes = new byte[rd];
            System.arraycopy(old,0,bytes,0,rd);
          }
          rd += b;
        }
      }
      else {
        ByteArrayOutputStream bas = new ByteArrayOutputStream(2048);  
        InputStream in = urlconn.getInputStream();
        bytes = new byte[1024];
        len = in.read(bytes, 0, 1024);
        while (len != -1) {
          bas.write(bytes,0,len);
          len = in.read(bytes, 0, 1024);
        }
        bytes = bas.toByteArray();
      }
    }  
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    if(type.equals("image/gif")) {
      return new com.acunia.wonka.rudolph.GIFImageSource(bytes);
    }
    else if(type.equals("image/jpg") || type.equals("image/jpeg")) {
      return new com.acunia.wonka.rudolph.JPEGImageSource(bytes);
    }
    else if(type.equals("image/png")) {
      return new com.acunia.wonka.rudolph.PNGImageSource(bytes);
    }

    return null;
  }

}

