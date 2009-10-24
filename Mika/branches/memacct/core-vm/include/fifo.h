/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2009 by Chris Gray,         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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

#ifndef _FIFO_H
#define _FIFO_H

#include "wonka.h"
#include "oswald.h"

/**
**
** A FIFO is a first-in, first-out queue of pointers (\texttt{void*}).
** New ``leaves'' are automatically added as existing ones become full:
** when a leaf is exhausted (number of puts = number of gets), it is
** moved from the beginning of the FIFO to the end and hence recycled.
*/

/*
** If THREAD_SAFE_FIFOS is defined it is possible (using allocThreadSafeFifo)
** to create a w_Fifo which can safely be used by multiple threads without
** requiring a separate lock (x_monitor or x_mutex), provided there is at most
** one thread which writes to the fifo. A standard w_Fifo created ** using 
** allocFifo is *not* thread-safe.
** Enabling THREAD_SAFE_FIFOS adds one word to the size of all w_Fifo struct's
** and an x_Mutex to every w_Fifo which is created thread-safe. The mutex is
** only locked while crossing leaf boundaries, so the majority of accesses are
** lock-free.
*/

#define THREAD_SAFE_FIFOS

/** 
 ** FIFO leaf structure.  The leaves of a FIFO form a simple singly-linked list.
 */
typedef struct FifoLeaf {
  struct FifoLeaf *next;
  void *dummy;
  void *data[0];
} FifoLeaf;

/** FIFO header structure.
*/
typedef struct w_Fifo {
// index into putFifoLeaf where the next put will occur (postincrementingly).
  w_size putFifoIndex;
// index into getFifoLeaf where the next get will occur (postincrementingly).
  w_size getFifoIndex;
// number of leaves already allocated.
  w_size numLeaves;
// capacity of each leaf (in words)
  w_size elementsPerLeaf;
// the current leaf for getting
  FifoLeaf *getFifoLeaf;
// the current leaf for putting
  FifoLeaf *putFifoLeaf;
// Total number of elements put since the fifo was created
  w_size numPut;
// Total number of elements got since the fifo was created
  w_size numGot;
#ifdef THREAD_SAFE_FIFOS
  x_mutex mutex;
  void *dummy;
#endif
} w_Fifo;

#define FIFO_SUCCESS   0
#define FIFO_NO_MEMORY 1

/*
** Allocate a new FIFO
** Each leaf will contain \texttt{elementsPerLeaf} elements.
** For speed, choose \texttt{elementsPerLeaf} to be two less than a power of two.
** The fifo thus created is not thread-safe.
*/
w_fifo allocFifo(w_size elementsPerLeaf);

#ifdef THREAD_SAFE_FIFOS
/*
** Allocate a new, thread-safe FIFO
** Each leaf will contain \texttt{elementsPerLeaf} elements.
** For speed, choose \texttt{elementsPerLeaf} to be two less than a power of two.
*/
w_fifo allocThreadSafeFifo(w_size elementsPerLeaf);
#endif

/**
 ** Release a FIFO
 */
void releaseFifo(w_fifo f);

/** Contract a FIFO (release exhausted leaves).
** @return the number of bytes of memory which were freed.
*/
int contractFifo(w_fifo f);

/** Reduce a FIFO (release at most one exhausted leaf).
** @return the number of bytes of memory which were freed.
*/
int reduceFifo(w_fifo f);

/** Expand a FIFO to ensure space for at least 'n' items in total.
** Returns WONKA_TRUE iff successful.
*/
w_boolean expandFifo(w_size newsize, w_fifo f);

/** Append an element e to FIFO f
 ** Returns 0 if succeeded, <0 if not (e.g. no memory for new leaf).
 */
w_int putFifo(void *e, w_fifo f);

/**
 ** Remove the front element of FIFO f
 */
void *getFifo(w_fifo f);

/**
 ** For every element e of fifo f, execute fun(e).
 */
w_int forEachInFifo(w_fifo f, void (*fun)(void *e));

/**
 ** Get the total capacity of the fifo.
 */
#define capacityOfFifo(f) ((f)->numLeaves * (f)->elementsPerLeaf)

/**
 ** Get the number of elements currently in the fifo (put and not yet got).
 */
#define occupancyOfFifo(f) ((f)->numPut - (f)->numGot)

#define isEmptyFifo(f) ((f)->numPut == (f)->numGot)

#endif /* _FIFO_H */
