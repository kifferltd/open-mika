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

#include "wonka.h"   /* the w_ types */
#include "oswald.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "arrays.h"
#include "clazz.h"
#include "heap.h"
#include "checks.h"

extern w_clazz clazzClass;
extern w_clazz clazzClassLoader;
extern w_clazz clazzString;
extern w_clazz clazzThread;
extern w_clazz clazzThreadGroup;

#ifdef DEBUG
static const char* array_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Length",
  /*  2 */ "GetValues",
  /*  3 */ "SetValues",
};

#define ARRAY_REFERENCE_MAX_COMMAND 3
#endif

/*
** Get the number of elements in a given array.
*/

w_void jdwp_array_length(jdwp_command_packet cmd) {
  w_instance instance;
  w_size offset = 0;
  w_int length;

  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (instance2clazz(instance)->previousDimension) {
      length = instance2Array_length(instance);
      woempa(7, "%j has length %d\n", instance, length);
      jdwp_put_u4(&reply_grobag, length);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_array(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Get the elements from given array. The given range should be within the bounds of
** the array.
*/

w_void jdwp_array_get_values(jdwp_command_packet cmd) {
  w_instance instance;
  w_instance element;
  w_size offset = 0;
  w_clazz clazz;
  w_clazz elementClazz;
  w_int first;
  w_int length;
  w_int tag;
  w_int element_tag;
  w_int i;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (instance2clazz(instance)->dims) {
      first = jdwp_get_u4(cmd->data, &offset);
      length = jdwp_get_u4(cmd->data, &offset);
      woempa(7, "Array = %j, first = %d length = %d\n", instance, first, length);

      if (first < 0 || length < 0 || first > instance2Array_length(instance) - length) {
        jdwp_send_invalid_length(cmd->id);
      } else {
        clazz = instance2clazz(instance);
        elementClazz = clazz->previousDimension;

        if(clazzIsPrimitive(elementClazz)) {
          if (elementClazz == clazz_boolean) {
            tag = jdwp_tag_boolean;
          }
          else if (elementClazz == clazz_char) {
            tag = jdwp_tag_char;
          }
          else if (elementClazz == clazz_float) {
            tag = jdwp_tag_float;
          }
          else if (elementClazz == clazz_double) {
            tag = jdwp_tag_double;
          }
          else if (elementClazz == clazz_byte) {
            tag = jdwp_tag_byte;
          }
          else if (elementClazz == clazz_short) {
            tag = jdwp_tag_short;
          }
          else if (elementClazz == clazz_int) {
            tag = jdwp_tag_int;
          }
          else if (elementClazz == clazz_long) {
            tag = jdwp_tag_long;
          }
          else {
            tag = jdwp_tag_void;
          }
        }
        else if(elementClazz->dims) {
          tag = jdwp_tag_array;
        }
        else { 
          if(isAssignmentCompatible(elementClazz, clazzThread)) {
            tag = jdwp_tag_thread;
          }
          else if(isAssignmentCompatible(elementClazz, clazzThreadGroup)) {
            tag = jdwp_tag_thread_group;
          }
          else if(isAssignmentCompatible(elementClazz, clazzClassLoader)) {
            tag = jdwp_tag_class_loader;
          }
          else if(isAssignmentCompatible(elementClazz, clazzString)) {
            tag = jdwp_tag_string;
          }
          else {
            tag = jdwp_tag_object; 
          }
        }

        jdwp_put_u1(&reply_grobag, tag);
        jdwp_put_u4(&reply_grobag, length); 

        for(i = first; i < first + length; i++) {
          switch(tag) {
            case jdwp_tag_void:
              break;
            case jdwp_tag_byte:
              jdwp_put_u1(&reply_grobag, instance2Array_byte(instance)[i]);
              break;
            case jdwp_tag_boolean:
              jdwp_put_u1(&reply_grobag, (instance2Array_byte(instance)[i / 8] >> (i % 8)) & 1);
              break;
            case jdwp_tag_char:
              jdwp_put_u2(&reply_grobag, instance2Array_char(instance)[i]);
              break;
            case jdwp_tag_short:
              jdwp_put_u2(&reply_grobag, instance2Array_short(instance)[i]);
              break;
            case jdwp_tag_int:
              jdwp_put_u4(&reply_grobag, instance2Array_int(instance)[i]);
              break;
            case jdwp_tag_float:
              jdwp_put_u4(&reply_grobag, instance2Array_float(instance)[i]);
              break;
            case jdwp_tag_double:                   /* double & long take 8 bytes */
              jdwp_put_u4(&reply_grobag, MSW_PART(instance2Array_double(instance)[i]));
              jdwp_put_u4(&reply_grobag, LSW_PART(instance2Array_double(instance)[i]));
              break;
            case jdwp_tag_long:         
              jdwp_put_u4(&reply_grobag, MSW_PART(instance2Array_double(instance)[i]));
              jdwp_put_u4(&reply_grobag, LSW_PART(instance2Array_double(instance)[i]));
              break;
            case jdwp_tag_array:
            case jdwp_tag_object:
            case jdwp_tag_string:
            case jdwp_tag_thread:
            case jdwp_tag_thread_group:
            case jdwp_tag_class_loader:
            case jdwp_tag_class_object:
              element = instance2Array_instance(instance)[i];
              elementClazz = instance2clazz(element);
              if(elementClazz->dims) {
                element_tag = jdwp_tag_array;
              }
              else { 
                if(isAssignmentCompatible(elementClazz, clazzThread)) {
                  element_tag = jdwp_tag_thread;
                }
                else if(isAssignmentCompatible(elementClazz, clazzThreadGroup)) {
                  element_tag = jdwp_tag_thread_group;
                }
                else if(isAssignmentCompatible(elementClazz, clazzClassLoader)) {
                  element_tag = jdwp_tag_class_loader;
                }
                else if(isAssignmentCompatible(elementClazz, clazzString)) {
                  element_tag = jdwp_tag_string;
                }
                else {
                  element_tag = jdwp_tag_object; 
                }
              jdwp_put_u1(&reply_grobag, element_tag);
              jdwp_put_objectref(&reply_grobag, element);
              break;
            }
          }
        }

        jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      } 
    }
    else {
      jdwp_send_invalid_array(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Set elements of the given array. The given range should be within the bounds of
** the array.
*/

w_void jdwp_array_set_values(jdwp_command_packet cmd) {
  w_instance instance;
  w_size offset = 0;
  w_clazz clazz;
  w_clazz elementClazz;
  w_int first;
  w_int length;
  w_int i;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  if (instance) {
    if (instance2clazz(instance)->dims) {
      first = jdwp_get_u4(cmd->data, &offset);
      length = jdwp_get_u4(cmd->data, &offset);
      woempa(7, "Array = %j, first = %d length = %d\n", instance, first, length);

      if (first < 0 || length < 0 || first > instance2Array_length(instance) - length) {
        jdwp_send_invalid_length(cmd->id);
      } else {
        clazz = instance2clazz(instance);
        elementClazz = clazz->previousDimension;

        for(i = first; i < first + length; i++) {
          if(clazzIsPrimitive(elementClazz)) {
            if (elementClazz == clazz_boolean) {
              w_ubyte s = instance2Array_byte(instance)[i / 8];
              s &= 0xff ^ (1 << i % 8);
              instance2Array_byte(instance)[i / 8] = s | ((jdwp_get_u1(cmd->data, &offset) & 1) << (i % 8));
            }
            else if (elementClazz == clazz_char) {
              instance2Array_char(instance)[i] = jdwp_get_u2(cmd->data, &offset);
            }
            else if (elementClazz == clazz_float) {
              instance2Array_float(instance)[i] = jdwp_get_u4(cmd->data, &offset);
            }
            else if (elementClazz == clazz_double) {
              // cheating a bit
              instance2Array_float(instance)[i * 2 + WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
              instance2Array_float(instance)[i * 2 + WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
            }
            else if (elementClazz == clazz_byte) {
              instance2Array_byte(instance)[i] = jdwp_get_u1(cmd->data, &offset);
            }
            else if (elementClazz == clazz_short) {
              instance2Array_short(instance)[i] = jdwp_get_u2(cmd->data, &offset);
            }
            else if (elementClazz == clazz_int) {
              instance2Array_int(instance)[i] = jdwp_get_u4(cmd->data, &offset);
            }
            else if (elementClazz == clazz_long) {
              // cheating a bit more
              instance2Array_int(instance)[i * 2 + WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
              instance2Array_int(instance)[i * 2 + WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
            }
            else {
            }
          }
          else {
            instance2Array_instance(instance)[i] = jdwp_get_objectref(cmd->data, &offset);
          }
        }

        jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      } 
    }
    else {
      jdwp_send_invalid_array(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** The dispatcher for the 'Array reference' command set.
*/

w_void dispatch_arrayref(jdwp_command_packet cmd) {

  woempa(7, "ArrayReference Command = %s\n", cmd->command > 0 && cmd->command <= ARRAY_REFERENCE_MAX_COMMAND ? array_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_arrayref_cmd)cmd->command) {
    case jdwp_arrayref_length:
      jdwp_array_length(cmd);
      break;
    case jdwp_arrayref_getValues:
      jdwp_array_get_values(cmd);
      break;
    case jdwp_arrayref_setValues:
      jdwp_array_set_values(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

