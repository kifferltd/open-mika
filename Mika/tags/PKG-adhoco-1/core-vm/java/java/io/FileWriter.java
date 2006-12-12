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
** $Id: FileWriter.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

public class FileWriter extends OutputStreamWriter {

  public FileWriter(String filename) throws IOException {
    super(new FileOutputStream(filename));
  }
  
  public FileWriter(String filename, boolean append) throws IOException {
    super(new FileOutputStream(filename, append));
  }
  
  public FileWriter(File file) throws IOException {
    super(new FileOutputStream(file));
  }
  
  public FileWriter(File file, boolean append) throws IOException {
    super(new FileOutputStream(file, append));
  }
  
  public FileWriter(FileDescriptor fd) {
    super(new FileOutputStream(fd));
  }  
}
