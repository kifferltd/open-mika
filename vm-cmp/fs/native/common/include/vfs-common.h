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

#ifndef HAVE_VFS_COMMON_H
#define HAVE_VFS_COMMON_H

#define MAX_FILE_DESCRIPTORS 256
#define MAX_CWD_SIZE 1024

void init_vfs(void);
void startVFS(void);

w_int vfs_open(const char *pathname, w_word flags, w_word mode);
w_int vfs_read(w_int fd, void *buf, w_size count);
w_int vfs_write(w_int fd, void *buf, w_size count);
w_int vfs_lseek(w_int fd, w_int offset, w_int whence);
w_int vfs_flush(w_int fd);
w_int vfs_close(w_int fd);

w_int  placeholder_open  (vfs_fd_entry fde, const char *path, w_word flags, w_word mode);
size_t placeholder_get_length(vfs_fd_entry fde);
w_boolean placeholder_is_eof(vfs_fd_entry fde);
w_int  placeholder_seek  (vfs_fd_entry fde, w_int offset, w_int whence);
w_int  placeholder_read  (vfs_fd_entry fde, char *buffer, w_size length, w_int *pos);
w_int  placeholder_write (vfs_fd_entry fde, const char *buffer, w_size length, w_int *pos);
w_int placeholder_flush(vfs_fd_entry fde);
w_int placeholder_close(vfs_fd_entry fde);

#define whence2text(n) ((n)==SEEK_SET?"SEEK_SET":(n)==SEEK_CUR:"SEEK_CUR":(n)==SEEK_END?"SEEK_END":"SEEK_???")

#endif // HAVE_VFS_COMMON_H
