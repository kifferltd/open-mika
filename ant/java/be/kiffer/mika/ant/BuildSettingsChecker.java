/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of /k/ Embedded Java Solutions nor the names of other contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL /K/
 * EMBEDDED SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id: BuildSettingsChecker.java,v 1.3 2006/09/20 14:21:05 cvsroot Exp $
 */
package be.kiffer.mika.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * BuildSettingsChecker:
 *
 * @author Gerrit Ruelens
 *
 * created: Sep 14, 2006
 */
public class BuildSettingsChecker extends Task {
  
  private String file;
  private String task;
  
  private final static String[] keylist = new String[] {
    "SECURITY" , "JAR", "MATH", "JAVA_BEANS", "DEBUG", "UNICODE_SUBSETS",
    "JAVAX_CRYPTO", "JAVAX_COMM", "AWT", "AWT_DEF", "STATIC", "JAVA_DEBUG",
  };
  
  public final String getFile() {
    return file;
  }

  public final void setFile(String file) {
    this.file = file;
  }

  public final String getTask() {
    return task;
  }

  public final void setTask(String task) {
    this.task = task;
  } 

  public void execute() throws BuildException {
    if(file == null) {
      throw new BuildException("specify a 'file'");
    }

    File f = new File(file);

    Project project = getProject();
    Hashtable props = (Hashtable) project.getProperties().clone();
    props.putAll(project.getUserProperties());

    try {
      if(f.isFile() && checkProperties(props)) {
        return;
      }
      //file doesn't exist or different.    
      writeFile(props);
    } catch (IOException e) {
      throw (BuildException) new BuildException(e.getMessage()).initCause(e);
    }
  }

  private void writeFile(Hashtable list) throws IOException {
    Properties props = new Properties();
    for(int i=0 ; i < keylist.length ; i++) {
      String key = keylist[i];
      Object value = list.get(key);
      props.setProperty(key,(value == null ? "" : (String)value));
    }
    Enumeration keys = list.keys();
    while(keys.hasMoreElements()) {
      try {
        String key = (String) keys.nextElement();
        if(key.startsWith("JAM.")) {
          Object value = list.get(key);
          props.setProperty(key,(value == null ? "" : (String)value));
        }
      } catch(ClassCastException cce) {
        //Ignore ...
      }
    }

    log("Storing "+props+" to '"+file+"'");
    props.store(new FileOutputStream(file), "ANT GENERATED - DO NOT CHANGE !");    
  }

  private boolean checkProperties(Hashtable list) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));
    for(int i=0 ; i < keylist.length ; i++) {
      String key = keylist[i];
      Object value = list.get(key);
      String setting = value == null ? "" : (String)value;
      if(!setting.equals(props.getProperty(key))) {
        doClean(key, setting, props);
        return false;
      }
    }
    Hashtable all = new Hashtable();
    all.putAll(list);
    all.putAll(props);
    Enumeration keys = all.keys();
    while(keys.hasMoreElements()) {
      try {
        String key = (String) keys.nextElement();
        if(key.startsWith("JAM.")) {
          Object value = list.get(key);
          String setting = value == null ? "" : (String)value;
          if(!setting.equals(props.getProperty(key))) {
            doClean(key, setting, props);
            return false;
          }
        }
      } catch(ClassCastException cce) {
        //Ignore ...
      }
    }
    log("All keys matched !");
    return true;
  }

  private void doClean(String key, Object value, Properties props) {
    log("Key '"+key+"' doesn't match:");
    log("\twas = '"+props.getProperty(key,"")+"'");
    log("\t is = '"+value+"'");
    CallTarget call = new CallTarget();
    call.setLocation(this.getLocation());
    call.setInheritAll(true);
    call.setProject(this.getProject());
    call.setOwningTarget(this.getOwningTarget());
    call.setDescription(task);
    call.setTaskName("/k/" + task);
    call.setTarget(task);
    call.init();
    call.execute();
  }
}
