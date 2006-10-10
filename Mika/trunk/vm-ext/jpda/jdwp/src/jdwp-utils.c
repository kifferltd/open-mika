/**************************************************************************
*                                                                         *
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
**************************************************************************/

#include "clazz.h"
#include "core-classes.h"
#include "jdwp.h"

/*
** Get the JDWP status of a clazz.
*/
w_word clazz2status(w_clazz clazz) {
  w_word status;
  int clazz_state = getClazzState(clazz);

  if (clazz_state == CLAZZ_STATE_BROKEN) {
    status = jdwp_cs_error;
  }
  else {
    status = 0;
    if (clazz_state >= CLAZZ_STATE_VERIFIED) {
      status |= jdwp_cs_verified ;
    }
    if (clazz_state >= CLAZZ_STATE_LINKED) {
      status |= jdwp_cs_prepared ;
    }
    if (clazz_state >= CLAZZ_STATE_INITIALIZED) {
      status |= jdwp_cs_initialized ;
    }
  } 

  return status;
}

/*
** Get the JDWP tag for a clazz.
*/
w_ubyte clazz2tag(w_clazz clazz) {
  w_ubyte tag = isSet(clazz->flags, ACC_INTERFACE) ? jdwp_tt_interface : clazz->dims ? jdwp_tt_array : jdwp_tt_class;

  woempa(7, "reftype %k has tag %d\n", clazz, tag);

  return tag;
}

/*
** Get the signature byte for a clazz.
*/
w_ubyte clazz2sigbyte(w_clazz clazz) {
  if (clazzIsPrimitive(clazz)) {
    if (clazz == clazz_boolean) {
      return jdwp_tag_boolean;
    }
    else if (clazz == clazz_char) {
      return jdwp_tag_char;
    }
    else if (clazz == clazz_float) {
      return jdwp_tag_float;
    }
    else if (clazz == clazz_double) {
      return jdwp_tag_double;
    }
    else if (clazz == clazz_int) {
      return jdwp_tag_int;
    }
    else if (clazz == clazz_long) {
      return jdwp_tag_long;
    }
    else if (clazz == clazz_short) {
      return jdwp_tag_short;
    }
    else if (clazz == clazz_byte) {
      return jdwp_tag_byte;
    }
    else if (clazz == clazz_void) {
      return jdwp_tag_void;
    }
    else {
      return 0;
    }
  } 
  else if(clazz->dims) {
    return jdwp_tag_array;
  }
  else { 
    if(isSuperClass(clazzThread, clazz)) {
      return jdwp_tag_thread;
    }
    else if(isSuperClass(clazzThreadGroup, clazz)) {
      return jdwp_tag_thread_group;
    }
    else if(isSuperClass(clazzClassLoader, clazz)) {
      return jdwp_tag_class_loader;
    }
    else if(isSuperClass(clazzString, clazz)) {
      return jdwp_tag_string;
    }
    else {
      return jdwp_tag_object;
    }
  }
}

/*
** Check that an alleged w_clazz pointer is for real.
** Returns TRUE if the pointer is genuine, FALSE otherwise.
*/
w_boolean jdwp_check_clazz(w_clazz clazz) {
  if (!clazz || strcmp(clazz->label, "clazz") != 0) {
    return FALSE;
  }
  else {
    w_instance instance = clazz2Class(clazz);
    w_clazz roundtrip = Class2clazz(instance);

    woempa(7, "  class instance = %j -> clazz %k\n", instance, roundtrip);

    return roundtrip == clazz;
  }
}

/*
** Check that an alleged w_field pointer is for real.
** Returns TRUE if the pointer is genuine, FALSE otherwise.
*/
w_boolean jdwp_check_field(w_field field) {
  return strcmp(field->label, "field") == 0;
}

