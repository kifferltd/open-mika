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

