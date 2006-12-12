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


package gnu.testlet.wonka.util.Observable; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.*; // at least the class you are testing ...

/**
*  this file contains test for java.util.Observable   <br>
*
*/
public class AcuniaObservableTest implements Testlet, Observer {

  protected TestHarness th;

  public boolean updated;
  public Object arg=this;
  public Observable obs;

  public void test (TestHarness harness) {
       th = harness;
       th.setclass("java.util.Observable");
       test_addObserver();
       test_countObservers();
       test_deleteObserver();
       test_deleteObservers();
       test_clearChanged();
       test_hasChanged();
       test_notifyObservers();
       test_setChanged();
  }

  public void clear(){
  	updated = false;
  	obs = null;
  	arg = this;  	
  }

  public boolean verify(Observable o, Object ar){
   	return updated && (o == obs) && (ar == arg);
  }

  public void update(Observable ob, Object ar){
      	updated = true;
      	arg = ar;
      	obs = ob;
  }



/**
* implemented. <br>
*
*/
  public void test_addObserver(){
    th.checkPoint("addObserver(java.util.Observer)void");
    Observable o = new Observable();
    o.addObserver(this);
    o.addObserver(this);
    th.check(o.countObservers(), 1 , "observer only added once ...");
    o.addObserver(new AcuniaObservableTest());
    th.check(o.countObservers(), 2 , "observer added -- 1");
    o.addObserver(new AcuniaObservableTest());
    o.addObserver(new AcuniaObservableTest());
    th.check(o.countObservers(), 4 , "observer added -- 2");
    try {
     	o.addObserver(null);
        th.check(o.countObservers(), 4 , "observer added -- 3");
    } 	
    catch (NullPointerException npe){ th.check(true); }
  }

/**
*  implemented. <br>
*
*/
  public void test_countObservers(){
    th.checkPoint("countObservers()int");
    Observable o = new Observable();
    th.check(o.countObservers(), 0 , "no observers added");
    o.addObserver(this);
    o.addObserver(this);
    th.check(o.countObservers(), 1 , "observer only added once ...");
    o.addObserver(new AcuniaObservableTest());
    th.check(o.countObservers(), 2 , "observer added -- 1");
    o.addObserver(new AcuniaObservableTest());
    o.addObserver(new AcuniaObservableTest());
    th.check(o.countObservers(), 4 , "observer added -- 2");
    o.deleteObserver(this);
    th.check(o.countObservers(), 3 , "observer deleted -- 3");
    o.deleteObservers();
    th.check(o.countObservers(), 0 , "all observers deleted -- 4");
    o.addObserver(this);
    th.check(o.countObservers(), 1 , "one observer added -- 5");
    try {
     	o.deleteObserver(null);
        th.check(o.countObservers(), 1 , "one observer added -- 6");
    } 	
    catch (NullPointerException npe){ th.check(false); }
  }

/**
* implemented. <br>
*
*/
  public void test_deleteObserver(){
    th.checkPoint("deleteObserver(java.util.Observer)void");
    Observable o = new Observable();
    o.addObserver(this);
    Observer obs = new AcuniaObservableTest();
    o.addObserver(obs);
    o.deleteObserver(this);
    th.check(o.countObservers(), 1 , "observer deleted -- 1");
    o.deleteObserver(this);
    th.check(o.countObservers(), 1 , "observer already deleted -- 2");
    o.deleteObserver(obs);
    th.check(o.countObservers(), 0 , "observer deleted -- 3");
    o.deleteObserver(obs);
    th.check(o.countObservers(), 0 , "observer already deleted -- 4");
    o.addObserver(this);
    th.check(o.countObservers(), 1 , "observer added -- 5");
    o.deleteObserver(this);
    th.check(o.countObservers(), 0 , "observer deleted -- 6");
  }

/**
* implemented. <br>
*
*/
  public void test_deleteObservers(){
    th.checkPoint("deleteObservers()void");
    Observable o = new Observable();
    o.addObserver(this);
    Observer obs = new AcuniaObservableTest();
    o.addObserver(obs);
    o.deleteObservers();
    th.check(o.countObservers(), 0 , "observers deleted -- 1");
    o.addObserver(this);
    th.check(o.countObservers(), 1 , "observer added -- 5");
    o.deleteObservers();
    th.check(o.countObservers(), 0 , "observer deleted -- 6");

  }

/**
* implemented. <br>
*
*/
  public void test_hasChanged(){
    th.checkPoint("hasChanged()boolean");
    AcuniaObservable ao = new AcuniaObservable();
    th.check(! ao.hasChanged() , "not changed -- 1");
    ao.setChanged();
    th.check(  ao.hasChanged() , "changed -- 2");
    ao.setChanged();
    th.check(  ao.hasChanged() , "changed -- 3");
    ao.clearChanged();
    th.check(! ao.hasChanged() , "not changed -- 4");
    ao.clearChanged();
    th.check(! ao.hasChanged() , "not changed -- 5");
    ao.setChanged();
    th.check(  ao.hasChanged() , "changed -- 6");
    ao.clearChanged();
    th.check(! ao.hasChanged() , "not changed -- 7");

  }

/**
* not implemented. <br>
* protected method ...
* @see hasChanged
*/
  public void test_clearChanged(){
    th.checkPoint("clearChanged()void");
  }

/**
* not implemented. <br>
* protected method ...
* @see hasChanged
*/
  public void test_setChanged(){
    th.checkPoint("setChanged()void");

   }

/**
*   not implemented. <br>
*
*/
  public void test_notifyObservers(){
    th.checkPoint("notifyObservers()void");
    AcuniaObservable o = new AcuniaObservable();
    o.addObserver(this);
    AcuniaObservableTest obs = new AcuniaObservableTest();
    o.addObserver(obs);
    o.setChanged();
    try {
     	o.notifyObservers();
    }
    catch(NullPointerException np){
     	th.fail("bad !");
    }
//    o.deleteObserver(null);
    clear();
    obs.clear();
    o.setChanged();
    o.notifyObservers();
    th.check(!o.hasChanged(),"cleared by notifyObservers");	
    th.check(verify(o,null), "update recieved -- 1");
    th.check(obs.verify(o,null), "update recieved -- 2");

    th.checkPoint("notifyObservers(java.lang.Object)void");
    clear();
    obs.clear();
    o.notifyObservers(null);
    th.check(!verify(o,null), "update recieved -- 3");
    th.check(!obs.verify(o,null), "update recieved -- 4");
    o.setChanged();
    clear();
    obs.clear();
    o.notifyObservers("a");
    th.check(!o.hasChanged(),"cleared by notifyObservers");	
    th.check(verify(o,"a"), "update recieved -- 5");
    th.check(obs.verify(o,"a"), "update recieved -- 6");

  }

}
