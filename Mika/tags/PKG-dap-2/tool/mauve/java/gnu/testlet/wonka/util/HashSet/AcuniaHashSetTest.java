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


package gnu.testlet.wonka.util.HashSet;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*;

/**
*  this file contains test for java.util.HashSet   <br>
*
*/
public class AcuniaHashSetTest implements Testlet
{

  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.HashSet");
       test_contains();
       test_isEmpty();
       test_size();
       test_add();
       test_clear();
       test_remove();
       test_iterator();
       test_clone();
       test_behaviour();
     }

/**
* implemented. <br>
*
*/
  public void test_HashSet(){
    th.checkPoint("HashSet(java.util.Collection)");
    Vector v = new Vector();
    v.add("a");	v.add(null); v.add("c"); v.add(this); v.add("a"); v.add(null);
    HashSet hs = new HashSet(v);
    th.check(hs.size(), 4 , "checking size -- 1");
    th.check(hs.contains(this) , "checking elements -- 1");
    th.check(hs.contains(null) , "checking elements -- 2");
    try {
     	new HashSet(null);
     	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException e) { th.check(true);}
    th.checkPoint("HashSet(int)");
    try {
     	new HashSet(-1);
     	th.fail("should throw an IllegalArgumentException");
    }
    catch(IllegalArgumentException e) { th.check(true);}
    th.checkPoint("HashSet(int,int)");
    try {
     	new HashSet(-1,0.5f);
     	th.fail("should throw an IllegalArgumentException");
    }
    catch(IllegalArgumentException e) { th.check(true);}
    try {
     	new HashSet(1,-0.5f);
     	th.fail("should throw an IllegalArgumentException");
    }
    catch(IllegalArgumentException e) { th.check(true);}

  }


/**
* implemented. <br>
*
*/
  public void test_contains(){
    th.checkPoint("contains(java.lang.Object)boolean");
    HashSet al = new HashSet();
    th.check(!al.contains(null),"checking empty List -- 1");
    th.check(!al.contains(al)  ,"checking empty List -- 2");
    al.add("a");
    al.add(null);
    al.add("6");
    th.check( al.contains(null), "check contains ... -- 1");
    th.check( al.contains("a") , "check contains ... -- 2");
    th.check(!al.contains(this), "check contains ... -- 3");
    al.remove("6");
    al.remove(null);
    th.check(!al.contains(null), "check contains ... -- 4");
    th.check(!al.contains("6") , "check contains ... -- 5");
  }

/**
* implemented. <br>
*
*/
  public void test_isEmpty(){
    th.checkPoint("isEmpty()boolean");
    HashSet hs = new HashSet();
    th.check( hs.isEmpty(), "isEmpty -- 1");
    hs.add("a");
    th.check(!hs.isEmpty(), "isEmpty -- 2");
    hs.clear();
    th.check( hs.isEmpty(), "isEmpty -- 3");
    hs.add("B");
    th.check(!hs.isEmpty(), "isEmpty -- 4");
    hs.remove("B");
    th.check( hs.isEmpty(), "isEmpty -- 5");
  }

/**
* implemented. <br>
*
*/
  public void test_size(){
    th.checkPoint("size()int");
    HashSet hs = new HashSet();
    th.check(hs.size() , 0 , "verifying size -- 1");
    hs.add("a");
    th.check(hs.size() , 1 , "verifying size -- 2");
    hs.add("B");
    th.check(hs.size() , 2 , "verifying size -- 3");
    hs.add("B");
    th.check(hs.size() , 2 , "verifying size -- 4");
    hs.add(null);
    th.check(hs.size() , 3 , "verifying size -- 5");
    hs.remove("not");
    th.check(hs.size() , 3 , "verifying size -- 6");
    hs.remove("B");
    th.check(hs.size() , 2 , "verifying size -- 7");
    hs.clear();
    th.check(hs.size() , 0 , "verifying size -- 8");


  }

/**
* implemented. <br>
*
*/
  public void test_add(){
    th.checkPoint("add(java.lang.Object)boolean");
    HashSet hs = new HashSet();
    th.check(hs.add("a") , "checking return value -- 1");
    th.check(hs.add("c") , "checking return value -- 2");
    th.check(!hs.add("a") , "checking return value -- 3");
    th.check(hs.add(null) , "checking return value -- 4");
    th.check(!hs.add(null) , "checking return value -- 5");
    th.check(hs.size() == 3, "duplicate elements aren't added");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    HashSet hs = new HashSet();
    hs.clear();
    hs.add("a");
    hs.clear();
    th.check(hs.size() , 0 , "HashSet is cleared");
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(java.lang.Object)boolean");
    HashSet hs = new HashSet();
    hs.clear();
    hs.add("a");
    hs.clear();
    th.check(!hs.remove(null) , "nothing to remove -- 1");
    th.check(hs.size() , 0 , "HashSet is cleared");
    hs.add("a");
    th.check(!hs.remove(null) , "nothing to remove -- 2");
    th.check(hs.size() , 1 , "HashSet is cleared");
    hs.add(null);
    th.check(!hs.remove("C") , "nothing to remove -- 3");
    th.check( hs.remove("a") , "remove -- 1");
    th.check(!hs.remove("a") , "remove -- 2");
    th.check( hs.remove(null), "remove -- 3");
    th.check(!hs.remove(null), "remove -- 4");
  }

/**
* implemented. <br>
*
*/
  public void test_iterator(){
    th.checkPoint("iterator()java.util.Iterator");
    HashSet v = new HashSet(19);
    v.add("a");     v.add("c");   v.add("u");   v.add("n");   v.add("i");
    v.add(null);    v.add("!");
    Iterator it = v.iterator();	
    HashSet vc = (HashSet) v.clone();
    int i=0;
    Object o;
    while (it.hasNext()) {
    	o = it.next();
    	if (!vc.remove(o)) th.debug("didn't find "+o+" in vector");	
    	if (i++> 20)  break;
    }
    th.check( i < 20 , "check for infinite loop");
    th.check(vc.isEmpty() ,"all elements iterated");
    try {
    	it.next();
    	th.fail("should throw a NoSuchElementException");
    	}
    catch(NoSuchElementException nsee) { th.check(true); }
    it = v.iterator();	
    try {
    	it.remove();
    	th.fail("should throw an IllegalStateException -- 1");
    	}
    catch(IllegalStateException ise) { th.check(true); }
    it.next();
    it.remove();
    try {
    	it.remove();
    	th.fail("should throw an IllegalStateException -- 2");
    	}
    catch(IllegalStateException ise) { th.check(true); }
    it.next();
    v.add("new");
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 1");
    	}
    catch(ConcurrentModificationException cme) { th.check(true); }
    try {
    	it.remove();
    	th.fail("should throw a ConcurrentModificationException -- 2");
    	}
    catch(ConcurrentModificationException cme) { th.check(true); }
    i = 0;
    it = v.iterator();	
    while (it.hasNext()) {
    	o = it.next();
    	it.remove();
    	if (v.contains(o)) th.fail("removed wrong element when tried to remove "+o+", got:"+v); 	
    	if (i++> 20)  break;
    }
    th.check(v.isEmpty() , "all elements are removed");
// check if modCount is updated correctly !!!
    v.add("a");     v.add("c");   v.add("u");   v.add("n");   v.add("i");
    it = v.iterator();	
    v.contains("a");
    v.isEmpty();
    v.clone();
    v.iterator();
    try {
    	it.next();
    	th.check(true, "Ok -- 1");
    	}
    catch(Exception e) { th.fail("should not throw an Exception, got "+e);}
    it = v.iterator();	
    v.add(null);
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- add(Object)");
    	}
    catch(ConcurrentModificationException cme) { th.check(true, "Ok -- 2"); }
    it = v.iterator();	
    v.remove("a");
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- remove(Object)");
    	}
    catch(ConcurrentModificationException cme) { th.check(true, "Ok -- 3"); }
    it = v.iterator();	
    v.clear();
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- clear");
    	}
    catch(ConcurrentModificationException cme) { th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_clone(){
    th.checkPoint("clone()java.lang.Object");
    HashSet hs = new HashSet(11);
    HashSet clone = (HashSet) hs.clone();
    th.check(clone.isEmpty() , "clone is empty");
    th.check(clone.add("a"), "clone can be modified");
    th.check(hs.size() , 0 , "changing clone didn't modify set -- 1");
    hs.add(null); 	hs.add("a"); 	hs.add(this);
    clone = (HashSet) hs.clone();
    th.check(clone.size(), 3 , "checking cloned size");
    th.check(clone.remove(this), "checking elements of clone -- 1");
    th.check(!clone.add(null), "checking elements of clone -- 2");
    th.check(!clone.add("a"), "checking elements of clone -- 2");
    th.check(hs.size() , 3 , "changing clone didn't modify set -- 2");

  }

/**
* the goal of this test is to see how the hashtable behaves if we do a lot add's and removes. <br>
* we perform this test for different loadFactors and a low initialsize <br>
* we try to make it difficult for the table by using objects with same hashcode
*/
  private final String st ="a";
  private final Byte b =new Byte((byte)97);
  private final Short sh=new Short((short)97);
  private final Integer i = new Integer(97);
  private final Long l = new Long(97L);
  private int sqnce = 1;

  public void test_behaviour(){
    th.checkPoint("behaviour testing");
//    do_behaviourtest(0.2f);
//    do_behaviourtest(0.70f);
    do_behaviourtest(0.75f);
    do_behaviourtest(0.95f);
    do_behaviourtest(1.0f);

    }

  protected void check_presence(HashSet h){
    th.check(! h.add(st), "checking presence st -- sequence "+sqnce);
    th.check(! h.add(sh), "checking presence sh -- sequence "+sqnce);
    th.check(! h.add(i) , "checking presence i -- sequence "+sqnce);
    th.check(! h.add(b) , "checking presence b -- sequence "+sqnce);
    th.check(! h.add(l) , "checking presence l -- sequence "+sqnce);
    sqnce++;
  }

  protected void do_behaviourtest(float loadFactor) {

    th.checkPoint("behaviour testing with loadFactor "+loadFactor);
    HashSet h = new HashSet(11 , loadFactor);
    int j=0;
    Float f;
    h.add(st); h.add(b); h.add(sh); h.add(i); h.add(l);
    check_presence(h);
    sqnce = 1;
    for ( ; j < 100 ; j++ )
    {   f = new Float((float)j);
        h.add(f);
    }
    th.check(h.size() == 105,"size checking -- 1 got: "+h.size());
    check_presence(h);
    for ( ; j < 200 ; j++ )
    {   f = new Float((float)j);
        h.add(f);
    }
    th.check(h.size() == 205,"size checking -- 2 got: "+h.size());
    check_presence(h);
    for ( ; j < 300 ; j++ )
    {   f = new Float((float)j);
        h.add(f);
    }
    th.check(h.size() == 305,"size checking -- 3 got: "+h.size());
    check_presence(h);
// replacing values -- checking if we get a non-zero value
    th.check(!(h.add(st)), "replacing values -- 1 - st");
    th.check(!(h.add(b)), "replacing values -- 2 - b");
    th.check(!(h.add(sh)), "replacing values -- 3 -sh");
    th.check(!(h.add(i))  , "replacing values -- 4 -i");
    th.check(!(h.add(l)), "replacing values -- 5 -l");


    for ( ; j > 199 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
    }
    th.check(h.size() == 205,"size checking -- 4 got: "+h.size());
    check_presence(h);
    for ( ; j > 99 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
    }
    th.check(h.size() == 105,"size checking -- 5 got: "+h.size());
    check_presence(h);
    for ( ; j > -1 ; j-- )
    {   f = new Float((float)j);
        h.remove(f);
    }
    th.check(h.size() == 5  ,"size checking -- 6 got: "+h.size());

    th.debug(h.toString());
    check_presence(h);
    }
}
