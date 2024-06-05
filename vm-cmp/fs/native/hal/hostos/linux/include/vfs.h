/**************************************************************************
* Copyright (c) 2024 by KIFFER Ltd. All rights reserved.                  *
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

#ifndef VFS_INCLUDE
#define VFS_INCLUDE

#include <sys/stat.h>
#include <unistd.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <errno.h>
#include <stdio.h>
#include <utime.h>
 
#include "file-descriptor.h"
#include "vfs-common.h"
#include "vfs_fcntl.h"
#include "ts-mem.h"

#ifdef OSWALD
#define FS_NON_BLOCKING
#include "oswald.h"
#endif

extern char *current_working_dir;
extern char *current_root_dir;
extern char *fsroot;

/* Macros to map the vfs_* functions to their normal C equivalents */

/* NOT NEEDED (?)
#define close_vfs()                woempa(9, "close_vfs -> Using native filesystem\n")
#define vfs_mount(...)            woempa(9, "vfs_mount -> Using native filesystem\n", __VA_ARGS__);

extern char *command_line_path;

static char *cwdbuffer;

#define MAX_CWD_SIZE 1024
*/

#ifndef FS_NON_BLOCKING

/*
** However, there's a problem with the following functions when using Oswald. Since Oswald is multithreading in a single 
** process, a function that blocks the process blocks every single thread. Therefore it's needed to make all calls 
** non-blocking.
*/

/* NOT NEEDED (?)
#define vfs_open(path, ...)       open(path, __VA_ARGS__)
#define vfs_creat(path, ...)      creat(path, __VA_ARGS__)
#define vfs_read(...)             read(__VA_ARGS__)
#define vfs_write(...)            write(__VA_ARGS__)

#define vfs_fopen(path, ...)      fopen(path, __VA_ARGS__)
#define vfs_fdopen(...)           fdopen(__VA_ARGS__)
#define vfs_fclose(...)           fclose(__VA_ARGS__)

#define vfs_fseek(...)            fseek(__VA_ARGS__)
#define vfs_ftell(...)            ftell(__VA_ARGS__)
#define vfs_rewind(...)           rewind(__VA_ARGS__)
#define vfs_fgetpos(...)          fgetpos(__VA_ARGS__)
#define vfs_fsetpos(...)          fsetpos(__VA_ARGS__)

#define vfs_fread(...)            fread(__VA_ARGS__)
#define vfs_fwrite(...)           fwrite(__VA_ARGS__)

#define vfs_fopen(path, ...)      fopen(path, __VA_ARGS__)
#define vfs_fdopen(...)           fdopen(__VA_ARGS__)
#define vfs_fclose(...)           fclose(__VA_ARGS__)

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
*/

#define VFS_FILEOPS_DEFINED

#endif /* !FS_NON_BLOCKING */

/*
** The following functions have no blocking issues
*/

/* NOT NEEDED (?)
#define vfs_close(a)               close(a)
#define vfs_lseek(a,b,c)           lseek(a,b,c)

#define vfs_utime(a,b)             utime(a,b)

#define vfs_opendir(path)          opendir(path)
#define vfs_closedir(a)            closedir(a)
#define vfs_readdir(a)             readdir(a)
#define vfs_rewinddir(a)           rewinddir(a)
#define vfs_telldir(...)          telldir(__VA_ARGS__)
#define vfs_seekdir(...)          seekdir(__VA_ARGS__)
#define vfs_alphasort(...)        alphasort(__VA_ARGS__)

#define vfs_fstat(...)            fstat(__VA_ARGS__)
#define vfs_stat(path, ...)       stat(path, __VA_ARGS__)
#define vfs_truncate(path,len)    truncate(path, len)

#define vfs_mkdir(path, ...)      mkdir(path, __VA_ARGS__)
#define vfs_rmdir(path)            rmdir(path)
#define vfs_unlink(path)           unlink(path)
#define vfs_chmod(path, ...)      chmod(path, __VA_ARGS__)
#define vfs_fchmod(...)           fchmod(__VA_ARGS__)

#define vfs_rename(a, b)           rename(a, b)

#define vfs_STAT                   stat
#define vfs_dirent                 struct dirent
#define vfs_DIR                    DIR
#define vfs_fpos_t                 w_int
*/

#define VFS_S_IFMT                 S_IFMT
#define VFS_S_IFDIR                S_IFDIR
#define VFS_S_IFREG                S_IFREG

#define VFS_S_IRWXU                S_IRWXU
#define VFS_S_IRUSR                S_IRUSR
#define VFS_S_IWUSR                S_IWUSR
#define VFS_S_IXUSR                S_IXUSR

#define VFS_S_IRWXG                S_IRWXG
#define VFS_S_IRGRP                S_IRGRP
#define VFS_S_IWGRP                S_IWGRP
#define VFS_S_IXGRP                S_IXGRP

#define VFS_S_IRWXO                S_IRWXO
#define VFS_S_IROTH                S_IROTH
#define VFS_S_IWOTH                S_IWOTH
#define VFS_S_IXOTH                S_IXOTH

#define VFS_S_ISREG(m)             S_ISREG(m)
#define VFS_S_ISDIR(m)             S_ISDIR(m)

#define VFS_ERRNO_ENOENT          ENOENT
#define VFS_ERRNO_EBADF           EBADF
#define VFS_ERRNO_EINVAL          EINVAL

void startVFS(void);

#endif
