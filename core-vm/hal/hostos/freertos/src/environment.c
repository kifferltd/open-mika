/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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

//#include <sys/utsname.h>
#include "argument.h"
#include "wstrings.h"

extern char *command_line_path;
extern char *fsroot;

char *getInstallationDir(void) {
  return fsroot;
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
  return "FreeRTOS";
}

char *getOSVersion(void) {
  return "V10.2.1";
}

char *getOSArch(void) {
  return "imsys";
}

char *getUserName(void) {
  return "FreeRTOS";
}

char *getUserHome(void) {
  return "";
}

char *getUserDir(void) {
  return "";
}

char *getLibraryPath(void) {
  return "";
}

static char bitez[4];

char *getUserLanguage(void) {
  return "se";
}


