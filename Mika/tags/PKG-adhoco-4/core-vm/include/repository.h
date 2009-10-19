#ifndef _REPOSITORY_H
#define _REPOSITORY_H

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

/*
** $Id: repository.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

#include "wonka.h"

/*
** The w_Repository structure.
*/

typedef struct w_Repository {
  w_hashtable hashtable;
} w_Repository;

/*
** The system repository
*/

// CG HACK extern w_repository W_Repository_system;

/*
** These are types (the lower 8 bits). Unknown files are catalogued as RT_RAW.
*/

#define RT_DIRECTORY                   0x00000001
#define RT_RAW                         0x00000005
#define RT_MANIFEST                    0x00000006
#define RT_SIGNATURE                   0x00000008
#define RT_ZIP                         0x00000009
#define RT_JAR                         0x0000000a
#define RT_LAST                        0x000000fe   /* 254 */
#define RT_MASK                        0x000000ff   /* 255 */
 
#define getRepositoryType(f)           (w_int)(RT_MASK & (f))
#define setRepositoryType(f, t)        ((f | RT_MASK) & (t))

extern const char *RepositoryType2char[];

/*
** These are flags (the upper 24 bits)
*/

#define RF_U_AVAILABLE                 0x00000100 /* Uncompressed data is available                           */
#define RF_C_STATIC                    0x00000200 /* The memory of the compressed entry should not be freed   */
#define RF_U_STATIC                    0x00000400 /* The memory of the uncompressed entry should not be freed */
#define RF_U_CACHED                    0x00000800 /* The memory of the uncompressed entry is cached           */
#define RF_CLAZZ_INCOMPLETE            0x00001000 /* The entry hasn't been consumed yet; DONT release yet !   */
#define RF_MASK                        0xffffff00

#define RepositoryFlags(f)             (w_flags)(RF_MASK &(f))

/*
** An entry in a repository.
*/

typedef struct w_Rentry {

  w_string  name;
  w_word    version;
  w_flags   flags;                                /* See RF_ flags and RT_ types above                        */

  /*
  ** Uncompressed data information
  */

  w_ubyte *  u_data;
  w_word    u_sum;
  w_size    u_size;

  /*
  ** Compressed data information
  */

  w_int     c_size;
  w_ubyte *  c_data;

} w_Rentry;

w_repository createRepository(w_thread thread, w_ubyte *data, w_size size);
void deleteRepositoryEntry(w_repository, w_string name);
void destroyRepository(w_repository);

#endif /* _REPOSITORY_H */
