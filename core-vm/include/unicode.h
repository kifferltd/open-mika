#ifndef _UNICODE_H
#define _UNICODE_H

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
** $Id: unicode.h,v 1.3 2006/03/29 20:10:40 cvs Exp $
*/

#include "wonka.h"

/*
** The file unicode.c is generated from UnicodeData.txt by means of a [g]awk script.
**
** Each array 'const unsigned char character_class_row_<xy>[]'
** holds 256 bytes, each of which holds information concerning
** one unicode character.  The high nibble contains the character
** 'class':
**   0   Character not defined
**   1   Character is lower-case letter
**   2   Character is upper-case letter
**   3   Character is title-case letter
**   4   Character is some other kind of "letter"
**   5   Character is a digit
**   6   Character is whitespace
**   7   Character is a formatting character
**   8   Character is none of the above (but is defined)
**   >8  reserved, will not be output by this program.
** For classes 1 and 2 the low nibble contains information
** to enable upper case to be derived from lower or vice versa:
**   0   There is no corresponding other-case letter
**   i   (0<i<F) Use upper_to_lower_delta[i] : add it to the
**       upper-case to get the lower, or subtract it from the
**       lower-case to get the upper.
**   F   Use to_lower_case[][] or to_upper_case[][] (see below).
**
**  'const int upper_to_lower_delta []'
**    contains the "deltas" which cover most upper-to-lower case
**    conversions (and vice versa).  upper_to_lower_delta[0]
**    holds the number 'n' of deltas, upper_to_lower_delta[1..n] 
**    holds the deltas themselves.  
*/

extern const int upper_to_lower_delta[];

/*
**  'const unsigned char *character_class[]' holds 256 pointers,
**  each pointing to 'character_class_row_<xy>[]' for some <xy>.
**  character_class[charcode/256U][charcode%256U] yields the
**  information byte corresponding to unicode character 'charcode'.
*/

extern const w_ubyte * character_class[];

#define CHAR_TABLE_ENTRY(ch) (character_class[(ch)/256U][(ch)%256U])
#define CHAR_CLASS(ch)       (CHAR_TABLE_ENTRY(ch)/16)
#define CHAR_SUBCLASS(ch)    (CHAR_TABLE_ENTRY(ch)%16)

/*
** A w_char_conversion maps one w_char (`from') to one w_char (`to').
*/
typedef struct {
  w_char from;
  w_char to;
} w_char_conversion;

/*
** A w_char_to_number maps a w_char (`from') to a w_int (`to').
*/
typedef struct {
  w_char from;
  w_int  to;
} w_char_to_number;

/*
** A w_char_to_string maps a w_char (`from') to a w_string (`to').
*/
typedef struct {
  w_char   from;
  w_string to;
} w_char_to_string;

/*  The two arrays 'const unsigned char to_lower_case[][]' and
**  'const unsigned char to_upper_case[][]' each hold a number
**  of pairs of characters, being respectively {upper,lower}
**  and {lower,upper}.  The pairs are stored in ascending
**  order, and are terminated by {0,0}.
*/
extern const w_char_conversion to_lower_case[];
extern const w_char_conversion to_upper_case[];

/*
**  The array 'const unsigned char to_title_case[][]' holds a number
**  of pairs of characters, each being {upper or lower case,title case}.
**  The pairs are stored in ascending order, and are terminated by {0,0}.
*/

extern const w_char_conversion to_title_case[];

/*
** A w_char_decomposition maps one w_char (`from') to a sequence of w_char's (`to').
** The `to' sequence consists of a length (encoded as a w_short), followed by
** a number of w_char's.  The conversion to a w_string should be carried out
** at runtime, on demand, in order not to stuff the string pool full of junk.
*/
typedef struct {
  w_char from;
  w_char *to;
} w_char_decomposition;

/*
** Create the tables used by unicode.c for character decomposition
** (call this from the static initialiser of RuleBasedCollator).
*/
void createDecompositionTables(void);

/*
** Create the tables used by unicode.c (call this from java.lang.Character).
*/
void createCharacterTables(void);

/*
** Convert a w_char to its digit value.
*/
w_int   charToDigitValue(w_char);

/*
** Convert a w_char to its numerical value.
** Note: the terms `digit value' and `numeric value' are defined by
** unicode.org, according to their own unique logic ...
*/
w_int   charToNumericValue(w_char);

/*
** Convert a char to a two-character string representing its general 
** category code, as shown in UnicodeData.txt.
*/
w_string charToCategory(w_char);

/*
** Convert a w_char to a counted UTF16 string which represents its
** (canonical or compatibility) decomposition.  The first w_char
** pointed to by the result is a character count, followed immediately
** by the characters of the decomposition.
*/
w_char *charToDecomposition(w_char);

/*
** Convert a w_char to a its combining class.
*/
w_int charToCombiningClass(w_char);

/*
** Convert a w_char to the integer identifier of the UnicodeSubset to which it belongs.
*/
w_int charToUnicodeSubset(w_char);

/*
** Convert a w_char to the value of the corresponding DIRECTIONALITY_XXXXXX 
** constant in java.lang.Character.
*/
w_int charToDirectionality(w_char);

/*
** Return TRUE if the w_char is defined and is a mirrored char, else FALSE. 
** See chapter 4.7 _Bidi Mirrored - Normative_ in the Unicode spec.
*/
w_boolean charIsMirrored(w_char);

/*
** Convert a w_char to a its compatibility class, in the form of a w_string
** such as ``noBreak''.
*/
w_string charToCompatibilityType(w_char);

#endif /* _UNICODE_H */
