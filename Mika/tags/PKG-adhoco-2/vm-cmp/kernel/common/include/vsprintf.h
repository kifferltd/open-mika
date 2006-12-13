#ifndef _VSNPRINTF_H
#define _VSNPRINTF_H

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
** The flags that can be used within the formatter functions.
*/

#define FMT_PAD_ZERO   0x00000001 /* Pad field with zeros. */
#define FMT_SIGN       0x00000002 /*    */
#define FMT_PLUS       0x00000004 /* Show the sign, even if positive. */
#define FMT_SPACE      0x00000008 /* space if plus */
#define FMT_ALIGN_LEFT 0x00000010 /* left justified */
#define FMT_SPECIAL    0x00000020 /*    */
#define FMT_CAPITALS   0x00000040 /* Print hexadecimal in capital characters. */
#define FMT_LONG       0x00000200 /* Argument is a long argument. */

/*
** Format routines, first we define the type for the format callback function.
*/

typedef char * (*x_fcb)(char * buffer, int * remain, void * arg, int width, int prec, unsigned int flags);

x_int x_vsnprintf(char * buf, x_size bufsize, const char *fmt, va_list args);
x_int x_snprintf(char * buf, x_size bufsize, const char *fmt, ...);
x_boolean x_formatter(x_int specifier, x_fcb fcb);

#endif
