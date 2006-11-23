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
** $Id: DriverManager.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.util.*;

public class DriverManager {

  private static Vector drivers = new Vector();
  private static int loginTimeout;
  private static PrintStream outStream;
  private static PrintWriter outWriter;

  /*
  ** TODO: Protect the drivers vector from simultanious accesses.
  */

  public static void registerDriver(Driver driver) throws SQLException {
    drivers.add(driver);
  }
  
  public static void deregisterDriver(Driver driver) throws SQLException {
    drivers.remove(driver);
  }
 
  public static synchronized Driver getDriver(String url) throws SQLException {
    Iterator iter = drivers.iterator();
    Driver   result = null;
    
    while(iter.hasNext() && result == null) {
      Driver driver = (Driver)iter.next();
      if(driver.acceptsURL(url)) {
        result = driver;
      }
    }

    return result;
  }
  
  public static Enumeration getDrivers() {
    return drivers.elements();
  }
  
 
  public static Connection getConnection(String url) throws SQLException {
    return getConnection(url, null);
  }
  
  public static synchronized Connection getConnection(String url, Properties info) throws SQLException {
    Driver driver = getDriver(url);
    if(driver != null) {
      return driver.connect(url, info);
    }
    return null;
  }
  
  public static Connection getConnection(String url, String user, String password) throws SQLException {
    Properties p = new Properties();
    p.setProperty("user", user);
    p.setProperty("password", password);
    return getConnection(url,p);
  }
 
  
  public static int getLoginTimeout() {
    return loginTimeout;
  }
  
  public static void setLoginTimeout(int seconds) {
    loginTimeout = seconds;
  }
 
 
  public static PrintStream getLogStream() {
    return outStream;
  }
  
  public static void setLogStream(PrintStream out) {
    outStream = out;
  }
  
  public static PrintWriter getLogWriter() {
    return outWriter;
  }
  
  public static void setLogWriter(PrintWriter out) {
    outWriter = out;
  }

  public static void println(String message) {
    if(outWriter != null) {
      outWriter.println(message);
    }
    else {
      System.out.println(message);
    }
  }
  
}

