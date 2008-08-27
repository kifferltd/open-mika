/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2008 by Chris Gray, /k/ Embedded Java         *
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

#include <vfs.h>
#include <vfs_fcntl.h>
#include "core-classes.h"
#include "fields.h"
#include "wstrings.h"

/*
** Get the file's name as a C-style UTF8 string. After you've finished with the
** name, call freeFileName() on the result to free up the memory.
*/
char *getFileName(w_instance thisFile) {
  w_instance pathString;
  w_string path;

  pathString = getReferenceField(thisFile, F_File_absname);
  path = String2string(pathString);

  return (char*)string2UTF8(path, NULL) + 2;	  
}

#define freeFileName(n) releaseMem((n)-2)

w_boolean statFile(w_instance thisFile, struct vfs_STAT *statbufptr) {
  char *pathname;
  jboolean result;
  
  pathname = getFileName(thisFile);	  

  result = (vfs_stat(pathname, statbufptr) != -1);

  freeFileName(pathname);

  return result;
}

w_boolean Java_File_exists (JNIEnv *env, w_instance thisFile) {
  struct vfs_STAT statbuf;

  return statFile(thisFile, &statbuf);
}

w_instance Java_File_get_CWD (JNIEnv *env, w_instance thisFile) {
  return newStringInstance(utf2String(current_working_dir, strlen(current_working_dir)));
}

w_instance Java_File_get_fsroot (JNIEnv *env, w_instance thisFile) {
  return newStringInstance(utf2String(fsroot, strlen(fsroot)));
}

w_instance Java_File_list (JNIEnv *env, w_instance thisFile) {
  w_thread thread = JNIEnv2w_thread(env);
  char *pathname;
  int count=0;
  int i;
  w_instance result = NULL;
  char *entryname;
  w_string entry_string;
  w_instance entry;
  vfs_DIR         *dir;
  vfs_dirent      *dirent;
  
  threadMustBeSafe(thread);
  pathname = getFileName(thisFile);	  

  dir = vfs_opendir(pathname);
  
  freeFileName(pathname);

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
        entry = newStringInstance(entry_string);
        setArrayReferenceField(result, entry, i++);
        deregisterString(entry_string);
      }	
    }
  
    vfs_closedir(dir);
  }

  return result;

}

w_boolean File_setReadOnly (JNIEnv *env, w_instance thisFile) {
  char *pathname;
  struct vfs_STAT statbuf;
  jboolean result;
  const int WRITABLE = VFS_S_IWUSR | VFS_S_IWGRP | VFS_S_IWOTH;
  
  pathname = getFileName(thisFile);	  
  result = vfs_stat(pathname, &statbuf) == 0
    && vfs_chmod(pathname, statbuf.st_mode & ~WRITABLE) == 0;

  freeFileName(pathname);
  
  return result;
}

w_boolean Java_File_canRead (JNIEnv *env, w_instance thisFile) {
  struct vfs_STAT statbuf;
  
  return statFile(thisFile, &statbuf) && (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IRUSR) == VFS_S_IRUSR);
}

w_boolean Java_File_canWrite (JNIEnv *env, w_instance thisFile) {
  struct vfs_STAT statbuf;
  
  return statFile(thisFile, &statbuf) && (((statbuf.st_mode & VFS_S_IRWXU) & VFS_S_IWUSR) == VFS_S_IWUSR);
}

w_boolean Java_File_isFile (JNIEnv *env, w_instance thisFile) {
  struct vfs_STAT statbuf;
  
  return statFile(thisFile, &statbuf) && VFS_S_ISREG(statbuf.st_mode);
}

w_boolean Java_File_isDirectory (JNIEnv *env, jobject thisFile) {
  struct vfs_STAT statbuf;
  
  return statFile(thisFile, &statbuf) && VFS_S_ISDIR(statbuf.st_mode);
}

w_long Java_File_lastModified (JNIEnv *env, jobject thisFile) {
  struct vfs_STAT statbuf;

  if (statFile(thisFile, &statbuf)) {
    return ((jlong)statbuf.st_mtime) * ((jlong)1000);
  }
  else {
    return 0;
  }
}

w_long Java_File_length (JNIEnv *env, jobject thisFile) {
  struct vfs_STAT statbuf;
  
  if (statFile(thisFile, &statbuf)) {
    return statbuf.st_size;
  }
  else {
    return 0;
  }
}

w_boolean File_delete (JNIEnv *env, w_instance thisFile) {
  char      *pathname;
  struct vfs_STAT statbuf;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  

  result == (vfs_stat(pathname, &statbuf) == 0);
  if (result) {
    if(VFS_S_ISDIR(statbuf.st_mode)) {
      result = (vfs_rmdir(pathname) == 0);
      woempa(9, "%s is a directory, result = %d\n", pathname, result);
      wprintf("%s is a directory, result = %d\n", pathname, result);
    } else {
      result = (vfs_unlink(pathname) == 0);
      woempa(9, "%s is a file, result = %d\n", pathname, result);
      wprintf("%s is a file, result = %d\n", pathname, result);
    }
  }
  
  freeFileName(pathname);

  return result;

}

w_boolean Java_File_mkdir(JNIEnv *env, jobject thisFile) {
  char *pathname;
  w_boolean result;
  
  pathname = getFileName(thisFile);	  

  result = (vfs_mkdir((w_ubyte *)pathname, VFS_S_IRWXU | VFS_S_IRWXG | VFS_S_IRWXO) != -1);

  freeFileName(pathname);

  return result;
}

/* Header for class File */

#ifndef _Included_File
#define _Included_File
#ifdef __cplusplus
extern "C" {
#endif

static w_instance   fileclazz;
static w_instance   stringclazz;
static w_field absname;

JNIEXPORT void JNICALL Java_File_init
  (JNIEnv *env, w_instance thisClazz) {
  
  fileclazz = thisClazz;
  stringclazz = (*env)->FindClass(env, "java/lang/String");
  absname = (*env)->GetFieldID(env, thisClazz, "absname", "Ljava/lang/String;");
  
}

/*
 * Class:     File
 * Method:    setLastModified
 * Signature: (J)Z
 */

JNIEXPORT jboolean JNICALL Java_File_setModTime(JNIEnv *env, jobject thisObj, jlong modtime) {
  jboolean        isCopy;
  const char      *pathname;
  jstring         path;
  jboolean        result;
  struct utimbuf  buf;

  path = (jstring)(*env)->GetObjectField(env, thisObj, absname);
  pathname = (*env)->GetStringUTFChars(env, path, &isCopy);
  modtime = modtime / 1000;

  buf.actime  = (time_t) modtime; 
  buf.modtime = (time_t) modtime; 


  result = (0 == vfs_utime(pathname, &buf)) ? JNI_TRUE : JNI_FALSE;
  if(isCopy == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, path, pathname);
  return result;
}

/*
 * Class:     File
 * Method:    renameTo
 * Signature: (Ljava/io/File;)Z
 */

JNIEXPORT jboolean JNICALL Java_File_rename(JNIEnv *env, jobject thisObj, jstring file1, jstring file2) {
    
  char      *pathname1;
  char      *pathname2;
  jsize           ulen1 = (*env)->GetStringUTFLength(env, file1);
  jsize           ulen2 = (*env)->GetStringUTFLength(env, file2);
  jsize           slen1 = (*env)->GetStringLength(env, file1);
  jsize           slen2 = (*env)->GetStringLength(env, file2);
  jboolean        result = FALSE;

  pathname1 = allocClearedMem(ulen1 + 1);

  if(!pathname1) {
    return JNI_FALSE;
  }

  pathname2 = allocClearedMem(ulen2 + 1);

  if(!pathname2){
    releaseMem(pathname1);
    return JNI_FALSE;
  }

  (*env)->GetStringUTFRegion(env, file1, 0, slen1, pathname1);	
  (*env)->GetStringUTFRegion(env, file2, 0, slen2, pathname2);	

  if(!(*env)-> ExceptionCheck(env)){
    if (vfs_rename(pathname1, pathname2) == 0) {
      result = JNI_TRUE;
    }
    else {
      result = JNI_FALSE;
    }
  }

  releaseMem(pathname1);
  releaseMem(pathname2);

  return result;

}

#ifdef __cplusplus
}
#endif
#endif
