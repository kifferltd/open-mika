/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

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
    while (returnvalue == STILL_RUNNING) {
      this.wait();
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
