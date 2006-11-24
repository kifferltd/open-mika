/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: ProcessOutputStream.java,v 1.2 2006/06/20 11:59:07 cvs Exp $
*/

package wonka.vm;

import java.io.*;

public class ProcessOutputStream extends OutputStream {

  
  /* Prevent the NativeProcess Object to gc'ed */
  NativeProcess process;
  private ProcessInfo info;

  public ProcessOutputStream(NativeProcess process) {
    info = process.info;
    this.process = process;
  }
  
  public native void write(int b) throws IOException;
  public native void write(byte[] bytes, int off, int len) throws IOException;

  public synchronized void close() throws IOException {
    if(info != null) {
      //System.out.println("ProcessOutputStream.close()"+process.info);
      nativeClose();
      info = null;
    }
  }

  private native void nativeClose();
}

