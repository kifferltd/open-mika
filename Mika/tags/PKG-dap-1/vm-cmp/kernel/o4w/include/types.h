#ifndef _TYPES_H
#define _TYPES_H

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
** $Id: types.h,v 1.2 2005/01/06 21:56:38 cvs Exp $
**
** The primitive types for an X86 CPU with the GCC compiler.
*/

typedef unsigned int           x_size;
typedef unsigned int           x_word;
typedef unsigned int           x_uword;
typedef int                    x_int;
typedef unsigned char *        x_code;
typedef unsigned int *         x_instance;
typedef signed __int64         x_long;
typedef unsigned char          x_ubyte;
typedef signed char            x_sbyte;
typedef unsigned short         x_ushort;
typedef signed short           x_short;
typedef unsigned __int64	   x_ulong;
typedef void *                 x_fun;
typedef unsigned short         x_char;
typedef signed int             x_address;
typedef DWORD				   x_dword;

#ifndef NULL
#define NULL ((void *)0)
#endif

#endif /* _TYPES_H */
