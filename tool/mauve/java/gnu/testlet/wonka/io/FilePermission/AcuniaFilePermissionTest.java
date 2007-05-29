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


package gnu.testlet.wonka.io.FilePermission; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.security.*; // at least the class you are testing ...
import java.io.FilePermission;
import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
*  this file contains test for java.io.FilePermission   <br>
*  we also test checkGuard, toString and getName
*/
public class AcuniaFilePermissionTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.FilePermission");
       test_FilePermission();
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
  public void test_FilePermission(){
    th.checkPoint("FilePermission(java.lang.String,java.lang.String)");
    FilePermission bp = new FilePermission("", "read");
    th.check(bp.getName() , "");
    th.check(bp.getActions() , "read");
    bp = new FilePermission("currentdir","write");
    th.check(bp.getName() , "currentdir");
    th.check(bp.getActions() , "write");
    try {
     	new FilePermission(null,"write");
     	th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException npe) { th.check(true); }
    try {
     	new FilePermission("write",null);
     	th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException npe) { th.check(true); }
    catch(IllegalArgumentException iae) { th.check(true);
    	th.debug("wanted a NullPointerException though");
    }
    try {
     	new FilePermission("write","read write");
     	th.fail("should throw a IllegalArgumentException -- 6");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new FilePermission("write","ready");
     	th.fail("should throw a IllegalArgumentException -- 7");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new FilePermission("write","read, delete, , execute");
     	th.fail("should throw a IllegalArgumentException -- 8");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    	
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    String s = "com.acunia";
    FilePermission bp = new FilePermission(s , "ReAd,  Write \n");
    th.check(!bp.equals(null), "null is allowed");
    th.check(bp.equals(new FilePermission(s,"Write ,Read \n\t ,Write")), "equal -- 1");
    th.check(!bp.equals(new SecurityPermission(s,"read,write")), "not equal -- 1");
    th.check(!bp.equals(new FilePermission(s,"read")), "not equal -- 2");
    bp = new FilePermission("" , "ReAd,  Write \n, EXecutE");
    th.check(bp.equals(new FilePermission("./","read,write,execute")), "equal -- 2");
    th.check(!bp.equals(new FilePermission("./up/","read,write,execute")), "equal -- 3");
    th.check(new FilePermission("./ok","write,reAd"),new FilePermission("ok","write,reAd"),"equal -- 4");

  }

/**
*   implemented. <br>
*   no hashCode calculation algorithm found in spec ...
*   we test the general contract of hashCode ...	
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="com.acunia";
    FilePermission bp = new FilePermission(s,"read");
    th.check(bp.hashCode() , bp.hashCode() , "cheking hashCode ... - 1");
    th.check(bp.hashCode() ,new FilePermission(s,"Read").hashCode());
  }

/**
*  implemented. <br>
*
*/
  public void test_getActions(){
    th.checkPoint("getActions()java.lang.String");
    String s ="com.acunia";
    FilePermission bp = new FilePermission(s,"read \n \f");
    th.check(bp.getActions() ,"read" , "getActions returnvalue - 1");
    s = "com.*";
    bp = new FilePermission(s,"  \t \nwRiTe\f");
    th.check(bp.getActions() , "write", "getActions returnvalue - 2");
    s = "*";
    bp = new FilePermission(s,"WriTe \n ,\t REaD , ReAd ,Write");
    th.check(bp.getActions() ,"read,write" , "getActions returnvalue - 3");
    bp = new FilePermission(s,"Delete\n \t   \f, WRite,\t ExEcUtE\n, \f \nRead \n");
    th.check(bp.getActions() ,"read,write,execute,delete", "getActions returnvalue - 4");

  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    FilePermission bp1 = new FilePermission(".","read");
    FilePermission bp2 = new FilePermission("", "read,delete");
    th.check(! bp1.implies(bp2) , "not implied - 1");
    th.check(! bp2.implies(null) , "not implied - 2");
    th.check(  bp2.implies(bp1) , "implied - 1");
    th.check(! bp1.implies(new SecurityPermission("com.acunia","read")), "not implied -- 3");
    bp2 = new FilePermission("<<ALL FILES>>", "read,execute,write");
    bp1 = new FilePermission("./com.acunia/","read,delete,write,execute");
    th.check(!  bp2.implies(bp1) , "not implied - 4");
    bp1 = new FilePermission("com.acunia","read,write");
    th.check(  bp2.implies(bp1) , "implied - 2");
    bp1 = new FilePermission("./com.acunia","read,delete");
    th.check(! bp2.implies(bp1) , "not implied - 5");
    bp2 = new FilePermission("toppie/-", "read,execute,write");
    bp1 = new FilePermission("toppie/*", "read,write");
    th.check(! bp1.implies(bp2) , "not implied - 6");
    th.check(  bp2.implies(bp1) , "implied - 3");
    bp1 = new FilePermission("toppie/", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 7");
    bp1 = new FilePermission("toppie.file", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 8");
    bp2 = new FilePermission("wr*te", "read,execute,write");
    bp1 = new FilePermission("write", "read,write");
    th.check(! bp2.implies(bp1) , "implied - 4");
    bp2 = new FilePermission("toppie/*", "read,execute,write");
    bp1 = new FilePermission("toppie/", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 9");
    bp1 = new FilePermission("toppie/dir/", "read,write");
    th.check(  bp2.implies(bp1) , "implied - 5");
    bp1 = new FilePermission("toppie/dir/file.txt", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 10");
    bp1 = new FilePermission("toppie/file.txt", "read,write");
    th.check(  bp2.implies(bp1) , "implied - 6");
    bp1 = new FilePermission("toppie.file", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 11");
    bp1 = new FilePermission("toppie/dir/dir2", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 12");
    bp2 = new FilePermission("toppie/file*", "read,execute,write");
    bp1 = new FilePermission("toppie/file1/test.class", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 13");
    bp1 = new FilePermission("toppie/file1/", "read,write");
    th.check(! bp2.implies(bp1) , "not implied - 14");
  }


/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="com/acunia/*";
    FilePermission bp = new FilePermission(s,"read,write,execute");
    PermissionCollection pc = bp.newPermissionCollection();

    // boolean isReadOnly()
    th.check(! pc.isReadOnly(), "not readOnly");

    // void setReadOnly()
    pc.setReadOnly();
    th.check(pc.isReadOnly(), "readOnly");

    // void add(Permission permission)
    try {
    	pc.add(bp);
    	Enumeration e = pc.elements();
    	th.check(! e.hasMoreElements(), "added element");
    }
    catch(SecurityException se) { th.check(true); }
    pc = bp.newPermissionCollection();
    pc.add(bp);
    pc.add(new FilePermission("toppie/-","read,write,execute"));
    pc.add(new FilePermission("toppie/","read,delete"));
    pc.add(bp);
    pc.add(new FilePermission("vm/smartmove/","write"));
    pc.add(bp);
    pc.add(new FilePermission("acunia/vm*","read,write"));
    pc.add(new FilePermission("<<ALL FILES>>/javadir","read,write"));
    pc.add(new FilePermission("<<ALL FILES>>/classdir","read,write"));

    // Enumeration elements()
    Enumeration e = pc.elements();
    try  {
    	
         boolean f=false;
         while (e.hasMoreElements()) {
         	f |= bp == e.nextElement();
         }
         th.check(f , "references should be kept");
    }
    catch(Exception ee) { th.fail("Enumeration elements() was bad"); }
    try  {
    	e.nextElement();	
    	th.fail("Enumeration elements() was bad");
    }
    catch(NoSuchElementException nse) { th.check(true); }


    // String toString()
    s = pc.toString();
    //th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    th.check(pc.implies(bp), "implies -- 1 "+bp);
    th.check(! pc.implies(new SecurityPermission("com/acunia/test","read")), "not implied -- 1");
    FilePermission bp1 = new FilePermission("com/acunia/","read,write,execute");
    th.check(! pc.implies(bp1) , "not implied - 2");
    bp1 = new FilePermission("com.acunia/test.file","read,write,delete");
    th.check(! pc.implies(bp1) , "not implied - 3");
    bp1 = new FilePermission("com/acunia/","read");
    th.check(! pc.implies(bp1) , "not implied - 4");
    bp1 = new FilePermission("com/acuniaCo/","read");
    th.check(! pc.implies(bp1) , "not implied - 5");
    bp1 = new FilePermission("com/acunia/dir/","read");
    th.check(  pc.implies(bp1) , "implied - 1");
    bp1 = new FilePermission("com/acunia/file1","read");
    th.check(  pc.implies(bp1) , "implied - 2");
    bp1 = new FilePermission("com/acunia/dir/file.txt","read");
    th.check(! pc.implies(bp1) , "not implied - 6");

    bp1 = new FilePermission("toppie/", "read,write");
    th.check(! pc.implies(bp1) , "not implied - 7");
    bp1 = new FilePermission("toppie/", "read,delete");
    th.check(  pc.implies(bp1) , "implied - 3a");
    bp1 = new FilePermission("toppie/*", "read,write");
    th.check(  pc.implies(bp1) , "implied - 3b");
    bp1 = new FilePermission("toppie.file", "read,write");
    th.check(! pc.implies(bp1) , "not implied - 9");
    bp1 = new FilePermission("toppie/dir/file.txt", "read,write");
    th.check(  pc.implies(bp1) , "implied - 4");
    bp1 = new FilePermission("toppie/dir/", "read,write");
    th.check(  pc.implies(bp1) , "implied - 5");
    bp1 = new FilePermission("toppie/file.txt", "read,write");
    th.check(  pc.implies(bp1) , "implied - 6");

    bp1 = new FilePermission("vm/smartmove/", "read");
    th.check(! pc.implies(bp1) , "not implied - 10");
    bp1 = new FilePermission("vm/smartmove/", "delete");
    th.check(! pc.implies(bp1) , "not implied - 11");
    bp1 = new FilePermission("vm/smartmove/", "execute");
    th.check(! pc.implies(bp1) , "not implied - 12");
    bp1 = new FilePermission("vm/smartmove/", "write");
    th.check(  pc.implies(bp1) , "implied - 7");

    bp1 = new FilePermission("acunia/vm/", "read");
    th.check(! pc.implies(bp1) , "not implied - 13");
    bp1 = new FilePermission("acunia/vm*", "read");
    th.check(  pc.implies(bp1) , "implied - 8");
    bp1 = new FilePermission("acunia/vmx/file1", "read");
    th.check(! pc.implies(bp1) , "not implied - 14");

    pc.add(new FilePermission("<<ALL FILES>>","read,execute"));
    bp1 = new FilePermission("<<ALL FILES>>", "read");
    th.check(  pc.implies(bp1) , "implied - 9");
    bp1 = new FilePermission("<<ALL FILES>>/not", "read,write");
    th.check(! pc.implies(bp1) , "not implied - 15");
    bp1 = new FilePermission("<<ALL FILES>>/classdir", "read,write");
    th.check(  pc.implies(bp1) , "implied - 10");

    pc.add(new FilePermission("toppie/dir/file","read,delete"));
    bp1 = new FilePermission("toppie/dir/file", "read,write,execute,delete");
    th.check(  pc.implies(bp1) , "implied - 11");

    pc.add(new FilePermission("toppieTo/-","read,delete"));
    bp1 = new FilePermission("toppieTo/dir/", "read,write,execute,delete");
    th.check(! pc.implies(bp1) , "not implied - 16");
    bp1 = new FilePermission("toppieTo/dir/file", "read,delete");
    th.check(  pc.implies(bp1) , "implied - 12");
  }

/**
*   implemented. <br>
*   inherited from Permission (tested here out of convenience)
*/
  public void test_toString(){
    th.setclass("java.security.Permission");
    th.checkPoint("toString()java.lang.String");
    String s ="com.acunia";
    FilePermission bp = new FilePermission(s,"write,reAd");
    th.check( bp.toString() , "("+bp.getClass().getName()+" "+s+" "+"read,write)");
         //'(ClassName name actions)'.
  }
}
