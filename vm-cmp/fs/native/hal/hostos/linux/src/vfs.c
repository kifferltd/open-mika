/**************************************************************************
* Copyright (c) 2024 by KIFFER Ltd.  All rights reserved.                 *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "vfs.h"

static x_Mutex fd_table_Mutex;
static x_mutex fd_table_mutex = &fd_table_Mutex;

#define SET_ERRNO(n) { x_int temp = (n); woempa(7, "Set x_errno to %d\n", temp); x_errno = temp; }

w_int  ufs_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode);
size_t ufs_get_length(vfs_fd_entry fde);
w_boolean ufs_is_eof(vfs_fd_entry fde);
w_int  ufs_tell  (vfs_fd_entry fde);
w_int  ufs_seek  (vfs_fd_entry fde, w_int offset, w_int whence);
w_int  ufs_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos);
w_int  ufs_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos);
w_int  ufs_close  (vfs_fd_entry fde);

static vfs_FileOperations ufs_ops = {
  .dummy = NULL,
  .open = ufs_open,
  .get_length = ufs_get_length,
  .is_eof = ufs_is_eof,
  .tell = ufs_tell,
  .seek = ufs_seek,
  .read = ufs_read,
  .write = ufs_write,
  .close = ufs_close,
};

static vfs_MountPoint ufs_mountpoint = {"/", &ufs_ops, NULL, NULL};

void init_ufsfs(void) {
// TODO - virtualise getcwd
  char *cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(7, "getcwd returned NULL, errno = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(7, "current dir  : %s\n", current_working_dir);
  woempa(7, "current root : %s\n", current_root_dir);

  ufs_ops.dummy = NULL;
  ufs_ops.open = ufs_open;
  ufs_ops.get_length = ufs_get_length;
  ufs_ops.is_eof = ufs_is_eof;
  ufs_ops.tell = ufs_tell;
  ufs_ops.seek = ufs_seek;
  ufs_ops.read = ufs_read;
  ufs_ops.write = ufs_write;
  ufs_ops.close = ufs_close;

  registerMountPoint(&ufs_mountpoint);
}

w_int ufs_open(vfs_fd_entry fde, const char *path, w_word flags, w_word mode) {
  int *fdptr = allocMem(sizeof(int));
  *fdptr = open(path, flags, mode);
  fde->data = fdptr;

  return *fdptr;
}

w_int ufs_tell(vfs_fd_entry fde) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to get length of %s, fd is not in use\n", fde->path);
    SET_ERRNO(EBADF);
    return -1;
  }

  return (w_int) fseek(*fdptr, 0, SEEK_CUR);
}

w_int ufs_read(vfs_fd_entry fde, char *buffer, w_size length, w_int *pos) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to read from %s, fd is not in use\n", fde->path);
    SET_ERRNO(EBADF);
    return -1;
  }
  // Java FileInputStream treats EOF as error case
  // TODO move this logic to FileInputStream.c?
  if (ufs_is_eof(fde)) {
    woempa(7, "failed to read from %s, fd is at EOF\n", fde->path);

    return -1;
  }

  ssize_t did_read = 0;
  ssize_t rc = 0;

  while ((w_size) did_read < length) {
    woempa(1, "reading %d bytes to %p from %s\n", length, buffer+did_read, fde->path);
    rc = read(*fdptr, buffer + did_read, length);
    if (rc < 0) {
      woempa(7, "only read %d bytes from %p to %s, write() returned -1\n", did_read, buffer, fde->path);
      //[CG 20240603] The spec is unclear, but Java seems to return the bytes read in this case
      return did_read;
    }
    did_read += rc;
  }

  woempa(1, "did read %d bytes from %p to %s\n", did_read, buffer, fde->path);

  return did_read;
}

w_int ufs_seek(vfs_fd_entry fde, w_int offset, w_int whence) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to seek %d bytes, fd is not in use\n", offset);
    SET_ERRNO(EBADF);
    return -1;
  }
  woempa(1, "seeking %d bytes in %s from %s\n", offset, fde->path,
    whence == SEEK_CUR ? "current file position" :
    whence == SEEK_END ? "end of the file" :
    whence == SEEK_SET ? "beginning of file" : "??? unknown ???");

  if (lseek(*fdptr, offset, whence) == 0) {
    w_int new_pos = ufs_tell(fde);
    woempa(1, "sought %d bytes from %d, offset is now %d\n", offset, fde->path, new_pos);
    return new_pos;
  }

  woempa(7, "failed to seek %d bytes from %s\n", offset, fde->path);

  return -1;
}

w_int ufs_close(vfs_fd_entry fde) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "did not close %s, fd is not in use\n", fde->path);
    return 0;
  }

  woempa(5, "closing %s\n", fde->path);
  if (fclose(*fdptr) == 0) {
    *fdptr = -1;
    woempa(5, "successfully closed %s\n", fde->path);
    return 0;
  }


  return -1;
}

size_t ufs_get_length(vfs_fd_entry fde) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to get length of %s, fd is not in use\n", fde->path);
    SET_ERRNO(EBADF);
    return -1;
  }
  return lseek(*fdptr, 0, SEEK_CUR);
}

w_boolean ufs_is_eof(vfs_fd_entry fde) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to check %s for EOF, fd is not in use\n", fde->pathffset);
    SET_ERRNO(EBADF);
    return -1;
  }
      // TODO set pos
  return (w_size) lseek(*fdptr, 0, SEEK_CUR) == ufs_get_length(fde);
}

w_int ufs_write(vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos) {
  int *fdptr = fde->data;
  if (*fdptr < 0) {
    woempa(7, "failed to write %d bytes, fd is not in use\n", offset);
    SET_ERRNO(EBADF);
    return -1;
  }

  ssize_t written = 0;
  ssize_t rc = 0;

  while ((w_size) written < length) {
    woempa(1, "writing %d bytes from %p to %s\n", length, buffer+written, fde->path);
    rc = write(*fdptr, buffer + written, length);
    if (rc < 0) {
      woempa(7, "only wrote %d bytes from %p to %s, write() returned -1\n", written, buffer, fde->path);
      return rc;
    }
    written += rc;
  }
      // TODO set pos

  woempa(1, "successfully wrote %d bytes to %s\n", written, fde->path);

  return written;
}

/* ---------------------------------- old code --------------------------------------

char *current_working_dir;
char *current_root_dir;

void init_vfs(void) {
  #ifdef FS_NON_BLOCKING
    woempa(9, "init_vfs  -> Using native filesystem (NON-BLOCKING)\n"); 
  #else
    woempa(9, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  #endif
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(9, "getcwd returned NULL, errono = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(9, "current dir  : %s\n", current_working_dir);
  woempa(9, "current root : %s\n", current_root_dir);
}

#ifndef VFS_FILEOPS_DEFINED

int vfs_open(const char *filename, const w_int flags, const w_word mode) {
  return open(filename, flags | O_NONBLOCK, mode);
}

int vfs_read(const int file_desc, w_void *buffer, const w_word count) {
  int retval = read(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    retval = read(file_desc, buffer, count);
  }
  return retval;  
}

int vfs_write(const int file_desc, const w_void *buffer, const w_word count) {
  int retval = write(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_thread_sleep(50);
    retval = write(file_desc, buffer, count);
  }
  return retval;  
}

#endif

---------------------------------- end old code ------------------------------------*/
