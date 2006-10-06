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


/*
** $Id: testInheritance.java
**
** The Wonka kernel is software copyright by Acunia.
** Please see the file Copyright for information on it's legal use.

ABSTRACT: a series of tests on special inheritance problems as described in the chapters 8 and 15.12
of the Java Language specification
**
// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

*/

package gnu.testlet.wonka.vm;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

class ClassZero
{
  final String name = "C-0";
  static String fullname = "C - Zero";

  boolean x;
  static boolean xs;
  float y;
  float z;

  //constructors
  public ClassZero() {reset();}
  public void reset()
  {
    this.xs=true;
    this.x =false;
    this.y =0.0f;
    this.z =0.0f;
  }

  public ClassZero(boolean xs, boolean x, float y, float z) {set(x,y,z);  this.xs=xs;}
  public ClassZero(boolean x, float y, float z) {set(x,y,z);}
  public void set(boolean x, float y, float z)
  {
    xs = true;
    this.x=x;
    this.y=y;
    this.z=z;
  }

  public ClassZero(ClassZero zero) {importZero(zero);}
  public void importZero(ClassZero zero)
  {
//System.out.println("Importing ClassZero: "+zero.toString());
    this.x=zero.x;
    this.y=zero.y;
    this.z=zero.z;
  }


  //class functions overridden by ClassTwo, same return type
  static String getXS()             {return getDescription(xs);}
  public String getX()              {return getDescription(x);}

  static void setXS(float newxs)  {xs = (newxs>0);}
  public void setX(float newx)    {x  = (newx>0); }
  static void setXS(int newxs)    {xs = (newxs>0);}
  public void setX(int newx)      {x  = (newx>0); }
  static void setXS(boolean newxs){xs = newxs;}
  public void setX(boolean newx)  {x  = newx; }

  //descriptions for primitives of this and derived classes
  static String getDescription(boolean b)   {return ("Z:"+b);}
  static String getDescription(int i)       {return ("I:"+i);}
  static String getDescription(float f)     {return ("F:"+f);}
  static String getDescription(String s)    {return ("Ljava.lang.String:<"+s+">");}
  static String getDescription(ClassZero c0){return c0.toString();}

  //descriptions for this and derived classes by variables
  static String describe(String item, boolean is, boolean i, float j, float k)
                                                                          {return (item+" < Xs=Z:"+is+" X=Z:"+i+" Y=F:"+j+" z=F:"+k+">");}
  static String describe(String item, int is, int i, float j, float k)    {return (item+" < Xs=I:"+is+" X=I:"+i+" Y=F:"+j+" z=F:"+k+">");}
  static String describe(String item, float is, float i, float j, float k){return (item+" < Xs=F:"+is+" X=F:"+i+" Y=F:"+j+" z=F:"+k+">");}

  //to String functions
  static String describeZero(ClassZero zero) {return zero.describeZero();}
  public String describeZero(){return describe(fullname, xs, x, y, z); }
  public String toString()    {return describeZero();}
}

class ClassOne extends ClassZero
{
  final String name = "C-1";
  static String fullname = "C - One"; //already defined as same type & static
  static int xs;  //defined as other type & static
  int x ;         //defined as other value
  float y;        //double defined
  //float z       //inherited

  //constructors
  public ClassOne() {reset();}
  public void reset()
  {
    super.reset();
    this.xs=10;
    this.x =0;
    this.y =1.0f;
    this.z =0.0f;
  }

  public ClassOne(int xs, int x, float y, float z) {set(x,y,z);  this.xs=xs;}
  public ClassOne(int x, float y, float z) {set(x,y,z);}
  public void set(int x, float y, float z)
  {
    this.x=x;
    this.y=y;
    this.z=z;
  }

  public ClassOne(ClassOne one) {importOne(one);}
  public void importOne(ClassOne one)
  {
    this.x=one.x;
    this.y=one.y;
    this.z=one.z;
  }

  //class functions overridden by ClassTwo, same return type
  static String getXS()             {return getDescription(xs);}
  public String getSuperXS()        {return getDescription(super.xs);}
  public String getX()              {return getDescription(x);}
  public String getSuperX()         {return getDescription(super.x);}

  //overriding the superclasses
  static void setXS(float newxs)  {xs = (int)newxs;}
  public void setX(float newx)    {x  = (int)newx; }
  static void setXS(int newxs)    {xs = newxs;}
  public void setX(int newx)      {x  = newx; }
  static void setXS(boolean newxs){xs = (newxs)?1:-1;}
  public void setX(boolean newx)  {x  = (newx )?1:-1;}

  //to String functions
  static String describeOne(ClassOne one) {return one.describeOne();}
  public String describeOne() {return describe(fullname, xs, x, y, z); }
  public String toString()    {return describeOne();}
}

class ClassTwo extends ClassOne implements IhOne
{
  static int classfield;

  final String name = "C-2";
  static String fullname = "C - Two"; //already defined as same type & static
  static float xs;  //already defined as other type & static
  float x;          //already defined as other type
  float y;          //already defined as same type
  // float z;       //inherited

  //constructors
  public ClassTwo() {reset();}
  public void reset()
  {
    super.reset();
    this.xs=10.0f;
    this.x =0.0f;
    this.y =2.0f;
    this.z =0.0f;
  }

  public ClassTwo(float xs, float x, float y, float z) {set(x,y,z);  this.xs=xs;}
  public ClassTwo(float x, float y, float z) {set(x,y,z);}
  public void set(float x, float y, float z)
  {
    this.x=x;
    this.y=y;
    this.z=z;
  }

  public ClassTwo(ClassTwo two) {importTwo(two);}
  public void importTwo(ClassTwo two)
  {
    this.x=two.x;
    this.y=two.y;
    this.z=two.z;
  }

  //access super variables
  static String getXS()             {return getDescription(xs);}
  public String getSuperXS()        {return getDescription(super.xs);}
  public String getX()              {return getDescription(x);}
  public String getSuperX()         {return getDescription(super.x);}

  //access interface variables
  public String getIhZeroXS()       {return getDescription(IhZero.xs);}
  public String getIhOneXS()        {return getDescription(IhOne.xs);}
  public String getIhZeroX()        {return getDescription(IhZero.x);}
  public String getIhOneX()         {return getDescription(IhOne.x);}
  //access super functions
  public String getXSClassOne()     {return super.getDescription(xs);}
  public String getSuperXSClassOne(){return super.getDescription(super.xs);}
  public String getXClassOne()      {return super.getDescription(x);}
  public String getSuperXClassOne() {return super.getDescription(super.x);}

  //overriding the superclasses
  static void setXS(float newxs)  {xs = newxs;}
  public void setX(float newx)    {x  = newx; }
  static void setXS(int newxs)    {xs = (float)newxs;}
  public void setX(int newx)      {x  = (float)newx; }
  static void setXS(boolean newxs){xs = (newxs)?1.0f:-1.0f;}
  public void setX(boolean newx)  {x  = (newx )?1.0f:-1.0f;}

  //final and static members for static field access
  final  ClassTwo zeroElement() {return new ClassTwo(0.0f, 0.0f, 0.0f); }
  static ClassTwo nullElement() {return null; }

  //to String functions
  static String describeTwo(ClassTwo two) {return two.describeTwo();}
  public String describeTwo() {return describe(fullname, xs, x, y, z); }
  public String toString()    {return describeTwo();}
}


interface IhZero
{
  static ClassZero xs = new ClassZero();
  public ClassZero x = new ClassZero(true,0.1f,0.2f);
}

interface IhOne extends IhZero
{
  static String xs = "ten";
  public String x  = "zero";
}


class InitTestOne
{
  static int get_b() {return b;}
  static int a = get_b();
  static int b=1;
}
class InitTestTwo
{
  static int b=1;
  static int a = get_b();
  static int get_b() {return b;}
}





/*********************************************************************************************************************************
*
* Main test class
*
*/

public class testInheritance implements Testlet
{
  protected static TestHarness harness;
		
  //passing of a static veriable from classinstance to classinstance
	private void testStatic()
  {
    ClassTwo test1 = new ClassTwo();
    test1.classfield = 1;
    ClassTwo test2 = new ClassTwo(0.1f, 0.2f, 0.3f);
    harness.checkPoint("Static member access through different class instances");
    //test other class instance
    harness.check(test2.classfield++,1);
    //test final static element
    harness.check(test2.zeroElement().classfield++,2);
    harness.check(test1.zeroElement().classfield++,3);
    //test null element
    harness.check(test2.nullElement().classfield++,4);
    harness.check(test1.nullElement().classfield++,5);
    //(final check 'original')
    harness.check(test1.classfield,6);
  }
	
	private void testEvaluationOrder()
  {
    harness.checkPoint("Evaluation order with static initialisers");
    harness.check(InitTestOne.a,0);
    harness.check(InitTestTwo.a,1);

    harness.checkPoint("Evaluation order with member update");
    String s="0";
    if(s.equals(s="1"))
      harness.fail("s.equals(s='1') comparing s='0' first part with s=>'1' last part should fail");
    else
      harness.check(true,"s.equals(s='1') comparing s='0' first part with s=>'1' last part returned 'not equal'");

    harness.check(s,"1","inline assignment");

    if((s="0").equals(s))
      harness.check(true,"(s='0').equals(s) : found s='0' at comparison already");
    else
      harness.fail("(s='0').equals(s) : found s='1' at comparison");
    harness.check(s,"0","inline assignment");

    int x=0;
    harness.check(x++,0,"inline assignment post-increment");
    harness.check(++x,2,"inline assignment pre-increment");
  }
	
/********************************************************
* Checking method access and inheritance
* for static and instance variables main and superclasses
* Checking method access and inheritance
* for final variables main and superclasses
*
*/
	private void testVariablesHiding()
  {
    ClassTwo test = new ClassTwo();
    harness.checkPoint("accessing superclass variables through super calls");
    harness.check(test.getXS(), ClassZero.getDescription(10.0f),"static superclass member");
    harness.check(test.getX(), ClassZero.getDescription(0.0f),"instance superclass member");
    harness.check(test.getSuperXS(), ClassZero.getDescription(10),"static superclass member");
    harness.check(test.getSuperX(), ClassZero.getDescription(0),"instance superclass member");

    harness.checkPoint("accessing superclass variables through casting");
    harness.check(ClassZero.getDescription(test.xs), ClassZero.getDescription(10.0f),"static member");
    harness.check(ClassZero.getDescription(test.x), ClassZero.getDescription(0.0f),"instance member");
    harness.check(ClassZero.getDescription(((ClassOne)test).xs), ClassZero.getDescription(10),"static superclass member");
    harness.check(ClassZero.getDescription(((ClassOne)test).x), ClassZero.getDescription(0),"instance superclass member");
    harness.check(ClassZero.getDescription(((ClassZero)test).xs), ClassZero.getDescription(true),"static superclass member");
    harness.check(ClassZero.getDescription(((ClassZero)test).x), ClassZero.getDescription(false),"instance superclass member");

    harness.checkPoint("accessing interface variables through interface calls");
    harness.check(test.getIhZeroXS() , ClassZero.getDescription(new ClassZero()),"static interface member");
    harness.check(test.getIhZeroX()  , ClassZero.getDescription(new ClassZero(true,0.1f,0.2f)),"instance interface member");
    harness.check(test.getIhOneXS(), ClassZero.getDescription("ten"),"static interface member");
    harness.check(test.getIhOneX() , ClassZero.getDescription("zero"),"instance interface member");

    harness.checkPoint("accessing interface variables through casting");
    harness.check(ClassZero.getDescription(((IhZero)test).xs), ClassZero.getDescription(new ClassZero()),"static interface member");
    harness.check(ClassZero.getDescription(((IhZero)test).x),ClassZero.getDescription(new ClassZero(true,0.1f,0.2f)),"instance interface member");
    harness.check(ClassZero.getDescription(((IhOne)test).xs), ClassZero.getDescription("ten"),"static interface member");
    harness.check(ClassZero.getDescription(((IhOne)test).x), ClassZero.getDescription("zero"),"instance interface member");
  }

/********************************************************
* Checking member access and inheritance
* for final, static and instance members
* for main and casting to superclasses
*/
	private void testMembersAccess()
  {
    ClassZero C0    = new ClassZero(true,0.1f, 0.2f);
    ClassOne  C1    = new ClassOne(1,    0.3f, 0.4f);
    ClassTwo  C2    = new ClassTwo(1.0f, 0.5f, 0.6f);

    harness.checkPoint("construction: final members");
    harness.check(C0.name,"C-0");
    harness.check(C0.name,(new ClassZero()).name);
    harness.check(C1.name,"C-1");
    harness.check(C1.name,(new ClassOne()).name);
    harness.check(C2.name,"C-2");
    harness.check(C2.name,(new ClassTwo()).name);
    harness.checkPoint("construction static members");
    //class C0
    harness.check(C0.fullname,"C - Zero");
    harness.check(C0.fullname,ClassZero.fullname);
    harness.check(C1.fullname,"C - One");
    harness.check(C1.fullname,ClassOne.fullname);
    harness.check(C2.fullname,"C - Two");
    harness.check(C2.fullname,ClassTwo.fullname);
    harness.checkPoint("construction instance members double defined");
    harness.check(C0.y,0.1f);//just set
    harness.check(C1.y,0.3f);//just set
    harness.check(C2.y,0.5f);//just set
    harness.checkPoint("construction instance members once defined");
    harness.check(C0.z,0.2f);//just set
    harness.check(C1.z,0.4f);//just set
    harness.check(C2.z,0.6f);//just set


    //Conversions through casting
    ClassZero C1to0 = (ClassZero)C1;
    ClassOne  C2to1 = (ClassOne)C2;
    ClassZero C2to0 = (ClassZero)C2;

    harness.checkPoint("conversion through casting: final members");
    harness.check(C1to0.name,"C-0");
    harness.check(C1to0.name,(new ClassZero()).name);

    harness.check(C2to1.name,"C-1");
    harness.check(C2to1.name,(new ClassOne()).name);
    harness.check(C2to0.name,"C-0");
    harness.check(C2to0.name,(new ClassZero()).name);

    harness.checkPoint("conversion through casting: static members");
    harness.check(C1to0.fullname,"C - Zero");
    harness.check(C1to0.fullname,ClassZero.fullname);

    harness.check(C2to1.fullname,"C - One");
    harness.check(C2to1.fullname,ClassOne.fullname);
    harness.check(C2to0.fullname,"C - Zero");
    harness.check(C2to0.fullname,ClassZero.fullname);

    harness.checkPoint("conversion through casting: instance members double defined");
    harness.check(C1to0.y,0.0f);
    harness.check(C1to0.y,(new ClassZero()).y);

    harness.check(C2to0.y,0.0f);
    harness.check(C2to0.y,(new ClassZero()).y);
    harness.check(C2to1.y,1.0f);//default value
    harness.check(C2to1.y,(new ClassOne()).y);
    harness.checkPoint("conversion through casting: construction instance members once defined");
    harness.check(C1to0.z,0.4f);//just set
    harness.check(C2to0.z,0.6f);//just set
    harness.check(C2to1.z,0.6f);//just set

    //Conversions through assignment
    C0 = C1;
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassOne");
    harness.checkPoint("conversion through (C1=>C0): final members");
    harness.check(C0.name,"C-0");
    harness.check(C0.name,(new ClassZero()).name);
    harness.checkPoint("conversion through assignment(C1=>C0): static members");
    harness.check(C0.fullname,"C - Zero");
    harness.check(C0.fullname,ClassZero.fullname);
    harness.checkPoint("conversion through assignment(C1=>C0): instance members once defined");
    harness.check(C0.y,0.0f);
    harness.check(C0.y,(new ClassZero()).y);
    harness.checkPoint("conversion through assignment(C1=>C0): construction instance members once defined");
    harness.check(C0.z,0.4f);

    C0 = C2;
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.checkPoint("conversion through assignment(C2=>C0): final members");
    harness.check(C0.name,"C-0");
    harness.check(C0.name,(new ClassZero()).name);
    harness.checkPoint("conversion through assignment(C2=>C0): static members");
    harness.check(C0.fullname,"C - Zero");
    harness.check(C0.fullname,ClassZero.fullname);
    harness.checkPoint("conversion through assignment(C2=>C0): instance members once defined");
    harness.check(C0.y,0.0f);
    harness.check(C0.y,(new ClassZero()).y);
    harness.checkPoint("conversion through assignment(C2=>C0): construction instance members once defined");
    harness.check(C0.z,0.6f);

    C1 = C2;
    harness.check(C1.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.checkPoint("conversion through assignment(C2=>C1): final members");
    harness.check(C1.name,"C-1");
    harness.check(C1.name,(new ClassOne()).name);
    harness.checkPoint("conversion through assignment(C2=>C1): static members");
    harness.check(C1.fullname,"C - One");
    harness.check(C1.fullname,ClassOne.fullname);
    harness.checkPoint("conversion through assignment(C2=>C1): instance members once defined");
    harness.check(C1.y,1.0f);
    harness.check(C1.y,(new ClassOne()).y);
    harness.checkPoint("conversion through assignment(C2=>C1): construction instance members once defined");
    harness.check(C1.z,0.6f);

  }


/********************************************************
* Checking method access and inheritance
* for static and instance function calls
* for main and casting to superclasses
*/
	private void testMethodsAccess()
  {
    ClassZero C0= new ClassZero();
    ClassOne  C1= new ClassOne();
    ClassZero C1to0= (ClassZero)C1;
    ClassTwo  C2= new ClassTwo();
    ClassOne  C2to1 = (ClassOne) C2;
    ClassZero C2to0 = (ClassZero)C2;

    /********************************************************
    * Checking method inheritance
    * public void ClassZero/ClassOne/ClassTwo .setX(float f);
    * static void ClassZero/ClassOne/ClassTwo .setXS(float f);
    */
    C0.reset();
    C1.reset();
    C2.reset();
    harness.checkPoint("class instance methods access by variable (float)");
    C0.setX(-11.9f);
    C1to0.setX(-11.9f);
    C2to0.setX(-11.9f);
    //ClassZero.setX
    harness.check(ClassZero.getDescription(C0.x),ClassZero.getDescription(false)   );
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1to0.x),ClassZero.getDescription(false)  );
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(-11)  );
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2to0.x),ClassZero.getDescription(false));
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(-11.9f));

    C1.setX(11.8f);
    C2to1.setX(11.8f);
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(11)  );
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(11.8f));

    C2.setX(-11.7f);
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(-11.7f));

    harness.checkPoint("class static methods access by variable (float)");
    C0.setXS(12.5f);
    C1to0.setXS(12.5f);
    C2to0.setXS(12.5f);
    //ClassZero.setX (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C0.xs),ClassZero.getDescription(true)   );
    harness.check(ClassZero.getDescription(C1to0.xs),ClassZero.getDescription(true)  );
    harness.check(ClassZero.getDescription(C2to0.xs),ClassZero.getDescription(true));
    //ClassOne.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(10)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(10));
    //ClassTwo.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C1.setXS(-12.6f);
    C2to1.setXS(-12.6f);
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(-12)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(-12));
    //ClassTwo.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C2.setXS(12.7f);
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(12.7f));



    /********************************************************
    * Checking method inheritance
    * public void ClassZero/ClassOne/ClassTwo .setX(int f);
    * static void ClassZero/ClassOne/ClassTwo .setXS(int f);
    */
    C0.reset();
    C1.reset();
    C2.reset();
    harness.checkPoint("class instance methods access by variable (int)");
    C0.setX(11);
    C1to0.setX(11);
    C2to0.setX(11);
    //ClassZero.setX
    harness.check(ClassZero.getDescription(C0.x),ClassZero.getDescription(true)   );
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1to0.x),ClassZero.getDescription(false)  );
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(11)  );
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2to0.x),ClassZero.getDescription(false));
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(11.0f));

    C1.setX(-12);
    C2to1.setX(-12);
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(-12)  );
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(-12.0f));

    C2.setX(13);
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(13.0f));

    harness.checkPoint("class static methods access by variable (int)");
    C0.setXS(-14);
    C1to0.setXS(-14);
    C2to0.setXS(-14);
    //ClassZero.setX (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C0.xs),ClassZero.getDescription(false)   );
    harness.check(ClassZero.getDescription(C1to0.xs),ClassZero.getDescription(false)  );
    harness.check(ClassZero.getDescription(C2to0.xs),ClassZero.getDescription(false));
    //ClassOne.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(10)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(10));
    //ClassTwo.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C1.setXS(15);
    C2to1.setXS(15);
    //ClassOne.setX (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(15)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(15));

    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C2.setXS(-16);
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(-16.0f));




    /********************************************************
    * Checking method inheritance
    * public void ClassZero/ClassOne/ClassTwo .setX(boolean f);
    * static void ClassZero/ClassOne/ClassTwo .setXS(boolean f);
    */
    C0.reset();
    C1.reset();
    C2.reset();
    harness.checkPoint("class instance methods access by variable (boolean)");
    C0.setX(true);
    C1to0.setX(true);
    C2to0.setX(true);
    //ClassZero.setX
    harness.check(ClassZero.getDescription(C0.x),ClassZero.getDescription(true)   );
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1to0.x),ClassZero.getDescription(false)  );
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(1)  );
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2to0.x),ClassZero.getDescription(false));
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(1.0f));

    C1.setX(false);
    C2to1.setX(false);
    //ClassOne.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C1.x),ClassZero.getDescription(-1)  );
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2to1.x),ClassZero.getDescription(0));
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(-1.0f));

    C2.setX(true);
    //ClassTwo.setX (instance calls to casted are applied to original
    harness.check(ClassZero.getDescription(C2.x),ClassZero.getDescription(1.0f));

    harness.checkPoint("class static methods access by variable (boolean)");
    C0.setXS(false);
    C1to0.setXS(false);
    C2to0.setXS(false);
    //ClassZero.setX (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C0.xs),ClassZero.getDescription(false)   );
    harness.check(ClassZero.getDescription(C1to0.xs),ClassZero.getDescription(false)  );
    harness.check(ClassZero.getDescription(C2to0.xs),ClassZero.getDescription(false));
    //ClassOne.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(10)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(10));
    //ClassTwo.setX not initialised (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C1.setXS(true);
    C2to1.setXS(true);
    //ClassOne.setX (static calls are applied to casted class)
    harness.check(ClassZero.getDescription(C1.xs),ClassZero.getDescription(1)  );
    harness.check(ClassZero.getDescription(C2to1.xs),ClassZero.getDescription(1));

    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(10.0f));

    C2.setXS(false);
    harness.check(ClassZero.getDescription(C2.xs),ClassZero.getDescription(-1.0f));



    /********************************************************
    * Checking method inheritance through assignation
    * public String ClassZero/ClassOne/ClassTwo .getX(boolean f);
    * static String ClassZero/ClassOne/ClassTwo .getXS(boolean f);
    */
    C0.reset();
    C1.reset();
    C2.reset();

    C0 = C1;
    harness.checkPoint("conversion through assignation(C1=>C0): Class and ToString");
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassOne");
    harness.check(C0.toString(),C1.toString());
    harness.check(C0.toString(),ClassOne.describeOne(C1) );
    harness.checkPoint("conversion through assignation(C1=>C0): static members");
    harness.check(C0.getXS(),ClassZero.getDescription(true) );
    harness.check(C0.getXS(),ClassZero.getDescription(ClassZero.xs) );
    harness.check(C0.getXS(),(new ClassZero()).getXS() );
    harness.checkPoint("conversion through assignation(C1=>C0): instance members");
    harness.check(C0.getX(),ClassZero.getDescription(0) );
    harness.check(C0.getX(),ClassZero.getDescription((new ClassOne()).x) );
    harness.check(C0.getX(),(new ClassOne()).getX());

    C0 = C2;
    harness.checkPoint("conversion through assignation(C2=>C0): Class and ToString");
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.check(C0.toString(),C2.toString());
    harness.check(C0.toString(),ClassTwo.describeTwo(C2) );
    harness.checkPoint("conversion through assignation(C2=>C0): static members");
    harness.check(C0.getXS(),ClassZero.getDescription(true));
    harness.check(C0.getXS(),ClassZero.getDescription(ClassZero.xs) );
    harness.check(C0.getXS(),(new ClassZero()).getXS()) ;
    harness.checkPoint("conversion through assignation(C2=>C0): instance members");
    harness.check(C0.getX(),ClassZero.getDescription(0.0f));
    harness.check(C0.getX(),ClassZero.getDescription((new ClassTwo()).x) );
    harness.check(C0.getX(),(new ClassTwo()).getX() );

    C1 = C2;
    harness.checkPoint("conversion through assignation(C2=>C1): Class and ToString");
    harness.check(C1.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.check(C1.toString(),C2.toString());
    harness.check(C1.toString(),ClassTwo.describeTwo(C2) );
    harness.checkPoint("conversion through assignation(C2=>C1): static members");
    harness.check(C1.getXS(),ClassZero.getDescription(10));
    harness.check(C1.getXS(),ClassZero.getDescription(ClassOne.xs) );
    harness.check(C1.getXS(),(new ClassOne()).getXS() );
    harness.checkPoint("conversion through assignation(C1=>C0): instance members");
    harness.check(C1.getX(),ClassZero.getDescription(0.0f));
    harness.check(C1.getX(),ClassZero.getDescription((new ClassTwo()).x) );
    harness.check(C1.getX(),(new ClassTwo()).getX() );


    /********************************************************
    * Checking method inheritance through explicit construction assignation
    * public String ClassZero/ClassOne/ClassTwo .getX(boolean f);
    * static String ClassZero/ClassOne/ClassTwo .getXS(boolean f);
    */

    C0 = new ClassOne(7,7.0f,7.0f);
    harness.checkPoint("conversion through explicit construction(C1=>C0): Class and ToString");
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassOne");
    harness.check(C0.toString(),ClassZero.describe(ClassOne.fullname,10, 7, 7.0f, 7.0f)  );
    harness.check(C0.toString(),ClassOne.describeOne(new ClassOne(7, 7.0f, 7.0f)) );
    harness.checkPoint("conversion through explicit construction(C1=>C0): static members");
    harness.check(C0.getXS(),ClassZero.getDescription(true) );
    harness.check(C0.getXS(),ClassZero.getDescription(ClassZero.xs) );
    harness.check(C0.getXS(),(new ClassZero()).getXS() );
    harness.checkPoint("conversion through explicit construction(C1=>C0): instance members");
    harness.check(C0.getX(),ClassZero.getDescription(7) );
    harness.check(C0.getX(),ClassZero.getDescription((new ClassOne(7,0.0f,0.0f)).x) );
    harness.check(C0.getX(),(new ClassOne(7,0.0f,0.0f)).getX());

    C0 = new ClassTwo(7.1f,7.2f,7.3f);
    harness.checkPoint("conversion through explicit construction(C2=>C0): Class and ToString");
    harness.check(C0.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.check(C0.toString(),ClassZero.describe(ClassTwo.fullname, 10.0f, 7.1f, 7.2f, 7.3f)  );
    harness.check(C0.toString(),ClassTwo.describeTwo(new ClassTwo(7.1f, 7.2f, 7.3f)) );
    harness.checkPoint("conversion through explicit construction(C2=>C0): static members");
    harness.check(C0.getXS(),ClassZero.getDescription(true));
    harness.check(C0.getXS(),ClassZero.getDescription(ClassZero.xs) );
    harness.check(C0.getXS(),(new ClassZero()).getXS()) ;
    harness.checkPoint("conversion through explicit construction(C2=>C0): instance members");
    harness.check(C0.getX(),ClassZero.getDescription(7.1f));
    harness.check(C0.getX(),ClassZero.getDescription((new ClassTwo(7.1f,0.0f,0.0f)).x) );
    harness.check(C0.getX(),(new ClassTwo(7.1f,0.0f,0.0f)).getX() );

    C1 = new ClassTwo(7.4f,7.5f,7.6f);
    harness.checkPoint("conversion through explicit construction(C2=>C1): Class and ToString");
    harness.check(C1.getClass().getName(),"gnu.testlet.wonka.vm.ClassTwo");
    harness.check(C1.toString(),ClassZero.describe(ClassTwo.fullname, 10.0f, 7.4f, 7.5f, 7.6f)  );
    harness.check(C1.toString(),ClassTwo.describeTwo(new ClassTwo(7.4f, 7.5f, 7.6f)) );
    harness.checkPoint("conversion through explicit construction(C2=>C1): static members");
    harness.check(C1.getXS(),ClassZero.getDescription(10));
    harness.check(C1.getXS(),ClassZero.getDescription(ClassOne.xs) );
    harness.check(C1.getXS(),(new ClassOne()).getXS() );
    harness.checkPoint("conversion through explicit construction(C2=>C1): instance members");
    harness.check(C1.getX(),ClassZero.getDescription(7.4f));
    harness.check(C1.getX(),ClassZero.getDescription((new ClassTwo(7.4f,0.0f,0.0f)).x) );
    harness.check(C1.getX(),(new ClassTwo(7.4f,0.0f,0.0f)).getX() );
  }
	
/********************************************************
* Checking method and parameter span
* with parameters casted through assignment to the form of the function parameter
* from derived to superclasses
*/
	private void testMethodsParameters()
	{
	
	  ClassTwo  q2 = new ClassTwo ();
	  harness.checkPoint("member checking through explicit superclass functions");
	  q2.importTwo(new ClassTwo(1.1f,2.2f,3.3f,4.4f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.1f, 2.2f, 3.3f, 4.4f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,   10,    0, 1.0f, 4.4f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,true,false,0.0f, 4.4f));
	
	  q2.reset();
	  harness.checkPoint("member checking through superclass functions, member setting with casted functions (ImportOne)");
	  q2.importOne(new ClassTwo(1.2f,2.3f,3.4f,4.5f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.2f, 0.0f, 2.0f, 4.5f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,   10,    0, 1.0f, 4.5f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,true,false,0.0f, 4.5f));
	  q2.importOne(new ClassOne(1,2,3.5f,4.6f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.2f, 0.0f, 2.0f, 4.6f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,    1,    2, 3.5f, 4.6f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,true,false,0.0f, 4.6f));
	
	  q2.reset();
	  harness.checkPoint("member checking through superclass functions, member setting with casted functions (ImportZero)");
	  q2.importZero(new ClassTwo(1.4f,2.5f,3.6f,4.7f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.4f, 0.0f, 2.0f, 4.7f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,   10,    0, 1.0f, 4.7f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,true,false,0.0f, 4.7f));
	  q2.importZero(new ClassOne(11,12,3.7f,4.8f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.4f, 0.0f, 2.0f, 4.8f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,   11,    0, 1.0f, 4.8f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,true,false,0.0f, 4.8f));
	  q2.importZero(new ClassZero(false,true,3.8f,4.9f) );
	  harness.check(q2.describeTwo(),ClassZero.describe(ClassTwo.fullname, 1.4f, 0.0f, 2.0f, 4.9f));
	  harness.check(q2.describeOne(),ClassZero.describe(ClassOne.fullname,   11,    0, 1.0f, 4.9f));
	  harness.check(q2.describeZero(),ClassZero.describe(ClassZero.fullname,false,true,3.8f, 4.9f));
	
	}

  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang.Class");
		
		testStatic();
		testEvaluationOrder();
		testVariablesHiding();
		testMembersAccess();
		testMethodsAccess();
		testMethodsParameters();
	}
}
