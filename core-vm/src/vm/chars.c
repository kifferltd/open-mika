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
** $Id: chars.c,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

#include "chars.h"
#include "wstrings.h"
#include "ts-mem.h"
#include "unicode.h"

/*
** Routines for dealing with Unicode
*/

w_int char2class(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch);

}

w_int char2digit(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_SUBCLASS(ch);

}

w_boolean charIsDefined(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) != NONESUCH;

}

w_boolean charIsLower(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == LOWERCASE;

}

w_boolean charIsUpper(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == UPPERCASE;

}

w_boolean charIsTitle(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == TITLECASE;

}

w_boolean charIsLetter(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) > NONESUCH && CHAR_CLASS(ch) < DIGIT;

}

w_boolean charIsDigit(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == DIGIT;

}

w_boolean charIsFormat(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == FORMAT;

}

w_boolean charIsWhitespace(int ch) {
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, CHAR_TABLE_ENTRY(ch));

  return CHAR_CLASS(ch) == WHITESPACE;

}

w_boolean charIsIdentifierIgnorable(int ch) {

  return (ch >= 0x0000 && ch <= 0x0009)
      || (ch >= 0x000e && ch <= 0x001b)
      || (ch >= 0x007f && ch <= 0x009f)
      || (ch >= 0x200c && ch <= 0x200f)
      || (ch >= 0x206a && ch <= 0x206f)
      || (ch == 0xfeff);

}

w_boolean charIsJavaIdentifierStart(int ch) {
  w_string category;

  if (charIsLetter(ch)) {

    return WONKA_TRUE;

  }

  category = charToCategory(ch);
  woempa(1, "Char %04x category is %w\n", ch, category);
  
  return (string_char(category, 0) == 'S' || string_char(category, 0) == 'P')
       && string_char(category, 1) == 'c';
}


w_boolean charIsJavaIdentifierPart(int ch) {
  w_string category;

  if (charIsJavaIdentifierStart(ch) || charIsDigit(ch)) {

    return WONKA_TRUE;

  }

  category = charToCategory(ch);
  
  if (string_char(category, 0) == 'N' && string_char(category, 1) == 'l') {
    woempa(1, "Char %04x is a numeric letter (%w)\n", ch, category);

    return WONKA_TRUE;

  }

  if (string_char(category, 0) == 'M' && (string_char(category, 1) == 'c' || string_char(category, 1) == 'n')) {
    woempa(1, "Char %04x is a combining/non-spacing (%w)\n", ch, category);

    return WONKA_TRUE;

  }

  if (charIsIdentifierIgnorable(ch)) {

    return WONKA_TRUE;

  }

  return WONKA_FALSE;
}

/*
** Look up a conversion in one of the tables of special cases
*/

static w_char lookupconversion(int ch, const w_char_conversion tab[]) {

  const w_char_conversion *chcon = tab;

  while (chcon->from != ch) {

    if (chcon->from == 0) {
      woempa(1, "Found no mapping for %04x , returning %04x\n", ch, ch);

      return ch;
    }
    chcon++;
  }

  woempa(1, "Found mapping %04x -> %04x\n", ch, chcon->to);

  return chcon->to;

}
 
/*
** char2lower returns the lower-case equivalent of ch if there is one, 
** or ch (unchanged) if there is not.
*/

w_char char2lower(int ch) {

  w_int cc = CHAR_TABLE_ENTRY(ch);
  w_int hi = cc / 16;
  w_int lo = cc % 16;

  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, cc);
  switch (hi) {
    case UPPERCASE:
    case TITLECASE:
    case OTHER:
      switch(lo) {
        case  0: return ch;
        case 15: return lookupconversion(ch, to_lower_case);
        default: return ch + upper_to_lower_delta[lo];
      }
    default: return ch;
  }

}

/*
** char2upper returns the upper-case equivalent of ch if there is one, 
** or ch (unchanged) if there is not.
*/

w_char char2upper(int ch) {

  w_int cc = CHAR_TABLE_ENTRY(ch);
  w_int hi = cc / 16;
  w_int lo = cc % 16;
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, cc);

  switch(hi) {
    case LOWERCASE:
    case TITLECASE:
    case OTHER: {
      switch (lo) {
        case  0: return ch;
        case 15: return lookupconversion(ch, to_upper_case);
        default: return ch - upper_to_lower_delta[lo];
      }
    }

    default: return ch;
  }

}

/*
** char2title returns the title-case equivalent of ch if there is one, 
** or else the upper-case equivalent, or ch (unchanged) if neither exists.
*/

w_char char2title(int ch) {

  w_int cc = CHAR_TABLE_ENTRY(ch);
  w_int hi = cc / 16;
  w_int lo = cc % 16;
  woempa(1, "CHAR_TABLE_ENTRY(%04x) == %02x\n", ch, cc);

  switch (hi) {
    case LOWERCASE:
    case UPPERCASE:
    case OTHER: {
        w_char t = lookupconversion(ch, to_title_case);
        woempa(1, "to_title_case -> %04x\n", t);

        if (t != ch) {
          return t;
        }

        switch (lo) {
          case  0: return ch;
          case 15: return lookupconversion(ch, to_upper_case);
          default: return ch - upper_to_lower_delta[lo];
        }
    }

    default: return ch;
  }
}
