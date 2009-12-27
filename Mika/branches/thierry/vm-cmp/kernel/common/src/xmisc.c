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
** $Id: xmisc.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include "oswald.h"

static const char * _status2char[] = {
  "success",                      /*  0 */
  "no instance",                  /*  1 */
  "not owner",                    /*  2 */
  "deadlock",                     /*  3 */
  "bad context",                  /*  4 */
  "bad option",                   /*  5 */
  "deleted",                      /*  6 */
  "bad element",                  /*  7 */
  "threads waiting",              /*  8 */
  "incomplete",                   /*  9 */
  "tick error",                   /* 10 */
  "timer error",                  /* 11 */
  "activation error",             /* 12 */
  "bad state",                    /* 13 */
  "insufficient memory",          /* 14 */
  "bad argument",                 /* 15 */
  "still owns events",            /* 16 */
  "is not ELF file",              /* 17 */
  "module is in error",           /* 18 */
  "module sequence error",        /* 19 */
  "no more memory",               /* 20 */
  "relocation failed",            /* 21 */
  "symbol multiply defined",      /* 22 */
  "unresolved symbols remain",    /* 23 */
  "operation deferred",           /* 24 */
  "interrupted operation",        /* 25 */
  "(unknown)",                    /* 26 */
};

const char * x_status2char(x_status status) {
  return _status2char[(status >= xs_unknown) ? xs_unknown : status];
}
