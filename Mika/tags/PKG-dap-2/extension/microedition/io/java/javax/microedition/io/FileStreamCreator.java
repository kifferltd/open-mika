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

class FileStreamCreator extends StreamCreator{

  private InputStream in;
  private OutputStream out;
  private String file;
  private boolean append;

  FileStreamCreator(String name, boolean read_only) throws ConnectionNotFoundException {
    int index = name.indexOf(';');
    if(index != -1){
      append = (name.indexOf("append=true",index) != -1);
      //PARSE for other options ...
      file = name.substring(0,index);
    }
    else {
      file = name;
    }

    File f = new File(file);
    if(!f.exists()){
      if(read_only){
        throw new ConnectionNotFoundException(file+" does not exist");
      }
      try {
        f.createNewFile();
      }
      catch(IOException ioe){
        throw new ConnectionNotFoundException("cannot create "+file);
      }
    }
    else if(!f.isFile()){
      throw new ConnectionNotFoundException(file+" is not a file");
    }
  }

  public void close(){
    closed = true;
    out = null;
    in = null;
  }

  protected InputStream getInputStream() throws IOException {
    if(in == null){
      in = new FileInputStream(file);
    }
    return in;
  }

  protected OutputStream getOutputStream() throws IOException {
    if(out == null){
      out = new FileOutputStream(file, append);
    }
    return out;
  }

}