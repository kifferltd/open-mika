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

/*
** a test-case exception to be thrown.For testing, this exception contains the same set of data as
** a standard FunctionContainer, except that here the string member variable is the Exception class'
** own string, set at initialisation, and afterwards retrievable by calls to Exception.getMessage()
*/

public class ContainerException extends Exception {
  
  private int        number = 0;        // a primitive
  private String     name = null;          // a java class
  private boolean[]  preferences = new boolean[3];   // an array
  private SimpleContainer internal = new SimpleContainer();

/// standard Exception constructor
  public ContainerException() {super();}
/// standard Exception constructor with error string
  public ContainerException(String s) {super(s);}
/// full ContainerException constructor
  public ContainerException(int i, String s, boolean[] bx, SimpleContainer sc)
  {
    super(s);
    setException(i,bx,sc);
  }
/// full ContainerException constructor
  public ContainerException(FunctionContainer fc)
  {
    super(fc.getName());
    setException(fc.getNumber() ,fc.getPreferences() , fc.getInternal());
  }

/// afterwards initialise the exceptions container data (except for the pure Exception string , that can only be set at initialisation)
  public void setException(int i, boolean[] bx, SimpleContainer sc)
  {
    number = i;
    //name = s;//done on initialisation
    if(bx.length > 0)
      preferences[0] = bx[0];
    if(bx.length > 1)
      preferences[1] = bx[1];
    if(bx.length > 2)
      preferences[2] = bx[2];
    internal.number = sc.number;
    internal.name = sc.name;
    internal.preferences[0] = sc.preferences[0];
    internal.preferences[1] = sc.preferences[1];
    internal.preferences[2] = sc.preferences[2];
  }

/// return the exceptions container data as a Functioncontainer
  public FunctionContainer getFunctionContainer()
  {
System.out.println("=> returning FunctionContainer()");
    FunctionContainer fc = new FunctionContainer();

System.out.println("=> setting variables: number "+number);
    fc.setNumber(number);
System.out.println("=> setting variables: name " +getMessage());
    fc.setName(getMessage());
System.out.println("=> setting variables: preferences");
    fc.setPreferences(preferences);
System.out.println("=> setting variables: internal container");
    fc.setInternal(internal);
System.out.println("=> Ok, returning container");
    return fc;
  }
}

