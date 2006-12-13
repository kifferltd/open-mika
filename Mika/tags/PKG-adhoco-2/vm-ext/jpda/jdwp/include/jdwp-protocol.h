#ifndef _JDWP_PROTOCOL_H
#define _JDWP_PROTOCOL_H

/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* 
** JavaTM Debug Wire Protocol 
*/

#include "jdwp_events.h"
#include "grobag.h"
#include "oswald.h"
#include <string.h>

extern w_grobag reply_grobag;
extern w_grobag command_grobag;

/*
** Transport related stuff.
*/

w_void jdwp_connect(w_void);
w_void jdwp_disconnect(w_void);
w_void *jdwp_recv_packet(w_void);
w_void jdwp_send_packet(w_void *);
w_void jdwp_send_reply(w_int, w_grobag*, w_int);
w_void jdwp_send_command(w_grobag*, w_int, w_int);

#define jdwp_send_invalid_array(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_array)
#define jdwp_send_invalid_class(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_class)
#define jdwp_send_invalid_class_loader(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_class_loader)
#define jdwp_send_invalid_event_type(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_event_type)
#define jdwp_send_invalid_fieldid(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_fieldid)
#define jdwp_send_invalid_length(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_length)
#define jdwp_send_invalid_object(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_object)
#define jdwp_send_invalid_string(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_string)
#define jdwp_send_invalid_thread(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_thread)
#define jdwp_send_invalid_thread_group(id) jdwp_send_reply((id), NULL, jdwp_err_invalid_thread_group)
#define jdwp_send_not_implemented(id) jdwp_send_reply((id), NULL, jdwp_err_not_implemented)
#define jdwp_send_thread_not_suspended(id) jdwp_send_reply((id), NULL, jdwp_err_thread_not_suspended)

/*
** Read different types of data from the input stream. The length is not
** checked, so if insufficient data is present in the buffer gibberish
** will be read.
*/

w_ubyte jdwp_get_u1(char *, w_size *offset);
w_ushort jdwp_get_u2(char *, w_size *offset);
w_word jdwp_get_u4(char *, w_size *offset);
w_instance jdwp_get_objectref(char *, w_size *offset);
#define jdwp_get_clazz(c,o) ((w_clazz)jdwp_get_u4((c), (o)))
#define jdwp_get_field(c,o) ((w_field)jdwp_get_u4((c), (o)))
#define jdwp_get_method(c,o) ((w_method)jdwp_get_u4((c), (o)))
#define jdwp_get_frame(c,o) ((w_frame)jdwp_get_u4((c), (o)))
w_ubyte *jdwp_get_bytes(char *, w_size *offset, w_size len);
void jdwp_get_bytes_here(char *, w_size *offset, w_size len, w_ubyte *here);
w_string jdwp_get_string(char *, w_size *offset);
void jdwp_get_location_here(char *, w_size *offset, jdwp_location);

/*
** Write different types of data into a w_Grobag from which a message will
** be constructed.
*/

void jdwp_put_u1(w_grobag *gb, w_ubyte);
void jdwp_put_u2(w_grobag *gb, w_ushort);
void jdwp_put_u4(w_grobag *gb, w_word);
void jdwp_put_objectref(w_grobag *gb, w_instance instance);
static inline void jdwp_put_clazz(w_grobag *gb, w_clazz clazz) {
  jdwp_put_u4(gb, (w_word)clazz);
}
static inline void jdwp_put_frame(w_grobag *gb, w_frame frame) {
  jdwp_put_u4(gb, (w_word)frame);
}
static inline void jdwp_put_field(w_grobag *gb, w_field field) {
  jdwp_put_u4(gb, (w_word)field);
}
static inline void jdwp_put_method(w_grobag *gb, w_method method) {
  jdwp_put_u4(gb, (w_word)method);
}
static inline void jdwp_put_tagged_type(w_grobag *gb, w_clazz clazz) {
  jdwp_put_u1(gb, clazz2tag(clazz));
  jdwp_put_clazz(gb, clazz);
}

void jdwp_put_bytes(w_grobag *gb, w_ubyte*, w_size len);
void jdwp_put_cstring(w_grobag *gb, char*, w_size len);
void jdwp_put_string(w_grobag *gb, w_string string);
void jdwp_put_location(w_grobag *gb, jdwp_location);

/*
** Check pointers received in a command to see if they are really what they claim.
*/
w_boolean jdwp_check_clazz(w_clazz clazz);

w_boolean jdwp_check_field(w_field);

#endif /* _JDWP_PROTOCOL_H */

