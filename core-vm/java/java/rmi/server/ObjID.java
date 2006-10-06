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

package java.rmi.server;

import java.io.Serializable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

public final class ObjID implements Serializable {

  public final static int ACTIVATOR_ID = 1;
  public final static int DGC_ID = 2;
  public final static int REGISTRY_ID = 0;

  public static ObjID read(ObjectInput in) throws IOException {
    long num = in.readLong();
    return new ObjID(num, UID.read(in));
  }

  private static final long serialVersionUID = -6386392263968365220L;

  private static final UID thisVMSpace = new UID();
  private static long theCounter = 0;

  private long objNum;
  private UID space;

  ObjID(long num, UID uid){
    objNum = num;
    space = uid;
  }

  public ObjID(){
    synchronized (thisVMSpace){
      space = thisVMSpace;
      objNum = theCounter++;
    }
  }
  
  public ObjID(int num) {
    this(num, new UID(0,0L,(short)0));
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof ObjID){
      ObjID id = (ObjID)obj;
      return id.objNum == objNum && space.equals(id.space);
    }
    return false;
  }
  
  public int hashCode() {
    return (int)objNum ^ (int)(objNum>>>32) + space.hashCode();
  }
  
  public String toString() {
    String result = "ObjID: objNum="+objNum;
    if(!space.equals(thisVMSpace)){
      result = result + ", space="+space;
    }
    return result;
  }
  
  public void write(ObjectOutput out) throws IOException {
    out.writeLong(objNum);
    space.write(out);
  }

}

