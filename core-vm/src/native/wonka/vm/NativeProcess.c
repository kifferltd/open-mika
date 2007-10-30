/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: NativeProcess.c,v 1.5 2006/10/04 14:24:17 cvsroot Exp $
*/

#include "wonka.h"
#include "core-classes.h"
#include "exec.h"
#include "fields.h"
#include "threads.h"
#include "exception.h"
#include "arrays.h"
#include "wstrings.h"
#include "vfs.h"
#include <unistd.h>

static char *convertPath(w_string p) {
  int n;
  char *utf8 = (char*)string2UTF8(p, &n);
  char *c;

  if(utf8 == NULL) {
    return NULL;
  }

  // The first 2 chars of utf8 are the length (which we don't use)
  n -= 2;
  if (n >= 3 && utf8[2] == '{' && utf8[3] == '}' && utf8[4] == '/') {
    char * chptr;
    int m = strlen(fsroot);

    c = allocMem(m + n - 1);

    if(c == NULL) {
      return NULL;
    }

    strcpy(c, fsroot);
    chptr = c + m;
    if (chptr[-1] != '/') {
      *chptr++ = '/';
    }
    strcpy(chptr, utf8 + 5);
    chptr += n - 3;
    *chptr = '\0';
  }
  else {
    c = allocMem(n + 1);
    strcpy(c, utf8 + 2);
  }

  releaseMem(utf8);

  return c;
}

w_instance NativeProcess_exec(JNIEnv* jnienv, w_instance thisObj, w_instance cmdArray, w_instance envArray, w_instance pathString) {
  w_thread    thread = JNIEnv2w_thread(jnienv);
  w_int       cmdlength = 0; 
  w_int       envlength = 0; 
  w_int       i;
  char     **cmd = NULL;
  char     **env = NULL;
  char     *path = NULL;
  w_string    string;
  w_instance  process = NULL;
  w_int       pid;
  w_void*     wpid = NULL;

  /*
  ** Check if there's a command to execute.
  */

  if(cmdArray == NULL) {
    throwException(thread,clazzNullPointerException,NULL);
    return process;
  }

  if(instance2Array_length(cmdArray) == 0) {
    throwException(thread,clazzIndexOutOfBoundsException,"empty array(lenght = 0)");
    return process;
  }

  /*
  ** Turn the cmd String array into a plain old char array (Last element should be NULL).
  */

  cmdlength = instance2Array_length(cmdArray);
  cmd = allocMem((cmdlength + 1) * sizeof(w_word));
  if(cmd == NULL) {
    return NULL;
  }

  for(i = 0; i < cmdlength; i++) {
    w_instance instance = instance2Array_instance(cmdArray)[i];
    if(instance == NULL) {
      cmdlength = i;
      throwException(thread,clazzNullPointerException,NULL);
      goto clean_up;
    }
    string = String2string(instance);
    cmd[i] = convertPath(string);
    if(cmd[i] == NULL) {
      cmdlength = i;
      goto clean_up;
    }
  }
  cmd[cmdlength] = 0;

  if(strlen(cmd[0]) == 0) {
    throwException(thread,clazzIllegalArgumentException,"empty command");
    goto clean_up;
  }

  /*
  ** Turn the env String array into a plain old char array (Last element should be NULL).
  ** If envArray is NULL, there's no environment.
  */

  if(envArray != NULL) {
    envlength = instance2Array_length(envArray);
    env = allocMem((envlength + 1) * sizeof(char*));

    if(env == NULL) {
      goto clean_up;
    }

    for(i = 0; i < envlength; i++) {
      w_instance instance = instance2Array_instance(envArray)[i];
      if(instance == NULL) {
        envlength = i;
        throwException(thread,clazzNullPointerException,NULL);
        goto clean_up;
      }
      string = String2string(instance);
      env[i] = convertPath(string);
      if(env[i] == NULL) {
        envlength = i;
        goto clean_up;
      }
    }
    env[envlength] = 0;
  }

  /*
  ** Get the working directory (if there's one).
  */

  if(pathString != NULL) {
    string = String2string(pathString); 
    path = convertPath(string);
    if(path == NULL) {
      goto clean_up;
    }
  }

  /*
  ** Call host_exec.
  */

  wpid = host_exec(cmd, env, path, &pid);

  if(wpid == NULL) {
    goto clean_up;
  }

  /*
  ** Instantiate a Process object.
  */

  mustBeInitialized(clazzProcessInfo);
  enterUnsafeRegion(thread);
  process = allocInstance_initialized(thread, clazzProcessInfo);
  enterSafeRegion(thread);

  if (process) {
    setIntegerField(process, F_ProcessInfo_wotsit, (w_int) wpid);
    setIntegerField(process, F_ProcessInfo_id, pid);
  } else {
    host_destroy(wpid);
    host_close(wpid);
  }

  /*
  ** Clean up.
  */
clean_up:
  for(i = 0; i < cmdlength; i++) releaseMem(cmd[i]);
  for(i = 0; i < envlength; i++) releaseMem(env[i]);
  if(cmd != NULL) releaseMem(cmd);
  if(env != NULL) releaseMem(env);
  if(path != NULL) releaseMem(path);

  return process;
}

#include <stdio.h>
w_int ProcessInputStream_read(JNIEnv *env, w_instance thisInstance) {
  w_int result;
  w_void* pid;
  char buffer;
  w_instance info = getReferenceField(thisInstance, F_ProcessInputStream_info);

  if(info == NULL) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"stream closed");
    return -1;
  }

  pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);

  result = getBooleanField(thisInstance,F_ProcessInputStream_input) == WONKA_TRUE ?
      host_read_in(pid,&buffer,1) : host_read_err(pid,&buffer,1);

  if(result == EXECUTION_ERROR){
    w_thread thread = JNIEnv2w_thread(env);
    throwException(thread,clazzIOException,NULL);
  }
  return buffer;
}

w_int ProcessInputStream_read_Array(JNIEnv *env, w_instance thisInstance, w_instance Array, w_int offset, w_int length) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int result = 0;
  w_void* pid;
  w_instance info = getReferenceField(thisInstance, F_ProcessInputStream_info);

  if(info == NULL) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"stream closed");
    return result;
  }
  if(Array == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }
  else if(offset < 0 || length < 0 || (offset + length > instance2Array_length(Array))){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }
  else if(length > 0) {
    char* buffer = (char*) instance2Array_byte(Array) + offset;
    pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);

    result = getBooleanField(thisInstance,F_ProcessInputStream_input) == WONKA_TRUE ?
      host_read_in(pid, buffer, length) : host_read_err(pid, buffer, length);
    if(result == EXECUTION_ERROR){
      throwException(thread,clazzIOException,NULL);
    }
  }
  return result;
}

w_int ProcessInputStream_available(JNIEnv *env, w_instance thisInstance) {
  w_void* pid;
  w_int result;

  w_instance info = getReferenceField(thisInstance, F_ProcessInputStream_info);

  if(info == NULL) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"stream closed");
    return -1;
  }
  pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);
  result = getBooleanField(thisInstance,F_ProcessInputStream_input) == WONKA_TRUE ?
     host_available_in(pid) : host_available_err(pid);
  if(result == EXECUTION_ERROR) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"I/O error !");
  }

  return result;
}

w_void ProcessOutputStream_write(JNIEnv *env, w_instance thisInstance, w_int byte) {
  w_ubyte buffer = byte;
  w_void* pid;
  w_instance info = getReferenceField(thisInstance, F_ProcessOutputStream_info);
  if(info == NULL) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"stream closed");
    return;
  }
  pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);
  if(host_write(pid, &buffer, 1) == EXECUTION_ERROR) {
    throwException(JNIEnv2w_thread(env),clazzIOException,"I/O error !");
  }
}

w_void ProcessOutputStream_close(JNIEnv *env, w_instance thisInstance) {
  w_instance info = getReferenceField(thisInstance, F_ProcessOutputStream_info);
  if(info) {
    w_void* pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);
    host_close_out(pid); 
  }
}

w_void ProcessOutputStream_write_Array(JNIEnv *env, w_instance thisInstance, 
      w_instance Array, w_int offset,w_int length) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance info = getReferenceField(thisInstance, F_ProcessOutputStream_info);
  if(info == NULL) {
    throwException(thread,clazzIOException,"stream closed");
    return;
  }

  if(Array == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }
  else if(offset < 0 || length < 0 || (offset + length > instance2Array_length(Array))){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }
  else if(length > 0) {
    w_ubyte* buffer = (w_ubyte*) instance2Array_byte(Array) + offset;
    w_void* pid = (w_void*) getIntegerField(info, F_ProcessInfo_wotsit);
    if(host_write(pid, buffer, length) == EXECUTION_ERROR) {
      throwException(thread,clazzIOException,"I/O error !");
    }
  }
}

w_int ProcessMonitor_WaitForAll(JNIEnv *env, w_instance thisInstance) {
  w_int retval;
  w_int pid = host_wait_for_all(&retval);

  setIntegerField(thisInstance, F_ProcessMonitor_returnvalue, retval);
  return pid;
}

w_void ProcessInfo_cleanUp(JNIEnv *env, w_instance thisInstance) {
  w_void* pid = (w_void*)getIntegerField(thisInstance, F_ProcessInfo_wotsit);

  if(pid) {
    host_close(pid);
    //w_dump("cleaning up %p\n",thisInstance);
    setIntegerField(thisInstance, F_ProcessInfo_wotsit, 0);
  }
}

w_void ProcessInfo_destroy(JNIEnv *env, w_instance thisInstance) {
   w_void* pid = (w_void*) getIntegerField(thisInstance, F_ProcessInfo_wotsit);

  if(pid != NULL && (getBooleanField(thisInstance, F_ProcessInfo_destroyed)== WONKA_FALSE)) {
    host_destroy(pid);
    setBooleanField(thisInstance, F_ProcessInfo_destroyed, WONKA_TRUE);
  }
}

w_void ProcessInfo_setReturnValue (JNIEnv *env, w_instance thisInstance, w_int retval) {
   w_void* pid = (w_void*) getIntegerField(thisInstance, F_ProcessInfo_wotsit);

  if(pid != NULL) {
    host_setreturnvalue(pid, retval);
  }
}
