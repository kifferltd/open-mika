/**************************************************************************
* Copyright (c) 2023 by KIFFER Ltd. All rights reserved.                  *
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

#ifndef VFS_FCNTL_H
#define VFS_FCNTL_H

#define VFS_MOUNT_RO  	    0x00
#define VFS_O_ACCMODE       0003  
#define VFS_O_RDONLY          00 
#define VFS_O_WRONLY          01  
#define VFS_O_RDWR            02    
#define VFS_O_CREAT         0100 
#define VFS_O_EXCL          0200    
#define VFS_O_NOCTTY        0400    
#define VFS_O_TRUNC        01000  
#define VFS_O_APPEND       02000 
#define VFS_O_NONBLOCK     04000 
#define VFS_O_NDELAY       04000
#define VFS_O_SYNC       04010000  
#define VFS_FASYNC         020000
#define VFS_O_DIRECT       040000

#define VFS_O_ACCMODE2TEXT(f) ((f)&VFS_O_ACCMODE)==VFS_O_RDONLY?"RDONLY":((f)&VFS_O_ACCMODE)==VFS_O_WRONLY?"WRONLY":"RDWR"

#define VFS_S_IRWXU 00700
#define VFS_S_IRUSR 00400
#define VFS_S_IWUSR 00200
#define VFS_S_IXUSR 00100

#define VFS_S_IRWXG 00070
#define VFS_S_IRGRP 00040
#define VFS_S_IWGRP 00020
#define VFS_S_IXGRP 00010

#define VFS_S_IRWXO 00007
#define VFS_S_IROTH 00004
#define VFS_S_IWOTH 00002
#define VFS_S_IXOTH 00001

#define VFS_S_IFMT  00170000
#define VFS_S_IFREG  0100000
#define VFS_S_IFDIR  0040000

// TODO - do we need these?
/*
#define VFS_S_IFBLK  0060000
#define VFS_S_IFSOCK 0140000
#define VFS_S_IFLNK  0120000
#define VFS_S_IFCHR  0020000
#define VFS_S_IFIFO  0010000
#define VFS_S_ISUID  0004000
#define VFS_S_ISGID  0002000
#define VFS_S_ISVTX  0001000
*/

#define VFS_S_ISREG(m)  (((m) & VFS_S_IFMT) == VFS_S_IFREG)
#define VFS_S_ISDIR(m)  (((m) & VFS_S_IFMT) == VFS_S_IFDIR)
// TODO - do we need VFS_S_ISLNK etc?


// TODO do we need these?
/*
#define VFS_O_LARGEFILE  O_LARGEFILE 
#define VFS_O_DIRECTORY  O_DIRECTORY
#define VFS_O_NOFOLLOW   O_NOFOLLOW
*/

#endif
