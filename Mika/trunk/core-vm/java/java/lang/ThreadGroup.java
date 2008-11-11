/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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

import java.util.Enumeration;
import java.util.Vector;

/**
 ** A ThreadGroup can contain Threads (its "flock") and ThreadGroups
 ** (its "children").  The children have of course their own flock
 ** and children, and so ad infinitum.
 **
 **                  T h r e a d G r o u p
 **               flock               children
 **               | | |             |    |    |
 **               T T T            TG   TG   TG
 **                                /|\  /|\  /|\
 **
 ** Membership of the Vectors "flock" and "children" is updated by
 ** the methods [de]registerThread[Group].
 */
public class ThreadGroup {

  /*
  ** Note: this class is initialized "by hand" before the VM is fully
  ** initialized.  Consequently it must not have a static initializer.
  ** (It can have static variables, and even constant initial values
  ** for those variables, but nothing fancier and certainly no static{}
  ** clause.)
  */
  
  /**
   ** The name of this ThreadGroup.
   */
  private final String name;

  /**
   ** The number of threads created in this ThreadGroup and not yet terminated
   ** (includes yet-to-be-started threads).
   */
  int totalCount;

  /**
   ** The maximum thread priority permitted within this group.
   */
  private int maxPriority;

  /**
   ** Set true iff ThreadGroup has already been destroy()ed.
   */
  private boolean destroyed;

  /**
   ** Set tru iff this is a daemon ThreadGroup.
   */
  private boolean daemon;

  /**
   ** The parent ThreadGroup of this group.
   */
  private ThreadGroup parent;

  /**
   ** The child ThreadGroups of this group.
   */
  private Vector children;

  /**
   ** The member Threads of this group (= only those started and not stopped).
   */
  private Vector flock;

  /**
   ** Constructor ThreadGroup(String)
   ** - parent is the ThreadGroup of which the current (calling) Thread
   **   is a member.  See ThreadGroup(ThreadGroup,String) below.
   */
  public ThreadGroup(String name) throws SecurityException {
    this(Thread.currentThread().getThreadGroup(),name);
  }

  /**
   ** Constructor ThreadGroup(ThreadGroup parent, String name)
   ** constructs a ThreadGroup with the given name and parent.
   ** The current (calling) thread must have the necessary access rights.
   ** The parent ThreadGroup must not be null, and must not have its
   ** "destroyed" flag set (this flag is set when destroy() is called).
   **
   ** Note that no instance of Vector is yet allocated to the fields
   ** "children" and "flock": one will be allocated the first time
   ** registerThreadGroup() or registerThread() is called.  This
   ** enables us to create the systemThreadGroup and systemInitThread
   ** without calling a constructor (and without having to initialize
   ** class Vector).
   */
  public ThreadGroup(ThreadGroup parent, String name)
    throws NullPointerException, SecurityException, IllegalThreadStateException 
  {
    Thread.currentThread().getThreadGroup().checkAccess();
    if(parent==null) throw new NullPointerException();
    if (parent.destroyed) {
      throw new IllegalThreadStateException();
    } 
    this.name = name;
    this.parent = parent;
    parent.registerThreadGroup(this);
  }

  /**
   ** toString()
   ** creates a String describing the ThreadGroup..
   */
  public String toString() {
    return "[name="+name+",maxpri="+maxPriority+"]";
  }

  /**
   ** checkAccess()
   ** checks that the current (calling) thread is allowed to modify this
   ** ThreadGroup.
   */
  public final void checkAccess() 
    throws SecurityException
  {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      if (parent == null) {
        java.security.AccessController.checkPermission(new RuntimePermission("modifyThreadGroup"));
      }
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkAccess(this);
      }
    }
  }

  /**
   * * registerThread(Thread t) * adds t to Vector flock, the list of member
   * Threads. * The Vector is created on demand (see
   * ThreadGroup(ThreadGroup,String)).
   */
  synchronized void registerThread(Thread t) {
    if (flock == null) {
      flock = new Vector();
    }
    totalCount++;

    flock.add(t);
  }

  /**
   ** deregisterThread(Thread t) 
   ** removes t from Vector flock, the list of member Threads.
   */
  synchronized void deregisterThread(Thread t) {
    flock.remove(t);
    totalCount--;
    checkForTermination();
  }

  /**
   ** registerThreadGroup(ThreadGroup tg) 
   ** adds tg to Vector children, the list of child ThreadGroups.
   ** The Vector is created on demand (see ThreadGroup(ThreadGroup,String)).
   */
  private void registerThreadGroup(ThreadGroup tg) {
    synchronized (this) {
      if (children==null) {
        children = new Vector();
      }
    }

    children.add(tg);
  }

  /**
   ** deregisterThreadGroup(ThreadGroup tg) 
   ** removes tg from Vector children, the list of child ThreadGroups.
   */
  private synchronized void deregisterThreadGroup(ThreadGroup tg) {
    children.remove(tg);
    checkForTermination();
  }

  /**
   ** If the `daemon' flag is set and the both `children' and `flock' are
   ** now empty, detach this ThreadGroup from its parent (and hence become 
   ** GC-able).
   */
  private void checkForTermination() {
    if (daemon 
        && (children == null || children.size() == 0) 
        && (totalCount == 0)
        && parent != null
       ) {
      parent.deregisterThreadGroup(this);
      parent = null;
    }
  }

  /**
   ** activeCount()
   ** estimates the number of active threads in this ThreadGroup.
   ** We first count the threads which are direct members of this
   ** ThreadGroup (Vector flock), and then apply this function 
   ** recursively to every child ThreadGroup (Vector children).
   */
  public int activeCount() {
    Enumeration e;
    int count = 0;

    if (flock != null) {
      e = flock.elements();
      while (e.hasMoreElements()) {
        if (((Thread)e.nextElement()).isAlive()) ++count;
      }
    }

    if (children != null) {
      e = children.elements();
      while (e.hasMoreElements()) {
        count += ((ThreadGroup)e.nextElement()).activeCount();
      }
    }

    return count;
  }

  /**
   ** activeGroupCount()
   ** estimates the number of thread groups in this ThreadGroup.
   ** We first count the thread groups which are direct members of this
   ** ThreadGroup (Vector children), and then apply this function 
   ** recursively to every child ThreadGroup.
   */
  public int activeGroupCount() {
    if (children == null) return 0;

    int count = children.size();
    Enumeration e = children.elements();
    while (e.hasMoreElements()) {
      count += ((ThreadGroup)e.nextElement()).activeGroupCount();
    }

    return count;
  }

  /**
   ** totalCount()
   ** Same as activeCount, but includes yet-to-be-started threads.
   ** For internal use.
   */
  private int totalCount() {
    Enumeration e;
    int count = totalCount;

    if (children != null) {
      e = children.elements();
      while (e.hasMoreElements()) {
        count += ((ThreadGroup)e.nextElement()).totalCount();
      }
    }

    return count;
  }

  /*
   ** nonDaemonCount()
   ** Same as activeCount, but excludes daemon threads.
   ** For internal use.
  private int nonDaemonCount() {
    Enumeration e;
    int count = 0;

    if (flock != null) {
      e = flock.elements();
      while (e.hasMoreElements()) {
        if (((Thread)e.nextElement()).isAlive()) ++count;
      }
    }

    if (children != null) {
      e = children.elements();
      while (e.hasMoreElements()) {
        count += ((ThreadGroup)e.nextElement()).activeCount();
      }
    }

    return count;
  }
  */

  public int enumerate(ThreadGroup[] list) {
    int max = list.length;
    int i = children.size();

    if(max > i){
      max = i;
    }

    for(i=0 ; i < max ; i++){
      list[i] = (ThreadGroup)children.get(i);
    }
    return max;
  }

  public int enumerate(ThreadGroup[] list, boolean recursive){
    if(recursive){
      //TODO also do this recursive !!!
      return enumerate(list);

    }
    else {
      return enumerate(list);
    }
  }

  /**
   ** enumerate(Thread[] threads) --> enumerate(Thread[] threads, false).
   ** enumerate(Thread[] threads, boolean recurse) enumerates the Threads
   ** in this ThreadGroup only (recurse==false) or in this ThreadGroup
   ** and all its children, recursively.  In both cases you have to kind
   ** of guess the size of the array, and the result might not mean anything
   ** very much ...
   **/
  public int enumerate(Thread[] threads) {
    return enumerate(threads,false);
  }

  public int enumerate(Thread[] threads, boolean recurse) {
    Enumeration et;
    int i = 0;

    /*
    ** First copy our own threads into the array.
    */
    if (flock != null) {
      et = flock.elements();
      while (i<threads.length && et.hasMoreElements()) {
        threads[i++] = (Thread)(et.nextElement());
      }
    }

    if (!recurse) return i;

    /*
    ** To enumerate recursively, iterate over all our children
    ** and call enumerate(...,true) on each.  Append the results
    ** to what we already have: if the threads[] array gets full,
    ** pack up and go home early.
    */
    if (children != null) {
      Enumeration echild = children.elements();
      while (i<threads.length && echild.hasMoreElements()) {
        ThreadGroup nextTG = (ThreadGroup)(echild.nextElement());
        Thread[] temp = new Thread[nextTG.activeCount()];
        int l = enumerate(temp,true);
        if (i+l>threads.length) {
          System.arraycopy(temp,0,threads,i,threads.length-i);

          return threads.length;

        }
        else {
          System.arraycopy(temp,0,threads,i,l);
          i += l;
        }
      }
    }

    return i;
  }

  /**
   ** getName()
   ** returns the name of this ThreadGroup.
   */
  public final String getName() {
    return name;
  }

  /**
   ** getParent()
   ** returns the parent of this ThreadGroup.
   */
  public final ThreadGroup getParent() {
    return parent;
  }

  /**
   ** isDestroyed()
   ** returns true iff destroy() has already been called on this ThreadGroup.
   */
  public boolean isDestroyed() {
    return destroyed;
  }

  /**
   ** parentOf(ThreadGroup group)
   ** returns true iff the group in question is a direct or indirect child
   ** of this ThreadGroup.
   */
  public final boolean parentOf(ThreadGroup group) {
    if (children == null) {

      return false;

    }
    if (children.contains(group)) {

      return true;

    }

    Enumeration e = children.elements();

    while (e.hasMoreElements()) {
      if (((ThreadGroup)e.nextElement()).parentOf(group)) {

        return true;

      }
    }

    return false;
  }

  /**
   ** stop()
   ** This deprecated method calls the stop() method of every thread in
   ** this group and in all of its child groups, recursively.
   ** The calling thread must have access permissions to modify this group.
   */
  public final synchronized void stop()
    throws SecurityException
  {
    Enumeration e;

    checkAccess();

    if (children != null) {
      e = children.elements();
      while (e.hasMoreElements()) {
        ((ThreadGroup)e.nextElement()).stop();
      }
      children = null;
    }

    if (flock != null) {
      Thread ownthread = Thread.currentThread();
      boolean suicide = false;
      Vector  snapshot = (Vector)flock.clone();
      e = snapshot.elements();
      while (e.hasMoreElements()) {
        Thread t = (Thread)e.nextElement();
        if (t == ownthread) {
          suicide = true;
        }
        else {
          t.stop();
        }
      }
      if (suicide) {
        ownthread.stop();
      }
    }

    if (daemon && parent != null) {
      parent.deregisterThreadGroup(this);
      parent = null;
    }
  }

  /**
   ** interrupt()
   ** This deprecated method calls the interrupt() method of every thread in
   ** this group and in all of its child groups, recursively.
   ** The calling thread must have access permissions to modify this group.
   */
  public final synchronized void interrupt()
    throws SecurityException
  {
    Enumeration e;

    checkAccess();

    if (children != null) {
      e = children.elements();
      while (e.hasMoreElements()) {
        ((ThreadGroup)e.nextElement()).interrupt();
      }
      children = null;
    }

    if (flock != null) {
      Thread ownthread = Thread.currentThread();
      Vector  snapshot = (Vector)flock.clone();
      e = snapshot.elements();
      while (e.hasMoreElements()) {
        Thread t = (Thread)e.nextElement();
        if (t == ownthread) {
        }
        else {
          t.interrupt();
        }
      }
    }
  }


  /**
   ** suspend()
   ** This deprecated method calls the suspend() method of every thread in
   ** this group and in all of its child groups, recursively.
   ** Fails if destroy() was already called on this ThreadGroup.
   ** The calling thread must have access permissions to modify this group.
   */
  public final synchronized void suspend()
    throws SecurityException
  {
    checkAccess();

    if(destroyed) {
      throw new IllegalThreadStateException();
    }

    if (children != null) {
      Enumeration e = children.elements();
      while (e.hasMoreElements()) {
        ((ThreadGroup)e.nextElement()).suspend();
      }
    }

    if (flock != null) {
      Enumeration e = flock.elements();
      while (e.hasMoreElements()) {
        ((Thread)e.nextElement()).suspend();
      }
    }
  }


  /**
   ** resume()
   ** This deprecated method calls the resume() method of every thread in
   ** this group and in all of its child groups, recursively.
   ** Fails if destroy() was already called on this ThreadGroup.
   ** The calling thread must have access permissions to modify this group.
   */
  public final synchronized void resume()
    throws SecurityException
  {
    checkAccess();

    if(destroyed) {
      throw new IllegalThreadStateException();
    }

    if (children != null) {
      Enumeration e = children.elements();
      while (e.hasMoreElements()) {
        ((ThreadGroup)e.nextElement()).suspend();
      }
    }

    if (flock != null) {
      Enumeration e = flock.elements();
      while (e.hasMoreElements()) {
        ((Thread)e.nextElement()).suspend();
      }
    }

  }

  /**
   ** destroy()
   ** deregisters this ThreadGroup and all its direct and indirect children
   ** from their respective parents (thereby rendering them eligible for
   ** garbage collection).  May only be called if 
   ** - the calling thread has permission to modify this ThreadGroup;
   ** - destroy() was not already called on this ThreadGroup; and
   ** - none of the ThreadGroups affected contains any threads.
   */
  public final synchronized void destroy()
    throws SecurityException,
           IllegalThreadStateException
  {
    checkAccess();

    if (destroyed) {
      throw new IllegalThreadStateException();
    }

    if (totalCount() != 0) {

       throw new IllegalThreadStateException();
    }
    
    if (children != null) {
      while (children.size()>0) {
        synchronized (children) {
          ((ThreadGroup)children.elementAt(children.size()-1)).destroy();
        }
      }
    }

    destroyed = true;
    if (parent != null) {
      parent.deregisterThreadGroup(this);
      parent = null;
    }
  }

  public final int getMaxPriority() {
    return maxPriority;
  }

  /**
   ** setMaxPriority(int newMaxPriority)
   ** checks its argument and the caller's permissions befor calling setMaxPriority0.
   */
  public final void setMaxPriority(int newMaxPriority)
    throws SecurityException, IllegalArgumentException
  {

    checkAccess();

    if(newMaxPriority<Thread.MIN_PRIORITY || newMaxPriority>Thread.MAX_PRIORITY) {
 
      return;

    }

     maxPriority = newMaxPriority;

  }


  /**
   ** isDaemon()
   ** returns the value of the `daemon' flag.
   */
  public final boolean isDaemon() {

    return daemon;

  }

  /**
   ** setDaemon()
   ** checks the caller's permissions before setting the `daemon' flag.
   */
  public final void setDaemon(boolean daemon)
    throws SecurityException
  {

    checkAccess();

    this.daemon = daemon;

  }

  /**
   ** list()
   ** prints all the threads in this ThreadGroup and its descendants
   ** on System.out.
   ** Our implementation uses a private method list0(int indent) in
   ** order to give the desired indentation:
   ** java.lang.ThreadGroup[name=SystemThreadGroup,maxpri=1:]
   **     Thread[systemInitThread,5,SystemThreadGroup]
   **     java.lang.ThreadGroup...
   */
  public void list() {
    list0(0);
  }

  /**
   ** list0(int indent)
   ** prints out the name of this ThreadGroup (indented by 4*indent spaces),
   ** then the name of each member thread ((indented by 4*indent+4 spaces),
   ** and then calls list0(indent+1) on every direct child ThreadGroup.
   */
  private void list0(int indent) {
    int i;
    StringBuffer b = new StringBuffer();

    for (i=0; i<indent; ++i) b.append("    ");
    b.append(this.toString());
    System.out.println(b.toString());

    if (flock != null) {
      b.setLength(indent*4);
      b.append("    ");

      Enumeration e = flock.elements();
      while (e.hasMoreElements()) {
        for (i=0; i<indent*4; ++i) b.append(' ');
        b.append(((Thread)e.nextElement()).toString());
        System.out.println(b.toString());
        b.setLength(indent*4+4);
      }
    }

    if (children != null) {
      Enumeration e = children.elements();
      while (e.hasMoreElements()) {
        ((ThreadGroup)e.nextElement()).list0(indent+1);
      }
    }
  }


  /**
   ** uncaughtException(Thread t, Throwable e)
   ** handles exceptions thrown by any thread in this group and not caught
   ** by any exception handler in that thread's execution stack.
   ** If this method is not overridden, the exception will be passed all
   ** the way up to the systemThreadGroup and then a stack dump will be
   ** produced (unless the exception was ThreadDeath, in which case it is
   ** silently ignored).
   */
  public void uncaughtException(Thread t, Throwable e) {
    if(parent!=null) {
      parent.uncaughtException(t,e);
    } else {
      if (!(e instanceof ThreadDeath)) {
        e.printStackTrace(System.err);
        if (e instanceof Error && "true".equalsIgnoreCase(System.getProperty("mika.terminate.on.error"))) {
          System.err.println("VM terminated due to " + e);
          System.exit(1);
        }
      }
    }
    // If t isn't a direct member of this TG then following does nothing
    flock.remove(t);
    checkForTermination();
  }
  
  public boolean allowThreadSuspension(boolean b) {
    return false;    
  }

}
