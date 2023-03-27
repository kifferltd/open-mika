/**************************************************************************
* Copyright (c) 2020, 2021, 2022, 2023 by KIFFER Ltd.                     *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "vfs-common.h"

char *current_working_dir;
char *current_root_dir;

vfs_fd_entry vfs_fd_table[MAX_FILE_DESCRIPTORS];
static x_Mutex fd_table_Mutex;
static x_mutex fd_table_mutex = &fd_table_Mutex;

static vfs_FileOperations fat_ops;
static vfs_FileOperations placeholder_ops;

#define SET_ERRNO(n) { x_int temp = (n); woempa(7, "Set x_errno to %d\n", temp); x_errno = temp; }

static vfs_FD_Entry placeholder;

w_int  placeholder_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode);
size_t placeholder_get_length(vfs_fd_entry fde);
w_boolean placeholder_is_eof(vfs_fd_entry fde);
w_int  placeholder_seek  (vfs_fd_entry fde, w_int offset, w_int whence);
w_int  placeholder_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos);
w_int  placeholder_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos);

w_int  placeholder_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode) {
  woempa(9, "placeholder : attempt to open a file on an fd where an open is already taking place\n"); 
}

size_t placeholder_get_length(vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to get the length of an fd where an open is currentlyy taking place\n"); 
}

w_boolean placeholder_is_eof(vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to check for EOF on an fd where an open is currentlyy taking place\n"); 
}

w_int  placeholder_tell  (vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to tell in an fd where an open is currentlyy taking place\n"); 
}

w_int  placeholder_seek  (vfs_fd_entry fde, w_int offset, w_int whence) {
  woempa(9, "placeholder : attempt to seek in an fd where an open is currentlyy taking place\n"); 
}

w_int  placeholder_read  (vfs_fd_entry fde, char *bufer, w_size length, w_int *pos) {
  woempa(9, "placeholder : attempt to read from an fd where an open is currentlyy taking place\n"); 
}

w_int  placeholder_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos) {
  woempa(9, "placeholder : attempt to write to an fd where an open is currentlyy taking place\n"); 
}

void init_vfs(void) {
  memset(vfs_fd_table, 0, sizeof(vfs_fd_table));
  woempa(7, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  x_mutex_create(fd_table_mutex);
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
#ifdef DEBUG
  dumpDir("/");
#endif
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = ff_getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(7, "ff_getcwd returned NULL, errno = %d\n", stdioGET_ERRNO());
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(7, "current dir  : %s\n", current_working_dir);
  woempa(7, "current root : %s\n", current_root_dir);

  fat_ops.dummy = NULL;
  fat_ops.get_length = fat_get_length;
  fat_ops.is_eof = fat_is_eof;
  fat_ops.tell = fat_tell;
  fat_ops.seek = fat_seek;
  fat_ops.read = fat_read;
  fat_ops.write = fat_write;

  placeholder_ops.dummy = NULL;
  placeholder_ops.get_length = placeholder_get_length;
  placeholder_ops.is_eof = placeholder_is_eof;
  placeholder_ops.tell = placeholder_tell;
  placeholder_ops.seek = placeholder_seek;
  placeholder_ops.read = placeholder_read;
  placeholder_ops.write = placeholder_write;
  placeholder.path = "placeholder";
  placeholder.ops = &placeholder_ops;
}

w_int vfs_open(const char *path, w_word flags, w_word mode) {
  w_int fd;

  woempa(7, "Opening file %s as %s\n", path, VFS_O_ACCMODE2TEXT(flags));
  x_mutex_lock(fd_table_mutex, x_eternal);
  // TODO - fix it so that fds 0, 1, 2 appear to be occupied
  for (fd = 3; fd < MAX_FILE_DESCRIPTORS; fd++) {
    if (!vfs_fd_table[fd]) {
      vfs_fd_table[fd] = &placeholder;
      x_mutex_unlock(fd_table_mutex);
      vfs_fd_entry fde = allocClearedMem(sizeof(vfs_FD_Entry));
      w_int rc = fat_open(fde, path, flags, mode);
      if (0 == rc) {
// WAS:        fde->path = strdup(path);
// TODO: check for memory leaks
        fde->path = allocMem(strlen(path) + 1);
        memcpy(fde->path, path, strlen(path)+1);
        vfs_fd_table[fd] = fde;

        return fd;
      }
      else {
        vfs_fd_table[fd] = NULL;

        return -1;
      }
    }
  }

  // looks we ran out of file descriptors
  x_mutex_unlock(fd_table_mutex);
  SET_ERRNO(pdFREERTOS_ERRNO_ENMFILE);
  
  return -1;
}

w_int vfs_open(const char *path, w_word flags, w_word mode) {
  w_int fd;

  woempa(7, "Opening file %s as %s\n", path, VFS_O_ACCMODE2TEXT(flags));
  x_mutex_lock(fd_table_mutex, x_eternal);
  // TODO - fix it so that fds 0, 1, 2 appear to be occupied
  for (fd = 3; fd < MAX_FILE_DESCRIPTORS; fd++) {
    if (!vfs_fd_table[fd]) {
      vfs_fd_table[fd] = &placeholder;
      x_mutex_unlock(fd_table_mutex);
      vfs_fd_entry fde = allocClearedMem(sizeof(vfs_FD_Entry));
      w_int rc = fat_open(fde, path, flags, mode);
      if (0 == rc) {
// WAS:        fde->path = strdup(path);
// TODO: check for memory leaks
        fde->path = allocMem(strlen(path) + 1);
        memcpy(fde->path, path, strlen(path)+1);
        vfs_fd_table[fd] = fde;

        return fd;
      }
      else {
        vfs_fd_table[fd] = NULL;

        return -1;
      }
    }
  }

  // looks we ran out of file descriptors
  x_mutex_unlock(fd_table_mutex);
  SET_ERRNO(pdFREERTOS_ERRNO_ENMFILE);
  
  return -1;
}

w_int vfs_ftell(w_int fd) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->tell(fde);
}

w_int vfs_read(w_int fd, void *buf, w_size length) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    woempa(7, "failed to read from fd %d (%s), is not open\n", fd, fde->path);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->read(fde, buf, length, NULL);
}

w_int vfs_write(w_int fd, void *buf, w_size length) {
  FF_FILE *ff_fileptr = (FF_FILE *)vfs_fd_table[fd]->data;
  if (!ff_fileptr) {
    woempa(7, "failed to write to fd %d, fd is not in use\n", fd);
    // TODO set errno
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);
    return -1;
  }

  woempa(7, "writing %d bytes to fd %d\n", length, fd);
  if (ff_fwrite(buf, length, 1, ff_fileptr )) {
    woempa(7, "successfully wrote %d bytes to fd %d\n", length, fd);
    return length;
  }

  // less items written than requested => error
  woempa(7, "failed to write %d bytes to fd %d, errno = %d\n", length, fd, stdioGET_ERRNO());
  SET_ERRNO(stdioGET_ERRNO());

  return -1;

}

w_int vfs_lseek(w_int fd, w_int offset, w_int whence) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    woempa(7, "failed to seek in fd %d (%s), is not open\n", fd, fde->path);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->seek(fde, offset, whence);
}

/*
  Looks like there aint't no stat function that works with ff_fileptr, always need a path

w_int vfs_fstat(w_int fd, vfs_STAT *statBuf) {
  FF_FILE *ff_fileptr = (FF_FILE *)vfs_fd_table[fd]->data;
  if (!ff_fileptr) {
    woempa(7, "failed to stat fd %d, fd is not in use\n", fd);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);
    return -1;
  }

  // set errno on error
  return ff_fstat(ff_fileptr, statBuf )
}
*/

w_int vfs_close(w_int fd) {
  FF_FILE *ff_fileptr = (FF_FILE *)vfs_fd_table[fd]->data;
  if (!ff_fileptr) {
    woempa(7, "failed to close fd %d, fd is not in use\n", fd);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);
    return -1;
  }

  int rc = ff_fclose(ff_fileptr);
  // TODO set errno
  // SET_ERRNO(pdFREERTOS_ERRNO_EBADF);
  if (rc) return rc;
  x_mutex_lock(fd_table_mutex, x_eternal);
  // TODO release oathname
  releaseMem(vfs_fd_table[fd]);
  vfs_fd_table[fd]->data = NULL;
  x_mutex_unlock(fd_table_mutex);
  woempa(1, "closed fd %d\n", fd);
}

w_int vfs_ftell(w_int fd) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->tell(fde);
}
 
