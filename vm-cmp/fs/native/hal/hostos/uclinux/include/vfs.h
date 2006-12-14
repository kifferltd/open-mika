/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (C) 2003, 2004 by /k/ Chris Gray, /k/ Embedded  *
* Java Solutions.  All rights reserved.                                   *

**************************************************************************/

#ifndef VFS_INCLUDE
#define VFS_INCLUDE

/* Set _FILE_OFFSET_BITS to 32 to make these functions work with glibc2.2 */

#define _FILE_OFFSET_BITS 32

#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <vfs_fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <utime.h>

#include "ts-mem.h"

#ifdef OSWALD
#define FS_NON_BLOCKING
#include <oswald.h>
#endif

extern char *current_working_dir;
extern char *current_root_dir;
extern char *fsroot;

/* Macros to map the vfs_* functions to their normal C equivalents */

#define close_vfs()                woempa(9, "close_vfs -> Using native filesystem\n")
#define vfs_mount(a...)            woempa(9, "vfs_mount -> Using native filesystem\n");

extern char *command_line_path;

static char *cwdbuffer;

#define MAX_CWD_SIZE 1024

static inline void init_vfs(void) {
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

#ifndef FS_NON_BLOCKING

/*
** However, there's a problem with the following functions when using Oswald. Since Oswald is multithreading in a single 
** process, a function that blocks the process blocks every single thread. Therefore it's needed to make all calls 
** non-blocking.
*/

#define vfs_open(path, a...)       open(path, ##a)
#define vfs_creat(path, a...)      creat(path, ##a)
#define vfs_read(a...)             read(##a)
#define vfs_write(a...)            write(##a)

#define vfs_fopen(path, a...)      fopen(path, ##a)
#define vfs_fdopen(a...)           fdopen(##a)
#define vfs_fclose(a...)           fclose(##a)

#define vfs_fseek(a...)            fseek(##a)
#define vfs_ftell(a...)            ftell(##a)
#define vfs_rewind(a...)           rewind(##a)
#define vfs_fgetpos(a...)          fgetpos(##a)
#define vfs_fsetpos(a...)          fsetpos(##a)

#define vfs_fread(a...)            fread(##a)
#define vfs_fwrite(a...)           fwrite(##a)

#define vfs_fopen(path, a...)      fopen(path, ##a)
#define vfs_fdopen(a...)           fdopen(##a)
#define vfs_fclose(a...)           fclose(##a)

#define vfs_feof(a...)             feof(##a)
#define vfs_ferror(a...)           ferror(##a)
#define vfs_clearerr(a...)         clearerr(##a)

#define vfs_fgetc(a...)            fgetc(##a)
#define vfs_getc(a...)             getc(##a)
#define vfs_fgets(a...)            fgets(##a)
#define vfs_fputc(a...)            fputc(##a)
#define vfs_fputs(a...)            fputs(##a)
#define vfs_putc(a...)             putc(##a)

#define vfs_fflush(a...)           fflush(##a)

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
#define vfs_telldir(a...)          telldir(##a)
#define vfs_seekdir(a...)          seekdir(##a)
#define vfs_alphasort(a...)        alphasort(##a)

#define vfs_fstat(a...)            fstat(##a)
#define vfs_stat(path, a...)       stat(path, ##a)

#define vfs_mkdir(path, a...)      mkdir(path, ##a)
#define vfs_rmdir(path)            rmdir(path)
#define vfs_unlink(path)           unlink(path)
#define vfs_chmod(path, a...)      chmod(path, ##a)
#define vfs_fchmod(a...)           fchmod(##a)

#define vfs_rename(a, b)           rename(a, b)

#define vfs_STAT                   stat
#define vfs_dirent                 struct dirent
#define vfs_DIR                    DIR
#define vfs_fpos_t                 w_int

#ifdef FS_NON_BLOCKING

/*
** These transform the original function calls into non-blocking calls. This does not mean
** that these functions don't block, they do actually, but they no longer block the 
** running process.
*/

static inline int vfs_open(const w_ubyte *filename, const w_int flags, const w_word mode) {
  return open(filename, flags | O_NONBLOCK, mode);
}

static inline int vfs_creat(const w_ubyte *pathname, w_word mode) {
  /* The normal creat function has no NONBLOCK option, so we need to reroute the call to open */
  return open(pathname, O_CREAT|O_WRONLY|O_TRUNC|O_NONBLOCK, mode);
}

static inline int vfs_read(const int file_desc, w_void *buffer, const w_word count) {
  int retval = read(file_desc, buffer, count);
  while (retval == -1 && (errno == EAGAIN || errno == EINTR)){
    x_thread_sleep(50);
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
  w_ubyte         *mode;                /* File mode, e.g. r+ */
  w_word          error;                /* Error flags */
} vfs_FILE;

static inline vfs_FILE *vfs_fopen(const w_ubyte *path, const w_ubyte *mode) {
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

  if(file_desc != -1) {                                 /* Valid filedes ? */
    stream = allocMem(sizeof(vfs_FILE));         /* Allocate memory */
    if (stream) {
      stream->file_desc = file_desc;                    /* Store the file descriptor */
      stream->error = 0;
      stream->mode = allocMem((w_size)strlen(mode)+1); /* Allocate memory to store the mode */
      if (stream->mode) {
        strcpy(stream->mode, mode);                     /* Copy the mode to the stream */
      }
      else {
        wabort(ABORT_WONKA, "No memory to create vfs_FILE->mode\n");
      }

    }
    else {
      wabort(ABORT_WONKA, "No memory to create vfs_FILE structure\n");
    }
  }

  return stream;
}

static inline vfs_FILE *vfs_fdopen(int file_desc, const w_ubyte *mode) {
  vfs_FILE   *stream = allocMem(sizeof(vfs_FILE));         /* Allocate memory */
  if (stream) {
    stream->file_desc = file_desc;                    /* Store the file descriptor */
    stream->error = 0;
    stream->mode = allocMem((w_size)strlen(mode)+1); /* Allocate memory to store the mode */
    if (stream->mode) {
      strcpy(stream->mode, mode);                     /* Copy the mode to the stream */
    } else {
      releaseMem(stream);
      return NULL;
    }
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
  woempa(9, "(VFS) FERROR !!!\n");
  return 0;
}

static inline w_void vfs_clearerr(vfs_FILE *stream) {
  stream->error = 0;
  return;
}

static inline w_word vfs_fgetc(vfs_FILE *stream) {
  w_word result = 0;
  return (vfs_fread((w_void *)&result, 1, 1, stream) == 0 ? (w_word)-1 : result);
}

static inline w_word vfs_getc(vfs_FILE *stream) {
  w_ubyte result;
  vfs_fread((w_void *)&result, 1, 1, stream);
  return (w_word)result;
}

static inline w_ubyte *vfs_fgets(w_ubyte *s, w_int size, vfs_FILE *stream) {
  w_int    count = 0;
  w_ubyte  *ptr;
  w_ubyte  c = 0;

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

static inline w_word vfs_fputs(const w_ubyte *s, vfs_FILE *stream) {
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
