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

