/* Copyright (C) 1999 Hewlett-Packard Company

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
   Boston, MA 02111-1307, USA.
*/

// Tags: JLS1.0

package gnu.testlet.wonka.lang.Number;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

/**
* this class contains test for all the operators on the primitive types
*/
public class PrimitiveTest implements Testlet
{
  private final static int BIGGER = 1;
  private final static int EQUALS = 0;
  private final static int SMALLER=-1;
  private final static int UNDEFINED = -2;


  protected static TestHarness harness;

  public void testBasicOps(){

  harness.checkPoint("+|- byte");
  byte b = (byte)55;
  harness.check( ((byte) 34 + b) == (byte)89);
//  harness.check( ((byte) 127 + b) == (byte)-74,"got: "+((byte) 127 + b));
  harness.check( ((byte) 34 - b) == (byte)-21);

  harness.checkPoint("+|- short");
  short s = (short)4455;
  harness.check( ((short) 34 + s) == (short)4489);
//  harness.check( ((short) 32767 + s) == (short)-28314,"got: "+((short) 32767 + s));
  harness.check( ((short) 4434 - s) == (short)-21,"got: "+((short) 4434 - s));

  harness.checkPoint("+|- int");
  int i = 4455;
  harness.check( ( 34 + i) == 4489);
  harness.check( (2147483647  + i) == -2147479194);
  harness.check( ( 4434 - i) == -21);

  harness.checkPoint("+|- long");
  long l = 4455L;
  harness.check( ( 34L + l) == 4489L);
  harness.check( (Long.MAX_VALUE + l) ==(Long.MIN_VALUE+4454));
  harness.check( ( 4434L - l) == -21L);

  harness.checkPoint("+|- float");
  float f = Float.NaN;
  harness.check( Float.isNaN(f + 545.787f));
  harness.check( Float.isNaN(545.787f - f));
  f = Float.NEGATIVE_INFINITY;
  harness.check( Float.isNaN( Float.POSITIVE_INFINITY + f ));
  harness.check( Float.isNaN( Float.NEGATIVE_INFINITY - f ));
  harness.check(! Float.isNaN( Float.POSITIVE_INFINITY - f ));
  harness.check(! Float.isNaN( f  - Float.POSITIVE_INFINITY));
  harness.check(( Float.POSITIVE_INFINITY - f ) == Float.POSITIVE_INFINITY );
  harness.check(( f  - Float.POSITIVE_INFINITY) == f );
  harness.check(( f  - 4.083E25f ) == f );
  harness.check(( f  + 5.877E25f ) == f,"got: "+( f  + 5.877E25f ) );
  f = Float.POSITIVE_INFINITY;
  harness.check(( f  - 4.083E25f ) == f,"got: "+( f  - 4.083E25f ) );
  harness.check(( f  + 5.877E25f ) == f );
  f = 0.0f;
  harness.check(( f  - 0.0f ) == f );
  harness.check(( f  + 0.0f ) == f );
  harness.check(( -0.0f - f ) == -0.0f );
  harness.check(( f  - 4.083E25f ) == -4.083E25f );
  harness.check(( f  + 5.877E25f ) ==  5.877E25f );
  f = -0.0f;
  harness.check(( f  - 4.083E25f ) == -4.083E25f );
  harness.check(( f  + 5.877E25f ) ==  5.877E25f );
  f = 23454.0f;
  harness.check( ( f - f ) == 0.0f );
  harness.check( ( f - 454.0f ) == 23000.0f);
  harness.check( ( f + 444.0f ) == 23898.0f);
  f = Float.MAX_VALUE;
  harness.check( ( f + 444.0E34f ) == Float.POSITIVE_INFINITY,"got: "+(f + 444.0f ));
  harness.check( ( -444.0E34f - f ) == Float.NEGATIVE_INFINITY,"got: "+(-444.0f - f ));

  harness.checkPoint("+|- double");
  double d = Double.NaN;
  harness.check( Double.isNaN(d + 545.787));
  harness.check( Double.isNaN(545.787 - d));
  d = Double.NEGATIVE_INFINITY;
  harness.check( Double.isNaN( Double.POSITIVE_INFINITY + d ));
  harness.check( Double.isNaN( Double.NEGATIVE_INFINITY - d ));
  harness.check(! Double.isNaN( Double.POSITIVE_INFINITY - d ));
  harness.check(! Double.isNaN( d  - Double.POSITIVE_INFINITY));
  harness.check(( Double.POSITIVE_INFINITY - d ) == Double.POSITIVE_INFINITY );
  harness.check(( d  - Double.POSITIVE_INFINITY) == d );
  harness.check(( d  - 4.083E56 ) == d );
  harness.check(( d  + 5.877E56 ) == d );
  d = Double.POSITIVE_INFINITY;
  harness.check(( d - 4.083E56 ) == d );
  harness.check(( d + 5.877E56 ) == d );
  d = 0.0;
  harness.check(( d - 0.0 ) == d );
  harness.check(( d + 0.0 ) == d );
  harness.check(( -0.0 - d )== -0.0);
  harness.check(( d  - 4.083E56 ) == -4.083E56 );
  harness.check(( d  + 5.877E56 ) ==  5.877E56 );
  d = -0.0;
  harness.check(( d  - 4.083E56 ) == -4.083E56 );
  harness.check(( d  + 5.877E56 ) ==  5.877E56 );
  d = 23454.0;
  harness.check( ( d - d ) == 0.0 );
  harness.check( ( d - 454.0 ) == 23000.0);
  harness.check( ( d + 444.0 ) == 23898.0);
  d = Double.MAX_VALUE;
  harness.check( ( d + 444.0E300 ) == Double.POSITIVE_INFINITY,"got: "+( d + 444.0 ));
  harness.check( ( -444.0E300 - d ) == Double.NEGATIVE_INFINITY,"got: "+( -444.0 - d ));

  harness.checkPoint("*|/|% int");
  i = 124;
  harness.check( (i * 10 ) == 1240 );
  harness.check( (i / 10 ) == 12 );
  harness.check( (i % 10 ) == 4 );
  i = 0;
  harness.check( (5%3) , 2 ,"5 % 3 = 2");
  harness.check( (5%(-3)) , 2 ,"5 % (-3) = 2");
  harness.check( ((-5)%3) , -2 ,"(-5) % 3 = -2");
  harness.check( ((-5)%(-3)) , -2 ,"(-5) % (-3) = -2");
  try { i = 123 / i ; harness.fail("should throw ArithmeticException"); }
  catch (ArithmeticException ae) { harness.check(true); }
  i = Integer.MIN_VALUE;
  harness.check( ( i / (-1))== i );
  harness.check( ( i % (-1))== 0 );
  harness.check( ( i * (-1))== i, "got: "+( i * (-1)) );
  harness.check( ( i * 2) == ((int)((long)i *2L)),"got: "+( i * 2));
  int min = Integer.MIN_VALUE;
  harness.check(min / 10000 , -2147483648 / 10000, "Integer.MIN_VALUE divide");
  harness.check(min * 10000 , -2147483648 * 10000, "Integer.MIN_VALUE multiply");
  harness.check(min % 10000 , -2147483648 % 10000, "Integer.MIN_VALUE remainder");

  harness.checkPoint("*|/|% long");
  l = 124L;
  harness.check( (l * 10L ) == 1240L );
  harness.check( (l / 10L ) == 12L );
  harness.check( (l % 10L ) == 4L );
  l = 0L;
  try { l = 134L / l ; harness.fail("should throw ArithmeticException"); }
  catch (ArithmeticException ae) { harness.check(true); }
  l = Long.MIN_VALUE;
  harness.check( ( l / (-1L))== l );
  harness.check( ( l % (-1L))== 0L );
  harness.check( ( l * (-1L))== l, "got: "+( l * (-1)) );
  harness.check( ( l * 2L) == 0L);

  long mn = Long.MIN_VALUE;
  harness.check(mn / 10000 , Long.MIN_VALUE / 10000, "Long.MIN_VALUE divide");
  harness.check(mn * 10000 , Long.MIN_VALUE * 10000, "Long.MIN_VALUE multiply");
  harness.check(mn % 10000 , Long.MIN_VALUE % 10000, "Long.MIN_VALUE remainder");

  harness.checkPoint("* Float");
  f = 124.0f;
  harness.check( (f * 10.0f ) == 1240.0f );
  harness.check( (f * -10.0f ) == - 1240.0f );
  harness.check( (-f * 10.0f ) == -1240.0f );
  harness.check( (-f * -10.0f ) == 1240.0f );
  harness.check( Float.isNaN(f * Float.NaN));
  f = Float.POSITIVE_INFINITY;
  harness.check( Float.isNaN(f * -0.0f));
  harness.check( Float.isNaN(f *  0.0f));
  harness.check( (f * 10.0f ) == f );
  harness.check( (f * -10.0f ) == Float.NEGATIVE_INFINITY);
  f = Float.NEGATIVE_INFINITY;
  harness.check( Float.isNaN(f * -0.0f));
  harness.check( Float.isNaN(f *  0.0f));
  harness.check( (f * 10.0f ) == f );
  harness.check( (f * -10.0f ) == Float.POSITIVE_INFINITY);

  harness.checkPoint("* double");
  d = 124.0;
  harness.check( (d * 10.0 ) == 1240.0 );
  harness.check( (d * -10.0 ) == - 1240.0 );
  harness.check( (-d * 10.0 ) == -1240.0 );
  harness.check( (-d * -10.0 ) == 1240.0 );
  harness.check( Double.isNaN(d * Double.NaN));
  d = Double.POSITIVE_INFINITY;
  harness.check( Double.isNaN(d * -0.0));
  harness.check( Double.isNaN(d *  0.0));
  harness.check( (d * 10.0 ) == d );
  harness.check( (d * -10.0 ) == Double.NEGATIVE_INFINITY);
  d = Double.NEGATIVE_INFINITY;
  harness.check( Double.isNaN(d * -0.0));
  harness.check( Double.isNaN(d *  0.0));
  harness.check( (d * 10.0 ) == d );
  harness.check( (d * -10.0 ) == Double.POSITIVE_INFINITY);

  harness.checkPoint("/ Float");
  f = 1240.0f;
  harness.check( (f / 10.0f ) == 124.0f );
  harness.check( (f / -10.0f ) == - 124.0f );
  harness.check( (-f / 10.0f ) == -124.0f );
  harness.check( (-f / -10.0f ) == 124.0f );
  harness.check( Float.isNaN(f / Float.NaN));
  harness.check( Float.isNaN(Float.NaN / f));
  f = Float.POSITIVE_INFINITY;
  harness.check( Float.isNaN(f / f));
  harness.check( Float.isNaN(f / Float.NEGATIVE_INFINITY));
  f = Float.NEGATIVE_INFINITY;
  harness.check( Float.isNaN(f / f));
  harness.check( Float.isNaN(f / Float.POSITIVE_INFINITY));
  f = Float.POSITIVE_INFINITY;
  harness.check( (10.0f / f ) == 0.0f );
  harness.check( (-10.0f / f) == -0.0f);
  f = 0.0f;
  harness.check( Float.isNaN(f / -0.0f));
  harness.check( Float.isNaN(f /  0.0f));
  harness.check( (f / 10.0f ) == f );
  harness.check( (f / -10.0f ) == -0.0f);
  harness.check( ( 10.0f / f ) == Float.POSITIVE_INFINITY);
  harness.check( (-10.0f / f ) == Float.NEGATIVE_INFINITY);
// making sure the overflow problem in the division is noted
  f = Float.MAX_VALUE;
  harness.check( (f % Float.MIN_VALUE) == 0,"overflow in division -- result should be correct GOT: "+(f % Float.MIN_VALUE));
  harness.check( (f % 0.5f) == 0,"overflow in division -- result should be correct GOT: "+(f % 0.5f));

  harness.checkPoint("/ double");
  d = 12400.0;
  harness.check( (d / 10.0 ) == 1240.0 );
  harness.check( (d / -10.0 ) == - 1240.0 );
  harness.check( (-d / 10.0 ) == -1240.0 );
  harness.check( (-d / -10.0 ) == 1240.0 );
  harness.check( Double.isNaN(d / Double.NaN));
  harness.check( Double.isNaN(Double.NaN / d));
  d = Double.POSITIVE_INFINITY;
  harness.check( Double.isNaN(d / d));
  harness.check( Double.isNaN(d / Double.NEGATIVE_INFINITY));
  d = Double.NEGATIVE_INFINITY;
  harness.check( Double.isNaN(d / d));
  harness.check( Double.isNaN(d / Double.POSITIVE_INFINITY));
  d = Double.POSITIVE_INFINITY;
  harness.check( ( 10.0 / d) ==  0.0 );
  harness.check( (-10.0 / d) == -0.0);
  d = 0.0;
  harness.check( Double.isNaN(d / -0.0));
  harness.check( Double.isNaN(d /  0.0));
  harness.check( (d / 10.0 ) ==  d );
  harness.check( (d /-10.0 ) ==-0.0);
  harness.check( ( 10.0 / d) == Double.POSITIVE_INFINITY);
  harness.check( (-10.0 / d) == Double.NEGATIVE_INFINITY);

  harness.checkPoint("% Float");
  f = 124.0f;
  harness.check( (f % 10.0f ) == 4.0f );
  harness.check( (f % -10.0f ) == 4.0f );
  harness.check( (-f % 10.0f ) == -4.0f );
  harness.check( (-f % -10.0f ) == -4.0f );
  harness.check( Float.isNaN(f % Float.NaN));
  harness.check( Float.isNaN(Float.NaN % f));
  f = Float.POSITIVE_INFINITY;
  harness.check( Float.isNaN(f % f));
  harness.check( Float.isNaN(f % Float.NEGATIVE_INFINITY));
  harness.check( Float.isNaN(f % 12.20f),"got: "+(f % 12.20f));
  harness.check( Float.isNaN(f % -2.34f),"got: "+(f % -2.34f));
  harness.check( Float.isNaN(f % 0.0f),"got: "+(f % 0.0f));
  harness.check( Float.isNaN(f % -0.0f),"got: "+(f % -0.0f));
  f = Float.NEGATIVE_INFINITY;
  harness.check( Float.isNaN(f % f),"got: "+(f % f));
  harness.check( Float.isNaN(f % Float.POSITIVE_INFINITY),"got: "+(f % Float.POSITIVE_INFINITY));
  harness.check( Float.isNaN(f % 23.45f),"got: "+(f % 23.45f));
  harness.check( Float.isNaN(f % -3.45f),"got: "+(f % -3.45f));
  harness.check( Float.isNaN(f % 0.0f),"got: "+(f % 0.0f));
  harness.check( Float.isNaN(f % -0.0f),"got: "+(f % -0.0f));
  f = 0.0f;
  harness.check( Float.isNaN(32.434f % f),"got: "+(32.434f % f));
  harness.check( Float.isNaN(-3.594f % f),"got: "+(-3.594f % f));
  f = -0.0f;
  harness.check( Float.isNaN(32.434f % f),"got: "+(32.434f % f));
  harness.check( Float.isNaN(-3.594f % f),"got: "+(-3.594f % f));
  f = Float.POSITIVE_INFINITY;
  harness.check( ( 10.0f % f ) == 10.0f,"got: "+( 10.0f % f ));
  harness.check( (-10.0f % f ) ==-10.0f,"got: "+(-10.0f % f ));
  harness.check( ( 0.0f % f ) == 0.0f,"got: "+( 0.0f % f ));
  harness.check( (-0.0f % f ) ==-0.0f,"got: "+(-0.0f % f ));
  f = Float.NEGATIVE_INFINITY;
  harness.check( ( 10.0f % f ) == 10.0f,"got: "+( 10.0f % f ));
  harness.check( (-10.0f % f ) ==-10.0f,"got: "+(-10.0f % f ));
  harness.check( ( 0.0f % f ) == 0.0f,"got: "+( 0.0f % f ));
  harness.check( (-0.0f % f ) ==-0.0f,"got: "+(-0.0f % f ));
  f = 124.0f;
  harness.check( ( 0.0f % f ) == 0.0f,"got: "+( 0.0f % f ));
  harness.check( (-0.0f % f ) ==-0.0f,"got: "+(-0.0f % f ));

  harness.checkPoint("++ Postfix Operator");
  i = 5;
  int j = i++;
  harness.check((j == 5) && (i == 6));
  l = 5L;
  long m = l++;
  harness.check((m == 5L) && (l == 6L));
  f = 5.5f;
  float g = f++;
  harness.check((g == 5.5f) && (f == 6.5f));
  d = 5.5;
  double e = d++;
  harness.check((e == 5.5) && (d == 6.5));

  harness.checkPoint("-- Postfix Operator");
  i = 5;
  j = i--;
  harness.check((j == 5) && (i == 4));
  l = 5L;
  m = l--;
  harness.check((m == 5L) && (l == 4L));
  f = 5.5f;
  g = f--;
  harness.check((g == 5.5f) && (f == 4.5f));
  d = 5.5;
  e = d--;
  harness.check((e == 5.5) && (d == 4.5));

  harness.checkPoint("++ Prefix Operator");
  i = 5;
  j = ++i;
  harness.check((j == i) && (i == 6));
  l = 5L;
  m = ++l;
  harness.check((m == l) && (l == 6L));
  f = 5.5f;
  g = ++f;
  harness.check((g == f) && (f == 6.5f));
  d = 5.5;
  e = ++d;
  harness.check((e == d) && (d == 6.5));

  harness.checkPoint("-- Prefix Operator");
  i = 5;
  j = --i;
  harness.check((j == i) && (i == 4));
  l = 5L;
  m = --l;
  harness.check((m == l) && (l == 4L));
  f = 5.5f;
  g = --f;
  harness.check((g == f) && (f == 4.5f));
  d = 5.5;
  e = --d;
  harness.check((e == d) && (d == 4.5));
  }

  public void testBooleanOps(){
  boolean b=true;
  harness.checkPoint("boolean !");
  harness.check((!b) == false);
  b =false;
  harness.check(!b);

  harness.checkPoint("boolean &&");
  b=true;
  harness.check(b && true);
  harness.check(!(b && false));
  b =false;
  harness.check(!(b && true ));
  harness.check(!(b && false));

  harness.checkPoint("boolean ||");
  b=true;
  harness.check(b || true);
  harness.check(b || false);
  b =false;
  harness.check(b || true);
  harness.check(!(b || false));

  harness.checkPoint("boolean ^^");
  b=true;
  harness.check(!(b ^ true));
  harness.check(b ^ false);
  b =false;
  harness.check(b ^ true);
  harness.check(!(b ^ false));

  }
  public void testShiftOps()
  {
    int ip = Integer.MAX_VALUE;
    long lp = Long.MAX_VALUE;
    int im = Integer.MIN_VALUE;
    long lm = Long.MIN_VALUE;
    int jp=65535,jm = -2147418113;
    harness.checkPoint("shiftoperator >>");
    harness.check( (ip>>16) == 32767);
    harness.check( (im>>16) == -32768,"got: "+(im>>16));
    harness.check( (lp>>48) == 32767);
    harness.check( (lm>>48) == -32768L,"got: "+(lm>>48));

    harness.checkPoint("shiftoperator <<");
    harness.check( (jp<<16) == -65536);
    harness.check( (jm<<16) == -65536);
    harness.check( (ip<<1) == -2,"got: "+(im<<1));
    harness.check( (im<<1) == 0,"got: "+(im<<1));


    harness.checkPoint("shiftoperator >>>");
    harness.check( (ip>>>16) == 32767);
    harness.check( (im>>>16) == 32768,"got: "+(im>>>16));
    harness.check( (lp>>>48) == 32767);
    harness.check( (lm>>>48) == 32768L,"got: "+(lm>>>48));


  }

  public void testBitwiseOps()
  {
    int i = Integer.MAX_VALUE;
    long l = (long)Integer.MAX_VALUE;

    harness.checkPoint("bitwise &");
    harness.check( (i & -2147418113)== 65535);
    harness.check( (l & 2147418112L)== 2147418112L);
    harness.check( (i & 0) == 0 );
    harness.check( (l & 0) == 0 );

    harness.checkPoint("bitwise |");
    harness.check( (i | -2147418112)== -1);
    harness.check( (l | 2147418112L)== l);
    harness.check( (i | 0) == i );
    harness.check( (l | 0) == l );

    harness.checkPoint("bitwise ^");
    harness.check( (i ^ -2147418113)== -65536);
    harness.check( (l ^ 2147418113L)== 65534L);
    harness.check( (i ^ 0) == i );
    harness.check( (l ^ 0) == l );

  }

  public void testMaximumValues()
  {
    harness.checkPoint("maximum byte values");
    compareValue (Byte.MAX_VALUE, EQUALS, (byte)127);
    compareValue ((byte)(Byte.MAX_VALUE + (byte)1), EQUALS, (byte)-128);
    compareValue ((byte)(Byte.MAX_VALUE + (byte)2), EQUALS, (byte)-127);
    compareValue (Byte.MIN_VALUE, EQUALS, (byte)-128);
    compareValue ((byte)(Byte.MIN_VALUE - (byte)1), EQUALS, (byte)127);
    compareValue ((byte)(Byte.MIN_VALUE - (byte)2), EQUALS, (byte)126);

    harness.checkPoint("maximum short values");
    compareValue (Short.MAX_VALUE, EQUALS, (short)32767);
    compareValue ((short)(Short.MAX_VALUE + (short)1), EQUALS, (short)-32768);
    compareValue ((short)(Short.MAX_VALUE + (short)2), EQUALS, (short)-32767);
    compareValue (Short.MIN_VALUE, EQUALS, (short)-32768);
    compareValue ((short)(Short.MIN_VALUE - (short)1), EQUALS, (short)32767);
    compareValue ((short)(Short.MIN_VALUE - (short)2), EQUALS, (short)32766);

    harness.checkPoint("maximum int values");
    compareValue (Integer.MAX_VALUE, EQUALS, 2147483647);
    compareValue (Integer.MAX_VALUE + 1, EQUALS, -2147483648);
    compareValue (Integer.MAX_VALUE + 2, EQUALS, -2147483647);
    compareValue (Integer.MIN_VALUE, EQUALS, -2147483648);
    compareValue (Integer.MIN_VALUE - 1, EQUALS, 2147483647);
    compareValue (Integer.MIN_VALUE - 2, EQUALS, 2147483646);
    harness.check(Integer.toString(Integer.MAX_VALUE).equals("2147483647"), "got: "+Integer.toString(Integer.MAX_VALUE));
    harness.check(Integer.toString(Integer.MIN_VALUE).equals("-2147483648"), "got: "+Integer.toString(Integer.MIN_VALUE));

    harness.checkPoint("maximum long values");
    compareValue(Long.MAX_VALUE, EQUALS, 9223372036854775807L);
    compareValue (Long.MAX_VALUE, EQUALS, 9223372036854775807L);
    compareValue (Long.MAX_VALUE + 1L, EQUALS, -9223372036854775808L);
    compareValue (Long.MAX_VALUE + 2L, EQUALS, -9223372036854775807L);
    compareValue (Long.MIN_VALUE, EQUALS, -9223372036854775808L);
    compareValue (Long.MIN_VALUE - 1L, EQUALS, 9223372036854775807L);
    compareValue (Long.MIN_VALUE - 2l, EQUALS, 9223372036854775806L);
    harness.check(Long.toString(Long.MAX_VALUE),"9223372036854775807", "got: "+Long.toString(Long.MAX_VALUE));
    harness.check(Long.toString(Long.MIN_VALUE),"-9223372036854775808", "got: "+Long.toString(Long.MIN_VALUE));
    harness.check(Long.toString(Long.MIN_VALUE,2),"-1000000000000000000000000000000000000000000000000000000000000000", "got: "+Long.toString(Long.MIN_VALUE));
    harness.check(Long.toString(0),"0");
  }

  public void testBiggerSmaller()
  {
    harness.checkPoint("Bigger/smaller Long");

    compareValue(Long.MAX_VALUE, BIGGER, 9223372036854775806L);
    compareValue(Long.MAX_VALUE, BIGGER, Long.MIN_VALUE);
    compareValue(-9223372036854775807L, BIGGER,Long.MIN_VALUE );
    compareValue(23545L, BIGGER, 9223L);
    compareValue(0L, BIGGER, -9223L);
    compareValue(245L, BIGGER, 0L);
    compareValue(-9222L, BIGGER, -9223L);
    compareValue(9224L, BIGGER, 9223L);

    harness.checkPoint("Bigger/smaller Integer");
    compareValue(Integer.MAX_VALUE, BIGGER, 2147483646);
    compareValue(Integer.MAX_VALUE, BIGGER, Integer.MIN_VALUE);
    compareValue(-2147483647, BIGGER, Integer.MIN_VALUE );
    compareValue(23545, BIGGER, 9223);
    compareValue(0, BIGGER, -9223);
    compareValue(245, BIGGER, 0);
    compareValue(-9222, BIGGER, -9223);
    compareValue(9224, BIGGER, 9223);

    harness.checkPoint("Bigger/smaller Short");
    compareValue(Short.MAX_VALUE, BIGGER, (short)32766);
    compareValue(Short.MAX_VALUE, BIGGER, Short.MIN_VALUE);
    compareValue((short)-32767, BIGGER,Short.MIN_VALUE );
    compareValue(23545, BIGGER, 9223);
    compareValue(0, BIGGER, -9223);
    compareValue(245, BIGGER, 0);
    compareValue(-9222, BIGGER, -9223);
    compareValue(9224, BIGGER, 9223);

    harness.checkPoint("Bigger/smaller Byte");
    compareValue(Byte.MAX_VALUE, BIGGER, 126);
    compareValue(Byte.MAX_VALUE, BIGGER, Byte.MIN_VALUE);
    compareValue(-127, BIGGER,Long.MIN_VALUE );
    compareValue(23, BIGGER, 22);
    compareValue(0, BIGGER, -92);
    compareValue(45, BIGGER, 0);
    compareValue(-22, BIGGER, -23);
    compareValue(24, BIGGER, 23);

    harness.checkPoint("Bigger/smaller  Floats");
    compareValue( 100.1f, BIGGER,   99.9f);
    compareValue( 100.1f, BIGGER, -100.1f);
    compareValue(-100.1f, SMALLER, -99.9f);
    compareValue( 100.1f, BIGGER,  -99.9f);
    compareValue(-100.1f, SMALLER,  99.9f);
    compareValue(Float.MIN_VALUE , BIGGER,  0.0f);
    compareValue(Float.MAX_VALUE , BIGGER,  3.0E12f);
    compareValue( 0.0f, EQUALS, -0.0f);
    harness.check(!(0.0f > -0.0f), "0.0 == -0.0 test -- 1" );
    harness.check(!(0.0f < -0.0f), "0.0 == -0.0 test -- 2" );
    harness.check(0.0f >= -0.0f, "0.0 == -0.0 test -- 3" );
    harness.check(0.0f >= -0.0f, "0.0 == -0.0 test -- 4" );


    harness.checkPoint("Bigger/smaller doubles");

    compareValue( 100.1, BIGGER,   99.9);
    compareValue( 100.1, BIGGER, -100.1);
    compareValue(-100.1, SMALLER, -99.9);
    compareValue( 100.1, BIGGER,  -99.9);
    compareValue(-100.1, SMALLER,  99.9);
    compareValue(Double.MIN_VALUE , BIGGER,  0.0);
    compareValue(Double.MAX_VALUE , BIGGER,  3.0E234);

    compareValue( 0.0, EQUALS, -0.0);
    harness.check(!(0.0 > -0.0), "0.0 == -0.0 test -- 1" );
    harness.check(!(0.0 < -0.0), "0.0 == -0.0 test -- 2" );
    harness.check(0.0 >= -0.0, "0.0 == -0.0 test -- 3" );
    harness.check(0.0 >= -0.0, "0.0 == -0.0 test -- 4" );

    harness.checkPoint("Bigger/smaller infinite and NaN values Float");
    compareValue( Float.POSITIVE_INFINITY, BIGGER, 0.0f);
    compareValue( Float.NEGATIVE_INFINITY, SMALLER,0.0f);
    compareValue( Float.NEGATIVE_INFINITY, SMALLER, (0.0f- Float.MAX_VALUE));
    compareValue( Float.POSITIVE_INFINITY, BIGGER, Float.MAX_VALUE);
    compareValue( Float.POSITIVE_INFINITY, BIGGER, Float.NEGATIVE_INFINITY);
    compareValue( Float.NaN, UNDEFINED);
    compareValue( Float.NEGATIVE_INFINITY, UNDEFINED, Float.NaN);
    compareValue( Float.POSITIVE_INFINITY, UNDEFINED, Float.NaN);
    compareValue( Float.MIN_VALUE, UNDEFINED, Float.NaN);
    compareValue( Float.MAX_VALUE, UNDEFINED, Float.NaN);
    harness.check(!(Float.NaN > 2323.78f),"Comparing NaN should always FALSE");
    harness.check(!(Float.NaN < 2323.78f),"Comparing NaN should always FALSE");
    harness.check(!(Float.NaN <= 2323.78f),"Comparing NaN should always FALSE");
    harness.check(!(Float.NaN >= 2323.78f),"Comparing NaN should always FALSE");

    harness.checkPoint("Bigger/smaller infinite and NaN values Double");
    compareValue( Double.POSITIVE_INFINITY, BIGGER, 0.0);
    compareValue( Double.NEGATIVE_INFINITY, SMALLER,0.0);
    compareValue( Double.NEGATIVE_INFINITY, SMALLER, (0.0- Double.MAX_VALUE));
    compareValue( Double.POSITIVE_INFINITY, BIGGER, Double.MAX_VALUE);
    compareValue( Double.POSITIVE_INFINITY, BIGGER, Double.NEGATIVE_INFINITY);
    compareValue( Double.NaN, UNDEFINED);
    compareValue( Double.NEGATIVE_INFINITY, UNDEFINED, Double.NaN);
    compareValue( Double.POSITIVE_INFINITY, UNDEFINED, Double.NaN);
    compareValue( Double.MIN_VALUE, UNDEFINED, Double.NaN);
    compareValue( Double.MAX_VALUE, UNDEFINED, Double.NaN);
    harness.check(!(Double.NaN > 2323.78), "Comparing NaN should always FALSE");
    harness.check(!(Double.NaN < 2323.78), "Comparing NaN should always FALSE");
    harness.check(!(Double.NaN >= 2323.78),"Comparing NaN should always FALSE");
    harness.check(!(Double.NaN <= 2323.78),"Comparing NaN should always FALSE");

    harness.checkPoint("== Float");
    harness.check(  234.0f == 234.0f,"test == nr 1");
    harness.check(!( 234.0f == -234.0f),"test == nr 2" );
    harness.check(!( 234.0f == 234.0E1f),"test == nr 3" );
    harness.check(!( 234.0f == 234.0E-45f),"test == nr 4" );
    harness.check(!( Float.NaN == 234.0E-45f),"test == nr 5" );
    harness.check(!( 234.0f != 234.0f),"test != nr 1");
    harness.check(  234.0f != -234.0f,"test != nr 2" );
    harness.check(  234.0f != 234.0E1f,"test != nr 3" );
    harness.check(  234.0f != 234.0E-45f,"test != nr 4" );
    harness.check(  Float.NaN != 234.0E-45f,"test != nr 5" );
    harness.check(  Float.NaN != Float.NaN,"NaN != NaN is true" );

    harness.checkPoint("== Double");
    harness.check(  234.0 == 234.0,"test == nr 1");
    harness.check(!( 234.0 == -234.0),"test == nr 2" );
    harness.check(!( 234.0 == 234.0E1),"test == nr 3" );
    harness.check(!( 234.0 == 234.0E-45),"test == nr 4" );
    harness.check(!( Double.NaN == 234.0E-45),"test == nr 5" );
    harness.check(!(  234.0 != 234.0),"test != nr 1");
    harness.check(  234.0 != -234.0,"test != nr 2" );
    harness.check(  234.0 != 234.0E1,"test != nr 3" );
    harness.check(  234.0 != 234.0E-45,"test != nr 4" );
    harness.check(  Double.NaN != 234.0E-45,"test != nr 5" );
    harness.check(  Double.NaN != Double.NaN,"NaN != NaN is true" );


  }

  public void testConversions()
  {

    harness.checkPoint("conversins to floating point primitives");
    compareValue( 100.0f, EQUALS, (float)((byte)  100) );
    compareValue( 100.0f, EQUALS, (float)((short) 100) );
    compareValue( 100.0f, EQUALS, (float) 100  );
    compareValue( 100.0f, EQUALS, (float) 100L );
    compareValue(-100.0f, EQUALS, (float)((byte) -100) );
    compareValue(-100.0f, EQUALS, (float)((short)-100) );
    compareValue(-100.0f, EQUALS, (float)-100  );
    compareValue(-100.0 , EQUALS, (double)-100L );
    compareValue( 100.0 , EQUALS, (double)((byte)  100) );
    compareValue( 100.0 , EQUALS, (double)((short) 100) );
    compareValue( 100.0 , EQUALS, (double) 100  );
    compareValue( 100.0 , EQUALS, (double) 100L );
    compareValue(-100.0 , EQUALS, (double)((byte) -100) );
    compareValue(-100.0 , EQUALS, (double)((short)-100) );
    compareValue(-100.0 , EQUALS, (double)-100  );
    compareValue(-100.0 , EQUALS, (double)-100L );

    compareValue((byte)  100, EQUALS, (byte)  100.0f );
    compareValue((short) 100, EQUALS, (short) 100.0f );
    compareValue( 100 , EQUALS, (int)  100.0f );
    compareValue( 100L, EQUALS, (long) 100.0f );
    compareValue((byte) -100, EQUALS, (byte) -100.0f );
    compareValue((short)-100, EQUALS, (short)-100.0f );
    compareValue(-100 , EQUALS, (int) -100.0f );
    compareValue(-100L, EQUALS, (long)-100.0f );
    compareValue((byte)  100, EQUALS, (byte)  100.0 );
    compareValue((short) 100, EQUALS, (short) 100.0 );
    compareValue( 100 , EQUALS, (int)  100.0 );
    compareValue( 100L, EQUALS, (long) 100.0 );
    compareValue((byte) -100, EQUALS, (byte) -100.0 );
    compareValue((short)-100, EQUALS, (short)-100.0 );
    compareValue(-100 , EQUALS, (int) -100.0 );
    compareValue(-100L, EQUALS, (long)-100.0 );

    harness.checkPoint("conversions infinite and NaN values");
    compareValue(Float.POSITIVE_INFINITY, EQUALS, (float)Double.POSITIVE_INFINITY);
    compareValue(Float.NEGATIVE_INFINITY, EQUALS, (float)Double.NEGATIVE_INFINITY);
    harness.check(Float.isNaN((float)Double.NaN));
    harness.check(Float.toString((float)Double.NaN).equals("NaN") );

    compareValue( 0.0f,EQUALS, (float) 0.0);
    compareValue( 0.0f,EQUALS, (float) 0.0 );
    compareValue(-0.0f,EQUALS, (float)-0.0);
    compareValue(-0.0f,EQUALS, (float)-0.0 );
    compareValue(Double.POSITIVE_INFINITY, EQUALS, (double)Float.POSITIVE_INFINITY);
    compareValue(Double.NEGATIVE_INFINITY, EQUALS, (double)Float.NEGATIVE_INFINITY);
    harness.check(Double.isNaN((double)Float.NaN));
    harness.check(Double.toString((double)Float.NaN).equals("NaN") );
    compareValue( 0.0 ,EQUALS, (double) 0.0f);
    compareValue( 0.0 ,EQUALS, (double) 0.0f);
    compareValue(-0.0 ,EQUALS, (double)-0.0f);
    compareValue(-0.0 ,EQUALS, (double)-0.0f);

  }

  public void testInfiniteNanOperators()
  {
    harness.checkPoint("Float infinity and NaN definitions");
    harness.check(Float.toString(0.0f/0.0f).equals("NaN") );
    harness.check(Float.toString(-0.0f/0.0f).equals("NaN") );
    compareValue( 1.0f/0.0f, EQUALS, Float.POSITIVE_INFINITY);
    compareValue(-1.0f/0.0f, EQUALS, Float.NEGATIVE_INFINITY);

    harness.checkPoint("Float infinity and NaN definitions");
    harness.check(Double.toString(0.0f/0.0f).equals("NaN") );
    harness.check(Double.toString(-0.0f/0.0f).equals("NaN") );
    compareValue( 1.0/0.0, EQUALS, Double.POSITIVE_INFINITY);
    compareValue(-1.0/0.0, EQUALS, Double.NEGATIVE_INFINITY);

    harness.checkPoint("Float infinity and NaN add, subtract");
    compareValue( Float.POSITIVE_INFINITY + Float.POSITIVE_INFINITY, EQUALS, Float.POSITIVE_INFINITY);
    compareValue( Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY, EQUALS, Float.NEGATIVE_INFINITY);
    compareValue( Float.POSITIVE_INFINITY - Float.NEGATIVE_INFINITY, EQUALS, Float.POSITIVE_INFINITY);
    compareValue( Float.NEGATIVE_INFINITY - Float.POSITIVE_INFINITY, EQUALS, Float.NEGATIVE_INFINITY);
    float f1 = Float.POSITIVE_INFINITY;
    compareValue( f1 - Float.POSITIVE_INFINITY, UNDEFINED);
    compareValue( f1 + Float.NEGATIVE_INFINITY, UNDEFINED);
    f1 = Float.NEGATIVE_INFINITY;
    compareValue( f1 - Float.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( f1 + Float.POSITIVE_INFINITY, UNDEFINED);

    harness.checkPoint("Double infinity and NaN add, subtract");
    compareValue( Double.POSITIVE_INFINITY + Double.POSITIVE_INFINITY, EQUALS, Double.POSITIVE_INFINITY);
    compareValue( Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY, EQUALS, Double.NEGATIVE_INFINITY);
    compareValue( Double.POSITIVE_INFINITY - Double.NEGATIVE_INFINITY, EQUALS, Double.POSITIVE_INFINITY);
    compareValue( Double.NEGATIVE_INFINITY - Double.POSITIVE_INFINITY, EQUALS, Double.NEGATIVE_INFINITY);

    compareValue( Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY, UNDEFINED);
    compareValue( Double.NEGATIVE_INFINITY - Double.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( Double.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY, UNDEFINED);


    harness.checkPoint("Float infinity and NaN multiplicatins, devisions, remainders");
    compareValue( Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY, EQUALS, Float.POSITIVE_INFINITY);
    compareValue( Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY, EQUALS, Float.POSITIVE_INFINITY);
    compareValue( Float.POSITIVE_INFINITY * Float.NEGATIVE_INFINITY, EQUALS, Float.NEGATIVE_INFINITY);
    compareValue( Float.NEGATIVE_INFINITY * Float.POSITIVE_INFINITY, EQUALS, Float.NEGATIVE_INFINITY);

    f1 = Float.POSITIVE_INFINITY;
    compareValue( f1 / Float.POSITIVE_INFINITY, UNDEFINED);
    compareValue( f1 / Float.NEGATIVE_INFINITY, UNDEFINED);
    f1 = Float.NEGATIVE_INFINITY;
    compareValue( f1 / Float.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( f1 / Float.POSITIVE_INFINITY, UNDEFINED);

    harness.checkPoint("Double infinity and NaN multiplicatins, devisions, remainders");
    compareValue( Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY, EQUALS, Double.POSITIVE_INFINITY);
    compareValue( Double.NEGATIVE_INFINITY * Double.NEGATIVE_INFINITY, EQUALS, Double.POSITIVE_INFINITY);
    compareValue( Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY, EQUALS, Double.NEGATIVE_INFINITY);
    compareValue( Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY, EQUALS, Double.NEGATIVE_INFINITY);

    compareValue( Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY, UNDEFINED);
    compareValue( Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY, UNDEFINED);
    compareValue( Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY, UNDEFINED);
  }


/** private help functions for the comparisons. since the normal check either compares two values by casting them
into integers (decimal values) or by casting them into strings(floating point), special functions have to be used for
direct primitive comparisons byte-to-byte, float-to-float etc.  */

private void compareValue(byte first, int specification, byte second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected byte "+first+" smaller then "+second+" (should be bigger)" );
      else
        harness.fail("Detected byte "+first+" equals to "+second+" (should be bigger)" );
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected byte "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected byte "+first+" smaller then "+second+" (should be equal)" );
      else
        harness.check(true);
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected byte "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else
        harness.fail("Detected byte "+first+" equals to "+second+" (should be smaller)" );
    break;
  }
}

private void compareValue(short first, int specification, short second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected short "+first+" smaller then "+second+" (should be bigger)" );
      else
        harness.fail("Detected short "+first+" equals to "+second+" (should be bigger)" );
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected short "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected short "+first+" smaller then "+second+" (should be equal)" );
      else
        harness.check(true);
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected short "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else
        harness.fail("Detected short "+first+" equals to "+second+" (should be smaller)" );
    break;
  }
}

private void compareValue(int first, int specification, int second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected int "+first+" smaller then "+second+" (should be bigger)" );
      else
        harness.fail("Detected int "+first+" equals to "+second+" (should be bigger)" );
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected int "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected int "+first+" smaller then "+second+" (should be equal)" );
      else
        harness.check(true);
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected int "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else
        harness.fail("Detected int "+first+" equals to "+second+" (should be smaller)" );
    break;
  }
}

private void compareValue(long first, int specification, long second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected long "+first+" smaller then "+second+" (should be bigger)" );
      else
        harness.fail("Detected long "+first+" equals to "+second+" (should be bigger)" );
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected long "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected long "+first+" smaller then "+second+" (should be equal)" );
      else
        harness.check(true);
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected long "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else
        harness.fail("Detected long "+first+" equals to "+second+" (should be smaller)" );
    break;
  }
}

private void compareValue(float first, int specification, float second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected float "+first+" smaller then "+second+" (should be bigger)" );
      else if (first == second)
        harness.fail("Detected float "+first+" equals to "+second+" (should be bigger)" );
      else
        harness.fail("no comparison between float "+first+" and "+second+" possible (should be bigger)");
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected float "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected float "+first+" smaller then "+second+" (should be equal)" );
      else if (first == second)
        harness.check(true);
      else
        harness.fail("no comparison between float "+first+" and "+second+" possible (should be bigger)");
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected float "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else if (first == second)
        harness.fail("Detected float "+first+" equals to "+second+" (should be smaller)" );
      else
        harness.fail("no comparison between float "+first+" and "+second+" possible (should be bigger)");
    break;

    case UNDEFINED:
      if (first > second)
        harness.fail("Detected float "+first+" bigger then "+second+" (should be undefined)" );
      else if (first < second)
        harness.fail("Detected float "+first+" smaller then "+second+" (should be undefined)" );
      else if (first == second)
        harness.fail("Detected float "+first+" equals to "+second+" (should be undefined)" );
      else
        harness.check(true);
    break;
  }
}

private void compareValue(double first, int specification, double second)
{
  switch (specification)
  {
    case BIGGER:
      if (first > second)
        harness.check(true);
      else if (first < second)
        harness.fail("Detected double "+first+" smaller then "+second+" (should be bigger)" );
      else if (first == second)
        harness.fail("Detected double "+first+" equals to "+second+" (should be bigger)" );
      else
        harness.fail("no comparison between double "+first+" and "+second+" possible (should be bigger)");
    break;

    case EQUALS:
      if (first > second)
        harness.fail("Detected double "+first+" bigger then "+second+" (should be equal)" );
      else if (first < second)
        harness.fail("Detected double "+first+" smaller then "+second+" (should be equal)" );
      else if (first == second)
        harness.check(true);
      else
        harness.fail("no comparison between double "+first+" and "+second+" possible (should be bigger)");
    break;

    case SMALLER:
      if (first > second)
        harness.fail("Detected double "+first+" bigger then "+second+" (should be smaller)" );
      else if (first < second)
        harness.check(true);
      else if (first == second)
        harness.fail("Detected double "+first+" equals to "+second+" (should be smaller)" );
      else
        harness.fail("no comparison between double "+first+" and "+second+" possible (should be bigger)");
    break;

    case UNDEFINED:
      if (first > second)
        harness.fail("Detected double "+first+" bigger then "+second+" (should be undefined)" );
      else if (first < second)
        harness.fail("Detected double "+first+" smaller then "+second+" (should be undefined)" );
      else if (first == second)
        harness.fail("Detected double "+first+" equals to "+second+" (should be undefined)" );
      else
        harness.check(true);
    break;
  }
}

private void compareValue(float first, int specification)
{
  compareValue(first, specification, 0.0f);
}

private void compareValue(double first, int specification)
{
  compareValue(first, specification, 0.0 );
}
/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("operators//opcodes");
		testMaximumValues();
		testBiggerSmaller();
		testConversions();
		testBasicOps();
		testBooleanOps();
		testShiftOps();
		testBitwiseOps();
		testInfiniteNanOperators();
	}
	
}
