/*

Copyright (c) 1990, 1994  X Consortium

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of the X Consortium shall not be
used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from the X Consortium.

*/

/*
 * Author:  Keith Packard, MIT X Consortium
 */

/**************************************************************************
* Portions Copyright  (c) 2002 by Acunia N.V. All rights reserved.        *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
* distribution or disclosure of this software is expressly forbidden.     *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5 box 3        info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* $Id: atom.c,v 1.3 2005/06/14 10:04:00 cvs Exp $ */

/* lame atom replacement routines for font applications */

#include <string.h>
#include "ts-mem.h"       // allocMem(), releaseMem()
#include "fontmisc.h"
#include "atom.h"


typedef struct _AtomList {
    char*		name;
    w_int		len;
    w_int		hash;
    Atom		atom;
} AtomListRec, *AtomListPtr;

static AtomListPtr  *hashTable;

static w_int	    hashSize, hashUsed;
static w_int	    hashMask;
static w_int	    rehash;

static AtomListPtr  *reverseMap;
static w_word	    reverseMapSize;
static Atom	    lastAtom;

static  w_int
Hash(
    char* string,
    w_int len
    )
{
    w_int	h;

    h = 0;
    while (len--)
	h = (h << 3) ^ *string++;
    if (h < 0)
	return -h;
    return h;
}

static w_int
ResizeHashTable (void)
{
    w_int		newHashSize;
    w_int		newHashMask;
    AtomListPtr	*newHashTable;
    w_int		i;
    w_int		h;
    w_int		newRehash;
    w_int		r;

    if (hashSize == 0)
	newHashSize = 1024;
    else
	newHashSize = hashSize * 2;
    newHashTable = (AtomListPtr *) allocClearedMem (newHashSize * sizeof (AtomListPtr));
    if (!newHashTable)
	return WONKA_FALSE;
    bzero ((char *) newHashTable, newHashSize * sizeof (AtomListPtr));
    newHashMask = newHashSize - 1;
    newRehash = (newHashMask - 2);
    for (i = 0; i < hashSize; i++)
    {
	if (hashTable[i])
	{
	    h = (hashTable[i]->hash) & newHashMask;
	    if (newHashTable[h])
	    {
		r = hashTable[i]->hash % newRehash | 1;
		do {
		    h += r;
		    if (h >= newHashSize)
			h -= newHashSize;
		} while (newHashTable[h]);
	    }
	    newHashTable[h] = hashTable[i];
	}
    }
    if (hashTable)
      releaseMem (hashTable);
    hashTable = newHashTable;
    hashSize = newHashSize;
    hashMask = newHashMask;
    rehash = newRehash;
    return WONKA_TRUE;
}

static w_int
ResizeReverseMap (void)
{
    if (reverseMapSize == 0)
	reverseMapSize = 1000;
    else
	reverseMapSize *= 2;
    if (reverseMap) {
      reverseMap = (AtomListPtr *) reallocMem (reverseMap, reverseMapSize * sizeof (AtomListPtr));
    }
    else {
      reverseMap = (AtomListPtr *) allocMem (reverseMapSize * sizeof (AtomListPtr));
    }

    if (!reverseMap) {
	return WONKA_FALSE;
    }

    return WONKA_TRUE;
}

static w_int
NameEqual (
    char* a,
    char* b,
    w_int l
    )
{
    while (l--)
	if (*a++ != *b++)
	    return WONKA_FALSE;
    return WONKA_TRUE;
}

Atom
MakeAtom(
    char* string,
    w_int len,
    w_int makeit
)
{
    AtomListPtr	a;
    w_int	hash;
    w_int	h = 0;
    w_int	r;

    hash = Hash (string, len);
    if (hashTable)
    {
    	h = hash & hashMask;
	if (hashTable[h])
	{
	    if (hashTable[h]->hash == hash && hashTable[h]->len == len &&
	    	NameEqual (hashTable[h]->name, string, len))
	    {
	    	return hashTable[h]->atom;
	    }
	    r = (hash % rehash) | 1;
	    for (;;)
	    {
		h += r;
		if (h >= hashSize)
		    h -= hashSize;
		if (!hashTable[h])
		    break;
		if (hashTable[h]->hash == hash && hashTable[h]->len == len &&
		    NameEqual (hashTable[h]->name, string, len))
		{
		    return hashTable[h]->atom;
		}
	    }
    	}
    }
    if (!makeit)
	return None;
    a = (AtomListPtr) allocMem (sizeof (AtomListRec) + len + 1);
    if (!a) {
      return None;
    }

    a->name = (char *) (a + 1);
    a->len = len;
    strncpy (a->name, string, len);
    a->name[len] = '\0';
    a->atom = ++lastAtom;
    a->hash = hash;
    if (hashUsed >= hashSize / 2)
    {
	ResizeHashTable ();
	h = hash & hashMask;
	if (hashTable[h])
	{
	    r = (hash % rehash) | 1;
	    do {
		h += r;
		if (h >= hashSize)
		    h -= hashSize;
	    } while (hashTable[h]);
	}
    }
    hashTable[h] = a;
    hashUsed++;
    if (reverseMapSize <= a->atom)
	ResizeReverseMap();
    reverseMap[a->atom] = a;
    return a->atom;
}

w_int
ValidAtom(
    Atom atom
)
{
    return (atom != None) && (atom <= lastAtom);
}

char *
NameForAtom(
    Atom atom
)
{
    if (atom != None && atom <= lastAtom)
	return reverseMap[atom]->name;
    return 0;
}
