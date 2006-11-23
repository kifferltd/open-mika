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
import gnu.testlet.TestHarness;

/*
** an interface for handling SimpleContainer FunctionContainer and ConstructionContainer classes
** as well as handling the boolean[3] arrays used in all the containers:                            <br>
** => build a boolean[3] array or container from a set of data,                                     <br>
** => compare a boolean[3] array or container to a reference of data                                <br>
**                                                                                                  <br>
** note: in order to generate an optimal testHarness output, we first transform  the containers, arrays or sets of values into
** standardized descriptor strings and then compare this strings, using the function SimpleHarness.check(object, object)
** (an implicit check(string to string)
*/

public class ContainerFunctions {

/***********************************************************************************************************************************/
// boolean[3]
/***********************************************************************************************************************************/
  /// transform a given boolean array into a 'standard' boolean[3].
  public boolean[] buildBoolArray(boolean[] bx)
  {
/**If the original array has more than 3 elements, only the first 3 will be copied, i
if he hasless then 3 elements, all elements non covered are set to false*/
    boolean newarray[] = new boolean[3];
    newarray[0] = (bx.length>0)?bx[0]:false;
    newarray[1] = (bx.length>1)?bx[1]:false;
    newarray[2] = (bx.length>2)?bx[2]:false;
    return newarray;
  }

  /// build a 'standard' boolean[3] out of 3 given booleans.
  public boolean[] buildBoolArray(boolean b0, boolean b1, boolean b2)
  {
    boolean newarray[] = new boolean[3];
    newarray[0] = b0;
    newarray[1] = b1;
    newarray[2] = b2;
    return newarray;
  }

  /// private comparison aid: 3 booleans to 'standard' boolean[3] container description string
  private String buildString(boolean b0, boolean b1, boolean b2)
  {
    String boolstring = "<";
    boolstring +=((b0==true)?"1,":"0,");
    boolstring +=((b1==true)?"1,":"0,");
    boolstring +=((b2==true)?"1>":"0>");
    return boolstring;
  }

  /// private comparison aid: boolean to 'standard' boolean[3] container description string
  private String buildString(boolean[] bx)
  {
  /// If the given array is not of length 3, it will be transformed into a standard boolean[3] first
    if(bx.length != 3)
      bx = buildBoolArray(bx);
    return buildString(bx[0],bx[1],bx[2]);
  }


  /** compare a boolean array to a set of values by comparing their container description strings.
  (if necessary, the boolean will be transformed into a standard boolean[3]) */
  public void checkBoolArray(boolean[] target, boolean b0, boolean b1, boolean b2, TestHarness harness)
  { harness.check(buildString(target), buildString(b0, b1, b2)); }

  /** compare a boolean array to a target array by comparing their description strings.
  (if necessary, the booleans will be transformed into standard boolean[3] */
  public void checkBoolArray(boolean[] target, boolean[] reference, TestHarness harness)
  { harness.check(buildString(target), buildString(reference)); }



/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
// SimpleContainer
/***********************************************************************************************************************************/
  /// build a simple container from data (do not regard the static integer)
  public SimpleContainer buildSimpleContainer(int i, String s, boolean[] bx)
  {
    /// if necessary, the boolean array will be transformed to container boolean[3]
    SimpleContainer dest = new SimpleContainer();
    dest.number = i;
    dest.name = s;
    dest.preferences = buildBoolArray(bx);
    return dest;
  }

  /// build a simple container from data (do not regard the static integer)
  public SimpleContainer buildSimpleContainer(int i, String s, boolean b0, boolean b1, boolean b2)
  {
    SimpleContainer dest = new SimpleContainer();
    dest.number = i;
    dest.name = s;
    dest.preferences = buildBoolArray(b0, b1, b2);
    return dest;
  }

  /// build a simple container from data, explicitly set the static integer
  public SimpleContainer buildSimpleContainer(int i, String s, boolean[] bx, int c)
  {
    /// if necessary, the boolean array will be transformed to container boolean[3]
    SimpleContainer dest = buildSimpleContainer(i, s, bx);
    dest.common = c;
    return dest;
  }

  /// build a simple container from data, explicitly set the static integer
  public SimpleContainer buildSimpleContainer(int i, String s,  boolean b0, boolean b1, boolean b2, int c)
  {
    SimpleContainer dest = buildSimpleContainer(i, s, b0, b1, b2);
    dest.common = c;
    return dest;
  }


  /// private comparison aid: SimpleContainer object to simpleContainer description string
  private String buildString(SimpleContainer c)
  { return ("{ number:"+c.number+" name:("+c.name+") preferences:"+buildString(c.preferences)+"}"); }

  /// private comparison aid: discrete SimpleContainer data to simpleContainer description string
  private String buildString(int i, String s, boolean[] bx)
  { return ("{ number:"+i+" name:("+s+") preferences:"+buildString(bx)+"}"); }

  /// private comparison aid: discrete SimpleContainer data to simpleContainer description string
  private String buildString(int i, String s, boolean b0, boolean b1, boolean b2)
  { return ("{ number:"+i+" name:("+s+") preferences:"+buildString(b0, b1, b2)+"}"); }



  /// compare two SimpleContainer instances (by comparing their description strings). Note that the static integer value is not regarded
  public void compareSimpleContainer(SimpleContainer target, SimpleContainer reference, TestHarness harness)
  { harness.check(buildString(target), buildString(reference) ); }

  /// compare a SimpleContainer object to a set of container data (the static integer value is not regarded)
  public void compareSimpleContainer(SimpleContainer target, int i, String s, boolean[] bx, TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,bx) ); }

  /// compare a SimpleContainer object to a set of container data (the static integer value is not regarded)
  public void compareSimpleContainer(SimpleContainer target, int i, String s, boolean b0, boolean b1, boolean b2, TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,b0, b1, b2) ); }


/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
// FunctionContainer
/***********************************************************************************************************************************/
  /// build a FunctionContainer from data, the SimpleContainer member is given as an object
  public FunctionContainer buildFunctionContainer(int i, String s, boolean[] bx, SimpleContainer inclass)
  {
    FunctionContainer dest = new FunctionContainer();
    dest.setNumber(i);
    dest.setName(s);
    dest.setPreferences(bx);
    dest.setInternal(inclass);
    return dest;
  }

  /// build a FunctionContainer from data, the SimpleContainer member is given as an object
  public FunctionContainer buildFunctionContainer(int i, String s, boolean b0, boolean b1, boolean b2, SimpleContainer inclass)
  {
    FunctionContainer dest = new FunctionContainer();
    dest.setNumber(i);
    dest.setName(s);
    dest.setPreferences(b0, b1, b2);
    dest.setInternal(inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well (do not regard its static integer)
  public FunctionContainer  buildFunctionContainer(int fi, String fs, boolean[] fbx, int si, String ss, boolean[] sbx)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sbx);
    FunctionContainer dest = buildFunctionContainer(fi,fs,fbx, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well (do not regard its static integer)
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean fb0, boolean fb1, boolean fb2, int si, String ss, boolean[] sbx)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sbx);
    FunctionContainer dest = buildFunctionContainer(fi, fs, fb0, fb1, fb2, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well (do not regard its static integer)
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean[] fbx, int si, String ss,
                                                      boolean sb0, boolean sb1, boolean sb2)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sb0, sb1, sb2);
    FunctionContainer dest = buildFunctionContainer(fi,fs,fbx, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer memberis constructed just as well (do not regard its static integer)
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean fb0, boolean fb1, boolean fb2, int si, String ss,
                                                      boolean sb0, boolean sb1, boolean sb2)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sb0, sb1, sb2);
    FunctionContainer dest = buildFunctionContainer(fi, fs, fb0, fb1, fb2, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well, its static integer explicitly set
  public FunctionContainer  buildFunctionContainer(int fi, String fs, boolean[] fbx, int si, String ss, boolean[] sbx, int scommon)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sbx, scommon);
    FunctionContainer dest = buildFunctionContainer(fi,fs,fbx, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well, its static integer explicitly set
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean fb0, boolean fb1, boolean fb2,
                                                    int si, String ss, boolean[] sbx, int scommon)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sbx, scommon);
    FunctionContainer dest = buildFunctionContainer(fi, fs, fb0, fb1, fb2, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer member is constructed just as well, its static integer explicitly set
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean[] fbx,
                                                    int si, String ss, boolean sb0, boolean sb1, boolean sb2, int scommon)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sb0, sb1, sb2, scommon);
    FunctionContainer dest = buildFunctionContainer(fi,fs,fbx, inclass);
    return dest;
  }

  /// build a simple container from data, the SimpleContainer memberis constructed just as well, its static integer explicitly set
  public FunctionContainer buildFunctionContainer(int fi, String fs, boolean fb0, boolean fb1, boolean fb2,
                                                    int si, String ss, boolean sb0, boolean sb1, boolean sb2, int scommon)
  {
    SimpleContainer inclass = buildSimpleContainer(si, ss, sb0, sb1, sb2, scommon);
    FunctionContainer dest = buildFunctionContainer(fi, fs, fb0, fb1, fb2, inclass);
    return dest;
  }


  /// private comparison aid: discrete FunctionContainer data to  description string
  private String buildString (int fi, String fs, boolean fb0, boolean fb1, boolean fb2,
                                int si, String ss, boolean sb0, boolean sb1, boolean sb2)
  { return ("{ number:"+fi+" name:("+fs+") preferences:"+buildString(fb0,fb1,fb2)+" Internal: "+buildString(si,ss,sb0,sb1,sb2)+"  }"); }

  /// private comparison aid: discrete FunctionContainer data to FunctionContainer description string
  private String buildString (int fi, String fs, boolean[] fbx,
                                int si, String ss, boolean sb0, boolean sb1, boolean sb2)
  { return ("{ number:"+fi+" name:("+fs+") preferences:"+buildString(fbx)+" Internal: "+buildString(si,ss,sb0,sb1,sb2)+"  }"); }

  /// private comparison aid: discrete FunctionContainer data to FunctionContainer description string
  private String buildString(int fi, String fs, boolean fb0, boolean fb1, boolean fb2, int si, String ss, boolean[] sbx)
  { return ("{ number:"+fi+" name:("+fs+") preferences:"+buildString(fb0,fb1,fb2)+" Internal: "+buildString(si,ss,sbx)+"  }"); }

  /// private comparison aid: discrete FunctionContainer data to FunctionContainer description string
  private String buildString(int fi, String fs, boolean[] fbx, int si, String ss, boolean[] sbx)
  { return ("{ number:"+fi+" name:("+fs+") preferences:"+buildString(fbx)+" Internal: "+buildString(si,ss,sbx)+"  }"); }

  /// private comparison aid: discrete FunctionContainer data to FunctionContainer description string
  private String buildString(int i, String s, boolean b0, boolean b1, boolean b2, SimpleContainer c)
  { return ("{ number:"+i+" name:("+s+") preferences:"+buildString(b0,b1,b2)+" Internal: "+buildString(c)+"  }"); }

  /// private comparison aid: discrete FunctionContainer data to  description string
  private String buildString(int i, String s, boolean[] bx, SimpleContainer c)
  { return ("{ number:"+i+" name:("+s+") preferences:"+buildString(bx)+" Internal: "+buildString(c)+"  }"); }

  /// private comparison aid: FunctionContainer object to FunctionContainer description string
  private String buildString(FunctionContainer c)
  { return ("{ number:"+c.getNumber()+" name:("+c.getName()+") preferences:"+buildString(c.getPreferences())
              +" Internal: "+buildString(c.getInternal())+"  }"); }


  /// compare two FunctionContainer instances (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer(FunctionContainer target, FunctionContainer reference, TestHarness harness)
  { harness.check(buildString(target), buildString(reference) ); }

  /// compare a FunctionContainer object to a set of container data (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer(FunctionContainer target, int i, String s, boolean[] bx, SimpleContainer c, TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,bx,c) ); }

  /// compare a FunctionContainer object to a set of container data (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer
  (FunctionContainer target, int i, String s, boolean b0, boolean b1, boolean b2, SimpleContainer c, TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,b0,b1,b2,c) ); }

  /// compare a FunctionContainer object to a set of container data (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer
    (FunctionContainer target, int fi, String fs, boolean[] fbx, int si, String ss, boolean[] sbx, TestHarness harness)
  { harness.check(buildString(target), buildString(fi,fs,fbx,si,ss,sbx) ); }

  /// compare a FunctionContainer object to a set of container data (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer
    (FunctionContainer target, int fi, String fs, boolean fb0, boolean fb1, boolean fb2,
      int si, String ss, boolean[] sbx, TestHarness harness)
  { harness.check(buildString(target), buildString(fi,fs,fb0,fb1,fb2,si,ss,sbx) ); }

  /// compare a FunctionContainer object to a set of container data (the SimpleContainer's static integer value is not regarded)
  public void compareFunctionContainer
    (FunctionContainer target, int fi, String fs, boolean fb0, boolean fb1, boolean fb2,
      int si, String ss, boolean sb0, boolean sb1, boolean sb2, TestHarness harness)
  { harness.check(buildString(target), buildString(fi,fs,fb0,fb1,fb2,si,ss,sb0,sb1,sb2) ); }


/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
// ConstructionContainer
/***********************************************************************************************************************************/
/** NOTE: All BuildConstructinContainer methods are obsolete, since the container can simply be built using its own constructors
nevertheless, we'll keep this functions out of symmetry */

  /// build a ConstructionContainer out of data (this is a simple shortcut to the container's own construction method for the same data)
  public ConstructionContainer buildConstructionContainer(SimpleContainer c) { return new ConstructionContainer(c);}
  /// build a ConstructionContainer out of data (this is a simple shortcut to the container's own construction method for the same data)
  public ConstructionContainer buildConstructionContainer(int i, String s, boolean[] bx) { return new ConstructionContainer(i,s,bx);}
  /// build a ConstructionContainer out of data (this is a simple shortcut to the container's own construction method for the same data)
  public ConstructionContainer buildConstructionContainer(int i, String s, boolean b0, boolean b1, boolean b2)
                                                           { return new ConstructionContainer(i,s,b0,b1,b2);}

  /// private comparison aid: ConstructionContainer object to ConstructionContainer description string
  private String buildString(ConstructionContainer c)
  {
  /** NOTE: as the descriptiion strings for ConstructionContainer ans SimpleContainer are the same,
  the same buildString functions can be used to construct a reference string out of discrete data.
  */
    return buildString(c.getBase());
  }


  /// compare two ConstructionContainer instances
  public void compareConstructionContainer(ConstructionContainer target, ConstructionContainer reference, TestHarness harness)
  { harness.check(buildString(target), buildString(reference) ); }

  /// compare a ConstructionContainer object to a set of data stored in a SimpleContainer (the SimpleContainers static is disregarded)
  public void compareConstructionContainer(ConstructionContainer target, SimpleContainer reference, TestHarness harness)
  { harness.check(buildString(target), buildString(reference) ); }

  /// compare a ConstructionContainer object to a set of container data
  public void compareConstructionContainer(ConstructionContainer target, int i, String s, boolean[] bx, TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,bx) ); }

  /// compare a ConstructionContainer object to a set of container data
  public void compareConstructionContainer(ConstructionContainer target, int i, String s, boolean b0, boolean b1, boolean b2,
                                            TestHarness harness)
  { harness.check(buildString(target), buildString(i,s,b0, b1, b2) ); }


/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
// common static integer of SimpleContainer and FunctionContainer.internal
/***********************************************************************************************************************************/
  /// set the common integer of a SimpleContainer (of all SimpleContainers) to a specific value
  public void setCommon(SimpleContainer target, int i)
  {
    target.common = i;
  }

  /// set the common integer of the SimpleContainer member of a FunctionContainer (and of all other SimpleContainers) to a specific value
  public void compareCommon(FunctionContainer target, int i)
  {
    SimpleContainer internal = target.getInternal();
    internal.common = i;
  }

  /// compare the common integer of a SimpleContainer to a given value
  public void compareCommon(SimpleContainer target, int i, TestHarness harness)
  {
    harness.check(target.common, i);
  }

  /// compare the common integer of the SimpleContainer member of a FunctionContainer to a given value
  public void compareCommon(FunctionContainer target, int i, TestHarness harness)
  {
    SimpleContainer internal = target.getInternal();
    harness.check(internal.common, i);
  }


/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
/***********************************************************************************************************************************/

/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
/***********************************************************************************************************************************/

/***********************************************************************************************************************************/
/***********************************************************************************************************************************/
/***********************************************************************************************************************************/

/**
*   a protected function designed to be overwritten by derived classes
*/
  protected byte getVersion() {return((byte)1); }

/***********************************************************************************************************************************/
  /// dummy test allows us to run this class in a testrunner
  public void test (TestHarness newharness)
	{
		newharness.setclass("ContainerFunction virtual base class");
		newharness.check(true,"ContainerFunction is a virtual base class");
		newharness.fail("ContainerFunction is a virtual base class");
	}
}
