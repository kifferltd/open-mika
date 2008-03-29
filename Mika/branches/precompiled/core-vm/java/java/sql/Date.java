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
** $Id: Date.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.util.*;

public class Date extends java.util.Date implements Cloneable, Comparable, Serializable {

  private static final long serialVersionUID = 1511598038487230103L;

  public Date(int year, int month, int day) {
    super(year, month, day);
  }
  
  public Date(long date) {
    super(date);
  }

  public int getHours() {
    System.out.println("[java.sql.Date] getHours is deprecated.");
    return 0;
  }
  
  public int getMinutes() {
    System.out.println("[java.sql.Date] getMinutes is deprecated.");
    return 0;
  }
  
  public int getSeconds() {
    System.out.println("[java.sql.Date] getSeconds is deprecated.");
    return 0;
  }
  
  public void setHours(int i) {
    System.out.println("[java.sql.Date] setHours is deprecated.");
  }
  
  public void setMinutes(int i) {
    System.out.println("[java.sql.Date] setMinutes is deprecated.");
  }
  
  public void setSeconds(int i) {
    System.out.println("[java.sql.Date] setSeconds is deprecated.");
  }
  
  public void setTime(long date) {
    super.setTime(date);
  }
  
  public String toString() {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(this);
  
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);

    return "" + year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day < 10 ? "0" + day : "" + day);
  }

  public static Date valueOf(String s) {
    StringTokenizer tok = new StringTokenizer(s, "-");
    
    int year = Integer.parseInt(tok.nextToken());
    int month = Integer.parseInt(tok.nextToken());
    int day = Integer.parseInt(tok.nextToken());

    return new Date(year, month, day);
  }

}

