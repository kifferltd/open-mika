/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 by Chris Gray,   *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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

#include "clazz.h"
#include "core-classes.h"
#include "fifo.h"
#include "hashtable.h"
#include "heap.h"
#include "ieee754.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "descriptor.h"

#define STRING_HASHTABLE_SIZE                         719
#define INTERNED_STRING_HASHTABLE_SIZE                 19

/*
 * Hashtable which contains all current w_string's. Key = value, so this
 * just tells us a w_string exists. Equality operation is string equality,
 * hashcode is string hashcode.
 */ 
w_hashtable string_hashtable;

/// The empty string
w_string string_empty;

/// The string `<NULL>'
w_string string_NULL;

/// The string `<EMPTY>'
w_string string_EMPTY;

/// The string `<BOGUS>'
w_string string_BOGUS;

static w_word hashString(w_string string);
static w_size string_reclaim_callback(w_int requested, w_instance instance);

static inline w_string allocString(w_size length, w_boolean is_latin1) {

  w_string string;
  
  string = allocMem(offsetof(w_String, contents) + length * (is_latin1 ? sizeof(w_byte) : sizeof(w_char)));
  if (!string) {
    return NULL;
  }
  string->refcount = 0;
  string->interned = NULL;
  string->length_and_flags = length + (is_latin1 ? STRING_IS_LATIN1 : 0);

  return string;

}


/*
** Unpack a w_string into an array of w_char's.  Caller must supply a
** suitably sized buffer (i.e. space for at least source->numChars). 
** Returns the number of w_char's which were found.
*/

w_size w_string2chars(w_string source, w_char *destination) {
  w_size length = string_length(source);

  if (string_is_latin1(source)) {
    w_size i;
    w_char *dst = destination;
    w_ubyte *src = source->contents.bytes;;
    for (i = 0; i < length; ++i) {
      *dst++ = *src++;
    }
  }
  else {
    w_memcpy(destination, source->contents.chars, sizeof(w_char) * length);
  }

  return length;

}

/*
** Extract a substring of a w_string, as a registered w_string.
** Note that no bounds checking is performed by this function!
** Can return NULL if insufficient memory is available.
*/
w_string w_substring(w_string s, w_int offset, w_int length) {
  w_size l = string_length(s);
  w_char *buf = allocMem(l * sizeof(w_char));
  w_string result = NULL;

  if (!buf) {
    return NULL;
  }

  w_string2chars(s, buf);
  result = unicode2String(buf + offset, length);

  releaseMem(buf);

  return result;
}

/*
** Encode a unicode character into UTF-8; the resulting 1 to three bytes
** are written to **destination, and *destination is updated accordingly.
**
** Note that it it is the caller's responsiblility to ensure that sufficient
** space is available after **destination.
*/

void putw_char(w_char ch, w_ubyte **destination) {

  if (ch == 0) {
    *(*destination)++ = 0xC0;
    *(*destination)++ = 0x80;
  }
  else if( ch < 0x80) {
    *(*destination)++ = ch;
  }
  else if(ch < 0x800) {
    *(*destination)++ = 0xC0+(ch >> 6);
    *(*destination)++ = 0x80+(ch & 63);
  }
  else {
    *(*destination)++ = 0xE0 + (ch >> 12);
    *(*destination)++ = 0x80 + ((ch >> 6) & 63);
    *(*destination)++ = 0x80 + (ch & 63);
  }

}

w_int compareStrings(w_string s1, w_string s2) {

  w_size i;
  w_int result = 0; 

  if (string_length(s1) == 0) {
    result -= string_length(s2);
  }  
  else if (string_length(s2) == 0) {
    result = string_length(s1);
  }
  else if (s1 != s2) {
    for (i = 0; i < string_length(s1); i++) {
      if (string_char(s1, i) != string_char(s2, i)) {
        result = string_char(s1, i) - string_char(s2, i);
        break;
      }
      if ((i + 1) >= string_length(s1) || (i + 1) >= string_length(s2)) {
        result = string_length(s1) - string_length(s2);
        break;
      }
    }
  }

  return result;

}


/*
** Convert a Wonka string into an array of UTF8 w_byte's. The caller
** is responsible for releasing the memory afterwards.
** After this function returns, 'count' will be set to the number of 
** bytes that where allocated in the buffer returned.
**
*/

inline static int char_is_singlet(w_word c) {

  if ((w_char)c == 0) {
    return 0;
  }
  else if ((w_char)c & 0xff80) {
    return 0;
  }
  else {
    return 1;
  }

}

inline static int char_is_duplet(w_word c) {

  if ((w_char)c & 0xf800) {
    return 0;
  }
  else {
    return 1;
  }

}

inline static void char2duplet(w_byte *buffer, w_word thechar) {

  w_char c = (w_char)thechar;
  
  buffer[0] = (w_byte)(c>>6) | 0xC0;
  buffer[1] = (w_byte)(c&63) | 0x80;
  
}

inline static void char2triplet(w_byte *buffer, w_word thechar) {

  w_char c = (w_char)thechar;
  
  buffer[0] = (w_byte)(c>>12) | 0xE0;
  buffer[1] = (w_byte)((c>>6)&63) | 0x80;
  buffer[2] = (w_byte)(c&63) | 0x80;
}

w_byte *string2UTF8(w_string string, w_int *count) {

  w_byte *buffer;
  w_byte *handle;
  w_byte *result;
  w_size i;
  w_size l;

  l = 0;

  /*
  ** Allocate a worst case buffer, 3 bytes for each unicode char and
  ** 2 bytes for the length and one for the terminating null byte.
  */

  handle = allocMem(3 + (string_length(string) * (3 * sizeof (w_byte))));
  if (!handle) {

    return NULL;

  }

  buffer = handle + 2;

  for (i = 0; i < string_length(string); i++) {
    w_char ch = string_char(string, i);
    if (char_is_singlet(ch)) {
      *buffer = (w_byte)ch;
      l += 1;
      buffer += 1;
    }
    else if (char_is_duplet(ch)) {
      char2duplet(buffer, ch);
      l += 2;
      buffer += 2;
    }
    else {
      char2triplet(buffer, ch);
      l += 3;
      buffer += 3;
    }
  }
  handle[0] = (w_byte)(l >> 8);
  handle[1] = (w_byte)(l);

  result = reallocMem(handle, l + 3);
  result[l + 2] = 0;
  if (count) {
    *count = l + 2;
  }

  return result;

}


w_byte* chars2UTF8(w_char *chars, w_int length, w_int *count) {

  w_byte *buffer;
  w_byte *handle;
  w_byte *result;
  w_int  i;
  w_size l;

  l = 0;

  /*
  ** Allocate a worst case buffer, 3 bytes for each unicode char and
  ** 2 bytes for the length and one for the terminating null byte.
  */

  handle = allocMem(3 + (length * 3 * sizeof (w_byte)));
  if (!handle) {

    return NULL;

  }

  buffer = handle + 2;

  result = handle;

  for (i = 0; i < length ; i++) {
    w_char ch = chars[i];
    if (char_is_singlet(ch)) {
      *buffer = (w_byte)ch;
      l += 1;
      buffer += 1;
    }
    else if (char_is_duplet(ch)) {
      char2duplet(buffer, ch);
      l += 2;
      buffer += 2;
    }
    else {
      char2triplet(buffer, ch);
      l += 3;
      buffer += 3;
    }
  }
  handle[0] = (w_byte)(l >> 8);
  handle[1] = (w_byte)(l);

  result = reallocMem(handle, l + 3);
  result[l + 2] = 0;
  if (count) {
    *count = l + 2;
  }

  return result;

}

/*
** Convert a UTF8 string into a Wonka string. The string returned is a registered
** string. The UTF8 string probably comes from a class file. We first create some
** small support functions.
*/

inline static w_int is_singlet(w_word a) {

  if (! (a & 0x80)) {
    return 1;
  }

  return 0;
  
}

inline static w_int is_duplet(w_word a, w_word b) {

  if (a & 0xc0) {
    if (! (a & 0x20)) {
      if (b & 0x80) {
        if (! (b & 0x40)) {
          return 1;
        }
      }
    }
  }

  return 0;
  
}

inline static w_int is_triplet(w_word a, w_word b, w_word c) {

  if (a & 0xe0) {
    if (! (a & 0x10)) {
      if (b & 0x80) {
        if (! (b & 0x40)) {
          if (c & 0x80) {
            if (! (c & 0x40)) {
              return 1;
            }
          }
        }
      }
    }
  }

  return 0;
  
}

inline static w_char duplet2char(w_word a, w_word b) {

  w_char result = (w_char)(((a & 0x1f) << 6) + (b & 0x3f));
  
  return result;
  
}

inline static w_char triplet2char(w_word a, w_word b, w_word c) {

  w_char result = (w_char)(((a & 0x0f) << 12) + ((b & 0x3f) << 6) + (c & 0x3f));

  return result;
  
}

/*
** Now the function to create the Wonka string from a UTF8 string of bytes. The
** length argument passed is the number of UTF8 bytes. Note that the UTF8 bytes
** string is not expected to be NULL terminated, so the full length should be processed.
*/

w_string utf2String(const char *utf8string, w_size utf8length) {
  w_string result;
  w_char *chars;
  w_string string;
  w_byte x;
  w_byte y;
  w_byte z;
  w_size idx = 0;
  w_size i = 0;
  w_boolean is_latin1 = WONKA_TRUE;

  /*
  ** Allocate a temporary buffer, we allocate it too be worst case size. This
  ** could be too large, but it will be copied over later to a buffer of a 
  ** correct size in the string itself. The worst case is when all UTF8 bytes
  ** are singlets, so we allocate 1 extra as the length.
  */
  
  if (utf8length) {
    chars = allocMem((utf8length + 1) * sizeof(w_char));
    while (i < utf8length) { // [CG 20000410] WAS: <=
      x = (w_byte)utf8string[i++];

      if (is_singlet(x)) {
        chars[idx++] = (w_char)x;
      }
      else {
// [CG 20000410] add:
         if (i >= utf8length) {
           woempa(9, "Incomplete UTF8 sequence 0x%02x\n", x);
           break;
         }
// [CG 20000410] end
        y = (w_byte)utf8string[i++];
        if (is_duplet(x, y)) {
          w_char c = duplet2char(x, y);
          is_latin1 &= (c < 256);
          chars[idx++] = c;
        }
        else {
// [CG 20000410] add:
           if (i >= utf8length) {
             woempa(9, "Incomplete UTF8 sequence 0x%02x 0x%02x\n", x, y);
             break;
           }
// [CG 20000410] end
          z = (w_byte)utf8string[i++];
          if (is_triplet(x, y, z)) {
            is_latin1 = WONKA_FALSE;
            chars[idx++] = triplet2char(x, y, z);
          }
          else {
            woempa(9, "Unknown UTF8 characters 0x%02x 0x%02x 0x%02x\n", x, y, z);
          }
        }
      }
    }

    string = allocString(idx, is_latin1);
    if (!string) {
      woempa(9, "Unable to allocate w_string\n");
      return NULL;
    }
    if (is_latin1) {
      w_size j;
      w_char *src = chars;
      w_ubyte *dst = string->contents.bytes;
      for (j = 0; j < idx; ++j) {
        *dst++ = *src++;
      }
    }
    else {
      w_memcpy(string->contents.chars, chars, sizeof(w_char) * idx);
    }
    releaseMem(chars);
    hashString(string);
  
    woempa(1, "Created string '%w' (%p).\n", string, string);

    result = registerString(string);  
    if (result != string) {
      woempa(1, "String '%w' (%p) already existed in pool\n", result, result);
    }
  }
  else {
    result = registerString(string_empty);
    woempa(1, "Created empty string (%p)\n", result);
  }

  return result;
  
}

w_char* utf2chars(const char *utf8string,  w_int* length) {
  w_byte x;
  w_byte y;
  w_byte z;
  w_size idx = 0;
  w_int i = 0;
  w_int utf8length = *length;
  w_char* chars  = (w_char*) allocMem(utf8length * sizeof (w_char));

  if(chars == NULL){
    return NULL;
  }

  while (i < utf8length) {
    x = (w_byte)utf8string[i++];
    if (is_singlet(x)) {
      chars[idx++] = (w_char)x;
    }
    else {
      if (i >= utf8length) {
        woempa(9, "Incomplete UTF8 sequence 0x%02x\n", x);
        break;
      }
      y = (w_byte)utf8string[i++];
      if (is_duplet(x, y)) {
        chars[idx++] = duplet2char(x, y);
      }
      else {
        if (i >= utf8length) {
          woempa(9, "Incomplete UTF8 sequence 0x%02x 0x%02x\n", x, y);
          break;
        }
        z = (w_byte)utf8string[i++];
        if (is_triplet(x, y, z)) {
          chars[idx++] = triplet2char(x, y, z);
        }
        else {
          woempa(9, "Unknown UTF8 characters 0x%02x 0x%02x 0x%02x\n", x, y, z);
        }
      }
    }
  }
  *length = idx;
  return chars;
}

/*
** Create a registered Wonka string from a unicode string. The unicode string
** is just an array of w_chars and the length is passed as a separate argument.
*/
w_string unicode2String(w_char *chars, w_size length) {
  w_boolean is_latin1 = WONKA_TRUE;
  w_string string;
  w_string result;
  w_size i;
  
  if (length) {
    for (i = 0; i < length && is_latin1; ++i) {
      is_latin1 &= (chars[i] < 256);
    }
    string = allocString(length, is_latin1);
    if (!string) {
      woempa(9, "Unable to allocate w_string\n");
      return NULL;
    }
    if (is_latin1) {
      w_char *src = chars;
      w_ubyte *dst = string->contents.bytes;
      for (i = 0; i < length; ++i) {
        *dst++ = *src++;
      }
    }
    else {
      w_memcpy(string->contents.chars, chars, sizeof(w_char) * length);
    }
    hashString(string);
    woempa(1, "Created string '%w' (%p).\n", string, string);

    result = registerString(string);  
    if (result != string) {
      woempa(1, "String '%w' (%p) already existed in pool\n", result, result);
    }
  }
  else {
    result = registerString(string_empty);
    woempa(1, "Created empty string (%p)\n", result);
  }

  return result;  
  
}

/*
** Create a Wonka string from a C string. The length should NOT take into
** account the '\0' character of the C string. I.e. one can use strlen()
** to calculate the length when calling this function.
*/

w_string cstring2String(const char *cstring, w_size length) {

  w_string string;
  w_string result;
  w_size i;
  char *src = (char*)cstring;

  string = allocString(length, WONKA_TRUE);
  if (!string) {
    woempa(9, "Unable to allocate w_string for '%s'\n", cstring);
    return NULL;
  }

  if (length) {
    w_ubyte *dst = string->contents.bytes;
    for (i = 0; i < length; i++) {
      *dst++ = *src++;
    }
  }
  else {
    string->length_and_flags = STRING_IS_LATIN1;
  }

  hashString(string);
  woempa(1, "Created string '%w' (%p).\n", string, string);
  
  result = registerString(string);  
  if (result != string) {
    woempa(1, "String '%w' (%p) already existed in pool\n", result, result);
  }

  return result;
}

/*
** The function that attaches a hash value to a w_string.
** From the JDK 1.2 documentation. Notice that the hashcode calculation
** has changed from JDK 1.1 to JDK 1.2 !
*/

w_word hashString(w_string string) {

  w_int hash = 0;
  w_size i;
   
#define string_char(s,n) (string_is_latin1(s)?(w_char)((s)->contents.bytes[n]):(s)->contents.chars[n])
  if (string_is_latin1(string)) {
    for (i=0;i<(w_size)(string_length(string));++i) {
      hash = hash*31 + string->contents.bytes[i];
    }
  }
  else {
    for (i=0;i<(w_size)(string_length(string));++i) {
      hash = hash*31 + string->contents.chars[i];
    }
  }

  string->wonka_hash = hash;

  return hash;
  
}

#ifdef DEBUG
static inline void releaseString(w_string string, const char *file, int line) {
  woempa(1, "Releasing string `%w' (%p): called from %s:%d\n", string, string, file, line);
  _d_releaseMem(string, file, line);
}    
#else
static inline void releaseString(w_string string) {
  releaseMem(string);
}    
#endif

/*
** The hash function for the string hashtable. It should return
** the hash value of the string that should be allready available.
*/

w_word ht_stringHash(w_word string_word) {

  w_string string = (w_string)string_word;

  return string->wonka_hash;
  
}

extern w_int woempa_bytecodecount;

/*
** Register a Wonka string. The result returned is the string that should be
** used for subsequent operations; the argument string that is passed could
** be the same as the result string, if it was not registered yet, or the
** pointer to the allready registered string if it was a duplicate. If there
** was a duplicate, the passed argument string is released.
*/

#ifdef DEBUG_STRINGS
w_string _registerString(w_string string, const char *file, int line) {
#else
w_string registerString(w_string string) {
#endif
 
  w_string result;

  threadMustBeSafe(currentWonkaThread);
  woempa(1, "Registering %w at %p\n", string, string);
  ht_lock(string_hashtable);
  result = (w_string)ht_read_no_lock(string_hashtable, (w_word)string);
  if (result) {
    woempa(1, "Found `%w' at %p, refcount %d \n", result, result, result->refcount);
    if (result != string) {
#ifdef DEBUG
      releaseString(string, file, line);
#else
      releaseString(string);
#endif
    }
  }
  else {
    woempa(1, "Didn't find it ... %w is new\n", string);
    result = string;
    ht_write_no_lock(string_hashtable, (w_word)result, (w_word)result);
  }
  ++result->refcount;
  woempa(1, "Refcount now %d\n", result->refcount);
  ht_unlock(string_hashtable);

  return result;
  
}

/*
** Deregister a string. When we see that the registered count drops to 0, 
** we release the w_String structure.
**
** [CG 20000413]: modified to use ht_deregister
*/

#ifdef DEBUG_STRINGS
void _deregisterString(w_string string, const char *file, int line) {
#else
void deregisterString(w_string string) {
#endif
  threadMustBeSafe(currentWonkaThread);
  woempa(1, "Deregistering %w\n", string);
  ht_lock(string_hashtable);
  if (--string->refcount == 0) {
    ht_erase_no_lock(string_hashtable, (w_word)string);
#ifdef DEBUG
    releaseString(string, file, line);
#else
    releaseString(string);
#endif
  }
  ht_unlock(string_hashtable);
}

/*
** If the w_string referenced by theString has no canonical instance, record
** 'theString' as the cononical instance and return it as the result. If the
** w_string already has a canonical instance, return the canonical instance.
** The caller of this function must own the lock on string_hashtable(!).
*/
w_instance internString(w_thread thread, w_instance theString) {
  w_string s = String2string(theString);
  w_instance existing = getCanonicalStringInstance(thread, s);
  if (existing) {

    return existing;

  }

  s->interned = theString;

  return theString;
}

/*
** If theString is the canonical entry for the w_string it references, remove
** the reference to it as canonical instance. The caller of this function must
** own the lock on string_hashtable(!).
*/
void uninternString(w_thread thread, w_instance theString) {
  w_string s = String2string(theString);
  
  if (s && s->interned == theString) {
    s->interned = NULL;
  }
}

/*
** The compare function for the string hashtable.
*/

w_boolean ht_stringCompare(w_word string1_word, w_word string2_word) {

  w_string string1 = (w_string)string1_word;
  w_string string2 = (w_string)string2_word;
  w_size   length1 = string_length(string1);
  w_size   length2 = string_length(string2);
  w_boolean result = WONKA_FALSE;

  /*
  ** Do a quick check first, when the two memory addresses match, the strings match.
  ** Otherwise, try some other checks, starting with the cheapest comparisons first and
  ** ending with a memory compare over the unicode strings.
  */

  woempa(1, "Comparing '%w' (%p) with '%w' (%p)\n", string1, string1, string2, string2);
  if (string1 == string2) {
    woempa(1, "Identical, return TRUE\n");
    result = WONKA_TRUE;
  }
  else if (length1 == 0 && length2 == 0) {
    woempa(1, "Both empty, return TRUE\n");
    result = WONKA_TRUE;
  }
  else {
    w_boolean is_latin1 = string_is_latin1(string1);

    woempa(1, "String1 is %slatin-1\n", is_latin1 ? "" : "not ");

    if (string_is_latin1(string2) == is_latin1 && length1 == length2) {
      woempa(1, "String2 is %slatin-1, both have length %d, hashcodes are 0x%08x/0x%08x\n", is_latin1 ? "" : "not ", length1, string1->wonka_hash, string2->wonka_hash);
      if (string1->wonka_hash == string2->wonka_hash) {
        woempa(1, "Comparing %d bytes starting at %p/%p\n", length1 * (is_latin1 ? 1 : 2), string1->contents.bytes, string2->contents.bytes);
        if (memcmp(string1->contents.bytes, string2->contents.bytes, (is_latin1 ? sizeof(w_ubyte) : sizeof(w_char)) * length1) == 0) {
          result = WONKA_TRUE;
        }
      }
    }
  }

  woempa(1, "Compared (%p) %w and (%p) %w: they are %sequal.\n", string1, string1, string2, string2, result ? "" : "NOT ");
  
  return result;

}

char * print_string(char * buffer, int * remain, void * s, w_int width, w_int prec, w_flags flags) {

  w_string string = s;
  w_size padchars;
  w_size length;
  w_size i;

  if (*remain < 1) {

    return buffer;

  }

  if (s) {
    length = string_length(string);
    padchars = 0;
    if (width) {
      if (length > (w_size)width) {
        length = width;
      }
      else {
        padchars = width - length;
      }
    }

    if (flags & FMT_ALIGN_LEFT) {
      while (padchars-- && *remain > 0) {
        *buffer++ = ' ';
        *remain -= 1;
      }
    }
    
    for (i = 0; *remain > 0 && i < length; ++i) {
      w_char ch = string_char(string, i);

      switch(ch/32) {
      case 1:
      case 2:
      case 3:
      case 5:
      case 6:
      case 7:
        *buffer++ = ch;
        break;

      default:
        *buffer++ = '?';
      }
      *remain -= 1;
    }

    if (!(flags & FMT_ALIGN_LEFT)) {
      while (padchars-- && *remain > 0) {
        *buffer++ = ' ';
        *remain -= 1;
      }
    }

  }
  else {
    *buffer++ = '<';
    *buffer++ = 'n';
    *buffer++ = 'u';
    *buffer++ = 'l';
    *buffer++ = 'l';
    *buffer++ = '>';
    *remain -= 6;
  }
              
  return buffer;
                
}
                
void startStrings() {

  /*
  ** Allocate the hashtable in which the strings will be stored.
  */

  string_hashtable = ht_create((char *)"hashtable:strings", STRING_HASHTABLE_SIZE, ht_stringHash, ht_stringCompare, 0, 0);
  woempa(7, "created string_hashtable at %p\n", string_hashtable);

  /*
  ** Set up the special strings
  */

  string_empty = cstring2String("",0);
  string_NULL = cstring2String("<NULL>",6);
  string_EMPTY = cstring2String("<EMPTY>",7);
  string_BOGUS = cstring2String("<BOGUS>",7);

  woempa(7, "Initialised string tables and static strings.\n");
  woempa(7, " string_empty = %p '%w'\n", string_empty, string_empty);
  woempa(7, " string_NULL  = %p '%w'\n", string_NULL, string_NULL);
  woempa(7, " string_EMPTY = %p '%w'\n", string_EMPTY, string_EMPTY);
  woempa(7, " string_BOGUS = %p '%w'\n", string_BOGUS, string_BOGUS);
  registerReclaimCallback(string_reclaim_callback);
}

w_int stringAdd(w_char *destination, w_string source) {

  if (string_is_latin1(source)) {
    w_size i;
    w_size l = string_length(source);
    w_char *dst = destination;
    w_ubyte *src = source->contents.bytes;

    for (i = 0; i < l; ++i) {
      *dst++ = *src++;
    }
  }
  else {
    w_memcpy(destination, source->contents.chars, string_length(source) * sizeof(w_char));
  }

  return string_length(source);

}

w_string dots2slashes(w_string string) {
  w_string result = string;
  w_size length = string_length(string);
  w_size i;

  woempa(1, "Converting string '%w'.\n", result);

  if (length) {
    w_boolean is_latin1 = string_is_latin1(string);

    result = allocString(length, is_latin1);
    if (!result) {
      woempa(9, "Unable to allocate target w_string for '%w'\n", string);
      return NULL;
    }
    if (is_latin1) {
      w_ubyte ch;

      for (i = 0; i < length; ++i) {
        ch = string->contents.bytes[i];
        if (ch == '.') {
          ch = '/';
        }
        else if (ch == '/') {
          woempa(9,"Wow!  Found a slush char, converted it to a DEL.\n");
          ch = 127;
        }
        result->contents.bytes[i] = ch;
      }
    }
    else {
      w_char ch;

      for (i = 0; i < length; ++i) {
        ch = string->contents.chars[i];
        if (ch == '.') {
          ch = '/';
        }
        else if (ch == '/') {
          woempa(9,"Wow!  Found a slysh char, converted it to a DEL.\n");
          ch = 127;
        }
        result->contents.chars[i] = ch;
      }
    }
    hashString(result);
    woempa(1, "Created string '%w' (%p).\n", result, result);
  }
  else {
    woempa(1, "Empty string, returning it unchanged\n");
  }
  
  result = registerString(result);  

  return result;

}

w_string slashes2dots(w_string string) {
  w_string result = string;
  w_size length = string_length(string);
  w_size i;

  woempa(1, "Converting string '%w'.\n", result);

  if (length) {
    w_boolean is_latin1 = string_is_latin1(string);

    result = allocString(length, is_latin1);
    if (!result) {
      woempa(9, "Unable to allocate target w_string for '%w'\n", string);
      return NULL;
    }
    if (is_latin1) {
      w_ubyte ch;

      for (i = 0; i < length; ++i) {
        ch = string->contents.bytes[i];
        if (ch == '/') {
          ch = '.';
        }
        else if (ch == 127) {
          woempa(9,"Wow!  Found a DEL char, converted it to a slesh.\n");
          ch = '/';
        }
        result->contents.bytes[i] = ch;
      }
    }
    else {
      w_char ch;

      for (i = 0; i < length; ++i) {
        ch = string->contents.chars[i];
        if (ch == '/') {
          ch = '.';
        }
        else if (ch == 127) {
          woempa(9,"Wow!  Found a DEL char, converted it to a slush.\n");
          ch = '/';
        }
        result->contents.chars[i] = ch;
      }
    }
    hashString(result);
    woempa(1, "Created string '%w' (%p).\n", result, result);
  }
  else {
    woempa(1, "Empty string, returning it unchanged\n");
  }
  
  result = registerString(result);  

  return result;

}

#ifdef DEBUG_STRINGS
w_instance _newStringInstance(w_string s, const char *file, int line) {
#else
w_instance newStringInstance(w_string s) {
#endif

  w_instance instance;
  w_thread thread = currentWonkaThread;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  instance = allocStringInstance(thread);
  enterSafeRegion(thread);
  if (instance) {
    w_string r = registerString(s);
    setWotsitField(instance, F_String_wotsit, r);
    woempa(1, "allocated instance %p of String for '%w'\n", instance, r);
  }
  else {
    woempa(9, "Unable to allocate instance of String for '%w'\n", s);
  }

  return instance;

}

w_instance getStringInstance(w_string s) {
  w_thread thread = currentWonkaThread;
  w_string r;
  w_instance new_instance;
  w_instance canonical;
  w_word *flagsptr;

  /*
  ** The story so far:
  ** - we could be anywhere in a GC cycle, (mark, sweep, complete)
  ** - w_string r will not be reclaimed, because it is registered to us
  */
  threadMustBeSafe(thread);
  r = registerString(s);
  ht_lock(string_hashtable);
  canonical = getCanonicalStringInstance(thread, s);
  ht_unlock(string_hashtable);

  /*
  ** If we found a canonical instance, we aren't creating a new reference to 
  ** the w_string, so we deregister it.
  */
  if (canonical) {
    deregisterString(r);

    return canonical;

  }

  /*
  ** OK, no canonical instance up to now so we create a new one. It's safe 
  ** from being GC'd, because allocInstance() creates a local reference to it.
  */
  enterUnsafeRegion(thread);
  new_instance = allocStringInstance(thread);
  enterSafeRegion(thread);

  if (!new_instance) {
    deregisterString(r);

    return NULL;

  }

  ht_lock(string_hashtable);
  if (r->interned) {
    canonical = r->interned;
    deregisterString(r);
    ht_unlock(string_hashtable);
    removeLocalReference(thread, new_instance);

    return canonical;

  }
  else {
    r->interned = new_instance;
  }

  /*
  ** Otherwise, everything's just fine.
  */
  setWotsitField(new_instance, F_String_wotsit, r);
  ht_unlock(string_hashtable);

  return new_instance;
}


#ifdef DEBUG
static w_int string_reclaim_count = 0;
static w_size string_reclaim_callback(w_int requested, w_instance instance) {
  //if (((string_reclaim_count++)%10)==0) dumpStrings();

  //printf("String hashtable size : %d\n", string_hashtable->occupancy);
  return 0;
}
#else
static w_size string_reclaim_callback(w_int requested, w_instance instance) {
  return 0;
}
#endif

#ifdef DEBUG

/*
** The total number of Unicode characters we have allocated.
*/

static w_int total_length = 0;

int string_sort(const void *v_string1, const void *v_string2) {

  w_string string1 = *(w_string *)v_string1;
  w_string string2 = *(w_string *)v_string2;
  w_int result;

  /*
  ** We compare on the basis of the memory pointer of the two strings so that strings
  ** that are allocated together end up together in the list.
  */

  result = (w_int)string1 - (w_int)string2;
  
  return result;
  
}

void string_iterate(w_word string_word, w_word dummy1, void * dummy2, void * dummy3) {
  w_string string = (w_string)string_word;

  wprintf("%p 0x%08x 0x%08x %6d '%w'\n", string, string->length_and_flags, string->wonka_hash, string->refcount, string); 
  total_length += string_length(string);
}


void dumpStrings(void) {
  wprintf("BEGIN string dump\n");
  wprintf("  address  flags/len   hashcode  refcnt contents\n");
  ht_iterate(string_hashtable, string_iterate, NULL, NULL);  
  wprintf("  address  flags/len   hashcode  refcnt contents\n");
  //wprintf("  momeness = %d\n", ht_momeness(string_hashtable));
  wprintf("END string dump\n");
  woempa(9, "Total number of bytes for Unicode characters: %6d\n", total_length * sizeof(w_char));
  woempa(9, "Number of bytes for w_String (%d bytes) structures: %6d\n", sizeof(w_String), string_hashtable->occupancy * sizeof(w_String));
}
#endif

