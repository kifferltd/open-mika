/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2011 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
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

package java.lang;

import java.util.HashMap;

public final class Character implements java.io.Serializable,Comparable {

  private static final long serialVersionUID = 3786198910865385080L;

  public static final char MIN_VALUE = '\u0000';
  public static final char MAX_VALUE = '\uffff';
  public static final int MIN_RADIX = 2;
  public static final int MAX_RADIX = 36;
  public static final Class TYPE = Character.getWrappedClass();
  public static final byte SPACE_SEPARATOR = 12;
  public static final byte LINE_SEPARATOR = 13;
  public static final byte PARAGRAPH_SEPARATOR = 14;
  public static final byte UPPERCASE_LETTER = 1;
  public static final byte LOWERCASE_LETTER = 2;
  public static final byte TITLECASE_LETTER = 3;
  public static final byte MODIFIER_LETTER = 4;
  public static final byte OTHER_LETTER = 5;
  public static final byte DECIMAL_DIGIT_NUMBER = 9;
  public static final byte LETTER_NUMBER = 10;
  public static final byte OTHER_NUMBER = 11;
  public static final byte NON_SPACING_MARK = 6;
  public static final byte ENCLOSING_MARK = 7;
  public static final byte COMBINING_SPACING_MARK = 8;
  public static final byte DASH_PUNCTUATION = 20;
  public static final byte START_PUNCTUATION = 21;
  public static final byte END_PUNCTUATION = 22;
  public static final byte CONNECTOR_PUNCTUATION = 23;
  public static final byte OTHER_PUNCTUATION = 24;
  public static final byte MATH_SYMBOL = 25;
  public static final byte CURRENCY_SYMBOL = 26;
  public static final byte MODIFIER_SYMBOL = 27;
  public static final byte OTHER_SYMBOL = 28;
  public static final byte CONTROL = 15;
  public static final byte FORMAT = 16;
  public static final byte UNASSIGNED = 0;
  public static final byte PRIVATE_USE = 18;
  public static final byte SURROGATE = 19;
// 1.4
  public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;
  public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;
  public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;
  public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;
  public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;
  public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;
  public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;
  public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;
  public static final byte DIRECTIONALITY_UNDEFINED = -1;
  public static final byte DIRECTIONALITY_WHITESPACE = 12;
  public static final byte FINAL_QUOTE_PUNCTUATION = 30;
  public static final byte INITIAL_QUOTE_PUNCTUATION = 29;


  /**
   ** The char which is wrapped by this Character.
   */
  private final char value;

  /**
   ** Create our native tables.
   */
  private static native void createTables();
  static {
    createTables();
  }

  /**
   ** Convert a char to a String.
   */
  public static String toString(char c) {
    char[] onechar = new char[1];

    onechar[0] = c;

    return new String(onechar);
  }

  /**
   ** Construct a Character wrapping a particular char
   */
  public Character(char value) {
    this.value = value;
  }

  /**
   ** The string representattion is simply a one-character string.
   */
  public String toString() {
    char[] onechar = new char[1];

    onechar[0] = this.value;

    return new String(onechar);
  }

  /**
   ** This Character is `equal' to any other Character which wraps the same
   ** char, and to nothing else.
   */
  public boolean equals(Object obj) {
    if (obj == null) {

      return false;

    }

    try {
      Character c = (Character)obj;

      return this.value == c.value;

    }
    catch (ClassCastException cce) {

      return false;

    }
  }

  /**
   ** The hashcode is equal to the wrapped char.
   */
  public int hashCode() {
    return value;

  }

  /**
   ** Get the wrapped char.
   */
  public char charValue() {

    return value;

  }

  /**
   ** Compare the values of the wrapped char's.
   */
  public int compareTo(Character anotherCharacter) {
    char other_value = anotherCharacter.value;

    if (value > other_value) return 1;

    if (value < other_value) return -1;

    return 0;
  }

  /**
   ** Will throw a ClassCastException if object of comparison is not a Character.
   */
  public int compareTo(Object something) throws ClassCastException {
    return compareTo((Character)something);
  }

  /**
   ** Consult native code tables to see if the char is assigned in UnicodeData.txt.
   */
  public native static boolean isDefined(char ch);

  /**
   ** Consult native code tables to see if the char is a lower-case letter
   ** (category Ll).
   */
  public native static boolean isLowerCase(char ch);

  /**
   ** Consult native code tables to see if the char is an upper-case letter
   ** (category Lu).
   */
  public native static boolean isUpperCase(char ch);

  /**
   ** Consult native code tables to see if the char is a title-case letter
   ** (category Lt).
   */
  public native static boolean isTitleCase(char ch);

  /**
   ** Consult native code tables to see if the char is a letter (general
   ** category code beginning with `L').
   */
  public native static boolean isLetter(char ch);

  /**
   ** Consult native code tables to see if the char is a digit (general
   ** category code `Nd').
   */
  public native static boolean isDigit(char ch);

  /**
   ** Consult native code tables to see if the char is whitespace (is one
   ** of 0009, 000A, 000B, 000C, 000D, 001C, 001D, 001E, or 001F, or has
   ** a general category code in UnicodeData.txt beginning with `Z' and
   ** does not have a compatibility mapping marked <nobreak>).
   */
  public native static boolean isWhitespace(char ch);

  /**
   ** A char is a letter or a digit if it is a letter or a digit.
   */
  public static boolean isLetterOrDigit(char ch) {
    return isLetter(ch) || isDigit(ch);
  }

  /**
   ** A `Java letter' is a letter or $_.
   */
  public static boolean isJavaLetter(char ch) {

    return (ch=='$') || (ch=='_') || Character.isLetter(ch);

  }

  /**
   ** A `Java letter or digit' is a Java letter or a digit.
   */
  public static boolean isJavaLetterOrDigit(char ch) {

    return (ch=='$') || (ch=='_') || Character.isLetterOrDigit(ch);

  }

  // DEPRECATED - use isWhitespace(char)
  public static boolean isSpace(char ch) {
    return ch=='\n' || ch=='\t' ||ch=='\f' || ch=='\r' || ch==' ';
  }

  /**
   ** Consult native code tables to see if the char is a formatting char
   ** (has general category code `Cf' in UnicodeData.txt).
   */
  private native static boolean isFormat(char ch);

  public native static char toLowerCase(char ch);

  public native static char toUpperCase(char ch);

  public native static char toTitleCase(char ch);

  private static native int numericValue(char ch);

  public native static int digit(char ch, int radix);

  public static int getNumericValue(char ch) {
    int result = digit(ch, 36);

    if (result == -1) {
      result = numericValue(ch);
    }

    return result;
  }

  public static boolean isIdentifierIgnorable(char ch) {
    return ( 
         (ch < ' ' && !Character.isWhitespace(ch))
      || (ch > '\u007e' && ch < '\u00a0') 
      || isFormat(ch)
    );
  }

  public static boolean isJavaIdentifierPart(char ch) {
    if (isLetterOrDigit(ch)) {

      return true;

    }
       
    int type = getType(ch);

    return
      type == CURRENCY_SYMBOL
      || type == CONNECTOR_PUNCTUATION
      || type == LETTER_NUMBER
      || type == COMBINING_SPACING_MARK
      || type == NON_SPACING_MARK
      || isIdentifierIgnorable(ch);

  }

  public static boolean isJavaIdentifierStart(char ch) {
    if (isLetter(ch)) {

      return true;

    }
       
    int type = getType(ch);

    return
      type == CURRENCY_SYMBOL
      || type == LETTER_NUMBER
      || type == CONNECTOR_PUNCTUATION;

  }

  public static boolean isUnicodeIdentifierPart(char ch) {
    if (isLetterOrDigit(ch)) {

      return true;

    }
       
    int type = getType(ch);

    return
      type == CONNECTOR_PUNCTUATION
      || type == LETTER_NUMBER
      || type == COMBINING_SPACING_MARK
      || type == NON_SPACING_MARK
      || isIdentifierIgnorable(ch);

  }

  public static boolean isUnicodeIdentifierStart(char ch) {
    return isLetter(ch) || getType(ch) == LETTER_NUMBER;
  }

  public static boolean isSpaceChar(char ch) {
    int type = getType(ch);

    return
      type == SPACE_SEPARATOR
      || type == LINE_SEPARATOR
      || type == PARAGRAPH_SEPARATOR;
  }

  public static boolean isISOControl(char ch) {
    return (ch <= 0x001f) || ((ch >= 0x007f) && (ch <= 0x009f));
  }

  public static native byte getDirectionality(char ch);

  public static native boolean isMirrored(char ch);

  public static int getType(char ch) {
    String cat = getCategory(ch);
    char cat0 = cat.charAt(0);
    char cat1 = cat.charAt(1);

    switch (cat0) {
      case 'C':
        switch (cat1) {
          case 'c':
            return CONTROL;

          case 'f':
            return FORMAT;

          case 'n':
            return UNASSIGNED;

          case 'o':
            return PRIVATE_USE;

          case 's':
            return SURROGATE;

        }
      case 'L':
        switch (cat1) {
          case 'l':
            return LOWERCASE_LETTER;

          case 'm':
            return MODIFIER_LETTER;

          case 'o':
            return OTHER_LETTER;

          case 't':
            return TITLECASE_LETTER;

          case 'u':
            return UPPERCASE_LETTER;

        }
      case 'M':
        switch (cat1) {
          case 'c':
            return COMBINING_SPACING_MARK;

          case 'e':
            return ENCLOSING_MARK;

          case 'n':
            return NON_SPACING_MARK;

        }
      case 'N':
        switch (cat1) {
          case 'd':
            return DECIMAL_DIGIT_NUMBER;

          case 'l':
            return LETTER_NUMBER;

/* New in 1.4?
          case 'm':
            return MODIFIER_NUMBER;
*/
          case 'o':
            return OTHER_NUMBER;

        }
      case 'P':
        switch (cat1) {
          case 'c':
            return CONNECTOR_PUNCTUATION;

          case 'd':
            return DASH_PUNCTUATION;

          case 'e':
            return END_PUNCTUATION;

          case 'o':
            return OTHER_PUNCTUATION;

          case 's':
            return START_PUNCTUATION;

          case 'f':
            return FINAL_QUOTE_PUNCTUATION;

          case 'i':
            return INITIAL_QUOTE_PUNCTUATION;
        }
      case 'S':
        switch (cat1) {
          case 'c':
            return CURRENCY_SYMBOL;

          case 'k':
            return MODIFIER_SYMBOL;

          case 'm':
            return MATH_SYMBOL;

          case 'o':
            return OTHER_SYMBOL;

        }
      case 'Z':
        switch (cat1) {
          case 'l':
            return LINE_SEPARATOR;

          case 'p':
            return PARAGRAPH_SEPARATOR;

          case 's':
            return SPACE_SEPARATOR;
          
        }
    }

    return UNASSIGNED;
  }

  public native static char forDigit(int digit, int radix);

  private native static Class getWrappedClass();

  private native static String getCategory(char ch);

  static native int toUnicodeBlock(char c);

  public static class Subset {

    private String name;

    protected Subset(String str){
      //TODO ...
      name = str;
    }

    public final boolean equals(Object o){
      return this == o;
    }

    public final int hashCode(){
      return super.hashCode();
    }

    public final String toString(){
      return name;
    }
  }

  public static final class UnicodeBlock extends Subset {

    public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", 63);
    public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC", 14);
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A", 64);
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B", 68);
    public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN", 11);
    public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS", 38);
    public static final UnicodeBlock BASIC_LATIN = new UnicodeBlock("BASIC_LATIN", 1);
    public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI", 17);
    public static final UnicodeBlock BLOCK_ELEMENTS = new UnicodeBlock("BLOCK_ELEMENTS", 45);
    public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO", 52);
    // TODO: this block is new to us. (31a0-31bf)
    public static final UnicodeBlock BOPOMOFO_EXTENDED = new UnicodeBlock("BOPOMOFO_EXTENDED", -1);
    public static final UnicodeBlock BOX_DRAWING = new UnicodeBlock("BOX_DRAWING", 44);
    public static final UnicodeBlock BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", 80);
    public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE", 75);
    public static final UnicodeBlock CJK_COMPATIBILITY = new UnicodeBlock("CJK_COMPATIBILITY", 56);
    public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = new UnicodeBlock("CJK_COMPATIBILITY_FORMS", 66);
    public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS", 62);
    public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT", 79);
    public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION", 49);
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS", 60);
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", 81);
    public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS", 7);
    public static final UnicodeBlock COMBINING_HALF_MARKS = new UnicodeBlock("COMBINING_HALF_MARKS", 65);
    public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS", 35);
    public static final UnicodeBlock CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", 41);
    public static final UnicodeBlock CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", 34);
    public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC", 10);
    public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI", 16);
    public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS", 48);
    public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = new UnicodeBlock("ENCLOSED_ALPHANUMERICS", 43);
    public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", 55);
    public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC", 73);
    public static final UnicodeBlock GENERAL_PUNCTUATION = new UnicodeBlock("GENERAL_PUNCTUATION", 32);
    public static final UnicodeBlock GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", 46);
    public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN", 27);
    public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", 8);
    public static final UnicodeBlock GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", 31);
    public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI", 19);
    public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI", 18);
    public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", 69);
    public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO", 53);
    public static final UnicodeBlock HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", 29);
    public static final UnicodeBlock HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", 71);
    public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW", 12);
    public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA", 50);
    public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", 207);
    public static final UnicodeBlock IPA_EXTENSIONS = new UnicodeBlock("IPA_EXTENSIONS", 5);
    // TODO: this one is new to us (3190-319F)
    public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN", -1);
    public static final UnicodeBlock KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", 78);
    public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA", 23);
    public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA", 51);
    public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER", 88);
    public static final UnicodeBlock LAO = new UnicodeBlock("LAO", 26);
    public static final UnicodeBlock LATIN_1_SUPPLEMENT = new UnicodeBlock("LATIN_1_SUPPLEMENT", 2);
    public static final UnicodeBlock LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", 3);
    public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL", 30);
    public static final UnicodeBlock LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", 4);
    public static final UnicodeBlock LETTERLIKE_SYMBOLS = new UnicodeBlock("LETTERLIKE_SYMBOLS", 36);
    public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM", 24);
    public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS", 39);
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS", 47);
    public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = new UnicodeBlock("MISCELLANEOUS_TECHNICAL", 40);
    public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN", 89);
    public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR", 87);
    public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", 37);
    public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM", 82);
    public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", 42);
    public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA", 20);
    public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC", 83);
    public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA", 84);
    public static final UnicodeBlock PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", 61);
    public static final UnicodeBlock SMALL_FORM_VARIANTS = new UnicodeBlock("SMALL_FORM_VARIANTS", 67);
    public static final UnicodeBlock SPACING_MODIFIER_LETTERS = new UnicodeBlock("SPACING_MODIFIER_LETTERS", 6);
    public static final UnicodeBlock SPECIALS = new UnicodeBlock("SPECIALS", 70);
    public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS", 33);
    // TODO: this is not in unicode.awk, look it up somewhere
    public static final UnicodeBlock SURROGATES_AREA = new UnicodeBlock("SURROGATES_AREA", -1);
    public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC", 85);
    public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL", 21);
    public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU", 22);
    public static final UnicodeBlock THAI = new UnicodeBlock("THAI", 25);
    public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA", 86);
    public static final UnicodeBlock TIBETAN  = new UnicodeBlock("TIBETAN", 91);
    public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS  = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", 74);
    public static final UnicodeBlock YI_RADICALS  = new UnicodeBlock("YI_RADICALS", 77);
    public static final UnicodeBlock YI_SYLLABLES  = new UnicodeBlock("YI_SYLLABLES", 76);

    private static HashMap number2block;

    public static UnicodeBlock of(char c){
      UnicodeBlock ub = null;
      int number = toUnicodeBlock(c);
      if (number > 0) {
        ub = (UnicodeBlock)number2block.get(new Integer(number));
      }
      return ub;
    }

    private UnicodeBlock(String name, int number){
      super(name);
      if (number2block == null) {
         number2block = new HashMap();
      }
      if (number > 0) {
        number2block.put(new Integer(number), this);
      }
    }

  }

  /**
  * We cache a Character instance for values of char from 0 to 127 inclusive.
  */
  private static class CharacterCache {

    static Character[] cache;

    static {
      cache = new Character[128];
      for (int i = 0; i < 128; ++i) {
        cache[i] = new Character((char) i);
      }
    }

    static Character charFactory(char value) {
      return value < 128 ? cache[value] : new Character(value);
    }
  }
}
