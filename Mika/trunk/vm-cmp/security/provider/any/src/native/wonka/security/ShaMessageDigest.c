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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: ShaMessageDigest.c,v 1.4 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "wonka.h"
#include "core-classes.h"
#include "ts-mem.h"
#include "sha.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

#include <string.h>

void ShaMessageDigest_shaClone(JNIEnv *env, w_instance This, w_instance theClone) {
    w_sha sha = getWotsitField(This, F_ShaMessageDigest_wotsit);
    w_sha shaClone = allocSha();

    setWotsitField(theClone, F_ShaMessageDigest_wotsit, shaClone);

    // copy values
    shaClone->buffered = sha->buffered;
    shaClone->lo_length = sha->lo_length;
    shaClone->hi_length = sha->hi_length;
    memcpy(shaClone->buffer.W, sha->buffer.W, sizeof(w_word) * 80);   
    memcpy(shaClone->signature.W, sha->signature.W, sizeof(w_word) * 5);   
    
    setWotsitField(theClone, F_ShaMessageDigest_wotsit, shaClone);
}

void ShaMessageDigest_engineReset(JNIEnv *env, w_instance This){
  w_sha sha =  getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha != NULL){
    releaseMem(sha);
  }

  sha = allocSha();

  setWotsitField(This, F_ShaMessageDigest_wotsit, sha);

}

void ShaMessageDigest_engineUpdate(JNIEnv *env, w_instance This, w_instance Input, w_int offset, w_int len){
  w_thread thread = JNIEnv2w_thread(env);
  w_int length;
  w_ubyte * bytes;
  w_sha sha;

  if(Input == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Input);

  if(offset < 0 || len < 0 || offset > length - len){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  sha = getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha == NULL){
    sha = allocSha();
    setWotsitField(This, F_ShaMessageDigest_wotsit, sha);
    if(sha == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Input)) + offset;

  processSha(sha, bytes, (w_size)len);

}

void ShaMessageDigest_finalize(JNIEnv *env, w_instance This){
  w_sha sha =  getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha != NULL){
    releaseMem(sha);
  }
}

void ShaMessageDigest_nativeDigest(JNIEnv *env, w_instance This, w_instance Bytes, w_int off){
  w_thread thread = JNIEnv2w_thread(env);
  w_int length;
  w_ubyte * bytes;
  w_sha sha;
  w_word word;

  if(Bytes == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Bytes);

  if(off < 0 || off > length - 20){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  sha = getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha == NULL){
    sha = allocSha();
    setWotsitField(This, F_ShaMessageDigest_wotsit, sha);
    if(sha == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Bytes)) + off;

  finishSha(sha);

  /** TODO make sure this still works on a BIG ENDIAN platform */
  word = sha->signature.W[0];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[1];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[2];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[3];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[4];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;


  releaseMem(sha);

  clearWotsitField(This, F_ShaMessageDigest_wotsit);
}

