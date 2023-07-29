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

typedef struct vfs_RamFileData {
  w_word mode;
  char *buffer;
  size_t length;
  size_t offset;
} vfs_RamFileData;

w_int  ram_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode);
size_t ram_get_length(vfs_fd_entry fde);
w_boolean ram_is_eof(vfs_fd_entry fde);
w_int  ram_tell  (vfs_fd_entry fde);
w_int  ram_seek  (vfs_fd_entry fde, w_int offset, w_int whence);
w_int  ram_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos);
w_int  ram_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos);
w_int  ram_close  (vfs_fd_entry fde);

static vfs_FileOperations ram_ops = {
  .dummy = NULL,
  .open = ram_open,
  .get_length = ram_get_length,
  .is_eof = ram_is_eof,
  .tell = ram_tell,
  .seek = ram_seek,
  .read = ram_read,
  .write = ram_write,
  .close = ram_close,
};

static vfs_fd_entry convertToRamDisk(vfs_fd_entry base_fde, w_word mode) {
  vfs_fd_entry ram_fde = allocClearedMem(sizeof(vfs_FD_Entry));
  ram_fde->path = base_fde->path; 
  ram_fde->flags = base_fde->flags;
  ram_fde->ops = &ram_ops;
  vfs_RamFileData *ram_fde_data = allocClearedMem(sizeof(vfs_RamFileData));
  size_t length = base_fde->ops->get_length(base_fde);
  char *buffer = allocMem(length);
  ram_fde_data->mode = mode;
  ram_fde_data->buffer = buffer;
  ram_fde_data->length = length;
  ram_fde->data = ram_fde_data;
 
 
  // TODO do we need to split this into multiple reads? (probably not)
  w_int returned_length = base_fde->ops->read(base_fde, buffer, length, NULL);
  if ((w_size) returned_length < length) {
    woempa(7, "Failed to read %d bytes from underlying file %s, got %d\n", length, base_fde->path, returned_length);
    releaseMem(ram_fde_data);
    releaseMem(buffer);

    return -1;
  }
  base_fde->ops->close(base_fde);
  // don't release base_fde->path, it's now pointed to by ram_fde
  releaseMem(base_fde);

  return ram_fde;
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

  woempa(5, "Opening file %s as %s\n", path, VFS_O_ACCMODE2TEXT(flags));

  const char *longest_prefix = "";
  vfs_mountpoint mountpoint;
  for (vfs_mountpoint mp = mountpoint_sentinel.next; mp != &mountpoint_sentinel; mp = mp->next) {
    woempa(5, "current longest_prefix is '%s', comparing with '%s'\n", longest_prefix, mp->prefix);
    if (strlen(mp->prefix) > strlen(longest_prefix)) {
      woempa(5, "  '%s' is longer\n", mp->prefix);
      longest_prefix = mp->prefix;
      mountpoint = mp;
    }
  }
  woempa(5, "mountpoint is '%s'\n", mountpoint->prefix);

  if (!mountpoint) {
    woempa(7, "Unable to open %s, no filesystem is mounted here\n", path);
    // TODO virtualise errno values
    SET_ERRNO(pdFREERTOS_ERRNO_ENOENT);

    return -1;
  }

  x_mutex_lock(fd_table_mutex, x_eternal);
  // TODO - fix it so that fds 0, 1, 2 appear to be occupied
  for (fd = 3; fd < MAX_FILE_DESCRIPTORS; fd++) {
    if (!vfs_fd_table[fd]) {
      vfs_fd_table[fd] = &placeholder;
      x_mutex_unlock(fd_table_mutex);
      vfs_fd_entry fde = allocClearedMem(sizeof(vfs_FD_Entry));
  // TODO: check for memory leaks
      fde->path = allocMem(strlen(path) + 1);
      strcpy(fde->path, path);
      fde->flags = flags;
      fde->ops = mountpoint->fileops;
      w_int rc = mountpoint->fileops->open(fde, path, flags, mode);
      if (0 == rc) {
        if (isSet(flags, VFS_O_MIKA_RAMDISK)) {
          fde = convertToRamDisk(fde, mode & ~VFS_O_MIKA_RAMDISK);
        }
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
   // TODO virtualise errno values
   SET_ERRNO(pdFREERTOS_ERRNO_ENMFILE);
  
  return -1;
}

w_int vfs_ftell(w_int fd) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
   // TODO virtualise errno values
     SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->tell(fde);
}
 
w_int vfs_get_length(w_int fd) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
   // TODO virtualise errno values
     SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->get_length(fde);
}
 
w_int vfs_read(w_int fd, void *buf, w_size length) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    woempa(7, "failed to read from fd %d, is not open\n", fde->path);
    // TODO virtualise errno values
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->read(fde, buf, length, NULL);
}

w_int vfs_write(w_int fd, void *buf, w_size length) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
   // TODO virtualise errno values
     SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }

  return fde->ops->write(fde, buf, length, NULL);
}

w_int vfs_lseek(w_int fd, w_int offset, w_int whence) {
  vfs_fd_entry fde = vfs_fd_table[fd];
  if (!fde) {
    woempa(7, "failed to seek in fd %d (%s), is not open\n", fd, fde->path);
   // TODO virtualise errno values
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
   // TODO virtualise errno values
     SET_ERRNO(pdFREERTOS_ERRNO_EBADF);

    return -1;
  }


  w_int rc = fde->ops->close(fde);

  // only now is it safe to free the fde memory
  releaseMem(fde->path);
  releaseMem(fde);

  return rc;
}

// File operations for files which are opened with the VFS_O_MIKA_RAMDISK flag

w_int  ram_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode) {
  wabort(ABORT_WONKA, "Bzzzt! ram_open() cannot be called directly, you need to call vfs_open() with the VFS_O_MIKA_RAMDISK flag\n");
}

size_t ram_get_length(vfs_fd_entry fde) {
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;
  return ram_fde_data->length;
}

w_boolean ram_is_eof(vfs_fd_entry fde) {
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;  return ram_fde_data->offset >= ram_fde_data->length;
}

w_int  ram_tell  (vfs_fd_entry fde) {
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;
  return ram_fde_data->offset;
}

w_int  ram_seek  (vfs_fd_entry fde, w_int offset, w_int whence) {
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;
  switch (whence) {
    case SEEK_SET:
      ram_fde_data->offset = offset;
      break;

    case SEEK_CUR:
      ram_fde_data->offset += offset;
      break;

    case SEEK_END:
      ram_fde_data->offset = ram_fde_data->length + offset;
      break;

    default:
      SET_ERRNO(pdFREERTOS_ERRNO_EINVAL);
      return -1;
  }

  return ram_fde_data->offset;
}

w_int  ram_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos) {
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;
  w_size copy_size = ram_fde_data->length - ram_fde_data->offset > length ? length : ram_fde_data->length - ram_fde_data->offset;
  woempa(1, "Copying %d bytes from %p to buffer %p\n", copy_size, ram_fde_data->buffer + ram_fde_data->offset, buffer);
  memcpy(buffer, ram_fde_data->buffer + ram_fde_data->offset, copy_size);
  ram_fde_data->offset += copy_size;
  return copy_size;
}

w_int  ram_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos) {
  woempa(7, "TBD\n");
  return -1;
}

w_int  ram_close  (vfs_fd_entry fde) {
  woempa(5, "Closing %s\n", fde->path);
  vfs_RamFileData *ram_fde_data = (vfs_RamFileData*)fde->data;
  releaseMem(ram_fde_data->buffer);

  return 0;
}

