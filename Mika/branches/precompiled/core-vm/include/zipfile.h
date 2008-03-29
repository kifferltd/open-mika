/**************************************************************************
* Copyright (c) 2003 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _ZIPFILE_H
#define _ZIPFILE_H

#include "dates.h"

typedef struct z_ZipEntry *                    z_zipEntry;
typedef struct z_ZipFile *                     z_zipFile;

/*
** A zip file is built with the following structure:
**
** ---------------------------------------------------------------------------------------------------------------
** [local file header + file data + data_descriptor]    for file 1
** [local file header + file data + data_descriptor]    for file 2
**         ...                                              ...
** [local file header + file data + data_descriptor]    for file n
** [directory file header]                              for file 1
** [directory file header]                              for file 2
**         ...                                              ...
** [directory file header]                              for file n
** Concluding file information
** ---------------------------------------------------------------------------------------------------------------
**
** Most of the information that is in the local file header is repeated in the directory 
** file header; we compile all this information in the structure z_Entry since for Wonka, all
** zipfiles will be one block and not by definition scattered over different floppies (the
** reason for all this redundancy). In the comment, we sometimes refer to the PKWARE Application notes
** that can be found back at http://www.pkware.com. When a comment starts with 'LFH' it is located
** in the 'Local File Header' according to that document, and when it starts with 'DFH' it is to
** be found back in the 'Directory File Header'.
*/

typedef struct z_ZipEntry { 
  z_zipEntry next;                             /* ) Linked list of entries in a zipfile.                           */
  z_zipEntry previous;                         /* ) TODO: get rid of this and just use zf->ht.                     */
  z_zipFile  zipFile;
  w_flags flags;                              /* LFH: General purpose bit flags, see ZE_ flags above              */
  w_short compression;                        /* LFH: compression method used                                     */
  w_short nameLength;                         /* LFH: filename length                                             */
  w_ubyte *name;                              /* Name as null-terminated string */
  w_size   c_data_offset;                      /* file offset of the begin of the compressed data                    */
  w_ubyte *u_data;                             /* data after decompression */
  w_int offset;                               /* DFH: relative offset of local header                             */
  w_word d_crc;                               /* LFH: CRC 32 as found in the directory header                     */
  w_size c_size;                              /* LFH: compressed size                                             */
  w_size u_size;                              /* LFH: uncompressed size                                           */
#ifdef DEBUG
  w_ubyte x_major;                             /* LFH: Version needed to extract (major version number)            */
  w_ubyte x_minor;                             /* LFH: Version needed to extract (minor version number)            */
  w_ubyte c_major;                             /* DFH: version made by (major version number)                      */
  w_ubyte c_minor;                             /* DFH: version made by (minor version number)                      */
  w_short s_disk;                             /* DFH: disk number start                                           */
  w_date modified;                            /* LFH: Date and time of last modification                          */
  w_flags i_attr;                             /* DFH: internal file attributes                                    */
  w_word e_attr;                              /* DFH: external file attributes                                    */
  w_short extraLength;
  w_short commentLength;                      /* DFH: file comment length                                         */
  w_ubyte *extra;
  w_ubyte *comment;                            /* the comment for the file                                         */
#endif
} z_ZipEntry;

typedef struct z_ZipFile {
  int fd;                         /* file descriptor */
  z_zipEntry entries;             /* linked list of entries */
  w_hashtable ht;                 /* hashtable mapping path (w_string) to entry */
#ifdef DEBUG
  w_short c_disk;                 /* number of this disk                                              */
  w_short d_disk;                 /* number of disk with the central directory                        */
  w_short numDisk;                /* total number of entries on this disk                             */
  w_short numDir;                 /* total number of entries in the central directory                 */
  w_int size;                     /* size of the central directory                                    */
  w_int offset;                   /* offset of start of central directory w.r.t. starting disk number */
  w_short commentLength;          /* zipfile comment length                                           */
  w_ubyte *comment;
#endif
} z_ZipFile;

/*
** Parse the zipfile indicated by `path'. The file is opened and remains open
** until releaseZipFile() is called.
*/
z_zipFile parseZipFile(char *path);

/*
** Get a pointer to one zip entry (file or directory) within a zipfile which
** has been parsed using parseZipFile().
*/
z_zipEntry findZipEntry(z_zipFile dir, w_byte *name);

/*
** Uncompress (or un-store) the data associated with a zip entry (which must be
** a file, not a directory). The uncompressed data is stored in zipEntry->u_data,
** and remains available until deleteZipEntry() or releaseZipFile() is called.
** Returns WONKA_TRUE if successful, WONKA_FALSE otherwise.
*/
w_boolean uncompressZipEntry(z_zipEntry zipEntry);

/*
** Delete one entry from a zipfile.  (Only the data in memory is deleted, the
** file itself is not affected).
*/
void deleteZipEntry(z_zipEntry zipEntry);

/*
** Close the zipfile and release all memory allocated to its data structures.
** None of the calls listed above may be made after releaseZipFile() has been
** called.
*/
void releaseZipFile(z_zipFile zipFile);

/*
 * Inflate the data block pointed to by c_data with size c_size and claimed 
 * uncompressed size u_size. Check that the CRC matches that given in d_crc.
 * If OK, return uncompressed data block (caller is responsible for releasing
 * memory when no longer needed. If NOK, return NULL and set errmsg to point to
 * an ASCII error description.
 */
w_int quickInflate(w_ubyte* c_data, w_size c_size, w_ubyte* u_data, w_size u_size, w_word d_crc, char **errmsg, w_int clonedata);

#endif /* _ZIPFILE_H */

