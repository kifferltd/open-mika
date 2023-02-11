/**************************************************************************
* Copyright (c) 2020, 2021, 2022 by KIFFER Ltd. All rights reserved.      *
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

#ifdef FS_NON_BLOCKING
ERROR - non-blocking file access is not supported on FreeRTOS
#endif

#define _FILE_OFFSET_BITS 32

#include <unistd.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <ff_stdio.h>
#include <utime.h>
 
#include "vfs_fcntl.h"
#include "ts-mem.h"

#define MAX_CWD_SIZE 1024
#define FLASH_CACHE_SIZE (256*1024)
#define MAX_FILE_DESCRIPTORS 256

typedef struct vfs_fd_entry {
  FF_FILE *ff_fileptr;
  w_word flags; // O_RDONLY, O_WRONLY, or O_RDWR
} vfs_fd_entry;

extern vfs_fd_entry vfs_fd_table[];

extern char *current_working_dir;
extern char *current_root_dir;
extern char *fsroot;

#define close_vfs()                woempa(9, "close_vfs -> Using native filesystem\n")
#define vfs_mount(...)            woempa(9, "vfs_mount -> Using native filesystem\n", __VA_ARGS__);

extern char *command_line_path;

static char *cwdbuffer;

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
//#define vfs_fclose(...)           fclose(__VA_ARGS__)

//#define vfs_fseek(...)            fseek(__VA_ARGS__)
//#define vfs_ftell(...)            ftell(__VA_ARGS__)
#define vfs_rewind(...)           rewind(__VA_ARGS__)
#define vfs_fgetpos(...)          fgetpos(__VA_ARGS__)
#define vfs_fsetpos(...)          fsetpos(__VA_ARGS__)

//#define vfs_fread(...)            fread(__VA_ARGS__)
//#define vfs_fwrite(...)           fwrite(__VA_ARGS__)

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


// old non-blocking code
#ifdef HIPPOPOTAMUS
/*
** These are the bastards. There's no way to make these calls nonblocking. So they are mapped to the normal 
** file calls.
*/

#define VFS_STREAM_EOF     0x01         /* End of file flag */
#define VFS_STREAM_ERROR   0x02         /* Error flag */

typedef struct vfs_FILE {
  w_int           file_desc;            /* file descriptor of this stream */
  char           *mode;                /* File mode, e.g. r+ */
  w_word          error;                /* Error flags */
} vfs_FILE;

static inline vfs_FILE *vfs_fdopen(w_int fildes, const char *mode) {
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

/*
inline static w_int vfs_lseek(vfs_FILE *stream, long offset, w_int whence) {
  return lseek(stream->file_desc, offset, whence);
}

inline static w_word vfs_ftell(vfs_FILE *stream) {
  w_word result = lseek(stream->file_desc, 0, SEEK_CUR);
  return result;
}
*/

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

#endif
// end old code

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
