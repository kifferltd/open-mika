#ifndef _DESCRIPTOR_H
#define _DESCRIPTOR_H

/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: descriptor.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

#include "hashtable.h"
#include "wonka.h"

extern const char primitive2char[];
extern const char *primitive2name[];
extern w_clazz primitive2clazz[];
extern const unsigned char primitive2bits[];
extern const unsigned char primitive2words[];
extern w_clazz primitive2wrapper[];
extern w_size primitive2wrapperSlot[];

/*
** Parse a descriptor (sub)string for the first class descriptor it contains,
** returning a pointer to a w_Clazz or w_UnloadedClazz.  The substring parsed
** starts at string->chars[*start] and ends just before string->char[end];
** after parsing, *start will index the first character of the next descriptor
** in the string or be equal to end if there is none.
** 
** If parsing fails, NULL is returned and *start is undefined.
*/
w_clazz parseDescriptor(w_string descriptor, w_size *start, w_size end, w_instance loader);

/*
** clazz2desc yields a registered string which contains the descriptor for a clazz.
*/
w_string clazz2desc(w_clazz clazz);

/*
** Print function associated with the %y format of x_vsnprintf: takes a w_clazz
** and prints it as a descriptor string, e.g. "Z" or "Ljava/lang/Object;".
*/
char * print_descriptor(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

#endif /* _DESCRIPTOR_H */
