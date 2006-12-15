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

