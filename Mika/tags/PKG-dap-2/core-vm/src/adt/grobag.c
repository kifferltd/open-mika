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

#include <string.h>

#include "grobag.h"
#include "ts-mem.h"

/** Ensure that the grobag has space for at least `capacity' bytes of contents. 
** If necessary a new grobag is created and `occupancy' and `contents' are
** copied across.
*/ 

w_boolean ensureGrobagCapacity(w_grobag *grobag, w_size capacity) {

  w_grobag oldbag = *grobag;
  w_grobag newbag;

  woempa(1, "Ensuring grobag *%p (%p) has at least capacity %d.\n", grobag, *grobag, capacity);

  if (oldbag) {
    woempa(1, "Capacity is now %d.\n", oldbag->capacity);
    if (capacity > oldbag->capacity) {
      newbag = allocClearedMem(sizeof(w_Grobag) + capacity - GROBAG_DUMMY_SIZE);
      if (!newbag) {
        woempa(9, "Unable to allocate new grobag with capacity %d bytes\n", capacity);

         return WONKA_FALSE;

      }

      woempa(1, "Reserved %d bytes\n", sizeof(w_Grobag) + capacity - GROBAG_DUMMY_SIZE);
      newbag->capacity = capacity;
      newbag->occupancy = oldbag->occupancy;
      woempa(1, "New grobag *%p is at %p, capacity %d.\n", grobag, newbag, newbag->capacity);
// TODO: we should be able to copy just oldbag->occupancy bytes.
      w_memcpy(&newbag->contents, &oldbag->contents, oldbag->capacity);
      woempa(1, "Copied %d bytes from %p to %p\n", newbag->capacity, &oldbag->contents, &newbag->contents);
      *grobag = newbag;
      releaseMem(oldbag);
    }
    else {
      woempa(1,"No need to grow\n");
    }
  }
  else {
    newbag = allocClearedMem(sizeof(w_Grobag) + capacity - GROBAG_DUMMY_SIZE);
    if (!newbag) {
      woempa(9, "Unable to allocate new grobag with capacity %d bytes\n", capacity);

       return WONKA_FALSE;

    }

    woempa(1,"New Grobag: reserved %d bytes\n",sizeof(w_Grobag)+capacity-GROBAG_DUMMY_SIZE);
    newbag->capacity = capacity;
    newbag->occupancy = 0;
    woempa(1, "New grobag *%p is at %p, capacity %d.\n", grobag, newbag, newbag->capacity); 
    *grobag = newbag;
  }

  return WONKA_TRUE;
}
  
/** Add nbytes of data at the ``end'' of a Grobag.
** The ``end'' is the point in grobag->contents indicated by grobag->occupancy. 
** We call ensureGrobagCapacity first.
** Returns WONKA_TRUE if successful, WONKA_FALSE otherwise.
*/
w_boolean appendToGrobag(w_grobag *grobag, void * data, w_size nbytes) {

  woempa(1, "Appending %d bytes to grobag #%p (%p).\n", nbytes, grobag, *grobag);

  if (ensureGrobagCapacity(grobag, ((*grobag) ? (*grobag)->occupancy + nbytes : nbytes))) {
    w_memcpy(&(*grobag)->contents[(*grobag)->occupancy], data, nbytes);
    woempa(1, "Copied %d bytes from %p to %p\n", nbytes, data, &(*grobag)->contents[(*grobag)->occupancy]);
    (*grobag)->occupancy += nbytes;
  
    return WONKA_TRUE;
  }

  return WONKA_FALSE;
}


