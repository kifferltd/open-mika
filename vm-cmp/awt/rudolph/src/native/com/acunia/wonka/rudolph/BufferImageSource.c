/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java     *
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

#include <string.h>
#include <stdio.h>

#include "arrays.h"
#include "fields.h"

#include "awt-classes.h"
#include "canvas.h"
#include "Image.h"

w_void BufferImageSource_getImageData(JNIEnv *env, jobject thisObject, jobject imageInstance) { 

  w_instance Array;
  w_thread thread = currentWonkaThread;
  r_image  image = getWotsitField(imageInstance, F_Image_wotsit);
  w_int    w = image->w;
  w_int    h = image->h;
  w_int    length = w * h;
  w_int    *result;
  w_int    x;
  w_int    y;
 
  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Array = allocArrayInstance(thread, atype2clazz[P_int], 1, &length);
  enterSafeRegion(thread);

  if (Array) {
    setReferenceField(thisObject, Array, F_BufferImageSource_pixels);
    result = instance2Array_int(Array);
    for (y = 0; y < h; y++) {
      for (x = 0; x < w; x++) {
        *result = pixelget(image->buffer->data, image->buffer->fw, x, y);
        result++;
      }
    }
// Not needed, surely?   (*env)->DeleteLocalRef(env, Array);
  }
  
  return;

}

