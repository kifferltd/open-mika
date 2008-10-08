/**************************************************************************
*                                                                         *
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: environment.c,v 1.4 2006/10/04 14:24:14 cvsroot Exp $
*/

#include <sys/utsname.h>
#include "argument.h"
#include "wstrings.h"

extern char *command_line_path;
extern char *fsroot;

static struct utsname uname_buffer;
static int uname_called = FALSE;

char *getInstallationDir(void) {
  w_int len = strlen(command_line_path);
  w_int skip = 0;
  w_int offset = 0;
  w_int m;
  char *bytes;

  if(len == 0 || command_line_path[0] == '/') {
    bytes = allocMem(len+1);
    memcpy(bytes, command_line_path, len);
  } else {
	  m = 32 + len;
	  bytes = allocClearedMem(m);
	  while (!getcwd(bytes, m - 2 - len)) {
	    m *= 2;
	    bytes = reallocMem(bytes, m);
	    memset(bytes, 0, m);
	  }
	  woempa(1, "cwd is '%s', command_line_path is %s\n", bytes, command_line_path);
	 
	  offset = strlen(bytes);
	 
	  if (len > 1 && command_line_path[0] == '.' && command_line_path[1] == '/') {
	   skip = 2;
	  }
	  else if (len > 2 && command_line_path[0] == '.' && command_line_path[1] == '.' && command_line_path[2] == '/') {
	    skip = 3;
	    while (bytes[--offset] != '/');
	  }
	 
	  woempa(1, "offset is %d, skip is %d\n", offset, skip);
	  bytes[offset] = '/';
	  memcpy(bytes + offset + 1, command_line_path + skip, len - skip);
	  len = offset + len - skip;
  }

  bytes[len] = 0;
  // We report two levels up from command_line_path
  woempa(1, "command_line_path is '%s' (%d chars)\n", bytes, len);
  while (len && bytes[--len] != '/') {
    woempa(1, "byte[%d] is not '/', decrementing length\n", len);
  }
  if (len) {
    --len;
    while (len && bytes[--len] != '/') {
      woempa(1, "byte[%d] is not '/', decrementing length\n", len);
    }
  }
  bytes[len] = 0;
  woempa(1, "returning %s\n", bytes);

  return bytes;
}

char *getExtensionDir(void) {
  char *result = NULL;

#ifdef EXTCLASSDIR
  result = EXTCLASSDIR;
  if (strlen(result) >= 3 && result[0] == '{' && result[1] == '}' && result[2] == '/') {
    int pathlen = strlen(result);
    char *path = result;
    int fsrootlen = strlen(fsroot);
    int l;

    result = allocMem(pathlen + fsrootlen);
    strcpy(result, fsroot);
    l = fsrootlen;
    if (fsroot[fsrootlen - 1] != '/') {
      result[l++] = '/';
    }
    strcpy(result + l, path + 3);
    l += pathlen - 3;
    result[l] = 0;
  }
#endif

  return result;
}


char *getOSName(void) {
  if (!uname_called) {
    uname(&uname_buffer);
    uname_called = TRUE;
  }

  return uname_buffer.sysname;
}

char *getOSVersion(void) {
  if (!uname_called) {
    uname(&uname_buffer);
    uname_called = TRUE;
  }

  return uname_buffer.release;
}

char *getOSArch(void) {
  if (!uname_called) {
    uname(&uname_buffer);
    uname_called = TRUE;
  }

  return uname_buffer.machine;
}

char *getUserName(void) {
  char *bytes = getenv("USER");

  if(!bytes){
    bytes = "";
  }

  return bytes;
}

char *getUserHome(void) {
  char *bytes = getenv("HOME");

  if(!bytes){
    bytes = "";
  }

  return bytes;
}

char *getUserDir(void) {
  char *bytes = getenv("PWD");

  if(!bytes){
    bytes = "";
  }

  return bytes;
}

char *getLibraryPath(void) {
  char *bytes = getenv("LD_LIBRARY_PATH");

  if(!bytes){
    bytes = "";
  }

  return bytes;
}

static char bitez[4];

char *getUserLanguage(void) {
  char *bytes = getenv("LANG");

  if (!bytes) {
    bytes = "en";
  }
  else if (strlen(bytes) > 2){
    bitez[0] = bytes[0]; 
    bitez[1] = bytes[1]; 
    bitez[2] = 0; 
    bytes = bitez;
  }

  return bytes;
}


