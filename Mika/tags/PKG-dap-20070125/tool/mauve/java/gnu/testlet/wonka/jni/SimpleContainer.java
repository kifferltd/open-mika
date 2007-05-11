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

package gnu.testlet.wonka.jni;
import gnu.testlet.TestHarness;

/// simple data containing class for testing purposes

public class SimpleContainer {

/*
** simple data container class, It's only purpose is to store a number of variables that can be set and read
** by calls to specific testing functions from outside.                                 <br>
** In this most simple forms, all data are public members, so they can be accessed by simple comparisone and assignments. <br>
**
** To cover the best range of possibilities, the containers 'contents' variables are:   <br>
** => a primitive (int)                                                                 <br>
** => a string                                                                          <br>
** => an array of primitives (boolean[3])                                               <br>
** => a static primitive,(int) to test static behaviour between different class members <br>
**
** The function handling class ContainerFunctions provides a wide array of functions to initialise a simple container
** and to compare its contents with other containers or with discrete sets of data      <br>
*/

  public int        number = 0;       // a primitive
  public String     name = null;         // a java class
  public boolean[]  preferences = new boolean[3];  // an array
  public static int common;// = 0;   // the same for all instances of the class
  
}

