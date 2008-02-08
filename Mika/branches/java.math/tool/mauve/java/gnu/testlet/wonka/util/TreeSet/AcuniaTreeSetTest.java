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



package gnu.testlet.wonka.util.TreeSet;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.TreeSet   <br>
*
*/
public class AcuniaTreeSetTest implements Testlet
{

  protected TestHarness th;
  protected ToStringComparator TSComp = new ToStringComparator();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.TreeSet");
       test_TreeSet();
       test_first();
       test_last();
       test_contains();
       test_isEmpty();
       test_size();
       test_add();
       test_clear();
       test_remove();
       test_iterator();
       test_clone();
       test_behaviour();
       test_headSet();
       test_subSet();
       test_tailSet();
       test_comparator();
//       test_behaviour();
     }

/**
* implemented. <br>
*
*/
  public void test_TreeSet(){
    th.checkPoint("TreeSet(java.util.Collection)");
    Vector v = new Vector();
    v.add("a");	v.add("null"); v.add("c"); v.add("this"); v.add("a"); v.add("null");
    TreeSet hs = new TreeSet(v);
    th.check(hs.size(), 4 , "checking size -- 1");
    th.check(hs.contains("this") , "checking elements -- 1");
    th.check(hs.contains("null") , "checking elements -- 2");
    try {
     	new TreeSet((Collection)null);
     	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException e) { th.check(true);}
  }


/**
* implemented. <br>
*
*/
  public void test_contains(){
    th.checkPoint("contains(java.lang.Object)boolean");
    TreeSet al = new TreeSet();
    th.check(!al.contains(null),"checking empty List -- 1");
    th.check(!al.contains(al)  ,"checking empty List -- 2");
    al.add("a");
    al.add("null");
    al.add("6");
    th.check( al.contains("null"), "check contains ... -- 1");
    th.check( al.contains("a") , "check contains ... -- 2");
    th.check(!al.contains("this"), "check contains ... -- 3");
    al.remove("6");
    al.remove("null");
    th.check(!al.contains("null"), "check contains ... -- 4");
    th.check(!al.contains("6") , "check contains ... -- 5");
  }

/**
* implemented. <br>
*
*/
  public void test_isEmpty(){
    th.checkPoint("isEmpty()boolean");
    TreeSet hs = new TreeSet();
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
    TreeSet hs = new TreeSet();
    th.check(hs.size() , 0 , "verifying size -- 1");
    hs.add("a");
    th.check(hs.size() , 1 , "verifying size -- 2");
    hs.add("B");
    th.check(hs.size() , 2 , "verifying size -- 3");
    hs.add("B");
    th.check(hs.size() , 2 , "verifying size -- 4");
    hs.add("null");
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
    TreeSet hs = new TreeSet();
    th.check(hs.add("a") , "checking return value -- 1");
    th.check(hs.add("c") , "checking return value -- 2");
    th.check(!hs.add("a") , "checking return value -- 3");
    th.check(hs.add("null") , "checking return value -- 4");
    th.check(!hs.add("null") , "checking return value -- 5");
    th.check(hs.size() == 3, "duplicate elements aren't added");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    TreeSet hs = new TreeSet();
    hs.clear();
    hs.add("a");
    hs.clear();
    th.check(hs.size() , 0 , "TreeSet is cleared");
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(java.lang.Object)boolean");
    TreeSet hs = new TreeSet();
    hs.clear();
    hs.add("a");
    hs.clear();
    th.check(!hs.remove("null") , "nothing to remove -- 1");
    th.check(hs.size() , 0 , "TreeSet is cleared");
    hs.add("a");
    th.check(!hs.remove("null") , "nothing to remove -- 2");
    th.check(hs.size() , 1 , "TreeSet is cleared");
    hs.add("null");
    th.check(!hs.remove("C") , "nothing to remove -- 3");
    th.check( hs.remove("a") , "remove -- 1");
    th.check(!hs.remove("a") , "remove -- 2");
    th.check( hs.remove("null"), "remove -- 3");
    th.check(!hs.remove("null"), "remove -- 4");
  }

/**
* implemented. <br>
*
*/
  public void test_iterator(){
    th.checkPoint("iterator()java.util.Iterator");
    TreeSet v = new TreeSet();
    v.add("a");     v.add("c");   v.add("u");   v.add("n");   v.add("i");
    v.add("null");    v.add("!");
    Iterator it = v.iterator();	
    TreeSet vc = (TreeSet) v.clone();
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
    v.add("null");
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
    TreeSet hs = new TreeSet();
    TreeSet clone = (TreeSet) hs.clone();
    th.check(clone.isEmpty() , "clone is empty");
    th.check(clone.add("a"), "clone can be modified");
    th.check(hs.size() , 0 , "changing clone didn't modify set -- 1");
    hs.add("null"); 	hs.add("a"); 	hs.add("this");
    clone = (TreeSet) hs.clone();
    th.check(clone.size(), 3 , "checking cloned size");
    th.check(clone.remove("this"), "checking elements of clone -- 1");
    th.check(!clone.add("null"), "checking elements of clone -- 2");
    th.check(!clone.add("a"), "checking elements of clone -- 2");
    th.check(hs.size() , 3 , "changing clone didn't modify set -- 2");

  }

/**
* implemented. <br>
*
*/
  public void test_first(){
    th.checkPoint("first()java.lang.Object");
    TreeSet ts = new TreeSet();
    try {
     	ts.first();
     	th.fail("should throw a NoSuchElementException -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    ts.add("g");
    th.check(ts.first() , "g" , "one element --> first");
    ts.add("f");
    th.check(ts.first() , "f" , "two elements --> first");
    ts.add("h");
    th.check(ts.first() , "f" , "three elements --> first");
    ts.add("a");
    th.check(ts.first() , "a" , "four elements --> first");
    ts.clear();
    try {
     	ts.first();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }

}

  protected TreeSet buildAlfabet(){
   	TreeSet t = new TreeSet();
   	t.add("a"); t.add("b"); t.add("c"); t.add("d");
   	t.add("h"); t.add("g"); t.add("f"); t.add("e");
   	t.add("i"); t.add("j"); t.add("k"); t.add("l");
   	t.add("p"); t.add("o"); t.add("n"); t.add("m");
   	t.add("q"); t.add("r"); t.add("s"); t.add("t");
   	t.add("x"); t.add("w"); t.add("v"); t.add("u");
   	t.add("y"); t.add("z");
  	return t;
  }


/**
* implemented. <br>
*
*/
  public void test_last(){
    th.checkPoint("last()java.lang.Object");
    TreeSet ts = new TreeSet();
    try {
     	ts.last();
     	th.fail("should throw a NoSuchElementException -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    ts.add("g");
    th.check(ts.last() , "g" , "one element --> last");
    ts.add("h");
    th.check(ts.last() , "h" , "two elements --> last");
    ts.add("a");
    th.check(ts.last() , "h" , "three elements --> last");
    ts.add("x");
    th.check(ts.last() , "x" , "four elements --> last");
    ts.clear();
    try {
     	ts.last();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
}
/**
* implemented. <br>
*
*/
  public void test_headSet(){
    th.checkPoint("headSet(java.lang.Object)java.util.SortedSet");
    TreeSet ts = buildAlfabet();
    SortedSet s = ts.headSet("a");
    th.check(s.isEmpty(), "headSet Should be empty");
    th.check(s.comparator(), null ,"comparator stays the same");
    try {
     	Object o = s.first();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	s.last();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.headSet("h");
    th.check(s.size(), 7, "headSet Should be empty");
    th.check(s.first(), "a" ,"first element is a");
    th.check(s.last() , "g" , "last element is g");
    s = s.subSet("d","g");
    th.check(s.size(), 3, "headSet Should be empty");
    th.check(s.first(), "d" ,"first element is e");
    th.check(s.last() , "f" , "last element is g");

}
/**
* not implemented. <br>
*
*/
  public void test_tailSet(){
    th.checkPoint("tailSet(java.lang.Object)java.util.SortedSet");
    TreeSet ts = buildAlfabet();
    SortedSet s = ts.tailSet("za");
    th.check(s.isEmpty(), "tailSet Should be empty");
    try {
     	Object o = s.first();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	Object o = s.last();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.tailSet("t");
    th.check(s.size(), 7, "tailSet Should be empty");
    th.check(s.first(), "t" ,"first element is t");
    th.check(s.last() , "z" , "last element is z");
    s = s.subSet("v","y");
    th.check(s.size(), 3, "tailSet has size 3");
    th.check(s.first(), "v" ,"first element is v");
    th.check(s.last() , "x" , "last element is x");
    try {
     		s.add("p");
     		th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(! s.remove("p"), "should not remove elements out range");
}
/**
* not implemented. <br>
*
*/
  public void test_subSet(){
    th.checkPoint("subSet(java.lang.Object,java.lang.Object)java.util.SortedSet");
    TreeSet ts = new TreeSet(TSComp);
    ts.addAll(buildAlfabet());
    SortedSet s = ts.subSet(""," ");
    try {
     		ts.subSet("x","a");
     		th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(s.isEmpty(), "headSet Should be empty");
    th.check(s.comparator(), TSComp ,"comparator stays the same");
    try {
     	Object o = s.first();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	Object o = s.last();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.subSet("h", "na");
    th.check(s.size(), 7, "headSet Should be empty");
    th.check(s.first(), "h" ,"first element is h");
    th.check(s.last() , "n" , "last element is n");
    try {
     		s.subSet("a","j");
     		th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    try {
     		s.subSet("j","p");
     		th.fail("should throw an IllegalArgumentException -- 3");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    try {
     		s.add("p");
     		th.fail("should throw an IllegalArgumentException -- 4");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(!s.remove("p"), "should not remove elements out range");
    s = s.tailSet("k");
    th.check(s.size(), 4, "subSet Should be empty");
    th.check(s.first(), "k" ,"first element is k");
    th.check(s.last() , "n" , "last element is n");


}
/**
* not implemented. <br>
*
*/
  public void test_comparator(){
    th.checkPoint("comparator()java.util.Comparator");
    TreeSet tm = new TreeSet((Comparator)null);
    th.check(tm.comparator(), null , "null comparator used");	
    tm = new TreeSet(TSComp);
    th.check(tm.comparator(), TSComp , "TSComp comparator used");	
    th.check(tm.tailSet("f").comparator(), TSComp , "TSComp passes to subSets -- 1");	
    th.check(tm.headSet("f").comparator(), TSComp , "TSComp passes to subSets -- 2");	
    th.check(tm.subSet("f", "g").comparator(), TSComp , "TSComp passes to subSets -- 3");	


}
/**
**	the goal of this comparator is to see if the Set handles a null key well
*/
public static class ToStringComparator implements java.util.Comparator{

	public ToStringComparator(){}
	
	public int compare(Object one, Object two){
	 	if (one == null){
	 		if (two == null){
	 	 		return 0;
	 	 	}
	 	 	return -(two.toString().compareTo("null"));
	 	}
 		if (two == null){
	 	 	return (one.toString().compareTo("null"));
	 	}
 	 	return (one.toString().compareTo(two.toString()));
	}
}

/**
* the goal of this test is to see how the TreeSet behaves if we do a lot add's and removes. <br>
*/
  private final String st = "a";
  private final String b = "new Byte((byte)97)";
  private final String sh = "new Short((short)97)";
  private final String i = "new Integer(97)";
  private final String l = "new Long(97L)";
  private int sqnce = 1;

  protected void check_presence(SortedSet h){
    th.check(! h.add(st), "checking presence st -- sequence "+sqnce);
    th.check(! h.add(sh), "checking presence sh -- sequence "+sqnce);
    th.check(! h.add(i) , "checking presence i -- sequence "+sqnce);
    th.check(! h.add(b) , "checking presence b -- sequence "+sqnce);
    th.check(! h.add(l) , "checking presence l -- sequence "+sqnce);
    sqnce++;
  }

  public String newFloat(float j){
   	return j+" float";
  }

  public void test_behaviour(){
    th.checkPoint("behaviour testing");
    SortedSet h = new TreeSet().subSet("","x");
    int j=0;
    String f;
    h.add(st); h.add(b); h.add(sh); h.add(i); h.add(l);
    check_presence(h);
    sqnce = 1;
    for ( ; j < 100 ; j++ )
    {   f = newFloat((float)j);
        h.add(f);
    }
    th.check(h.size() == 105,"size checking -- 1 got: "+h.size());
    check_presence(h);
    for ( ; j < 200 ; j++ )
    {   f = newFloat((float)j);
        h.add(f);
    }
    th.check(h.size() == 205,"size checking -- 2 got: "+h.size());
    check_presence(h);
    for ( ; j < 500 ; j++ )
    {   f = newFloat((float)j);
        h.add(f);
    }
    th.check(h.size() == 505,"size checking -- 3 got: "+h.size());
    check_presence(h);
// replacing values -- checking if we get a non-zero value
    th.check(!(h.add(st)), "replacing values -- 1 - st");
    th.check(!(h.add(b)), "replacing values -- 2 - b");
    th.check(!(h.add(sh)), "replacing values -- 3 -sh");
    th.check(!(h.add(i))  , "replacing values -- 4 -i");
    th.check(!(h.add(l)), "replacing values -- 5 -l");


    for ( ; j > 199 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() == 205,"size checking -- 4 got: "+h.size());
    check_presence(h);
    for ( ; j > 99 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() == 105,"size checking -- 5 got: "+h.size());
    check_presence(h);
    for ( ; j > -1 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() == 5  ,"size checking -- 6 got: "+h.size());

    th.debug(h.toString());
    check_presence(h);
    }
}
