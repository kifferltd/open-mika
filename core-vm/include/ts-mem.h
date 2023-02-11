/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2007 by Chris Gray, /k/ Embedded  *
* Java Solutions. All rights reserved.                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#ifndef _TS_MEM_H
#define _TS_MEM_H

#include "oswald.h"
#include "mika_threads.h"
#include "wonka.h"

/*
** If TRACE_MEM_ALLOC is set, extra information is included in the w_Chunk
** header and the reportMemStat() function can be used to anylyse heap usage.
** Normally TRACE_MEM_ALLOC is define iff DEBUG is defined, but feel free to
** define it by hand if you want to trace this data in a non-DEBUG build.
*/
#ifdef DEBUG
#define TRACE_MEM_ALLOC
#endif

// #define TRACE_MEM_ALLOC

/*
** The internal structure of a memory chunk for Wonka when TRACE_MEM_ALLOC is defined. 
*/

#ifdef TRACE_MEM_ALLOC // --------------------------------------------------------------

typedef struct w_Chunk {          // offset of  ends at
  w_word       madgic;            //     0         3  madgic word to identify a debug memory chunk
  const char * file;              //     4         7  in which file the chunk was allocated
  w_int        line;              //     8        11  at which line the chunk was allocated
  w_int        back;              //    12        15  an index to the backfence byte
  w_size       size;              //    16        19  the size as was requested by the user for datax
  w_word       front;             //    20        23  the front control word to check for out of bound writes
  w_ubyte      data[8];           //    24       ...  pre-allocate 4 bytes for backfence
} w_Chunk;

#else

typedef struct w_Chunk {
  w_ubyte      data[0];
} w_Chunk;

#endif // --- TRACE_MEM_ALLOC ----------------------------------------------------------

typedef struct w_Chunk *          w_chunk;

/*
** Macros for allocating and freeing memory. Note that when TRACE_MEM_ALLOC is defined, the routines
** get a _d_ prepended and have file and line number information as last parameters. For
** non TRACE_MEM_ALLOC compiles, the macros refer to the normal _x memory functions.
*/

#ifdef TRACE_MEM_ALLOC

#define allocMem(n)                 _d_allocMem(n, __FILE__, __LINE__)
#define allocClearedMem(n)          _d_allocClearedMem(n, __FILE__, __LINE__)
#define reallocMem(b, s)            _d_reallocMem(b, s, __FILE__, __LINE__)
#define releaseMem(b)               _d_releaseMem(b, __FILE__, __LINE__)
#define discardMem(b)               _d_discardMem(b, __FILE__, __LINE__)

#else

#define allocMem(n)                 _allocMem(n)
#define allocClearedMem(n)          _allocClearedMem(n)
#define reallocMem(b, s)            _reallocMem(b, s)
#define releaseMem(b)               _releaseMem(b)
#define discardMem(b)               _discardMem(b)

#endif /* TRACE_MEM_ALLOC */

/*
** The ID for anonymous memory; please use static const w_size to assign ID's.
*/

static const w_size ID_anon = 32;

/*
** Get the chunk reference from a void * block.
*/

#ifndef offsetof
#define offsetof(TYPE, MEMBER) ((size_t) &((TYPE *)0)->MEMBER)
#endif

inline static w_chunk block2chunk(void * block) {
  return ((w_chunk)((unsigned char *)block - offsetof(w_Chunk, data)));
}

inline static w_object chunk2object(void * block) {
  return (w_object) (((w_chunk)block)->data);
}

/*
** The memory tag bit that is turned on when the heap block is an object.
*/

#define OBJECT_TAG                (0x80)

/*
** A spare bit in the tag bits, could be used for Wonka strings...
*/

#define SPARE_TAG                 (0x100)

inline static x_word getMemTag(void * block) {
  return x_mem_tag_get(block2chunk(block));
}

inline static void setMemTag(void * block, x_word tag) {
  x_mem_tag_set(block2chunk(block), tag);
}

/*
** The function prototypes for the allocation and release routines.
*/

#ifdef TRACE_MEM_ALLOC

void * _d_allocMem(w_size size, const char * file, const int line);
void * _d_allocClearedMem(w_size size, const char * file, const int line);
void * _d_reallocMem(void * block, w_size size, const char * file, const int line);
void   _d_releaseMem(void * block, const char * file, const int line);
void   _d_discardMem(void * block, const char * file, const int line);

#else

void * _allocMem(w_size size);
void * _allocClearedMem(w_size size);
void * _reallocMem(void * block, w_size size);
void _releaseMem(void * block);
void _discardMem(void * block);

#endif /* TRACE_MEM_ALLOC */

w_size gc_reclaim(w_int requested, w_instance caller);

/*
 * Allocate cleared memory from the system pool, with automatic retries built in.
 */
static inline void *allocClearedMem_with_retries(w_size bytes, w_int retries) {
  void *result = allocClearedMem(bytes);
  w_int count = retries;

  if (!result && currentWonkaThread != marking_thread) {
    while (count--) {
      woempa(7, "allocClearedMem_with_retries : RETRY\n");
      gc_reclaim(bytes, NULL);
      result = allocClearedMem(bytes);
      if (result) {
        break;
      }
    }
  }

  return result;
}

/*
** For chasing memory leaks, the reportMemStat function can be used. It dumps a sorted
** text representation of the memory that is in use and where it is allocated. Sorting
** can be done on a number of equal blocks or filename basis.
*/

#ifdef TRACE_MEM_ALLOC

#define SORT_BY_HITS                  1
#define SORT_BY_FILE                  2

void reportMemStat(w_int type);
#define heapCheck _heapCheck(__FUNCTION__, __LINE__)
void _heapCheck(const char * funtion, const int line);

inline static const char * allocedInFile(void * block) {
  return block2chunk(block)->file;
}

inline static int allocedAtLine(void * block) {
  return block2chunk(block)->line;
}

#endif /* TRACE_MEM_ALLOC */

/**
 ** Hashtable used to collect all "global' references; in practice this means
 ** 1. a reference to each Thread object (and hence to its jstack, etc.);
 ** 2. global references created by JNI.
 */
w_hashtable globals_hashtable;

#ifdef JNI
// reserve space for plenty of the beasties
#define GLOBALS_HASHTABLE_SIZE   1439
#else
// reserve less space, it's basicallt just for threads
#define GLOBALS_HASHTABLE_SIZE   97
#endif

#endif /* _TS_MEM_H */
