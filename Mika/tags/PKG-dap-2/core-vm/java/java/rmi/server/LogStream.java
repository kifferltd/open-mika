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

import java.io.*;

public class LogStream extends PrintStream {

  public final static int BRIEF = 10;
  public final static int SILENT = 0;
  public final static int VERBOSE = 20;

  private LogStream() {
    super(null);
  }
                            
  public static PrintStream getDefaultStream() {
    System.out.println("[LogStream.getDefaultStream] Not implemented...");
    return null;
  }
  
  public OutputStream getOutputStream() {
    System.out.println("[LogStream.getOutputStream] Not implemented...");
    return null;
  }
  
  public static LogStream log(String name) {
    System.out.println("[LogStream.log] Not implemented...");
    return null;
  }
  
  public static int parseLevel(String s) {
    System.out.println("[LogStream.parseLevel] Not implemented...");
    return 0;
  }
  
  public static void setDefaultStream(PrintStream newDefault) {
    System.out.println("[LogStream.setDefaultStream] Not implemented...");
  }
  
  public void setOutputStream(OutputStream out) {
    System.out.println("[LogStream.setOutputStream] Not implemented...");
  }
  
  public String toString() {
    System.out.println("[LogStream.toString] Not implemented...");
    return null;
  }
  
  public void write(byte[] b, int off, int len) {
    System.out.println("[LogStream.write] Not implemented...");
  }
  
  public void write(int b) {
    System.out.println("[LogStream.write] Not implemented...");
  }

}

