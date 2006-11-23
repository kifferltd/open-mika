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