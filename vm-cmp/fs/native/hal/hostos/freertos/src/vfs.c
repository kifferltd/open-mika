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

// NEW TEST //
#define FLASH_DISK_NAME                                        "/"
#define MAX_NUM_OF_SUB_DIRS 5
#define MAX_DIR_NAME_LENGTH 10
#define MAX_PATH_LENGTH 40

/*-----------------------------------------------------------*/

static FF_Disk_t *pxFlashDisk;

/*
static void doc( const char *pcDirPath, const char *pcFileName, const char *mode )
{
char path[MAX_PATH_LENGTH];
FF_FILE *file;
char charbuf;

        path[0] = '\0';
        strcat(path, pcDirPath);
        strncat(path, pcFileName, MAX_PATH_LENGTH - strlen(pcDirPath));
        file = ff_fopen(path, mode);
        if (file)
        {
             printf("opened %s for '%s'\n", path, mode);
             int len = ff_fread(&charbuf, 1, 1, file );
             if (len)
             {
                 printf("first byte is 0x%02x\n", charbuf);
             }
             else {
                 printf("could not read first byte, errno is %d\n", errno);
             }
             printf("closing %s\n", path);
             ff_fclose(file);
        }
        else {
             printf("failed to open %s for '%s'\n", path, mode);
        }
}
*/

static void dir( const char *pcDirPath )
{
FF_FindData_t xFindStruct;
const char        *pcAttrib;
const char        *pcWritableFile = "writable file";
const char        *pcReadOnlyFile = "read only file";
const char        *pcDirectory = "directory";
UBaseType_t i, next = 0;
char subdirs[MAX_NUM_OF_SUB_DIRS][MAX_DIR_NAME_LENGTH];
char path[MAX_PATH_LENGTH];
const char *mode;
 
        FF_PRINTF( "Directory content for %s:\n", pcDirPath); 
        memset( &xFindStruct, 0, sizeof( FF_FindData_t ) );

        if( ff_findfirst( pcDirPath, &xFindStruct ) == 0 )
        {
                do
                {
                        /* Point pcAttrib to a string that describes the file. */
                        if( ( xFindStruct.ucAttributes & FF_FAT_ATTR_DIR ) != 0 )
                        {
                                pcAttrib = pcDirectory;
                                if( strcmp(xFindStruct.pcFileName, ".") != 0 &&
                                        strcmp(xFindStruct.pcFileName, "..") != 0 )
                                {
                                        strncpy(subdirs[next++], xFindStruct.pcFileName, MAX_DIR_NAME_LENGTH);
                                }
                        }
                        else if( xFindStruct.ucAttributes & FF_FAT_ATTR_READONLY )
                        {
                                pcAttrib = pcReadOnlyFile;
                                mode = "r";
                        }
                        else
                        {
                                pcAttrib = pcWritableFile;
                                mode = "r+";
                        }

                        FF_PRINTF( "%s [%s] [size=%d]\n",
                                           xFindStruct.pcFileName,
                                           pcAttrib,
                                           xFindStruct.ulFileSize );

/*
                        if( ( xFindStruct.ucAttributes & FF_FAT_ATTR_DIR ) == 0 )
                        {
                                doc(pcDirPath, xFindStruct.pcFileName, mode);
                        }
*/
                } while( ff_findnext( &xFindStruct ) == 0 );
        }
        for (i = 0; i < next; i++)
        {
                path[0] = '\0';
                strcat(path, pcDirPath);
                strncat(path, subdirs[i], MAX_DIR_NAME_LENGTH);
                strcat(path, "/");
                dir(path);
        }
}



void init_vfs(void) {
  memset(vfs_fd_table, 0, sizeof(vfs_fd_table));
  woempa(9, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  x_mutex_create(fd_table_mutex);
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = ff_getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(9, "ff_getcwd returned NULL, errno = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(9, "current dir  : %s\n", current_working_dir);
  woempa(9, "current root : %s\n", current_root_dir);

// OLD TEST //
#ifdef DEBUG
//  dumpDir(current_root_dir, 0);
#endif

// NEW TEST //
  pxFlashDisk = FFInitFlash(FLASH_DISK_NAME, FLASH_CACHE_SIZE);

//  dir( FLASH_DISK_NAME );

}

w_int vfs_open(const char *pathname, w_word flags) {
  int fd;
  // TODO deal with more flags
  const char *mode = isSet(flags, O_RDONLY) ? "r" : "r+";
  FF_FILE *ff_fileptr = ff_fopen(pathname, mode);
  if (ff_fileptr) {
    // TODO - fix it so that fds 0, 1, 2 appear to be occupied
    for (fd = 2; fd < MAX_FILE_DESCRIPTORS; fd++) {
      if (!vfs_fd_table[fd].ff_fileptr) {
        vfs_fd_table[fd].ff_fileptr = ff_fileptr;
        vfs_fd_table[fd].flags = flags;
        woempa(7, "opened %s in mode %s, fd = %d\n", pathname, mode, fd);

        return fd;
      }
    }
  }

  w_int fat_errno = stdioGET_ERRNO();
  
  woempa(7, "unable to open %s in mode %s, fat_errno = %d\n", pathname, mode, fat_errno);
  // TODO we should set errno
  return -1;
}

w_int vfs_read(w_int fd, void *buf, w_size count) {
  woempa(7, "reading %d bytes from fd %d\n", count, fd);
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to read %d bytes from fd %d, fd is not in use\n", count, fd);
    // TODO set errno
    return -1;
  }

  int rc = ff_fread(buf, 1, count, ff_fileptr);
  if (rc == count) {
    woempa(7, "did read %d bytes from fd %d\n", count, fd);
    return count;
  }

  w_int fat_errno = stdioGET_ERRNO();
  woempa(7, "failed to read %d bytes from fd %d, fat_errno = %d\n", count, fd, fat_errno);
  // TODO set errno
  return -1;
}

w_int vfs_lseek(w_int fd, w_int offset, w_int whence) {
  woempa(7, "seeking %d bytes in fd %d from %s\n", offset, fd,
    whence == FF_SEEK_CUR ? "current file position" :
    whence == FF_SEEK_END ? "end of the file" :
    whence == FF_SEEK_SET ? "beginning of file" : "??? unknown ???");
  FF_FILE *ff_fileptr = vfs_fd_table[fd].ff_fileptr;
  if (!ff_fileptr) {
    woempa(7, "failed to seek %d bytes from fd %d, fd is not in use\n", offset, fd);
    // TODO set errno
    return -1;
  }

// According to [FreeRTOS+FAT Standard API Reference] :
// On success 0 is returned.
// If the read/write position could not be moved then -1 is returned and the task's errno is set to indicate the reason
  if (ff_fseek(ff_fileptr, offset, whence) == 0) {
    w_int new_pos = ff_ftell(ff_fileptr);
    woempa(7, "sought %d bytes from fd %d\n", offset, fd);
    return new_pos;
  }

  w_int fat_errno = stdioGET_ERRNO();
  woempa(7, "failed to seek %d bytes from fd %d, fat_errno = %d\n", offset, fd, fat_errno);
  // TODO set errno
  return -1;
}

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
  woempa(7, "closed fd %d\n", fd);
}
