/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java         *
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

#include "checks.h"
#include "clazz.h"
#include "core-classes.h"
#include "fields.h"
#include "heap.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "oswald.h"
#include "wonka.h"

#ifdef DEBUG
static const char* object_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "ReferenceType",
  /*  2 */ "GetValues",
  /*  3 */ "SetValues",
  /*  4 */ NULL,
  /*  5 */ "MonitorInfo",
  /*  6 */ "InvokeMethod",
  /*  7 */ "DisableCollection",
  /*  8 */ "EnableCollection",
  /*  9 */ "IsCollected",
};

#define OBJECT_REFERENCE_MAX_COMMAND 9

#endif

/*
** The lowest objectID which has been issued and not yet rescinded.
*/
static w_int oldest_object_id;

/*
** The next objectID which will be issued.
*/
static w_int next_object_id;

/*
** A wordset containing the w_instance and reference count corresponding to
** every valid objectID. Entry (objectID - oldest_object_id) * 2 contains
** the w_instance pointer, entry objectID - oldest_object_id) * 2 + 1 the
** reference count (number of times the objectID was sent in a message).
** If the reference count is zero the objectID is no lmonger valid.
** When an object is garbage collected, the w_instance pointer is set to zero.
*/
static w_wordset object_id_wordset;

/*
** A hashtable mapping w_instance to objectID. Its lock is also used to
** protect the object_id_wordset.
*/
static w_hashtable object_id_hashtable;

/*
** If one or more elements at the start of object_id_wordset have refcount zero,
** remove them and increment oldest_object_id accordingly. Caller should lock
** object_id_hashtable.
*/
static void compact_object_id_wordset(void) {
  w_size i;
  w_size n = 0;

  while (elementOfWordset(&object_id_wordset, n * 2 + 1) == 0) ++n;
  woempa(7, "Removing %d obsolete objectIDs, oldest is %d\n", n, oldest_object_id);

  for (i = n * 2; i < sizeOfWordset(&object_id_wordset); ++i) {
    woempa(7, "Copying value '%d' from slot[%d] to slot[%d]\n", elementOfWordset(&object_id_wordset, i + n * 2), i + n * 2, i);
    modifyElementOfWordset(&object_id_wordset, i, elementOfWordset(&object_id_wordset, i + n * 2));
  }

  for (i = 0; i < n * 2; ++i) takeLastFromWordset(&object_id_wordset);

  oldest_object_id += n;
  woempa(7, "Oldest objectID is now %d\n", oldest_object_id);
}

/*
** Increment the refcount of objectID. Returns the new refcount.
*/
static w_int bump_object_id_refcount(w_word objectID) {
  w_int windex = (objectID - oldest_object_id) * 2 + 1;
  w_int newcount = elementOfWordset(&object_id_wordset, windex) + 1;

  woempa(7, "Incrementing refcount of objectID %d to %d\n", objectID, newcount);
  modifyElementOfWordset(&object_id_wordset, windex, newcount);

  return newcount;
}

/*
** Convert an objectID into a w_instance pointer. If the objectID is not
** valid, or the instance has been garbage collected, NULL is returned.
*/
w_instance jdwp_objectID2instance(w_word objectID) {
  w_instance result;

  if (!object_id_hashtable || (w_int)objectID < oldest_object_id || (w_int)objectID >= next_object_id) {

    return NULL;

  }

  ht_lock(object_id_hashtable);
  result = (w_instance)elementOfWordset(&object_id_wordset, (objectID - oldest_object_id) * 2);
  woempa(7, "objectID %d is %j\n", objectID, result);
  ht_unlock(object_id_hashtable);

  return result;
}

/*
** Convert a w_instance pointer to an objectID. If an objectID was already
** associated with the instance, increment its reference count; otherwise
** create a new objectID with a reference count of 1.
*/
w_word jdwp_instance2objectID(w_instance instance) {
  w_word objectID;

  if (!instance) {
    return 0;
  }

  if (!object_id_hashtable) {
    woempa(7, "Creating object_id_hashtable\n");
    object_id_hashtable = ht_create("hashtable:objectID", 37, NULL, NULL, 0, 0);
    oldest_object_id = next_object_id = 1;
  }

  ht_lock(object_id_hashtable);
  objectID = ht_read_no_lock(object_id_hashtable, (w_word)instance);
  if (!objectID) {
    woempa(7, "%j is not yet in object_id_hashtable, adding it with objectID %d\n", instance, next_object_id);
    objectID = next_object_id++;
    addToWordset(&object_id_wordset, (w_word)instance);
    addToWordset(&object_id_wordset, 1);
    ht_write_no_lock(object_id_hashtable, (w_word)instance, objectID);
  }
  else {
    woempa(7, "%j found in object_id_hashtable with objectID %d\n", instance, objectID);
    bump_object_id_refcount(objectID);
  }
  ht_unlock(object_id_hashtable);

  return objectID;
}

/*
** Decrement the refcount of objectID by the given amount, but don't let it
** go negative. If the resulting refcount is zero, remove the objectID from
** the hashtable also, and clear any JDWP-releated flags. 
** Returns the new refcount.
*/
w_int jdwp_decrement_object_id_refcount(w_word objectID, w_int amount) {
  w_int windex = (objectID - oldest_object_id) * 2 + 1;
  w_int oldcount;
  w_int newcount = 0;
  w_boolean do_notify = FALSE;

  ht_lock(object_id_hashtable);
  if (windex > 0 && windex < (w_int)sizeOfWordset(&object_id_wordset)) {
    oldcount = elementOfWordset(&object_id_wordset, windex);
    if (amount >= oldcount) {
      w_instance instance = (w_instance)elementOfWordset(&object_id_wordset, windex - 1);
      if (instance) {
        unsetFlag(instance2flags(instance), O_JDWP_BLACK);
        if (isSet(instance2clazz(instance)->flags, CLAZZ_IS_THREAD)) {
          w_thread thread = getWotsitField(instance, F_Thread_wotsit);
          if (thread) {
            unsetFlag(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK);
            do_notify = TRUE;
          }
        }
      }
      modifyElementOfWordset(&object_id_wordset, windex, 0);
      compact_object_id_wordset();
    }
    else {
      newcount = oldcount - amount;
    }
    woempa(7, "Decrementing refcount of objectID %d by %d to %d\n", objectID, amount, newcount);
    modifyElementOfWordset(&object_id_wordset, windex, newcount);
  }
  ht_unlock(object_id_hashtable);

  if (do_notify) {
    x_monitor_eternal(safe_points_monitor);
    x_monitor_notify_all(safe_points_monitor);
    x_monitor_exit(safe_points_monitor);
  }

  return newcount;
}

/*
** Called by the garbage collector to signal that it is about to reclaim 
** an instance. Removes the instance from object_id_hashtable/wordset.
*/
void jdwp_set_garbage(w_instance instance) {
  w_word objectID;

  if (!object_id_hashtable) {

    return;

  }

  ht_lock(object_id_hashtable);
  objectID = ht_read_no_lock(object_id_hashtable, (w_word)instance);
  if (objectID) {
    woempa(7, "%j found in object_id_hashtable with objectID %d, removing it\n", instance, objectID);
    ht_erase_no_lock(object_id_hashtable, (w_word)instance);
    modifyElementOfWordset(&object_id_wordset, (objectID - oldest_object_id) * 2, 0);
  }
  ht_unlock(object_id_hashtable);
  
}

/*
** Get the reference type of the given instance.
*/

w_void jdwp_object_reference_type(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance objectID;
  w_clazz reftypeID;

  objectID = jdwp_get_objectref(cmd->data, &offset);
  // TODO: reject if not an instance
  woempa(7, " Object ID: %j\n", objectID);

  reftypeID = instance2clazz(objectID);

  jdwp_put_tagged_type(&reply_grobag, reftypeID);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}


/*
** Get the values of the given fields of the given instance. The fields should be
** members of the given instance or one of it's superclasses, superinterfaces or
** implemented interfaces. There is no access control, so private fields can be
** obtained aswell.
*/

w_void jdwp_object_get_values(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_clazz value_clazz;
  w_field field;
  w_int count;
  w_int        i;

  instance = jdwp_get_objectref(cmd->data, &offset);
  count = jdwp_get_u4(cmd->data, &offset);
  jdwp_put_u4(&reply_grobag, count);
  woempa(7, "Instance = %j, requested %d fields\n", instance, count);

  for(i = 0; i < count; i++) {
    field = jdwp_get_field(cmd->data, &offset);
    woempa(7, "  %v\n");
    value_clazz = field->value_clazz;

    if (clazzIsPrimitive(value_clazz)) {
      if (value_clazz == clazz_boolean) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_boolean);
        jdwp_put_u1(&reply_grobag, getBooleanField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_char) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_char);
        jdwp_put_u2(&reply_grobag, getCharacterField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_float) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_float);
        jdwp_put_u4(&reply_grobag, getFloatField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_double) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_double);
        jdwp_put_u4(&reply_grobag, instance[field->size_and_slot + WORD_MSW]);
        jdwp_put_u4(&reply_grobag, instance[field->size_and_slot + WORD_LSW]);
      }
      else if (value_clazz == clazz_byte) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_byte);
        jdwp_put_u1(&reply_grobag, getByteField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_short) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_short);
        jdwp_put_u2(&reply_grobag, getShortField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_int) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_int);
        jdwp_put_u4(&reply_grobag, getIntegerField(instance, field->size_and_slot));
      }
      else if (value_clazz == clazz_long) {
        jdwp_put_u1(&reply_grobag, jdwp_tag_long);
        jdwp_put_u4(&reply_grobag, instance[field->size_and_slot + WORD_MSW]);
        jdwp_put_u4(&reply_grobag, instance[field->size_and_slot + WORD_LSW]);
      }
      else {
        jdwp_put_u1(&reply_grobag, jdwp_tag_void);
      }
    }
    else if(value_clazz->dims) {
      woempa(7, "         Array field\n");
      jdwp_put_u1(&reply_grobag, jdwp_tag_array);
      jdwp_put_objectref(&reply_grobag, getReferenceField(instance, field->size_and_slot));
    }
    else { 
      if(isSet(value_clazz->flags, CLAZZ_IS_CLASSLOADER)) {
        woempa(7, "       Classloader field\n");
        jdwp_put_u1(&reply_grobag, jdwp_tag_class_loader);
      }
      if(isSet(value_clazz->flags, CLAZZ_IS_THREAD)) {
        woempa(7, "       Thread field\n");
        jdwp_put_u1(&reply_grobag, jdwp_tag_thread);
      }
      else if(isSuperClass(clazzThreadGroup, value_clazz)) {
        woempa(7, "       Threadgroup field\n");
        jdwp_put_u1(&reply_grobag, jdwp_tag_thread_group);
      }
      else if(value_clazz == clazzString) {
        woempa(7, "       String field\n");
        jdwp_put_u1(&reply_grobag, jdwp_tag_string);
      }
      else {
        woempa(7, "       reference field\n");
        jdwp_put_u1(&reply_grobag, jdwp_tag_object);
      }
      jdwp_put_objectref(&reply_grobag, getReferenceField(instance, field->size_and_slot));
    }
  }

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
  

/*
** Set the values of the given fields of the given instance. The fields should be
** members of the given instance or one of it's superclasses, superinterfaces or
** implemented interfaces. There is no access control, so private fields can be
** obtained aswell.
*/

w_void jdwp_object_set_values(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_clazz value_clazz;
  w_field field;
  w_int count;
  w_int        i;

  instance = jdwp_get_objectref(cmd->data, &offset);
  count = jdwp_get_u4(cmd->data, &offset);
  woempa(7, "Instance = %j, setting %d fields\n", instance, count);

  for(i = 0; i < count; i++) {
    field = jdwp_get_field(cmd->data, &offset);
    woempa(7, "  %v\n");
    value_clazz = field->value_clazz;

    if (clazzIsPrimitive(value_clazz)) {
      if (value_clazz == clazz_boolean) {
        setBooleanField(instance, field->size_and_slot, jdwp_get_u1(cmd->data, &offset));
      }
      else if (value_clazz == clazz_char) {
        setCharacterField(instance, field->size_and_slot, jdwp_get_u2(cmd->data, &offset));
      }
      else if (value_clazz == clazz_float) {
        setFloatField(instance, field->size_and_slot, jdwp_get_u4(cmd->data, &offset));
      }
      else if (value_clazz == clazz_double) {
        (wordFieldPointer(instance, field->size_and_slot))[WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
        (wordFieldPointer(instance, field->size_and_slot))[WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
      }
      else if (value_clazz == clazz_byte) {
        setByteField(instance, field->size_and_slot, jdwp_get_u1(cmd->data, &offset));
      }
      else if (value_clazz == clazz_short) {
        setShortField(instance, field->size_and_slot, jdwp_get_u2(cmd->data, &offset));
      }
      else if (value_clazz == clazz_int) {
        setIntegerField(instance, field->size_and_slot, jdwp_get_u4(cmd->data, &offset));
      }
      else if (value_clazz == clazz_long) {
        (wordFieldPointer(instance, field->size_and_slot))[WORD_MSW] = jdwp_get_u4(cmd->data, &offset);
        (wordFieldPointer(instance, field->size_and_slot))[WORD_LSW] = jdwp_get_u4(cmd->data, &offset);
      }
      else {
        // void ??? error ???
      }
    }
    else {
      setReferenceField(instance, jdwp_get_objectref(cmd->data, &offset), field->size_and_slot);
    }
  }

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
  

/*
** Disable garbage collection of the given instance, by setting it JDWP_BLACK.
*/

w_void jdwp_object_disable_collection(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;

  instance = jdwp_get_objectref(cmd->data, &offset);
  setFlag(instance2flags(instance), O_JDWP_BLACK);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
  

/*
** Enable garbage collection of the given instance, by JDWP_BLACK.
*/

w_void jdwp_object_enable_collection(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;

  instance = jdwp_get_objectref(cmd->data, &offset);
  unsetFlag(instance2flags(instance), O_JDWP_BLACK);

  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
  

/*
** Return 1 if the objectID was valid but has been garbage-collected, 0 otherwised.
*/

void jdwp_object_is_collected(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_instance instance;
  w_word objectID = jdwp_get_u4(cmd->data, &offset);

  if (!object_id_hashtable || (w_int)objectID < oldest_object_id || (w_int)objectID >= next_object_id) {
    jdwp_send_invalid_object(cmd->id);

    return;
  }

  ht_lock(object_id_hashtable);
  instance = (w_instance)elementOfWordset(&object_id_wordset, (objectID - oldest_object_id) * 2);
  woempa(7, "objectID %d is %j\n", objectID, instance);
  ht_unlock(object_id_hashtable);

  if (instance) {
    jdwp_put_u1(&reply_grobag, 0);
  }
  else {
    // The debugger knows this reference is dead, so zero its refcount
    w_int windex = (objectID - oldest_object_id) * 2 + 1;

    ht_lock(object_id_hashtable);
    modifyElementOfWordset(&object_id_wordset, windex, 0);
    compact_object_id_wordset();
    ht_unlock(object_id_hashtable);
    jdwp_put_u1(&reply_grobag, 1);
  }
  jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
}
  

/*
** The dispatcher for the 'Object reference' command set.
*/

w_void dispatch_objref(jdwp_command_packet cmd) {

  woempa(7, "Object Reference Command = %s\n", cmd->command > 0 && cmd->command <= OBJECT_REFERENCE_MAX_COMMAND ? object_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_objref_cmd)cmd->command) {
    case jdwp_objref_referenceType:
      jdwp_object_reference_type(cmd);
       break;
    case jdwp_objref_getValues:
      jdwp_object_get_values(cmd);
      break;
    case jdwp_objref_setValues:
      jdwp_object_set_values(cmd);
      break;
    case jdwp_objref_disableCollection:
      jdwp_object_disable_collection(cmd);
      break;
    case jdwp_objref_enableCollection:
      jdwp_object_enable_collection(cmd);
      break;
    case jdwp_objref_monitorInfo:
// TODO
      jdwp_send_not_implemented(cmd->id);
      break;
    case jdwp_objref_invokeMethod:
// TODO
      jdwp_send_not_implemented(cmd->id);
      break;
    case jdwp_objref_isCollected:
      jdwp_object_is_collected(cmd);
      break;
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

