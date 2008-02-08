#ifndef _LIST_H
#define _LIST_H

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
** $Id: list.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

/*
** Macros to manipulate doubly linked lists. These macro's require an
** immutable first sentinel entry that is setup by means of the list_init
** macro.
**
** In the following macro's, 'f' refers to this first sentinel entry and 'x'
** refers to the to be manipulated list entry.
**
** These macros will only compile if the 'f' and 'l' types passed are pointers 
** to structures that contain a literal 'next' and 'previous' structure element.
*/

#define list_remove(x) {                               \
  (x)->previous->next = (x)->next;                     \
  (x)->next->previous = (x)->previous;                 \
}

#define list_prune_from(f, x) {                        \
  (x)->next = (f);                                     \
  (f)->previous = (x);                                 \
}

/*
** Note that in the following macro, order is important. We run over
** lists via the next pointer so we want the next to be setup
** correctly before we insert the item.
*/

#define list_insert(f, x) {                            \
  (x)->next = (f);                                     \
  (x)->previous = (f)->previous;                       \
  (f)->previous->next = (x);                           \
  (f)->previous = (x);                                 \
}

#define list_init(f) {                                 \
  (f)->next = (f);                                     \
  (f)->previous = (f);                                 \
}

/*
** The following macros are for convenience in going over a doubly linked list.
** They are called in a for loop like this:
**
** for (pointer = list_oldest(list); pointer; pointer = list_next(list, pointer)) {
**   ...
** }
**
** Where 'list' is the sentinel or the first immutable (dummy) entry of the list.
** The next and previous macros will return NULL when the end or the begin of the
** list is reached. They will not give you the sentinel.
*/

#define list_next(f, x)                                ((x)->next != (f) ? (x)->next : NULL)
#define list_previous(f, x)                            ((x)->previous != (f) ? (x)->previous : NULL)
#define list_youngest(f)                               ((f)->previous != (f) ? (f)->previous : NULL)
#define list_oldest(f)                                 ((f)->next != (f) ? (f)->next : NULL)

#endif /* _LIST_H */
