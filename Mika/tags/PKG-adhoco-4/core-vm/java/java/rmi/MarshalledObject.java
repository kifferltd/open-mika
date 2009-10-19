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

package java.rmi;

import java.rmi.server.RMIClassLoader;
import java.io.*;

public final class MarshalledObject implements Serializable {

  private static final long serialVersionUID = 8988374069173025854L;

  private byte[] objBytes;
  private byte[] locBytes;
  private int hash;

  public MarshalledObject(Object obj) throws IOException {
    if(obj != null){
      hash = obj.hashCode();
      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bas);
      out.writeObject(obj);
      out.close();
      objBytes = bas.toByteArray();
      String annotation = RMIClassLoader.getClassAnnotation(obj.getClass());
      if(annotation != null){
        bas.reset();
        out = new ObjectOutputStream(bas);
        out.writeObject(annotation);
        out.close();
        locBytes = bas.toByteArray();
      }
    }
  }
  
  public Object get() throws IOException, ClassNotFoundException {
    if(objBytes == null){
      return null;
    }
    ByteArrayInputStream bas = new ByteArrayInputStream(objBytes);
    ObjectInputStream in = (locBytes == null ? new ObjectInputStream(bas):new MarshalledObjectInputStream(bas,locBytes));
    return in.readObject();
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof MarshalledObject){
      return java.util.Arrays.equals(objBytes, ((MarshalledObject)obj).objBytes);
    }
    return false;
  }
  
  public int hashCode() {
    return hash;
  }

  static class MarshalledObjectInputStream extends ObjectInputStream {

    ClassLoader loader;

    MarshalledObjectInputStream(InputStream in, byte[] bytes) throws IOException, ClassNotFoundException {
      super(in);
      ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
      loader = RMIClassLoader.getClassLoader((String)oin.readObject());
      oin.close();
    }

    protected Class resolveClass(ObjectStreamClass osclass) throws IOException, ClassNotFoundException {
      return Class.forName(osclass.getName(), true, loader);
    }
  }

}

