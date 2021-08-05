/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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

/* Set _FILE_OFFSET_BITS to 32 to make these functions work with glibc2.2 */

#define _FILE_OFFSET_BITS 32

#include <sys/stat.h>
#include <unistd.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
//#include <dirent.h>
#include <errno.h>
#include <stdio.h>
#include <utime.h>
 
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

#define close_vfs()                woempa(9, "close_vfs -> Using native filesystem\n")
#define vfs_mount(...)            woempa(9, "vfs_mount -> Using native filesystem\n", __VA_ARGS__);

extern char *command_line_path;

static char *cwdbuffer;

#define MAX_CWD_SIZE 1024

void init_vfs(void);

#ifndef FS_NON_BLOCKING

/*
** However, there's a problem with the following functions when using Oswald. Since Oswald is multithreading in a single 
** process, a function that blocks the process blocks every single thread. Therefore it's needed to make all calls 
** non-blocking.
*/

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

#endif /* !FS_NON_BLOCKING */

/*
** The following functions have no blocking issues
*/

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

// HACK HACK HACK

           struct dirent {
               ino_t          d_ino;       /* Inode number */
               off_t          d_off;       /* Not an offset; see below */
               unsigned short d_reclen;    /* Length of this record */
               unsigned char  d_type;      /* Type of file; not supported
                                              by all filesystem types */
               char           d_name[256]; /* Null-terminated filename */
           };

struct DIR {
    struct dirent ent;
    struct _WDIR *wdirp;
};
typedef struct DIR DIR;

#define vfs_dirent                 struct dirent
#define vfs_DIR                    DIR
#define vfs_fpos_t                 w_int

#ifdef FS_NON_BLOCKING

/*
** These transform the original function calls into non-blocking calls. This does not mean
** that these functions don't block, they do actually, but they no longer block the 
** running process.
*/

static inline int vfs_open(const char *filename, const w_int flags, const w_word mode) {
  return open(filename, flags | O_NONBLOCK, mode);
}

static inline int vfs_creat(const char *pathname, w_word mode) {
  /* The normal creat function has no NONBLOCK option, so we need to reroute the call to open */
  return open(pathname, O_CREAT|O_WRONLY|O_TRUNC|O_NONBLOCK, mode);
}

static inline int vfs_read(const int file_desc, w_void *buffer, const w_word count) {
  int retval = read(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    // [CG 20050515] Surely this isn't needed?
    // x_thread_sleep(50);
    retval = read(file_desc, buffer, count);
  }
  return retval;  
}

static inline int vfs_write(const int file_desc, const w_void *buffer, const w_word count) {
  int retval = write(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_thread_sleep(50);
    retval = write(file_desc, buffer, count);
  }
  return retval;  
}

/*
** These are the bastards. There's no way to make these calls nonblocking. So they are mapped to the normal 
** file calls.
*/

#define VFS_STREAM_EOF     0x01         /* End of file flag */
#define VFS_STREAM_ERROR   0x02         /* Error flag */

typedef struct vfs_FILE {
  int             file_desc;            /* file descriptor of this stream */
  char           *mode;                /* File mode, e.g. r+ */
  w_word          error;                /* Error flags */
} vfs_FILE;

static inline vfs_FILE *vfs_fdopen(int fildes, const char *mode) {
  vfs_FILE   *stream = allocMem(sizeof(vfs_FILE)); 

  if (stream) {
    stream->file_desc = fildes;
    stream->error = 0;
    stream->mode = allocMem((w_size)strlen(mode)+1);
    if (stream->mode) {
      strcpy(stream->mode, mode);
    }
    else {
      wabort(ABORT_WONKA, "No memory to create vfs_FILE->mode\n");
    }
  }
  else {
    wabort(ABORT_WONKA, "No memory to create vfs_FILE structure\n");
  }

  return stream;
}

static vfs_FILE *vfs_fopen(const char *path, const char *mode) {
  w_int      flags = 0;
  w_int      file_desc;
  vfs_FILE   *stream = NULL; 

  woempa(9, "path: %s, mode: %s\n", path, mode);
  
  if(strcmp(mode, "r") == 0) flags = VFS_O_RDONLY;
  else if(strcmp(mode, "r+") == 0) flags = VFS_O_CREAT | VFS_O_RDWR;
  else if(strcmp(mode, "w") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_WRONLY;
  else if(strcmp(mode, "w+") == 0) flags = VFS_O_CREAT | VFS_O_TRUNC | VFS_O_RDWR;
  else if(strcmp(mode, "a") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_WRONLY;
  else if(strcmp(mode, "a+") == 0) flags = VFS_O_CREAT | VFS_O_APPEND | VFS_O_RDWR;

  file_desc = open(path, flags|O_NONBLOCK, S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH|S_IWOTH);

  if(file_desc != -1) {
    stream = vfs_fdopen(file_desc, mode);
  }

  return stream;
}

static inline w_word vfs_fclose(vfs_FILE *stream) {
  vfs_close(stream->file_desc);
  releaseMem(stream->mode);
  releaseMem(stream);
  return 0;
}

inline static int vfs_fseek(vfs_FILE *stream, long offset, w_int whence) {
  return lseek(stream->file_desc, offset, whence);
}

inline static w_word vfs_ftell(vfs_FILE *stream) {
  w_word result = lseek(stream->file_desc, 0, SEEK_CUR);
  return result;
}

inline static w_void vfs_rewind(vfs_FILE *stream) {
  lseek(stream->file_desc, 0, SEEK_SET);
}

inline static w_int vfs_fgetpos(vfs_FILE *stream, vfs_fpos_t *pos) {
  *pos = vfs_ftell(stream);
  if(*pos != -1) return 0;
  return -1;
}

inline static w_word vfs_fsetpos(vfs_FILE *stream, vfs_fpos_t *pos) {
  return lseek(stream->file_desc, *pos, SEEK_SET);
}

static inline w_word vfs_fread(w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream) {
  w_word result = vfs_read(stream->file_desc, ptr, size * nmemb) / size;
  if((result == 0) && ((size * nmemb) != 0)) {
    stream->error |= VFS_STREAM_EOF;
  }
  return result;
}

static inline w_word vfs_fwrite(const w_void *ptr, w_word size, w_word nmemb, vfs_FILE *stream) {
  return vfs_write(stream->file_desc, ptr, size * nmemb) / size;
}

static inline w_word vfs_feof(vfs_FILE *stream) {
  return ((stream->error & VFS_STREAM_EOF) == VFS_STREAM_EOF);
}

static inline w_word vfs_ferror(vfs_FILE *stream) {
  return 0;
}

static inline w_void vfs_clearerr(vfs_FILE *stream) {
  stream->error = 0;
  return;
}

static inline w_int vfs_fgetc(vfs_FILE *stream) {
  unsigned char result = 0;
  return (vfs_fread(&result, 1, 1, stream) == 0 ? -1 : result);
}

#define vfs_getc(s) vfs_fgetc(s)

static inline char *vfs_fgets(char *s, w_int size, vfs_FILE *stream) {
  w_int count = 0;
  char *ptr;
  char  c = 0;

  ptr = s;

  while((count < size) && (!vfs_feof(stream)) && (c != '\n') && (c != '\0')) {
    vfs_fread(ptr, 1, 1, stream);
    c = *ptr;
    count++;
    ptr++;
  }

  ptr--;
  ptr[0] = '\0';

  if(count != 0) return s; else return NULL;
}

static inline w_word vfs_fputc(w_int c, vfs_FILE *stream) {
  w_ubyte result = c;
  vfs_write(stream->file_desc, (w_ubyte *)&result, 1);
  return (w_word)result; 
}

static inline w_word vfs_fputs(const char *s, vfs_FILE *stream) {
  return vfs_write(stream->file_desc, s, strlen(s));
}

static inline w_word vfs_putc(w_int c, vfs_FILE *stream) {
  return vfs_fputc(c, stream);
}

static inline w_word vfs_fflush(vfs_FILE *stream) {
  return 0;
}

#endif /* FS_NON_BLOCKING */

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

void startVFS(void);

#endif
