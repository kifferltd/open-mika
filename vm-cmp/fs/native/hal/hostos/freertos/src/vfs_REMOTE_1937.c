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

#include "vfs.h"

static x_Mutex fd_table_Mutex;
static x_mutex fd_table_mutex = &fd_table_Mutex;

static vfs_FileOperations fat_ops;
static vfs_FileOperations placeholder_ops;

#ifdef DEBUG
#include "fifo.h"

static w_fifo fifi;

static int countSlashes(const char* s) {
  int n = 0;
  char *cursor = strchr(s,'/');
  const char *end = s + strlen(s);
  while (cursor && cursor < end) {
    n++;
    cursor=strchr(cursor+1,'.');
  }

  return n;
}

static void dumpSubDir(const char *path) {
  const int level = countSlashes(path) - 1;
  char *pathbuf = NULL;

  woempa(7, "%*sScanning directory %s\n", level * 4, " ", path);
  FF_FindData_t *findData = allocClearedMem(sizeof(FF_FindData_t));
  int rc = ff_findfirst(path, findData );
  while (rc == 0) {
    bool isDir = findData->ucAttributes & FF_FAT_ATTR_DIR;
    bool isReadOnly = findData->ucAttributes && FF_FAT_ATTR_READONLY;
    // ignore . and ..
    if (strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
      woempa(7, "%*s%s [%s %s] [size=%d]\n", level * 4, "", findData->pcFileName, isDir ? "DIR" : "", isReadOnly ? "RO" : "", findData->ulFileSize);
      if (isDir) {
        size_t pathlen = strlen(path) + strlen(findData->pcFileName) + 4;
        pathbuf = allocMem(pathlen);
        snprintf(pathbuf, pathlen, "%s%s/", path, findData->pcFileName);
        woempa(1, "pushing %s onto fifo\n", pathbuf);
        putFifo(pathbuf, fifi);
      }
    }
    rc =  ff_findnext( findData );
  }
  releaseMem(findData);
  woempa(7, "%*sEnd of directory %s\n", level * 4, "", path);

  while ((pathbuf = getFifo(fifi))) {
    woempa(1, "pulled %s from fifo\n", pathbuf);
    dumpSubDir(pathbuf);
    releaseMem(pathbuf);
  }
}

/*
 * Dump out the contents of a directory and all its subdirectories.
 * 'path' should begin and end with a slash, e.g. "/" or "/app/".
 */
static void dumpDir(const char *path) {
  char *pathbuf = NULL;

  woempa(7, "Scanning directory %s\n", path);
  fifi = allocFifo(30);

  FF_FindData_t *findData = allocClearedMem(sizeof(FF_FindData_t));
  int rc = ff_findfirst(path, findData );
  while (rc == 0) {
    bool isDir = findData->ucAttributes & FF_FAT_ATTR_DIR;
    bool isReadOnly = findData->ucAttributes && FF_FAT_ATTR_READONLY;
    // ignore . and ..
    if (strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
      woempa(7, "%s [%s %s] [size=%d]\n", findData->pcFileName, isDir ? "DIR" : "", isReadOnly ? "RO" : "", findData->ulFileSize);
      if ((findData->ucAttributes & FF_FAT_ATTR_DIR) && strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
        size_t pathlen = strlen(path) + strlen(findData->pcFileName) + 2;
        pathbuf = allocMem(pathlen);
        snprintf(pathbuf, pathlen, "%s%s/", path, findData->pcFileName);
        woempa(1, "pushing %s onto fifo\n", pathbuf);
        putFifo(pathbuf, fifi);
      }
    }
    rc =  ff_findnext( findData );
  }
  releaseMem(findData);
  woempa(7, "End of directory %s\n", path);

  while ((pathbuf = getFifo(fifi))) {
    woempa(1, "pulled %s from fifo\n", pathbuf);
    dumpSubDir(pathbuf);
    releaseMem(pathbuf);
  }

  releaseFifo(fifi);
}

static void dumpFile(const char *path) {
  FF_FILE *ff_fileptr = ff_fopen(path, "r");
  char *buffer = allocClearedMem(65);
  int rc;

  if (!ff_fileptr) {
    woempa(9, "Could not open %s!\n", path);
    return;
  }

  while (rc = ff_fread(buffer, 1, 64, ff_fileptr)) {
    buffer[rc] = 0;
    x_debug_puts(buffer);
    x_thread_sleep(x_millis2ticks(100));
  }

  ff_fclose(ff_fileptr);
  releaseMem(buffer);
}

#endif

#define FLASH_DISK_NAME    "/"

#define SET_ERRNO(n) { x_int temp = (n); woempa(7, "Set x_errno to %d\n", temp); x_errno = temp; }
static FF_Disk_t *vfs_flashDisk;

static vfs_FD_Entry placeholder;

static vfs_MountPoint fat_mountpoint = {"/", &fat_ops, NULL, NULL};

w_int  fat_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode);
size_t fat_get_length(vfs_fd_entry fde);
w_boolean fat_is_eof(vfs_fd_entry fde);
w_int  fat_tell  (vfs_fd_entry fde);
w_int  fat_seek  (vfs_fd_entry fde, w_int offset, w_int whence);
w_int  fat_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos);
w_int  fat_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos);
w_int  fat_close  (vfs_fd_entry fde);

void init_fatfs(void) {
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
#ifdef DEBUG
  dumpDir("/");
  woempa(7, "\nDumping results from previous run\n\n");
  dumpFile("/test/workspace/results");
  woempa(7, "\n===========================\n\n");
#endif
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
#ifdef DEBUG
  dumpDir("/");
  woempa(7, "\nDumping results from previous run\n\n");
  dumpFile("/test/workspace/results");
  woempa(7, "\n===========================\n\n");
#endif
// TODO - virtualise getcwd
  char *cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = ff_getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(7, "ff_getcwd returned NULL, errno = %d\n", stdioGET_ERRNO());
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(7, "current dir  : %s\n", current_working_dir);
  woempa(7, "current root : %s\n", current_root_dir);

  fat_ops.dummy = NULL;
  fat_ops.open = fat_open;
  fat_ops.get_length = fat_get_length;
  fat_ops.is_eof = fat_is_eof;
  fat_ops.tell = fat_tell;
  fat_ops.seek = fat_seek;
  fat_ops.read = fat_read;
  fat_ops.write = fat_write;
  fat_ops.close = fat_close;

  registerMountPoint(&fat_mountpoint);
}

w_int fat_open(vfs_fd_entry fde, const char *path, w_word flags, w_word mode) {
  w_int accessmode = flags & VFS_O_ACCMODE;
  if (accessmode > VFS_O_RDWR) {
    SET_ERRNO(pdFREERTOS_ERRNO_EINVAL); // TODO declare as EINVAL

    return -1;
  }

  w_int append = flags & VFS_O_APPEND;
  char how[4] = {0};
  how[0] = accessmode == VFS_O_RDONLY ? 'r' :  append ? 'a' : 'w';
  how[1] = accessmode < VFS_O_RDWR ? 0 : '+';
  FF_FILE *ff_fileptr = ff_fopen(path, how);
  if (ff_fileptr) {
    fde->data = ff_fileptr;
    fde->flags = flags;
    fde->ops = &fat_ops;
    woempa(1, "opened %s in mode %s\n", path, how);

    x_errno = 0;
    return 0;
  }

  woempa(7, "unable to open %s in mode %s\n", path, how);
  SET_ERRNO(stdioGET_ERRNO());

  return -1;
}

w_int fat_tell(vfs_fd_entry fde) {
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;

  // N.B. ff_tell returns a 64-bit value
  return (w_int)ff_ftell(ff_fileptr);
}

w_int fat_read(vfs_fd_entry fde, char *buffer, w_size length, w_int *pos) {
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;
  if (fde->ops->is_eof(fde)) {
    woempa(7, "failed to read from %s, fd is at EOF\n", fde->path);

    return -1;
  }

  long offset = ff_ftell(ff_fileptr);
  size_t filelen = fde->ops->get_length(fde);
  woempa(1, "reading from %s, offset is %d, file length is %d\n", fde->path, offset, filelen);
  if (offset > filelen) {

    return -1;
  }

  size_t effective = offset + length > filelen ? filelen - offset : length;
  woempa(1, "requested read = %d bytes from offset %d, file length = %d, effective = %d \n", length, offset, filelen, effective);

  size_t rc = ff_fread(buffer, 1, effective, ff_fileptr);
  if (rc == effective) {
    woempa(1, "did read %d bytes from %s\n", effective, fde->path);
    if (pos) {
      *pos = fat_tell(fde);
    }

    return (w_int)effective;
  }

  woempa(7, "failed to read %d bytes from %s\n", effective, fde->path);
  SET_ERRNO(stdioGET_ERRNO());

  return -1;
}

w_int fat_seek(vfs_fd_entry fde, w_int offset, w_int whence) {
  woempa(1, "seeking %d bytes in %s from %s\n", offset, fde->path,
    whence == FF_SEEK_CUR ? "current file position" :
    whence == FF_SEEK_END ? "end of the file" :
    whence == FF_SEEK_SET ? "beginning of file" : "??? unknown ???");
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;
  if (!ff_fileptr) {
    woempa(7, "failed to seek %d bytes, fd is not in use\n", offset);
    SET_ERRNO(pdFREERTOS_ERRNO_EBADF);
    return -1;
  }

// According to [FreeRTOS+FAT Standard API Reference] :
// On success 0 is returned.
// If the read/write position could not be moved then -1 is returned and the task's errno is set to indicate the reason
  if (ff_fseek(ff_fileptr, offset, whence) == 0) {
    w_int new_pos = ff_ftell(ff_fileptr);
    woempa(1, "sought %d bytes from %d, offset is now %d\n", offset, fde->path, new_pos);
    return new_pos;
  }

  woempa(7, "failed to seek %d bytes from %s\n", offset, fde->path);
  SET_ERRNO(stdioGET_ERRNO());

  return -1;
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

w_int fat_close(vfs_fd_entry fde) {
  FF_FILE *ff_fileptr = fde->data;

  woempa(7, "closing %s\n", fde->path);
  if (ff_fclose(ff_fileptr) == 0) {
    woempa(7, "successfully closed %s\n", fde->path);
    return 0;
  }

  SET_ERRNO(stdioGET_ERRNO());

  return -1;
}

size_t fat_get_length(vfs_fd_entry fde) {
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;
  return ff_filelength(ff_fileptr);
}

w_boolean fat_is_eof(vfs_fd_entry fde) {
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;
  return ff_feof(ff_fileptr);
}

w_int fat_write(vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos) {
  FF_FILE *ff_fileptr = (FF_FILE *)fde->data;

  woempa(1, "writing %d bytes from %p to %s\n", length, buffer, fde->path);
  if (ff_fwrite(buffer, length, 1, ff_fileptr )) {
    woempa(1, "successfully wrote %d bytes to %s\n", length, fde->path);
    // TODO set pos
    return length;
  }

  // less items written than requested => error
  woempa(7, "failed to write %d bytes to %s, errno = %d\n", length, fde->path, stdioGET_ERRNO());
  SET_ERRNO(stdioGET_ERRNO());

  return -1;
}

