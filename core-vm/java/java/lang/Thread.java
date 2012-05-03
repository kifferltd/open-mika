/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009, 2011  *
* by Chris Gray, /k/ Embedded Java Solutions.  All rights reserved.       *
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

package java.lang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Thread implements Runnable {

  private static int nameCounter;
  
  private synchronized static String createName() {    
    return "Thread-"+nameCounter++;
  }

  /*
  ** Note: this class is initialized "by hand" before the VM is fully
  ** initialized.  Consequently it must not have a static initializer.
  ** (It can have static variables, and even constant initial values
  ** for those variables, but nothing fancier and certainly no static{}
  ** clause.)
  */
  
  public final static int MIN_PRIORITY  =  1;
  public final static int MAX_PRIORITY  = 10;
  public final static int NORM_PRIORITY =  5;

   /**
   ** Dummy object used to synchronise accesses to 'started' and 'stopped'.
   ** Note: we don't allocate this here or in the constructors 'coz the
   ** system init thread gets created by native code, and allocating
   ** state_lock in said native code doesn't work either. Hence the kludgey
   ** stuff preceding each use of this field, sorry.
   */
  private Object state_lock;

  /**
   * The name of this Thread.
   */
  private String name;

  /**
   * Priority of this Thread.
   */
  private int priority;

  /**
   ** started    is true iff start() has been called on this thread.
   **            N.B. Remains true when the thread dies! (see 'stopped' below).
   */
  private boolean started;

  /**
   ** stopped    is true iff the thread has been started and subsequently died
   **            (either as a result of stop()/destroy() or of natural causes).
   */
  private boolean stopped;

  /**
   ** runObject  is either this Thread (if the current thread was created
   **             using a subclass of Thread) or the Runnable object used
   **             to create the thread.
   */
  private Runnable runObject;

  /**
   ** parent     is the ThreadGroup of which this thread is a member.
   */
  private ThreadGroup parent;

  /**
   ** context_classloader is the ClassLoader of last resort to be used
   ** by this Thread.
   */
  private ClassLoader context_classloader;

  /**
   ** threadLocals  maps instances of ThreadLocal (or its subclasses,
   **            including InheritableThreadLocal) onto the corresponding
   **            values for this Thread.
   */
  private Map threadLocals;

  /**
   ** thrown     used to store a pending exception 
   *  used in native code: do not remove !!!
   */
  private Throwable thrown;
  private boolean isDaemon; 

  /**
   * Link to a resource monitor on builds where this feature is enabled.
   */
  private Object resourceMonitor;

  private static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.theSecurityManager;
      if (sm != null) {
        sm.checkPermission(new RuntimePermission(permission));
      }
    }
  }

  /**
   ** Thread(ThreadGroup group, Runnable runObject, String name)
   **   --> name as specified, or "Thread-<n>" if name==null; 
   **       runObject as specified;
   **       parent group as specified, or if this is null then 
   **       sm.getThreadGroup() or parent of current thread.
   */
  public Thread(ThreadGroup group, Runnable runObject, String myname) 
    throws SecurityException  {
    ThreadGroup myparent;
    SecurityManager sm = System.theSecurityManager;

    if(myname == null) {
      throw new NullPointerException();
    }
    name = myname;
    
    priority = Thread.currentThread().getPriority();
    if (priority == 0) {
      priority = 5;
    }

    if (group == null) {
      if (sm == null) {
        myparent = Thread.currentThread().getThreadGroup();
      }
      else {
        myparent = sm.getThreadGroup();
      }
    }
    else {
      myparent = group;
      int groupMaxPriority = myparent.getMaxPriority();
      if (priority > groupMaxPriority) {
        priority = groupMaxPriority;
      }
    }

    if (myparent.isDestroyed()) {
      throw new IllegalThreadStateException();
    }

    this.parent = myparent;

    if (sm != null) {
      sm.checkAccess(myparent);
    }

    this.runObject = runObject != null ? runObject : this;

    inheritThreadLocals(Thread.currentThread());
    context_classloader = currentThread() == null ? null : currentThread().getContextClassLoader();

    create(myparent, myname, runObject);
  }

  /**
   ** Thread(ThreadGroup group, Runnable runObject, String name, long stacksize)
   **   --> Thread(ThreadGroup group, Runnable runObject, String name),
   **       i.e. stacksize is ignored.
   */
  public Thread(ThreadGroup group, Runnable runObject, String name, long stacksize) 
    throws SecurityException,
    IllegalThreadStateException 
  {
    this(group, runObject, name);
  }

  /**
   ** Thread constructor (Runnable runObject, String name)
   **   --> Thread(ThreadGroup,Runnable,String) with null ThreadGroup
   */
  public Thread(Runnable runObject, String name) {
    this((ThreadGroup)null,runObject,name);
  }

  /**
   ** Thread(ThreadGroup group,Runnable runObject)
   **   --> Thread(ThreadGroup,Runnable,String) with null String
   */
  public Thread(ThreadGroup group, Runnable runObject) 
    throws SecurityException,
    IllegalThreadStateException 
  {
    this(group,runObject,createName());
  }

  /**
   ** Thread(Runnable)
   **   --> Thread(ThreadGroup,Runnable,String) with null ThreadGroup, String
   */
  public Thread(Runnable runObject) {
    this((ThreadGroup)null,runObject,createName());
  }

  /**
   ** Thread(ThreadGroup group,String name)
   **   --> name as specified, or "Thread-<n>" if name==null; 
   **       Thread is own runObject;
   **       parent group as specified, or if this is null then 
   **       sm.getThreadGroup() or parent of current thread.
   */
  public Thread(ThreadGroup group, String name) 
    throws SecurityException,
    IllegalThreadStateException 
  {
    this(group, null, name);
  }

  /**
   ** Thread constructor (String) --> name as specified, or "Thread-<n>"
   ** if name==null; Thread is own runObject, parent is sm.getThreadGroup() 
   ** or parent of current thread.
   */
  public Thread(String name) {
    this((ThreadGroup)null,null,name);
  }

  /**
   ** Thread constructor with no arguments --> Thread(String) with null argument
   */
  public Thread() {
    this((ThreadGroup)null,null, createName());
  }

  /**
   ** create(ThreadGroup, String, Runnable) creates the native w_Thread and
   ** osthread structures, sets up the mapping osthread->w_Thread in the
   ** thread hashtable, and increments totalCount.
   */
  private native void create(ThreadGroup group, String name, Runnable runnable);

  /**
   ** activeCount() returns the number of threads executing in the
   ** current ThreadGroup and its children.  How pointless.
   */
  public static int activeCount() {
    return currentThread().getThreadGroup().activeCount();
  }

  /**
   ** run() : 
   ** If the runObject of this Thread is the Thread itself, does nothing.
   ** Otherwise, call method run() of runObject.
   */
  public void run() {
    if(runObject!=this) runObject.run();
  }

  /**
   ** checkAccess() checkes whether the SecurityManager (if any) objects
   ** to our performing priviledged actions on this thread.
   */
  public final void checkAccess() 
    throws SecurityException
  {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.theSecurityManager;
      if (sm != null) {
        sm.checkAccess(this);
      }
    }
  }

  /**
   ** countStackFrames() should count the number of stack frames in 
   ** this thread: the thread must be suspended.  NOT YET IMPLEMENTED.
   */
  public native int countStackFrames();

  /**
   * _run() is the method which is used to define the initial stack frame. 
   */
  void _run() {
    parent.registerThread(this);
    try {
      runObject.run();
    } catch (Throwable t) {
      if (t instanceof ThreadDeath) {
      } else {
        parent.uncaughtException(this, t);
      }
    } finally {
      synchronized (this) {
        // [CG 20080131]
        // Emulate behaviour of Sun's VM, in which returning from run() seems
        // to cause a wait() on the Thread to complete.
        notifyAll();
      }
      synchronized (state_lock) {
        stopped = true;
        state_lock.notifyAll();
      }
      parent.deregisterThread(this);
      parent = null;
    }
  }

  /**
   ** destroy() is supposed to destroy the thread "without performing
   ** any cleanup".  Implemented as a synonym of stop0(new ThreadDeath()).
   */
  public void destroy() {
    stop0(new ThreadDeath());
  }

  /**
   ** dumpStack() is prints a stack trace to System.err.
   */
  public static void dumpStack() {
    new Exception("Thread.dumpStack called").printStackTrace();
  }

  /**
   ** enumerate() enumerates the active threads in the current ThreadGroup
   ** and its child groups.  Another pointless synonym for a ThreadGroup method.
   */
  public static int enumerate(Thread[] threads) {
    return currentThread().getThreadGroup().enumerate(threads);
  }
 
  /**
   ** toString() creates a string which includes the thread's name, priority,
   ** and parent ThreadGroup.
   */
  public String toString() {
    return "Thread[" + name + "," + priority + "," + 
    (parent != null ? parent.getName() : null) +"]";
  }

  /**
   ** start() causes the Thread to be started, invoking its run() method
   ** (via a call to Thread._run()).
   */
  public void start() 
    throws IllegalThreadStateException
  {
    synchronized(this) {
      if (state_lock == null) {
        state_lock = new Object();
      }
    }
    synchronized (state_lock) {
      if (started) {
        if (stopped) {

          return; // silently

        }
        throw new IllegalThreadStateException(this+" already active");
      }
    }
    int retcode = start0();
    synchronized (state_lock) {
      started = true;
      if (retcode != 0) {
        started = false;
        throw new InternalError("Could not start "+this+", OSwald return code = "+retcode);
      }
      state_lock.notifyAll();
    }
  }

  /**
   ** Try to start the native thread.  Returns 0 on success, OSwald status on failure.
   */
  private native int start0() ;

  /**
   ** Try to sleep for a while.
   */
  private native void sleep0(long millis, int nanos);

  /**
   ** stop0() causes exception "thr" to be thrown in the thread, by sending
   ** an appropriate message to its ThreadGrouop manager.
   */
  private native void stop0(Throwable thr);

  /**
   ** stop() performs an access check and then throws a ThreadDeath in
   ** the victim thread.
   */
  public final synchronized void stop()
    throws SecurityException
  {
    checkAccess();

    stop0(new ThreadDeath());
  }

  /**
   ** stop(Throwable thr) performs an access check and then throws "thr" in
   ** the victim thread.
   */
  public final void stop(Throwable thr)
    throws SecurityException, NullPointerException
  {
    checkAccess();

    if(thr==null) {
      throw new NullPointerException();
    }

    stop0(thr);
  }


  /**
   ** suspend0() suspends a thread
   */
  private final native void suspend0();

  /**
   ** resume0() resumes a thread which was previously suspended.
   ** Note: suspension and resumption are binary, not counting
   ** (one resume0() will undo any number of suspend0()s).
   */
  private final native void resume0();

  /**
   ** suspend0() performs a checkAccess() before calling suspend0().
   */
  public final void suspend()
    throws SecurityException
  {
    checkAccess();

    suspend0();
  }



  /**
   ** resume0() performs a checkAccess() before calling resume0().
   */
  public final void resume()
    throws SecurityException
  {
    checkAccess();

    resume0();
  }



  /**
   ** getName() returns the name of the Thread
   */
  public final String getName() {
    return name;
  }

  /**
   ** setName() changes the name of the Thread.
   */
  private final native void setName0(String name);

  /**
   ** setName(String name) invokes checkAccess() before calling setName0(name).
   ** If parameter "name" is null then we set the name to "".
   */
  public final void setName(String name) 
    throws SecurityException
  {
    checkAccess();

    if(name==null) {
      throw new NullPointerException();
    } else {
      this.name = name;
      setName0(name);
    }
  }

  /**
   ** getThreadGroup() returns the parent ThreadGroup.
   */
  public final ThreadGroup getThreadGroup() {
    return parent;
  }

  /**
   ** getPriority() returns the current priority of this thread.
   */
  public final int getPriority() {
    return priority;
  }

  /**
   ** setPriority(int) changes the current priority of this thread.
   */
  private final native void setPriority0(int newPriority);

  /**
   ** setPriority(int newPriority) first calls checkAccess(), and then 
   ** calls setPriority0() to set the priority to the lesser of
   **   - the value requested; or
   **   - the maxPriority of the parent ThreadGroup.
   */
  public final void setPriority(int newPriority)
    throws SecurityException, IllegalArgumentException
  {
    checkAccess();

    if(newPriority<MIN_PRIORITY || newPriority>MAX_PRIORITY)
      throw new IllegalArgumentException("priority "+newPriority+" not allowed");

    if (parent!=null) {
      priority = Math.min(newPriority,parent.getMaxPriority());
    }
    else {
      priority = newPriority;
    }

    setPriority0(priority);
  }


  /**
   ** isDaemon() returns the status of the Thread's daemon flag.
   */
  public final boolean isDaemon() {
    return isDaemon;
  }

  /**
   ** setDaemon0() sets the status of the Thread's daemon flag.
   ** Note: the daemon flag has no significance in Wonka.
   */
  private final native void setDaemon0(boolean on);

  /**
   * * setDaemon() first calls checkAccess() and then invokes setDaemon0() * -
   * unless the thread is currently active, in which case *
   * IllegalThreadStateException is thrown.
   */
  public final void setDaemon(boolean on) throws SecurityException,
      IllegalThreadStateException {
    checkAccess();

    synchronized (this) {
      if (state_lock == null) {
        state_lock = new Object();
      }
    }
    synchronized (state_lock) {
      if (!stopped) {        
        if (started) {
          throw new IllegalThreadStateException(this.toString());
        } else {
          setDaemon0(on);
        }
      }
      isDaemon = on;
    }
  }

  /**
   * * isAlive() returns true iff the thread is "alive" (alive, oh-oh).
   */
  public final boolean isAlive() {
    return started && !stopped;
  }

  /**
   ** join() blocks the calling thread until this Thread has terminated
   ** (see the implementation of _run()). 
   ** [CG 20080901] Used to Return immediately if a thread tried to join
   ** itself, but this is not mandated by JSR210.
   */
  public final void join() throws InterruptedException {
    synchronized(this) {
      if (state_lock == null) {
        state_lock = new Object();
      }
    }
    if (interrupted()) {
      throw new InterruptedException();
    }
    synchronized (state_lock) {
      while (started && !stopped) {
        state_lock.wait();
      }
    }
  }

  /**
   ** join(millis) blocks the calling thread until either this Thread has 
   ** terminated or the stated number of milliseconds elapse.
   */
  public final void join(long millis) throws InterruptedException {
    join(millis,0);
  }

  /**
   ** join(millis,nanos) blocks the calling thread until either this Thread *
   ** has terminated or the stated time elapses.
   ** [CG 20080901] Used to Return immediately if a thread tried to join
   ** itself, but this is not mandated by JSR210.
   */
  public final void join(long millis, int nanos) throws InterruptedException {
    if (millis == 0 && nanos == 0) {
      join();
    } else if (millis < 0 || nanos < 0 || nanos >= 1000000) {
      throw new IllegalArgumentException();
    } else {
      long now = System.currentTimeMillis();
      long then = now + millis + 1;
      synchronized (this) {
        if (state_lock == null) {
          state_lock = new Object();
        }
      }
      if (interrupted()) {
        throw new InterruptedException();
      }
      synchronized (state_lock) {
        while ((started && !stopped) && (then > now)) {
          state_lock.wait(then - now, nanos);
          now = System.currentTimeMillis();
        }
      }
    }
  }

  /**
   * * interrupt() causes the interrupt status flag to be set, and any current *
   * wait() or sleep() to be aborted.
   */
  public native synchronized void interrupt();

  /**
   ** isInterrupted() tests the state of the interrupt status flag, without
   ** affecting its value.
   */
  public native boolean isInterrupted();

  /** interrupted() tests the state of the interrupt status flag,  and if
   ** the flag was set it resets it (atomically)
   */
  public static native synchronized boolean interrupted();

  /**
   ** currentThread() returns the Thread object corresponding to the
   ** currently running thread (i.e., the calling thread).
   */
  public static native Thread currentThread();

  /**
   ** yield() causes the calling thread to "sleep" for an infinitesimal
   ** length of time (in order to give other threads at the same priority
   ** a chance to run).
   */
  public static native void yield();

  /**
   ** holdsLock() returns true iff the current thread has a lock (is
   ** synchronized) on the specified object.
   */
  public static native boolean holdsLock(Object o);

  /**
   ** sleep(millis) causes the calling thread to "sleep" for
   ** the specified length of time.
   */
  public static void sleep(long millis) throws InterruptedException {
    sleep(millis, 0);
  }

  /**
   ** sleep(millis,nanos) causes the calling thread to "sleep" for
   ** the specified length of time.
   */
  public static void sleep(long millis, int nanos)
    throws InterruptedException
  {
    if (millis < 0) {
      throw new IllegalArgumentException("millis must be > 0");
    }

    if (nanos < 0 || nanos > 999999) {
      throw new IllegalArgumentException("nanos must be from 0 to 999999");
    }


    currentThread().sleep0(millis,nanos);
  }

  /**
   ** inheritThreadLocals(Thread t) copies those threadLocals of t
   ** which are instances of InheritableThreadLocal into the threadLocals
   ** table of the current Thread (in other words, it inherits them).
   */
  private void inheritThreadLocals(Thread t) {
    Map   h = t.threadLocals;

    if (h==null) return;

    Iterator i = h.keySet().iterator();

    if (threadLocals == null) {
      threadLocals = new HashMap();
    }

    while (i.hasNext()) {
      try {
        InheritableThreadLocal ihl = (InheritableThreadLocal)(i.next());
        threadLocals.put(ihl,ihl.childValue(t.threadLocals.get(ihl)));
      }
      catch (ClassCastException x) {}
    }
  }

  /**
   ** setThreadLocal(ThreadLocal tl, Object obj) sets the value of tl
   ** in the current thread to obj.
   ** TODO: only allow this to be called by tl itself.
   */
  void setThreadLocal(ThreadLocal tl, Object obj) {
    if (threadLocals == null) {
      threadLocals = new HashMap();
    }

    threadLocals.put(tl,obj);
  }


  /**
   ** getThreadLocal(ThreadLocal tl) returns the value of tl in the
   ** current thread.  If no such value exists then one is created
   ** using initialValue().
   ** TODO: only allow this to be called by tl itself.
   */
  Object getThreadLocal(ThreadLocal tl) {
    if (threadLocals == null) {
      threadLocals = new HashMap();
    }

    Object result = threadLocals.get(tl);

    return result;
  }

  /**
   ** removeThreadLocal(ThreadLocal tl) deletes the value associated with tl.
   ** TODO: only allow this to be called by tl itself.
   ** Only needed for Java 5+
  void removeThreadLocal(ThreadLocal tl) {
    if (threadLocals != null) {
    threadLocals.remove(tl);
    }
  }
   */

  public ClassLoader getContextClassLoader() {
    ClassLoader caller = ClassLoader.getCallingClassLoader();
    if (caller != null && !caller.isDelegationAncestor(context_classloader)) {
      permissionCheck("getClassLoader");
    }

    return context_classloader;
  }

  public void setContextClassLoader (ClassLoader cl) {
    ClassLoader caller = ClassLoader.getCallingClassLoader();

    if (caller != null) {
      if (context_classloader == null) {
        context_classloader = ClassLoader.getSystemClassLoader();
      }
      if (!caller.isDelegationAncestor(context_classloader)) {
        permissionCheck("setContextClassLoader");
      }
    }

    context_classloader = cl;
  }

}

