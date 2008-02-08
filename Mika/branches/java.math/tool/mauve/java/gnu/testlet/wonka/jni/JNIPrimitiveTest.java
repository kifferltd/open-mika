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
public class JNIPrimitiveTest implements Testlet
{
  protected static TestHarness harness;

// load the native library//
  static
  {
    System.loadLibrary("JNIPrimitiveTest");
  }

// testing primitive members as call variables
/**
 Simple JNI calls to C-functions f(java-primitive) returns java-primitive
*/
  public void testPrimitiveCalls()
  {
    harness.checkPoint("function Boolean = inverse Boolean");
    harness.check(nativeInverse(true) == false);
    harness.check(nativeInverse(false) == true);

    harness.checkPoint("function byte = 1/2 byte");
    harness.check(Byte.toString(nativeHalve(Byte.MAX_VALUE)), Byte.toString((byte)(Byte.MAX_VALUE / 2)) );
    harness.check(Byte.toString(nativeHalve(Byte.MIN_VALUE)), Byte.toString((byte)(Byte.MIN_VALUE / 2)) );
    harness.checkPoint("function short = 1/2 short");
    harness.check(Short.toString(nativeHalve(Short.MAX_VALUE)), Short.toString((short)(Short.MAX_VALUE / 2)) );
    harness.check(Short.toString(nativeHalve(Short.MIN_VALUE)), Short.toString((short)(Short.MIN_VALUE / 2)) );
    harness.checkPoint("function int = 1/2 int");
    harness.check(Integer.toString(nativeHalve(Integer.MAX_VALUE)), Integer.toString(Integer.MAX_VALUE / 2) );
    harness.check(Integer.toString(nativeHalve(Integer.MIN_VALUE)), Integer.toString(Integer.MIN_VALUE / 2) );
    harness.checkPoint("function long = 1/2 long");
    harness.check(Long.toString(nativeHalve(Long.MAX_VALUE)), Long.toString(Long.MAX_VALUE / 2) );
    harness.check(Long.toString(nativeHalve(Long.MIN_VALUE)), Long.toString(Long.MIN_VALUE / 2) );

    harness.checkPoint("function decimal = 1/decimal");
    harness.check(Float.toString(nativePart(3.0f)),Float.toString(1.0f/3.0f) );
    harness.check(Double.toString(nativePart(3.0d)),Double.toString(1.0D/3.0d) );
  }
/** native functions */
//@{
  /// f(jboolean) = !jboolean
  protected native boolean nativeInverse(boolean value);
  /// f(jbyte) = jbyte/2
  protected native byte  nativeHalve(byte  value);
  /// f(jshort) = jshort/2
  protected native short nativeHalve(short value);
  /// f(jint) = jint/2
  protected native int   nativeHalve(int   value);
  /// f(jlong) = jlong/2
  protected native long  nativeHalve(long  value);
  /// f(jfloat) = 1.0/jfloat
  protected native float  nativePart(float  value);
  /// f(jdouble) = 1.0/jdouble
  protected native double nativePart(double value);
//@}
/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI calls (primitive)primitive");
		testPrimitiveCalls();
	}

}
