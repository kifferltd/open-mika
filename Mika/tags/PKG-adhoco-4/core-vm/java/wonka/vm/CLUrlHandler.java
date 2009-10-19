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

/*
** $Id: CLUrlHandler.java,v 1.2 2006/05/16 08:24:41 cvs Exp $
*/

package wonka.vm;

import java.io.*;
import java.net.URL;

public class CLUrlHandler extends ClassLoaderURLHandler {

  protected URL url;

  CLUrlHandler(URL file){
    if(file.getFile().endsWith(".jar")){
      try {
        url = new URL("jar:"+file+"!/");
      }
      catch(Exception e){}
    }
    else {
      url = file;
    }
  }

  public byte[] getByteArray(String resource){
    try {
      URL res = new URL(url, resource);
      InputStream in = res.openStream();
      byte[] bytes = new byte[1024];
      ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
      int j = in.read(bytes,0,1024);
      while(j != -1){
        out.write(bytes,0,j);
        j = in.read(bytes,0,1024);
      }
      return out.toByteArray();
    } catch(Exception e){}

    return null;
  }

  public InputStream getInputStream(String resource){
    try {
      URL res = new URL(url, resource);
      return res.openStream();
    } catch(Exception e){}

    return null;
  }

  public URL getURL(String resource){
    try {
      URL res = new URL(url, resource);
      res.openStream();
      return res;
    } catch(Exception e){}

    return null;
  }

  public String toString() {
    return "CLUrlHandler for URL " + url;
  }
}
