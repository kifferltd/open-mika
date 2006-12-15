/**************************************************************************
*                                                                         *
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: environment.c,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

#include <sys/utsname.h>
#include "argument.h"
#include "wstrings.h"

char *getInstallationDir(void) {
  return "unknown";
}

char *getExtensionDir(void) {
  return "unknown";
}


char *getOSName(void) {
  return "unknown";
}

char *getOSVersion(void) {
  return "unknown";
}

char *getOSArch(void) {
  return "unknown";
}

char *getUserName(void) {
  return "unknown";
}

char *getUserHome(void) {
  return "unknown";
}

char *getUserDir(void) {
  return "unknown";
}

char *getLibraryPath(void) {
  return "unknown";
}



