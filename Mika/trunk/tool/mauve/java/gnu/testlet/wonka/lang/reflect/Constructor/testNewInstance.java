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


/*
* $Id: testNewInstance.java,v 1.2 2005/11/07 15:45:25 cvs Exp $
*/

package gnu.testlet.wonka.lang.reflect.Constructor;

import java.lang.reflect.*;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class testNewInstance implements Testlet
{

  private static final String testPackageName = "gnu.testlet.wonka.lang.reflect.Constructor";
  private static final String testClassName = "NewInstanceClass";

  private String fullClassName = testPackageName + "." + testClassName;;

  protected static TestHarness harness;

	private void testConstructorArray()
	{
	  //  Class forName and constructor array
	
	  try
	  {
	    harness.checkPoint("Class forName");
      Class c = Class.forName(fullClassName);
      if( c==null )
        harness.fail("Class.forName("+testClassName+") returned null");
      else
        harness.check(c.getName(), testPackageName + "." + testClassName);

	    harness.checkPoint("Class.getConstructors");
      Constructor[] constructorarray = c.getConstructors();
      if( constructorarray==null )
        harness.fail("Class "+testClassName+"getConstructors returned null");
      else
      {
        harness.check(constructorarray.length, 5);
        for(int i=0; i< constructorarray.length; i++)
        {
          harness.verbose(" Constructor <"+constructorarray[i].getName()+"> value : " + constructorarray[i].toString());
          harness.check(constructorarray[i].getName(),fullClassName);
        }
      }
	
	    harness.checkPoint("Class.getDeclaredConstructors");
      Constructor[] declaredconstructorarray = c.getDeclaredConstructors();
      if( constructorarray==null )
        harness.fail("Class "+testClassName+"getConstructors returned null");
      else
      {
        harness.check(declaredconstructorarray.length, 6);
        for(int i=0; i< declaredconstructorarray.length; i++)
        {
          harness.verbose(" Constructor <"+declaredconstructorarray[i].getName()+"> value : " + declaredconstructorarray[i].toString());
          harness.check(declaredconstructorarray[i].getName(),fullClassName);
        }
      }

	  }
	  catch (Exception e)
	  {
	    harness.fail(e.toString());
	  }
	
	}

	private void testConstructorInstances()
	{
    Class[]  params;
    Object[] values ;

    harness.checkPoint("Testing constructor instances : default testclass(null)");
    //harness.debug("Let's do some naughty stuff...");
    checkNewInstance(null, null, fullClassName + "()");

    params = new Class[0];
    values = new Object[0];
    harness.checkPoint("Testing constructor instances : default testclass()");
    checkNewInstance(params, values, fullClassName + "()");

    params = new Class[1];
    values = new Object[1];
    harness.checkPoint("Testing constructor instances : default testclass(String)");
    params[0] = String.class;
    values[0] = "abcde";
    checkNewInstance(params, values, fullClassName + "(Ljava.lang.String; abcde)");

    harness.checkPoint("Testing constructor instances : default testclass(int)");
    params[0] = Integer.TYPE;
    values[0] = new Integer(123);
    checkNewInstance(params, values, fullClassName + "(I 123)");

    harness.checkPoint("Testing constructor instances : default testclass(Integer)");
    params[0] = Integer.class;
    values[0] = new Integer(123);
    checkNewInstance(params, values, fullClassName + "(Ljava.lang.Integer; 123)");

    params=new Class[2];
    values=new Object[2];
    harness.checkPoint("Testing constructor instances : default testclass(String, String)");
    params[0] =String.class;
    params[1] =String.class;
    values[0] = "abcde";
    values[1] = "ABCDE";
    checkNewInstance(params, values, fullClassName+"(Ljava.lang.String;Ljava.lang.String; abcde, ABCDE)");
	}

  /*
   * Private helper utility class.
   * 1. Look for the constructor matching arguments of types specified by 'params'
   * 2. then invoke a new instance by calling this constructor with the values given by 'values'
   * 3. finally, print this object and see if the result match the 'expected' string.
   */
  private void checkNewInstance(Class params[], Object values[], String expected)
  {
    try
    {
      // Retrieve class
      Class cl = Class.forName(testPackageName + "." + testClassName);
      	
      // Retrieve constructor
      Constructor c = cl.getConstructor(params);
      // Create a new instance with it
      Object o = c.newInstance(values);
        	
      // Test if ok
      harness.check( o.toString(),expected);

    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }

  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.lang.reflect");
//    fullClassName; = testPackageName + "." + testClassName;
		testConstructorArray();
		testConstructorInstances();
		
	}
}
