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

