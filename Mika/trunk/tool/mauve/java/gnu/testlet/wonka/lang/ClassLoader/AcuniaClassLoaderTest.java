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


package gnu.testlet.wonka.lang.ClassLoader; //complete the package name ...

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//import java.*; // at least the class you are testing ...

/**
*  Written by ACUNIA. <br>
*                        <br>
*  this file contains test for java.lang.ClassLoader <br>
*
*/
public class AcuniaClassLoaderTest implements Testlet
{
  protected TestHarness th;
  protected HashMap hm;	
  protected Object class1;
  protected Object class2;
  protected Object duplicate;

  protected ClassLoader baseCl;
  protected ClassLoader evol1Cl;
  protected ClassLoader atest1Cl;
  protected ClassLoader atest2Cl;
  protected ClassLoader rtest1Cl;
  protected ClassLoader rtest2Cl;
  protected ClassLoader duplicateCl;

  private static final String tc1 = "gnu.testlet.wonka.lang.ClassLoader.TestClass1";
  private static final String tc2 = "gnu.testlet.wonka.lang.ClassLoader.TestClass2";
  private static final String ti1 = "gnu.testlet.wonka.lang.ClassLoader.TestInterface1";
  private static final String ti2 = "gnu.testlet.wonka.lang.ClassLoader.TestInterface2";
  private static final String bsi = "gnu.testlet.wonka.lang.ClassLoader.BasicInterface";
  private static final String abc = "gnu.testlet.wonka.lang.ClassLoader.AbstractBaseClass";
  private static final String ac1 = "gnu.testlet.wonka.lang.ClassLoader.AbstractClass1";
  private static final String ac2 = "gnu.testlet.wonka.lang.ClassLoader.AbstractClass2";
  private static final String ae1 = "gnu.testlet.wonka.lang.ClassLoader.AbstractEvol1";
  private static final String e1i = "gnu.testlet.wonka.lang.ClassLoader.Evol1Interface";

  protected boolean setup() {
    hm = new HashMap();
    th.debug("start seting up ClassLoaderTest");
    try {
      JarFile jf = newJarFile("/CLTest.jar");
      Enumeration e = jf.entries();
      while (e.hasMoreElements()) {
        JarEntry je = (JarEntry) e.nextElement();
        String s = je.getName();
        if (!s.endsWith(".class")) {
          continue;
        }
        int i = s.indexOf('/');
        while (i != -1) {
          s = s.substring(0, i) + "." + s.substring(i + 1);
          i = s.indexOf('/');
        }
        i = s.lastIndexOf('.');
        if (i != -1) {
          s = s.substring(0, i);
        }
        InputStream in = jf.getInputStream(je);
        byte[] bytes = new byte[1024];
        int rd = in.read(bytes, 0, 1024);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (rd != -1) {
          bos.write(bytes, 0, rd);
          rd = in.read(bytes, 0, 1024);
        }
        bytes = bos.toByteArray();
        hm.put(s, bytes);
      }
      HashMap clmap = new HashMap();
      clmap.put(abc, hm.get(abc));
      clmap.put(bsi, hm.get(bsi));
      baseCl = new ExClassLoader(clmap, "baseClassLoader");

      clmap = new HashMap(clmap);
      clmap.put(ae1, hm.get(ae1));
      clmap.put(e1i, hm.get(e1i));
      evol1Cl = new ExClassLoader(baseCl, clmap, "evol1ClassLoader");

      clmap = new HashMap(clmap);
      clmap.put(ac1, hm.get(ac1));
      clmap.put(ti1, hm.get(ti1));
      clmap.put(ac2, hm.get(ac2));
      clmap.put(ti2, hm.get(ti2));
      atest1Cl = new ExClassLoader(evol1Cl, clmap, "atest1ClassLoader");
      atest2Cl = new ExClassLoader(evol1Cl, clmap, "atest2ClassLoader");

      clmap = new HashMap(clmap);
      clmap.put(tc1, hm.get(tc1));
      clmap.put(tc2, hm.get(tc2));
      rtest1Cl = new ExClassLoader(atest1Cl, clmap, "rtest1ClassLoader");
      rtest2Cl = new ExClassLoader(atest2Cl, clmap, "rtestClassLoader");
      duplicateCl = new ExClassLoader(clmap, "duplucateClassLoader");

      th.debug("done setting up ClassLoaderTest");
      return true;
    } catch (Exception e) {
      th.debug("Jar-file is Missing");
      return false;
    }

  }

  private JarFile newJarFile(String string) throws IOException {
    InputStream in = getClass().getResourceAsStream(string);
    if(in != null) {
      File out = new File("tmp.file");
      out.deleteOnExit();
      FileOutputStream fos = new FileOutputStream(out);
      byte[] bytes = new byte[1024];
      int rd = in.read(bytes);
      while(rd != -1) {
        fos.write(bytes,0,rd);
        rd = in.read(bytes);
      }
      fos.close();
      return new JarFile(out);
    }
    return null;
  }

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.lang.ClassLoader");
       if (setup()){
       	test_delegation();       	
       }else th.debug("setup failed");
       	test_badClasses();
     }


/**
*   not implemented. <br>
*
*/
  public void test_delegation(){
    th.checkPoint("delegation");
    Thread t = new Thread(new InitClass(rtest1Cl));
    ClassLoader cl = new ExClassLoader(new HashMap(), "dumbClassLoader");
    try {
   	  Class.forName("gnu.testlet.wonka.lang.ClassLoader.BasicInterface",true,cl);
    	th.fail("should throw a ClassNotFoundException ");
    }catch(ClassNotFoundException cnfe){ th.check(true); }

    try {
   	Class c = Class.forName("gnu.testlet.wonka.lang.ClassLoader.BasicInterface",true,baseCl);
    	t.start();
    	Thread.yield();	  	 	
   	c = Class.forName("gnu.testlet.wonka.lang.ClassLoader.TestClass2",false,rtest2Cl);
  	Object o = c.newInstance();
  	class2 = o;
  	System.out.println("Main class constructed: "+o);
    } catch (Exception e){
   	e.printStackTrace();
    }
    while (t.isAlive()){
    	Thread.yield();
    }
/*    try { t.join(); }
    catch (InterruptedException _){}
*/
    try {
   	  Class.forName("gnu.testlet.wonka.lang.ClassLoader.BasicInterface",true,cl);
    	th.fail("should throw a ClassNotFoundException");
    }catch(ClassNotFoundException cnfe){ th.check(true); }

    Class c = class1.getClass();
    th.check(c.getClassLoader(), rtest1Cl,"checking classLoader -- 1");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), atest1Cl,"checking classLoader -- 2");
    Class [] ca = c.getInterfaces();
    th.check(ca[0].getClassLoader(), atest1Cl,"checking classLoader -- 3");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), evol1Cl,"checking classLoader -- 4");
    ca = c.getInterfaces();
    th.check(ca[0].getClassLoader(), evol1Cl,"checking classLoader -- 5");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), baseCl,"checking classLoader -- 6");
    ca = c.getInterfaces();
    th.debug(ca[0].getName());
    th.check(ca[0].getClassLoader(), baseCl,"checking classLoader -- 7");
    c = class2.getClass();
    th.check(c.getClassLoader(), rtest2Cl,"checking classLoader -- 9");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), atest2Cl,"checking classLoader -- 10");
    ca = c.getInterfaces();
    th.check(ca[0].getClassLoader(), atest2Cl,"checking classLoader -- 11");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), evol1Cl,"checking classLoader -- 12");
    ca = c.getInterfaces();
    th.check(ca[0].getClassLoader(), evol1Cl,"checking classLoader -- 13");
    c = c.getSuperclass();	
    th.check(c.getClassLoader(), baseCl,"checking classLoader -- 14");
    ca = c.getInterfaces();
    th.check(ca[0].getClassLoader(), baseCl,"checking classLoader -- 15");
    th.checkPoint("duplicate class loading");
    try {
   	c = Class.forName("gnu.testlet.wonka.lang.ClassLoader.TestClass2",false,duplicateCl);
  	Object o = c.newInstance();
  	duplicate = o;
  	System.out.println("Main class constructed: "+o);
    } catch (Exception e){
   	e.printStackTrace();
    }
    c = class2.getClass();
    th.check(! c.isInstance(duplicate), "not the same instance");
  }


  private class InitClass implements Runnable{
   	
  	ClassLoader cl;
  	
  	public InitClass(ClassLoader cl){
  	   	this.cl = cl;
  	}
  	
  	public void run(){
  	 	try {
  	 	 	Class c = Class.forName("gnu.testlet.wonka.lang.ClassLoader.TestClass1",false,cl);
  	 	        Object o = c.newInstance();
  	 	        class1 = o;
  	 	        System.out.println("InitClass constructed: "+o);
  	 	} catch (Exception e){
  	 	 	e.printStackTrace();
  	 	}
  	}
  }
/**
*  implemented. <br>
*
*/
  public void test_badClasses(){
    th.checkPoint("defineClass(java.lang.String,byte[],int,int)java.lang.Class");
    BadClassLoader cl = new BadClassLoader();
    try {
     	Class c = cl.findClass("A");
      c.newInstance();
     	th.fail("should throw ClassCircularityError "+c);
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t.getClass(), ClassCircularityError.class);
    }
 
    // not everyone should be allowed to
    try {
     	Class c = cl.findClass("String");
     	String s =(String) c.newInstance();
     	th.fail("should throw an Error "+s);	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check((t instanceof ClassCastException) || (t instanceof SecurityException), "checking Throwable type");
    }
    try {
     	cl.findClass("java.lang.Bad");      
      th.fail("Bad class");
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check((t instanceof Error) || (t instanceof SecurityException));
    }
    try {
     	Class c = cl.findClass("Old");
     	c.newInstance();
    }catch (Throwable t){
     th.fail("should be allowed, but got "+t);   
    }
    try {
     	Class c = cl.findClass("Mis");
     	c.newInstance();
     	th.fail("should throw a NoClassDefFoundError");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t.getClass(), NoClassDefFoundError.class, "Mis: "+t);
    }
    try {
     	Class c = cl.findClass("MisClass");
     	c.newInstance();
     	th.fail("should throw a NoClassDefFoundError");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t.getClass(), NoClassDefFoundError.class, "MisClass: "+t);
    }
    try {
     	cl.findClass("BadFormat1");
     	th.fail("should throw a ClassFormatError");	
    }catch (Throwable t){
        t.printStackTrace();
     	th.check(t.getClass(), ClassFormatError.class);
    }
    try {
     	cl.findClass("BadFormat2");
     	th.fail("should throw a ClassFormatError");	
    }catch (Throwable t){
        t.printStackTrace();
     	th.check(t.getClass(), ClassFormatError.class);
    }
    try {
     	Class c = cl.findClass("BadFormat3");
     	c.newInstance();
     	th.fail("should throw a VerifyError");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t.getClass(), VerifyError.class);
    }
    try {
     	cl.findClass("CreateByteArray");
     	th.fail("should throw a ClassFormatError");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t.getClass(), ClassFormatError.class);
    }
    try {
     	cl.findClass("wrongName");//, true, cl);
     	th.fail("should throw an Error");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t instanceof Error);
    }
    try {
     	Class c = cl.findClass("Acces.AcMethod");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an IncompatibleClassChangeError - 1");	
    }catch (Throwable t){
      //t.printStackTrace();
     	th.check(t instanceof IllegalAccessError);
    }
    try {
     	Class c = cl.findClass("Acces");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an IncompatibleClassChangeError - 2");	
    }catch (Throwable t){
      //t.printStackTrace();
     	th.check(t instanceof IllegalAccessError);
    }
    try {
     	Class c = cl.findClass("NoSuchField");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an IncompatibleClassChangeError - 3");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t instanceof NoSuchFieldError, 
          "need a NoSuchFieldError, but got "+t);
    }
    try {
     	Class c = cl.findClass("NoSuchMethod");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an IncompatibleClassChangeError - 4");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t instanceof NoSuchMethodError, 
          "need a NoSuchMethodError, but got "+t);
    }
    try {
     	Class c = cl.findClass("Instantiate");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an IncompatibleClassChangeError - 5");	
    }catch (Throwable t){
      //t.printStackTrace();
     	th.check(t instanceof InstantiationError);
    }
    try {
     	Class c = cl.findClass("Initializer");//, true, cl);
     	c.newInstance();
     	th.fail("should throw an ExceptionInInitializerError");	
    }catch (Throwable t){
        //t.printStackTrace();
     	th.check(t instanceof ExceptionInInitializerError);
    }

  }

}
