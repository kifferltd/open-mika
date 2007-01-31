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
/// tests behavior of native strings declared public, protected, private and static
public class JNIInheritanceTest extends JNIPrimitiveTest
{
/**
//  static
//  {
//    System.loadLibrary("JNIPrimitiveTest");
//    System.loadLibrary("JNIStringTest");
//  }
All class loading should already been dine by the classes that define the native functions

*/

/// calls the inherited protected classes from JNIPrimitiveTest, the static classes from JNIStringTest and their public ones just as well
  public void test (TestHarness newharness)
	{
/// protected classes, inherited from JNIPrimitiveTest,
		newharness.setclass("JNI calls (dependency tests)");
    newharness.checkPoint("protected functions from JNIPrimitiveTest");
    newharness.check(nativeInverse(true) == false);
    newharness.check(nativePart(3.0f) == (float)(1.0f/3.0f) );
		
/// static classes from JNIStringTest
    newharness.checkPoint("static functions from JNIStringTest");
    String ucstring = JNIStringTest.unicodeString("sm/u00f6rebr/u00f6d");
    newharness.check(JNIStringTest.nativeUnicodeLength(ucstring),9);
    newharness.check(JNIStringTest.nativeUTFLength(ucstring),11);

/// public classes from JNIStringTest
    newharness.checkPoint("public functions from JNIStringTest");
    JNIStringTest stringtest = new JNIStringTest();
/// public classes from JNIStringTest : explicitely defined public
    newharness.check(stringtest.nativeUTFHello(), "hello");
//JNIStringTest:  public native String nativeUTFHello();
/// public classes from JNIStringTest : no definition flag: implicitly thought of as public
    newharness.check(stringtest.nativeUTFBuildString(6,(byte)'*'), "******");
//JNIStringTest:  native String nativeUTFBuildString(int length, byte b);
	}

}
