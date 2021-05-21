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


package gnu.testlet.wonka.util.AbstractList; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.AbstractList   <br>
*
*/
public class SMAbstractListTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.AbstractList");
       test_get();
       test_indexOf();
       test_lastIndexOf();
       test_add();
       test_addAll();
       test_clear();
       test_remove();
       test_removeRange();
       test_set();
       test_iterator();
       test_listIterator();
       test_subList();
       test_hashCode();
       test_equals();
     }


/**
*   not implemented. <br>
*   Abstract Method
*/
  public void test_get(){
    th.checkPoint("()");
  }

/**
* implemented. <br>
*
*/
  public void test_indexOf(){
    th.checkPoint("indexOf(java.lang.Object)int");
    SMExAbstractList eal = new SMExAbstractList();
    eal.v.add("ab");    eal.v.add("bc");    eal.v.add(null);
    eal.v.add("ab");    eal.v.add("cd");    eal.v.add(this);
    th.check( eal.indexOf(this) == 5 , "checking return value -- 1");
    th.check( eal.indexOf(null) == 2 , "checking return value -- 2");
    th.check( eal.indexOf("ab") == 0 , "checking return value -- 3");
    th.check( eal.indexOf("ab") == 0 , "checking return value -- 4");
    th.check( eal.indexOf("b") == -1 , "checking return value -- 5");
    eal.v.remove(null);
    th.check( eal.indexOf(null) == -1 , "checking return value -- 6");
    eal.v.remove(this);
    th.check( eal.indexOf(this) == -1 , "checking return value -- 7");

  }

/**
* implemented. <br>
*
*/
  public void test_lastIndexOf(){
    th.checkPoint("lastIndexOf(java.lang.Object)int");
    SMExAbstractList eal = new SMExAbstractList();
    eal.v.add("ab");    eal.v.add("bc");    eal.v.add(null);
    eal.v.add("ab");    eal.v.add(null);    eal.v.add(this);
    th.check( eal.lastIndexOf(this) == 5 , "checking return value -- 1");
    th.check( eal.lastIndexOf(null) == 4 , "checking return value -- 2");
    th.check( eal.lastIndexOf("ab") == 3 , "checking return value -- 3");
    th.check( eal.lastIndexOf("ab") == 3 , "checking return value -- 4");
    th.check( eal.lastIndexOf("b") == -1 , "checking return value -- 5");
    eal.v.remove(4);
    th.check( eal.lastIndexOf(null) == 2 , "checking return value -- 6");
    eal.v.remove(null);
    th.check( eal.lastIndexOf(null) == -1 , "checking return value -- 7");

  }

/**
* implemented. <br>
*
*/
  public void test_add(){
   th.checkPoint("add(java.lang.Object)boolean");
    SMExAbstractList eal = new SMExAbstractList();
    int mc = eal.getMC();
    th.check(eal.add(this), "checking return value -- 1");
//    we should change the modCount if we add, remove or set!
//    th.check(mc != eal.getMC() , "got mc "+mc+" and modCount "+eal.getMC());
    th.check(eal.v.get(0) == this , "checking add -- 1");
    th.check(eal.add("a"), "checking return value -- 2");
    th.check("a".equals(eal.v.get(1)) , "checking add -- 2");
    th.check(eal.add("b"), "checking return value -- 3");
    th.check("b".equals(eal.v.get(2)) , "checking add -- 3");
    th.check(eal.add(null), "checking return value -- 4");
    th.check(eal.v.get(3) == null , "checking add -- 4");
    th.check(eal.add(null), "checking return value -- 5");
    th.check(eal.v.get(4) == null , "checking add -- 5");
    eal.set_edit(false);
    try {
    	eal.add("a");
    	th.fail("should throw an UnsupportedOperationExeption");
    	}
    catch (UnsupportedOperationException uoe) { th.check(true);}
   th.checkPoint("add(int,java.lang.Object)void");
    try {
    	eal.add(3,"a");
    	th.fail("should throw an UnsupportedOperationExeption");
    	}
    catch (UnsupportedOperationException uoe) { th.check(true);}
  }

/**
* implemented. <br>
*
*/
  public void test_addAll(){
   th.checkPoint("addAll(java.util.Collection)boolean");
    //inherited from AbstractCollection ...

   th.checkPoint("addAll(int,java.util.Collection)boolean");
    SMExAbstractList eal = new SMExAbstractList();
    try {	
    	eal.addAll(0, null);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }
    Vector v = new Vector();
    th.check(! eal.addAll(0, v), "checking returnvalue -- 1");
    th.check( eal.size() == 0 , "nothing added yet");
    v.add(this); 	v.add(null);	v.add("a");
    v.add("b");		v.add("a");	v.add(null);
    try {	
    	eal.addAll(-1, v);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ioobe) { th.check(true); }
    try {	
    	eal.addAll(1, v);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ioobe) { th.check(true); }
    th.check( eal.addAll(0, v), "checking returnvalue -- 1");
    th.check( eal.v.get(0) == this && eal.v.get(1) == null && eal.v.get(5) == null );
    th.check( eal.addAll(3, v), "checking returnvalue -- 2");
    th.check( eal.v.get(0) == this && "a".equals(eal.v.get(2)) && eal.v.get(3) == this );
    th.check( eal.v.get(8) == null && "b".equals(eal.v.get(9)) && eal.v.get(11) == null );
    v =new Vector();
    th.check(! eal.addAll(3, v), "checking returnvalue -- 3");

  }

/**
* implemented. <br>
*
*/
  public void test_clear(){
    th.checkPoint("clear()void");
    SMExAbstractList eal = new SMExAbstractList();
    eal.set_updateMC(true);
    eal.clear();
    th.check(eal.get_dRR() , "check if removeRange was called");
    th.check(eal.get_from() == 0 && eal.get_to() == 0);
    eal = new SMExAbstractList();
    eal.v.add("a"); eal.v.add("b"); eal.v.add("c"); eal.v.add("d");	
    eal.clear();
    th.check(eal.get_dRR() , "check if removeRange was called");
    th.check(eal.get_from() == 0 && eal.get_to() == 4);
    th.check(eal.v.size() == 0 , "checking if everything is gone");
  }

/**
* implemented. <br>
*
*/
  public void test_remove(){
    th.checkPoint("remove(int)java.lang.Object");
    SMExAbstractList eal = new SMExAbstractList();
    eal.set_edit(false);
    eal.v.add("a");
    try {
    	eal.remove(0);	
    	th.fail("should throw an UnsupportedOperationException");
    	}
    catch(UnsupportedOperationException uoe) { th.check(true);}

  }

/**
* implemented. <br>
*
*/
  public void test_removeRange(){
    th.checkPoint("removeRange(int,int)void");
    SMExAbstractList eal = new SMExAbstractList();
    for (int i=0 ; i < 20 ; i++) { eal.v.add("a"+i); }
    try {
    	eal.removeRange(10,25);
    	th.fail("should throw an exception");
    }		
    catch(NoSuchElementException e) { th.check(true); }
    Vector v = (Vector) eal.v.clone();
    eal.removeRange(10,10);
    th.check(eal.v.equals(v) , "nothing removed -- 1");
    eal.removeRange(9,10);
    v.remove(9);
    th.check(eal.v.equals(v) , "one element removed");
    eal.removeRange(5,7);
    v.remove(5);
    v.remove(5);
    //th.debug("got v = "+v+", and eal.v = "+eal.v);
    th.check(eal.v.equals(v) , "two elements removed");
    eal.removeRange(2,1);
    th.check(eal.v.equals(v) , "nothing removed -- 2");
    try {
    	eal.removeRange(-1,5);
    	th.fail("should throw an exception");
    }		
    catch(IndexOutOfBoundsException e) { th.check(true); th.debug(e);}


  }

/**
* implemented. <br>
*
*/
  public void test_set(){
    th.checkPoint("set(int,java.lang.Object)java.lang.Object");
    SMExAbstractList eal = new SMExAbstractList();
    eal.set_edit(false);
    eal.v.add("a");
    try {
    	eal.set(0,"b");	
    	th.fail("should throw an UnsupportedOperationException");
    	}
    catch(UnsupportedOperationException uoe) { th.check(true);}

  }

/**
* implemented. <br>
* Since the iterator is an innerclass we also test all iterator methods.<br>
*   - hasNext()                                            <br>
*   - next()                                               <br>
*   - remove()                                             <br>
*/
  public void test_iterator(){
    th.checkPoint("iterator()java.util.Iterator");
    SMExAbstractList eal = new SMExAbstractList();
    eal.set_updateMC(true);
    eal.v.add("a");   eal.v.add("b");	eal.v.add("c");
    Iterator it = eal.iterator();
    th.check( it.hasNext() , "true -- 1");
    th.check( "a".equals(it.next()) , "order is important -- 1" );
    th.check( it.hasNext() , "true -- 2");
    th.check( "b".equals(it.next()) , "order is important -- 2" );
    th.check( it.hasNext() , "true -- 3");
    th.check( "c".equals(it.next()) , "order is important -- 3" );
    th.check(! it.hasNext() , "false -- 4");
    th.check(! it.hasNext() , "false -- 5");
    try {
    	it.next();
    	th.fail("should throw a NoSuchElementException");
    	}
    catch(NoSuchElementException nse) { th.check(true); }

    eal.add("changed");
    try {
    	it.remove();
    	th.fail("should throw a ConcurrentModificationException -- 1");
    	}
    catch(ConcurrentModificationException cme) { th.check(true, "remove"); }
    try {
    	it.next();
    	th.fail("should throw a ConcurrentModificationException -- 2");
    	}
    catch(ConcurrentModificationException cme) { th.check(true, "next"); }
    try  {
    /*
     * [CG 20101226] Apache Harmony implementation fails this test because
     * it doesn't take account of the extra element which was created after
     * the iterator was created.  I think this is allowed, because the
     * spec says "The behavior of an iterator is unspecified if the underlying
     * collection is modified while the iteration is in progress in any way
     * other than by calling [remove()]".
     * It's true however that this method should not throw a
     * ConcurrentModificationException: only next, remove, previous, set, and
     * add do that:-
     * http://download.oracle.com/javase/1.4.2/docs/api/java/util/AbstractList.html#modCount
     *
     * WAS: 
    	th.check(it.hasNext());
     */
    	it.hasNext();
    	}
    catch(ConcurrentModificationException cme) {     	
    	th.fail("should not throw a ConcurrentModificationException -- 3");
    }
    it = eal.iterator();
    try {
    	it.remove();	
        th.fail("should throw IllegalStateException");
        }
    catch(IllegalStateException ise) { th.check(true); }
    try {
    	th.debug(eal.v.toString());
    	it.next();
    	it.hasNext();
    	it.remove();
    	th.check(! eal.v.contains("a") && eal.v.size() == 3, "first element removed");
    	it.next();
    	it.remove();
    	th.check(! eal.v.contains("b") && eal.v.size() == 2, "second element removed");
    	it.next();
    	it.remove();
    	th.check(! eal.v.contains("c") && eal.v.size() == 1, "third element removed");
    	it.next();
    	it.remove();
    	th.check( eal.v.isEmpty(), "all are elements removed");
    }
    catch (Exception e) { th.fail("got unexpected exception !, got "+e); }

  }

/**
* implemented. <br>
* Since the listIterator is an innerclass we also test all iterator methods.
*/
  public void test_listIterator(){
    th.checkPoint("listIterator()java.util.ListIterator");
    SMExAbstractList ll = new SMExAbstractList();
    ll.set_updateMC(true);
    ListIterator li = ll.listIterator();
    try {
     	li.next();
     	th.fail("should throw a NoSuchElementException -- 1");
    }
    catch(NoSuchElementException nsee) { th.check(true, "caught exeption -- 1"); }
    try {
     	li.previous();
     	th.fail("should throw a NoSuchElementException -- 2");
    }
    catch(NoSuchElementException nsee) { th.check(true, "caught exeption -- 2"); }
    th.check(!li.hasNext() , "no elements ... -- 1");
    th.check(!li.hasPrevious() , "no elements ... -- 1");
    th.check(li.nextIndex() , 0 ,"nextIndex == 0 -- 1");
    th.check(li.previousIndex() , -1 ,"previousIndex == -1 -- 1");
    li.add("a");
    th.check(!li.hasNext() , "no elements ... -- 2");
    th.check(li.hasPrevious() , "one element ... -- 2");
    th.check(li.nextIndex() , 1 ,"nextIndex == 1 -- 2");
    th.check(li.previousIndex() , 0 ,"previousIndex == 0 -- 2");
    try {
     	li.next();
     	th.fail("should throw a NoSuchElementException -- 3");
    }
    catch(NoSuchElementException nsee) { th.check(true, "caught exeption -- 3"); }
    th.check("a".equals(li.previous()) , "checking previous element -- 1");
    li.add(null);
//    th.debug(ll.toString());
    th.check(li.previousIndex() , 0 ,"previousIndex == 0 -- 3");
    th.check(li.previous() == null , "checking previous element -- 2");
    th.check(li.next() == null , "checking next element -- 1");
    li.add("b");
    th.check("a".equals(li.next()) ,"checking next element -- 2");
    li.add("c");
    try {
     	li.set("not");
     	th.fail("should throw a IllegalStateException -- 1");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 4"); }
    th.check(!ll.contains("not"), "set should not have been executed");
    try {
     	li.remove();
     	th.fail("should throw a IllegalStateException -- 2");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 5"); }
    /*
     * [CG 20101226] Both Harmony and RI fail this test - they throw
     * ConcurrentModificationException, which seems reasonable to me.
    th.check("c".equals(li.previous()) , "checking previous element -- 3");
     */
    li.set("new");
    th.check("new".equals(li.next()) , "validating set");
    li.set("not");
    li.set("notOK");
    li.remove();
    try {
     	li.set("not");
     	th.fail("should throw a IllegalStateException -- 3");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 6"); }
    th.check(!ll.contains("not"), "set should not have been executed");
    try {
     	li.remove();
     	th.fail("should throw a IllegalStateException -- 4");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 7"); }
    try {
     	li.next();
     	th.fail("should throw a NoSuchElementException -- 4");
    }
    catch(NoSuchElementException nsee) { th.check(true, "caught exeption -- 8"); }
    th.check("a",li.previous(),"checking on previous element");
    li.remove();
    try {
     	li.set("not");
     	th.fail("should throw a IllegalStateException -- 5");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 9"); }
    th.check(!ll.contains("not"), "set should not have been executed");
    try {
     	li.remove();
     	th.fail("should throw a IllegalStateException -- 6");
    }
    catch(IllegalStateException ise) { th.check(true, "caught exeption -- 10"); }


  }

/**
* not implemented. <br>
* not implented in wonka !!!
*/
  public void test_subList(){
    th.checkPoint("subList(int,int)List");

  }

/**
* implemented. <br>
*
*/
  public void test_hashCode(){
    th.checkPoint("hashCode()int");
    SMExAbstractList eal = new SMExAbstractList();
    th.check( eal.hashCode() == 1 , "hashCode of empty list is 1");
    int hash=1;
    hash = hash*31+ "a".hashCode();
    eal.v.add("a");
    th.check( eal.hashCode() == hash , "checking hashCode algortihm -- 1");
    hash = hash*31+ "adg".hashCode();
    eal.v.add("adg");
    th.check( eal.hashCode() == hash , "checking hashCode algortihm -- 2");
    hash = hash*31;
    eal.v.add(null);
    th.check( eal.hashCode() == hash , "checking hashCode algortihm -- 3");
    hash = hash*31+ this.hashCode();
    eal.v.add(this);
    th.check( eal.hashCode() == hash , "checking hashCode algortihm -- 4");

  }

/**
* implemented. <br>
*
*/
  public void test_equals(){
    th.checkPoint("equals(java.lang.Object)boolean");
    SMExAbstractList eal = new SMExAbstractList();
    Vector v = new Vector();
    th.check(! eal.equals(null) , "null is allowed");
    th.check(! eal.equals(this) , "not equal to an non-List Object");
    th.check( eal.equals(v) , "equal == true -- 1");
    eal.v.add(null);	v.add(null);
    th.check( eal.equals(v) , "equal == true -- 2");
    eal.v.add(this);	v.add(this);
    th.check( eal.equals(v) , "equal == true -- 3");
    eal.v.add("a");	v.add("b");
    th.check(! eal.equals(v) , "equal != true -- 4");
    eal.v.add("b");	v.add("a");
    th.check(! eal.equals(v) , "equal != true -- 5");
    eal.v.remove("a");
    th.check(! eal.equals(v) , "equal != true -- 5");

  }

}
