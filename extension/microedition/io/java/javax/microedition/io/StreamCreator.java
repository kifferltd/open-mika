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

/**
** the StreamCreator class is used to provide an API for Connections implemtentations.
** insteadof creating a InputConnection, OutputConnection and StreamConnection for each protocol
** we make 3 general classes and give the a StreamCreator todo the work.  This means we only create one class
** for each protocol ...
*/
abstract class StreamCreator {

  protected boolean closed;

  StreamCreator() {}

  public void close(){
    closed = true;
  }

  public final DataInputStream openDataInputStream() throws IOException {
    return new DataInputStream(openInputStream());
  }

  public final DataOutputStream openDataOutputStream() throws IOException {
    return new DataOutputStream(openOutputStream());
  }

  public final InputStream openInputStream() throws IOException {
    if(closed){
      throw new IOException("connection is closed");
    }
    return getInputStream();
  }

  public final OutputStream openOutputStream() throws IOException {
    if(closed){
      throw new IOException("connection is closed");
    }
    return getOutputStream();
  }

  public String getEncoding(){
    return null;
  }

  public long getLength(){
    return -1;
  }

  public String getType(){
    return null;
  }

  protected abstract InputStream getInputStream() throws IOException;
  protected abstract OutputStream getOutputStream() throws IOException;

}
