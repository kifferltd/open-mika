/**************************************************************************
* Copyright (c) 2022 by KIFFER Ltd. All rights reserved.                  *
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

#ifndef _FILE_DESCRIPTOR_H
#define _FILE_DESCRIPTOR_H

#include "wonka.h"

typedef struct vfs_FD_Entry *vfs_fd_entry;

typedef struct vfs_FileOperations *vfs_fileops;

typedef struct vfs_FileOperations {
  /**
   * we'll find a use for this later
   */
  void *dummy;

  w_int (*open) (vfs_fd_entry, const char *, w_word, w_word);

  /**
   * Obtain the total length of a file-like object.
   * Returns -1 for stream-like objects.
   */
  size_t (*get_length) (vfs_fd_entry);

  /**
   * Returns true if the object is in an EOF state, false otherwise.
   */
  w_boolean (*is_eof) (vfs_fd_entry);

  /**
   * 'tell' operation returns current position if such a thing exists
   */
  w_int (*tell) (vfs_fd_entry);

  /**
   * universal 'seek' operation
   */
  w_int (*seek) (vfs_fd_entry, w_int, w_int);

  /**
   * universal 'read' operation
   */
  w_int (*read) (vfs_fd_entry, char *, w_size, w_int *);

  /**
   * universal 'write' operation
   */
  w_int (*write) (vfs_fd_entry, const char *, w_size, w_int *);

  /**
   * universal 'close' operation
   */
  w_int (*close) (vfs_fd_entry);

  /**
   * file-like objects only: directory searc
   */
  //int (*readdir) (vfs_fd_entry, void *, filldir_t);

  /**
   * stream sockets only: accept bind connect listen
   */
} vfs_FileOperations;

typedef struct vfs_MountPoint *vfs_mountpoint;

typedef struct vfs_MountPoint {
  /**
   * Path to the MountPoint
   */
  const char *prefix;

  /**
   * Pointer to the vfs_FileOperations table of the mounted filesystem
   */
  vfs_fileops fileops;

  /**
   * Next entry in the linked list of MointPoints
   */
  vfs_mountpoint next;

  /**
   * Next entry in the linked list of MointPoints
   */
  vfs_mountpoint previous;
} vfs_MountPoint;

/**
 * Structure of a File Descriptor table entry.
 */
typedef struct vfs_FD_Entry {
  /**
   * Path to the file, as a UTF-8 C string.
   * Note: FreeRTOS+FAT not yet tested with non-ASCII characters.
   */
  char *path;

  /**
   * File flags
   * ms byte: generic static flags
   * next byte: fs-specific static flags
   * next byte: fs-specific dynamic flags
   * ls byte: generic dynamic flags
   */
  w_word flags; // O_RDONLY, O_WRONLY, or O_RDWR

  /**
   * Pointer to function table
   */
   vfs_FileOperations *ops;

  /**
   * Pointer to fs-specific data
   */
  void *data;
} vfs_FD_Entry;

extern vfs_fd_entry vfs_fd_table[];

void registerMountPoint(vfs_mountpoint mp);
void deregisterMountPoint(vfs_mountpoint mp);

#endif /* _FILE_DESCRIPTOR_H */
