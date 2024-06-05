/**************************************************************************
* Copyright (c) 2021, 2023 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "oswald.h"
#include "wonka.h"
#include <stdbool.h>
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
static struct sigaction usr1_action;
static struct sigaction usr1_oldaction;
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
  w_printf("\n\r\n*** SEGMENTATION FAULT ***\n\r\n");
  w_printf("signo = %d code = %d%s addr = %p\n\r\n", sis->si_signo, sis->si_code, sis->si_code == SEGV_MAPERR ? " (SEGV_MAPERR)" : sis->si_code == SEGV_ACCERR ? " (SEGV_ACCERR)" : "", sis->si_addr);
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
    w_printf("Registering the term handler failed, return code = %d.\n", result);
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
    w_printf("Registering the int handler failed, return code = %d.\n", result);
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
    w_printf("Registering the quit handler failed, return code = %d.\n", result);
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

  usr1_action.sa_handler = SIG_IGN;
  sigemptyset(&usr1_action.sa_mask);
  usr1_action.sa_flags = 0;
  result = sigaction(SIGUSR1, &usr1_action, &usr1_oldaction);
  if (result == -1) {
    w_printf("Registering the usr1 handler failed, return code = %d.\n", result);
    abort();
  }

#ifdef O4P
  segv_action.sa_sigaction = segv_handler;
  sigemptyset(&segv_action.sa_mask);
  segv_action.sa_flags = SA_SIGINFO | SA_ONESHOT;
  result = sigaction(SIGSEGV, &segv_action, &segv_oldaction);
  if (result == -1) {
    w_printf("Registering the segv handler failed, return code = %d.\n", result);
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

w_boolean Wonka_static_useCli(w_thread thread, w_instance theClass) {
  return false;
}

