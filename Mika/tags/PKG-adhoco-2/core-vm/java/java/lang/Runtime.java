/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2005 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


/*
** $Id: Runtime.java,v 1.13 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.lang;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import wonka.vm.GarbageCollector;
import wonka.vm.NativeProcess;

/**
** A bunch of methods used to manipulate the runtime environment.
*/

public class Runtime {

  private static int FINA_DONNA_PATIENZA = 1000;

  private static boolean verboseVal; // value of the verbose property (if verboseSet)
  private static boolean verboseSet; // true iff verbose property has been read

  /**
  ** The single unique instance of Runtime.
  */
  
  private static Runtime theRuntime;

  /**
  ** The number of instances of FinaDonna created and not yet finalized.
  ** Sycnchronize on FinaDonna.class when modifying this.
  */
  
  static int finalizatora;

  /**
  ** The Threads which should be started on shutdown.
  */
  
  private Vector shutdownHooks;

  /**
  ** Set to true iff a shutdown is in progress.
  */
  
  private boolean shuttingDown;

  /**
  ** Inner class used to implement runFinalization()
  */
  
  private class FinaDonna {

    public FinaDonna() {
      synchronized (FinaDonna.class) {
        ++finalizatora ;
      }
    }

    protected void finalize() {
      synchronized (FinaDonna.class) {
        --finalizatora ;
        FinaDonna.class.notifyAll();
      }
    }
  }
        
  /**
  * Implement verbosity.
  * The first time the method is called we read the mika.verbose property
  * For every subsequent call we just return the value read
  * We need this method because trying the read the verbose property in the 
  * constructor causes a seg fault
  */

  private void debug(String s) {
     if (!verboseSet)  {
        String verboseProperty = System.getProperty("mika.verbose", "");
        verboseVal = (verboseProperty.indexOf("shutdown") >= 0);
        verboseSet = true;
     }

     if (verboseVal) {
       System.err.println(s);
     }
  }

  /**
  ** Private constructor to block attempts to create a new instance.
  */
  
  private Runtime() {
  }

  private void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission(permission));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(new RuntimePermission(permission));
      }
    }
  }

  /**
  ** Add a Thread to the list of threads to be started when we decide
  ** to shut down the system.
  */
  
  public void addShutdownHook(Thread t) {
    permissionCheck("shutdownHooks");

    synchronized(this) {
      if (shuttingDown) {
        throw new IllegalStateException();
      }

      if (shutdownHooks == null) {
        shutdownHooks = new Vector();
      }

      if (shutdownHooks.contains(t)) {
        throw new IllegalArgumentException();
      }

      shutdownHooks.add(t);
    }
  }

  /**
  ** Remove a Thread from the list of threads to be started when we decide
  ** to shut down the system.
  */
  
  public boolean removeShutdownHook(Thread t) {
    permissionCheck("shutdownHooks");

    synchronized(this) {
      if (shutdownHooks == null) {

        return false;

      }

      if (shuttingDown) {
        throw new IllegalStateException();
      }

      return shutdownHooks.remove(t);
    }
  }

  /*
  ** Method which should be called to run all the shutdown hooks.
  ** The LIFO behaviour is not mandated by the specification.
  */
  
  private void runShutdownHooks() {
    synchronized(this) {
      if (shuttingDown || shutdownHooks == null) {
        return;
      }

      shuttingDown = true;
    }

    int n = shutdownHooks.size();
    while (n > 0) {
      Thread t = (Thread)shutdownHooks.elementAt(--n);
      debug("Runtime: starting shutdown hook "+t);
      t.start();
    }

    n = shutdownHooks.size();
    while (n > 0) {
      Thread t = (Thread)shutdownHooks.elementAt(n-1);
      try {
        t.join();
        --n;
      }
      catch (InterruptedException ie) {}
      debug("Runtime: joined shutdown hook "+t);
      shutdownHooks.removeElementAt(n);
    }
    shutdownHooks = null;

    debug("Runtime: all shutdown hooks have completed");
  }

  /**
  ** Return the solitary instance of Runtime.
  */
  
  public static synchronized Runtime getRuntime() {
  // CG 20030625
//    synchronized (Runtime.class) { 
      if (theRuntime == null) {
        theRuntime = new Runtime();
      }
//    }

    return theRuntime;
  }

  /**
  ** Native method called by exit() after performing all checks.
  */
  
  private native void exit0(int status);

  /**
  ** Exit the VM, after running all shutdown hooks.
  */
  
  public void exit(int status) throws SecurityException {
    permissionCheck("VMexit");

    debug("Runtime: call to exit(" + status + "), running shutdown hooks.");
    runShutdownHooks();
    debug("Runtime: Finished running shutdown hooks, goodnight.");
    
    try {
      System.in.close();
      System.out.close();
      System.err.close();
    }
    catch (Throwable t) {}

    exit0(status);
  }
    
  public void halt(int status) throws SecurityException {
    permissionCheck("VMexit");
    debug("Runtime: call to halt(" + status + "), skip running shutdown hooks.");
    exit0(status);
  }
    
  /**
  **  Not allowed ...
  **  @remark always throws a SecurityException
  */
  
  public Process exec(String command) throws IOException, SecurityException {
     return exec(command, null, null);
  }

  /**
  **  Not allowed ...
  **  @remark always throws a SecurityException
  */
  
  public Process exec(String command, String envp[]) throws IOException, SecurityException {
    return exec(command, envp, null);
  }

  public Process exec(String command, String envp[], File path) throws IOException, SecurityException {
    if(command.length() == 0) {
      throw new IllegalArgumentException();
    }
    StringTokenizer st = new StringTokenizer(command);
    String cmd[] = new String[st.countTokens()];
    int i = 0;
    while(st.hasMoreTokens()) {
      cmd[i++] = st.nextToken();
    }
    return exec(cmd, envp, path);
  }

  /**
  **  Not allowed ...
  **  @remark always throws a SecurityException
  */
  
  public Process exec(String cmdarray[]) throws IOException, SecurityException{
    return exec(cmdarray, null, null);
  }

  /**
  **  Not allowed ...
  **  @remark always throws a SecurityException
  */
  
  public Process exec(String cmdarray[], String envp[]) throws IOException, SecurityException{
    return exec(cmdarray, envp, null);
  }

  public Process exec(String cmdarray[], String envp[], File path) throws IOException, SecurityException{
    return new NativeProcess(cmdarray, envp, (path != null ? path.getAbsolutePath() : null));
    // throw new SecurityException("execution of commands is not allowed");
  }


  /**
  **  Not allowed ...
  **  @remark always throws a SecurityException
  */
  
  public void load(String filename) throws SecurityException, UnsatisfiedLinkError{
       throw new SecurityException("load is not allowed");
  }

  /**
  ** Tell the garbage collector to do some work.
  */
  
  public void gc(){
    GarbageCollector theGC = GarbageCollector.getInstance();
    theGC.request(Integer.MAX_VALUE);
//    theGC.request((int)(totalMemory() - freeMemory()));
  }

  /**
  ** Create an instance of FinaDonna and then throw it away.
  */
  private void generateFinaDonna() {
    new FinaDonna();
  }

  /**
  ** Ask the garbage collector to run finalizers.  Actually what we really
  ** do is to create an instance of FinaDonna and then make it unreachable,
  ** kick the Garbage Collector, and wait for the finalizer to be run.
  */
  
  public void runFinalization(){

    debug("Runtime: runFinalization() invoked");
    GarbageCollector theGC = GarbageCollector.getInstance();
    synchronized(FinaDonna.class) {
      generateFinaDonna();
      try {
        theGC.kick();
        FinaDonna.class.wait(FINA_DONNA_PATIENZA);
        while (finalizatora  > 0) {
          debug("Runtime: runFinalization() still waiting");
          theGC.kick();
          FinaDonna.class.wait(FINA_DONNA_PATIENZA);
        }
      }
      catch (InterruptedException ie) {
        debug("Runtime: runFinalization() interrupted, baling out");
      }
      debug("Runtime: runFinalization() completed");
    }
  }

  /**
  **  @status not implemented
  */
  
  public void traceInstructions(boolean on){}
  
  /**
  ** @status not implemented
  */
  
  public void traceMethodCalls(boolean on){}


  /**
  ** @deprecated
  **    @remark always throws a runtime exception
  */
  
  public InputStream getLocalizedInputStream(InputStream in){
    return new BufferedInputStream(in);
  }
  
  /**
  ** @deprecated
  **    @remark always throws a runtime exception
  */
  
  public OutputStream getLocalizedOutputStream(OutputStream out){
    return out;
  }

  public native long totalMemory();
  public native long freeMemory();

  private static native ClassLoader getCallingClassLoader();
  private static native ClassLoader getCallingCallingClassLoader();
  
  public void loadLibrary(String libname) throws SecurityException, UnsatisfiedLinkError {

    SecurityManager sm = System.getSecurityManager();

    if (sm != null) {
//      sm.checkLink(new RuntimePermission(libname));
    }

    String path = null;
   
    /*
    ** First try to look it up with the findLibrary method of ClassLoader.
    */
    
    ClassLoader cl = getCallingClassLoader();
    if(cl == null) {
      cl = getCallingCallingClassLoader();
    }
    
    if(cl != null) {
      path = cl.findLibrary(libname);
    }
    if(cl != null && path == null) {
      path = cl.findLibrary("lib" + libname + ".so");
    }

    if(path != null) {

      /*
      ** Found it with the classloader.
      */
      
      loadLibrary0(null, path);
    }
    else {

      /*
      ** No luck -> get the path from java.library.path and look it up.
      */
      
      path = System.getProperty("java.library.path", null);
      loadLibrary0(libname, path);
    }

  }
    
  /**
   ** Currently a synonym for totalMemory(). Could be coupled to -Xmx.
   */
  public long maxMemory () {
    return totalMemory();
  }

  /**
   ** Always returns 1.
   */
  public int availableProcessors() {
    return 1;
  }

  private native void loadLibrary0(String libname, String libpath) throws UnsatisfiedLinkError;

  public static void runFinalizersOnExit(boolean run) {
    //ignore
  }
}

