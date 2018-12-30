/* $Id: Font.h,v 1.1 2005/06/14 08:48:24 cvs Exp $ */

#ifndef _FONT_H
#define _FONT_H

/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

#include "rudolph.h"
#include "jni.h"

#include "font.h"
#include "fntfilio.h"
#include "pcf.h"
#include "fileio.h"
#include "fontmisc.h"
#include "fontstruct.h"
#include "pcfread.h"
#include "atom.h"
#include "defaults.h"
#include "bitmap.h"

#include "pcfprint.h"

/*
** Font structure: 
*/

typedef struct r_Font {
  char* name;                     /* font name */
  char* family;                   /* font family ("Times") */
  w_int style;                    /* 0, 1, 2 or 3: plain, bold, italic, bold-italic */
  w_int size;                     /* value identifying a font together with its family and style */
  w_int ascent;                   /* height of a font in pixels above the baseline*/
  w_int descent;                  /* height of a font in pixels below the baseline*/
  w_int leading;
  w_int maxAscent;
  w_int maxDescent;
  w_int maxAdvance;               /* maximum actual width of all characters in a font */
  FontPtr       pFont;
  char*         fileName;
//  FontFilePtr   file;           /* for later use */
//  w_word        filePosition;   /* for later use */
  CharInfoPtr   pDefault;         /* only used if the font has no default glyph or the default is the space character */
  w_boolean     loaded;           /* true if font has been in use, false if initialised only; 23/09/2002 */
} r_Font;

w_void Font_create(JNIEnv *env, jobject thisFont, jobject name, jint style, jint size);
r_font Font_getFont(char *name, w_int style, w_int size);
w_boolean Font_loadFont(r_font font);
//w_void Font_finalize(JNIEnv *env, jobject thisFont);
w_int Font_getCStringWidth(r_font font, char* text, w_int len);
w_int Font_getCharWidth(r_font font, w_int charno);
w_int Font_getStringWidth(r_font font, w_string string);
w_int Font_getHeight(r_font);
w_void Font_drawStringAligned(r_buffer buffer, r_font font, w_int x, w_int y, w_int w, w_int h, w_string text, w_int color, w_word alignment);
w_void Font_drawStringUnAligned(r_buffer buffer, r_font font, w_int charx, w_int chary, w_int x, w_int y, w_int w, w_int h, w_string string, w_int color);
w_void Font_drawGlyphString(r_buffer buffer, FontPtr pFont, w_int charx, w_int chary, w_int x, w_int y, w_int w, w_int h, CharInfoPtr* glyphs, w_word glyphCount, w_int color);

#ifdef OLD_STUFF
/*
** Character structure:
*/

typedef struct r_Char {
  w_char value;      /* translated name ("A", "B", "\033agrave;") */
  char *data;            /* character definition */
  int width;                    /* character width */
  int height;            /* height (size of lines array) */
  int advance;                   /* width with padding on left and right */
  int xoffset;      /* # pixels to move to the right before drawing */
  int yoffset;      /* # pixels to move up (>0) or down (<0) before drawing */
} r_Char;


/*
** Font structure:
*/

typedef struct r_Font {
  char *name;      /* font name */
  char *family;      /* font family ("Times") */
  char *face_name;    /* face name ("Times Roman") */
  char *width_name;    /* face name ("Normal") */
  int proportional;    /* yes or no? (fixed/proportional font) */
  int style;                    /* 0, 1, 2 or 3: plain, bold, italic, bold-italic */
  int size;                     /* value identifying a font together with its family and style */
  int ascent;                   /* height of a font in pixels above the baseline*/
  int descent;                  /* height of a font in pixels below the baseline*/
  int leading;
  int maxAscent;
  int maxDescent;
  int maxAdvance;               /* maximum actual width of all characters in a font */
  r_Char *chars[512];    /* list of chars */
} r_Font;

w_void Font_init(JNIEnv *env, jclass clazz);
w_void Font_initialize(const char *name, const char *file);
w_void Font_readFile(char *name, char *path);
w_void Font_readData(char *name, char **lines);
r_font Font_getFont(char *name, int style, int size);
w_void Font_getSize(r_font font, unsigned int *height_return);
w_void Font_getDimensions(r_font font, w_string text, int len, int *width_return, int *height_return);
int Font_getCStringWidth(r_font font, char* text, int len);
int Font_getStringWidth(r_font font, w_string text);
int Font_getHeight(r_font);
w_void Font_getStringHeight(r_font font, w_string text, int* ascent, int* descent);
w_void Font_drawStringAligned(r_buffer buffer, r_font font, int x, int y, int w, int h, w_string text, int col, unsigned int alignment);
w_void Font_drawStringUnAligned(r_buffer buffer, r_font font, int charx, int chary, int x, int y, int w, int h, w_string text, int col);
w_void Font_drawString(r_buffer buffer, r_font font, int charx, int chary, int x, int y, int w, int h, w_string string, int c);

inline static const r_char Font_getChar(r_font font, int ch) {
  return font->chars[(ch > 0x2fff && ch < 0x3100) ? ch-0x2f00 : ch%512];
}
#endif /* OLD_STUFF */


#endif
