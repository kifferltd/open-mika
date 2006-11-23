// Tags: JDK1.4

// Copyright (C) 1998 Cygnus Solutions

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

package gnu.testlet.wonka.lang.Character;

import java.util.Random;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.UnicodeSubsets;

public class UnicodeBlock14 implements Testlet
{

  private Random prng = new Random();

/* 
We test 12 characters from each range: the first, the last, and 10 in the middle.
*/
  private void test_undefined(TestHarness harness, int start, int end) {
    Character.UnicodeBlock bl;
    bl = Character.UnicodeBlock.of((char)start); 
    harness.check (bl == null, "Character " + Integer.toHexString(start) + " should not be in any UnicodeBlock, but got " + bl);
    for (int i = 0; i < 10; ++i) {
      char c = (char)(start + ((char)prng.nextInt() % (end - start)));
      bl = Character.UnicodeBlock.of(c); 
      harness.check (bl == null, "Character " + Integer.toHexString(c) + " should not be in any UnicodeBlock, but got " + bl);
    }
    bl = Character.UnicodeBlock.of((char)end); 
    harness.check (bl == null, "Character " + Integer.toHexString(end) + " should not be in any UnicodeBlock, but got " + bl);
  }

  private void test_defined(TestHarness harness, int start, int end, Character.UnicodeBlock rightBlock) {
    Character.UnicodeBlock bl;
    bl = Character.UnicodeBlock.of((char)start); 
    harness.check (bl == rightBlock, "Character " + Integer.toHexString(start) + " should be in Character.UnicodeBlock." + rightBlock + ", but got " + bl);
    for (int i = 0; i < 10; ++i) {
      char c = (char)(start + ((char)prng.nextInt() % (end - start)));
      bl = Character.UnicodeBlock.of(c); 
      harness.check (bl == rightBlock, "Character " + Integer.toHexString(c) + " should be in UnicodeBlock " + rightBlock + ", but got " + bl);
    }
    bl = Character.UnicodeBlock.of((char)end); 
    harness.check (bl == rightBlock, "Character " + Integer.toHexString(end) + " should be in UnicodeBlock " + rightBlock + ", but got " + bl);
  }

  public void test (TestHarness harness) {
      test_undefined(harness, 0x0000, 0x001f);
//    1+  BASIC LATIN                        0020-007E
      test_defined(harness, 0x0020, 0x007e, Character.UnicodeBlock.BASIC_LATIN);
//    2+  LATIN-1 SUPPLEMENT                 00A0-00FF
      test_defined(harness, 0x00a0, 0x00ff, Character.UnicodeBlock.LATIN_1_SUPPLEMENT);
//    3   LATIN EXTENDED-A                   0100-017F
      if (UnicodeSubsets.isSupported("3")) {
        test_defined(harness, 0x0100, 0x017f, Character.UnicodeBlock.LATIN_EXTENDED_A);
      }
//    4   LATIN EXTENDED-B                   0180-024F
      if (UnicodeSubsets.isSupported("4")) {
        test_defined(harness, 0x0180, 0x024f, Character.UnicodeBlock.LATIN_EXTENDED_B);
      }
//    5   IPA EXTENSIONS                     0250-02AF
      if (UnicodeSubsets.isSupported("5")) {
        test_defined(harness, 0x0250, 0x02af, Character.UnicodeBlock.IPA_EXTENSIONS);
      }
//    6   SPACING MODIFIER LETTERS           02B0-02FF
      if (UnicodeSubsets.isSupported("6")) {
        test_defined(harness, 0x02b0, 0x02ff, Character.UnicodeBlock.SPACING_MODIFIER_LETTERS);
      }
//    7   COMBINING DIACRITICAL MARKS        0300-036F
      if (UnicodeSubsets.isSupported("7")) {
        test_defined(harness, 0x0300, 0x036f, Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS);
      }
//    8   BASIC GREEK                        0370-03CF
      if (UnicodeSubsets.isSupported("8")) {
        test_defined(harness, 0x0370, 0x03cf, Character.UnicodeBlock.GREEK);
      }
//    9   GREEK SYMBOLS AND COPTIC           03D0-03FF
      if (UnicodeSubsets.isSupported("9")) {
        test_defined(harness, 0x03d0, 0x03ff, Character.UnicodeBlock.GREEK);
      }
//   10   CYRILLIC                           0400-04FF
      if (UnicodeSubsets.isSupported("10")) {
        test_defined(harness, 0x0400, 0x04ff, Character.UnicodeBlock.CYRILLIC);
      }
      test_undefined(harness, 0x0500, 0x052f);
//   11   ARMENIAN                           0530-058F
      if (UnicodeSubsets.isSupported("11")) {
        test_defined(harness, 0x0530, 0x058f, Character.UnicodeBlock.ARMENIAN);
      }
//   13   HEBREW EXTENDED                    0590-05CF
      if (UnicodeSubsets.isSupported("13")) {
        test_defined(harness, 0x0590, 0x05cf, Character.UnicodeBlock.HEBREW);
      }
//   12   BASIC HEBREW                       05D0-05EA
      if (UnicodeSubsets.isSupported("12")) {
        test_defined(harness, 0x05d0, 0x05ea, Character.UnicodeBlock.HEBREW);
      }
//   13   HEBREW EXTENDED                    05EB-05FF
      if (UnicodeSubsets.isSupported("13")) {
        test_defined(harness, 0x05eb, 0x05ff, Character.UnicodeBlock.HEBREW);
      }
//   14   BASIC ARABIC                       0600-065F
      if (UnicodeSubsets.isSupported("14")) {
        test_defined(harness, 0x0600, 0x065f, Character.UnicodeBlock.ARABIC);
      }
//   15   ARABIC EXTENDED                    0660-06FF
      if (UnicodeSubsets.isSupported("15")) {
        test_defined(harness, 0x0660, 0x06ff, Character.UnicodeBlock.ARABIC);
      }
//   85   SYRIAC                             0700-074F
      if (UnicodeSubsets.isSupported("85")) {
        test_defined(harness, 0x0700, 0x074f, Character.UnicodeBlock.SYRIAC);
      }
//   86   THAANA                             0780-07BF
      if (UnicodeSubsets.isSupported("86")) {
        test_defined(harness, 0x0780, 0x07bf, Character.UnicodeBlock.THAANA);
      }
//   16   DEVANAGARI                         0900-097F
      if (UnicodeSubsets.isSupported("16")) {
        test_defined(harness, 0x0900, 0x097f, Character.UnicodeBlock.DEVANAGARI);
      }
//   17   BENGALI                            0980-09FF
      if (UnicodeSubsets.isSupported("17")) {
        test_defined(harness, 0x0980, 0x09ff, Character.UnicodeBlock.BENGALI);
      }
//   18   GURMUKHI                           0A00-0A7F
      if (UnicodeSubsets.isSupported("18")) {
        test_defined(harness, 0x0a00, 0x0a7f, Character.UnicodeBlock.GURMUKHI);
      }
//   19   GUJARATI                           0A80-0AFF
      if (UnicodeSubsets.isSupported("19")) {
        test_defined(harness, 0x0a80, 0x0aff, Character.UnicodeBlock.GUJARATI);
      }
//   20   ORIYA                              0B00-0B7F
      if (UnicodeSubsets.isSupported("20")) {
        test_defined(harness, 0x0b00, 0x0b7f, Character.UnicodeBlock.ORIYA);
      }
//   21   TAMIL                              0B80-0BFF
      if (UnicodeSubsets.isSupported("21")) {
        test_defined(harness, 0x0b80, 0x0bff, Character.UnicodeBlock.TAMIL);
      }
//   22   TELUGU                             0C00-0C7F
      if (UnicodeSubsets.isSupported("22")) {
        test_defined(harness, 0x0c00, 0x0c7f, Character.UnicodeBlock.TELUGU);
      }
//   23   KANNADA                            0C80-0CFF
      if (UnicodeSubsets.isSupported("23")) {
        test_defined(harness, 0x0c80, 0x0cff, Character.UnicodeBlock.KANNADA);
      }
//   24   MALAYALAM                          0D00-0D7F
      if (UnicodeSubsets.isSupported("24")) {
        test_defined(harness, 0x0d00, 0x0d7f, Character.UnicodeBlock.MALAYALAM);
      }
//   84   SINHALA                            0D80-0DFF
      if (UnicodeSubsets.isSupported("84")) {
        test_defined(harness, 0x0d80, 0x0dff, Character.UnicodeBlock.SINHALA);
      }
//   25   THAI                               0E00-0E7F
      if (UnicodeSubsets.isSupported("25")) {
        test_defined(harness, 0x0e00, 0x0e7f, Character.UnicodeBlock.THAI);
      }
//   26   LAO                                0E80-0EFF
      if (UnicodeSubsets.isSupported("26")) {
        test_defined(harness, 0x0e80, 0x0eff, Character.UnicodeBlock.LAO);
      }
//   72   BASIC TIBETAN                      0F00-0FBF
//   91   TIBETAN                            0F00-0FFF
      if (UnicodeSubsets.isSupported("91")) {
        test_defined(harness, 0x0f00, 0x0fff, Character.UnicodeBlock.TIBETAN);
      }
      else if (UnicodeSubsets.isSupported("72")) {
        test_defined(harness, 0x0f00, 0x0fbf, Character.UnicodeBlock.TIBETAN);
      }
//   87   BASIC MYANMAR                      1000-104F
      if (UnicodeSubsets.isSupported("87")) {
        test_defined(harness, 0x1000, 0x104f, Character.UnicodeBlock.MYANMAR);
      }
//   90   EXTENDED MYANMAR                   1050-109F
      if (UnicodeSubsets.isSupported("90")) {
        test_defined(harness, 0x1050, 0x109f, Character.UnicodeBlock.MYANMAR);
      }
//   28   GEORGIAN EXTENDED                  10A0-10CF
      if (UnicodeSubsets.isSupported("28")) {
        test_defined(harness, 0x10a0, 0x10cf, Character.UnicodeBlock.GEORGIAN);
      }
//   27   BASIC GEORGIAN                     10D0-10FF
      if (UnicodeSubsets.isSupported("27")) {
        test_defined(harness, 0x10d0, 0x10ff, Character.UnicodeBlock.GEORGIAN);
      }
//   29   HANGUL JAMO                        1100-11FF
      if (UnicodeSubsets.isSupported("29")) {
        test_defined(harness, 0x1100, 0x11ff, Character.UnicodeBlock.HANGUL_JAMO);
      }
//   73   ETHIOPIC                           1200-137F
      if (UnicodeSubsets.isSupported("73")) {
        test_defined(harness, 0x1200, 0x137f, Character.UnicodeBlock.ETHIOPIC);
      }
      test_undefined(harness, 0x1380, 0x139f);
//   75   CHEROKEE                           13A0-13FF
      if (UnicodeSubsets.isSupported("75")) {
        test_defined(harness, 0x13a0, 0x13ff, Character.UnicodeBlock.CHEROKEE);
      }
//   74   UNIFIED CANADIAN ABORIGINAL SYLLABICS
//                                           1400-167F
      if (UnicodeSubsets.isSupported("74")) {
        test_defined(harness, 0x1400, 0x167f, Character.UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS);
      }
//   82   OGHAM                              1680-169F
      if (UnicodeSubsets.isSupported("82")) {
        test_defined(harness, 0x1680, 0x169f, Character.UnicodeBlock.OGHAM);
      }
//   83   RUNIC                              16A0-16FF
      if (UnicodeSubsets.isSupported("83")) {
        test_defined(harness, 0x16a0, 0x16ff, Character.UnicodeBlock.RUNIC);
      }
      test_undefined(harness, 0x1700, 0x177f);

//   88   KHMER                              1780-17FF
      if (UnicodeSubsets.isSupported("88")) {
        test_defined(harness, 0x1780, 0x17ff, Character.UnicodeBlock.KHMER);
      }
//   89   MONGOLIAN                          1800-18AF
      if (UnicodeSubsets.isSupported("89")) {
        test_defined(harness, 0x1800, 0x18af, Character.UnicodeBlock.MONGOLIAN);
      }
      test_undefined(harness, 0x18b0, 0x1dff);
//   30   LATIN EXTENDED ADDITIONAL          1E00-1EFF
      if (UnicodeSubsets.isSupported("30")) {
        test_defined(harness, 0x1e00, 0x1eff, Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL);
      }
//   31   GREEK EXTENDED                     1F00-1FFF
      if (UnicodeSubsets.isSupported("31")) {
        test_defined(harness, 0x1f00, 0x1fff, Character.UnicodeBlock.GREEK_EXTENDED);
      }
//   32+  GENERAL PUNCTUATION                2000-206F
      test_defined(harness, 0x2000, 0x206f, Character.UnicodeBlock.GENERAL_PUNCTUATION);
//   33+  SUPERSCRIPTS AND SUBSCRIPTS        2070-209F
      test_defined(harness, 0x2070, 0x209f, Character.UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS);
//   34+  CURRENCY SYMBOLS                   20A0-20CF
      test_defined(harness, 0x20a0, 0x20cf, Character.UnicodeBlock.CURRENCY_SYMBOLS);
//   35   COMBINING DIACRITICAL MARKS FOR SYMBOLS
//                                           20D0-20FF
      if (UnicodeSubsets.isSupported("35")) {
        test_defined(harness, 0x20d0, 0x20ff, Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS);
      }
//   36   LETTERLIKE SYMBOLS                 2100-214F
      if (UnicodeSubsets.isSupported("36")) {
        test_defined(harness, 0x2100, 0x214f, Character.UnicodeBlock.LETTERLIKE_SYMBOLS);
      }
//   37   NUMBER FORMS                       2150-218F
      if (UnicodeSubsets.isSupported("37")) {
        test_defined(harness, 0x2150, 0x218f, Character.UnicodeBlock.NUMBER_FORMS);
      }
//   38   ARROWS                             2190-21FF
      if (UnicodeSubsets.isSupported("38")) {
        test_defined(harness, 0x2190, 0x21ff, Character.UnicodeBlock.ARROWS);
      }
//   39   MATHEMATICAL OPERATORS             2200-22FF
      if (UnicodeSubsets.isSupported("39")) {
        test_defined(harness, 0x2290, 0x22ff, Character.UnicodeBlock.MATHEMATICAL_OPERATORS);
      }
//   40   MISCELLANEOUS TECHNICAL            2300-23FF
      if (UnicodeSubsets.isSupported("39")) {
        test_defined(harness, 0x2300, 0x23ff, Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL);
      }
//   41   CONTROL PICTURES                   2400-243F
      if (UnicodeSubsets.isSupported("41")) {
        test_defined(harness, 0x2400, 0x243f, Character.UnicodeBlock.CONTROL_PICTURES);
      }
//   42   OPTICAL CHARACTER RECOGNITION      2440-245F
      if (UnicodeSubsets.isSupported("42")) {
        test_defined(harness, 0x2440, 0x245f, Character.UnicodeBlock.OPTICAL_CHARACTER_RECOGNITION);
      }
//   43   ENCLOSED ALPHANUMERICS             2460-24FF
      if (UnicodeSubsets.isSupported("43")) {
        test_defined(harness, 0x2460, 0x24ff, Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS);
      }
//   44   BOX DRAWING                        2500-257F
      if (UnicodeSubsets.isSupported("44")) {
        test_defined(harness, 0x2500, 0x257f, Character.UnicodeBlock.BOX_DRAWING);
      }
//   45   BLOCK ELEMENTS                     2580-259F
      if (UnicodeSubsets.isSupported("45")) {
        test_defined(harness, 0x2580, 0x259f, Character.UnicodeBlock.BLOCK_ELEMENTS);
      }
//   46   GEOMETRIC SHAPES                   25A0-25FF
      if (UnicodeSubsets.isSupported("46")) {
        test_defined(harness, 0x25a0, 0x25ff, Character.UnicodeBlock.GEOMETRIC_SHAPES);
      }
//   47   MISCELLANEOUS SYMBOLS              2600-26FF
      if (UnicodeSubsets.isSupported("47")) {
        test_defined(harness, 0x2600, 0x26ff, Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS);
      }
//   48   DINGBATS                           2700-27BF
      if (UnicodeSubsets.isSupported("48")) {
        test_defined(harness, 0x2700, 0x27bf, Character.UnicodeBlock.DINGBATS);
      }
      test_undefined(harness, 0x27c0, 0x27ff);
//   80   BRAILLE PATTERNS                   2800-28FF
      if (UnicodeSubsets.isSupported("80")) {
        test_defined(harness, 0x2800, 0x28ff, Character.UnicodeBlock.BRAILLE_PATTERNS);
      }
      test_undefined(harness, 0x2900, 0x2e7f);

//   79   CJK RADICALS SUPPLEMENT            2E80-2EFF
      if (UnicodeSubsets.isSupported("79")) {
        test_defined(harness, 0x2e80, 0x2eff, Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
      }
//   78   KANGXI RADICALS                    2F00-2FDF
      if (UnicodeSubsets.isSupported("78")) {
        test_defined(harness, 0x2f00, 0x2fdf, Character.UnicodeBlock.KANGXI_RADICALS);
      }
      test_undefined(harness, 0x2fe0, 0x2fef);
//  207   IDEOGRAPHIC DESCRIPTION CHARACTERS 2FF0-2FFF
      if (UnicodeSubsets.isSupported("207")) {
        test_defined(harness, 0x2ff0, 0x2fff, Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
      }

//   49   CJK SYMBOLS AND PUNCTUATION        3000-303F
      if (UnicodeSubsets.isSupported("49")) {
        test_defined(harness, 0x3000, 0x303f, Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
      }
//   50   HIRAGANA                           3040-309F
      if (UnicodeSubsets.isSupported("50")) {
        test_defined(harness, 0x3040, 0x309f, Character.UnicodeBlock.HIRAGANA);
      }
//   51   KATAKANA                           30A0-30FF
      if (UnicodeSubsets.isSupported("51")) {
        test_defined(harness, 0x30a0, 0x30ff, Character.UnicodeBlock.KATAKANA);
      }
//   52   BOPOMOFO                           3100-312F,31A0-31BF
      if (UnicodeSubsets.isSupported("52")) {
        test_defined(harness, 0x3100, 0x312f, Character.UnicodeBlock.BOPOMOFO);
      }
//   53   HANGUL COMPATIBILTY JAMO           3130-318F
      if (UnicodeSubsets.isSupported("53")) {
        test_defined(harness, 0x3130, 0x318f, Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO);
      }
      test_undefined(harness, 0x3190, 0x319f);
//   52   BOPOMOFO                           3100-312F,31A0-31BF
      if (UnicodeSubsets.isSupported("52")) {
        test_defined(harness, 0x31a0, 0x31bf, Character.UnicodeBlock.BOPOMOFO);
      }
      test_undefined(harness, 0x31c0, 0x31ff);     
//   55   ENCLOSED CJK LETTERS AND MONTHS    3200-32FF
      if (UnicodeSubsets.isSupported("55")) {
        test_defined(harness, 0x3200, 0x32ff, Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
      }
//   56   CJK COMPATIBILTY                   3300-33FF
      if (UnicodeSubsets.isSupported("56")) {
        test_defined(harness, 0x3300, 0x33ff, Character.UnicodeBlock.CJK_COMPATIBILITY);
      }
//   81   CJK UNIFIED IDEOGRAPHS EXTENSION A 3400-4DBF
      if (UnicodeSubsets.isSupported("81")) {
        test_defined(harness, 0x3400, 0x4dbf, Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
      }
      test_undefined(harness, 0x4dc0, 0x4dff);     
     
//   60   CJK UNIFIED IDEOGRAPHS             4E00-9FFF
      if (UnicodeSubsets.isSupported("55")) {
        test_defined(harness, 0x4e00, 0x9fff, Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
      }
//   76   YI SYLLABLES                       A000-A48F
      if (UnicodeSubsets.isSupported("76")) {
        test_defined(harness, 0xa000, 0xa48f, Character.UnicodeBlock.YI_SYLLABLES);
      }
//   77   YI RADICALS                        A490-A4CF
      if (UnicodeSubsets.isSupported("77")) {
        test_defined(harness, 0xa490, 0xa4cf, Character.UnicodeBlock.YI_RADICALS);
      }
//   71   HANGUL SYLLABLES                   AC00-D7A3
      if (UnicodeSubsets.isSupported("71")) {
        test_defined(harness, 0xac00, 0xd7a3, Character.UnicodeBlock.HANGUL_SYLLABLES);
      }
      test_undefined(harness, 0xd7a4, 0xdfff);     
//   61   PRIVATE USE AREA                   E000-F8FF
      if (UnicodeSubsets.isSupported("61")) {
        test_defined(harness, 0xe000, 0xf8ff, Character.UnicodeBlock.PRIVATE_USE_AREA);
      }
//   62   CJK COMPATIBILITY IDEOGRAPHS        F900-FAFF
      if (UnicodeSubsets.isSupported("62")) {
        test_defined(harness, 0xf900, 0xfaff, Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
      }
//   63   ALPHABETIC PRESENTATION FORMS      FB00-FB4F
      if (UnicodeSubsets.isSupported("63")) {
        test_defined(harness, 0xfb00, 0xfb4f, Character.UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS);
      }
//   64   ARABIC PRESENTATION FORMS-A        FB50-FDFF
      if (UnicodeSubsets.isSupported("64")) {
        test_defined(harness, 0xfb50, 0xfdff, Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A);
      }
      test_undefined(harness, 0xfe00, 0xfe1f);     
//   65   COMBINING HALF-MARKS               FE20-FE2F
      if (UnicodeSubsets.isSupported("65")) {
        test_defined(harness, 0xfe20, 0xfe2f, Character.UnicodeBlock.COMBINING_HALF_MARKS);
      }
//   66   CJK COMPATIBILTY FORMS             FE30-FE4F
      if (UnicodeSubsets.isSupported("66")) {
        test_defined(harness, 0xfe30, 0xfe4f, Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
      }
//   67   SMALL FORM VARIANTS                FE50-FE6F
      if (UnicodeSubsets.isSupported("67")) {
        test_defined(harness, 0xfe50, 0xfe6f, Character.UnicodeBlock.SMALL_FORM_VARIANTS);
      }
//   68   ARABIC PRESENTATION FORMS-B        FE70-FEFE
      if (UnicodeSubsets.isSupported("68")) {
        test_defined(harness, 0xfe70, 0xfefe, Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B);
      }
//   69   HALFWIDTH AND FULLWIDTH FORMS      FF00-FFEF
      if (UnicodeSubsets.isSupported("69")) {
        test_defined(harness, 0xff00, 0xffef, Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
      }
//   70   SPECIALS                           FFF0-FFFD
      if (UnicodeSubsets.isSupported("70")) {
        test_defined(harness, 0xfff0, 0xfffd, Character.UnicodeBlock.SPECIALS);
      }
  }
}

