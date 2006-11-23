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
** $Id: FileInputStream.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class FileInputStream extends InputStream {

  private FileDescriptor fd;

  private native int createFromString(String path);

  private native void createFromFileDescriptor(FileDescriptor fdObj)
    throws SecurityException;

  public FileInputStream(String path) throws SecurityException, FileNotFoundException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new FilePermission(path, "read"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if(sm!=null) {
        sm.checkRead(path);
      }
    }
    if(createFromString((new File(path)).getAbsolutePath()) != 0) {
      throw new FileNotFoundException(path);
    }
  }
  
  public FileInputStream(File file) throws SecurityException, FileNotFoundException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new FilePermission(file.getAbsolutePath(), "read"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if(sm!=null) {
        sm.checkRead(file.getAbsolutePath());
      }
    }
    if(createFromString(file.getAbsolutePath()) != 0) {
      throw new FileNotFoundException("" + file);
    }
  }

  public FileInputStream(FileDescriptor fdObj) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission("readFileDescriptor"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if(sm!=null) {
        sm.checkRead(fdObj);
      }
    }
    fd = fdObj;
    createFromFileDescriptor(fdObj);
  }

  public synchronized native int read()
    throws IOException;
  
  private synchronized native int readIntoBuffer(byte[] b, int off, int len)
    throws IOException;
  
  public int read(byte[] b)
    throws IOException, NullPointerException {
      return read(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {

    return readIntoBuffer(b, off, len);
  }
  
  public synchronized native long skip(long n)
    throws IOException;

  public native int available()
    throws IOException;
  
  public synchronized native void close()
    throws IOException;
  
  public final FileDescriptor getFD()
    throws IOException {
    // when would this throw IOException?
    return fd;
  }
  
  protected void finalize()
    throws IOException {
    if(fd != null) close();
  }

}
