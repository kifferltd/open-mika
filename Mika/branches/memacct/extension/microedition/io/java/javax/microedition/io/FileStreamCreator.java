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
