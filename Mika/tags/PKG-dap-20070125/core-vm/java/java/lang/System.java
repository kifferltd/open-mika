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

/*
** $Id: System.java,v 1.13 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.PropertyPermission;

public final class System {

  public final static InputStream in;
  public final static PrintStream out;
  public final static PrintStream err;

  private static SecurityManager theSecurityManager;
  private static Runtime theRuntime;
  private static Properties systemProperties;
  private static boolean initialized;

  static {
    theRuntime = Runtime.getRuntime();
    systemProperties = new Properties(new DefaultProperties());

    try {
      InputStream syspropstream = ClassLoader.getSystemResourceAsStream("system.properties");
      if (syspropstream != null) {
        systemProperties.load(syspropstream);
      }
      String mikaprops = systemProperties.getProperty("mika.properties");
      if (mikaprops == null) {
        mikaprops = "mika.properties";
      }
      syspropstream = ClassLoader.getSystemResourceAsStream(mikaprops);
      if (syspropstream != null) {
        systemProperties.load(syspropstream);
      }
      // if not found we still set the 'loaded' property, as if we found
      // a trivial or empty file.
      systemProperties.setProperty("mika.properties.loaded", "true");
    }
    catch (IOException e) {}

    parseCmdLineProperties();
    InputStream sin = null;
    try {
      sin = new wonka.io.StandardInputStream();
    } catch(Throwable t){
      t.printStackTrace();      
    }
    in = sin;
    PrintStream sout = null;
    try {
      boolean  system_out_autoflush = Boolean.valueOf(getProperty("mika.out.autoflush", "true")).booleanValue();
      sout = new PrintStream(new wonka.io.StandardOutputStream(), system_out_autoflush);
    } catch(Throwable t){
      t.printStackTrace();      
    }
    out = sout;
    PrintStream serr = null;
    try {
      boolean  system_err_autoflush = Boolean.valueOf(getProperty("mika.err.autoflush", "true")).booleanValue();
      serr = new PrintStream(new wonka.io.ErrorOutputStream(), system_err_autoflush);
    } catch(Throwable t){
      t.printStackTrace();      
    }
    err = serr;
    try {    
      //FORCE SOME CALLS things that are postponed until the system properties were set up correctly
      ClassLoader.get_defaultProtectionDomain();
      ClassLoader.createApplicationClassLoader();

      //SETUP DEFAULT LOCALE
      java.util.Locale.setDefault(new java.util.Locale(getProperty("user.language", "en"), getProperty("user.country", "")));
      
      String theManager = getProperty("java.security.manager");
      if("".equals(theManager) || "default".equals(theManager)) {
        theSecurityManager = new SecurityManager();
      }
      else {
        // TODO: Use the given class..
      }
      
      initialized = true;
    }
    catch(Throwable t){
      t.printStackTrace();
    }

    if (!initialized) {
      exit(1);
    }
  }

  private static native String[] getCmdLineProperties();

  private static void parseCmdLineProperties() {
    String[] props = getCmdLineProperties();
    if(props != null) {
      for(int i=0; i < props.length; i++) {
        String key, value;
        int equals_at = props[i].indexOf('=');
        if (equals_at < 0) {
          key = props[i];
          wonka.vm.Etc.woempa(10,"SystemInit: Setting system property '"+key+"' to \"\"");
          systemProperties.put(key,"");
        }
        else if (equals_at == 0) {
          wonka.vm.Etc.woempa(10,"SystemInit: Ignoring nameless property in "+props[i]);
        }
        else if (equals_at == props[i].length()-1) {
          key = props[i].substring(0,equals_at);
          wonka.vm.Etc.woempa(10,"SystemInit: Setting system property '"+key+"' to \"\"");
          systemProperties.put(key,"");
        }
        else {
          key = props[i].substring(0, equals_at);
          value = props[i].substring(equals_at + 1);
          wonka.vm.Etc.woempa(10,"SystemInit: Setting system property '"+key+"' to '"+value+"'");
          systemProperties.put(key,value);
        }
      }
    }
  }

  public static String getenv(String name) {
    return getProperty(name);
  }
  
  public static void runFinalizersOnExit(boolean run) {
    Runtime.runFinalizersOnExit(run);
  }
  
  /**
   ** Initialise the internal table of native properties.
   ** Must be called before any call to getNativeProperty().
   */
  private static native void initNativeProperties();

  /**
   ** Release the internal table of native properties.
   ** Should be called after all calls to getNativeProperty() have been made.
   */
  private static native void termNativeProperties();

  /**
   ** Get a system property which is determined by the OS, build parameters,
   ** or whatever.
   ** @param name The name of the system property.
   ** @result The value of the system property, or null if name is not known.
   */
  private static native String getNativeProperty(String name);

  public static void setIn(InputStream newIn) {
    if (in!=null) {
      try {
        in.close();
      } catch (java.io.IOException e) {}
    }
    setField("in", newIn);
  }

  private static void setField(String name, Object value) {
    try {      
      Field field = System.class.getDeclaredField(name);
      field.setAccessible(true);
      try {
        field.set(null, value);
      }catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }    
    
  }
  
  public static void setOut(PrintStream newOut) {
    if (out!=null) {
        out.close();
    }
    setField("out", newOut);
  }

  public static void setErr(PrintStream newErr) {
    if (err!=null) {
        err.close();
    }
    setField("err", newErr);
  }

  public static SecurityManager getSecurityManager() { 
    return theSecurityManager;
  }

  public static void setSecurityManager(SecurityManager sm)
    throws SecurityException
  { 
    if (theSecurityManager!=null) {
      if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER
       || wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
        java.security.AccessController.checkPermission(new RuntimePermission("setSecurityManager"));
      }
    }
    theSecurityManager = sm;
  }

  public static native long currentTimeMillis();

  private static void propertyCheck(String propname) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new PropertyPermission(propname, "read"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      if (theSecurityManager != null) {
        theSecurityManager.checkPropertyAccess(propname);
      }
    }
  }

  private static void propertiesCheck() {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new PropertyPermission("*", "read,write"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      if (theSecurityManager != null) {
        theSecurityManager.checkPropertiesAccess();
      }
    }
  }

  public static Properties getProperties() 
    throws SecurityException
  {
    propertiesCheck();

    return systemProperties;
  }

  public static void setProperties(Properties props) 
    throws SecurityException
  {
    propertiesCheck();

    systemProperties = props;
  }


  public static String getProperty(String key) 
    throws SecurityException
  {
    propertyCheck(key);

    try {

      return systemProperties.getProperty(key);

    }
    catch (NullPointerException npe) {

      return null;

    }
  }

    
  public static String getProperty(String key, String defaults)
    throws SecurityException
  {
    propertyCheck(key);

    try {

      return systemProperties.getProperty(key,defaults);

    }
    catch (NullPointerException npe) {

      return defaults;

    }
  }

  public static String setProperty(String key, String defaults)
    throws SecurityException
  {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new PropertyPermission(key, "write"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      if (theSecurityManager != null) {
        theSecurityManager.checkPermission(new PropertyPermission(key, "write"));
      }
    }
    return (String)systemProperties.setProperty(key,defaults);
  }


  public static void exit(int status) 
    throws SecurityException
  {
    theRuntime.exit(status);
  }

  public static void gc() {
    theRuntime.gc();
  }

  public static void runFinalization() {
    theRuntime.runFinalization();
  }

  public static void load(String libname) 
    throws SecurityException, UnsatisfiedLinkError
  {
    throw new UnsatisfiedLinkError("not implemented");
  }

  public static void loadLibrary(String libname) 
    throws SecurityException, UnsatisfiedLinkError
  {
    // throw new UnsatisfiedLinkError("not implemented");
    Runtime.getRuntime().loadLibrary(libname);
  }
  
  public static String mapLibraryName(String libname) {
    return "lib" + libname + ".so";
  }

  native public static void arraycopy(Object src, int srcOffset, Object dst, int dstOffset, int length)
    throws NullPointerException, ArrayStoreException, ArrayIndexOutOfBoundsException;

  public static native int identityHashCode(Object o);

  private System() {}
}
