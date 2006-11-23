/**************************************************************************
* Copyright (c) 2002, 2003 by Acunia N.V. All rights reserved.            *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
* distribution or disclosure of this software is expressly forbidden.     *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5 box 3        info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/* 
** $Id: FontMetrics.c,v 1.1 2005/06/14 16:00:18 cvs Exp $ 
*/

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
