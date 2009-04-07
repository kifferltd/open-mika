/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded Java   *
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 ** Class Init is a utility class used to launch the garbage collection,
 ** finaliser, and heartbeat threads, and to run the starting class or
 ** jarfile.
 */

final class Init {

  private static boolean verbose;

  private static Method invoke_method;
  private static Object[] invoke_args;
  private static boolean launched;
  private static URLClassLoader application_class_loader;
  private static String [] start_class_args;
  private static String jar_class_path;

  private static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  /**
   ** Take one token from the argument list and process it as a system property
   ** (begins with -D) or a normal argument.  Normal arguments are accumulated
   ** in temp_args.
   */
  private static void argumentOrSysProp(String this_arg, Vector temp_args) {
    if (this_arg.startsWith("-D")) {
      Properties sysprops = System.getProperties();
      String key, value;
      int equals_at = this_arg.indexOf('=');
      if (equals_at < 0) {
        key = this_arg.substring(2);
        debug("Init: Deleting system property '"+key+"'");
        sysprops.remove(key);
      }
      else if (equals_at == 0) {
        System.err.println("Init: Ignoring nameless property in "+this_arg);
      }
      else if (equals_at == this_arg.length()-1) {
        key = this_arg.substring(2,equals_at);
        debug("Init: Deleting system property '"+key+"'");
        sysprops.remove(key);
      }
      else {
        key = this_arg.substring(2, equals_at);
        value = this_arg.substring(equals_at + 1);
        debug("Init: Setting system property '"+key+"' to '"+value+"'");
        sysprops.put(key,value);
      }
    }
    else {
      debug("Init: passing argument "+this_arg);
      temp_args.addElement(this_arg);
    }
  }

  /**
   ** Take all the arguments in temp_args and make them into an array of
   ** String which can be passed to the starting class's main() method.
   */
  private static String[] marshallArgs(Vector temp_args) {
    String[] args = new String[temp_args.size()];

    for (int i = 0; i < temp_args.size(); ++i) {
      args[i] = (String)temp_args.elementAt(i);
    }

    return args;
  }

  /**
   ** Get the name of the Main-Class in a jar file, with the path to the
   ** jar file prepended. Return null if no mainifest or no Main-Class
   ** attribute found.
   ** If a Class-Path attribute is found, its value is parsed and every
   ** element found is added to the URL list of the Application Class Loader
   ** (after prepending the path to the jar file).
   */
  private static String getJarStartClassName(String[] args) {
    String start_class_name = null;
    if(args.length < 2){
      System.err.println("Init: -jar option is used but no JarFile is mentioned");

      return null;

    }

    try {
      File file = new File(args[1]);
      JarFile jf = new JarFile(file);
      Manifest man = jf.getManifest();
      if(man == null){
        System.err.println("Init: JarFile "+args[1]+" has no Manifest");

        return null;

      }
      start_class_name = man.getMainAttributes().getValue("Main-Class");
      if(start_class_name == null){
        System.err.println("Init: no Main-Class attribute in Manifest of JarFile "+args[1]);

        return null;

      }
      String jarpath;
      int jarpathend = args[1].lastIndexOf('/');
      if (jarpathend < 0) {
        jarpath = "";
      }
      else {
        jarpath = args[1].substring(0, jarpathend + 1);
      }
      URL url = new URL("jar:"+file.toURL()+"!/");
      Method m = URLClassLoader.class.getDeclaredMethod("addURL",  new Class[]{url.getClass()});
      m.setAccessible(true);
      m.invoke(application_class_loader,new Object[]{url});
      String jarclasspath = man.getMainAttributes().getValue("Class-Path");
      if (jarclasspath != null) {
        debug("Jarfile " + file + " has Class-Path " + jarclasspath);
        StringTokenizer toks = new StringTokenizer(jarclasspath);
        try {
          while (true) {
            file = new File(jarpath + toks.nextToken());
            url = new URL("jar:"+file.toURL()+"!/");
            debug("  Appending " + url + " to application class path");
            m.invoke(application_class_loader,new Object[]{url});
          }
        }
        catch (NoSuchElementException nsee) {
          debug("Finished with jarfile Class-Path");
        }
      }
    }
    catch(Exception e){
      System.err.println("Init: failed to load jar "+args[1]+" due to "+e);
      e.printStackTrace();

      return null;

    }

    return start_class_name;
  }

  /**
   ** Sift the array raw_args, using -D arguments to modify system properties
   ** and returning an array which contains the remaining arguments. Both 
   ** raw_args and the result returned may be zero-length, but they may not be null.
   */
  private static String[] processArgsArray(String[] raw_args, int offset) {
    Vector temp_args = new Vector();

    for (int i = offset; i < raw_args.length; ++i) {
      argumentOrSysProp(raw_args[i],temp_args);
    }

    return marshallArgs(temp_args);
  }

  /**
   ** This function is invoked directly when the VM is created.  First we 
   ** parse the command line to extract the name of the starting class and
   ** any changes to system properties; the remaining arguments will be passed
   ** on to the starting class when it has been loaded.  Then we try to load
   ** starting class and to find its main(String[]) method.  If this succeeds
   ** then we first install the SecurityMAnager if required and launch some 
   ** housekeeping threads and then invoke the main(String[]) method.  Lastly,
   ** if the main(String[]) method of the starting class terminates abnormally
   ** we pass the exception thrown to the UncaughtExceptionHandler of the
   ** system thread group.
   */
  private static void main(String[] args) {
    String[] effective_args = args;
    String start_class_name = null;
    Class start_class = null;

    if(args == null || args.length == 0 || args[0] == null) {
      String start_command = System.getProperty("mika.default.commandline");
      if (start_command != null && start_command.length() != 0){
        StringTokenizer start_toks = new StringTokenizer(start_command);
        ArrayList l = new ArrayList();

        while (start_toks.hasMoreTokens()) {
          l.add(start_toks.nextToken());
        }

        effective_args = (String[])l.toArray(new String[0]);
        l = null;
      }
      else {
        System.err.println("Init: no command line parameters found. Game over.");
        System.exit(1);
      }
    }

    String verboseProperty = System.getProperty("mika.verbose","");
    Wonka.setWonkaVerbose(verboseProperty);
    debug("Init: loading extensions");
    Wonka.loadExtensions();

    String debugLineNumbers = System.getProperty("mika.debug.line.numbers", "");
    if (debugLineNumbers.equalsIgnoreCase("true")) {
      Wonka.setMethodDebugInfo(true);
    }
    else if (debugLineNumbers.equalsIgnoreCase("false")) {
      Wonka.setMethodDebugInfo(false);
    }

    if (verboseProperty.indexOf("startup") >= 0) {
        verbose = true;
    }

    application_class_loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    
    Thread.currentThread().setContextClassLoader(application_class_loader);
    
    if (effective_args[0].equals("-jar")) {
      debug("Init: '-jar' is used");
      jar_class_path = effective_args[1];
      start_class_name = getJarStartClassName(effective_args);
      start_class_args = processArgsArray(effective_args, 2);
    }
    else {
      start_class_name = effective_args[0];
      start_class_args = processArgsArray(effective_args, 1);
    }

    if (start_class_name == null) {
      System.err.println("Init: no start class named.  Game over.");
      System.exit(1);
    }

    invoke_args = new Object[1];
    invoke_args[0] = start_class_args;

    try {
      start_class = Class.forName(start_class_name, false, application_class_loader);
      debug("Init: launcher is " + start_class + ", loaded by " + application_class_loader);
    } catch (ClassNotFoundException e) {
      System.err.println("Init: no such class as "+start_class_name+" in class path!");
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }

    if (start_class == null) {
      System.err.println("Init: no start class found.  Game over.");
      System.exit(1);
    }

    try {
      invoke_method = start_class.getMethod("main",new Class[]{Class.forName("[Ljava.lang.String;")});

      if(invoke_method == null) {
        System.err.println("Init: no 'main' method start class "+start_class+" found.  Game over.");
        System.exit(1);        
      }
      
      if (!Modifier.isStatic(invoke_method.getModifiers())) {
        System.err.println("Init: " + invoke_method + " is not static.  Game over.");
        System.exit(1);
      }

      debug("Init: will invoke " + invoke_method);
      invoke_method.setAccessible(true);
    } catch (SecurityException e) {
      System.err.println("Init: unable to invoke " + invoke_method + ": " + e);
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      System.err.println("Init: unable to invoke " + invoke_method + ": " + e);
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      System.err.println("Init: unable to invoke " + invoke_method + ": " + e);
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
   

    if (invoke_method == null) {
      System.err.println("Init: no 'main(String[])' method found. Game over.");
      System.exit(1);
    }

  // Install the default SecurityManager if required
    if (SecurityConfiguration.SET_SECURITY_MANAGER) {
      debug("Init: installing SecurityManager");
      if (SecurityConfiguration.USE_ACCESS_CONTROLLER) {
        debug("Init: note that Wonka libraries will not use this SecurityManager, but will call AccessController directly."); 
      }
      else if (!SecurityConfiguration.USE_SECURITY_MANAGER) {
        debug("Init: note that Wonka libraries will not use this SecurityManager (security checks are disabled)."); 
      }
      System.setSecurityManager(new SecurityManager());
    }
    else {
      String theManager = System.getProperty("java.security.manager");
      if("".equals(theManager) || "default".equals(theManager)) {
        System.setSecurityManager(new SecurityManager());
      }
      else if (theManager != null) {
        try {
          System.setSecurityManager((SecurityManager)Class.forName(theManager, true, ClassLoader.getSystemClassLoader()).newInstance());
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  // Start up the Garbage Collector
    debug("Init: starting Garbage Collector");
    GarbageCollector gc = GarbageCollector.getInstance();
  // Start up the Heartbeat
    debug("Init: starting Heartbeat");
    Heartbeat h = Heartbeat.getInstance();
  // Start JDWP (does nothing if JDWP not compiled in)
    JDWP.getInstance();
  // Set the default timezone to the default default
    String user_timezone = System.getProperty("user.timezone", "GMT");
    debug("Init: user.timezone = " + user_timezone);
    TimeZone defaultTimeZone = TimeZone.getTimeZone(user_timezone);
    if (defaultTimeZone == null) {
      System.err.println("Unable to find the default timezone '" + user_timezone + "': check the system.property 'user.timezone' and the mika.timezones file!");
      defaultTimeZone = TimeZone.getTimeZone("GMT");
    }
    debug("Init: setting default TimeZone to " + defaultTimeZone);
    TimeZone.setDefault(TimeZone.getTimeZone(user_timezone));
    if (jar_class_path != null) {
      // [CG 20060330] HACK to make java.class.path to look right when -jar is used
      System.setProperty("java.class.path", jar_class_path);
    }

    try {
      debug("Init: invoking "+invoke_method+" ...");
      invoke_method.invoke(null,invoke_args);
      launched = true;
    } catch (IllegalAccessException e) {
      System.err.println("Init: error invoking "+invoke_method+"! got "+e);
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      Thread this_thread = Thread.currentThread();
      this_thread.getThreadGroup().uncaughtException(this_thread,e.getTargetException());
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    finally {
      if (!launched) {
        System.exit(1);
      }
    }
  }

}

