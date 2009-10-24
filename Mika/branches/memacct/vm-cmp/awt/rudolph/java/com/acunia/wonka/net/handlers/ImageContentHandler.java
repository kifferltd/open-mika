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

