/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

#include "arrays.h"        // Array stuff
#include "fields.h"

#include "awt-classes.h"
#include "wpng.h"          // PNG reader

w_void PNGImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imagedata) {
  
  w_thread     thread = currentWonkaThread;
  w_png_image  p;
  w_ubyte      *start = instance2Array_byte(imagedata);
  w_int        *result;
  w_int        length = instance2Array_length(imagedata);
  w_instance   Array;

  threadMustBeSafe(thread);
  // return rgba image
  p = w_png_read(start, (signed) length);

  if(p == NULL || p->data == NULL) {
    woempa(10, "WARNING: could not load PNG image.\n");
    return;
  }

  // Initialize PNGImageSource where possible:
  setIntegerField(thisObject, F_PNGImageSource_width, p->width);
  setIntegerField(thisObject, F_PNGImageSource_height, p->height);

  length = p->width * p->height;
  enterUnsafeRegion(thread);
  Array = allocArrayInstance(thread, atype2clazz[P_int], 1, &length);
  enterSafeRegion(thread);

  if (Array) {
    setReferenceField(thisObject, Array, F_PNGImageSource_pixels);
    result = instance2Array_int(Array);
    w_memcpy(result, p->data, (w_word)(p->width * p->height * 4));
// Not needed    (*env)->DeleteLocalRef(env, Array);
  }
  else {
    woempa(10, "WARNING: could not allocate array to hold image data.\n");
  }
  
  // Release memory:
  releaseMem(p->data);
  releaseMem(p);  
  
  return;
}

