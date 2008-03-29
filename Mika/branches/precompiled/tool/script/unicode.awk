###########################################################################
# Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 #
#                                                                         #
# This software is copyrighted by and is the sole property of Acunia N.V. #
# and its licensors, if any. All rights, title, ownership, or other       #
# interests in the software remain the property of Acunia N.V. and its    #
# licensors, if any.                                                      #
#                                                                         #
# This software may only be used in accordance with the corresponding     #
# license agreement. Any unauthorized use, duplication, transmission,     #
#  distribution or disclosure of this software is expressly forbidden.    #
#                                                                         #
# This Copyright notice may not be removed or modified without prior      #
# written consent of Acunia N.V.                                          #
#                                                                         #
# Acunia N.V. reserves the right to modify this software without notice.  #
#                                                                         #
#   Acunia N.V.                                                           #
#   Vanden Tymplestraat 35      info@acunia.com                           #
#   3000 Leuven                 http://www.acunia.com                     #
#   Belgium - EUROPE                                                      #
#                                                                         #
# Modifications copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded Java #
# Solutions. All rights reserved.                                         #
#                                                                         #
###########################################################################

#! /usr/bin/awk -f

#
# $Id: unicode.awk,v 1.13 2006/04/06 14:42:44 cvs Exp $
#

# This script converts UnicodeData.txt into a set of lookup tables 
# which are convenient to use when implementing the methods of
# java.util.Character.  
#
# The tables used to implement the JDK 1.1 methods isLetter(), isDigit(),
# etc. are output as C declarations:
#
#   Each array 'const w_byte character_class_row_<xy>[]' contains
#   256 bytes, each of which holds information concerning one
#   unicode character.  The high nibble contains the character
#   'class':
#     0   Character not defined (is unassigned)
#     1   Character is lower-case letter
#     2   Character is upper-case letter
#     3   Character is title-case letter
#     4   Character is some other kind of "letter"
#     5   Character is a digit
#     6   Character is whitespace
#     7   Character is formatting
#     8   Character is none of the above (but is defined)
#     >8  reserved, will not be output by this program.
#   For classes 1 and 2 the low nibble contains information
#   to enable upper case to be derived from lower or vice versa:
#     0   There is no corresponding other-case letter
#     i   (0<i<F) Use upper_to_lower_delta[i] : add it to the
#         upper-case to get the lower, or subtract it from the
#         lower-case to get the upper.
#     F   Use to_lower_case[][] or to_upper_case[][] (see below).
#
#   'const int upper_to_lower_delta []'
#     contains the "deltas" which cover most upper-to-lower case
#     conversions (and vice versa).  upper_to_lower_delta[0]
#     holds the number 'n' of deltas, upper_to_lower_delta[1..n] 
#     holds the deltas themselves.  
#
#   'const char *character_class[]' holds 256 pointers,
#   each pointing to 'character_class_row_<xy>[]' for some <xy>.
#   character_class[charcode/256][charcode%256] yields the
#   information byte corresponding to unicode character 'charcode'.
#
#   The two arrays 'const w_char to_lower_case[][]' and
#   'const w_char to_upper_case[][]' each hold a number
#   of pairs of characters, being respectively {upper,lower}
#   and {lower,upper}.  The pairs are stored in ascending
#   order, and are terminated by {0,0}.
#
#   The array 'const w_char to_title_case[][]' hold a number
#   of pairs of characters, each being {upper or lower case,title case}.
#   The pairs are stored in ascending order, and are terminated by {0,0}.
#
#   The methods introduced in JDK 1.2 and later provide for a more 
#   detailed classification of characters, which is not a straightforward
#   refinement of the JDK 1.1 classification.  Therefore we build
#   further tables which enable the general category and numeric 
#   value to be determined.
#
#   Class RuleBasedCollator needs to be able to look up a character's
#   canonical combining class and decomposition (canonical or not),
#   so we provide functions for this too.
#
#   Although the functions to perform these lookups all appear very
#   similar (they are all based on a binary search algorithm), there
#   are differences in the type of data they access (char, int, w_string
#   pointer) and in the result to be returned in the event of a `miss'.
#   Caveat optimisator!
#
# TODO:
#   Enable the UnicodeData.txt file to be filtered according to a list
#   of ``Subsets'': if a Subset is not included, all its characters are
#   treated as unassigned.  The Subsets marked with a `+' below will
#   always be included, even if they are not requested.
#
#   See Annex A of ISO 10646-1:2000.
#    1+  BASIC LATIN                        0020-007E
#    2+  LATIN-1 SUPPLEMENT                 00A0-00FF
#    3   LATIN EXTENDED-A                   0100-017F
#    4   LATIN EXTENDED-B                   0180-024F
#    5   IPA EXTENSIONS                     0250-02AF
#    6   SPACING MODIFIER LETTERS           02B0-02FF
#    7   COMBINING DIACRITICAL MARKS        0300-036F
#    8   BASIC GREEK                        0370-03CF
#    9   GREEK SYMBOLS AND COPTIC           03D0-03FF
#   10   CYRILLIC                           0400-04FF
#   11   ARMENIAN                           0530-058F
#   12   BASIC HEBREW                       05D0-05EA
#   13   HEBREW EXTENDED                    0590-05CF,05EB-05FF
#   14   BASIC ARABIC                       0600-065F
#   15   ARABIC EXTENDED                    0660-06FF
#   16   DEVANGARI                          0900-097F,200C,200D
#   17   BENGALI                            0980-09FF,200C,200D
#   18   GURMUKHI                           0A00-0A7F,200C,200D
#   19   GUJARATI                           0A80-0AFF,200C,200D
#   20   ORIYA                              0B00-0B7F,200C,200D
#   21   TAMIL                              0B80-0BFF,200C,200D
#   22   TELUGU                             0C00-0C7F,200C,200D
#   23   KANNADA                            0C80-0CFF,200C,200D
#   24   MALAYALAM                          0D00-0D7F,200C,200D
#   25   THAI                               0E00-0E7F
#   26   LAO                                0E80-0EFF
#   27   BASIC GEORGIAN                     10D0-10FF
#   28   GEORGIAN EXTENDED                  10A0-10CF
#   29   HANGUL JAMO                        1100-11FF
#   30   LATIN EXTENDED ADDITIONAL          1E00-1EFF
#   31   GREEK EXTENDED                     1F00-1FFF
#   32+  GENERAL PUNCTUATION                2000-206F
#   33+  SUPERSCRIPTS AND SUBSCRIPTS        2070-209F
#   34+  CURRENCY SYMBOLS                   20A0-20CF
#   35   COMBINING DIACRITICAL MARKS FOR SYMBOLS
#                                           20D0-20FF
#   36   LETTERLIKE SYMBOLS                 2100-214F
#   37   NUMBER FORMS                       2150-218F
#   38   ARROWS                             2190-21FF
#   39   MATHEMATICAL OPERATORS             2200-22FF
#   40   MISCELLANEOUS TECHNICAL            2300-23FF
#   41   CONTROL PICTURES                   2400-243F
#   42   OPTICAL CHARACTER RECOGNITION      2440-245F
#   43   ENCLOSED ALPHANUMERICS             2460-24FF
#   44   BOX DRAWING                        2500-257F
#   45   BLOCK ELEMENTS                     2580-259F
#   46   GEOMETRIC SHAPES                   25A0-25FF
#   47   MISCELLANEOUS SYMBOLS              2600-26FF
#   48   DINGBATS                           2700-27BF
#   49   CJK SYMBOLS AND PUNCTUATION        3000-303F
#   50   HIRAGANA                           3040-309F
#   51   KATAKANA                           30A0-30FF
#   52   BOPOMOFO                           3100-312F,31A0-31BF
#   53   HANGUL COMPATIBILTY JAMO           3130-318F
#   54   CJK MISCELLANEOUS                  3190-319F
#   55   ENCLOSED CJK LETTERS AND MONTHS    3200-32FF
#   56   CJK COMPATIBILTY                   3300-33FF
#   60   CJK UNIFIED IDEOGRAPHS             4E00-9FFF
#   61   PRIVATE USE AREA                   E000-F8FF
#   62   CJK COMPATIBILTY IDEOGRAPHS        F900-FAFF
#   63   ALPHABETIC PRESENTATION FORMS      FB00-FB4F
#   64   ARABIC PRESENTATION FORMS-A        FB50-FDFF
#   65   COMBINING HALF-MARKS               FE20-FE2F
#   66   CJK COMPATIBILTY FORMS             FE30-FE4F
#   67   SMALL FORM VARIANTS                FE50-FE6F
#   68   ARABIC PRESENTATION FORMS-B        FE70-FEFE
#   69   HALFWIDTH AND FULLWIDTH FORMS      FF00-FFEF
#   70   SPECIALS                           FFF0-FFFD
#   71   HANGUL SYLLABLES                   AC00-D7A3
#   72   BASIC TIBETAN                      0F00-0FBF
#   73   ETHIOPIC                           1200-137F
#   74   UNIFIED CANADIAN ABORIGINAL SYLLABICS
#                                           1400-167F
#   75   CHEROKEE                           13A0-13FF
#   76   YI SYLLABLES                       A000-A48F
#   77   YI RADICALS                        A490-A4CF
#   78   KANGXI RADICALS                    2F00-2FDF
#   79   CJK RADICALS SUPPLEMENT            2E80-2EFF
#   80   BRAILLE PATTERNS                   2800-28FF
#   81   CJK UNIFIED IDEOGRAPHS EXTENSION A 3400-4DBF
#   82   OGHAM                              1680-169F
#   83   RUNIC                              16A0-16FF
#   84   SINHALA                            0D80-0DFF
#   85   SYRIAC                             0700-074F
#   86   THAANA                             0780-07BF
#   87   BASIC MYANMAR                      1000-104F,200C,200D
#   88   KHMER                              1780-17FF,200C,200D
#   89   MONGOLIAN                          1800-18AF
#   90   EXTENDED MYANMAR                   1050-109F
#   91   TIBETAN                            0F00-0FFF
#  200   ZERO-WIDTH BOUNDARY INDICATORS     200B-200D,FEFF
#  201   FORMAT SEPARATORS                  2028-2029
#  202   BI-DIRECTIONAL FORMAT MARKS        200E-200F
#  203   BI-DIRECTIONAL FORMAT EMBEDDINGS   202A-202E
#  204   HANGUL FILL CHARACTERS             3164,FFA0
#  205   CHARACTER SHAPING SELECTORS        206A-206D
#  206   NUMERIC SHAPE SELECTORS            206E-206F
#  207   IDEOGRAPHIC DESCRIPTION CHARACTERS 2FF0-2FFF
# To this list we add two special values:
#  999   (Wildcard)                         0000-FFFD
#  0     (Minimal)                          0020-007E,00A0-00FF,2000-20CF
# These special values should always be used alone, e.g. blox=999 or blox=0

function select_subsets() {
  subsets[1] = 1
  subsets[2] = 1
  subsets[32] = 1
  subsets[33] = 1
  subsets[34] = 1

  if (blox) {
    n = split(blox, blocks, ":")
    for (i=1; i<=n; ++i) {
      j = 0 + blocks[i]
      if (j) subsets[j] = 1
    }
  }

  if (subsets[999]) {
    print "/* Including support for all Unicode characters */"
  }
  else for (i=1; i<999; ++i) {
    if (subsets[i]) {
      print "/* Including support for " subset_name[i] " */"
    }
  }
  print ""
}

function supported(codenum) {
  hex0180 = 384
  hex0250 = 512 + 5 * 16
  hex02b0 = 512 + 11 * 16
  hex0300 = 768
  hex0370 = 768 + 7 * 16
  hex03d0 = 768 + 13 * 16
  hex0400 = 1024
  hex0500 = 1280
  hex0530 = 1280 + 3 * 16
  hex0590 = 1280 + 9 * 16
  hex05d0 = 1280 + 13 * 16
  hex05eb = 1280 + 14 * 16 + 11
  hex0600 = 6 * 256
  hex0660 = 6 * 256 + 6 * 16
  hex0700 = 7 * 256
  hex0750 = 7 * 256 + 5 * 16
  hex0780 = 7 * 256 + 8 * 16
  hex07c0 = 7 * 256 + 12 * 16
  hex0900 = 9 * 256
  hex0980 = 9 * 256 + 8 * 16
  hex0a00 = 10 * 256
  hex0a80 = 10 * 256 + 8 * 16
  hex0b00 = 11 * 256
  hex0b80 = 11 * 256 + 8 * 16
  hex0c00 = 12 * 256
  hex0c80 = 12 * 256 + 8 * 16
  hex0d00 = 13 * 256
  hex0d80 = 13 * 256 + 8 * 16
  hex0e00 = 14 * 256
  hex0e80 = 14 * 256 + 8 * 16
  hex0f00 = 15 * 256
  hex0fc0 = 15 * 256 + 12 * 16
  hex1000 = 4096
  hex1050 = 4096 + 5 * 16
  hex10a0 = 4096 + 10 * 16
  hex10d0 = 4096 + 13 * 16
  hex1100 = 4096 + 256
  hex1200 = 4096 + 2 * 256
  hex1380 = 4096 + 3 * 256 + 8 * 16
  hex13a0 = 4096 + 3 * 256 + 10 * 16
  hex1400 = 4096 + 4 * 256
  hex1680 = 4096 + 6 * 256 + 8 * 16
  hex16a0 = 4096 + 6 * 256 + 10 * 16
  hex1700 = 4096 + 7 * 256
  hex1780 = 4096 + 7 * 256 + 8 * 16
  hex1800 = 3 * 2048
  hex18b0 = 3 * 2048 + 11 * 16
  hex1e00 = 30 * 256
  hex1f00 = 31 * 256
  hex2000 = 8192
  hex20d0 = 8192 + 13 * 16
  hex2100 = 8192 + 256
  hex2150 = 8192 + 256 + 5 * 16
  hex2190 = 8192 + 256 + 9 * 16
  hex2200 = 8192 + 2 * 256
  hex2300 = 8192 + 3 * 256
  hex2400 = 8192 + 4 * 256
  hex2460 = 8192 + 4 * 256 + 6 * 16
  hex2500 = 8192 + 5 * 256
  hex2600 = 8192 + 6 * 256
  hex2700 = 8192 + 7 * 256
  hex2580 = 8192 + 5 * 256 + 8 * 16
  hex25a0 = 8192 + 5 * 256 + 10 * 16
  hex27c0 = 2 * 4096 + 7 * 256 + 12 * 16
  hex2800 = 2 * 4096 + 8 * 256
  hex2900 = 2 * 4096 + 9 * 256
  hex2e80 = 2 * 4096 + 14 * 256 + 8 * 16
  hex2f00 = 2 * 4096 + 15 * 256
  hex2fe0 = 2 * 4096 + 15 * 256 * 14 * 16
  hex2ff0 = 2 * 4096 + 15 * 256 * 15 * 16
  hex3000 = 3 * 4096
  hex3040 = 3 * 4096 + 4 * 16
  hex30a0 = 3 * 4096 + 10 * 16
  hex3100 = 3 * 4096 + 256
  hex3130 = 3 * 4096 + 256 + 3 * 256
  hex3164 = 3 * 4096 + 256 + 100
  hex3190 = 3 * 4096 + 256 + 9 * 256
  hex31a0 = 3 * 4096 + 256 + 10 * 256
  hex31c0 = 3 * 4096 + 256 + 12 * 256
  hex3200 = (3 * 16 + 2) * 256
  hex3300 = (3 * 16 + 3) * 256
  hex3400 = (3 * 16 + 4) * 256
  hex4dc0 = (4 * 16 + 13) * 256 + 12 * 16
  hex4e00 = (4 * 16 + 14) * 256
  hexa400 = (10 * 16 + 12) * 256
  hexa490 = (10 * 16 + 12) * 256 + 9 * 16
  hexa4d0 = (10 * 16 + 12) * 256 + 13 * 16
  hexac00 = (10 * 16 + 12) * 256
  hexd7a4 = (13 * 16 + 7) * 256 + 10 * 16 + 4
  hexf900 = 249 * 256
  hexfb00 = 251 * 256
  hexfb50 = 251 * 256 + 80
  hexfe00 = 254 * 256
  hexfe20 = 254 * 256 + 32
  hexfe30 = 254 * 256 + 48
  hexfe50 = 254 * 256 + 80
  hexfe70 = 254 * 256 + 112
  hexfeff = 254 * 256 + 255
  hexffa0 = 255 * 256 + 10 * 16
  hexfff0 = 255 * 256 + 15 * 16
  hexfffe = 65534
  
  # Special cases
  if (subsets[999] || codenum < 256) {
    return 1;
  }

  # Special case: 2000..20CF
  if (codenum >= hex2000 && codenum < hex20d0) {
    return 1;
  }

  if (codenum < hex2000) {
    if (codenum < hex0900) {
      if (codenum < hex0400) {
        if (codenum < hex0300) {
          # Subsets 1, 2 (0x0000..0x0100) are always supported.
          if (codenum < hex0180) {
	    # 0100..017F LATIN EXTENDED-A
            return subsets[3]
          }
          else if (codenum < hex0250) {
	    # 0180..024F LATIN EXTENDED-B 
            return subsets[4]
          }
          else if (codenum < hex02b0) {
	    # 0250..02AF IPA EXTENSIONS
            return subsets[5]
          }
          else {
	    # 02B0..02FF SPACING MODIFIER LETTERS
            return subsets[6]
          }
        }
        else { # 0x0300..0x03ff
          if (codenum < hex0370) {
	    # 0300..036F COMBINING DIACRITICAL MARKS
            return subsets[7]
          }
          else if (codenum < hex03d0) {
	    # 0370..03CF BASIC GREEK
            return subsets[8]
          }
          else {
	    # 03D0..03FF GREEK SYMBOLS AND COPTIC
            return subsets[9]
          }
        }
      }
      else { # 0x0400..0x8ff
        if (codenum < hex05d0) {
          if (codenum < hex0500) {
	    # 0400..04FF CYRILLIC
            return subsets[10]
          }
	  else if (codenum < hex0530) {
	    return 0
	  }
          else if (codenum < hex0590) {
	    # 0530..058F ARMENMIAN
            return subsets[11]
          }
          else {
	    # 0590..05CF HEBREW EXTENDED
            return subsets[13]
          }
        }
        else if (codenum < hex0600) {
          if (codenum < hex05eb) {
	    # 05D0..05EA BASIC HEBREW
            return subsets[12]
          }
          else {
	    # 05EB..05FF HEBREW EXTENDED
            return subsets[13]
          }
        }
        else { # 0x0600..0x08ff
          if (codenum < hex0660) {
	    # 0600..065F BASIC ARABIC
            return subsets[14]
          }
          else if (codenum < hex0700) {
	    # 0660..06FF ARABIC EXTENDED
            return subsets[15]
          }
          else if (codenum < hex0750) {
	    # 0700..074F SYRIAC
            return subsets[85]
          }
	  else if (codenum < hex0780) {
	    return 0
	  }
          else if (codenum < hex07c0) {
	    # 0780..07BF THAANA
            return subsets[86]
          }
          else {
	    # 07C0..08FF
            return 0
          }
        }
      }
    }
    else { # 0x0900..0x1fff
      if (codenum < hex0e00) {
        if (codenum < hex0a00) {
          if (codenum < hex0980) {
	    # 0900..097F DEVANGARI
            return subsets[16]
          }
          else {
	    # 0980..09FF BENGALI
            return subsets[17]
          }
        }
        else if (codenum < hex0b00) {
          if (codenum < hex0a80) {
	    # 0A00..0A7F GURMUKHI
            return subsets[18]
          }
          else {
	    # 0A80..0AFF GUJARATI
            return subsets[19]
          }
        }
        else if (codenum < hex0c00) {
          if (codenum < hex0b80) {
	    # 0B00..0B7F ORIYA
            return subsets[20]
          }
          else {
	    # 0B80..0BFF TAMIL
            return subsets[21]
          }
        }
        else if (codenum < hex0d00) {
          if (codenum < hex0c80) {
	    # 0C00..0C7F TELUGU
            return subsets[22]
          }
          else {
	    # 0C80..0CFF KANNADA
            return subsets[23]
          }
        }
        else { # 0x0d00..0x0dff
          if (codenum < hex0d80) {
	    # 0D00..0D7F MALAYALAM
            return subsets[24]
          }
          else {
	    # 0D80..0DFF SINHALA
            return subsets[84]
          }
        }
      }
      else { # 0x0e00..0x1fff
        if (codenum < hex1000) {
          if (codenum < hex0e80) {
	    # 0E00..0E7F THAI
            return subsets[25]
          }
          else if (codenum < hex0f00) {
	    # 0E80..0EFF LAO
            return subsets[26]
          }
          else {
	    # 0F00..0FFF TIBETAN
            # 0F00..0FBF BASIC TIBETAN
            return subsets[91] || ((codenum < hex0fc0) && subsets[72])
          }
        }
        else if (codenum < hex1800) {
          if (codenum < hex1050) {
	    # 1000..104F BASIC MYANMAR
            return subsets[87]
          }
          else if (codenum < hex10a0) {
	    # 1050..109F EXTENDED MYANMAR
            return subsets[90]
          }
          else if (codenum < hex10d0) {
	    # 10A0..10CF GEORGIAN EXTENDED
            return subsets[28]
          }
          else if (codenum < hex1100) {
	    # 10D0..10FF BASIC GEORGIAN
            return subsets[27]
          }
          else if (codenum < hex1200) {
	    # 1100..11FF HANGUL JAMO
            return subsets[29]
          }
          else if (codenum < hex1380) {
	    # 1200..137F ETHIOPIC
            return subsets[73]
          }
	  else if (codenum < hex13a0) {
	    return 0
	  }
          else if (codenum < hex1400) {
	    # 13A0..13FF CHEROKEE
            return subsets[75]
          }
          else if (codenum < hex1680) {
	    # 1400..167F UNIFIED CANADIAN ABORIGINAL SYLLABICS
            return subsets[74]
          }
          else if (codenum < hex16a0) {
	    # 1680..169F OGHAM
            return subsets[82]
          }
          else if (codenum < hex1700) {
	    # 16A0..16FF RUNIC
            return subsets[83]
          }
          else if (codenum < hex1780) {
	    # 1700..177F
            return 0;
          }
          else { # 0x1780..0x17ff
	    # 1780..17FF KHMER
            return subsets[88]
          }
        }
        else { # 0x1800..0x1fff
          if (codenum < hex18b0) {
	    # 1800..18AF MONGOLIAN
            return subsets[89]
          }
          if (codenum < hex1e00) {
	    # 18B0..1DFF
            return 0
          }
          else if (codenum < hex1f00) {
	    # 1E00..1EFF LATIN EXTENDED ADDITIONAL
            return subsets[30]
          }
          else { # 0x1f00..0x1fff
            # 1F00..1FFF GREEK EXTENDED
            return subsets[31]
          }
        }
      }
    }
  }
  else { # 0x2000..
    if (codenum < hex3000) {
      if (codenum < (36 * 256)) {
        # Subsets 32--34 (0x2000..0x20cf) are always supported.
        if (codenum < hex2100) {
	  # 20D0..20FF COMBINING DIACRITICAL MARKS FOR SYMBOLS
          return subsets[35]
        }
        else if (codenum < (hex2150)) {
	  # 2100..214F LETTERLIKE SYMBOLS
          return subsets[36]
        }
        else if (codenum < hex2190) {
	  # 2150..218F NUMBER FORMS
          return subsets[37]
        }
        else if (codenum < hex2200) {
	  # 2190..21FF ARROWS
          return subsets[38]
        }
        else if (codenum < hex2300) {
	  # 2200..22FF MATHEMATICAL OPERATORS
          return subsets[39]
        }
        else {
	  # 2300..23FF MISCELLANEOUS TECHNICAL
          return subsets[40]
        }
      }
      else { # 0x2400..0x2fff
        if (codenum < hex2440) {
	  # 2400..243F CONTROL PICTURES
          return subsets[41]
        }
        else if (codenum < hex2460) {
	  # 2440..245F OPTICAL CHARACTER RECOGNITION
          return subsets[42]
        }
        else if (codenum < hex2500) {
	  # 2460..24FF ENCLOSED ALPHANUMERICS
          return subsets[43]
        }
        else if (codenum < hex2580) {
	  # 2500..257F BOX DRAWING
          return subsets[44]
        }
        else if (codenum < hex25a0) {
	  # 2580..259F BLOCK ELEMENTS
          return subsets[45]
        }
        else if (codenum < hex2600) {
	  # 25A0..25FF GEOMETRIC SHAPES
          return subsets[46]
        }
        else if (codenum < hex2700) {
	  # 2600..26FF MISCELLANEOUS SYMBOLS
          return subsets[47]
        }
        else if (codenum < hex27c0) {
	  # 2700..27BF DINGBATS
          return subsets[48]
        }
        else if (codenum < hex2800) {
	  # 27C0..27FF
          return 0
        }
        else if (codenum < hex2900) {
	  # 2800..28FF BRAILLE PATTERNS
          return subsets[80]
        }
        else if (codenum < hex2e80) {
	  # 2900..2E7F
          return 0
        }
        else if (codenum < 2f00) {
	  # 2E80..2EFF CJK RADICALS SUPPLEMENT
          return subsets[79]
        }
        else if (codenum < hex2fe0) {
	  # 2F00..2FDF KANGXI RADICALS
          return subsets[78]
        }
        else if (codenum < hex2ff0) {
	  # 2FE0..2FEF
          return 0
        }
        else {
	  # 2FF0..2FFF IDEOGRAPHIC DESCRIPTION CHARACTERS 2FF0-2FFF
          return subsets[207]
        }
      }
    }
    else { # 0x3000 ..
      if (codenum < (14 * 4096)) {
        if (codenum < hex3040) {
	  # 3000..303F CJK SYMBOLS AND PUNCTUATION
          return subsets[49]
        }
        else if (codenum < hex30a0) {
	  # 3040..309F HIRAGANA
          return subsets[50]
        }
        else if (codenum < hex3100) {
	  # 30A0..30FF KATAKANA
          return subsets[51]
        }
        else if (codenum < hex3130) {
	  # 3100..312F BOPOMOFO
	  # 3164       HANGUL FILL CHARACTERS
          return subsets[52] || (codenum == hex3164 && subsets[204])
        }
        else if (codenum < hex3190) {
	  # 3130..318F HANGUL COMPATIBILTY JAMO
          return subsets[53]
        }
        else if (codenum < hex31a0) {
	  # 3190..319F CJK MISCELLANEOUS
          return subsets[54]
        }
        else if (codenum < hex31c0) {
	  # 31A0..31BF BOPOMOFO
          return subsets[52]
        }
        else if (codenum < hex3200) {
	  # 31C0..31FF
          return 0
        }
        else if (codenum < hex3300) {
	  # 3200..32FF ENCLOSED CJK LETTERS AND MONTHS
          return subsets[55]
        }
        else if (codenum < hex3400) {
	  # 3300..33FF CJK COMPATIBILTY
          return subsets[56]
        }
        else if (codenum < hex4dc0) {
	  # 3400..4DBF CJK UNIFIED IDEOGRAPHS EXTENSION A
          return subsets[81]
        }
        else if (codenum < hex4e00) {
	  # 4DC0..4DFF
          return 0
        }
        else if (codenum < hexa000) {
	  # 4E00..9FFF CJK UNIFIED IDEOGRAPHS
          return subsets[60]
        }
        else if (codenum < hexa490) {
	  # A000..A48F YI SYLLABLES
          return subsets[76]
        }
        else if (codenum < hexa4d0) {
	  # A490..A4CF YI RADICALS
          return subsets[77]
        }
        else if (codenum < hexac00) {
	  # A4D0..ABFF
          return 0
        }
        else if (codenum < hexd7a4) {
	  # AC00..D7A3 HANGUL SYLLABLES
          return subsets[71]
        }
        else {
          return 0
        }
      }
      else { # 0xe000..
        if (codenum < hexfe00) {
          if (codenum < hexf900) {
	    # E000..F8FF PRIVATE USE AREA
            return subsets[61]
          }
          else if (codenum < hexfb00) {
	    # F900..FAFF CJK COMPATIBILTY IDEOGRAPHS
            return subsets[62]
          }
          else if (codenum < hexfb50) {
	    # FB00..FB4F ALPHABETIC PRESENTATION FORMS
            return subsets[63]
          }
          else {
	    # FB50..FDFF ARABIC PRESENTATION FORMS-A
            return subsets[64]
          }
        }
        else { #0xfe00..
          if (codenum < hexfe20) {
	    # FE00..FE1F
            return 0
          }
          else if (codenum < hexfe30) {
	    # FE20..FE2F COMBINING HALF-MARKS
            return subsets[65]
          }
          else if (codenum < hexfe50) {
	    # FE30..FE4F CJK COMPATIBILTY FORMS
            return subsets[66]
          }
          else if (codenum < hexfe70) {
	    # FE50..FE6F SMALL FORM VARIANTS
            return subsets[67]
          }
          else if (codenum < hexfeff) {
	    # FE70..FEFE ARABIC PRESENTATION FORMS-B
            return subsets[68]
          }
	  else if (codenum == hexfeff) {
	    # FEFF       ZERO-WIDTH BOUNDARY INDICATORS
	    return subsets[200]
	  }
          else if (codenum < hexfff0) {
	    # FF00..FFEF HALFWIDTH AND FULLWIDTH FORMS
            # FFA0       HANGUL FILL CHARACTERS
            return subsets[69] || ((codenum == hexffa0) && subsets[204])
          }
          else if (codenum < hexfffe) {
	    # FFF0..FFFD SPECIALS
            return subsets[70]
          }
        }
      }
    }
  }

  return 0;
}

# Append an item to a list.  All items except the first are preceded by ``, ''.
# We add tabs and newlines in an attempt to produce something human-readable.
function appendtolist(list,new) {
  itemlen=length(new)+3
  itemsperline=int(80/itemlen)
  if(list=="") return "\t" comment new
  else if(int((length(list)+itemlen)/80) > int(length(list)/80)) return list",\n\t" comment new
  else return list",  " comment new
}

# Optional information provided if 'debug' is defined
# (e.g. run with -v debug=1).

function debugoutput(number,name,class,subclass,uce,lce,tce) {
      debugline=sprintf("class %s (%s) subclass %X ",class,classname[class],subclass)
      if(uce) debugline=debugline sprintf("upper is %s ",uce)
      if(lce) debugline=debugline sprintf("lower is %s ",lce)
      if(tce) debugline=debugline sprintf("title is %s ",tce)
      printf "/* %04X %32s %s */\n",number,name,debugline
    }

# Build up the classtable as rows of 256 bytes each, where each byte holds
# a class (high nibble) and subclass (low nibble).  On completing each
# row, check to see if it's a duplicate of one we made earlier; if it
# is, re-use the earlier row.

function buildtableentry(n,class,subclass) {
    currentrow=appendtolist(currentrow,sprintf("0x%x%x",class,subclass))

    m=n%256
    if(m==255) {
      r=(n-m)/256;

      dup=0
      for(s=0;s<r;++s) if(row[s]==currentrow) { dup=1; break }

      if(dup) {
        table[r]=s
      } else {
        row[r]=currentrow
        table[r]=r
      }
      currentrow=""
    }
}

BEGIN {
  FS=";"
  debug = 1

  subset_name[  1] = "BASIC LATIN                            "
  subset_name[  2] = "LATIN-1 SUPPLEMENT                     "
  subset_name[  3] = "LATIN EXTENDED-A                       "
  subset_name[  4] = "LATIN EXTENDED-B                       "
  subset_name[  5] = "IPA EXTENSIONS                         "
  subset_name[  6] = "SPACING MODIFIER LETTERS               "
  subset_name[  7] = "COMBINING DIACRITICAL MARKS            "
  subset_name[  8] = "BASIC GREEK                            "
  subset_name[  9] = "GREEK SYMBOLS AND COPTIC               "
  subset_name[ 10] = "CYRILLIC                               "
  subset_name[ 11] = "ARMENIAN                               "
  subset_name[ 12] = "BASIC HEBREW                           "
  subset_name[ 13] = "HEBREW EXTENDED                        "
  subset_name[ 14] = "BASIC ARABIC                           "
  subset_name[ 15] = "ARABIC EXTENDED                        "
  subset_name[ 16] = "DEVANGARI                              "
  subset_name[ 17] = "BENGALI                                "
  subset_name[ 18] = "GURMUKHI                               "
  subset_name[ 19] = "GUJARATI                               "
  subset_name[ 20] = "ORIYA                                  "
  subset_name[ 21] = "TAMIL                                  "
  subset_name[ 22] = "TELUGU                                 "
  subset_name[ 23] = "KANNADA                                "
  subset_name[ 24] = "MALAYALAM                              "
  subset_name[ 25] = "THAI                                   "
  subset_name[ 26] = "LAO                                    "
  subset_name[ 27] = "BASIC GEORGIAN                         "
  subset_name[ 28] = "GEORGIAN EXTENDED                      "
  subset_name[ 29] = "HANGUL JAMO                            "
  subset_name[ 30] = "LATIN EXTENDED ADDITIONAL              "
  subset_name[ 31] = "GREEK EXTENDED                         "
  subset_name[ 32] = "GENERAL PUNCTUATION                    "
  subset_name[ 33] = "SUPERSCRIPTS AND SUBSCRIPTS            "
  subset_name[ 34] = "CURRENCY SYMBOLS                       "
  subset_name[ 35] = "COMBINING DIACRITICAL MARKS FOR SYMBOLS"
  subset_name[ 36] = "LETTERLIKE SYMBOLS                     "
  subset_name[ 37] = "NUMBER FORMS                           "
  subset_name[ 38] = "ARROWS                                 "
  subset_name[ 39] = "MATHEMATICAL OPERATORS                 "
  subset_name[ 40] = "MISCELLANEOUS TECHNICAL                "
  subset_name[ 41] = "CONTROL PICTURES                       "
  subset_name[ 42] = "OPTICAL CHARACTER RECOGNITION          "
  subset_name[ 43] = "ENCLOSED ALPHANUMERICS                 "
  subset_name[ 44] = "BOX DRAWING                            "
  subset_name[ 45] = "BLOCK ELEMENTS                         "
  subset_name[ 46] = "GEOMETRIC SHAPES                       "
  subset_name[ 47] = "MISCELLANEOUS SYMBOLS                  "
  subset_name[ 48] = "DINGBATS                               "
  subset_name[ 49] = "CJK SYMBOLS AND PUNCTUATION            "
  subset_name[ 50] = "HIRAGANA                               "
  subset_name[ 51] = "KATAKANA                               "
  subset_name[ 52] = "BOPOMOFO                               "
  subset_name[ 53] = "HANGUL COMPATIBILTY JAMO               "
  subset_name[ 54] = "CJK MISCELLANEOUS                      "
  subset_name[ 55] = "ENCLOSED CJK LETTERS AND MONTHS        "
  subset_name[ 56] = "CJK COMPATIBILTY                       "
  subset_name[ 60] = "CJK UNIFIED IDEOGRAPHS                 "
  subset_name[ 61] = "PRIVATE USE AREA                       "
  subset_name[ 62] = "CJK COMPATIBILTY IDEOGRAPHS            "
  subset_name[ 63] = "ALPHABETIC PRESENTATION FORMS          "
  subset_name[ 64] = "ARABIC PRESENTATION FORMS-A            "
  subset_name[ 65] = "COMBINING HALF-MARKS                   "
  subset_name[ 66] = "CJK COMPATIBILTY FORMS                 "
  subset_name[ 67] = "SMALL FORM VARIANTS                    "
  subset_name[ 68] = "ARABIC PRESENTATION FORMS-B            "
  subset_name[ 69] = "HALFWIDTH AND FULLWIDTH FORMS          "
  subset_name[ 70] = "SPECIALS                               "
  subset_name[ 71] = "HANGUL SYLLABLES                       "
  subset_name[ 72] = "BASIC TIBETAN                          "
  subset_name[ 73] = "ETHIOPIC                               "
  subset_name[ 74] = "UNIFIED CANADIAN ABORIGINAL SYLLABICS  "
  subset_name[ 75] = "CHEROKEE                               "
  subset_name[ 76] = "YI SYLLABLES                           "
  subset_name[ 77] = "YI RADICALS                            "
  subset_name[ 78] = "KANGXI RADICALS                        "
  subset_name[ 79] = "CJK RADICALS SUPPLEMENT                "
  subset_name[ 80] = "BRAILLE PATTERNS                       "
  subset_name[ 81] = "CJK UNIFIED IDEOGRAPHS EXTENSION A     "
  subset_name[ 82] = "OGHAM                                  "
  subset_name[ 83] = "RUNIC                                  "
  subset_name[ 84] = "SINHALA                                "
  subset_name[ 85] = "SYRIAC                                 "
  subset_name[ 86] = "THAANA                                 "
  subset_name[ 87] = "BASIC MYANMAR                          "
  subset_name[ 88] = "KHMER                                  "
  subset_name[ 89] = "MONGOLIAN                              "
  subset_name[ 90] = "EXTENDED MYANMAR                       "
  subset_name[ 91] = "TIBETAN                                "
  subset_name[200] = "ZERO-WIDTH BOUNDARY INDICATORS         "
  subset_name[201] = "FORMAT SEPARATORS                      "
  subset_name[202] = "BI-DIRECVTIONAL FORMAT MARKS           "
  subset_name[203] = "BI-DIRECTIONAL FORMAT EMBEDDINGS       "
  subset_name[204] = "HANGUL FILL CHARACTERS                 "
  subset_name[205] = "CHARACTER SHAPING SELECTORS            "
  subset_name[206] = "NUMERIC SHAPE SELECTORS                "
  subset_name[207] = "IDEOGRAPHIC DESCRIPTION CHARACTERS     "
  select_subsets()

#      DIRECTIONALITY_UNDEFINED = -1;
# AL   DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;
# AN   DIRECTIONALITY_ARABIC_NUMBER 6
# BN   DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;
# CS   DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;
# EN   DIRECTIONALITY_EUROPEAN_NUMBER = 3;
# ES   DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;
# ET   DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;
# L    DIRECTIONALITY_LEFT_TO_RIGHT = 0;
# LRE  DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;
# LRO  DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;
# NSM  DIRECTIONALITY_NONSPACING_MARK = 8;
# ON   DIRECTIONALITY_OTHER_NEUTRALS = 13;
# B    DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;
# PDF  DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
# R    DIRECTIONALITY_RIGHT_TO_LEFT = 1;
# RLE  DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;
# RLO  DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;
# S    DIRECTIONALITY_SEGMENT_SEPARATOR = 11;
# WS   DIRECTIONALITY_WHITESPACE = 12;

  dirvalue["L"]   = 0;
  dirvalue["R"]   = 1;
  dirvalue["AL"]  = 2;
  dirvalue["EN"]  = 3;
  dirvalue["ES"]  = 4;
  dirvalue["ET"]  = 5;
  dirvalue["AN"]  = 6;
  dirvalue["CS"]  = 7;
  dirvalue["NSM"] = 8;
  dirvalue["BN"]  = 9;
  dirvalue["B"]   = 10;
  dirvalue["S"]   = 11;
  dirvalue["WS"]  = 12;
  dirvalue["ON"]  = 13;
  dirvalue["LRE"] = 14;
  dirvalue["LRO"] = 15;
  dirvalue["RLE"] = 16;
  dirvalue["RLO"] = 17;
  dirvalue["PDF"] = 18;

# debug=1;

# Initialise numcompats to 0, otherwise it we end up generating a subscript`[]'.
# But we initialize numcats to 1, so that we can tell that the first entry to
# be added to catindex is there (otherwise it would be 0, which looks like
# no entry).  Later we will use category 0 to mean `unassigned character code'.
# Hack hack hack ...
  numcompats = 0
  numcats = 1

# Fields of UnicodeData.txt
# Note that these are 1 greater than in the Unicode Standard,
# because awk numbers fields starting from 1.

  codefield=1
  namefield=2
  catfield=3
  combifield=4
  dirfield=5
  decompfield=6
  digitvalfield=7
  numericvalfield=9
  mirrorfield=10
  ucefield=13  
  lcefield=14
  tcefield=15

# Our character classes 

  class_undefined = 0
  class_lowercase = 1
  class_uppercase = 2
  class_titlecase = 3
  class_other_letter = 4
  class_digit = 5
  class_whitespace = 6
  class_format = 7
  class_miscellaneous = 8

# Names of our character classes, for debugging purposes

  classname[class_undefined]="undefined"
  classname[class_lowercase]="lowercase"
  classname[class_uppercase]="uppercase"
  classname[class_titlecase]="titlecase"
  classname[class_mod_letter]="mod.lettr"
  classname[class_other_letter]="misclettr"
  classname[class_digit]="digit    "
  classname[class_whitespace]="whitespce"
  classname[class_miscellaneous]="other    "

# Some common differences between uppercase and lowercase forms
# (lowercase = uppercase + delta)

  delta[1]=1
  delta[2]=2
  delta[3]=16
  delta[4]=32
  delta[5]=48
  delta[6]=26
  delta[7]=80
  delta[8]=-112
  delta[9]=-8
  numdeltas=9
  
# Emit our #include's

  print "#include \"wonka.h\""
  print "#include \"hashtable.h\""
  print "#include \"wstrings.h\""
  print "#include \"unicode.h\""
  print " "

}

{
# We force 'code' to be treated as a string, by prefixing "" 
# (otherwise e.g. 2000 would be treated as a decimal number).

  code=""$codefield

# Handle a gap in the sequence of codes.  There are two kinds of gap:
# empty ones and full ones.  Empty gaps represent unassigned codes,
# full ones represent a block of characters with the same name and 
# properties.  

  if(blockname) {
    name=blockname # class and subclass remain same throughout block
  }
  else {
    name=""
    class=class_undefined; subclass=0
  }

  while(sprintf("%04X",number)!=code) {
    if(debug) debugoutput(number,name,class,subclass);

    buildtableentry(number,class,subclass);

    ++number
  }

# If not in a supported subset, build a (null) table entry and skip the rest.

  if(!supported(number)) {
    if(debug) debugoutput(number,name,0,0);

    buildtableentry(number,0,0);

    ++number;

    next;

  }

# First challenge: deal with the blocks.  A block appears in the table as 
# two entries named <foo, First> and <foo, Last>.  We treat every entry 
# from first to last as if it were named <foo>.

  startblock=index($namefield,", First");
  stopblock=index($namefield,", Last");

  if(startblock) blockname=substr($namefield,1,startblock-1) substr(name,startblock+7)

  if(blockname) name=blockname
  else name=$namefield

  if(stopblock) blockname=""

# Get the digit and/or numerical values and the combining class, if nonzero. 
# Build lists in ascending order of character code.

  digitval=""$digitvalfield;
  if (digitval != "") {
    digitvalues=appendtolist(digitvalues,"{0x"code","digitval"}")
    ++numdigitvalues
  }

  numericval=""$numericvalfield;
  if (numericval != "") {
    if (index(numericval,"/") || index(numericval,"-")) {
      numericval = "-2"
    }
    numericvalues=appendtolist(numericvalues,"{0x"code","numericval"}")
    ++numnumericvalues
  }

  if ($combifield) {
    combiclass[number] = $combifield
    combilist=appendtolist(combilist,"{0x"code","$combifield"}")
    ++numcombis
  }

  category = $catfield;
  directionality = $dirfield;
  mirrored = $mirrorfield;
  uce=""$ucefield
  lce=""$lcefield
  tce=""$tcefield

  if (!catindex[category]) {
    catindex[category] = numcats;
    ++numcats
  }

  if (category != prevcat) {
    catlist = appendtolist(catlist,"{0x"code","catindex[category]"}")
    prevcat = category;
    ++numcatpairs
  }

  if (directionality != prevdir) {
    thisdir = (directionality in dirvalue) ? dirvalue[directionality] : "-1"
    dirlist = appendtolist(dirlist,"{0x"code","thisdir"}")
    prevdir = directionality;
    ++numdirpairs
  }

  if (mirrored == "Y") {
    mirrorlist = appendtolist(mirrorlist,"0x"code)
    ++nummirrors
  }

  class=class_undefined; subclass=0;

# Evidence from the general category code of the character.

  letterCat = index(category,"L") == 1;
  spaceCat = index(category,"Z") == 1;

# Even the decomposition field can be of interest ..
  nobreakDecomp = index($decompfield,"<noBreak>") == 1;

# This is where the classification proper begins.
# -----------------------------------------------

# If category is "Ll", classify as "lower-case letter".
  if (category == "Ll") {
    class=class_lowercase
  }

# If category is "Lu", classify as "upper-case letter".
  else if (category == "Lu") {
    class=class_uppercase
  }

# If category is "Lt", classify as "title-case letter".
  else if (category == "Lt") {
    class=class_titlecase;
    if (!uce) uce = code
  }

# If category is "Nd", classify as "digit".
  else if (category == "Nd") {
	class=class_digit;
        subclass=digitval
  }

# If category is "Cf", classify as "format".
  else if (category == "Cf") {
	class=class_format;
  }

# Implement the Java rules for classification as "whitespace"
  else if ((number >= 9 && number <=13) || (number >= 28 && number <= 31) || (spaceCat && !nobreakDecomp)) {
        class=class_whitespace;
  }

# Anything defined and not yet classified is either a "miscellaneous letter" or "other".

  if(class==class_undefined && name) {
    if(letterCat) {
        class=class_other_letter;
    } else class=class_miscellaneous;
  }

# If a lower-case equivalent is specified, either set the subclass to an index
# into upper_to_lower_delta[] or, if no suitable index exists, add this code
# and its lower-case equivalent to to_lower_case[] and set subclass to 15.
  if (lce) {
    if (letterCat) {
      for (d in delta) {
        if (sprintf("%04X",number+delta[d])==lce) subclass = d
      }
    }

    if (!subclass) {
      subclass = 15;
      lcespecial = appendtolist(lcespecial,"{0x"code",0x"lce"}")
    }
  }

# If a upper-case equivalent is specified, either set the subclass to an index
# into upper_to_lower_delta[] or, if no suitable index exists, add this code
# and its upper-case equivalent to to_upper_case[] and set subclass to 15.
  if (uce) {
    if (letterCat) {
      for (d in delta) {
        if (sprintf("%04X",number-delta[d])==uce) subclass = d
      }
    }

    if (!subclass) {
      subclass = 15;
      ucespecial = appendtolist(ucespecial,"{0x"code",0x"uce"}")
    }
  }

# If a title-case equivalent is specified and it is different to the upper-case
# equivalent, add this code and its title-case equivalent to to_title_case[]
# and set subclass to 15.

  if (tce && tce!=uce) {
    subclass = 15;
    tcespecial = appendtolist(tcespecial,"{0x"code",0x"tce"}")
  }

# OK, now we are ready to add the entry to the character class table.

  if(debug) debugoutput(number,name,class,subclass,uce,lce,tce);

  buildtableentry(number,class,subclass);

# If this character has a decomposition, make a note of this.

  if ($decompfield) {
    raw = $decompfield
    rawdecomp[number] = raw
    n = split(raw, decomp, " ")
    i = 1
    j = index(decomp[1],"<")
    if (j) {
      k = index(decomp[1],">")
      comptype = substr(decomp[1], j+1, k-2)
      compatnames[comptype] = "string_" comptype
      compatlist = compatlist "\n  compatible[" numcompats "].from = 0x" code ";"
      compatlist = compatlist "\n  compatible[" numcompats "].to = string_" comptype ";"
      ++numcompats
      ++i
    }

    thisdecomp = sprintf("static w_char decomp_%s[] = {%d",code,n-i+1)
    for (; i<=n; ++i) {
      thisdecomp = thisdecomp sprintf(", 0x%s",""decomp[i])
    }
    thisdecomp = thisdecomp "};"
    decomplist = decomplist "\n" thisdecomp
    decopairlist=appendtolist(decopairlist,"{0x"code", decomp_"code"}")
    ++numdecopairs
  }

# Proceed with the next character

  ++number

}

END{

# There is a gap at the end, for the two non-characters 0xFFFE and 0xFFFF.

  while(number!=65536) {

    if(debug) debugoutput(number,name,0,subclass);

    buildtableentry(number,class,subclass);

    ++number
  }

  for (category in catindex) {
    print "#define CATEGORY_" category " " catindex[category]
  }
  print "#define NUMBER_OF_CATEGORIES " numcats
  print "w_string category_name[NUMBER_OF_CATEGORIES + 1];"
  print ""

# Create the list of mappings from code to digit value, and a function
# to search the list.  The function takes an int rather than a w_char
# just to avoid spurious warnings from gcc.
#
# Note: For the time being we don't do this, because the old 1.1 code
# seems to work fine.
#
#  printf "const w_char_conversion to_digit_value[] = {\n\t"
#  printf digitvalues
#  printf ", {0xffff, 0xffff},{0xffff, 0xffff}\n};\n"
#  print  ""
#  print  "w_int charToDigitValue(w_char ch) {"
#  print  "  w_int min = 0;"
#  print  "  w_int max = " numdigitvalues - 1 ";"
#  print  "  w_int try = " numdigitvalues " / 2;"
#  print  "  w_int next;"
#  print  "  while (try != min && try != max) {"
#  print  "  if (to_digit_value[try].from > ch) {"
#  print  "      next = (try + min) / 2;"
#  print  "      max = try;"
#  print  "      try = next;"
#  print  "    }"
#  print  "  else if (to_digit_value[try].from < ch) {"
#  print  "      next = (try + max + 1) / 2;"
#  print  "      min = try;"
#  print  "      try = next;"
#  print  "    }"
#  print  "    else {"
#  print  "      break;"
#  print  "    }"
#  print  "  }"
#  print  "  if (to_digit_value[try].from == ch) {"
#  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
#  print  "    return to_digit_value[try].to;"
#  print  "  }"
#  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
#  print  "  return -1;"
#  print  "}"
#  print  ""

# Create the list of mappings from code to numeric value, and a function
# to search the list.  The function takes an int rather than a w_char
# just to avoid spurious warnings from gcc.

  printf "const w_char_to_number to_numeric_value[] = {\n\t"
  printf numericvalues
  printf ", {0xffff, 0},{0xffff, 0}\n};\n"
  print  ""
  print  "w_int charToNumericValue(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numnumericvalues - 1 ";"
  print  "  w_int try = " numnumericvalues " / 2;"
  print  "  w_int next;"
  print  "  while (try != min && try != max) {"
  print  "  if (to_numeric_value[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (to_numeric_value[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  "  if (to_numeric_value[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
  print  "    return to_numeric_value[try].to;"
  print  "  }"
  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
  print  "  return -1;"
  print  "}"
  print  ""

# Create the list of mappings from code to subset, and a function
# to search the list.  The function takes an int rather than a w_char
# just to avoid spurious warnings from gcc.

  printf "typedef struct {w_char min; w_char max; w_int subset;} w_char_subset;\n\n";

  printf "const w_char_subset to_unicode_subset[] = {\n"
  printf "  {0, 0x001f, -1} // undefined\n"
  printf ", {0x0020, 0x007e, 1} // BASIC LATIN\n"
  numblocks = 2
  if (subsets[2] || subsets[999]) {
    printf ", {0x00a0, 0x00ff, 2} // LATIN-1 SUPPLEMENT\n"
    ++numblocks
  }
  if (subsets[3] || subsets[999]) {
    printf ", {0x0100, 0x017f, 3} // LATIN EXTENDED-A\n"
    ++numblocks
  }
  if (subsets[4] || subsets[999]) {
    printf ", {0x0180, 0x024f, 4} // LATIN EXTENDED-B\n"
    ++numblocks
  }
  if (subsets[5] || subsets[999]) {
    printf ", {0x0250, 0x02af, 5} // IPA EXTENSIONS\n"
    ++numblocks
  }
  if (subsets[6] || subsets[999]) {
    printf ", {0x02b0, 0x02ff, 6} // SPACING MODIFIER LETTERS\n"
    ++numblocks
  }
  if (subsets[7] || subsets[999]) {
    printf ", {0x0300, 0x036f, 7} // COMBINING DIACRITICAL MARKS\n"
    ++numblocks
  }
# Note: normally we have
#    8   BASIC GREEK                        0370-03CF
#    9   GREEK SYMBOLS AND COPTIC           03D0-03FF
# However Character.UnicodeBlock only has GREEK, so we register both as 8.
  if (subsets[8] || subsets[999]) {
    printf ", {0x0370, 0x03cf, 8} // BASIC GREEK\n"
    ++numblocks
  }
  if (subsets[9] || subsets[999]) {
    printf ", {0x03d0, 0x03ff, 8} // GREEK SYMBOLS AND COPTIC\n"
    ++numblocks
  }
  if (subsets[10] || subsets[999]) {
    printf ", {0x0400, 0x04ff, 10} // CYRILLIC\n"
    ++numblocks
  }
  if (subsets[11] || subsets[999]) {
    printf ", {0x0530, 0x058f, 11} // ARMENIAN\n"
    ++numblocks
  }
# Note: normally we have
#   12   BASIC HEBREW                      05D0-05EA
#   13   HEBREW EXTENDED                   0590-05CF,05EB-05FF
# However Character.UnicodeBlock only has HEBREW, so we register both as 12.
  if (subsets[13] || subsets[999]) {
    printf ", {0x0590, 0x05cf, 12} // HEBREW EXTENDED\n"
    ++numblocks
  }
  if (subsets[12] || subsets[999]) {
    printf ", {0x05d0, 0x05ea, 12} // BASIC HEBREW\n"
    ++numblocks
  }
  if (subsets[13] || subsets[999]) {
    printf ", {0x05eb, 0x05ff, 12} // HEBREW EXTENDED\n"
    ++numblocks
  }
# Note: normally we have
#   14   BASIC ARABIC                       0600-065F
#   15   ARABIC EXTENDED                    0660-06FF
# However Character.UnicodeBlock only has ARABIC, so we register both as 14.
  if (subsets[14] || subsets[999]) {
    printf ", {0x0600, 0x065f, 14} // BASIC ARABIC\n"
    ++numblocks
  }
  if (subsets[15] || subsets[999]) {
    printf ", {0x0660, 0x06ff, 14} // ARABIC EXTENDED\n"
    ++numblocks
  }
  if (subsets[85] || subsets[999]) {
    printf ", {0x0700, 0x074f, 85} // SYRIAC\n"
    ++numblocks
  }
  if (subsets[86] || subsets[999]) {
    printf ", {0x0780, 0x07bf, 86} // THAANA\n"
    ++numblocks
  }
  if (subsets[16] || subsets[999]) {
    printf ", {0x0900, 0x097f, 16} // DEVANGARI\n"
    ++numblocks
  }
  if (subsets[17] || subsets[999]) {
    printf ", {0x0980, 0x09ff, 17} // BENGALI\n"
    ++numblocks
  }
  if (subsets[18] || subsets[999]) {
    printf ", {0x0a00, 0x0a7f, 18} // GURMUKHI\n"
    ++numblocks
  }
  if (subsets[19] || subsets[999]) {
    printf ", {0x0a80, 0x0aff, 19} // GUJARATI\n"
    ++numblocks
  }
  if (subsets[20] || subsets[999]) {
    printf ", {0x0b00, 0x0b7f, 20} // ORIYA\n"
    ++numblocks
  }
  if (subsets[21] || subsets[999]) {
    printf ", {0x0b80, 0x0bff, 21} // TAMIL\n"
    ++numblocks
  }
  if (subsets[22] || subsets[999]) {
    printf ", {0x0c00, 0x0c7f, 22} // TELUGU\n"
    ++numblocks
  }
  if (subsets[23] || subsets[999]) {
    printf ", {0x0c80, 0x0cff, 23} // KANNADA\n"
    ++numblocks
  }
  if (subsets[24] || subsets[999]) {
    printf ", {0x0d00, 0x0d7f, 24} // MAYALAM\n"
    ++numblocks
  }
  if (subsets[84] || subsets[999]) {
    printf ", {0x0d80, 0x0dff, 84} // SINHALA\n"
    ++numblocks
  }
  if (subsets[25] || subsets[999]) {
    printf ", {0x0e00, 0x0e7f, 25} // THAI\n"
    ++numblocks
  }
  if (subsets[26] || subsets[999]) {
    printf ", {0x0e80, 0x0eff, 26} // LAO\n"
    ++numblocks
  }
# Note: normally we have also
#   72   BASIC TIBETAN                      0F00-0FBF
# but it's just a subset so we ignore it.
  if (subsets[91] || subsets[999]) {
    printf ", {0x0f00, 0x0fff, 91} // TIBETAN\n"
    ++numblocks
  }
# Note: normally we have
#   87   BASIC MYANMAR                      1000-104F
#   90   EXTENDED MYANMAR                   1050-109F
# However Character.UnicodeBlock only has MYANMAR, so we register both as 87.
  if (subsets[87] || subsets[999]) {
    printf ", {0x1000, 0x104f, 87} // BASIC MYANMAR\n"
    ++numblocks
  }
  if (subsets[90] || subsets[999]) {
    printf ", {0x1050, 0x109f, 87} // EXTENDED MYANMAR\n"
    ++numblocks
  }
# Note: normally we have
#   27   BASIC GEORGIAN                     10D0-10FF
#   28   GEORGIAN EXTENDED                  10A0-10CF
# However Character.UnicodeBlock only has GEORGIAN, so we register both as 27.
  if (subsets[28] || subsets[999]) {
    printf ", {0x10a0, 0x10cf, 27} // GEORGIAN EXTENDED\n"
    ++numblocks
  }
  if (subsets[27] || subsets[999]) {
    printf ", {0x10d0, 0x10ff, 27} // BASIC GEORGIAN\n"
    ++numblocks
  }
  if (subsets[29] || subsets[999]) {
    printf ", {0x1100, 0x11ff, 29} // HANGUL JAMO\n"
    ++numblocks
  }
  if (subsets[73] || subsets[999]) {
    printf ", {0x1200, 0x137f, 73} // HANGUL JAMO\n"
    ++numblocks
  }
  if (subsets[75] || subsets[999]) {
    printf ", {0x13a0, 0x13ff, 75} // CHEROKEE\n"
    ++numblocks
  }
  if (subsets[74] || subsets[999]) {
    printf ", {0x1400, 0x167f, 74} // UNIFIED CANADIAN ABORIGINAL SYLLABICS\n"
    ++numblocks
  }
  if (subsets[82] || subsets[999]) {
    printf ", {0x1680, 0x169f, 82} // OGHAM\n"
    ++numblocks
  }
  if (subsets[83] || subsets[999]) {
    printf ", {0x16a0, 0x16ff, 83} // RUNIC\n"
    ++numblocks
  }
  if (subsets[88] || subsets[999]) {
    printf ", {0x1780, 0x17ff, 88} // KHMER\n"
    ++numblocks
  }
  if (subsets[89] || subsets[999]) {
    printf ", {0x1800, 0x18af, 89} // MONGOLIAN\n"
    ++numblocks
  }
  if (subsets[30] || subsets[999]) {
    printf ", {0x1e00, 0x1eff, 30} // LATIN EXTENDED ADDITIONAL\n"
    ++numblocks
  }
  if (subsets[31] || subsets[999]) {
    printf ", {0x1f00, 0x1fff, 31} // GREEK EXTENDED\n"
    ++numblocks
  }
  printf ", {0x2000, 0x206f, 32} // GENERAL PUNCTUATION\n"
  ++numblocks
  printf ", {0x2070, 0x209f, 33} // SUPERSCRIPTS AND SUBSCRIPTS\n"
  ++numblocks
  printf ", {0x20a0, 0x20cf, 34} // CURRENCY SYMBOLS\n"
  ++numblocks
  if (subsets[35] || subsets[999]) {
    printf ", {0x20d0, 0x20ff, 35} // COMBINING DIACRITICAL MARKS FOR SYMBOLS\n"
    ++numblocks
  }
  if (subsets[36] || subsets[999]) {
    printf ", {0x2100, 0x214f, 36} // LETTERLIKE SYMBOLS\n"
    ++numblocks
  }
  if (subsets[37] || subsets[999]) {
    printf ", {0x2150, 0x218f, 37} // NUMBER FORMS\n"
    ++numblocks
  }
  if (subsets[38] || subsets[999]) {
    printf ", {0x2190, 0x21ff, 38} // ARROWS\n"
    ++numblocks
  }
  if (subsets[39] || subsets[999]) {
    printf ", {0x2200, 0x22ff, 39} // MATHEMATICAL OPERATORS\n"
    ++numblocks
  }
  if (subsets[40] || subsets[999]) {
    printf ", {0x2300, 0x23ff, 40} // MISCELLANEOUS TECHNICAL\n"
    ++numblocks
  }
  if (subsets[41] || subsets[999]) {
    printf ", {0x2400, 0x243f, 41} // CONTROL PICTURES\n"
    ++numblocks
  }
  if (subsets[42] || subsets[999]) {
    printf ", {0x2440, 0x245f, 42} // OPTICAL CHARACTER RECOGNITION\n"
    ++numblocks
  }
  if (subsets[43] || subsets[999]) {
    printf ", {0x2460, 0x24ff, 43} // ENCLOSED ALPHANUMERICS\n"
    ++numblocks
  }
  if (subsets[44] || subsets[999]) {
    printf ", {0x2500, 0x257f, 44} // BOX DRAWING\n"
    ++numblocks
  }
  if (subsets[45] || subsets[999]) {
    printf ", {0x2580, 0x259f, 45} // NUMBER FORMS\n"
    ++numblocks
  }
  if (subsets[46] || subsets[999]) {
    printf ", {0x25a0, 0x25ff, 46} // GEOMETRIC SHAPES\n"
    ++numblocks
  }
  if (subsets[47] || subsets[999]) {
    printf ", {0x2600, 0x26ff, 47} // MISCELLANEOUS SYMBOLS\n"
    ++numblocks
  }
  if (subsets[48] || subsets[999]) {
    printf ", {0x2700, 0x27bf, 48} // DINGBATS\n"
    ++numblocks
  }
  if (subsets[80] || subsets[999]) {
    printf ", {0x2800, 0x28ff, 80} // BRAILLE PATTERNS\n"
    ++numblocks
  }
  if (subsets[79] || subsets[999]) {
    printf ", {0x2e80, 0x2eff, 79} // CJK RADICALS SUPPLEMENT\n"
    ++numblocks
  }
  if (subsets[78] || subsets[999]) {
    printf ", {0x2f00, 0x2fdf, 78} // KANGXI RADICALS\n"
    ++numblocks
  }
  if (subsets[207] || subsets[999]) {
    printf ", {0x2ff0, 0x2fff, 207} // IDEOGRAPHIC DESCRIPTION CHARACTERS\n"
    ++numblocks
  }
  if (subsets[49] || subsets[999]) {
    printf ", {0x3000, 0x303f, 49} // CJK SYMBOLS AND PUNCTUATION\n"
    ++numblocks
  }
  if (subsets[50] || subsets[999]) {
    printf ", {0x3040, 0x309f, 50} // HIRAGANA\n"
    ++numblocks
  }
  if (subsets[51] || subsets[999]) {
    printf ", {0x30a0, 0x30ff, 51} // KATAKANA\n"
    ++numblocks
  }
  if (subsets[52] || subsets[999]) {
    printf ", {0x3100, 0x312f, 52} // BOPOMOFO\n"
    ++numblocks
  }
  if (subsets[53] || subsets[999]) {
    printf ", {0x3130, 0x318f, 53} // HANGUL COMPATIBILTY JAMO\n"
    ++numblocks
  }
  if (subsets[54] || subsets[999]) {
    printf ", {0x3190, 0x319f, 54} // CJK MISCELLANEOUS\n"
    ++numblocks
  }
  if (subsets[52] || subsets[999]) {
    printf ", {0x31a0, 0x31bf, 52} // BOPOMOFO\n"
    ++numblocks
  }
  if (subsets[55] || subsets[999]) {
    printf ", {0x3200, 0x32ff, 55} // ENCLOSED CJK LETTERS AND MONTHS\n"
    ++numblocks
  }
  if (subsets[56] || subsets[999]) {
    printf ", {0x3300, 0x33ff, 56} // CJK COMPATIBILTY\n"
    ++numblocks
  }
  if (subsets[81] || subsets[999]) {
    printf ", {0x3400, 0x4dbf, 81} // CJK UNIFIED IDEOGRAPHS EXTENSION A\n"
    ++numblocks
  }
  if (subsets[60] || subsets[999]) {
    printf ", {0x4e00, 0x9fff, 60} // CJK UNIFIED IDEOGRAPHS\n"
    ++numblocks
  }
  if (subsets[76] || subsets[999]) {
    printf ", {0xa000, 0xa48f, 76} // YI SYLLABLES\n"
    ++numblocks
  }
  if (subsets[77] || subsets[999]) {
    printf ", {0xa490, 0xa4cf, 77} // YI RADICALS\n"
    ++numblocks
  }
  if (subsets[71] || subsets[999]) {
    printf ", {0xac00, 0xd7a3, 71} // HANGUL SYLLABLES\n"
    ++numblocks
  }
  if (subsets[61] || subsets[999]) {
    printf ", {0xe000, 0xf8ff, 61} // PRIVATE USE AREA\n"
    ++numblocks
  }
  if (subsets[62] || subsets[999]) {
    printf ", {0xf900, 0xfaff, 62} // CJK COMPATIBILTY IDEOGRAPHS\n"
    ++numblocks
  }
  if (subsets[63] || subsets[999]) {
    printf ", {0xfb00, 0xfb4f, 63} // ALPHABETIC PRESENTATION FORMS\n"
    ++numblocks
  }
  if (subsets[64] || subsets[999]) {
    printf ", {0xfb50, 0xfdff, 64} // ARABIC PRESENTATION FORMS-A\n"
    ++numblocks
  }
  if (subsets[65] || subsets[999]) {
    printf ", {0xfe20, 0xfe2f, 65} // COMBINING HALF-MARKS\n"
    ++numblocks
  }
  if (subsets[66] || subsets[999]) {
    printf ", {0xfe30, 0xfe4f, 66} // CJK COMPATIBILTY FORMS\n"
    ++numblocks
  }
  if (subsets[67] || subsets[999]) {
    printf ", {0xfe50, 0xfe6f, 67} // SMALL FORM VARIANTS\n"
    ++numblocks
  }
  if (subsets[68] || subsets[999]) {
    printf ", {0xfe70, 0xfefe, 68} // ARABIC PRESENTATION FORMS-B\n"
    ++numblocks
  }
  if (subsets[69] || subsets[999]) {
    printf ", {0xff00, 0xffef, 69} // HALFWIDTH AND FULLWIDTH FORMS\n"
    ++numblocks
  }
  if (subsets[70] || subsets[999]) {
    printf ", {0xfff0, 0xfffd, 70} // SPECIALS\n"
    ++numblocks
  }
  printf ", {0xffff, 0xffff, -1}\n};\n"
  ++numblocks
  print  ""
  print  "w_int charToUnicodeSubset(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numblocks - 1 ";"
  print  "  w_int try = " numblocks " / 2;"
  print  "  w_int next;"
  print  "  while (try != min && try != max) {"
  print  "  if (to_unicode_subset[try].min > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (to_unicode_subset[try].max < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  "  if (to_unicode_subset[try].min <= ch && to_unicode_subset[try].max >= ch) {"
  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
  print  "    return to_unicode_subset[try].subset;"
  print  "  }"
  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
  print  "  return -1;"
  print  "}"
  print  ""

# Create a list of mappings from code value onto canonical combining class, 
# and a function to search the list.

#  combilist=appendtolist(combilist,"{0,0}")
  printf "const w_char_to_number to_combining_class[] = {\n\t"
  printf combilist
  printf "\n};\n\n"
  print  ""
  print  "w_int charToCombiningClass(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numcombis - 1 ";"
  print  "  w_int try = (" numcombis " + 0) / 2;"
  print  "  w_int next;"
  print  "  while (try != min && try != max) {"
  print  "  if (to_combining_class[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (to_combining_class[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  "  if (to_combining_class[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
  print  "    return to_combining_class[try].to;"
  print  "  }"
  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
  print  "  return 0;"
  print  "}"
  print  ""

# Create the list of mappings from code to decomposition, and a function
# to search the list.  Both canonical and compatibility decompositions
# are included.

  print  decomplist
  print  ""
  print  "const w_char_decomposition decomposition[] = {"
  print  "\t" decopairlist
  print  "};"
  print  ""
  print  "w_char* charToDecomposition(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numdecopairs - 1 ";"
  print  "  w_int try = " numdecopairs " / 2;"
  print  "  w_int next;"
  print  "  while (try != min && try != max) {"
  print  "  if (decomposition[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (decomposition[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  "  if (decomposition[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
  print  "    return decomposition[try].to;"
  print  "  }"
  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
  print  "  return NULL;"
  print  "}"
  print  ""

# Define the array which maps character codes onto compatibility types,
# and a function to search it.

  print "static w_char_to_string compatible[" numcompats "];"
  print  ""
  for (cname in compatnames) {
    print "w_string string_" cname ";"
  }
  print ""

  print  "void createDecompositionTables() {"
  for (cname in compatnames) {
    print "  string_" cname " = cstring2String(\"" cname "\", " length(cname) ");"
  }
  print  compatlist
  print  ""
  print  "}"
  print  ""
  print  "void createCharacterTables() {"
  print "  category_name[0] = cstring2String(\"--\", 2);"
  for (category in catindex) {
    print "  category_name[CATEGORY_" category "] = cstring2String(\"" category "\", " length(category) ");"
  }
  print  "  category_name[NUMBER_OF_CATEGORIES] = NULL;"
  print  "}"
  print  ""

  print  "w_string charToCompatibilityType(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numcompats - 1 ";"
  print  "  w_int try = " numcompats " / 2;"
  print  "  w_int next;"
  print  "  while (try != min && try != max) {"
  print  "  if (compatible[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (compatible[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  "  if (compatible[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it at %d\\n\", ch, try);"
  print  "    return compatible[try].to;"
  print  "  }"
  print  "  woempa(1, \"Looking for %04X, didn't find it\\n\", ch);"
  print  "  return NULL;"
  print  "}"
  print  ""

# Create a list of mappings from code value onto general category, 
# and a function to search the list.  We list only the characters
# which have a different category from their predecessor.

  printf "const w_char_conversion to_category[] = {\n\t"
  printf catlist
  printf ", {0xFFFE,0}\n};\n\n"
  print  ""
  print  "w_string charToCategory(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numcatpairs + 1";"
  print  "  w_int try = " numcatpairs " / 2;"
  print  "  w_int next;"
  print  ""
  print  "  if (!CHAR_CLASS(ch)) {"
  print  ""
  print  "  return category_name[0];"
  print  ""
  print  "  }"
  print  ""
  print  "  while (try != min && try != max) {"
  print  "  if (to_category[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "  else if (to_category[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  ""
  print  "  if (to_category[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it (category %d) at %d\\n\", ch, to_category[try].to, try);"
  print  "    woempa(1, \"Returning ``%w''\\n\", category_name[to_category[try].to]);"
  print  "  }"
  print  "  else {"
  print  "    woempa(1, \"Looking for %04X, found %04X (%d) at %d\\n\", ch, to_category[min].from, to_category[min].to, min);"
  print  "    woempa(1, \"Returning ``%w''\\n\", category_name[to_category[min].to]);"
  print  "  }"
  print  ""
  print  "  return category_name[to_category[to_category[try].from == ch ? try : min].to];"
  print  "}"
  print  ""

# Create a list of mappings from code value onto directionality, 
# and a function to search the list.  We list only the characters
# which have a different directionality from their predecessor.

  printf "const w_char_conversion to_directionality[] = {\n\t"
  printf dirlist
  printf ", {0xFFFE,-1}\n};\n\n"
  print  ""
  print  "w_int charToDirectionality(w_char ch) {"
  print  "  w_int min = 0;"
  print  "  w_int max = " numdirpairs + 1";"
  print  "  w_int try = " numdirpairs " / 2;"
  print  "  w_int next;"
  print  ""
  print  "  if (!CHAR_CLASS(ch)) {"
  print  ""
  print  "  return -1;"
  print  ""
  print  "  }"
  print  ""
  print  "  while (try != min && try != max) {"
  print  "    if (to_directionality[try].from > ch) {"
  print  "      next = (try + min) / 2;"
  print  "      max = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else if (to_directionality[try].from < ch) {"
  print  "      next = (try + max + 1) / 2;"
  print  "      min = try;"
  print  "      try = next;"
  print  "    }"
  print  "    else {"
  print  "      break;"
  print  "    }"
  print  "  }"
  print  ""
  print  "  if (to_directionality[try].from == ch) {"
  print  "    woempa(1, \"Looking for %04X, found it (directionality %d) at %d\\n\", ch, to_directionality[try].to, try);"
  print  "    woempa(1, \"Returning %d\\n\", to_directionality[try].to);"
  print  "  }"
  print  "  else {"
  print  "    woempa(1, \"Looking for %04X, found %04X (%d) at %d\\n\", ch, to_directionality[min].from, to_directionality[min].to, min);"
  print  "    woempa(1, \"Returning %d\\n\", to_directionality[min].to);"
  print  "  }"
  print  ""
  print  "  return to_directionality[to_directionality[try].from == ch ? try : min].to;"
  print  "}"
  print  ""

# Create a list of mirrored characters, and a function to search the list.

  printf "const w_char mirrored_chars[] = {\n\t"
  printf mirrorlist
  printf "};\n\n"
  print  ""
  print  "w_boolean charIsMirrored(w_char ch) {"
  print  "  w_int i;"
  print  ""
  print  "  if (!CHAR_CLASS(ch)) {"
  print  ""
  print  "  return 0;"
  print  ""
  print  "  }"
  print  ""
  print  "  for (i = 0; i < " nummirrors "; ++i) {"
  print  "    if (mirrored_chars[i] == ch) {"
  print  "      return 1;"
  print  "    }"
  print  "    else if (mirrored_chars[i] > ch) {"
  print  "      return 0;"
  print  "    }"
  print  "  }"
  print  ""
  print  "  return 0;"
  print  "}"
  print  ""

# Print out all the (non-duplicate) rows of the classification table:

  for(r=0;r<256;++r) if(row[r]) {
      printf "static const w_byte character_class_row_%02x[] = {\n",r
      print "\t"row[r]
      print "};\n\n"
  }

# Now the table of rows:

  printf "const w_byte * character_class[] = {\n"
  for(r=0;r<256;++r) printf "\tcharacter_class_row_%02x, \n",table[r] 
  printf "};\n\n"

# Now for the little table of deltas (preceded by a count and 
# terminated by a zero):

  printf "const int upper_to_lower_delta[] = { %d",numdeltas
  for(i=0;i<numdeltas;++i) printf ", %d",delta[i+1];
  printf ", 0};\n\n"

# Finally the three tables of special case conversions.

  ucespecial=appendtolist(ucespecial,"{0,0}")
  printf "const w_char_conversion to_upper_case[] = {\n\t"
  printf ucespecial
  printf "\n};\n"

  lcespecial=appendtolist(lcespecial,"{0,0}")
  printf "const w_char_conversion to_lower_case[] = {\n\t"
  printf lcespecial
  printf "\n};\n"

  tcespecial=appendtolist(tcespecial,"{0,0}")
  printf "const w_char_conversion to_title_case[] = {\n\t"
  printf tcespecial
  printf "\n};\n"

}
