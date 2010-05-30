/**************************************************************************
* Copyright (C) 2006, 2009 by Chris Gay, /k/ Embedded Java Solutions.     *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED BY /K/ EMBEDDED JAVA SOLUTIONS AND OTHER      *
* CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,*
* BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND       *
* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL     *
* /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY     *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "hashtable.h"
#include "jdwp.h"
#include "jdwp_events.h"
#include "jdwp-protocol.h"
#include "methods.h"
#include "wordset.h"
#include "wstrings.h"

/*
** Command set names
*/
const char *command_set_names[] = {
  "", "VirtualMachine", "ReferenceType", "ClassType", "ArrayType",
  "InterfaceType", "Method", "Field", "",
  "ObjectReference", "StringReference", 
  "ThreadReference", "ThreadGroupReference", "ArrayReference", 
  "ClassLoaderReference", "EventRequest", "StackFrame", 
  "ClassObjectReference"
  };

/*
** Command names
*/
const char * EMPTY_NAMES[] = {};

const char *virtual_machine_names[] = { "", "Version", "ClassesBySignature", "AllClasses", "AllThreads", "TopLevelThreadGroups", "Dispose", "IDSizes", "Suspend", "Resume", "Exit", "CreateString", "Capabilities", "ClassPaths", "DisposeObjects", "HoldEvents", "ReleaseEvents" };
const char *reference_type_names[] = { "", "Signature", "ClassLoader", "Modifiers", "Fields", "Methods", "GetValues", "SourceFile", "NestedTypes", "Status", "Interfaces", "ClassObject" };
const char *class_type_names[] = { "", "Superclass", "SetValues", "InvokeMethod", "NewInstance" };
const char *array_type_names[] = { "", "NewInstance" };
const char *method_names[] = { "", "LineTable", "VariableTable", "Bytecodes" };
const char *object_reference_names[] = { "", "ReferenceType", "GetValues", "SetValues", "MonitorInfo", "InvokeMethod", "DisableCollection", "EnableCollection", "IsCollected" };
const char *string_reference_names[] = { "", "Value" };
const char *thread_reference_names[] = { "", "Name", "Suspend", "Resume", "Status", "ThreadGroup", "Frames", "FrameCount", "OwnedMonitors", "CurrentContendedMonitor", "Stop", "Interrupt", "SuspendCount" };
const char *thread_group_reference_names[] = { "", "Name", "Parent", "Children" };
const char *array_reference_names[] = { "", "Length", "GetValues", "SetValues" };
const char *class_loader_reference_names[] = { "", "VisibleClasses" };
const char *event_request_names[] = { "", "Set", "Clear", "ClearAllBreakpoints" };
const char *stack_frame_names[] = { "", "GetValues", "SetValues", "ThisObject" };
const char *class_object_reference_names[] = { "", "ReflectedType" };

const char **command_names[] = {
  EMPTY_NAMES,
  virtual_machine_names,
  reference_type_names,
  class_type_names,
  array_type_names,
  EMPTY_NAMES,
  method_names,
  EMPTY_NAMES,
  EMPTY_NAMES,
  object_reference_names,
  string_reference_names,
  thread_reference_names,
  thread_group_reference_names,
  array_reference_names,
  class_loader_reference_names,
  event_request_names,
  stack_frame_names,
  class_object_reference_names
  };

/*
** Error code names
*/
#define UNKERR "unknown"
const char *error_names[] = {
  "OK", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_THREAD", "INVALID_THREAD_GROUP", "INVALID_PRIORITY", "THREAD_NOT_SUSPENDED", "THREAD_SUSPENDED", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_OBJECT", "INVALID_CLASS", "CLASS_NOT_PREPARED", "INVALID_METHODID", "INVALID_LOCATION", "INVALID_FIELDID", UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_FRAMEID", "NO_MORE_FRAMES", "OPAQUE_FRAME", "NOT_CURRENT_FRAME", "TYPE_MISMATCH", "INVALID_SLOT", UNKERR, UNKERR, UNKERR, UNKERR,
  "DUPLICATE", "NOT FOUND", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_MONITOR", "NOT_MONITOR_OWNER", "INTERRUPT", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_CLASS_FORMAT", "CIRCULAR_CLASS_DEFINITION", "FAILS_VERIFICATION", "ADD_METHOD_NOT_IMPLEMENTED", "SCHEMA_CHANGE_NOT_IMPLEMENTED", "INVALID_TYPESTATE", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_CLASS_FORMAT", "CIRCULAR_CLASS_DEFINITION", "FAILS_VERIFICATION", "ADD_METHOD_NOT_IMPLEMENTED", "SCHEMA_CHANGE_NOT_IMPLEMENTED", "INVALID_TYPESTATE", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "INVALID_CLASS_FORMAT", "CIRCULAR_CLASS_DEFINITION", "FAILS_VERIFICATION", "ADD_METHOD_NOT_IMPLEMENTED", "SCHEMA_CHANGE_NOT_IMPLEMENTED", "INVALID_TYPESTATE", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, UNKERR, "NOT_IMPLEMENTED",
  "NULL_POINTER", "ABSENT_INFORMATION", "INVALID_EVENT_TYPE", "ILLEGAL_ARGUMENT", UNKERR, UNKERR, UNKERR, UNKERR, UNKERR,
  "OUT_OF_MEMORY", "ACCESS_DENIED", "VM_DEAD", "INTERNAL", UNKERR, "UNATTACHED_THREAD"
  };

const int MAX_ERROR = 115;

/*
** A grobag in which to write reply data.
*/
w_grobag reply_grobag;

/*
** A grobag in which to write command data.
*/
w_grobag command_grobag;

/*
** Queue of replies/commands to be sent to the debugger. Be so kind as to own
** jdwp_mutex when you manipulate this.
*/
w_fifo jdwp_send_fifo;

w_ubyte jdwp_get_u1(char *buffer, w_size *offset) {
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get byte = %02x\n", buffer[(*offset)]);
  }
  return (w_ubyte)buffer[(*offset)++];
}

w_ushort jdwp_get_u2(char *buffer, w_size *offset) {
  w_ushort s;
  char* p = buffer + *offset;

  s = (w_ubyte)*p++;
  s = (s << 8) | (w_ubyte)*p++;
  *offset += 2;
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get short = %04x\n", s);
  }

  return s;
}

static w_word i_get_u4(char *buffer, w_size *offset) {
  w_word w;
  char* p = buffer + *offset;

  w = (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  *offset += 4;

  return w;
}

w_word jdwp_get_u4(char *buffer, w_size *offset) {
  w_word w = i_get_u4(buffer, offset);
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get int = %08x\n", w);
  }

  return w;
}

w_instance jdwp_get_objectref(char *buffer, w_size *offset) {
  w_word objectID = i_get_u4(buffer, offset);
  w_instance result = jdwp_objectID2instance(objectID);
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get objectref = %d = %j\n", objectID, result);
  }

  return result;
}

w_ubyte *jdwp_get_bytes(char *buffer, w_size *offset, w_size len) {
  w_ubyte *result = allocMem(len + 1);

  memcpy(result, buffer + *offset, len);
  result[len] = 0;
  *offset += len;
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    char *contents = bytes2hex(result, len);
    w_printf("JDWP: get bytes = %s\n", contents);
    releaseMem(contents);
  }

  return result;
}

void jdwp_get_bytes_here(char *buffer, w_size *offset, w_size len, w_ubyte *here) {
  memcpy(here, buffer + *offset, len);
  *offset += len;
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    char *contents = bytes2hex(here, len);
    w_printf("JDWP: get bytes = %s\n", contents);
    releaseMem(contents);
  }
}

w_string jdwp_get_string(char *buffer, w_size *offset) {
  w_size len = i_get_u4(buffer, offset);
  w_ubyte *bytes = allocMem(len + 1);
  w_string result;

  woempa(7, "length = %d\n", len);
  jdwp_get_bytes_here(buffer, offset, len, bytes);
  bytes[len] = 0;
  result = utf2String(bytes, (w_int)len);
  releaseMem(bytes);
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get string = %w\n", result);
  }

  return result;
}

void jdwp_get_location_here(char *buffer, w_size *offset, jdwp_location location) {
  jdwp_get_u1(buffer, offset);
  jdwp_get_clazz(buffer, offset);
  location->method = jdwp_get_method(buffer, offset);
  i_get_u4(buffer, offset); // skip MS 4 bytes
  location->pc = i_get_u4(buffer, offset);
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: get location = %M at pc %d\n", location->method, location->pc);
  }
}

void jdwp_put_u1(w_grobag *gb, w_ubyte b) {
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put byte = %02x\n", b);
  }
  appendToGrobag(gb, &b, 1);
}

void jdwp_put_u2(w_grobag *gb, w_ushort s) {
  w_ubyte temp[2];

  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put short = %04x\n", s);
  }
  temp[0] = (s >> 8) & 0xff;
  temp[1] = s & 0xff;
  appendToGrobag(gb, temp, 2);
}

void i_put_u4(w_grobag *gb, w_word w) {
  w_ubyte temp[4];

  temp[0] = (w >> 24) & 0xff;
  temp[1] = (w >> 16) & 0xff;
  temp[2] = (w >> 8) & 0xff;
  temp[3] = w & 0xff;
  appendToGrobag(gb, temp, 4);
}

void jdwp_put_u4(w_grobag *gb, w_word w) {
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put int = %08x\n", w);
  }
  i_put_u4(gb, w);
}

void jdwp_put_objectref(w_grobag *gb, w_instance instance) {
  w_word objectID = jdwp_instance2objectID(instance);;

  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put objectref = %d = %j\n", objectID, instance);
  }
  i_put_u4(gb, objectID);
}

void jdwp_put_bytes(w_grobag *gb, w_ubyte *b, w_size l) {
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    char *contents = bytes2hex((char*)b, l);
    w_printf("JDWP: put bytes = %s\n", contents);
    releaseMem(contents);
  }
  appendToGrobag(gb, b, l);
}

void jdwp_put_cstring(w_grobag *gb, char *b, w_size l) {
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put string = %s\n", b);
  }
  i_put_u4(gb, l);
  appendToGrobag(gb, b, l);
}

void jdwp_put_string(w_grobag *gb, w_string string) {
  w_int length;
  w_ubyte *cstring = jdwp_string2UTF8(string, &length);

  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put string = %w\n", string);
  }
  i_put_u4(gb, length);
  appendToGrobag(gb, cstring + 4, length);
  releaseMem(cstring);
}

void jdwp_put_location(w_grobag *gb, jdwp_location location) {
  w_clazz clazz = location->method->spec.declaring_clazz;

  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    w_printf("JDWP: put location = %M at pc %d\n", location->method, location->pc);
  }
  jdwp_put_u1(gb, isSet(clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : jdwp_tt_class);
  i_put_u4(gb, (w_word)clazz);
  i_put_u4(gb, (w_word)location->method);
  i_put_u4(gb, 0);
  i_put_u4(gb, location->pc);
}

/*
** Convert an array of bytes into a C string of the format " xx xx xx ..."
** where the 'x' are hex digits. The memory for the string is allocated
** using allocMem, and the caller should free it using releaseMem.
*/
char *bytes2hex(char *bytes, w_size length) {
  char *buffer = allocMem(length * 3 + 1);
  char *toptr = buffer;
  char *fromptr = bytes;
  w_size i;

  for (i = 0; i < length; ++i) {
    snprintf(toptr, 4, " %02x", *fromptr++);
    toptr += 3;
  }
  *toptr = 0;

  return buffer;
}

void jdwp_send_reply(w_int id, w_grobag *gb, w_int error) {
  jdwp_reply_packet reply;
  w_int length = (gb && *gb) ? (*gb)->occupancy : 0;

  woempa(7, "Replying to commandID %d, length = %d, error = %d\n", swap_int(id), length, error);
  reply = allocMem(offsetof(jdwp_Reply_Packet, data) + length);

  reply->length = swap_int((offsetof(jdwp_Reply_Packet, data) + length));
  reply->id = id;
  reply->flags = 0x80;
  reply->err1 = (error >> 8) & 0x00FF;
  reply->err2 = error & 0x00FF;
  if (isSet(verbose_flags, VERBOSE_FLAG_JDWP)) {
    if (length) {
      char *contents = bytes2hex((*gb)->contents, length);

      w_printf("JDWP: Sending reply id %d, error code %d (%s),  length: %d, contents:%s\n", swap_int(id), error, error > MAX_ERROR ? UNKERR : error_names[error], swap_int(reply->length), contents);
      releaseMem(contents);
    }
    else {
      w_printf("JDWP: Sending reply id %d, error code %d (%s),  length: %d\n", swap_int(id), error, error > MAX_ERROR ? UNKERR : error_names[error], swap_int(reply->length));
    }
  }

  /*
  ** If there is data to be sent with this packet, add it to the packet and clear the data.
  */

  if (length) {
    memcpy(reply->data, (*gb)->contents, (*gb)->occupancy);
    (*gb)->occupancy = 0;
  }

  /*
  ** Send the packet to the debugger.
  */

  jdwp_send_packet_dt_socket(reply);

  /*
  ** Clean up.
  */

  releaseMem(reply);
}

