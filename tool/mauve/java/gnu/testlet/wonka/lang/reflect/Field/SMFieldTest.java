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


package gnu.testlet.wonka.lang.reflect.Field;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.String;

/**
* this file contains testcode for the java.lang.reflect.Field class<br>
* <br>
* Needs tests on: <br>
* - 
*
*/
public class SMFieldTest implements Testlet
{
	protected TestHarness th;
	protected Field[] f,fa,fax,fpr;
	protected Field fn = null;
        protected FhlpAllpr fhap;
	protected Fhlp fh = new Fhlp();
  	protected FhlpAll fha = new FhlpAll();

  	public void test (TestHarness harness)
  	{
		th = harness;
  		th.setclass("java.lang.reflect.Field");
       		
  		SetupFields();
  		
		test_getDeclaringClass ();
		test_getModifiers ();
		test_getName ();
		test_getType ();
		test_equals ();
		test_hashCode ();
		test_toString ();

		test_get();
		test_getBoolean();
		test_getByte();
		test_getChar();
		test_getDouble();
		test_getFloat();
		test_getInt();
		test_getLong();
		test_getShort();

		test_set();
		test_setBoolean();
		test_setByte();
		test_setChar();
		test_setDouble();
		test_setFloat();
		test_setInt();
		test_setLong();
		test_setShort();

 	} 

	private void SetupFields() {

			try 	{
			fa = new Field[4];
			fa[0]  = new Fhlp().getClass().getDeclaredField("pui");
			fa[1]  = new Fhlp().getClass().getDeclaredField("puj");
			fa[2]  = new Fhlp().getClass().getDeclaredField("prl");
			fa[3]  = new Fhlp().getClass().getDeclaredField("prk");
			fax = new Field[2];
			fax[0]  = new Fhlp().getClass().getField("pui");
			fax[1]  = new Fhlp().getClass().getField("puj");
			fhap = new FhlpAllpr();
			fpr = new Field[10];
			fpr[0] = new FhlpAllpr().getClass().getDeclaredField("b");
			fpr[1] = new FhlpAllpr().getClass().getDeclaredField("bt");
			fpr[2] = new FhlpAllpr().getClass().getDeclaredField("c");
			fpr[3] = new FhlpAllpr().getClass().getDeclaredField("d");
			fpr[4] = new FhlpAllpr().getClass().getDeclaredField("f");
			fpr[5] = new FhlpAllpr().getClass().getDeclaredField("i");
			fpr[6] = new FhlpAllpr().getClass().getDeclaredField("l");
			fpr[7] = new FhlpAllpr().getClass().getDeclaredField("s");
			fpr[8] = new FhlpAllpr().getClass().getDeclaredField("st");
			fpr[9] = new FhlpAllpr().getClass().getDeclaredField("str");
			f = new Field[10];
			f[0] = new FhlpAll().getClass().getDeclaredField("b");
			f[1] = new FhlpAll().getClass().getDeclaredField("bt");
			f[2] = new FhlpAll().getClass().getDeclaredField("c");
			f[3] = new FhlpAll().getClass().getDeclaredField("d");
			f[4] = new FhlpAll().getClass().getDeclaredField("f");
			f[5] = new FhlpAll().getClass().getDeclaredField("i");
			f[6] = new FhlpAll().getClass().getDeclaredField("l");
			f[7] = new FhlpAll().getClass().getDeclaredField("s");
			f[8] = new FhlpAll().getClass().getDeclaredField("st");
			f[9] = new FhlpAll().getClass().getDeclaredField("str");
			}
		catch 	(Exception e) {};


	}

/**
* implemented.
*
*/
	protected void test_getDeclaringClass ()
	{
		th.checkPoint("getDeclaringClass()java.lang.Class");
                th.check( fa[0].getDeclaringClass() == new Fhlp().getClass(),
                	"test -- 1");
                th.check( fax[0].getDeclaringClass() == new Fhlp().getClass(),
                	"test -- 2");
	}

/**
* implemented.
*
*/
	protected void test_getModifiers ()
	{
		th.checkPoint("getModifiers()int");
  		th.check(fa[0].getModifiers() == Modifier.PUBLIC + Modifier.FINAL, 		    	"test -- 1 "+Modifier.toString(fa[0].getModifiers()));
		th.check(fa[1].getModifiers() == Modifier.PUBLIC + Modifier.STATIC, 		    	"test -- 2 "+Modifier.toString(fa[1].getModifiers()));
		th.check(fa[2].getModifiers() == Modifier.PRIVATE, 				    	"test -- 3 "+Modifier.toString(fa[2].getModifiers()));
		th.check(fa[3].getModifiers() == Modifier.PROTECTED + Modifier.FINAL + Modifier.STATIC,	"test -- 4 "+Modifier.toString(fa[3].getModifiers()));
	}

/**
* implemented.
*
*/
	protected void test_getName ()
	{
		th.checkPoint("getName()java.lang.String");
                th.check(fa[0].getName().equals("pui") , "test -- 1");
                th.check(fa[1].getName().equals("puj") , "test -- 2");
                th.check(fa[2].getName().equals("prl") , "test -- 3");
                th.check(fa[3].getName().equals("prk") , "test -- 4");
	}

/**
* implemented.
*
*/
	protected void test_getType()	
	{
		th.checkPoint("getType()java.lang.Class");
	 	th.check(fa[0].getType() == Integer.TYPE , 	"test -- 1");
                th.check(fa[1].getType() == Integer.TYPE , 	"test -- 2");
                th.check(fa[2].getType() == Long.TYPE , 	"test -- 3");
                th.check(fa[3].getType() == Long.TYPE , 	"test -- 4");

	}

/**
* implemented.
*
*/
	protected void test_get ()
	{
		th.checkPoint("get(java.lang.Object)java.lang.Object");
  		try   {	fa[0].get(null);
	        	th.fail("should throw a NullPointerException -- 1");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
  		try   {	fa[1].get(null);
	   		th.check(true);
	     	      }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
 		try   {	fa[0].get(this);
	        	th.fail("should throw an IllegalArgumentException");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
  		try   {	fa[2].get(fh);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		try   {	th.check(((Integer) fa[0].get(fh)).intValue() == 23 	, "test if correct value returned -- 1" );
  			th.check(((Integer) fa[1].get(fh)).intValue() == 54 	, "test if correct value returned -- 2" );
  			fa[2].setAccessible(true);
  			th.check(((Long) fa[2].get(fh)).longValue() == 1234l	, "test if correct value returned -- 3" );
  			th.check(((Long) fa[3].get(fh)).longValue() == 1l 	, "test if correct value returned -- 4" );
  			th.check(((Boolean) f[0].get(fha)).booleanValue()== true, "test if correct value returned -- 5" );
	                th.check(((Byte) f[1].get(fha)).byteValue()== (byte) 1	, "test if correct value returned -- 6" );
			th.check(((Character) f[2].get(fha)).charValue()== 'c'	, "test if correct value returned -- 7" );
			th.check(((Double) f[3].get(fha)).doubleValue()== 2.0 	, "test if correct value returned -- 8" );
	                th.check(((Float) f[4].get(fha)).floatValue()== 3.0f 	, "test if correct value returned -- 9" );
			th.check(((Integer) f[5].get(fha)).intValue()== 4 	, "test if correct value returned -- 10" );
			th.check(((Long) f[6].get(fha)).longValue()== 5L 	, "test if correct value returned -- 11" );
			th.check(((Short) f[7].get(fha)).shortValue()== (short)6, "test if correct value returned -- 12" );
	                th.check( f[8].get(fha) == null				, "test if correct value returned -- 13" );
	                th.check( f[9].get(fha).equals("a")				, "test if correct value returned -- 14 (expected 'a', got '" + f[9].get(fha) + "'" );
  			fa[2].setAccessible(false);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }	
	 }
/**
* implemented.
*
*/
	protected void test_getBoolean ()
	{
		th.checkPoint("getBoolean(java.lang.Object)boolean");
  		try   {	fa[0].getBoolean(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		try   {	fa[0].getBoolean(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		try   {	fpr[0].getBoolean(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		try   {	th.check( f[0].getBoolean(fha), "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	fa[1].getBoolean(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[0].getBoolean(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

	}

/**
* implemented.
*
*/
	protected void test_getByte ()
	{
		th.checkPoint("getByte(java.lang.Object)byte");
  		try   {	fa[0].getByte(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		try   {	fa[0].getByte(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		try   {	fpr[1].getByte(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		try   {	th.check( f[1].getByte(fha) == (byte) 1, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	fa[3].getByte(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[1].getByte(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

	}

/**
* implemented.
*
*/
	protected void test_getChar ()
	{
		th.checkPoint("getChar(java.lang.Object)char");
  		try   {	fa[0].getChar(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getChar(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[2].getChar(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[2].getChar(fha) == 'c', "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	fa[0].getChar(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[2].getChar(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }


	}

/**
* implemented.
*
*/
	protected void test_getDouble ()
	{
		th.checkPoint("getDouble(java.lang.Object)double");
  		try   {	fa[0].getDouble(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getDouble(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[3].getDouble(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[3].getDouble(fha) == 2.0, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	f[0].getDouble(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[3].getDouble(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

  		try   { th.check (f[1].getDouble(fha) == (double)((byte)1),
  				"test returned wrong value -- 1");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 1"); }
  		try   { th.check (f[2].getDouble(fha) == (double)'c',
  				"test returned wrong value -- 2");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 2"); }
  		try   { th.check (f[4].getDouble(fha) == (double)3.0f,
  				"test returned wrong value -- 3");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 3"); }
  		try   { th.check (f[5].getDouble(fha) == (double)4,
  				"test returned wrong value -- 4");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 4"); }
  		try   { th.check (f[6].getDouble(fha) == (double) 5L,
  				"test returned wrong value -- 5, got:"+f[6].getDouble(fha));
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 5"); }
  		try   { th.check (f[7].getDouble(fha) == (double)((char)6),
  				"test returned wrong value -- 6, got:"+f[7].getDouble(fha));
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 6"); }
	
	}

/**
* implemented.
*
*/
	protected void test_getFloat ()
	{
		th.checkPoint("getFloat(java.lang.Object)float");
  		try   {	fa[0].getFloat(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getFloat(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[4].getFloat(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[4].getFloat(fha) == 3.0f, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	f[0].getFloat(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[4].getFloat(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

  		try   { th.check (f[1].getFloat(fha) == (float)((byte)1),
  				"test returned wrong value -- 1");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 1"); }
  		try   { th.check (f[2].getFloat(fha) == (float)'c',
  				"test returned wrong value -- 2");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 2"); }
  		try   { th.check (f[5].getFloat(fha) == (float)4,
  				"test returned wrong value -- 3");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 3"); }
  		try   { th.check (f[6].getFloat(fha) == (float) 5L,
  				"test returned wrong value -- 4");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 4"); }
  		try   { th.check (f[7].getFloat(fha) == (float)((char)6),
  				"test returned wrong value -- 5");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 5"); }

	}

/**
* implemented.
*
*/
	protected void test_getInt ()
	{
		th.checkPoint("getInt(java.lang.Object)int");
  		try   {	fa[0].getInt(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getInt(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[5].getInt(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[5].getInt(fha) == 4, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	f[0].getInt(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[5].getInt(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

  		try   { th.check (f[1].getInt(fha) == (int)((byte)1),
  				"test returned wrong value -- 1");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 1"); }
  		try   { th.check (f[2].getInt(fha) == (int)'c',
  				"test returned wrong value -- 2");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 2"); }
  		try   { th.check (f[7].getInt(fha) == (int)((char)6),
  				"test returned wrong value -- 4");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 4"); }


	}

/**
* implemented.
*
*/
	protected void test_getLong ()
	{
		th.checkPoint("getLong(java.lang.Object)long");
  		try   {	fa[0].getLong(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getLong(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[6].getLong(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[6].getLong(fha) == 5L, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	f[0].getLong(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[6].getLong(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

  		try   { th.check (f[1].getLong(fha) == (long)((byte)1),
  				"test returned wrong value -- 1");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 1"); }
  		try   { th.check (f[2].getLong(fha) == (long)'c',
  				"test returned wrong value -- 2");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 2"); }
  		try   { th.check (f[5].getLong(fha) == (long)4,
  				"test returned wrong value -- 3");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 3"); }
  		try   { th.check (f[7].getLong(fha) == (long)((char)6),
  				"test returned wrong value -- 4");
  			th.check(true);
  		      }  				
  		catch ( Exception e ) { th.fail("should not throw an exception -- 4"); }

	}

/**
* implemented.
*
*/
	protected void test_getShort ()
	{
		th.checkPoint("getShort(java.lang.Object)short");
  		try   {	fa[0].getShort(null);
	        	th.fail("should throw a NullPointerException");
	              }
               	catch ( NullPointerException ne ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 1 "+e); }
 		
 		try   {	fa[0].getShort(this);
	        	th.fail("should throw an IllegalArgumentException -- 1");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 2 "+e); }
  		
  		try   {	fpr[7].getShort(fhap);
	        	th.fail("should throw an IllegalAccessException");
	              }
               	catch ( IllegalAccessException ie ) { th.check(true); }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 3 "+e); }
 		
 		try   {	th.check( f[7].getInt(fha) == (short) 6, "test if correct value returned -- 1" );
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 4 "+e); }
 		try   {	f[0].getShort(fh);
	        	th.fail("should throw an IllegalArgumentException -- 2");
	              }
               	catch ( IllegalArgumentException ie ) { th.check(true); }
 		catch ( Exception e ) { th.fail("wrong Exception thrown -- 5 "+e); }
                try   {	f[7].getShort(null);
	        	th.check(true);
	              }
  		catch ( Exception e ) { th.fail("wrong Exception thrown -- 6 "+e); }

  		try   { th.check (f[1].getShort(fha) == (short)((byte)1),
  				"test returned wrong value -- 1");
  			th.check(true);
  		      }
  		catch ( Exception e ) { th.fail("should not throw an exception -- 1"); }
  		       				
	}

/**
* not implemented.
*
*/
	protected void test_set ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setBoolean ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setByte ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setChar ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setDouble ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setFloat ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setInt ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setLong ()
	{
		th.checkPoint("	()");

	}
/**
* not implemented.
*
*/
	protected void test_setShort ()
	{
		th.checkPoint("	()");

	}

/**
* implemented.
*
*/
	protected void test_toString ()
	{
		th.checkPoint("toString()java.lang.String");
                th.check( fa[0].toString(), "public final int gnu.testlet.wonka.lang.reflect.Field.Fhlp.pui",
                	"testing string representation -- 1");
                th.check( fax[0].toString(), "public final int gnu.testlet.wonka.lang.reflect.Field.Fhlp.pui",
                	"testing string representation -- 2");
                th.check( fa[1].toString(), "public static int gnu.testlet.wonka.lang.reflect.Field.Fhlp.puj",
                	"testing string representation -- 3");
                th.check( fax[1].toString(), "public static int gnu.testlet.wonka.lang.reflect.Field.Fhlp.puj",
                	"testing string representation -- 4");
                th.check( fa[2].toString(), "private long gnu.testlet.wonka.lang.reflect.Field.Fhlp.prl",
                	"testing string representation -- 5");
                th.check( fa[3].toString(), "protected static final long gnu.testlet.wonka.lang.reflect.Field.Fhlp.prk",
                	"testing string representation -- 6");
        	}

/**
* implemented.
*
*/
	protected void test_hashCode ()
	{
		th.checkPoint("hashCode()int");
		int h = fa[0].getDeclaringClass().getName().hashCode();
		int j = fa[0].getName().hashCode();		
		j = h ^ j;
		h = fa[0].hashCode();
		th.check( h == j ,"hashCodes shouldbe the same -- 1" );
		th.check(fa[0].hashCode() == h,"hashCodes shouldbe the same -- 2" );
                th.check(fa[0].hashCode() != fa[1].hashCode() , "hashCodes should be different" );
                th.check(fa[0].hashCode() == fa[0].hashCode(),"hashCodes shouldbe the same -- 3" );
	}

/**
* implemented.
*
*/
	protected void test_equals ()
	{
		th.checkPoint("equals(java.lang.Object)boolean");
		try   { fn.equals("a");
			th.fail("Should throw a NullPointerException");
		      }
		catch ( NullPointerException ne) { th.check(true); } 		
		try   { th.check(! fa[0].equals(null)); th.check(true); }
		catch ( NullPointerException ne) { th.fail("Shouldn't throw a NullPointerException"); } 		

		th.check( fa[0].equals(fa[0])  , "should be equal -- 1" );
		th.check( fa[0].equals(fax[0]) , "should be equal -- 2" );
		th.check( ! fa[0].equals(fa[1]), "shouldn't be equal -- 1");
	}

} 
