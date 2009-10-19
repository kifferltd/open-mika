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

package wonka.rmi;

import java.io.*;
import java.rmi.server.RMIClassLoader;

public class RMIObjectInputStream extends ObjectInputStream {

  private PushbackInputStream in;

  public RMIObjectInputStream(PushbackInputStream in) throws IOException {
    super(in);
    this.in = in;
  }

  protected Class resolveClass(ObjectStreamClass osclass) throws IOException, ClassNotFoundException {
    String name = osclass.getName();
    int rd = in.read();
    if(rd != TC_ENDBLOCKDATA){
      if(RMIConnection.DEBUG < 5) {System.out.println(osclass+" was annotated rd = "+Integer.toHexString(rd));}
      if(rd == -1){
        throw new EOFException();
      }
      if(rd != TC_NULL){
        in.unread(rd);
        if(rd == TC_REFERENCE || rd == TC_STRING){
          Object o = readObject();
          if(RMIConnection.DEBUG < 8) {System.out.println(osclass+" was annotated with '"+o+"' of "+o.getClass());}
          return RMIClassLoader.loadClass((String)o, name);
        }
      }
    }
    else {
      in.unread(rd);
      if(RMIConnection.DEBUG < 5) {System.out.println("\n\n"+osclass+" was not annotated rd = "+Integer.toHexString(rd)+"\n\n");}

    }

    return Class.forName(name,true, ClassLoader.getSystemClassLoader());
  }
}

