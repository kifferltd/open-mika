/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/



// Tags: JLS1.0

package gnu.testlet.wonka.jni;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
public class JNIStringTest implements Testlet
{
  protected static TestHarness harness;

// load the native library//
  static
  {
    System.loadLibrary("JNIStringTest");
  }

/**
Strings form a special case in jni.h c-calls as the java.lang.jstring variables can not be accessed as normal variables,
nor as c-code character arrays terminated by a null-character.   <br>
the only way to work with strings is through transformer-functions that switch between a jstring object (that can't be manipulated)
and a buffer of characters (that can) <br>
jni.h provides functions to build a jstring out of an array of characters, get the length in characters of a jstring object,
and put a jstring into an array of characters and release this array into a string again <br>
All this functins come in two versiona: <br>
1) the simple String-functions work with an array of jchar unsigned 16-bit values
2) the string-UTF functions that mirror the jstring back and forth into an array of c-type char (8-bits signed). Unicode
characters bigger then 0xf7 are described as a series of 8-bit UTF characters: <br>
-> standard ascii 7-bit characters (number 0 to 0x7f) are just copied as positive signed 8-bit chars
-> unicode characters from 0x80 to 0x7ff are described by their bit value (0000.0xxx/xxyy.yyyy) as two chars 110x.xxxx and 10yy.yyyy <br>
-> unicode characters higher then 0x800 are described by their bit value (xxxx.yyyy/yyzz.zzzz) as three
chars 111x.xxxx , 10yy.yyyy and 10zz.zzzz <br>
Note therefore that an UTF-8 transformed char buffer of a string may contain more chars then the original string has (unicode) characters
if some of the characters are not standard ascii-7 types. Length and transformation functions may therefore return different values
regarding whether jchar unicode and UTF char routines are used  <br>
<br>
This test class will simultaneously test the various unicode and UTF routines and compare their results <br>
(In UTF-8 transcoding, all non-ascii characters are defined by the fact that their highest bit is 1,
ergo that their char-values are negative, this makes detection easy)
*/

/** testing the JNI.h c-code function NewString() and NewStringUTF */
  protected void testStringConstruction()
  {
    harness.checkPoint("String(void) /NewStringUTF");
    harness.check(nativeUTFHello(), "hello");

    harness.checkPoint("string(int) using NewStringUTF");
    harness.check(nativeUTFBuildString(6,(byte)'*'), "******");

    harness.checkPoint("String(void) /NewString");
		//harness.fail("Call to <NewString()> causes Wonka to crash");
    harness.check(nativeUnicodeHello(), "hello");

    harness.checkPoint("string(int) using NewString");
		//harness.fail("Call to <NewString()> causes Wonka to crash");
    harness.check(nativeUnicodeBuildString(3,(char)0xf4), unicodeString("/u00f4/u00f4/u00f4"));
   }

  /** returns a string built from an array 'hello' of jchars   */
  public native String nativeUnicodeHello();
  /** returns a string built from a c-code char string 'hello\0'  */
  public native String nativeUTFHello();
  /** builds a string out of 'length' given jchar unicode characters   */
  native String nativeUnicodeBuildString(int length, char ch);
  /** builds a string out of 'length' given 8-bit values (c-code char's)   */
  native String nativeUTFBuildString(int length, byte b);


/** tests the jni.h GetStringLength()/GetUTFStringLength() functins to return the length of a string (in jchars and unicode) */
  protected void testStringLength()
  {
    String testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    String testtriples = unicodeString("thai </u0e01/u0e0f>");

    harness.checkPoint("int(string): letter count using GetUTFStringLength()");
    harness.check(nativeUTFLength(testdoubles),11,"Length of " + testdoubles);
    harness.check(nativeUTFLength(testtriples),13,"Length of " + testtriples);
    harness.checkPoint("int(string): letter count using GetStringLength()");
    harness.check(nativeUnicodeLength(testdoubles),9,"Length of " + testdoubles);
    harness.check(nativeUnicodeLength(testtriples),9,"Length of " + testtriples);
  }


/** returns the length of a java string in unicode (java) jchars  */
  static native int nativeUnicodeLength(String source);
/** returns the length of a java string in UTF-8 transcoded (c-type) 8-bit chars    */
  static native int nativeUTFLength(String source);


/** tests the transformation of a java jstring into an array of (unicode or UTF-8) characters
using the GetStringChars/ReleaseStringChars (unicode) and GetStringUTFChars/ReleaseStringUTFChars (UTF-8) functions    */
  protected void testStringAccess()
  {
    String testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    String testtriples = unicodeString("thai </u0e01/u0e0f>");
    String decoded;

    harness.checkPoint("int(string): string scan using GetStringUTFChars/ReleaseStringUTFChars");
    harness.check(nativeUTFCountNonAscii(testdoubles),4,testdoubles);
    harness.check(nativeUTFCountNonAscii(testtriples),6,testtriples);
    harness.checkPoint("int(string): string scan using GetStringChars/ReleaseStringChars");
    harness.check(nativeUnicodeCountNonAscii(testdoubles),2,testdoubles);
    harness.check(nativeUnicodeCountNonAscii(testtriples),2,testtriples);
  }


/** scans the jchar char buffer obtained with GetStringChars/ReleaseStringChars for non-ascii-characters   */
  private native int nativeUnicodeCountNonAscii(String source);
/** scans the UTF-8 char buffer obtained with GetStringUTFChars/ReleaseStringUTFChars for non-ascii-characters
 there are 2 or 3 UTF-8 chars to represent one unicode non-ascii 16-bit character  */
  private native int nativeUTFCountNonAscii(String source);


  protected void testStringRebuild()
  {
    String testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    String testtriples = unicodeString("thai </u0e01/u0e0f>");
    String decoded;
    harness.checkPoint("string(string): copy using GetStringUTFChars/ReleaseStringUTFChars and NewStringUTF)");
    decoded = nativeUTFBuildOnlyAscii(testdoubles);
    harness.verbose(decoded);
    harness.check(decoded,"sm**rebr**d");
    decoded = nativeUTFBuildOnlyAscii(testtriples);
    harness.verbose(decoded);
    harness.check(decoded,"thai <******>");
    harness.checkPoint("string(string): copy using GetStringChars/ReleaseStringChars and NewString)");
		//harness.fail("Call to <NewString()> causes Wonka to crash");

    decoded = nativeUnicodeBuildOnlyAscii(testdoubles);
    harness.verbose(decoded);
    harness.check(decoded,"sm*rebr*d");

		//harness.fail("Call to <NewString()> causes Wonka to crash");

    decoded = nativeUnicodeBuildOnlyAscii(testtriples);
    harness.verbose(decoded);
    harness.check(decoded,"thai <**>");
}

/** uses the jchar char buffer obtained with GetStringChars/ReleaseStringChars to construct a jchar string
 where all non-unicode chars are replaced by asterixes  */
  private native String nativeUnicodeBuildOnlyAscii(String source);
/** uses the jchar char buffer obtained with GetStringUTFChars/ReleaseStringUTFChars to construct an UTF-8 char string
 where all non-unicode chars are replaced by asterixes. Note that there are 2 or 3 UTF-8 chars
 to represent one unicode non-ascii 16-bit character   */
  private native String nativeUTFBuildOnlyAscii(String source);


/** tests direct manipulation of a java jstring turned into an array of (unicode or UTF-8) characters
using the GetStringChars/ReleaseStringChars (unicode) and GetStringUTFChars/ReleaseStringUTFChars (UTF-8) functions
when releasing this array, the string itself is changed    

==> NOTE: Direct manipulation of Strings is not allowed !!! Disabled the tests

*/
  protected void testStringManipulation()
  {
    /*
    String testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    String testtriples = unicodeString("thai </u0e01/u0e0f>");

    harness.checkPoint("string(string): direct manipulation using GetStringUTFChars/ReleaseStringUTFChars");
    testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    testtriples = unicodeString("thai </u0e01/u0e0f>");
    nativeUTFReplaceNonAscii(testdoubles);
    harness.check(testdoubles,"sm**rebr**d","replacing non-ascii-characters by <*>, utf doubles");
    nativeUTFReplaceNonAscii(testtriples);
    harness.check(testtriples,"thai <******>","replacing non-ascii-characters by <*>, utf triples");

    harness.checkPoint("string(string): direct manipulation using GetStringChars/ReleaseStringChars");
    testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    testtriples = unicodeString("thai </u0e01/u0e0f>");
    nativeUnicodeReplaceNonAscii(testdoubles);
    harness.check(testdoubles,"sm*rebr*d","replacing non-ascii-characters by <*>, utf doubles");
    nativeUnicodeReplaceNonAscii(testtriples);
    harness.check(testtriples,"thai <**>","replacing non-ascii-characters by <*>, utf triples");
    */
  }


/** manipulates the jchar char buffer obtained with GetStringChars/ReleaseStringChars by replacing all non-unicode chars by asterixes
releasing the buffer also changes the original string  */
  private native String nativeUnicodeReplaceNonAscii(String source);
/** manipulates the jchar char buffer obtained with GetStringUTFChars/ReleaseStringUTFChars by replacing all non-unicode chars
 by asterixes. (Note that there are 2 or 3 UTF-8 chars to represent one unicode non-ascii 16-bit character)
 releasing the buffer will transform the char array back to unicode, therefore building a string that has multiple asterixes
 for each original non-ascii unicode character. <br>
 releasing the buffer also directly changes the original string <br>
 THIS FUNCTION DOES NOT WORK IN SUN JDK1.2: RELEASING THE BUFFER REVERTS TO THE ORIGINAL JSTRING.
  CHECK THE ReleaseStringUTFChars FUNCTION  */

  private native String nativeUTFReplaceNonAscii(String source);


/** testing the special native GetStringCritical/ReleaseStringCritical functions. This are special cases of the
 GetStringChars/ReleaseStringchars functions where the buffer is not locked during manipulation: calling this
 functions brings the buffer in a 'critical' state where it is to the programmer to make sure no other functions
 are calling the same string at the same time.
*/
  public void testStringCritical()
  {
    harness.checkPoint("int(string): string scan using GetStringCritical/ReleaseStringCritical");
		// harness.fail("Critical access <GetStringCritical() / ReleaseStringCritical()> causes Wonka to crash");

    String testdoubles = unicodeString("sm/u00f6rebr/u00f6d");
    String testtriples = unicodeString("thai </u0e01/u0e0f>");
    String result;

    harness.checkPoint("int(string): string scan using GetStringCritical/ReleaseStringCritical");
    harness.check(nativeCriticalCountNonAscii(testdoubles),2,testdoubles);

    harness.check(nativeCriticalCountNonAscii(testtriples),2,testtriples);
    harness.checkPoint("string(string): copy using GetStringCritical/ReleaseStringCritical and NewString)");
    harness.check(nativeCriticalBuildOnlyAscii(testdoubles),"sm*rebr*d",testdoubles);
    harness.check(nativeCriticalBuildOnlyAscii(testtriples),"thai <**>",testtriples);
    /* Direct manipulation of strings is not allowed !! 
    harness.checkPoint("string(string): direct manipulation using GetStringCritical/ReleaseStringCritical");
    result =nativeCriticalReplaceNonAscii(testdoubles);
    harness.verbose(result);
    harness.check(result,"sm*rebr*d");
    harness.check(testdoubles,"sm*rebr*d");
    result =nativeCriticalReplaceNonAscii(testtriples);
    harness.verbose(result);
    harness.check(result,"thai <**>");
    harness.check(testtriples,"thai <**>");
    */
 }

//@{
/** as nativeUnicodeCountNonAscii: scans the buffer for non-ascii characters   */
  private native int nativeCriticalCountNonAscii(String source);
/** as nativeUnicodeBuildOnlyAscii: mirrors the buffer to a new jchar buffer where all non-ascii characters are replaced by asterixes  */
  private native String nativeCriticalBuildOnlyAscii(String source);
/** as nativeUnicodeReplaceNonAscii: scans the buffer for non-ascii characters and replaces them.
Upon release, the original string is changed as well */
  private native String nativeCriticalReplaceNonAscii(String source);
//@}


/** testing the special native GetStringUTFRegion() function <br>
This function is special as it allows to copy a substring or a region of the UTF-8 encoded string into a given buffer.
After calling this function, no special release-calls will be necessary. <br>
Of paritcular interest is the possibility to assing one char ch and then copy a one-char substring of the string in the
&ch buffer, This is an easy way to quickly scan the string UTF-8 char by UTF-8 char <br>
THIS JNI FUNCTION IS NOT YET IMPLEMENTED IN WONKA
*/
  public void testStringUTFRegion()
  {
    harness.checkPoint("testing special native GetStringUTFRegion() function");
    //harness.fail("GetStringUTFRegion (defined for java 1.2) not implemented in Wonka");
  }


/********************************************************************************************************/
/**
 Special help function: transform a substring "/u1234" of a given string into a /u1234 unicode char. Like this we can
 initialise strings with unicode characters from a western standard keyboard
*/
static String unicodeString(String source)
{
  String dest = "";
  int letter = 0;
  String unicodestring;
  char c;
  while(letter <source.length() )
  {
    c=source.charAt(letter);
    if(c != '/')
      letter++;
    else
    {
      unicodestring = source.substring(letter+2,letter+6);
      c= (char)(Integer.parseInt(unicodestring,16));
      letter +=6;
    }

    dest += c;
  }

  return dest;
}

/**
* calls the tests for the different functions
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI cass access");
		testStringConstruction();
		testStringLength();
		testStringAccess();
		testStringRebuild();
		testStringManipulation();
		testStringCritical();
		testStringUTFRegion();
	}

/** NOTE: At present (17-11-2000) we found out that Wonka does not support the JNI-C unicode function NewString(), only the
utf function NewStringUTF().
Furthermore GetStringCritical/ReleaseStringCritical will cause wonka to exit
Therefore running this class thows an exception and breaks the Wonka on the testing functions
testStringConstruction and testStringRebuild. For Wonka testing, it's best to rebuild this file various times with
differnt combinations of tests flagged off to find out the real errors
and
*/	
}
