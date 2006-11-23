#ifndef _PROCESSOR_H
#define _PROCESSOR_H

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
* Modifications Copyright (C) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions.  All rights reserved.                                        *
*                                                                         *
**************************************************************************/

/*
** $Id: processor.h,v 1.5 2005/10/24 14:05:45 cvs Exp $
** 
** The Wonka kernel is software copyright by SmartMove NV (1999).
** Please see the file Copyright for information on it's legal use.
** 
*/

/*
** Machine dependent stuff for the StrongARM Architecture
*/

#include <sys/types.h>

#ifndef __BIT_TYPES_DEFINED__
#error You have work to do here! See wonka/include/wonka.h.
#endif

/*
** A double word quantity is represented as an array of two 32 bit words. 
** LSW(l) and MSW(l) return the lower 32 bits and the upper 32 bits of a 64 bit quantity according
** to the settings of the MS_WORD and LS_WORD parameters. Our Wonka machine always uses these
** macros to get at double word values (be it floating point doubles or long integers).
**
*/

#ifndef __LITTLE_ENDIAN
#define __LITTLE_ENDIAN 1234
#endif

#ifndef __BYTE_ORDER
#define __BYTE_ORDER __LITTLE_ENDIAN
#endif

#define WORDS2LONG(m, l)             (w_long)(((w_long)(m) << 32) | (l))

#define WORD_MSW                       1
#define WORD_LSW                       0

#endif /* _PROCESSOR_H */
