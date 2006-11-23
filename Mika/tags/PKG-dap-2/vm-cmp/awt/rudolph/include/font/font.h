/***********************************************************
Copyright 1987 by Digital Equipment Corporation, Maynard, Massachusetts.

                        All Rights Reserved

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted,
provided that the above copyright notice appear in all copies and that
both that copyright notice and this permission notice appear in
supporting documentation, and that the name of Digital not be
used in advertising or publicity pertaining to distribution of the
software without specific, written prior permission.

DIGITAL DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING
ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL
DIGITAL BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR
ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
SOFTWARE.
*/


/**************************************************************************
* Portions Copyright  (c) 2002 by Acunia N.V. All rights reserved.        *
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

/* $Id: font.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

#ifndef FONT_H
#define FONT_H

#ifndef BitmapFormatByteOrderMask
#include	"fsmasks.h"
#endif

/* data structures */
#ifndef _XTYPEDEF_FONTPTR
typedef struct _Font *FontPtr;
#define _XTYPEDEF_FONTPTR
#endif

typedef struct _FontInfo *FontInfoPtr;
typedef struct _FontProp *FontPropPtr;
typedef struct _ExtentInfo *ExtentInfoPtr;
typedef struct _FontPathElement *FontPathElementPtr;

#ifndef _XTYPEDEF_CHARINFOPTR
typedef struct _CharInfo *CharInfoPtr;
#define _XTYPEDEF_CHARINFOPTR
#endif

typedef struct _FontNames *FontNamesPtr;
typedef struct _FontResolution *FontResolutionPtr;

#define NullCharInfo	((CharInfoPtr) 0)
#define NullFont	((FontPtr) 0)
#define NullFontInfo	((FontInfoPtr) 0)

 /* draw direction */
#define LeftToRight 0
#define RightToLeft 1
#define BottomToTop 2
#define TopToBottom 3
typedef w_int DrawDirection;

/* constant used to indicate max w_ushort by casting it to w_ushort */
/* (result normally is 256*256-1) */
#define NO_SUCH_CHAR	-1


#define	FontAliasType	0x1000

#define	AllocError	80
#define	StillWorking	81
#define	FontNameAlias	82
#define	BadFontName	83
#define	Suspended	84
#define	Successful	85
#define	BadFontPath	86
#define	BadCharRange	87
#define	BadFontFormat	88
#define	FPEResetFailed	89	/* for when an FPE reset won't work */

#ifdef USED_IN_WONKA


/* OpenFont flags */
#define FontLoadInfo	0x0001
#define FontLoadProps	0x0002
#define FontLoadMetrics	0x0004
#define FontLoadBitmaps	0x0008
#define FontLoadAll	0x000f
#define	FontOpenSync	0x0010
#define FontReopen	0x0020

/* Query flags */
#define	LoadAll		0x1
#define	FinishRamge	0x2
#define EightBitFont    0x4
#define SixteenBitFont  0x8

/* Glyph Caching Modes */
#define CACHING_OFF 0
#define CACHE_16_BIT_GLYPHS 1
#define CACHE_ALL_GLYPHS 2
#define DEFAULT_GLYPH_CACHING_MODE CACHING_OFF
extern w_int glyphCachingMode;

extern w_int StartListFontsWithInfo(
#if NeedFunctionPrototypes
    ClientPtr /*client*/,
    w_int /*length*/,
    w_ubyte */*pattern*/,
    w_int /*max_names*/
#endif
void );

extern FontNamesPtr MakeFontNamesRecord(
#if NeedFunctionPrototypes
    w_word /* size */
#endif
);

extern void FreeFontNames(
#if NeedFunctionPrototypes
    FontNamesPtr /* pFN*/
#endif
);

extern w_int  AddFontNamesName(
#if NeedFunctionPrototypes
    FontNamesPtr /* names */,
    char * /* name */,
    w_int /* length */
#endif
);

#if 0 /* unused */
extern w_int  FontToFSError();
extern FontResolutionPtr GetClientResolution();
#endif

typedef struct _FontPatternCache    *FontPatternCachePtr;

extern FontPatternCachePtr  MakeFontPatternCache (
#if NeedFunctionPrototypes
    void
#endif
);

extern void		    FreeFontPatternCache (
#if NeedFunctionPrototypes
    FontPatternCachePtr /* cache */
#endif
);

extern void		    EmptyFontPatternCache (
#if NeedFunctionPrototypes
    FontPatternCachePtr /* cache */
#endif
);

extern void		    CacheFontPattern (
#if NeedFunctionPrototypes
    FontPatternCachePtr /* cache */,
    char * /* pattern */,
    w_int /* patlen */,
    FontPtr /* pFont */
#endif
);
extern FontResolutionPtr GetClientResolutions(
#if NeedFunctionPrototypes
    w_int * /* num */
#endif
);

extern FontPtr		    FindCachedFontPattern (
#if NeedFunctionPrototypes
    FontPatternCachePtr /* cache */,
    char * /* pattern */,
    w_int /* patlen */
#endif
);

extern void		    RemoveCachedFontPattern (
#if NeedFunctionPrototypes
    FontPatternCachePtr /* cache */,
    FontPtr /* pFont */
#endif
);

#endif             /* USED_IN_WONKA */

typedef enum {
    Linear8Bit, TwoD8Bit, Linear16Bit, TwoD16Bit
}           FontEncoding;


#endif				/* FONT_H */
