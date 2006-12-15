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
import java.io.Serializable; //serializaable property

/*********************************************************************************************************************
*
*  Test class for conversion between types
*
*********************************************************************************************************************/
public class ReferenceConversionTest implements Testlet
{
/**********************************************************************************************************************
* variables for classes and interfaces
*/
  //classes, interfaces
  Hexagon           HX;
  Parallellogram    PL;
  Diamond           DM;
  Rectangle         RC;
  Square            SQ;

  EqualSides       IEqualSides;
  FourEqualSides   IFourEqual;
  FourRightAngles  IFourRight;
  //object
  Object    O;

  //arrays...
  Hexagon[]         HXArray;
  Parallellogram[]  PLArray;
  Diamond[]         DMArray;
  Rectangle[]       RCArray;
  Square[]          SQArray;

  EqualSides[]      IEqualSidesArray;
  FourEqualSides[]  IFourEqualArray;// = new HasFourEqualSides[3];
  FourRightAngles[] IFourRightArray;

  //primitive arrays
  boolean[] ZArray = new boolean[3];
  byte[]    BArray = new byte[3];
  short[]   SArray = new short[3];
  char[]    CArray = new char[3];
  int[]     IArray = new int[3];
  long[]    LArray = new long[3];
  float[]   FArray = new float[3];
  double[]  DArray = new double[3];

  //object aray
  Object[]  OArray;

  //our test harness
  TestHarness harness;
		
/**********************************************************************************************************************
* Class widening through assignment
* => assign class to superclass
* => assign class to implemented interface
* => assign sub-interface to base interface
* => assign null type to class, interface, array...
* => assign class into object
* => assign array into object
* => assign array into Cloneable
* => assign array into java.io.Serializable
*/
	public void	testAssignmentWidening()
	{
    DM=new Diamond(5,45);
    DMArray=new Diamond[3];
    DMArray[0]=DM;

    RC=new Rectangle(5,7);
    RCArray=new Rectangle[3];
    RCArray[0]=RC;

    SQ=new Square(6);
    SQArray=new Square[3];
    SQArray[0]=SQ;

    HX=new Hexagon();
    HXArray=new Hexagon[3];

    // => assign class to superclass
    // ---------------------------
    harness.checkPoint("Cast widening: class to super class");
    PL=DM;
    harness.check(PL.type,"Dm");
    PL=RC;
    harness.check(PL.type,"Rc");
    PL=SQ;
    harness.check(PL.type,"Sq");
    // not permitted, throws a compile-time error:
    //PL=(Parallellogram)HX;
    //harness.check(PL.type,"Hx");

    harness.checkPoint("Cast widening: class Array to super class Array");
    PLArray=DMArray;
    harness.check(PLArray[0].type,"Dm");
    PLArray=RCArray;
    harness.check(PLArray[0].type,"Rc");
    PLArray=SQArray;
    harness.check(PLArray[0].type,"Sq");
    // not permitted, throws a compile-time error:
    //PLArray=(Parallellogram[])HXArray;
    //harness.check(PLArray[0].type,"Hx");


    // => assign class to implemented interface
    // --------------------------------------
    harness.checkPoint("assignment widening: class to implemented interface");
    IFourEqual=DM;
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=RC;
    harness.check(IFourRight.getSurfaceFormula(),"base x height");
    // not permitted, throws a compile-time error:
    //IFourEqual=(FourEqualsides)RC; // rectange does not implement equal sides
    IFourEqual=SQ;
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=SQ;
    harness.check(IFourRight.getSurfaceFormula(),"square side");

    harness.checkPoint("assignment widening: class Array to implemented interface Array");
    IFourEqualArray=DMArray;
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=RCArray;
    harness.check(IFourRightArray[0].getSurfaceFormula(),"base x height");
    // not permitted, throws a compile-time error:
    //IFourEqualArray=(FourEqualsides[])RC; // rectange does not implement equal sides
    IFourEqualArray=SQArray;
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=SQArray;
    harness.check(IFourRightArray[0].getSurfaceFormula(),"square side");



    // => assign sub-interface to base interface
    // ----------------------------------------
    harness.checkPoint("Cast widening: sub interface to base interface");
    //iFourEqual== //already implemented above
    IEqualSides=IFourEqual;
    harness.check(IEqualSides.getLengthFormula(),"side x 4");
    // not permitted, throws a compile-time error:
    //IEqualSides=(EqualSides)IFourEqual;

    harness.checkPoint("Cast widening: sub interface to base interface");
    //iFourEqual== //already implemented above
    IEqualSidesArray=IFourEqualArray;
    harness.check(IEqualSidesArray[0].getLengthFormula(),"side x 4");
    // not permitted, throws a compile-time error:
    //IEqualSides=(EqualSides)IFourEqual;



    // => assign null type to class, interface, array...
    // ------------------------------------------------
    harness.checkPoint("assignment widening: null type to class, interface, array..");
    String nulltype;

    // class
    try
    {
      HX=null; //casting is allowed
      harness.check(true);
      nulltype=HX.type;//accessing should throw a null pointer exception
      harness.fail("null type to class: accessing data not failed");
      //harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    // interface
    try
    {
      IFourRight=null; //casting is allowed
      harness.check(true);
      nulltype=FourRightAngles.Prequisition;//casting should provide an implicit variable
     // harness.fail("null type to interface accessing data not failed");
      harness.check(nulltype,"four right angles");
      nulltype=IFourRight.getSurfaceFormula();//virtual function, not defined by NULL class
      harness.fail("null type to interface accessing virtual data not failed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    // array
    try
    {
      HXArray=null; //casting is allowed
      harness.check(true);
      nulltype=HXArray[0].type;//accessing should throw a null pointer exception
      harness.fail("null type to array: accessing data not failed");
      //harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }


    // => cast class into object
    // -------------------------
    harness.checkPoint("assignment widening: class into object");
    O=DM;
    O=RC;
    O=SQ;
    O=HX;

    harness.checkPoint("assignment widening: class Array into object Array");
    OArray=DMArray;
    OArray=RCArray;
    OArray=SQArray;
    OArray=HXArray;

    // => cast interface into object
    // -----------------------------
    harness.checkPoint("assignment widening: interface into object");
    O=IEqualSides;
    O=IFourEqual;
    O=IFourRight;;

    harness.checkPoint("assignment widening: class interface Array into object Array");
    OArray=IEqualSidesArray;
    OArray=IFourEqualArray;
    OArray=IFourRightArray;

    // => cast array into object
    // --------------------------
    harness.checkPoint("assignment widening: array into object");
    //classes
    O=HXArray;
    O=PLArray;
    O=DMArray;
    O=RCArray;
    O=SQArray;
    O=IEqualSidesArray;
    O=IFourEqualArray;
    O=IFourRightArray;
    // primitive arrays
    O=ZArray;
    O=BArray;
    O=SArray;
    O=CArray;
    O=IArray;
    O=LArray;
    O=FArray;
    O=DArray;

    // => cast array into Cloneable
    // ----------------------------
    harness.checkPoint("Cast widening: array into Cloneable");
    Cloneable Cl;
    //classes
    Cl =HXArray;
    Cl =PLArray;
    Cl =DMArray;
    Cl =RCArray;
    Cl =SQArray;
    Cl =IEqualSidesArray;
    Cl =IFourEqualArray;
    Cl =IFourRightArray;
    // primitive arrays
    Cl =ZArray;
    Cl =BArray;
    Cl =SArray;
    Cl =CArray;
    Cl =IArray;
    Cl =LArray;
    Cl =FArray;
    Cl =DArray;

    // => cast array into java.io.Serializable
    // ----------------------------------------
    harness.checkPoint("assignment widening: array into Serializable");
    //classes
    Serializable Sl;
    Sl =HXArray;
    Sl =PLArray;
    Sl =DMArray;
    Sl =RCArray;
    Sl =SQArray;
    Sl =IEqualSidesArray;
    Sl =IFourEqualArray;
    Sl =IFourRightArray;
    // primitive arrays
    Sl =ZArray;
    Sl =BArray;
    Sl =SArray;
    Sl =CArray;
    Sl =IArray;
    Sl =LArray;
    Sl =FArray;
    Sl =DArray;
	}
	
/**********************************************************************************************************************
* Class widening through method invocation
* => class to superclass
* => class to implemented interface
* => sub-interface to base interface
* => null type to class, interface, array...
* => class into object
* => array into object
* => array into Cloneable
* => array into java.io.Serializable
*/
	
	public void	testInvocationWidening()
	{

    // => cast class to superclass
    // ---------------------------
    harness.checkPoint("invocation widening: class to super class");
    PL=build45Diamond(2);
    harness.check(PL.type,"Dm");
    PL=buildHarmonicRectangle(3);
    harness.check(PL.type,"Rc");
    PL=buildSquare(4);
    harness.check(PL.type,"Sq");
    // not permitted, throws a compile-time error:
    //PL=(Parallellogram)HX;
    //harness.check(PL.type,"Hx");

    harness.checkPoint("invocation widening: class Array to super class Array");
    PLArray=build45Diamonds(2);
    harness.check(PLArray[0].type,"Dm");
    PLArray=buildHarmonicRectangles(3);
    harness.check(PLArray[0].type,"Rc");
    PLArray=buildSquares(4);
    harness.check(PLArray[0].type,"Sq");
    // not permitted, throws a compile-time error:
    //PLArray=(Parallellogram[])HXArray;
    //harness.check(PLArray[0].type,"Hx");


    // => cast class to implemented interface
    // --------------------------------------
    harness.checkPoint("Invocation widening: class to implemented interface");
    IFourEqual=build45Diamond(2);
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=buildHarmonicRectangle(3);
    harness.check(IFourRight.getSurfaceFormula(),"base x height");
    IFourEqual=buildSquare(4);
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=buildSquare(5);
    harness.check(IFourRight.getSurfaceFormula(),"square side");

    harness.checkPoint("Invocation widening: class Array to implemented interface Array");
    IFourEqualArray=build45Diamonds(2);
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=buildHarmonicRectangles(3);
    harness.check(IFourRightArray[0].getSurfaceFormula(),"base x height");
    IFourEqualArray=buildSquares(4);
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=buildSquares(5);
    harness.check(IFourRightArray[0].getSurfaceFormula(),"square side");




    // => cast sub-interface to base interface
    // ----------------------------------------
    harness.checkPoint("Invocation widening: sub interface to base interface");
    IEqualSides=buildEqualsidedParallellogram(3);
    harness.check(IEqualSides.getLengthFormula(),"side x 4");

    harness.checkPoint("CInvocation widening: sub interface to base interface");
    IEqualSidesArray=buildEqualsidedParallellograms(1);
    harness.check(IEqualSidesArray[0].getLengthFormula(),"side x 4");


/*
    // => cast null type to class, interface, array...
    // ------------------------------------------------
    harness.checkPoint("Cast widening: null type to class, interface, array..");
    String nulltype;

    // class
    try
    {
      HX=(Hexagon)null; //casting is allowed
      harness.check(true);
      nulltype=HX.type;//accessing should throw a null pointer exception
      harness.fail("null type to class accessing data not failed");
      harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    // interface
    try
    {
      IFourRight=(FourRightAngles)null; //casting is allowed
      harness.check(true);
      nulltype=IFourRight.Prequisition;//accessing should throw a null pointer exception
      harness.fail("null type to interface accessing data not failed");
      harness.check(nulltype,"four right angles");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    // array
    try
    {
      HXArray=(Hexagon[])null; //casting is allowed
      harness.check(true);
      nulltype=HXArray[0].type;//accessing should throw a null pointer exception
      harness.fail("null type to array accessing data not failed");
      harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

 */
    // => cast class into object
    // -------------------------
    harness.checkPoint("Invocation widening: class into object");
    O=build45Diamond(2);
    //harness.check(O.type,"Dm");
    O=buildHarmonicRectangle(3);
    //harness.check(O.type,"Rc");
    O=buildSquare(4);
    //harness.check(O.type,"SQ");

    harness.checkPoint("Invocation widening: class Array into object Array");
    OArray=build45Diamonds(2);
    //harness.check(OArray[0].type,"Dm");
    OArray=buildHarmonicRectangles(3);
    //harness.check(OArray[0].type,"Rc");
    OArray=buildSquares(4);
    //harness.check(OArray[0].type,"SQ");

    // => cast interface into object
    // -----------------------------
    harness.checkPoint("Invocation widening: interface into object");
    O=buildEqualsidedParallellogram(3);

    harness.checkPoint("Invocation widening: class interface Array into object Array");
    OArray=buildEqualsidedParallellograms(1);
    //harness.check(OArray[0].getSurfaceFormula(),"square side");

    // => cast array into object
    // --------------------------
    harness.checkPoint("Invocation widening: array into object");
    //classes
    O=build45Diamonds(1);
    O=buildHarmonicRectangles(2);
    O=buildSquares(3);
    O=buildEqualsidedParallellograms(4);
    // primitive arrays
    O=buildZArray(5);
    O=buildBArray(6);
    O=buildSArray(7);
    O=buildCArray(8);
    O=buildIArray(9);
    O=buildLArray(10);
    O=buildFArray(11);
    O=buildDArray(12);

    // => cast array into Cloneable
    // ----------------------------
    harness.checkPoint("invocation widening: array into Cloneable");
    Cloneable Cl;
    //classes
    Cl=build45Diamonds(1);
    Cl=buildHarmonicRectangles(2);
    Cl=buildSquares(3);
    Cl=buildEqualsidedParallellograms(4);
    // primitive arrays
    Cl=buildZArray(5);
    Cl=buildBArray(6);
    Cl=buildSArray(7);
    Cl=buildCArray(8);
    Cl=buildIArray(9);
    Cl=buildLArray(10);
    Cl=buildFArray(11);
    Cl=buildDArray(12);

    // => cast array into java.io.Serializable
    // ----------------------------------------
    harness.checkPoint("Invocation widening: array into Serializable");
    //classes
    Serializable Sl;
    //classes
    Sl=build45Diamonds(1);
    Sl=buildHarmonicRectangles(2);
    Sl=buildSquares(3);
    Sl=buildEqualsidedParallellograms(4);
    // primitive arrays
    Sl=buildZArray(5);
    Sl=buildBArray(6);
    Sl=buildSArray(7);
    Sl=buildCArray(8);
    Sl=buildIArray(9);
    Sl=buildLArray(10);
    Sl=buildFArray(11);
    Sl=buildDArray(12);

	}

/**********************************************************************************************************************
* the functions used : build diamond, rectangle, square.. and array of them
*/
  private Diamond     build45Diamond(int side){return new Diamond(side, 45);}
  private Diamond[]   build45Diamonds(int side)
    {Diamond[] diamonds = new Diamond[3];
    diamonds[0]= build45Diamond(side);
    diamonds[1]= build45Diamond(side*2);
    diamonds[2]= build45Diamond(side*3);
    return diamonds;}

  private Rectangle   buildHarmonicRectangle(int side){return new Rectangle(side,(int)(side*0.707));}
  private Rectangle[] buildHarmonicRectangles(int side)
    {Rectangle[] rectangles = new Rectangle[3];
    rectangles[0]= buildHarmonicRectangle(side);
    rectangles[1]= buildHarmonicRectangle(side*2);
    rectangles[2]= buildHarmonicRectangle(side*3);
    return rectangles;}

  private Square      buildSquare(int side){return new Square(side);}
  private Square[]    buildSquares(int side)
    {Square[] squares = new Square[3];
    squares[0]= buildSquare(side);
    squares[1]= buildSquare(side*2);
    squares[2]= buildSquare(side*3);
    return squares;}
	
	private FourEqualSides    buildEqualsidedParallellogram(int side) {return((FourEqualSides) build45Diamond(side));}
	private FourEqualSides[]  buildEqualsidedParallellograms(int side)
    {FourEqualSides[] equalsides = new FourEqualSides[3];
    equalsides[0]= buildEqualsidedParallellogram(side);
    equalsides[1]= buildEqualsidedParallellogram(side*2);
    equalsides[2]= buildEqualsidedParallellogram(side*3);
	  return equalsides;}
	
	//private null buildNullClass() {return null;}
	
/**********************************************************************************************************************
* the functions used : build array of primitives
*/
	private boolean[] buildZArray(int size)
  	{boolean[] primitives = new boolean[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?true:false;
  	return primitives;}
	private byte[] buildBArray(int size)
  	{byte[] primitives = new byte[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Byte.MIN_VALUE:Byte.MAX_VALUE;
  	return primitives;}
	private short[] buildSArray(int size)
  	{short[] primitives = new short[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Short.MIN_VALUE:Short.MAX_VALUE;
  	return primitives;}
	private char[] buildCArray(int size)
  	{char[] primitives = new char[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Character.MIN_VALUE:Character.MAX_VALUE;
  	return primitives;}
	private int[] buildIArray(int size)
  	{int[] primitives = new int[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Integer.MIN_VALUE:Integer.MAX_VALUE;
  	return primitives;}
	private long[] buildLArray(int size)
  	{long[] primitives = new long[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Long.MIN_VALUE:Long.MAX_VALUE;
  	return primitives;}
	private float[] buildFArray(int size)
  	{float[] primitives = new float[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Float.MIN_VALUE:Float.MAX_VALUE;
  	return primitives;}
	private double[] buildDArray(int size)
  	{double[] primitives = new double[size];
  	for(int i=0; i<size; i++)
  	  primitives[i]=(i%2==0)?Double.MIN_VALUE:Double.MAX_VALUE;
  	return primitives;}
	
	
/**********************************************************************************************************************
* Class widening through casting:
* => cast class to superclass
* => cast class to implemented interface
* => cast sub-interface to base interface
* => cast null type to class, interface, array...
* => cast class into object
* => cast array into object
* => cast array into Cloneable
* => cast array into java.io.Serializable

*/
	public void	testCastingWidening()
	{
    DM=new Diamond(5,45);
    DMArray=new Diamond[3];
    DMArray[0]=DM;

    RC=new Rectangle(5,7);
    RCArray=new Rectangle[3];
    RCArray[0]=RC;

    SQ=new Square(6);
    SQArray=new Square[3];
    SQArray[0]=SQ;

    HX=new Hexagon();
    HXArray=new Hexagon[3];

    // => cast class to superclass
    // ---------------------------
    harness.checkPoint("Cast widening: class to super class");
    PL=(Parallellogram)DM;
    harness.check(PL.type,"Dm");
    PL=(Parallellogram)RC;
    harness.check(PL.type,"Rc");
    PL=(Parallellogram)SQ;
    harness.check(PL.type,"Sq");
    // not permitted, throws a compile-time error:
    //PL=(Parallellogram)HX;
    //harness.check(PL.type,"Hx");

    harness.checkPoint("Cast widening: class Array to super class Array");
    PLArray=(Parallellogram[])DMArray;
    harness.check(PLArray[0].type,"Dm");
    PLArray=(Parallellogram[])RCArray;
    harness.check(PLArray[0].type,"Rc");
    PLArray=(Parallellogram[])SQArray;
    harness.check(PLArray[0].type,"Sq");
    // not permitted, throws a compile-time error:
    //PLArray=(Parallellogram[])HXArray;
    //harness.check(PLArray[0].type,"Hx");


    // => cast class to implemented interface
    // --------------------------------------
    harness.checkPoint("Cast widening: class to implemented interface");
    IFourEqual=(FourEqualSides)DM;
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=(FourRightAngles)RC;
    harness.check(IFourRight.getSurfaceFormula(),"base x height");
    // not permitted, throws a compile-time error:
    //IFourEqual=(FourEqualsides)RC; // rectange does not implement equal sides
    IFourEqual=(FourEqualSides)SQ;
    harness.check(IFourEqual.getLengthFormula(),"side x 4");
    IFourRight=(FourRightAngles)SQ;
    harness.check(IFourRight.getSurfaceFormula(),"square side");

    harness.checkPoint("Cast widening: class Array to implemented interface Array");
    IFourEqualArray=(FourEqualSides[])DMArray;
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=(FourRightAngles[])RCArray;
    harness.check(IFourRightArray[0].getSurfaceFormula(),"base x height");
    // not permitted, throws a compile-time error:
    //IFourEqualArray=(FourEqualsides[])RC; // rectange does not implement equal sides
    IFourEqualArray=(FourEqualSides[])SQArray;
    harness.check(IFourEqualArray[0].getLengthFormula(),"side x 4");
    IFourRightArray=(FourRightAngles[])SQArray;
    harness.check(IFourRightArray[0].getSurfaceFormula(),"square side");



    // => cast sub-interface to base interface
    // ----------------------------------------
    harness.checkPoint("Cast widening: sub interface to base interface");
    //iFourEqual== //already implemented above
    IEqualSides=(EqualSides)IFourEqual;
    harness.check(IEqualSides.getLengthFormula(),"side x 4");
    // not permitted, throws a compile-time error:
    //IEqualSides=(EqualSides)IFourEqual;

    harness.checkPoint("Cast widening: sub interface to base interface");
    //iFourEqual== //already implemented above
    IEqualSidesArray=(EqualSides[])IFourEqualArray;
    harness.check(IEqualSidesArray[0].getLengthFormula(),"side x 4");
    // not permitted, throws a compile-time error:
    //IEqualSides=(EqualSides)IFourEqual;



    // => cast null type to class, interface, array...
    // ------------------------------------------------
    harness.checkPoint("Cast widening: null type to class, interface, array..");
    String nulltype;

    // class
    try
    {
      HX=(Hexagon)null; //casting is allowed
      harness.check(true);
      nulltype=HX.type;//accessing should throw a null pointer exception
      harness.fail("null type to class accessing data not failed");
      //harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    // interface
    try
    {
      IFourRight=(FourRightAngles)null; //casting is allowed
      harness.check(true);
      nulltype=FourRightAngles.Prequisition;//casting should provide an imlicit variable
     // harness.fail("null type to interface accessing data not failed");
      harness.check(nulltype,"four right angles");
      nulltype=IFourRight.getSurfaceFormula();//virtual function, not defined by NULL class
      harness.fail("null type to interface accessing virtual data not failed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    // array
    try
    {
      HXArray=(Hexagon[])null; //casting is allowed
      harness.check(true);
      nulltype=HXArray[0].type;//accessing should throw a null pointer exception
      harness.fail("null type to array accessing data not failed");
      harness.check(nulltype,"HX");
    }
    catch(Exception e)
    {
      harness.check(true);
    }


    // => cast class into object
    // -------------------------
    harness.checkPoint("Cast widening: class into object");
    O=(Object)DM;
    //harness.check(O.type,"Dm");
    O=(Object)RC;
    //harness.check(O.type,"Rc");
    O=(Object)SQ;
    //harness.check(O.type,"SQ");
    O=(Object)HX;
    //harness.check(O.type,"Hx");

    harness.checkPoint("Cast widening: class Array into object Array");
    OArray=(Object[])DMArray;
    //harness.check(OArray[0].type,"Dm");
    OArray=(Object[])RCArray;
    //harness.check(OArray[0].type,"Rc");
    OArray=(Object[])SQArray;
    //harness.check(OArray[0].type,"SQ");
    OArray=(Object[])HXArray;
    //harness.check(OArray[0].type,"Hx");

    // => cast interface into object
    // -----------------------------
    harness.checkPoint("Cast widening: interface into object");
    O=(Object)IEqualSides;
    //harness.check(O.getLengthFormula(),"side x 4");
    O=(Object)IFourEqual;
    //harness.check(O.getLengthFormula(),"side x 4");
    O=(Object)IFourRight;;
    //harness.check(O.getSurfaceFormula(),"square side");

    harness.checkPoint("Cast widening: class interface Array into object Array");
    OArray=(Object[])IEqualSidesArray;
    //harness.check(OArray[0].getLengthFormula(),"side x 4");
    OArray=(Object[])IFourEqualArray;
    //harness.check(OArray[0].getLengthFormula(),"side x 4");
    OArray=(Object[])IFourRightArray;
    //harness.check(OArray[0].getSurfaceFormula(),"square side");

    // => cast array into object
    // --------------------------
    harness.checkPoint("Cast widening: array into object");
    //classes
    O=(Object)HXArray;
    O=(Object)PLArray;
    O=(Object)DMArray;
    O=(Object)RCArray;
    O=(Object)SQArray;
    O=(Object)IEqualSidesArray;
    O=(Object)IFourEqualArray;
    O=(Object)IFourRightArray;
    // primitive arrays
    O=(Object)ZArray;
    O=(Object)BArray;
    O=(Object)SArray;
    O=(Object)CArray;
    O=(Object)IArray;
    O=(Object)LArray;
    O=(Object)FArray;
    O=(Object)DArray;

    // => cast array into Cloneable
    // ----------------------------
    harness.checkPoint("Cast widening: array into Cloneable");
    Cloneable Cl;
    //classes
    Cl = (Cloneable)HXArray;
    Cl = (Cloneable)PLArray;
    Cl = (Cloneable)DMArray;
    Cl = (Cloneable)RCArray;
    Cl = (Cloneable)SQArray;
    Cl = (Cloneable)IEqualSidesArray;
    Cl = (Cloneable)IFourEqualArray;
    Cl = (Cloneable)IFourRightArray;
    // primitive arrays
    Cl = (Cloneable)ZArray;
    Cl = (Cloneable)BArray;
    Cl = (Cloneable)SArray;
    Cl = (Cloneable)CArray;
    Cl = (Cloneable)IArray;
    Cl = (Cloneable)LArray;
    Cl = (Cloneable)FArray;
    Cl = (Cloneable)DArray;

    // => cast array into java.io.Serializable
    // ----------------------------------------
    harness.checkPoint("Cast widening: array into Serializable");
    //classes
    Serializable Sl;
    Sl = (Serializable)HXArray;
    Sl = (Serializable)PLArray;
    Sl = (Serializable)DMArray;
    Sl = (Serializable)RCArray;
    Sl = (Serializable)SQArray;
    Sl = (Serializable)IEqualSidesArray;
    Sl = (Serializable)IFourEqualArray;
    Sl = (Serializable)IFourRightArray;
    // primitive arrays
    Sl = (Serializable)ZArray;
    Sl = (Serializable)BArray;
    Sl = (Serializable)SArray;
    Sl = (Serializable)CArray;
    Sl = (Serializable)IArray;
    Sl = (Serializable)LArray;
    Sl = (Serializable)FArray;
    Sl = (Serializable)DArray;

	}
	
/**********************************************************************************************************************
* Class narrowing through casting
*  => Cast superclass into extended class
*  => Cast nonfinal class into non-impemented interface
*  => ( Casting class into impemented interface is widening)
*  => Casting final class into non-impemented interface  causes compiler error
*
*  => Cast interface into nonfinal implementing class
*  => Cast interface into nonfinal non-implementing class
*  => Cast interface into final implementing class
*  => Casting interface into final non-implementing class causes compiler error
*  => Cast interface into other non-base interface
*  => Casting interface into other non-base interface with competing functions causes compioer error
*  => (Cast extending interface into base inter  face is widening)
*
*  => Cast object into class
*  => Cast object into interface
*  => Cast object into array
*/

final Rectangle FRC=new Rectangle(3,4);
final Rectangle[] FRCARRAY=new Rectangle[3];
//FRCARRAY[0]=FRC;
	
	public void	testCastingNarrowing()
	{

    String formula;
//  => Cast superclass into extended class
    //  ---------------------------------------------------------------------------
    harness.checkPoint(" Narrowing: superclass into extended class");
    PL=new Parallellogram(3,4,45);
    PLArray=new Parallellogram[3];
    PLArray[0]=PL;
    //narrowing pure base class into extended
    try
    {
      RC=(Rectangle)PL;// run-time error: promoting a pure base class into derived class not allowed
      harness.fail("promoting a pure base class into a derived class should not be allowed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    //narrowing pure base class array into extended array
    try
    {
      RCArray=(Rectangle[])PLArray;
      harness.fail("promoting a pure base class array into a derived class array should not be allowed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    PL=new Square(3);
    PLArray=new Square[3]; //Parallellogram[3];
    PLArray[0]=PL;
    //narrowing exteded assigned base class into extended class
    try
    {
    RC=(Rectangle)PL; //this is not undeserved promotion: the class already WAS a disguised rectangle
    harness.check(RC.type,"Sq","e");
    harness.check(RC.getSurfaceFormula(),"square side","f");
    }
    catch(Exception e)
    {
      harness.check(false,"unable to cast Parallellogram into Rectangle");
    }
    //narrowing exteded assigned base class array into extended array
    try
    {
    RCArray=(Rectangle[])PLArray;
    harness.check(RCArray[0].type,"Sq","g");
    harness.check(RCArray[0].getSurfaceFormula(),"square side","h");
    }
    catch(Exception e)
    {
      harness.check(false,"unable to cast Parallellogram array into Rectangle array");
    }

    //  => Cast nonfinal class into non-impemented interface
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: nonfinal class into non-implemented interface");
    PL=new Parallellogram(3,4,45);
    PLArray=new Parallellogram[3];
    PLArray[0]=PL;
    //narrowing pure class into non-implemented interface
    try
    {
      IEqualSides=(EqualSides)PL;   //should throw a ClassCastException:
      harness.fail("Casting class into non-supported interface should not be allowed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    //narrowing pure class array into non-implemented interface array
    try
    {
      IEqualSidesArray=(EqualSides[])PLArray;
      harness.fail("Casting class array into non-supported interface array should not be allowed");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    //narrowing base-class assigned inplementing class class into non-implemented interface
    PL=new Square(3);
    PLArray=new Square[3]; //Square[3];
    PLArray[0]=PL;
    try
    {
    IEqualSides=(EqualSides)PL; // allowed because the 'real'PL secretly also implements equal sides
    harness.check(IEqualSides.getLengthFormula(),"side x 4");
    }
    catch(Exception e)
    {
      harness.fail("thrown exception casting (square)PL into equalsides");
    }
    //narrowing base-class assigned inplementing array class into non-implemented interface array
    try
    {
    IEqualSidesArray=(EqualSides[])PLArray;
    harness.check(IEqualSidesArray[0].getLengthFormula(),"side x 4");
    }
    catch(Exception e)
    {
      harness.fail("thrown exception casting (square)PL array into equalsides array");
    }

    //  => ( Casting class into impemented interface is widening)
    //  ---------------------------------------------------------------------------
    harness.checkPoint("(Narrowing: Casting class into impemented interface is widening)");

    //  => Casting final class into non-impemented interface  causes compiler error
    //  ---------------------------------------------------------------------------
    harness.checkPoint("(Narrowing: Casting final class into non-impemented interface  causes compiler error)");
    try
    {
      IEqualSides=(EqualSides)FRC; //causes compiler error
      harness.fail("Casting final class into non-impemented interface should cause error");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    //IFourRight=(FourRightAngles)FRC; //implemented interface is widening
    //harness.check(IFourRight.getSurfaceFormula(),"base x height");
    FRCARRAY[0]=FRC;
    try
    {
      IEqualSidesArray=(EqualSides[])FRCARRAY; //causes compiler error
      harness.fail("Casting final class array into non-impemented interface should cause error");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    //IFourRightArray=(FourRightAngles[])FRCARRAY; //implemented interface is widening
    //harness.check(IFourRightArray[0].getSurfaceFormula(),"base x height");


    //  => Cast interface into nonfinal implementing class
    //  ---------------------------------------------------------------------------
    // (at run-time, only casting an interface into an Object is allowed
    harness.checkPoint("Narrowing: interface into nonfinal implementing class");
    IEqualSides=new Square(3); //interface from instance
    IEqualSidesArray = new EqualSides[3];
    IEqualSidesArray[0]=IEqualSides;
    try
    {
      HX = (Hexagon)IEqualSides;  // run-time exception, no means to widen a square into a hexagon
      harness.fail("should be impossible to turn a square into a hexagon");
    }
    catch(Exception e)
    {
      harness.check(true,"a");
    }

    try
    {
      HXArray = (Hexagon[])IEqualSidesArray; // run-time exception, no means to widen a square array into a hexagon array
      harness.fail("Only allowed to cast interface arrays into object arrays, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true,"b");
    }


    IFourRight=new Square(3); //interface from instance
    IFourRightArray = new FourRightAngles[3];
    IFourRightArray[0]=IFourRight;
    try
    {
      RC = (Rectangle)IFourRight;  // must pass, square can be widened into rectangle
      harness.check(RC.type,"Sq");
      harness.check(RC.getSurfaceFormula(),"square side");
    }
    catch(Exception e)
    {
      harness.fail("error casting interface from Square into rectangle");
    }

    try
    {
      RC = (Rectangle)IFourRightArray[0]; // must pass, array[0] is a square
      harness.check(RC.type,"Sq");
      harness.check(RC.getSurfaceFormula(),"square side");
    }
    catch(Exception e)
    {
      harness.fail("error casting interface array from Square array into rectangle array");
    }

    try
    {
      RCArray= (Rectangle[])IFourRightArray;  // must fail, Interface(array) can only be cast into Object(array)
      harness.fail("Only allowed to cast interfaces into objects, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true,"c");
    }

    //  => Cast interface into nonfinal non-implementing class
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: interface into nonfinal non-implementing class");

    IFourRight=new Square(5); //interface
    IFourRightArray=new FourRightAngles[3]; //interface array
    IFourRightArray[0]=IFourRight; //member
    // non-implementing base class
    try
    {
      HX = (Hexagon)IFourRight;  //must fail, square can ot be turned into hexagon
      harness.fail("should be impossible to turn a square into a hexagon");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    //non-imlementing base class array
    try
    {
      HXArray = (Hexagon[])IFourRightArray;
      harness.fail("Only allowed to cast interface arrays into object arrays, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    // interface doesn't suport class function, but widened interface class does
    IFourEqual=new Square(5); //interface
    IFourEqualArray=new FourEqualSides[3]; //interface array
    IFourEqualArray[0]=IFourEqual; //member
    try
    {
      RC = (Rectangle)IFourEqual;// though rectangle doesn't implement four equal sides, square can be widened into rectqangle
      harness.check(RC.type,"Sq");
      formula =RC.getSurfaceFormula(); // FourEqualSides doesn't support surface formula, but Rectangle and Square do
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square interface into rectangle");
    }

    try
    {
      RC = (Rectangle)IFourEqualArray[0];// though rectangle doesn't implement four equal sides, square can be widened into rectqangle
      harness.check(RC.type,"Sq");
      formula =RC.getSurfaceFormula(); // FourEqualSides doesn't support surface formula, but Rectangle and Square do
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square interface into rectangle");
    }
    //id for arrays

    try
    {
      RCArray = (Rectangle[])IFourRightArray;
      harness.fail("Only allowed to cast interface arrays into object arrays, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
/*
    //  => Cast interface into final implementing class
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: interface into final implementing class");
   IFourRight=new FourRightAngles(); //interface
    try
    {
      FRC = (Rectangle)IFourRight;
      harness.check(true);
      formula =FRC.getSurfaceFormula();
      harness.fail("getAngle() passed exception");
    }
    catch(Exception e)
    {
      harness.check(true);
//      harness.fail("caught exception");
    }


    IFourRight=new Square(5); //interface
    try
    {
      FRC = (Rectangle)IFourRight;
      harness.check(true);
      formula =FRC.getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception");
    }


    //  => Casting interface into final non-implementing class causes compiler error
    //  ---------------------------------------------------------------------------
    harness.checkPoint("(Narrowing: Casting interface into final non-implementing class causes compiler error");
    IEqualSides=new Hexagon(); //interface
    FRC = (Rectangle)IEqualSides;    //causes error
    IEqualSides=new Square(4); //interface
    FRC = (Rectangle)IEqualSides;    //causes error
*/


    //  => Cast interface into other non-base interface
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: interface into other non-base interface");
    IEqualSides = new Hexagon();
    IEqualSidesArray = new Hexagon[3];
    IEqualSidesArray[0] = new Hexagon();
    try
    {
      IFourRight = (FourRightAngles)IEqualSides;//should fail, hexagon doesn't support right angles interface
      harness.fail("must be impossible to turn hexagon into right angles interface");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    try
    {
      IFourRightArray = (FourRightAngles[])IEqualSidesArray;
      harness.fail("Only allowed to cast interface arrays into object arrays, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true);
    }


    IEqualSides = new Square(5); //interface
    IEqualSidesArray = new EqualSides[3]; //interface
    IEqualSidesArray[0] = IEqualSides; //interface
    try
    {
      IFourRight = (FourRightAngles)IEqualSides; //must pass, square supports equal sides
      formula =IFourRight.getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square into right-angle interface");
    }
    try
    {
      IFourRight = (FourRightAngles)IEqualSidesArray[0]; //must pass, square supports equal sides
      formula =IFourRight.getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square into right-angle interface");
    }

    try
    {
      IFourRightArray = (FourRightAngles[])IEqualSidesArray;
      harness.fail("Only allowed to cast interface arrays into object arrays, not into discrete classes");
    }
    catch(Exception e)
    {
      harness.check(true);
    }


    //  => Casting interface into other non-base interface with competing functions causes compiler error
    //  ---------------------------------------------------------------------------
    harness.checkPoint("(Narrowing: Casting interface into other non-base interface with competing functions causes compiler error)");
    //IFourRight = new Square(5); //int FourRightAngles.getAngle()
    //RegularForm RF=(RegularForm)IFourRight; //float RegularForm.getAngle(); =>CAUSES COMPIER ERROR

    //  => (Cast extending interface into base interface is widening)
    //  ---------------------------------------------------------------------------
    harness.checkPoint("(Narrowing: extending interface into base interface is widening)");


    //  => Cast object into class
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: object into class");
    O=new Parallellogram(5,2,120);
    OArray=new Parallellogram[3];
    OArray[0]=O;
    try
    {
      RC = (Rectangle)O;  // run time error
      harness.fail("should not be possible to cast Parallellogram into rectangle");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    try
    {
      RCArray = (Rectangle[])OArray;
      harness.fail("should not be possible to cast Parallellogram array into rectangle array");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    O=new Square(5);
    OArray=new Square[3];
    OArray[0]=O;
    try
    {
      RC = (Rectangle)O; //must pass
      harness.check(RC.type, "Sq");
    }
    catch(Exception e)
    {
      harness.fail("caught exception ");
    }

    try
    {
      RCArray = (Rectangle[])OArray;
      harness.check(RCArray[0].type, "Sq");
    }
    catch(Exception e)
    {
      harness.fail("caught exception");
    }

    //  => Cast object into interface
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: object into interface");
    O=new Parallellogram(5,2,120);
    OArray=new Parallellogram[3];
    OArray[0]=O;
    try
    {
      IFourRight = (FourRightAngles)O;//should fail
      harness.fail("should not be possible to cast Parallellogram into right angle interface");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    try
    {
      IFourRightArray = (FourRightAngles[])OArray;
      harness.fail("should not be possible to cast Parallellogram array into right angle interface array");
      harness.check(true);
      formula = IFourRightArray[0].getSurfaceFormula();
      harness.fail("should throw exception");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    O=new Square(5);
    OArray=new Square[3];
    OArray[0]=O;
    try
    {
      IFourRight = (FourRightAngles)O;  //square supports right angle interface
      formula = IFourRight.getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square into right-angles interface");
    }
    OArray=new Square[3];
    OArray[0]=new Square(5);
    try
    {
      IFourRightArray = (FourRightAngles[])OArray;
      formula = IFourRightArray[0].getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square array into right-angles interface array");
    }

    //  => Cast object into array
    //  ---------------------------------------------------------------------------
    harness.checkPoint("Narrowing: object into array");
    PLArray=new Parallellogram[3];
    PLArray[0]=new Parallellogram(5,2,120);
    O=(Object)PLArray;
    //class array
    try
    {
      RCArray = (Rectangle[])O;  // should throw exception
      harness.fail("should not be possible to cast Parallellogram array into rectangle array");
    }
    catch(Exception e)
    {
      harness.check(true);
    }
    //interface array
    try
    {
      IFourRightArray = (FourRightAngles[])O; //should throw exception
      harness.fail("should not be possible to cast Parallellogram array into right angle interface array");
    }
    catch(Exception e)
    {
      harness.check(true);
    }

    SQArray = new Square[3];
    SQArray[0]=new Square(5);
    O=(Object)SQArray;
    //class array
    try
    {
      RCArray = (Rectangle[])O;
      harness.check(RCArray[0].type, "Sq");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square array into rectangle array");
    }

    //interface array
    try
    {
      IFourRightArray = (FourRightAngles[])O; //should pass
      formula = IFourRightArray[0].getSurfaceFormula();
      harness.check(formula,"square side");
    }
    catch(Exception e)
    {
      harness.fail("caught exception casting square array into right angle array");
    }

 }
	
/*  // ("Narrowing implemented interface into final class");
  private HasFourRightAngles privateFourRightAngles = new Square();
  Rectangle RFINAL = (Rectangle)privateFourRightAngles;
  //("Narrowing implemented interface array into final class array");
  private HasFourRightAngles[] privateArrayFourRightAngles = new Square[3];
  Rectangle[] RFINALARRAY = (Rectangle[])privateArrayFourRightAngles;
  //forbidden: cast not impemented interface to final class
  //RFINAL = (Rectangle)IFourEqualSides;
*/	
	
	private void testStringConversion()
	{
    /*
    =>String + reference = string +"null" if primitive = null, String+ class.toString() otherwise
    */
    String stringvalue = "StringValue:";
    String asstring = "(as String)";
    //rectangle (default Object toString function)
    Rectangle r1=new Rectangle(1,2);
    Rectangle r2=new Rectangle(3,4);
    Rectangle r0=null;
    harness.checkPoint("class reference to string");
    harness.check(stringvalue+r1,stringvalue+r1.toString() );
    harness.check(r1+asstring,r1.toString() +asstring );
    harness.check(stringvalue+r2,stringvalue+r2.toString() );
    harness.check(r2+asstring,r2.toString() +asstring );
    harness.check(stringvalue+r0,stringvalue+"null" );
    harness.check(r0+asstring,"null" +asstring );

    //rectangle (default Object toString function)
    Square s1=new Square(5);
    Square s0=null;
    harness.checkPoint("class reference to string");
    harness.check(stringvalue+s1,stringvalue+s1.toString() );
    harness.check(s1+asstring,s1.toString() +asstring );
    harness.check(stringvalue+s1,"StringValue:Square with side of 5" );
    harness.check(s1+asstring,   "Square with side of 5(as String)");
    harness.check(stringvalue+s0,stringvalue+"null" );
    harness.check(s0+asstring,"null" +asstring );
}	
/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang: conversion and casting class references");
		//conversoin has the following fields:
		//Assignment,
		//primitive widening,  (see ConversionReferences.java)
	  //primitive narrowing for constants in certain cases (see ConversionReferences.java)
		//reference widening
  	testAssignmentWidening();
		
		
		//invocation through functions
		//primitive widening (see ConversionReferences.java)
		//reference widening
		testInvocationWidening();
		
		//casting
		//primitive widening (see ConversionReferences.java)
		//complete primitive narrowing (see ConversionReferences.java)
		//reference widening
		testCastingWidening();
		//reference narrowing
		testCastingNarrowing();
		
		//String conversion
		testStringConversion();
		//numeric Promotion
		//Special case on numeric promotion conditional operator on reference type
		//testPromotionConditionalOperator();
		
		//String conversion
		//see the String +operator and the primitives tests

	}

/*********************************************************************************************************************
*
*  Test classes and interfaces for reference conversion
*
*********************************************************************************************************************/
/**********************************************************
* Interfaces
*/
public static interface FourRightAngles
{
  static String Prequisition = "four right angles";

  public int getAngle();// {return 90;}

  public String getSurfaceFormula();
}

public static interface EqualSides
{
  public String getLengthFormula();
}

public static interface FourEqualSides extends EqualSides
{
  static String Prequisition = "four equal sides";
}

public static interface RegularForm extends EqualSides
{
  public float getAngle();// {return(360.0f/sides);}
}

/**********************************************************
* classes
*/
public static class Parallellogram
{
  public String type;
  public int base;
  public int height;
  public int angle;

  public Parallellogram(int b,int h, int a)
  {
    base=b;
    height=h;
    angle=a;
    type="Pl";
  }
}

public static class Rectangle extends Parallellogram implements FourRightAngles
{
  public Rectangle(int b,int h)
  {
    super(b,h,90);
    type="Rc";
  }

  public String getSurfaceFormula() {return("base x height");};

  public int getAngle() {return angle;}
}

public static class Diamond extends Parallellogram implements FourEqualSides
{
  public Diamond(int side, int angle)
  {
    super(side, side, angle);
    type="Dm";
  }

  public String getLengthFormula() {return("side x 4");}
}

public static class Square extends Rectangle implements FourEqualSides
{
  public Square(int b)
  {
    super(b,b);
    type="Sq";
  }
  public String getSurfaceFormula() {return("square side");};
  public String getLengthFormula() {return("side x 4");}
  public String toString() {return("Square with side of "+base);}
}

public static class Hexagon implements EqualSides
{
  public String type="Hx";
  public String getLengthFormula() {return("side x 6");}
  public float getAngle() {return(120.0f);}
}













}
