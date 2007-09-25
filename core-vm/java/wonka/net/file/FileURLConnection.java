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

package wonka.net.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.FileNotFoundException;

import java.net.URL;
import java.net.URLConnection;

import java.security.Permission;

public class FileURLConnection extends URLConnection {

  public FileURLConnection(URL url) {
    super(url);
  }

  public void connect() throws IOException {
    if (url.getHost().length() > 0){
      throw new IOException("file is not on localhost -->"+url);
    }
    if(!new File(url.getFile()).isFile()){
      throw new FileNotFoundException("file "+url.getFile()+" does not exist or is no file");
    }

    connected = true;
  }    

  public Permission getPermission() throws IOException {
    //WE ONLY ALLOW READING FROM THIS FILE ... SEE ALSO POLICY
    return new FilePermission(url.getFile() , "read");
  }

  public InputStream getInputStream() throws IOException {
    if (!connected) {
      connect();
    }
    return new FileInputStream(url.getFile());
  }

  public OutputStream getOutputStream() throws IOException {
    if (!connected) {
      connect();
    }
    return new FileOutputStream(url.getFile());
  }
}
