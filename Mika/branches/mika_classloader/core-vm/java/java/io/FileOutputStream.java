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
** $Id: FileOutputStream.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

public class FileOutputStream extends OutputStream {

  private FileDescriptor fd;

  private native int createFromString(String path, boolean append);

  private native void createFromFileDescriptor(FileDescriptor fdObj)
    throws SecurityException;

  private static void permissionCheck(String path) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new FilePermission(path, "write"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm!=null) {
        sm.checkWrite(path);
      }
    }
  }

  public FileOutputStream(String path) throws SecurityException, FileNotFoundException {
    permissionCheck(path);
    if(createFromString((new File(path)).getAbsolutePath(), false) != 0) {  
      throw new FileNotFoundException(path);
    }
  }

  public FileOutputStream(String path, boolean append) 
    throws SecurityException, FileNotFoundException
  {
    permissionCheck(path);
    if(createFromString((new File(path)).getAbsolutePath(), append) != 0) {
      throw new FileNotFoundException(path + " (" + append + ")");
    }
  }

  public FileOutputStream(File file, boolean append) throws SecurityException, FileNotFoundException {
    permissionCheck(file.getAbsolutePath());
    if(createFromString(file.getAbsolutePath(), append) != 0) {
      throw new FileNotFoundException("" + file);
    }
  }
  public FileOutputStream(File file) throws SecurityException, FileNotFoundException {
    this(file, false);
  }

  public FileOutputStream(FileDescriptor fdObj) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission("writeFileDescriptor"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if(sm!=null) {
        sm.checkWrite(fdObj);
      }
    }
    fd = fdObj;
    createFromFileDescriptor(fdObj);
  }

  public final FileDescriptor getFD() throws IOException {
    return fd;
  }

  public synchronized native void write(int b) 
    throws IOException;

  private synchronized native void writeFromBuffer(byte[] b, int off, int len)
    throws IOException;

  public void write(byte[] b) throws IOException, NullPointerException {
    writeFromBuffer(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {
    writeFromBuffer(b, off,len);
  }

  public synchronized native void close() throws IOException;

  protected void finalize() throws IOException {
    if(fd != null) close();
  }

  public native void flush() throws IOException;

}
