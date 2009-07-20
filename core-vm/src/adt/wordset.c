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

#include "grobag.h"
#include "ts-mem.h"
#include "wordset.h"
#include "wonka.h"

/** Add a word to a wordset, after expanding it if necessary.
    Duplicates may be created.
*/
w_boolean addToWordset(w_wordset* wordset, w_word what) {
  register w_word *w;
  w_int   i;
  w_boolean success;

  if (*wordset) {
    i = ((*wordset)->occupancy) / sizeof(w_word);
    success = ensureWordsetCapacity(wordset, i+1);
  }
  else {
    i = 0;
    success = ensureWordsetCapacity(wordset, 1);
  }

  if (success) {
    w = (w_word*)((*wordset)->contents);
    w[i] = what;
    (*wordset)->occupancy = (i+1) * sizeof(w_word);
  }

  return success;
}

/** Remove first word from a waitset.  Returns the word or 0 if the set was empty.
*/
w_word takeFirstFromWordset(w_wordset* wordset) {
  if (*wordset && (*wordset)->occupancy) {
    w_word *w = (w_word*)((*wordset)->contents);
    w_int   n = ((*wordset)->occupancy) / sizeof(w_word);
    w_word result;

    woempa(1,"Wordset %p contains %d items\n",*wordset,(*wordset)->occupancy / sizeof(w_word));
    result = w[0];
    woempa(1,"Removed front element 0x%08x\n",result);
    if (n > 1) {
      w[0] = w[n-1];
      woempa(1,"  -- replaced with 0x%08x\n",w[0]);
    }
    (*wordset)->occupancy -= sizeof(w_word);

    return result;

  }
  else {

    return 0;

  }
}

/** Look for a word in a wordset.  Returns TRUE iff the word was found, FALSE otherwise.
*/
w_boolean isInWordset(w_wordset* wordset, w_word what) {
  w_word *w;
  w_size  i;

  if (*wordset == NULL) {

    return WONKA_FALSE;

  }

  w = (w_word*)((*wordset)->contents);
  for (i = 0; i < ((*wordset)->occupancy) / sizeof(w_word); ++i) {
    if (w[i] == what) {

      return WONKA_TRUE;

    }
  }

  return WONKA_FALSE;
}

/** Remove a word from a wordset, if it is present.  Returns TRUE iff the word was found.
*/
w_boolean removeFromWordset(w_wordset* wordset, w_word what) {
  w_word *w = (w_word*)((*wordset)->contents);
  w_int   n = ((*wordset)->occupancy) / sizeof(w_word);
  w_int   i;

  for (i=0; i < n; ++i) {
    if (w[i] == what) {
      w[i] = w[n-1];
      (*wordset)->occupancy -= sizeof(w_word);

      return WONKA_TRUE;

    }
  }

  return WONKA_FALSE;
}

/**
 ** Returns the i'th element of a wordset.
 */
w_word elementOfWordset(w_wordset* wordset, w_int i) {
  w_word *w = (w_word*)((*wordset)->contents);
#ifdef RUNTIME_CHECKS
  w_int   n = *wordset ? ((*wordset)->occupancy) / sizeof(w_word) : 0;
  if (i >= n) {
    wabort(ABORT_WONKA, "Attempt to access element[%d] of wordset of size %d\n", i, n);
  }
#endif

  return w[i];
}

/**
 ** Modify the i'th element of a wordset.
 */
void modifyElementOfWordset(w_wordset* wordset, w_int i, w_word val) {
  w_word *w = (w_word*)((*wordset)->contents);
#ifdef RUNTIME_CHECKS
  w_int   n = *wordset ? ((*wordset)->occupancy) / sizeof(w_word) : 0;
  if (i >= n) {
    wabort(ABORT_WONKA, "Attempt to access element[%d] of wordset of size %d\n", i, n);
  }
#endif

  w[i] = val;
}
/** Remove a the n'th element from a wordset.
*/
void removeWordsetElementAt(w_wordset* wordset, w_int i) {
  w_word *w = (w_word*)((*wordset)->contents);
#ifdef RUNTIME_CHECKS
  w_int   n = *wordset ? ((*wordset)->occupancy) / sizeof(w_word) : 0;
  if (i >= n) {
    wabort(ABORT_WONKA, "Attempt to remove element[%d] from wordset of size %d\n", i, n);
  }
#else
  w_int   n = ((*wordset)->occupancy) / sizeof(w_word);
#endif

  w[i] = w[n-1];
  (*wordset)->occupancy -= sizeof(w_word);
}

/** Sort a Wordset. Will need to be re-sorted if anything is added or removed.
*/
void sortWordset(w_wordset* wordset) {
  w_word *w = (w_word*)((*wordset)->contents);
  w_int   n = (*wordset)->occupancy;
  w_int   i;
  w_int   j;

  for (i = 0; i < n; ++i) {
    for (j = i+1; j < n; ++j) {
      if (w[j] < w[i]) {
        w_word x = w[i];
        w[i] = w[j];
        w[j] = x;
      }
    }
  }
}

/*
** Take the last word from a wordset (must not be empty!).
** Leaves the other elements undisturbed.
*/
w_word takeLastFromWordset(w_wordset* wordset) {
  w_word *w = (w_word*)((*wordset)->contents);
  w_word result = w[sizeOfWordset(wordset) - 1];

  (*wordset)->occupancy -= sizeof(w_word);

  return result;
}

/*
** Find out how many items are in a wordset.
*/
w_size sizeOfWordset(w_wordset *wordset) {
  return (*wordset) ? (*wordset)->occupancy / sizeof(w_word) : 0;
}
