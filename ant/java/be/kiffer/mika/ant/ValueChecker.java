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
 * $Id: ValueChecker.java,v 1.1 2006/09/20 14:21:05 cvsroot Exp $
 */
package be.kiffer.mika.ant;

import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * ValueChecker:
 *
 * @author Gerrit Ruelens
 *
 * created: Sep 20, 2006
 */
public class ValueChecker extends Task {

   private String property;
   private String values;

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
  /**
   * @return Returns the values.
   */
  public final String getValues() {
    return values;
  }
  /**
   * @param values The values to set.
   */
  public final void setValues(String values) {
    this.values = values;
  }
  
  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    if(property == null) {
      throw new BuildException("No property defined");
    }
    Project project = this.getProject();
    String value = project.getProperty(property);
    if(value == null) {
      value = project.getUserProperty(property);
      if(value == null) {
        throw new BuildException("property '"+property
             +"' is not set. please define "+property);
      }
    }
    if(values != null) {
      StringTokenizer tokens = new StringTokenizer(values, "|");
      while (tokens.hasMoreElements()) {
        if(value.equals(tokens.nextToken())) {
          return;
        }        
      }
      throw new BuildException("Invalid value '"+value+"' for property '"+
          property+"'. Use on of: "+values);
    }
  }
  
}
