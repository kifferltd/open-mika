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

#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#include "new_deflate_internals.h"
#include "ts-mem.h"
//#include "vfs.h"
#include "wstrings.h"
#include "zipfile.h"

#define WSF_ZIS_STRUCTURED                     0x00010000

#define Z_LOCAL_HEADER                         0x04034b50
#define Z_DIR_HEADER                           0x02014b50
#define Z_DIR_END                              0x06054b50
#define Z_SENTINEL0                            0x50
#define Z_SENTINEL1                            0x4b
#define Z_DIR_BYTE0                            0x01
#define Z_DIR_BYTE1                            0x02

#define bytes2short(b)                         (w_short)(((b)[1] << 8) | (b)[0])
#define bytes2word(b)                          (w_word)(((b)[3] << 24) | ((b)[2] << 16) | ((b)[1] << 8) | (b)[0])

typedef w_ubyte *                              (*z_decoder)(z_zipEntry zipEntry);

#define ZE_COMPRESSED_IS_STATIC               0x00010000 /* When the z_entry->c_data is pointing to static memory */
#define ZE_UNCOMPRESSED                       0x00020000 /* The entry has been uncompressed already              */
#define ZE_ERROR                              0x00040000 /* Something is wrong with the data in this entry        */
#define ZE_NO_DECOMPRESSOR                    0x00080000 /* The decompressor is not yet implemented               */
#define ZE_NO_MEMORY                          0x00100000 /* Not enough memory was available for decompressing     */
#define ZE_ZERO_LENGTH                        0x00200000 /* The original file had 0 bytes in it!                  */

/*
** The different general purpose bit flags of PKWARE. Note that the flags used or reserved by
** PKWARE are 16 bits long. We use a complete 32 bit word to add our own flags to it. These flags
** start after the 16'th bit.
*/

#define ZE_ENCRYPTED                          0x00000001
#define ZE_BIT_1                              0x00000002
#define ZE_BIT_2                              0x00000004
#define ZE_DESCRIPTOR_AVAILABLE               0x00000008
#define ZE_BIT_4                              0x00000010
#define ZE_PATCHED_DATA                       0x00000020
#define ZE_BIT_6                              0x00000040
#define ZE_BIT_7                              0x00000080
#define ZE_BIT_8                              0x00000100
#define ZE_BIT_9                              0x00000200
#define ZE_BIT_10                             0x00000400
#define ZE_BIT_11                             0x00000800
#define ZE_BIT_12                             0x00001000
#define ZE_BIT_13                             0x00002000
#define ZE_BIT_14                             0x00004000
#define ZE_BIT_15                             0x00008000

/*
 * Time (in ticks) we wait for data sent to the inflater to be
 * accepted, or for a read to yield data.
 */
#define Z_INFLATER_WAIT x_millis2ticks(10000)

/*
** This table maps the z_zipEntry->compression with the appriopriate decoder or NULL
** if this decoder is not yet implemented. We first give the prototypes of the
** functions in the table. The entry 'notyet' signals that the appropriate decompressor
** routine is not yet implemented.
*/

static w_ubyte *notyet(z_zipEntry entry);
static w_ubyte *zip_inflate(z_zipEntry entry);
static w_ubyte *zip_destore(z_zipEntry entry);

static const z_decoder decoders[] = {
  zip_destore,        /*  0 - the file is stored without compression        */
  notyet,             /*  1 - the file is shrunk                            */
  notyet,             /*  2 - the file is reduced with compression factor 1 */
  notyet,             /*  3 - the file is reduced with compression factor 2 */
  notyet,             /*  4 - the file is reduced with compression factor 3 */
  notyet,             /*  5 - the file is reduced with compression factor 4 */
  notyet,             /*  6 - the file is imploded                          */
  notyet,             /*  7 - reserved for tokenizing compression           */
  zip_inflate,        /*  8 - the file is inflated                          */
  notyet,             /*  9 - reserved for enhanced deflating               */
  notyet,             /* 10 - PKWARE Date Compression Library imploding     */
};

/*
** Read an item from a stream of bytes. After reading, set the byte data pointer to the
** next position to parse.
*/

#define readWord(d)                     bytes2word(d); (d) += sizeof(w_word)
#define readShort(d)                    bytes2short(d); (d) += sizeof(w_short)
#define readByte(d)                     (*(d)++)

#ifdef DEBUG
static void dumpEntry(z_zipEntry entry) {

  w_int offset = (w_int)entry->c_data_offset;

  woempa(7, "            Version to extract: %d.%d\n", entry->x_major, entry->x_minor);
  woempa(7, "               Version made by: %d.%d\n", entry->c_major, entry->c_minor);
  woempa(7, "                         Flags: 0x%08x\n", entry->flags);
  woempa(7, "              Compression used: %d\n", entry->compression);
  woempa(7, "               Compressed size: %d\n", entry->c_size);
  woempa(7, "             Uncompressed size: %d\n", entry->u_size);
  woempa(7, "                    Namelength: %d\n", entry->nameLength);
  woempa(7, "                     File name: %s\n", (char *)entry->name);
  woempa(7, "            Extra field length: %d\n", entry->extraLength);
  woempa(7, "                Comment length: %d\n", entry->commentLength);
  if (entry->commentLength) {
    woempa(7, "                       Comment: '%s'\n", (char *)entry->comment);
  }
  woempa(7, "             Disk Number start: %d\n", entry->s_disk);
  woempa(7, "           Internal attributes: 0x%08x\n", entry->i_attr);
  woempa(7, "           External attributes: 0x%08x\n", entry->e_attr);
  woempa(7, "               Relative offset: %d\n", entry->offset);
  woempa(7, "                           CRC: 0x%08x\n", entry->d_crc);
  woempa(7, "     Compressed data at offset: %d\n", offset);

}


static void dumpDir(z_zipFile dir) {

  z_zipEntry entry;
  w_int i = 1;
  
  woempa(7, "============================================= ZIP DIR 0x%08x ======================\n", (w_word)dir);
  woempa(7, "           Current disk number: %d\n", dir->c_disk);
  woempa(7, "         Disk with central dir: %d\n", dir->d_disk);
  woempa(7, "Number of entries on this disk: %d\n", dir->numDisk);
  woempa(7, " Number of entries in this dir: %d\n", dir->numDir);
  woempa(7, "              Central dir size: %d\n", dir->size);
  woempa(7, "            Central dir offset: %d\n", dir->offset);
  woempa(7, "                Comment length: %d\n", dir->commentLength);
  if (dir->commentLength) {
    woempa(1, "                       Comment: '%s'\n", (char *)dir->comment);
  }

  entry = dir->entries;
  do {
//    woempa(7, "--------------------------------------------- Entry number %3d ------------------------\n", i);
//    dumpEntry(entry);
    entry = entry->next;
    i += 1;
  } while (entry != dir->entries);
  woempa(7, "=======================================================================================\n", (w_word)dir);

}
#endif

/**
 * ZIPENTRY_BUFSIZ defines the sise of the initial read() which is performed
 * for each entry in the zip directory. Mostly it will be more than adequate,
 * and we will only need to make a supplementary read if the entry contains a
 * long name, comment, or extra data. This does mean that often we will make
 * overlapping read()s for successive directory entries, so some further
 * optimisation is certainly possible here, especially if the underlying
 * filesystem is very simple. (OSes such as NetBSD or Linux probably handle
 * this pretty well, but an RTOS might not).
 *
 * We assume that the first read will read at least 42 bytes if it reads
 * anything at all (i.e. does not fail with EINTR or EAGAIN).
 */
#define ZIPENTRY_BUFSIZ 512

/**
 * Read one entry of the zip directory, and write the information in 'entry'.
 * *offsetptr contains the starting offset of the entry within the file on
 * entry, and the starting offset of the next entry on exit. The filedescriptor
 * of the zip file is already present in 'entry' when this function is called.
 */
static void readZipEntry(w_boolean local, z_zipEntry entry, w_size *offsetptr) {

  w_word timestamp;
  w_size offs = *offsetptr;
  w_byte *start = allocMem(ZIPENTRY_BUFSIZ);
  w_byte *data = start;
  w_byte o_data[4];
  w_byte *other_data = o_data;
  w_word signature;
  w_int nameLength;
  w_int extraLength;
  w_int commentLength;
  w_size l;
  w_int rc;

  if (!start) {
    wabort(ABORT_WONKA, "Unable to allocate %d bytes for buffer\n", ZIPENTRY_BUFSIZ);
  }
  woempa(1, "Reading %slocal entry at offset %d\n", local ? "" : "non-", offs);
  rc = vfs_lseek(entry->zipFile->fd, offs, SEEK_SET);
  if (rc != offs) {
    wabort(ABORT_WONKA, "Failed to seek to offset %d (rc is %d), can't handle that", offs, rc);
  }
  rc = vfs_read(entry->zipFile->fd, data, ZIPENTRY_BUFSIZ);
#ifndef OSWALD
  while (rc == -1 && (errno == EAGAIN || errno == EINTR)) {
    w_printf("readZipEntry(): read() interrupted, retrying\n");
    rc = vfs_read(entry->zipFile->fd, data, ZIPENTRY_BUFSIZ);
  }
#endif
  if (rc < 22 + 2 * (!local)) {
    wabort(ABORT_WONKA, "Tried to read %d bytes starting at offset %d and got %d bytes, can't handle that", 22 + 2 * (!local), offs, rc);
  }
#ifdef DEBUG
  if (! local) {
    entry->c_major = readByte(data);
    entry->c_minor = readByte(data);
  }
  entry->x_major = readByte(data);
  entry->x_minor = readByte(data);
#else
  // trash c_major, c_minor, x_minor, x_major
  if (! local) {
    data += 2;
  }
  data += 2;
#endif
  entry->flags = readShort(data);
  entry->compression = readShort(data);
  timestamp = readWord(data);
  woempa(1, "x_major = %d, x_minor = %d, flags = 0x%04x, compression = %d, timestamp = %d\n", entry->x_major, entry->x_minor, entry->flags, entry->compression, timestamp);

  if (! local) {
    entry->d_crc = readWord(data);
    entry->c_size = (w_int)readWord(data);
    entry->u_size = (w_int)readWord(data);
    woempa(1, "crc = 0x%08x, c_size = %d, u_size = %d\n", entry->d_crc, entry->c_size, entry->u_size);
  }
  else {
    /* 
    ** Throw the information away, it could be 0 anyway...
    */
    signature = readWord(data);
    signature = readWord(data);
    signature = readWord(data);
  }
  // Bytes consumed so far == 22 + 2 * (!local)

  if (! local) {
    if (rc < 44) {
      wabort(ABORT_WONKA, "Read less than 44 bytes, can't handle that");
    }
    nameLength = readShort(data);
    extraLength = readShort(data);
    commentLength = readShort(data);
    entry->nameLength = nameLength;
#ifdef DEBUG
    entry->extraLength = extraLength;
    entry->commentLength = commentLength;
    woempa(1, "nameLength = %d, extraLength = %d, commentLength = %d\n", entry->nameLength, entry->extraLength, entry->commentLength);
    entry->s_disk = (w_short)readShort(data);
    entry->i_attr = (w_flags)readShort(data);
    entry->e_attr = readWord(data);
    woempa(1, "s_disk = %d, i_attr = %d, e_attr = %d\n", entry->s_disk, entry->i_attr, entry->e_attr);
#else
    data += 8;
#endif
    entry->offset = readWord(data);
    woempa(1, "offset = %d\n", entry->offset);
    // Bytes consumed so far == 42
 
    if (42 + nameLength + extraLength + commentLength > ZIPENTRY_BUFSIZ) {
      w_ubyte* readptr;

      // [CG 20050513] So far as I know this branch has never been tested ...
      start = reallocMem(start, 42 + nameLength + extraLength + commentLength);
      data = start + 42;
      readptr = data;
      l =  nameLength + extraLength + commentLength - ZIPENTRY_BUFSIZ;
      w_printf("Need to read an extra %d bytes\n", l);
      while (l > 0) {
        rc = vfs_read(entry->zipFile->fd, readptr,  l);
#ifndef OSWALD
        while (rc == -1 && (errno == EAGAIN || errno == EINTR)) {
          rc = vfs_read(entry->zipFile->fd, readptr,  l);
        }
#endif
        if (rc < 0) {
          wabort(ABORT_WONKA, "Read less than %d bytes, can't handle that", 42 + nameLength + extraLength + commentLength);
        }
        readptr += rc;
        l -= rc;
        w_printf("Read another %d bytes, total = %d\n", rc, readptr - start);
      }
    }

    if (nameLength) {
      entry->name = (w_byte *)allocMem((entry->nameLength + 1) * sizeof(w_byte));
      if (!entry->name) {
        wabort(ABORT_WONKA, "Unable to allocate space for name\n");
      }
      w_memcpy(entry->name, data, (w_size)entry->nameLength);
      entry->name[entry->nameLength] = 0x00;
      woempa(1, "name = %s\n", entry->name);
      data += entry->nameLength;
    }
    if (extraLength) {
#ifdef DEBUG
      entry->extra = (w_byte *)allocMem((extraLength + 1) * sizeof(w_byte));
      if (!entry->extra) {
        wabort(ABORT_WONKA, "Unable to allocate space for extra\n");
      }
      w_memcpy(entry->extra, data, (w_size)extraLength);
      entry->extra[extraLength] = 0x00;
      woempa(1, "extra = %s\n", entry->extra);
#endif
      data += extraLength;
    }
    if (commentLength) {
#ifdef DEBUG
      entry->comment = allocMem((entry->commentLength + 1) * sizeof(w_byte));
      if (!entry->comment) {
        wabort(ABORT_WONKA, "Unable to allocate space for comment\n");
      }
      w_memcpy(entry->comment, data, (w_size)entry->commentLength);
      entry->comment[entry->commentLength] = 0x00;
      woempa(1, "comment = %s\n", entry->comment);
#endif
      data += commentLength;
    }

    /*
    ** For some strange reason, the extra length in the local header doesn't always match
    ** the extra length of the directory entry. So we jump to the local header with 'offset'
    ** and extra the correct lengths ourselfs.
    */

    vfs_lseek(entry->zipFile->fd, entry->offset + 26, SEEK_SET);
    vfs_read(entry->zipFile->fd, o_data, 4);
    nameLength = (w_int)readShort(other_data);
    extraLength = (w_int)readShort(other_data);
    entry->c_data_offset = entry->offset + 30 + nameLength + extraLength;
  }

  offs += data - start;
  *offsetptr = offs;
  woempa(1, "Finished reading entry, offset is now %d\n", offs);
  releaseMem(start);
 
}

/*
** TRAWL_SIZE specifies the amount of data to read during each file access
** while searching through the file for the global directory.  It needs to
** be at least 16 bytes, and can usefully be bigger, e.g. the block size used
** by the underlying filesystem.
*/

#define TRAWL_SIZE 16384

/* Define SANITY_CHECKS to include extra sanity checks at runtime */
//#define SANITY_CHECKS

#ifdef SANITY_CHECKS
static unsigned char previous[TRAWL_SIZE];
#endif

/*
** Read just enough of the first entry to be able to recognise it in the central directory.
*/
static z_zipEntry readFirstEntry(z_zipFile zipFile, w_size *offset_ptr) {
  w_word signature;
  // TODO probably we only need 4 bytes here
  w_ubyte *temp = allocMem(TRAWL_SIZE + 4);
  if (!temp) {
    woempa(9, "Unable to allocate temp buffer\n");
    return NULL;
  }

  vfs_read (zipFile->fd, temp, 4);
  signature = bytes2word(temp);
  if (signature != Z_LOCAL_HEADER) {
    woempa(9, "Zip file signature is 0x%08x, should be 0x%08x\n", signature, Z_LOCAL_HEADER);
    releaseMem(temp);
    return NULL;
  }

  z_zipEntry entry = allocClearedMem(sizeof(z_ZipEntry));
  if (!entry) {
    woempa(9, "Unable to allocate entry for zip file\n");
    releaseMem(temp);
    return NULL;
  }
  entry->zipFile = zipFile;
  *offset_ptr = 4; /* Skip over the signature */
  readZipEntry(WONKA_TRUE, entry, offset_ptr);
  releaseMem(temp);

  return entry;
}

/*
** Read one entry in the zipfile.
*/
static z_zipEntry readNextEntry(z_zipFile zipFile, w_size *offset_ptr) {
  w_word signature;
  w_ubyte sigbuf[4];
  woempa(7, "Reading ZipEntry at offset 0%07o\n", *offset_ptr);

  z_zipEntry entry = allocClearedMem(sizeof(z_ZipEntry));
  if (!entry) {
    woempa(7, "Unable to allocate memory for ZipEntry\n");
    return NULL;
  }

  entry->zipFile = zipFile;
  vfs_lseek (zipFile->fd, *offset_ptr, SEEK_SET);
  vfs_read (zipFile->fd, sigbuf, 4);
  signature = bytes2word(sigbuf);
  // TODO we should make a better distinction between these two cases
  if (signature == Z_DIR_END) {
    woempa(7, "Found end-of-directory signature %0x %0x %0x %0x\n", sigbuf[0], sigbuf[1], sigbuf[2], sigbuf[3]);
    return NULL;
  }
  else if (signature != Z_DIR_HEADER) {
    woempa(9, "Bad signature %0x %0x %0x %0x\n", sigbuf[0], sigbuf[1], sigbuf[2], sigbuf[3]);
    return NULL;
  }

  *offset_ptr += sizeof(w_word);
  entry->zipFile = zipFile;
  readZipEntry(WONKA_FALSE, entry, offset_ptr);

  return entry;
}

/*
** Find the central directory header
*/
static z_zipEntry findDirectoryHeader(z_zipFile zipFile,w_size *offset_ptr) {
  w_boolean found = FALSE;

  w_ubyte *trawlbuf = allocMem(TRAWL_SIZE + 4);
  if (!trawlbuf) {
    woempa(9, "Unable to allocate trawlbuf\n");
    return NULL;
  }

  z_zipEntry match = allocClearedMem(sizeof(z_ZipEntry));
  if (!match) {
    releaseMem(trawlbuf);
    return NULL;
  }

  match->zipFile = zipFile;
  while (!found) {
    w_boolean found_sentinel = FALSE;

    while (!found_sentinel) {
      w_size i;
      w_size l;

      vfs_lseek (zipFile->fd, *offset_ptr, SEEK_SET);
      l = vfs_read (zipFile->fd, trawlbuf, TRAWL_SIZE + 4);
      woempa(7, "was able to read %d bytes from 0%07o\n", l, *offset_ptr);
#ifdef SANITY_CHECKS
      if (memcmp(previous, trawlbuf, TRAWL_SIZE) == 0) {
        wabort(ABORT_WONKA, "OOH LAH LAH %07o %02x %02x %02x %02x %02x %02x %02x ...\n", *offset_ptr, trawlbuf[0], trawlbuf[1], trawlbuf[2], trawlbuf[3], trawlbuf[4], trawlbuf[5], trawlbuf[6], trawlbuf[7]);
      }
#endif
      woempa(7, "looking for pattern %02x %02x %02x %02x in bytes 0%07o to 0%07o\n", Z_SENTINEL0, Z_SENTINEL1, Z_DIR_BYTE0, Z_DIR_BYTE1, *offset_ptr, *offset_ptr + l - 1);
      for (i = 0; i + 4 < l; ++i) {
        if (trawlbuf[i] == Z_SENTINEL0 && trawlbuf[i + 1] == Z_SENTINEL1 && trawlbuf[i + 2] == Z_DIR_BYTE0 && trawlbuf[i + 3] == Z_DIR_BYTE1) {
          woempa(7, "Found directory sentinel at offset 0%07o + 0%o\n", *offset_ptr, i);
          found_sentinel = TRUE;
          *offset_ptr += i + 4;
          break;
        }
      }
      if (!found_sentinel) {
#ifdef SANITY_CHECKS
        memcpy(previous, trawlbuf, TRAWL_SIZE);
#endif
        woempa(7, "Not found, advancing offset by 0%o\n", TRAWL_SIZE);
        *offset_ptr += TRAWL_SIZE;
      }
    }
    readZipEntry(WONKA_FALSE, match, offset_ptr);
    
    if (match->offset == 0) {
      found = TRUE;
      woempa(7, "Found matching first entry in central directory.\n");
      woempa(7, "Match offset %d\n", match->c_data_offset);
    }
    else {
      woempa(7, "Found non-matching entry in central directory, trying again from %d ...\n", *offset_ptr);
    }
  }

  releaseMem(trawlbuf);

  if (found) {
    return match;
  }
  else {
   releaseMem(match);
   return NULL;
  }
}

z_zipFile parseZipFile(char *path) {

  z_zipEntry entry;
  z_zipEntry match = NULL;
  z_zipFile zipFile;
  w_ubyte *temp;
  w_size offset;

  temp = allocMem(TRAWL_SIZE + 4);
  if (!temp) {
    return NULL;
  }

  zipFile = allocMem(sizeof(z_ZipFile));
  if (! zipFile) {
    releaseMem(temp);
    return NULL;
  }

//  vfs_stat_struct statbuf;
//  w_int statrc = vfs_stat(path, &statbuf);
//  woempa(7, "Zipfile %s size is %d 0%o 0x%x\n", statbuf.st_size, statbuf.st_size, statbuf.st_size);

  zipFile->fd = vfs_open(path, O_RDONLY);
  if (zipFile->fd < 0) {
    woempa(9, "Unable to open zip file `%s'\n", path);
    releaseMem(zipFile);
    return NULL;
  }

  woempa(1, "Opened zip file `%s'\n", path);
  
  /*
  ** Read partial information of the first entry.
  */
  entry = readFirstEntry(zipFile, &offset);
  if (!entry) {
    woempa(9, "Unable to allocate entry for zip file `%s'\n", path);
    releaseMem(zipFile);
    return NULL;
  }

  /*
  ** Now skip over the file data until we find a directory header that matches
  ** in name and offset with this first entry.
  */
  match = findDirectoryHeader(zipFile, &offset);
  if (!match) {
    woempa(9, "Unable to find directory header `%s'\n", path);
    releaseMem(zipFile);
    return NULL;
  }

  list_init(match);
  zipFile->ht = ht_create ("hashtable:zipfile", 101, cstring_hash, cstring_equal, 0, 0);
  if (!zipFile->ht) {
    woempa(9, "Unable to find create %s\n", "hashtable:zipfile");
    releaseMem(zipFile);
    releaseMem(temp);
    releaseMem(match);
    releaseMem(zipFile);
    return NULL;
  }
  ht_write(zipFile->ht, (w_word)match->name, (w_word)match);

  zipFile->entries = match;
  // MEMORY LEAK, RELEASE entry	!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1

  while (1) {
    entry = readNextEntry(zipFile, &offset);
    if (!entry) {
// NAH
//      woempa(9, "Unable to read zipfile entry at offset 0%07o\n", offset);
//      releaseMem(zipFile);
//      return NULL;
      break;
    }
    list_insert(match, entry);
    ht_write(zipFile->ht, (w_word)entry->name, (w_word)entry);
  }
  
  /*
  ** ... now read the concluding file information.
  */

  woempa(7, "Hashtable contains %d entries (capacity = %d)\n", zipFile->ht->occupancy, zipFile->ht->currentsize);
  woempa(7, "Reading central directory of zipfile 0x%08x\n", (w_word)zipFile);

  offset += sizeof(w_word);
  vfs_lseek (zipFile->fd, offset, SEEK_SET);
  vfs_read (zipFile->fd, temp, 18);
#ifdef DEBUG
  zipFile->c_disk = bytes2short(temp);
  zipFile->d_disk = bytes2short(temp + 2);
  zipFile->numDisk = bytes2short(temp + 4);
  zipFile->numDir = bytes2short(temp + 6);
  zipFile->size = (w_int)bytes2word(temp + 8);
  zipFile->offset = (w_int)bytes2word(temp + 12);
  zipFile->commentLength = bytes2short(temp + 16);
  offset += 18;
  zipFile->comment = allocMem((zipFile->commentLength + 1) * sizeof(w_byte));
  if (! zipFile->comment) {
    releaseMem(temp);
    releaseMem(zipFile);
    return NULL;
  }
  vfs_lseek(zipFile->fd, offset, SEEK_SET);
  vfs_read(zipFile->fd, zipFile->comment, zipFile->commentLength);

  dumpDir(zipFile);
#endif

  releaseMem(temp);

  return zipFile;

}

static void releaseZipEntry(z_zipEntry entry) {

  woempa(1, "Releasing entry '%s'\n", entry->name);
  
  if (entry->name) {
    releaseMem(entry->name);
  }
  if (entry->u_data) {
    releaseMem(entry->u_data);
  }
#ifdef DEBUG
  if (entry->comment) {
    releaseMem(entry->comment);
  }
  if (entry->extra) {
    releaseMem(entry->extra);
  }
#endif
  
  releaseMem(entry);
  
}

void deleteZipEntry(z_zipEntry entry) {
  woempa(1, "Deleting entry '%s'\n", entry->name);
  if (entry->zipFile->entries == entry) {
    entry->zipFile->entries = entry->next;
  }
  list_remove(entry);
  ht_erase(entry->zipFile->ht, (w_word)entry->name);
  releaseZipEntry(entry);
}

void releaseZipFile(z_zipFile zf) {

  z_zipEntry entry;
  z_zipEntry victim;

  /*
  ** First clean up the stuff that has been allocated in the entries,
  ** then the entries themselves.
  */
  
  entry = zf->entries->next;
  while (entry != zf->entries) {
    victim = entry;
    entry = entry->next;
    list_remove(victim);
    releaseZipEntry(victim);
  }

  /*
  ** Now release the first entry itself...
  */
  
  releaseZipEntry(zf->entries);
  ht_destroy(zf->ht);

  /*
  ** Release the file itself...
  */
  
  close(zf->fd);
  releaseMem(zf);
    
}

z_zipEntry findZipEntry(z_zipFile dir, char *name) {
  return (z_zipEntry)ht_read(dir->ht, (w_word)name);
}

w_boolean uncompressZipEntry(z_zipEntry entry) {

  z_decoder decoder;
  
  /*
  ** .. see if it has been decoded yet, and if yes, return it when it's not
  ** containing an error ...
  */

  if (isSet(entry->flags, ZE_UNCOMPRESSED) && isNotSet(entry->flags, ZE_ERROR)) {
    woempa(1, "Already decompressed\n");
    setFlag(entry->flags, ZE_ZERO_LENGTH);
    return WONKA_TRUE;
  }

  if (isSet(entry->flags, ZE_ERROR) || entry->u_size == 0) {
    woempa(1, "Error / zero-length\n");
    return WONKA_FALSE;
  }  

  if (entry->compression >= 0 && entry->compression <= 10) {
    decoder = decoders[entry->compression];
    woempa(1, "Using decoder[%d] (%p)\n", entry->compression, decoder);
    entry->u_data = (*decoder)(entry);
    if (!entry->u_data) {
      return WONKA_FALSE;
    }
    if (isSet(entry->flags, ZE_UNCOMPRESSED) && isNotSet(entry->flags, ZE_ERROR)) {
      woempa(1, "Decompression succeeded\n");
      return WONKA_TRUE;
    }
  }
  else {
    wabort(ABORT_WONKA, "Unknown compression code %d in zip file.\n", entry->compression);
  }

  /*
  ** When we get here, something is wrong...
  */

  return WONKA_FALSE;
  
}

/*
** Unpack a file that has been "compressed" or better, "stored" with method 0.
*/

static w_ubyte *zip_destore(z_zipEntry entry) {
  w_ubyte* result = allocMem(entry->u_size);
  if (!result) {
    wabort(ABORT_WONKA, "Unable to allocate space for result\n");
  }
  vfs_lseek(entry->zipFile->fd, entry->c_data_offset, SEEK_SET);
  vfs_read(entry->zipFile->fd, result, entry->u_size);

  if (entry->d_crc == CCITT_32(result, (w_size)entry->u_size)) {
    setFlag(entry->flags, ZE_UNCOMPRESSED);
  }

  return result;  
}

/*
w_bits readSingleBit(w_deflate_control bs);
w_bits readBits(w_deflate_control bs, w_size count);
w_hnode decode(w_ztable table, w_deflate_control in);
w_zdict buildFixedDictionary(void);
w_zdict buildDynamicDictionary(w_deflate_control in);
w_int inflateBlock(w_deflate_control bs, w_zdict dict);
void releaseDictionary(w_zdict dict);
*/
/*
** The function to handle not yet implemented decompressors.
*/

static w_ubyte *notyet(z_zipEntry entry) {
  setFlag(entry->flags, ZE_ERROR);
  setFlag(entry->flags, ZE_NO_DECOMPRESSOR);
  woempa(1, "No decompressor yet for compression type %d.\n", entry->compression);

  return NULL;
}

w_int quickInflate(w_ubyte* c_data, w_size c_size, w_ubyte* u_data, w_size u_size, w_word d_crc, char **errmsg, w_int clone){
  w_inflate_control unzipper = allocInflateControl();
  w_int result = 1;
  if (unzipper == NULL) {
    *errmsg = "Out of Memory";
  } else {
    inflate_control_setInput(unzipper, c_data, c_size, clone);
    inflate_control_inflate(unzipper);
    result = u_size != (w_size)inflate_control_getbytes_from_queue(unzipper, u_data, u_size);
    if(result) {
      *errmsg = "Not enough data";
    }
    releaseInflateControl(unzipper);
  }
  return result;
}

/*
w_ubyte* quickInflate(w_ubyte* c_data, w_size c_size, w_size u_size, w_word d_crc, char **errmsg){
  w_ubyte* result;
  w_deflate_control unzip = NULL;
  w_deflate_queueelem qe = NULL;
  x_status s;
  w_size offset;
  w_boolean out_of_memory;

  *errmsg = NULL;
  result = allocMem(u_size);
  out_of_memory = !result;
  if (!out_of_memory) {
    unzip = allocClearedMem(sizeof(w_Deflate_Control));
    out_of_memory = !unzip;
  }

  if (!out_of_memory) {
    unzip->output_bekken = allocMem(32 * 1024 + 1024);
    out_of_memory = !unzip->output_bekken;
  }

  if (!out_of_memory) {
    unzip->input_bekken = allocMem(32 * 1024 + 512);
    out_of_memory = !unzip->input_bekken;
  }

  if (!out_of_memory) {
    unzip->qmem_in = allocMem(4 * 512);
    out_of_memory = !unzip->qmem_in;
  }
    
  if (!out_of_memory) {
    unzip->qmem_out = allocMem(4 * 512);
    out_of_memory = !unzip->qmem_out;
  }
  
  if (!out_of_memory) {
    unzip->q_in = allocMem(sizeof(x_Queue));
    out_of_memory = !unzip->q_in;
  }
  
  if (!out_of_memory) {
    unzip->q_out = allocMem(sizeof(x_Queue));
    out_of_memory = !unzip->q_out;
  }

  if (!out_of_memory) {
    qe = allocClearedMem(sizeof(w_Deflate_QueueElem));
    out_of_memory = !qe;
  }

  if (!out_of_memory) {
    qe->data = allocMem(c_size);
    out_of_memory = !qe->data;
  }

  if (out_of_memory) {
    *errmsg = "out of memory";
  }
  else {
    unzip->i_bits = 0x01;
    unzip->o_mask = 0x1;
    unzip->compression_level = 9;
    unzip->need_more_input = 1;             // we are starting up, so we need input

    // TODO - check status each time
    x_queue_create(unzip->q_in, unzip->qmem_in, 512);
    x_queue_create(unzip->q_out, unzip->qmem_out, 512);

    w_memcpy(qe->data, c_data, c_size);
    qe->size = c_size;
    qe->errnum = WUNZIP_OK;

    woempa(1, "Sending %d bytes at %p\n", c_size, qe->data);
    s = x_queue_send(unzip->q_in, qe, Z_INFLATER_WAIT);
    woempa(1, "Result = %d\n", s);
    if (s != xs_success) {
      wabort(ABORT_WONKA, "x_queue_send() returned %d\n", s);
    }

    unzip->stop = 1;

  // CG 20050512
  // zzzinflate(unzip);
  // START INLINE zzzinflate, eliminating synchronisation and unzip->reset/no_auto
  {
    w_bits lastblock;
    w_bits type;
    w_zdict fixed_dict;
    w_zdict dict;
    w_int size, check, err;

    err = 0;

    fixed_dict = NULL;
    dict = NULL;

    woempa(1, "Inflating stream.\n");

    do {
      woempa(1, "State: err %i\n", err);
      lastblock = readSingleBit(unzip);

      if (unzip->par_in == NULL) {
        goto hastalavista;
      }

      type = readBits(unzip, 2);
      woempa(1, "Inflating block, is %sthe last block, type %d.\n", lastblock ? "" : "NOT ", type);

      if (unzip->par_in == NULL) {
        goto hastalavista;
      }

      switch (type) {
        case 0:
          readByteAlign(unzip);
          size = 0;
          size = readLiteralByte(unzip);
          size |= (readLiteralByte(unzip) << 8);
          check = 0;
          check = readLiteralByte(unzip);
          check |= (readLiteralByte(unzip) << 8);

          if (unzip->par_in == NULL) {
            goto hastalavista;
	  }

          woempa(1, "--> block is of the 'stored' type. %d bytes (0x%04x == 0x%04x)\n", size, size & 0x0000ffff, ~check & 0x0000ffff);
          if ((size & 0x0000ffff) != (~check & 0x0000ffff)) {
            woempa(9, "Wrong block check 0x%04x != 0x%04x.\n", size & 0x0000ffff, ~check & 0x0000ffff);
            err = 1;
            goto hastalavista;
          }
          while (size--) {
            if (writeLiteralByte(unzip, readLiteralByte(unzip))) {
              err = 1;
              goto hastalavista;
            }
          }
          break;

        case 1:
          if (! fixed_dict ) {
            fixed_dict = buildFixedDictionary();
	    woempa(1, "Fixed dictionary = %p\n", fixed_dict);
            if (!fixed_dict) {
              wabort(ABORT_WONKA, "Unable to build fixed dictionary\n");
            }
          }
          woempa(1, "--> block is of the 'fixed huffman code' type.\n");
          if (inflateBlock(unzip, fixed_dict)) {
            err = 1;
            goto hastalavista;
          }
          break;

        case 2:
          woempa(1, "--> block is of the 'dynamic huffman code' type.\n");
          dict = buildDynamicDictionary(unzip);
	  woempa(1, "Dynamic dictionary = %p\n", dict);
          if (! dict) {
            err = 1;
            goto hastalavista;
          }
          if (inflateBlock(unzip, dict) != 0) {
            err = 1;
            goto hastalavista;
          }
          releaseDictionary(dict);
          dict = NULL;
	  woempa(1, "Released dynamic dictionary\n");
          break;

        default:
          woempa(9, "Block has an unknown type (0x%08x) or has an error in it!\n", type);
          err = 1;
          goto hastalavista;

      }
    } while (! lastblock);

hastalavista:

    woempa(1, "HASTALAVISTA BABY ...\n");

    if (!err) {
      bekkenFlush(unzip);
    }
    else {
      woempa(7, "HOOLA, an error occured while decompressing...\n");
      errorFlush(unzip);
    }

    if (fixed_dict) {
      releaseDictionary(fixed_dict);
    }

    if (dict) {
      releaseDictionary(dict);
    }

    woempa(1, "State: err %i\n", err);
    // reinit so we can keep on processing
    unzip->offset_in = unzip->offset_bek_out = 0;
    unzip->lookahead_bek_in = unzip->offset_bek_in = unzip->size_bek_out = 0;
    unzip->i_bits = 0x01;
    unzip->o_mask = 0x1;
    unzip->o_bits = 0;

    woempa(1, "State: err %i\n", err);

    woempa(1, "Exiting\n");
    unzip->processed_size = 0;
  }

    // reinit so we can keep on processing
    unzip->offset_in = 0;
    unzip->offset_bek_out = 0;
    unzip->lookahead_bek_in = 0;
    unzip->offset_bek_in = 0;
    unzip->size_bek_out = 0;
    unzip->i_bits = 0x01;
    unzip->o_mask = 0x1;
    unzip->o_bits = 0;
    unzip->no_auto = 0;

    unzip->processed_size = 0;
    if (!unzip->stop) unzip->nomoreinput = 0;

    offset = 0;

    while (offset < u_size) {
      woempa(1, "quickInflate: offset = %d, u_size = %d\n", offset, u_size);
      s = x_queue_receive(unzip->q_out, (w_void **)&qe, Z_INFLATER_WAIT);

      switch (s) {
      case xs_success:
        woempa(1, "quickInflate: x_queue_receive() succeeded, result is %p\n", qe->data);
        unzip->par_out = qe;
        unzip->offset_out = 0;
        break;

      case xs_no_instance:
        wabort(ABORT_WONKA, "quickInflate(): xs_no_instance when dequeueing\n");

        if (offset > 0) {
            woempa(1, "returning block. size %i\n", offset);

            break;
          }
        else {
          wabort(ABORT_WONKA, "offset <= 0\n");
        }
      default:
        wabort(ABORT_WONKA, "quickInflate(): status %d when dequeueing\n", s);
      }

      woempa(1, "unzip->par_out = %p\n", unzip->par_out);
      if (unzip->par_out != NULL) {
        if (offset == u_size){
          woempa(1, "returning full block\n");

          break;
        }
        else if ((unzip->par_out->size - unzip->offset_out + offset) < u_size) {
          woempa(1, "copying next block\n");
          w_memcpy(result + offset, unzip->par_out->data + unzip->offset_out, (unsigned)(unzip->par_out->size - unzip->offset_out));

          offset += unzip->par_out->size - unzip->offset_out;

          if (unzip->par_out->data != NULL) {
            releaseMem(unzip->par_out->data);
          }
          releaseMem(unzip->par_out);
          unzip->par_out = NULL;
          unzip->offset_out = 0;
        }
        else {
          woempa(1, "copying last block\n");
          w_memcpy(result + offset, unzip->par_out->data + unzip->offset_out, (u_size - offset));
          unzip->offset_out += u_size - offset;
          offset += u_size - offset;

          woempa(1, "returning block. size %i\n", offset);

          // buffer is now full, so move out
          break;
        }
      }
    }
  }

  if (unzip) {
    if (unzip->q_in) {
      woempa(1, "Deleting q_in at %p\n", unzip->q_in);
      x_queue_delete(unzip->q_in);
      releaseMem(unzip->q_in);
    }

    if (unzip->qmem_in) {
      releaseMem(unzip->qmem_in);
    }

    if (unzip->q_out) {
      woempa(1, "Deleting q_out at %p\n", unzip->q_out);
      x_queue_delete(unzip->q_out);
      releaseMem(unzip->q_out);
    }

    if (unzip->qmem_out) {
      releaseMem(unzip->qmem_out);
    }

    if (unzip->par_out) {
      if (unzip->par_out->data) {
        releaseMem(unzip->par_out->data);
      }
      releaseMem(unzip->par_out);
    }

    if (unzip->output_bekken) {
      releaseMem(unzip->output_bekken);
    }

    if (unzip->input_bekken) {
      releaseMem(unzip->input_bekken);
    }

    releaseMem(unzip);
  }

  if (result && d_crc != CCITT_32(result, u_size)) {
    woempa(9, "CRC check failed\n");
    *errmsg = "CRC check failed";
    releaseMem(result);

    return NULL;

  }
  else if (out_of_memory) {

    return NULL;

  }
  else {

    return result;

  }
}
*/
/**
 * Inflate a zip entry.
 */
static w_ubyte *zip_inflate(z_zipEntry entry) {
  w_ubyte* source = allocMem(entry->c_size);
  w_ubyte *result = allocMem(entry->u_size);
  char *errmsg;
  w_int rc;
  w_size l = 0;

  if (!source) {
    wabort(ABORT_WONKA, "Unable to allocate space for source\n");
  }
  if (!result) {
    wabort(ABORT_WONKA, "Unable to allocate space for result\n");
  }

  rc = vfs_lseek(entry->zipFile->fd, entry->c_data_offset, SEEK_SET);
  if (rc == -1) {
    wabort(ABORT_WONKA, "lseek() failed\n");
  }
  woempa(1, "zip_inflate(): Need to read %d bytes\n", entry->c_size);
  //w_printf("zip_inflate(): Need to read %d bytes\n", entry->c_size);
  while (l < entry->c_size) {
    rc = vfs_read(entry->zipFile->fd, source + l, entry->c_size - l);
#if !defined (OSWALD) && !defined (FREERTOS)
    while (rc == -1 && (errno == EAGAIN || errno == EINTR)) {
      rc = vfs_read(entry->zipFile->fd, source + l, entry->c_size - l);
    }
#endif
    if (rc == -1) {
      wabort(ABORT_WONKA, "read() failed after %d bytes\n", l);
    }
    woempa(1, "zip_inflate(): Read %d bytes\n", rc);
    //w_printf("zip_inflate(): Read %d bytes\n", rc);
    l += rc;
  }

  if(quickInflate(source, entry->c_size, result, entry->u_size, entry->d_crc, &errmsg, WINF_DELETE_DATA)) {
    wabort(ABORT_WONKA, "zip_inflate : %s : %s\n", entry->name, errmsg ? errmsg : "????");
  }

  setFlag(entry->flags, ZE_UNCOMPRESSED);
  entry->u_data = result;

  return result;
}

