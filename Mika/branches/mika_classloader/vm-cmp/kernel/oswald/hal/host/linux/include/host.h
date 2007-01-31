#ifndef _HOST_H
#define _HOST_H

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
** $Id: host.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** Linux host specific parts.
*/

#include <types.h>

/*
** For linux, we only have dummy irqs
*/

#define IRQ_MAX            1      /* The number of IRQ's we have for this host         */
#define IRQ_TICK           0      /* The index for the TIMER TICK IRQ                  */

void x_host_setup(void);
void x_irqs_setup(void);

extern x_ubyte * host_memory_end;

x_size x_millis2ticks(x_size millis);
x_size x_seconds2ticks(x_size seconds);
x_size x_ticks2usecs(x_size ticks);
x_size x_usecs2ticks(x_size usecs);

void x_host_post(void);

x_ubyte * x_host_sbrk(x_int bytes);
 
void x_host_break(x_ubyte * memory);
     
#endif /* _HOST_H */
