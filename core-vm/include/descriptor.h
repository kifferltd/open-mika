/**************************************************************************
* Copyright (c) 2001, 2002 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2021 by Chris Gray, Kiffer Ltd.                     *
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
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, KIFFER LTD, OR OTHER CONTRIBUTORS    *
* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR  *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _DESCRIPTOR_H
#define _DESCRIPTOR_H

#include "hashtable.h"
#include "wonka.h"

extern const char primitive2char[];
extern const char *primitive2name[];
extern w_clazz primitive2clazz[];
extern w_instance primitive2classInstance[];
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
