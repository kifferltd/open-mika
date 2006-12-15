// Tags: JDK1.0

// Copyright (C) 1998 Cygnus Solutions

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

package gnu.testlet.wonka.lang.Boolean;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;

public class BooleanTest implements Testlet
{
  TestHarness harness;

/**
* tests the Boolean constructors Boolean(boolean) and Boolean(String), also checks the initialisation of the types
* by calling on Boolean.equals and Boolean.booleanValue();
* (By doing so, also 'introduces' the static instances Boolean.TRUE and Boolean.FALSE.)
*/
  void testConstructors()
  {
      //constructor and boolean values
      harness.checkPoint("Boolean(boolean)");
      Boolean a = new Boolean(true);
      Boolean b = new Boolean(false);
      Boolean c = new Boolean(true);
      boolean b_true = true;
      boolean b_false = false;
      Boolean d = new Boolean(b_true);
      Boolean e = new Boolean(b_false);
      Boolean f = a;

      harness.checkPoint("equals(java.lang.Object)boolean");
      harness.check( a != null);
      harness.check(!a.equals(null));
      harness.check( a.equals(Boolean.TRUE));
      harness.check( a.equals(new Boolean(true) ));
      harness.check( a != Boolean.TRUE);
      harness.check( a.booleanValue());
      harness.check( b.equals(Boolean.FALSE));
      harness.check( b.equals(new Boolean(false) ));
      harness.check( b != Boolean.FALSE);
      harness.check(!b.booleanValue());

      harness.check( a != b     );
      harness.check(!a.equals(b));
      harness.check( a != c     );
      harness.check( a.equals(c));
      harness.check( a == f     );
      harness.check( a.equals(f));
      harness.check( a == a     );
      harness.checkPoint("booleanValue()boolean");
      harness.check(a.booleanValue() == true);
      harness.check(d.booleanValue() == true);
      harness.check(d.booleanValue() == b_true);
      harness.check(a.booleanValue() == d.booleanValue());
      harness.check(b.booleanValue() == false);
      harness.check(e.booleanValue() == false);
      harness.check(e.booleanValue() == b_false);
      harness.check(b.booleanValue() == e.booleanValue());



      Integer i = new Integer(123);
      harness.check (! a.equals(i));
      harness.check (! b.equals(i));

      harness.checkPoint("Boolean(java.lang.String)");
      Boolean stringbool;
      stringbool = new Boolean("true");
      harness.check (stringbool.equals(Boolean.TRUE));
      stringbool = new Boolean("false");
      harness.check (stringbool.equals(Boolean.FALSE));
      stringbool = new Boolean("TRUE");
      harness.check (stringbool.equals(Boolean.TRUE));
      stringbool = new Boolean("tRuE");
      harness.check (stringbool.equals(Boolean.TRUE));
      stringbool = new Boolean("foo");
      harness.check (stringbool.equals(Boolean.FALSE));
      stringbool = new Boolean("");
      harness.check (stringbool.equals(Boolean.FALSE));
      stringbool = new Boolean(null);
      harness.check (stringbool.equals(Boolean.FALSE));
  }

/**
* tests the Boolean-to-String and String-to-Boolean functions toString(Boolean), valueOf(String) and getBoolean(String)
*/
  void testStringConversion()
  {
      Boolean stringbool;

      harness.checkPoint("valueOf(java.lang.String)java.lang.Boolean");
      stringbool = Boolean.valueOf("true");
      harness.check( stringbool.booleanValue() );
      stringbool = Boolean.valueOf("TRUE");
      harness.check( stringbool.booleanValue() );
      stringbool = Boolean.valueOf("TruE");
      harness.check( stringbool.booleanValue() );

      stringbool = Boolean.valueOf("false");
      harness.check(!stringbool.booleanValue() );
      stringbool = Boolean.valueOf("trou");
      harness.check(!stringbool.booleanValue() );
      stringbool = Boolean.valueOf("  true  ");
      harness.check(!stringbool.booleanValue() );
      stringbool = Boolean.valueOf("");
      harness.check(!stringbool.booleanValue() );
      stringbool = Boolean.valueOf(null);
      harness.check(!stringbool.booleanValue() );

      harness.check((Boolean.valueOf("true")).booleanValue() );
/*
      harness.checkPoint("boolean Boolean.getBoolean(String)");
      harness.check( Boolean.getBoolean("true")== true, "GetBoolean(<true>)");
      harness.check( Boolean.getBoolean("TRUE")== true, "GetBoolean(<TRUE>)");
      harness.check( Boolean.getBoolean("trUE")== true, "GetBoolean(<trUE>)");
 //     harness.check(!Boolean.getBoolean("false"));
      harness.check(!Boolean.getBoolean("trou"));
      harness.check(!Boolean.getBoolean("  true  "));
      harness.check(!Boolean.getBoolean(""));
      //harness.check(!Boolean.getBoolean(null));
*/
      harness.checkPoint("toString()java.lang.String");
      Boolean a = new Boolean(true);
      Boolean b = new Boolean(false);
      harness.check(a.toString(), "true");
      harness.check(b.toString(), "false");


  }

/**
* tests the properties put() method
*/
  void testProperties()
  {
      // Augment the System properties with the following.
      // Overwriting is bad because println needs the
      // platform-dependent line.separator property.
      harness.checkPoint("getBoolean(java.lang.String)boolean");
      Properties p = System.getProperties();
      p.put("e1", "true");
      p.put("e2", "false");

      harness.check (Boolean.getBoolean("e1"));
      harness.check (! Boolean.getBoolean("e2"));
      harness.check (! Boolean.getBoolean("e3"));
  }

/**
* tests the Boolean object overwrites hashCode()
*/
  void testHashCode()
  {
    Boolean a = new Boolean("true");
    Boolean b = new Boolean("false");
    Boolean c = new Boolean("true");
    Boolean d = new Boolean("false");

    harness.checkPoint("hashCode()int");
    harness.check (a.hashCode(), c.hashCode());
    harness.check (b.hashCode(), d.hashCode());
    harness.check (a.hashCode() != b.hashCode());
    harness.check (a.hashCode(), 1231);
    harness.check (b.hashCode(), 1237);
  }

/**
* tests the Boolean object overwrites getClass()
*/
  void testGetClass()
  {
    Boolean a = new Boolean("true");
    Boolean b = new Boolean("false");
    Integer i = new Integer(0);

    harness.checkPoint("TYPE(public)java.lang.Class");
/** NOTE: it seems that sun SDK returns primitive type <boolean> instead of wrapper <Boolean> for Boolean.getClass() */
    try
    {
      harness.check (a instanceof Boolean );
      harness.check (b instanceof Boolean );
      harness.check (a.getClass().getName(), "java.lang.Boolean");
      harness.check (b.getClass().getName(), "java.lang.Boolean");
      harness.check (a.getClass(), Class.forName("java.lang.Boolean") );
      harness.check (b.getClass(), Class.forName("java.lang.Boolean") );
      harness.check (i.getClass() != Class.forName("java.lang.Boolean") );
      harness.check (a.getClass(), b.getClass());
      harness.check (a.getClass() != i.getClass());
      harness.check ((Boolean.TYPE).getName(), "boolean");
//      harness.check ( Boolean.TYPE, Class.forName("boolean"));
    }
    catch (ClassNotFoundException e)
    {
      harness.fail("error finding class name");
      harness.debug(e);
    }
  }

/**
* calls the tests described
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang.Boolean");
		testConstructors();
		testStringConversion();
	 	testProperties();
	 	testHashCode();
	 	testGetClass();
	}
}
