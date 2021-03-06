// Tags: JDK1.1
//  
// Copyright (C) 2004, Free Software Foundation, Inc.
// Contributed by Mark J. Wielaard (mark@klomp.org)
//   
// This file is part of Mauve.
//    
// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//     
// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//      
// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.

package gnu.testlet.wonka.lang.Class;

import gnu.testlet.*;
import java.lang.reflect.*;

// This tests VM Spec 2.17.4
// As discussed at http://gcc.gnu.org/ml/java-patches/2004-q2/msg00443.html 
public class init implements Testlet
{
  static boolean alreadyran = false;
  static boolean initI = false;
  static boolean initC1 = false;
  static boolean initC2 = false;
  static boolean initC3 = false;
  static boolean initC4 = false;
  static boolean initC5 = false;
  static boolean initC6 = false;
  static boolean initC7 = false;
  static boolean initC8 = false;
  static boolean invokedM = false;

  interface I
  {
    static long l = init.initI();
    void m();
  }

  static class C1 implements I
  {
    static long l = init.initC1();
    public void m()
    {
      invokedM = true;
    }
  }

  static class C2 implements I
  {
    static long l = init.initC2();
    public void m() { }
  }

  static class C3 extends C2
  {
    static long l = init.initC3();
  }

  static class C4 extends C2
  {
    static long l = init.initC4();
    static boolean m2() {
      return true;
    }
  }

  static class C5 extends C4
  {
    static long l = init.initC5();
    public static int i;
  }

  static class C6 extends C4
  {
    static long l = init.initC6();
    public static int i6;
  }
 
  static class C7 extends C4
  {
    static long l = init.initC7();
    public static int i7;
  }

  static class C8 extends C4
  {
    static long l = init.initC8();
    public static int i7;
  }

  public void test(TestHarness h)
  {
    // These tests only make sense the first time they are run
    if (alreadyran) {
      return;
    }

    alreadyran = true;
    try
      {
	// None of this should initialize anything
	Class i = new I[0].getClass().getComponentType();
	Method m = i.getDeclaredMethod("m", null);
	Class cf = Class.forName(getClass().getName() + "$C5",
	  false, getClass().getClassLoader());
  h.check(!initC2, "C2 not init");

  Field f =cf.getField("i");

  h.check(!initI, "I not init");

	// Static field access should initialize C3 and superclass C2 but not I
	h.check(!initC2, "C2 not init");
	h.check(!initC3, "C3 not init");
	if (C3.l == 123)
	    hashCode();
	h.check(initC2,"C2 init");
	h.check(initC3, "C3 init");

	// Static method invocation should initialize C4 but not I
	h.check(!initC4,"C4 not init");
	if (C4.m2())
		hashCode();
	h.check(initC4, "C4 init");

	// Static field access should initialize C5
	h.check(!initC5);
	f.set(null, new Character((char)0xffff));
	h.check(C5.i == 0xffff);
	h.check(initC5);

	// Instantiation of a C should initialize C but not I
	h.check(!initC1);
	Object o = new C1();
	h.check(initC1);

	// Apparently, invocation of interface method initializes I
	h.check(!initI, "I not init");
	h.check(!invokedM, "m not invoked");
	m.invoke(o, null);
	h.check(initI, "I init");
	h.check(invokedM, "m invoked");
  
  
  cf = Class.forName(getClass().getName() + "$C6",
      false, getClass().getClassLoader());
  h.check(!initC6, "C6 not init");
  f = cf.getField("i6");
  h.check(!initC6, "C6 not init");
  f.setInt(null, 3);
  h.check(initC6, "C6 init");
  
  cf = Class.forName(getClass().getName() + "$C7",
      false, getClass().getClassLoader());
  h.check(!initC7, "C7 not init");
  f = cf.getField("i7");
  h.check(!initC7, "C7 not init");
  f.getInt(null);
  h.check(initC7, "C7 init");
  cf = Class.forName(getClass().getName() + "$C8",
      false, getClass().getClassLoader());
  h.check(!initC8, "C8 not init");
  cf.newInstance();
  h.check(initC8, "C8 init");
      }
    catch (NoSuchMethodException nsme)
      {
	h.debug(nsme);
	h.check(false);
      }
    catch (NoSuchFieldException e)
      {
	h.debug(e);
	h.check(false);
      }
    catch (InvocationTargetException ite)
      {
	h.debug(ite);
	h.check(false);
      }
    catch (IllegalAccessException iae)
      {
	h.debug(iae);
	h.check(false);
      }
    catch (ClassNotFoundException e)
      {
	h.debug(e);
	h.check(false);
      } 
    catch (InstantiationException e) {
      h.debug(e);
      h.check(false);
    }
  }

  static long initI()
  {
    initI = true;
    return 5;
  }

  static long initC1()
  {
    initC1 = true;
    return 5;
  }

  static long initC2()
  {
    initC2 = true;
    return 5;
  }

  static long initC3()
  {
    initC3 = true;
    return 5;
  }

  static long initC4()
  {
    initC4 = true;
    return 5;
  }

  static long initC5()
  {
    initC5 = true;
    return 5;
  }
  static long initC6()
  {
    initC6 = true;
    return 5;
  }

  static long initC7()
  {
    initC7 = true;
    return 5;
  }
  static long initC8()
  {
    initC8 = true;
    return 5;
  }
}
