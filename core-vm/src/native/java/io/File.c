/**************************************************************************
* Copyright (c) 2021, 2022, 2023 by KIFFER Ltd. All rights reserved.      *
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

#ifdef FREERTOS
#include <ff_stdio.h>
#endif

#include "vfs.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "wordset.h"
#include "wstrings.h"

/*
** Get the file's name as a C-style UTF8 string. After you've finished with the
** name, call releaseMem() on the result to free up the memory.
*/
char *getFileName(w_instance thisFile) {
  w_instance pathString;
  w_string path;

  pathString = getReferenceField(thisFile, F_File_hostpath);
  path = String2string(pathString);

  return w_string2UTF8(path, NULL);	  
}

w_boolean statFile(w_instance thisFile, struct vfs_stat_t *statbufptr) {
  char *pathname;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  

  result = (vfs_stat(pathname, statbufptr) != -1);

  releaseMem(pathname);

  return result;
}

w_boolean File_createNew (w_thread thread, w_instance thisFile) {
  struct vfs_stat_t statbuf;
  struct vfs_FILE *file;
  char *pathname;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  
  result = !!vfs_stat(pathname, &statbuf);
  if (result) {
    file = vfs_open(pathname, VFS_O_CREAT, VFS_S_IRWXU | VFS_S_IRGRP | VFS_S_IROTH);
    if (file) {
      vfs_close(file);
    }
    else {
      throwException(thread, clazzIOException, "could not open file '%s' using mode w+: %s\n", pathname, strerror(errno));
    }
  }

  releaseMem(pathname);

  return result;
}

w_boolean File_exists (w_thread thread, w_instance thisFile) {
  struct vfs_stat_t statbuf;

  return statFile(thisFile, &statbuf);
}

/**
 * Get the current working directory (without any trailing slash).
 * When running under an RTOS this will return NULL, and the Java code will use the value of
 * the user.dir property instead.
 */
w_instance File_get_CWD (w_thread thread, w_instance thisFile) {
#ifdef FREERTOS
  return NULL;
#else
  w_int cwdlen = strlen(current_working_dir);
  if (current_working_dir[cwdlen-1] == '/') {
    --cwdlen;
  }
  return getStringInstance(utf2String(current_working_dir, cwdlen));
#endif
}

w_instance File_get_fsroot (w_thread thread, w_instance thisFile) {
  // special case: fsroot = "/" really means fsroot = "", i.e. "{}/" maps to "/" (not "//")
  w_string fsroot_string = (strlen(fsroot) == 1 && fsroot[0] == '/') ? registerString(string_empty) : utf2String(fsroot, strlen(fsroot));
  return getStringInstance(fsroot_string);
}

#ifdef FREERTOS
// FreeRTOS FAT filesystem uses a different logic for scanning directories:
// ff_findfirst is equivalent to opendir followed by readdir
// TODO can we rewrite the POSIX code to work the same way?

w_instance File_list (w_thread thread, w_instance thisFile) {
  char *pathname;
  int count=0;
  int i;
  w_instance result = NULL;
  char *entryname;
  w_string entry_string;
  w_instance entry;
  w_wordset ws = NULL;
  w_wordset *temp = &ws;
  w_size numberOfFiles = 0;
  struct vfs_stat_t statbuf;
  FF_FindData_t finddata_buffer;

  threadMustBeSafe(thread);
  pathname = getFileName(thisFile);	  
  woempa(1, "dir %s\n", pathname);

  if (ff_stat(pathname, &statbuf) != 0 || statbuf.st_mode != FF_IFDIR) {
    // this is not a directory
    releaseMem(pathname);
    return NULL;
  }

  if (ff_findfirst(pathname, &finddata_buffer ) == 0 ) {
    do {
      const int fileNameLength = strlen(finddata_buffer.pcFileName);
      if (fileNameLength < 1 || finddata_buffer.pcFileName[0] !='.' || finddata_buffer.pcFileName[fileNameLength-1] != 0 || fileNameLength > 2) {
        entryname = allocMem(fileNameLength + 1);
        strcpy(entryname, finddata_buffer.pcFileName);
        woempa(1, "  put entry %s\n", entryname);
      }
      else {
        woempa(1, "Dir entry '%s' is bogus, ignoring\n", finddata_buffer.pcFileName);
      }
      addToWordset(temp, (w_word) entryname);
    } while( ff_findnext(&finddata_buffer) == 0 );
  }
  releaseMem(pathname);
    
  if (!wordsetIsEmpty(temp)) {
    enterUnsafeRegion(thread);
    result = allocArrayInstance_1d(thread, clazzArrayOf_String, sizeOfWordset(temp));
    enterSafeRegion(thread);
  }

  for (i = 0; !wordsetIsEmpty(temp); ++i) {
    entryname = (char*) takeFirstFromWordset(temp);
    woempa(1, "  take entry %s\n", entryname);
    entry_string = utf2String(entryname, strlen(entryname));
    releaseMem(entryname);
    entry = getStringInstance(entry_string);
    setArrayReferenceField(result, entry, i);
    deregisterString(entry_string);
  }
  releaseWordset(temp);

  return result;
}

#else

// POSIX version

w_instance File_list (w_thread thread, w_instance thisFile) {
  char *pathname;
  int count=0;
  int i;
  w_instance result = NULL;
  char *entryname;
  w_string entry_string;
  w_instance entry;
  vfs_dir_t       *dir;
  vfs_dirent      *dirent;
  
  threadMustBeSafe(thread);
  pathname = getFileName(thisFile);	  

  dir = vfs_opendir(pathname);
  
  releaseMem(pathname);

  if(dir != NULL) {

    /* Count the entries */
    while((dirent = vfs_readdir(dir)) != NULL) count++;
    
    /* Substract 2 for . and .. */
    count -= 2;

    if(count < 0) {
      count = 0;
      woempa(9, "!!! %s is a corrupt directory, attempting to read it anyway...\n", pathname);
      woempa(9, "!!! It's recommended do to a fsck -f on this partition as soon as possible to prevent further corruption\n");
    }
    
    enterUnsafeRegion(thread);
    result = allocArrayInstance_1d(thread, clazzArrayOf_String, count);
    enterSafeRegion(thread);

    /* Rewind the directory */
    vfs_rewinddir(dir);
    i = 0;
    
    /* Fill up the array - don't trust vsf_readdir not to return more elements */
    while((dirent = vfs_readdir(dir)) != NULL && i < count) {
      entryname = dirent->d_name;
      if (strcmp(entryname, ".") && strcmp(entryname, "..")) {
        entry_string = utf2String(entryname, strlen(entryname));
        entry = getStringInstance(entry_string);
        setArrayReferenceField(result, entry, i++);
        deregisterString(entry_string);
        removeLocalReference(thread, entry);
      }	
    }
  
    vfs_closedir(dir);
  }

  return result;

}
#endif

w_boolean File_setReadOnly (w_thread thread, w_instance thisFile) {
  char *pathname;
  struct vfs_stat_t statbuf;
  w_boolean result;
  const int WRITABLE = VFS_S_IWUSR | VFS_S_IWGRP | VFS_S_IWOTH;
  
  pathname = getFileName(thisFile);	  
  result = vfs_stat(pathname, &statbuf) == 0
    && vfs_chmod(pathname, statbuf.st_mode & ~WRITABLE) == 0;

  releaseMem(pathname);
  
  return result;
}

w_boolean File_canRead (w_thread thread, w_instance thisFile) {
  struct vfs_stat_t statbuf;
  
  return statFile(thisFile, &statbuf) && (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IRUSR) == VFS_S_IRUSR);
}

w_boolean File_canWrite (w_thread thread, w_instance thisFile) {
  struct vfs_stat_t statbuf;
  
  return statFile(thisFile, &statbuf) && (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IWUSR) == VFS_S_IWUSR);
}

w_boolean File_isFile (w_thread thread, w_instance thisFile) {
  struct vfs_stat_t statbuf;
  
  return statFile(thisFile, &statbuf) && VFS_S_ISREG(statbuf.st_mode);
}

w_boolean File_isDirectory (w_thread thread, jobject thisFile) {
  struct vfs_stat_t statbuf;
  
  return statFile(thisFile, &statbuf) && VFS_S_ISDIR(statbuf.st_mode);
}

w_long File_lastModified (w_thread thread, jobject thisFile) {
  struct vfs_stat_t statbuf;

/*
TODO sort out why  ffconfigTIME_SUPPORT == 1 and yet no st_mtime in statbuf
#if ( ffconfigTIME_SUPPORT == 1 )
  if (statFile(thisFile, &statbuf)) {
    return ((jlong)statbuf.st_mtime) * ((jlong)1000);
  }
*/
    return 0;
}

w_long File_length (w_thread thread, jobject thisFile) {
  struct vfs_stat_t statbuf;
  
  if (statFile(thisFile, &statbuf)) {
    return statbuf.st_size;
  }
  else {
    return 0;
  }
}

w_boolean File_delete (w_thread thread, w_instance thisFile) {
  char      *pathname;
  struct vfs_stat_t statbuf;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  

  result = (vfs_stat(pathname, &statbuf) == 0);
  if (result) {
    if(VFS_S_ISDIR(statbuf.st_mode)) {
      result = (vfs_rmdir(pathname) == 0);
      woempa(9, "%s is a directory, result = %d\n", pathname, result);
    } else {
      result = (vfs_unlink(pathname) == 0);
      woempa(9, "%s is a file, result = %d\n", pathname, result);
    }
  }
  
  releaseMem(pathname);

  return result;

}

w_boolean File_mkdir(w_thread thread, jobject thisFile) {
  char *pathname;
  int rc;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  

  rc = vfs_mkdir(pathname, VFS_S_IRWXU | VFS_S_IRWXG | VFS_S_IRWXO);
  result = (rc != -1);

  releaseMem(pathname);

  return result;
}

w_boolean File_setModTime(w_thread thread, w_instance thisFile, w_long modtime) {
  char *pathname;
  w_boolean        result;
  struct utimbuf  buf;

  pathname = getFileName(thisFile);
  modtime = modtime / 1000;

  buf.actime  = (time_t) modtime; 
  buf.modtime = (time_t) modtime; 

  result = (0 == vfs_utime(pathname, &buf));

  releaseMem(pathname);

  return result;
}

w_boolean File_rename(w_thread thread, w_instance thisObj, w_instance file1String, w_instance file2String) {
    
  w_string file1_string;
  w_string file2_string;
  char *pathname1;
  char *pathname2;
  w_int pathlength1;
  w_int pathlength2;
  w_boolean        result = FALSE;

  if (!file1String || !file2String) {
    throwException(thread, clazzNullPointerException, NULL);
  }

  file1_string = String2string(file1String);
  file2_string = String2string(file2String);
  pathname1 = w_string2UTF8(file1_string, &pathlength1);
  pathname2 = w_string2UTF8(file2_string, &pathlength2);

  result = (vfs_rename(pathname1, pathname2) == 0);

  releaseMem(pathname1);
  releaseMem(pathname2);

  return result;

}

