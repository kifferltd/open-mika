/**************************************************************************
* Copyright (C) 2006 by Chris Gay, /k/ Embedded Java Solutions.           *
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
#include "wordset.h"
#include "wstrings.h"

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
  return (w_ubyte)buffer[(*offset)++];
}

w_ushort jdwp_get_u2(char *buffer, w_size *offset) {
  w_ushort s;
  char* p = buffer + *offset;

  s = (w_ubyte)*p++;
  s = (s << 8) | (w_ubyte)*p++;
  *offset += 2;

  return s;
}

w_word jdwp_get_u4(char *buffer, w_size *offset) {
  w_word w;
  char* p = buffer + *offset;

  w = (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  w = (w << 8) | (w_ubyte)*p++;
  *offset += 4;

  return w;
}

w_instance jdwp_get_objectref(char *buffer, w_size *offset) {
  w_word objectID = jdwp_get_u4(buffer, offset);
  w_instance result = jdwp_objectID2instance(objectID);

  return result;
}

w_ubyte *jdwp_get_bytes(char *buffer, w_size *offset, w_size len) {
  w_ubyte *result = allocMem(len + 1);

  memcpy(result, buffer + *offset, len);
  result[len] = 0;
  *offset += len;

  return result;
}

void jdwp_get_bytes_here(char *buffer, w_size *offset, w_size len, w_ubyte *here) {
  memcpy(here, buffer + *offset, len);
  *offset += len;
}

w_string jdwp_get_string(char *buffer, w_size *offset) {
  w_size len = jdwp_get_u4(buffer, offset);
  w_ubyte *bytes = allocMem(len + 1);
  w_string result;

  woempa(7, "length = %d\n", len);
  jdwp_get_bytes_here(buffer, offset, len, bytes);
  bytes[len] = 0;
  result = utf2String(bytes, len);
  releaseMem(bytes);
  woempa(7, "string = %w\n", result);

  return result;
}

void jdwp_get_location_here(char *buffer, w_size *offset, jdwp_location location) {
  location->tag = jdwp_get_u1(buffer, offset);
  location->clazz = jdwp_get_clazz(buffer, offset);
  location->method = jdwp_get_method(buffer, offset);
  jdwp_get_u4(buffer, offset); // skip MS 4 bytes
  location->pc = jdwp_get_u4(buffer, offset);
}

void jdwp_put_u1(w_grobag *gb, w_ubyte b) {
  woempa(1, "Appending 1 byte %02x\n", b);
  appendToGrobag(gb, &b, 1);
}

void jdwp_put_u2(w_grobag *gb, w_ushort s) {
  w_ubyte temp[2];

  woempa(1, "Appending 2 bytes %04x\n", s);
  temp[0] = s >> 8;
  temp[1] = s & 0xff;
  appendToGrobag(gb, temp, 2);
}

void jdwp_put_u4(w_grobag *gb, w_word w) {
  w_ubyte temp[4];

  woempa(1, "Appending 4 bytes %08x\n", w);
  temp[0] = w >> 24;
  temp[1] = (w >> 16) & 0xff;
  temp[2] = (w >> 8) & 0xff;
  temp[3] = w & 0xff;
  appendToGrobag(gb, temp, 4);
}

void jdwp_put_objectref(w_grobag *gb, w_instance instance) {
  w_word objectID = jdwp_instance2objectID(instance);;

  jdwp_put_u4(gb, objectID);
}

void jdwp_put_bytes(w_grobag *gb, w_ubyte *b, w_size l) {
  woempa(1, "Appending %d bytes\n");
  appendToGrobag(gb, b, l);
}

void jdwp_put_cstring(w_grobag *gb, char *b, w_size l) {
  jdwp_put_u4(gb, l);
  woempa(1, "Appending %d bytes\n");
  appendToGrobag(gb, b, l);
}

void jdwp_put_string(w_grobag *gb, w_string string) {
  w_int length;
  w_ubyte *cstring = jdwp_string2UTF8(string, &length);

  jdwp_put_u4(gb, length);
  appendToGrobag(gb, cstring + 4, length);
  releaseMem(cstring);
}

void jdwp_put_location(w_grobag *gb, jdwp_location location) {
  woempa(7, "Appending location: tag = %d, clazz = %k, method = %m, pc = %d\n", location->tag, location->clazz, location->method, location->pc);
  jdwp_put_u1(gb, location->tag);
  jdwp_put_u4(gb, (w_word)location->clazz);
  jdwp_put_u4(gb, (w_word)location->method);
  jdwp_put_u4(gb, 0);
  jdwp_put_u4(gb, location->pc);
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

  /*
  ** If we send back a not_implemented error, make some noise !
  */

  if(error == jdwp_err_not_implemented) {
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
    woempa(9, "                    J D W P :     N O T    Y E T    I M P L E M E N T E D\n");
    woempa(9, "-------------------------------------------------------------------------------------------------\n");
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

  jdwp_send_packet(reply);

  /*
  ** Clean up.
  */

  releaseMem(reply);
}

