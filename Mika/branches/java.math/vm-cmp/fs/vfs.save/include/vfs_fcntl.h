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

/* open/fcntl - VFS_O_SYNC is only implemented on blocks devices and on files
   located on an ext2 file system */

 #define VFS_O_ACCMODE    0x0003
 #define VFS_O_RDONLY     0x04
 #define VFS_O_WRONLY     0x01
 #define VFS_O_RDWR       0x02
 #define VFS_O_CREAT      0x0100     /* not fcntl */
 #define VFS_O_EXCL       0x0200     /* not fcntl */
 #define VFS_O_NOCTTY     0x0400     /* not fcntl */
 #define VFS_O_TRUNC      0x001000   /* not fcntl */
 #define VFS_O_APPEND     0x002000
 #define VFS_O_NONBLOCK   0x004000
 #define VFS_O_NDELAY     VFS_O_NONBLOCK
 #define VFS_O_SYNC       0x010000
 #define VFS_FASYNC       0x020000   /* fcntl, for BSD compatibility */
 #define VFS_O_DIRECT     0x040000   /* direct disk access hint - currently ignored */
 #define VFS_O_LARGEFILE  0x00100000
 #define VFS_O_DIRECTORY  0x00200000 /* must be a directory */
 #define VFS_O_NOFOLLOW   0x00400000 /* don't follow links */

 #define F_DUPFD      0          /* dup */
 #define F_GETFD      1          /* get f_flags */
 #define F_SETFD      2          /* set f_flags */
 #define F_GETFL      3          /* more flags (cloexec) */
 #define F_SETFL      4
 #define F_GETLK      5
 #define F_SETLK      6
 #define F_SETLKW     7

 #define F_SETOWN     8          /*  for sockets. */
 #define F_GETOWN     9          /*  for sockets. */
 #define F_SETSIG     10         /*  for sockets. */
 #define F_GETSIG     11         /*  for sockets. */

/* for F_[GET|SET]FL */
 #define FD_CLOEXEC   1          /* actually anything with low bit set goes */

/* for posix fcntl() and lockf() */
 #define F_RDLCK      0
 #define F_WRLCK      1
 #define F_UNLCK      2

/* for old implementation of bsd flock () */
 #define F_EXLCK      4          /* or 3 */
 #define F_SHLCK      8          /* or 4 */

/* operations for bsd flock(), also used by the kernel implementation */
 #define LOCK_SH      1          /* shared lock */
 #define LOCK_EX      2          /* exclusive lock */
 #define LOCK_NB      4          /* or'd with one of the above to prevent blocking */
 #define LOCK_UN      8          /* remove lock */

#endif
