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
** $Id: platform.c,v 1.5 2005/11/03 11:19:49 cvs Exp $
*/

#include "oswald.h"
#include "wonka.h"
#include <stdio.h> /* For printf */
#include <signal.h>
#include <string.h>
#include <unistd.h>

void w_dump_info(void);

static struct sigaction term_action;
static struct sigaction term_oldaction;
static struct sigaction int_action;
static struct sigaction int_oldaction;
static struct sigaction quit_action;
static struct sigaction quit_oldaction;
#ifdef O4P
static struct sigaction segv_action;
static struct sigaction segv_oldaction;
#endif

int wonka_killed;
int dumping_info;

x_thread heartbeat_thread = NULL;

static void term_handler(int signum) {
  wonka_killed = 1;
  //if(heartbeat_thread) x_thread_priority_set(heartbeat_thread, 1);
}

static void int_handler(int signum) {
  wonka_killed = 1;
  //if(heartbeat_thread) x_thread_priority_set(heartbeat_thread, 1);
}

static void quit_handler(int signum) {
  if (dumping_info == 0) {
    dumping_info = 1;
    w_dump_info();
  }
}

#ifdef O4P
static void segv_handler(int signum, siginfo_t * sis, void * arg) {
  printf("\n\r\n*** SEGMENTATION FAULT ***\n\r\n");
  printf("signo = %d code = %d addr = %p\n\r\n", sis->si_signo, sis->si_code, sis->si_addr);
  w_dump_info();
  abort();
}
#endif

void install_term_handler(void) {

  w_int result;

  term_action.sa_handler = term_handler;
  sigemptyset(&term_action.sa_mask);
  term_action.sa_flags = 0;
  result = sigaction(SIGTERM, &term_action, &term_oldaction);
  if (result == -1) {
    wprintf("Registering the term handler failed, return code = %d.\n", result);
    abort();
  }
  if (term_oldaction.sa_handler == SIG_DFL) {
     woempa(7, "Old term handler was SIG_DFL\n");
  }
  else if (term_oldaction.sa_handler == SIG_IGN) {
    woempa(7, "Old term handler was SIG_IGN, re-instating it\n");
    sigaction(SIGTERM, &term_oldaction, NULL);
  }
  else {
    woempa(7, "Old term handler was %p\n", term_oldaction.sa_handler);
  }

  int_action.sa_handler = int_handler;
  sigemptyset(&int_action.sa_mask);
  int_action.sa_flags = 0;
  result = sigaction(SIGINT, &int_action, &int_oldaction);
  if (result == -1) {
    wprintf("Registering the int handler failed, return code = %d.\n", result);
    abort();
  }
  if (int_oldaction.sa_handler == SIG_DFL) {
    woempa(7, "Old int handler was SIG_DFL\n");
  }
  else if (int_oldaction.sa_handler == SIG_IGN) {
    woempa(7, "Old int handler was SIG_IGN, re-instating it\n");
    sigaction(SIGINT, &int_oldaction, NULL);
  }
  else {
    woempa(7, "Old int handler was %p\n", int_oldaction.sa_handler);
  }

  quit_action.sa_handler = quit_handler;
  sigemptyset(&quit_action.sa_mask);
  quit_action.sa_flags = 0;
  result = sigaction(SIGQUIT, &quit_action, &quit_oldaction);
  if (result == -1) {
    printf("Registering the quit handler failed, return code = %d.\n", result);
    abort();
  }
  if (quit_oldaction.sa_handler == SIG_DFL) {
    woempa(7, "Old quit handler was SIG_DFL\n");
  }
  else if (quit_oldaction.sa_handler == SIG_IGN) {
    woempa(7, "Old quit handler was SIG_IGN, re-instating it\n");
    sigaction(SIGQUIT, &quit_oldaction, NULL);
  }
  else {
    woempa(7, "Old quit handler was %p\n", quit_oldaction.sa_handler);
  }

#ifdef O4P
  segv_action.sa_sigaction = segv_handler;
  sigemptyset(&segv_action.sa_mask);
  segv_action.sa_flags = SA_SIGINFO | SA_ONESHOT;
  result = sigaction(SIGSEGV, &segv_action, &segv_oldaction);
  if (result == -1) {
    wprintf("Registering the segv handler failed, return code = %d.\n", result);
    abort();
  }
  if (segv_oldaction.sa_handler == SIG_DFL) {
    woempa(7, "Old segv handler was SIG_DFL\n");
  }
  else if (segv_oldaction.sa_handler == SIG_IGN) {
    woempa(7, "Old segv handler was SIG_IGN, re-instating it\n");
    sigaction(SIGSEGV, &segv_oldaction, NULL);
  }
  else {
    woempa(7, "Old segv handler was %p\n", segv_oldaction.sa_handler);
  }
#endif

}

