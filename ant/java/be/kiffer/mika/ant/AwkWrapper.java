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
 * $Id: AwkWrapper.java,v 1.1 2006/09/20 14:21:05 cvsroot Exp $
 */
package be.kiffer.mika.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * AwkWrapper:
 *
 * @author Gerrit Ruelens
 *
 * created: Sep 19, 2006
 */
public class AwkWrapper extends Task {

  private String script;
  private String input;
  private String output;
  private String condition;
  private String value;
  private String args;
  
  /**
   * @return Returns the args.
   */
  public final String getArgs() {
    return args;
  }

  /**
   * @param args The args to set.
   */
  public final void setArgs(String args) {
    this.args = args;
  }

  /**
   * @return Returns the condition.
   */
  public final String getCondition() {
    return condition;
  }

  /**
   * @param condition The condition to set.
   */
  public final void setCondition(String condition) {
    this.condition = condition;
  }

  /**
   * @return Returns the input.
   */
  public final String getInput() {
    return input;
  }

  /**
   * @param input The input to set.
   */
  public final void setInput(String input) {
    this.input = input;
  }

  /**
   * @return Returns the output.
   */
  public final String getOutput() {
    return output;
  }

  /**
   * @param output The output to set.
   */
  public final void setOutput(String output) {
    this.output = output;
  }

  /**
   * @return Returns the script.
   */
  public final String getScript() {
    return script;
  }

  /**
   * @param script The script to set.
   */
  public final void setScript(String script) {
    this.script = script;
  }

  /**
   * @return Returns the value.
   */
  public final String getValue() {
    return value;
  }

  /**
   * @param value The value to set.
   */
  public final void setValue(String value) {
    this.value = value;
  }

  public void execute() throws BuildException {
    if(value == null ? condition != null :
      !value.equals(condition)) {
      return;
    }
    checkParams();
    File out = new File(output);
    if(out.isFile()) {
      long time = out.lastModified();
      long in = new File(input).lastModified();
      long scr = new File(script).lastModified();
      if(time > in && time > scr) {
        return;
      }
    }
    try {
      generateFile();
    } catch (IOException e) {
      throw (BuildException) 
        new BuildException(e.getMessage()).initCause(e); 
    }
  }

  private void generateFile() throws IOException {
    this.log("generating '"+output+"' from '"+input+"' using "+script);
    Process proc = this.args == null ? 
        Runtime.getRuntime().exec("awk -f "+script+" "+input):
        Runtime.getRuntime().exec("awk -f "+script+" "+args+" "+input);
    InputStream in = proc.getInputStream();
    OutputStream out = new FileOutputStream(output);
    byte[] bytes = new byte[2048];
    int rd = in.read(bytes);
    while(rd != -1) {
      out.write(bytes,0, rd);
      rd = in.read(bytes);
    }
    out.close();
  }

  private void checkParams() {
    if(output == null) {
      throw new BuildException("'output' file not set");
    }
    if(input == null) {
      throw new BuildException("'input' file not set");
    }
    if(!new File(input).isFile()) {
      throw new BuildException("input '"+input+"' doesn't point to a valid file");
    }
    if(script == null) {
      throw new BuildException("'script' file not set");
    }
    if(!new File(script).isFile()) {
      throw new BuildException("script '"+script+"' doesn't point to a valid file");
    }
  }
}