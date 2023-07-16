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

#include "wonka.h"
#include "file-descriptor.h"
#include "oswald.h"
#include "ts-mem.h"
#include "vfs-common.h"
#include "vfs_fcntl.h"

char *current_working_dir;
char *current_root_dir;

vfs_fd_entry vfs_fd_table[MAX_FILE_DESCRIPTORS];
static x_Mutex fd_table_Mutex;
static x_mutex fd_table_mutex = &fd_table_Mutex;

static vfs_FileOperations fat_ops;
static vfs_FileOperations placeholder_ops;

#define SET_ERRNO(n) { x_int temp = (n); woempa(7, "Set x_errno to %d\n", temp); x_errno = temp; }

static vfs_FD_Entry placeholder;

static vfs_MountPoint mountpoint_sentinel = { NULL, NULL, &mountpoint_sentinel, &mountpoint_sentinel};

void registerMountPoint(vfs_mountpoint mountpoint) {
  x_list_insert(&mountpoint_sentinel, mountpoint);
#ifdef DEBUG
  woempa(7, "MountPoint list after adding element with path '%s':\n", mountpoint->prefix);
  for (vfs_mountpoint mp = mountpoint_sentinel.next; mp != &mountpoint_sentinel; mp = mp->next) {
    woempa(7, "  %s\n", mp->prefix);
  }
#endif
}

void deregisterMountPoint(vfs_mountpoint mountpoint) {
  x_list_remove(mountpoint);
  #ifdef DEBUG
  woempa(7, "MountPoint list after removing element with path '%s':\n", mountpoint->prefix);
  for (vfs_mountpoint mp = mountpoint_sentinel.next; mp != &mountpoint_sentinel; mp = mp->next) {
    woempa(7, "  %s\n", mp->prefix);
  }
#endif
}

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
  woempa(9, "placeholder : attempt to get the length of an fd where an open is currently taking place\n"); 
}

w_boolean placeholder_is_eof(vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to check for EOF on an fd where an open is currently taking place\n"); 
}

w_int  placeholder_tell  (vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to tell in an fd where an open is currently taking place\n"); 
}

w_int  placeholder_seek  (vfs_fd_entry fde, w_int offset, w_int whence) {
  woempa(9, "placeholder : attempt to seek in an fd where an open is currently taking place\n"); 
}

w_int  placeholder_read  (vfs_fd_entry fde, char *bufer, w_size length, w_int *pos) {
  woempa(9, "placeholder : attempt to read from an fd where an open is currently taking place\n"); 
}

w_int  placeholder_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos) {
  woempa(9, "placeholder : attempt to write to an fd where an open is currently taking place\n"); 
}

w_int placeholder_close(vfs_fd_entry fde) {
  woempa(9, "placeholder : attempt to close an fd where an open is currently taking place\n"); 
}

void init_vfs(void) {
  memset(vfs_fd_table, 0, sizeof(vfs_fd_table));
  woempa(7, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  x_mutex_create(fd_table_mutex);
// TODO initialise cvd and fsroot here instead of in hal/hostos/freertos

  placeholder_ops.dummy = NULL;
  placeholder_ops.get_length = placeholder_get_length;
  placeholder_ops.is_eof = placeholder_is_eof;
  placeholder_ops.tell = placeholder_tell;
  placeholder_ops.seek = placeholder_seek;
  placeholder_ops.read = placeholder_read;
  placeholder_ops.write = placeholder_write;
  placeholder_ops.close = placeholder_close;
  placeholder.path = "placeholder";
  placeholder.ops = &placeholder_ops;

// TODO - put an ifdef here
  init_fatfs();
}

w_int vfs_open(const char *path, w_word flags, w_word mode) {
  w_int fd;

  woempa(7, "Opening file %s as %s\n", path, VFS_O_ACCMODE2TEXT(flags));

  const char *longest_prefix = "";
  vfs_mountpoint mountpoint;
  for (vfs_mountpoint mp = mountpoint_sentinel.next; mp != &mountpoint_sentinel; mp = mp->next) {
    woempa(7, "current longest_prefix is '%s', comparing with '%s'\n", longest_prefix, mp->prefix);
    if (strlen(mp->prefix) > strlen(longest_prefix)) {
      woempa(7, "  '%s' is longer\n", mp->prefix);
      longest_prefix = mp->prefix;
      mountpoint = mp;
    }
  }
  woempa(7, "mountpoint is '%s'\n", mountpoint->prefix);

  if (!mountpoint) {
    woempa(7, "Unable to open %s, no filesystem is mounted\n", path);
    return -1;
  }

  x_mutex_lock(fd_table_mutex, x_eternal);
  // TODO - fix it so that fds 0, 1, 2 appear to be occupied
  for (fd = 3; fd < MAX_FILE_DESCRIPTORS; fd++) {
    if (!vfs_fd_table[fd]) {
      vfs_fd_table[fd] = &placeholder;
      x_mutex_unlock(fd_table_mutex);
      vfs_fd_entry fde = allocClearedMem(sizeof(vfs_FD_Entry));
      w_int rc = mountpoint->fileops->open(fde, path, flags, mode);
      if (0 == rc) {
// WAS:        fde->path = strdup(path);
// TODO: check for memory leaks
        fde->path = allocMem(strlen(path) + 1);
        strcpy(fde->path, path);
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
 
w_int vfs_get_length(w_int fd) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->get_length(fde);
}
 
w_int vfs_read(w_int fd, void *buf, w_size length) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    woempa(7, "failed to read from fd %d, is not open\n", fde->path);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->read(fde, buf, length, NULL);
}

w_int vfs_write(w_int fd, void *buf, w_size length) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->write(fde, buf, length, NULL);
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

w_int vfs_close(w_int fd) {
  x_mutex_lock(fd_table_mutex, x_eternal);
  vfs_fd_entry fde = vfs_fd_table[fd];
  vfs_fd_table[fd] = NULL;
  x_mutex_unlock(fd_table_mutex);

  if (!fde) {
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }


  w_int rc = fde->ops->close(fde);

  // only now is it safe to free the fde memory
  releaseMem(fde->path);
  releaseMem(fde);

  return rc;
}
 
