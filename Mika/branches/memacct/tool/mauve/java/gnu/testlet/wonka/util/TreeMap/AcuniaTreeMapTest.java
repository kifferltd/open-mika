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


package gnu.testlet.wonka.util.TreeMap; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.TreeMap   <br>
*
*/
public class AcuniaTreeMapTest implements Testlet
{
  protected TestHarness th;
  protected ToStringComparator TSComp = new ToStringComparator();

  public void test (TestHarness harness)   {
       th = harness;
       th.setclass("java.util.TreeMap");
       test_TreeMap();
       test_get();
       test_containsKey();
       test_containsValue();
       test_isEmpty();
       test_size();
       test_clear();
       test_put();
       test_putAll();
       test_remove();
       test_entrySet();
       test_keySet();
       test_values();
       test_clone();
       test_comparator();
       test_firstKey();	
       test_lastKey();	
       test_headMap();	
       test_subMap();	
       test_tailMap();	
       test_behaviour();
       test_behaviour2();
  }

  protected TreeMap buildHM() {
  	TreeMap hm = new TreeMap();
  	String s;
  	for (int i=0 ; i < 15 ; i++) {
  		s = "a"+i;
  		hm.put(s , s+" value");
  	}
  	hm.put("null",null);
  	return hm;
  }	

/**
* implemented. <br>*
*/
  public void test_TreeMap(){
    TreeMap hm;
   th.checkPoint("TreeMap()");
   th.checkPoint("TreeMap(java.util.Map)");
    TreeMap hm1 = buildHM();
    hm = new TreeMap(hm1);
    th.check(hm.size() == 16 , "all elements are put, got "+hm.size());
    try {
    	hm.get(null);
    	th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException ne) {th.check(true);}
    th.check("a1 value".equals(hm.get("a1")) , "test key and value pairs -- 2");
    th.check("a10 value".equals(hm.get("a10")) , "test key and value pairs -- 3");
    th.check("a0 value".equals(hm.get("a0")) , "test key and value pairs -- 4");
    hm = new TreeMap(new Hashtable());
    th.check(hm.size() == 0 , "no elements are put, got "+hm.size());
    try {
   	new TreeMap((SortedMap)null);
   	th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException ne) {th.check(true);}
    try {
   	new TreeMap((Map)null);
   	th.fail("should throw a NullPointerException -- 3");
    }
    catch(NullPointerException ne) {th.check(true);}
    hm = new TreeMap(TSComp);
    th.check(hm.comparator() , TSComp , "comparator should be used");
    hm.put("f",null);	hm.put(null,null);
    hm = new TreeMap(hm);	
    th.check(hm.comparator() , TSComp , "comparator should be taken from SortedSet");
  }

/**
* implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get(java.lang.Object)java.lang.Object");
    TreeMap hm = buildHM();
    try {
    	hm.get(this);
    	th.fail("should throw a ClassCastException");
    }
    catch (ClassCastException e){ th.check(true); }
    try {
    	hm.get(null);
    	th.fail("should throw a NullPointerException");
    }
    catch (NullPointerException e){ th.check(true); }
    th.check(hm.get("bad key") , null ,"checking get -- 1");
    th.check(hm.get("null") , null ,"checking get -- 2");
    hm.put("a" ,this);
    th.check(hm.get("a1") ,"a1 value",  "checking get -- 3");
    th.check(hm.get("a11"),"a11 value", "checking get -- 4");

  }

/**
* implemented. <br>
*
*/
  public void test_containsKey(){
    th.checkPoint("containsKey(java.lang.Object)boolean");
    TreeMap hm = new TreeMap();
    hm.clear();
    th.check(! hm.containsKey(null) ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.containsKey("null") ,"Map does not contains the key -- 1");
    th.check( hm.containsKey("a") ,"Map does contain the key -- 2");
    hm = buildHM();
    th.check( hm.containsKey("null") ,"Map does contain the key -- 3");
    try {
    	hm.containsKey(this);
    	th.fail("should throw a ClassCastException");
    }
    catch (ClassCastException e){ th.check(true); }
    try {
    	hm.containsKey(null);
    	th.fail("should throw a NullPointerException");
    }
    catch (NullPointerException e){ th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_containsValue(){
    th.checkPoint("containsValue(java.lang.Object)boolean");
    TreeMap hm = new TreeMap();
    hm.clear();
    th.check(! hm.containsValue(null) ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.containsValue(null) ,"Map does not containsthe value -- 1");
    th.check(! hm.containsValue("a") ,"Map does  not contain the value -- 2");
    th.check( hm.containsValue(this) ,"Map does contain the value -- 3");
    hm = buildHM();
    th.check( hm.containsValue(null) ,"Map does contain the value -- 4");
    th.check(! hm.containsValue(this) ,"Map does not contain the value -- 5");
    th.check(! hm.containsValue("a1value") ,"Map does  not contain the value -- 6");

  }

/**
* implemented. <br>
*
*/
  public void test_isEmpty(){
    th.checkPoint("isEmpty()boolean");
    TreeMap hm = new TreeMap();
    th.check( hm.isEmpty() ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.isEmpty() ,"Map is not empty");

  }

/**
* implemented. <br>
*
*/
  public void test_size(){
    th.checkPoint("size()int");
    TreeMap hm = new TreeMap();
    th.check(hm.size() , 0 ,"Map is empty -- 1");
    hm.put("a" ,this);
    th.check(hm.size() , 1 ,"Map has 1 element");
    hm = buildHM();
    th.check(hm.size() , 16 ,"Map has 16 elements");
    hm.remove("null");
    th.check(hm.size() , 15 ,"Map has 15 elements");
    hm.clear();
    th.check(hm.size() , 0 ,"Map is empty -- 2");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    TreeMap hm = buildHM();
    hm.clear();
    th.check(hm.size() == 0 ,"Map is cleared -- 1");
    th.check(hm.isEmpty() ,"Map is cleared -- 2");
	
  }

/**
* implemented. <br>
* is tested also in the other parts ...
*/
  public void test_put(){
    th.checkPoint("put(java.lang.Object,java.lang.Object)java.lang.Object");
    TreeMap hm  = new TreeMap();
    th.check( hm.put("null" , this ) == null , "check on return value -- 1");
    th.check( hm.get("null") == this , "check on value -- 1");
    th.check( hm.put("null" , "a" ) == this , "check on return value -- 2");
    th.check( "a".equals(hm.get("null")) , "check on value -- 2");
    th.check( "a".equals(hm.put("null" , "a" )), "check on return value -- 3");
    th.check( "a".equals(hm.get("null")) , "check on value -- 3");
    th.check( hm.size() == 1 , "only one key added");
    th.check( hm.put("a" , null ) == null , "check on return value -- 4");
    th.check( hm.get("a") == null , "check on value -- 4");
    th.check( hm.put("a" , this ) == null , "check on return value -- 5");
    th.check( hm.get("a") == this , "check on value -- 5");
    th.check( hm.size() , 2 , "two keys added");

  }

/**
* implemented. <br>
*
*/
  public void test_putAll(){
    th.checkPoint("putAll(java.util.Map)void");
    TreeMap hm  = new TreeMap();
    hm.putAll(new Hashtable());
    th.check(hm.isEmpty() , "nothing addad");
    hm.putAll(buildHM());
    th.check(hm.size() , 16 , "checking if all elements are added -- 1");
    th.check(hm.equals(buildHM()) , "check on all elements -- 1");
    hm.put("null" ,this);
    hm.putAll(buildHM());
    th.check(hm.size() , 16 , "checking if all elements are added -- 2");
    th.check(hm.equals(buildHM()) , "check on all elements -- 2");
    try {
    	hm.putAll(null);
    	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) { th.check(true); }	
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(java.lang.Object)java.lang.Object");
    TreeMap hm  = buildHM();
    th.check(hm.remove("null") == null , "checking return value -- 1");
    th.check(hm.remove("null") == null , "checking return value -- 2");
    th.check(!hm.containsKey("null") , "checking removed key -- 1");
    for (int i = 0 ; i < 15 ; i++) {
    	th.check( hm.remove("a"+i), "a"+i+" value", " removing a"+i);
    	th.check( hm.remove("a"+i), null,  " removing a"+i);
    }
    th.check(hm.isEmpty() , "checking if al is gone");
  }

/**
* implemented. <br>
* uses AbstractSet --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
* check the Map.Entry Objects ...
*/
  public void test_entrySet(){
    th.checkPoint("entrySet()java.util.Set");
    TreeMap hm  = buildHM();
    Set s = hm.entrySet();
    Iterator it= s.iterator();
    java.util.Map.Entry me=null;
    it.next();
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }
    th.check( s.size() , 16 ,"checking size -- 1");
    hm.remove("a12");
    th.check( s.size() , 15 ,"checking size -- 2");
    th.check(it.hasNext());
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 1");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    try {
    	it.remove();
    	th.fail("should throw a ConcurrentModificationException -- 2");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    it= s.iterator();
    try {
    	me = (java.util.Map.Entry)it.next();
    	if (me.getKey()==null) me = (java.util.Map.Entry)it.next();
    	th.check( me.hashCode() , (me.getValue().hashCode() ^ me.getKey().hashCode()),"verifying hashCode");
    	th.check(! me.equals(it.next()));
    	
    	}
    catch(Exception e) { th.fail("got unwanted exception ,got "+e);
    	th.debug("got ME key = "+me+" and value = "+me.getKey());
    }
    Object o = me.getValue();
    Object k = me.getKey();
    th.check(me.setValue(this), o, "return value set");
    th.check(hm.get(k) , this , "setting value reflected on TreeMap");
    it= s.iterator();
    Vector v = new Vector();
    Object ob;
    v.addAll(s);
    while (it.hasNext()) {
    	ob = it.next();
    	it.remove();
     	if (!v.remove(ob))
        th.debug("Object "+ob+" not in the Vector");
     }
     th.check( v.isEmpty() , "all elements gone from the vector");
     th.check( hm.isEmpty() , "all elements removed from the TreeMap");
    hm.put("null","sdf");
    it= s.iterator();
    hm.put("n","sdf");
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 3");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    it= s.iterator();
    hm.clear();
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 4");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }

  }

/**
* implemented. <br>
* uses AbstractSet --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
*/
  public void test_keySet(){
    th.checkPoint("keySet()java.util.Set");
    TreeMap hm = buildHM();
    th.check( hm.size() == 16 , "checking map size(), got "+hm.size());
    Set s=null;
    Object [] o;
    Iterator it;
    try {
        s = hm.keySet();
        th.check( s != null ,"s != null");
        th.check(s.size() == 16 ,"checking size keyset, got "+s.size());
        o = s.toArray();
        th.check( o != null ,"o != null");
        th.check( o.length == 16 ,"checking length, got "+o.length);
	it = s.iterator();
	Vector v = new Vector();
	Object ob;
	v.addAll(s);
	while ( it.hasNext() ) {
        	ob = it.next();
        	it.remove();
        	if (!v.remove(ob))
        	th.debug("Object "+ob+" not in the Vector");
        }
        th.check( v.isEmpty() , "all elements gone from the vector");
        th.check( hm.isEmpty() , "all elements removed from the TreeMap");
    }
    catch (Exception e) { th.fail("got bad Exception -- got "+e); }
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }

  }

/**
* implemented. <br>
* uses AbstractCollection --> check only the overwritten methods ... !
* iterator and size
* fail-fast iterator !
* add not supported !
*/
  public void test_values(){
    th.checkPoint("values()java.util.Collection");
    TreeMap hm = buildHM();
    th.check( hm.size() == 16 , "checking map size(), got "+hm.size());
    Collection s=null;
    Object [] o;
    Iterator it;
    try {
        s = hm.values();
        th.check( s != null ,"s != null");
        th.check(s.size() == 16 ,"checking size keyset, got "+s.size());
        o = s.toArray();
        th.check( o != null ,"o != null");
        th.check( o.length == 16 ,"checking length, got "+o.length);
//        for (int i = 0 ; i < o.length ; i++ ){ th.debug("element "+i+" is "+o[i]); }
	it = s.iterator();
	Vector v = new Vector();
	Object ob;
	v.addAll(s);
	while ( it.hasNext() ) {
        	ob = it.next();
        	it.remove();
        	if (!v.remove(ob))
        	th.debug("Object "+ob+" not in the Vector");
        }
        th.check( v.isEmpty() , "all elements gone from the vector");
        th.check( hm.isEmpty() , "all elements removed from the TreeMap");
    }
    catch (Exception e) { th.fail("got bad Exception -- got "+e); }
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }


  }

/**
* implemented. <br>
*
*/
  public void test_clone(){
    th.checkPoint("clone()java.lang.Object");
    TreeMap hm = buildHM();
    Object o = hm.clone();
    th.check( o != hm , "clone is not the same object");
    th.check( hm.equals(o) , "clone is equal to Map");
    hm.put("a","b");
    th.check(! hm.equals(o) , "clone doesn't change if Map changes");

  }       	
       	
/**
* implemented. <br>
*
*/
  public void test_comparator(){
    th.checkPoint("comparator()java.util.Comparator");
    TreeMap tm = new TreeMap((Comparator)null);
    th.check(tm.comparator(), null , "null comparator used");	
    tm = new TreeMap(TSComp);
    th.check(tm.comparator(), TSComp , "TSComp comparator used");	
    th.check(tm.tailMap("f").comparator(), TSComp , "TSComp passes to subMaps -- 1");	
    th.check(tm.headMap("f").comparator(), TSComp , "TSComp passes to subMaps -- 2");	
    th.check(tm.subMap("f", "g").comparator(), TSComp , "TSComp passes to subMaps -- 3");	
  }

/**
* implemented. <br>
*
*/
  public void test_firstKey(){
    th.checkPoint("firstKey()java.lang.Object");
    TreeMap ts = new TreeMap();
    try {
     	ts.firstKey();
     	th.fail("should throw a NoSuchElementException -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    ts.put("g","gb");
    th.check(ts.firstKey() , "g" , "one element --> firstKey");
    ts.put("f","fm");
    th.check(ts.firstKey() , "f" , "two elements --> firstKey");
    ts.put("h","hm");
    th.check(ts.firstKey() , "f" , "three elements --> firstKey");
    ts.put("a","ac");
    th.check(ts.firstKey() , "a" , "four elements --> firstKey");
    ts.clear();
    try {
     	ts.firstKey();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }


  }
/**
* implemented. <br>
*
*/
  public void test_lastKey(){
    th.checkPoint("lastKey()java.lang.Object");
    TreeMap ts = new TreeMap();
    try {
     	ts.lastKey();
     	th.fail("should throw a NoSuchElementException -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    ts.put("g","gb");
    th.check(ts.lastKey() , "g" , "one element --> lastKey");
    ts.put("h","hm");
    th.check(ts.lastKey() , "h" , "two elements --> lastKey");
    ts.put("a","ac");
    th.check(ts.lastKey() , "h" , "three elements --> lastKey");
    ts.put("x", "xx");
    th.check(ts.lastKey() , "x" , "four elements --> lastKey");
    ts.clear();
    try {
     	ts.lastKey();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }

  }

  protected TreeMap buildAlfabet(){
   	TreeMap t = new TreeMap();
   	t.put("a","a1"); t.put("b","a2"); t.put("c","a3"); t.put("d","a4");
   	t.put("h","a8"); t.put("g","a7"); t.put("f","a6"); t.put("e","a5");
   	t.put("i","a9"); t.put("j","a10"); t.put("k","a11"); t.put("l","a12");
   	t.put("p","a16"); t.put("o","a15"); t.put("n","a14"); t.put("m","a13");
   	t.put("q","a17"); t.put("r","a18"); t.put("s","a19"); t.put("t","a20");
   	t.put("x","a24"); t.put("w","a23"); t.put("v","a22"); t.put("u","a21");
   	t.put("y","a25"); t.put("z","a26");
  	return t;
  }

/**
* implemented. <br>
*
*/
  public void test_headMap(){
    th.checkPoint("headMap(java.lang.Object)java.util.SortedMap");
    TreeMap ts = buildAlfabet();
    SortedMap s = ts.headMap("a");
    th.check(s.isEmpty(), "headMap Should be empty");
    th.check(s.comparator(), null ,"comparator stays the same");
    try {
     	Object o = s.firstKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	Object o = s.lastKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.headMap("h");
    th.check(s.size(), 7, "headMap Should be empty");
    th.check(s.firstKey(), "a" ,"first element is a");
    th.check(s.lastKey() , "g" , "last element is g");
    s = s.subMap("d","g");
    th.check(s.size(), 3, "headMap Should be empty");
    th.check(s.firstKey(), "d" ,"first element is e");
    th.check(s.lastKey() , "f" , "last element is g");
    try {
     		s.put("p","p");
     		th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(s.remove("p"), null , "should not remove elements out range");
  }
/**
* implemented. <br>
*
*/
  public void test_subMap(){
    th.checkPoint("subMap(java.lang.Object,java.lang.Object)java.util.SortedMap");
    TreeMap ts = new TreeMap(TSComp);
    ts.putAll(buildAlfabet());
    SortedMap s = ts.subMap(""," ");
    try {
     		ts.subMap("x","a");
     		th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(s.isEmpty(), "headSet Should be empty");
    th.check(s.comparator(), TSComp ,"comparator stays the same");
    try {
     	Object o = s.firstKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	Object o = s.lastKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.subMap("h", "na");
    th.check(s.size(), 7, "headSet Should be empty");
    th.check(s.firstKey(), "h" ,"first element is h");
    th.check(s.lastKey() , "n" , "last element is n");
    try {
     		s.subMap("a","j");
     		th.fail("should throw an IllegalArgumentException -- 2");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    try {
     		s.subMap("j","p");
     		th.fail("should throw an IllegalArgumentException -- 3");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    try {
     		s.put("p","p");
     		th.fail("should throw an IllegalArgumentException -- 4");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(s.remove("p"), null , "should not remove elements out range");
    s = s.tailMap("k");
    th.check(s.size(), 4, "subMap Should be empty");
    th.check(s.firstKey(), "k" ,"first element is k");
    th.check(s.lastKey() , "n" , "last element is n");
  }
/**
* implemented. <br>
*
*/
  public void test_tailMap(){
    th.checkPoint("tailMap(java.lang.Object)java.util.SortedMap");
    TreeMap ts = buildAlfabet();
    SortedMap s = ts.tailMap("za");
    th.check(s.isEmpty(), "tailMap Should be empty");
    try {
     	Object o = s.firstKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 1");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    try {
     	Object o = s.lastKey();
     	th.fail("should throw a NoSuchElementException, but got "+o+" -- 2");
    }
    catch(NoSuchElementException nse){ th.check(true); }
    s = ts.tailMap("t");
    th.check(s.size(), 7, "tailMap Should be empty");
    th.check(s.firstKey(), "t" ,"first element is t");
    th.check(s.lastKey() , "z" , "last element is z");
    s = s.subMap("v","y");
    th.check(s.size(), 3, "tailMap has size 3");
    th.check(s.firstKey(), "v" ,"first element is v");
    th.check(s.lastKey() , "x" , "last element is x");
    try {
     		s.put("p","p");
     		th.fail("should throw an IllegalArgumentException -- 1");
    }
    catch(IllegalArgumentException iae){ th.check(true); }
    th.check(s.remove("p"), null , "should not remove elements out range");
  }
       	
/**
* the goal of this test is to see how the TreeMap behaves if we do a lot put's and removes. <br>
*/
  private final String st = "a";
  private final String b  = "b";
  private final String sh = "sh";
  private final String i  = "i";
  private final String l  = "l";
  private int sqnce = 1;

  protected void check_presence(Map h){
    th.check( h.get(st) != null, "checking presence st -- sequence "+sqnce);
    th.check( h.get(sh) != null, "checking presence sh -- sequence "+sqnce);
    th.check( h.get(i) != null, "checking presence i -- sequence "+sqnce);
    th.check( h.get(b) != null, "checking presence b -- sequence "+sqnce);
    th.check( h.get(l) != null, "checking presence l -- sequence "+sqnce);
    sqnce++;
  }
  public String newFloat(float f){
   	return "float = "+f;
  }

  public void test_behaviour(){
    th.checkPoint("behaviour testing");
    SortedMap h = new TreeMap(TSComp).subMap("","x");
    int j=0;
    String f;
    h.put(st,"a"); h.put(b,"byte"); h.put(sh,"short"); h.put(i,"int"); h.put(l,"long");
    h.put(null,null);
    check_presence(h);
    sqnce = 1;
    for ( ; j < 100 ; j++ )
    {   f = newFloat((float)j);
        h.put(f,f);
    }
    th.check(h.size() , 106,"size checking -- 1 got: "+h.size());
    check_presence(h);
    for ( ; j < 200 ; j++ )
    {   f = newFloat((float)j);
        h.put(f,f);
    }
    th.check(h.size() , 206,"size checking -- 2 got: "+h.size());
    check_presence(h);

    for ( ; j < 1000 ; j++ )
    {   f = newFloat((float)j);
        h.put(f,f);
    }
    th.check(h.size() , 1006,"size checking -- 3 got: "+h.size());
    check_presence(h);
// replacing values -- checking if we get a non-zero value
    th.check("a".equals(h.put(st,"na")), "replacing values -- 1 - st");
    th.check("byte".equals(h.put(b,"nbyte")), "replacing values -- 2 - b");
    th.check("short".equals(h.put(sh,"nshort")), "replacing values -- 3 -sh");
    th.check("int".equals(h.put(i,"nint"))  , "replacing values -- 4 -i");
    th.check("long".equals(h.put(l,"nlong")), "replacing values -- 5 -l");


    for ( ; j > 199 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() , 206,"size checking -- 4 got: "+h.size());
    check_presence(h);
    for ( ; j > 99 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() , 106,"size checking -- 5 got: "+h.size());
    check_presence(h);
    for ( ; j > -1 ; j-- )
    {   f = newFloat((float)j);
        h.remove(f);
    }
    th.check(h.size() , 6  ,"size checking -- 6 got: "+h.size());

    th.debug(h.toString());
    check_presence(h);
    }

  public void test_behaviour2(){
    th.checkPoint("behaviour testing 2");
    TreeMap tm = new TreeMap ();
    for (int i = 0; i < 200; i+=2) {
      tm.put (new Integer (i), "" + i);
    }
    Iterator it = tm.keySet().iterator();
    while (it.hasNext()) {
      Integer i = (Integer)it.next();
      if (i.intValue() > 30 && i.intValue() < 90){
        it.remove();
      }
    }
    for (int i = 51; i < 150; i+=2) {
      tm.put (new Integer (i), "" + i);
    }
    it = tm.entrySet ().iterator ();
    int i = 0;
    while (it.hasNext()) {
      java.util.Map.Entry me = (java.util.Map.Entry)it.next();
      Integer in = (Integer) me.getKey();
      th.check(me.getValue(),in.toString(), "checking entries "+i);
      th.check(in.intValue(), i, "checking order "+i);
      if(i == 30){
        i = 51;
      }
      else if(i < 89 || i >= 150){
        i += 2;
      }
      else {
        i++;
      }
    }
  }

/**
**	the goal of this comparator is to see if the Map handles a null key well
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

}
