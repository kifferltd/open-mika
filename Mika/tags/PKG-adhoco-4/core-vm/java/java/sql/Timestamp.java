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

/*
** $Id: Timestamp.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.util.*;

public class Timestamp extends java.util.Date implements Cloneable, Comparable, Serializable {

  private static final long serialVersionUID = 2745179027874758501L;

  private int nanos;

  public Timestamp(int year, int month, int date, int hour, int minute, int second, int nanos) {
    super(year, month, date, hour, minute, second);
    this.nanos = nanos;
  }
  
  public Timestamp(long time) {
    super(time - (time % 1000));
    nanos = (int)(time % 1000) * 1000000;
  }

  public boolean after(Timestamp ts) {
    return ((getTime() * 1000000) + nanos) > ((ts.getTime() * 1000000) + nanos);
  }
  
  public boolean before(Timestamp ts) {
    return ((getTime() * 1000000) + nanos) < ((ts.getTime() * 1000000) + nanos);
  }
  
  public boolean equals(Object ts) {
    if(ts instanceof Timestamp) {
      return ((getTime() == ((Timestamp)ts).getTime()) && nanos == ((Timestamp)ts).getNanos());
    }
    else {
      return false;
    }
  }
  
  public boolean equals(Timestamp ts) {
    return (getTime() == ts.getTime()) && (nanos == ts.getNanos());
  }
  
  public int getNanos() {
    return nanos;
  }
  
  public void setNanos(int n) {
    nanos = n;
  }
  
  public String toString() {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(this);
  
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int min = cal.get(Calendar.MINUTE);
    int sec = cal.get(Calendar.SECOND);

    return "" + year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day < 10 ? "0" + day : "" + day) + " " +
                (hour < 10 ? "0" + hour : "" + hour) + "-" +
                (min < 10 ? "0" + min : "" + min) + "-" +
                (sec < 10 ? "0" + (float)(sec + (nanos / 1000000000)): "" + (float)(sec + (nanos / 1000000000)));
  }

  public static Timestamp valueOf(String s) {
    StringTokenizer tok = new StringTokenizer(s, " ");
    String date = tok.nextToken();
    String time = tok.nextToken();
    
    tok = new StringTokenizer(date, "-");
    int year = Integer.parseInt(tok.nextToken());
    int month = Integer.parseInt(tok.nextToken());
    int day = Integer.parseInt(tok.nextToken());

    tok = new StringTokenizer(time, ":");
    int hour = Integer.parseInt(tok.nextToken());
    int minute = Integer.parseInt(tok.nextToken());
    double second = Double.parseDouble(tok.nextToken());

    return new Timestamp(year, month, day, hour, minute, (int)Math.floor(second), (int)((second - Math.floor(second)) * 1000000000));
  }

}

