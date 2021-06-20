/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2005, 2007 by Chris Gray, /k/ Embedded Java         *
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

#include "awt-classes.h"  // F_Component_*, F_Graphics_*
#include "fields.h"
#include "mika_threads.h"       // currentWonkaThread
#include "heap.h"
#include "arrays.h"        // Array stuff

#ifdef JPEG_SUPPORT

#include "jpeg/jpeglib.h"
#include <setjmp.h>

typedef struct {
  struct jpeg_source_mgr pub;	
  w_ubyte *data;
  w_int length;
} my_source_mgr;

typedef my_source_mgr * my_src_ptr;

struct my_error_mgr {
  struct jpeg_error_mgr pub;
  jmp_buf setjmp_buffer;
};

typedef struct my_error_mgr * my_error_ptr;

void my_error_exit (j_common_ptr cinfo) {
  my_error_ptr myerr = (my_error_ptr)cinfo->err;

  /* Always display the message. */
  (*cinfo->err->output_message) (cinfo);

  /* Return control to the setjmp point */
  longjmp(myerr->setjmp_buffer, 1);
}

void init_source(j_decompress_ptr cinfo) {
}

int fill_input_buffer(j_decompress_ptr cinfo) {
  return TRUE;
}

void skip_input_data(j_decompress_ptr cinfo, long num_bytes) {
}

void term_source(j_decompress_ptr cinfo) {
}

void jpeg_array_src(j_decompress_ptr cinfo, w_ubyte *data, w_int length) {
  my_src_ptr src;

  cinfo->src = (struct jpeg_source_mgr *)allocClearedMem(sizeof(my_source_mgr));
  if (!cinfo->src) {
    return;
  }
  
  src = (my_src_ptr)cinfo->src;
  src->pub.init_source = init_source;
  src->pub.fill_input_buffer = fill_input_buffer;
  src->pub.skip_input_data = skip_input_data;
  src->pub.resync_to_restart = jpeg_resync_to_restart; /* use default method */
  src->pub.term_source = term_source;
  src->pub.bytes_in_buffer = length;
  src->pub.next_input_byte = data;
  src->length = length;
  src->data = data;
}

w_void JPEGImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imagedata) {
  
  w_thread     thread = currentWonkaThread;
  w_ubyte      *start = instance2Array_byte(imagedata);
  w_int        *result;
  w_int        length = instance2Array_length(imagedata);
  w_instance   Array;
  
  struct jpeg_decompress_struct  cinfo;
  struct my_error_mgr            jerr;
  JSAMPARRAY buffer;		/* Output row buffer */
  int row_stride;		/* physical row width in output buffer */

  threadMustBeSafe(thread);
  cinfo.err = jpeg_std_error(&jerr.pub);
  jerr.pub.error_exit = my_error_exit;

  if(setjmp(jerr.setjmp_buffer)) {
    /* An error has occured */
    
    jpeg_destroy_decompress(&cinfo);

    return;
  }
  
  jpeg_create_decompress(&cinfo);
  jpeg_array_src(&cinfo, start, length);

  jpeg_read_header(&cinfo, TRUE);
  jpeg_start_decompress(&cinfo);
  
  setIntegerField(thisObject, F_JPEGImageSource_width, cinfo.output_width);
  setIntegerField(thisObject, F_JPEGImageSource_height, cinfo.output_height);
  
  length = cinfo.output_width * cinfo.output_height;
  enterUnsafeRegion(thread);
  Array = allocArrayInstance(thread, atype2clazz[P_int], 1, &length);
  enterSafeRegion(thread);

  if (Array) {
    setReferenceField(thisObject, Array, F_JPEGImageSource_pixels);
    result = instance2Array_int(Array);

    row_stride = cinfo.output_width * cinfo.output_components;
    buffer = (*cinfo.mem->alloc_sarray)((j_common_ptr) &cinfo, JPOOL_IMAGE, row_stride, 1);

    while (cinfo.output_scanline < cinfo.output_height) {
      unsigned int i;
      jpeg_read_scanlines(&cinfo, buffer, 1);
      for(i=0; i<cinfo.output_width; i++) {
        w_ubyte *d = buffer[0];
        result[i] = d[i * 3] | (d[i * 3 + 1] << 8) | (d[i * 3 + 2] << 16) | (0xff << 24);
      }
      result += cinfo.output_width; 
    }
// Not needed    (*env)->DeleteLocalRef(env, Array);
  }
  else {
    woempa(10, "WARNING: could not allocate array to hold image data.\n");
  }

  jpeg_finish_decompress(&cinfo);
  jpeg_destroy_decompress(&cinfo);

  return;
}

#else

/*
** No JPEG support
*/

w_void JPEGImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imageFile) {

  /*
  ** Be nice and show a message that this is not possible.
  */
  
  fprintf(stderr, "--== This version of Wonka is compiled without JPEG support ==--\n");
  return;

}

#endif

