// Tags: JDK1.1
// Uses: rf_help

// Copyright (C) 2000 Cygnus Solutions

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

package gnu.testlet.wonka.lang.Class;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.*;

public class reflect implements Testlet
{
  public int do_nothing (int arg)
  {
    return ++arg;
  }

  public Class getClass (String name)
  {
    // gcj can't handle `.class' notation yet.
    Class k = null;
    try
      {
	k = Class.forName (name);
      }
    catch (Throwable _)
      {
	// Nothing.
      }
    return k;
  }

  public Object getCons (Class k, Class[] types, boolean decl)
  {
    try
      {
	return (decl
		? k.getDeclaredConstructor (types)
		: k.getConstructor (types));
      }
    catch (Throwable _)
      {
	return _;
      }
  }

  public Object getMethod (Class k, String name, Class[] types, boolean decl)
  {
    try
      {
	return (decl
		? k.getDeclaredMethod (name, types)
		: k.getMethod (name, types));
      }
    catch (Throwable _)
      {
	return _;
      }
  }

  public Object getField (Class k, String name, boolean decl)
  {
    try
      {
	return (decl
		? k.getDeclaredField (name)
		: k.getField (name));
      }
    catch (Throwable _)
      {
	return _;
      }
  }

  public void test (TestHarness harness)
  {
    Class reflect_class = getClass ("gnu.testlet.wonka.lang.Class.reflect");
    Class rf_help_class = getClass ("gnu.testlet.wonka.lang.Class.rf_help");
    Class i_class = Integer.TYPE;

    Class[] ptz = new Class[0];
    Class[] pt1 = new Class[1];
    pt1[0] = Integer.TYPE;

    harness.checkPoint ("getConstructor");
    // This class doesn't have an explicit constructor, so we make
    // sure that the implicit one can be found.
    Object cons = getCons (reflect_class, ptz, false);
    harness.check(cons instanceof Constructor);

    cons = getCons (reflect_class, pt1, false);
    harness.check(cons instanceof NoSuchMethodException);

    cons = getCons (rf_help_class, ptz, false);
    harness.check(cons instanceof NoSuchMethodException);

    cons = getCons (i_class, ptz, false);
    harness.check(cons instanceof NoSuchMethodException);

/*    harness.checkPoint("getConstructors");
    try
      {
	Constructor[] cls = reflect_class.getConstructors();
	harness.check(cls.length, 1);
	harness.check(cls[0].getName(), "gnu.testlet.wonka.lang.Class.reflect");
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
	harness.check(false);
      }

    try
      {
	Constructor[] cls = rf_help_class.getConstructors();
	harness.check(cls.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }
    try
      {
	Constructor[] cls = i_class.getConstructors();
	harness.check(cls.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }
*/
    harness.checkPoint ("getDeclaredConstructor");
    cons = getCons (rf_help_class, ptz, true);
    harness.check(cons instanceof Constructor);
    cons = getCons (rf_help_class, pt1, true);
    harness.check(cons instanceof NoSuchMethodException);
    cons = getCons (i_class, ptz, true);
    harness.check(cons instanceof NoSuchMethodException);

/*    harness.checkPoint("getDeclaredConstructors");
    try
      {
	Constructor[] cls = reflect_class.getDeclaredConstructors();
	harness.check(cls.length, 1);
	harness.check(cls[0].getName(), "gnu.testlet.wonka.lang.Class.reflect");
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
	harness.check(false);
      }

    try
      {
	Constructor[] cls = rf_help_class.getDeclaredConstructors();
	harness.check(cls.length, 1);
	harness.check(cls[0].getName(), "gnu.testlet.wonka.lang.Class.rf_help");
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
	harness.check(false);
      }
    try
      {
	Constructor[] cls = i_class.getDeclaredConstructors();
	harness.check(cls.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }
*/
    harness.checkPoint ("getDeclaredField");
    Object f = getField (rf_help_class, "size", true);
    harness.check(f instanceof Field);
    harness.check(((Field) f).getModifiers(), Modifier.PRIVATE);
    f = getField (rf_help_class, "value", true);
    harness.check(f instanceof Field);
    harness.check(((Field) f).getModifiers(),Modifier.STATIC);	
    harness.checkPoint("getField");
    f = getField (rf_help_class, "size", false);
    harness.check(f instanceof NoSuchFieldException);
    f = getField (rf_help_class, "name", false);
    harness.check(f instanceof Field);
    harness.check(((Field) f).getModifiers(), Modifier.PUBLIC);

    harness.checkPoint("getDeclaredFields");
    try
      {
	Field[] flds = rf_help_class.getDeclaredFields();
	harness.check(flds.length, 3);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }

    harness.checkPoint("getFields");
    try
      {
	Field[] flds = rf_help_class.getFields();
	harness.check(flds.length, 1);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }

    try
      {
	Field[] flds = i_class.getFields();
	harness.check(flds.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check(false);
      }

    harness.checkPoint("getMethod");
    Object m = getMethod (rf_help_class, "doit", ptz, false);
    harness.check (m instanceof NoSuchMethodException);
    m = getMethod (reflect_class, "do_nothing", pt1, false);
    harness.check (m instanceof Method);
    harness.check (((Method) m).getName (), "do_nothing");
    // FIXME: replace `==' with `,' and libgcj will segv.
    harness.check (((Method) m).getDeclaringClass () == reflect_class);
    // See if we can get something that is inherited.
    m = getMethod (rf_help_class, "hashCode", ptz, false);
    harness.check (m instanceof Method);

    harness.checkPoint("getMethods");
    try
      {
	Method[] ms = rf_help_class.getMethods();
	// There are 9 public methods in Object.
	harness.check (ms.length, 9);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }

    try
      {
	Method[] ms = reflect_class.getMethods();
	int expected =
	  (6 /* from reflect.java; note that Testlet's method is also
		declared here and should only be counted once.  */
	   + 9 /* from Object */
	   );
	harness.check (ms.length, expected);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }

    try
      {
	Method[] ms = i_class.getMethods();
	harness.check (ms.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }

    harness.checkPoint("getDeclaredMethod");
    m = getMethod (rf_help_class, "doit", ptz, true);
    harness.check (m instanceof Method);
    harness.check (((Method) m).getName (), "doit");
    // Make sure we can't fetch a constructor this way.
    m = getMethod (rf_help_class, "rf_help", ptz, true);
    harness.check (m instanceof NoSuchMethodException);
    m = getMethod (reflect_class, "do_nothing", pt1, false);
    harness.check (m instanceof Method);
    // See if we can get something that is inherited.
    m = getMethod (rf_help_class, "hashCode", ptz, true);
    harness.check (m instanceof NoSuchMethodException);

    harness.checkPoint("getDeclaredMethods");
    try
      {
	Method[] ms = rf_help_class.getDeclaredMethods();
	harness.check (ms.length, 1);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }

    try
      {
	Method[] ms = reflect_class.getDeclaredMethods();
	harness.check (ms.length, 6);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }

    try
      {
	Method[] ms = i_class.getDeclaredMethods();
	harness.check (ms.length, 0);
      }
    catch (SecurityException se)
      {
	// One per check above.
	harness.check (false);
      }
    try {
    	harness.checkPoint("getDeclaringClass");
    	// None of these classes has a declaring class.
    	harness.check(rf_help_class.getDeclaringClass(), null);
    	harness.check(reflect_class.getDeclaringClass(), null);
    	harness.check(i_class.getDeclaringClass(), null);
    	}
   catch (Exception e) { harness.fail("Method getDeclaringClass is not Supported");}
  }
}
