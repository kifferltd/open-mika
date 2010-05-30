/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2008, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
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

package java.util;

import java.io.IOException;
import java.io.InputStream;
public abstract class ResourceBundle {

  private Locale locale;

  protected ResourceBundle parent=null;

  protected void setParent(ResourceBundle p) {
    parent = p;
  }

  protected abstract Object handleGetObject(String key) throws MissingResourceException;

  private static final Hashtable cache = new Hashtable();
  private static final char underscore = '_';

  public abstract Enumeration getKeys();

  public Locale getLocale(){
    return locale;
  }

  public final Object getObject(String key) throws MissingResourceException {
    ResourceBundle check = this;
    Object o = null;
    while (check != null) {
      o = check.handleGetObject(key);
      if (o != null ) {
     	  return o;
      }
      check = check.parent;
    }
    throw new MissingResourceException(this + ": key '" + key + "' not found",this.getClass().getName(),key);
  }

  public final String getString(String key) throws MissingResourceException {
   	return (String) getObject(key);
  }

  public final String[] getStringArray(String key) throws MissingResourceException {
   	return (String[]) getObject(key);
  }

  public static final ResourceBundle getBundle(String baseName) throws MissingResourceException {
  	return getBundle(baseName, Locale.getDefault(), getCallingClassLoader(), true);
  }	

  public static final ResourceBundle getBundle(String baseName, Locale locale) throws MissingResourceException {
  	return getBundle(baseName, locale, getCallingClassLoader(), locale.equals(Locale.getDefault()));
  }

  public static final ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) throws MissingResourceException {
  	return getBundle(baseName, locale, loader, locale.equals(Locale.getDefault()));
  }

  /**
  ** this method calls a function in ClassLoader.c. This against the convention but avoids code duplication,
  ** has better performance, ...
  */
  private static native ClassLoader getCallingClassLoader();

  private static String cutEnd(String name) {
    //System.out.println("cutting String ..."+name);
    int i = name.lastIndexOf(underscore);
    return ( i == -1 ? "" : name.substring(0,i));       	  	
  }

  /**
   ** Find or create the resource bundle corresponding to a given 'baseName'.
   ** We search for bundles with the following names:
   ** <ul><li>basename_lang_country_variant 
   **     <li>basename_lang_country 
   **     <li>basename_lang
   **     <li>if the locale is not the default locale, the same three names
   **         but for the default locale
   **     <li>basename
   ** </ul>
   ** For each name we try to find first a class ans then a .properties file.
   ** <p>[CG 20090108] Added negative caching to avoid repeated attempts
   ** to load the same non-existent class or file. Note that this may not be
   ** the Right Thing to do if users expect to be able to dynamically add
   ** new resource bundles to the classpath. (In my view it would be better
   ** to define a separate, OSGi-friendly API for this).
   */
  private static final ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader, boolean defaultLoc)
    throws MissingResourceException {
    boolean inNegativeCache;

    if (baseName == null){
      throw new NullPointerException();
    }
    Class rbClass=null;
    ResourceBundle newBundle=null;	
          	
    String local = baseName+"_"+locale.toString();
    while (true) {
      inNegativeCache = false;
      Object cached = cache.get(local);
      if (cached != null) {
        if (cached instanceof String) {
          inNegativeCache = true;
        }
        else {
          newBundle = (ResourceBundle)cached;
          return newBundle; // found in cache
        }
      }
      else {
        try {
          rbClass =  Class.forName(local, true, loader);
          newBundle = (ResourceBundle)rbClass.newInstance();
          break;
        }
        catch(Exception cnfe){
        }
      }

      // Not found in positive cache; either found in negative cache or we 
      // tried to load the class and couldn't. Time to try something else ...
      // How about a properties file ?
      InputStream in = (loader == null ? ClassLoader.getSystemResourceAsStream(local.replace('.','/')+".properties") :
      loader.getResourceAsStream(local.replace('.','/')+".properties"));
      if (in == null) { // no properties file --> we cut down our string
        if (!inNegativeCache) {
          cache.put(local, baseName);
        }
        local = cutEnd(local);
        if ((local.equals(baseName)&& !defaultLoc)||local.equals("")) {
          if (!defaultLoc) {
            defaultLoc = true;
            local = baseName+"_"+Locale.getDefault().toString();
          }
          else {
            throw new MissingResourceException("couldn't find resourceBundle '" + baseName + "' using locale '" + locale + (defaultLoc ? "' or default" : "'"), baseName,locale.toString());
          }
        }
        continue;
      }
      // we have a properties file ...
      try {
        newBundle = new PropertyResourceBundle(in);
        break; //leave the WHILE- loop
      }
      catch(IOException ioe) {
        // something went wrong ??? --> lets look for something else ...
        continue;
      } 			
    }

    // We found a ResourceBundle now search for the parents ...      		
    newBundle.setParent(findParent(cutEnd(local), loader));
    newBundle.locale = locale;
    cache.put(local,newBundle);
    return newBundle;
  }

  private static ResourceBundle findParent(String name, ClassLoader loader) {
    //System.out.println("Looking for parent ..."+name);
    if (name.equals("")) return null;
    ResourceBundle rb = (ResourceBundle) cache.get(name);
    if (rb != null) {
      //System.out.println("found parent in the cache ..."+name);
      return rb;
    }
    try {
      rb = (ResourceBundle)Class.forName(name, true, loader).newInstance();
    }
    catch (Exception e) {
      // we don't seem to able to load the direct parent --> maybe in a prop. file
      try {
        rb = new PropertyResourceBundle(loader == null ?
        ClassLoader.getSystemResourceAsStream(name.replace('.','/')+".properties"):
        loader.getResourceAsStream(name.replace('.','/')+".properties"));
      }
      // no class or properties file ==> no parent (we return null)	
      catch (Exception pe) { return null; }
    }
    rb.setParent(findParent(cutEnd(name), loader));
    cache.put(name,rb);
    return rb;  	
  }
}
