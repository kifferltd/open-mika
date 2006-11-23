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


package javax.microedition.io;

import java.io.*;
import java.net.*;

class URLStreamCreator extends StreamCreator {

  protected URLConnection connection;

  protected URLStreamCreator(String name) throws ConnectionNotFoundException {
    int index = name.indexOf(';');
    int timeout = 0;
    if(index != -1){
      //Todo PARSE for options ...
      name = name.substring(0,index);
    }
    try {
      connection = new URL(name).openConnection();    }
    catch(Exception e){
      throw new ConnectionNotFoundException("unable to create Connection");
    }
  }

  protected InputStream getInputStream() throws IOException {
    return connection.getInputStream();
  }

  protected OutputStream getOutputStream() throws IOException {
    return connection.getOutputStream();
  }

  public String getEncoding(){
    return connection.getContentEncoding();
  }

  public long getLength(){
    return connection.getContentLength();
  }

  public String getType(){
    return connection.getContentType();
  }
}