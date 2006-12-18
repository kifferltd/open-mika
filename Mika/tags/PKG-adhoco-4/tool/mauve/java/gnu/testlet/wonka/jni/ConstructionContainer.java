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
** data container with constructor functions
*/

public class ConstructionContainer {

  /*
  ** A data container class that features its own constructors. To be used for tests involving constructor calls. For simplicity,
  ** the containers 'contents' variables are limited to:   <br>
  ** => a primitive (int)                                                                 <br>
  ** => a string                                                                          <br>
  ** => an array of primitives (boolean[3])                                               <br>
  ** 
  ** The function handling class ContainerFunctions provides functions to compare a member of this class
  ** with other members or with discrete sets of data      <br>
  */

  static boolean invert = false;

  protected int        number = 0;        // a primitive
  protected String     name = null;          // a java class
  protected boolean[]  preferences = new boolean[3];   // an array

  /// void constructor
  public ConstructionContainer() { }

  /// constructs the container from a set of discrete data
  public ConstructionContainer(int i, String s, boolean b0,  boolean b1,  boolean b2) { setVariables(i,s,b0,b1,b2); }
  /// constructs the container from a set of discrete data
  public ConstructionContainer(int i, String s, boolean[] bx) { setVariables(i,s,bx); }
  /// constructs the container from data of a given SimpleContainer
  public ConstructionContainer(SimpleContainer sc) { setVariables(sc); }
  /// constructs the container ccloning from a given member ConstructionContainer
  public ConstructionContainer(ConstructionContainer cc) { setVariables(cc); }

  /// set the different variables from discrete data
  public void setNumber (int i) {number=i;}
  public void setName   (String s) {name = s;}
  public void setPreferences(boolean b0, boolean b1, boolean b2)
  {
    preferences[0] = b0;
    preferences[1] = b1;
    preferences[2] = b2;
  }

  public void setPreferences(boolean[] bx)
  {
    if(bx.length > 0)
      preferences[0] = bx[0];
    if(bx.length > 1)
      preferences[1] = bx[1];
    if(bx.length > 2)
      preferences[2] = bx[2];
  }

  /// set all variables at once from a set of discrete data
  public void setVariables(int i,String s, boolean b0, boolean b1, boolean b2)
  {
    if(invert){
      setNumber(-i);
      setName(s.toUpperCase());
      setPreferences(!b0,!b1,!b2);
    }
    else {
      setNumber(i);
      setName(s);
      setPreferences(b0,b1,b2);
    }
  }

  public void setVariables(int i,String s, boolean[] bx)
  {
    setNumber(i);
    setName(s);
    setPreferences(bx);
  }

  /// set all variables to the values contained in a 'base' SimpleContainer
  public void setVariables(SimpleContainer base)
  {
    number = base.number;
    name = base.name;
    preferences = base.preferences;
  }
  /// copy a container into an other (this is used for the objectarray
  public void setVariables(ConstructionContainer toclone)
  {
    number = toclone.number;
    name = toclone.name;
    preferences = toclone.preferences;
  }

  /// returns a 'base' simplecontainer that contains this containers basic values
  public SimpleContainer getBase()
  {
    SimpleContainer base = new SimpleContainer();
    base.number = number;
    base.name = name;
    base.preferences = preferences;
    return base;
  }

  public void writeBase()
  {
     	System.out.print("{"+number + ":("+name+") <"+ preferences[0]+", "+ preferences[1]+", "+ preferences[2]+">}");	
  }

  /** This base class function is designed to be replaced by the derived MultiFunctionContainer's
  setVariables so that there is a difference between the nonvirtual base functions SetContainer/getSetContainerBase and the
  MultifunctionContainers derived ones. It is used in testing the NonVirtual -A /-V access functions
  */
  public void setVariablesBase(int i, String s, boolean b0, boolean b1, boolean b2)
  {
    number = -i;
    if (s.length()>0)
      name = s.substring(1) + s.substring(0,1);
    else
      name = "";
    preferences[0] = !b0;
    preferences[1] = !b1;
    preferences[2] = !b2;
  }
  //@{
  /**
    a set of void setting functions derived from SetVariable(all) graduately filling the container with the given values
    these functions are 'base' functions designed to be replaced by the derived MultiFunctionContainer's
    setVariables so that there is a difference between the nonvirtual base functions SetContainer/getSetContainerBase and the
    MultifunctionContainers derived ones. It is used in testing the NonVirtual -A /-V access functions
  */
  public void buildContainer()                                                        {setVariablesBase(0,"",false,false,false);}
  public void buildContainer(int i)                                                   {setVariablesBase(i,"",false,false,false);}
  public void buildContainer(int i, String s)                                         {setVariablesBase(i,s,false,false,false);}
  public void buildContainer(int i, String s, boolean b0)                             {setVariablesBase(i,s,b0,false,false);}
  public void buildContainer(int i, String s, boolean b0, boolean b1)                 {setVariablesBase(i,s,b0,b1,false);}
  public void buildContainer(int i, String s, boolean b0, boolean b1, boolean b2)     {setVariablesBase(i,s,b0,b1,b2);}
  //@}

  //@{
  /**
    a set of object functions, setting the container and returning a SimpleContainer base to it
    these functions are 'base' functions designed to be replaced by the derived MultiFunctionContainer's
    setVariables so that there is a difference between the nonvirtual base functions SetContainer/getSetContainerBase and the
    MultifunctionContainers derived ones. It is used in testing the NonVirtual -A /-V access functions
  */
  public SimpleContainer getBuiltContainerBase()
      {setVariablesBase(0,"",false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i)
      {setVariablesBase(i,"",false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s)
      {setVariablesBase(i,s,false,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0)
      {setVariablesBase(i,s,b0,false,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0, boolean b1)
      {setVariablesBase(i,s,b0,b1,false); return getBase();}
  public SimpleContainer getBuiltContainerBase(int i, String s, boolean b0, boolean b1, boolean b2)
      {setVariablesBase(i,s,b0,b1,b2); return getBase();}
  //@}

  public String toString(){
    return getClass()+"<number ="+number+", name ='"+name+"', preferences "+preferences[0]+", "+preferences[1]+", "+preferences[2]+">";
  }

}

