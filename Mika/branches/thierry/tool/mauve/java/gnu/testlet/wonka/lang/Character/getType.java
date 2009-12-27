// Tags: JDK1.0

// Copyright (C) 1998, 1999 Cygnus Solutions

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

public class getType implements Testlet
{
  public static void p (TestHarness harness, char c, String expected)
    {
      String s;
      switch (Character.getType (c))
	{
	case Character.SPACE_SEPARATOR:
	  s = "space_separator";
	  break;
	case Character.LINE_SEPARATOR:
	  s = "line_separator";
	  break;
	case Character.PARAGRAPH_SEPARATOR:
	  s = "paragraph_separator";
	  break;
	case Character.UPPERCASE_LETTER:
	  s = "uppercase_letter";
	  break;
	case Character.LOWERCASE_LETTER:
	  s = "lowercase_letter";
	  break;
	case Character.TITLECASE_LETTER:
	  s = "titlecase_letter";
	  break;
	case Character.MODIFIER_LETTER:
	  s = "modifier_letter";
	  break;
	case Character.OTHER_LETTER:
	  s = "other_letter";
	  break;
	case Character.DECIMAL_DIGIT_NUMBER:
	  s = "decimal_digit_number";
	  break;
	case Character.LETTER_NUMBER:
	  s = "letter_number";
	  break;
	case Character.OTHER_NUMBER:
	  s = "other_number";
	  break;
	case Character.NON_SPACING_MARK:
	  s = "non_spacing_mark";
	  break;
	case Character.ENCLOSING_MARK:
	  s = "enclosing_mark";
	  break;
	case Character.COMBINING_SPACING_MARK:
	  s = "combining_spacing_mark";
	  break;
	case Character.DASH_PUNCTUATION:
	  s = "dash_punctuation";
	  break;
	case Character.START_PUNCTUATION:
	  s = "start_punctuation";
	  break;
	case Character.END_PUNCTUATION:
	  s = "end_punctuation";
	  break;
	case Character.INITIAL_QUOTE_PUNCTUATION:
	  s = "initial_quote";
	  break;
	case Character.FINAL_QUOTE_PUNCTUATION:
	  s = "final_quote";
	  break;
	case Character.CONNECTOR_PUNCTUATION:
	  s = "connector_punctuation";
	  break;
	case Character.OTHER_PUNCTUATION:
	  s = "other_punctuation";
	  break;
	case Character.MATH_SYMBOL:
	  s = "math_symbol";
	  break;
	case Character.CURRENCY_SYMBOL:
	  s = "currency_symbol";
	  break;
	case Character.MODIFIER_SYMBOL:
	  s = "modifier_symbol";
	  break;
	case Character.OTHER_SYMBOL:
	  s = "other_symbol";
	  break;
	case Character.CONTROL:
	  s = "control";
	  break;
	case Character.FORMAT:
	  s = "format";
	  break;
	case Character.UNASSIGNED:
	  s = "unassigned";
	  break;
	case Character.PRIVATE_USE:
	  s = "private_use";
	  break;
	case Character.SURROGATE:
	  s = "surrogate";
	  break;
	default:
	  s = "???";
	  break;
	}

      harness.check (s, expected);
    }

  public void test (TestHarness harness)
    {
      p (harness, ' ', "space_separator");
      p (harness, '\u2028', "line_separator");
      p (harness, '\u2029', "paragraph_separator");
      if (UnicodeSubsets.isSupported("36")) {
        p (harness, '\u2110', "uppercase_letter");
      }
      p (harness, 'Z', "uppercase_letter");
      if (UnicodeSubsets.isSupported("69")) {
        p (harness, '\uff44', "lowercase_letter");
      }
      p (harness, 'z', "lowercase_letter");
      if (UnicodeSubsets.isSupported("31")) {
        p (harness, '\u1fe4', "lowercase_letter");
      }
      if (UnicodeSubsets.isSupported("4")) {
        p (harness, '\u01c5', "titlecase_letter");
      }
      if (UnicodeSubsets.isSupported("49")) {
        p (harness, '\u3005', "modifier_letter");
      }
      if (UnicodeSubsets.isSupported("4")) {
        p (harness, '\u01bf', "other_letter");
      }
      if (UnicodeSubsets.isSupported("15")) {
        p (harness, '\u0666', "decimal_digit_number");
      }
      if (UnicodeSubsets.isSupported("37")) {
        p (harness, '\u216f', "letter_number");
      }
      if (UnicodeSubsets.isSupported("72") || UnicodeSubsets.isSupported("91")) {
        p (harness, '\u0f32', "other_number");
      }
      if (UnicodeSubsets.isSupported("72") || UnicodeSubsets.isSupported("91")) {
        p (harness, '\u0f35', "non_spacing_mark");
      }
      if (UnicodeSubsets.isSupported("15")) {
        p (harness, '\u06de', "enclosing_mark");
      }
      if (UnicodeSubsets.isSupported("16")) {
        p (harness, '\u0903', "combining_spacing_mark");
      }
      p (harness, '-', "dash_punctuation");
      if (UnicodeSubsets.isSupported("67")) {
        p (harness, '\ufe59', "start_punctuation");
      }
      p (harness, '\u00ab', "initial_quote");
      if (UnicodeSubsets.isSupported("72") || UnicodeSubsets.isSupported("91")) {
        p (harness, '\u0f3b', "end_punctuation");
      }
      p (harness, '\u2019', "final_quote");
      if (UnicodeSubsets.isSupported("69")) {
        p (harness, '\uff3f', "connector_punctuation");
      }
      if (UnicodeSubsets.isSupported("39")) {
        p (harness, '\u2202', "math_symbol");
      }
      p (harness, '\u20ab', "currency_symbol");
      if (UnicodeSubsets.isSupported("6")) {
        p (harness, '\u02c2', "modifier_symbol");
      }
      if (UnicodeSubsets.isSupported("19")) {
        p (harness, '\u0ad0', "other_letter");
      }
      p (harness, '\u20ab', "currency_symbol");
      if (UnicodeSubsets.isSupported("20")) {
        p (harness, '\u0b70', "other_symbol");
      }
      p (harness, '\u009f', "control");
      if (UnicodeSubsets.isSupported("200")) {
        p (harness, '\ufeff', "format");
      }
      p (harness, '\uffff', "unassigned");
      p (harness, '\uffef', "unassigned");
      if (UnicodeSubsets.isSupported("61")) {
        p (harness, '\uebeb', "private_use");
      }
      // We don't support surrogates
      // p (harness, '\udb9c', "surrogate");
      if (UnicodeSubsets.isSupported("43")) {
        p (harness, '\u249f', "other_symbol");
      }
      if (UnicodeSubsets.isSupported("36")) {
        p (harness, '\u2102', "uppercase_letter");
      }
    }
}
