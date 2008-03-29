/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: ErrorOutputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.io;

import java.io.*;

public class ErrorOutputStream extends OutputStream {

  /** The internal device ID.
  */
  private int devid;

  /** Buffer used for write(I)V.
  */
  private byte[] microbuffer = new byte[1];

  public ErrorOutputStream() {
  // TODO: security check?
  }

  public void write(int b) 
    throws IOException
  {
    microbuffer[0] = (byte)b;
    write(microbuffer, 0, 1);
  }

  public void write(byte[] b) throws IOException, NullPointerException {

    if(b==null) throw new NullPointerException();

    write(b, 0, b.length);
  }

  public native void write(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException 
  ;

}

