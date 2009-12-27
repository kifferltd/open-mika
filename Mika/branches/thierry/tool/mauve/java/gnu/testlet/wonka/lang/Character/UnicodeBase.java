// Uses: CharInfo

/* Copyright (C) 1999 Artur Biesiadowski
   Copyright (C) 2004 Stephen Crawley 

This file is part of Mauve.

Mauve is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

Mauve is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Mauve; see the file COPYING.  If not, write to
the Free Software Foundation, 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.  */

package gnu.testlet.wonka.lang.Character;

import java.io.*;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.ResourceNotFoundException;

/*
  MISSING:
  Instance tests
  (constructor, charValue, serialization): should be in other file
*/

public abstract class UnicodeBase implements Testlet
{
  public static boolean testDeprecated;
  public static boolean verbose;
  public static boolean benchmark;

  public CharInfo[] chars = new CharInfo[0x10000];
  public int failures;
  public int tests;
  TestHarness harness;


  public UnicodeBase()
  {
  }

  public UnicodeBase(TestHarness aHarness, String filename) 
    throws IOException, ResourceNotFoundException
  {
    harness = aHarness;
    Reader bir = new InputStreamReader(
      this.getClass().getResourceAsStream("/" +filename));
    harness.debug("Reading unicode database...");
    while (bir.ready())
      {
	String str;
	CharInfo ci = new CharInfo();
	// 0 - Code value
	str = getNext(bir);
	int code = (char) Integer.parseInt(str, 16);
	// 1 - Character name
	ci.name = getNext(bir);
	// 2 - General category
	ci.category = getNext(bir);
	// 3 - Canonical combining classes
	getNext(bir);
	// 4 - Bidirectional category
	getNext(bir);
	// 5 - Character decomposition mapping
	getNext(bir);
	// 6 - Decimal digit value
	str = getNext(bir);
	if (!str.equals(""))
	  ci.decimalDigit = Integer.parseInt(str, 10);
	else
	  ci.decimalDigit = -1;
	// 7 - Digit value
	str = getNext(bir);
	if (!str.equals(""))
	  ci.digit = Integer.parseInt(str, 10);
	else
	  ci.digit = -1;
	// 8 - Numeric value
	str = getNext(bir);
	if (str.equals(""))
	  {
	    ci.numericValue = -1;
	  }
	else
	  {
	    try {
	      ci.numericValue = Integer.parseInt(str, 10);
	      if (ci.numericValue < 0)
		ci.numericValue = -2;
	    } 
	    catch (NumberFormatException e)
	      {
		ci.numericValue = -2;
	      }
	  }
	// 9 - Mirrored
	getNext(bir);
	// 10 - Unicode 1.0 name
	getNext(bir);
	// 11 - ISO 10646 comment field
	getNext(bir);
	// 12 - Upper case mapping
	str = getNext(bir);
	if (!str.equals(""))
	  ci.uppercase = (char) Integer.parseInt(str, 16);
	// 13 - Lower case mapping
	str = getNext(bir);
	if (!str.equals(""))
	  ci.lowercase = (char) Integer.parseInt(str, 16);
	// 14 - Title case mapping
	str = getNext(bir);
	if (!str.equals(""))
	  ci.titlecase = (char) Integer.parseInt(str, 16);

	// Character.digit() only treats "Nd" as decimal digits, not "No" 
	// or "Nl".  Tweak the character defns accordingly.
	if (ci.digit != -1 && !("Nd".equals(ci.category))) 
	  ci.digit = -1;
	chars[code] = ci;
      }

    // Fill in the character ranges that are reserved in Unicode 3.0
    CharInfo ch = new CharInfo();
    ch.name = "CJK Ideograph";
    ch.category = "Lo";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0x4E01; i <= 0x9FA4; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "CJK Ideograph Extension A";
    ch.category = "Lo";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0x3400; i <= 0x4DB5; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "Hangul Syllable";
    ch.category = "Lo";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0xAC01; i <= 0xD7A2; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "CJK Compatibility Ideograph";
    ch.category = "Lo";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0xF901; i <= 0xFA2C; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "Surrogate";
    ch.category= "Cs";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0xD800; i <= 0xDFFFl; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "Private Use";
    ch.category = "Co";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0xE000; i <= 0xF8FF; i++)
      {
	chars[i] = ch;
      }

    ch = new CharInfo();
    ch.name = "UNDEFINED";
    ch.category = "Cn";
    ch.decimalDigit = -1;
    ch.digit = -1;
    ch.numericValue = -1;
    for (int i = 0; i <= 0xFFFF; i++)
      {
	if (chars[i] == null)
	  chars[i] = ch;
      }

    /*
      Override the character definitions for Latin letters with digit
      values to cope with the semantics of Character.digit(), etc.
      It is not stated that A-Z and a-z should
      have getNumericValue() (as it is in digit())
    */
    for (int i = 'A'; i <= 'Z'; i++)
      {
	chars[i].digit = i - 'A' + 10;
	chars[i].numericValue = chars[i].digit; // ??
      }
    for (int i = 'a'; i <= 'z'; i++)
      {
	chars[i].digit = i - 'a' + 10;
	chars[i].numericValue = chars[i].digit; // ??
      }
    for (int i = 0xFF21; i <= 0xFF3A; i++)
      {
	chars[i].digit = i - 0xFF21 + 10;
	chars[i].numericValue = chars[i].digit; // ??
      }
    for (int i = 0xFF41; i <= 0xFF5A; i++)
      {
	chars[i].digit = i - 0xFF41 + 10;
	chars[i].numericValue = chars[i].digit; // ??
      }
    
    harness.debug("done");
  }

  private String getNext(Reader r) throws IOException
  {
    StringBuffer sb = new StringBuffer();
    while (r.ready())
      {
	char ch = (char) r.read();
	if (ch == '\r')
	  {
	    continue;
	  }
	else if (ch == ';' ||  ch == '\n')
	  {
	    return sb.toString();
	  }
	else
	  sb.append(ch);
      }
    return sb.toString();
  }

  public String stringChar(int ch)
  {
    return "Character " + Integer.toString(ch,16) + ":" + chars[ch].name;
  }
	
  protected void reportError( String what)
  {
    harness.check(false, what);
  }
	
  protected void reportError( int ch, String what)
  {
    harness.check(false, stringChar(ch) +" incorrectly reported as " + what);
  }
	
  protected void checkPassed()
  {
    harness.check(true);
  }

  public boolean range(int mid, int low, int high)
  {
    return (mid >= low && mid <= high);
  }

  public boolean ignorable(int i)
  {
    return (range(i, 0x0000, 0x0008) ||
	    range(i, 0x000E, 0x001B) ||
	    range(i, 0x007f, 0x009f) ||
	    "Cf".equals(chars[i].category));
  }

  public boolean whitespace(int i) 
  {
    return ((chars[i].category.charAt(0) == 'Z' && 
	     i != 0x00a0 && i != 0x2007 && i != 0x202f) ||
	    range(i, 0x0009, 0x000D) || 
	    range(i, 0x001C, 0x001F));
  }

  public boolean identifierStart(int i) 
  {
    return ("Ll".equals(chars[i].category) || 
	    "Lu".equals(chars[i].category) || 
	    "Lt".equals(chars[i].category) || 
	    "Lm".equals(chars[i].category) || 
	    "Lo".equals(chars[i].category) || 
	    "Nl".equals(chars[i].category) || 
	    "Sc".equals(chars[i].category) || 
	    "Pc".equals(chars[i].category));
  }

  public boolean unicodeIdentifierStart(int i) 
  {
    return ("Ll".equals(chars[i].category) || 
	    "Lu".equals(chars[i].category) || 
	    "Lt".equals(chars[i].category) || 
	    "Lm".equals(chars[i].category) || 
	    "Lo".equals(chars[i].category) || 
	    "Nl".equals(chars[i].category));
  }

  public void performTests()
  {
    for (int x = 0; x <= 0xffff; x++)
      {

	// isLowerCase
	char i = (char) x;
	if ("Ll".equals(chars[i].category) != Character.isLowerCase((char) i))
	  {
	    reportError(i,
			(Character.isLowerCase((char) i) ? "lowercase" :
			 "not-lowercase"));

	  }
	else checkPassed();

	// isUpperCase
	if ("Lu".equals(chars[i].category) != Character.isUpperCase((char) i))
	  {
	    reportError(i,
			(Character.isUpperCase((char) i) ? "uppercase" :
			 "not-uppercase"));
	  }
	else checkPassed();

	// isTitleCase
	if ( "Lt".equals(chars[i].category) !=
	      Character.isTitleCase((char) i))
	  {
	    reportError(i,
			(Character.isTitleCase((char) i) ? "titlecase" :
			 "not-titlecase"));
	  }
	else checkPassed();

	// isDigit
	if ("Nd".equals(chars[i].category) != Character.isDigit((char) i))
	  {
	    reportError(i,
			(Character.isDigit((char) i) ? "digit" : "not-digit"));
	  }
	else checkPassed();

	// isDefined
	if (!chars[i].category.equals("Cn") != Character.isDefined((char) i))
	  {
	    reportError(i,
			(Character.isDefined((char) i) ? "defined" : 
			 "not-defined"));
	  }
	else checkPassed();

	// isLetter
	if ((chars[i].category.charAt(0) == 'L') != 
	    Character.isLetter((char) i))
	  {
	    reportError(i,
			(Character.isLetter((char) i) ? "letter" : 
			 "not-letter"));
	  }
	else checkPassed();

	// isLetterOrDigit
	if (Character.isLetterOrDigit(i) !=
	    (Character.isLetter(i) || Character.isDigit(i)))
	  {
	    reportError(i,
			(Character.isLetterOrDigit(i) ? "letterordigit" :
			 "not-letterordigit"));
	  }
	else checkPassed();

	// isSpaceChar
	if ((chars[i].category.charAt(0) == 'Z') != Character.isSpaceChar(i))
	  {
	    reportError(i,
			(Character.isSpaceChar(i) ? "spacechar" : 
			 "not-spacechar"));
	  }
	else checkPassed();

	// isWhiteSpace
	if (whitespace(i) != Character.isWhitespace(i))
	  {
	    reportError(i,
			Character.isWhitespace(i) ? "whitespace" : 
			"not-whitespace");
	  }
	else checkPassed();

	// isISOControl
	if (((i <= 0x001F) || range(i, 0x007F, 0x009F)) !=
	    Character.isISOControl(i))
	  {
	    reportError(i,
			Character.isISOControl(i) ? "isocontrol" :
			"not-isocontrol");
	  }
	else checkPassed();

	int type = Character.getType(i);
	String typeStr = null;
	switch (type)
	  {
	  case Character.UNASSIGNED: typeStr = "Cn"; break;
	  case Character.UPPERCASE_LETTER: typeStr = "Lu"; break;
	  case Character.LOWERCASE_LETTER: typeStr = "Ll"; break;
	  case Character.TITLECASE_LETTER: typeStr = "Lt"; break;
	  case Character.MODIFIER_LETTER: typeStr = "Lm"; break;
	  case Character.OTHER_LETTER: typeStr = "Lo"; break;
	  case Character.NON_SPACING_MARK: typeStr = "Mn"; break;
	  case Character.ENCLOSING_MARK: typeStr = "Me"; break;
	  case Character.COMBINING_SPACING_MARK: typeStr = "Mc"; break;
	  case Character.DECIMAL_DIGIT_NUMBER: typeStr = "Nd"; break;
	  case Character.LETTER_NUMBER: typeStr = "Nl"; break;
	  case Character.OTHER_NUMBER: typeStr = "No"; break;
	  case Character.SPACE_SEPARATOR: typeStr = "Zs"; break;
	  case Character.LINE_SEPARATOR: typeStr = "Zl"; break;
	  case Character.PARAGRAPH_SEPARATOR: typeStr = "Zp"; break;
	  case Character.CONTROL: typeStr = "Cc"; break;
	  case Character.FORMAT: typeStr = "Cf"; break;
	  case Character.PRIVATE_USE: typeStr = "Co"; break;
	  case Character.SURROGATE: typeStr = "Cs"; break;
	  case Character.DASH_PUNCTUATION: typeStr = "Pd"; break;
	  case Character.START_PUNCTUATION: typeStr = "Ps"; break;
	  case Character.END_PUNCTUATION: typeStr = "Pe"; break;
	  case Character.CONNECTOR_PUNCTUATION: typeStr = "Pc"; break;
	  case Character.FINAL_QUOTE_PUNCTUATION: typeStr = "Pf"; break;
	  case Character.INITIAL_QUOTE_PUNCTUATION: typeStr = "Pi"; break;
	  case Character.OTHER_PUNCTUATION: typeStr = "Po"; break;
	  case Character.MATH_SYMBOL: typeStr = "Sm"; break;
	  case Character.CURRENCY_SYMBOL: typeStr = "Sc"; break;
	  case Character.MODIFIER_SYMBOL: typeStr = "Sk"; break;
	  case Character.OTHER_SYMBOL: typeStr = "So"; break;
	  default: typeStr = "ERROR (" + type + ")"; break;
	  }

	if (!(chars[i].category.equals(typeStr) ||
	      (typeStr.equals("Ps") && chars[i].category.equals("Pi")) ||
	      (typeStr.equals("Pe") && chars[i].category.equals("Pf"))))
	  {
	    reportError(stringChar(i) + " is reported to be type " + typeStr +
			" instead of " + chars[i].category);
	  }
	else checkPassed();

	// isJavaIdentifierStart
	if (identifierStart(i) != Character.isJavaIdentifierStart(i))
	  {
	    reportError(i,
			Character.isJavaIdentifierStart(i) ?
			"javaindentifierstart" : "not-javaidentifierstart");
	  }
	else checkPassed();

	// isJavaIdentifierPart
	boolean shouldbe = false;
	typeStr = chars[i].category;
	if ((typeStr.charAt(0) == 'L' ||
	     typeStr.equals("Sc") ||
	     typeStr.equals("Pc") ||
	     typeStr.equals("Nd") ||
	     typeStr.equals("Nl") ||
	     typeStr.equals("Mc") ||
	     typeStr.equals("Mn") ||
	     typeStr.equals("Cf") ||
	     (typeStr.equals("Cc") && ignorable(i))) != 
	    Character.isJavaIdentifierPart(i))
	  {
	    reportError(i,
			Character.isJavaIdentifierPart(i) ? 
			"javaidentifierpart" : "not-javaidentifierpart");
	  }
	else checkPassed();
	
	//isUnicodeIdentifierStart
	if (unicodeIdentifierStart(i) != Character.isUnicodeIdentifierStart(i))
	  {
	    reportError(i,
			Character.isUnicodeIdentifierStart(i) ? 
			"unicodeidentifierstart" : 
			"not-unicodeidentifierstart");
	  }
	else checkPassed();
	
	//isUnicodeIdentifierPart
	shouldbe = false;
	typeStr = chars[i].category;
	if ((typeStr.charAt(0) == 'L' ||
	     typeStr.equals("Pc") ||
	     typeStr.equals("Nd") ||
	     typeStr.equals("Nl") ||
	     typeStr.equals("Mc") ||
	     typeStr.equals("Mn") ||
	     typeStr.equals("Cf") ||
	     (typeStr.equals("Cc") && ignorable(i))) != 
	    Character.isUnicodeIdentifierPart(i))
	  {
	    reportError(i,
			Character.isUnicodeIdentifierPart(i) ?
			"unicodeidentifierpart" : "not-unicodeidentifierpart");
	  }
	else checkPassed();


	//isIdentifierIgnorable
	if (ignorable(i) != Character.isIdentifierIgnorable(i))
	  {
	    reportError(i,
			Character.isIdentifierIgnorable(i) ? 
			"identifierignorable": "not-identifierignorable");
	  }
	else checkPassed();

	// toLowerCase
	char cs = (chars[i].lowercase != 0 ?
		   chars[i].lowercase : i);
	if (Character.toLowerCase(i) != cs)
	  {
	    reportError(stringChar(i) + " has wrong lowercase form of " +
			stringChar(Character.toLowerCase(i)) +" instead of " +
			stringChar(cs));
	  }
	else checkPassed();
	
	// toUpperCase
	cs =(chars[i].uppercase != 0 ? 
	     chars[i].uppercase : i);
	if (Character.toUpperCase(i) != cs)
	  {
	    reportError(stringChar(i) +
			" has wrong uppercase form of " +
			stringChar(Character.toUpperCase(i)) +
			" instead of " +
			stringChar(cs));
	  }
	else checkPassed();
	
	// toTitleCase
	cs = (chars[i].titlecase != 0 ? 
	      chars[i].titlecase :
	      (chars[i].uppercase != 0 ? 
	       chars[i].uppercase : i));
	
	if ("Lt".equals(chars[i].category))
	  {
	    cs = i;
	  }
	
	if (Character.toTitleCase(i) != cs)
	  {
	    reportError(stringChar(i) +
			" has wrong titlecase form of " +
			stringChar(Character.toTitleCase(i)) +
			" instead of " + 
			stringChar(cs));
	  }
	else checkPassed();
	
	// digit
	for (int radix = Character.MIN_RADIX; 
	     radix <= Character.MAX_RADIX;
	     radix++)
	  {
	    int digit = chars[i].digit;	    
	    if (digit >= radix) 
	      digit = -1;
	    if (Character.digit(i, radix) != digit)
	      {
		reportError(stringChar(i) + " has wrong digit form of " +
			    Character.digit(i, radix) + " for radix " + 
			    radix + " instead of " + digit +
			    "(" + chars[i].digit + ")");
	      }
	    else checkPassed();
	  }
	
	// getNumericValue
	if (chars[i].numericValue != Character.getNumericValue(i))
	  {
	    reportError(stringChar(i) + " has wrong numeric value of " +
			Character.getNumericValue(i) + " instead of " + 
			chars[i].numericValue);
	  }
	
	
	
	if (testDeprecated)
	  {
	    
	    // isJavaLetter
	    if ((i == '$' || i == '_' || Character.isLetter(i)) !=
		Character.isJavaLetter(i))
	      {
		reportError(i,
			    (Character.isJavaLetter(i)? "javaletter" : 
			     "not-javaletter"));
	      }
	    else checkPassed();
	    
	    // isJavaLetterOrDigit
	    if ((Character.isJavaLetter(i) || Character.isDigit(i) ||
		 i == '$' || i == '_') !=
		Character.isJavaLetterOrDigit(i)
		)
	      {
		reportError(i,
			    (Character.isJavaLetterOrDigit(i) ?
			     "javaletterordigit" : "not-javaletterordigit"));
	      }
	    else checkPassed();
	    
	    // isSpace
	    if (((i == ' ' || i == '\t' || i == '\n' || i == '\r' ||
		  i == '\f')) != Character.isSpace(i))
	      {
		reportError(i,
			    (Character.isSpace(i) ? "space" : "non-space"));
	      }
	    else checkPassed();
	  } // testDeprecated
	
      } // for
    
    // forDigit
    for (int r = -100; r < 100; r++)
      {
	for (int d = -100; d < 100; d++)
	  {
	    char dch = Character.forDigit(d,r);
	    char wantch = 0;
	    if (range(r, Character.MIN_RADIX, Character.MAX_RADIX) &&
		 range(d, 0, r - 1))
	      {
		if (d < 10)
		  {
		    wantch = (char) ('0' + (char) d);
		  }
		else if (d < 36)
		  {
		    wantch = (char) ('a' + d - 10);
		  }
	      }

	    if (dch != wantch)
	      {
		reportError("Error in forDigit(" + d +
			     "," + r + "), got " + dch + " wanted " +
			     wantch);
	      }
	    else checkPassed();
	  }
      }
  }

}
