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
** $Id: interrupt.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

volatile x_size irq_depth = 0x03041965; // Booting condition

volatile unsigned int system_ticks = 0;

static void x_timer_tick(x_irq irq) {

  x_pcb pcb;

  system_ticks++;

  x_assert(irq_depth == 1);

  if (critical_status == 0) {
    pcb = x_prio2pcb(thread_current->c_prio);
    pcb->revolver(pcb);
    xi_timers_tick();
    xi_pending_tick();
  }

}

static x_Irq irq_Tick = {
  x_timer_tick,
};

x_irq irq_tick = &irq_Tick;

static void irq_default_handler(x_irq irq) {
  loempa(9, "Ding dong ??\n");
}

x_irq * irq_handlers;

static x_Irq irq_Default = {
  irq_default_handler,
};

x_irq irq_default = &irq_Default;
