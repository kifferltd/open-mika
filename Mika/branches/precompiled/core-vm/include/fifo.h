#ifndef _FIFO_H
#define _FIFO_H

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
** $Id: fifo.h,v 1.2 2004/11/18 21:17:07 cvs Exp $
*/

#include "wonka.h"

/**
**
** A FIFO is a first-in, first-out queue of pointers (\texttt{void*}).
** New ``leaves'' are automativally added as existing ones become full:
** when a leaf is exhausted (number of puts = number of gets), it is
** moved from the beginning of the FIFO to the end and hence recycled.
*/

/** Default size for a FIFO leaf.
** Please use a number that is one less then a true binary number
** because we loose a pointer size in a fifoLeaf. e.g. 511, 1023, ... and
** this would give nice sized FifoLeafs.
*/

#define FIFO_DEFAULT_LEAF_SIZE   511

/** 
 ** FIFO leaf structure.  The leaves of a FIFO form a simple singly-linked list.
 */
typedef struct FifoLeaf {
  struct FifoLeaf *next;
  void *data[1];
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
  w_size leafElements;
// number of elements currently in use (put and not yet got)
  w_size numElements;
// the current leaf for getting
  FifoLeaf *getFifoLeaf;
// the current leaf for putting
  FifoLeaf *putFifoLeaf;
// spare, just to round up total size to 8 words  
  void *dummy;
} w_Fifo;

#define FIFO_SUCCESS   0
#define FIFO_NO_MEMORY 1

// Allocate a new FIFO
// Each leaf will contain \texttt{leafElements} elements.
// For speed, choose \texttt{leafElements} to be one less than a power of two.
w_fifo allocFifo(w_size leafElements);

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

#endif /* _FIFO_H */
