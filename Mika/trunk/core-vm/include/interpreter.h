#ifndef _INTERPRETER_H
#define _INTERPRETER_H

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
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: interpreter.h,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "wonka.h"

w_frame pushFrame(w_thread thread, w_method method);
w_frame activateFrame(w_thread thread, w_method method, w_word flags, w_int nargs, ...);
void deactivateFrame(w_frame frame, w_instance protect);

/*
** Helper structure for match offset pair lookup in lookupswitch; 
** this structure is also used in the method code rewritting routine.
*/

typedef struct w_Mopair {
  w_int m;
  w_int o;
} w_Mopair;

void initialize_bytecode_dispatcher(w_frame caller, w_method method);

void interpret(w_frame caller, w_method method);

#endif /* _INTERPRETER_H */
