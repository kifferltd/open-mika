/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/



#ifndef VFS_FCNTL_H
#define VFS_FCNTL_H


#include <fcntl.h>

#define VFS_MOUNT_RO  	 0x00
#define VFS_O_ACCMODE    O_ACCMODE  
#define VFS_O_RDONLY     O_RDONLY 
#define VFS_O_WRONLY     O_WRONLY  
#define VFS_O_RDWR       O_RDWR    
#define VFS_O_CREAT      O_CREAT 
#define VFS_O_EXCL       O_EXCL    
#define VFS_O_NOCTTY     O_NOCTTY    
#define VFS_O_TRUNC      O_TRUNC  
#define VFS_O_APPEND     O_APPEND 
#define VFS_O_NONBLOCK   O_NONBLOCK 
#define VFS_O_NDELAY     O_NONBLOCK
#define VFS_O_SYNC       O_SYNC  
#define VFS_FASYNC       FASYNC

//IT seems O_DIRECT doesn't exist in all linuxes ...
#ifdef O_DIRECT
#define VFS_O_DIRECT     O_DIRECT
#else
#define VFS_O_DIRECT	 0x00
#endif
#define VFS_O_LARGEFILE  O_LARGEFILE 
#define VFS_O_DIRECTORY  O_DIRECTORY
#define VFS_O_NOFOLLOW   O_NOFOLLOW

#define VFS_O_ACCMODE2TEXT(f) ((f)&VFS_O_ACCMODE)==VFS_O_RDONLY?"RDONLY":((f)&VFS_O_ACCMODE)==VFS_O_WRONLY?"WRONLY":"RDWR"

#define VFS_S_IRWXU S_IRWXU
#define VFS_S_IRUSR S_IRUSR
#define VFS_S_IWUSR S_IWUSR
#define VFS_S_IXUSR S_IXUSR

#define VFS_S_IRWXG S_IRWXG
#define VFS_S_IRGRP S_IRGRP
#define VFS_S_IWGRP S_IWGRP
#define VFS_S_IXGRP S_IXGRP

#define VFS_S_IRWXO S_IRWXO
#define VFS_S_IROTH S_IROTH
#define VFS_S_IWOTH S_IWOTH
#define VFS_S_IXOTH S_IXOTH

#define VFS_S_IFMT  S_IFMT
#define VFS_S_IFREG S_IFREG
#define VFS_S_IFDIR S_IFDIR

#endif
