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

#include "vfs.h"

char *current_working_dir;
char *current_root_dir;

#ifdef DEBUG
static void dumpDir(const char *path, int level) {
  woempa(7, "%*sScanning directory %s", level * 4, "", path);
  FF_FindData_t *findData = allocMem(sizeof(FF_FindData_t));
  int rc = ff_findfirst(path, findData );
  while (rc == 0) {
    woempa(7, "%*s%s [%s %s] [size=%d]", level * 4, "", findData->pcFileName, (findData->ucAttributes & FF_FAT_ATTR_DIR) ? "DIR" : "", (findData->ucAttributes && FF_FAT_ATTR_DIR) ? "RO" : "", findData->ulFileSize);
    if ((findData->ucAttributes & FF_FAT_ATTR_DIR) && strcmp(findData->pcFileName, ".") && strcmp(findData->pcFileName, "..")) {
      char *pathbuf = allocMem(strlen(path) + strlen(findData->pcFileName) + 2);
      sprintf(pathbuf, "%s%s/", path, findData->pcFileName);
      dumpDir(pathbuf, level + 1);
      releaseMem(pathbuf);
    }
    rc =  ff_findnext( findData ) == 0;
  }
  releaseMem(findData);
  woempa(7, "%*sEnd of directory %s", level * 4, "", path);
}
#endif

void init_vfs(void) {
  woempa(9, "init_vfs  -> Using native filesystem (BLOCKING)\n"); 
  vfs_flashDisk = FFInitFlash("/", FLASH_CACHE_SIZE);
  cwdbuffer = allocClearedMem(MAX_CWD_SIZE);
  current_working_dir = ff_getcwd(cwdbuffer, MAX_CWD_SIZE);
  if (!current_working_dir) {
    woempa(9, "ff_getcwd returned NULL, errno = %d\n", errno);
  }
  current_working_dir = reallocMem(cwdbuffer, strlen(cwdbuffer) + 1);
  current_root_dir = fsroot;
  woempa(9, "current dir  : %s\n", current_working_dir);
  woempa(9, "current root : %s\n", current_root_dir);

#ifdef DEBUG
  dumpDir(current_root_dir, 0);
#endif
}

