#ifndef _WORDSET_H
#define _WORDSET_H

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
* Modifications for Mika(TM) Copyright (c) 2004, 2005 by Chris Gray,      *
* /k/ Embedded Java Solutions, Antwerp, Belgium. All rights reserved.     *
*                                                                         *
**************************************************************************/

/*
** $Id: wordset.h,v 1.5 2006/03/22 14:59:39 cvs Exp $
*/

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
inline static w_size sizeOfWordset(w_wordset *wordset) {
  return (*wordset) ? (*wordset)->occupancy / sizeof(w_word) : 0;
}

/*
** Sometimes we only care whether a wordset is empty or not.
*/
inline static w_boolean wordsetIsEmpty(w_wordset *wordset) {
  return !(*wordset) || (*wordset)->occupancy == 0;
}

/** Release the memory used by a Wordset
** (Sets \texttt{*wordset} to \texttt{NULL}.
*/
inline static void releaseWordset(w_wordset* wordset) {
  releaseMem(*wordset);
  *wordset = NULL;
}

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

