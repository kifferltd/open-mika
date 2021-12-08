/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

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
#include "mika_threads.h"
#include "unicode.h"

/*
** Call this from your static initialiser.
*/
void
Character_static_createTables
( w_thread thread, w_instance ClassCharacter
) {
  createCharacterTables();
}

/*
** Get the general category code of a character, as a 2-character String.
*/
w_instance
Character_static_getCategory
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return getStringInstance(charToCategory(ch));
}

/*
** Returns true iff the character code is assigned in Unicode.
*/
w_boolean
Character_static_isDefined
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsDefined(ch);
}

/*
** Returns true iff ch is a digit according to Java.
*/
w_boolean Character_static_isDigit
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsDigit(ch);
}

void fast_Character_isLetter(w_frame frame) {
  frame->jstack_top[-1].c = charIsLetter(frame->jstack_top[-1].c);
}

void fast_Character_isWhitespace(w_frame frame) {
  frame->jstack_top[-1].c = charIsWhitespace(frame->jstack_top[-1].c);
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
( w_thread thread, w_instance ClassCharacter, w_int digit, w_int radix
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

    if (dv < radix) {
      return dv;
    }

    /* [CG 20080828] Moved up from down below */
    deco = charToDecomposition(ch);
    if (deco && *deco == 1) {
      return i_digit(deco[1], radix);
    }

    return -1 ;

  }

  if(ch >= 'A' && ch <= 'Z' && ch < 'A' + radix - 10) {

    return ch - 'A' + 10;

  }

  if(ch >= 'a' && ch <= 'z' && ch < 'a' + radix - 10) {

    return ch - 'a'+ 10;
  } 

  /* [CG 20080828] Only applicable if isDigit(ch) is true
  deco = charToDecomposition(ch);
  if (deco && *deco == 1) {
    return i_digit(deco[1], radix);
  }
  */

  return -1;

}

w_int Character_static_digit
( w_thread thread, w_instance ClassCharacter, w_char ch, w_int radix
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
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsLower(ch);
}

/*
** Returns true iff ch is upper-case according to Java.
*/
w_boolean
Character_static_isUpperCase
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsUpper(ch);
}

/*
** Returns true iff ch is title-case according to Java.
*/
w_boolean
Character_static_isTitleCase
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsTitle(ch);
}

/*
** Returns true iff ch is a letter according to Java.
*/
w_boolean
Character_static_isLetter
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsLetter(ch);
}

/*
** Returns true iff ch is a formatting character according to Java.
*/
w_boolean
Character_static_isFormat
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsFormat(ch);
}

/*
** Returns true iff ch is a whitespace character according to Java.
*/
w_boolean
Character_static_isWhitespace
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charToUnicodeSubset(ch);
}

/*
** Return the directionality of ch.
** Returns -1 if ch is undefined its directionality is unknown.
*/
w_sbyte
Character_static_getDirectionality
( w_thread thread, w_instance ClassCharacter, w_char ch
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
( w_thread thread, w_instance ClassCharacter, w_char ch
) {
  return charIsMirrored(ch);
}


w_instance
Character_getWrappedClass(w_thread thread, w_instance thisClass) {

  return clazz2Class(clazz_char);
  
}
