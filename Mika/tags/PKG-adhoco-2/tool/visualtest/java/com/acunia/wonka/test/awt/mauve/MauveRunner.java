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


package com.acunia.wonka.test.awt.mauve;

import com.acunia.wonka.test.awt.*;
import java.lang.reflect.*;
import java.awt.Font;
import java.awt.Label;


public class MauveRunner extends VisualTestImpl{

  static Thread runner;
  static Class  mauve_class;
  static Object mauve_instance;
  private static int count;

  static {
    try {
      mauve_class = Class.forName("gnu.testlet.TestRunner");
    }
    catch (ClassNotFoundException e) {
    }

    try {
      mauve_instance = mauve_class.newInstance();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private synchronized void performMauveTest(){
    this.add(new Label("The Mauve test suite "));
    if(runner == null){
      ++count;
      runner = new Thread(new Runner(), "Mauve Test Thread -- "+count);
      runner.setPriority(Thread.NORM_PRIORITY - 1);
      runner.start();
      this.add(new Label("is now running "));
    }
    else {
      this.add(new Label("is already running "));
    }
    this.add(new Label("in a background thread "));
    this.add(new Label("(Run no. "+count+"). "));
    try {
      Method m;
      m = mauve_class.getDeclaredMethod("getTestClass" , new Class[0]);
      String testclass = (String)m.invoke(mauve_instance, new Object[0]);
      m = mauve_class.getDeclaredMethod("getCheckPoint" , new Class[0]);
      String checkpoint = (String)m.invoke(mauve_instance, new Object[0]);
      m = mauve_class.getDeclaredMethod("getTestsTotal" , new Class[0]);
      Integer total = (Integer)m.invoke(mauve_instance, new Object[0]);
      m = mauve_class.getDeclaredMethod("getTestsFailed" , new Class[0]);
      Integer failed = (Integer)m.invoke(mauve_instance, new Object[0]);
      this.add(new Label("Current test class: "+testclass+" "));
      this.add(new Label("Current checkpoint: "+checkpoint+" "));
      this.add(new Label("Failed "+failed.intValue()+" tests of "+total.intValue()+" "));
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    validate();
  }

	public MauveRunner(){
          this.setFont(new Font("helvB17", 1, 21));
	}
	
 	public String getHelpText(){
 	  return "This test will run the mauve test in a background thread.  This has no visible effect."
 	    +" The results will be logged to the VisualTester.";
 	}
 	
  public void start(java.awt.Panel p, boolean b){
    performMauveTest();
  }

  private static class Runner implements Runnable {

    public void run(){
      try {
        Class[] ca = new Class[2];
        ca[0] = mauve_class;
        ca[1] = String.class;
        Method m = mauve_class.getDeclaredMethod("runTests" , ca);
        Object[] obj = new Object[2];
        obj[0] = mauve_instance;
        obj[1] = "wonkatest.properties";
        m.invoke(null, obj);
      } catch (Exception e){
         System.out.println("MauveRunner thread encountered an exception "+e);
         e.printStackTrace();
      }
      runner = null;
    }
  }
}
