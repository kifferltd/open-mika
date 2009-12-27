/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
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

package java.net;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Serializable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.security.Permission;
import java.security.AllPermission;

public abstract class URLConnection {

  private static boolean defaultAllowUserInteraction;
  private static boolean defaultUseCaches;
  private static FileNameMap fileNameMap = new DefaultFileNameMap();
  private static ContentHandlerFactory factory;

  protected boolean allowUserInteraction=defaultAllowUserInteraction;
  protected boolean connected;
  protected boolean doInput = true;
  protected boolean doOutput;
  protected boolean useCaches=defaultUseCaches;

  protected long ifModifiedSince;
  protected URL url;

  private String contentType;

  protected URLConnection(URL url){
    	this.url = url;
  }

//abstract methods ...

  public abstract void connect() throws IOException;

//static methods ...

  public static String guessContentTypeFromName(String filename){
    return getFileNameMap().getContentTypeFor(filename);
  }

  public static boolean getDefaultAllowUserInteraction() {
	return defaultAllowUserInteraction;
  }

  public static FileNameMap getFileNameMap() {
    if (fileNameMap == null) {
      fileNameMap = new DefaultFileNameMap();
    }

    return fileNameMap;
  }

  public static void setContentHandlerFactory(ContentHandlerFactory fac){
  	if (fac == null) {
  	 	throw new NullPointerException();
  	}
  	
       SecurityManager sManager = System.getSecurityManager();
        if (sManager != null) {
            sManager.checkSetFactory();
        }

  	if (factory != null) {
  	 	throw new Error("factory already set");
  	}
  	factory = fac;
  }

  public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
    	defaultAllowUserInteraction = defaultallowuserinteraction;
  }

  public static void setDefaultRequestProperty(String key, String value){}

  public static void setFileNameMap(FileNameMap map){
        SecurityManager manager = System.getSecurityManager();
        if (manager != null) {
            manager.checkSetFactory();
        }

  	if (map == null) { throw new NullPointerException(); }
  	fileNameMap = map;
  }

/**
**	default implementation returns null
**	@ remark sublcasses should override this method
*/
  public static String getDefaultRequestProperty(String key) {
   	return null;
  }

//Object methods

  public boolean getAllowUserInteraction(){
   	return allowUserInteraction;
  }

  public boolean getDoInput(){
   	return doInput;
  }

  public boolean getDoOutput(){
   	return doOutput;
  }

  public long getIfModifiedSince() {
	return ifModifiedSince;
  }

/**
**	default implementation throws an UnknownServiceException
**	@ remark sublcasses should override this method
*/
  public InputStream getInputStream() throws IOException {
   	throw new UnknownServiceException();
  }

/**
**	default implementation throws an UnknownServiceException
**	@ remark sublcasses should override this method
*/
  public OutputStream getOutputStream() throws IOException {
   	throw new UnknownServiceException();
  }

/**
**	default implementation returns null
**	@ remark sublcasses should override this method
*/
  public String getHeaderFieldKey(int n) {
   	return null;
  }

/**
**	default implementation returns null
**	@ remark sublcasses should override this method
*/
  public String getHeaderField(int n){
   	return null;
  }

/**
**	default implementation returns empty map
**	@ remark sublcasses should override this method
*/
  public Map getHeaderFields() {
    return new ImmutableEmptyMap();
  }

/**
**	default implementation returns null
**	@ remark sublcasses should override this method
*/
  public String getHeaderField(String name){
   	return null;
  }

  public long getDate(){
   	return getHeaderFieldDate("Date",0);
  }
  public boolean getDefaultUseCaches(){
    	return defaultUseCaches;
  }
  public long getExpiration(){
        return getHeaderFieldDate("Expires",0);
  }	
  public long getLastModified() {
        return getHeaderFieldDate("Last-Modified",0);
  }

/**
**	default implementation returns null
**	@ remark sublcasses should override this method
*/
  public String getRequestProperty(String key){
    if (connected) {
      throw new IllegalStateException();
    }
 
    return null;
  }

/**
**	default implementation returns empty map
**	@ remark sublcasses should override this method
*/
  public Map getRequestProperties(){
    if (connected) {
      throw new IllegalStateException();
    }

    return new ImmutableEmptyMap();
  }

/**
 ** Here's a silly one - default impl does nothing and subclasses should 
 ** override *but* the API specifies some exceptions so we throw them.
 */
  public void addRequestProperty(String field, String newValue) {
    if (connected) {
      throw new IllegalStateException();
    }
    if (field == null) {
      throw new NullPointerException();
    }
  }

/**
**	SubClasses must override this method ...	
*/
  public Permission getPermission() throws IOException {
   	return new AllPermission();
  }

  public URL getURL(){
   	return url;
  }
  public boolean getUseCaches(){
   	return useCaches;
  }

  public void setAllowUserInteraction(boolean allowuserinteraction) {
   	if (connected) {
   	 	throw new IllegalStateException("property cannot be set after connecting");
   	}
   	allowUserInteraction = allowuserinteraction;
  }

  // MAJOR SUN BREAKAGE
  // Clearly the method should be static but it ain't. See Sun bug 4851466.
  // Apache Harmony throws an IllegalAccessException if this instance is
  // already connected, but this doesn't solve the basic problem that any
  // code anywhere can call it (probably in the mistaken belief that it
  // only applies to the current instance). O well ...
  public void setDefaultUseCaches(boolean defaultusecaches){
   	defaultUseCaches = defaultusecaches;
  }

  public void setDoInput(boolean doinput){
   	if (connected) {
   	 	throw new IllegalStateException("property cannot be set after connecting");
   	}
   	doInput = doinput;
  }

  public void setDoOutput(boolean dooutput){
   	if (connected) {
   	 	throw new IllegalStateException("property cannot be set after connecting");
   	}
   	doOutput = dooutput;
  }

  public void setIfModifiedSince(long ifmodifiedsince){
   	if (connected) {
   	 	throw new IllegalStateException("property cannot be set after connecting");
   	}
  	ifModifiedSince = ifmodifiedsince;
  }

  public void setUseCaches(boolean usecaches){
   	if (connected) {
   	 	throw new IllegalStateException("property cannot be set after connecting");
   	}
   	useCaches = usecaches;
  }

/**
**	default implementation does nothing
**	@ remark sublcasses should override this method
*/
  public void setRequestProperty(String key, String value){  }

  public String toString(){
   	return getClass().getName() + ":" + url.toString();
  }

  public long getHeaderFieldDate(String name, long dflt) {
    String date = getHeaderField(name);
    if (date == null) {
      return dflt;
    }
    try {
      return Date.parse(date);
    } catch (Exception e) {
      return dflt;
    }
  }
 
  public int getHeaderFieldInt(String name, int dflt) {
   	try {
   		return Integer.parseInt(getHeaderField(name));
   	}
   	catch(RuntimeException e) { // we expect a NullPointerException or a NumberFormatException
	   	return dflt;
	  }
  }

  public String getContentEncoding() {
   	return getHeaderField("Content-Encoding");
  }

  public int getContentLength(){
   	return getHeaderFieldInt("Content-Length",-1);
  }
  public String getContentType() {
   	return getHeaderField("Content-Type");
  }


  public static String guessContentTypeFromStream(InputStream is) throws IOException {
    if (!is.markSupported()) {
      return null;
    }
    // Look ahead up to 64 bytes for the longest encoded header
    is.mark(64);
    byte[] bytes = new byte[64];
    int length = is.read(bytes);
    is.reset();
    // TODO: Apache Harmony checks for Unicode BOM here

    if ("PK".equals(new String(bytes, 0, 2))) {
      return "application/zip";
    }
    if ("GIF".equals(new String(bytes, 0, 3))) {
      return "image/gif";
    }
    if ("PNG".equals(new String(bytes, 1, 3))) {
      return "image/png";
    }
    if ("JFIF".equals(new String(bytes, 4, 4))) {
      return "image/jpeg";
    }
    if (bytes[0] == '<') {
      if ("!DOCTYPE HTML".equalsIgnoreCase(new String(bytes, 1, 13))) {
        return "text/html";
      }
      String foo = new String(bytes, 1, 4).toUpperCase();
      if ("HTML".equals(foo) || "HEAD".equals(foo) || "BODY".equals(foo)) {
        return "text/html";
      }
      if ("?XML".equalsIgnoreCase(new String(bytes, 1, 4))) {
        return "application/xml";
      }
    }

    return null;
  }

  public Object getContent() throws IOException {
    if (!connected) {
      connect();
    }

    contentType = getContentType();
System.out.println(this + ".getContent(): contentType = " + contentType);
    if (contentType == null) {
      contentType = guessContentTypeFromName(url.getFile());
System.out.println(this + ".getContent(): contentType = " + contentType);
      if (contentType == null) {
         contentType = guessContentTypeFromStream(new BufferedInputStream(getInputStream()));
System.out.println(this + ".getContent(): contentType = " + contentType);
        if (contentType == null) {
          return null;
        }
      }
    }
System.out.println(this + ".getContent(): contentType = " + contentType);
System.out.println(this + ".getContent(): handler = " + getContentHandler(contentType));

    return getContentHandler(contentType).getContent(this);
  }
 
  public Object getContent(Class[] classes) throws IOException {
    if (!connected) {
      connect();
    }

    contentType = getContentType();
    if (contentType == null) {
      contentType = guessContentTypeFromName(url.getFile());
      if (contentType == null) {
        contentType = guessContentTypeFromStream(new BufferedInputStream(getInputStream()));
        if (contentType == null) {
          return null;
        }
      }
    }

    return getContentHandler(contentType).getContent(this, classes);
  }

  private ContentHandler getContentHandler(String type) throws IOException {
    ContentHandler cth;
System.out.println(this + ".getContentHandler(" + type + "): factory = " + factory);
    if (factory != null) {
      cth = factory.createContentHandler(type);
      if (!(cth instanceof ContentHandler)) {
        throw new UnknownServiceException();
      }

      if (cth != null) {
        return cth;
      }
    }
    //get wonka classes to save the day (or at least try ...)
    // TODO: import logic from Apache Harmony implementation?
System.out.println(this + ".getContentHandler(" + type + "): (new wonka.net.DefaultContentHandlerFactory()).createContentHandler(" + type + ")");
    cth = (new wonka.net.DefaultContentHandlerFactory()).createContentHandler(type);
    if(cth != null) {
      return cth;
    }
    //too bad..
    return null;
  }


  private static class DefaultFileNameMap implements FileNameMap {

  	private ResourceBundle filenameMap;

  	public DefaultFileNameMap() {
		try {
			filenameMap = ResourceBundle.getBundle("wonka.net.MimeTypeMap");			 	
		}	
		catch(MissingResourceException mre){}	  	
  	}
  	
  	
    public String getContentTypeFor(String filename){
System.out.println(this + "getContentTypeFor(" + filename + "): filenameMap = " + filenameMap);
      if (filenameMap != null) {
        String extension = "html";
          if (!filename.endsWith("/")) {
            int lastCharInExtension = filename.lastIndexOf('#');
            if (lastCharInExtension < 0) {
              lastCharInExtension = filename.length();
            }
            int firstCharInExtension = filename.lastIndexOf('.') + 1;
            if (firstCharInExtension > filename.lastIndexOf('/')) {
               extension = filename.substring(firstCharInExtension, lastCharInExtension);
            }
            else {
              extension = "";
            }
          }

          try {
             return filenameMap.getString(filename);
          }	
          catch(MissingResourceException mre){}	  	
        }      	
      return null;  	
    }
  }

  private static class ImmutableEmptyMap extends AbstractMap implements
            Serializable {
    private static final long serialVersionUID = 6428348081105594320L;

    Set emptySet;

    public boolean containsKey(Object key) {
      return false;
    }

    public boolean containsValue(Object value) {
      return false;
    }

    public Set entrySet() {
      return new ImmutableEmptySet();
    }

    public Object get(Object key) {
      return null;
    }

    public Set keySet() {
      return new ImmutableEmptySet();
    }

    public Collection values() {
      return new ImmutableEmptySet();
    }
  }

  private static class ImmutableEmptySet extends AbstractSet implements
            Serializable {
    private static final long serialVersionUID = 1582296315990362920L;

    public boolean contains(Object object) {
      return false;
    }

    public int size() {
      return 0;
    }

    public Iterator iterator() {
      return new Iterator() {
        public boolean hasNext() {
          return false;
        }

        public Object next() {
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

}
