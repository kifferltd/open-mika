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

******************************************************************/

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

/* $Id: fontstruct.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

#ifndef FONTSTR_H
#define FONTSTR_H

#include "font.h"

/*
 * This version of the server font data strucutre is only for describing
 * the in memory data structure. The file structure is not necessarily a
 * copy of this. That is up to the compiler and the OS layer font loading
 * machinery.
 */

#define GLYPHPADOPTIONS 4	/* 4 possible glyph padding schemes identified as 1, 2, 4, or 8 */

typedef struct _FontProp {
    w_int        name;
    w_int        value;		/* assumes ATOM is not larger than INT32 */
}           FontPropRec;

/*
 * info needed for font initialisation   // jvde added 23092002
 */

typedef struct _FontProps {
  w_int       nprops;
  FontPropPtr props;
  char*       isStringProp;
}           FontPropsRec;

typedef struct _FontResolution {
    w_ushort x_resolution;
    w_ushort y_resolution;
    w_ushort point_size;
}           FontResolutionRec;

typedef struct _ExtentInfo {
    DrawDirection drawDirection;
    w_int         fontAscent;
    w_int         fontDescent;
    w_int         overallAscent;
    w_int         overallDescent;
    w_int         overallWidth;
    w_int         overallLeft;
    w_int         overallRight;
}           ExtentInfoRec;

typedef struct _CharInfo {
    xCharInfo   metrics;	/* info preformatted for Queries */
    char*       bits;		/* pointer to glyph image */
}           CharInfoRec;

/*
 * Font is created at font load time. It is specific to a single encoding.
 * e.g. not all of the glyphs in a font may be part of a single encoding.
 */

typedef struct _FontInfo {
    w_ushort    firstCol;
    w_ushort    lastCol;
    w_ushort    firstRow;
    w_ushort    lastRow;
    w_ushort    defaultCh;
    w_word      noOverlap:1;
    w_word      constantMetrics:1;
    w_word      terminalFont:1;
    w_word      constantWidth:1;
    w_word      inkInside:1;
    w_word      inkMetrics:1;
    w_word      allExist:1;
    w_word      drawDirection:2;
    w_word      cachable:1;
    w_word      anamorphic:1;
    w_short     maxOverlap;
    w_short     pad;
    xCharInfo   maxbounds;
    xCharInfo   minbounds;
    xCharInfo   ink_maxbounds;
    xCharInfo   ink_minbounds;
    w_short     fontAscent;
    w_short     fontDescent;
    w_int       nprops;
    FontPropPtr props;
    char*       isStringProp;
}           FontInfoRec;

typedef struct _Font {
    w_int       refcnt;
    FontInfoRec info;
    char        bit;
    char        byte;
    char        glyph;
    char        scan;
    fsBitmapFormat format;
#if NeedNestedPrototypes
    w_int       (*get_glyphs) (              /* see one impl in bitmap.c */
	FontPtr         /* font */,
	w_word          /* count */,
	w_ubyte*        /* chars */,
	FontEncoding    /* encoding */,
	w_word*         /* count */,
	CharInfoPtr *   /* glyphs */
        );
    w_int       (*get_metrics) (             /* see one impl in bitmap.c */
	FontPtr         /* font */,
	w_word          /* count */,
	w_ubyte *       /* chars */,
	FontEncoding    /* encoding */,
	w_word  *       /* count */,
	xCharInfo **    /* glyphs */
        );
    void        (*unload_font) (             /* see one impl in pcfread.c */
	FontPtr         /* font */
        );
    void        (*unload_glyphs) (           /* for instance set to NULL in pcfread.c */
	FontPtr         /* font */
        );
#else
    w_int       (*get_glyphs) ();
    w_int       (*get_metrics) ();
    void        (*unload_font) ();
    void        (*unload_glyphs) ();
#endif
    FontPathElementPtr fpe;
    pointer     svrPrivate;
    pointer     fontPrivate;
    pointer     fpePrivate;
    w_int	maxPrivate;
    pointer*	devPrivates;
}           FontRec;


extern w_boolean _FontSetNewPrivate (
#if NeedFunctionPrototypes
                FontPtr        /* pFont */,
                w_int          /* n */,
                pointer        /* ptr */
#endif
                );
extern w_int    AllocateFontPrivateIndex (
#if NeedFunctionPrototypes
                void
#endif
                );

#define FontGetPrivate(pFont,n) ((n) > (pFont)->maxPrivate ? (pointer) 0 : \
			     (pFont)->devPrivates[n])

#define FontSetPrivate(pFont,n,ptr) ((n) > (pFont)->maxPrivate ? \
			_FontSetNewPrivate (pFont, n, ptr) : \
			((((pFont)->devPrivates[n] = (ptr)) != 0) || WONKA_TRUE))

typedef struct _FontNames {
    w_int         nnames;
    w_int         size;
    w_int*        length;
    char**        names;
}           FontNamesRec;

/* External view of font paths */
typedef struct _FontPathElement {
    w_int       name_length;
    char*       name;
    w_int       type;
    w_int       refcount;
    pointer     private;
}           FontPathElementRec;

typedef struct _FPEFunctions {
#if NeedFunctionPrototypes
    w_int
    (*name_check) (
                  char* /* name */
                  );
    w_int
    (*init_fpe) (
		FontPathElementPtr /* fpe */
		);
    w_int
    (*reset_fpe) (
		 FontPathElementPtr /* fpe */
		 );
    w_int
    (*free_fpe) (
		FontPathElementPtr /* fpe */
		);
    w_int
    (*open_font) (
		 pointer /* client */,
		 FontPathElementPtr /* fpe */,
		 w_int /* flags */,
		 char* /* name */,
		 w_int /* namelen */,
		 fsBitmapFormat /* format */,
		 fsBitmapFormatMask /* fmask */,
		 w_word /* id (type XID or FSID) */,
		 FontPtr* /* pFont */,
		 char** /* aliasName */,
		 FontPtr /* non_cachable_font */
		 );

    w_int
    (*close_font) (
                  FontPathElementPtr /* fpe */,
                  FontPtr /* pFont */
		  );
    w_int
    (*list_fonts) (
                  pointer /* client */,
		  FontPathElementPtr /* fpe */,
		  char* /* pat */,
		  w_int /* len */,
		  w_int /* max */,
		  FontNamesPtr /* names */
		  );
    w_int
    (*start_list_fonts_and_aliases) (
                                     pointer /* client */,
                                     FontPathElementPtr /* fpe */,
                                     char* /* pat */,
                                     w_int /* len */,
                                     w_int /* max */,
                                     pointer* /* privatep */
                                     );
    w_int
    (*list_next_font_or_alias) (
			       pointer /* client */,
			       FontPathElementPtr /* fpe */,
			       char** /* namep */,
			       w_int* /* namelenp */,
			       char** /* resolvedp */,
			       w_int* /* resolvedlenp */,
			       pointer /* private */
			       );
    w_int
    (*start_list_fonts_with_info) (
				  pointer /* client */,
				  FontPathElementPtr /* fpe */,
				  char* /* pat */,
				  w_int /* patlen */,
				  w_int /* maxnames */,
				  pointer* /* privatep */
				  );
    w_int
    (*list_next_font_with_info) (
    	                        pointer /* client */,
				FontPathElementPtr /* fpe */,
				char** /* name */,
				w_int* /* namelen */,
				FontInfoPtr* /* info */,
				w_int* /* numFonts */,
				pointer /* private */
				);
    w_int
    (*wakeup_fpe) (
                  FontPathElementPtr /* fpe */,
		  w_word* /* LastSelectMask */
		  );
    w_int		
    (*client_died) (
		   pointer /* client */,
		   FontPathElementPtr /* fpe */
                   );
		/* for load_glyphs, range_flag = 0 ->
			nchars = # of characters in data
			item_size = bytes/char
			data = list of characters
		   range_flag = 1 ->
			nchars = # of fsChar2b's in data
			item_size is ignored
			data = list of fsChar2b's */
    w_int		
    (*load_glyphs) (
                   pointer /* client */,
		   FontPtr /* pfont */,
		   w_boolean /* range_flag */,
		   w_word /* nchars */,
		   w_int /* item_size */,
		   w_ubyte* /* data */
		   );
    void	
    (*set_path_hook)(
		    void
		    );
#else
    w_int
    (*name_check) ( );
    w_int
    (*init_fpe) ( );
    w_int
    (*reset_fpe) ( );
    w_int
    (*free_fpe) ( );
    w_int
    (*open_font) ( );

    w_int
    (*close_font) ( );
    w_int
    (*list_fonts) ( );
    w_int
    (*start_list_fonts_and_aliases) ( );
    w_int
    (*list_next_font_or_alias) ( );
    w_int
    (*start_list_fonts_with_info) ( );
    w_int
    (*list_next_font_with_info) ( );
    w_int
    (*wakeup_fpe) ( );
    w_int		
    (*client_died) ( );
    w_int		
    (*load_glyphs) ( );
    void	
    (*set_path_hook)( );

#endif
}           FPEFunctionsRec, FPEFunctions;

#if 0	/* unused */
extern w_int  InitFPETypes();
#endif

/*
 * Various macros for computing values based on contents of
 * the above structures
 */

#define	GLYPHWIDTHPIXELS(pci) \
	((pci)->metrics.rightSideBearing - (pci)->metrics.leftSideBearing)

#define	GLYPHHEIGHTPIXELS(pci) \
 	((pci)->metrics.ascent + (pci)->metrics.descent)

#define	GLYPHWIDTHBYTES(pci)	(((GLYPHWIDTHPIXELS(pci))+7) >> 3)

#define GLYPHWIDTHPADDED(bc)	(((bc)+7) & ~0x7)

#define BYTES_PER_ROW(bits, nbytes) \
	((nbytes) == 1 ? (((bits)+7)>>3)	/* pad to 1 byte */ \
	:(nbytes) == 2 ? ((((bits)+15)>>3)&~1)	/* pad to 2 bytes */ \
	:(nbytes) == 4 ? ((((bits)+31)>>3)&~3)	/* pad to 4 bytes */ \
	:(nbytes) == 8 ? ((((bits)+63)>>3)&~7)	/* pad to 8 bytes */ \
	: 0)

#define BYTES_FOR_GLYPH(ci,pad)	(GLYPHHEIGHTPIXELS(ci) * \
				 BYTES_PER_ROW(GLYPHWIDTHPIXELS(ci),pad))
/*
 * Macros for computing different bounding boxes for fonts; from
 * the font protocol
 */

#define FONT_MAX_ASCENT(pi)	((pi)->fontAscent > (pi)->ink_maxbounds.ascent ? \
			    (pi)->fontAscent : (pi)->ink_maxbounds.ascent)
#define FONT_MAX_DESCENT(pi)	((pi)->fontDescent > (pi)->ink_maxbounds.descent ? \
			    (pi)->fontDescent : (pi)->ink_maxbounds.descent)
#define FONT_MAX_HEIGHT(pi)	(FONT_MAX_ASCENT(pi) + FONT_MAX_DESCENT(pi))
#define FONT_MIN_LEFT(pi)	((pi)->ink_minbounds.leftSideBearing < 0 ? \
			    (pi)->ink_minbounds.leftSideBearing : 0)
#define FONT_MAX_RIGHT(pi)	((pi)->ink_maxbounds.rightSideBearing > \
				(pi)->ink_maxbounds.characterWidth ? \
			    (pi)->ink_maxbounds.rightSideBearing : \
				(pi)->ink_maxbounds.characterWidth)
#define FONT_MAX_WIDTH(pi)	(FONT_MAX_RIGHT(pi) - FONT_MIN_LEFT(pi))

#endif				/* FONTSTR_H */
