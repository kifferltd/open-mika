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


package gnu.testlet.wonka.security.UnresolvedPermission; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.security.*; // at least the class you are testing ...
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
*  this file contains test for java.security.UnresolvedPermission   <br>
*  --> add some more test when it is easier to build Certificate arrays
*/
public class AcuniaUnresolvedPermissionTest implements Testlet
{
  protected TestHarness th;

  private final static String cname = "java.security.Permission";
  private final static String action = "do_action";
  private final Certificate [] cert = new Certificate[0];
  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.security.UnresolvedPermission");
       test_UnresolvedPermission();
       test_equals();
       test_hashCode();
       test_implies();
       test_getActions();
       test_newPermissionCollection();
       test_toString();
       test_checkGuard();
     }


/**
*  implemented. <br>
*
*/
  public void test_UnresolvedPermission(){
    th.checkPoint("UnresolvedPermission(java.lang.String,java.lang.String,java.lang.String,java.security.cert.Certificate[])");
    String s = "acunia.*";
    UnresolvedPermission bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.getName() , cname , "checking name");
    s = "*";
    bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.getName() , cname , "checking name");
    try {
        new UnresolvedPermission(null,cname, action,cert);
        th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException iae) { th.check(true , "caught exException -- 1"); }     	
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    UnresolvedPermission bp = new UnresolvedPermission(cname,"com.acunia",action,cert);
    th.check(!bp.equals(null), "null is allowed");
    th.check(!bp.equals(new UnresolvedPermission(cname,"com.*",action,cert)), "not equal -- 1");
    th.check(!bp.equals(new UnresolvedPermission(cname,"com.acunia","1",cert)), "not equal --2");
    th.check(!bp.equals(new SecurityPermission("com.acunia","1")), "not equal -- 3");
    th.check(!bp.equals(new UnresolvedPermission("cname","com.acunia",action,cert)), "not equal --4");
    Certificate [] certs = new Certificate[2];
    th.check(!bp.equals(new UnresolvedPermission(cname,"com.acunia",action,certs)), "not equal --5");
    th.check(bp.equals(new UnresolvedPermission(cname,"com.acunia",action,cert)), "equal");
  }

/**
*   implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="com.acunia";
    UnresolvedPermission bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.hashCode() , (s.hashCode()^cname.hashCode()^action.hashCode()) , "cheking hash algorithm ... - 1");
    s = "com.*";
    bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.hashCode() , (s.hashCode()^cname.hashCode()^action.hashCode()) , "cheking hash algorithm ... - 2");
    s = "*";
    bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.hashCode() , (s.hashCode()^cname.hashCode()^action.hashCode()) , "cheking hash algorithm ... - 3");
    bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.hashCode() , (s.hashCode()^cname.hashCode()^action.hashCode()) , "cheking hash algorithm ... - 4");
  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    UnresolvedPermission bp1 = new UnresolvedPermission(cname,"com.acunia",action,cert);
    UnresolvedPermission bp2 = new UnresolvedPermission(cname,"com.*",action,cert);
    th.check( ! bp1.implies(bp2) , "not implied - 1");
    th.check( ! bp2.implies(bp1) , "implied - 1");
    th.check( ! bp1.implies(bp1) , "implied - 2");
    th.check( ! bp2.implies(bp2) , "implied - 3");
    th.check( ! bp1.implies(new SecurityPermission("com.acunia","1")), "not implied -- 2");
  }

/**
*  implemented. <br>
*
*/
  public void test_getActions(){
    th.checkPoint("getActions()java.lang.String");
    String s ="com.acunia";
    UnresolvedPermission bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.getActions() , "" , "getActions returnvalue - 1");
    s = "com.*";
    bp = new UnresolvedPermission(cname,s,action,cert);
    th.check(bp.getActions() , "" , "getActions returnvalue - 2");
    s = "*";
    bp = new UnresolvedPermission(cname,s,s,cert);
    th.check(bp.getActions() , "" , "getActions returnvalue - 3");
  }

/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="com.acunia";
    UnresolvedPermission bp = new UnresolvedPermission(cname,s,action,cert);
    PermissionCollection pc = bp.newPermissionCollection();

    // boolean isReadOnly()
    th.check(! pc.isReadOnly());

    // void setReadOnly()
    pc.setReadOnly();
    th.check(pc.isReadOnly());

    // void add(Permission permission)
    try {
    	pc.add(new UnresolvedPermission(cname,s,"Action2",cert));
    	Enumeration e = pc.elements();
    	th.check(! e.hasMoreElements(),"element added after read only is true");
    }
    catch(SecurityException se) { th.check(true); }
    pc = bp.newPermissionCollection();
    pc.add(bp);
    pc.add(new UnresolvedPermission(cname,"be.*",action,cert));
    pc.add(new UnresolvedPermission(cname,"vm.smartmove",action,cert));
    pc.add(new UnresolvedPermission(cname,"com.acunia.vm*",action,cert));

    // Enumeration elements()
    try  {
    	 Enumeration e = pc.elements();
         boolean found = bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         found |= bp==e.nextElement();
         th.check(found , "references should be kept");
         th.check(! e.hasMoreElements(), "should be empty");
    }
    catch(Exception e) { th.fail("Enumeration elements() was bad"); }

    // String toString()
    s = pc.toString();
    //th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new UnresolvedPermission(cname,"be.*",action,cert).toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new UnresolvedPermission(cname,"vm.smartmove",action,cert).toString()) >= 0 ,"checking toString ...");
    th.check(s.indexOf(new UnresolvedPermission(cname,"com.acunia.vm*",action,cert).toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    th.check(!pc.implies(bp), "implies -- 1 "+bp);
  }

/**
*   implemented. <br>
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    String s ="com.acunia";
    UnresolvedPermission bp = new UnresolvedPermission("java.security.Permission",s,"read", cert);
    th.check( bp.toString() , "(unresolved java.security.Permission "+s+" read)");

         //'(unresolved ClassName name actions)'.
  }
/**
*   not implemented. <br>
*   inherited from Permission (tested here out of convenience)
*   STILL TODO !!!
*/
  public void test_checkGuard(){
    th.checkPoint("checkGuard()void");
  }

  private class ExPermission extends Permission {
   	
         public ExPermission (String n) {
          	super(n);
         }
         public int hashCode() { return 0;}
         public boolean equals(Object o) { return false;}
         public boolean implies(Permission p) { return false;}
         public String getActions() { return "";}

  }
}
