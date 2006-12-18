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

