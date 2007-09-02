#ifndef _CHARS_H
#define _CHARS_H

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
