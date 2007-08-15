/**************************************************************************
* Copyright (c) 2004, 2005 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.awt.Window;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 ** Rudolph's internal event scanning thread.
 */
public final class Scanner implements Runnable {

  /**
   ** Sequence number so we can distinguish instances of Haigha by name.
   */
  private static int seq;

  /**
   ** Thread priority when scanning input devices.
   */
  private static final int poll_priority;

  /**
   ** Reference to our colleague the Dispatcher.
   */
  private static Dispatcher mainDispatcher;

  /**
   ** Time to sleep between scans Determined by system property
   ** com.acunia.wonka.rudolph.poll.interval, default = 100.
   */
  private static int sleep_millis;

  /**
   ** The current root window.
   */
  private static Window currentRoot;

  /**
   ** A stack of Window objects - the top element is the ancestor of the current root.
   */
  private static Stack rootStack;

  /**
   ** Stack of Thread objects - the top element is the ancestor of the current dispatch thread.
   ** (Only used if event queueing is disabled).
   */
  private static Stack threadStack;

  /**
   ** The current dispatch thread.
   ** (Only used if event queueing is disabled).
   */
  private static Thread currentDispatchThread;

  /**
   ** The thread which performs the scanning.
   */
  private Thread ourThread;

  /**
   ** The name of this Scanner (and of the corresponding thread).
   */
  private String name;

  /**
   **
   */
  private Window root = null;

  /**
   ** State of the scanning thread - takes one of the THREAD_xxx values below.
   */
  private int threadState;

  private static final int THREAD_UNSTARTED = 0;

  private static final int THREAD_STARTING = 1;

  private static final int THREAD_RUNNING = 2;

  private static final int THREAD_STOPPING = 3;

  private static final int THREAD_STOPPED = 4;

  /**
   ** During class initialisation we set up the mouse device and create the
   ** master instance of this class. We also set up a VM shutdown routine to
   ** undo this work when the VM is shut down.
   */
  static {
    String mouse_device = getMouseDevice();

    sleep_millis = Integer.getInteger("rudolph.poll.interval", 100).intValue();
    poll_priority = Integer.getInteger("rudolph.poll.priority", 10).intValue();

    init(mouse_device);

    // Add a hook to restore keyboard settings, etc.
    Runtime.getRuntime().addShutdownHook(
      new Thread() {
        public void run() {
          shutdown();
        }
      });
  }

  /**
   ** Native part of the static initialiser.
   */
  private native static void init(String mouse);

  /**
   ** Set up the mouse or other pointing device.
   ** The information describing the pointing device is found in file
   ** 'device.config' in the system directory, in a line beginning
   ** 'attach-mouse-device'. Then next two tokens on this line are
   ** ignored, and the fourth token is used as a path to the pointing device.
   ** <p>
   ** If no such line is found in the 'device.config' file, system property
   ** 'rudolph.default.mouse.device' is used; if this is undefined then the 
   ** path defaults to '/dev/ts'.
   */
  private static String getMouseDevice() {
    InputStream s = ClassLoader.getSystemResourceAsStream("device.config");
	
    BufferedReader r = new BufferedReader(new InputStreamReader(s));
    String line;
    try {
      while ((line = r.readLine()) != null) {
        int start;
        int end;
        while ((start = line.indexOf("  ")) != -1) {
          line = line.substring(0, start) + line.substring(start + 1);
        }
        while ((start = line.indexOf("( ")) != -1) {
          end = line.indexOf(")", start + 2);
          if (end > start) {
            line = line.substring(0, start) + line.substring(end + 1);
          }
          else {
            line = line.substring(0, start);
          }
        }
        StringTokenizer t = new StringTokenizer(line);
        while (t.hasMoreTokens()) {
          String command = t.nextToken();
          if (command.toLowerCase().equals("attach-mouse-device")) {
            try {
              t.nextToken(); // ignored
              t.nextToken(); // ignored
              String path = t.nextToken();

              return path;

            }
            catch (NoSuchElementException e) {
              System.err.println("attach-mouse-device : syntax is attach-mouse-device <family> <number> <path>");
            }
            catch (NumberFormatException nfe) {
              System.err.println("attach-mouse-device : illegal device number");
            }
          }
        }
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return System.getProperty("rudolph.default.mouse.device", "/dev/ts");
  }

  /**
   ** Compute name of this instance. If this is the first instance to be
   ** created, also launch the scanning thread.
   */
  private static synchronized void prepare(Scanner instance, Window root) {
    instance.name = "Haigha";
    if (seq > 0) {
      instance.name += "-" + seq + "(" + root + ")";
    }
    seq += 1;
  }

  private Scanner() {}

  public Scanner(Dispatcher d, Window w) {
    if (mainDispatcher == null) {
      mainDispatcher = d;
    }
    prepare(this, w);
    root = w;
  }

  /**
   ** Make this root the current root, and push the prior current root onto 
   ** the stack. If queueing is disabled, launch a new scanning thread
   ** (because the existing thread is our caller, and will be blocked).
   */
  void push() {
    if (rootStack == null) {
      rootStack = new Stack();
    }
    else {
      rootStack.push(currentRoot);
      currentRoot = root;
    }

    if ("Haigha".equals(name) || !Dispatcher.queueing_enabled) {
      threadState = THREAD_STARTING;
      ourThread = new Thread(this, name);
      ourThread.setPriority(poll_priority);
      ourThread.setDaemon(false);
      ourThread.start();
    }

    if (!Dispatcher.queueing_enabled) {
      Dispatcher.pushThread(ourThread);
    }
  }
  
  /**
   ** Pop the prior current root from the stack.
   */
  void pop() {
    currentRoot = (Window)rootStack.pop();
    if (ourThread != null) {
      threadState = THREAD_STOPPING;
      ourThread.interrupt();
      synchronized(this) {
        while (threadState < THREAD_STOPPED) {
          try {
            wait(250);
          }
          catch (InterruptedException ie) {
          }
        }
      }
    }

    if (!Dispatcher.queueing_enabled) {
      Dispatcher.popThread();
    }
  }

  /**
   ** Native code to drain the input devices, i.e. to read and discard all
   ** available events.
   */
  public native static void drain(); 

  /**
   ** Native code for the shutdown hook.
   */
  native static void shutdown();

  /**
   ** Native code to poll the input devices.
   */
  private native void poll(Window root); 

  /**
   ** Scanner main loop.
   ** The input devices are polled at priority 'poll_priority'; this native
   ** code attempts to read as many events as possible from each device.
   */
  public void run() {
    threadState = THREAD_RUNNING;

    try {
      while(threadState == THREAD_RUNNING) {
        try {
          Thread.sleep(sleep_millis);
          poll(currentRoot);
        }
        catch (InterruptedException ie) {
        }
        catch (Exception exc) {
          try {
            exc.printStackTrace();
          }
          catch (Exception exc2) {
          }
        }
        catch (Error err) {
          try {
            err.printStackTrace();
          }
          finally {
            throw err; // this will kill thread
          }
        }
      }
    }
    finally {
      synchronized(this) {
        threadState = THREAD_STOPPED;
        notifyAll();
      }
    }
  }
}

