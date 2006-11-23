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


package gnu.testlet.wonka.security.BasicPermission; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.NetPermission; // at least the class you are testing ...
import java.util.Enumeration;
import java.security.*;

/**
*  this file contains test for java.security.NetPermission   <br>
*
*/
public class AcuniaNetPermissionTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.net.NetPermission");
       test_NetPermission();
       test_equals();
       test_hashCode();
       test_implies();
       test_getActions();
       test_newPermissionCollection();
     }


/**
*  implemented. <br>
*
*/
  public void test_NetPermission(){
    th.checkPoint("NetPermission(java.lang.String)");
    String s = "acunia.*";
    NetPermission bp = new NetPermission(s);
    th.check(bp.getName() , s , "checking name");
    s = "*";
    bp = new NetPermission(s);
    th.check(bp.getName() , s , "checking name");
    try {
        new NetPermission("");
        th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 1"); }
    try {
        new NetPermission(null);
        th.fail("should throw a NullPointerException -- 7");
    }
    catch(NullPointerException iae) { th.check(true , "caught exception -- 7"); }
    
    th.checkPoint("NetPermission(java.lang.String,java.lang.String)");
    try {
	bp = new NetPermission("cool.*", null);
	th.check(bp.getName() , "cool.*");
  new NetPermission("cool.*", "");
  new NetPermission("cool*.*", "");
	new NetPermission("cool.*", "*ddf");	
	th.check(true);
    }
    catch(Exception e) { th.fail("got unwanted exception "+e); }
    	
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    NetPermission bp = new NetPermission("com.acunia");
    th.check(!bp.equals(null), "null is allowed");
    th.check(!bp.equals(new NetPermission("com.*")), "not equal -- 2");
    th.check(bp.equals(new NetPermission("com.acunia","1")), "equal");
    th.check(!bp.equals(new gnu.testlet.wonka.security.BasicPermission.ExBasicPermission("com.acunia","1")), "not equal -- 2");
  }

/**
*   implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="com.acunia";
    NetPermission bp = new NetPermission(s);
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 1");
    s = "com.*";
    bp = new NetPermission(s);
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 2");
    s = "*";
    bp = new NetPermission(s,s);
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 3");
    bp = new NetPermission(s);
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 4");
  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    NetPermission bp1 = new NetPermission("com.acunia");
    NetPermission bp2 = new NetPermission("com.*");
    th.check( ! bp1.implies(bp2) , "not implied - 1");
    th.check(  bp2.implies(bp1) , "implied - 1");
    th.check(  bp1.implies(bp1) , "implied - 2");
    th.check(  bp2.implies(bp2) , "implied - 3");
    th.check( ! bp1.implies(new gnu.testlet.wonka.security.BasicPermission.ExBasicPermission("com.acunia","1")), "not implied -- 2");
    bp2 = new NetPermission("com.*");
    th.check(  bp2.implies(bp1) , "implied - 4");
    bp1 = new NetPermission("com.");
    th.check( ! bp2.implies(bp1) , "implied - 5");
    th.check( ! bp1.implies(bp2) , "not implied - 3");
    bp1 = new NetPermission("coma");
    bp2 = new NetPermission("coma*");
    th.check( ! bp2.implies(bp1) , "implied - 6");
    th.check( ! bp1.implies(bp2) , "not implied - 4");
  }

/**
*  implemented. <br>
*
*/
  public void test_getActions(){
    th.checkPoint("getActions()java.lang.String");
    String s ="com.acunia";
    NetPermission bp = new NetPermission(s);
    th.check(bp.getActions() , "" , "getActions returnvalue - 1");
    s = "com.*";
    bp = new NetPermission(s);
    th.check(bp.getActions() , "" , "getActions returnvalue - 2");
    s = "*";
    bp = new NetPermission(s,s);
    th.check(bp.getActions() , "" , "getActions returnvalue - 3");
    bp = new NetPermission(s);
    th.check(bp.getActions() , "" , "getActions returnvalue - 4");

  }

/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="com.acunia";
    NetPermission bp = new NetPermission(s);
    PermissionCollection pc = bp.newPermissionCollection();

    // boolean isReadOnly()
    th.check(! pc.isReadOnly());

    // void setReadOnly()
    pc.setReadOnly();
    th.check(pc.isReadOnly());

    // void add(Permission permission)
    try {
    	pc.add(bp);
    	Enumeration e = pc.elements();
    	th.check(! e.hasMoreElements());
    }
    catch(SecurityException se) { th.check(true); }
    pc = bp.newPermissionCollection();
    pc.add(bp);
    pc.add(new NetPermission("be.*"));
    pc.add(new NetPermission("vm.smartmove"));
    pc.add(new NetPermission("com.acunia.vm*"));

    // Enumeration elements()
    try  {
    	 Enumeration e = pc.elements();
         boolean found= bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         th.check(found , "reference should be kept");
         th.check(! e.hasMoreElements(), "should be empty");
    }
    catch(Exception e) { th.fail("Enumeration elements() was bad"); }

    Enumeration e = pc.elements();
    for (int i=0 ; i < 50 && e.hasMoreElements(); i++) {
      	th.debug("got "+e.nextElement()+", i = "+i);
    }

    // String toString()
    s = pc.toString();
    th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new NetPermission("be.*").toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new NetPermission("vm.smartmove").toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new NetPermission("com.acunia.vm*").toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    th.check(pc.implies(bp), "implies -- 1 "+bp);
    bp = new NetPermission("com.acu*");
    th.check(!pc.implies(bp), "implies -- 2 "+bp);
    bp = new NetPermission("be.");
    th.check(!pc.implies(bp), "implies -- 3 "+bp);
    bp = new NetPermission("be");
    th.check(!pc.implies(bp), "implies -- 4 "+bp);
    bp = new NetPermission("com.acunia");
    th.check(pc.implies(bp), "implies -- 5 "+bp);
    bp = new NetPermission("com.acunia.vm");
    th.check(!pc.implies(bp), "implies -- 6 "+bp);
    bp = new NetPermission("vm");
    th.check(!pc.implies(bp), "implies -- 7 "+bp);
  }
}
