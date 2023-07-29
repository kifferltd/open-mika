/**************************************************************************
* Copyright (c) 2020, 2021, 2022, 2023 by KIFFER Ltd. All rights reserved.*
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

#ifndef HAVE_VFS_H
#define HAVE_VFS_H

#ifdef FS_NON_BLOCKING
ERROR - non-blocking file access is not supported on FreeRTOS
#endif

#define _FILE_OFFSET_BITS 32

#include <ff_stdio.h>
#include <unistd.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <utime.h>
 
#include "file-descriptor.h"
#include "ts-mem.h"
#include "vfs-common.h"
#include "vfs_fcntl.h"

// Maybe 256Ki was too big?
#define FLASH_CACHE_SIZE (16*1024)

extern char *current_working_dir;
extern char *current_root_dir;
extern char *fsroot;

#define close_vfs()                woempa(9, "close_vfs -> Using native filesystem\n")
#define vfs_mount(...)            woempa(9, "vfs_mount -> Using native filesystem\n", __VA_ARGS__);

extern char *command_line_path;

void init_vfs(void);

#define vfs_STAT                   FF_Stat_t

extern w_int vfs_open(const char *pathname, w_word flags, w_word mode);
extern w_int vfs_read(w_int fd, void *buf, w_size count);
extern w_int vfs_write(w_int fd, void *buf, w_size count);
extern w_int vfs_lseek(w_int fd, w_int offset, w_int whence);
extern w_int vfs_close(w_int fd);

#define vfs_getcwd(b,l)           ff_getcwd(b,l)
//#define vfs_fopen(path, ...)      ff_fopen(path, __VA_ARGS__)
//#define vfs_fdopen(...)           fdopen(__VA_ARGS__)

//#define vfs_fseek(...)            fseek(__VA_ARGS__)
//#define vfs_ftell(...)            ftell(__VA_ARGS__)
#define vfs_rewind(...)           rewind(__VA_ARGS__)
#define vfs_fgetpos(...)          fgetpos(__VA_ARGS__)
#define vfs_fsetpos(...)          fsetpos(__VA_ARGS__)

//#define vfs_fread(...)            fread(__VA_ARGS__)
//#define vfs_fwrite(...)           fwrite(__VA_ARGS__)

#define vfs_fopen(path, ...)      fopen(path, __VA_ARGS__)
#define vfs_fdopen(...)           fdopen(__VA_ARGS__)

#define vfs_feof(...)             feof(__VA_ARGS__)
#define vfs_ferror(...)           ferror(__VA_ARGS__)
#define vfs_clearerr(...)         clearerr(__VA_ARGS__)

#define vfs_fgetc(...)            fgetc(__VA_ARGS__)
#define vfs_getc(...)             getc(__VA_ARGS__)
#define vfs_fgets(...)            fgets(__VA_ARGS__)
#define vfs_fputc(...)            fputc(__VA_ARGS__)
#define vfs_fputs(...)            fputs(__VA_ARGS__)
#define vfs_putc(...)             putc(__VA_ARGS__)

#define vfs_fflush(...)           fflush(__VA_ARGS__)

#define vfs_FILE                   FILE

/*
** The following functions have no blocking issues
*/

// FIXME - FreeRTOS FAT must have a way to set access time
#define vfs_utime(a,b)             (-1)

#define vfs_opendir(path)          opendir(path)
#define vfs_closedir(a)            closedir(a)
#define vfs_readdir(a)             readdir(a)
#define vfs_rewinddir(a)           rewinddir(a)
#define vfs_telldir(...)          telldir(__VA_ARGS__)
#define vfs_seekdir(...)          seekdir(__VA_ARGS__)
#define vfs_alphasort(...)        alphasort(__VA_ARGS__)

#define vfs_stat_struct           ff_stat_struct
//#define vfs_fstat(...)            fstat(__VA_ARGS__)
#define vfs_stat(path, statbufptr)       ff_stat(path,statbufptr)
#define vfs_truncate(path,len)    ff_truncate(path, len)

#define vfs_mkdir(path, ...)      ff_mkdir(path)
#define vfs_rmdir(path)           ff_rmdir(path)
#define vfs_unlink(path)           unlink(path)
#define vfs_chmod(path, ...)      chmod(path, __VA_ARGS__)
#define vfs_fchmod(...)           fchmod(__VA_ARGS__)

#define vfs_rename(a, b)           rename(a, b)

#define vfs_STAT                   FF_STAT

#define vfs_dirent                 struct dirent
#define vfs_DIR                    FF_FindData_t
#define vfs_fpos_t                 w_int

void startVFS(void);

#endif // HAVE_VFS_H
