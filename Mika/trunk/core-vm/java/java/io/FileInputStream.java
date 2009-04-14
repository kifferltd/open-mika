/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.io;

public class FileInputStream extends InputStream {

  private FileDescriptor fd;

  private native int createFromString(String path);

  private native void createFromFileDescriptor(FileDescriptor fdObj)
    throws SecurityException;

  public FileInputStream(String path) throws SecurityException, FileNotFoundException {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
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
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
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
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
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
