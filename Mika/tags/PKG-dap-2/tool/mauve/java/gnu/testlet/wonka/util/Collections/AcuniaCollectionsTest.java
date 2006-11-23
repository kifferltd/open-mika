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


package gnu.testlet.wonka.util.Collections; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.Collections    <br>
*
*/
public class AcuniaCollectionsTest implements Testlet
{
  protected TestHarness th;
  protected HashComparator hc = new HashComparator();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.Collections");
       test_enumeration();
       test_fields();
       test_max();
       test_min();
       test_binarySearch();
       test_copy();
       test_fill();
       test_nCopies();
       test_reverse();
       test_shuffle();
       test_sort();
       test_singleton();
       test_reverseOrder();
       test_synchronizedCollection();
       test_synchronizedList();
       test_synchronizedMap();
       test_synchronizedSet();
       test_synchronizedSortedMap();
       test_synchronizedSortedSet();
       test_unmodifiableCollection();
       test_unmodifiableList();
       test_unmodifiableMap();
       test_unmodifiableSet();
       test_unmodifiableSortedMap();
       test_unmodifiableSortedSet();
    }

    protected List buildList(int size){
     	ArrayList al = new ArrayList();
     	for (int i=0;i < size ; i++){
     	 	al.add("string "+i);
     	}
     	return al;
    }

    protected class HashComparator implements Comparator{
     	public int compare(Object o, Object t){
     	 	int ho = (o == null ? 0 : o.hashCode());
     	 	int ht = (t == null ? 0 : t.hashCode());
     	 	return ho - ht;
     	}

    }

/**
* implemented. <br>
*
*/
  public void test_enumeration(){
    th.checkPoint("enumeration(java.util.Collection)java.util.Enumeration");
    List l = buildList(10); 	
    List ls = buildList(10);
    Enumeration e = Collections.enumeration(l);
    boolean b=true;
    while (e.hasMoreElements()){
     	b &= ls.remove(e.nextElement());
    }
    th.check(b && ls.isEmpty());
    try {
     	e.nextElement();
     	th.fail("should throw a NoSuchElementException");
    }
    catch (NoSuchElementException nsee){ th.check(true); }
    Set s = new HashSet();
    e = Collections.enumeration(s);
    th.check(! e.hasMoreElements() , "empty set");
    try {
     	e.nextElement();
     	th.fail("should throw a NoSuchElementException");
    }
    catch (NoSuchElementException nsee){ th.check(true); }
    s.add("element");	
    e = Collections.enumeration(s);
    th.check(e.hasMoreElements() , "not an empty set");
    s.add("another");
// this test is just to find out if the Enumeration is build on top of an Iterator
/*    try {
     	e.nextElement();
     	th.fail("should throw a ConcurrentModificationException");
    }
    catch (Exception cme){ th.check(cme instanceof ConcurrentModificationException); }
*/
  }

/**
*  implemented. <br>
*
*/
  public void test_fields(){
    th.checkPoint("EMPTY_LIST");
    List l = Collections.EMPTY_LIST;
    th.check(l.size() , 0 , "empty list");
    try {
     	l.add("String");
     	th.fail("shoult throw an UnsupportedOperationException -- 1");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    th.checkPoint("EMPTY_SET");
    Set s = Collections.EMPTY_SET;
    th.check(s.size() , 0 , "empty set");
    try {
     	s.add("String");
     	th.fail("shoult throw an UnsupportedOperationException -- 1");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
  }

/**
*  implemented. <br>
*
*/
  public void test_max(){
    th.checkPoint("max(java.util.Collection)java.lang.Object");
    List l = buildList(20);
    th.check(Collections.max(l) , "string 9" , "verifying maximum -- 1");
    try {
    	Collections.max(Collections.EMPTY_SET);
        th.fail("should throw a NoSuchElementException");
    }catch(NoSuchElementException nsee){ th.check(true); }
    l = new ArrayList();
    l.add(null);
    th.check(Collections.max(l) , null , "verifying maximum -- 1");
    try {
	Collections.max(null);
	th.fail("should throw a NullPointerException");
    } catch(NullPointerException npe) { th.check(true); }


    th.checkPoint("max(java.util.Collection,java.util.Comparator)java.lang.Object");
    l = buildList(20);
    l.add(4,null);
    l.add(14,null);
    th.check(Collections.max(l,hc) , "string 9" , "verifying maximum -- 1");
    try {
    	Collections.max(Collections.EMPTY_SET,hc);
        th.fail("should throw a NoSuchElementException");
    }catch(NoSuchElementException nsee){ th.check(true); }
    l = new ArrayList();
    l.add(null);
    th.check(Collections.max(l,hc) , null , "verifying maximum -- 1");
    l.add("null");
    try {
	Collections.max(null,hc);
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }
    try {
	th.debug("got: "+Collections.max(l,null));
	th.fail("should throw a NullPointerException -- 2");
    } catch(NullPointerException npe) { th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_min(){
    th.checkPoint("min(java.util.Collection)java.lang.Object");
    List l = buildList(20);
    th.check(Collections.min(l) , "string 0" , "verifying minimum -- 1");
    try {
    	Collections.min(Collections.EMPTY_SET);
        th.fail("should throw a NoSuchElementException");
    }catch(NoSuchElementException nsee){ th.check(true); }
    l = new ArrayList();
    l.add(null);
    th.check(Collections.min(l) , null , "verifying minimum -- 1");
    try {
	Collections.min(null);
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }

    th.checkPoint("min(java.util.Collection,java.util.Comparator)java.lang.Object");
    l = buildList(20);
    l.add(4,null);
    l.add(14,null);
    th.check(Collections.min(l,hc) , "string 10" , "verifying minimum -- 1");
    try {
    	Collections.min(Collections.EMPTY_SET,hc);
        th.fail("should throw a NoSuchElementException");
    }catch(NoSuchElementException nsee){ th.check(true); }
    l = new ArrayList();
    l.add(null);
    th.check(Collections.min(l,hc) , null , "verifying minimum -- 1");
    l.add("null");
    try {
	Collections.min(null,hc);
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }
    try {
	th.debug("got: "+Collections.min(l,null));
	th.fail("should throw a NullPointerException -- 2");
    } catch(NullPointerException npe) { th.check(true); }

  }

/**
*  implemented. <br>
*
*/
  public void test_binarySearch(){
    th.checkPoint("binarySearch(java.util.List,java.lang.Object)int");
    List l = buildList(10);
    th.check(Collections.binarySearch(l, "not found"), -1,"(- index - 1) is returned if not found -- 1");
    th.check(Collections.binarySearch(l, "string 5bis"), -7,"(- index - 1) is returned if not found -- 2");
    th.check(Collections.binarySearch(l, "wstring"), -11,"(- index - 1) is returned if not found -- 3");
    th.check(Collections.binarySearch(l, "string 8bis"), -10,"(- index - 1) is returned if not found -- 4");
    th.check(Collections.binarySearch(l, "string 0"), 0,"index  is returned if found -- 1");
    th.check(Collections.binarySearch(l, "string 5"), 5,"index is returned if found -- 2");
    th.check(Collections.binarySearch(l, "string 9"), 9,"index is returned if found -- 3");
    th.check(Collections.binarySearch(Collections.EMPTY_LIST, null), -1,"(-index -1) is returned if not found -- 5");
    try {
	Collections.binarySearch(null,"");
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }

    th.checkPoint("binarySearch(java.util.List,java.lang.Object,java.util.Comparator)int");
    th.check(Collections.binarySearch(l, "not found",hc), -1,"(- index - 1) is returned if not found -- 1");
    th.check(Collections.binarySearch(l, null,hc), -1,"(- index - 1) is returned if not found -- 2");
    th.check(Collections.binarySearch(l, "string 0",hc), 0,"index  is returned if found -- 1");
    th.check(Collections.binarySearch(l, "string 5",hc), 5,"index is returned if found -- 2");
    th.check(Collections.binarySearch(l, "string 9",hc), 9,"index is returned if found -- 3");
    try {
	Collections.binarySearch(null,"",hc);
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }
    try {
	Collections.binarySearch(l,"",null);
	th.fail("should throw a NullPointerException -- 2");
    } catch(NullPointerException npe) { th.check(true); }

  }

/**
*  implemented. <br>
*
*/
  public void test_copy(){
    th.checkPoint("copy(java.util.List,java.util.List)void");
    List ls = buildList(10);
    List ld = new ArrayList();
    ld.add(null);     ld.add(null);    ld.add(null);
    try {
     	Collections.copy(ld,ls);
     	th.fail("should throw an IndexOutOfBoundsException");
    }catch (IndexOutOfBoundsException ioobe){ th.check(true); }
    try {
	Collections.copy(null,ls);
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }
    try {
	Collections.copy(ld,null);
	th.fail("should throw a NullPointerException -- 2");
    } catch(NullPointerException npe) { th.check(true); }
    while (ld.size()<11){ld.add(null);}
    Collections.copy(ld,ls);
    th.check(ld.get(10) , null);
    ld.remove(null);
    th.check(ld.equals(ls) , "verify copied parts");
  }

/**
* implemented. <br>
*
*/
  public void test_fill(){
    th.checkPoint("fill(java.util.List,java.lang.Object)void");
    List ls = buildList(5);
    Collections.fill(ls, null);
    th.check(ls.get(0) == null && ls.get(1) == null && ls.get(2) == null &&
    	ls.get(3) == null && ls.get(4) == null, "check if all elements are set to null");
    try {
	Collections.fill(null,"a");
	th.fail("should throw a NullPointerException -- 1");
    } catch(NullPointerException npe) { th.check(true); }
    ls = new ArrayList();
    Collections.fill(ls,null);
    ls.add("a");
    Collections.fill(ls,"b");
    th.check(ls.get(0) , "b");


  }

/**
* implemented. <br>
*
*/
  public void test_nCopies(){
    th.checkPoint("nCopies(int,java.lang.Object)java.util.List");
    try {
    	Collections.nCopies(-1, "a");	
    	th.fail("should throw an IllegalArgumentException");
    }catch (IllegalArgumentException iae){ th.check(true); }
    List l = Collections.nCopies(3,"a");
    try {
     	l.add("String");
     	th.fail("shoult throw an UnsupportedOperationException -- 1");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    try {
     	l.remove("a");
     	th.fail("shoult throw an UnsupportedOperationException -- 2");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    try {
     	l.set(2,"String");
     	th.fail("shoult throw an UnsupportedOperationException -- 3");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    boolean b = "a".equals(l.get(0));
    b &= "a".equals(l.get(1));	
    b &= "a".equals(l.get(2));	
    th.check(b , "all elements should be 'a'");

  }

/**
* implemented. <br>
*
*/
  public void test_reverse(){
    th.checkPoint("reverse(java.util.List)void");
    List l = new ArrayList();
    Collections.reverse(l);
    l.add("a");
    Collections.reverse(l);
    l.add("b");
    Collections.reverse(l);
    th.check("b".equals(l.get(0)) && "a".equals(l.get(1)));
    l.add("c");
    Collections.reverse(l);
    th.check("b".equals(l.get(2)) && "a".equals(l.get(1))&& "c".equals(l.get(0)));

  }

/**
* implemented. <br>
*
*/
  public void test_shuffle(){
    th.checkPoint("shuffle(java.util.List)void");
    ArrayList als = (ArrayList) buildList(7);
    ArrayList alo = new ArrayList(als);
    Collections.shuffle(als);
    th.debug("shuffled result 1 = \n"+als);
    ArrayList al1 = new ArrayList(alo);
    th.debug("shuffled result 2 = \n"+al1);
    Collections.shuffle(al1);
    ArrayList al2 = new ArrayList(alo);
    Collections.shuffle(al2);
    th.debug("shuffled result 3 = \n"+al2);

    th.checkPoint("shuffle(java.util.List,java.util.Random)void");

  }

/**
* implemented. <br>
*
*/
  public void test_sort(){
    th.checkPoint("sort(java.util.List)void");
    List l  = buildList(10);
    for (int i=0 ; i < 10 ; i++){ l.add("string 9"+i); }
    for (int i=0 ; i < 10 ; i++){ l.add("string 99"+i); }
    for (int i=0 ; i < 10 ; i++){ l.add("string 999"+i); }
    List ls = new ArrayList(l);
    Random r = new Random();
    boolean b = true;
    for (int i = 0 ; i < 25 ; i++){
	Collections.shuffle(ls,r);
	Collections.sort(ls);
	b &= l.equals(ls);
    }
    th.check(b , "check if sort works");
    th.checkPoint("sort(java.util.List)void");
    l  = buildList(10);
    ls = buildList(10);
    b = true;
    for (int i = 0 ; i < 15 ; i++){
	Collections.shuffle(ls,r);
	Collections.sort(ls);
	b &= l.equals(ls);
    }
    th.check(b , "check if sort works");
  }

/**
* implemented. <br>
*
*/
  public void test_singleton(){
    th.checkPoint("singleton(java.lang.Object)java.util.Set");
    Set s = Collections.singleton(null);
    th.check(s.size(),1);
    Iterator it = s.iterator();
    th.check(it.next(), null);
    try {
     	it.remove();
     	th.fail("shoult throw an UnsupportedOperationException -- 1");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    try {
     	s.add("String");
     	th.fail("shoult throw an UnsupportedOperationException -- 2");
    } catch(UnsupportedOperationException uoe){ th.check(true); }
    try {
     	s.remove(null);
     	th.fail("shoult throw an UnsupportedOperationException -- 3");
    } catch(UnsupportedOperationException uoe){ th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_reverseOrder(){
    th.checkPoint("reverseOrder()java.util.Comparator");
    List l = buildList(10);
    Collections.sort(l,Collections.reverseOrder());
    th.check("string 9".equals(l.get(0)) && "string 8".equals(l.get(1))&& "string 7".equals(l.get(2)));
    th.check("string 6".equals(l.get(3)) && "string 5".equals(l.get(4))&& "string 4".equals(l.get(5)));
    th.check("string 3".equals(l.get(6)) && "string 2".equals(l.get(7))&& "string 1".equals(l.get(8))&& "string 0".equals(l.get(9)));
    th.debug(""+l);
  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedCollection(){
    th.checkPoint("synchronizedCollection(java.util.Collection)java.util.Collection");

  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedList(){
    th.checkPoint("synchronizedList(java.util.List)java.util.List");

  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedMap(){
    th.checkPoint("synchronizedMap(java.util.Map)java.util.Map");

  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedSet(){
    th.checkPoint("synchronizedSet(java.util.Set)java.util.Set");

  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedSortedMap(){
    th.checkPoint("synchronizedSortedMap(java.util.SortedMap)java.util.SortedMap");
  }

/**
*   not implemented. <br>
*
*/
  public void test_synchronizedSortedSet(){
    th.checkPoint("synchronizedSortedSet(java.util.SortedSet)java.util.SortedSet");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableCollection(){
    th.checkPoint("unmodifiableCollection(java.util.Collection)java.util.Collection");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableList(){
    th.checkPoint("unmodifiableList(java.util.List)java.util.List");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableMap(){
    th.checkPoint("unmodifiableMap(java.util.Map)java.util.Map");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableSet(){
    th.checkPoint("unmodifiableSet(java.util.Set)java.util.Set");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableSortedMap(){
    th.checkPoint("unmodifiableSortedMap(java.util.SortedMap)java.util.SortedMap");

  }

/**
*   not implemented. <br>
*
*/
  public void test_unmodifiableSortedSet(){
    th.checkPoint("unmodifiableSortedSet(java.util.SortedSet)java.util.SortedSet");
    TreeSet ts = new TreeSet(buildList(10));
    SortedSet s = Collections.unmodifiableSortedSet(ts);
    SortedSet tls = s.tailSet("string 4");
    Iterator it = tls.iterator();
    it.next();
    try {
    	it.remove();
    	th.fail("should throw an UnsupportedOperationException");
    }catch(UnsupportedOperationException uoe){ th.check(true); }
  }

}
