#ifndef _REPOSITORY_H
#define _REPOSITORY_H

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
