/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2009 by Chris Gray,         *
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

#ifndef _WSTRINGS_H
#define _WSTRINGS_H

#include "fields.h"
#include "oswald.h"
#include "wonka.h"

/*
** Define F_String_wotsit. This way we don't need to include the huge core-classes.h file.
*/

extern w_int F_String_wotsit; 

/*
** DEBUG_STRINGS is always defined if DEBUG is defined, but you can also define
** it independently.
*/
#ifdef DEBUG
#define DEBUG_STRINGS
#endif
#define DEBUG_STRINGS

/*
** The Wonka string structure.  Incorporates a reference count and a cached
** copy of the hash function.  Use function hashString() to calculate and 
** cache the hashcode.
*/

typedef struct w_String {
  w_word      refcount;
  w_word      wonka_hash;
  w_instance  interned;
  w_size      length_and_flags;
  union {
  w_ubyte     bytes[8];
  w_char      chars[4];
  } contents;
} w_String;

#define string_length(s) ((s)->length_and_flags & 0x00ffffff)

#define STRING_IS_LATIN1 0x80000000
#define string_is_latin1(s) (!!((s)->length_and_flags & STRING_IS_LATIN1))

#define string_char(s,n) (string_is_latin1(s)?(w_char)((s)->contents.bytes[n]):(s)->contents.chars[n])

/*
** methods used by the character decoders!
*/
w_char*  utf2chars(const char *utf8string, w_int* utf8length);

/*
** The compareStrings function does a compare on two unicode Wonka strings
** as documented in the compareTo method for java/lang/String.
*/

w_int compareStrings(w_string string1, w_string string2);

/*
** Extract a substring of a w_string, as a registered w_string.
** Note that no bounds checking is performed by this function!
** Can return NULL if insufficient memory is available.
*/
w_string w_substring(w_string s, w_int offset, w_int length);

/*
** w_string2chars converts a w_string to an array of unicode characters.
** The caller is responsible for providing a sufficient amount of memory
** for the unpacked characters.  The result returned is the number of
** w_chars written (equal to source->numChars).
** The source w_string need not be registered, but *must* be well-formed.
*/

w_size w_string2chars(w_string source, w_char *dest);

#define WONKA_DUMMY_CHAR 0xFFFF

/*
** putw_char outputs one unicode character as UTF8.  The character is
** stored at *dest, and dest is then incremented by 1, 2 or 3 as appropriate.
*/

void putw_char(w_char ch, w_ubyte **dest);

/*
** string2UTF8 converts the given w_string into a UTF8 string.
** The UTF8 string is returned as a byte array in which the first two bytes
** are a 16-byte character count (most significant byte first).  A null byte 
** is also appended, but is not included in the count.
** If 'buflen' non-NULL the total length of the UTF8 string (including the
** two count bytes, but excluding the terminating null byte) is returned in
** '*buflen'. 
** The memory used for the UTF8 string is allocated from the system pool. The
** caller is responsible for returning this memory when it is no longer needed.
*/
w_ubyte *string2UTF8(w_string string, w_int * buflen);

/*
** chars2UTF8 converts the 'length' unicode chars at 'chars' into a UTF8 string.
** The UTF8 string is returned as a byte array in which the first two bytes
** are a 16-byte character count (most significant byte first). The same count
** is also returned as a w_int in 'count'. A null byte is also appended, but 
** is not included in the count.
** The memory used for the UTF8 string is allocated from the system pool. The
** caller is responsible for returning this memory when it is no longer needed.
*/
w_ubyte* chars2UTF8(w_char *chars, w_int length, w_int *count);

/*
** w_charLength calculates the length in characters of a well-formed UTF8 string.
*/

w_size w_charLength(w_ubyte *utf8);

/*
** compareUtf8 compares two UTF8 strings
*/

w_int compareUtf8(w_ubyte* utf8a, w_ubyte* utf8b);

/*
** isPrefixUtf8 determines whether string a is a prefix of string b
*/

w_boolean isPrefixUtf8(w_ubyte* utf8a, w_ubyte* utf8b);

/*
** Dump the string hashtable to stdout
*/

void stringDump(void);


/*
** ht_stringCompare() compares two strings for equality and returns WONKA_TRUE
** iff they are identical.  Canonical equality function for string-based
** hashtables.
*/
w_boolean ht_stringCompare(w_word string1_word, w_word string2_word);

/*
** ht_stringHash() returns the hashcode cached within a w_string.  Use this
** for string-based hashtables.
*/
w_word ht_stringHash(w_word string_word);

/*
** cstring2String converts a C (ASCII) string into a registered w_string.
** The terminating null byte need not be present, and is not included
** in the length.
*/
w_string cstring2String(const char *cstring, w_size length);

/*
** unicode2String converts a sequence of unicode characters into a registered w_string.
*/
w_string unicode2String(w_char *chars, w_size length);

/*
** Create a Wonka string from a UTF8 string of bytes. The length argument 
**is the number of UTF8 bytes, not the number of characters! No null terminating** byte is expected, so if one is present it should not be included in the
** length argument.
*/
w_string utf2String(const char *utf8string, w_size length);

extern w_hashtable string_hashtable;

/*
** Register a Wonka string. The result returned may be the same as the argument
** passed (if the string is "new"), or it may be different (an already existing
** string). In the latter case, the string passed as argument is automatically
** released. 
*/
#ifdef DEBUG_STRINGS
#define registerString(s) _registerString((s),__FILE__,__LINE__)
w_string _registerString(w_string string, const char *file, int line);
#else
w_string registerString(w_string string);
#endif

/*
** Deregister a Wonka string. When a string has been deregistered as many
** times as it has been registered, the string will be realeased.
*/
#ifdef DEBUG_STRINGS
#define deregisterString(s) _deregisterString((s),__FILE__,__LINE__)
void _deregisterString(w_string string, const char *file, int line);
#else
void deregisterString(w_string string);
#endif

/*
** Get the canonical String instance (if any) associated with this w_string.
** The caller of this function must own the lock on string_hashtable(!):
** this ensures that the logic in collector.c to reclaim canonical instances
** either runs before this (so we will not find  a canonical instance) or runs
** after it (and it will see our O_BLACK flag and not reclaim the instance).
*/
static inline w_instance getCanonicalStringInstance(w_thread thread, w_string s) {
  w_string canonical;
  w_flags *flagsptr;

  threadMustBeSafe(thread);
  canonical = s->interned;
  if (canonical) {
    enterUnsafeRegion(thread);
    addLocalReference(thread, canonical);
    flagsptr = instance2flagsptr(canonical);
#ifdef PIGS_MIGHT_FLY
    unsetFlag(*flagsptr, O_GARBAGE);
#endif
    setFlag(*flagsptr, O_BLACK);
    enterSafeRegion(thread);
  }

  return canonical;
}

/*
** If the w_string referenced by theString has no canonical instance, record
** 'theString' as the cononical instance and return it as the result. If the
** w_string already has a canonical instance, return the canonical instance.
** The caller of this function must own the lock on string_hashtable(!).
*/
w_instance internString(w_thread, w_instance theString);

/*
** If theString is the canonical entry for the w_string it references, remove
** the reference to it as canonical instance. The caller of this function must
** own the lock on string_hashtable(!).
*/
void uninternString(w_thread, w_instance theString);

/*
** Get an instance of java.lang.String which points to s.
*/
#ifdef DEBUG_STRINGS
w_instance _newStringInstance(w_string s, const char *file, int line);
#define newStringInstance(s) _newStringInstance((s), __FILE__, __LINE__)
#else
w_instance newStringInstance(w_string s);
#endif

/*
** Get the canonical instance of s, or create one ("intern" s) if needed.
*/
w_instance getStringInstance(w_string s);

/*
** stringAdd will copy over the 'source' string to the 'destination' buffer.
** The caller is again expected to give a suitably sized buffer. It returns the
** number of characters added to the buffer so that the calls can again be daisy chained,
** as in this example:
**
** w_char *buffer = allocMem(...);
** w_int length = 0;
** ...
** length += stringAdd(buffer + length, string1);
** length += stringAdd(buffer + length, string2);
** ...
*/

w_int stringAdd(w_char *destination, w_string source);

/*
** dots2slashes takes a Wonka string and returns a registered Wonka
** string in which the dots are replaced with slashes If the string already
** contains slashes then these are replaced by DEL characters(!). Why? 
** Because the only use for this function is to convert fully-qualified
** class names from Java to "internal" format, and in Java format a slash
** should never occur.
** slashes2dots does the exact opposite.
*/

w_string dots2slashes(w_string string);
w_string slashes2dots(w_string string);

/*
** startStrings is called from startWonka: it creates the hashtable
** used by the string registration process, and populates it with all
** those commonly-used strings we were just talking about.
*/

void startStrings(void);

// The empty string
extern w_string string_empty;

// Special strings `<NULL>', `<BOGUS>', `<EMPTY>'
extern w_string string_NULL;
extern w_string string_BOGUS;
extern w_string string_EMPTY;

// Names of classfile attributes
extern w_string string_Code;
extern w_string string_ConstantValue;
extern w_string string_Deprecated;
extern w_string string_Exceptions;
extern w_string string_InnerClasses;
extern w_string string_LineNumberTable;
extern w_string string_LocalVariableTable;
extern w_string string_Reference;
extern w_string string_SourceFile;
extern w_string string_Synthetic;
#ifdef SUPPORT_BYTECODE_SCRAMBLING
extern w_string string_be_kiffer_Scrambled;
#endif

// Descriptors, names of primitive types, etc.
extern w_string string_angle_brackets_init;
extern w_string string_angle_brackets_clinit;
extern w_string string_no_params;
extern w_string string_no_params_V;
extern w_string string_boolean;
extern w_string string_byte;
extern w_string string_c_h_a_r;
extern w_string string_double;
extern w_string string_finalize;
extern w_string string_run;
extern w_string string_float;
extern w_string string_int;
extern w_string string_long;
extern w_string string_short;
extern w_string string_void;
extern w_string string_L_java_lang_String;

// Strings used by unicode.c
/* Missing if UNICODE_SUBSETS = 0!
 * extern w_string string_medial;
extern w_string string_square;
extern w_string string_narrow;
extern w_string string_small;
extern w_string string_isolated;
extern w_string string_initial;
extern w_string string_font;
extern w_string string_final;
extern w_string string_circle;
extern w_string string_vertical;
extern w_string string_wide;
*/
extern w_string string_compat;
extern w_string string_super;
extern w_string string_fraction;
extern w_string string_noBreak;
extern w_string string_sub;

// Defined in unicode.c
extern w_string category_name[];

#define w_string_equals(a, b)                  ((a) == (b))

void dumpStrings(void);

static inline w_string String2string(w_instance String) {
  return getWotsitField(String, F_String_wotsit);
}

static inline void attachString(w_instance strinstance, w_string string) {
  w_string old = String2string(strinstance);
  w_string nieuw;
  if (old) {
    deregisterString(old);
  }
  nieuw = registerString(string);
  setWotsitField(strinstance, F_String_wotsit, nieuw);
}

char * print_string(char * buffer, int * remain, void * s, w_int width, w_int prec, w_flags flags);

/*
** Mutex required for mutual exclusion to the string pool.
*/
x_mutex string_mutex;

#endif /* _WSTRINGS_H */

