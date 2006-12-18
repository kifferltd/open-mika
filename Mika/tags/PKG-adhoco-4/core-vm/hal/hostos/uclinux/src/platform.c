/**************************************************************************
* Copyright (c) 2001, 2003 by Acunia N.V. All rights reserved.            *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: platform.c,v 1.3 2004/11/30 15:49:26 cvs Exp $
*/

#include "oswald.h"
#include "wonka.h"
#include <stdio.h> /* For printf */
#include <signal.h>
#include <string.h>
#include <unistd.h>

void w_dump_info(void);

static struct sigaction term;
static struct sigaction quit;

int wonka_killed;
int dumping_info;

x_thread heartbeat_thread = NULL;

static void term_handler(int signum, siginfo_t * sis, void * arg) {
  wonka_killed = 1;
  if(heartbeat_thread) x_thread_priority_set(heartbeat_thread, 1);
}

static void quit_handler(int signum, siginfo_t * sis, void * arg) {
  if (dumping_info == 0) {
    dumping_info = 1;
    w_dump_info();
  }
}

void install_term_handler(void) {

  w_int result;

  term.sa_sigaction = term_handler;
  sigemptyset(&term.sa_mask);
  term.sa_flags = SA_SIGINFO;
  result = sigaction(SIGTERM, &term, NULL);
  if (result == -1) {
    woempa(9, "Registering the term handler failed.\n");
    abort();
  }

  result = sigaction(SIGINT, &term, NULL);
  if (result == -1) {
    woempa(9, "Registering the int handler failed.\n");
    abort();
  }

  quit.sa_sigaction = quit_handler;
  sigemptyset(&quit.sa_mask);
  quit.sa_flags = SA_SIGINFO;
  result = sigaction(SIGQUIT, &quit, NULL);
  if (result == -1) {
    woempa(9, "Registering the quit handler failed.\n");
    abort();
  }

}

