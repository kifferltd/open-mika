#ifndef _VSNPRINTF_H
#define _VSNPRINTF_H

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
