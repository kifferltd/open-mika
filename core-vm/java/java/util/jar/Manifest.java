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

package java.util.jar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @version	$Id: Manifest.java,v 1.2 2006/02/17 10:53:19 cvs Exp $
 *
 */
public class Manifest implements Cloneable {

  private Attributes mainAttributes;
  private HashMap entries;

  private static final String NVSep = ": ";
  private static final String LineSep = GetSystemProperty.LINE_SEPARATOR;
  private static final byte [] newline = LineSep.getBytes();
  private static final int MAX_LINELENGTH = 72 - newline.length;

  public Manifest() {
  	mainAttributes = new Attributes();
  	entries = new HashMap();
  }

  public Manifest(InputStream is) throws IOException {
    mainAttributes = new Attributes();
    entries = new HashMap();
    read(is);
  }

  public Manifest(Manifest man) {
	  this.mainAttributes = (Attributes)man.mainAttributes.clone();
	  this.entries = (HashMap)man.entries.clone();
  }

  public Attributes getMainAttributes() {
    return this.mainAttributes;
  }

  public Map getEntries() {
    return this.entries;
  }

  public Attributes getAttributes (String name) {
    return (Attributes)getEntries().get(name);
  }

  public void clear() {
    this.mainAttributes.clear();
    this.entries.clear();
  }

  public Object clone() {
  	Manifest cloned=null;
	  try {
	    cloned = (Manifest) super.clone();
	  }
	  catch(CloneNotSupportedException cnse){}
	  cloned.mainAttributes = (Attributes)this.mainAttributes.clone();
	  cloned.entries = (HashMap)this.entries.clone();
    return cloned;
  }

  public boolean equals(Object o){
   	if(!(o instanceof Manifest)) {
   	 	return false;
   	}
   	Manifest eq = (Manifest)o;
   	return (eq.mainAttributes.equals(this.mainAttributes) && eq.entries.equals(this.entries));   	
  }

  public void read(InputStream in) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    Attributes attributes = mainAttributes;
    String at = br.readLine();
    boolean validEntry = true;

    while (at != null) {
      String att = at;
      at = br.readLine();
      while (at != null && at.startsWith(" ")) {
        /*if(at.length() > 71){
          it is not allowed to have lines longer then 72 chars but no exception
          is thrown ... TODO
          throw new IOException("line '"+at+"' is too long "+at.length());
        }*/
        att = att+at.substring(1);
        at = br.readLine();
      }
// [CG 20021202] As suggested by Mark Anderson: don't trim the line, it only
// causes a problem if the attribute value is empty (we lose the preceding
// space). I don't see anything in the JAR spec about trailing whitespace,
// so we should probably leave it be.
//      att = att.trim();
      if (att.trim().length() > 0) { //Skip empty line
        int sep = att.indexOf(NVSep);
        if(validEntry){
          String name = att.substring(0,sep);
          String value = att.substring(sep+2);

          if(attributes == null){
            if(name.equals("Name")){
              attributes = (Attributes) entries.get(value);
              if(attributes == null){
                attributes = new Attributes(13);
                entries.put(value.trim(), attributes);
              }
            }
            else {
              validEntry = false;
            }
          }
          else {
            attributes.putValue(name,value);
          }
        }
      }
      else {
        attributes = null;
        validEntry = true;
      }
    }
  }

  public void write(OutputStream os) throws IOException { 	
    writeAttributes(os, mainAttributes);
   	Iterator it = entries.entrySet().iterator();
   	Map.Entry me;
   	StringBuffer buf;
   	while (it.hasNext()) {
   		me = (Map.Entry) it.next();
   		buf = new StringBuffer(LineSep);
   		buf.append("Name: ");
   		buf.append((String)me.getKey());
   		os.write(newline);
   		writeBuffer(os,buf);
   		writeAttributes(os, (Attributes)me.getValue());   	 		
   	}   	
  }

  private void writeAttributes(OutputStream os, Attributes at) throws IOException {
  	StringBuffer buf;
   	Iterator it = at.entrySet().iterator();
   	Map.Entry me;
   	while (it.hasNext()) {
   		me = (Map.Entry) it.next();
   		buf = new StringBuffer(((Attributes.Name)me.getKey()).toString());
   		buf.append(NVSep);
   		buf.append((String)me.getValue());
   		writeBuffer(os,buf);
   	}
  }

  private void writeBuffer(OutputStream os, StringBuffer buf) throws IOException {
    int len = buf.length();
    int offset = 0;
    byte [] bytes = new String(buf).getBytes();
    while(len > 0) {
      if ( len > MAX_LINELENGTH ) {
        os.write(bytes,offset,len);
        os.write(newline);
        os.write(' ');
        len -= MAX_LINELENGTH;
        offset += MAX_LINELENGTH;
      }
      else {	 	
        os.write(bytes,offset,len);
        os.write(newline);
        break;
      }
    }
  }
}
