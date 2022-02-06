/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2010 by Chris Gray,         *
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

#ifndef _WORDSET_H
#define _WORDSET_H

#include "grobag.h"

/*
** A Wordset is an expandable array of words, in no particular order.
** 
** By "in no particular order" we mean that some of the functions below
** may result in a re-ordering of the wordset outside of the user's
** control, so the user should never assume that a particular word
** will remain at a particular offset.  There is one important exception:
** when only the functions addToWordset and takeLastFromWordet are used
** (in a balanced way), the wordset behaves as a LIFO stack.
**
** A \texttt{w_Wordset} is really just a \texttt{w_Grobag} used in a
** particular way.  Like the \texttt{\w_Grobag} operations, all operations
** on a wordset take a *w_wordset (i.e. a **w_Wordset) as parameter;
** this is because the w_wordset pointer will be updated when new space is
** allocated.
*/

#define w_Wordset w_Grobag
typedef struct w_Wordset *w_wordset;

/** We always allocate a multiple of WORDSET_SIZE_GRANULARITY words.
*/
#define WORDSET_SIZE_GRANULARITY 16

/** Reserve space for at least 'newsize' words of contents.
**
** If \texttt{wordsetptr} is currently \texttt{NULL}, allocates a new Wordset.
*/
#define ensureWordsetCapacity(wordsetptr,newsize) ensureGrobagCapacity(wordsetptr,((((newsize)+WORDSET_SIZE_GRANULARITY-1) & (-WORDSET_SIZE_GRANULARITY)) * sizeof(w_word)))

/*
** Find out how many items are in a wordset.
*/
w_size sizeOfWordset(w_wordset *wordset);

/*
** Sometimes we only care whether a wordset is empty or not.
*/
inline static w_boolean wordsetIsEmpty(w_wordset *wordset) {
  return !wordset || !(*wordset) || !(*wordset)->occupancy;
}

/** Release the memory used by a Wordset
** (Sets \texttt{*wordset} to \texttt{NULL}.
*/
extern void releaseWordset(w_wordset* wordset);

/** Add a word to a wordset, after expanding it if necessary.
    Duplicates may be created.
*/
extern w_boolean addToWordset(w_wordset* wordset, w_word what);

/** Remove first word from a waitset.  Returns the word or NULL.
*/
extern w_word takeFirstFromWordset(w_wordset* wordset);

/** Look for a word in a wordset.  Returns TRUE iff the word was found, FALSE otherwise.
*/
extern w_boolean isInWordset(w_wordset* wordset, w_word what);

/*
** Take the last word from a wordset (must not be empty!).
** Leaves the other elements undisturbed.
*/
extern w_word takeLastFromWordset(w_wordset* wordset);

/** Sort a Wordset. Will need to be re-sorted if anything is added or removed.
*/
extern void sortWordset(w_wordset* wordset);

/** Remove a the n'th element from a wordset.
*/
extern void removeWordsetElementAt(w_wordset* wordset, w_int i);

/** Remove a word from a wordset, if it is present.  Returns TRUE iff the word was found.
*/
extern w_boolean removeFromWordset(w_wordset* wordset, w_word what);

/**
 ** Return the i'th element of a wordset. Abort if i out of range!
 */
extern w_word elementOfWordset(w_wordset* wordset, w_int i);

/**
 ** Modify the i'th element of a wordset.
 */
extern void modifyElementOfWordset(w_wordset* wordset, w_int i, w_word val);

#endif /* _WORDSET_H */

