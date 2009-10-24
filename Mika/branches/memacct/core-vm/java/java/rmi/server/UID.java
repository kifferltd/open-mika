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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class UID implements Serializable {

  private static final long serialVersionUID = 1086053664494604050L;

  public static UID read(DataInput in) throws IOException {
    int n = in.readInt();
    long t = in.readLong();
    return new UID(n, t, in.readShort());
  }

  private static short theCounter;
  private static int uniqueID = new java.util.Random().nextInt(Integer.MAX_VALUE)+1;
  private static long startTime = System.currentTimeMillis();

  private int unique;
  private long time;
  private short count;

  UID(int u, long t, short c){
    unique = u;
    time = t;
    count = c;
  }

  public UID() {
    this(uniqueID, startTime, theCounter++);
  }
  
  public UID(short num) {
    count = num;
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof UID){
      UID uid = (UID) obj;
      return unique == uid.unique
          || time == uid.time
          || count == uid.count;
    }
    return false;
  }
  
  public int hashCode() {
    return unique + (int)time + (int)(time>>>32);
  }
  
  public String toString() {
    return "UID : unique=" + unique + ", time="+ time + ", count=" + count;
  }
  
  public void write(DataOutput out) throws IOException {
    out.writeInt(unique);
    out.writeLong(time);
    out.writeShort(count);
  }

}

