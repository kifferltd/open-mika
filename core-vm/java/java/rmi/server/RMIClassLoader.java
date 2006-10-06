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

import java.net.*;
import java.lang.ref.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.Vector;
import java.security.*;
import wonka.vm.SystemTimer;

public class RMIClassLoader {

  private RMIClassLoader(){}

  static final String DEFAULT_CODEBASE;
  private static final boolean USE_CODEBASE_ONLY;

  static final Hashtable classLoaders;
  static ReferenceQueue queue;

  static {
    boolean b = false;
    classLoaders = new Hashtable(11);
    DEFAULT_CODEBASE = System.getProperty("java.rmi.server.codebase");
    try {

      b = Boolean.getBoolean("java.rmi.server.useCodebaseOnly");
    }
    catch(RuntimeException rt){}
    USE_CODEBASE_ONLY = b && (DEFAULT_CODEBASE != null);
  }

  public static String getClassAnnotation(final Class cl) {
    return(String) AccessController.doPrivileged(new PrivilegedAction(){
      public Object run(){
        ProtectionDomain pd = cl.getProtectionDomain();
        if(pd != null){
          CodeSource cs = pd.getCodeSource();
          if(cs != null){
            return cs.getLocation().toString();
          }
        }
        return DEFAULT_CODEBASE;
      }
    });
  }
  
  public static ClassLoader getClassLoader(String codebase) throws MalformedURLException, SecurityException {
    synchronized(classLoaders){
      WeakReference ref = (WeakReference) classLoaders.get(codebase);
      if(ref != null){
        ClassLoader cl = (ClassLoader)ref.get();
        if(cl != null){
          return cl;
        }
      }
      Vector v = new Vector();
      StringTokenizer st = new StringTokenizer(codebase);
      while(st.hasMoreTokens()){
        v.add(new URL(st.nextToken()));
      }
      ClassLoader cl = new URLClassLoader((URL[]) v.toArray(new URL[v.size()]));

      if(queue == null){
        queue = new ReferenceQueue();
        new CleanupTask();
      }

      classLoaders.put(codebase, new WeakReference(cl,queue));
      return cl;
    }
  }
  
  /**
  ** @deprecated
  */
  public static Object getSecurityContext(ClassLoader loader) {
    throw new wonka.vm.DeprecatedMethodError("RMIClassLoader.getSecurityContext(java.lang.ClassLoader) is depracted");
  }

  /**
  ** @deprecated
  */
  public static Class loadClass(String name) throws MalformedURLException, ClassNotFoundException {
    throw new wonka.vm.DeprecatedMethodError("RMIClassLoader.loadClass(java.lang.String) is depracted");
  }
  
  public static Class loadClass(String codebase, String name) throws MalformedURLException, ClassNotFoundException {
    if(USE_CODEBASE_ONLY){
      codebase = DEFAULT_CODEBASE;
    }
    return Class.forName(name, true, getClassLoader(codebase));
  }
  
  public static Class loadClass(URL codebase, String name) throws MalformedURLException, ClassNotFoundException {
    return loadClass(codebase.toString(), name);
  }

  private static class CleanupTask extends TimerTask {

    public CleanupTask(){
      SystemTimer.scheduleSystemTask(this, 10000);
    }

    public void run(){
      Reference ref = queue.poll();
      while(ref != null){
        synchronized(classLoaders){
          Iterator it = classLoaders.entrySet().iterator();
          try {
            Map.Entry entry = (Map.Entry)it.next();
            if(ref == entry.getValue()){
              it.remove();
            }
          }
          catch(java.util.NoSuchElementException nsee){}
          if(classLoaders.size() == 0){
            queue = null;
            this.cancel();
            break;
          }
        }
        ref = queue.poll();
      }
    }
  }
}

