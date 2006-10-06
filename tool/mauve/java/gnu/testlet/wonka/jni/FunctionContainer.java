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
** more complex data container where all data are accessed by get- and set- functions (function access testing)
*/

public class FunctionContainer {

/*
** a more complex data container class. Compared to the simple container, all variables are private, and can only be accessed
** by calling the container's own get- and set- functions<br>
**
** To cover the best range of possibilities, here the containers 'contents' variables are:   <br>
** => a primitive (int)                                                                 <br>
** => a string                                                                          <br>
** => an array of primitives (boolean[3])                                               <br>
** => an instance of a self-defined class (the simplecontainer above  <br>
**
** The function handling class ContainerFunctions provides a wide array of functions to initialise this container
** and to compare its contents with other containers or with discrete sets of data      <br>
*/

  private int        number = 0;        /// a primitive
  private String     name = null;          /// a java class
  private boolean[]  preferences = new boolean[3];   /// an array
  private SimpleContainer internal = new SimpleContainer(); /// a self-defined class

  /// get the container's integer value
  public int getNumber()     {return number;}
  /// set the container's integer value
  public void setNumber (int i) {number = i;}

  /// get the container's string value
  public String getName()     {return name;}
  /// set the container's string value
  public void setName (String s) {name = s;}

  /// get the container's primitive array
  public boolean[] getPreferences()     {return preferences;}
  /// set the container's primitive array (copy from given array)
  public void setPreferences (boolean[] bx)
  {
    if(bx.length > 0)
      preferences[0] = bx[0];
    if(bx.length > 1)
      preferences[1] = bx[1];
    if(bx.length > 2)
      preferences[2] = bx[2];
  }
  /// set the container's primitive array Boolean[3] (construct from 3 booleans)
  public void setPreferences(boolean b0, boolean b1, boolean b2)
  {
    preferences[0] = b0;
    preferences[1] = b1;
    preferences[2] = b2;
  }

  /// get the container's SimpleContainer variable
  public SimpleContainer getInternal()     {return internal;}
  /// set the container's SimpleContainer variable
  public void setInternal (SimpleContainer inclass) {internal = inclass;}

}

