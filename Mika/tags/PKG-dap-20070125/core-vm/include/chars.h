#ifndef _CHARS_H
#define _CHARS_H

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

#include "wonka.h"

/*
** $Id: chars.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

/*
** Routines for dealing with Unicode etc.
*/

#define MIN_RADIX 2
#define MAX_RADIX 36

/*
** upper nibble of character class
*/

#define NONESUCH   0
#define LOWERCASE  1
#define UPPERCASE  2
#define TITLECASE  3
#define OTHERLETTER 4
#define DIGIT      5
#define WHITESPACE 6
#define FORMAT    7
#define OTHER     8

/*
** char2lower, char2upper, and char2title return the lowercase, uppercase,
** and titlecase equivalent of their argument, respectively.  (Note that if
** there is no special titlecase for a character then titlecase is the same
** as uppercase).  If the character has no equivalent in the requested case 
** then the argument is returned unchanged as the result.
**
** N.B. parameter 'ch' is logically a w_char, but is declared as int
** to keep gcc happy.  (On some platforms the w_char is widened to int
** before being put on the stack, and gcc -Wconversion warns about this).
*
*/

w_char char2lower(int ch);

w_char char2upper(int ch);

w_char char2title(int ch);

/*
** charIsDefined returns WONKA_TRUE iff ch is defined in the Unicode table.
*/

w_boolean charIsDefined(int ch);

/*
** charIsDigit returns WONKA_TRUE iff ch is specified in the Unicode table
** to be a digit.  char2digit returns the numerical value (0..9) of ch if
** ch is a digit, else the result is undefined.
*/

w_boolean charIsDigit(int ch);

w_int char2digit(int ch);

/*
** charIsFormat returns WONKA_TRUE iff ch is specified in the Unicode table
** to be a formatting character.
*/

w_boolean charIsFormat(int ch);

/*
** charIsLower returns WONKA_TRUE iff ch is specified in the Unicode table
** to be a lower-case letter.
*/

w_boolean charIsLower(int ch);

/*
** charIsUpper returns WONKA_TRUE iff ch is specified in the Unicode table
** to be an upper-case letter.
*/

w_boolean charIsUpper(int ch);

/*
** charIsTitle returns WONKA_TRUE iff ch is specified in the Unicode table
** to be a title-case letter.
*/

w_boolean charIsTitle(int ch);

/*
** charIsLetter returns WONKA_TRUE iff ch is specified in the Unicode table
** to be any kind of letter.
*/

w_boolean charIsLetter(int ch);

/*
** charIsWhitespace returns WONKA_TRUE iff ch is specified in the Unicode table
** to be whitespace.
*/

w_boolean charIsWhitespace(int ch);

/*
** charIsIdentifierIgnorable returns WONKA_TRUE iff ch should be ignored in a
** Java identifier.
*/
w_boolean charIsIdentifierIgnorable(int ch);

/*
** charIsJavaIdentifierStart returns WONKA_TRUE iff ch is allowed as the first 
** character of a Java identifier.
*/
w_boolean charIsJavaIdentifierStart(int ch);

/*
** charIsJavaIdentifierPart returns WONKA_TRUE iff ch is allowed as a 
** subsequent character of a Java identifier.
*/
w_boolean charIsJavaIdentifierPart(int ch);


#endif
