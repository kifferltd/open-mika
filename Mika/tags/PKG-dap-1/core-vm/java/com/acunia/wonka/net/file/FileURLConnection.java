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
