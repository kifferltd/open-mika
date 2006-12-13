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

