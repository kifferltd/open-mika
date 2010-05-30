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
 * $Id: PropertySetter.java,v 1.1 2006/09/20 14:21:05 cvsroot Exp $
 */
package be.kiffer.mika.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * PropertySetter:
 *
 * @author Gerrit Ruelens
 *
 * created: Sep 20, 2006
 */
public class PropertySetter extends Task {

  private String property;
  private String arg1;
  private String arg2;
  private String value;
  private String elsevalue;
  
  /**
   * @return Returns the arg1.
   */
  public final String getArg1() {
    return arg1;
  }
  /**
   * @param arg1 The arg1 to set.
   */
  public final void setArg1(String arg1) {
    this.arg1 = arg1;
  }
  /**
   * @return Returns the arg2.
   */
  public final String getArg2() {
    return arg2;
  }
  /**
   * @param arg2 The arg2 to set.
   */
  public final void setArg2(String arg2) {
    this.arg2 = arg2;
  }
  /**
   * @return Returns the elsevalue.
   */
  public final String getElsevalue() {
    return elsevalue;
  }
  /**
   * @param elsevalue The elsevalue to set.
   */
  public final void setElsevalue(String elseValue) {
    this.elsevalue = elseValue;
  }
  /**
   * @return Returns the property.
   */
  public final String getProperty() {
    return property;
  }
  /**
   * @param property The property to set.
   */
  public final void setProperty(String property) {
    this.property = property;
  }
  /*
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
  
  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    if(property == null) {
      throw new BuildException("no property defined to set.");
    }
    if (value == null) {
      throw new BuildException("no value to set.");
    }
    if((arg1 == null || arg2 == null)) {
      throw new BuildException("no argument to checks");
    }
    Project project = this.getProject();
    if(arg2.equals(arg1)) {
      project.setProperty(property,value);
    } else if(elsevalue != null) {
      project.setProperty(property, elsevalue);
    }  
  }
}
