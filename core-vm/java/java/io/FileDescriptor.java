/**************************************************************************
* Copyright (c) 2009, 2022 by Chris Gray, KIFFER Ltd.                     *
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
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package java.io;

public final class FileDescriptor {

  // Package-private API (not standard)

  // Constants for various access modes
  // Caution: these values are also hard-coded in FileDescriptor.c
  // MODE_READ -> file must exist and must not be a directory; will be opened read-only.
  static final int MODE_READ = 0;
  // MODE_WRITE-> if file exists it must not be a directory; it will be truncated and will be opened write-only.
  static final int MODE_WRITE = 1;
  // MODE_APPEND -> if file exist it must must not be a directory; will be opened write-only for append.
  //              otherwise it will be created and then opened for write-only.
  static final int MODE_APPEND = 2;

  public static final FileDescriptor  in = null;
  public static final FileDescriptor out = null;
  public static final FileDescriptor err = null;

  // The real file descriptor - this should be an open file
  int fd;

  public static final FileDescriptor  in = null;
  public static final FileDescriptor out = null;
  public static final FileDescriptor err = null;

  /**
   ** Public constructor which sets fd to -1; consequently the FileDescriptor
   ** created is not valid.
   */
  public FileDescriptor() {
    fd = -1;
  }

  /**
   ** Package-local constructor used to set the fileName and wotsit.
   ** Takes an absolute path and a mode as defined by MODE_XXXX.
   */
  FileDescriptor(String abspath, int mode) {
    if (abspath == null) {
      throw new NullPointerException();
    }
    fd = createFromPath(abspath, mode);
  }

  private native int createFromPath(String path, int mode);

  public boolean valid() {
    // TODO add native check at vfs level?
    return fd >= 0;
  }

  public void sync() throws SyncFailedException {
    if (fd >= 0) {
     _sync();
    }
  }

  private native void _sync() throws SyncFailedException;
}

