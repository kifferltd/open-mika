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


