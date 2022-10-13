/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
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

char *current_working_dir;
char *current_root_dir;
vfs_fd_entry vfs_fd_table[MAX_FILE_DESCRIPTORS];
static x_Mutex fd_table_Mutex;
static x_mutex fd_table_mutex = &fd_table_Mutex;

#ifdef DEBUG
static void dumpDir(const char *path, int level) {
  woempa(7, "%*sScanning directory %s", level * 4, "", path);
  FF_FindData_t *findData = allocClearedMem(sizeof(FF_FindData_t));
  int rc = ff_findfirst(path, findData );
  while (rc == 0) {
    woempa(7, "%*s%s [%s %s] [size=%d]", level * 4, "", findData->pcFileName, (findData->ucAttributes & FF_FAT_ATTR_DIR) ? "DIR" : "", (findData->ucAttributes && FF_FAT_ATTR_DIR) ? "RO" : "", findData->ulFileSize);
    if ((findData->ucAttributes & FF_FAT_ATTR_DIR) && strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
      char *pathbuf = allocMem(strlen(path) + strlen(findData->pcFileName) + 2);
      sprintf(pathbuf, "%s%s/", path, findData->pcFileName);
      dumpDir(pathbuf, level + 1);
      releaseMem(pathbuf);
    }
    rc =  ff_findnext( findData ) == 0;
  }
  releaseMem(findData);
  woempa(7, "%*sEnd of directory %s", level * 4, "", path);
}
#endif

#define FLASH_DISK_NAME    "/"

static FF_Disk_t *vfs_flashDisk;

void init_vfs(void) {
  memset(vfs_fd_table, 0, sizeof(vfs_fd_table));
  woempa(7, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  x_mutex_create(fd_table_mutex);
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = ff_getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(7, "ff_getcwd returned NULL, errno = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(7, "current dir  : %s\n", current_working_dir);
  woempa(7, "current root : %s\n", current_root_dir);
}

w_int vfs_open(const char *pathname, w_word flags, w_word mode) {
  int fd;
  // TODO deal with more flags
  const char *how = (flags == O_RDONLY) ? "r" : "w";
  FF_FILE *ff_fileptr = ff_fopen(pathname, how);
  if (ff_fileptr) {
    // TODO - fix it so that fds 0, 1, 2 appear to be occupied
    for (fd = 3; fd < MAX_FILE_DESCRIPTORS; fd++) {
      if (!vfs_fd_table[fd].ff_fileptr) {
        vfs_fd_table[fd].ff_fileptr = ff_fileptr;
        vfs_fd_table[fd].flags = flags;
        woempa(1, "opened %s in mode %s, fd = %d\n", pathname, how, fd);

        return fd;
      }
    }
  }

  w_int fat_errno = stdioGET_ERRNO();
  
  woempa(7, "unable to open %s in mode %s, fat_errno = %d\n", pathname, how, fat_errno);
  // TODO we should set errno
  return -1;
}

w_int vfs_ftell(w_int fd) {
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to read from fd %d, fd is not in use\n", fd);
    // TODO set errno
    return -1;
  }

  // TODO set errno on error
  return ff_ftell(ff_fileptr);
}

w_int vfs_read(w_int fd, void *buf, w_size length) {
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to read from fd %d, fd is not in use\n", fd);
    // TODO set errno
    return -1;
  }

  if (ff_feof(ff_fileptr)) {
    woempa(7, "failed to read from fd %d, fd is at EOF\n", fd);
    return -1;
  }

  woempa(1, "reading from fd %d\n", fd);
  long offset = ff_ftell(ff_fileptr);
  size_t filelen = ff_filelength(ff_fileptr);
  if (offset > filelen) {
    return -1;
  }

  w_size effective = offset + length > filelen ? filelen - offset : length;
  woempa(1, "requested read = %d bytes from offset %d, file length = %d, effective = %d \n", length, offset, filelen, effective);

  int rc = ff_fread(buf, 1, effective, ff_fileptr);
  if (rc == effective) {
    woempa(1, "did read %d bytes from fd %d\n", effective, fd);
    return effective;
  }

  w_int fat_errno = stdioGET_ERRNO();
  woempa(7, "failed to read %d bytes from fd %d, fat_errno = %d\n", effective, fd, fat_errno);
  // TODO set errno
  return -1;
}

w_int vfs_write(w_int fd, void *buf, w_size length) {
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to write to fd %d, fd is not in use\n", fd);
    // TODO set errno
    return -1;
  }

  woempa(1, "writing to %d bytes to fd %d\n", length, fd);
  // TODO !!!
  int rc = ff_fwrite(buf, 1, length, ff_fileptr );
  w_int fat_errno = stdioGET_ERRNO();
  if (fat_errno) {
    woempa(7, "failed to read %d bytes from fd %d, fat_errno = %d\n", length, fd, fat_errno);
    // TODO set errno
    return -1;
  }

  return rc;
}

w_int vfs_lseek(w_int fd, w_int offset, w_int whence) {
  woempa(1, "seeking %d bytes in fd %d from %s\n", offset, fd,
    whence == FF_SEEK_CUR ? "current file position" :
    whence == FF_SEEK_END ? "end of the file" :
    whence == FF_SEEK_SET ? "beginning of file" : "??? unknown ???");
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    wabort(ABORT_WONKA, "failed to seek %d bytes from fd %d, fd is not in use\n", offset, fd);
    // TODO set errno
    return -1;
  }

// According to [FreeRTOS+FAT Standard API Reference] :
// On success 0 is returned.
// If the read/write position could not be moved then -1 is returned and the task's errno is set to indicate the reason
  if (ff_fseek(ff_fileptr, offset, whence) == 0) {
    w_int new_pos = ff_ftell(ff_fileptr);
    woempa(1, "sought %d bytes from fd %d, offset is now %d\n", offset, fd, new_pos);
    return new_pos;
  }

  w_int fat_errno = stdioGET_ERRNO();
  woempa(9, "failed to seek %d bytes from fd %d, fat_errno = %d\n", offset, fd, fat_errno);
  // TODO set errno
  return -1;
}

/*
  Looks like there aint't no stat function that works with ff_fileptr, always need a path

w_int vfs_fstat(w_int fd, vfs_STAT *statBuf) {
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    wabort(ABORT_WONKA, "failed to stat fd %d, fd is not in use\n", fd);
    // TODO set errno
    return -1;
  }

  // set errno on error
  return ff_fstat(ff_fileptr, statBuf )
}
*/

w_int vfs_close(w_int fd) {
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to close fd %d, fd is not in use\n", fd);
    // TODO set errno
    return EOF;
  }

  int rc = ff_fclose(ff_fileptr);
  // TODO set errno
  if (rc) return rc;
  vfs_fd_table[fd].ff_fileptr = NULL;
  woempa(1, "closed fd %d\n", fd);
}
