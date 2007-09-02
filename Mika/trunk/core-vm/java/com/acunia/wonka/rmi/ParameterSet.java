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


package com.acunia.wonka.rmi;

import java.lang.reflect.Method;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

final class ParameterSet {

  static final ParameterSet NOARGS_SET = new ParameterSet(new Object[0]);

  private Method method;
  private Object[] params;

  ParameterSet(Method method, Object[] params){
    this.method = method;
    this.params = params;
  }

  ParameterSet(Object[] params){
    this.params = params;
  }

  void writeData(ObjectOutputStream out) throws IOException {
    if(method != null){
      Class[] classes = method.getParameterTypes();
      for(int i = 0 ; i < classes.length ; i++){
        if(classes[i].isPrimitive()){
          writePrimitive(classes[i], params[i], out);
        }
        else {
          out.writeObject(params[i]);
        }
      }
    }
    else {
      for(int i = 0 ; i < params.length ; i++){
        out.writeObject(params[i]);
      }
    }
  }

  static void writePrimitive(Class primitive, Object o, ObjectOutputStream oout) throws IOException {
    if(primitive == Integer.TYPE){
      oout.writeInt(((Integer)o).intValue());
    }
    else if(primitive == Long.TYPE){
      oout.writeLong(((Long)o).longValue());
    }
    else if(primitive == Double.TYPE){
      oout.writeDouble(((Double)o).doubleValue());
    }
    else if(primitive == Float.TYPE){
      oout.writeFloat(((Float)o).floatValue());
    }
    else if(primitive == Short.TYPE){
      oout.writeShort(((Short)o).shortValue());
    }
    else if(primitive == Byte.TYPE){
      oout.writeByte(((Byte)o).byteValue());
    }
    else if(primitive == Character.TYPE){
      oout.writeChar(((Character)o).charValue());
    }
    else if(primitive == Boolean.TYPE){
      oout.writeBoolean(((Boolean)o).booleanValue());
    }
  }

  static Object readPrimitive(Class primitive, ObjectInputStream oin)  throws IOException {
    if(primitive == Integer.TYPE){
      return new Integer(oin.readInt());
    }
    else if(primitive == Long.TYPE){
      return new Long(oin.readLong());
    }
    else if(primitive == Double.TYPE){
      return new Double(oin.readDouble());
    }
    else if(primitive == Float.TYPE){
      return new Float(oin.readFloat());
    }
    else if(primitive == Short.TYPE){
      return new Short(oin.readShort());
    }
    else if(primitive == Byte.TYPE){
      return new Byte(oin.readByte());
    }
    else if(primitive == Character.TYPE){
      return new Character(oin.readChar());
    }
    else if(primitive == Boolean.TYPE){
      return new Boolean(oin.readBoolean());
    }
    throw new IOException("Cannot read of variable of type void");
  }
}
