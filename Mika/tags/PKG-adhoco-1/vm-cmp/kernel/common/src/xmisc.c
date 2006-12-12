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
