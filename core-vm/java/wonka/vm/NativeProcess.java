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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: NativeProcess.java,v 1.4 2006/10/04 14:24:15 cvsroot Exp $
*/

package wonka.vm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class NativeProcess extends Process {

  private static final ProcessMonitor monitor = new ProcessMonitor();
  private static final int LAUNCHING = 10001;
  
  final static int STILL_RUNNING = 10000;
  final static int ERROR = 10002;

  private OutputStream out;
  private InputStream in;
  private InputStream err;
  ProcessInfo info;

  String[] cmdarray;
  String[] envp;
  String path;
  int returnvalue;
  Throwable excecption; 

  public NativeProcess(String[] cmdarray, String[] envp, String string) throws IOException {
    this.cmdarray = cmdarray;
    this.envp = envp;
    this.path = string;
    returnvalue = LAUNCHING;
    synchronized(this) {
      NativeProcess.monitor.exec(this);
      do {
        try {
          this.wait();
        } catch (InterruptedException e) {
        }
      }while(returnvalue == LAUNCHING);
    }
    if(returnvalue == ERROR) {
      if(excecption instanceof RuntimeException) {
        excecption.fillInStackTrace();
        throw (RuntimeException) excecption;
      }
      throw new IOException(excecption == null ? null : excecption.toString());
    }
  }
  
  public synchronized OutputStream getOutputStream() {
    if (out == null) {
      out = new ProcessOutputStream(this);
    }
    return out;
  }
  
  public synchronized InputStream getInputStream() {
    if(in == null) {
      in = new ProcessInputStream(this, true);
    }   
    return in;
  }
  
  public synchronized InputStream getErrorStream() {
    if (err == null) {
      err = new ProcessInputStream(this, false);
    }
    return err;
  }
  
  public synchronized int waitFor() throws InterruptedException {
    if(returnvalue == STILL_RUNNING) {
      this.wait();
      return returnvalue;
    }
    
    if(returnvalue == ERROR) {
      return -1;
    }
    else {
      return returnvalue;
    }
  }
  
  public int exitValue() throws IllegalThreadStateException {
    if(returnvalue == STILL_RUNNING) {
      throw new IllegalThreadStateException("Process is still running");
    }
    if(returnvalue == ERROR) {
      return -1;
    }
    return returnvalue;
  }
    
  public void destroy() {
    info.destroy();
  }
 
  public String toString() {
    return "PID: " + (info == null ? "???" : String.valueOf(info.id)) + " returnvalue: " + 
    (returnvalue < STILL_RUNNING ? String.valueOf(returnvalue) : "?");
  }

  synchronized void setReturnValue(int returnValue) {
    returnvalue = returnValue;
    this.notifyAll();    
  }
}
