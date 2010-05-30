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


package gnu.testlet.wonka.util.WeakHashMap; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...
import java.lang.reflect.*;
/**
*  this file contains test for java.util.WeakHashMap   <br>
*  (this is a Modified version of SMHashMapTest with extra features to test the behaviour
*   WeakReferences ...)
*/
public class AcuniaWeakHashMapTest implements Testlet {
  protected TestHarness th;
  protected Vector keys;

  public void test (TestHarness harness)   {
       th = harness;
       th.setclass("java.util.WeakHashMap");
       test_WeakHashMap();
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
       //test_behaviour();
       test_WeakRefBehaviour();
  }

  protected WeakHashMap buildHM() {
  	WeakHashMap hm = new WeakHashMap();
  	keys = new Vector(16);
  	String s;
  	for (int i=0 ; i < 15 ; i++) {
  		s = "a"+i;
  		hm.put(s , s+" value");
  	  keys.add(s);
  	}
  	hm.put(null,null);
  	keys.add(null);
  	return hm;
  }	

/**
* implemented. <br>
*
*/
  public void test_WeakHashMap(){
   th.checkPoint("WeakHashMap(int)");
    WeakHashMap hm = new WeakHashMap(1);
    try { new WeakHashMap(-1);
   	 th.fail("should throw an IllegalArgumentException");
        }
    catch(IllegalArgumentException iae) { th.check(true); }

   th.checkPoint("WeakHashMap(int,float)");
    try {new WeakHashMap(-1,0.1f);
       	 th.fail("should throw an IllegalArgumentException -- 1");
        }
    catch(IllegalArgumentException iae) { th.check(true); }
    try { new WeakHashMap(1,-0.1f);
   	 th.fail("should throw an IllegalArgumentException -- 2");
        }
    catch(IllegalArgumentException iae) { th.check(true); }
    try { new WeakHashMap(1,0.0f);
    	 th.fail("should throw an IllegalArgumentException -- 2");
        }
    catch(IllegalArgumentException iae) { th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_get(){
    th.checkPoint("get(java.lang.Object)java.lang.Object");
    WeakHashMap hm = buildHM();
    th.check(hm.get(null) == null , "checking get -- 1");
    th.check(hm.get(this) == null , "checking get -- 2");
    hm.put("a" ,this);
    th.check("a1 value".equals(hm.get("a1")), "checking get -- 3");
    th.check("a11 value".equals(hm.get("a11")), "checking get -- 4");
    th.check( hm.get(new Integer(97)) == null , "checking get -- 5");


  }

/**
* implemented. <br>
*
*/
  public void test_containsKey(){
    th.checkPoint("containsKey(java.lang.Object)boolean");
    WeakHashMap hm = new WeakHashMap();
    hm.clear();
    th.check(! hm.containsKey(null) ,"Map is empty");
    hm.put("a" ,this);
    th.check(! hm.containsKey(null) ,"Map does not containsthe key -- 1");
    th.check( hm.containsKey("a") ,"Map does contain the key -- 2");
    hm = buildHM();
    th.check( hm.containsKey(null) ,"Map does contain the key -- 3");
    th.check(! hm.containsKey(this) ,"Map does not contain the key -- 4");

  }

/**
* implemented. <br>
*
*/
  public void test_containsValue(){
    th.checkPoint("containsValue(java.lang.Object)boolean");
    WeakHashMap hm = new WeakHashMap();
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
    WeakHashMap hm = new WeakHashMap();
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
    WeakHashMap hm = new WeakHashMap();
    th.check(hm.size() == 0 ,"Map is empty");
    hm.put("a" ,this);
    th.check(hm.size() == 1 ,"Map has 1 element");
    hm = buildHM();
    th.check(hm.size() == 16 ,"Map has 16 elements");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    WeakHashMap hm = buildHM();
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
    WeakHashMap hm  = new WeakHashMap();
    th.check( hm.put(null , this ) == null , "check on return value -- 1");
    th.check( hm.get(null) == this , "check on value -- 1");
    th.check( hm.put(null , "a" ) == this , "check on return value -- 2");
    th.check( "a".equals(hm.get(null)) , "check on value -- 2");
    th.check( "a".equals(hm.put(null , "a" )), "check on return value -- 3");
    th.check( "a".equals(hm.get(null)) , "check on value -- 3");
    th.check( hm.size() == 1 , "only one key added");
    th.check( hm.put("a" , null ) == null , "check on return value -- 4");
    th.check( hm.get("a") == null , "check on value -- 4");
    th.check( hm.put("a" , this ) == null , "check on return value -- 5");
    th.check( hm.get("a") == this , "check on value -- 5");
    th.check( hm.size() == 2 , "two keys added");

  }

/**
* implemented. <br>
*
*/
  public void test_putAll(){
    th.checkPoint("putAll(java.util.Map)void");
    WeakHashMap hm  = new WeakHashMap();
    hm.putAll(new Hashtable());
    th.check(hm.isEmpty() , "nothing addad");
    hm.putAll(buildHM());
    Vector store = keys;
    th.check(hm.size() , 16 , "checking if all enough elements are added -- 1");
    th.check(hm, buildHM() , "check on all elements -- 1");
    hm.put(null ,this);
    hm.putAll(buildHM());
    th.check(hm.size() , 16 , "checking if all enough elements are added -- 2");
    store = keys;
    th.check(hm, (buildHM()) , "check on all elements -- 2");
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
    WeakHashMap hm  = buildHM();
    th.check(hm.remove(null) == null , "checking return value -- 1");
    th.check(hm.remove(null) == null , "checking return value -- 2");
    th.check(!hm.containsKey(null) , "checking removed key -- 1");
    th.check(!hm.containsValue(null) , "checking removed value -- 1");
    for (int i = 0 ; i < 15 ; i++) {
    	th.check( ("a"+i+" value").equals(hm.remove("a"+i)), " removing a"+i);
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
    WeakHashMap hm  = buildHM();
    Set s = hm.entrySet();
    Iterator it= s.iterator();
    java.util.Map.Entry me=null;
    it.next();
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }
    th.check( s.size() == 16 );
    hm.remove("a12");
    th.check( s.size() == 15 );
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
    //th.debug(hm.debug());
    it= s.iterator();
    try {
    	me = (java.util.Map.Entry)it.next();
      //Thread.sleep(600L);
    	if (me.getKey()==null) me = (java.util.Map.Entry)it.next();
    	th.check( me.hashCode() , (me.getValue().hashCode() ^ me.getKey().hashCode()),"verifying hashCode");
    	th.check(! me.equals(it.next()));
    	
    }
    catch(Exception e) { th.fail("got unwanted exception ,got "+e);
    	th.debug("got ME key = "+me+" and value = "+me.getKey());}

    try {
      //th.debug("got ME key = "+me.getKey()+" and value = "+me.getValue());
    	me.setValue(this);
    	th.check(hm.get(me.getKey()) , this, "set is supported");
    	
    }
    catch(UnsupportedOperationException uoe) {
      th.fail("should not throw an UnsupportedOperationException");
    }
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
     //for (int k=0 ; k < v.size() ; k++ ) { th.debug("got "+v.get(k)+" as element "+k); }
     th.check( hm.isEmpty() , "all elements removed from the WeakHashMap");
    it= s.iterator();
    hm.put(null,"sdf");
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 3");
    }
    catch(ConcurrentModificationException cme){ th.check(true); }
    hm.put(null,"sdf");
    it = s.iterator();
    hm.clear();
    hm.put(null,"sdf");
    System.gc();
    th.check(hm.size(), 1, "checking null key");
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
    WeakHashMap hm = buildHM();
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
      //for (int i = 0 ; i < o.length ; i++ ){ th.debug("element "+i+" is "+o[i]); }
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
      th.check( hm.isEmpty() , "all elements removed from the WeakHashMap");
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
    WeakHashMap hm = buildHM();
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
        th.check( hm.isEmpty() , "all elements removed from the WeakHashMap");
    }
    catch (Exception e) { th.fail("got bad Exception -- got "+e); }
    try {
    	s.add("ADDING");
    	th.fail("should throw an UnsupportedOperationException");
    }
    catch (UnsupportedOperationException uoe) { th.check(true); }


  }

/**
* the goal of this test is to see how the hashtable behaves if we do a lot put's and removes. <br>
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
    do_behaviourtest(0.70f);
    do_behaviourtest(0.75f);
    do_behaviourtest(0.95f);
    do_behaviourtest(1.0f);

    }
  protected void sleep(int time){
  	try { Thread.sleep(time); }
  	catch (Exception e) {}	
  }

  protected void check_presence(WeakHashMap h){
    th.check( h.get(st) != null, "checking presence st -- sequence "+sqnce);
    th.check( h.get(sh) != null, "checking presence sh -- sequence "+sqnce);
    th.check( h.get(i) != null, "checking presence i -- sequence "+sqnce);
    th.check( h.get(b) != null, "checking presence b -- sequence "+sqnce);
    th.check( h.get(l) != null, "checking presence l -- sequence "+sqnce);
    sqnce++;
  }

  protected void do_behaviourtest(float loadFactor) {

    th.checkPoint("behaviour testing with loadFactor "+loadFactor);
    WeakHashMap h = new WeakHashMap(11 , loadFactor);
    int j=0;
    Float f;
    h.put(st,"a"); h.put(b,"byte"); h.put(sh,"short"); h.put(i,"int"); h.put(l,"long");
    check_presence(h);
    sqnce = 1;
    for ( ; j < 100 ; j++ ){
      f = new Float((float)j);
      h.put(f,f);
    }
    th.check(h.size() == 105,"size checking -- 1 got: "+h.size());
    check_presence(h);
    for ( ; j < 200 ; j++ ){
      f = new Float((float)j);
      h.put(f,f);
    }
    th.check(h.size() == 205,"size checking -- 2 got: "+h.size());
    check_presence(h);

    for ( ; j < 300 ; j++ ){
      f = new Float((float)j);
      h.put(f,f);
    }
    th.check(h.size() == 305,"size checking -- 3 got: "+h.size());
    check_presence(h);
    //replacing values -- checking if we get a non-zero value
    th.check("a".equals(h.put(st,"na")), "replacing values -- 1 - st");
    th.check("byte".equals(h.put(b,"nbyte")), "replacing values -- 2 - b");
    th.check("short".equals(h.put(sh,"nshort")), "replacing values -- 3 -sh");
    th.check("int".equals(h.put(i,"nint"))  , "replacing values -- 4 -i");
    th.check("long".equals(h.put(l,"nlong")), "replacing values -- 5 -l");


    for ( ; j > 199 ; j-- ){
      f = new Float((float)j);
      h.remove(f);
    }
    th.check(h.size() == 205,"size checking -- 4 got: "+h.size());
    check_presence(h);
    for ( ; j > 99 ; j-- ){
      f = new Float((float)j);
      h.remove(f);
    }
    th.check(h.size() == 105,"size checking -- 5 got: "+h.size());
    check_presence(h);
    for ( ; j > -1 ; j-- ){
      f = new Float((float)j);
      h.remove(f);
    }
    th.check(h.size() == 5  ,"size checking -- 6 got: "+h.size());

    th.debug(h.toString());
    check_presence(h);
  }

/**
** implemented
**
*/
  public void test_WeakRefBehaviour(){
    th.checkPoint("behaviour testing of 'null' key");
    WeakHashMap hm = buildHM();
    keys = null;
    hm.put(null,"abc");
    th.check(hm.get(null),"abc" , "checking presence of 'null'");
    for(int i = 0 ; i < 10 ; i++){
      System.gc();
      if(hm.size() == 1){
        break;
      }
    }
    th.check(hm.size() , 1, "only 'null' key should remain");

    th.checkPoint("behaviour testing of WeakReferences");
    hm = buildHM();
    hm.remove(null);
    Iterator it = hm.entrySet().iterator();
    int size = hm.size();
    while(it.hasNext()){
      java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
      keys.remove(entry.getKey());
      entry = null;
      System.gc();
      th.check(hm.size() , size--);
    }

    hm = buildHM();
    it = hm.entrySet().iterator();
    size = hm.size();
    while(it.hasNext()){
      java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
      it.remove();
      th.check(hm.size() , --size);
    }
    try {
      it.remove();
      th.fail("should throw an IllegalStateException");
    }
    catch(IllegalStateException ise){
      th.check(true);
    }

    Integer key = new Integer(25);
    hm.clear();
    hm.put(key,key);
    key = new Integer(25);
    th.check(hm.put(key, "REMOVE ME"), key, "checking put");

    for(int i = 0 ; i < 10 ; i++){
      System.gc();
      if(hm.size() == 0){
        break;
      }
      try {
        Thread.sleep(200);
      }
      catch(InterruptedException ie){}
    }
    th.check(hm.size(), 0 , "only first key counts");
  }
}
