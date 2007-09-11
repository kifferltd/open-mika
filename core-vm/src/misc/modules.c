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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include "misc.h"
#include "oswald.h"
#include "wonka.h"
#include "ts-mem.h"

#include <stdio.h>  /* for sprintf */
#include <string.h> /* for strlen */
#include <unistd.h>

char *loading_problem;

#ifndef SHARED_OBJECTS

#ifdef OSWALD

#include "modules.h"
#include "vfs.h"

void *loadModule(char * name, char * path) {
  
  x_status         status;
  x_ubyte          *elfdata;
  int              fd;
  struct vfs_STAT  statbuf;
  w_word           elfsize;
  x_Module         Module;
  x_module         module = &Module;
  char             *filename;
  
  filename = allocMem(strlen(name) + 12);
  // TODO : get the path from somewhere sensible
  sprintf(filename, "./system/%s.o", name);
  
  if(vfs_stat(filename, &statbuf) != -1) { 
    elfsize = statbuf.st_size;
    woempa(9, "%s is %d bytes\n", filename, elfsize);
    elfdata = allocMem(elfsize);
    fd = vfs_open(filename, VFS_O_RDONLY, 0);
    vfs_read(fd, elfdata, elfsize);
    vfs_close(fd);
    releaseMem(filename);
  
    status = x_module_load(module, elfdata, x_mem_alloc, x_mem_free);
    if(status != xs_success) {
      woempa(9, "Loading of lib %s failed... (%d)\n", name, status);
      releaseMem(elfdata);
      return NULL;
    }

    status = x_module_resolve(module);
    if(status != xs_success) {
      woempa(9, "Resolving of lib %s failed... (%d)\n", name, status);
      releaseMem(elfdata);
      return NULL;
    }
  
    status = x_module_relocate(module);
    if(status != xs_success) {
      woempa(9, "Relocating of lib %s failed... (%d)\n", name, status);
      releaseMem(elfdata);
      return NULL;
    }
  
    status = x_module_init(module);
    if(status != xs_success) {
      woempa(9, "Initialization of lib %s failed... (%d)\n", name, status);
      releaseMem(elfdata);
      return NULL;
    }
  
  } else {
    releaseMem(filename);
    woempa(9, "Could not find lib %s.\n", name);
    return NULL;
  }

  return (void *)module;
}

x_boolean searchForSymbol(x_module module, void * argument) {
  return 1;
}

void *lookupModuleSymbol(char *name) {
 /* Iterate through all the loaded modules to find the symbol */
  x_module_iterate(searchForSymbol, name);
  return NULL; 
}

#else /* !OSWALD */

void *loadModule(char *name, char *path) { return NULL; }
void *lookupModuleSymbol(char *name) { return NULL; }

#endif /* OSWALD */

#else /* SHARED_OBJECTS */

#include "vfs.h"
#include <dlfcn.h>

static void **handles = NULL;
static void **current = NULL;
extern char *fsroot;

static char *buildLibPath(char *path, int pathlen, char *filename, int filenamelen) {
  int fsrooted = (pathlen >= 3 && path[0] == '{' && path[1] == '}' && path[2] == '/');
  int fsrootlen = strlen(fsroot);
  char *result = x_mem_alloc(pathlen + filenamelen + (fsrooted ? fsrootlen : 2));
  int l = 0;

  if (result) {
    if (fsrooted) {
      strcpy(result, fsroot);
      l = fsrootlen;
      if (fsroot[fsrootlen - 1] != '/') {
        result[l++] = '/';
      }
      strcpy(result + l, path + 3);
      l += pathlen - 3;
    }
    else {
      strcpy(result + l, path);
      l += pathlen;
    }
    if (path[pathlen - 1] != '/') {
      result[l++] = '/';
    }
    strcpy(result + l, filename);
  }

  return result;
}

/*
 * Call the JNI_OnLoad function if it exists.
 */
void callOnLoad(void *handle) {
  void *sym;
  jint  (*function_OnLoad)(JavaVM*,void*);
  jint version; // TODO use this for something?
  JNIEnv *env;
  JavaVM *vm;

  sym = dlsym(handle, "JNI_OnLoad");
  function_OnLoad = sym;
  if (!function_OnLoad) {

    return;

  }
  env = w_thread2JNIEnv(currentWonkaThread);
  if ((*env)->GetJavaVM(env, &vm) != 0) {

    return;

  }
  version = function_OnLoad(vm, NULL);

  if ((version & 0xffff0000) != 0x00010000 || (version & 0x0000ffff) == 0 || (version & 0x0000ffff) > 4) {
    // TODO: refuse to load library
  }
}

void *loadModule(char *name, char *path) {
  char *filename = NULL;
  void *handle = NULL;
  int  offset = current - handles;
  char *orig_ld = NULL;
  char *ld_start = NULL;
  char *ld_end;
  char *ld_segment;
  char *chptr;
  char *libPath = NULL;

  if (!handles) {
    woempa(7, "No handles array allocated yet, allocating array of 10\n");
    handles = x_mem_alloc(10 * sizeof(void *));
    current = handles;
  }
  else if((offset % 10) == 0) {
    woempa(7, "Size of handles array is now %d, expanding to %d\n", offset, offset + 10);
    handles = x_mem_realloc(handles, (offset + 10) * sizeof(void *));
    current = handles + offset;
  }

  if(name) {
  // 'name' is non-null, must search path
    filename = name;

    woempa(7, "Module name is %s, path is %s\n", filename, path);

    if (path) {
      // search path is given
      orig_ld = getenv("LD_LIBRARY_PATH");
      if(orig_ld) {
        woempa(7, "Appending path to existing LD_LIBRARY_PATH = %s\n", orig_ld);
        ld_start = x_mem_calloc(strlen(orig_ld) + strlen(path) + 2);
	ld_end = ld_start + strlen(orig_ld) + strlen(path) + 1;
        sprintf(ld_start, "%s:%s", orig_ld, path);
      }
      else {
        woempa(7, "No existing LD_LIBRARY_PATH\n");
        ld_start = x_mem_calloc(strlen(path) + 1);
	ld_end = ld_start + strlen(path);
	strcpy(ld_start, path);
      }
      woempa(9, "new LD_LIBRARY_PATH = %s\n", ld_start);
      // try each element in the ld_start path
      ld_segment = ld_start;
      while (!handle && ld_segment < ld_end) {
        chptr = strchr(ld_start, ':');
        if (chptr == NULL) {
          libPath = buildLibPath(ld_segment, ld_end - ld_segment, filename, strlen(filename));
          woempa(9, "Calling dlopen on %s\n", libPath);
          handle = dlopen(libPath, RTLD_LAZY | RTLD_GLOBAL);
          x_mem_free(libPath);
          break;
	}
	else {
          *chptr = 0;
          libPath = buildLibPath(ld_segment, chptr - ld_segment, filename, strlen(filename));
          woempa(9, "Calling dlopen on %s\n", libPath);
          handle = dlopen(libPath, RTLD_LAZY | RTLD_GLOBAL);
          x_mem_free(libPath);
          ld_segment = ++chptr;
	}
      }
      x_mem_free(ld_start);
    }
    else {
    // no search path given, LD_LIBRARY_PATH will be used
      woempa(7, "LD_LIBRARY_PATH = %s\n", getenv("LD_LIBRARY_PATH"));
      handle = dlopen(filename, RTLD_LAZY | RTLD_GLOBAL);
      woempa(7, "handle = %p\n", handle);
    }
  }
  else if(path) {
    woempa(7, "path = %s\n", path);
    filename = path;
    handle = dlopen(filename, RTLD_LAZY | RTLD_GLOBAL);
    woempa(7, "handle = %p\n", handle);
  }
  
  if(handle) {
    *current++ = handle;
    woempa(7, "Added handle %p to list, now have %d entries\n", handle, current - handles);
    callOnLoad(handle);
  }
  else {
    loading_problem = dlerror();
    woempa(9, "filename: %s, dlerror() : %s\n", filename, loading_problem);
  }

  return handle;
}

void *lookupModuleSymbol(char *name) {
  void *symbol = NULL;
  void **check = handles;

  woempa(9, "%s\n", name);

  while(symbol == NULL && check != current) {
    symbol = dlsym(*check++, name);
    if (!symbol) {
      woempa(9, "symbol: %s, dlerror() : %s\n", name, dlerror());
    }
  }
  
  return symbol;
}

#endif /* SHARED_OBJECTS */ 

