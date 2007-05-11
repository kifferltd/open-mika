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


package gnu.testlet.wonka.util.AbstractMap; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

/**
*  this file contains test for java.util.AbstractMap   <br>
*
*/
public class SMAbstractMapTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
  {
       th = harness;
       th.setclass("java.util.AbstractMap");
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
       test_equals();
       test_hashCode();
       test_toString();
  }

  protected SMExAbstractMap buildHT() {
   	SMExAbstractMap t = new SMExAbstractMap();
   	String s;
   	for (int i=0 ; i < 15 ; i++) {
   	 	s = "a"+i;
   	 	t.put(s,s+" value");
   	}
   	return t;
  }


/**
* implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get(java.lang.Object)java.lang.Object");
    SMExAbstractMap ehm = buildHT();
    Object o;
    String s="a1";
    o = ehm.get(s);
    th.check( (s+" value").equals(o) , "checking return value");
    o = ehm.get(null);
    th.check( o == null );
    o = ehm.get(s+" value");
    th.check( o == null );
    ehm.put(null,s);
    o = ehm.get(null);
    th.check( s.equals(o));

  }

/**
* implemented. <br>
*
*/
  public void test_containsKey(){
    th.checkPoint("containsKey(java.lang.Object)boolean");
    SMExAbstractMap ehm = buildHT();
    th.check(!ehm.containsKey(null) , "null not there");
    ehm.put(null,"test");
    th.check(ehm.containsKey(null) , "null is in there");
    th.check(ehm.containsKey("a1") , "object is in there");
    th.check(!ehm.containsKey("a1 value") , "object is not in there -- 1");
    th.check(!ehm.containsKey(this) , "object is not in there -- 2");

  }

/**
* implemented. <br>
*
*/
  public void test_containsValue(){
    th.checkPoint("containsValue(java.lang.Object)boolean");
    SMExAbstractMap ehm = buildHT();
    th.check(!ehm.containsValue(null) , "null not there");
    ehm.put(null,null);
    th.check(ehm.containsValue(null) , "null is in there");
    th.check(!ehm.containsValue("a1") , "object is not in there -- 1");
    th.check(ehm.containsValue("a1 value") , "object is in there -- 1");
    th.check(!ehm.containsValue(this) , "object is not in there -- 2");

  }

/**
* implemented. <br>
*
*/
  public void test_isEmpty(){
    th.checkPoint("isEmpty()boolean");
    SMExAbstractMap ehm = new SMExAbstractMap();
    th.check(ehm.isEmpty() , "true");
    ehm = buildHT();
    th.check(!ehm.isEmpty() , "false");

  }

/**
*  not implemented. <br>
*  Abstract Method
*/
  public void test_size(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    SMExAbstractMap ehm = buildHT();
    ehm.clear();
    th.check(ehm.isEmpty() , "true");
  }

/**
* implemented. <br>
*
*/
  public void test_put(){
    th.checkPoint("put(java.lang.Object,java.lang.Object)java.lang.Object");
    SMExAbstractMap ehm = buildHT();
    ehm.set_edit(false);
    try {
    	ehm.put("a","b");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }		
  }

/**
* implemented. <br>
*
*/
  public void test_putAll(){
    th.checkPoint("putAll(java.util.Map)void");
    Hashtable ht = new Hashtable();
    SMExAbstractMap ehm = new SMExAbstractMap();
    th.check( ehm.equals(ht) , "true -- both empty");
    ht.put("a","b");	ht.put("c","d");	ht.put("e","f");
    ehm.putAll(ht);
    th.check( ehm.equals(ht) , "true -- 1");
    ht.put("a1","f");
    ht.put("e","b");
    ehm.putAll(ht);
    th.check( ehm.equals(ht) , "true -- 2");
    ehm = buildHT();
    ehm.putAll(ht);
    th.check(ehm.size() == 18 , "added three elements");
    th.check("f".equals(ehm.get("a1")) , "overwritten old value");
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(java.lang.Object)java.lang.Object");
    SMExAbstractMap ehm = buildHT();
    ehm.remove("a1");
    th.check(!ehm.containsKey("a1") , "key removed -- 1");
    th.check(!ehm.containsValue("a1 value") , "value removed -- 1");
    ehm.remove("a0");
    th.check(!ehm.containsKey("a0") , "key removed -- 2");
    th.check(!ehm.containsValue("a0 value") , "value removed -- 2");
    for (int i=2 ; i < 15 ; i++ ) {
	    ehm.remove("a"+i);
    }
    th.check(ehm.isEmpty());
  }

/**
*   not implemented. <br>
*   Abstract Method
*/
  public void test_entrySet(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
* check only on methods not inherited from AbstractSet
*/
  public void test_keySet(){
    th.checkPoint("keySet()java.util.Set");
    SMExAbstractMap ehm = buildHT();
    Set s = ehm.keySet();
    th.check(s.size() == 15);
    ehm.put(null,"test");
    th.check(s.size() == 16);
    th.check(s.contains("a1"),"does contain a1");
    th.check(s.contains(null),"does contain null");
    th.check(!s.contains(this),"does contain this");
    th.check(!s.contains("test"),"does contain test");
    th.check( s == ehm.keySet() , "same Set is returned");
    Iterator it = s.iterator();
    Vector v = ehm.getKeyV();
    int i;
    Object o;
    for (i=0 ; i < 16 ; i++) {
    	o = it.next();
    	th.check(v.indexOf(o) == 0, "order is not respected");
    	if (!v.remove(o)) th.debug("didn't find "+o);
     	
    }
    it = s.iterator();
    while (it.hasNext()) {
     	it.next();
     	it.remove();
    }
    th.check(s.isEmpty(), "everything is removed");
    s = ehm.keySet();
    th.check(s.isEmpty(), "new Set is also empty");
    ehm.put("a","B");
    th.check(!s.isEmpty(), "Set is updated by underlying actions");
  }

/**
* implemented. <br>
* check only on methods not inherited from AbstractCollection
*/
  public void test_values(){
    th.checkPoint("values()java.util.Collection");
    SMExAbstractMap ehm = buildHT();
    Collection s = ehm.values();
    th.check(s.size() == 15);
    ehm.put(null,"test");
    ehm.put("a10",null);
    th.check(s.size() == 16);
    th.check(s.contains("a1 value"),"does contain a1 value");
    th.check(s.contains(null),"does contain null");
    th.check(!s.contains(this),"does contain this");
    th.check(s.contains("test"),"does contain test");
    th.check(!s.contains("a1"),"does not contain a1");
    th.check( s == ehm.values() , "same Set is returned");
    Iterator it = s.iterator();
    Vector v = ehm.getValuesV();
    int i;
    Object o;
    for (i=0 ; i < 16 ; i++) {
    	o = it.next();
    	th.check(v.indexOf(o) == 0, "order is not respected");
    	if (!v.remove(o)) th.debug("didn't find "+o);
     	
    }
    it = s.iterator();
    while (it.hasNext()) {
     	it.next();
     	it.remove();
    }
    th.check(s.isEmpty(), "everything is removed");
    s = ehm.values();
    th.check(s.isEmpty(), "new Set is also empty");
    ehm.put("a","B");
    th.check(!s.isEmpty(), "Set is updated by underlying actions");

  }

/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    Hashtable ht = new Hashtable();
    SMExAbstractMap ehm = new SMExAbstractMap();
    th.check( ehm.equals(ht) , "true -- both empty");
    ht.put("a","b");	ht.put("c","d");	ht.put("e","f");
    ehm.put("a","b");	ehm.put("c","d");	ehm.put("e","f");
    th.check( ehm.equals(ht) , "true -- same key && values");
    ht.put("a","f");
    th.check(! ehm.equals(ht) , "false -- same key && diff values");
    ht.put("e","b");
    th.check(! ehm.equals(ht) , "false --  key with diff values");
    th.check(! ehm.equals(ht.entrySet()) , "false --  no Map");
    th.check(! ehm.equals(this) , "false -- Object is no Map");
    th.check(! ehm.equals(null) , "false -- Object is null");



  }

/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    SMExAbstractMap ehm = new SMExAbstractMap();
    th.check( ehm.hashCode() == 0 , "hashCode of Empty Map is 0, got "+ehm.hashCode());
    int hash = 0;
    Iterator s = ehm.entrySet().iterator();
    while (s.hasNext()) { hash += s.next().hashCode(); }
    th.check( ehm.hashCode() , hash , "hashCode of Empty Map -- checking Algorithm");

  }

/**
* implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    SMExAbstractMap ehm = new SMExAbstractMap();
    th.check("{}".equals(ehm.toString()) , "checking empty Map");
    ehm.put("a","b");	
    th.check("{a=b}".equals(ehm.toString()) , "checking Map with one element");
    ehm.put("c","d");	ehm.put("e","f");
    th.check("{a=b, c=d, e=f}".equals(ehm.toString()) , "checking Map with three elements");
  }

}
