/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java         *
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

#include "clazz.h"
#include "descriptor.h"
#include "hashtable.h"
#include "jdwp.h"
#include "jdwp_events.h"
#include "jdwp-protocol.h"
#include "loading.h"
#include "oswald.h"
#include "threads.h"
#include "wonka.h"
#include "wstrings.h"

#include <string.h>

extern char *fsroot;

#ifdef DEBUG
static const char* vm_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Version",
  /*  2 */ "ClassesBySignature",
  /*  3 */ "AllClasses",
  /*  4 */ "AllThreads",
  /*  5 */ "TopLevelThreadGroups",
  /*  6 */ "Dispose",
  /*  7 */ "IDSizes",
  /*  8 */ "Suspend",
  /*  9 */ "Resume",
  /* 10 */ "Exit",
  /* 11 */ "CreateString",
  /* 12 */ "Capabilities",
  /* 13 */ "ClassPaths",
  /* 14 */ "DisposeObjects",
  /* 15 */ "HoldEvents",
  /* 16 */ "ReleaseEvents",
  /* 17 */ "CapabilitiesNew",
  /* 18 */ "RedefineClasses",
  /* 19 */ "SetDefaultStratum",
};

#define VM_MAX_COMMAND 19

#endif

/*
** Return a few details about this virtual machine and the JDWP version.
*/

void jdwp_version(jdwp_command_packet cmd) {
  char *version_string = VERSION_STRING;
  char *ver = "1.4.2";
  char *name = "Mika";
  w_int jdwp_ver1 = 1;
  w_int jdwp_ver2 = 0;

  // description
  jdwp_put_cstring(&reply_grobag, version_string, strlen(version_string));
  // jdwpMajor, jdwpMinor
  jdwp_put_u4(&reply_grobag, jdwp_ver1);
  jdwp_put_u4(&reply_grobag, jdwp_ver2);
  // vmVersion
  jdwp_put_cstring(&reply_grobag, ver, strlen(ver));
  // vmName
  jdwp_put_cstring(&reply_grobag, name, strlen(name));
  
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}

/*
** The dotted form of the descriptor specified in ClassesBySignature.
** This is not thread-safe, but we assume that JDWP is single-threaded. .
*/
static w_string classname;

/*
** Test whether the FQN of clazz matches match_pattern.
** 'match_pattern' may include a leading or trailing '*' as wildcard.
*/
w_boolean matchClassname(w_clazz clazz, w_string match_pattern) {
  w_int i;
  w_int j;
  w_int l;

  if (clazz->dotified == match_pattern) {
    woempa(7, "Direct match!\n");

    return TRUE;
  }

  l = string_length(match_pattern);
  if (string_char(match_pattern, 0) == '*') {
    for (i = 1, j = string_length(clazz->dotified) - l + 1; i < l; ++i, ++j) {
      if (string_char(clazz->dotified, j) != string_char(match_pattern, i)) {

        return FALSE;

      }
    }
    woempa(7, "Wildcard match %w = %w\n", clazz->dotified, match_pattern);

    return TRUE;
  }
  else if (string_char(match_pattern, l - 1) == '*') {
    --l;
    for (i = 0; i < l; ++i) {
      if (string_char(clazz->dotified, i) != string_char(match_pattern, i)) {

        return FALSE;

      }
    }
    woempa(7, "Wildcard match %w = %w\n", clazz->dotified, match_pattern);

    return TRUE;
  }

  return FALSE;
}

/*
** Search the specified classloader for classes matching 'classname'.
*/
static void *getMatchingClasses(w_instance loader) {
  w_hashtable ht = getWotsitField(loader, F_ClassLoader_loaded_classes);
  w_fifo fifo1 = ht_list_values(ht);
  w_fifo fifo2 = allocFifo(254);
  w_clazz clazz;

  while ((clazz = getFifo(fifo1))) {
    if (matchClassname(clazz, classname)) {
      woempa(7, "%k matches %w\n", clazz, classname);
      putFifo(clazz, fifo2);
    }
  }
  woempa(7, "%j has loaded %d classes matching %w\n", loader, occupancyOfFifo(fifo2), classname);
  releaseFifo(fifo1);

  if (occupancyOfFifo(fifo2) == 0) {
    releaseFifo(fifo2);
    fifo2 = NULL;
  }

  return fifo2;
}

/*
** Get a list of all classes loaded by the specified classloader.
*/
static void *getAllLoadedClasses(w_instance loader) {
  w_hashtable ht = getWotsitField(loader, F_ClassLoader_loaded_classes);
  w_fifo fifo = ht_list_values(ht);

  return fifo;
}

/*
** Return the reference type of all the loaded classes which match the given
** signature. 
*/

static void jdwp_vm_classes_by_sig(jdwp_command_packet cmd) {
  w_int length;
  w_ubyte *sig;
  w_string sig_string;
  w_fifo fifo_of_fifos;
  w_fifo clazz_fifo;
  w_fifo one_fifo;
  w_clazz clazz;

  sig = jdwp_UTF82cstring((w_ubyte*)cmd->data, &length);
  sig_string = cstring2String((char*)sig, (w_word)length);
  classname = undescriptifyClassName(sig_string);
  releaseMem(sig);
  deregisterString(sig_string);

  woempa(7, "Searching all class loaders for classes with signature %w\n", classname);
  fifo_of_fifos = forEachClassLoader(getMatchingClasses);
  if (fifo_of_fifos) {
    woempa(7, "Found %d class loaders\n", occupancyOfFifo(fifo_of_fifos));
    deregisterString(classname);
    clazz_fifo = allocFifo(254);
    while ((one_fifo = getFifo(fifo_of_fifos))) {
      woempa(7, "Reading fifo %p\n", one_fifo);
      while ((clazz = getFifo(one_fifo))) {
          woempa(1, "  %K -> clazz_fifo\n", clazz);
          putFifo(clazz, clazz_fifo);
      }
      woempa(7, "Releasing fifo %p\n", one_fifo);
      releaseFifo(one_fifo);
    }
    woempa(7, "Found %d classes\n", occupancyOfFifo(clazz_fifo));
    jdwp_put_u4(&reply_grobag, occupancyOfFifo(clazz_fifo));

    while ((clazz = getFifo(clazz_fifo))) {
      woempa(1, "  %K\n", clazz);
      jdwp_put_u1(&reply_grobag, isSet(clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class);
      jdwp_put_clazz(&reply_grobag, clazz);
      jdwp_put_u4(&reply_grobag, clazz2status(clazz));
    }
    releaseFifo(clazz_fifo);
  }
  else {
    jdwp_put_u4(&reply_grobag, 0);
  }

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Return all classes currently loaded by the VM.
*/

static void jdwp_vm_classes(jdwp_command_packet cmd) {
  w_int length;
  w_ubyte *sig;
  w_string sig_string;
  w_fifo fifo_of_fifos;
  w_fifo clazz_fifo;
  w_fifo one_fifo;
  w_clazz clazz;

  woempa(7, "Searching all class loaders for all loaded classes\n");
  fifo_of_fifos = forEachClassLoader(getAllLoadedClasses);
  if (fifo_of_fifos) {
    woempa(7, "Found %d class loaders\n", occupancyOfFifo(fifo_of_fifos));
    clazz_fifo = allocFifo(254);
    while ((one_fifo = getFifo(fifo_of_fifos))) {
      woempa(7, "Reading fifo %p\n", one_fifo);
      while ((clazz = getFifo(one_fifo))) {
        if (!clazzIsPrimitive(clazz)) {
          woempa(1, "  %K -> clazz_fifo\n", clazz);
          putFifo(clazz, clazz_fifo);
        }
        else {
          woempa(7, "  %K is primitive class, ignoring\n", clazz);
        }
      }
      woempa(7, "Releasing fifo %p\n", one_fifo);
      releaseFifo(one_fifo);
    }
    woempa(7, "Found %d classes\n", occupancyOfFifo(clazz_fifo));

    jdwp_put_u4(&reply_grobag, occupancyOfFifo(clazz_fifo));
    woempa(7, "Reading clazz_fifo\n");
    while ((clazz = getFifo(clazz_fifo))) {
      jdwp_put_u1(&reply_grobag, isSet(clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class);
      jdwp_put_clazz(&reply_grobag, clazz);
      sig_string = clazz2desc(clazz);
      woempa(1, "  %K descriptor %w\n", clazz, sig_string);
      sig = jdwp_string2UTF8(sig_string, &length);
      jdwp_put_bytes(&reply_grobag, sig, length + 4);
      deregisterString(sig_string);
      releaseMem(sig);
      jdwp_put_u4(&reply_grobag, clazz2status(clazz));
    }
    woempa(7, "Releasing clazz_fifo\n");
    releaseFifo(clazz_fifo);
  }
  else {
    jdwp_put_u4(&reply_grobag, 0);
  }

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Returns all the active threads. This includes threads created with java.lang.Thread, native threads 
** attached to Wonka with JNI and system threads created by Wonka itself. Threads which aren't started
** yet or threads that are no longer running are not included.
*/

static void jdwp_vm_threads(jdwp_command_packet cmd) {
  w_fifo    fifo;
  w_thread  thread = NULL;
  w_int     length;

  /*
  ** Use a fifo to get all the threads from the thread hashtable.
  ** Subtract one from the length for the JDWP thread. The debugger
  ** doesn't need to know about this.
  */

  fifo = ht_list_values(thread_hashtable);
  length = occupancyOfFifo(fifo) - 1;
  woempa(7, "We have %d threads (not counting the JDWP thread)\n", length);

  /*
  ** Store the number of threads in the reply.
  */

  jdwp_put_u4(&reply_grobag, length);

  /*
  ** Go over the entries one by one and put them into the reply packet.
  */

  while((thread = getFifo(fifo)) != NULL) {
    if(thread != jdwp_thread) {
      woempa(7, "  %t\n", thread);
      jdwp_put_objectref(&reply_grobag, thread->Thread);
    }
  }

  releaseFifo(fifo);
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Return all the threadgroups that don't have a parent. (In our case this is 
** only one, the system thread group). This can be used by the debugger to
** build a tree of all the threads and threadgroups.
*/

static void jdwp_vm_threadgroups(jdwp_command_packet cmd) {
  /* 
  ** Since we only have one toplevel threadgroup, 'groups' equals 1 and
  ** threadGroupID is the address of the system threadgroup.
  */
  
  jdwp_put_u4(&reply_grobag, 1);
  woempa(7, "One top-level ThreadGroup = %j\n", I_ThreadGroup_system);
  jdwp_put_objectref(&reply_grobag, I_ThreadGroup_system);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Disconnect from the debugger. There are a few tasks involved after disconnecing :
**  - cancel all event requests.
**  - all threads suspended by the debugger should be started again (as many times
**    as it takes).
*/

static void jdwp_vm_disconnect(jdwp_command_packet cmd) {
  w_fifo fifo = ht_list_values(thread_hashtable);
  w_thread thread;
  
  while((thread = getFifo(fifo)) != NULL) {
    woempa(7, "Clearing suspend count of %t\n", thread);
    thread->flags &= ~WT_THREAD_SUSPEND_COUNT_MASK;
  }

  unsetFlag(blocking_all_threads, BLOCKED_BY_JDWP);

  jdwp_state = jdwp_state_initialised;
  jdwp_events_enabled = 0;

  jdwp_clear_all_events();

  /* TODO: if garbage collection was disabled, enable it again. */
}


/*
** Returns the sizes of the different identifiers. These values indicate the size
** of these identifiers in all the packets sent between debugger and VM.
*/

struct idSizes {
  w_int fieldIDSize;
  w_int methodIDSize;
  w_int objectIDSize;
  w_int refTypeIDSize;
  w_int frameIDSize;
};

static void jdwp_vm_sizes(jdwp_command_packet cmd) {
  jdwp_put_u4(&reply_grobag, 4); // fieldIDSize = w_field
  jdwp_put_u4(&reply_grobag, 4); // methodIDSize = w_method
  jdwp_put_u4(&reply_grobag, 4); // objectIDSize = w_instance
  jdwp_put_u4(&reply_grobag, 4); // refTypeIDSize = w_clazz
  jdwp_put_u4(&reply_grobag, 4); // frameIDSize = w_frame

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}

/*
** Suspend all threads.
*/

static void jdwp_vm_suspend_vm(jdwp_command_packet cmd) {
  jdwp_internal_suspend_all();
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Returns the capabilities of this JPDA implementation. A zero indicates that we 
** don't have that feature, anything else means we can handle such requests.
*/

static void jdwp_capabilities(jdwp_command_packet cmd) {
  jdwp_put_u1(&reply_grobag, 0);  /* Watch field modifications and send modification watchpoint events */
  jdwp_put_u1(&reply_grobag, 0);  /* Watch field access and send access watchpoint events */
  jdwp_put_u1(&reply_grobag, 1);  /* Get bytecodes of methods */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we detect if a field or method is 'invented' by the compiler */
  jdwp_put_u1(&reply_grobag, 0);  /* Get owned monitors information for a thread */
  jdwp_put_u1(&reply_grobag, 0);  /* Get current contended monitor of a thread */
  jdwp_put_u1(&reply_grobag, 0);  /* Get monitor information for an object */
  
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}

/*
** Create a String object with the given content.
** TODO: what stops this string from being immediately GC'd?
*/
static void jdwp_create_string(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_string string = jdwp_get_string(cmd->data, &offset);
  w_instance instance = newStringInstance(string);

  jdwp_put_objectref(&reply_grobag, instance);
  deregisterString(string);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}

/*
** Resume all threads. 
*/

static void jdwp_vm_resume_vm(jdwp_command_packet cmd) {
  jdwp_internal_resume_all();
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Terminate the vm with a given exit code.
*/

static void jdwp_vm_terminate(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_word exit_code = jdwp_get_u4(cmd->data, &offset);
  exit(exit_code);
}


/*
** Returns the (new) capabilities of this JPDA implementation. A zero indicates that we 
** don't have that feature, anything else means we can handle such requests.
*/

static void jdwp_capabilities_new(jdwp_command_packet cmd) {
  jdwp_put_u1(&reply_grobag, 0);  /* Watch field modifications and send modification watchpoint events */
  jdwp_put_u1(&reply_grobag, 0);  /* Watch field access and send access watchpoint events */
  jdwp_put_u1(&reply_grobag, 1);  /* Get bytecodes of methods */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we detect if a field or method is 'invented' by the compiler */
  jdwp_put_u1(&reply_grobag, 0);  /* Get owned monitors information for a thread */
  jdwp_put_u1(&reply_grobag, 0);  /* Get current contended monitor of a thread */
  jdwp_put_u1(&reply_grobag, 0);  /* Get monitor information for an object */

  /* 
  ** These are the new capabilities
  */

  jdwp_put_u1(&reply_grobag, 0);  /* Can we redefine classes */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we add methods while redefining */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we redefine classes in an arbitrary way */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we pop the top stack frame */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we filter events by specific object */
  jdwp_put_u1(&reply_grobag, 1);  /* Can we get source debug extension */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we request VM death events */
  jdwp_put_u1(&reply_grobag, 0);  /* Can we set a default stratum */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 16 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 17 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 18 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 19 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 20 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 21 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 22 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 23 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 24 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 25 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 26 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 27 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 28 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 29 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 30 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 31 */
  jdwp_put_u1(&reply_grobag, 0);  /* Reserved for future additions - 32 */
  
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}

extern Wonka_InitArgs *system_vm_args;
extern char *bootclasspath;

/*
** Classpaths command
*/

static void jdwp_vm_classpaths(jdwp_command_packet cmd) {
  char *finger = (char*)system_vm_args->classpath;
  char *next;
  char **array;
  int fsrootlen = strlen(fsroot);
  int i;
  int l;
  int n;

  woempa(7, "Base dir = %s\n", jdwp_base_directory);
  // baseDir
  jdwp_put_cstring(&reply_grobag, jdwp_base_directory, strlen(jdwp_base_directory));

  if (fsroot[fsrootlen - 1] == '/') {
    --fsrootlen;
  }
  woempa(7, "fsroot = %s\n", fsroot);
  woempa(7, "Classpath = %s\n", system_vm_args->classpath);
  n = 0;
  while (finger) {
    n++;
    finger = strstr(++finger, ":");
  }
  woempa(7, "Classpath has %d components\n", n);
  array = allocMem(n * sizeof(char*));
  finger = (char*)system_vm_args->classpath;
  i = 0;
  while (finger) {
    next = strstr(finger, ":");
    l = next ? next - finger : (int)strlen(finger);
    if (l >= 3 && finger[0] == '{' && finger[1] == '}' && finger[2] == '/') {
      array[i] = allocMem(l + fsrootlen);
      strcpy(array[i], fsroot);
      memcpy(array[i] + fsrootlen, finger + 2, l);
      array[i][fsrootlen + l - 2] = 0;
    }
    else {
      array[i] = allocMem(l + 1);
      strcpy(array[i], finger);
    }
    woempa(7, "  %s\n", array[i]);
    ++i;
    finger = next ? next + 1 : NULL;
  }
  // classpaths
  jdwp_put_u4(&reply_grobag, n);
  for (i = 0; i < n; ++i) {
    woempa(7, "  %s\n", array[i]);
    jdwp_put_cstring(&reply_grobag, array[i], strlen(array[i]));
    releaseMem(array[i]);
  }
  
  finger = bootclasspath;

  woempa(7, "Bootclasspath = %s\n", bootclasspath);
  n = 0;
  while (finger) {
    n++;
    finger = strstr(++finger, ":");
  }
  woempa(7, "Bootclasspath has %d components\n", n);
  array = reallocMem(array, n * sizeof(char*));
  finger = bootclasspath;
  i = 0;
  while (finger) {
    next = strstr(finger, ":");
    l = next ? next - finger : (int)strlen(finger);
    if (l >= 3 && finger[0] == '{' && finger[1] == '}' && finger[2] == '/') {
      array[i] = allocMem(l + fsrootlen);
      strcpy(array[i], fsroot);
      memcpy(array[i] + fsrootlen, finger + 2, l);
      array[i][fsrootlen + l - 2] = 0;
    }
    else {
      array[i] = allocMem(l + 1);
      strcpy(array[i], finger);
    }
    woempa(7, "  %s\n", array[i]);
    ++i;
    finger = next ? next + 1 : NULL;
  }
  // bootclasspaths
  jdwp_put_u4(&reply_grobag, n);
  for (i = 0; i < n; ++i) {
    woempa(7, "  %s\n", array[i]);
    jdwp_put_cstring(&reply_grobag, array[i], strlen(array[i]));
    releaseMem(array[i]);
  }
  
  releaseMem(array);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Dispose objects
*/

static void jdwp_vm_dispose_objects(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_size count = jdwp_get_u4(cmd->data, &offset);
  w_word objectID;
  w_int refcount;
  w_size i;

  for (i = 0; i < count; ++i) {
    objectID = jdwp_get_u4(cmd->data, &offset);
    refcount = jdwp_get_u4(cmd->data, &offset);
    jdwp_decrement_object_id_refcount(objectID, refcount);
  }
  
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
**
*/

static void jdwp_vm_hold_events(jdwp_command_packet cmd) {
  jdwp_holding_events = 1;
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
**
*/

static void jdwp_vm_release_events(jdwp_command_packet cmd) {
  jdwp_holding_events = 0;
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** The dispatcher for the 'Virtual machine' command set.
*/

void dispatch_vm(jdwp_command_packet cmd) {

  woempa(7, "VM Command = %s\n", cmd->command > 0 && cmd->command <= VM_MAX_COMMAND ? vm_command_names[cmd->command] : "unknown");
  switch((jdwp_vm_cmd)cmd->command) {
    case jdwp_vm_version:
      jdwp_version(cmd);
       break;
    case jdwp_vm_classesBySignature:
      jdwp_vm_classes_by_sig(cmd);
      break;          
    case jdwp_vm_allClasses:
      jdwp_vm_classes(cmd);
       break;
    case jdwp_vm_allThreads:
      jdwp_vm_threads(cmd);
       break;
    case jdwp_vm_topLevelThreadGroups:
      jdwp_vm_threadgroups(cmd);
      break;
    case jdwp_vm_dispose:
      jdwp_vm_disconnect(cmd);
       break;
    case jdwp_vm_idSizes:
      jdwp_vm_sizes(cmd);
      break;
    case jdwp_vm_suspend:
      jdwp_vm_suspend_vm(cmd);
      break;
    case jdwp_vm_resume:
      jdwp_vm_resume_vm(cmd);
      break;
    case jdwp_vm_exit:
      jdwp_vm_terminate(cmd);
      break;
    case jdwp_vm_createString:
      jdwp_create_string(cmd);
      break;
    case jdwp_vm_capabilities:
      jdwp_capabilities(cmd);
      break;
    case jdwp_vm_classPaths:
      jdwp_vm_classpaths(cmd);
      break;
    case jdwp_vm_disposeObjects:
      jdwp_vm_dispose_objects(cmd);
      break;
    case jdwp_vm_holdEvents:
      jdwp_vm_hold_events(cmd);
      break;
    case jdwp_vm_releaseEvents:
      jdwp_vm_release_events(cmd);
       break;
    case jdwp_vm_capabilitiesNew:
      jdwp_capabilities_new(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  } 
}

