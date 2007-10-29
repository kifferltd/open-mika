/**************************************************************************
* Copyright (c) 2002, 2003 by Punch Telematix. All rights reserved.       *
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

#include "fields.h"
#include "wstrings.h"

#include "rudolph.h"
#include "awt-classes.h"

#include "Font.h"

/*
** resolve the ascent of a font, in pixels
*/
jint FontMetrics_getAscent(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getAscent(font = %s)\n", font->name);

  // Return value (if any):
  return (font ? font->ascent : 0);
}

/*
** resolve the descent of a font, in pixels
*/
jint FontMetrics_getDescent(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getDescent(font = %s)\n", font->name);

  // Return value (if any):
  return (font ? font->descent : 0);
}

/*
** resolve the leading of a font, in pixels
*/
jint FontMetrics_getLeading(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getLeading(font = %s)\n", font->name);

  // Return value (if any):
  return (font ? font->leading : 0);
}

/*
** resolve the height of a font, in pixels
*/
jint FontMetrics_getHeight(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getHeight(font = %s)\n", font->name);

  // Return value (if any):
  return Font_getHeight(font);
}

/*
** resolve the maximum ascent of the given font, in pixels
*/
jint FontMetrics_getMaxAscent(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getMaxAscent(font = %s) \n", font->name);

  /* Return value (if any): */
  return (font ? font->maxAscent : 0);
}

/*
** resolve the maximum descent of the given font, in pixels
*/
jint FontMetrics_getMaxDescent(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getMaxDescent(font = %s)\n", font->name);

  // Return value (if any):
  return (font ? font->maxDescent : 0);
}

/*
** resolve the maximum advance of the given font, in pixels
*/
jint FontMetrics_getMaxAdvance(JNIEnv *env, jobject thisFontMetrics) {
  // Resolve font:
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);

  // Debug output:
  // woempa(9, "called FontMetrics_getMaxAdvance( font = %s)\n", font->name);

  // Return value (if any):
  return (font ? font->maxAdvance : 0);
}

/*
** resolve width in pixels of the given string
*/
jint FontMetrics_stringWidth(JNIEnv *env, jobject thisFontMetrics, jobject textInstance) {
  if (textInstance) {
    // Resolve font
    w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
    r_font font = getWotsitField(fontInstance, F_Font_wotsit);
    // Resolve string
    w_string string = String2string(textInstance);
    // Resolve text width:
    w_int width = Font_getStringWidth(font, string);

    // Debug output:
    // woempa(9, "called FontMetrics_stringWidth(font = %s, string = '%s') \n", font->name, text);

    return width;
  }
  else {
    return 0;
  }
}


/*
** resolve width in pixels of the given character
*/
jint FontMetrics_charWidth(JNIEnv *env, jobject thisFontMetrics, jint charno) {
  // Resolve font
  w_instance fontInstance = getReferenceField(thisFontMetrics, F_FontMetrics_font);
  r_font font = getWotsitField(fontInstance, F_Font_wotsit);
  // get width
  w_int width = Font_getCharWidth(font, (w_int)charno);

  // Debug output:
  // woempa(9, "called FontMetrics_charWidth(font = %s, char = '%c') \n", font->name, charno);

  return width;
}
