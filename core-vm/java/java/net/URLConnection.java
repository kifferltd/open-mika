/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.net;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.security.Permission;
import java.security.AllPermission;

public abstract class URLConnection {

  private static boolean defaultAllowUserInteraction=false;
  private static boolean defaultUseCaches=false;
  private static FileNameMap fileNameMap= new DefaultFileNameMap();
  private static ContentHandlerFactory factory=null;

  protected boolean allowUserInteraction=defaultAllowUserInteraction;
  protected boolean connected;
  protected boolean doInput = true;
  protected boolean doOutput;
  protected boolean useCaches=defaultUseCaches;

  protected long ifModifiedSince;
  protected URL url;

  protected URLConnection(URL url){
    	this.url = url;
  }

//abstract methods ...

  public abstract void connect() throws IOException;

//static methods ...

  public static String guessContentTypeFromName(String fname){
   	return fileNameMap.getContentTypeFor(fname);
  }

  public static boolean getDefaultAllowUserInteraction() {
	return defaultAllowUserInteraction;
  }

  public static FileNameMap getFileNameMap() {
  	return fileNameMap;
  }

  public static void setContentHandlerFactory(ContentHandlerFactory fac){
  	if (fac == null) {
  	 	throw new NullPointerException();
  	}
  	
  	// ToDo security check ...
  	
  	if (factory != null) {
  	 	throw new Error();
  	}
  	factory = fac;
  }

  public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
    	defaultAllowUserInteraction = defaultallowuserinteraction;
  }

  public static void setDefaultRequestProperty(String key, String value){}

  public static void setFileNameMap(FileNameMap map){
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
   	return null;
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
   	 	throw new IllegalAccessError("property cannot be set after connecting");
   	}
   	allowUserInteraction = allowuserinteraction;
  }

  public void setDefaultUseCaches(boolean defaultusecaches){
   	defaultUseCaches = defaultusecaches;
  }

  public void setDoInput(boolean doinput){
   	if (connected) {
   	 	throw new IllegalAccessError("property cannot be set after connecting");
   	}
   	doInput = doinput;
  }

  public void setDoOutput(boolean dooutput){
   	if (connected) {
   	 	throw new IllegalAccessError("property cannot be set after connecting");
   	}
   	doOutput = dooutput;
  }

  public void setIfModifiedSince(long ifmodifiedsince){
   	if (connected) {
   	 	throw new IllegalAccessError("property cannot be set after connecting");
   	}
  	ifModifiedSince = ifmodifiedsince;
  }

  public void setUseCaches(boolean usecaches){
   	if (connected) {
   	 	throw new IllegalAccessError("property cannot be set after connecting");
   	}
   	useCaches = usecaches;
  }

/**
**	default implementation does nothing
**	@ remark sublcasses should override this method
*/
  public void setRequestProperty(String key, String value){  }

  public String toString(){
   	return "connecting to "+url.toString()+" using "+this.getClass().getName();
  }

  public long getHeaderFieldDate(String name, long Default) {
   	try {
   		return Long.parseLong(getHeaderField(name));
   	}
   	catch(RuntimeException e) { // we expect a NullPointerException or a NumberFormatException
   		return Default;
  	}
  }
  public int getHeaderFieldInt(String name, int Default) {
   	try {
   		return Integer.parseInt(getHeaderField(name));
   	}
   	catch(RuntimeException e) { // we expect a NullPointerException or a NumberFormatException
	   	return Default;
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


//  TODO:

/**
**	the inputstream should support mark reset ...
**	@remark return null no attempt made to guess !
*/   	
  public static String guessContentTypeFromStream(InputStream is) throws IOException {
   	return null;
  }

  public Object getContent() throws IOException {
  	//step 1. get the contenttype
    String ct = getContentType();
    if (ct == null) {
      ct = guessContentTypeFromName(url.getFile());
      if (ct == null) {
        //try {
          ct = guessContentTypeFromStream(new BufferedInputStream(getInputStream()));
        //}
        //catch(IOException ioe) {}
        if (ct == null) {
          return null;
        }
      }
    }
    //step 2. get a handler	to build an Object ...
    ContentHandler cth;
    if (factory != null) {
      cth = factory.createContentHandler(ct);
      if (cth != null) {
        return cth.getContent(this);
      }
    }
    //get wonka classes to save the day (or at least try ...)
    cth = (new com.acunia.wonka.net.DefaultContentHandlerFactory()).createContentHandler(ct);
    if(cth != null) {
      return cth.getContent(this);
    }
    //too bad..
    return null;
  }

  public Object getContent(Class[] classes) throws IOException {
  	//step 1. get the contenttype
    String ct = getContentType();
    if (ct == null) {
      ct = guessContentTypeFromName(url.getFile());
      if (ct == null) {
        try {
          ct = guessContentTypeFromStream(new BufferedInputStream(getInputStream()));
        }
        catch(IOException ioe) {}
        if (ct == null) {
          return null;
        }
      }
    }
    //step 2. get a handler	to build an Object ...
    ContentHandler cth;
    if (factory != null) {
      cth = factory.createContentHandler(ct);
      if (cth != null) {
        return cth.getContent(this, classes);
      }
    }
    //get wonka classes to save the day (or at least try ...)
    return null;
  }


  private static class DefaultFileNameMap implements FileNameMap {

  	private ResourceBundle filenameMap;

  	public DefaultFileNameMap() {
		try {
			filenameMap = ResourceBundle.getBundle("com.acunia.wonka.net.MimeTypeMap");			 	
		}	
		catch(MissingResourceException mre){}	  	
  	}
  	
  	
  	public String getContentTypeFor(String fileName){
  		if (filenameMap != null) {
  		 	return filenameMap.getString(fileName);
  		}      	
  		return null;  	
        }
  }


}
