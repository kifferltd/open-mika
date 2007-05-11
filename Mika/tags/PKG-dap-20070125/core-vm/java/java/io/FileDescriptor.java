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
** $Id: FileDescriptor.java,v 1.3 2006/05/27 20:14:31 cvs Exp $
*/

package java.io;

public final class FileDescriptor {

  private boolean validFD;
  
  /** used in native code */
  final String fileName;
  
  public static final FileDescriptor  in = null;
  public static final FileDescriptor out = null;
  public static final FileDescriptor err = null;

  public FileDescriptor() {
    validFD = false;
    fileName = null;
  }

  public boolean valid() {
    return validFD;
  }

  public native void sync() throws SyncFailedException;
}

