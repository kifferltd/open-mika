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


package gnu.testlet.wonka.net.SocketPermission; 
//complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.security.*; // at least the class you are testing ...
import java.net.SocketPermission;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
*  this file contains test for java.net.SocketPermission   <br>
*  we also test checkGuard, toString and getName
*/
public class AcuniaSocketPermissionTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.net.SocketPermission");
       test_SocketPermission();
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
  public void test_SocketPermission(){
    th.checkPoint("SocketPermission(java.lang.String,java.lang.String)");
    SocketPermission bp = new SocketPermission("", "accept");
    th.check(bp.getName() , "localhost");
    th.check(bp.getActions() , "accept,resolve");
    bp = new SocketPermission("localhost","resolve");
    th.check(bp.getName() , "localhost");
    th.check(bp.getActions() , "resolve");
    bp = new SocketPermission("127.0.0.1:2-","resolve");
    th.check(bp.getName() , "127.0.0.1:2-");
    try {
     	new SocketPermission(null,"connect");
     	th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException npe) { th.check(true); }
    try {
     	new SocketPermission("connect",null);
     	th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException npe) { th.check(true); }
    catch(IllegalArgumentException iae) { th.check(true);
    	th.debug("wanted a NullPointerException though");
    }
    /*
    try {
     	bp = new SocketPermission("*:12345678","accept");
     	th.fail("should throw a IllegalArgumentException -- 3");
     	th.debug("just created :"+bp);
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    */
    try {
     	new SocketPermission("1.2.3.4:567-89","accept");
     	th.fail("should throw a IllegalArgumentException -- 4");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	bp = new SocketPermission("23-445:1.1.56.3","accept");
     	th.fail("should throw a IllegalArgumentException -- 5");
     	th.debug("just created :"+bp);
    }
    catch(IllegalArgumentException iae) { th.check(true); }

    try {
     	new SocketPermission("","accept connect");
     	th.fail("should throw a IllegalArgumentException -- 6");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("","accepty");
     	th.fail("should throw a IllegalArgumentException -- 7");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 8");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("222.255.3.4,123.2.3.4:34","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 9");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("*:23,25","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 10");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("234.3.4.5:234:345","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 11");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission(" 123. 1 .32 . 1\n","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 12");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("*.sun.*","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 13");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("sun.*","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 14");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("*.*","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 15");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("*.243.33.4","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 16");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("123.256.34.4","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 17");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("123.25.34","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 18");
    }
    catch(IllegalArgumentException iae) { th.check(true); }   	
    try {
     	new SocketPermission("123.0.256.4","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 19");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("123..1.1.256","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 20");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("123.1.1.25.6","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 21");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("123..1.1.25.6","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 22");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission(":123","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 23");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
    try {
     	new SocketPermission("1.2.2.3:3 3","accept, resolve, , listen");
     	th.fail("should throw a IllegalArgumentException -- 24");
    }
    catch(IllegalArgumentException iae) { th.check(true); }
  }

/**
*  implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    String s = "www.acunia.com:20-";
    SocketPermission bp = new SocketPermission(s , "AcCePt,  CoNnEcT \n");
    th.check(!bp.equals(null), "null is allowed");
    th.check(bp.equals(new SocketPermission(s,"CoNnEcT ,AcCePt \n\t ,CoNnEcT")), "equal -- 1");
    th.check(!bp.equals(new SecurityPermission(s,"accept,connect")), "not equal -- 1");
    th.check(!bp.equals(new SocketPermission(s,"accept")), "not equal -- 2");
    th.check(bp.equals(new SocketPermission(s,"accept,connect,resolve")), "equal -- 2");
    bp = new SocketPermission("" , "AcCePt,  CoNnEcT \n, LiStEn");
    th.check(bp,(new SocketPermission("localhost","accept,connect,listen")), "equal -- 3");
    th.check(bp,(new SocketPermission("127.0.0.1","accept,connect,listen")), "equal -- 4");
    th.check(!bp.equals(new SocketPermission("*:-3","accept,connect,listen")), "not equal -- 2");
    th.check(!bp.equals(new SocketPermission("127.0.0.1","connect,listen")), "not equal -- 3");
  }

/**
*   implemented. <br>
*   no hashCode calculation algorithm found in spec ...
*   we test the general contract of hashCode ...	
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    String s ="*.acunia.com";
    SocketPermission bp = new SocketPermission(s,"accept,listen");
    th.check(bp.hashCode() , s.hashCode() , "cheking hashCode ... - 1");
    th.check(bp.hashCode() ,new SocketPermission(s,"AcCePt").hashCode());
    s ="*";
    bp = new SocketPermission(s,"accept");
    th.check(bp.hashCode() , s.hashCode() , "cheking hashCode ... - 1");
    th.check(bp.hashCode() ,new SocketPermission(s,"AcCePt,cOnnEct").hashCode());
  }

/**
*  implemented. <br>
*
*/
  public void test_getActions(){
    th.checkPoint("getActions()java.lang.String");
    String s ="www.acunia.com:80-99";
    SocketPermission bp = new SocketPermission(s,"connect \n \f");
    th.check(bp.getActions() ,"connect,resolve" , "getActions returnvalue - 1");
    s = "*.com:12";
    bp = new SocketPermission(s,"  \t \nCoNnEcT\f");
    th.check(bp.getActions() , "connect,resolve", "getActions returnvalue - 2");
    s = "*:232-";
    bp = new SocketPermission(s,"CoNnEcT \n ,\t AcCePt , AcCePt ,CoNnEcT");
    th.check(bp.getActions() ,"connect,accept,resolve" , "getActions returnvalue - 3");
    bp = new SocketPermission(s,"ReSoLvE\n \t   \f, CoNnEcT,\t LiStEn\n, \f \nAcCePt \n");
    th.check(bp.getActions() ,"connect,listen,accept,resolve", "getActions returnvalue - 4");

  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
// must be an instance of SocketPermission
    SocketPermission bp1 = new SocketPermission("localhost:80","accept");
    SocketPermission bp2 = new SocketPermission("", "accept,resolve");
    th.check(! bp1.implies(bp2) , "not implied - 1");
    th.check(! bp2.implies(null) , "not implied - 2");
    th.check(  bp2.implies(bp1) , "implied - 1");
    th.check(! bp1.implies(new SecurityPermission("com.acunia","accept")), "not implied -- 3");
// actions should be a subset
    bp2 = new SocketPermission("", "accept,resolve,connect");
    bp1 = new SocketPermission("","accept,resolve,connect,listen");
    th.check(!  bp2.implies(bp1) , "not implied - 4");
    bp1 = new SocketPermission("","accept,connect");
    th.check(  bp2.implies(bp1) , "implied - 2");
    bp1 = new SocketPermission("","accept,listen");
    th.check(! bp2.implies(bp1) , "not implied - 5");
// port number must be a subset
    bp2 = new SocketPermission("localhost:30-30000", "accept,resolve,connect");
    bp1 = new SocketPermission("localhost:300-3000","accept,resolve,connect");
    th.check(! bp1.implies(bp2) , "not implied - 6");
    th.check(  bp2.implies(bp1) , "implied - 3");
    bp1 = new SocketPermission("localhost:-3000","accept,resolve,connect");
    th.check(! bp2.implies(bp1) , "not implied - 7");
    bp1 = new SocketPermission("localhost","accept,resolve,connect");
    th.check(! bp2.implies(bp1) , "not implied - 8");
    bp1 = new SocketPermission("localhost:300-","accept,resolve,connect");
    th.check(! bp2.implies(bp1) , "not implied - 9");
    bp1 = new SocketPermission("localhost:3000","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 4");
// one of this four cases should be true ...
// 1. this permission is initialized with a numeric IP adress and one of p's adresses is equal to it
    bp2 = new SocketPermission("192.168.0.1", "accept,resolve,connect");
    bp1 = new SocketPermission("192.168.0.1:80","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 5");
    bp1 = new SocketPermission("192.168.1.1:80","accept,resolve,connect");
    th.check(! bp2.implies(bp1) , "not implied - 10");
// 2. this object is *.wathever and p is something.whatever
    bp2 = new SocketPermission("*.sun.com", "accept,resolve,connect");
    bp1 = new SocketPermission("java.sun.com:80","accept,resolve,connect");
    //th.check(  bp2.implies(bp1) , "implied - 6");
    bp1 = new SocketPermission("*.www.sun.com:80","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 7");
    bp1 = new SocketPermission("sun.com:80","accept,resolve,connect");
    th.check(!  bp2.implies(bp1) , "implied - 7bis");  //there is a host named sun.com
    bp2 = new SocketPermission("*.acunia.com", "accept,resolve,connect");
    bp1 = new SocketPermission("acunia.com:80","accept,resolve,connect");
    th.check(!  bp2.implies(bp1) , "not implied - 11"); //no host named com.acunia
// 3. if this objects hostname IP adresses equals one of p's IP adresses
    bp2 = new SocketPermission("*", "accept,resolve,connect");
    bp1 = new SocketPermission("192.168.0.1","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 8");
// 4. if this canonical name equals p's canonical name
    bp2 = new SocketPermission("www.sun.com", "accept,resolve,connect");
    bp1 = new SocketPermission("www.sun.com:80","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 9");
    bp2 = new SocketPermission("*.sun.com", "accept,resolve,connect");
    bp1 = new SocketPermission("*.sun.com:80","accept,resolve,connect");
    th.check(  bp2.implies(bp1) , "implied - 10");
    bp1 = new SocketPermission("*.sun.us:80","accept,resolve,connect");
    th.check(!  bp2.implies(bp1) , "not implied - 12");
  }


/**
*   implemented. <br>
*   the returned PermissionCollection should also be tested since it could be done
*   by an innerclass, or any other class extending PermissionCollection.
*/
  public void test_newPermissionCollection(){
    th.checkPoint("newPermissionCollection()java.security.PermissionCollection");
    String s ="*.sun.com";
    SocketPermission bp = new SocketPermission(s,"connect");
    PermissionCollection pc = bp.newPermissionCollection();

    // boolean isAcceptOnly()
    th.check(! pc.isReadOnly(), "not readOnly");

    // void setAcceptOnly()
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
    pc.add(new SocketPermission("192.168.0.1:30-30000","accept,connect,listen"));
    pc.add(bp);
    pc.add(new SocketPermission("java.sun.com:80-100","connect,accept,listen"));
    pc.add(bp);
    pc.add(new SocketPermission("java.sun.com:90-120","accept,connect"));
    pc.add(new SocketPermission("localhost:90-120","connect"));

    // Enumeration elements()
    boolean f = false;
    Enumeration e = pc.elements();
    try  {
         while (e.hasMoreElements()) {
         	f |= bp == e.nextElement();
         }
         th.check(f , "references should be kept");
    }
    catch(Exception ee) { th.fail("Enumeration elements() was bad"); }
    try {
	e.nextElement();
	th.fail("Enumeration elements() was bad");	
    }
    catch(NoSuchElementException nse) { th.check(true); }
    // String toString()
    s = pc.toString();
    th.debug(s);
    th.check(s.indexOf(bp.toString()) >= 0 ,"checking toString ...");

    // boolean implies(Permission permission)
    SocketPermission bp1 = new SocketPermission("localhost:100","connect");
    th.check(  pc.implies(bp1) , "implied - 1");
    th.check(! pc.implies(new SecurityPermission("com.acunia","accept")), "not implied -- 1");
// actions should be a subset
    bp1 = new SocketPermission("localhost:100","accept,resolve,connect,listen");
    th.check(!  pc.implies(bp1) , "not implied - 2");
    bp1 = new SocketPermission("localhost:100","resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 2");
    bp1 = new SocketPermission("localhost:100","accept,listen");
    th.check(! pc.implies(bp1) , "not implied - 3");
// port number must be a subset
    bp1 = new SocketPermission("192.168.0.1:300-3000","accept,resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 3");
    bp1 = new SocketPermission("192.168.0.1:-3000","accept,resolve,connect");
    th.check(! pc.implies(bp1) , "not implied - 4");
    bp1 = new SocketPermission("192.168.0.1","accept,resolve,connect");
    th.check(! pc.implies(bp1) , "not implied - 5");
    bp1 = new SocketPermission("192.168.0.1:300-","accept,resolve,connect");
    th.check(! pc.implies(bp1) , "not implied - 6");
    bp1 = new SocketPermission("192.168.0.1:3000","accept,resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 4");
// one of this four cases should be true ...
// 1. this permission is initialized with a numeric IP adress and one of p's adresses is equal to it
    bp1 = new SocketPermission("192.168.0.1:80","accept,resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 5");
    bp1 = new SocketPermission("192.168.1.1:80","accept,resolve,connect");
    th.check(! pc.implies(bp1) , "not implied - 7");
// 2. this object is *.wathever and p is something.whatever
    bp1 = new SocketPermission("java.sun.com:90-110","accept,resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 6");
    bp1 = new SocketPermission("java.sun.com:80-120","accept,listen,connect");
    th.check(! pc.implies(bp1) , "not implied - 8");
    bp1 = new SocketPermission("*.www.sun.com:80","resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 7");
    bp1 = new SocketPermission("java.sun.com:80","resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 7bis");  //there is a host named sun.com
    bp1 = new SocketPermission("acunia.com:80","accept,resolve,connect");
    th.check(!  pc.implies(bp1) , "not implied - 9"); //no host named com.acunia
// 3. if this objects hostname IP adresses equals one of p's IP adresses
    bp1 = new SocketPermission("127.0.0.1:100","resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 8");
// 4. if this canonical name equals p's canonical name
    bp1 = new SocketPermission("www.sun.com:80","resolve,connect");
    //th.check(  pc.implies(bp1) , "implied - 9");
    bp1 = new SocketPermission("*.sun.com:80","resolve,connect");
    th.check(  pc.implies(bp1) , "implied - 10");
    bp1 = new SocketPermission("developer.java.sun.us:80","resolve,connect");
    th.check(!  pc.implies(bp1) , "not implied - 10");

  }

/**
*   implemented. <br>
*   inherited from Permission (tested here out of convenience)
*/
  public void test_toString(){
    th.setclass("java.security.Permission");
    th.checkPoint("toString()java.lang.String");
    String s ="*.acunia.com";
    SocketPermission bp = new SocketPermission(s,"AcCePt");
    th.check( bp.toString() , "("+bp.getClass().getName()+" "+s+" accept,resolve)");

         //'(ClassName name actions)'.
  }
}
