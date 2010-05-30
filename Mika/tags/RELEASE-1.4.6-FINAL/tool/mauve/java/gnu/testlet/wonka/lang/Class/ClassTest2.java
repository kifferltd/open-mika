/* Copyright (C) 1999, 2000 Hewlett-Packard Company

   This file is part of Mauve.

   Mauve is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   Mauve is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Mauve; see the file COPYING.  If not, write to
   the Free Software Foundation, 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/

// Tags: JLS1.0



// Edited by Smartmove on Fri Aug 25
// new Tag:JLS1.2

package gnu.testlet.wonka.lang.Class;
// 

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*;
import java.net.*;

public class ClassTest2	 implements Cloneable, java.io.Serializable, Testlet
{
  protected static TestHarness harness;

	public void test_toString()
	{
		if ( !getClass().toString().equals(getClass().isInterface()? 	"interface " : "class " +getClass().getName()))
		{
			harness.fail("Error: toString returned wrong string");
		}
		else { harness.check(true);}
	

		if ( !(new Object()).getClass().toString().equals("class java.lang.Object"))
		{
			harness.fail("Error: toString returned wrong string");
		}
		else { harness.check(true);}
// adding extra code
	
		Class ci = Integer.TYPE;	
		harness.check( ci.toString().equals("int"));
		harness.check( ci.toString().equals(ci.getName())); 

		



	}

	public void test_getName()
	{
	   try 	{ 
		if ( ! (new java.util.Vector()).getClass().getName().equals("java.util.Vector"))
			harness.fail("Error: getName returned wrong string - 1");
		else { harness.check(true);}

		if ( ! (new Object[3]).getClass().getName().equals("[Ljava.lang.Object;"))
			harness.fail("Error: getName returned wrong string - 2");
		else { harness.check(true);}

		if ( ! ( new int[6][7][8]).getClass().getName().equals("[[[I"))
			harness.fail("Error: getName returned wrong string - 3");
		else { harness.check(true);}

//adding code	
		Class ci = Float.TYPE;	
		harness.check(ci.getName().equals("float"));
		}
		catch ( Error e ) 	{ harness.fail("Error: getName failed  - 4"); }

	}

	public void test_isInterface()
	{
		if ( (new Object()).getClass().isInterface())
			harness.fail("Error: isInterface returned wrong result - 1");
		else { harness.check(true);}

		if ( getClass().isInterface())
			harness.fail("Error: isInterface returned wrong result - 2");
		else { harness.check(true);}

		try 	{
			if ( !Class.forName("java.lang.Cloneable").isInterface())
			harness.fail("Error: isInterface returned wrong result - 3");
			else { harness.check(true);}
			}
		catch ( Exception e ){}
	}

	public void test_getSuperclass()
	{
		try 	{
			if(  (new Boolean(true)).getClass().getSuperclass() != Class.forName("java.lang.Object"))
				harness.fail("Error: getSuperclass returned wrong values - 1");
			else { harness.check(true);}
			}
		catch ( Exception e ){}

		if ( (new Object()).getClass().getSuperclass() != null )
			harness.fail("Error: getSuperclass returned wrong values - 2");
		else { harness.check(true);}

		try 	{	
		   	Class clss = Class.forName("[[I");
		   	if ( clss.getSuperclass() != Class.forName("java.lang.Object"))
		   	harness.fail(" Error : getSuperclass  " +" failed - 3 " );
			else { harness.check(true);}


			}
		catch ( Exception e ){ harness.fail(" Error: getSuperclass failed - 4");
		}

		try 	{	
		   	Class clss = Class.forName("[D");
		   	if ( clss.getSuperclass() != Class.forName("java.lang.Object"))
		   		harness.fail(" Error : getSuperclass  " +" failed - 5 " );
			else { harness.check(true); }
			harness.check(true);

			}
		catch ( Exception e ){  harness.fail(" Error: getSuperclass failed - 6");
		}
// adding code
			Class ci = Integer.TYPE;	
			harness.check( (ci.getSuperclass()) == null, "integer class");
			ci = Void.TYPE;
			harness.check( (ci.getSuperclass()) == null, "void class");
							
		try 	{
			ci = getClass();
			harness.check((ci.getSuperclass()) == Class.forName("java.lang.Object"));
		   	harness.check((Class.forName("java.lang.Cloneable").getSuperclass())== null);
			}
		catch ( Exception e ){harness.fail("Debuggin ERROR");}


	}

	public void test_getInterfaces()
	{
		Class clss[] = getClass().getInterfaces();

		Class clclass = null,clclass1 = null;
		try 	{
			clclass = Class.forName("java.lang.Cloneable");
			clclass1 = Class.forName("java.io.Serializable");
			}
		catch ( Exception e ){}

		if ( clss == null )
			harness.fail("Error: getInterfaces returned wrong values - 1");
		else
		{	harness.check(true);
			if ( clss.length != 3 )
			harness.fail("Error: getInterfaces returned wrong values - 2");
			else
		  	{	harness.check(true);
				if (!( clss[0] == clclass  && clss[1] == clclass1))
		      		{
					harness.fail("Error: getInterfaces returned wrong values - 3");
					for (int i = 0; i < clss.length; i++)
			  		{
			    			harness.debug ("" + clss[i], false);
			   	 		harness.debug (" ", false);
			  		}
					harness.debug ("");
		      		}
				else { harness.check(true);}
			}
		}
	
		try 	{	
		   	Class clsss = Class.forName("[[I");
		   	harness.check ( clsss.getInterfaces().length,  2 );
			}
		catch ( Exception e ){ harness.fail(" Error: getInterfaces failed - 5");}

		try 	{	
		   	Class clsss = Class.forName("[D");
		   	harness.check ( clsss.getInterfaces().length, 2 );
			}
		catch ( Exception e ){ harness.fail(" Error: getInterfaces failed - 7");}
// adding code
		harness.check(clclass.getInterfaces().length == 0);
		try 	{
			clclass = Class.forName("java.lang.Object");
			harness.check(clclass.getInterfaces().length == 0);


			}
		catch ( Exception e ){}


	}

	public void test_newInstance()
	{
		Class clss = getClass();
		Object obj;

		try 	{
			obj = clss.newInstance();
			obj = clss.newInstance();
			obj = clss.newInstance();
			obj = clss.newInstance();
			harness.check(true);

			}
		catch ( Exception e ){harness.fail("Error: newInstance failed ");}
		catch ( Error e ){harness.fail("Error: newInstance failed "+" with out of memory error " );}
// adding code
		try 	{ 
			Class.class.newInstance();
			harness.fail("should throw an IllegalAccessException");
			}
		catch   (IllegalAccessException iae){ harness.check(true);}
		catch	(Exception e) { harness.fail("should throw an IllegalAccessException, but got"+e);}
		try 	{ 
			Number.class.newInstance();
			harness.fail("should throw an InstatiationException");
			}
		catch   (InstantiationException ie){ harness.check(true);}
		catch	(Exception e) { harness.fail("should throw an InstatiationException, but got"+e);}
	}


	public void test_forName()
	{
		harness.checkPoint("forName");
		try 	{
			Object obj = Class.forName("java.lang.Object");
			harness.check ( obj != null );
			}
		catch ( Exception e ){harness.check(false);}

		try 	{
			Object obj1 = Class.forName("ab.cd.ef");
			harness.check(false);
			}
		catch ( ClassNotFoundException e ){harness.check(true);}

		try 	{	// The docs say that this should fail.
			Object obj2 = Class.forName("I");
			harness.check(false);
			}
		catch ( ClassNotFoundException e ){harness.check(true);	}		
	}

        public void test_getClassloader()
	{
		try 	{
                    	Class obj1 = Class.forName("java.lang.String");
                    	ClassLoader ldr = obj1.getClassLoader();
                    	if ( ldr != null )
				harness.fail("Error: test_getClassLoader failed - 1");
                    	else { harness.check(true);}
			Class obj2 = Class.forName("gnu.testlet.wonka.lang.Class.ClassTest");
                    		ClassLoader ldr1 = obj2.getClassLoader();
					
                    	if ( ldr1 == null )
				harness.fail("Error: test_getClassLoader failed - 2");
	               	else { harness.check(true);}
			}
                catch ( Exception e ){	harness.fail("Error: test_getClassLoader failed -3");}	
	}

        public void test_ComponentType()
	{
	        try 	{
                    	Class obj1 = Class.forName("java.lang.String");
                    	if ( obj1.getComponentType() != null )
				harness.fail("Error: test_getComponentType failed - 1");
                	else { harness.check(true);}
    			Class obj2 = Class.forName("java.lang.Exception");
                    	if ( obj2.getComponentType() != null )
				harness.fail("Error: test_getComponentType failed - 2");
			else { harness.check(true);}
    			Class arrclass = Class.forName("[I");
                    	if ( arrclass.getComponentType() == null )
				harness.fail("Error: test_getComponentType failed - 3");
			else 	{ 
				harness.check(true);
				harness.check(arrclass.getComponentType().getName().equals("int"));
				}
    			arrclass = Class.forName("[[[[I");
                    	if ( arrclass.getComponentType() == null )
				harness.fail("Error: test_getComponentType failed - 4");
			else { harness.check(true);}

			}
                catch ( Exception e ){	harness.fail("Error: test_getComponentType failed - 6");}	
		
	}

        public void test_isMethods()
        {
	        try 	{
                    	Class obj1 = Class.forName("java.lang.String");
                    	if ( !obj1.isInstance("babu"))
				harness.fail("Error: test_isMethods failed - 1");
			else { harness.check(true);}

	                Class obj2 = Class.forName("java.lang.Integer");
        	        if ( !obj2.isInstance(new Integer(10)))
				harness.fail("Error: test_isMethods failed - 2");
			else { harness.check(true);}

	                int arr[]= new int[3];
		    	Class arrclass = Class.forName("[I");
                    	if ( !arrclass.isInstance(arr))
				harness.fail("Error: test_isMethods failed - 3");
			else 	{ harness.check(true);}

                    	Class cls1 = Class.forName("java.lang.String");
                    	Class supercls = Class.forName("java.lang.Object"); 
                    	if ( !supercls.isAssignableFrom( cls1 ))
				harness.fail("Error: test_isMethods failed - 4");
			else { harness.check(true);}

        	        if ( cls1.isAssignableFrom( supercls ))
				harness.fail("Error: test_isMethods failed - 5");
			else { harness.check(true);}
		    
	                 Class cls2 = Class.forName("java.lang.String");
        	         if ( !cls2.isAssignableFrom( cls1 ))
				harness.fail("Error: test_isMethods failed - 6");
			else { harness.check(true);}

                    
			arrclass = Class.forName("[I");
		    	Class arrclass1 = Class.forName("[[[I");
		    	Class arrclass2 = Class.forName("[[D");
			
			harness.check(!(cls1.isArray()));
				    
                    	if ( arrclass.isArray() &&arrclass1.isArray() && arrclass2.isArray() )
                   		{harness.check(true);}
                   	else
				harness.fail("Error : test_isMethods failed - 7" );    
			}
                catch ( Exception e ){	harness.fail("Error: test_isMethods failed - 6");}	
// adding code
		Class ci = Integer.TYPE;
		Class cit = Integer.TYPE;
		int i=5;
		ci = cit;
		harness.check(!(ci.isInstance(cit)));
		harness.check(ci.isAssignableFrom(cit));
		try	{
			ci = Class.forName("java.io.Serializable");			
			int[] ia=new int[2];
			// for (i=0;  i < ia.getClass().getInterfaces().length ; i++){
			// harness.debug("ia implements "+ia.getClass().getInterfaces()[i]); 
                        // }
			harness.check(ci.isInstance(ia),"array implements");
			harness.check(!(ci.isInstance(null)));
			Class arrclass = Class.forName("[I");
			harness.check(ci.isAssignableFrom(arrclass));
			}
		catch (Exception e){}
				
		
	}		
/*
        public void test_getResource()
        {  	// this test assume the classpath setting include current directory
 	  	try 	{
 	    		FileInputStream is = new FileInputStream("gnu/testlet/wonka/lang/Class/ClassTest.class");
 	    		URL url = getClass().getResource("/gnu/testlet/wonka/lang/Class/ClassTest.class");
 	    		if (url == null)
 	      			harness.fail("Error : test_getResource Failed - 1");
			else { harness.check(true);}

		    	InputStream uis = url.openStream();
 	    		byte[] b1 = new byte[100];
 	    		byte[] b2 = new byte[100];
 	    		int ret = is.read(b1);
 	    		if (ret != 100)
 	      			harness.fail("Error : test_getResource Failed - 2");
			else { harness.check(true);}
 	    		ret = uis.read(b2);
	 	    	if (ret != 100)
 	      			harness.fail("Error : test_getResource Failed - 3");
			else { harness.check(true);}
 	    		for (int i=0; i < 100; i++)	
				{
		     		if (b1[i] != b2[i])
					{
 					harness.fail("Error : test_getResource Failed - 4");
					break;
	 	      			}
				else { harness.check(true);}
 	    			}

	 	    	uis = getClass().getResourceAsStream("/gnu/testlet/wonka/lang/Class/ClassTest.class");
 	    		if (uis == null)
 	      			harness.fail("Error : test_getResource Failed - 5");
			else { harness.check(true);}
 	    		ret = uis.read(b2);
 	    		if (ret != 100)
 	      			harness.fail("Error : test_getResource Failed - 6");
			else { harness.check(true);}
 	    		for (int i=0; i < 100; i++)
				{
 	      			if (b1[i] != b2[i])	
					{
 					harness.fail("Error : test_getResource Failed - 7");
 					break;
 	      				}
				else { harness.check(true);}
				}

 	  	}
		catch (Throwable e){ harness.fail("Error : test_getResource Failed - 0 Cought an Exception"); }
		Class ci = Integer.TYPE;
		harness.check( (ci.getResource("this resource will not be found")) == null);
	}
*/
	public void testall()
	{
		harness.setclass("java.lang.Class");
		harness.checkPoint("toString");
		test_toString();
		harness.checkPoint("getName");
		test_getName();
		harness.checkPoint("isInterface");
		test_isInterface();
		harness.checkPoint("getSuperclass");
		test_getSuperclass();
		harness.checkPoint("getInterface");
		test_getInterfaces();
		harness.checkPoint("newInstance");
		test_newInstance();
		test_forName();
		harness.checkPoint("ComponentType");
                test_ComponentType();
		harness.checkPoint("getClassloader");
                test_getClassloader();
		harness.checkPoint("isMethods");
                test_isMethods();
		harness.checkPoint("getResource");
//                test_getResource();

	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
