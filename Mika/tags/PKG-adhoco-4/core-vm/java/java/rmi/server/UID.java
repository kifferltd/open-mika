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

