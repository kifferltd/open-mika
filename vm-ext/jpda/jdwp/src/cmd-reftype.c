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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "descriptor.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "methods.h"
#include "oswald.h"
#include "wonka.h"
#include "wstrings.h"

extern w_clazz clazzClass;
extern w_clazz clazzClassLoader;
extern w_clazz clazzString;
extern w_clazz clazzThread;
extern w_clazz clazzThreadGroup;

#ifdef DEBUG
static const char* reftype_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Signature",
  /*  2 */ "ClassLoader",
  /*  3 */ "Modifiers",
  /*  4 */ "Fields",
  /*  5 */ "Methods",
  /*  6 */ "GetValues",
  /*  7 */ "SourceFile",
  /*  8 */ "NestedTypes",
  /*  9 */ "Status",
  /* 10 */ "Interfaces",
  /* 11 */ "ClassObject",
  /* 12 */ "SourceDebugExtension",
};

#define REFTYPE_MAX_COMMAND 12

#endif

/*
** Returns the JNI signature of the given referenceID.
*/

static void jdwp_ref_signature(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_string desc_string;
 
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    desc_string = clazz2desc(clazz);
    woempa(7, "Signature of %k is %w\n", clazz, desc_string);
    jdwp_put_string(&reply_grobag, desc_string);
    deregisterString(desc_string);
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns a ClassLoaderID for the the given reference type. 
*/

static void jdwp_ref_classloader(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_instance  classloader;
 
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    classloader = clazz->loader;
    woempa(7, "  class loader = %j\n", classloader);
    jdwp_put_objectref(&reply_grobag, classloader);

    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns the modifiers for the the given reference type. 
*/

static void jdwp_ref_modifiers(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_word  modifiers;
 
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    modifiers = clazz->flags & (ACC_ABSTRACT | ACC_INTERFACE | ACC_SUPER | ACC_FINAL | ACC_PUBLIC);
    woempa(7, "  modifiers = 0x%04x\n", modifiers);
    jdwp_put_u4(&reply_grobag, modifiers);

    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns the direct inner classes of the the given reference type. 
*/

static void jdwp_ref_nested_types(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_clazz inner;
  w_int count;
  w_int i;
 
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    count = 0;
    for (i = 0; i < clazz->temp.inner_class_info_count; ++i) {
      if (clazz->temp.inner_class_info[i].outer_class_info_index == clazz->temp.this_index) {
        ++count;
      }
    }
    woempa(7, "%d nested types\n", count);
    jdwp_put_u4(&reply_grobag, count);
    for (i = 0; i < clazz->temp.inner_class_info_count; ++i) {
      if (clazz->temp.inner_class_info[i].outer_class_info_index == clazz->temp.this_index) {
        inner = getClassConstant(clazz, i);
        jdwp_put_tagged_type(&reply_grobag, inner);
      }
    }

    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}

/*
** Returns the status of the given reference type. 
*/

static void jdwp_ref_status(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_word  status;
 
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    status = clazz2status(clazz);
    woempa(7, "  status = 0x%04x\n", status);
    jdwp_put_u4(&reply_grobag, status);

    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}



/*
** Get information about each field (except the inhereted fields) of the given reference type. 
** Note: Fields will not be returned in the same order as they are in the .class file, we don't know how to do that.
*/

static void jdwp_ref_fields(jdwp_command_packet cmd) {
  w_clazz clazz;
  w_size offset = 0;
  w_field field;
  w_string desc_string;
  w_int      i;
  w_int      count;
  w_flags    flags;
  
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    count = clazz->numFields;
    woempa(7, "%d fields\n", count);
    jdwp_put_u4(&reply_grobag, count);

    for(i = 0; i < count; i++) {
      field = &clazz->own_fields[i];
      woempa(7, "  field %v\n", field);
      jdwp_put_field(&reply_grobag, field);
      jdwp_put_string(&reply_grobag, field->name);
      desc_string = clazz2desc(field->value_clazz);
      jdwp_put_string(&reply_grobag, desc_string);
      woempa(7, "  name = %w, descriptor = %w\n", field->name, desc_string);
      deregisterString(desc_string);
      flags = field->flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_VOLATILE | ACC_TRANSIENT);
      // TODO: | 0xf0000000 if synthetic
      woempa(7, "  flags = 0x%08x\n", flags);
      jdwp_put_u4(&reply_grobag, flags);
    }
      
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns all (but not the inherited) methods of a given referenceID. 
** Also constructors (<init>) and initializers (<clinit>) are included.
** Methods should be returned in the same order they occur in the classfile.
** [CG 20020321] ... but they won't be, can't be, sorry.
*/

static void jdwp_ref_methods(jdwp_command_packet cmd) {
  w_clazz    clazz;
  w_size offset = 0;
  w_method method;
  w_int    i;
  w_int    count;
  w_flags  flags;
  
  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    count = clazz->numDeclaredMethods;
    woempa(7, "%d methods\n", count);
    jdwp_put_u4(&reply_grobag, count);

    for(i = 0; i < count; i++) {
      method = &clazz->own_methods[i];
      woempa(7, "  method %m\n", method);
      jdwp_put_method(&reply_grobag, method);
      jdwp_put_string(&reply_grobag, method->spec.name);
      jdwp_put_string(&reply_grobag, method->desc);
      woempa(7, "  name = %w, descriptor = %w\n", method->spec.name, method->desc);
      flags = method->flags & (ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE | ACC_ABSTRACT);
      // TODO: | 0xf0000000 if synthetic
      woempa(7, "  flags = 0x%08x\n", flags);
      jdwp_put_u4(&reply_grobag, flags);
    }
      
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}

/*
** Returns the value of selected static fields of a referenceID. 
*/

static void jdwp_ref_get_values(jdwp_command_packet cmd) {
  w_clazz clazz;
  w_size offset = 0;
  //w_clazz value_clazz;
  w_field field;
  w_int count;
  w_int i;
  w_ubyte sigbyte;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    count = jdwp_get_u4(cmd->data, &offset);
    woempa(7, "%d fields requested\n", count);
    jdwp_put_u4(&reply_grobag, count);

    for(i = 0; i < count; i++) {
      field = jdwp_get_field(cmd->data, &offset);
      if (jdwp_check_field(field)) {
        //value_clazz = field->value_clazz;
        sigbyte = clazz2sigbyte(field->value_clazz);
        woempa(9, "  %v: sigbyte = '%c'\n", field, sigbyte);
        switch (sigbyte) {
          case jdwp_tag_boolean:
            woempa(7, "  byte: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u1(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_char:
            woempa(7, "  char: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u2(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_float:
            woempa(7, "  float: 0x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_double:
            woempa(7, "  double: 0x%08x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW], field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            break;
          case jdwp_tag_byte:
            woempa(7, "  byte: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u1(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_short:
            woempa(7, "  short: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u2(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_int:
            woempa(7, "  int: %d\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          case jdwp_tag_long:
            woempa(7, "  long: 0x%08x%08x\n", field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW], field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot + WORD_MSW]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot + WORD_LSW]);
            break;
          case jdwp_tag_void:
            woempa(7, "  void\n");
            break;
          case jdwp_tag_array:
            woempa(7, "  array: %j\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
            break;
          default:
            woempa(7, "  reference: %j\n", field->declaring_clazz->staticFields[field->size_and_slot]);
            jdwp_put_u4(&reply_grobag, field->declaring_clazz->staticFields[field->size_and_slot]);
        }
      }
      else {
        jdwp_send_invalid_fieldid(cmd->id);
      }
    }
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Returns the filename in which a given referenceID is defined.
** The path to the filename is not included.
*/

static void jdwp_ref_source(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_string filename;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    filename = clazz->filename ? clazz->filename : string_empty;
    woempa(7, "  filename = %w\n", filename);
    jdwp_put_string(&reply_grobag, filename);
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Get all the interfaces that the given class implements.
*/

static void jdwp_ref_interfaces(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_clazz interfaze;
  w_int count;
  w_int i;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    count = clazz->numDirectInterfaces;
    woempa(7, "  has %d interfaces\n", count);
    jdwp_put_u4(&reply_grobag, count);

    for(i = 0; i < count; i++) {
      interfaze = clazz->interfaces[i];
      woempa(7, "  %k\n", interfaze);
      jdwp_put_clazz(&reply_grobag, interfaze);
    }
    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** Get the Class object corresponding to this class.
*/

static void jdwp_ref_class_object(jdwp_command_packet cmd) {
  w_clazz  clazz;
  w_size offset = 0;
  w_instance theClass;

  clazz = jdwp_get_clazz(cmd->data, &offset);
  woempa(7, "clazz = %K\n", clazz);

  if (jdwp_check_clazz(clazz)) {
    theClass = clazz2Class(clazz);
    jdwp_put_objectref(&reply_grobag, theClass);

    jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
  }
  else {
    jdwp_send_invalid_class(cmd->id);
  }
}


/*
** The dispatcher for the 'Reference Type' command set.
*/

void dispatch_reftype(jdwp_command_packet cmd) {

  woempa(7, "Reference Type Command = %s\n", cmd->command > 0 && cmd->command <= REFTYPE_MAX_COMMAND ? reftype_command_names[cmd->command] : "unknown");
  switch((jdwp_reftype_cmd)cmd->command) {
    case jdwp_reftype_signature:
     jdwp_ref_signature(cmd);
      break;
    case jdwp_reftype_classLoader:
      jdwp_ref_classloader(cmd);
      break; 
    case jdwp_reftype_modifiers:
      jdwp_ref_modifiers(cmd);
      break;
    case jdwp_reftype_fields:
      jdwp_ref_fields(cmd);
      break;
    case jdwp_reftype_methods:
      jdwp_ref_methods(cmd);
      break;
    case jdwp_reftype_getValues:
      jdwp_ref_get_values(cmd);
      break;
    case jdwp_reftype_sourceFile:
      jdwp_ref_source(cmd);
      break;
    case jdwp_reftype_interfaces:
      jdwp_ref_interfaces(cmd);
      break;
    case jdwp_reftype_nestedTypes:
      jdwp_ref_nested_types(cmd);
      break;
    case jdwp_reftype_status:
      jdwp_ref_status(cmd);
      break;
    case jdwp_reftype_classObject:
      jdwp_ref_class_object(cmd);
      break;
    case jdwp_reftype_sourceDebugExtension:
      // TODO
    default:
      jdwp_send_not_implemented(cmd->id);
  }

}

