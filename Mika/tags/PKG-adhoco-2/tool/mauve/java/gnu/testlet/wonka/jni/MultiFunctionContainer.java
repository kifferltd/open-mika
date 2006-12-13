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
** container class with lots of similar functions, only differing in the number of arguments,
*/

public class MultiFunctionContainer extends ConstructionContainer {

  /*
  ** An extension to the ConstructionContainer class featuring a series of static, setting- and constructor functions
  ** that only differ by the number of variables. Like this, they can be called from native functins using the call-A
  ** and call-V functions.
  ** Note that the setVariablesPartial() functions are overriding the ConstructionContainer's functions of that name,
  ** leaving us the option to access the base's functions by using the super. (java) or -Nonvirtual- (jni) flags
  */

  /** the main set-variables function for the container */
  public void setVariables(int i, String s, boolean b0, boolean b1, boolean b2)
  {
    number = i;
    name = s;
    preferences[0] = b0;
    preferences[1] = b1;
    preferences[2] = b2;
  }

  //@{
  /** a set of constructors building a 'void' container, graduately filling in the variables
  According the number of variables of the constructor, the variables of the container are initialised in this order: <br>
  (none), integer, string, first boolean of array, second boolean of array, third boolean of array
  */
  public MultiFunctionContainer()                                                   {setVariables(0,"",false,false,false);}
  public MultiFunctionContainer(int i)                                              {setVariables(i,"",false,false,false);}
  public MultiFunctionContainer(int i,String s)                                     {setVariables(i,s,false,false,false);}
  public MultiFunctionContainer(int i,String s, boolean b0)                         {setVariables(i,s,b0,false,false);}
  public MultiFunctionContainer(int i,String s, boolean b0, boolean b1)             {setVariables(i,s,b0,b1,false);}
  public MultiFunctionContainer(int i,String s, boolean b0, boolean b1, boolean b2) {setVariables(i,s,b0,b1,b2);}
  //@}

  //@{
  /** a set of void setting functions derived from SetVariable(all) graduately filling the container with the given values
  */
  public void buildContainer()                                                        {setVariables(0,"",false,false,false);}
  public void buildContainer(int i)                                                   {setVariables(i,"",false,false,false);}
  public void buildContainer(int i, String s)                                         {setVariables(i,s,false,false,false);}
  public void buildContainer(int i, String s, boolean b0)                             {setVariables(i,s,b0,false,false);}
  public void buildContainer(int i, String s, boolean b0, boolean b1)                 {setVariables(i,s,b0,b1,false);}
  public void buildContainer(int i, String s, boolean b0, boolean b1, boolean b2)     {setVariables(i,s,b0,b1,b2);}
  //@}

  //@{
  /** a set of object functions, setting the container and returning a SimpleContainer base to it
  */
  public SimpleContainer getBuiltContainerBase()
      {setVariables(0,"",false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i)
      {setVariables(i,"",false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s)
      {setVariables(i,s,false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0)
      {setVariables(i,s,b0,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0, boolean b1)
      {setVariables(i,s,b0,b1,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0, boolean b1, boolean b2)
      {setVariables(i,s,b0,b1,b2); return getBase();}
  //@}

  //@{
  /** a set of static void functions, setting the variables of a given container to a series of given values
  */
  //@}
  static void buildContainerStatic(MultiFunctionContainer mfc)
    {mfc.setVariables(0,"",false,false,false);}
  static void buildContainerStatic(MultiFunctionContainer mfc, int i )
    {mfc.setVariables(i,"",false,false,false);}
  static void buildContainerStatic(MultiFunctionContainer mfc, int i, String s )
    {mfc.setVariables(i,s,false,false,false);}
  static void buildContainerStatic(MultiFunctionContainer mfc, int i, String s, boolean b0 )
    {mfc.setVariables(i,s,b0,false,false);}
  static void buildContainerStatic(MultiFunctionContainer mfc, int i, String s, boolean b0, boolean b1 )
    {mfc.setVariables(i,s,b0,b1,false);}
  static void buildContainerStatic(MultiFunctionContainer mfc, int i, String s, boolean b0, boolean b1, boolean b2 )
    {mfc.setVariables(i,s,b0,b1,b2);}

  static SimpleContainer simpleContainerStatic()
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(0,"",false,false,false); return mfc.getBase();}
  static SimpleContainer simpleContainerStatic(int i )
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(i,"",false,false,false); return mfc.getBase();}
  static SimpleContainer simpleContainerStatic(int i, String s )
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(i,s,false,false,false); return mfc.getBase();}
  static SimpleContainer simpleContainerStatic(int i, String s, boolean b0 )
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(i,s,b0,false,false); return mfc.getBase();}
  static SimpleContainer simpleContainerStatic(int i, String s, boolean b0, boolean b1 )
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(i,s,b0,b1,false); return mfc.getBase();}
  static SimpleContainer simpleContainerStatic(int i, String s, boolean b0, boolean b1, boolean b2 )
    {MultiFunctionContainer mfc = new MultiFunctionContainer(); mfc.setVariables(i,s,b0,b1,b2); return mfc.getBase();}

  //@{
  /** a set of static object functions, returning a SimpleContainer base of graduately filled containers
  */
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc)
      {return mfc.getBuiltContainerBase(0,"",false,false,false);}
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc, int i)
      {return mfc.getBuiltContainerBase(i,"",false,false,false);}
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc, int i, String s)
      {return mfc.getBuiltContainerBase(i,s,false,false,false);}
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc, int i, String s, boolean b0)
      {return mfc.getBuiltContainerBase(i,s,b0,false,false);}
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc, int i, String s, boolean b0, boolean b1)
      {return mfc.getBuiltContainerBase(i,s,b0,b1,false);}
  static SimpleContainer getBuiltContainerBaseStatic(MultiFunctionContainer mfc, int i, String s, boolean b0, boolean b1, boolean b2)
      {return mfc.getBuiltContainerBase(i,s,b0,b1,b2);}
  //@}

}

