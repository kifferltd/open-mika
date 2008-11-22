/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2007, 2008 by Chris Gray, /k/ Embedded  *
* Java Solutions. All rights reserved.                                    *
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
    in = new wonka.io.StandardInputStream();
    // We make 'out' non-autoflush and 'err' autoflush, by analogy with Posix.
    out = new PrintStream(new wonka.io.StandardOutputStream(), false);
    err = new PrintStream(new wonka.io.ErrorOutputStream(), true);
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
    if(key.equals("")) {
      throw new IllegalArgumentException();
    }
    return systemProperties.getProperty(key);
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
