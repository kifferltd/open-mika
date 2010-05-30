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
** $Id: Time.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.util.*;

public class Time extends java.util.Date implements Cloneable, Comparable, Serializable {

  private static final long serialVersionUID = 8397324403548013681L;

  public Time(int hour, int minute, int second) {
    super(1970, 1, 1, hour, minute, second);
  }
  
  public Time(long time) {
    super(time);
  }

  public int getDate() {
    System.out.println("[java.sql.Time] getDate is deprecated.");
    return 0;
  }
  
  public int getDay() {
    System.out.println("[java.sql.Time] getDay is deprecated.");
    return 0;
  }
  
  public int getMonth() {
    System.out.println("[java.sql.Time] getMonth is deprecated.");
    return 0;
  }
  
  public int getYear() {
    System.out.println("[java.sql.Time] getYear is deprecated.");
    return 0;
  }
  
  public void setDate(int i) {
    System.out.println("[java.sql.Time] setDate is deprecated.");
  }
  
  public void setMonth(int i) {
    System.out.println("[java.sql.Time] setMonth is deprecated.");
  }
  
  public void setTime(long time) {
    super.setTime(time);
  }
  
  public void setYear(int i) {
    System.out.println("[java.sql.Time] setYear is deprecated.");
  }
  
  public String toString() {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(this);
  
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int min = cal.get(Calendar.MINUTE) + 1;
    int sec = cal.get(Calendar.SECOND);

    return "" + (hour < 10 ? "0" + hour : "" + hour) + "-" +
                (min < 10 ? "0" + min : "" + min) + "-" +
                (sec < 10 ? "0" + sec : "" + sec);
  }
  
  public static Time valueOf(String s) {
    StringTokenizer tok = new StringTokenizer(s, ":");
    
    int hour = Integer.parseInt(tok.nextToken());
    int minute = Integer.parseInt(tok.nextToken());
    int second = Integer.parseInt(tok.nextToken());

    return new Time(hour, minute, second);
  }

}

