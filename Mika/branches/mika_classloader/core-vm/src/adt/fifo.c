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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: fifo.c,v 1.4 2004/11/30 11:12:08 cvs Exp $
*/

#include "fifo.h"
#include "ts-mem.h"

extern w_fifo window_fifo;

/* returns number of bytes saved during contraction */

int contractFifo(w_fifo f) {
  FifoLeaf *current = f->putFifoLeaf;
  FifoLeaf *next = current->next;
  int released = 0;

  woempa(1, "Fifo %p has putFifoLeaf %p, getFifoLeaf %p\n", f, f->putFifoLeaf, f->getFifoLeaf);

  while (f->getFifoLeaf != next) {
    woempa(1, "current is %p, next is %p\n", current, next);
    woempa(1, "making current->next point to next->next (%p)\n", next->next);
    current->next = next->next;
    releaseMem(next);
    released += sizeof(FifoLeaf) + (sizeof(void *) * (f->leafElements));
    woempa(1, "releasing %p, freed %d bytes so far\n", next, released);
    current = current->next;
    next = current->next;
  }

  return released;

}

/* returns number of bytes saved during reduction */

int reduceFifo(w_fifo f) {
  FifoLeaf *current = f->putFifoLeaf;
  FifoLeaf *next = current->next;
  int released = 0;

  woempa(1, "Fifo %p has putFifoLeaf %p, getFifoLeaf %p\n", f, f->putFifoLeaf, f->getFifoLeaf);

  if (f->getFifoLeaf != next) {
    woempa(1, "current is %p, next is %p\n", current, next);
    woempa(1, "making current->next point to next->next (%p)\n", next->next);
    current->next = next->next;
    releaseMem(next);
    released = sizeof(FifoLeaf) + (sizeof(void *) * (f->leafElements));
    woempa(1, "releasing %p, freed %d bytes\n", next, released);
  }

  return released;

}

w_boolean expandFifo(w_size newsize, w_fifo f) {
  unsigned int i;
  unsigned int j = f->putFifoIndex;
  FifoLeaf *leaf = f->putFifoLeaf;

  if (newsize <= f->numLeaves * f->leafElements) {

    return WONKA_TRUE;

  }

  woempa(1, "current size is %d: expanding capacity from %d to %d\n", f->numElements, f->numLeaves * f->leafElements, newsize);
  for (i = f->numElements; i < newsize; ++i) {
    ++j;
    if (j == f->leafElements) {
      if (leaf->next != f->getFifoLeaf) {
        leaf = leaf->next;
      }
      else {
        FifoLeaf *newLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * (f->leafElements)));
        if (!newLeaf) {
		
          return WONKA_FALSE;

        }
      
	woempa(1, "inserting new leaf %p after %p\n", newLeaf, leaf);
        newLeaf->next = leaf->next;
        leaf->next = newLeaf;
        leaf = newLeaf->next;
	f->numLeaves += 1;
      }
      j = 0;
    }
  }

  return WONKA_TRUE;

}

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
// NO PRINTF !!
void dumpFifo(w_fifo f) {

  FifoLeaf *fl = f->putFifoLeaf;
  FifoLeaf *last = f->putFifoLeaf ;
  int i;

  printf("***** Dump of fifo *****\n");
  do {
    printf("\nFifoleaf %p\n", fl);
    for (i = 0; i < (int)f->leafElements; i++) {
      printf("   %10s", ((int)f->putFifoLeaf == (int)fl && (int)f->putFifoIndex == i) ? "EQIX --> " : "         ");
      printf(" %2d [0x%08lx] ", i, (unsigned long)fl->data[i]);
      printf("%10s\n", ((int)f->getFifoLeaf == (int)fl && (int)f->getFifoIndex == (int)i) ? " <-- DQIX" : "         ");
    }
    fl = fl->next;
  } while (fl != last);
  
}
#endif

w_fifo allocFifo(w_size leafElements) {

  /* XXX */
  w_fifo f = allocClearedMem(sizeof(w_Fifo));
  
  if (f == NULL) {
    return NULL;
  }

  if (leafElements == 0) {
    f->leafElements = FIFO_DEFAULT_LEAF_SIZE;
  }
  else {
    f->leafElements = leafElements;
  }

  /* XXX */
  f->putFifoLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * (f->leafElements)));

  if (f->putFifoLeaf == NULL) {
    releaseMem(f);
    return NULL;
  }

  f->numLeaves = 1;
  f->getFifoLeaf = f->putFifoLeaf;
  f->putFifoLeaf->next = f->putFifoLeaf;

  return f;
   
}

void *getFifo(w_fifo f) {

  void *e;

  if (f->numElements == 0) {
    return NULL;
  }

  woempa(1, "Defifoing element %p in leaf %p at position %d\n", f->getFifoLeaf->data[f->getFifoIndex], f->getFifoLeaf, f->getFifoIndex);

  e = f->getFifoLeaf->data[f->getFifoIndex++];
  f->numElements--;
  if (f == window_fifo) woempa(1, "window_fifo now contains %d elements\n", f->numElements);

  if (f->getFifoIndex == f->leafElements) {
    f->getFifoLeaf = f->getFifoLeaf->next;
    f->getFifoIndex = 0;
  }

  return e;
  
}

w_int putFifo(void *e, w_fifo f) {

  woempa(1,"Enfifoing element %p in leaf %p at index %d\n", e, f->putFifoLeaf, f->putFifoIndex);

  f->putFifoLeaf->data[f->putFifoIndex++] = e;
  f->numElements++;
  if (f == window_fifo) woempa(1, "window_fifo now contains %d elements\n", f->numElements);

  if (f->putFifoIndex == f->leafElements) {
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
      FifoLeaf *newLeaf = allocClearedMem(sizeof(FifoLeaf) + (sizeof(void *) * (f->leafElements)));
      if (!newLeaf) {
        woempa(9, "No memory to add leaf to fifo %p\n", f);

        return -1;

      }
      
      woempa(1, "current size is %d: expanding capacity from %d to %d\n", f->numElements, f->numLeaves * f->leafElements, (f->numLeaves + 1) * f->leafElements);
      woempa(1, "inserting new leaf %p after %p\n", newLeaf, f->putFifoLeaf);
      f->numLeaves += 1;
      newLeaf->next = f->putFifoLeaf->next;
      f->putFifoLeaf->next = newLeaf;
      f->putFifoLeaf = newLeaf;
    }
    f->putFifoIndex = 0;
  }

  return 0;

}

w_int forEachInFifo(w_fifo f, void (*fun)(void *e)) {
  w_size i;
  w_size n = f->numElements;
  FifoLeaf *currentLeaf = f->getFifoLeaf;
  w_size currentIndex = f->getFifoIndex;

  for (i = 0; i < n; ++i) {
    void *next = currentLeaf->data[currentIndex++];
    if (currentIndex == f->leafElements) {
      currentLeaf = currentLeaf->next;
      currentIndex = 0;
    }
    fun(next);
  }

  return n;
}

