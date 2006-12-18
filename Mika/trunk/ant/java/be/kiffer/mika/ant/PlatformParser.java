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
 * $Id: PlatformParser.java,v 1.1 2006/09/08 11:57:20 cvsroot Exp $
 */
package be.kiffer.mika.ant;

import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;

/**
 * PlatformParser:
 *
 * @author Gerrit Ruelens
 *
 * created: Sep 8, 2006
 */
public class PlatformParser extends Task {

  private String list;
  private String target;
  
  public final String getList() {
    return list;
  }

  public final void setList(String list) {
    this.list = list;
  }

  public void execute() throws BuildException {
    if(list == null) {
      throw new BuildException("No 'list' of platform definitions set");
    }
    if(target == null) {
      target = "build";
    }
    StringTokenizer platforms = new StringTokenizer(list ,",");
    while (platforms.hasMoreElements()) {
      String platform = platforms.nextToken();
      CallTarget call = new CallTarget();
      call.setLocation(this.getLocation());
      call.setInheritAll(true);
      Project proj = this.getProject();
      proj.setProperty("platform", platform);
      call.setProject(proj);
      call.setOwningTarget(this.getOwningTarget());
      call.setDescription(target);
      call.setTaskName("/k/" + target);
      call.setTarget(target);
      call.execute();
      
    }
  }

  public final String getTarget() {
    return target;
  }

  public final void setTarget(String target) {
    this.target = target;
  }
}
