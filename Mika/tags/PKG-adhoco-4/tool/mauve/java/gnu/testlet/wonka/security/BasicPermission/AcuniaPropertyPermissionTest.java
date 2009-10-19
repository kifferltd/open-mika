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

import java.security.*; // at least the class you are testing ...
import java.util.Enumeration;
import java.util.PropertyPermission;

/**
*  this file contains test for java.security.PropertyPermission   <br>
*  we also test checkGuard, toString and getName
*/
public class AcuniaPropertyPermissionTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.PropertyPermission");
       test_PropertyPermission();
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
  public void test_PropertyPermission(){
    th.checkPoint("PropertyPermission(java.lang.String,java.lang.String)");
    String s = "acunia.*";
    PropertyPermission bp = new PropertyPermission(s, "read");
    th.check(bp.getName() , s , "checking name");
    th.check(bp.getActions() , "read" , "checking actions");
    s = "*";
    bp = new PropertyPermission(s, " write ");
    th.check(bp.getName() , s , "checking name");
    th.check(bp.getActions() , "write" , "checking actions");
    try {
        new PropertyPermission("", "read");
        th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 1"); }
    try {
        new PropertyPermission(null, "read");
        th.fail("should throw a NullPointerException -- 7");
    }
    catch(NullPointerException iae) { th.check(true , "caught exception -- 7"); }
    try {
        new PropertyPermission("read.write", null);
        th.fail("should throw a NullPointerException -- 8");
    }
    catch(NullPointerException iae) { th.check(true , "caught exception -- 8"); }
    catch(IllegalArgumentException iae) { th.check(true, "exception caught -- 8"); }
    try {
        new PropertyPermission("*", "ready");
        th.fail("should throw an IllegalArgumentException -- 9");
    }
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 9"); }
    try {
        new PropertyPermission("*", "white");
        th.fail("should throw an IllegalArgumentException -- 10");
    }
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 10"); }
    try {
        new PropertyPermission("*", "write , , read");
        th.fail("should throw an IllegalArgumentException -- 11");
    }
    catch(IllegalArgumentException iae) { th.check(true , "caught exception -- 11"); }
    	
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    PropertyPermission bp = new PropertyPermission("com.acunia" , "read");
    th.check(!bp.equals(null), "null is allowed");
    th.check(!bp.equals(new PropertyPermission("com.*", "read")), "not equal -- 1");
    th.check(bp.equals(new PropertyPermission("com.acunia","\t  read  \n")), "equal");
    th.check(!bp.equals(new PropertyPermission("com.acunia","\twrite , read  \n")), "not equal -- 2");
    th.check(!bp.equals(new SecurityPermission("com.acunia","read")), "not equal -- 3");
    bp = new PropertyPermission("com.acunia" , "write,read");
    th.check(bp.equals(new PropertyPermission("com.acunia","\twrite , read  \n")), "equal");
  }

/**
*   implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="com.acunia";
    PropertyPermission bp = new PropertyPermission(s, "read");
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 1");
    s = "com.*";
    bp = new PropertyPermission(s,"write");
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 2");
    s = "*";
    bp = new PropertyPermission(s,"write, read");
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 3");
    bp = new PropertyPermission(s, "WritE");
    th.check(bp.hashCode() , s.hashCode() , "cheking hash algorithm ... - 4");
  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    PropertyPermission bp1 = new PropertyPermission("com.acunia","read");
    PropertyPermission bp2 = new PropertyPermission("com.*", "write");
    th.check( ! bp1.implies(bp2) , "not implied - 1");
    th.check( ! bp2.implies(bp1) , "implied - 1");
    th.check(  bp1.implies(bp1) , "implied - 2");
    th.check(  bp2.implies(bp2) , "implied - 3");
    th.check( ! bp1.implies(new SecurityPermission("com.acunia","1")), "not implied -- 2");
    bp2 = new PropertyPermission("com.*", "write,read");
    th.check(  bp2.implies(bp1) , "implied - 4");
    bp1 = new PropertyPermission("com.","ReAD    , WRitE \n \t \f ");
    th.check( ! bp2.implies(bp1) , "implied - 5");
    th.check( ! bp1.implies(bp2) , "not implied - 3");
    bp1 = new PropertyPermission("coma","READ");
    bp2 = new PropertyPermission("coma*","read,Write");
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
    PropertyPermission bp = new PropertyPermission(s,"read \n \f");
    th.check(bp.getActions() , "read" , "getActions returnvalue - 1");
    s = "com.*";
    bp = new PropertyPermission(s,"  \t \nwRiTe\f");
    th.check(bp.getActions() , "write" , "getActions returnvalue - 2");
    s = "*";
    bp = new PropertyPermission(s,"WriTe \n ,\t REaD , ReAd ,Write");
    th.check(bp.getActions() , "read,write" , "getActions returnvalue - 3");
    bp = new PropertyPermission(s,"Read");
    th.check(bp.getActions() , "read" , "getActions returnvalue - 4");

  }

/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="com.acunia";
    PropertyPermission bp = new PropertyPermission(s,"read");
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
    try {
    	pc.add(new ExBasicPermission("*"));
    	th.fail("wrong Permission Type ...");
    }
    catch(IllegalArgumentException se) { th.check(true); }
    pc.add(bp);
    pc.add(new PropertyPermission("be.*","read"));
    pc.add(new PropertyPermission("vm.smartmove","write"));
    pc.add(new PropertyPermission("com.acunia.vm*","read,write"));

    // Enumeration elements()
    try  {
    	 Enumeration e = pc.elements();
         boolean found = bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         th.check(found , "reference should be kept");
         th.check(! e.hasMoreElements(), "should be empty");
    }
    catch(Exception e) { th.fail("Enumeration elements() was bad"); }

    Enumeration e = pc.elements();
    //for (int i=0 ; i < 50 && e.hasMoreElements(); i++) {
    //  	th.debug("got "+e.nextElement()+", i = "+i);
    //}

    // String toString()
    s = pc.toString();
    //th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new PropertyPermission("be.*","read").toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new PropertyPermission("vm.smartmove","write").toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new PropertyPermission("com.acunia.vm*","read,write").toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    th.check(pc.implies(bp), "implies -- 1 "+bp);
    bp = new PropertyPermission("com.acu*","read");
    th.check(!pc.implies(bp), "implies -- 2 "+bp);
    bp = new PropertyPermission("be.e","read");
    th.check(pc.implies(bp), "implies -- 3 "+bp);
    bp = new PropertyPermission("be","write");
    th.check(!pc.implies(bp), "implies -- 4 "+bp);
    bp = new PropertyPermission("com.acunia","read");
    th.check(pc.implies(bp), "implies -- 5 "+bp);
    bp = new PropertyPermission("com.acunia.vm1","read");
    th.check(!pc.implies(bp), "implies -- 6 "+bp);
    bp = new PropertyPermission("vm","write");
    th.check(!pc.implies(bp), "implies -- 7 "+bp);
    bp = new PropertyPermission("be.","read,write");
    th.check(!pc.implies(bp), "implies -- 8 "+bp);
    bp = new PropertyPermission("com.acunia","write");
    th.check(!pc.implies(bp), "implies -- 9 "+bp);
    bp = new PropertyPermission("com.acunia.vm","read,write");
    th.check(!pc.implies(bp), "implies -- 10 "+bp);
    bp = new PropertyPermission("com.acunia.vm*","write");
    th.check(pc.implies(bp), "implies -- 11 "+bp);
    pc.add(new PropertyPermission("*","read"));
    th.check(pc.implies(new PropertyPermission("everything","read")), "implies -12");
    try  {
    	 e = pc.elements();
         e.nextElement();
         e.nextElement();
         e.nextElement();
         e.nextElement();
         e.nextElement();
         th.check(! e.hasMoreElements(), "should be empty");
    }
    catch(Exception ee) { th.fail("Enumeration elements() was bad"); }
  }

/**
*   implemented. <br>
*   inherited from Permission (tested here out of convenience)
*/
  public void test_toString(){
    th.setclass("java.security.Permission");
    th.checkPoint("toString()java.lang.String");
    String s ="com.acunia";
    PropertyPermission bp = new PropertyPermission(s,"reAd");
    th.check( bp.toString() , "("+bp.getClass().getName()+" "+s+" read)");

         //'(ClassName name actions)'.
  }
}
