#ifndef _EXEC_H
#define _EXEC_H

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
** $Id: exec.h,v 1.4 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "wonka.h"

#define EXECUTION_ERROR -2
#define EXECUTION_ENDED -3


w_void* host_exec(char **cmd, char **env, char *path, w_int* pid);

w_int   host_wait_for_all(w_int* retval);
w_int   host_available_in(w_void* pid);
w_int   host_available_err(w_void* pid);
w_int   host_read_in(w_void* pid, char* array, w_int len);
w_int   host_read_err(w_void* pid, char* array, w_int len);
w_int   host_write(w_void* pid, w_ubyte* bytes, w_int l);
w_int   host_getretval(w_void* pid);

w_void  host_setreturnvalue(w_void* pid, int retval);
w_void  host_destroy(w_void* pid);
w_void  host_close_in(w_void* pid);
w_void  host_close_out(w_void* pid);
w_void  host_close_err(w_void* pid);
w_void  host_close(w_void* pid);

char*   host_getCommandPath(void);
#endif /* _EXEC_H */
