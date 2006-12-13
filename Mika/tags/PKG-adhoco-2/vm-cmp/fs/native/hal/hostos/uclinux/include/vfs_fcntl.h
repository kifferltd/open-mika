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
**************************************************************************/



#ifndef VFS_FCNTL_H
#define VFS_FCNTL_H

#include <fcntl.h>

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
#define VFS_O_LARGEFILE  O_LARGEFILE 
#define VFS_O_DIRECTORY  O_DIRECTORY
#define VFS_O_NOFOLLOW   O_NOFOLLOW

#ifndef O_DIRECT
#define VFS_O_DIRECT	 0x00
#else
#define VFS_O_DIRECT     O_DIRECT
#endif

#endif
