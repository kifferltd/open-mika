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

package gnu.testlet.wonka.security.AllPermission; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.security.*; // at least the class you are testing ...
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
*  this file contains test for java.security.AllPermission   <br>
*  we also test checkGuard, toString and getName
*/
public class AcuniaAllPermissionTest implements Testlet
{
  protected TestHarness th;

  private final static String allp = "<all permissions>";
  private final static String action = "<all actions>";

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.security.AllPermission");
       test_AllPermission();
       test_equals();
       test_hashCode();
       test_implies();
       test_getActions();
       test_newPermissionCollection();
       test_toString();
     }


/**
*  implemented. <br>
*
*/
  public void test_AllPermission(){
    th.checkPoint("AllPermission()");
    AllPermission bp = new AllPermission();
    th.check(bp.getName() , allp);
    th.check(bp.getActions() , action);

    th.checkPoint("AllPermission(java.lang.String,java.lang.String)");
    bp = new AllPermission(null, null);
    th.check(bp.getName() , allp);
    th.check(bp.getActions() , action);
    bp = new AllPermission("*.*fghhgjhf*g", null);
    th.check(bp.getName() , allp);
    th.check(bp.getActions() , action);
    	
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    AllPermission bp = new AllPermission("com.acunia" , "read");
    th.check(!bp.equals(null), "null is allowed");
    th.check(bp.equals(new AllPermission()), "equal -- 1");
    th.check(!bp.equals(new SecurityPermission("com.acunia","read")), "not equal -- 2");
  }

/**
*   implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="com.acunia";
    AllPermission bp = new AllPermission();
    th.check(bp.hashCode() , 1 , "cheking hash algorithm ... - 1");
    s = "com.*";
    bp = new AllPermission(s,null);
    th.check(bp.hashCode() , 1 , "cheking hash algorithm ... - 2");
    s = "*";
  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    AllPermission bp1 = new AllPermission("com.acunia","read");
    AllPermission bp2 = new AllPermission();
    th.check(  bp1.implies(bp2) , "implied - 1");
    th.check(  bp2.implies(null) , "not implied - 1");
    th.check(  bp2.implies(bp1) , "implied - 2");
    th.check(  bp1.implies(new SecurityPermission("com.acunia","1")), "implied -- 3");
  }

/**
*  implemented. <br>
*
*/
  public void test_getActions(){
    th.checkPoint("getActions()java.lang.String");
    String s ="com.acunia";
    AllPermission bp = new AllPermission(s,"read \n \f");
    th.check(bp.getActions() , action, "getActions returnvalue - 1");
    s = "com.*";
    bp = new AllPermission(s,"  \t \nwRiTe\f");
    th.check(bp.getActions() , action, "getActions returnvalue - 2");
    s = "*";
    bp = new AllPermission(s,"WriTe \n ,\t REaD , ReAd ,Write");
    th.check(bp.getActions() , action, "getActions returnvalue - 3");
    bp = new AllPermission(s,"Read");
    th.check(bp.getActions() , action , "getActions returnvalue - 4");

  }

/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="com.acunia";
    AllPermission bp = new AllPermission(s,"read");
    PermissionCollection pc = bp.newPermissionCollection();

    // Enumeration elements()
    Enumeration e = pc.elements();
    th.check(! e.hasMoreElements(), "should be empty");
    try {
    	e.nextElement();
	th.fail("Enumeration elements() was bad");
    }
    catch(NoSuchElementException ee) { th.check(true); }

    // boolean isReadOnly()
    th.check(! pc.isReadOnly(), "not readOnly");

    // void setReadOnly()
    pc.setReadOnly();
    th.check(pc.isReadOnly(), "readOnly");

    // void add(Permission permission)
    try {
    	pc.add(bp);
    	e = pc.elements();
    	th.check(! e.hasMoreElements(), "added element");
    }
    catch(SecurityException se) { th.check(true); }
    pc = bp.newPermissionCollection();
    pc.add(bp);
    pc.add(new AllPermission("be.*","read"));
    pc.add(new AllPermission("vm.smartmove","write"));
    pc.add(new AllPermission("com.acunia.vm*","read,write"));

    // String toString()
    s = pc.toString();
    th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    th.check(pc.implies(bp), "implies -- 1 "+bp);
    th.check(pc.implies(new SecurityPermission("cool")), "implies -- 2 ");
  }

/**
*   implemented. <br>
*   inherited from Permission (tested here out of convenience)
*/
  public void test_toString(){
    th.setclass("java.security.Permission");
    th.checkPoint("toString()java.lang.String");
    String s ="com.acunia";
    AllPermission bp = new AllPermission(s,"reAd");
    th.check( bp.toString() , "("+bp.getClass().getName()+" "+allp+" "+action+")");

         //'(ClassName name actions)'.
  }
}
