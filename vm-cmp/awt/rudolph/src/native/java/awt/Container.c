/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#include "wstrings.h"     // String2string()
#include "loading.h"      // instance2clazz()

#include "awt-classes.h"
#include "rudolph.h"
#include "canvas.h"
#include "registry.h"
#include "Component.h"
#include "Event.h"

int Container_verifyBounds(r_component dst, r_component src) {
    
  r_canvas canvas = (r_canvas)dst->object;
  w_instance i = (w_instance)src->instance;

  woempa(5, "VERIFY: %d, %d %dx%d -> %dx%d\n", getIntegerField(i, F_Component_x), getIntegerField(i, F_Component_y), getIntegerField(i, F_Component_width), getIntegerField(i, F_Component_height), canvas->buffer->fw, canvas->buffer->fh);

  #ifdef RUNTIME_CHECKS
    if (canvas->buffer->ox + canvas->buffer->vw > canvas->buffer->fw || canvas->buffer->oy + canvas->buffer->vh > canvas->buffer->fh) {
      wabort(ABORT_WONKA, "invalid viewport size:  %d + %d -> %d,  %d + %d -> %d\n", canvas->buffer->ox, canvas->buffer->vw, canvas->buffer->fw, canvas->buffer->oy, canvas->buffer->vh, canvas->buffer->fh);
    }
    
    if ((getIntegerField(dst->instance, F_Component_width) != canvas->buffer->fw) || (getIntegerField(dst->instance, F_Component_height) != canvas->buffer->fh)) {
//      wabort(ABORT_WONKA, "canvas buffer width/height mismatch: %d == %d, %d == %d\n", getIntegerField(dst->instance, F_Component_width), canvas->buffer->fw, getIntegerField(dst->instance, F_Component_height), canvas->buffer->fh);
    } 
  #endif

  if(src->tag == Z_CONTAINER) {
    return WONKA_TRUE;
  } 
  else {
    return ((getIntegerField(i, F_Component_x) <= canvas->buffer->fw) && (getIntegerField(i, F_Component_y) <= canvas->buffer->fh));
  }
}
