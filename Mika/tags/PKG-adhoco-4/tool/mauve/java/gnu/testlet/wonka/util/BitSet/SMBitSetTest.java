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


package gnu.testlet.wonka.util.BitSet;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.BitSet <br>
*  in jdk10.java are some basic tests on BitSet from Mauve libraries <br>
*
*/
public class SMBitSetTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.BitSet");
       test_BitSet();
       test_clone();
       test_equals();
       test_hashCode();
       test_toString();
       test_and();
       test_andNot();
       test_clear();
       test_get();
       test_or();
       test_set();
       test_xor();
       test_length();
       test_size();

     }


/**
* implemented.
*
*/
  public void test_BitSet(){
    th.checkPoint("BitSet()");
    BitSet bs = new BitSet();
    th.check(bs.size() == 64 , "new BitSet has size 64");
    boolean ok = true;
    for (int i=0; i < 64 ; i++)
    { if (bs.get(i) ) ok =false; }
    th.check(ok ,"all bits should be 0 -- got:"+bs);

    th.checkPoint("BitSet(int)");
    bs = new BitSet(1);
    th.check(bs.size() == 64 , "new BitSet has size 64");
    ok = true;
    for (int i=0; i < 64 ; i++)
    { if (bs.get(i) ) ok =false; }
    th.check(ok ,"all bits should be 0 -- got:"+bs);
    bs = new BitSet(65);
    th.check(bs.size() == 128 , "new BitSet has size 128");
    bs = new BitSet(129);
    th.check(bs.size() ==192  , "new BitSet has size 192");
    bs = new BitSet(1024);
    th.check(bs.size() == 1024 , "new BitSet has size 1024");
    bs = new BitSet(0);
    th.check(bs.size() == 0 , "new BitSet has size 0");
    try { new BitSet(-1);
    	  th.fail("should throw NegativeArraySizeException");
    	}
    catch(NegativeArraySizeException ne) {th.check(true); }

  }


/**
* implemented.
*
*/
  public void test_clone(){
    th.checkPoint("clone()java.lang.Object");
    BitSet bs = new BitSet(3);
    int i;
    for (i = 0; i < 64 ; i= i+2) { bs.set(i); }
    BitSet bsc = (BitSet) bs.clone();
    boolean ok = true;
    for (i = 0; i < 64 ; i++)
    { if (bsc.get(i) !=  (((i % 2) == 0) ? true : false )) ok = false ;}
    th.check( ok , "all bits should be set" );
    th.check( bsc.size()==64 ,"size is the same !!!");
    bs = new BitSet(0);
    bsc = (BitSet) bs.clone();
    bs.set(4);
    th.check( bsc.size() == 0 , "changes in the original don't affect the clone");
  }


/**
* implemented.
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    BitSet bs = new BitSet(35);
    int i;
    for (i = 0; i < 64 ; i= i+2) { bs.set(i); }
    th.check( ! bs.equals(null) , "returns false if compared to null" );
    th.check( ! bs.equals("dsf") , "returns false if compared to another Object" );
    Vector v = new Vector();
    for (i = 0; i < 64 ; i= i+2) { v.add(new Integer(1)); v.add(new Integer(0));}
    th.check( ! bs.equals(v) , "returns false if compared to a vector" );
    BitSet bsc = (BitSet) bs.clone();
    th.check(bsc.equals(bs) , "a BitSet equals his clone");
    bs.set(1);
    th.check(! bsc.equals(bs) , "one different bit ==> not equal");
    bs.clear(1);
    bsc.clear(100);
    th.check(bsc.equals(bs) , "different size still can be equal -- 1");
    th.check(bs.equals(bsc) , "different size still can be equal -- 2");
    bsc.set(127);
    th.check(!bsc.equals(bs) , "different size don't have to be equal -- 1");
    th.check(!bs.equals(bsc) , "different size don't have to be equal -- 2");

  }


/**
* implemented.	<br>
* the hashCode is only depending on the bits set in the BitSet. <br>
* this means that two equal bitSets (with different size) still have the same <br>
* hashcode
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    BitSet bs = new BitSet(34);
    th.check(bs.hashCode() == 1234 , "checking hashCode for empty BitSet size 64");
    bs = new BitSet(0);
    th.check(bs.hashCode() == 1234 , "checking hashCode for empty BitSet size 0");
    int i;
    for (i=0 ; i < 8 ; i++) {bs.set(i);}
    th.check(bs.hashCode() == 1069 , "checking hashCode for  BitSet  FF");

  }


/**
* implemented.
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    BitSet bs = new BitSet();
    th.check(bs.toString(), "{}" , "check empty BitSet");
    bs.set(1);
    th.check(bs.toString(), "{1}", "check BitSet string representation -- 1");
    bs.set(60);
    th.check(bs.toString(), "{1, 60}" , "check BitSet string representation -- 1");
    bs.set(15);
    th.check(bs.toString(), "{1, 15, 60}", "check BitSet string representation -- 1");

  }


/**
* implemented.
*
*/
  public void test_and(){
    th.checkPoint("and(java.util.BitSet)void");
    BitSet bs1 = new BitSet();
    BitSet bs2 = new BitSet();
    int i;
    for (i=0 ; i < 64 ; i++ ) { bs2.set(i); }
    try { bs1.and(null);
    	  th.fail("Should throw a NullPointerException");
        }
    catch( NullPointerException ne) { th.check(true); }
    bs2.and(bs1);
    th.check( bs1.equals(bs2) , "all ones anded to zeros give zeros");
    bs1.set(1); bs2.set(1);
    bs1.and(bs2);
    th.check( bs1.get(1) && bs2.equals(bs1), "checking and -- 1");
    for (i=0 ; i < 64 ; i++ ) { bs2.set(i); bs1.set(i);}
    for (i=64 ; i < 128 ; i++ ) { bs2.set(i); }
    bs1.clear(2); bs1.clear(20) ;bs1.clear(25); bs1.clear(28);
    Object o = bs1.clone();
    bs1.and(bs2);
    th.check(bs1.equals(o) , "extra bits from bs2 are unused");
    th.check(bs1.size() == 64 ,"BitSet is not grown to size bs2");
    bs2.and(bs1);
    th.check(bs1.equals(bs2) , "extra bits in bs2 are cleared");    	
  }


/**
* implemented.	<br>
* --> since JDK 1.2
*/
  public void test_andNot(){
    th.checkPoint("andNot(java.util.BitSet)void");
    BitSet bs1 = new BitSet();
    BitSet bs2 = new BitSet();
    int i;
    for (i=0 ; i < 64 ; i++ ) { bs2.set(i); }
    try { bs1.and(null);
    	  th.fail("Should throw a NullPointerException");
        }
    catch( NullPointerException ne) { th.check(true); }
    BitSet bs3 = (BitSet) bs2.clone();
    bs2.andNot(bs1);
    th.check( bs3.equals(bs2) , "all ones andnotted to zeros give ones");
    bs3.andNot(bs2);
    th.check( !bs3.get(1) && bs3.equals(bs1), "checking andNot -- 1");
    for (i=0 ; i < 64 ; i++ ) { bs2.set(i); bs1.set(i);}
    for (i=64 ; i < 128 ; i++ ) { bs2.set(i); }
    BitSet bs4 = (BitSet)bs2.clone();
    bs3 = (BitSet)bs1.clone();
    bs4.xor(bs1);
    bs1.andNot(bs2);
    th.check(bs1.equals(new BitSet(64)) , "extra bits from bs2 are unused");
    th.check(bs1.size() == 64 ,"BitSet is not grown to size bs2");
    bs2.andNot(bs3);
    th.check(bs4.equals(bs2) , "extra bits in bs2 are not altered");    	
    bs1.clear(0); bs2.clear(0);
    bs2.andNot(bs1);
    th.check(!bs2.get(0) , "checking or -- 1");
    bs2.set(0);
    bs2.andNot(bs1);
    th.check(bs2.get(0) , "checking or -- 2");
    bs1.set(0);
    bs2.andNot(bs1);
    th.check(!bs2.get(0) , "checking or -- 3");
    bs2.andNot(bs1);
    th.check(!bs2.get(0) , "checking or -- 4");

  }


/**
* implemented.
*
*/
  public void test_clear(){
    th.checkPoint("clear(int)void");
    BitSet bs = new BitSet();
    Object o = bs.clone();
    int i;
    for (i=0 ; i < 64 ; i++ ) { bs.set(i); bs.clear(i);}
    th.check(bs.equals(o) , "checking set/clear");
    bs.set(4);
    th.check( bs.get(4) ,"make sure the set worked" );
    bs.clear(4);
    th.check( !bs.get(4) ,"make sure the clear worked -- 1" );
    bs.clear(4);
    th.check( !bs.get(4) ,"make sure the clear worked -- 2" );
    bs.clear(123);
    th.check(bs.size() , 64 ,"clear can't make the set grow -- got: "+bs.size());
    try { bs.clear(-1);
    	  th.fail("should throw an IndexsOutOfBoundsException");
        }
    catch(IndexOutOfBoundsException ie) {th.check(true);}
    bs.set(134);
    th.check(bs.size() == 192 , "growing BitSet");
    bs.clear(134);
    th.check(bs.size() == 192 , "clear cannot cause the BitSet to downsize");
    bs = new BitSet(0);
    try { bs.clear(0);
          bs.clear(64);
          bs.clear(128);
          bs.set(146);
          bs.clear(146);
          th.check(true);
        }
    catch(Exception e) { th.fail("should not throw an exception");}
  }


/**
* implemented.
*
*/
  public void test_get(){
    th.checkPoint("get(int)boolean");
    BitSet bs = new BitSet();
    try { bs.get(-1);
    	  th.fail("should throw an IndexsOutOfBoundsException");
        }
    catch(IndexOutOfBoundsException ie) {th.check(true);}
    th.check(!bs.get(Integer.MAX_VALUE) , "returns false if pos > size");
    bs.set(3);
    th.check(bs.get(3) , "returns true if pos is set");
    bs.clear(3);
    th.check(!bs.get(3) , "returns false if pos is cleared");
    th.check(!bs.get(0) , "returns false if pos is cleared/or not set -- 1");
    th.check(!bs.get(63) , "returns false if pos is cleared/or not set -- 2");
  }


/**
* implemented.
*
*/
  public void test_or(){
    th.checkPoint("or(java.util.BitSet)void");
    BitSet bs1 = new BitSet();
    BitSet bs2 = new BitSet();
    try { bs1.or(null);
    	  th.fail("Should throw a NullPointerException");
    }
    catch( NullPointerException ne) { th.check(true); }
    int i;
    for (i=0 ; i < 64 ; i++ ) { bs2.set(i); }
    bs1.or(bs2);
    th.check( bs1, bs2 , "all ones ored with zeros give ones");
    for (i=64 ; i < 128 ; i++ ) { bs2.set(i); }
    BitSet bs3 = new BitSet(3);
    BitSet bs4 = new BitSet(127);
    bs3.or(bs2);
    th.check(bs1, bs3 , "extra bits from bs2 are unused -- got: "+bs3);
    bs4.or(bs1);
    th.check(bs4.equals(bs1) , "extra bits in bs4 are left"+bs1);
    bs1.clear(0); bs2.clear(0);
    bs2.or(bs1);
    th.check(!bs2.get(0) , "checking or -- 1");
    bs1.set(0);
    bs2.or(bs1);
    th.check(bs2.get(0) , "checking or -- 2");
    bs2.or(bs1);
    th.check(bs2.get(0) , "checking or -- 3");
    bs1.clear(0);
    bs2.or(bs1);
    th.check(bs2.get(0) , "checking or -- 4");
  }


/**
* implemented.	<br>
* is tested together with clear
*/
  public void test_set(){
    th.checkPoint("set(int)void");
    BitSet bs = new BitSet(3);
    try { bs.clear(-1);
    	  th.fail("should throw an IndexsOutOfBoundsException");
        }
    catch(IndexOutOfBoundsException ie) {th.check(true);}
    bs = new BitSet(0);
    bs.set(0);
    bs.set(23);
  }


/**
*implemented.
*
*/
  public void test_xor(){
    th.checkPoint("xor(java.util.BitSet)void");
    BitSet bs1 = new BitSet();
    BitSet bs2 = new BitSet();
    int i;
    for (i=0 ; i < 32 ; i++ ) { bs2.set(i); }
    try { bs1.xor(null);
    	  th.fail("Should throw a NullPointerException");
        }
    catch( NullPointerException ne) { th.check(true); }
    bs1.xor(bs2);
    th.check( bs1.equals(bs2) , "checking global xor");
    for (i=64 ; i < 128 ; i++ ) { bs1.set(i); }
    BitSet bs3 = new BitSet(3);
    bs3.xor(bs1);
    th.check(bs2.equals(bs3) , "extra bits from bs1 are unused -- got: "+bs3);
    bs1.xor(bs2);
    boolean ok=true;
    for (i=0 ; i < 64 ; i++ ) { if (bs1.get(i)) ok = false; }
    for (i=64 ; i < 128 ; i++ ) { if (!bs1.get(i)) ok = false; }
    if (!ok) th.debug("got wrong bitpattern:"+bs1);
    th.check(ok , "extra bits in bs4 are left");
    bs1.clear(0); bs2.clear(0);
    bs2.xor(bs1);
    th.check(!bs2.get(0) , "checking xor -- 1");
    bs1.set(0);
    bs2.xor(bs1);
    th.check(bs2.get(0) , "checking xor -- 2");
    bs2.xor(bs1);
    th.check(!bs2.get(0) , "checking xor -- 3");
    bs2.xor(bs1);
    th.check(bs2.get(0) , "checking xor -- 4");
    bs1.clear(0);
    bs2.xor(bs1);
    th.check(bs2.get(0) , "checking xor -- 5");

  }


/**
* implemented.	<br>
* --> since jdk 1.2
*/
  public void test_length(){
    th.checkPoint("length()int");
    BitSet bs = new BitSet(0);
    th.check(bs.length(),0);
    bs.clear(100);
    th.check(bs.length(),0);
    bs.set(50);	
    th.check(bs.length(),51);
    bs.set(120);
    th.check(bs.length(),121);
    bs.set(150);
    th.check(bs.length(),151);
    bs.set(150);
    th.check(bs.length(),151);
    bs.clear(150);
    th.check(bs.length(),121);
    bs.clear(120);
    th.check(bs.length(),51);
    bs.clear(50);
    th.check(bs.length(),0);

  }


/**
* implemented.	<br>
* much test will rely on size() <br>
* if size fails, lots of the other tests will also fail !
*/
  public void test_size(){
    th.checkPoint("size()int");
    BitSet bs = new BitSet();
    bs.set(56);
    th.check( bs.size() == 64);
    bs.set(64);
    th.check( bs.size() == 128);

  }

}
