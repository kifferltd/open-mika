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


package com.acunia.wonka.test.awt;

import java.awt.Panel;
import java.util.Date;
import java.util.TimeZone;
import java.io.*;

class VTELogger implements Runnable {

// static Logging section ...
   private static Runtime rt = Runtime.getRuntime();
   private static java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy/MM/dd\tkk:mm:ss zzz");
   private static java.text.FieldPosition position = new java.text.FieldPosition(0);

   static {
     try {
       dateFormatter.setTimeZone(TimeZone.getTimeZone("ECT"));
     }
     catch(RuntimeException rt){}
   }

  private static void formatAndLogMessage(String message, String event, boolean doWrite){
    StringBuffer b = new StringBuffer(256);
    dateFormatter.format(new Date(), b, position);
    b.append("\nEvent: ");
    b.append(event);
    long free = rt.freeMemory();
    long total = rt.totalMemory();
    b.append("\nMemory status: used ");
    b.append((total - free));
    b.append(" out of total ");
    b.append(total);
    b.append("\nLog message:\n");
    b.append(message);
    b.append("\nEnd of log message\n\n");

    if(doWrite){
      writeLogMessage(b.toString());
    }
    else {
      new VTELogger(b.toString());
    }
  }


/**
** if doWrite is true this method will not start a thread new thread but call writeLogMessage itself
** this method should be be called with doWrite set to false if called from within the rudolph thread
*/
  static void reportException(String msg, Throwable t, boolean doWrite){
    System.out.println("reportException is called");
    StringBuffer b = new StringBuffer(256);
    b.append(msg);
    b.append('\n');
    b.append(logStackTrace(t));

    formatAndLogMessage(b.toString(), "reporting Exception", doWrite);
  }

  static void logMessage(String message, boolean doWrite){
    System.out.println("logMessage is called");
    formatAndLogMessage(message, "reporting internal VTE mesage", doWrite);
  }

  static void logTestResult(boolean r, String name, VisualTest vtest, Panel testPanel){
    System.out.println("logTestResult is called");
    StringBuffer b = new StringBuffer(256);
    b.append(vtest.getClass().getName());
    b.append("\nMessage: ");
	  b.append(vtest.getLogInfo(testPanel,r));

    formatAndLogMessage(b.toString(), "reporting result for test "+name+" "+(r ? "PASS" : "FAIL") , false);
  }

  static void log(String log, VisualTest v) {
    System.out.println("log is called");
    StringBuffer b = new StringBuffer(256);
    b.append(v.getClass().getName());
    b.append('\n');
	  b.append(log);

    formatAndLogMessage(b.toString(), "logging message from test", false);
  }	

  static void logException(String log, VisualTest v, Throwable ex) {
    System.out.println("logException is called");
    StringBuffer b = new StringBuffer(256);
    b.append(v.getClass().getName());
    b.append("\nMessage: ");
	  b.append(log);
    b.append("\n");
    b.append(logStackTrace(ex));

    formatAndLogMessage(b.toString(), "reporting Exception in test", false);
     	
  }	

  private static char[] logStackTrace(Throwable t){
    try {
      CharArrayWriter cw = new CharArrayWriter();
      PrintWriter pw = new PrintWriter(cw);
      t.printStackTrace(pw);
      pw.close();
      return cw.toCharArray();
    }
    catch(Exception e){
      return ("FAILED TO PRINT A STACKTRACE OF "+t+"\n\tDUE TO :"+e+"\n").toCharArray();
    }
  }

  private synchronized static void writeLogMessage(String error) {
     FileWriter fw = null;
     try {
    	 fw = new FileWriter("vte.log", true);
    	 fw.write(error);
     }
     catch (IOException ioe) { }
     finally { 	
    	 if (fw != null) {
    	   try { fw.flush(); }
    	   catch (IOException e) { }
    	   try { fw.close(); }
    	   catch (IOException e) { }    	      		
    	 }
    }  	     	
  }

  private String log;
    	
  private VTELogger(String l) {
    log = l;
  	new Thread(VisualTestEngine.tg, this, "Logger thread " + this).start();
  }    	    	
    	    	
  public void run() {
    writeLogMessage(log);
  }	

}
