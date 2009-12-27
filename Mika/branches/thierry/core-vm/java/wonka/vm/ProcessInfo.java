/**
 * Copyright  (c) 2006, 2008 by Chris Gray, /k/ Embedded Java Solutions.
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
 * $Id: ProcessInfo.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
 */
package wonka.vm;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * ProcessInfo:
 *
 * @author ruelens
 *
 * created: Jun 14, 2006
 */
public class ProcessInfo {
  
  int id;
  int wotsit;

  boolean destroyed;
  
  private WeakReference reference;
  
  
  /** 
   * @see java.lang.Object#toString()
   */
  public String toString() {    
    return super.toString()+" pid "+id+", destroyed = "+destroyed;
  }

  /**
   * @see java.lang.Object#finalize()
   */
  protected void finalize() {
    //System.out.println("ProcessInfo.finalize()"+id);
    cleanUp();
  }

  void setNativeProcess(NativeProcess process) {
    reference = new WeakReference(process);   
    process.info = this;
  }

  void finish(int retval) throws IOException {
    nativeReturnValue(retval);
    if(reference == null) {
      System.err.println("[PANIC] ProcessInfo: " +
          "No referencece, this should never happen !!!");
      return;
    }
    
    NativeProcess process = (NativeProcess)  reference.get();
    //System.out.println("ProcessInfo.finish() pid "+id+", NativeProcess "+process);  
    
    if(process != null) {
      process.setReturnValue(retval);
      return;
    } 
    // [CG 20080814] Safer to leave this until the finalizer?
    // cleanUp();
  }

  synchronized native void destroy();
  private synchronized native void cleanUp();
  private native void nativeReturnValue(int retval);

}
