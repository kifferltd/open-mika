// Tags: JDK1.0

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

package gnu.testlet.wonka.lang.Number;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;

public class PrimitiveConversionTest implements Testlet
{
  TestHarness harness;
	
  private void testAssignmentWidening()
  {
    //our assignment targets:
    short   s;
    int     i;
    long    l;
    float   f;
    double  d;

    byte  bp = Byte.MAX_VALUE;
    byte  bn = Byte.MIN_VALUE;
    // byte to short, int, long, float, double...
    harness.checkPoint("AssignmentWidening : byte to short, int, long, float, double...");
    s=bp;
    harness.check( (s== 0x007f),"widening byte to short");
    s=bn;
    harness.check( (s==-0x0080),"widening byte to short");
    i=bp;
    harness.check( (i== 0x0000007f),"widening byte to int");
    i=bn;
    harness.check( (i==-0x00000080),"widening byte to int");
    l=bp;
    harness.check( (l== 0x000000000000007fL),"widening byte to long");
    l=bn;
    harness.check( (l==-0x0000000000000080L),"widening byte to long");
    f=bp;
    harness.check( (f== 127.0f),"widening byte to float");
    f=bn;
    harness.check( (f==-128.0f),"widening byte to float");
    d=bp;
    harness.check( (d== 127.0D),"widening byte to double");
    d=bn;
    harness.check( (d==-128.0D),"widening byte to double");

    short sp=Short.MAX_VALUE;
    short sn=Short.MIN_VALUE;
    //short to int, long, float, double
    harness.checkPoint("AssignmentWidening : short to int, long, float, double...");
    i=sp;
    harness.check( (i== 0x00007fff),"widening short to int");
    i=sn;
    harness.check( (i==-0x00008000),"widening short to int");
    l=sp;
    harness.check( (l== 0x0000000000007fffL),"widening short to long");
    l=sn;
    harness.check( (l==-0x0000000000008000L),"widening short to long");
    f=sp;
    harness.check( (f== 32767.0f),"widening short to float");
    f=sn;
    harness.check( (f==-32768.0f),"widening short to float");
    d=sp;
    harness.check( (d== 32767.0D),"widening short to double");
    d=sn;
    harness.check( (d==-32768.0D),"widening short to double");

    char  cp=Character.MAX_VALUE;//'\uffff';
    char  cn=Character.MIN_VALUE;//'\u0000';
    //short to int, long, float, double
    harness.checkPoint("AssignmentWidening : char to int, long, float, double...");
    i=cp;
    harness.check( (i== 0x0000ffff),"widening char to int");
    i=cn;
    harness.check( (i==-0x00000000),"widening char to int");
    l=cp;
    harness.check( (l== 0x000000000000ffffL),"widening char to long");
    l=cn;
    harness.check( (l==-0x0000000000000000L),"widening char to long");
    f=cp;
    harness.check( (f== 65535.0f),"widening char to float");
    f=cn;
    harness.check( (f== 0.0f),"widening char to float");
    d=cp;
    harness.check( (d== 65535.0D),"widening char to double");
    d=cn;
    harness.check( (d== 0.0D),"widening char to double");

    //As a float is a IEEE 754 32-bit value representation with only x bits rederved for mantisse representation,
    //calling for a full 32-bit number will cause rounding. Therefore, use a large but round value for the big numbers
    //but also test small numbers without rounding
    int oneAndHalfGiga= 1500000000;
    int minusOneGiga  =-1000000000;
    int caesar=-15;
    int napoleon=1815;
    //int to long, float, double
    harness.checkPoint("AssignmentWidening : int to long, float, double...");
    l=oneAndHalfGiga;
    harness.check( (l== 1500000000L),"widening int to long");
    l=minusOneGiga;
    harness.check( (l==-1000000000L),"widening int to long");
    l=caesar;
    harness.check( (l==-15L),"widening int to long");
    l=napoleon;
    harness.check( (l==1815L),"widening int to long");
    f=oneAndHalfGiga;
    harness.check( (f== 1.5e9f),"widening int to float");
    f=minusOneGiga;
    harness.check( (f==-1.0e9f),"widening int to float");
    f=caesar;
    harness.check( (f==-15.0f),"widening int to float");
    f=napoleon;
    harness.check( (f==1815.0f),"widening int to float");
    d=oneAndHalfGiga;
    harness.check( (d== 1.5e9D),"widening int to double");
    d=minusOneGiga;
    harness.check( (d==-1.0e9D),"widening int to double");
    d=caesar;
    harness.check( (d==-15.0D),"widening int to double");
    d=napoleon;
    harness.check( (d==1815.0D),"widening int to double");

    //same remarks as for int
    long elevenFourTerra= 11400000000000L;
    long minusSevenTerra= -7000000000000L;
    long Friendswood=77546;
    long HulsteDorp =-8531;
    //long to float, double
    harness.checkPoint("AssignmentWidening : long to float, double...");
    f=elevenFourTerra;
    harness.check( (f== 11.4e12f),"widening long to float");
    f=minusSevenTerra;
    harness.check( (f== -7.0e12f),"widening long to float");
    f=Friendswood;
    harness.check( (f==77546.0f),"widening long to float");
    f=HulsteDorp;
    harness.check( (f==-8531.0f),"widening long to float");
    d=elevenFourTerra;
    harness.check( (d== 11.4e12D),"widening long to double");
    d=minusSevenTerra;
    harness.check( (d== -7.0e12D),"widening long to double");
    d=Friendswood;
    harness.check( (d==77546.0D),"widening long to double");
    d=HulsteDorp;
    harness.check( (d==-8531.0D),"widening long to double");

    //float to double
    float posCharge = 1.9e-23f;
    float negCharge =-1.9e-23f;
    float smallest = Float.MIN_VALUE;
    float biggest  = Float.MAX_VALUE;
    float nan=Float.NaN;
    float posInfinite=Float.POSITIVE_INFINITY;
    float negInfinite=Float.NEGATIVE_INFINITY;
    //  float & double
    harness.checkPoint("AssignmentWidening : float to double...");
    d=posCharge;
    harness.check(inRange(1.9,-23,d),"widening float to double "); // calculate a 'granularity 0f +-0.0000001
    d=negCharge;
    harness.check(inRange(-1.9,-23,d),"widening float to double ");
    d=smallest;
    harness.check(inRange(1.4012984,-45,d),"widening float to double "+d);
    d=biggest;
    harness.check(inRange(3.4028234,38,d),"widening float to double "+d);
    d=nan;
    harness.check(Double.isNaN(d),"widening float to double "+d);
    d=posInfinite;
    harness.check((d==Double.POSITIVE_INFINITY),"widening float to double"+d);
    d=negInfinite;
    harness.check((d==Double.NEGATIVE_INFINITY),"widening float to double"+d);
  }

    //Assignment (variable = expression) is valid for narrowing conversion IF
    //=> expression is constant (final static)
    //=> expression of type byte, short, char or int,
    //=> variable of type byte, short, char
    //=> value of expression is in valid range for variable

  final static byte  bnull = 0x00;
  final static short snull = 0x0000;
  final static char  cnull ='\u0000';
  final static int   inull = 0x00000000;

  //int, short, char =>byte
  final static byte  bmax  = 0x7f;
  final static byte  bmin  =-0x80;
  final static short smaxbyte  = 0x007f;
  final static short sminbyte  =-0x0080;
  final static char  cmaxbyte  = '\u007f';
  //final static char  cminbyte  =-0x0080;
  final static int   imaxbyte  = 0x0000007f;
  final static int   iminbyte  =-0x00000080;

  // int, byte=>short
  final static short smax  = 0x7fff;
  final static short smin  =-0x8000;
  final static char  cmaxshort  ='\u7fff';
  //final static char  cminshort  =-0x0000;
  final static int   imaxshort  = 0x00007fff;
  final static int   iminshort  =-0x00008000;

  // int, short => byte
  final static char  cmax  ='\uffff';
  //final static char  cmin  ='\u0000';
  //final static short  smaxchar  = 0xffff';
  //final static char  sminchar  =-0x0000;
  final static int   imaxchar  = 0x0000ffff;
  //final static int   iminchar  =-0x00008000;


  private void testAssignmentNarrowing()
  {
    //our assignment targets:
    byte    b;
    short   s;
    char    c;
    int     i;
    //--------------------------------------------------------------------------------------------------------------------------------  	
    //   short, char, int, long, narrowed into byte
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("AssignmentNarrowing :  short, char, int, long, narrowed into byte");
    //byte==0
    b=(byte)inull;
    harness.check((b==bnull),"int=>byte, byte=(+)0");
    b=(byte)cnull;
    harness.check((b==bnull),"char=>byte, byte=(+)0");
    b=(byte)snull;
    harness.check((b==bnull),"short=>byte, byte=(+)0");
    //byte>0
    b=(byte)imaxbyte;
    harness.check((b==bmax),"(+)int=>byte, byte=>0");
    b=(byte)cmaxbyte;
    harness.check((b==bmax),"(+)char=>byte, byte>0");
    b=(byte)smaxbyte;
    harness.check((b==bmax),"(+)short=>byte, byte>0"); 	
    //byte<0 =>
    b=(byte)iminbyte;
    harness.check((b==bmin),"(+)int=>byte, byte=<0");
    //b=(byte)cminbyte;
    //harness.check((b==bmin),"(+)int=>byte, byte=<0");
    b=(byte)sminbyte;
    harness.check((b==bmin),"(+)int=>byte, byte=<0");
      	
    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  char, int, narrowed into short
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("AssignmentNarrowing :   char, int, long, narrowed into short");
    //char==0 => last 16 bits(4hexes) of number must be +-0
    s=(short)inull;
    harness.check((s==0),"int=>short, short=(+)0");
    s=(short)cnull;
    harness.check((s==0),"char=>short, short=(+)0");
    //char>0
    s=(short)imaxshort;
    harness.check((s==smax),"(+)int=>short, short="+s+" instead of 4095");
    s=(short)cmaxshort;
    harness.check((s==smax),"(+)char=>short, short="+s+" instead of 4095");
    //char<0
    s=(short)iminshort;
    harness.check((s==smin),"(+)int=>short, short="+s+" instead of 4095");
    //s=(short)cminshort;
    //harness.check((s==smin),"(+)char=>short, short="+s+" instead of 4095");

    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  short, int, long, narrowed into char
    //--------------------------------------------------------------------------------------------------------------------------------  		
    harness.checkPoint("AssignmentNarrowing :   short, int, long, narrowed into char");
    //char==0 => last 16 bits(4hexes) of number must be +-0
    c=(char)inull;
    harness.check((c==cnull),"int=>char, char=(+)0");
    c=(char)snull;
    harness.check((c==cnull),"short=>char, char=(+)0");
    //char'>0'
    c=(char)imaxchar;
    harness.check((c==cmax),"(+)int=>char, char="+c+" instead of 4095");
    c=(char)smax;
    harness.check((c==cmaxshort),"(+)short=>char, char="+c+" instead of 4095");

    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  special case: byte into char
    //--------------------------------------------------------------------------------------------------------------------------------  		
    c=(char)bnull;
    harness.check((c==cnull),"(-)int=>char, char="+c+" instead of 4095");
    c=(char)bmax;
    harness.check((c==cmaxbyte),"(-)int=>char, char="+c+" instead of 4095");

  }


    private byte  b_Positive() {return Byte.MAX_VALUE;}
    private byte  b_Negative() {return Byte.MIN_VALUE;}
    private short s_Positive() {return Short.MAX_VALUE;}
    private short s_Negative() {return Short.MIN_VALUE;}
    private char  c_Positive() {return Character.MAX_VALUE;}//'\uffff';
    private char  c_Negative() {return Character.MIN_VALUE;}//'\u0000';

    private int  i_OneAndHalfGiga() {return 1500000000;}
    private int  i_MinusOneGiga()   {return -1000000000;}
    private int  i_Napoleon()       {return 1815;}
    private int  i_Caesar()         {return -15; }
    private long l_elevenFourTerra() {return 11400000000000L;}
    private long l_minusSevenTerra() {return -7000000000000L;}
    private long l_Friendswood()    {return 77546;}
    private long l_HulsteDorp()     {return -8531;}
	
    private float f_PosCharge()   {return  1.9e-23f;}
    private float f_NegCharge()   {return -1.9e-23f;}
    private float f_Smallest()    {return Float.MIN_VALUE;}
    private float f_Biggest()     {return Float.MAX_VALUE;}
    private float f_Nan()         {return Float.NaN;}
    private float f_PosInfinite() {return Float.POSITIVE_INFINITY;}
    private float f_NegInfinite() {return Float.NEGATIVE_INFINITY;}
	
	private void testInvocationWidening()
	{
    //our assignment targets:
    short   s;
    int     i;
    long    l;
    float   f;
    double  d;

    // byte to short, int, long, float, double...
    harness.checkPoint("Invocation Widening : byte to short, int, long, float, double...");
    s=b_Positive();
    harness.check( (s== 0x007f),"widening byte to short");
    s=b_Negative();
    harness.check( (s==-0x0080),"widening byte to short");
    i=b_Positive();
    harness.check( (i== 0x0000007f),"widening byte to int");
    i=b_Negative();
    harness.check( (i==-0x00000080),"widening byte to int");
    l=b_Positive();
    harness.check( (l== 0x000000000000007fL),"widening byte to long");
    l=b_Negative();
    harness.check( (l==-0x0000000000000080L),"widening byte to long");
    f=b_Positive();
    harness.check( (f== 127.0f),"widening byte to float");
    f=b_Negative();
    harness.check( (f==-128.0f),"widening byte to float");
    d=b_Positive();
    harness.check( (d== 127.0D),"widening byte to double");
    d=b_Negative();
    harness.check( (d==-128.0D),"widening byte to double");

    //short to int, long, float, double
    harness.checkPoint("Invocation Widening : short to int, long, float, double...");
    i=s_Positive();
    harness.check( (i== 0x00007fff),"widening short to int");
    i=s_Negative();
    harness.check( (i==-0x00008000),"widening short to int");
    l=s_Positive();
    harness.check( (l== 0x0000000000007fffL),"widening short to long");
    l=s_Negative();
    harness.check( (l==-0x0000000000008000L),"widening short to long");
    f=s_Positive();
    harness.check( (f== 32767.0f),"widening short to float");
    f=s_Negative();
    harness.check( (f==-32768.0f),"widening short to float");
    d=s_Positive();
    harness.check( (d== 32767.0D),"widening short to double");
    d=s_Negative();
    harness.check( (d==-32768.0D),"widening short to double");

    //char to int, long, float, double
    harness.checkPoint("Invocation Widening : char to int, long, float, double...");
    i=c_Positive();
    harness.check( (i== 0x0000ffff),"widening char to int");
    i=c_Negative();
    harness.check( (i==-0x00000000),"widening char to int");
    l=c_Positive();
    harness.check( (l== 0x000000000000ffffL),"widening char to long");
    l=c_Negative();
    harness.check( (l==-0x0000000000000000L),"widening char to long");
    f=c_Positive();
    harness.check( (f== 65535.0f),"widening char to float");
    f=c_Negative();
    harness.check( (f== 0.0f),"widening char to float");
    d=c_Positive();
    harness.check( (d== 65535.0D),"widening char to double");
    d=c_Negative();
    harness.check( (d== 0.0D),"widening char to double");

    //As a float is a IEEE 754 32-bit value representation with only x bits rederved for mantisse representation,
    //calling for a full 32-bit number will cause rounding. Therefore, use a large but round value for the big numbers
    //but also test small numbers without rounding
    //int to long, float, double
    harness.checkPoint("Invocation Widening : int to long, float, double...");
    l=i_OneAndHalfGiga();
    harness.check( (l== 1500000000L),"widening int to long");
    l=i_MinusOneGiga();
    harness.check( (l==-1000000000L),"widening int to long");
    l=i_Napoleon();
    harness.check( (l==1815L),"widening int to long");
    l=i_Caesar();
    harness.check( (l==-15L),"widening int to long");
    f=i_OneAndHalfGiga();
    harness.check( (f== 1.5e9f),"widening int to float");
    f=i_MinusOneGiga();
    harness.check( (f==-1.0e9f),"widening int to float");
    f=i_Napoleon();
    harness.check( (f==1815.0f),"widening int to float");
    f=i_Caesar();
    harness.check( (f==-15.0f),"widening int to float");
    d=i_OneAndHalfGiga();
    harness.check( (d== 1.5e9D),"widening int to double");
    d=i_MinusOneGiga();
    harness.check( (d==-1.0e9D),"widening int to double");
    d=i_Napoleon();
    harness.check( (d==1815.0D),"widening int to double");
    d=i_Caesar();
    harness.check( (d==-15.0D),"widening int to double");

    //same remarks as for int
    //long to float, double
    harness.checkPoint("Invocation Widening : long, to float, double...");
    f=l_elevenFourTerra();
    harness.check( (f== 11.4e12f),"widening long to float");
    f=l_minusSevenTerra();
    harness.check( (f== -7.0e12f),"widening long to float");
    f=l_Friendswood();
    harness.check( (f==77546.0f),"widening long to float");
    f=l_HulsteDorp();
    harness.check( (f==-8531.0f),"widening long to float");
    d=l_elevenFourTerra();
    harness.check( (d== 11.4e12D),"widening long to double");
    d=l_minusSevenTerra();
    harness.check( (d== -7.0e12D),"widening long to double");
    d=l_Friendswood();
    harness.check( (d==77546.0D),"widening long to double");
    d=l_HulsteDorp();
    harness.check( (d==-8531.0D),"widening long to double");

    //float to double
    //  float & double
    harness.checkPoint("Invocation Widening : float to double...");
    d=f_PosCharge();
    harness.check(inRange(1.9,-23,d),"widening float to double "+d); // calculate a 'granularity 0f +-0.0000001
    d=f_NegCharge();
    harness.check(inRange(-1.9,-23,d),"widening float to double "+d);
    d=f_Smallest();
    harness.check(inRange(1.4012984,-45,d),"widening float to double "+d);
    d=f_Biggest();
    harness.check(inRange(3.4028234,38,d),"widening float to double "+d);
    d=f_Nan();
    harness.check(Double.isNaN(d),"widening float to double "+d);
    d=f_PosInfinite();
    harness.check((d==Double.POSITIVE_INFINITY),"widening float to double");
    d=f_NegInfinite();
    harness.check((d==Double.NEGATIVE_INFINITY),"widening float to double");
  }

	private void testCastingWidening()
	{
    //our assignment targets:
    short   s;
    int     i;
    long    l;
    float   f;
    double  d;

    byte  bp = Byte.MAX_VALUE;
    byte  bn = Byte.MIN_VALUE;
    // byte to short, int, long, float, double...
    harness.checkPoint("type cast widening : byte to short, int, long, float, double...");;
    s=(short)bp;
    harness.check( (s== 0x007f),"widening byte to short");
    s=(short)bn;
    harness.check( (s==-0x0080),"widening byte to short");
    i=(int)bp;
    harness.check( (i== 0x0000007f),"widening byte to int");
    i=(int)bn;
    harness.check( (i==-0x00000080),"widening byte to int");
    l=(long)bp;
    harness.check( (l== 0x000000000000007fL),"widening byte to long");
    l=(long)bn;
    harness.check( (l==-0x0000000000000080L),"widening byte to long");
    f=(float)bp;
    harness.check( (f== 127.0f),"widening byte to float");
    f=(float)bn;
    harness.check( (f==-128.0f),"widening byte to float");
    d=(double)bp;
    harness.check( (d== 127.0D),"widening byte to double");
    d=(double)bn;
    harness.check( (d==-128.0D),"widening byte to double");

    short sp=Short.MAX_VALUE;
    short sn=Short.MIN_VALUE;
    //short to int, long, float, double
    harness.checkPoint("type cast widening : short to int, long, float, double...");
    i=(int)sp;
    harness.check( (i== 0x00007fff),"widening short to int");
    i=(int)sn;
    harness.check( (i==-0x00008000),"widening short to int");
    l=(long)sp;
    harness.check( (l== 0x0000000000007fffL),"widening short to long");
    l=(long)sn;
    harness.check( (l==-0x0000000000008000L),"widening short to long");
    f=(float)sp;
    harness.check( (f== 32767.0f),"widening short to float");
    f=(float)sn;
    harness.check( (f==-32768.0f),"widening short to float");
    d=(double)sp;
    harness.check( (d== 32767.0D),"widening short to double");
    d=(double)sn;
    harness.check( (d==-32768.0D),"widening short to double");

    char  cp=Character.MAX_VALUE;//'\uffff';
    char  cn=Character.MIN_VALUE;//'\u0000';
    //char to int, long, float, double
    harness.checkPoint("type cast widening : char to int, long, float, double...");
    i=(int)cp;
    harness.check( (i== 0x0000ffff),"widening char to int");
    i=(int)cn;
    harness.check( (i==-0x00000000),"widening char to int");
    l=(long)cp;
    harness.check( (l== 0x000000000000ffffL),"widening char to long");
    l=(long)cn;
    harness.check( (l==-0x0000000000000000L),"widening char to long");
    f=(float)cp;
    harness.check( (f== 65535.0f),"widening char to float");
    f=(float)cn;
    harness.check( (f== 0.0f),"widening char to float");
    d=(double)cp;
    harness.check( (d== 65535.0D),"widening char to double");
    d=(double)cn;
    harness.check( (d== 0.0D),"widening char to double");

    //As a float is a IEEE 754 32-bit value representation with only x bits rederved for mantisse representation,
    //calling for a full 32-bit number will cause rounding. Therefore, use a large but round value for the big numbers
    //but also test small numbers without rounding
    int oneAndHalfGiga= 1500000000;
    int minusOneGiga  =-1000000000;
    int caesar=-15;
    int napoleon=1815;
    //int to long, float, double
    harness.checkPoint("type cast widening : int to long, float, double...");
    l=(long)oneAndHalfGiga;
    harness.check( (l== 1500000000L),"widening int to long");
    l=(long)minusOneGiga;
    harness.check( (l==-1000000000L),"widening int to long");
    l=(long)caesar;
    harness.check( (l==-15L),"widening int to long");
    l=(long)napoleon;
    harness.check( (l==1815L),"widening int to long");
    f=(float)oneAndHalfGiga;
    harness.check( (f== 1.5e9f),"widening int to float");
    f=(float)minusOneGiga;
    harness.check( (f==-1.0e9f),"widening int to float");
    f=(float)caesar;
    harness.check( (f==-15.0f),"widening int to float");
    f=(float)napoleon;
    harness.check( (f==1815.0f),"widening int to float");
    d=(double)oneAndHalfGiga;
    harness.check( (d== 1.5e9D),"widening int to double");
    d=(double)minusOneGiga;
    harness.check( (d==-1.0e9D),"widening int to double");
    d=(double)caesar;
    harness.check( (d==-15.0D),"widening int to double");
    d=(double)napoleon;
    harness.check( (d==1815.0D),"widening int to double");

    //same remarks as for int
    long elevenFourTerra= 11400000000000L;
    long minusSevenTerra= -7000000000000L;
    long Friendswood=77546;
    long HulsteDorp =-8531;
    //long to float, double
    harness.checkPoint("type cast widening : long to float, double...");
    f=(float)elevenFourTerra;
    harness.check( (f== 11.4e12f),"widening long to float");
    f=(float)minusSevenTerra;
    harness.check( (f== -7.0e12f),"widening long to float");
    f=(float)Friendswood;
    harness.check( (f==77546.0f),"widening long to float");
    f=(float)HulsteDorp;
    harness.check( (f==-8531.0f),"widening long to float");
    d=(double)elevenFourTerra;
    harness.check( (d== 11.4e12D),"widening long to double");
    d=(double)minusSevenTerra;
    harness.check( (d== -7.0e12D),"widening long to double");
    d=(double)Friendswood;
    harness.check( (d==77546.0D),"widening long to double");
    d=(double)HulsteDorp;
    harness.check( (d==-8531.0D),"widening long to double");

    //float to double
    float posCharge = 1.9e-23f;
    float negCharge =-1.9e-23f;
    float smallest = Float.MIN_VALUE;
    float biggest  = Float.MAX_VALUE;
    float nan=Float.NaN;
    float posInfinite=Float.POSITIVE_INFINITY;
    float negInfinite=Float.NEGATIVE_INFINITY;
    //  float & double
    harness.checkPoint("type cast widening : float to, double...");
    d=(double)posCharge;
    harness.check(inRange(1.9,-23,d),"widening float to double "+d); // calculate a 'granularrity 0f +-0.0000001
    d=(double)negCharge;
    harness.check(inRange(-1.9,-23,d),"widening float to double "+d);
    d=(double)smallest;
    harness.check(inRange(1.4012984,-45,d),"widening float to double "+d);
    d=(double)biggest;
    harness.check(inRange(3.4028234,38,d),"widening float to double "+d);
    d=(double)nan;
    harness.check(Double.isNaN(d),"widening float to double "+d);
    d=(double)posInfinite;
    harness.check((d==Double.POSITIVE_INFINITY),"widening float to double");
    d=(double)negInfinite;
    harness.check((d==Double.NEGATIVE_INFINITY),"widening float to double");
  }
	
	private void testCastingNarrowing()
	{
	  //casting variables
	  byte   b;
	  short  s;
	  char   c;
	  int    i;
	  long   l;
	  float  f;
	  double d;
    //--------------------------------------------------------------------------------------------------------------------------------  	
    //   short, char, int, long, narrowed into byte
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("cast narowing : short, char, int, long, narrowed into byte");
    //byte==0 => 8 last bits(2hexes) of number must be +-0
    l=0x0fffffffffffff00L;
    i=0x0fffff00;
    c='\u0f00';
    s=0x0f00;
    b=(byte)l;
    harness.check((b==0),"long=>byte, byte=(+)0");
    b=(byte)i;
    harness.check((b==0),"int=>byte, byte=(+)0");
    b=(byte)c;
    harness.check((b==0),"char=>byte, byte=(+)0");
    b=(byte)s;
    harness.check((b==0),"short=>byte, byte=(+)0");
    //byte=0 => 8 last bits(2hexes) of number must be +-0
    l=-0x0fffffffffffff00L;
    i=-0x0fffff00;
    c='\uf000';
    s=-0x0f00;
    b=(byte)l;
    harness.check((b==0),"long=>byte, byte=(-)0");
    b=(byte)i;
    harness.check((b==0),"int=>byte, byte=(-)0");
    b=(byte)c;
     harness.check((b==0),"char=>byte, byte=(-)0");
    b=(byte)s;
    harness.check((b==0),"short=>byte, byte=(-)0");

    //byte>0 => 8 last bits(2hexes) of number must be <=7f or negative number must be >=80
    //(positive numbers)
    l=0x0fffffffffffff0fL;
    i=0x0fffff0f;
    c='\u0f0f';
    s=0x0f0f;
    b=(byte)l;
    harness.check((b==0x0f),"(+)long=>byte, byte = <"+b+"> instead +15");
    b=(byte)i;
    harness.check((b==0x0f),"(+)int=>byte, byte = <"+b+"> instead +15");
    b=(byte)c;
    harness.check((b==0x0f),"(+)char=>byte, byte = <"+b+"> instead +15");
    b=(byte)s;
    harness.check((b==0x0f),"(+)short=>byte, byte = <"+b+"> instead +15");
    //(negative numbers)
    l=-0x0ffffffffffffff1L;
    i=-0x0ffffff1;
    c='\u0f0f';
    s=-0x0ff1;
    b=(byte)l;
    harness.check((b==0x0f),"(-)long=>byte, byte <"+b+"> instead +15");
    b=(byte)i;
    harness.check((b==0x0f),"(-)int=>byte, byte="+b+"> instead +15");
    b=(byte)c;
    harness.check((b==0x0f),"(-)char=>byte, byte= <"+b+"> instead +15");
    b=(byte)s;
    harness.check((b==0x0f),"(-)short=>byte, byte= <"+b+"> instead +15");
    	
    //byte<0 => 8 last bits(2hexes) of number must be >=90 or negative number must be <=7f
    //(positive numbers)
    l=0x0ffffffffffffff1L;
    i=0x0ffffff1;
    c='\u0ff1';
    s=0x0ff1;
    b=(byte)l;
    harness.check((b==-0x0f),"(+)long=>byte, byte <"+b+"> instead -15");
    b=(byte)i;
    harness.check((b==-0x0f),"(+)int=>byte, byte <"+b+"> instead -15");
    b=(byte)c;
    harness.check((b==-0x0f),"(+)char=>byte, byte <"+b+"> instead -15");
    b=(byte)s;
    harness.check((b==-0x0f),"(+)short=>byte, byte <"+b+"> instead -15");
    //(negative numbers)
    l=-0x0fffffffffffff0fL;
    i=-0x0fffff0f;
    c='\uf0f1';
    s=-0x0f0f;
    b=(byte)l;
    harness.check((b==-0x0f),"(-)long=>byte, byte <"+b+"> instead -15");
    b=(byte)i;
    harness.check((b==-0x0f),"(-)int=>byte, byte= <"+b+"> instead -15");
    b=(byte)c;
    harness.check((b==-0x0f),"(-)char=>byte, byte= <"+b+"> instead -15");
    b=(byte)s;
    harness.check((b==-0x0f),"(-)short=>byte, byte= <"+b+"> instead -15");
      	
    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  char, int, long, narrowed into short
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("cast narowing :  char, int, long, narrowed into short");
    //char==0 => last 16 bits(4hexes) of number must be +-0
    l=0x0fffffffffff0000L;
    i=0x0fff0000;
    c='\u0000';
    s=(short)l;
    harness.check((s==0),"long=>short, short=(+)0");
    s=(short)i;
    harness.check((s==0),"int=>short, short=(+)0");
    s=(short)c;
    harness.check((s==0),"char=>short, short=(+)0");
    //byte=0 => 8 last bits(2hexes) of number must be +-0
    l=-0x0fffffffffff0000L;
    i=-0x0fff0000;
  //  c='\u0000';
    s=(short)l;
    harness.check((s==0),"long=>short, short=(-)0");
    s=(short)i;
    harness.check((s==0),"int=>short, short=(-)0");
  //  s=(short)c;
  //  harness.check((s==0),"char=>byte, byte=(-)0");

    //char="+c+" instead of 4095 => last 16 bits(4hexes) of number must be <=0x7fff (pos numbers) or>-0x8000 for negative numbers
    //(positive numbers)
    l=0x0ffffffffffff0fffL;
    i=0x0fff0fff;
    c='\u0fff';
    s=(short)l;
    harness.check((s==0x0fff),"(+)long=>short, short"+s+"instead 4095");
    s=(short)i;
    harness.check((s==0x0fff),"(+)int=>short, short="+s+" instead of 4095");
    s=(short)c;
    harness.check((s==0x0fff),"(+)char=>short, short="+s+" instead of 4095");
    //(negative numbers)
    l=-0x0ffffffffffff001L;
    i=-0x0ffff001;
  //  c='\u0fff';
    s=(short)l;
    harness.check((s==0x0fff),"(-)long=>short, short="+s+" instead of 4095");
    s=(short)i;
    harness.check((s==0x0fff),"(-)int=>short, short="+s+" instead of 4095");
  //  s=(short)c;
  //  harness.check((s==0x0fff),"(-)char=>short, byte>0");

    //char<0 => last 16 bits(4hexes) of number must be >0x8000 (pos numbers) or>-0x7fff for negative numbers
    //(positive numbers)
    l=0x0ffffffffffff001L;
    i=0x0ffff001;
    c='\uf001';
    s=(short)l;
    harness.check((s==-0x0fff),"(+)long=>short, short="+s+" instead of -4095");
    s=(short)i;
    harness.check((s==-0x0fff),"(+)int=>short, short="+s+" instead of -4095");
    s=(short)c;
    harness.check((s==-0x0fff),"(+)char=>short, short="+s+" instead of -4095");
    //(negative numbers)
    l=-0x0fffffffffff0fffL;
    i=-0x0fff0fff;
    //c='\uf001';
    s=(short)l;
    harness.check((s==-0x0fff),"(-)long=>short, short="+s+" instead of -4095");
    s=(short)i;
    harness.check((s==-0x0fff),"(-)int=>short, short="+s+" instead of -4095");
  //  s=(short)c;
  //  harness.check((s==-0x0fff),"(+)char=>short, short="+s+" instead of 4095");

    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  short, int, long, narrowed into char
    //--------------------------------------------------------------------------------------------------------------------------------  		
    harness.checkPoint("cast narowing :  short, int, long, narrowed into char");
    //char==0 => last 16 bits(4hexes) of number must be +-0
    l=0x0fffffffffff0000L;
    i=0x0fff0000;
    s=0x0000;
    c=(char)l;
    harness.check((c=='\u0000'),"long=>char, char=(+)0");
    c=(char)i;
    harness.check((c=='\u0000'),"int=>char, char=(+)0");
    c=(char)s;
    harness.check((c=='\u0000'),"short=>char, char=(+)0");
    //byte=0 => 8 last bits(2hexes) of number must be +-0
    l=-0x0fffffffffff0000L;
    i=-0x0fff0000;
  //  s=0x0000;
    c=(char)l;
    harness.check((c=='\u0000'),"long=>char, char=(-)0");
    c=(char)i;
    harness.check((c=='\u0000'),"int=>char, char=(-)0");
  //  s=short(c);
  //  harness.check((s=='\u0000'),"short=>char, char=(-)0");

    //char'>0' => when C is assigned a value between 0 and 0x7fff, the last 16 bits of the long, int, short must either be
    // in the range 0 to 0x7fff for positive numbers or -0x8000 to -0xffff for negative numbers
    //(positive numbers)
    l=0x0ffffffffffff0fffL;
    i=0x0fff0fff;
    s=0x0fff;
    c=(char)l;
    harness.check((c=='\u0fff'),"(+)long=>char, char="+c+" instead of 4095");
    c=(char)i;
    harness.check((c=='\u0fff'),"(+)int=>char, char="+c+" instead of 4095");
    c=(char)s;
    harness.check((c=='\u0fff'),"(+)short=>char, char="+c+" instead of 4095");
    //(negative numbers)
    l=-0x0ffffffffffff001L;
    i=-0x0ffff001;
  //  s='\u0fff';
    c=(char)l;
    harness.check((c=='\u0fff'),"(-)long=>char, char="+c+" instead of 4095");
    c=(char)i;
    harness.check((c=='\u0fff'),"(-)int=>char, char="+c+" instead of 4095");
  //  c=(char)s;
  //  harness.check((),"(-)short=>char, char="+c+" instead of 4095");

    //char'<0' => when C is assigned a value between 0x7fff and 0x800, the last 16 bits of the long, int, short must either be
    // in the range 0x7fff to 0xffff for positive numbers or 0 to -0x7fff for negative numbers
    //(positive numbers)
    l=0x0ffffffffffff000L;
    i=0x0ffff000;
    //s=0xf000; //out of range
    c=(char)l;
    harness.check((c=='\uf000'),"(+)long=>char, char="+c+" instead of 61440");
    c=(char)i;
    harness.check((c=='\uf000'),"(+)int=>char, char="+c+" instead of 61440");
  //  c=(char)s;
  //  harness.check((s==-0x0fff),"(+)short=>char, char="+c+" instead of 4095");
    //(negative numbers)
    l=-0x0fffffffffff0fffL;
    i=-0x0fff0fff;
    s=-0x0fff;
    s=(short)l;
    harness.check((c=='\uf000'),"(-)long=>char, char="+((int)c)+" instead of 61440");
    s=(short)i;
    harness.check((c=='\uf000'),"(-)int=>char, char="+((int)c)+" instead of 61440");
    s=(short)c;
    harness.check((c=='\uf000'),"(+)short=>char, char="+((int)c)+" instead of 61440");

    //--------------------------------------------------------------------------------------------------------------------------------  	
    //  long narrowed into int
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("cast narowing :  long narrowed into int");
    //int==0 => last 32 bits(8hexes) of number must be +-0
    //(positive)
    l=0x0fffffff00000000L;
    i=(int)l;
    harness.check((i==0),"(+)long=>int, int=0");
    l=-0x0fffffff00000000L;
    i=(int)l;
    harness.check((i==0),"(-)long=>int, int=0");

    //int>0 => last 32 bits(8hexes) of number must be <=0x7fffffff (pos numbers) or -0x800000000 to -0xffffffff for negative numbers
    //(positive numbers)
    l=0x0ffffffff0fffffffL;
    i=(int)l;
    harness.check((i==0x0fffffff),"(+)long=>int, int>0");
    //(negative numbers)
    l=-0x0ffffffff0000001L;
    i=(int)l;
    harness.check((i==0x0fffffff),"(+)long=>int, int>0");

    //int<0 => last 32 bits(8hexes) of number must be 0x80000000 to 0xffffffff (pos numbers) or >-0x7fffffff for negative numbers
    //(positive numbers)
    l=0x0fffffffff0000001L;
    i=(int)l;
    harness.check((i==-0x0fffffff),"(+)long=>int, int<0");
    //(negative numbers)
    l=-0x0fffffff0fffffffL;
    i=(int)l;
    harness.check((i==-0x0fffffff),"(-)long=>int, int<0");



    //--------------------------------------------------------------------------------------------------------------------------------  	
    //special case: 'narrowing' byte into char : regard the 8 bits of the byte in its crude form(complement for negative numbers)
    //and turn them into a char
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("cast narowing : narrowing' byte into char");
    //positive:
    b=0x0f;
    c=(char)b;	
    harness.check((c=='\u000f'),"(positive) byte into char");
    //negative:
    b=-0x0f;
    c=(char)b;	
    harness.check((c=='\ufff1'),"(negative) byte into char: got"+((int)c)+" instead of 241/-15");



    //--------------------------------------------------------------------------------------------------------------------------------  	
    // special case: 'narrowing' float and double into long
    //--------------------------------------------------------------------------------------------------------------------------------  	
    // zeros
    harness.checkPoint("'narrowing' float and double into long");
    f=0.0f;
    d=0.00d;
    l=(long)f;
    harness.check((l==0L),"zero float to long");
    l=(long)d;
    harness.check((l==0L),"zero double to long");

    // 'special' floating point values: infinite and NAN
    f=Float.POSITIVE_INFINITY;
    d=Double.POSITIVE_INFINITY;
    l=(long)f;
    harness.check((l==0x7fffffffffffffffL),"positive infinite float to long");
    l=(long)d;
    harness.check((l==0x7fffffffffffffffL),"positive infinite double to long");

    f=Float.NEGATIVE_INFINITY;
    d=Double.NEGATIVE_INFINITY;
    l=(long)f;
    harness.check((l==0x8000000000000000L),"negative infinite float to long");
    l=(long)d;
    harness.check((l==0x8000000000000000L),"negative infinite double to long");

    f=Float.NaN;
    d=Double.NaN;
    l=(long)f;
    harness.check((l==0L),"NaN float to long");
    l=(long)d;
    harness.check((l==0L),"NaN double to long");

    // out of range long values
    // max float= +-2^64 /2 ~= +-9.2e18
    f=3.0e30f; // < 3.4e38  but >9.2e18
    d=1.0e300; // < 1.79e308 but >9.2e18
    l=(long)f;
    harness.check((l==0x7fffffffffffffffL),"positive too large float to long");
    l=(long)d;
    harness.check((l==0x7fffffffffffffffL),"positive too large double to long");

    f=-3.0e30f; // < 3.4e38 but >9.2e18
    d=-1.0e300; // < 1.79e308 but >9.2e18
    l=(long)f;
    harness.check((l==0x8000000000000000L),"negative too large float to long");
    l=(long)d;
    harness.check((l==0x8000000000000000L),"negative too large double to long");


    // 'normal' case
    //(positive long)     long elevenFourTerra= 11400000000000L;
    //(negative long)     long minusSevenTerra= -7000000000000L;
    f=11.4e6f;
    d=11.40e12;
    l=(long)f;
    harness.check((l==11400000L),"positive in range float to long : "+l);
    l=(long)d;
    harness.check((l==11400000000000L),"positive in range double to long : "+l);

    f=-7.0e6f;
    d=-7.00e12;
    l=(long)f;
    harness.check((l==-7000000L),"negative in range float to long : "+l);
    l=(long)d;
    harness.check((l==-7000000000000L),"negative in range double to long : "+l);

    f=1.45f;
    d=1.045;
    l=(long)f;
    harness.check((l==1L),"positive round to zero float to long");
    l=(long)d;
    harness.check((l==1L),"positive round to zero double to long");

    f=-1.45f;
    d=-1.045;
    l=(long)f;
    harness.check((l==-1L),"negative round to zero float to long");
    l=(long)d;
    harness.check((l==-1L),"negative round to zero double to long");



    //--------------------------------------------------------------------------------------------------------------------------------  	
    // special case: 'narrowing' float and double into int
    // special case: 'narrowing' float and double into char, short and byte :
    // first narrow them into integers, and then apply the conversins described above
    //--------------------------------------------------------------------------------------------------------------------------------  	
    // zeros
    harness.checkPoint("'narrowing' float and double into int, char, short, byte");
    f=0.0f;
    d=0.00d;
    i=(int)f;
    harness.check((i==0),"zero float to int");
    c=(char)f;
    harness.check((c==0),"zero float to char");
    s=(short)f;
    harness.check((s==0),"zero float to short");
    b=(byte)f;
    harness.check((b==0),"zero float to byte");
    i=(int)d;
    harness.check((i==0),"zero double to int");
    c=(char)d;
    harness.check((c==0),"zero double to char");
    s=(short)d;
    harness.check((s==0),"zero double to short");
    b=(byte)d;
    harness.check((b==0),"zero double to byte");

    // 'special' floating point values: infinite and NAN
    f=Float.POSITIVE_INFINITY;
    d=Double.POSITIVE_INFINITY;
    i=(int)f;
    harness.check((i==0x7fffffff),"positive infinity float to int");
    c=(char)f;
    harness.check((c=='\uffff'),"positive infinity float to char");
    s=(short)f;
    harness.check((s==-1),"positive infinity float to short");
    b=(byte)f;
    harness.check((b==-1),"positive infinity float to byte");
    i=(int)d;
    harness.check((i==0x7fffffff),"positive infinity double to int");
    c=(char)d;
    harness.check((c=='\uffff'),"positive infinity double to char");
    s=(short)d;
    harness.check((s==-1),"positive infinity double to short");
    b=(byte)d;
    harness.check((b==-1),"positive infinity double to byte");

    f=Float.NEGATIVE_INFINITY;
    d=Double.NEGATIVE_INFINITY;
    i=(int)f;
    harness.check((i==0x80000000),"negative infinity float to int");
    c=(char)f;
    harness.check((c=='\u0000'),"negative infinity float to char");
    s=(short)f;
    harness.check((s==0),"negative infinity float to short");
    b=(byte)f;
    harness.check((b==0),"negative infinity float to byte");
    i=(int)d;
    harness.check((i==0x80000000),"negative infinity double to int");
    c=(char)d;
    harness.check((c=='\u0000'),"negative infinity double to char");
    s=(short)d;
    harness.check((s==0),"negative infinity double to short");
    b=(byte)d;
    harness.check((b==0),"negative infinity double to byte");

    f=Float.NaN;
    d=Double.NaN;
    i=(int)f;
    harness.check(i,0,"NaN float to int");
    c=(char)f;
    harness.check(c,0,"NaN float to char");
    s=(short)f;
    harness.check(s,0,"NaN float to short");
    b=(byte)f;
    harness.check(b,0,"NaN float to byte");
    i=(int)d;
    harness.check(i,0,"NaN double to int");
    c=(char)d;
    harness.check(c,0,"NaN double to char");
    s=(short)d;
    harness.check(s,0,"NaN double to short");
    b=(byte)d;
    harness.check(b,0,"NaN double to byte");

    // out of range long values
    // max int= +-2^32 /2 ~= +-0.2e9
    f=3.0e10f; // < 3.4e38  but >9.2e18
    d=1.0e100; // < 1.79e308 but >9.2e18
    i=(int)f;
    harness.check((i==0x7fffffff),"positive too large float to int");
    c=(char)f;
    harness.check((c=='\uffff'),"positive too large float to char");
    s=(short)f;
    harness.check((s==-1),"positive too large float to short");
    b=(byte)f;
    harness.check((b==-1),"positive too large float to byte");
    i=(int)d;
    harness.check((i==0x7fffffff),"positive too large double to int");
    c=(char)d;
    harness.check((c=='\uffff'),"positive too large double to char");
    s=(short)d;
    harness.check((s==-1),"positive too large double to short");
    b=(byte)d;
    harness.check((b==-1),"positive too large double to byte");

    f=-3.0e10f; // < 3.4e38 but >9.2e18
    d=-1.0e100; // < 1.79e308 but >9.2e18
    i=(int)f;
    harness.check((i==0x80000000),"negative too large float to int");
    c=(char)f;
    harness.check((c=='\u0000'),"negative too large float to char");
    s=(short)f;
    harness.check((s==0),"negative too large float to short");
    b=(byte)f;
    harness.check((b==0),"negative too large float to byte");
    i=(int)d;
    harness.check((i==0x80000000),"negative too large double to int");
    c=(char)d;
    harness.check((c=='\u0000'),"negative too large double to char");
    s=(short)d;
    harness.check((s==0),"negative too large double to short");
    b=(byte)d;
    harness.check((b==0),"negative too large double to byte");
    // 'normal' case
    // positive and negative byte, short, char rounding values
    //( 0x101D0   66000 => c=01D0, s= 01D0, c=-2F )
    //( 0x1f018  127000 => c=F018, s=-0FE7, b= 18 )
    //(-0x101D0  -66000 => c=FE2F, s=-01D0, c= 2F )
    //(-0x1f018 -127000 => c=0FE7, s= 0FE7, b=-18 )
    f=66e3f;  //   66000 => 0x101D0
    d=6.6e4;  // c=01D0, s= 01D0, c=-2F
    i=(int)f;
    harness.check((i==0x000101D0),"in-range rounding float to int");
    c=(char)f;
    harness.check((c=='\u01D0'),"in-range rounding float to char");
    s=(short)f;
    harness.check((s==0x01D0),"in-range rounding float to short");
    b=(byte)f;
    harness.check((b==-0x30),"in-range rounding float to byte");
    i=(int)d;
    harness.check((i==0x000101D0),"in-range rounding double to int");
    c=(char)d;
    harness.check((c=='\u01D0'),"in-range rounding double to char");
    s=(short)d;
    harness.check((s==0x01D0),"in-range rounding double to short");
    b=(byte)d;
    harness.check((b==-0x30),"in-range rounding double to byte");

    f=127e3f; // 127000 => 0x1F018
    d=1.27e5; // c=F018, s=-0FE7, b= 18
    i=(int)f;
    harness.check((i==0x0001F018),"in-range rounding float to int");
    c=(char)f;
    harness.check((c=='\uF018'),"in-range rounding float to char");
    s=(short)f;
    harness.check((s==-0x0FE8),"in-range rounding float to short");
    b=(byte)f;
    harness.check((b==0x18),"in-range rounding float to byte");
    i=(int)d;
    harness.check((i==0x0001F018),"in-range rounding double to int");
    c=(char)d;
    harness.check((c=='\uF018'),"in-range rounding double to char");
    s=(short)d;
    harness.check((s==-0x0FE8),"in-range rounding double to short");
    b=(byte)d;
    harness.check((b==0x18),"in-range rounding double to byte");

    f=-66e3f; //  -66000 =>-0x101D0
    d=-6.6e4; // c=FE2F, s=-01D0, c= 2F
    i=(int)f;
    harness.check((i==-0x000101D0),"in-range rounding float to int");
    c=(char)f;
    harness.check((c=='\uFE30'),"in-range rounding float to char");
    s=(short)f;
    harness.check((s==-0x01D0),"in-range rounding float to short");
    b=(byte)f;
    harness.check((b==0x30),"in-range rounding float to byte");
    i=(int)d;
    harness.check((i==-0x000101D0),"in-range rounding double to int");
    c=(char)d;
    harness.check((c=='\uFE30'),"in-range rounding double to char");
    s=(short)d;
    harness.check((s==-0x01D0),"in-range rounding double to short");
    b=(byte)d;
    harness.check((b==0x30),"in-range rounding double to byte");

    f=-127e3f;// -127000 => -0x1F018
    d=-1.27e5;// c=0FE7, s= 0FE7, b=-18
    i=(int)f;
    harness.check((i==-0x0001F018),"in-range rounding float to int");
    c=(char)f;
    harness.check((c=='\u0FE8'),"in-range rounding float to char");
    s=(short)f;
    harness.check((s==0x0FE8),"in-range rounding float to short");
    b=(byte)f;
    harness.check((b==-0x18),"in-range rounding float to byte");
    i=(int)d;
    harness.check((i==-0x0001F018),"in-range rounding double to int");
    c=(char)d;
    harness.check((c=='\u0FE8'),"in-range rounding double to char");
    s=(short)d;
    harness.check((s==0x0FE8),"in-range rounding double to short");
    b=(byte)d;
    harness.check((b==-0x18),"in-range rounding double to byte");

    // rounding down
    f=1.45f;
    d=1.045;
    i=(int)f;
    harness.check((i==1),"positive round to zero float to int");
    c=(char)f;
    harness.check((c=='\u0001'),"positive round to zero float to char");
    s=(short)f;
    harness.check((s==1),"positive round to zero float to short");
    b=(byte)f;
    harness.check((b==1),"positive round to zero float to byte");
    i=(int)d;
    harness.check((i==1),"positive round to zero double to int");
    c=(char)d;
    harness.check((c=='\u0001'),"positive round to zero double to char");
    s=(short)d;
    harness.check((s==1),"positive round to zero double to short");
    b=(byte)d;
    harness.check((b==1),"positive round to zero double to byte");

    f=-1.45f;
    d=-1.045;
    i=(int)f;
    harness.check((i==-1),"negative round to zero float to int");
    c=(char)f;
    harness.check((c=='\uffff'),"negative round to zero float to char");
    s=(short)f;
    harness.check((s==-1),"negative round to zero float to short");
    b=(byte)f;
    harness.check((b==-1),"negative round to zero float to byte");
    i=(int)d;
    harness.check((i==-1),"negative round to zero double to int");
    c=(char)d;
    harness.check((c=='\uffff'),"negative round to zero double to char");
    s=(short)d;
    harness.check((s==-1),"negative round to zero double to short");
    b=(byte)d;
    harness.check((b==-1),"negative round to zero double to byte");




    //--------------------------------------------------------------------------------------------------------------------------------  	
    //special case: 'narrowing' double to float: regard the special values and the maximum/minimum limits
    //--------------------------------------------------------------------------------------------------------------------------------  	
    harness.checkPoint("'narrowing' double to float");
    // zero
    d=0.0;
    f=(float)d;
    harness.check((f==0.0f),"zero double to float");
    d=-0.0;
    f=(float)d;
    harness.check((f==0.0f),"zero double to float");
    // special values, infinity and NaN
    d=Double.POSITIVE_INFINITY;
    f=(float)d;
    harness.check((f==Float.POSITIVE_INFINITY),"positive infinity double to float");
    d=Double.NEGATIVE_INFINITY;
    f=(float)d;
    harness.check((f==Float.NEGATIVE_INFINITY),"negative infinity double to float");
    d=Double.NaN;
    f=(float)d;
    harness.check(Float.isNaN(f),"NaN double to float : "+f);
     // outside of the minimum/Maximum value boundaries for floating points
    d=3.4e40;
    f=(float)d;
    harness.check((f==Float.POSITIVE_INFINITY),"positive too large double to float");
    d=-3.4e40;
    f=(float)d;
    harness.check((f==Float.NEGATIVE_INFINITY),"negative too large double to float");
    d=1.4e-50;
    f=(float)d;
    harness.check((f==0.0f),"positive too small double to float : "+f);
    d=-1.4e-50;
    f=(float)d;
    harness.check((f==0.0f),"negative too small double to float : "+f);
     // normal case
    d=1.9e23;
    f=(float)d;
    harness.check((f==1.9e23f),"normal case double to float");
    d=-1.9e23;
    f=(float)d;
    harness.check((f==-1.9e23f),"normal case double to float");

	}
	
	
	private void testStringConversion()
	{
    /*  when adding a primitive to a string, the primitive is converted to a String following this algorithms:
    =>string + boolean  = String + Boolean.toString(boolean) idem for boolean+String
    =>String + byte     = String + Integer.toString((integer)byte)
    =>String + char     = String + Character.toString(char)
    =>String + short    = String + Integer.toString((integer)short)
    =>String + int      = String + Integer.toString(int)
    =>String + long     = String + Long.toString(long)
    =>String + float    = String + Float.toString(float)
    =>String + double   = String + Double.toString(double)
    with
    =>String + reference = string +"null" if primitive = null, String+ class.toString() otherwise

    */
    String stringvalue = "StringValue:";
    String asstring = "(as String)";
    //boolean
    boolean ztrue=true;
    boolean zfalse=false;
    harness.checkPoint("boolean to string");
    harness.check(stringvalue+ztrue,stringvalue+new Boolean(ztrue) );
    harness.check(stringvalue+ztrue,stringvalue+(new Boolean(ztrue)).toString() );
    harness.check(ztrue+asstring,new Boolean(ztrue)+asstring );
    harness.check(ztrue+asstring,(new Boolean(ztrue)).toString() +asstring );
    harness.check(stringvalue+zfalse,stringvalue+new Boolean(zfalse) );
    harness.check(stringvalue+zfalse,stringvalue+(new Boolean(zfalse)).toString() );
    harness.check(zfalse+asstring,new Boolean(zfalse)+asstring );
    harness.check(zfalse+asstring,(new Boolean(zfalse)).toString() +asstring );

    harness.checkPoint("byte to string");
    byte bnul=(byte)0;
    byte bpos=(byte) 0x01;
    byte bneg=(byte)-0x01;
    byte bmax=(byte) 0x7f;
    byte bmin=(byte)-0x80;
    harness.check(stringvalue+bnul,stringvalue+new Integer((int)bnul) );
    harness.check(stringvalue+bnul,stringvalue+Integer.toString((int)bnul) );
    harness.check(bnul+asstring,new Integer((int)bnul)+asstring );
    harness.check(bnul+asstring,Integer.toString((int)bnul)+asstring );

    harness.check(stringvalue+bpos,stringvalue+new Integer((int)bpos) );
    harness.check(stringvalue+bpos,stringvalue+Integer.toString((int)bpos) );
    harness.check(bpos+asstring,new Integer((int)bpos)+asstring );
    harness.check(bpos+asstring,Integer.toString((int)bpos)+asstring );

    harness.check(stringvalue+bneg,stringvalue+new Integer((int)bneg) );
    harness.check(stringvalue+bneg,stringvalue+Integer.toString((int)bneg) );
    harness.check(bneg+asstring,new Integer((int)bneg)+asstring );
    harness.check(bneg+asstring,Integer.toString((int)bneg)+asstring );

    harness.check(stringvalue+bmax,stringvalue+new Integer((int)bmax) );
    harness.check(stringvalue+bmax,stringvalue+Integer.toString((int)bmax) );
    harness.check(bmax+asstring,new Integer((int)bmax)+asstring );
    harness.check(bmax+asstring,Integer.toString((int)bmax)+asstring );

    harness.check(stringvalue+bmin,stringvalue+new Integer((int)bmin) );
    harness.check(stringvalue+bmin,stringvalue+Integer.toString((int)bmin) );
    harness.check(bmin+asstring,new Integer((int)bmin)+asstring );
    harness.check(bmin+asstring,Integer.toString((int)bmin)+asstring );

    harness.checkPoint("char to string");
    char cpos=(char) 0x0080;
    char cneg=(char) 0x8000;
    char cmax=(char) 0xffff;
    char cmin=(char)-0x000;
    harness.check(stringvalue+cpos,stringvalue+new Character(cpos) );
    harness.check(stringvalue+cpos,stringvalue+(new Character(cpos)).toString() );
    harness.check(cpos+asstring,new Character(cpos)+asstring );
    harness.check(cpos+asstring,(new Character(cpos)).toString()+asstring );

    harness.check(stringvalue+cneg,stringvalue+new Character(cneg) );
    harness.check(stringvalue+cneg,stringvalue+(new Character(cneg)).toString() );
    harness.check(cneg+asstring,new Character(cneg)+asstring );
    harness.check(cneg+asstring,(new Character(cneg)).toString()+asstring );

    harness.check(stringvalue+cmax,stringvalue+new Character(cmax) );
    harness.check(stringvalue+cmax,stringvalue+(new Character(cmax)).toString() );
    harness.check(cmax+asstring,new Character(cmax)+asstring );
    harness.check(cmax+asstring,(new Character(cmax)).toString()+asstring );

    harness.check(stringvalue+cmin,stringvalue+new Character(cmin) );
    harness.check(stringvalue+cmin,stringvalue+(new Character(cmin)).toString() );
    harness.check(cmin+asstring,new Character(cmin)+asstring );
    harness.check(cmin+asstring,(new Character(cmin)).toString()+asstring );

    harness.checkPoint("short to string");
    short snul=(short) 0x0000;
    short spos=(short) 0x0080;
    short sneg=(short)-0x0081;
    short smax=(short) 0x7fff;
    short smin=(short)-0x8000;
    harness.check(stringvalue+snul,stringvalue+new Integer((int)snul) );
    harness.check(stringvalue+snul,stringvalue+Integer.toString((int)snul) );
    harness.check(snul+asstring,new Integer((int)snul)+asstring );
    harness.check(snul+asstring,Integer.toString((int)snul)+asstring );

    harness.check(stringvalue+spos,stringvalue+new Integer((int)spos) );
    harness.check(stringvalue+spos,stringvalue+Integer.toString((int)spos) );
    harness.check(spos+asstring,new Integer((int)spos)+asstring );
    harness.check(spos+asstring,Integer.toString((int)spos)+asstring );

    harness.check(stringvalue+sneg,stringvalue+new Integer((int)sneg) );
    harness.check(stringvalue+sneg,stringvalue+Integer.toString((int)sneg) );
    harness.check(sneg+asstring,new Integer((int)sneg)+asstring );
    harness.check(sneg+asstring,Integer.toString((int)sneg)+asstring );

    harness.check(stringvalue+smax,stringvalue+new Integer((int)smax) );
    harness.check(stringvalue+smax,stringvalue+Integer.toString((int)smax) );
    harness.check(smax+asstring,new Integer((int)smax)+asstring );
    harness.check(smax+asstring,Integer.toString((int)smax)+asstring );

    harness.check(stringvalue+smin,stringvalue+new Integer((int)smin) );
    harness.check(stringvalue+smin,stringvalue+Integer.toString((int)smin) );
    harness.check(smin+asstring,new Integer((int)smin)+asstring );
    harness.check(smin+asstring,Integer.toString((int)smin)+asstring );

    harness.checkPoint("int to string");
    int inul= 0x00000000;
    int ipos= 0x00008000;
    int ineg=-0x00008001;
    int imax= 0x7fffffff;
    int imin=-0x80000000;
    harness.check(stringvalue+inul,stringvalue+new Integer(inul) );
    harness.check(stringvalue+inul,stringvalue+Integer.toString(inul) );
    harness.check(inul+asstring,new Integer(inul)+asstring );
    harness.check(inul+asstring,Integer.toString(inul)+asstring );

    harness.check(stringvalue+ipos,stringvalue+new Integer(ipos) );
    harness.check(stringvalue+ipos,stringvalue+Integer.toString(ipos) );
    harness.check(ipos+asstring,new Integer(ipos)+asstring );
    harness.check(ipos+asstring,Integer.toString(ipos)+asstring );

    harness.check(stringvalue+ineg,stringvalue+new Integer(ineg) );
    harness.check(stringvalue+ineg,stringvalue+Integer.toString(ineg) );
    harness.check(ineg+asstring,new Integer(ineg)+asstring );
    harness.check(ineg+asstring,Integer.toString(ineg)+asstring );

    harness.check(stringvalue+imax,stringvalue+new Integer(imax) );
    harness.check(stringvalue+imax,stringvalue+Integer.toString(imax) );
    harness.check(imax+asstring,new Integer(imax)+asstring );
    harness.check(imax+asstring,Integer.toString(imax)+asstring );

    harness.check(stringvalue+imin,stringvalue+new Integer(imin) );
    harness.check(stringvalue+imin,stringvalue+Integer.toString(imin) );
    harness.check(imin+asstring,new Integer(imin)+asstring );
    harness.check(imin+asstring,Integer.toString(imin)+asstring );

    harness.checkPoint("long to string");
    long lnul= 0x0000000000000000L;
    long lpos= 0x0000800000000000L;
    long lneg=-0x0000800000000001L;
    long lmax= 0x7fffffffffffffffL;
    long lmin=-0x8000000000000000L;
    harness.check(stringvalue+lnul,stringvalue+new Long(lnul) );
    harness.check(stringvalue+lnul,stringvalue+Long.toString(lnul) );
    harness.check(lnul+asstring,new Long(lnul)+asstring );
    harness.check(lnul+asstring,Long.toString(lnul)+asstring );

    harness.check(stringvalue+lpos,stringvalue+new Long(lpos) );
    harness.check(stringvalue+lpos,stringvalue+Long.toString(lpos) );
    harness.check(lpos+asstring,new Long(lpos)+asstring );
    harness.check(lpos+asstring,Long.toString(lpos)+asstring );

    harness.check(stringvalue+lneg,stringvalue+new Long(lneg) );
    harness.check(stringvalue+lneg,stringvalue+Long.toString(lneg) );
    harness.check(lneg+asstring,new Long(lneg)+asstring );
    harness.check(lneg+asstring,Long.toString(lneg)+asstring );

    harness.check(stringvalue+lmax,stringvalue+new Long(lmax) );
    harness.check(stringvalue+lmax,stringvalue+Long.toString(lmax) );
    harness.check(lmax+asstring,new Long(lmax)+asstring );
    harness.check(lmax+asstring,Long.toString(lmax)+asstring );

    harness.check(stringvalue+lmin,stringvalue+new Long(lmin) );
    harness.check(stringvalue+lmin,stringvalue+Long.toString(lmin) );
    harness.check(lmin+asstring,new Long(lmin)+asstring );
    harness.check(lmin+asstring,Long.toString(lmin)+asstring );

    harness.checkPoint("float to string");
    float fnul= 0.00f;
    float fpos= 0.01f;
    float fneg=-0.01f;
    float fmax=Float.MAX_VALUE;
    float fmin=Float.MIN_VALUE;
    float fpin=Float.POSITIVE_INFINITY;
    float fnin=Float.NEGATIVE_INFINITY;
    float fnan=Float.NaN;
    harness.check(stringvalue+fnul,stringvalue+new Float(fnul) );
    harness.check(stringvalue+fnul,stringvalue+Float.toString(fnul) );
    harness.check(fnul+asstring,new Float(fnul)+asstring );
    harness.check(fnul+asstring,Float.toString(fnul)+asstring );

    harness.check(stringvalue+fpos,stringvalue+new Float(fpos) );
    harness.check(stringvalue+fpos,stringvalue+Float.toString(fpos) );
    harness.check(fpos+asstring,new Float(fpos)+asstring );
    harness.check(fpos+asstring,Float.toString(fpos)+asstring );

    harness.check(stringvalue+fneg,stringvalue+new Float(fneg) );
    harness.check(stringvalue+fneg,stringvalue+Float.toString(fneg) );
    harness.check(fneg+asstring,new Float(fneg)+asstring );
    harness.check(fneg+asstring,Float.toString(fneg)+asstring );

    harness.check(stringvalue+fmax,stringvalue+Float.toString(fmax) );
    harness.check(fmax+asstring,Float.toString(fmax)+asstring );

    harness.check(stringvalue+fmin,stringvalue+Float.toString(fmin) );
    harness.check(fmin+asstring,Float.toString(fmin)+asstring );

    harness.check(stringvalue+fpin,stringvalue+Float.toString(fpin) );
    harness.check(fpin+asstring,Float.toString(fpin)+asstring );

    harness.check(stringvalue+fnin,stringvalue+Float.toString(fnin) );
    harness.check(fnin+asstring,Float.toString(fnin)+asstring );

    harness.check(stringvalue+fnan,stringvalue+Float.toString(fnan) );
    harness.check(fnan+asstring,Float.toString(fnan)+asstring );

    harness.checkPoint("double to string");
    double dnul= 0.0000f;
    double dpos= 0.0001f;
    double dneg=-0.0001f;
    double dmax=Double.MAX_VALUE;
    double dmin=Double.MIN_VALUE;
    double dpin=Double.POSITIVE_INFINITY;
    double dnin=Double.NEGATIVE_INFINITY;
    double dnan=Double.NaN;
    harness.check(stringvalue+dnul,stringvalue+new Double(dnul) );
    harness.check(stringvalue+dnul,stringvalue+Double.toString(dnul) );
    harness.check(dnul+asstring,new Double(dnul)+asstring );
    harness.check(dnul+asstring,Double.toString(dnul)+asstring );

    harness.check(stringvalue+dpos,stringvalue+new Double(dpos) );
    harness.check(stringvalue+dpos,stringvalue+Double.toString(dpos) );
    harness.check(dpos+asstring,new Double(dpos)+asstring );
    harness.check(dpos+asstring,Double.toString(dpos)+asstring );

    harness.check(stringvalue+dneg,stringvalue+new Double(dneg) );
    harness.check(stringvalue+dneg,stringvalue+Double.toString(dneg) );
    harness.check(dneg+asstring,new Double(dneg)+asstring );
    harness.check(dneg+asstring,Double.toString(dneg)+asstring );

    harness.check(stringvalue+dmax,stringvalue+Double.toString(dmax) );
    harness.check(dmax+asstring,Double.toString(dmax)+asstring );

    harness.check(stringvalue+dmin,stringvalue+Double.toString(dmin) );
    harness.check(dmin+asstring,Double.toString(dmin)+asstring );

    harness.check(stringvalue+dpin,stringvalue+Double.toString(dpin) );
    harness.check(dpin+asstring,Double.toString(dpin)+asstring );

    harness.check(stringvalue+dnin,stringvalue+Double.toString(dnin) );
    harness.check(dnin+asstring,Double.toString(dnin)+asstring );

    harness.check(stringvalue+dnan,stringvalue+Double.toString(dnan) );
    harness.check(dnan+asstring,Double.toString(dnan)+asstring );
  }
  	
	

  // type detecting functions
	private char getType(boolean z) {return 'Z';}
	private char getType(byte b)    {return 'B';}
	private char getType(short s)   {return 'S';}
	private char getType(char c)    {return 'C';}
	private char getType(int i)     {return 'I';}
	private char getType(long l)    {return 'L';}
	private char getType(float f)   {return 'F';}
	private char getType(double d)  {return 'D';}
	
	private void testPromotionUnary()
	{
	//byte, short or char promoted to int
	byte  b3=3;
	short s3=3;
	char  c3='\u0003';
	int   i3=3;
	byte  bmin3=-3;
	short smin3=-3;
	char  cmin3='\ufffb';
	int   imin3=-3;
	byte  b8=8;
	short s8=8;
	char  c8=8;
	int   i8=8;
	
	int[] testarray1=new int[b8];
	int[] testarray2=new int[s8];
	int[] testarray3=new int[c8];
	int[] testarray0=new int[i8];
	for (int i=0; i<8; i++)
	{
  	testarray0[i]=i;
  	testarray1[i]=i;
  	testarray2[i]=i;
  	testarray3[i]=i;
	}
	//array dimension
	harness.checkPoint("Unary arrray promotion");
  harness.check(testarray1.length==testarray0.length);
  harness.check(testarray2.length==testarray0.length);
  harness.check(testarray3.length==testarray0.length);
  //aray access
  harness.check(testarray0[b3]==testarray0[i3]);
  harness.check(testarray0[s3]==testarray0[i3]);
  harness.check(testarray0[c3]==testarray0[i3]);
  harness.check(testarray1[b3]==testarray0[i3]);
  harness.check(testarray2[s3]==testarray0[i3]);
  harness.check(testarray3[c3]==testarray0[i3]);

	long   l3=3L;
	float  f3=3.0f;
	double d3=3.00;
	long   lmin3=-3L;
	float  fmin3=-3.0f;
	double dmin3=-3.00;	
	//unary +
	harness.checkPoint("Unary +,-,~ promotion");
	harness.check(getType(+b3)=='I',"unary + pos b");
	harness.check(getType(+s3)=='I',"unary + pos s");
	harness.check(getType(+c3)=='I',"unary + pos c");
	harness.check(getType(+i3)=='I',"unary + pos i");
	harness.check(getType(+l3)=='L',"unary + pos l");
	harness.check(getType(+f3)=='F',"unary + pos f");
	harness.check(getType(+d3)=='D',"unary + pos d");
	
	harness.check(getType(+bmin3)=='I',"unary + neg b");
	harness.check(getType(+smin3)=='I',"unary + neg s");
	harness.check(getType(+cmin3)=='I',"unary + neg c");
	harness.check(getType(+imin3)=='I',"unary + neg i");
	harness.check(getType(+lmin3)=='L',"unary + neg l");
	harness.check(getType(+fmin3)=='F',"unary + neg f");
	harness.check(getType(+dmin3)=='D',"unary + neg d");
	
	//unary -
	harness.check(getType(-b3)=='I',"unary + pos b");
	harness.check(getType(-s3)=='I',"unary + pos s");
	harness.check(getType(-c3)=='I',"unary + pos c");
	harness.check(getType(-i3)=='I',"unary + pos i");
	harness.check(getType(-l3)=='L',"unary + pos l");
	harness.check(getType(-f3)=='F',"unary + pos f");
	harness.check(getType(-d3)=='D',"unary + pos d");
	
	harness.check(getType(-bmin3)=='I',"unary + neg b");
	harness.check(getType(-smin3)=='I',"unary + neg s");
	harness.check(getType(-cmin3)=='I',"unary + neg c");
	harness.check(getType(-imin3)=='I',"unary + neg i");
	harness.check(getType(-lmin3)=='L',"unary + neg l");
	harness.check(getType(-fmin3)=='F',"unary + neg f");
	harness.check(getType(-dmin3)=='D',"unary + neg d");
		
	//unary ~
	harness.check(getType(~b3)=='I',"unary ~ pos b");
	harness.check(getType(~s3)=='I',"unary ~ pos s");
	harness.check(getType(~c3)=='I',"unary ~ pos c");
	harness.check(getType(~i3)=='I',"unary ~ pos i");
	harness.check(getType(~l3)=='L',"unary ~ pos l");
//	harness.check(getType(~f3)=='F',"result should be float"); //no bitwise complement on floating point notations
//	harness.check(getType(~d3)=='D',"result should be double");
	
	harness.check(getType(~bmin3)=='I',"unary ~ neg b");
	harness.check(getType(~smin3)=='I',"unary ~ neg s");
	harness.check(getType(~cmin3)=='I',"unary ~ neg c");
	harness.check(getType(~imin3)=='I',"unary ~ neg i");
	harness.check(getType(~lmin3)=='L',"unary ~ neg l");
//	harness.check(getType(~fmin3)=='F',"result should be float");
//	harness.check(getType(~dmin3)=='D',"result should be double");
	
	// base of bitwise >>, <<, >>>
	harness.checkPoint("Unary >>, <<, >>> promotion on base");
	harness.check(getType(b8>>3) =='I', "unary >> (pos operator)");
	harness.check(getType(b8>>3L)=='I', "unary >> (pos operator)");
	harness.check(getType(s8>>3) =='I', "unary >> (pos operator)");
	harness.check(getType(s8>>3L)=='I', "unary >> (pos operator)");
	harness.check(getType(c8>>3) =='I', "unary >> (pos operator)");
	harness.check(getType(c8>>3L)=='I', "unary >> (pos operator)");
	harness.check(getType(i8>>3) =='I', "unary >> (pos operator)");
	harness.check(getType(i8>>3L)=='I', "unary >> (pos operator)");
	
	harness.check(getType(b8>>-3) =='I', "unary >> (neg operator)");
	harness.check(getType(b8>>-3L)=='I', "unary >> (neg operator)");
	harness.check(getType(s8>>-3) =='I', "unary >> (neg operator)");
	harness.check(getType(s8>>-3L)=='I', "unary >> (neg operator)");
	harness.check(getType(c8>>-3) =='I', "unary >> (neg operator)");
	harness.check(getType(c8>>-3L)=='I', "unary >> (neg operator)");
	harness.check(getType(i8>>-3) =='I', "unary >> (neg operator)");
	harness.check(getType(i8>>-3L)=='I', "unary >> (neg operator)");
	
	harness.check(getType(b8<<3) =='I', "unary <<");
	harness.check(getType(b8<<3L)=='I', "unary <<");
	harness.check(getType(s8<<3) =='I', "unary <<");
	harness.check(getType(s8<<3L)=='I', "unary <<");
	harness.check(getType(c8<<3) =='I', "unary <<");
	harness.check(getType(c8<<3L)=='I', "unary <<");
	harness.check(getType(i8<<3) =='I', "unary <<");
	harness.check(getType(i8<<3L)=='I', "unary <<");
	
	harness.check(getType(b8>>>3) =='I', "unary >>>");
	harness.check(getType(b8>>>3L)=='I', "unary >>>");
	harness.check(getType(s8>>>3) =='I', "unary >>>");
	harness.check(getType(s8>>>3L)=='I', "unary >>>");
	harness.check(getType(c8>>>3) =='I', "unary >>>");
	harness.check(getType(c8>>>3L)=='I', "unary >>>");
	harness.check(getType(i8>>>3) =='I', "unary >>>");
	harness.check(getType(i8>>>3L)=='I', "unary >>>");
	
	// operand of bitwise >>, <<, >>>
	harness.checkPoint("Unary >>, <<, >>> promotion on operand");
	harness.check(( 25>>b3) == ( 25>>i3), "unary >> (pos operand)");
	harness.check((25L>>b3) == (25L>>i3), "unary >> (pos operand)");
	harness.check(( 25>>s3) == ( 25>>i3), "unary >> (pos operand)");
	harness.check((25L>>s3) == (25L>>i3), "unary >> (pos operand)");
	harness.check(( 25>>c3) == ( 25>>i3), "unary >> (pos operand)");
	harness.check((25L>>c3) == (25L>>i3), "unary >> (pos operand)");
	
	harness.check(( 25>>bmin3) == ( 25>>imin3), "unary >> (neg operand)");
	harness.check((25L>>bmin3) == (25L>>imin3), "unary >> (neg operand)");
	harness.check(( 25>>smin3) == ( 25>>imin3), "unary >> (neg operand)");
	harness.check((25L>>smin3) == (25L>>imin3), "unary >> (neg operand)");	
	harness.check(( 25>>cmin3) == ( 25>>imin3), "unary >> (neg operand)");
	harness.check((25L>>cmin3) == (25L>>imin3), "unary >> (neg operand)");
	
	harness.check(( 25<<b3) == ( 25<<i3), "unary <<");
	harness.check((25L<<b3) == (25L<<i3), "unary <<");
	harness.check(( 25<<s3) == ( 25<<i3), "unary <<");
	harness.check((25L<<s3) == (25L<<i3), "unary <<");
	harness.check(( 25<<c3) == ( 25<<i3), "unary <<");
	harness.check((25L<<c3) == (25L<<i3), "unary <<");
	
	harness.check(( 25>>>b3) == ( 25>>>i3), "unary >>>");
	harness.check((25L>>>b3) == (25L>>>i3), "unary >>>");
	harness.check(( 25>>>s3) == ( 25>>>i3), "unary >>>");
	harness.check((25L>>>s3) == (25L>>>i3), "unary >>>");
	harness.check(( 25>>>c3) == ( 25>>>i3), "unary >>>");
	harness.check((25L>>>c3) == (25L>>>i3), "unary >>>");
	}
	
	private void testPromotionBinary()
	{
	  //one operand is double: other=double,result=double;
	  //else:
	  //one operand is float: other=float,result=float;
	  //else:
	  //one operand is long: other=long,result=long;
	  //else:
	  //all operands widened to integer,result=integer;
	  // + and -
	  harness.checkPoint("Binary pronotion on <+> operand");
	  testPromotionAddition();
	  harness.checkPoint("Binary pronotion on <-> operand");
	  testPromotionSubtraction();
	  // *, / and %
	  harness.checkPoint("Binary pronotion on <*> operand");
	  testPromotionMultiplication();
	  harness.checkPoint("Binary pronotion on </> operand");
	  testPromotionDivision();
	  harness.checkPoint("Binary pronotion on <%> operand");
	  testPromotionRemainder();
	  // &, ^ and |
	  harness.checkPoint("Binary pronotion on bitwise <&> operand");
	  testPromotionBitwiseAnd();
	  harness.checkPoint("Binary pronotion on bitwise <|> operand");
	  testPromotionBitwiseOr();
	  harness.checkPoint("Binary pronotion on bitwise <^> operand");
	  testPromotionBitwiseXor();
	  // comparison,< <= == != >= >
	  harness.checkPoint("Binary pronotion smaller then (<) comparison");
	  testPromotionCompareSmaller();
	  harness.checkPoint("Binary pronotion smaller-equals (<=) comparison");
	  testPromotionCompareSmallerEquals();
	  harness.checkPoint("Binary pronotion equality (==) comparison");
	  testPromotionCompareEqual();
	  harness.checkPoint("Binary pronotion not-equal (!=) comparison");
	  testPromotionCompareNotEqual();
	  harness.checkPoint("Binary pronotion bigger then (>) comparison");
	  testPromotionCompareBigger();
	  harness.checkPoint("Binary pronotion bigger-equals (>=) comparison");
	  testPromotionCompareBiggerEquals();
	
	  // special case: ?-operator
	  harness.checkPoint("Binary pronotion conditional operator (b)?x1:x2");
	  testPromotionConditionalOperator();
	}
	
	private void testPromotionAddition()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // + and -
	  // to double:
	  harness.check((b8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+b3)==(d8+d3),"to double, equals");
	  harness.check(getType(b8+d3)=='D',"check type==double");
	  harness.check(getType(d8+b3)=='D',"result should be double");
	  harness.check((s8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+s3)==(d8+d3),"to double, equals");
	  harness.check(getType(s8+d3)=='D',"result should be double");
	  harness.check(getType(d8+s3)=='D',"result should be double");
	  harness.check((c8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+c3)==(d8+d3),"to double, equals");
	  harness.check(getType(c8+d3)=='D',"result should be double");
	  harness.check(getType(d8+c3)=='D',"result should be double");
	  harness.check((i8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+i3)==(d8+d3),"to double, equals");
	  harness.check(getType(i8+d3)=='D',"result should be double");
	  harness.check(getType(d8+i3)=='D',"result should be double");
	  harness.check((l8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+l3)==(d8+d3),"to double, equals");
	  harness.check(getType(l8+d3)=='D',"result should be double");
	  harness.check(getType(d8+l3)=='D',"result should be double");
	  harness.check((f8+d3)==(d8+d3),"to double, equals");
	  harness.check((d8+f3)==(d8+d3),"to double, equals");
	  harness.check(getType(f8+d3)=='D',"result should be double");
	  harness.check(getType(d8+f3)=='D',"result should be double");//24
	  // to float:
	  harness.check((b8+f3)==(f8+f3),"to float equals");
	  harness.check((f8+b3)==(f8+f3),"to float equals");
	  harness.check(getType(b8+f3)=='F',"result should be float");
	  harness.check(getType(f8+b3)=='F',"result should be float");
	  harness.check((s8+f3)==(f8+f3),"to float equals");
	  harness.check((f8+s3)==(f8+f3),"to float equals");  //30
	  harness.check(getType(s8+f3)=='F',"result should be float");
	  harness.check(getType(f8+s3)=='F',"result should be float");
	  harness.check((c8+f3)==(f8+f3),"to float equals");
	  harness.check((f8+c3)==(f8+f3),"to float equals");
	  harness.check(getType(c8+f3)=='F',"result should be float");
	  harness.check(getType(f8+c3)=='F',"result should be float");
	  harness.check((i8+f3)==(f8+f3),"to float equals");
	  harness.check((f8+i3)==(f8+f3),"to float equals");
	  harness.check(getType(i8+f3)=='F',"result should be float");
	  harness.check(getType(f8+i3)=='F',"result should be float");//40
	  harness.check((l8+f3)==(f8+f3),"to float equals");
	  harness.check((f8+l3)==(f8+f3),"to float equals");
	  harness.check(getType(l8+f3)=='F',"result should be float");
	  harness.check(getType(f8+l3)=='F',"result should be float");
	  // to long:
	  harness.check((b8+l3)==(l8+l3),"to long, equals");
	  harness.check((l8+b3)==(l8+l3),"to long, equals");
	  harness.check(getType(b8+l3)=='L',"result should be long");
	  harness.check(getType(l8+b3)=='L',"result should be long");
	  harness.check((s8+l3)==(l8+l3),"to long, equals");
	  harness.check((l8+s3)==(l8+l3),"to long, equals");
	  harness.check(getType(s8+l3)=='L',"result should be long");
	  harness.check(getType(l8+s3)=='L',"result should be long");
	  harness.check((c8+l3)==(l8+l3),"to long, equals");
	  harness.check((l8+c3)==(l8+l3),"to long, equals");
	  harness.check(getType(c8+l3)=='L',"result should be long");
	  harness.check(getType(l8+c3)=='L',"result should be long");
	  harness.check((i8+l3)==(l8+l3),"to long, equals");
	  harness.check((l8+i3)==(l8+l3),"to long, equals");
	  harness.check(getType(i8+l3)=='L',"result should be long");
	  harness.check(getType(l8+i3)=='L',"result should be long");
	  // type+int to int:
	  harness.check((b8+i3)==(i8+i3),"to int, equals");
	  harness.check((i8+b3)==(i8+i3),"to int, equals");
	  harness.check(getType(b8+i3)=='I',"result should be int");
	  harness.check(getType(i8+b3)=='I',"result should be int");
	  harness.check((s8+i3)==(i8+i3),"to int, equals");
	  harness.check((i8+s3)==(i8+i3),"to int, equals");
	  harness.check(getType(s8+i3)=='I',"result should be int");
	  harness.check(getType(i8+s3)=='I',"result should be int");
	  harness.check((c8+i3)==(i8+i3),"to int, equals");
	  harness.check((i8+c3)==(i8+i3),"to int, equals");
	  harness.check(getType(c8+i3)=='I',"result should be int");
	  harness.check(getType(i8+c3)=='I',"result should be int");
	  // type+char to int:
	  harness.check((b8+c3)==(i8+i3),"to int, equals");
	  harness.check((c8+b3)==(i8+i3),"to int, equals");
	  harness.check(getType(b8+c3)=='I',"result should be int");
	  harness.check(getType(c8+b3)=='I',"result should be int");
	  harness.check((s8+c3)==(i8+i3),"to int, equals");
	  harness.check((c8+s3)==(i8+i3),"to int, equals");
	  harness.check(getType(b8+c3)=='I',"result should be int");
	  harness.check(getType(c8+b3)=='I',"result should be int");
	  // type+short to int:
	  harness.check((b8+s3)==(i8+i3),"char to int, equals");
	  harness.check((s8+b3)==(i8+i3),"char to int, equals");
	  harness.check(getType(b8+s3)=='I',"result should be int");
	  harness.check(getType(s8+b3)=='I',"result should be int");
	}	

	 private void testPromotionSubtraction()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-b3)==(d8-d3),"to double, equals");
	  harness.check(getType(b8-d3)=='D',"result should be double");
	  harness.check(getType(d8-b3)=='D',"result should be double");
	  harness.check((s8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-s3)==(d8-d3),"to double, equals");
	  harness.check(getType(s8-d3)=='D',"result should be double");
	  harness.check(getType(d8-s3)=='D',"result should be double");
	  harness.check((c8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-c3)==(d8-d3),"to double, equals");
	  harness.check(getType(c8-d3)=='D',"result should be double");
	  harness.check(getType(d8-c3)=='D',"result should be double");
	  harness.check((i8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-i3)==(d8-d3),"to double, equals");
	  harness.check(getType(i8-d3)=='D',"result should be double");
	  harness.check(getType(d8-i3)=='D',"result should be double");
	  harness.check((l8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-l3)==(d8-d3),"to double, equals");
	  harness.check(getType(l8-d3)=='D',"result should be double");
	  harness.check(getType(d8-l3)=='D',"result should be double");
	  harness.check((f8-d3)==(d8-d3),"to double, equals");
	  harness.check((d8-f3)==(d8-d3),"to double, equals");
	  harness.check(getType(f8-d3)=='D',"result should be double");
	  harness.check(getType(d8-f3)=='D',"result should be double");
	  // to float:
	  harness.check((b8-f3)==(f8-f3),"to float equals");
	  harness.check((f8-b3)==(f8-f3),"to float equals");
	  harness.check(getType(b8-f3)=='F',"result should be float");
	  harness.check(getType(f8-b3)=='F',"result should be float");
	  harness.check((s8-f3)==(f8-f3),"to float equals");
	  harness.check((f8-s3)==(f8-f3),"to float equals");
	  harness.check(getType(s8-f3)=='F',"result should be float");
	  harness.check(getType(f8-s3)=='F',"result should be float");
	  harness.check((c8-f3)==(f8-f3),"to float equals");
	  harness.check((f8-c3)==(f8-f3),"to float equals");
	  harness.check(getType(c8-f3)=='F',"result should be float");
	  harness.check(getType(f8-c3)=='F',"result should be float");
	  harness.check((i8-f3)==(f8-f3),"to float equals");
	  harness.check((f8-i3)==(f8-f3),"to float equals");
	  harness.check(getType(i8-f3)=='F',"result should be float");
	  harness.check(getType(f8-i3)=='F',"result should be float");
	  harness.check((l8-f3)==(f8-f3),"to float equals");
	  harness.check((f8-l3)==(f8-f3),"to float equals");
	  harness.check(getType(l8-f3)=='F',"result should be float");
	  harness.check(getType(f8-l3)=='F',"result should be float");
	  // to long:
	  harness.check((b8-l3)==(l8-l3),"to long, equals");
	  harness.check((l8-b3)==(l8-l3),"to long, equals");
	  harness.check(getType(b8-l3)=='L',"result should be long");
	  harness.check(getType(l8-b3)=='L',"result should be long");
	  harness.check((s8-l3)==(l8-l3),"to long, equals");
	  harness.check((l8-s3)==(l8-l3),"to long, equals");
	  harness.check(getType(s8-l3)=='L',"result should be long");
	  harness.check(getType(l8-s3)=='L',"result should be long");
	  harness.check((c8-l3)==(l8-l3),"to long, equals");
	  harness.check((l8-c3)==(l8-l3),"to long, equals");
	  harness.check(getType(c8-l3)=='L',"result should be long");
	  harness.check(getType(l8-c3)=='L',"result should be long");
	  harness.check((i8-l3)==(l8-l3),"to long, equals");
	  harness.check((l8-i3)==(l8-l3),"to long, equals");
	  harness.check(getType(i8-l3)=='L',"result should be long");
	  harness.check(getType(l8-i3)=='L',"result should be long");
	  // type-int to int:
	  harness.check((b8-i3)==(i8-i3),"to int, equals");
	  harness.check((i8-b3)==(i8-i3),"to int, equals");
	  harness.check(getType(b8-i3)=='I',"result should be int");
	  harness.check(getType(i8-b3)=='I',"result should be int");
	  harness.check((s8-i3)==(i8-i3),"to int, equals");
	  harness.check((i8-s3)==(i8-i3),"to int, equals");
	  harness.check(getType(s8-i3)=='I',"result should be int");
	  harness.check(getType(i8-s3)=='I',"result should be int");
	  harness.check((c8-i3)==(i8-i3),"to int, equals");
	  harness.check((i8-c3)==(i8-i3),"to int, equals");
	  harness.check(getType(c8-i3)=='I',"result should be int");
	  harness.check(getType(i8-c3)=='I',"result should be int");
	  // type-char to int:
	  harness.check((b8-c3)==(i8-i3),"to int, equals");
	  harness.check((c8-b3)==(i8-i3),"to int, equals");
	  harness.check(getType(b8-c3)=='I',"result should be int");
	  harness.check(getType(c8-b3)=='I',"result should be int");
	  harness.check((s8-c3)==(i8-i3),"to int, equals");
	  harness.check((c8-s3)==(i8-i3),"to int, equals");
	  harness.check(getType(b8-c3)=='I',"result should be int");
	  harness.check(getType(c8-b3)=='I',"result should be int");
	  // type-short to int:
	  harness.check((b8-s3)==(i8-i3),"to int, equals");
	  harness.check((s8-b3)==(i8-i3),"to int, equals");
	  harness.check(getType(b8-s3)=='I',"result should be int");
	  harness.check(getType(s8-b3)=='I',"result should be int");
	}	
	
	private void testPromotionMultiplication()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*b3)==(d8*d3),"to double, equals");
	  harness.check(getType(b8*d3)=='D',"result should be double");
	  harness.check(getType(d8*b3)=='D',"result should be double");
	  harness.check((s8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*s3)==(d8*d3),"to double, equals");
	  harness.check(getType(s8*d3)=='D',"result should be double");
	  harness.check(getType(d8*s3)=='D',"result should be double");
	  harness.check((c8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*c3)==(d8*d3),"to double, equals");
	  harness.check(getType(c8*d3)=='D',"result should be double");
	  harness.check(getType(d8*c3)=='D',"result should be double");
	  harness.check((i8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*i3)==(d8*d3),"to double, equals");
	  harness.check(getType(i8*d3)=='D',"result should be double");
	  harness.check(getType(d8*i3)=='D',"result should be double");
	  harness.check((l8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*l3)==(d8*d3),"to double, equals");
	  harness.check(getType(l8*d3)=='D',"result should be double");
	  harness.check(getType(d8*l3)=='D',"result should be double");
	  harness.check((f8*d3)==(d8*d3),"to double, equals");
	  harness.check((d8*f3)==(d8*d3),"to double, equals");
	  harness.check(getType(f8*d3)=='D',"result should be double");
	  harness.check(getType(d8*f3)=='D',"result should be double");
	  // to float:
	  harness.check((b8*f3)==(f8*f3),"to float equals");
	  harness.check((f8*b3)==(f8*f3),"to float equals");
	  harness.check(getType(b8*f3)=='F',"result should be float");
	  harness.check(getType(f8*b3)=='F',"result should be float");
	  harness.check((s8*f3)==(f8*f3),"to float equals");
	  harness.check((f8*s3)==(f8*f3),"to float equals");
	  harness.check(getType(s8*f3)=='F',"result should be float");
	  harness.check(getType(f8*s3)=='F',"result should be float");
	  harness.check((c8*f3)==(f8*f3),"to float equals");
	  harness.check((f8*c3)==(f8*f3),"to float equals");
	  harness.check(getType(c8*f3)=='F',"result should be float");
	  harness.check(getType(f8*c3)=='F',"result should be float");
	  harness.check((i8*f3)==(f8*f3),"to float equals");
	  harness.check((f8*i3)==(f8*f3),"to float equals");
	  harness.check(getType(i8*f3)=='F',"result should be float");
	  harness.check(getType(f8*i3)=='F',"result should be float");
	  harness.check((l8*f3)==(f8*f3),"to float equals");
	  harness.check((f8*l3)==(f8*f3),"to float equals");
	  harness.check(getType(l8*f3)=='F',"result should be float");
	  harness.check(getType(f8*l3)=='F',"result should be float");
	  // to long:
	  harness.check((b8*l3)==(l8*l3),"to long, equals");
	  harness.check((l8*b3)==(l8*l3),"to long, equals");
	  harness.check(getType(b8*l3)=='L',"result should be long");
	  harness.check(getType(l8*b3)=='L',"result should be long");
	  harness.check((s8*l3)==(l8*l3),"to long, equals");
	  harness.check((l8*s3)==(l8*l3),"to long, equals");
	  harness.check(getType(s8*l3)=='L',"result should be long");
	  harness.check(getType(l8*s3)=='L',"result should be long");
	  harness.check((c8*l3)==(l8*l3),"to long, equals");
	  harness.check((l8*c3)==(l8*l3),"to long, equals");
	  harness.check(getType(c8*l3)=='L',"result should be long");
	  harness.check(getType(l8*c3)=='L',"result should be long");
	  harness.check((i8*l3)==(l8*l3),"to long, equals");
	  harness.check((l8*i3)==(l8*l3),"to long, equals");
	  harness.check(getType(i8*l3)=='L',"result should be long");
	  harness.check(getType(l8*i3)=='L',"result should be long");
	  // type*int to int:
	  harness.check((b8*i3)==(i8*i3),"to int, equals");
	  harness.check((i8*b3)==(i8*i3),"to int, equals");
	  harness.check(getType(b8*i3)=='I',"result should be int");
	  harness.check(getType(i8*b3)=='I',"result should be int");
	  harness.check((s8*i3)==(i8*i3),"to int, equals");
	  harness.check((i8*s3)==(i8*i3),"to int, equals");
	  harness.check(getType(s8*i3)=='I',"result should be int");
	  harness.check(getType(i8*s3)=='I',"result should be int");
	  harness.check((c8*i3)==(i8*i3),"to int, equals");
	  harness.check((i8*c3)==(i8*i3),"to int, equals");
	  harness.check(getType(c8*i3)=='I',"result should be int");
	  harness.check(getType(i8*c3)=='I',"result should be int");
	  // type*char to int:
	  harness.check((b8*c3)==(i8*i3),"to int, equals");
	  harness.check((c8*b3)==(i8*i3),"to int, equals");
	  harness.check(getType(b8*c3)=='I',"result should be int");
	  harness.check(getType(c8*b3)=='I',"result should be int");
	  harness.check((s8*c3)==(i8*i3),"to int, equals");
	  harness.check((c8*s3)==(i8*i3),"to int, equals");
	  harness.check(getType(b8*c3)=='I',"result should be int");
	  harness.check(getType(c8*b3)=='I',"result should be int");
	  // type*short to int:
	  harness.check((b8*s3)==(i8*i3),"to int, equals");
	  harness.check((s8*b3)==(i8*i3),"to int, equals");
	  harness.check(getType(b8*s3)=='I',"result should be int");
	  harness.check(getType(s8*b3)=='I',"result should be int");
	}	
	
	private void testPromotionDivision()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/b3)==(d8/d3),"to double, equals");
	  harness.check(getType(b8/d3)=='D',"result should be double");
	  harness.check(getType(d8/b3)=='D',"result should be double");
	  harness.check((s8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/s3)==(d8/d3),"to double, equals");
	  harness.check(getType(s8/d3)=='D',"result should be double");
	  harness.check(getType(d8/s3)=='D',"result should be double");
	  harness.check((c8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/c3)==(d8/d3),"to double, equals");
	  harness.check(getType(c8/d3)=='D',"result should be double");
	  harness.check(getType(d8/c3)=='D',"result should be double");
	  harness.check((i8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/i3)==(d8/d3),"to double, equals");
	  harness.check(getType(i8/d3)=='D',"result should be double");
	  harness.check(getType(d8/i3)=='D',"result should be double");
	  harness.check((l8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/l3)==(d8/d3),"to double, equals");
	  harness.check(getType(l8/d3)=='D',"result should be double");
	  harness.check(getType(d8/l3)=='D',"result should be double");
	  harness.check((f8/d3)==(d8/d3),"to double, equals");
	  harness.check((d8/f3)==(d8/d3),"to double, equals");
	  harness.check(getType(f8/d3)=='D',"result should be double");
	  harness.check(getType(d8/f3)=='D',"result should be double");
	  // to float:
	  harness.check((b8/f3)==(f8/f3),"to float equals");
	  harness.check((f8/b3)==(f8/f3),"to float equals");
	  harness.check(getType(b8/f3)=='F',"result should be float");
	  harness.check(getType(f8/b3)=='F',"result should be float");
	  harness.check((s8/f3)==(f8/f3),"to float equals");
	  harness.check((f8/s3)==(f8/f3),"to float equals");
	  harness.check(getType(s8/f3)=='F',"result should be float");
	  harness.check(getType(f8/s3)=='F',"result should be float");
	  harness.check((c8/f3)==(f8/f3),"to float equals");
	  harness.check((f8/c3)==(f8/f3),"to float equals");
	  harness.check(getType(c8/f3)=='F',"result should be float");
	  harness.check(getType(f8/c3)=='F',"result should be float");
	  harness.check((i8/f3)==(f8/f3),"to float equals");
	  harness.check((f8/i3)==(f8/f3),"to float equals");
	  harness.check(getType(i8/f3)=='F',"result should be float");
	  harness.check(getType(f8/i3)=='F',"result should be float");
	  harness.check((l8/f3)==(f8/f3),"to float equals");
	  harness.check((f8/l3)==(f8/f3),"to float equals");
	  harness.check(getType(l8/f3)=='F',"result should be float");
	  harness.check(getType(f8/l3)=='F',"result should be float");
	  // to long:
	  harness.check((b8/l3)==(l8/l3),"to long, equals");
	  harness.check((l8/b3)==(l8/l3),"to long, equals");
	  harness.check(getType(b8/l3)=='L',"result should be long");
	  harness.check(getType(l8/b3)=='L',"result should be long");
	  harness.check((s8/l3)==(l8/l3),"to long, equals");
	  harness.check((l8/s3)==(l8/l3),"to long, equals");
	  harness.check(getType(s8/l3)=='L',"result should be long");
	  harness.check(getType(l8/s3)=='L',"result should be long");
	  harness.check((c8/l3)==(l8/l3),"to long, equals");
	  harness.check((l8/c3)==(l8/l3),"to long, equals");
	  harness.check(getType(c8/l3)=='L',"result should be long");
	  harness.check(getType(l8/c3)=='L',"result should be long");
	  harness.check((i8/l3)==(l8/l3),"to long, equals");
	  harness.check((l8/i3)==(l8/l3),"to long, equals");
	  harness.check(getType(i8/l3)=='L',"result should be long");
	  harness.check(getType(l8/i3)=='L',"result should be long");
	  // type/int to int:
	  harness.check((b8/i3)==(i8/i3),"to int, equals");
	  harness.check((i8/b3)==(i8/i3),"to int, equals");
	  harness.check(getType(b8/i3)=='I',"result should be int");
	  harness.check(getType(i8/b3)=='I',"result should be int");
	  harness.check((s8/i3)==(i8/i3),"to int, equals");
	  harness.check((i8/s3)==(i8/i3),"to int, equals");
	  harness.check(getType(s8/i3)=='I',"result should be int");
	  harness.check(getType(i8/s3)=='I',"result should be int");
	  harness.check((c8/i3)==(i8/i3),"to int, equals");
	  harness.check((i8/c3)==(i8/i3),"to int, equals");
	  harness.check(getType(c8/i3)=='I',"result should be int");
	  harness.check(getType(i8/c3)=='I',"result should be int");
	  // type/char to int:
	  harness.check((b8/c3)==(i8/i3),"to int, equals");
	  harness.check((c8/b3)==(i8/i3),"to int, equals");
	  harness.check(getType(b8/c3)=='I',"result should be int");
	  harness.check(getType(c8/b3)=='I',"result should be int");
	  harness.check((s8/c3)==(i8/i3),"to int, equals");
	  harness.check((c8/s3)==(i8/i3),"to int, equals");
	  harness.check(getType(b8/c3)=='I',"result should be int");
	  harness.check(getType(c8/b3)=='I',"result should be int");
	  // type/short to int:
	  harness.check((b8/s3)==(i8/i3),"to int, equals");
	  harness.check((s8/b3)==(i8/i3),"to int, equals");
	  harness.check(getType(b8/s3)=='I',"result should be int");
	  harness.check(getType(s8/b3)=='I',"result should be int");
	}	
	
	private void testPromotionRemainder()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%b3)==(d8%d3),"to double, equals");
	  harness.check(getType(b8%d3)=='D',"result should be double");
	  harness.check(getType(d8%b3)=='D',"result should be double");
	  harness.check((s8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%s3)==(d8%d3),"to double, equals");
	  harness.check(getType(s8%d3)=='D',"result should be double");
	  harness.check(getType(d8%s3)=='D',"result should be double");
	  harness.check((c8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%c3)==(d8%d3),"to double, equals");
	  harness.check(getType(c8%d3)=='D',"result should be double");
	  harness.check(getType(d8%c3)=='D',"result should be double");
	  harness.check((i8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%i3)==(d8%d3),"to double, equals");
	  harness.check(getType(i8%d3)=='D',"result should be double");
	  harness.check(getType(d8%i3)=='D',"result should be double");
	  harness.check((l8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%l3)==(d8%d3),"to double, equals");
	  harness.check(getType(l8%d3)=='D',"result should be double");
	  harness.check(getType(d8%l3)=='D',"result should be double");
	  harness.check((f8%d3)==(d8%d3),"to double, equals");
	  harness.check((d8%f3)==(d8%d3),"to double, equals");
	  harness.check(getType(f8%d3)=='D',"result should be double");
	  harness.check(getType(d8%f3)=='D',"result should be double");
	  // to float:
	  harness.check((b8%f3)==(f8%f3),"to float equals");
	  harness.check((f8%b3)==(f8%f3),"to float equals");
	  harness.check(getType(b8%f3)=='F',"result should be float");
	  harness.check(getType(f8%b3)=='F',"result should be float");
	  harness.check((s8%f3)==(f8%f3),"to float equals");
	  harness.check((f8%s3)==(f8%f3),"to float equals");
	  harness.check(getType(s8%f3)=='F',"result should be float");
	  harness.check(getType(f8%s3)=='F',"result should be float");
	  harness.check((c8%f3)==(f8%f3),"to float equals");
	  harness.check((f8%c3)==(f8%f3),"to float equals");
	  harness.check(getType(c8%f3)=='F',"result should be float");
	  harness.check(getType(f8%c3)=='F',"result should be float");
	  harness.check((i8%f3)==(f8%f3),"to float equals");
	  harness.check((f8%i3)==(f8%f3),"to float equals");
	  harness.check(getType(i8%f3)=='F',"result should be float");
	  harness.check(getType(f8%i3)=='F',"result should be float");
	  harness.check((l8%f3)==(f8%f3),"to float equals");
	  harness.check((f8%l3)==(f8%f3),"to float equals");
	  harness.check(getType(l8%f3)=='F',"result should be float");
	  harness.check(getType(f8%l3)=='F',"result should be float");
	  // to long:
	  harness.check((b8%l3)==(l8%l3),"to long, equals");
	  harness.check((l8%b3)==(l8%l3),"to long, equals");
	  harness.check(getType(b8%l3)=='L',"result should be long");
	  harness.check(getType(l8%b3)=='L',"result should be long");
	  harness.check((s8%l3)==(l8%l3),"to long, equals");
	  harness.check((l8%s3)==(l8%l3),"to long, equals");
	  harness.check(getType(s8%l3)=='L',"result should be long");
	  harness.check(getType(l8%s3)=='L',"result should be long");
	  harness.check((c8%l3)==(l8%l3),"to long, equals");
	  harness.check((l8%c3)==(l8%l3),"to long, equals");
	  harness.check(getType(c8%l3)=='L',"result should be long");
	  harness.check(getType(l8%c3)=='L',"result should be long");
	  harness.check((i8%l3)==(l8%l3),"to long, equals");
	  harness.check((l8%i3)==(l8%l3),"to long, equals");
	  harness.check(getType(i8%l3)=='L',"result should be long");
	  harness.check(getType(l8%i3)=='L',"result should be long");
	  // type%int to int:
	  harness.check((b8%i3)==(i8%i3),"to int, equals");
	  harness.check((i8%b3)==(i8%i3),"to int, equals");
	  harness.check(getType(b8%i3)=='I',"result should be int");
	  harness.check(getType(i8%b3)=='I',"result should be int");
	  harness.check((s8%i3)==(i8%i3),"to int, equals");
	  harness.check((i8%s3)==(i8%i3),"to int, equals");
	  harness.check(getType(s8%i3)=='I',"result should be int");
	  harness.check(getType(i8%s3)=='I',"result should be int");
	  harness.check((c8%i3)==(i8%i3),"to int, equals");
	  harness.check((i8%c3)==(i8%i3),"to int, equals");
	  harness.check(getType(c8%i3)=='I',"result should be int");
	  harness.check(getType(i8%c3)=='I',"result should be int");
	  // type%char to int:
	  harness.check((b8%c3)==(i8%i3),"to int, equals");
	  harness.check((c8%b3)==(i8%i3),"to int, equals");
	  harness.check(getType(b8%c3)=='I',"result should be int");
	  harness.check(getType(c8%b3)=='I',"result should be int");
	  harness.check((s8%c3)==(i8%i3),"to int, equals");
	  harness.check((c8%s3)==(i8%i3),"to int, equals");
	  harness.check(getType(b8%c3)=='I',"result should be int");
	  harness.check(getType(c8%b3)=='I',"result should be int");
	  // type%short to int:
	  harness.check((b8%s3)==(i8%i3),"to int, equals");
	  harness.check((s8%b3)==(i8%i3),"to int, equals");
	  harness.check(getType(b8%s3)=='I',"result should be int");
	  harness.check(getType(s8%b3)=='I',"result should be int");
	}	
	
	private void testPromotionBitwiseAnd()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	//(no float and double floating-point notations for bitwise functions)
	  // to long:
	  harness.check((b8&l3)==(l8&l3),"to long, equals");
	  harness.check((l8&b3)==(l8&l3),"to long, equals");
	  harness.check(getType(b8&l3)=='L',"result should be long");
	  harness.check(getType(l8&b3)=='L',"result should be long");
	  harness.check((s8&l3)==(l8&l3),"to long, equals");
	  harness.check((l8&s3)==(l8&l3),"to long, equals");
	  harness.check(getType(s8&l3)=='L',"result should be long");
	  harness.check(getType(l8&s3)=='L',"result should be long");
	  harness.check((c8&l3)==(l8&l3),"to long, equals");
	  harness.check((l8&c3)==(l8&l3),"to long, equals");
	  harness.check(getType(c8&l3)=='L',"result should be long");
	  harness.check(getType(l8&c3)=='L',"result should be long");
	  harness.check((i8&l3)==(l8&l3),"to long, equals");
	  harness.check((l8&i3)==(l8&l3),"to long, equals");
	  harness.check(getType(i8&l3)=='L',"result should be long");
	  harness.check(getType(l8&i3)=='L',"result should be long");
	  // type&int to int:
	  harness.check((b8&i3)==(i8&i3),"to int, equals");
	  harness.check((i8&b3)==(i8&i3),"to int, equals");
	  harness.check(getType(b8&i3)=='I',"result should be int");
	  harness.check(getType(i8&b3)=='I',"result should be int");
	  harness.check((s8&i3)==(i8&i3),"to int, equals");
	  harness.check((i8&s3)==(i8&i3),"to int, equals");
	  harness.check(getType(s8&i3)=='I',"result should be int");
	  harness.check(getType(i8&s3)=='I',"result should be int");
	  harness.check((c8&i3)==(i8&i3),"to int, equals");
	  harness.check((i8&c3)==(i8&i3),"to int, equals");
	  harness.check(getType(c8&i3)=='I',"result should be int");
	  harness.check(getType(i8&c3)=='I',"result should be int");
	  // type&char to int:
	  harness.check((b8&c3)==(i8&i3),"to int, equals");
	  harness.check((c8&b3)==(i8&i3),"to int, equals");
	  harness.check(getType(b8&c3)=='I',"result should be int");
	  harness.check(getType(c8&b3)=='I',"result should be int");
	  harness.check((s8&c3)==(i8&i3),"to int, equals");
	  harness.check((c8&s3)==(i8&i3),"to int, equals");
	  harness.check(getType(b8&c3)=='I',"result should be int");
	  harness.check(getType(c8&b3)=='I',"result should be int");
	  // type&short to int:
	  harness.check((b8&s3)==(i8&i3),"to int, equals");
	  harness.check((s8&b3)==(i8&i3),"to int, equals");
	  harness.check(getType(b8&s3)=='I',"result should be int");
	  harness.check(getType(s8&b3)=='I',"result should be int");
	}		
	private void testPromotionBitwiseOr()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	//(no float and double floating-point notations for bitwise functions)
	  // to long:
	  harness.check((b8|l3)==(l8|l3),"to long, equals");
	  harness.check((l8|b3)==(l8|l3),"to long, equals");
	  harness.check(getType(b8|l3)=='L',"result should be long");
	  harness.check(getType(l8|b3)=='L',"result should be long");
	  harness.check((s8|l3)==(l8|l3),"to long, equals");
	  harness.check((l8|s3)==(l8|l3),"to long, equals");
	  harness.check(getType(s8|l3)=='L',"result should be long");
	  harness.check(getType(l8|s3)=='L',"result should be long");
	  harness.check((c8|l3)==(l8|l3),"to long, equals");
	  harness.check((l8|c3)==(l8|l3),"to long, equals");
	  harness.check(getType(c8|l3)=='L',"result should be long");
	  harness.check(getType(l8|c3)=='L',"result should be long");
	  harness.check((i8|l3)==(l8|l3),"to long, equals");
	  harness.check((l8|i3)==(l8|l3),"to long, equals");
	  harness.check(getType(i8|l3)=='L',"result should be long");
	  harness.check(getType(l8|i3)=='L',"result should be long");
	  // type|int to int:
	  harness.check((b8|i3)==(i8|i3),"to int, equals");
	  harness.check((i8|b3)==(i8|i3),"to int, equals");
	  harness.check(getType(b8|i3)=='I',"result should be int");
	  harness.check(getType(i8|b3)=='I',"result should be int");
	  harness.check((s8|i3)==(i8|i3),"to int, equals");
	  harness.check((i8|s3)==(i8|i3),"to int, equals");
	  harness.check(getType(s8|i3)=='I',"result should be int");
	  harness.check(getType(i8|s3)=='I',"result should be int");
	  harness.check((c8|i3)==(i8|i3),"to int, equals");
	  harness.check((i8|c3)==(i8|i3),"to int, equals");
	  harness.check(getType(c8|i3)=='I',"result should be int");
	  harness.check(getType(i8|c3)=='I',"result should be int");
	  // type|char to int:
	  harness.check((b8|c3)==(i8|i3),"to int, equals");
	  harness.check((c8|b3)==(i8|i3),"to int, equals");
	  harness.check(getType(b8|c3)=='I',"result should be int");
	  harness.check(getType(c8|b3)=='I',"result should be int");
	  harness.check((s8|c3)==(i8|i3),"to int, equals");
	  harness.check((c8|s3)==(i8|i3),"to int, equals");
	  harness.check(getType(b8|c3)=='I',"result should be int");
	  harness.check(getType(c8|b3)=='I',"result should be int");
	  // type|short to int:
	  harness.check((b8|s3)==(i8|i3),"to int, equals");
	  harness.check((s8|b3)==(i8|i3),"to int, equals");
	  harness.check(getType(b8|s3)=='I',"result should be int");
	  harness.check(getType(s8|b3)=='I',"result should be int");
	}
		
	private void testPromotionBitwiseXor()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	//(no float and double floating-point notations for bitwise functions)
	  // to long:
	  harness.check((b8^l3)==(l8^l3),"to long, equals");
	  harness.check((l8^b3)==(l8^l3),"to long, equals");
	  harness.check(getType(b8^l3)=='L',"result should be long");
	  harness.check(getType(l8^b3)=='L',"result should be long");
	  harness.check((s8^l3)==(l8^l3),"to long, equals");
	  harness.check((l8^s3)==(l8^l3),"to long, equals");
	  harness.check(getType(s8^l3)=='L',"result should be long");
	  harness.check(getType(l8^s3)=='L',"result should be long");
	  harness.check((c8^l3)==(l8^l3),"to long, equals");
	  harness.check((l8^c3)==(l8^l3),"to long, equals");
	  harness.check(getType(c8^l3)=='L',"result should be long");
	  harness.check(getType(l8^c3)=='L',"result should be long");
	  harness.check((i8^l3)==(l8^l3),"to long, equals");
	  harness.check((l8^i3)==(l8^l3),"to long, equals");
	  harness.check(getType(i8^l3)=='L',"result should be long");
	  harness.check(getType(l8^i3)=='L',"result should be long");
	  // type^int to int:
	  harness.check((b8^i3)==(i8^i3),"to int, equals");
	  harness.check((i8^b3)==(i8^i3),"to int, equals");
	  harness.check(getType(b8^i3)=='I',"result should be int");
	  harness.check(getType(i8^b3)=='I',"result should be int");
	  harness.check((s8^i3)==(i8^i3),"to int, equals");
	  harness.check((i8^s3)==(i8^i3),"to int, equals");
	  harness.check(getType(s8^i3)=='I',"result should be int");
	  harness.check(getType(i8^s3)=='I',"result should be int");
	  harness.check((c8^i3)==(i8^i3),"to int, equals");
	  harness.check((i8^c3)==(i8^i3),"to int, equals");
	  harness.check(getType(c8^i3)=='I',"result should be int");
	  harness.check(getType(i8^c3)=='I',"result should be int");
	  // type^char to int:
	  harness.check((b8^c3)==(i8^i3),"to int, equals");
	  harness.check((c8^b3)==(i8^i3),"to int, equals");
	  harness.check(getType(b8^c3)=='I',"result should be int");
	  harness.check(getType(c8^b3)=='I',"result should be int");
	  harness.check((s8^c3)==(i8^i3),"to int, equals");
	  harness.check((c8^s3)==(i8^i3),"to int, equals");
	  harness.check(getType(b8^c3)=='I',"result should be int");
	  harness.check(getType(c8^b3)=='I',"result should be int");
	  // type^short to int:
	  harness.check((b8^s3)==(i8^i3),"to int, equals");
	  harness.check((s8^b3)==(i8^i3),"to int, equals");
	  harness.check(getType(b8^s3)=='I',"result should be int");
	  harness.check(getType(s8^b3)=='I',"result should be int");
	}
	
	private void testPromotionCompareSmaller()
{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<b3)==(d8<d3),"to double, equals");
	  harness.check((s8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<s3)==(d8<d3),"to double, equals");
	  harness.check((c8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<c3)==(d8<d3),"to double, equals");
	  harness.check((i8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<i3)==(d8<d3),"to double, equals");
	  harness.check((l8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<l3)==(d8<d3),"to double, equals");
	  harness.check((f8<d3)==(d8<d3),"to double, equals");
	  harness.check((d8<f3)==(d8<d3),"to double, equals");
	  // to float:
	  harness.check((b8<f3)==(f8<f3),"to float equals");
	  harness.check((f8<b3)==(f8<f3),"to float equals");
	  harness.check((s8<f3)==(f8<f3),"to float equals");
	  harness.check((f8<s3)==(f8<f3),"to float equals");
	  harness.check((c8<f3)==(f8<f3),"to float equals");
	  harness.check((f8<c3)==(f8<f3),"to float equals");
	  harness.check((i8<f3)==(f8<f3),"to float equals");
	  harness.check((f8<i3)==(f8<f3),"to float equals");
	  harness.check((l8<f3)==(f8<f3),"to float equals");
	  harness.check((f8<l3)==(f8<f3),"to float equals");
	  // to long:
	  harness.check((b8<l3)==(l8<l3),"to long, equals");
	  harness.check((l8<b3)==(l8<l3),"to long, equals");
	  harness.check((s8<l3)==(l8<l3),"to long, equals");
	  harness.check((l8<s3)==(l8<l3),"to long, equals");
	  harness.check((c8<l3)==(l8<l3),"to long, equals");
	  harness.check((l8<c3)==(l8<l3),"to long, equals");
	  harness.check((i8<l3)==(l8<l3),"to long, equals");
	  harness.check((l8<i3)==(l8<l3),"to long, equals");
	  // type<int to int:
	  harness.check((b8<i3)==(i8<i3),"to int, equals");
	  harness.check((i8<b3)==(i8<i3),"to int, equals");
	  harness.check((s8<i3)==(i8<i3),"to int, equals");
	  harness.check((i8<s3)==(i8<i3),"to int, equals");
	  harness.check((c8<i3)==(i8<i3),"to int, equals");
	  harness.check((i8<c3)==(i8<i3),"to int, equals");
	  // type<char to int:
	  harness.check((b8<c3)==(i8<i3),"to int, equals");
	  harness.check((c8<b3)==(i8<i3),"to int, equals");
	  harness.check((s8<c3)==(i8<i3),"to int, equals");
	  harness.check((c8<s3)==(i8<i3),"to int, equals");
	  // type<short to int:
	  harness.check((b8<s3)==(i8<i3),"to int, equals");
	  harness.check((s8<b3)==(i8<i3),"to int, equals");
  }

	private void testPromotionCompareSmallerEquals()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=b3)==(d8<=d3),"to double, equals");
	  harness.check((s8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=s3)==(d8<=d3),"to double, equals");
	  harness.check((c8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=c3)==(d8<=d3),"to double, equals");
	  harness.check((i8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=i3)==(d8<=d3),"to double, equals");
	  harness.check((l8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=l3)==(d8<=d3),"to double, equals");
	  harness.check((f8<=d3)==(d8<=d3),"to double, equals");
	  harness.check((d8<=f3)==(d8<=d3),"to double, equals");
	  // to float:
	  harness.check((b8<=f3)==(f8<=f3),"to float equals");
	  harness.check((f8<=b3)==(f8<=f3),"to float equals");
	  harness.check((s8<=f3)==(f8<=f3),"to float equals");
	  harness.check((f8<=s3)==(f8<=f3),"to float equals");
	  harness.check((c8<=f3)==(f8<=f3),"to float equals");
	  harness.check((f8<=c3)==(f8<=f3),"to float equals");
	  harness.check((i8<=f3)==(f8<=f3),"to float equals");
	  harness.check((f8<=i3)==(f8<=f3),"to float equals");
	  harness.check((l8<=f3)==(f8<=f3),"to float equals");
	  harness.check((f8<=l3)==(f8<=f3),"to float equals");
	  // to long:
	  harness.check((b8<=l3)==(l8<=l3),"to long, equals");
	  harness.check((l8<=b3)==(l8<=l3),"to long, equals");
	  harness.check((s8<=l3)==(l8<=l3),"to long, equals");
	  harness.check((l8<=s3)==(l8<=l3),"to long, equals");
	  harness.check((c8<=l3)==(l8<=l3),"to long, equals");
	  harness.check((l8<=c3)==(l8<=l3),"to long, equals");
	  harness.check((i8<=l3)==(l8<=l3),"to long, equals");
	  harness.check((l8<=i3)==(l8<=l3),"to long, equals");
	  // type<=int to int:
	  harness.check((b8<=i3)==(i8<=i3),"to int, equals");
	  harness.check((i8<=b3)==(i8<=i3),"to int, equals");
	  harness.check((s8<=i3)==(i8<=i3),"to int, equals");
	  harness.check((i8<=s3)==(i8<=i3),"to int, equals");
	  harness.check((c8<=i3)==(i8<=i3),"to int, equals");
	  harness.check((i8<=c3)==(i8<=i3),"to int, equals");
	  // type<=char to int:
	  harness.check((b8<=c3)==(i8<=i3),"to int, equals");
	  harness.check((c8<=b3)==(i8<=i3),"to int, equals");
	  harness.check((s8<=c3)==(i8<=i3),"to int, equals");
	  harness.check((c8<=s3)==(i8<=i3),"to int, equals");
	  // type<=short to int:
	  harness.check((b8<=s3)==(i8<=i3),"to int, equals");
	  harness.check((s8<=b3)==(i8<=i3),"to int, equals");
  }

	private void testPromotionCompareEqual()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==b3)==(d8==d3),"to double, equals");
	  harness.check((s8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==s3)==(d8==d3),"to double, equals");
	  harness.check((c8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==c3)==(d8==d3),"to double, equals");
	  harness.check((i8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==i3)==(d8==d3),"to double, equals");
	  harness.check((l8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==l3)==(d8==d3),"to double, equals");
	  harness.check((f8==d3)==(d8==d3),"to double, equals");
	  harness.check((d8==f3)==(d8==d3),"to double, equals");
	  // to float:
	  harness.check((b8==f3)==(f8==f3),"to float equals");
	  harness.check((f8==b3)==(f8==f3),"to float equals");
	  harness.check((s8==f3)==(f8==f3),"to float equals");
	  harness.check((f8==s3)==(f8==f3),"to float equals");
	  harness.check((c8==f3)==(f8==f3),"to float equals");
	  harness.check((f8==c3)==(f8==f3),"to float equals");
	  harness.check((i8==f3)==(f8==f3),"to float equals");
	  harness.check((f8==i3)==(f8==f3),"to float equals");
	  harness.check((l8==f3)==(f8==f3),"to float equals");
	  harness.check((f8==l3)==(f8==f3),"to float equals");
	  // to long:
	  harness.check((b8==l3)==(l8==l3),"to long, equals");
	  harness.check((l8==b3)==(l8==l3),"to long, equals");
	  harness.check((s8==l3)==(l8==l3),"to long, equals");
	  harness.check((l8==s3)==(l8==l3),"to long, equals");
	  harness.check((c8==l3)==(l8==l3),"to long, equals");
	  harness.check((l8==c3)==(l8==l3),"to long, equals");
	  harness.check((i8==l3)==(l8==l3),"to long, equals");
	  harness.check((l8==i3)==(l8==l3),"to long, equals");
	  // type==int to int:
	  harness.check((b8==i3)==(i8==i3),"to int, equals");
	  harness.check((i8==b3)==(i8==i3),"to int, equals");
	  harness.check((s8==i3)==(i8==i3),"to int, equals");
	  harness.check((i8==s3)==(i8==i3),"to int, equals");
	  harness.check((c8==i3)==(i8==i3),"to int, equals");
	  harness.check((i8==c3)==(i8==i3),"to int, equals");
	  // type==char to int:
	  harness.check((b8==c3)==(i8==i3),"to int, equals");
	  harness.check((c8==b3)==(i8==i3),"to int, equals");
	  harness.check((s8==c3)==(i8==i3),"to int, equals");
	  harness.check((c8==s3)==(i8==i3),"to int, equals");
	  // type==short to int:
	  harness.check((b8==s3)==(i8==i3),"to int, equals");
	  harness.check((s8==b3)==(i8==i3),"to int, equals");
  }

	private void testPromotionCompareNotEqual()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=b3)==(d8!=d3),"to double, equals");
	  harness.check((s8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=s3)==(d8!=d3),"to double, equals");
	  harness.check((c8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=c3)==(d8!=d3),"to double, equals");
	  harness.check((i8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=i3)==(d8!=d3),"to double, equals");
	  harness.check((l8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=l3)==(d8!=d3),"to double, equals");
	  harness.check((f8!=d3)==(d8!=d3),"to double, equals");
	  harness.check((d8!=f3)==(d8!=d3),"to double, equals");
	  // to float:
	  harness.check((b8!=f3)==(f8!=f3),"to float equals");
	  harness.check((f8!=b3)==(f8!=f3),"to float equals");
	  harness.check((s8!=f3)==(f8!=f3),"to float equals");
	  harness.check((f8!=s3)==(f8!=f3),"to float equals");
	  harness.check((c8!=f3)==(f8!=f3),"to float equals");
	  harness.check((f8!=c3)==(f8!=f3),"to float equals");
	  harness.check((i8!=f3)==(f8!=f3),"to float equals");
	  harness.check((f8!=i3)==(f8!=f3),"to float equals");
	  harness.check((l8!=f3)==(f8!=f3),"to float equals");
	  harness.check((f8!=l3)==(f8!=f3),"to float equals");
	  // to long:
	  harness.check((b8!=l3)==(l8!=l3),"to long, equals");
	  harness.check((l8!=b3)==(l8!=l3),"to long, equals");
	  harness.check((s8!=l3)==(l8!=l3),"to long, equals");
	  harness.check((l8!=s3)==(l8!=l3),"to long, equals");
	  harness.check((c8!=l3)==(l8!=l3),"to long, equals");
	  harness.check((l8!=c3)==(l8!=l3),"to long, equals");
	  harness.check((i8!=l3)==(l8!=l3),"to long, equals");
	  harness.check((l8!=i3)==(l8!=l3),"to long, equals");
	  // type!=int to int:
	  harness.check((b8!=i3)==(i8!=i3),"to int, equals");
	  harness.check((i8!=b3)==(i8!=i3),"to int, equals");
	  harness.check((s8!=i3)==(i8!=i3),"to int, equals");
	  harness.check((i8!=s3)==(i8!=i3),"to int, equals");
	  harness.check((c8!=i3)==(i8!=i3),"to int, equals");
	  harness.check((i8!=c3)==(i8!=i3),"to int, equals");
	  // type!=char to int:
	  harness.check((b8!=c3)==(i8!=i3),"to int, equals");
	  harness.check((c8!=b3)==(i8!=i3),"to int, equals");
	  harness.check((s8!=c3)==(i8!=i3),"to int, equals");
	  harness.check((c8!=s3)==(i8!=i3),"to int, equals");
	  // type!=short to int:
	  harness.check((b8!=s3)==(i8!=i3),"to int, equals");
	  harness.check((s8!=b3)==(i8!=i3),"to int, equals");
  }

	private void testPromotionCompareBigger()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>b3)==(d8>d3),"to double, equals");
	  harness.check((s8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>s3)==(d8>d3),"to double, equals");
	  harness.check((c8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>c3)==(d8>d3),"to double, equals");
	  harness.check((i8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>i3)==(d8>d3),"to double, equals");
	  harness.check((l8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>l3)==(d8>d3),"to double, equals");
	  harness.check((f8>d3)==(d8>d3),"to double, equals");
	  harness.check((d8>f3)==(d8>d3),"to double, equals");
	  // to float:
	  harness.check((b8>f3)==(f8>f3),"to float equals");
	  harness.check((f8>b3)==(f8>f3),"to float equals");
	  harness.check((s8>f3)==(f8>f3),"to float equals");
	  harness.check((f8>s3)==(f8>f3),"to float equals");
	  harness.check((c8>f3)==(f8>f3),"to float equals");
	  harness.check((f8>c3)==(f8>f3),"to float equals");
	  harness.check((i8>f3)==(f8>f3),"to float equals");
	  harness.check((f8>i3)==(f8>f3),"to float equals");
	  harness.check((l8>f3)==(f8>f3),"to float equals");
	  harness.check((f8>l3)==(f8>f3),"to float equals");
	  // to long:
	  harness.check((b8>l3)==(l8>l3),"to long, equals");
	  harness.check((l8>b3)==(l8>l3),"to long, equals");
	  harness.check((s8>l3)==(l8>l3),"to long, equals");
	  harness.check((l8>s3)==(l8>l3),"to long, equals");
	  harness.check((c8>l3)==(l8>l3),"to long, equals");
	  harness.check((l8>c3)==(l8>l3),"to long, equals");
	  harness.check((i8>l3)==(l8>l3),"to long, equals");
	  harness.check((l8>i3)==(l8>l3),"to long, equals");
	  // type>int to int:
	  harness.check((b8>i3)==(i8>i3),"to int, equals");
	  harness.check((i8>b3)==(i8>i3),"to int, equals");
	  harness.check((s8>i3)==(i8>i3),"to int, equals");
	  harness.check((i8>s3)==(i8>i3),"to int, equals");
	  harness.check((c8>i3)==(i8>i3),"to int, equals");
	  harness.check((i8>c3)==(i8>i3),"to int, equals");
	  // type>char to int:
	  harness.check((b8>c3)==(i8>i3),"to int, equals");
	  harness.check((c8>b3)==(i8>i3),"to int, equals");
	  harness.check((s8>c3)==(i8>i3),"to int, equals");
	  harness.check((c8>s3)==(i8>i3),"to int, equals");
	  // type>short to int:
	  harness.check((b8>s3)==(i8>i3),"to int, equals");
	  harness.check((s8>b3)==(i8>i3),"to int, equals");
  }

	private void testPromotionCompareBiggerEquals()
  {
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=b3)==(d8>=d3),"to double, equals");
	  harness.check((s8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=s3)==(d8>=d3),"to double, equals");
	  harness.check((c8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=c3)==(d8>=d3),"to double, equals");
	  harness.check((i8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=i3)==(d8>=d3),"to double, equals");
	  harness.check((l8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=l3)==(d8>=d3),"to double, equals");
	  harness.check((f8>=d3)==(d8>=d3),"to double, equals");
	  harness.check((d8>=f3)==(d8>=d3),"to double, equals");
	  // to float:
	  harness.check((b8>=f3)==(f8>=f3),"to float equals");
	  harness.check((f8>=b3)==(f8>=f3),"to float equals");
	  harness.check((s8>=f3)==(f8>=f3),"to float equals");
	  harness.check((f8>=s3)==(f8>=f3),"to float equals");
	  harness.check((c8>=f3)==(f8>=f3),"to float equals");
	  harness.check((f8>=c3)==(f8>=f3),"to float equals");
	  harness.check((i8>=f3)==(f8>=f3),"to float equals");
	  harness.check((f8>=i3)==(f8>=f3),"to float equals");
	  harness.check((l8>=f3)==(f8>=f3),"to float equals");
	  harness.check((f8>=l3)==(f8>=f3),"to float equals");
	  // to long:
	  harness.check((b8>=l3)==(l8>=l3),"to long, equals");
	  harness.check((l8>=b3)==(l8>=l3),"to long, equals");
	  harness.check((s8>=l3)==(l8>=l3),"to long, equals");
	  harness.check((l8>=s3)==(l8>=l3),"to long, equals");
	  harness.check((c8>=l3)==(l8>=l3),"to long, equals");
	  harness.check((l8>=c3)==(l8>=l3),"to long, equals");
	  harness.check((i8>=l3)==(l8>=l3),"to long, equals");
	  harness.check((l8>=i3)==(l8>=l3),"to long, equals");
	  // type>=int to int:
	  harness.check((b8>=i3)==(i8>=i3),"to int, equals");
	  harness.check((i8>=b3)==(i8>=i3),"to int, equals");
	  harness.check((s8>=i3)==(i8>=i3),"to int, equals");
	  harness.check((i8>=s3)==(i8>=i3),"to int, equals");
	  harness.check((c8>=i3)==(i8>=i3),"to int, equals");
	  harness.check((i8>=c3)==(i8>=i3),"to int, equals");
	  // type>=char to int:
	  harness.check((b8>=c3)==(i8>=i3),"to int, equals");
	  harness.check((c8>=b3)==(i8>=i3),"to int, equals");
	  harness.check((s8>=c3)==(i8>=i3),"to int, equals");
	  harness.check((c8>=s3)==(i8>=i3),"to int, equals");
	  // type>=short to int:
	  harness.check((b8>=s3)==(i8>=i3),"to int, equals");
	  harness.check((s8>=b3)==(i8>=i3),"to int, equals");
}

/*
	private void testPromotionXXXX()
	{
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
  	
	  // to double:
	  harness.check((b8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXb3)==(d8XXd3),"to double, equals");
	  harness.check(getType(b8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXb3)=='D',"result should be double");
	  harness.check((s8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXs3)==(d8XXd3),"to double, equals");
	  harness.check(getType(s8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXs3)=='D',"result should be double");
	  harness.check((c8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXc3)==(d8XXd3),"to double, equals");
	  harness.check(getType(c8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXc3)=='D',"result should be double");
	  harness.check((i8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXi3)==(d8XXd3),"to double, equals");
	  harness.check(getType(i8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXi3)=='D',"result should be double");
	  harness.check((l8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXl3)==(d8XXd3),"to double, equals");
	  harness.check(getType(l8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXl3)=='D',"result should be double");
	  harness.check((f8XXd3)==(d8XXd3),"to double, equals");
	  harness.check((d8XXf3)==(d8XXd3),"to double, equals");
	  harness.check(getType(f8XXd3)=='D',"result should be double");
	  harness.check(getType(d8XXf3)=='D',"result should be double");
	  // to float:
	  harness.check((b8XXf3)==(f8XXf3),"to float equals");
	  harness.check((f8XXb3)==(f8XXf3),"to float equals");
	  harness.check(getType(b8XXf3)=='F',"result should be float");
	  harness.check(getType(f8XXb3)=='F',"result should be float");
	  harness.check((s8XXf3)==(f8XXf3),"to float equals");
	  harness.check((f8XXs3)==(f8XXf3),"to float equals");
	  harness.check(getType(s8XXf3)=='F',"result should be float");
	  harness.check(getType(f8XXs3)=='F',"result should be float");
	  harness.check((c8XXf3)==(f8XXf3),"to float equals");
	  harness.check((f8XXc3)==(f8XXf3),"to float equals");
	  harness.check(getType(c8XXf3)=='F',"result should be float");
	  harness.check(getType(f8XXc3)=='F',"result should be float");
	  harness.check((i8XXf3)==(f8XXf3),"to float equals");
	  harness.check((f8XXi3)==(f8XXf3),"to float equals");
	  harness.check(getType(i8XXf3)=='F',"result should be float");
	  harness.check(getType(f8XXi3)=='F',"result should be float");
	  harness.check((l8XXf3)==(f8XXf3),"to float equals");
	  harness.check((f8XXl3)==(f8XXf3),"to float equals");
	  harness.check(getType(l8XXf3)=='F',"result should be float");
	  harness.check(getType(f8XXl3)=='F',"result should be float");
	  // to long:
	  harness.check((b8XXl3)==(l8XXl3),"to long, equals");
	  harness.check((l8XXb3)==(l8XXl3),"to long, equals");
	  harness.check(getType(b8XXl3)=='L',"result should be long");
	  harness.check(getType(l8XXb3)=='L',"result should be long");
	  harness.check((s8XXl3)==(l8XXl3),"to long, equals");
	  harness.check((l8XXs3)==(l8XXl3),"to long, equals");
	  harness.check(getType(s8XXl3)=='L',"result should be long");
	  harness.check(getType(l8XXs3)=='L',"result should be long");
	  harness.check((c8XXl3)==(l8XXl3),"to long, equals");
	  harness.check((l8XXc3)==(l8XXl3),"to long, equals");
	  harness.check(getType(c8XXl3)=='L',"result should be long");
	  harness.check(getType(l8XXc3)=='L',"result should be long");
	  harness.check((i8XXl3)==(l8XXl3),"to long, equals");
	  harness.check((l8XXi3)==(l8XXl3),"to long, equals");
	  harness.check(getType(i8XXl3)=='L',"result should be long");
	  harness.check(getType(l8XXi3)=='I',"result should be int");
	  // typeXXint to int:
	  harness.check((b8XXi3)==(i8XXi3),"to int, equals");
	  harness.check((i8XXb3)==(i8XXi3),"to int, equals");
	  harness.check(getType(b8XXi3)=='I',"result should be int");
	  harness.check(getType(i8XXb3)=='I',"result should be int");
	  harness.check((s8XXi3)==(i8XXi3),"to int, equals");
	  harness.check((i8XXs3)==(i8XXi3),"to int, equals");
	  harness.check(getType(s8XXi3)=='I',"result should be int");
	  harness.check(getType(i8XXs3)=='I',"result should be int");
	  harness.check((c8XXi3)==(i8XXi3),"to int, equals");
	  harness.check((i8XXc3)==(i8XXi3),"to int, equals");
	  harness.check(getType(c8XXi3)=='I',"result should be int");
	  harness.check(getType(i8XXc3)=='I',"result should be int");
	  // typeXXchar to int:
	  harness.check((b8XXc3)==(i8XXi3),"to int, equals");
	  harness.check((c8XXb3)==(i8XXi3),"to int, equals");
	  harness.check(getType(b8XXc3)=='I',"result should be int");
	  harness.check(getType(c8XXb3)=='I',"result should be int");
	  harness.check((s8XXc3)==(i8XXi3),"to int, equals");
	  harness.check((c8XXs3)==(i8XXi3),"to int, equals");
	  harness.check(getType(b8XXc3)=='I',"result should be int");
	  harness.check(getType(c8XXb3)=='I',"result should be int");
	  // typeXXshort to int:
	  harness.check((b8XXs3)==(i8XXi3),"to int, equals");
	  harness.check((s8XXb3)==(i8XXi3),"to int, equals");
	  harness.check(getType(b8XXs3)=='I',"result should be int");
	  harness.check(getType(s8XXb3)=='I',"result should be int");
	}	
*/
  public void testPromotionConditionalOperator()
  {
/*  result = (Condition)?value1:value2;
    value1, value 2 same type: result that type:
    values byte & short: result short
    byte, short char + constant castable to (1) =>type of (1)
    byte, short, char, int to byte, short, char ,int => result int
    one operand = long, result = long
    one operand = float, result = float
    one operand = double, result = double
    value1 & null: result = type of value1
    null & value2: result = type of value2

    value1 & value2 reference types:
      => value1 convertable to value2 : type = type of value2
      => value2 convertable to value1 : type = type of value1
      =>else: compile-time error
*/
  	byte    b3=3;
  	short   s3=3;
  	char    c3=3;
  	int     i3=3;
  	long    l3=3L;
  	float   f3=3.0f;
  	double  d3=3.00;
  	byte    b8=8;
  	short   s8=8;
  	char    c8=8;
  	int     i8=8;
  	long    l8=8L;
  	float   f8=8.0f;
  	double  d8=8.00;
    //equal types make result of that type
    harness.check(getType( (true)?d3:d8)=='D',"result should be double");
    harness.check(getType((false)?d3:d8)=='D',"result should be double");
    harness.check(getType( (true)?f3:f8)=='F',"result should be float");
    harness.check(getType((false)?f3:f8)=='F',"result should be float");
    harness.check(getType( (true)?l3:l8)=='L',"result should be long");
    harness.check(getType((false)?l3:l8)=='L',"result should be long");
    harness.check(getType( (true)?i3:i8)=='I',"result should be int");
    harness.check(getType((false)?i3:i8)=='I',"result should be int");
    harness.check(getType( (true)?c3:c8)=='C',"result should be char");
    harness.check(getType((false)?c3:c8)=='C',"result should be char");
    harness.check(getType( (true)?s3:s8)=='S',"result should be short");
    harness.check(getType((false)?s3:s8)=='S',"result should be short");
    harness.check(getType( (true)?b3:b8)=='B',"result should be byte");
    harness.check(getType((false)?b3:b8)=='B',"result should be byte");

    //byte and short make result short
    harness.check(getType( (true)?b3:s8)=='S',"result should be short");
    harness.check(getType((false)?b3:s8)=='S',"result should be short");
    harness.check(getType( (true)?s3:b8)=='S',"result should be short");
    harness.check(getType((false)?s3:b8)=='S',"result should be short");

    // all other byte, short, char, int make int
    //(byte & char)
    harness.check(getType( (true)?b3:c8)=='I',"result should be int");
    harness.check(getType((false)?b3:c8)=='I',"result should be int");
    harness.check(getType( (true)?c3:b8)=='I',"result should be int");
    harness.check(getType((false)?c3:b8)=='I',"result should be int");
    //(byte & int)
    harness.check(getType( (true)?b3:i8)=='I',"result should be int");
    harness.check(getType((false)?b3:i8)=='I',"result should be int");
    harness.check(getType( (true)?i3:b8)=='I',"result should be int");
    harness.check(getType((false)?i3:b8)=='I',"result should be int");
    //(short & char)
    harness.check(getType( (true)?s3:c8)=='I',"result should be int");
    harness.check(getType((false)?s3:c8)=='I',"result should be int");
    harness.check(getType( (true)?c3:s8)=='I',"result should be int");
    harness.check(getType((false)?c3:s8)=='I',"result should be int");
    //(short & int)
    harness.check(getType( (true)?s3:i8)=='I',"result should be int");
    harness.check(getType((false)?s3:i8)=='I',"result should be int");
    harness.check(getType( (true)?i3:s8)=='I',"result should be int");
    harness.check(getType((false)?i3:s8)=='I',"result should be int");
    //(char & int)
    harness.check(getType( (true)?c3:i8)=='I',"result should be int");
    harness.check(getType((false)?c3:i8)=='I',"result should be int");
    harness.check(getType( (true)?i3:c8)=='I',"result should be int");
    harness.check(getType((false)?i3:c8)=='I',"result should be int");

    // one operand long makes result long
    //(byte & long)
    harness.check(getType( (true)?b3:l8)=='L',"result should be long");
    harness.check(getType((false)?b3:l8)=='L',"result should be long");
    harness.check(getType( (true)?l3:b8)=='L',"result should be long");
    harness.check(getType((false)?l3:b8)=='L',"result should be long");
    //(short & long)
    harness.check(getType( (true)?s3:l8)=='L',"result should be long");
    harness.check(getType((false)?s3:l8)=='L',"result should be long");
    harness.check(getType( (true)?l3:s8)=='L',"result should be long");
    harness.check(getType((false)?l3:s8)=='L',"result should be long");
    //(char & long)
    harness.check(getType( (true)?c3:l8)=='L',"result should be long");
    harness.check(getType((false)?c3:l8)=='L',"result should be long");
    harness.check(getType( (true)?l3:c8)=='L',"result should be long");
    harness.check(getType((false)?l3:c8)=='L',"result should be long");
    //(int & long)
    harness.check(getType( (true)?i3:l8)=='L',"result should be long");
    harness.check(getType((false)?i3:l8)=='L',"result should be long");
    harness.check(getType( (true)?l3:i8)=='L',"result should be long");
    harness.check(getType((false)?l3:i8)=='L',"result should be long");

    // one operand float makes result float
    //(byte & float)
    harness.check(getType( (true)?b3:f8)=='F',"result should be float");
    harness.check(getType((false)?b3:f8)=='F',"result should be float");
    harness.check(getType( (true)?f3:b8)=='F',"result should be float");
    harness.check(getType((false)?f3:b8)=='F',"result should be float");
    //(short & float)
    harness.check(getType( (true)?s3:f8)=='F',"result should be float");
    harness.check(getType((false)?s3:f8)=='F',"result should be float");
    harness.check(getType( (true)?f3:s8)=='F',"result should be float");
    harness.check(getType((false)?f3:s8)=='F',"result should be float");
    //(char & float)
    harness.check(getType( (true)?c3:f8)=='F',"result should be float");
    harness.check(getType((false)?c3:f8)=='F',"result should be float");
    harness.check(getType( (true)?f3:c8)=='F',"result should be float");
    harness.check(getType((false)?f3:c8)=='F',"result should be float");
    //(int & float)
    harness.check(getType( (true)?i3:f8)=='F',"result should be float");
    harness.check(getType((false)?i3:f8)=='F',"result should be float");
    harness.check(getType( (true)?f3:i8)=='F',"result should be float");
    harness.check(getType((false)?f3:i8)=='F',"result should be float");
    //(long & float)
    harness.check(getType( (true)?l3:f8)=='F',"result should be float");
    harness.check(getType((false)?l3:f8)=='F',"result should be float");
    harness.check(getType( (true)?f3:l8)=='F',"result should be float");
    harness.check(getType((false)?f3:l8)=='F',"result should be float");

    // one operand double makes result double
    //(byte & float)
    harness.check(getType( (true)?b3:d8)=='D',"result should be double");
    harness.check(getType((false)?b3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:b8)=='D',"result should be double");
    harness.check(getType((false)?d3:b8)=='D',"result should be double");
    //(short & float)
    harness.check(getType( (true)?s3:d8)=='D',"result should be double");
    harness.check(getType((false)?s3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:s8)=='D',"result should be double");
    harness.check(getType((false)?d3:s8)=='D',"result should be double");
    //(char & float)
    harness.check(getType( (true)?c3:d8)=='D',"result should be double");
    harness.check(getType((false)?c3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:c8)=='D',"result should be double");
    harness.check(getType((false)?d3:c8)=='D',"result should be double");
    //(int & float)
    harness.check(getType( (true)?i3:d8)=='D',"result should be double");
    harness.check(getType((false)?i3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:i8)=='D',"result should be double");
    harness.check(getType((false)?d3:i8)=='D',"result should be double");
    //(long & float)
    harness.check(getType( (true)?l3:d8)=='D',"result should be double");
    harness.check(getType((false)?l3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:l8)=='D',"result should be double");
    harness.check(getType((false)?d3:l8)=='D',"result should be double");
    //(float and double)
    harness.check(getType( (true)?f3:d8)=='D',"result should be double");
    harness.check(getType((false)?f3:d8)=='D',"result should be double");
    harness.check(getType( (true)?d3:f8)=='D',"result should be double");
    harness.check(getType((false)?d3:f8)=='D',"result should be double");

/*
  final static int   inull = 0x00000000;

  //int, short, char =>byte
  final static int   imaxbyte  = 0x0000007f;
  final static int   iminbyte  =-0x00000080;

  // int, byte=>short
  final static int   imaxshort  = 0x00007fff;
  final static int   iminshort  =-0x00008000;

  // int, short => byte
  final static int   imaxchar  = 0x0000ffff;
  //final static int   iminchar  =-0x00008000;
*/
  //special case: byte and static constant int of value between byte-min and byte-max
    harness.check(getType( (true)?b3:imaxbyte)=='B',"result should be byte");
    harness.check(getType((false)?b3:imaxbyte)=='B',"result should be byte");
    harness.check(getType( (true)?imaxbyte:b8)=='B',"result should be byte");
    harness.check(getType((false)?imaxbyte:b8)=='B',"result should be byte");
    harness.check(getType( (true)?b3:imaxshort)=='I',"result should be int");
    harness.check(getType((false)?b3:imaxshort)=='I',"result should be int");
    harness.check(getType( (true)?imaxshort:b8)=='I',"result should be int");
    harness.check(getType((false)?imaxshort:b8)=='I',"result should be int");
  //special case: short and static constant int of value between short-min and short-max
    harness.check(getType( (true)?s3:imaxshort)=='S',"result should be short");
    harness.check(getType((false)?s3:imaxshort)=='S',"result should be short");
    harness.check(getType( (true)?imaxshort:s8)=='S',"result should be short");
    harness.check(getType((false)?imaxshort:s8)=='S',"result should be short");
    harness.check(getType( (true)?s3:imaxchar)=='I',"result should be int");
    harness.check(getType((false)?s3:imaxchar)=='I',"result should be int");
    harness.check(getType( (true)?imaxchar:s8)=='I',"result should be int");
    harness.check(getType((false)?imaxchar:s8)=='I',"result should be int");
  //special case: char and static constant int of value between char-min and char-max
    harness.check(getType( (true)?c3:imaxchar)=='C',"result should be char");
    harness.check(getType((false)?c3:imaxchar)=='C',"result should be char");
    harness.check(getType( (true)?imaxchar:c8)=='C',"result should be char");
    harness.check(getType((false)?imaxchar:c8)=='C',"result should be char");
    harness.check(getType( (true)?c3:iminbyte)=='I',"result should be int");
    harness.check(getType((false)?c3:iminbyte)=='I',"result should be int");
    harness.check(getType( (true)?iminbyte:c8)=='I',"result should be int");
    harness.check(getType((false)?iminbyte:c8)=='I',"result should be int");
  }

  private boolean inRange(float base, int exponent, float tocheck)
  {
    //instead of converting a Math.pow()or math.log function, we are safer (but slower) providing our own exponent algorithm;
    float ex=1;
    if(exponent >0)
    {
      for(int i=0; i<exponent; i++)
        ex*=10;
    }
    if(exponent <0)
    {
      for(int i=0; i>exponent; i--)
        ex/=10;
    }
    float min = (base-0.0001f)* ex;
    if(min>tocheck)
      return false;
    float max = (base+0.0001f)* ex;
    if(max<tocheck)
      return false;
    return true;
  }

  private boolean inRange(double base, int exponent, double tocheck)
  {
    //we could use Math.pow(), but for clarity, we provide our own exponent algorithm;
    double ex=1;
    if(exponent >0)
    {
      for(int i=0; i<exponent; i++)
        ex*=10;
    }
    if(exponent <0)
    {
      for(int i=0; i>exponent; i--)
        ex/=10;
    }
    double min = (base-0.0000001)* ex;
    if(min>tocheck)
      return false;
    double max = (base+0.0000001)* ex;
    if(max<tocheck)
      return false;
    return true;
  }


/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang: conversion and casting primitives");
		//conversoin has the following fields:
		//Assignment,
		//primitive widening
		testAssignmentWidening();
		//primitive narrowing for constants in certain cases
		testAssignmentNarrowing();
		//reference widening (see ConversionReferences.java)
		
		
		//invocation through functions
		//primitive widening
		testInvocationWidening();
		//reference widening (see ConversionReferences.java)
		
		//casting
		//primitive widening
		testCastingWidening();
		//complete primitive narrowing
		testCastingNarrowing();
		//reference widening (see ConversionReferences.java)
		//reference narrowing (see ConversionReferences.java)
		
		//String conversion
		testStringConversion();
		
		//numeric Promotion
		//primitive widening on either unary operations or binary functions
		testPromotionUnary();
		testPromotionBinary();
		
		//String conversion
		//see the String +operator and the primitives tests
	}
}
