/**
 * Copyright  (c) 2006, 2008 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of /k/ Embedded Java Solutions nor the names of other contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL /K/
 * EMBEDDED SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package wonka.vm;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * ProcessMonitor:
 *
 * @author ruelens
 *
 * created: Jun 14, 2006
 */
public class ProcessMonitor implements Runnable {

  private Thread thread;
  private final HashMap processes = new HashMap(17);
  private final LinkedList queue = new LinkedList();
  private int returnvalue;

  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    Runtime theRuntime = Runtime.getRuntime();
    try {
      do {
        while (!queue.isEmpty()) {
          synchronized(this) {
            NativeProcess np = null;
            try {
              np = (NativeProcess)queue.removeFirst();
            }
            catch (NoSuchElementException nsee) {
            }
            if (np != null) {
              //System.out.println("ProcessMonitor.run() Starting new process " + np);
              startProcess(np);
            }
          }
        }

        int pid = waitForAllProcesses();
        synchronized(this) {
          if(pid == 0) {
            //System.out.println("ProcessMonitor.run() Sleeping ... "+System.currentTimeMillis());
            if(queue.isEmpty()) {              
              wait(250);
            }
          } else if(pid != -1) {
            //System.out.println("ProcessMonitor.run() pid "+pid+" has finished");
            Integer ipid = new Integer(pid);
            ProcessInfo info = (ProcessInfo) processes.remove(ipid);
            if(info == null) {
              System.err.println("[PANIC]ProcessMonitor.run() pid "+pid+" not in list !!!");
            } else {
              info.finish(returnvalue);              
            }
            if (theRuntime.freeMemory() < theRuntime.totalMemory() / 3) {
              theRuntime.runFinalization();
            }
          } else if (queue.isEmpty()){
            wait(10000);
            if (queue.isEmpty()){
              thread = null;
              break;                     
            }
          }
        }        
      } while(true);
    } catch (Throwable t) {
      System.err.println("[Mika] PANIC: NativeProcessMonitor stopped due to "+t);
      t.printStackTrace();      
      thread = null;
    }
    theRuntime.runFinalization();
    //System.out.println("ProcessMonitor.run() processes = "+processes);
  }

  synchronized void exec(NativeProcess process) {
    queue.add(process);
    if(thread == null) {
      thread = new Thread(this,"NativeProcessMonitor");
      thread.setDaemon(true);
      thread.setPriority(Thread.MAX_PRIORITY);
      thread.start();
    } else {
      notifyAll();
    }
  }

  private void startProcess(NativeProcess process) {
    try {
      ProcessInfo info = nativeExec(process.cmdarray, process.envp, process.path);
      if (info == null) {
        setProcessReturnValue(process, NativeProcess.ERROR);
      } else {
        info.setNativeProcess(process);
        Integer pid = new Integer(info.id);
        //System.out.println("ProcessMonitor.monitorProcess("+pid+") add to processes");
        processes.put(pid,info);  
        setProcessReturnValue(process, NativeProcess.STILL_RUNNING);
      }
    } catch (Throwable e) {
      process.exception = e;
      setProcessReturnValue(process, NativeProcess.ERROR);
    }
  }
  
  private void setProcessReturnValue(NativeProcess process, int retval) {
    synchronized(process) {
      process.returnvalue = retval;
      process.notifyAll();
    }
  }

  private  native ProcessInfo nativeExec(String[] cmdarray,
           String[] envp, String string) throws IOException;

  private native int waitForAllProcesses();
}
