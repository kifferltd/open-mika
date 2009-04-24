/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
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

#include "core-classes.h"
#include "threads.h"
#include "vfs.h"
#include "wonka.h"
#include "wstrings.h"

/*
** Open a file and store the fileName and wotsit fields in thisFileDescriptor;
** set validFD true. The path string must be an absolute path and 'modenum' 
** must be one of the following:
** 0 -> read -> fopen(3) mode "r"
*/
void FileDescriptor_createFromPath(JNIEnv *env, w_instance thisFileDescriptor, w_instance pathString, w_int modenum) {
  w_string path_string = pathString ? String2string(pathString) : NULL;
  w_int path_length;
  w_ubyte *path = string2UTF8(path_string, &path_length) + 2;
  w_ubyte *mode = modenum == 0 ? "r" : modenum == 1 ? "w+" : modenum == 2 ? "a+" : NULL;
  void *file;

  if (!mode) {
    throwException(JNIEnv2w_thread(env), clazzInternalError, NULL);
  }

  file = vfs_fopen(path, mode);
  if (!file) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "could not open file '%s' using mode '%s': %s\n", path, mode, strerror(errno));
  }
  releaseMem(path - 2);

  setWotsitField(thisFileDescriptor, F_FileDescriptor_fd, file);
}

void FileDescriptor_sync
  (JNIEnv *env, jobject thisObj) {

  woempa(9, "sync !");
	  
}

