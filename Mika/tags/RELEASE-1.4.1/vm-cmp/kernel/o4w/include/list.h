#ifndef _LIST_H
#define _LIST_H

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
