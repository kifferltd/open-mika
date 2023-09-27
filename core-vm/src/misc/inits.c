/**************************************************************************
* Copyright (c) 2020, 2021, 2022, 2023 by KIFFER Ltd.                     *
* All rights reserved.                                                    *
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

#ifdef MODULES
extern void  x_symtab_kernel(void);
#endif

#ifdef USE_OBJECT_HASHTABLE
w_hashtable object_hashtable;
#endif

char *bootclasspath =
#ifdef USE_ROMFS
    "ROMFS"
#else
#ifdef BOOTCLASSSUBDIR
    BOOTCLASSDIR "/" BOOTCLASSSUBDIR
#else
    BOOTCLASSDIR "/" BOOTCLASSFILE
#endif
    ":" BOOTCLASSDIR "/"
#endif
;

char *fsroot = NULL;

/**
 * base_directory is the directory which was current (cwd) when Mika was launched.
 * It will not be updated if a chdir() is executed later.
 */
char *base_directory;

extern x_Mutex woempaMutex;
extern char *command_line_path;
extern w_fifo assertions_fifo;

#ifdef NATIVE_FP
wfp_float32 F_NAN;
wfp_float64 D_NAN;
#endif

#ifdef FREERTOS
#define SYSTEM_STACK_SIZE (configMINIMAL_STACK_SIZE * 2)
#else
#define SYSTEM_STACK_SIZE ((unsigned short)16384)
#endif

/* [CG 20050601]
 * For O4P we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#if defined O4P || defined FREERTOS
#define ur_thread_stack NULL
#else
static char ur_thread_stack[SYSTEM_STACK_SIZE];
#endif
x_Thread ur_thread_x_Thread;

// #if defined IMSYS_NATIVE_BC
extern void setupBcEmulation(void);
// #endif

void initWonka(void) {
  initDebug();

// #if defined IMSYS_NATIVE_BC
  setupBcEmulation();
// #endif

#ifdef NATIVE_FP
  F_NAN = strtof("NAN", NULL);
  D_NAN = strtod("NAN", NULL);
#endif

  initLocks();
#ifdef MODULES
  initModules();
  x_symtab_kernel();
#endif

#ifdef O4P
  install_term_handler();
#endif
#ifdef FREERTOS
// just so the FreeRTOS task gets a nice name
  strcpy(ur_thread_x_Thread.name, INIT_THREAD_NAME);
#endif

  x_thread_create(&ur_thread_x_Thread, 
    startWonka, NULL, ur_thread_stack, 
    SYSTEM_STACK_SIZE, SYSTEM_GROUP_MANAGER_PRIORITY, TF_START);
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

  /*
  ** Note the working directory in which we were launched.
  */
  w_int m = 32;
  base_directory = allocMem(m);
  while (!vfs_getcwd(base_directory, m - 1)) {
    m *= 2;
    base_directory = reallocMem(base_directory, m);
    memset(base_directory, 0, m);
  }
  woempa(7, "base_directory is '%s'\n", base_directory);
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

#define PRINT_FIELD_OFFSET(nm,field) woempa(7,"FIELD_%s = %d\n", #nm, offsetof(w_Field, field) )
#define PRINT_METHOD_OFFSET(nm,field) woempa(7,"METHOD_%s = %d\n", #nm, offsetof(w_Method, field) )
#define PRINT_METHOD_SPEC_OFFSET(nm,field) woempa(7,"METHOD_SPEC_%s = %d\n", #nm, offsetof(w_Method, spec) + offsetof(w_MethodSpec, field) )
#define PRINT_METHOD_EXEC_OFFSET(nm,field) woempa(7,"METHOD_EXEC_%s = %d\n", #nm, offsetof(w_Method, exec) + offsetof(w_MethodExec, field) )

void startWonka(void* data) {

  JavaVM *vm;

#ifdef O4P
  struct timespec ts;
#endif
// temporary hack
#ifdef FREERTOS
  grab_low_memory();
#endif

  haveWonkaThreads = WONKA_TRUE;

  lowMemoryCheck;

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

  lowMemoryCheck;

// temporary hack
  woempa(7,"w_Fieldd offset definitions:\n");
  PRINT_FIELD_OFFSET(DECLARING_CLAZZ,declaring_clazz);
  PRINT_FIELD_OFFSET(NAME,name);
  PRINT_FIELD_OFFSET(VALUE_CLAZZ,value_clazz);
  PRINT_FIELD_OFFSET(FLAGS,flags);
  PRINT_FIELD_OFFSET(SLOT,size_and_slot);
  PRINT_FIELD_OFFSET(DESC,desc);
  PRINT_FIELD_OFFSET(INITVAL,initval);
  woempa(7,"w_Method offset definitions:\n");
  PRINT_METHOD_OFFSET(SPEC,spec);
  PRINT_METHOD_SPEC_OFFSET(DECLARING_CLAZZ, declaring_clazz);
  PRINT_METHOD_SPEC_OFFSET(NAME, name);
  PRINT_METHOD_SPEC_OFFSET(DESC, desc);
  PRINT_METHOD_SPEC_OFFSET(ARG_TYPES, arg_types);
  PRINT_METHOD_SPEC_OFFSET(RETURN_TYPE, return_type);
  PRINT_METHOD_OFFSET(SLOT,slot);
  PRINT_METHOD_OFFSET(FLAGS,flags);
  PRINT_METHOD_OFFSET(PARENT,parent);
  PRINT_METHOD_OFFSET(NUM_THROWS,numThrows);
  PRINT_METHOD_OFFSET(THROWS,throws);
  PRINT_METHOD_OFFSET(EXEC,exec);
  PRINT_METHOD_EXEC_OFFSET(DISPATCHER, dispatcher);
  PRINT_METHOD_EXEC_OFFSET(ARG_I, arg_i);
  PRINT_METHOD_EXEC_OFFSET(FUNCTION, function);
  PRINT_METHOD_EXEC_OFFSET(RETURN_I, return_i);
  PRINT_METHOD_EXEC_OFFSET(LOCAL_I, local_i);
  PRINT_METHOD_EXEC_OFFSET(STACK_I, stack_i);
  PRINT_METHOD_EXEC_OFFSET(NARGS, nargs);
  PRINT_METHOD_EXEC_OFFSET(CODE_LENGTH, code_length);
  PRINT_METHOD_EXEC_OFFSET(CODE, code);
  PRINT_METHOD_EXEC_OFFSET(NUM_EXCEPTIONS, numExceptions);
  PRINT_METHOD_EXEC_OFFSET(EXCEPTIONS, exceptions);
 

//   w_methodDebugInfo debug_info; /* Method debug info if available         */
// // TODO Following are only used if either bytecode verification or new 
// // execution engine are used, we should have an ifdef for this.
//   w_ubyte *status_array;   /* Per-byte flags showing instruction boundaries etc. */
//   w_wordset basicBlocks;   /* Each entry in the wordset is a v_BasicBlock* */

  /*
  ** Here we start routines that require a valid heap (for malloc) to
  ** be set up...
  */
  lowMemoryCheck;

  make_ISO3309_CRC_table();

  globals_hashtable = ht_create((char*)"hashtable:global-refs", GLOBALS_HASHTABLE_SIZE, NULL, NULL, 0, 0);
  if (!globals_hashtable) {
    wabort(ABORT_WONKA, "Unable to create globals_hashtable\n");
  }
  woempa(1, "created globals_hashtable at %p\n", globals_hashtable);

#ifdef USE_OBJECT_HASHTABLE
  object_hashtable = ht_create((char*)"hashtable:objects", 32767, NULL, NULL, 0, 0);
  woempa(7, "Created object_hashtable at %p\n",object_hashtable);
#endif

  lowMemoryCheck;

  assertions_fifo = allocFifo(30);

  lowMemoryCheck;

  args_read();

  lowMemoryCheck;

  startStrings();

  lowMemoryCheck;

  startDeviceRegistry();
  startDriverRegistry();

  lowMemoryCheck;

  createCharacterTables();
  
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
  
  lowMemoryCheck;

#if (defined(FSENABLE)) 
  init_e2fs(); 
#endif
  startVFS();

  lowMemoryCheck;

  startNetwork();

  lowMemoryCheck;

  registerDevice("unzip_", "zip", 0, wdt_byte_serial);
  registerDevice("zip_", "zip", 20, wdt_byte_serial);
  registerExternals();
 
  registerDriver((w_driver)&deflate_driver);

  lowMemoryCheck;

#ifdef USE_ROMFS
  init_romfs();
#endif

  lowMemoryCheck;

#ifdef JNI
  JNIEnv *env;
  woempa(7, "Calling JNI_CreateJavaVM() ...\n");
  JNI_CreateJavaVM(&vm, &env, &system_InitArgs);
#else
  // TODO separate this cleanly from JNI
  system_vm_args = &system_InitArgs;
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

  lowMemoryCheck;

}

