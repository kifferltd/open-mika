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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: MD5MessageDigest.c,v 1.3 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "wonka.h"
#include "core-classes.h"
#include "ts-mem.h"
#include "md5.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

#include <string.h>

void MD5MessageDigest_md5Clone(JNIEnv *env, w_instance This, w_instance theClone) {
  w_md5Acc md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);
  w_md5Acc md5Clone = allocMD5Acc(JNIEnv2w_thread(env));

  setWotsitField(theClone, F_MD5MessageDigest_wotsit, md5Clone);

  // copy values
  md5Clone->len = md5->len;
  md5Clone->total = md5->total;
  memcpy(md5Clone->sums, md5->sums, 4 * sizeof(w_word));
  memcpy(md5Clone->bytes, md5->bytes, 64 * sizeof(w_ubyte));
  
  setWotsitField(theClone, F_MD5MessageDigest_wotsit, md5Clone);
}

void MD5MessageDigest_engineReset(JNIEnv *env, w_instance This){
  w_md5Acc md5 =  getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 != NULL){
    releaseMem(md5);
  }

  md5 = allocMD5Acc(JNIEnv2w_thread(env));
  setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
}

void MD5MessageDigest_engineUpdate(JNIEnv *env, w_instance This, w_instance Input, w_int offset, w_int len){
  w_thread thread = JNIEnv2w_thread(env);
  w_int length;
  w_ubyte * bytes;
  w_md5Acc md5;

  if(Input == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Input);

  if(offset < 0 || len < 0 || offset > length - len){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 == NULL){
    md5 = allocMD5Acc(thread);
    setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
    if(md5 == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Input)) + offset;

  processMD5Acc(md5, bytes, (w_size)len);

}

void MD5MessageDigest_finalize(JNIEnv *env, w_instance This){
  w_md5Acc md5 =  getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 != NULL){
    releaseMem(md5);
  }
}

void MD5MessageDigest_nativeDigest(JNIEnv *env, w_instance This, w_instance Bytes, w_int off){
  w_thread thread = JNIEnv2w_thread(env);
  w_int length;
  w_ubyte * bytes;
  w_md5Acc md5;
  w_word words[4];

  if(Bytes == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Bytes);

  if(off < 0 || off > length - 16){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 == NULL){
    md5 = allocMD5Acc(thread);
    setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
    if(md5 == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Bytes)) + off;

  finishMD5Acc(md5,words);

  w_memcpy(bytes,words,16);

  releaseMem(md5);

  clearWotsitField(This, F_MD5MessageDigest_wotsit);
}
