/**************************************************************************
* Parts copyright (c) 2002, 2003 by Punch Telematix.                      *
* All rights reserved.                                                    *
* Parts copyright (c) 2005, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
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

#include <string.h>

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "wstrings.h"

#include "awt-classes.h"
#include "canvas.h"
#include "varia.h"

#include "Font.h"

#define MAX_FONTS 37

extern char *fsroot;

w_instance defaultFont;

static r_font *fonts = NULL;
static w_int num_fonts = 0;
static char *fonts_path;

w_void Font_initialize(w_thread thread, jclass thisClass, jstring name, jstring file) {
  w_int         i;
  FontFilePtr	  input = 0;
  FontPropsRec  fontProps;
  char          *atomName = 0;
  r_font        font = 0;
  w_boolean     initFailed = WONKA_FALSE;
  const char    *fontName;
  const char    *fileName;
  jboolean      isCopy1;
  jboolean      isCopy2;

  if (!fonts_path) {
    i = strlen(FONTDIR);
    if (strncmp(FONTDIR, "{}/", 3) ==0) {
      fonts_path = allocMem(strlen(FONTDIR) + strlen(fsroot));
      strcpy(fonts_path, fsroot);
      strcpy(fonts_path + strlen(fsroot), FONTDIR + 2);
      fonts_path[strlen(fsroot) + strlen(FONTDIR) - 2] = '/';
      fonts_path[strlen(fsroot) + strlen(FONTDIR) - 1] = 0;
    }
    else {
      fonts_path = allocMem(strlen(FONTDIR) + 2);
      strcpy(fonts_path, FONTDIR);
      fonts_path[strlen(FONTDIR)] = '/';
      fonts_path[strlen(FONTDIR) + 1] = 0;
    }
  }

  if (num_fonts >= MAX_FONTS) {
    woempa(9, "WARNING, MAX_FONTS exceeded, cannot load font \"%s\" \n", fileName);
    return;
  }

  fontName = (*env)->GetStringUTFChars(env, name, &isCopy1);
  fileName = (*env)->GetStringUTFChars(env, file, &isCopy2);
  
  /*
  ** Allocate memory for static array 'fonts':
  */

  if (num_fonts == 0) {
    fonts = (r_font *)allocClearedMem(MAX_FONTS * sizeof(r_font));
    if (!fonts) {
      if(isCopy1 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, name, fontName);
      if(isCopy2 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, file, fileName);
      return;   /* mem alloc failure will be handled by interpreter */
    }
  }
  else {

    /*
    ** Check that font is not already in fonts table:
    */
    i=0;
    while (i < num_fonts && strcmp(fonts[i]->name, fontName) != 0) {
      i++;
    }
    if (i < num_fonts) {
      woempa(9, "attempt to initialize font \"%s\" a second time refused\n", fontName);
      if(isCopy1 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, name, fontName);
      if(isCopy2 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, file, fileName);
      return;
    }
  }

  fontProps.isStringProp = NULL;
  fontProps.props = NULL;

  /*
  ** Allocate cleared memory for the new font: the memory has to be
  ** cleared because its content will be used in subsequent checks.
  */
  font = (r_font)allocClearedMem(sizeof(r_Font));
  if (!font) {
    initFailed = WONKA_TRUE;
  }
  else {
    /*
    ** set the fonts file name
    */
    font->fileName = (char*)allocMem((strlen(fonts_path) + strlen(fileName) + 1 ) * sizeof(char));
    if (!font->fileName) {
      initFailed = WONKA_TRUE;
    }
    else {
      strcpy(font->fileName, fonts_path);
      strcpy(font->fileName + strlen(fonts_path), fileName);

      /*
      ** open the font file
      */
      input = FontFileOpen (font->fileName);
      if (!input) {
        initFailed = WONKA_TRUE;

        // if this fails, don't throw an exception - continue loading other
        // fonts anyway.
      }
      else {
        /*
        ** read font's properties tabel from file
        */

        if (!pcfGetInitialProperties(&fontProps, input)) {
          initFailed = WONKA_TRUE;
          throwException (thread, clazzIllegalArgumentException, "Failed to read properties table in font file \"%s\"\n", fileName);
        }
        else {
//  printf("\n");	
//  pcfPrintProperties(fontProps.props, fontProps.isStringProp, fontProps.nprops);
          /*
          ** Set the new font's name:
          */
          font->name = (char *)allocMem((strlen(fontName) + 1) * sizeof(char));
          if (!font->name) {
            initFailed = WONKA_TRUE;
          }
          else {
            strcpy(font->name, fontName);
            /*
            ** Set the new font's family name:
            */
            for (i = 0; i < fontProps.nprops; i++) {
              atomName=NameForAtom(fontProps.props[i].name);
              if (atomName && (fontProps.isStringProp[i]) &&
                  (strncmp(atomName, "FAMILY_NAME", 11) == 0) ) {
                break;
              }
            }
            if (i == fontProps.nprops) {
              initFailed = WONKA_TRUE;
              throwException (thread, clazzIllegalArgumentException, "Failed finding FAMILY_NAME property in font file \"%s\"\n", fileName);
            }
            else {
              atomName=NameForAtom(fontProps.props[i].value);
              font->family = (char *)allocMem(strlen(atomName) + 1);
              if (!font->family) {
                initFailed = WONKA_TRUE;
              }
              else {
                strncpy(font->family, atomName, strlen(atomName));
                font->family[strlen(atomName)] = '\0';

                /*
                ** Set the new font's style, based on WEIGHT_NAME properties
                */
                for (i = 0; i < fontProps.nprops; i++) {
                  atomName=NameForAtom(fontProps.props[i].name);
                  if (atomName && (fontProps.isStringProp[i]) &&
                      (strncmp(atomName, "WEIGHT_NAME", 11) == 0) ) {
                    break;
                  }
                }
                if (i == fontProps.nprops) {
                  initFailed = WONKA_TRUE;
                  throwException (thread, clazzIllegalArgumentException, "Failed finding WEIGHT_NAME property in font file \"%s\"\n", fileName);
                }
                else {
                  atomName=NameForAtom(fontProps.props[i].value);
                  if (strncmp(atomName, "Medium", 6) == 0) {
                    font->style = font->style | (w_int)getStaticReferenceField(clazzFont,F_Font_PLAIN);
                  }
                  else {
                    if (strncmp(atomName, "Bold", 4) == 0) {
                      font->style = font->style | (w_int)getStaticReferenceField(clazzFont,F_Font_BOLD);
                    }
                    else {
                      woempa(9, "WARNING: un-expected value for WEIGHT_NAME in font file \"%s\"; \"Medium\" will be assumed\n", fileName);
                      font->style = font->style | (w_int)getStaticReferenceField(clazzFont,F_Font_PLAIN);
                    }
                  }

                  /*
                  ** Set the new font's style, based on SLANT properties
                  */
                  for (i = 0; i < fontProps.nprops; i++) {
                    atomName=NameForAtom(fontProps.props[i].name);
                    if (atomName && (fontProps.isStringProp[i]) &&
                        (strncmp(atomName, "SLANT", 5) == 0) ) {
                      break;
                    }
                  }
                  if (i == fontProps.nprops) {
                    initFailed = WONKA_TRUE;
                    throwException (thread, clazzIllegalArgumentException, "Failed finding SLANT property in font file \"%s\"\n", fileName);
                  }
                  else {
                    atomName=NameForAtom(fontProps.props[i].value);
                    if (strncmp(atomName, "R", 1) == 0) {
                      ;  // leave the style unchanged
                    }
                    else {
                      if (strncmp(atomName, "I", 1) == 0) {
                        font->style = font->style | (w_int)getStaticReferenceField(clazzFont,F_Font_ITALIC);
                      }
                      else {
                        woempa(9, "WARNING: un-expected value for SLANT in font file \"%s\"; \"R\" will be assumed\n", fileName);
                        // leave the style unchanged
                      }
                    }
                    for (i = 0; i < fontProps.nprops; i++) {
                      atomName=NameForAtom(fontProps.props[i].name);
                      if (atomName && (!fontProps.isStringProp[i]) &&     /* property is numerical */
                          (strncmp(atomName, "PIXEL_SIZE", 10) == 0) ) {
                        break;
                      }
                    }

                    if (i == fontProps.nprops) {
                      initFailed = WONKA_TRUE;
                      throwException (thread, clazzIllegalArgumentException, "Failed finding PIXEL_SIZE property in font file \"%s\"\n", fileName);
                    }
                    else {
                      font->size = fontProps.props[i].value;   /* property is numerical */
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (fontProps.isStringProp){
    releaseMem(fontProps.isStringProp);
  }
  if (fontProps.props){
    releaseMem(fontProps.props);
  }
  if (input){
    FontFileClose(input);
  }

  if (initFailed) {
    woempa(9, "Failed initializing font '%s' using file '%s' \n", fontName, fileName);
    if (font) {
      if (font->name){
        releaseMem(font->name);
      }
      if (font->family){
        releaseMem(font->family);
      }
      if (font->fileName){
        releaseMem(font->fileName);
      }
      releaseMem(font);
    }
    if (num_fonts == 0 && fonts) {
      releaseMem(fonts);
      fonts = 0;
    }
  }
  else {                   /* init succeeded */

    font->loaded = WONKA_FALSE;

    fonts[num_fonts++] = font;

    // Debug output:
    woempa(9, "sucessfully initialized font '%s' (family: %s; size: %i; style: %i)\n", font->name, font->family, font->size, font->style );

    if(num_fonts == 1) {  /* This is the first font, make it the default font ! */
      w_instance String;

      enterUnsafeRegion(thread);
      defaultFont = (w_instance)allocInstance(thread, clazzFont);
      enterSafeRegion(thread);
      setWotsitField(defaultFont, F_Font_wotsit, fonts[0]);
      String = newStringInstance(ascii2String(fonts[0]->name, strlen(fonts[0]->name)));
      setReferenceField(defaultFont, String, F_Font_name);
      String = newStringInstance(ascii2String(fonts[0]->family, strlen(fonts[0]->family)));
      setReferenceField(defaultFont, String, F_Font_family);
      setIntegerField(defaultFont, F_Font_size, fonts[0]->size);
      setIntegerField(defaultFont, F_Font_style, fonts[0]->style);
      newGlobalReference(defaultFont);
      if (!Font_loadFont(fonts[0])){
        throwException (thread, clazzIllegalArgumentException, "Error loading default font \"%s\"\n", fonts[0]->fileName);
        return;
      }
    }
  }

  if(isCopy1 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, name, fontName);
  if(isCopy2 == JNI_TRUE) (*env)->ReleaseStringUTFChars(env, file, fileName);
  return;

}

/*
** Native implementation of java.awt.Font.create(String fontname, w_int style, w_int size)
** the main call of java.awt.Font.<constructor>(String fontname, w_int style, w_int size)
*/

w_void Font_create(w_thread thread, jobject thisFont, jobject name, jint style, jint size) {
  w_string   wString;
  char*      cString;
  w_instance stringInstanceName;
  w_instance stringInstanceFamily;
  r_font     font = NULL;

  if (name!=NULL)
    wString = getWotsitField(name, F_String_wotsit);
  else
    wString = ascii2String("Default", strlen("Default"));

  cString = allocMem((string_length(wString) + 1) * sizeof(char));
  if (!cString) {
    return;
  }

  w_string2c_string(wString, cString);

  // get the closest match to the requested font
  font = Font_getFont(cString, style, size);

  if (!Font_loadFont(font)) {
    if (exceptionThrown(thread)){
      return;
    }
    woempa(9, "returning default font\n", font->name, font->fileName);
    font = getWotsitField(defaultFont, F_Font_wotsit);
  }

  // like in jdk, assign the 'name' parameter of this function 'Font_create', to the returned
  // java font 'thisFont'. Leave the name of the internal font struct to which it will point
  // (via the wotsit field) intact (otherwise the internal font in tabel 'fonts' is changed!).
  stringInstanceName = newStringInstance(wString);
  setReferenceField(thisFont, stringInstanceName, F_Font_name);

  // set thisFont's family name (reuse 'wString')
  wString = ascii2String((char*)font->family, strlen(font->family));
  stringInstanceFamily = newStringInstance(wString);
  setReferenceField(thisFont, stringInstanceFamily, F_Font_family);

  // set thisFont's size and style
  setIntegerField(thisFont, F_Font_size, font->size);
  setIntegerField(thisFont, F_Font_style, font->style);

  // Attach an internal rudolph font struct to the Java font instance:
  setWotsitField(thisFont, F_Font_wotsit, font);

  releaseMem(cString);

  // woempa(9, "called Font_create(): '%s, %i' --> '%s, %i'\n", cString, size, font->name, font->size);
}


/*
** if the font has no default glyph or the default glyph is the space character,
** create a new default glyph and let the FontPtr's default glyph point to it;
** remark that the caller must dispose of this glyph before unloading the font
** structure in FontPtr!
*/

static CharInfoPtr Font_fixDefaultGlyph(FontPtr pFont) {

  w_int widthBytes;
  w_int width;
  w_int height;
  char* bits = 0;
  char* pByte = 0;
  w_int r,c,b;
  BitmapFontPtr bitmapFont = (BitmapFontPtr) pFont->fontPrivate;
  CharInfoPtr pDefault = 0;

  if (bitmapFont->pDefault != NULL /* && pFont->info.defaultCh != 32 */ ) {
    return 0;
  }
  else { /* create a new default glyph */
    pDefault = (CharInfoPtr)allocMem(sizeof(CharInfoRec));
    if (!pDefault){
      woempa(9, "Failed fixing default glyph: cannot allocate glyph structure\n");
      return 0;
    }
    pDefault->metrics.ascent = pFont->info.maxbounds.ascent;
    pDefault->metrics.descent = pFont->info.maxbounds.descent;
    pDefault->metrics.characterWidth = pFont->info.maxbounds.characterWidth;
    pDefault->metrics.leftSideBearing = pFont->info.maxbounds.leftSideBearing;
    pDefault->metrics.rightSideBearing = pFont->info.maxbounds.rightSideBearing;
    height =  pDefault->metrics.ascent;
    width = pDefault->metrics.rightSideBearing - pDefault->metrics.leftSideBearing;

    switch (pFont->glyph) {
      case 1:
        widthBytes = (width+7)>>3;
        break;
      case 2:
        widthBytes = ((width+15)>>4)<<1;
        break;
      case 4:	
        widthBytes = ((width+31)>>5)<<2;
        break;
      case 8:	
        widthBytes = ((width+63)>>6)<<3;
        break;
      default:
        woempa(9, "Failed fixing default glyph: illegal value for font glyph padding: %d\n", pFont->glyph);
        releaseMem(pDefault);
        return 0;
    }
    bits = (char*)allocClearedMem(widthBytes * (pDefault->metrics.ascent + pDefault->metrics.descent) * sizeof(char));
    if (!bits){
      woempa(9, "Failed fixing default glyph: cannot allocate bitmap\n");
      releaseMem(pDefault);
      return 0;
    }
    for (c = 0; c < width; c += 2) {
      b = c/8;
      pByte =  bits + (widthBytes * 2) + b;
      *pByte = (*pByte) | (1 << ( 7 - c + b * 8 ));
      pByte =  bits + (widthBytes * (height - 1)) + b;
      *pByte = (*pByte) | (1 << ( 7 - c + b * 8));
    }
    for (r = 3; r < height - 1; r += 2) {
      pByte =  bits + (r * widthBytes);
      *pByte = (*pByte) | (0x80);
      b = (width - 1) / 8;
      pByte =  pByte + b;
      *pByte = (*pByte) | ( 1 << (7 - width + 1 + b * 8));
    }
    pDefault->bits = bits;
    bitmapFont->pDefault = pDefault;
    return pDefault;
  }

}


/*
** Auxilliary: Get the available font closest to a given font name, style,size
*/

r_font Font_getFont(char *name, w_int style, w_int size) {

  // Initialize the internal font using the default font:
  r_font   font = getWotsitField(defaultFont, F_Font_wotsit);
  w_int    i;

  woempa(9, "looking for font '%s, %i, %i' \n", name, style, size);

  for (i = 0; i < num_fonts; i++) {
    if (fonts[i]) {
      // Check to see whether we have an exact match:
      if ((fonts[i]->name) && (strcmp(fonts[i]->name, name)) == 0) {
        font = fonts[i];
        break;
      }

      // Check to see whether we have an exact match:
      if ((fonts[i]->family) && (strcmp(fonts[i]->family, name) == 0)) {
        if (fonts[i]->size == size) {
          if (fonts[i]->style == style) {
            font = fonts[i];
            break;
          }
        }
//        else {
//          if (abs((w_int)(fonts[i]->size - (w_word) size)) < abs((w_int)(font->size - size))) {
//            font = fonts[i];
//          }
//        }
      }

      /*
       * In case no exact match exists, at least try to select a font
       * with a size that matches the requested font size as close as
       * possible to ensure a correct look and feel:
       */
      if (abs((w_int)(fonts[i]->size - (w_word) size)) < abs((w_int)(font->size - size))) {
        font = fonts[i];
      }
    }
  }

  woempa(9, "found font '%s, %i, %i' \n", font->name, font->style, font->size);

  return font;
}

w_boolean Font_loadFont(r_font font) {

  w_thread    thread = currentWonkaThread;
  w_boolean   loadFailed = WONKA_FALSE;
  FontFilePtr input = NULL;
  FontPtr     pFont = NULL;

  if (font->loaded) {
    return WONKA_TRUE;
  }

  input = FontFileOpen (font->fileName);
  if (!input) {
    throwException (thread, clazzIllegalArgumentException, "Error opening font file \"%s\"\n", font->fileName);
    loadFailed = WONKA_TRUE;
  }
  else {
    pFont = (FontPtr) allocClearedMem(sizeof(FontRec) );
    if (!pFont) {
      loadFailed = WONKA_TRUE;
    }
    else {
      if (pcfReadFont(pFont, input, DEFAULT_BIT_ORDER, DEFAULT_BYTE_ORDER,
                      DEFAULT_GLYPH_PAD, DEFAULT_SCAN_UNIT)  != Successful)  {
        throwException (thread, clazzIllegalArgumentException, "Error while loading font file \"%s\"\n", font->fileName);
        loadFailed = WONKA_TRUE;
      }
      else {

        font->pDefault = Font_fixDefaultGlyph(pFont);
        font->ascent  = pFont->info.fontAscent;
        font->descent = pFont->info.fontDescent;
        font->maxAscent  = FONT_MAX_ASCENT(&(pFont->info));
        font->maxDescent = FONT_MAX_DESCENT(&(pFont->info));
        font->maxAdvance = FONT_MAX_WIDTH(&(pFont->info));
        // calculate the fonts' 'leading'
        font->leading  = ((font->maxAscent > font->ascent) ? font->maxAscent - font->ascent : 0) +
                         ((font->maxDescent > font->descent) ? font->maxDescent - font->descent : 0);
        font->pFont = pFont;
        font->loaded = WONKA_TRUE;

      }
    }
  }

  if (input) {
    FontFileClose(input);
  }
  if (loadFailed) {
    if (pFont) {
      pcfUnloadFont(pFont);
    }
    woempa(9, "failed loading font '%s' from file '%s' \n", font->name, font->fileName);
    return WONKA_FALSE;
  }
  else {
    woempa(9, "successfully loaded font '%s' from file '%s' \n", font->name, font->fileName);
    return WONKA_TRUE;
  }

}

/*
w_void Font_unloadFont(w_thread thread, jobject thisFont){

  r_font font = getWotsitField(thisFont, F_Font_wotsit);

  if (font && font->loaded) {
    if (font->pDefault){
      if (font->pDefault->bits){
        releaseMem(font->pDefault->bits);
      }
      releaseMem(font->pDefault);
      font->pDefault = 0;
    }
    if (font->pFont){
      font->pFont->unload_font(font->pFont);
    font->pFont = 0;
    }
    font->ascent = 0;
    font->descent = 0;
    font->leading = 0;
    font->maxAscent = 0;
    font->maxDescent = 0;
    font->maxAdvance =0;
    font->loaded = WONKA_FALSE;
    woempa(9, "successfully unloaded font '%s' \n", font->name);
    woempa(9, "            successfully unloaded font '%s' \n", font->name);
    woempa(9, "                      successfully unloaded font '%s' \n", font->name);
    woempa(9, "                                   successfully unloaded font '%s' \n", font->name);
  }
}
*/

/*
** Auxilliary: return the fontheight of a certain font
*/

inline w_int Font_getHeight(r_font font) {
  return (font ? font->leading + font->ascent + font->descent : 0);
}


/*
** Auxilliary: return length in bits of a given array of glyphs (CharInfoPtr*);
*/

w_int Font_getGlyphStringWidth(CharInfoPtr* glyphs, w_word glyphCount) {

  w_int        width = 0;
  w_word       i;

  if (!glyphs || glyphCount == 0){
    return 0;
  }

  for (i = 0; i < glyphCount; i++) {
    width += glyphs[i]->metrics.characterWidth;    // or do we have to use rsbearing - lsbearing?
  }

  return width;
}

w_int Font_getCharWidth(r_font font, w_int charno) {

  w_ubyte      code[2];
  CharInfoPtr  glyphs[1];
  w_word       glyphCount;
  w_int        width = 0;
  FontEncoding encoding;

  if (font->pFont->info.constantWidth) {
    return font->pFont->info.maxbounds.characterWidth;
  }

  code[0] = ((charno & 0xff00)>>8);
  code[1] = (charno & 0x00ff);

  //woempa(9, "getting width of w_int: charno= 0x%4x\n", charno);
  //woempa(9, "getting width of w_char: byte[0]= 0x%2x, byte[1]=0x%2x\n", code[0], code[1]);

  if (font->pFont->info.firstRow == 0 && font->pFont->info.lastRow == 0)
    encoding = Linear16Bit;
  else
    encoding = TwoD16Bit;

  if ( bitmapGetGlyphs(font->pFont, 1, (w_ubyte*)(&code), encoding, &glyphCount, glyphs) != Successful ) {
    throwException(currentWonkaThread, clazzIllegalArgumentException, "Failed retrieving glyphs from font \"%s\"\n", font->name);
  }
  else  {
    width = glyphs[0]->metrics.characterWidth;
  }

  return width;

}

/*
** Auxilliary: return width in pixels of a given w_string of characters written in a given font
*/

w_int Font_getStringWidth(r_font font, w_string string) {

  w_thread     thread = currentWonkaThread;
  w_int        width = 0;
  w_word       stringLen = string_length(string);
  w_char       wch;
  w_ubyte*     codes = 0;
  w_ubyte*     pcodes = 0;
  w_word       i;
  CharInfoPtr* glyphs = 0;
  w_word       glyphCount = 0;
  FontEncoding encoding;



  // Prune if the passed text is empty:
  if (stringLen == 0) {
    return 0;
  }

  if (font->pFont->info.constantWidth) {
    return (font->pFont->info.maxbounds.characterWidth * stringLen);
  }

  codes = (w_ubyte*)allocMem(2 * stringLen * sizeof(w_ubyte));
  if (!codes) {
    return 0;
  }
  pcodes = codes;
  for (i = 0; i < stringLen; i++) {
    wch = string_char(string, i);
    *pcodes++ = wch >> 8;
    *pcodes++ = wch & 0x00ff;
  }

  glyphs = (CharInfoPtr*)allocMem(stringLen * sizeof(CharInfoPtr));
  if (glyphs) {
    if (font->pFont->info.firstRow == 0 && font->pFont->info.lastRow == 0)
      encoding = Linear16Bit;
    else
      encoding = TwoD16Bit;

    if ( bitmapGetGlyphs(font->pFont, stringLen, codes, encoding, &glyphCount, glyphs) != Successful ) {
      throwException(thread, clazzIllegalArgumentException, "Failed retrieving glyphs from font \"%s\"\n", font->name);
      return 0;
    }
    else {
      width = Font_getGlyphStringWidth(glyphs, glyphCount);
    }
  }

  if (codes)
    releaseMem(codes);
  if (glyphs)
    releaseMem(glyphs);

  return width;
}

/*
** Font non-java function: Draw the desired String in the desired 
** target rectangle either left aligned, right aligned or centered.
** If necessary, the string is clipped to fit into the rectangle
** => called by Graphics.c function
*/


w_void Font_drawStringAligned(r_buffer buffer, r_font font, w_int x, w_int y, w_int w, w_int h, w_string string, w_int color, w_word alignment) {

  /*
  ** x,y,w and h specify the bounding box for drawing the string, in the given buffer.
  ** coordinates of the bounding box are (x, y, x+w-1, y+h-1).
  ** calculate a baseline and starting x position for drawing inside the bounding box
  ** and call Font_drawCharString
  */

  w_thread     thread = currentWonkaThread;
  w_int        charx = 0;                 // x coordinate for starting drawing text in x direction
  w_int        chary = 0;                 // baseline coordinate for starting drawing text
  w_word       stringLen = string_length(string);  // number of chars in the given string
  w_char       wch;
  w_ubyte*     codes = 0;
  w_ubyte*     pcodes = 0;
  w_word       i;
  w_word       glyphCount = 0;
  CharInfoPtr* glyphs = 0;
  w_boolean    drawFailed = WONKA_FALSE;
  FontEncoding encoding;

  // Prune if the passed text is empty:
  if (stringLen == 0) {
    return;
  }

  codes = (w_ubyte*)allocMem(2 * stringLen * sizeof(w_ubyte));
  if (!codes) {
    return;
  }

  pcodes = codes;
  for (i = 0; i < stringLen; i++) {
    wch = string_char(string, i);
    *pcodes++ = wch >> 8;
    *pcodes++ = wch & 0x00ff;
  }

  glyphs = (CharInfoPtr*)allocMem(stringLen * sizeof(CharInfoPtr));
  if (!glyphs) {
    drawFailed = WONKA_TRUE;
  }
  else {
    if (font->pFont->info.firstRow == 0 && font->pFont->info.lastRow == 0)
      encoding = Linear16Bit;
    else
      encoding = TwoD16Bit;

    if ( bitmapGetGlyphs(font->pFont, stringLen, codes, encoding, &glyphCount, glyphs)
        != Successful ) {
      throwException(thread, clazzIllegalArgumentException, "Failed retrieving glyps from font \"%s\"\n", font->name);
      drawFailed = WONKA_TRUE;
    }
    else {
      switch(alignment) {
        case 0:
          // -- left alignment --
          charx = x;
          chary = y + (h + font->maxAscent - font->maxDescent) / 2;
          break;
        case 1:
          // -- center alignment --
          // Calculate coordinate offsets:
          charx = x + (w - Font_getGlyphStringWidth(glyphs, glyphCount)) / 2 ;
          chary = y + (h + font->maxAscent - font->maxDescent) / 2;
          break;
        case 2:
          // -- right alignment --
          // Calculate coordinate offsets:
          charx = x + w - Font_getGlyphStringWidth(glyphs, glyphCount);
          chary = y + (h + font->maxAscent - font->maxDescent) / 2;
          break;
        default:
          woempa(10, "WARNING: something whicked happened: unknown alignment tag\n");
          drawFailed = WONKA_TRUE;
      }
    }
  }


  if (!drawFailed) {
    /*
    ** draw the glyphs of the text string:
    */
    Font_drawGlyphString(buffer, font->pFont, charx, chary, x, y, w, h, glyphs, glyphCount, color);
  }

  if (codes) {
    releaseMem(codes);
  }
  if (glyphs) {
    releaseMem(glyphs);
  }
  return;

}

/*
** Font non-java function: Draw the desired String in the desired
** target rectangle using the desired font. If necessary,the string
** is clipped to fit into the rectangle
** => called by Graphics.c function Graphics_drawString()
*/

w_void Font_drawStringUnAligned(r_buffer buffer, r_font font, w_int charx, w_int chary, w_int x, w_int y, w_int w, w_int h, w_string string, w_int color) {

  /*
  ** x,y,w and h specify the bounding box for drawing the string, in the given buffer.
  ** coordinates of the bounding box are (x, y, x+w-1, y+h-1).
  ** baseline and starting x coordinate for drawing are given as charx and chary. They can be located
  ** outside the bounding box. If the string to be drawn falls outside this box as a whole, the function
  ** returns immediatly.
  */

  w_thread     thread = currentWonkaThread;
  w_word       stringLen = string_length(string);  // number of chars in the given string
  w_char       wch;
  w_ubyte*     codes = 0;
  w_ubyte*     pcodes = 0;
  w_word       i;
  w_word       glyphCount = 0;
  CharInfoPtr* glyphs = 0;
  FontEncoding encoding;

  // Prune if the passed text is empty:
  if (stringLen == 0) {
    return;
  }

  codes = (w_ubyte*)allocMem(2 * stringLen * sizeof(w_ubyte));
  if (!codes) {
    return;
  }

  pcodes = codes;
  for (i = 0; i < stringLen; i++) {
    wch = string_char(string, i);
    *pcodes++ = wch >> 8;
    *pcodes++ = wch & 0x00ff;
  }


  glyphs = (CharInfoPtr*)allocMem(stringLen * sizeof(CharInfoPtr));
  if (glyphs) {
    if (font->pFont->info.firstRow == 0 && font->pFont->info.lastRow == 0)
      encoding = Linear16Bit;
    else
      encoding = TwoD16Bit;

    if ( bitmapGetGlyphs(font->pFont, stringLen, codes, encoding, &glyphCount, glyphs) != Successful ) {
      throwException(thread, clazzIllegalArgumentException, "Failed retrieving glyps from font \"%s\"\n", font->name);
    }
    else {
      // check overlap of string area and bounding box
      if (!(charx + Font_getGlyphStringWidth(glyphs, glyphCount) < x || chary + font->maxDescent < y || charx >= x + w || chary - font->maxAscent >= y + h)) {
        Font_drawGlyphString(buffer, font->pFont, charx, chary, x, y, w, h, glyphs, glyphCount, color);
      }
    }
  }


  if (codes) {
    releaseMem(codes);
  }
  if (glyphs) {
    releaseMem(glyphs);
  }
  return;
}

w_void Font_drawGlyphString(r_buffer buffer, FontPtr pFont, w_int charx, w_int chary, w_int x, w_int y, w_int w, w_int h, CharInfoPtr* glyphs, w_word glyphCount, w_int color) {

  // x,y,w and h specify the bounding box for drawing the string, in the given buffer.
  // charx and chary specify the baseline for text drawing.

  char*     pByte = NULL;               // points to the beginning of each bitmap row in sequence
  w_int     col;                        // counts the nbr of bytes in one row in the bitmap data,
  w_int     row;                        // counts the rows in the bitmap data
  w_int     height;                     // height in pixels of a particular character
  w_int     width;                      // width in pixels  of a particular character
  w_int     widthBytes = 0;             // width in bytes of a padded row of a character
  w_int     px;                         // pixel x-coordinate in drawing buffer
  w_int     py;                         // pixel y-coordinate in drawinng buffer
  w_word    stringInd = 0;              // character index in the glyphs array.
  w_int     i;
  xCharInfo m;                          // metrics data of a glyph
  w_int     advance = 0;
  /*
  ** Prune if the passed text is empty:
  */
  if (glyphCount == 0) {
    return;
  }

  /*
  ** loop through the text string and render each glyph individually;
  ** can be optimized using accelerator fields in 'pFont->info' :
  ** 'constantMetrics', 'constantWidth', 'fontAscent', 'fontDescent',
  ** 'maxbounds.leftSideBearing', 'maxbounds.rightSideBearing' and
  ** 'maxbounds.characterWidth'
  */

  if  (pFont->info.constantWidth) {
    width = pFont->info.maxbounds.characterWidth;
    advance = width;
    switch (pFont->glyph) {
      case 1:	
        widthBytes = (width+7)>>3;
        break;
      case 2:
        widthBytes = ((width+15)>>4)<<1;
        break;
      case 4:	
        widthBytes = ((width+31)>>5)<<2;
        break;
      case 8:	
        widthBytes = ((width+63)>>6)<<3;
        break;
      default:
        throwException(currentWonkaThread, clazzIllegalArgumentException, "Wrong value for bitmap padding : %i\n", pFont->glyph);
        return;
    }
  }

//  if  (pFont->info.constantMetrics) {
//  }

  for (stringInd = 0; stringInd < glyphCount; stringInd++ ) {
    /*
    ** get the metrics about the current glyph
    */
    m = glyphs[stringInd]->metrics;
    height  = m.ascent + m.descent;
    /*
    ** calculate the number of bytes the pixels of a row are stored in;
    ** depending on the value of pFont->glyph, bitmap rows are padded to the next byte, short int, int, or double int
    */
    if  (!pFont->info.constantWidth) {
    width   = m.rightSideBearing - m.leftSideBearing;
    advance = m.characterWidth;
    switch (pFont->glyph) {
      case 1:	
        widthBytes = (width+7)>>3;
        break;
      case 2:
        widthBytes = ((width+15)>>4)<<1;
        break;
      case 4:	
        widthBytes = ((width+31)>>5)<<2;
        break;
      case 8:	
        widthBytes = ((width+63)>>6)<<3;
        break;
      default:
        throwException(currentWonkaThread, clazzIllegalArgumentException, "Wrong value for bitmap padding : %i\n", pFont->glyph);
        return;
    }
    }
    /*
    ** draw the glyph
    */
    // get the bitmap of the current glyph
    pByte = glyphs[stringInd]->bits;

    py = chary - m.ascent;    // set vertical start position in drawing buffer to 'm.ascent' above baseline
    for (row = 0; row < height; row++) {
      // Calculate frame y-coordinate:
      if (py >= y && py < y + h) {
        // Calculate frame x-coordinate:
        px = charx + m.leftSideBearing;
        for (col = 0; col < widthBytes; col++) {   // iterate over bytes in a row
          for (i = 0; i < 8; i++) {              // iterate over bits in a byte
            if ( *(pByte+col)&(0x80>>i) ) {
              if (px >= x && px < x + w) {
                drawPixelClipped(buffer, px, py, color);  // draw pixel:
              }
            }
            px++;
          }
        }
      }
      pByte += widthBytes;      // point to the next bitmap row
      py++;                     // point to the next row in drawing buffer
    }

    charx += advance;  // move to drawing start position for next glyph
  }
}


/*
** Auxilliary: return length of a given cstring of characters written in a given font
*/

w_int Font_getCStringWidth(r_font font, char* text, w_int len) {

  w_thread     thread = currentWonkaThread;
  w_ubyte*     codes = 0;
  w_ubyte*     pcodes = 0;
  w_word       glyphCount = 0;
  CharInfoPtr* glyphs = 0;
  w_int        i;
  w_int        width = 0;
  FontEncoding encoding;

  // Prune if the passed text is empty:
  if (len == 0) {
    return 0;
  }

  if (font->pFont->info.constantWidth) {
    return (font->pFont->info.maxbounds.characterWidth * len);
  }

  codes = (w_ubyte*)allocMem(2 * len * sizeof(w_ubyte));
  if (!codes) {
    return 0;
  }

  pcodes = codes;
  for(i = 0; i < len; i++) {
    *pcodes++ = 0x00;
    *pcodes++ = (w_ubyte)text[i];
  }

  glyphs = (CharInfoPtr*)allocMem(len * sizeof(CharInfoPtr));
  if (glyphs) {
    if (font->pFont->info.firstRow == 0 && font->pFont->info.lastRow == 0)
      encoding = Linear16Bit;
    else
      encoding = TwoD16Bit;

    if ( bitmapGetGlyphs(font->pFont, len, codes, encoding, &glyphCount, glyphs) != Successful ) {
      throwException(thread, clazzIllegalArgumentException, "Failed retrieving glyps from font \"%s\"\n", font->name);
    }
    else {
      width = Font_getGlyphStringWidth(glyphs, glyphCount);
    }
  }

  if (codes)
    releaseMem(codes);
  if (glyphs)
    releaseMem(glyphs);

  return width;

}

