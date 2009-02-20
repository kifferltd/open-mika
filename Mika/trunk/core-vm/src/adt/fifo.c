/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
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

#include "fifo.h"
#include "ts-mem.h"

#ifdef THREAD_SAFE_FIFOS
#define CRITICAL_ENTER if (f->mutex) { x_mutex_lock(f->mutex, x_eternal); }
#define CRITICAL_EXIT  if (f->mutex) { x_mutex_unlock(f->mutex); }
#else
#define CRITICAL_ENTER 
#define CRITICAL_EXIT
#endif

/* returns number of bytes saved during contraction */

int contractFifo(w_fifo f) {
  FifoLeaf *current;
  FifoLeaf *next;
  int released = 0;

  CRITICAL_ENTER
  current = f->putFifoLeaf;
  next = current->next;
  woempa(7, "Fifo %p has putFifoLeaf %p, getFifoLeaf %p\n", f, f->putFifoLeaf, f->getFifoLeaf);

  while (f->getFifoLeaf != next) {
    woempa(7, "current is %p, next is %p\n", current, next);
    woempa(7, "making current->next point to next->next (%p)\n", next->next);
    current->next = next->next;
    CRITICAL_EXIT // releaseMem will acquire memory lock
    releaseMem(next);
    released += sizeof(FifoLeaf) + (sizeof(void *) * (f->elementsPerLeaf));
    woempa(7, "releasing %p, freed %d bytes so far\n", next, released);
    CRITICAL_ENTER
    current = current->next;
    next = current->next;
  }
  CRITICAL_EXIT

  return released;

}

/* returns number of bytes saved during reduction */

int reduceFifo(w_fifo f) {
  FifoLeaf *current = f->putFifoLeaf;
  FifoLeaf *next = current->next;
  int released = 0;

#ifdef THREAD_SAFE_FIFOS
  if (f->mutex) {
    x_mutex_lock(f->mutex, x_eternal);
  }
#endif
  woempa(7, "Fifo %p has putFifoLeaf %p, getFifoLeaf %p\n", f, f->putFifoLeaf, f->getFifoLeaf);

  if (f->getFifoLeaf != next) {
    CRITICAL_ENTER
    woempa(7, "current is %p, next is %p\n", current, next);
    woempa(7, "making current->next point to next->next (%p)\n", next->next);
    current->next = next->next;
    CRITICAL_EXIT
    releaseMem(next);
    released = sizeof(FifoLeaf) + (sizeof(void *) * (f->elementsPerLeaf));
    woempa(7, "releasing %p, freed %d bytes\n", next, released);
  }

  return released;

}

/**
 ** Increase the capacity of the fifo to at least newsize elements.
 ** Note that this counts as a "writer", i.e. if another thread is also
 ** writing to the fifo or calls this function then errors may result.
 */
w_boolean expandFifo(w_size newsize, w_fifo f) {
  unsigned int i;
  unsigned int j = f->putFifoIndex;
  FifoLeaf *leaf = f->putFifoLeaf;

  if (newsize <= capacityOfFifo(f)) {

    return WONKA_TRUE;

  }

  woempa(7, "current size is %d: expanding capacity from %d to %d\n", occupancyOfFifo(f), capacityOfFifo(f), newsize);
  for (i = occupancyOfFifo(f); i < newsize; ++i) {
    ++j;
    if (j == f->elementsPerLeaf) {
      CRITICAL_ENTER
      if (leaf->next != f->getFifoLeaf) {
        leaf = leaf->next;
      }
      else {
        CRITICAL_EXIT
        FifoLeaf *newLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * (f->elementsPerLeaf)));
        if (!newLeaf) {
		
          return WONKA_FALSE;

        }
      
        CRITICAL_ENTER
	woempa(7, "inserting new leaf %p after %p\n", newLeaf, leaf);
        newLeaf->next = leaf->next;
        leaf->next = newLeaf;
        leaf = newLeaf->next;
	f->numLeaves += 1;
      }
      j = 0;
      CRITICAL_EXIT
    }
  }

  return WONKA_TRUE;

}

/**
 ** Release all memory used by this fifo. We do not attempt to make this
 ** thread-safe, it's up to the user not to release a fifo which is ib use.
 */
void releaseFifo(w_fifo f) {

  FifoLeaf *current = f->putFifoLeaf;
  FifoLeaf *last = f->putFifoLeaf;
  FifoLeaf *victim;

  do {
    victim = current;
    current = current->next;
    releaseMem(victim);
  } while (current != last);

  releaseMem(f);

}

#ifdef BAR
void dumpFifo(w_fifo f) {

  FifoLeaf *fl = f->putFifoLeaf;
  FifoLeaf *last = f->putFifoLeaf ;
  int i;

  printf("***** Dump of fifo *****\n");
  do {
    printf("\nFifoleaf %p\n", fl);
    for (i = 0; i < (int)f->elementsPerLeaf; i++) {
      printf("   %10s", ((int)f->putFifoLeaf == (int)fl && (int)f->putFifoIndex == i) ? "EQIX --> " : "         ");
      printf(" %2d [0x%08lx] ", i, (unsigned long)fl->data[i]);
      printf("%10s\n", ((int)f->getFifoLeaf == (int)fl && (int)f->getFifoIndex == (int)i) ? " <-- DQIX" : "         ");
    }
    fl = fl->next;
  } while (fl != last);
  
}
#endif

w_fifo allocFifo(w_size elementsPerLeaf) {
  w_fifo f = allocClearedMem(sizeof(w_Fifo));
  
  if (f == NULL) {
    return NULL;
  }

  f->elementsPerLeaf = elementsPerLeaf;

  f->putFifoLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * elementsPerLeaf));

  if (f->putFifoLeaf == NULL) {
    releaseMem(f);
    return NULL;
  }

  f->numLeaves = 1;
  f->getFifoLeaf = f->putFifoLeaf;
  f->putFifoLeaf->next = f->putFifoLeaf;

  return f;
   
}

#ifdef THREAD_SAFE_FIFOS
w_fifo allocThreadSafeFifo(w_size elementsPerLeaf) {
  w_fifo f = allocFifo(elementsPerLeaf);
  
  if (f == NULL) {
    return NULL;
  }

  f->mutex = allocClearedMem(sizeof(x_Mutex));
  if (!f->mutex) {
    return NULL;
  }

  x_mutex_create(f->mutex);

  return f;
   
}
#endif

void *getFifo(w_fifo f) {

  void *e;

  if (isEmptyFifo(f)) {
    return NULL;
  }

  woempa(1, "Defifoing element %p in leaf %p at position %d\n", f->getFifoLeaf->data[f->getFifoIndex], f->getFifoLeaf, f->getFifoIndex);

  e = f->getFifoLeaf->data[f->getFifoIndex++];
  f->numGot++;

  if (f->getFifoIndex == f->elementsPerLeaf) {
    CRITICAL_ENTER
    f->getFifoLeaf = f->getFifoLeaf->next;
    f->getFifoIndex = 0;
    CRITICAL_EXIT
  }

  return e;
  
}

w_int putFifo(void *e, w_fifo f) {

  woempa(1,"Enfifoing element %p in leaf %p at index %d\n", e, f->putFifoLeaf, f->putFifoIndex);

  f->putFifoLeaf->data[f->putFifoIndex++] = e;
  f->numPut++;

  if (f->putFifoIndex == f->elementsPerLeaf) {
    CRITICAL_ENTER
    if (f->getFifoLeaf != f->putFifoLeaf->next) {
      /*
      ** We can recycle a leaf; we just restart with the next leaf
      */
      f->putFifoLeaf = f->putFifoLeaf->next;
    }
    else {
      /*
      ** We link in a new leaf
      */
      CRITICAL_EXIT
      FifoLeaf *newLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * (f->elementsPerLeaf)));
      if (!newLeaf) {
        woempa(9, "No memory to add leaf to fifo %p\n", f);

        return -1;

      }
      
      CRITICAL_ENTER
      woempa(7, "current size is %d: expanding capacity from %d to %d\n", occupancyOfFifo(f), capacityOfFifo(f), capacityOfFifo(f) + f->elementsPerLeaf);
      woempa(7, "inserting new leaf %p after %p\n", newLeaf, f->putFifoLeaf);
      f->numLeaves += 1;
      newLeaf->next = f->putFifoLeaf->next;
      f->putFifoLeaf->next = newLeaf;
      f->putFifoLeaf = newLeaf;
    }
    CRITICAL_EXIT
    f->putFifoIndex = 0;
  }

  return 0;

}

w_int forEachInFifo(w_fifo f, void (*fun)(void *e)) {
  w_size i;
  FifoLeaf *currentLeaf = f->getFifoLeaf;
  w_size currentIndex = f->getFifoIndex;

  for (i = f->numGot; i < f->numPut; ++i) {
    void *next = currentLeaf->data[currentIndex++];
    if (currentIndex == f->elementsPerLeaf) {
      CRITICAL_ENTER
      currentLeaf = currentLeaf->next;
      currentIndex = 0;
      CRITICAL_EXIT
    }
    fun(next);
  }

  return occupancyOfFifo(f);
}

