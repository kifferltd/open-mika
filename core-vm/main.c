/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java               *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include <unistd.h>
#ifdef USE_NANOSLEEP
#include <time.h>
#endif

#include "argument.h" 
#include "misc.h"
#include "oswald.h"
#include "network.h"
#include "threads.h"

#ifdef JAVAX_COMM
#include "driver_byteserial.h"
extern w_Driver_ByteSerial sio_driver;
extern void init_comm(void);
#endif

#ifdef MODULES 
void loadExtensions(void) {
#ifdef JAVAX_COMM  
  loadModule("mod_comm");
#endif
}
#else 
void init_extensions(void) {
#ifdef JAVAX_COMM  
  init_comm();
#endif
}
#endif

char *command_line_path;

Wonka_InitArgs  system_InitArgs;

char *woempa_dump_file = NULL;
int   woempa_stderr = 1;
int   woempa_bytecodecount   = 0;
int   woempa_bytecodetrigger = 0;

/*
** Help-style command line options and the texts which go with them.
*/
char *help_option_names[] = {"-help", "--help", "-?", "-X", "-W", "-Winfo", "-version", NULL};

const char *general_help_text =
  "Usage: mika [-options] class [args...]\n"
  "           (to execute a class)\n"
  "   or  mika [-options] -jar jarfile [args...]\n"
  "           (to execute a jar file)\n"
  " options:\n"
  "   -cp -classpath <classpath>   set search path for classes and resources\n"
  "   -D<name>=<value>             set a system property\n"
#ifdef USE_BYTECODE_VERIFIER
  "   -noverify                    do not verify any classes\n"
  "   -verify                      verify all classes, even system classes\n"
  "   -verifyremote                verify classes loaded by non-system classloaders (default)\n"
#endif
  "   -version                     print version\n"
  "   -? -help --help              print this explanation\n"
  "   -X                           print help on non-standard options\n"
  "   -W                           print help on Mika specific options\n"
  "\n";

const char *X_help_text =
  "  -Xms<size>                    set initial Java heap size (currently does nothing)\n"
  "  -Xmx<size>                    set maximum Java heap size\n"
  "  -Xbootclasspath:<classpath>   set the bootclasspath.\n"
#ifdef JDWP
  "  -Xdebug             enable remote debugging\n"
  "  -Xnoagent           obscure historic flag, ignored\n"
  "  -Xrunjdwp:...       set jdwp options. (use -Xrunjdwp:help for more info)\n"
#endif
#ifdef USE_BYTECODE_VERIFIER
  "  -Xverify:all        verify all classes, even system classes\n"
  "  -Xverify:none       do not verify any classes\n"
  "  -Xverify:remote     verify classes loaded by non-system classloaders (default)\n"
#endif
  "\n";

const char *W_help_text =
  "  -Winfo              print extra information (build configuration etc.).\n"
#ifdef DEBUG
  "  -Wlogfile=<file>    enable dumping the debug output to a file.\n"
  "  -Woempa=y|n         show debug output on stderr. (default: y)\n"
  "  -Wdebugfrom=<count> enable level 1 debugging from bytecode count\n"
#endif
#ifdef JSPOT
  "  -Wcompiler:...      set compiler options. (use -Wcompiler:help for more info)\n"
#endif
#if defined(AWT_XSIM) || defined(AWT_NONE)
  "  -Wrudolph:<w>x<h>   change the geometry of the AWT window on X.\n"
#endif
#if defined(AWT_FDEV) 
  "  -Wsplash:<filename> show a splash screen after the framebuffer has been opened.\n"
#endif
  "\n";

const char *version_help_text = 
VERSION_STRING " (compiled on " __DATE__ " " __TIME__ ")\n";

const char *info_help_text = 
  "Version: " VERSION_STRING " (compiled on " __DATE__ " " __TIME__ ")\n"
  "VM options: " WONKA_INFO "\n"
  "Default heap size: " DEFAULT_HEAP_SIZE "\n"
  "AWT options: " AWT_INFO "\n"
#ifdef O4P
  "O4P options: " O4P_INFO "\n"
#endif
#ifdef OSWALD
  "OSwald options: " OSWALD_INFO "\n"
#endif
  "Build host: " BUILD_HOST 
  "\n";

const char *help_texts[8];

/*
** If there is only one argument and it is one of -help, -?, -X, -W, -Winfo or -version,
** justVisiting() prints an appropriate text and returns WONKA_TRUE.  Otherwise
** WONKA_FALSE is returned.
*/
w_boolean justVisiting(int argument_count, char *arguments[]) {
  char ** cptr = help_option_names;
  char  *argument = arguments[1];

  if (argument_count == 2) {
    while (*cptr) {
      if (strcmp(argument, *cptr) == 0) {
        PutString((char *)help_texts[cptr - help_option_names]);

        return WONKA_TRUE;
      }
      ++cptr;
    }
  }
  return WONKA_FALSE;
}

/*
 * Interpret an unsigned decimal integer with optional leading equals
 * and/or trailing k/K/m/M suffix.
 */
static w_int getSize(char *str) {
  w_int j;
  w_int n = 0;
  char  ch;

  for (j = 0; j < (w_int)strlen(str); ++j) {
    ch = str[j];
    if (ch == '=') {
      continue;
    }
    else if (ch >= '0' && ch <= '9') {
      n = n * 10 + ch - '0';
    }
    else if (ch == 'M' || ch == 'm') {
      n = n * 1000000;
      break;
    }
    else if (ch == 'K' || ch == 'k') {
      n = n * 1000;
      break;
    }
    else {
      printf("Warning: heap size parameter terminated by illegal character '%c'\n", ch);
      break;
    }
  }

  return n;
}

/*
** Search the command line for an initial heap size, and if one is found
** just remove it.
*/
w_int getInitialHeapSize(int *argument_count_ptr, char *arguments[]) {
  w_int i;
  char *ms = NULL;
  w_int initsize;

  for (i = 1; i < *argument_count_ptr; ++i) {
    if (strncmp(arguments[i], "-Xms", 4) == 0) {
      ms = arguments[i];
    }
    else if (ms) {
      arguments[i - 1] = arguments[i];
    }
  }

  if (ms) {
    --*argument_count_ptr;
  }
  else {
    ms = "-Xms" DEFAULT_HEAP_SIZE;
  }

#ifdef DEBUG
  printf("Using -Xms%s\n", ms + 4);
#endif

  initsize = getSize(ms + 4);

#ifdef DEBUG
  printf("Initial heap size = %d\n", initsize);
#endif

  return initsize;
}

/*
** Search the command line for a max heap size, and if found process and remove it.
*/
w_int getMaxHeapSize(int *argument_count_ptr, char *arguments[]) {
  w_int i;
  char *mx = NULL;
  w_int maxsize;

  for (i = 1; i < *argument_count_ptr; ++i) {
    if (strncmp(arguments[i], "-Xmx", 4) == 0) {
      mx = arguments[i];
    }
    else if (mx) {
      arguments[i - 1] = arguments[i];
    }
  }

  if (mx) {
    --*argument_count_ptr;
  }
  else {
    mx = "-Xmx" DEFAULT_HEAP_SIZE;
  }

#ifdef DEBUG
  printf("Using -Xmx%s\n", mx + 4);
#endif

  maxsize = getSize(mx + 4);

#ifdef DEBUG
  printf("Max heap size = %d\n", maxsize);
#endif

  return maxsize;
}

/*
** Search the command line for a dump file, and if found process and remove it.
** Do the same also for the -Woempa=y|n and -Wdebugfrom=<count> commands.
*/
void getLogFile(int *argument_count_ptr, char ** arguments) {
  w_int i;
  w_int n;
  char  yn = 0;

  n = 0;
  for (i = 1; i < *argument_count_ptr; ++i) {
    if (n) {
      arguments[i - n] = arguments[i];
    }
    if (strncmp(arguments[i], "-Wlogfile=", 10) == 0) {
      woempa_dump_file = (char *)&arguments[i][10];
      ++n;
    }
    else if(strncmp(arguments[i], "-Woempa=", 8) == 0) {
      yn = arguments[i][8];
      ++n;
    }
    else if(strncmp(arguments[i], "-Wdebugfrom=", 12) == 0) {
      woempa_bytecodetrigger = atoi(&arguments[i][12]);
      ++n;
    }
  }


  if (yn) {
    if(yn == 'y') {
      woempa_stderr = 1;
    }
    else {
#ifdef DEBUG
      if(yn != 'n') {
        PutString("Illegal argument for -Woempa (should be 'y' or 'n'), treating as 'n'\n");
      }
#endif
      woempa_stderr = 0;
    }
  }

#ifndef DEBUG
  if (n) {
    PutString("Warning: one or more -W parameters ignored because DEBUG is set to false\n");
  }
#endif

  *argument_count_ptr -= n;
}

void registerExternals(void) {
#ifdef JAVAX_COMM
  registerDriver((w_driver)&sio_driver);
#endif
}

/*
** x_os_main is the function called by Oswald/o4p during system
** startup.  At this point we have no threads, no heap, no nothing: just
** a pointer to the first byte of memory after the Oswald/o4p data segment.
** We initialize all our static memory structures and the heap, and then
** start our initial threads.
*/

w_ubyte* x_os_main(int argument_count, char ** arguments, w_ubyte* FirstUnusedMemory) {
  initWonka();
  return FirstUnusedMemory;
}

#ifndef O4P
static int tick_detents[] = {500, 333, 250, 200, 167, 125, 100, 83, 50, 40, 30, 25, 20, 15, 10, 5, 0};
#endif

int main(int argc, char * argv[]) {
  int max_heap_size;
  int tick_millis;
#ifndef O4P
#ifdef CPU_MIPS
  int cpu_mips = CPU_MIPS;
#else
#warning CPU_MIPS not defined, arbitrarily assuming 166 MIPS
  int cpu_mips = 166;
#endif
  int *detent = tick_detents;
#endif
#ifdef USE_NANOSLEEP
  struct timespec ts;
#ifdef HOST_TIMER_GRANULARITY
  w_int host_timer_granularity_millis = HOST_TIMER_GRANULARITY / 1000;
#else
#warning HOST_TIMER_GRANULARITY not defined, assuming 10000 (usec)
  w_int host_timer_granularity_millis = 10;
#endif
#endif

  help_texts[0] = general_help_text;
  help_texts[1] = general_help_text;
  help_texts[2] = general_help_text;
  help_texts[3] = X_help_text;
  help_texts[4] = W_help_text;
  help_texts[5] = info_help_text;
  help_texts[6] = version_help_text;

  command_line_path = argv[0];

  if (justVisiting(argc,  argv)) {
     return 1;
  }

  getLogFile(&argc, argv);

  // We accept the -Xms parameter, but currently we do nothing with it
  getInitialHeapSize(&argc, argv);
  max_heap_size = getMaxHeapSize(&argc, argv);
  if (max_heap_size == 0) {
#ifdef UNC20
    max_heap_size = 8 * 1024 * 1024;
#else
#ifdef RUDOLPH
    max_heap_size = 32 * 1024 * 1024;
#else
    max_heap_size = 16 * 1024 * 1024;
#endif
#endif
  }

#ifdef O4P
  tick_millis = host_timer_granularity_millis;
#ifdef DEBUG
  printf("Using %d milliseconds per tick\n", tick_millis);
#endif
#else
  tick_millis = 25000 / cpu_mips;
  while (*(detent + 1)) {
    if (tick_millis > *detent) {
      tick_millis = *detent;
      break;
    }
    else {
      ++detent;
    }
  }
  if (tick_millis % host_timer_granularity_millis) {
    tick_millis -= (tick_millis % host_timer_granularity_millis);
    tick_millis += host_timer_granularity_millis;
  }
#ifdef DEBUG
  printf("Assumed CPU speed is %d MIPS, using %d milliseconds per tick\n", cpu_mips, tick_millis);
#endif
#endif

  command_line_argument_count = argc - 1;
  command_line_arguments = argv + 1;

  startNetwork();

  x_oswald_init(max_heap_size, tick_millis);
  // Hm. We get here for O4P, but not for OSwald. This needs to change.

#ifdef USE_NANOSLEEP
  ts.tv_sec = 1;
  ts.tv_nsec = 0;
#endif

  while(nondaemon_thread_count || !system_init_thread_started) {
#ifdef USE_NANOSLEEP
    nanosleep(&ts, NULL);
#else
    usleep(1000000);
#endif
  }

  return 0;

}
