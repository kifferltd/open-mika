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
** $Id: ProcessInputStream.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package wonka.vm;

import java.io.*;

public class ProcessInputStream extends InputStream {

  boolean input;
  ProcessInfo info;

  /* Prevent the NativeProcess Object to gc'ed */
  NativeProcess process;

  public ProcessInputStream(NativeProcess process, boolean input) {
    this.input = input;
    this.info = process.info;
    this.process = process;
  }
  
  public void close() throws IOException {
    info = null;
    //TODO: need to close native Stream ?
  }
  
  public native int available() throws IOException;
  public native int read() throws IOException;
  public native int read(byte[] b, int off, int len) throws IOException;

}

