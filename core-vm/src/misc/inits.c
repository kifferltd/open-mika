/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
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

#include <string.h>
#include <time.h>

#include "argument.h"
#include "clazz.h"
#include "descriptor.h"
#include "device.h"
#include "exception.h"
#include "fifo.h"
#include "methods.h"
#include "ts-mem.h"
#include "driver_virtual.h"
#include "file_driver.h"
#include "deflate_driver.h"
#include "locks.h"
#include "verifier.h"
#include "wstrings.h"
#include "wonka.h"
#include "vfs.h"
#include "network.h"
#include "exec.h"

#ifndef FSROOT
#define FSROOT "./fsroot"
#endif
#ifndef CLASSPATH
#ifdef FREERTOS
#define CLASSPATH "/app"
#else
#define CLASSPATH "."
#endif
#endif

#ifdef MODULES
extern void  x_symtab_kernel(void);
#endif

//TODO: get rid of these extern's
extern void mouse_set_path(char *s);

char *bootclasspath = BOOTCLASSDIR "/" BOOTCLASSFILE ":" BOOTCLASSDIR "/";

char *fsroot = NULL;

extern x_Mutex woempaMutex;
extern char *command_line_path;
extern w_fifo assertions_fifo;

#ifdef NATIVE_FP
wfp_float32 F_NAN;
wfp_float64 D_NAN;
#endif

void initWonka(void) {
  initDebug();

#ifdef NATIVE_FP
  F_NAN = strtof("NAN", NULL);
  D_NAN = strtod("NAN", NULL);
#endif

  initLocks();
#ifdef MODULES
  initModules();
  x_symtab_kernel();
#endif

  initKernel();
}

#ifdef FSENABLE
#ifdef ECOS
  extern w_driver image_driver;
#else
  extern w_driver disk_driver;
#endif //ECOS
#endif
  
w_int testWonka(void);

void startVFS(void) {
  init_vfs();
#ifdef FSENABLE
#ifdef ECOS
    vfs_mount((char *)"hdb", (char *)"/", (char *)"e2fs", VFS_MOUNT_RW);
#elif defined(LINUX)
    vfs_mount((char *)"hda4", (char *)"/", (char *)"e2fs", VFS_MOUNT_RW);
#elif defined(NETBSD)
#error "Bzzzt. Not implemented."
#else
#error "Bzzzt. Not implemented."
#endif 
#else
  vfs_mount(0, (char *)"/", (char *)"wromfs", VFS_MOUNT_RO); 
#endif
}

extern char *get_default_classpath(void);

char            *awt_args = NULL;
char            *awt_splash = NULL;

#ifdef JDWP
int      jdwp_enabled = 0;
char     *jdwp_args = NULL;
void     print_jdwp_help(void);
#endif

void args_read(void) {
  Wonka_InitArgs  *args = &system_InitArgs;
  char            **properties;
  int             prop_count = 0;
  char            *classpath;
  int             jarfileCommand = 0;

  memset(args, 0, sizeof(Wonka_InitArgs));
  properties = allocMem(100 * sizeof(char *));
#ifdef USE_BYTECODE_VERIFIER
  verify_flags = VERIFY_LEVEL_DEFAULT;
#endif
  classpath = get_default_classpath();

  woempa(7, "Found %d command line arguments\n", command_line_argument_count);
  while (command_line_argument_count) {
    w_int extra_args = 0;
    woempa(7, "Analysing command line argument `%s'\n", command_line_arguments[0]);

    if((strcmp(command_line_arguments[0], "-cp") == 0) || 
       (strcmp(command_line_arguments[0], "-classpath") == 0)) {
      extra_args = 1;
      woempa(7, "Found a classpath argument `%s'\n", command_line_arguments[1]);
      if(classpath) {
        woempa(7, "Overrides the default classpath `%s'\n", classpath);
      } 
      classpath = command_line_arguments[1];
    }
    else if(strncmp(command_line_arguments[0], "-D", 2) == 0) {
      woempa(7, "Found a property argument `%s'\n", command_line_arguments[0] + 2);
      if ((prop_count % 100) == 99) {
        properties = reallocMem(properties, prop_count + 101);
      }
      properties[prop_count++] = (char *)&command_line_arguments[0][2];
    }
    else if(strncmp(command_line_arguments[0], "-Xbootclasspath:", 16) == 0) {
      woempa(7, "Found an Xbootclasspath argument `%s'\n", command_line_arguments[0] + 16);
      bootclasspath = (char *)&command_line_arguments[0][16];
    }
#ifdef JDWP 

    else if(strncmp(command_line_arguments[0], "-Xrunjdwp:", 10) == 0) {
      woempa(7, "Found an Xrunjdwp argument `%s'\n", command_line_arguments[0] + 10);
      jdwp_args = (char *)&command_line_arguments[0][10];
      if(strcmp(jdwp_args, "help") == 0) {
        print_jdwp_help();
      }
    }
    else if(strncmp(command_line_arguments[0], "-Xdebug", 7) == 0) {
      woempa(7, "Found an Xdebug argument\n");
      jdwp_enabled = 1;
    }

#endif  /* JDWP */

#ifdef USE_BYTECODE_VERIFIER
    else if(strncmp(command_line_arguments[0], "-Xverify:all", 12) == 0) {
      woempa(7, "Found an Xverify:all argument\n");
      verify_flags = VERIFY_LEVEL_ALL;
    }
    else if(strncmp(command_line_arguments[0], "-Xverify:none", 13) == 0) {
      woempa(7, "Found an Xverify:none argument\n");
      verify_flags = VERIFY_LEVEL_NONE;
    }
    else if(strncmp(command_line_arguments[0], "-Xverify:remote", 15) == 0) {
      woempa(7, "Found an Xverify:remote argument\n");
      verify_flags = VERIFY_LEVEL_REMOTE;
    }
    else if(strncmp(command_line_arguments[0], "-verifyremote", 13) == 0) {
      woempa(7, "Found a verifyremote argument\n");
      verify_flags = VERIFY_LEVEL_REMOTE;
    }
    else if(strncmp(command_line_arguments[0], "-verify", 7) == 0) {
      woempa(7, "Found a verify argument\n");
      verify_flags = VERIFY_LEVEL_ALL;
    }
    else if(strncmp(command_line_arguments[0], "-noverify", 9) == 0) {
      woempa(7, "Found a noverify argument\n");
      verify_flags = VERIFY_LEVEL_NONE;
    }
#else
    else if(strncmp(command_line_arguments[0], "-Xverify:", 9) == 0 || strncmp(command_line_arguments[0], "-verify", 7) == 0 || strncmp(command_line_arguments[0], "-noverify", 9) == 0) {
      PutString("Warning: ignoring verify argument as verifier is disabled\n");
    }
#endif

    else if(strncmp(command_line_arguments[0], "-X", 2) == 0) {
      woempa(7, "Found an unknown argument '%s', ignoring it\n", command_line_arguments[0]);
    }

    else if(strncmp(command_line_arguments[0], "-Wrudolph:", 10) == 0) { 
      woempa(7, "Found a Wrudolph argument `%s'\n", command_line_arguments[0] + 10);
      awt_args = (char *)&command_line_arguments[0][10];
    }

#if defined(AWT_FDEV)

    else if(strncmp(command_line_arguments[0], "-Wsplash:", 9) == 0) { 
      woempa(7, "Found a Wsplash argument `%s'\n", command_line_arguments[0] + 9);
      awt_splash = (char *)&command_line_arguments[0][9];
    }
#endif  /* AWT_FDEV */
    else if(strncmp(command_line_arguments[0], "-Wfsroot:", 9) == 0) {
      woempa(7, "Found an Xfsroot argument `%s'\n", command_line_arguments[0] + 9);
      fsroot = (char *)&command_line_arguments[0][9];
    }
    else if(strcmp(command_line_arguments[0], "-jar") == 0) {
      woempa(7, "Found a -jar argument\n");
      jarfileCommand = 1;
    }
    else if(strcmp(command_line_arguments[0], "-Wpedantic") == 0) {
      woempa(7, "Found a -Wpedantic argument\n");
      pedantic = WONKA_TRUE;
    }
    else if(strcmp(command_line_arguments[0], "-ea") == 0
         || strcmp(command_line_arguments[0], "-enableassertions") == 0
         || strncmp(command_line_arguments[0], "-ea:", 4) == 0
         || strncmp(command_line_arguments[0], "-enableassertions:", 18) == 0) {
      woempa(7, "Found a -ea argument\n");
      putFifo(command_line_arguments[0], assertions_fifo);
    }
    else if(strcmp(command_line_arguments[0], "-da") == 0
         || strcmp(command_line_arguments[0], "-disableassertions") == 0
         || strncmp(command_line_arguments[0], "-da:", 4) == 0
         || strncmp(command_line_arguments[0], "-disableassertions", 18) == 0) {
      woempa(7, "Found a -da argument\n");
      putFifo(command_line_arguments[0], assertions_fifo);
    }
    else if(strcmp(command_line_arguments[0], "-esa") == 0
         || strcmp(command_line_arguments[0], "-enablesystemassertions") == 0) {
      woempa(7, "Found a -esa argument\n");
      // silently ignore it, we have no system assertions
    }
    else if(strcmp(command_line_arguments[0], "-dsa") == 0
         || strcmp(command_line_arguments[0], "-disablesystemassertions") == 0) {
      woempa(7, "Found a -dsa argument\n");
      // silently ignore it, we have no system assertions
    }
    else if(strncmp(command_line_arguments[0], "-", 1) == 0) {
      woempa(7, " Unrecognized option: %s\n", command_line_arguments[0]);

      exit(1);

    } else {
      woempa(7, " Not an option: %s, passing remaining %d arguments on to Init/main()\n", command_line_arguments[0], command_line_argument_count);

      /*
      ** It's not an option, so it has to be the main class and 
      ** it's arguments. Since command_line_argument_count and 
      ** command_line_arguments were updated each time we went
      ** through this loop, they're now pointing right at the
      ** requested startclass.
      */

      break;

    }
    
      command_line_argument_count = command_line_argument_count - 1 - extra_args;
      command_line_arguments += extra_args + 1;
  }

  if (!fsroot) {
    fsroot = getenv("MIKA_FSROOT");
  }

  if (!fsroot) {
    fsroot = FSROOT;
  }

// TODO this code is applicable to any OS in which Mika is launched as a process
//      it is generally not applicable in an RTOS where Mika is "the application"
#ifndef FREERTOS
  {
    char *new_fsroot;
    char *path = host_getCommandPath();
    int l = strlen(path);

    woempa(7, "fsroot was '%s', path is '%s'\n", fsroot, path);
    while (path[--l] != '/') {
      if(l <= 0) {
        woempa(9,"Panic mode: where are we called from ?\n\tNo slash found in '%s'\n",path); 
        wabort(ABORT_WONKA, "Unable to locate binary !\n");
      }
    } 

    while (strlen(fsroot) >= 2 && fsroot[0] == '.' && fsroot[1] == '/') {
      fsroot += 2;
    }

    while (strlen(fsroot) >= 3 && fsroot[0] == '.' && fsroot[1] == '.' && fsroot[2] == '/') {
      fsroot += 3;
      while (path[--l] != '/') {
        if(l <= 0) {
          woempa(9,"Panic mode: too many ../ in fsroot\n"); 
          wabort(ABORT_WONKA, "Unable to locate binary !\n");
        }
      } 
    }
    woempa(7, "Allocating %d bytes for new fsroot\n", l + strlen(fsroot) + 2);
    new_fsroot = allocClearedMem(l + strlen(fsroot) + 2);
    memcpy(new_fsroot, path, l + 1);
    woempa(7, "command_line_path directory is '%s'\n", new_fsroot);
    memcpy(new_fsroot + l + 1, fsroot, strlen(fsroot));
    releaseMem(path);

    fsroot = new_fsroot;
    woempa(7, "fsroot is now '%s'\n", fsroot);
  }
#endif

  if(jarfileCommand){
    command_line_argument_count += 1;
    command_line_arguments -= 1;
    woempa(7, "Found a -jar argument overwriting '%s' with '-jar'\n",command_line_arguments[0]);
    command_line_arguments[0] = "-jar";
  }

  args->properties = reallocMem(properties, (prop_count + 1) * sizeof(void *));;
  args->properties[prop_count] = NULL;
  args->classpath  = classpath;



#ifdef DEBUG
  {
    w_int i;
    for (i = 0; i < prop_count; ++i) {
      woempa(7, "Property: %s\n", properties[i]);
    }
    woempa(7, "Classpath: %s\n", args->classpath);
    woempa(7, "Xbootclasspath: %s\n", bootclasspath);
    for (i = 0; i < command_line_argument_count; ++i) {
      woempa(7, "Java argument: %s\n", command_line_arguments[i]);
    }
  }
#endif
}

void startWonka(void* data) {

  JavaVM *vm;

#ifdef O4P
  struct timespec ts;
#endif

  x_formatter('w', print_string);
  x_formatter('k', print_clazz_short);
  x_formatter('K', print_clazz_long);
  x_formatter('j', print_instance_short);
  x_formatter('J', print_instance_long);
  x_formatter('m', print_method_short);
  x_formatter('M', print_method_long);
  x_formatter('v', print_field_short);
  x_formatter('V', print_field_long);
  x_formatter('e', print_exception);
  x_formatter('t', print_thread_short);
  x_formatter('T', print_thread_long);
  x_formatter('y', print_descriptor);

  make_ISO3309_CRC_table();

  /*
  ** Here we start routines that require a valid heap (for malloc) to
  ** be set up...
  */
  assertions_fifo = allocFifo(30);

  args_read();

  startStrings();

  startDeviceRegistry();
  startDriverRegistry();
  
#ifdef FSENABLE
#ifdef ECOS
  registerDevice("hdb", "hdb", 0, wdt_block_random);
  registerDriver((w_driver)&image_driver);
#else
  registerDevice("hda", "hda", 0, wdt_block_random);
  registerDriver((w_driver)&disk_driver);
#endif // ECOS
#endif

  /* Initialize known filesystems */
  
#if (defined(FSENABLE)) 
  init_e2fs(); 
#endif
  startVFS();

  startNetwork();

  registerDevice("unzip_", "zip", 0, wdt_byte_serial);
  registerDevice("zip_", "zip", 20, wdt_byte_serial);
  registerExternals();
 
  registerDriver((w_driver)&deflate_driver);

  haveWonkaThreads = WONKA_TRUE;

#ifdef JNI
  JNIEnv *env;
  woempa(7, "Calling JNI_CreateJavaVM() ...\n");
  JNI_CreateJavaVM(&vm, &env, &system_InitArgs);
#else
  // TODO separate this cleanly from JNI

  startHeap();
  startLoading();
  startKernel();
#ifdef RUDOLPH
#ifdef MODULES
  loadModule("mod_awt");
#else
  init_awt();
#endif
#endif
#endif

#ifdef O4P
  // Don't return from here, 'coz the vm gets popped off the stack! (D'oh)
  ts.tv_sec = 10;
  ts.tv_nsec = 0;

  while(1) {
    nanosleep(&ts, NULL);
  }
#endif
}

