/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

/*
** $Id: map.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

inline static x_size round_up(x_size value, x_size rounding) {
  return (value + (rounding - 1)) & ~(rounding - 1);
}

/*
** Returns the number of bytes we need to allocate to have a map that can have
** at least 'entries' number of elements. It will always return a number that
** is divisible by 4 (sizeof(word)).
*/

x_size x_map_size(x_size entries) {
  return (round_up(entries, 32) / 8);
}

x_size x_umap_create(x_umap umap, x_size entries, x_word * table) {

  x_size i;

  /*
  ** Clear all bitpositions. Note that we first save the table pointer before we use
  ** it as a cursor.
  */

  umap->table = table;
  umap->entries = round_up(entries, 32);
    
  for (i = 0; i < umap->entries / 32; i++) {
    *table++ = 0x00000000;
  }

  umap->cache_0 = 0;
  
  return umap->entries;

}

/*
** Find a non set (0) position in the map. Any position will do. The position number
** will be written back in the field pointed to by 'entry'. The position returned will
** be set to 1.
*/

x_status x_umap_any(x_umap umap, x_size * entry) {

  x_word bits;
  x_word mask;
  x_word id;
  x_ushort i = umap->cache_0;

  /*
  ** Preset id to i * 32.
  */
  
  id = i << 5;

  while (i < (umap->entries / 32)) {
    bits = umap->table[i];
    if (bits != 0xffffffff) {
      mask = 0x00000001;
      while (isSet(bits, mask)) {
        mask <<= 1;
        id += 1;
      }
      setFlag(umap->table[i], mask);
      *entry = id;
      umap->cache_0 = i;
      return xs_success;
    }
    id += 32;
    i += 1;
  }

  return xs_no_instance;
  
}

/*
** Release an entry. Return xs_success if successfuly reset to 0, xs_no_instance if out of
** bounds or if the entry was not set to 1.
*/

inline static x_status xi_umap_reset(x_umap umap, x_size entry) {

  x_word mask = 0x00000001;
  x_size i = entry / 32;

  mask <<= (entry % 32);

  if (umap->entries <= entry || isNotSet(umap->table[i], mask)) {
    return xs_no_instance;
  }
  
  /*
  ** Update the 0 cache index if required and unset the bit.
  */

  if (i < umap->cache_0) {
    umap->cache_0 = i;
  }
  
  unsetFlag(umap->table[i], mask);

  return xs_success;

}

x_status x_umap_reset(x_umap umap, x_size entry) {
  return xi_umap_reset(umap, entry);
}

/*
** Return true when the entry bit is set, false when not set or out of bounds.
*/

inline static x_boolean xi_umap_probe(x_umap umap, x_size entry) {

  x_word mask = 0x00000001;

  mask <<= (entry % 32);

  if (umap->entries <= entry || isNotSet(umap->table[entry / 32], mask)) {
    return false;
  }

  return true;

}

x_boolean x_umap_probe(x_umap umap, x_size entry) {
  return xi_umap_probe(umap, entry);
}

/*
** Set a specific entry. Returns true when the status has changed, returns false when the bit was
** set allready, so the table did not change, or when the entry number was out of bounds.
*/

inline static x_boolean xi_umap_set(x_umap umap, x_size entry) {

  x_word mask = 0x00000001;

  mask <<= (entry % 32);

  if (umap->entries <= entry || isSet(umap->table[entry / 32], mask)) {
    return false;
  }

  setFlag(umap->table[entry / 32], mask);

  return true;

}

x_boolean x_umap_set(x_umap umap, x_size entry) {
  return xi_umap_set(umap, entry);
}

/*
** The event map functions. Note that some of the semantics change.
*/

x_status x_map_create(x_map map, x_size entries, x_word * table) {

  x_umap_create(&map->Umap, entries, table);
  return x_event_init(&map->Event, xe_map);

}

inline static x_status xi_map_try_set(x_map map, x_size entry, const x_boolean decrement_competing) {

  x_assert(critical_status);

  /*
  ** Note that the check for the deleted flag should come before the check for event
  ** type integrity since the x_event_destroy function that deletes and event, resets
  ** the type to unknown...
  */

  if (x_event_is_deleted(map)) {
    if (decrement_competing) {
      map->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(map, xe_map)) {
    return xs_bad_element;
  }

  if (x_umap_probe(&map->Umap, entry) == false) {
    x_umap_set(&map->Umap, entry);
    return xs_success;
  }

  return xs_no_instance;
   
}

x_status x_map_set(x_map map, x_size entry, x_window window) {

  x_status status;
  x_thread thread = thread_current;
  
  if (x_in_context_critical(window)) {
    return xs_bad_context;
  }

  x_preemption_disable;

  status = xi_map_try_set(map, entry, false);
  if (status == xs_no_instance) {
    if (window) {
      while (window && status == xs_no_instance) {
        window = x_event_compete_for(thread, &map->Event, window);
        status = xi_map_try_set(map, entry, true);
      }
    }
  }
  
  x_preemption_enable;
  
  return status;

}

inline static x_status x_map_try_any(x_map map, x_size * entry, const x_boolean decrement_competing) {

  x_assert(critical_status);

  /*
  ** Note that the check for the deleted flag should come before the check for event
  ** type integrity since the x_event_destroy function that deletes and event, resets
  ** the type to unknown...
  */

  if (x_event_is_deleted(map)) {
    if (decrement_competing) {
      map->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(map, xe_map)) {
    return xs_bad_element;
  }

  return x_umap_any(&map->Umap, entry);
   
}

x_status x_map_any(x_map map, x_size * entry, x_window window) {

  x_status status;
  x_thread thread = thread_current;
  
  if (x_in_context_critical(window)) {
    return xs_bad_context;
  }

  x_preemption_disable;

  status = x_map_try_any(map, entry, false);
  if (status == xs_no_instance) {
    if (window) {
      while (window && status == xs_no_instance) {
        window = x_event_compete_for(thread, &map->Event, window);
        status = x_map_try_any(map, entry, true);
      }
    }
  }
  
  x_preemption_enable;
  
  return status;

}

x_status x_map_reset(x_map map, x_size entry) {

  x_status status;

  x_preemption_disable;
  
  if (x_event_is_deleted(map)) { 
    status = xs_deleted;
  }
  else if (x_event_type_bad(map, xe_map)) { 
    status = xs_bad_element;
  }
  else {
    status = x_umap_reset(&map->Umap, entry);
    x_event_signal_all(&map->Event);
  }

  x_preemption_enable;
  
  return status;

}

x_status x_map_probe(x_map map, x_size entry, x_boolean * bool) {

  x_status status;

  x_preemption_disable;
  
  if (x_event_is_deleted(map)) { 
    status = xs_deleted;
  }
  else if (x_event_type_bad(map, xe_map)) { 
    status = xs_bad_element;
  }
  else {
    *bool = x_umap_probe(&map->Umap, entry);
    status = xs_success;
  }

  x_preemption_enable;

  return status;

}

x_status x_map_delete(x_map map) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&map->Event);
  x_preemption_enable;

  return status;

}
