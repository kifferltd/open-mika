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

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.UnicodeSubsets;

public class directionality14 implements Testlet
{

  private void test_directionality(TestHarness harness, int ch, byte expected) {
    byte dir = Character.getDirectionality((char)ch);
    harness.check (dir == expected, "Character " + Integer.toHexString(ch) + " should have directionality " + expected + ", but got " + dir);
  }
    
  // A quasi-random set of probes ...
  // I cannot find any AL, BN, LRE, LRO, RLE, RLO examples in our database.
  public void test (TestHarness harness) {
    test_directionality(harness, 0x0000, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    test_directionality(harness, 0x0009, Character.DIRECTIONALITY_SEGMENT_SEPARATOR);
    test_directionality(harness, 0x000c, Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR);
    test_directionality(harness, 0x0020, Character.DIRECTIONALITY_WHITESPACE);
    test_directionality(harness, 0x0023, Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR);
    test_directionality(harness, 0x002c, Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
    test_directionality(harness, 0x002f, Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR);
    test_directionality(harness, 0x0033, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
    test_directionality(harness, 0x003a, Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
    test_directionality(harness, 0x003d, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    test_directionality(harness, 0x0041, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    test_directionality(harness, 0x00a0, Character.DIRECTIONALITY_WHITESPACE);
    test_directionality(harness, 0x00ae, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    test_directionality(harness, 0x00b2, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
    if (UnicodeSubsets.isSupported("10")) {
      test_directionality(harness, 0x0484, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    if (UnicodeSubsets.isSupported("13")) {
      test_directionality(harness, 0x0591, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    if (UnicodeSubsets.isSupported("12")) {
      test_directionality(harness, 0x05d2, Character.DIRECTIONALITY_RIGHT_TO_LEFT);
    }
    if (UnicodeSubsets.isSupported("14")) {
      test_directionality(harness, 0x060c, Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
    }
    if (UnicodeSubsets.isSupported("15")) {
      test_directionality(harness, 0x0660, Character.DIRECTIONALITY_ARABIC_NUMBER);
    }
    if (UnicodeSubsets.isSupported("15")) {
      test_directionality(harness, 0x066a, Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR);
    }
    if (UnicodeSubsets.isSupported("15")) {
      test_directionality(harness, 0x066c, Character.DIRECTIONALITY_ARABIC_NUMBER);
    }
    if (UnicodeSubsets.isSupported("15")) {
      test_directionality(harness, 0x066d, Character.DIRECTIONALITY_RIGHT_TO_LEFT);
    }
    if (UnicodeSubsets.isSupported("15")) {
      test_directionality(harness, 0x06f6, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
    }
    if (UnicodeSubsets.isSupported("16")) {
      test_directionality(harness, 0x0903, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("17")) {
      test_directionality(harness, 0x09c1, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    if (UnicodeSubsets.isSupported("20")) {
      test_directionality(harness, 0x0b4d, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    if (UnicodeSubsets.isSupported("30")) {
      test_directionality(harness, 0x1e11, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("31")) {
      test_directionality(harness, 0x1fc1, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    test_directionality(harness, 0x200f, Character.DIRECTIONALITY_RIGHT_TO_LEFT);
    test_directionality(harness, 0x2006, Character.DIRECTIONALITY_WHITESPACE);
    test_directionality(harness, 0x2007, Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
    test_directionality(harness, 0x2029, Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR);
    test_directionality(harness, 0x2087, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
    if (UnicodeSubsets.isSupported("40")) {
      test_directionality(harness, 0x233a, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("43")) {
      test_directionality(harness, 0x246c, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
    }
    if (UnicodeSubsets.isSupported("49")) {
      test_directionality(harness, 0x3000, Character.DIRECTIONALITY_WHITESPACE);
    }
    if (UnicodeSubsets.isSupported("53")) {
      test_directionality(harness, 0x314e, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("56")) {
      test_directionality(harness, 0x3363, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("62")) {
      test_directionality(harness, 0xfa21, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
    if (UnicodeSubsets.isSupported("63")) {
      test_directionality(harness, 0xfb1e, Character.DIRECTIONALITY_OTHER_NEUTRALS);
    }
    if (UnicodeSubsets.isSupported("63")) {
      test_directionality(harness, 0xfb1f, Character.DIRECTIONALITY_RIGHT_TO_LEFT);
    }
    if (UnicodeSubsets.isSupported("67")) {
      test_directionality(harness, 0xfe55, Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR);
    }
    if (UnicodeSubsets.isSupported("69")) {
      test_directionality(harness, 0xff0f, Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR);
      test_directionality(harness, 0xff15, Character.DIRECTIONALITY_EUROPEAN_NUMBER);
      test_directionality(harness, 0xffe1, Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR);
      test_directionality(harness, 0xffd5, Character.DIRECTIONALITY_LEFT_TO_RIGHT);
    }
  }
}

