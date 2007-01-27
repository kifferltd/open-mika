/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
* Modifications copyright (C) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to distribute these             *
* modifications under the terms of the Wonka Public Licence.              *
*                                                                         *
**************************************************************************/

/*
** $Id: Character.c,v 1.6 2006/10/04 14:24:16 cvsroot Exp $
*/

#include <string.h>
#include "chars.h"
#include "checks.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "fastcall.h"
#include "hashtable.h"
#include "heap.h"
#include "methods.h"
#include "wstrings.h"
#include "threads.h"
#include "unicode.h"

/*
** Call this from your static initialiser.
*/
void
Character_static_createTables
( JNIEnv *env, w_instance ClassCharacter
) {
  createCharacterTables();
}

/*
** Get the general category code of a character, as a 2-character String.
*/
w_instance
Character_static_getCategory
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return newStringInstance(charToCategory(ch));
}

/*
** Returns true iff the character code is assigned in Unicode.
*/
w_boolean
Character_static_isDefined
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsDefined(ch);
}

/*
** Returns true iff ch is a digit according to Java.
*/
w_boolean Character_static_isDigit
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsDigit(ch);
}

void fast_Character_isDigit_char(w_frame frame) {
  frame->jstack_top[-1].c = charIsDigit(frame->jstack_top[-1].c);
}

#define MIN_RADIX 2
#define MAX_RADIX 36

/*
** Returns the w_char which represents 'digit' in base 'radix' (or 0 if none)
*/
static w_char forDigit (w_int digit, w_int radix) {
  if (radix < MIN_RADIX || radix > MAX_RADIX) {

    return 0;

  }

  if (digit < 0 || digit >= radix) {

    return 0;

  }

  if (digit < 10) {

    return (w_char)(digit + '0');

  }

  return (w_char)(digit + 'a' - 10);
}

w_char Character_static_forDigit
( JNIEnv *env, w_instance ClassCharacter, w_int digit, w_int radix
) {
  return forDigit(digit, radix);
}

void fast_Character_forDigit_int_int(w_frame frame) {
  frame->jstack_top[-2].c = forDigit(frame->jstack_top[-2].c, frame->jstack_top[-1].c);
  frame->jstack_top -= 1;
}

/*
** Returns the value of 'ch' in base 'radix' (or -1 if none)
*/
static w_int i_digit(w_char ch, w_int radix) {
  w_char *deco;

  if(radix < MIN_RADIX || radix > MAX_RADIX) {

    return -1;
 
  }

  if(charIsDigit(ch)) {
    w_int dv = char2digit(ch);
    // Or maybe:
    // w_int dv = charToDigitValue(ch);

    return dv < radix ? dv : -1 ;

  }

  if(ch >= 'A' && ch <= 'Z' && ch < 'A' + radix - 10) {

    return ch - 'A' + 10;

  }

  if(ch >= 'a' && ch <= 'z' && ch < 'a' + radix - 10) {

    return ch - 'a'+ 10;
  } 

  deco = charToDecomposition(ch);
  if (deco && *deco == 1) {
    return i_digit(deco[1], radix);
  }

  return -1;

}

w_int Character_static_digit
( JNIEnv *env, w_instance ClassCharacter, w_char ch, w_int radix
) {
  return i_digit(ch, radix);
}

void fast_Character_digit_char_int(w_frame frame) {
  frame->jstack_top[-2].c = i_digit(frame->jstack_top[-2].c, frame->jstack_top[-1].c);
  frame->jstack_top -= 1;
}

/*
** Returns true iff ch is lower-case according to Java.
*/
w_boolean
Character_static_isLowerCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsLower(ch);
}

/*
** Returns true iff ch is upper-case according to Java.
*/
w_boolean
Character_static_isUpperCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsUpper(ch);
}

/*
** Returns true iff ch is title-case according to Java.
*/
w_boolean
Character_static_isTitleCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsTitle(ch);
}

/*
** Returns true iff ch is a letter according to Java.
*/
w_boolean
Character_static_isLetter
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsLetter(ch);
}

/*
** Returns true iff ch is a formatting character according to Java.
*/
w_boolean
Character_static_isFormat
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsFormat(ch);
}

/*
** Returns true iff ch is a whitespace character according to Java.
*/
w_boolean
Character_static_isWhitespace
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsWhitespace(ch);
}

/*
** Convert a character to its lower-case equivalent.
** If ch has no lower-case equivalent then ch is returned unchanged.
** Note that a character can have a lower-case equivalent even though
** both isUpperCase() and isTitleCase() return false. <shrug>
*/
w_char
Character_static_toLowerCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return char2lower(ch);
}

/*
** Convert a character to its upper-case equivalent.
** If ch has no upper-case equivalent then ch is returned unchanged.
** Note that a character can have an upper-case equivalent even though
** isLowerCase() returns false. <shrug>
*/
w_char
Character_static_toUpperCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return char2upper(ch);
}

/*
** Convert a character to its title-case equivalent.
** If ch has no title-case equivalent then ch is returned unchanged.
** Note that a character can have a lower-case equivalent even though
** isLowerCase() returns false. <shrug>
*/
w_char
Character_static_toTitleCase
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return char2title(ch);
}

/*
** Return the numeric value associated with a character, or -1 if it has none,
** or -2 if the numeric value is not a nonnegative integer (e.g. it is
** negative or fractional).
*/
w_int
Character_static_numericValue
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charToNumericValue(ch);
}

/*
** Return the (most specific) unicode block to which ch belongs.
** Returns -1 if ch is undefined or the block to which it belongs is not
** supported.
*/
w_int
Character_static_toUnicodeBlock
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charToUnicodeSubset(ch);
}

/*
** Return the directionality of ch.
** Returns -1 if ch is undefined its directionality is unknown.
*/
w_sbyte
Character_static_getDirectionality
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  // [CG 20070126] Special-case these because what the mauve tests expect
  // makes more sense than what is in the Unicode database
  switch(ch) {
  case 0x0000:
    return 13; // DIRECTIONALITY_OTHER_NEUTRALS

  case 0x000c:
    return 10; // DIRECTIONALITY_PARAGRAPH_SEPARATOR

  case 0x00a0:
    return 12; // DIRECTIONALITY_WHITESPACE

  case 0x2007:
    return 7;  // DIRECTIONALITY_COMMON_NUMBER_SEPARATOR

  default:
    return charToDirectionality(ch);
  }
}


/*
** Returns TRUE if ch is a mirrored char, FALSE if not (including unknown).
*/
w_boolean
Character_static_isMirrored
( JNIEnv *env, w_instance ClassCharacter, w_char ch
) {
  return charIsMirrored(ch);
}


w_instance
Character_getWrappedClass(JNIEnv *env, w_instance thisClass) {

  return clazz2Class(clazz_char);
  
}
