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


package gnu.testlet.wonka.lang.reflect.Array;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.Array;
import java.lang.*;

/**
* Tests for java.lang.reflect.Array
* <br>
* the set functions are not implemented in wonka --> test not written yet	
*
*/
public class SMArrayTest implements Testlet
{
	protected TestHarness th;

  	public void test (TestHarness harness)
  	{
		th = harness;    
      		th.setclass("java.lang.reflect.Array");
		test_newInstance();
		test_getLength();
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
/**
* implemented.	<br>
* This test should be altered if the max dimensionsize of arrays is NOT 255 <br>
*
*/
	protected void test_newInstance()
	{
	    th.checkPoint("newInstance(java.lang.Class,int)java.lang.Object");

		Object oa = Array.newInstance(Integer.TYPE, 10);
		th.check(oa.getClass().isArray(), "oa should be an Array");

		int [] ia = (int[])Array.newInstance(Integer.TYPE, 10);
		th.check (ia.length == 10, "ia.length = "+ia.length+", should be 10");
		try 	{ ia[9] = 10; th.check(true); 
			  th.check(ia[9] == 10, "array is not correctly implemented");	
			}
		catch (Exception e) { th.fail("no exception expected"); }
		try   { ia[10] = 10; 
			th.fail("should throw an ArrayIndexOutOfBoundsException"); 
		      }
		catch (Exception e) { th.check(true); }

		try 	{
			oa = Array.newInstance(Float.TYPE, -1);
			th.fail("should throw an NegativeArraySizeException");
			}
		catch	(NegativeArraySizeException ne)	{ th.check(true); }
		try 	{
			oa = Array.newInstance(Float.TYPE, 0);
		        th.check(true);
			th.check(oa != null);
			}
		catch	(NegativeArraySizeException ne)	{th.fail("should throw not NegativeArraySizeException"); }



	    th.checkPoint("newInstance(java.lang.Class,int[])java.lang.Object");

		ia = new int [10];
		oa = Array.newInstance(Integer.TYPE, ia);
		th.check(oa.getClass().isArray(), "oa should be an Array");
// test on max dimensionsize
		ia = (int[])Array.newInstance(Integer.TYPE, 256);
		for (int i=0 ; i < 256 ; i++) { ia[i] = 10; }
		try 	{ 
			oa = Array.newInstance(Integer.TYPE, ia);
			th.fail("should throw an IllegalArgumentException"); 
		        }
		catch (IllegalArgumentException e) { th.check(true); }
		ia = new int [0];		
		try 	{ 
			oa = Array.newInstance(Integer.TYPE, ia);
			th.fail("should throw an IllegalArgumentException"); 
		        }
		catch (IllegalArgumentException e) { th.check(true); }
		
	}
/**
* implemented.
*
*/
	protected void test_getLength()
	{
		th.checkPoint("getLength(java.lang.Object)int");
		
		int [] ia = null;
		try  {	Array.getLength(ia);
			th.fail("should throw an NullPointerException");
		     }
		catch (NullPointerException ne)	{ th.check(true); }

		Object oa = Array.newInstance(Integer.TYPE, 2);
		try  {	th.check(Array.getLength(oa), 2 ,"length should be 2");
			th.check(true);
		     }
		catch (IllegalArgumentException ie)	{ th.fail("should not throw IllegalArgumentException, got:"+ie); }

		oa = new Object();
		try  {	Array.getLength(oa);
			th.fail("should throw IllegalArgumentException");
		     }
		catch (IllegalArgumentException ie)	{ th.check(true); }

		ia = (int[])Array.newInstance(Integer.TYPE, 25);
		for (int i=0 ; i < 25 ; i++) { ia[i] = 10; }
		th.check(Array.getLength(ia)== 25, "length is the number of dimensions");
		th.check(ia.length == 25, "test covers also newIstance()");
	}
/**
* implemented.
*
*/
	protected void test_get()
	{
		th.checkPoint("get(java.lang.Object,int)java.lang.Object");

		boolean []ba = new boolean[2];
		ba[1] = true; ba[0] =false;
		Object ro = Array.get(ba, 1);
		th.check(ro instanceof Boolean ,"get() failed  -- 1 ");
		th.check(((Boolean)ro).booleanValue(), "wrong boolean value returned -- 2");

		byte []bta = new byte[2];
		bta[1] = 13; bta[0] =23;
		ro =Array.get(bta, 1);
		th.check(ro instanceof Byte ,"get() failed  -- 3 ");
		th.check(((Byte)ro).byteValue()==13, "wrong byte value returned -- 4 ,got:"+ro);

		char [] ca = new char[2];
		ca[1] = 't'; ca[0] ='f';
		ro =Array.get(ca, 0);
		th.check(ro instanceof Character ,"get() failed  -- 5 ");
		th.check(((Character)ro).charValue()== 'f', "wrong char value returned -- 6 ,got:"+ro);

		double []da = new double[2];
		da[1] = 3.0; da[0] =2.0;
		ro =Array.get(da, 1);
		th.check(ro instanceof Double ,"get() failed  -- 7 ");
		th.check(((Double)ro).doubleValue()== 3.0, "wrong double value returned -- 8  ,got:"+ro);
                th.check(da[1] == 3.0 ,"basic check");

		float []fa = new float[2];
		fa[1] = 3.6f; fa[0] =2.5f;
		ro =Array.get(fa, 0);
		th.check(ro instanceof Float ,"get() failed  -- 9 ");
		th.check(((Float)ro).floatValue()== 2.5f, "wrong float value returned -- 10  ,got:"+ro);

		int []ia = new int[2];
		ia[1] = 23; ia[0] = 45;
		ro =Array.get(ia, 1);
		th.check(ro instanceof Integer ,"get() failed  -- 11 ");
		th.check(((Integer)ro).intValue()== 23, "wrong int value returned -- 12  ,got:"+ro);

		long []la = new long[2];
		la[1] = 123L; la[0] =213L;
		ro =Array.get(la, 1);
		th.check(ro instanceof Long ,"get() failed  -- 13 ");
		th.check(((Long)ro).longValue()== 123, "wrong long value returned -- 14  ,got:"+ro);

		short []sa = new short[2];
		sa[1] = 12; sa[0] =21;
		ro =Array.get(sa, 1);
		th.check(ro instanceof Short ,"get() failed  -- 15 ");
		th.check(((Short)ro).shortValue()== 12, "wrong short value returned --16 ,got:"+ro);

		Object []oa = new Object[2];
		oa[0] = this ; oa[1] = null; 
		ro = Array.get(oa , 0 );
		th.check( ro.equals(this),"get returned wrong Object");		
		ro = Array.get(oa , 1 );
		th.check( ro == null , "null objects are allowed!");
		try   {	Array.get(oa , -1);
			th.fail("ArrayIndexOutOfBoundsException should be thrown");
		      }		
		catch ( ArrayIndexOutOfBoundsException ae ) { th.check(true); }
		try   {	Array.get(oa , 10);
			th.fail("ArrayIndexOutOfBoundsException should be thrown");
		      }		
		catch ( ArrayIndexOutOfBoundsException ae ) { th.check(true); }
		oa = null;
		try   {	Array.get(oa , 0);
			th.fail("NullPointerException should be thrown");
		      }		
		catch ( NullPointerException ne ) { th.check(true); }




	}
/**
* implemented.
*
*/
	protected void test_getBoolean()
	{
		th.checkPoint("getBoolean(java.lang.Object,int)boolean");

		boolean [] ba = new boolean [10];
		for (int i=0; i < 10 ; i++) { ba[i] = true; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getBoolean(ba , j), "retrieved wrong value -- "+j); }
		ba[0] = false;
		th.check(!Array.getBoolean(ba,0) , "retrieved wrong value -- 11");
		
		try {	Array.getBoolean(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getBoolean(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getBoolean(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getBoolean(new Object() , 1);
			th.fail("should throw an IllegalArgumentException");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		short []sa = new short[2];
		sa[1] = 12; sa[0] =21;
		try {	Array.getBoolean(sa , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }		
		Object[] ob = (Object [])Array.newInstance(new Boolean(false).getClass() , 2);
		ob[1] = ob[0] =new Boolean(false);
		try {	Array.getBoolean(ob , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }		
	}
	
/**
* implemented.
*
*/
	protected void test_getByte()
	{
		th.checkPoint("getByte(java.lang.Object,int)byte");

		byte [] ba = new byte [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (byte)i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getByte(ba , j)== (byte)j, "retrieved wrong value -- "+j); }
	
		try {	Array.getByte(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getByte(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getByte(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getByte(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		short []sa = new short[2];
		sa[1] = 12; sa[0] =21;
		try {	Array.getByte(sa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }
	}
/**
* implemented.
*
*/
	protected void test_getChar()
	{
		th.checkPoint("getChar(java.lang.Object,int)char");

		char [] ba = new char [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (char)i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getChar(ba , j)== (char)j, "retrieved wrong value -- "+j); }
	
		try {	Array.getChar(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getChar(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getChar(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getChar(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		short []sa = new short[2];
		sa[1] = 12; sa[0] =21;
		try {	Array.getChar(sa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }
		
		byte []bta = new byte[2];
		bta[1] = (byte)12; bta[0] =(byte)21;
		try {	Array.getChar(bta , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }
	}
/**
* implemented.
*
*/
	protected void test_getDouble()
	{
		th.checkPoint("getDouble(java.lang.Object,int)double");

		double d;
		double [] ba = new double [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (double)i; }
		Array.get(ba , 1);
		for (int j=0; j < 10 ; j++)
			{
			th.check(((Double)Array.get(ba,j)).doubleValue() == (double)j, "retrieved wrong value -- a "+j);
			th.check( Array.getDouble(ba , j)== (double)j, "retrieved wrong value -- b "+j);
		}
	
		try {	Array.getDouble(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getDouble(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getDouble(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getDouble(new Object() , 1);
			th.fail("should throw an IllegalArgumentException --  1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		boolean []wa = new boolean[2];
		wa[1] = false; wa[0] =true;
		try {	Array.getDouble(wa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		float []fa = new float[2];
		fa[1] = 1.2f; fa[0] =2.3f;
		try {	d = Array.getDouble(fa , 1); th.check(d == (double) 1.2f,"checking value of float"); }
		catch (IllegalArgumentException ne) { th.fail("shouldn't throw an IllegalArgumentException -- 1"); }
		long []la = new long[2];
		la[1] = 1L; la[0] =2L;
		try {	d = Array.getDouble(la , 1); th.check(d == 1.0);  }
		catch (IllegalArgumentException ne) { th.fail("shouldn't throw an IllegalArgumentException -- 2"); }
		int []ia = new int[2];
		ia[1] = 1; ia[0] =2;
		try {	Array.getDouble(ia , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 3"); }
		char []ca = new char[2];
		ca[1] = 'f'; ca[0] ='a';
		try {	Array.getDouble(ca , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 4"); }
		byte []bta = new byte[2];
		bta[1] = 1; bta[0] =1;
		try {	Array.getDouble(bta , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 5"); }
		short []sa = new short[2];
		sa[1] = 1; sa[0] =1;
		try {	Array.getDouble(sa , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 6"); }

	}
/**
* implemented.
*
*/
	protected void test_getFloat()
	{
		th.checkPoint("getFloat(java.lang.Object,int)float");

		float [] ba = new float [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (float)i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getFloat(ba , j)== (float)j, "retrieved wrong value -- "+j); }
	
		try {	Array.getFloat(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getFloat(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getFloat(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getFloat(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		boolean []wa = new boolean[2];
		wa[1] = false; wa[0] =true;
		try {	Array.getFloat(wa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		double []da = new double[2];
		da[1] = 1.2; da[0] =2.3;
		try {	Array.getFloat(da , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
	
		long []la = new long[2];
		la[1] = 1L; la[0] =2L;
		try {	Array.getFloat(la , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 1"); }
		int []ia = new int[2];
		ia[1] = 1; ia[0] =2;
		try {	Array.getFloat(ia , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 2"); }
		char []ca = new char[2];
		ca[1] = 'f'; ca[0] ='a';
		try {	Array.getFloat(ca , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 3"); }
		byte []bta = new byte[2];
		bta[1] = 1; bta[0] =1;
		try {	Array.getFloat(bta , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 4"); }
		short []sa = new short[2];
		sa[1] = 1; sa[0] =1;
		try {	Array.getFloat(sa , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 5"); }


	}
/**
* implemented.
*
*/
	protected void test_getInt()
	{
		th.checkPoint("getInt(java.lang.Object,int)int");

		int [] ba = new int [10];
		for (int i=0; i < 10 ; i++) { ba[i] = i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getInt(ba , j)== j, "retrieved wrong value -- "+j); }
	
		try {	Array.getInt(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getInt(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getInt(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getInt(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		boolean []wa = new boolean[2];
		wa[1] = false; wa[0] =true;
		try {	Array.getInt(wa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		double []da = new double[2];
		da[1] = 1.2; da[0] =2.3;
		try {	Array.getInt(da , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		float []fa = new float[2];
		fa[1] = 1.2f; fa[0] =2.3f;
		try {	Array.getInt(fa , 1);
			th.fail("should throw an IllegalArgumentException -- 4");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		long []la = new long[2];
		la[1] = 12L; la[0] =343443L;
		try {	Array.getInt(la , 1);
			th.fail("should throw an IllegalArgumentException -- 5");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		char []ca = new char[2];
		ca[1] = 'f'; ca[0] ='a';
		try {	Array.getInt(ca , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 1"); }
		byte []bta = new byte[2];
		bta[1] = 1; bta[0] =1;
		try {	Array.getInt(bta , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 2"); }
		short []sa = new short[2];
		sa[1] = 1; sa[0] =1;
		try {	Array.getInt(sa , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 3"); }


	}
/**
* implemented.
*
*/
	protected void test_getLong()
	{
		th.checkPoint("getLong(java.lang.Object,int)long");

		long [] ba = new long [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (long)i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getLong(ba , j)== (long)j, "retrieved wrong value -- "+j); }
	
		try {	Array.getLong(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getLong(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getLong(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getLong(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		boolean []wa = new boolean[2];
		wa[1] = false; wa[0] =true;
		try {	Array.getLong(wa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		double []da = new double[2];
		da[1] = 1.2; da[0] =2.3;
		try {	Array.getLong(da , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		float []fa = new float[2];
		fa[1] = 1.2f; fa[0] =2.3f;
		try {	Array.getLong(fa , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
	
		int []ia = new int[2];
		ia[1] = 1; ia[0] =2;
		try {	Array.getLong(ia , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 1"); }
		char []ca = new char[2];
		ca[1] = 'f'; ca[0] ='a';
		try {	Array.getLong(ca , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 2"); }
		byte []bta = new byte[2];
		bta[1] = 1; bta[0] =1;
		try {	Array.getLong(bta , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 3"); }
		short []sa = new short[2];
		sa[1] = 1; sa[0] =1;
		try {	Array.getLong(sa , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException -- 4"); }


	}
/**
* implemented.
*
*/
	protected void test_getShort()
	{
		th.checkPoint("getShort(java.lang.Object,int)short");

		short [] ba = new short [10];
		for (int i=0; i < 10 ; i++) { ba[i] = (short)i; }
		for (int j=0; j < 10 ; j++)
			{ th.check( Array.getShort(ba , j)==(short) j, "retrieved wrong value -- "+j); }
	
		try {	Array.getShort(ba , -1);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		try {	Array.getShort(ba , 10);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }
		ba = null ;
		try {	Array.getShort(ba , 1);
			th.fail("should throw a NullPointerException");
		    }
		catch (NullPointerException ne) { th.check(true); }

		try {	Array.getShort(new Object() , 1);
			th.fail("should throw an IllegalArgumentException -- 1");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		boolean []wa = new boolean[2];
		wa[1] = false; wa[0] =true;
		try {	Array.getShort(wa , 1);
			th.fail("should throw an IllegalArgumentException -- 2");
		    }
		catch (IllegalArgumentException ne) { th.check(true); }

		double []da = new double[2];
		da[1] = 1.2; da[0] =2.3;
		try {	Array.getShort(da , 1);
			th.fail("should throw an IllegalArgumentException -- 3");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		float []fa = new float[2];
		fa[1] = 1.2f; fa[0] =2.3f;
		try {	Array.getShort(fa , 1);
			th.fail("should throw an IllegalArgumentException -- 4");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		long []la = new long[2];
		la[1] = 12L; la[0] =343443L;
		try {	Array.getShort(la , 1);
			th.fail("should throw an IllegalArgumentException -- 5");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		char []ca = new char[2];
		ca[1] = 'L'; ca[0] ='L';
		try {	Array.getShort(ca , 1);
			th.fail("should throw an IllegalArgumentException -- 6");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }
		int []ia = new int[2];
		ia[1] = 12; ia[0] =343;
		try {	Array.getShort(ia , 1);
			th.fail("should throw an IllegalArgumentException -- 7");
		    }
		catch (IllegalArgumentException ne) {  th.check(true); }

		byte []bta = new byte[2];
		bta[1] = 1; bta[0] =1;
		try {	Array.getShort(bta , 1); th.check(true);  }
		catch (IllegalArgumentException ne) {  th.fail("shouldn't throw an IllegalArgumentException"); }


	}
/**
* implemented.
*
*/
	protected void test_set()
	{
		th.checkPoint("set(java.lang.Object,int,java.lang.Object)void");

		Object[] oa = null;
		Integer integer = new Integer(42);

		try {	
			Array.set(oa , 0, integer);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		oa = new Integer[10];

		integer = new Integer(12345);

		try {	Array.set(oa , -1, integer);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(oa , 10, integer);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(oa , -1, integer);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 3");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(oa, 5, integer);
		th.check(((Integer[])oa)[5].intValue() == 12345, "oa[5] should be set");
		th.check(oa[0] == null && oa[1] == null && oa[2] == null && oa[3] == null && oa[4] == null && oa[6] == null && oa[7] == null && oa[8] == null && oa[9] == null, "no other element of oa[] should be affected");

		try {
			Array.set(oa, 7, new String("hopla!"));
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		boolean[] za = null;
		byte[] ba = null;
		short[] sa = null;
		char[] ca = null;
		int[] ia = null;
		float[] fa = null;
		double[] da = null;
		long[] ja = null;
		Boolean z = new Boolean(true);
		Byte b = new Byte((byte)-33);
		Short s = new Short((short)8192);
		Character c = new Character((char)60000);
		Float f = new Float(12.34F);
		Double d = new Double(56.789D);
		Integer i = new Integer(54775807);
		Long j = new Long(9223372036854775807L);

		try {
			Array.set(za, 1, z);
			th.fail("should throw a NullPointerException -- 2");
		    }
		catch (NullPointerException ae) { th.check(true); }

		za = new boolean[10];
		z = null;

		try {	Array.set(za , 2, z);
			th.fail("should throw a NullPointerException -- 3");
		    }
		catch (NullPointerException ae) { th.check(true); }

		z = new Boolean(true);
		try {	Array.set(za , 10, z);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 4");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(za , -1, z);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 5");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(za, 5, z);
		th.check(za[5], "za[5] should be set");
		th.check(za[0] == false && za[1] == false && za[2] == false && za[3] == false && za[4] == false && za[6] == false && za[7] == false && za[8] == false && za[9] == false, "no other element of za[] should be affected");

		try {
			Array.set(za, 1, b);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, s);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, c);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, i);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, j);
			th.fail("should throw an IllegalArgumentException -- 6");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, f);
			th.fail("should throw an IllegalArgumentException -- 7");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(za, 1, d);
			th.fail("should throw an IllegalArgumentException -- 8");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, b);
			th.fail("should throw a NullPointerException -- 4");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ba = new byte[10];
		b = null;

		try {	Array.set(ba , 3, b);
			th.fail("should throw a NullPointerException -- 5");
		    }
		catch (NullPointerException ae) { th.check(true); }

		b = new Byte((byte)-42);
		try {	Array.set(ba , 10, b);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 6");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(ba , -1, b);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 7");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(ba, 5, b);
		th.check(ba[5] == -42, "ba[5] should be set");
		th.check(ba[0] == 0 && ba[1] == 0 && ba[2] == 0 && ba[3] == 0 && ba[4] == 0 && ba[6] == 0 && ba[7] == 0 && ba[8] == 0 && ba[9] == 0, "no other element of ba[] should be affected");

		try {
			Array.set(ba, 1, z);
			th.fail("should throw an IllegalArgumentException -- 9");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, s);
			th.fail("should throw an IllegalArgumentException -- 10");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, c);
			th.fail("should throw an IllegalArgumentException -- 11");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, i);
			th.fail("should throw an IllegalArgumentException -- 12");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, j);
			th.fail("should throw an IllegalArgumentException -- 13");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, f);
			th.fail("should throw an IllegalArgumentException -- 14");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ba, 1, d);
			th.fail("should throw an IllegalArgumentException -- 15");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, s);
			th.fail("should throw a NullPointerException -- 6");
		    }
		catch (NullPointerException ae) { th.check(true); }

		sa = new short[10];
		s = null;

		try {	Array.set(sa , 4, s);
			th.fail("should throw a NullPointerException -- 7");
		    }
		catch (NullPointerException ae) { th.check(true); }

		s = new Short((short)-12345);
		try {	Array.set(sa , 10, s);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 8");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(sa , -1, s);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 9");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(sa, 5, s);
		th.check(sa[5] == -12345, "sa[5] should be set");
		th.check(sa[0] == 0 && sa[1] == 0 && sa[2] == 0 && sa[3] == 0 && sa[4] == 0 && sa[6] == 0 && sa[7] == 0 && sa[8] == 0 && sa[9] == 0, "no other element of sa[] should be affected");

		Array.set(sa, 6, b);
		th.check(sa[6] == -42, "sa[6] should be set");
		th.check(sa[0] == 0 && sa[1] == 0 && sa[2] == 0 && sa[3] == 0 && sa[4] == 0 && sa[7] == 0 && sa[8] == 0 && sa[9] == 0, "no other element of sa[] should be affected");

		try {
			Array.set(sa, 1, z);
			th.fail("should throw an IllegalArgumentException -- 16");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, c);
			th.fail("should throw an IllegalArgumentException -- 17");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, i);
			th.fail("should throw an IllegalArgumentException -- 18");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, j);
			th.fail("should throw an IllegalArgumentException -- 19");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, f);
			th.fail("should throw an IllegalArgumentException -- 20");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(sa, 1, d);
			th.fail("should throw an IllegalArgumentException -- 21");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, c);
			th.fail("should throw a NullPointerException -- 8");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ca = new char[10];
		c = null;

		try {	Array.set(ca , 4, c);
			th.fail("should throw a NullPointerException -- 9");
		    }
		catch (NullPointerException ae) { th.check(true); }

		c = new Character('@');
		try {	Array.set(ca , 10, c);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 10");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(ca , -1, c);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 11");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(ca, 5, c);
		th.check(ca[5] == '@', "ca[5] should be set");
		th.check(ca[0] == 0 && ca[1] == 0 && ca[2] == 0 && ca[3] == 0 && ca[4] == 0 && ca[6] == 0 && ca[7] == 0 && ca[8] == 0 && ca[9] == 0, "no other element of ca[] should be affected");

		try {
			Array.set(ca, 1, z);
			th.fail("should throw an IllegalArgumentException -- 22");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, b);
			th.fail("should throw an IllegalArgumentException -- 23");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, s);
			th.fail("should throw an IllegalArgumentException -- 24");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, i);
			th.fail("should throw an IllegalArgumentException -- 25");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, j);
			th.fail("should throw an IllegalArgumentException -- 26");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, f);
			th.fail("should throw an IllegalArgumentException -- 27");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ca, 1, d);
			th.fail("should throw an IllegalArgumentException -- 28");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ia, 1, i);
			th.fail("should throw a NullPointerException -- 10");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ia = new int[10];
		i = null;

		try {	Array.set(ia , 6, i);
			th.fail("should throw a NullPointerException -- 11");
		    }
		catch (NullPointerException ae) { th.check(true); }

		i = new Integer(-54321);
		try {	Array.set(ia , 10, i);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 12");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(ia , -1, i);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 13");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(ia, 5, i);
		th.check(ia[5] == -54321, "ia[5] should be set");
		th.check(ia[0] == 0 && ia[1] == 0 && ia[2] == 0 && ia[3] == 0 && ia[4] == 0 && ia[6] == 0 && ia[7] == 0 && ia[8] == 0 && ia[9] == 0, "no other element of ia[] should be affected");

		Array.set(ia, 4, b);
		th.check(ia[4] == -42, "ia[4] should be set");
		th.check(ia[0] == 0 && ia[1] == 0 && ia[2] == 0 && ia[3] == 0 && ia[6] == 0 && ia[7] == 0 && ia[8] == 0 && ia[9] == 0, "no other element of ia[] should be affected");

		Array.set(ia, 6, s);
		th.check(ia[6] == -12345, "ia[6] should be set");
		th.check(ia[0] == 0 && ia[1] == 0 && ia[2] == 0 && ia[3] == 0 && ia[7] == 0 && ia[8] == 0 && ia[9] == 0, "no other element of ia[] should be affected");

		Array.set(ia, 3, c);
		th.check(ia[3] == 64, "ia[3] should be set");
		th.check(ia[0] == 0 && ia[1] == 0 && ia[2] == 0 && ia[7] == 0 && ia[8] == 0 && ia[9] == 0, "no other element of ia[] should be affected");

		try {
			Array.set(ia, 1, z);
			th.fail("should throw an IllegalArgumentException -- 29");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ia, 1, j);
			th.fail("should throw an IllegalArgumentException -- 30");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ia, 1, f);
			th.fail("should throw an IllegalArgumentException -- 31");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ia, 1, d);
			th.fail("should throw an IllegalArgumentException -- 32");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ja, 1, j);
			th.fail("should throw a NullPointerException -- 12");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ja = new long[10];
		j = null;

		try {	Array.set(ja , 7, j);
			th.fail("should throw a NullPointerException -- 13");
		    }
		catch (NullPointerException ae) { th.check(true); }

		j = new Long(123451234512345L);
		try {	Array.set(ja , 10, j);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 14");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(ja , -1, j);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 15");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(ja, 5, j);
		th.check(ja[5] == 123451234512345L, "ja[5] should be set");
		th.check(ja[0] == 0L && ja[1] == 0L && ja[2] == 0L && ja[3] == 0L && ja[4] == 0L && ja[6] == 0L && ja[7] == 0L && ja[8] == 0L && ja[9] == 0L, "no other element of ja[] should be affected");

		Array.set(ja, 6, b);
		th.check(ja[6] == -42L, "ja[6] should be set");
		th.check(ja[0] == 0L && ja[1] == 0L && ja[2] == 0L && ja[3] == 0L && ja[4] == 0L && ja[7] == 0L && ja[8] == 0L && ja[9] == 0L, "no other element of ja[] should be affected");

		Array.set(ja, 7, s);
		th.check(ja[7] == -12345L, "ja[7] should be set");
		th.check(ja[0] == 0L && ja[1] == 0L && ja[2] == 0L && ja[3] == 0L && ja[4] == 0L && ja[8] == 0L && ja[9] == 0L, "no other element of ja[] should be affected");

		Array.set(ja, 8, c);
		th.check(ja[8] == 64L, "ja[8] should be set");
		th.check(ja[0] == 0L && ja[1] == 0L && ja[2] == 0L && ja[3] == 0L && ja[4] == 0L && ja[9] == 0L, "no other element of ja[] should be affected");

		Array.set(ja, 9, i);
		th.check(ja[9] == -54321L, "ja[9] should be set");
		th.check(ja[0] == 0L && ja[1] == 0L && ja[2] == 0L && ja[3] == 0L && ja[4] == 0L, "no other element of ja[] should be affected");

		try {
			Array.set(ja, 1, z);
			th.fail("should throw an IllegalArgumentException -- 33");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ja, 1, f);
			th.fail("should throw an IllegalArgumentException -- 34");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(ja, 1, d);
			th.fail("should throw an IllegalArgumentException -- 35");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(fa, 1, f);
			th.fail("should throw a NullPointerException -- 14");
		    }
		catch (NullPointerException ae) { th.check(true); }

		fa = new float[10];
		f = null;

		try {	Array.set(fa , 1, f);
			th.fail("should throw a NullPointerException -- 15");
		    }
		catch (NullPointerException ae) { th.check(true); }

		f = new Float(12.34F);
		try {	Array.set(fa , 10, f);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 16");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(fa , -1, f);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 17");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(fa, 5, f);
		th.check(fa[5] == 12.34F, "fa[5] should be set");
		th.check(fa[0] == 0.0F && fa[1] == 0.0F && fa[2] == 0.0F && fa[3] == 0.0F && fa[4] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Array.set(fa, 4, b);
		th.check(fa[4] == -42F, "fa[4] should be set");
		th.check(fa[0] == 0.0F && fa[1] == 0.0F && fa[2] == 0.0F && fa[3] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Array.set(fa, 3, s);
		th.check(fa[3] == -12345.0F, "fa[3] should be set");
		th.check(fa[0] == 0.0F && fa[1] == 0.0F && fa[2] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Array.set(fa, 2, c);
		th.check(fa[2] == 64.0F, "fa[2] should be set");
		th.check(fa[0] == 0.0F && fa[1] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Array.set(fa, 1, i);
		th.check(fa[1] == -54321.0F, "fa[1] should be set");
		th.check(fa[0] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Array.set(fa, 0, j);
		th.check(fa[0] > 123451000000000.0F && fa[0] < 123452000000000.0F, "fa[0] should be set");
		th.check(fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		try {
			Array.set(fa, 1, z);
			th.fail("should throw an IllegalArgumentException -- 36");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(fa, 1, d);
			th.fail("should throw an IllegalArgumentException -- 37");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.set(da, 1, d);
			th.fail("should throw a NullPointerException -- 16");
		    }
		catch (NullPointerException ae) { th.check(true); }

		da = new double[10];
		d = null;

		try {	Array.set(da , 1, d);
			th.fail("should throw a NullPointerException -- 17");
		    }
		catch (NullPointerException ae) { th.check(true); }

		d = new Double(56.789D);
		try {	Array.set(da , 10, d);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 18");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.set(da , -1, d);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 19");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.set(da, 5, d);
		th.check(da[5] == 56.789D, "da[5] should be set");
		th.check(da[0] == 0.0D && da[1] == 0.0D && da[2] == 0.0D && da[3] == 0.0D && da[4] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Array.set(da, 4, b);
		th.check(da[4] == -42D, "da[4] should be set");
		th.check(da[0] == 0.0D && da[1] == 0.0D && da[2] == 0.0D && da[3] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Array.set(da, 3, s);
		th.check(da[3] == -12345.0D, "da[3] should be set");
		th.check(da[0] == 0.0D && da[1] == 0.0D && da[2] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Array.set(da, 2, c);
		th.check(da[2] == 64.0D, "da[2] should be set");
		th.check(da[0] == 0.0D && da[1] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Array.set(da, 1, i);
		th.check(da[1] == -54321.0D, "da[1] should be set");
		th.check(da[0] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Array.set(da, 0, j);
		th.check(da[0] == 123451234512345.0D, "da[0] should be set");
		th.check(da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		try {
			Array.set(fa, 1, z);
			th.fail("should throw an IllegalArgumentException -- 38");
		}
		catch (IllegalArgumentException ae) { th.check(true); }
	}
/**
* implemented.
*
*/
	protected void test_setBoolean()
	{
		th.checkPoint("setBoolean(java.lang.Object,int,boolean)void");

		boolean[] za = null;

		try {
			Array.setBoolean(za, 1, true);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		za = new boolean[10];

		try {	Array.setBoolean(za , 10, false);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setBoolean(za , -1, true);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setBoolean(za, 5, true);
		th.check(za[5], "za[5] should be set");
		th.check(za[0] == false && za[1] == false && za[2] == false && za[3] == false && za[4] == false && za[6] == false && za[7] == false && za[8] == false && za[9] == false, "no other element of za[] should be affected");
		Array.setBoolean(za, 5, false);
		th.check(!za[5], "za[5] should be reset");

		Object[] oa = new Object[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		char[] ca = new char[10];
		int[] ia = new int[10];
		float[] fa = new float[10];
		double[] da = new double[10];
		long[] ja = new long[10];

		try {
			Array.setBoolean(oa, 0, true);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(ba, 1, true);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(sa, 2, true);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(ca, 3, true);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(ia, 4, true);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(ja, 5, true);
			th.fail("should throw an IllegalArgumentException -- 6");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(fa, 7, true);
			th.fail("should throw an IllegalArgumentException -- 7");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setBoolean(da, 8, true);
			th.fail("should throw an IllegalArgumentException -- 8");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

	}
/**
* implemented.
*
*/
	protected void test_setByte()
	{
		th.checkPoint("setByte(java.lang.Object,int,byte)void");

		byte[] ba = null;

		try {
			Array.setByte(ba, 1, (byte)26);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ba = new byte[10];

		try {	Array.setByte(ba , 10, (byte)-14);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setByte(ba , -1, (byte)-123);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setByte(ba, 5, (byte)88);
		th.check(ba[5] == 88, "ba[5] should be set");
		th.check(ba[0] == 0 && ba[1] == 0 && ba[2] == 0 && ba[3] == 0 && ba[4] == 0 && ba[6] == 0 && ba[7] == 0 && ba[8] == 0 && ba[9] == 0, "no other element of ba[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		char[] ca = new char[10];
		short[] sa = new short[10];
		int[] ia = new int[10];
		float[] fa = new float[10];
		double[] da = new double[10];
		long[] ja = new long[10];

		try {
			Array.setByte(oa, 0, (byte)-8);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setByte(za, 1, (byte)79);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setByte(ca, 1, (byte)79);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setByte(ia, 4, (byte)126);
		th.check(ia[4] == 126, "ia[4] should be set");

		Array.setByte(ja, 7, (byte)35);
		th.check(ja[7] == 35L, "ja[7] should be set");

		Array.setByte(fa, 9, (byte)-54);
		th.check(fa[9] == -54.0F, "fa[9] should be set");

		Array.setByte(da, 0, (byte)97);
		th.check(da[0] == 97.0D, "da[0] should be set");
	}

/**
* implemented.
*
*/
	protected void test_setChar()
	{
		th.checkPoint("setChar(java.lang.Object,int,char)void");

		char[] ca = null;

		try {
			Array.setChar(ca, 1, 't');
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ca = new char[10];

		try {	Array.setChar(ca , 10, '/');
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setChar(ca , -1, 'X');
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setChar(ca, 5, 'Q');
		th.check(ca[5] == 'Q', "ca[5] should be set");
		th.check(ca[0] == 0 && ca[1] == 0 && ca[2] == 0 && ca[3] == 0 && ca[4] == 0 && ca[6] == 0 && ca[7] == 0 && ca[8] == 0 && ca[9] == 0, "no other element of ca[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		int[] ia = new int[10];
		float[] fa = new float[10];
		double[] da = new double[10];
		long[] ja = new long[10];

		try {
			Array.setChar(oa, 0, 'z');
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setChar(za, 1, '?');
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setChar(sa, 1, '?');
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setChar(ia, 4, '~');
		th.check(ia[4] == 126, "ia[4] should be set");

		Array.setChar(ja, 7, (char)65535);
		th.check(ja[7] == 65535L, "ja[7] should be set");

		Array.setChar(fa, 9, '0');
		th.check(fa[9] == 48.0F, "fa[9] should be set");

		Array.setChar(da, 0, 'a');
		th.check(da[0] == 97.0D, "da[0] should be set");
	}

/**
* implemented.
*
*/
	protected void test_setDouble()
	{
		th.checkPoint("setDouble(java.lang.Object,int,double)void");

		double[] da = null;

		try {
			Array.setDouble(da, 1, 96.87D);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		da = new double[10];

		try {	Array.setDouble(da , 10, -0.123D);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setDouble(da , -1, -8.7D);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setDouble(da, 5, 0.00000044D);
		th.check(da[5] == 0.00000044D, "da[5] should be set");
		th.check(da[0] == 0.0D && da[1] == 0.0D && da[2] == 0.0D && da[3] == 0.0D && da[4] == 0.0D && da[6] == 0.0D && da[7] == 0.0D && da[8] == 0.0D && da[9] == 0.0D, "no other element of da[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		char[] ca = new char[10];
		int[] ia = new int[10];
		float[] fa = new float[10];
		long[] ja = new long[10];

		try {
			Array.setDouble(oa, 0, -42.0D);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(za, 1, 79.0D);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(ba, 2, 0.79D);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(sa, 3, -34.0D);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(ca, 1, 79.0D);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(ia, 1, 12379.0D);
			th.fail("should throw an IllegalArgumentException -- 6");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(ja, 1, -789.0D);
			th.fail("should throw an IllegalArgumentException -- 7");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setDouble(fa, 5, 64.0D);
			th.fail("should throw an IllegalArgumentException -- 8");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

	}

/**
* implemented.
*
*/
	protected void test_setFloat()
	{
		th.checkPoint("setFloat(java.lang.Object,int,float)void");

		float[] fa = null;

		try {
			Array.setFloat(fa, 1, 96.87F);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		fa = new float[10];

		try {	Array.setFloat(fa , 10, -0.123F);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setFloat(fa , -1, -8.7F);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setFloat(fa, 5, 0.00000044F);
		th.check(fa[5] == 0.00000044F, "fa[5] should be set");
		th.check(fa[0] == 0.0F && fa[1] == 0.0F && fa[2] == 0.0F && fa[3] == 0.0F && fa[4] == 0.0F && fa[6] == 0.0F && fa[7] == 0.0F && fa[8] == 0.0F && fa[9] == 0.0F, "no other element of fa[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		char[] ca = new char[10];
		int[] ia = new int[10];
		long[] ja = new long[10];
		double[] da = new double[10];

		try {
			Array.setFloat(oa, 0, -42.0F);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(za, 1, 79.0F);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(ba, 2, 0.79F);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(sa, 3, -34.0F);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(ca, 1, 79.0F);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(ia, 1, 12379.0F);
			th.fail("should throw an IllegalArgumentException -- 6");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setFloat(ja, 1, -789.0F);
			th.fail("should throw an IllegalArgumentException -- 7");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setFloat(da, 0, 97.0F);
		th.check(da[0] == 97.0D, "da[0] should be set");
	}
/**
* implemented.
*
*/
	protected void test_setInt()
	{
		th.checkPoint("setInt(java.lang.Object,int,int)void");

		int[] ia = null;

		try {
			Array.setInt(ia, 1, 96);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		ia = new int[10];

		try {	Array.setInt(ia , 10, 123);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setInt(ia , -1, -87);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setInt(ia, 5, 44);
		th.check(ia[5] == 44, "ia[5] should be set");
		th.check(ia[0] == 0 && ia[1] == 0 && ia[2] == 0 && ia[3] == 0 && ia[4] == 0 && ia[6] == 0 && ia[7] == 0 && ia[8] == 0 && ia[9] == 0, "no other element of ia[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		char[] ca = new char[10];
		long[] ja = new long[10];
		float[] fa = new float[10];
		double[] da = new double[10];

		try {
			Array.setInt(oa, 0, -42);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setInt(za, 1, 79);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setInt(ba, 2, 79);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setInt(sa, 3, -34);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setInt(ca, 1, 79);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setInt(ja, 0, -20000);
		th.check(ja[0] == -20000L, "ja[0] should be set");

		Array.setInt(fa, 1, 12332);
		th.check(fa[1] == 12332.0F, "fa[1] should be set");

		Array.setInt(da, 2, 97);
		th.check(da[2] == 97.0D, "da[2] should be set");
	}
/**
* implemented.
*
*/
	protected void test_setLong()
	{
		th.checkPoint("setLong(java.lang.Object,int,long)void");

		long[] la = null;

		try {
			Array.setLong(la, 1, 9687L);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		la = new long[10];

		try {	Array.setLong(la , 10, 123L);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setLong(la , -1, -87L);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setLong(la, 5, 44L);
		th.check(la[5] == 44L, "la[5] should be set");
		th.check(la[0] == 0 && la[1] == 0 && la[2] == 0 && la[3] == 0 && la[4] == 0 && la[6] == 0 && la[7] == 0 && la[8] == 0 && la[9] == 0, "no other element of la[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		short[] sa = new short[10];
		char[] ca = new char[10];
		int[] ia = new int[10];
		float[] fa = new float[10];
		double[] da = new double[10];

		try {
			Array.setLong(oa, 0, -42L);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setLong(za, 1, 79L);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setLong(ba, 2, 79L);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setLong(sa, 3, -34);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setLong(ca, 1, 79);
			th.fail("should throw an IllegalArgumentException -- 5");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setLong(ia, 1, -1279);
			th.fail("should throw an IllegalArgumentException -- 6");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setLong(fa, 1, 12332L);
		th.check(fa[1] == 12332.0F, "fa[1] should be set");

		Array.setLong(da, 2, 9765310L);
		th.check(da[2] == 9765310.0D, "da[2] should be set");
	}
/**
* implemented.
*
*/
	protected void test_setShort()
	{
		th.checkPoint("setShort(java.lang.Object,int,short)void");

		short[] sa = null;

		try {
			Array.setShort(sa, 1, (short)9687);
			th.fail("should throw a NullPointerException -- 1");
		    }
		catch (NullPointerException ae) { th.check(true); }

		sa = new short[10];

		try {	Array.setShort(sa , 10, (short)123);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 1");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		try {	Array.setShort(sa , -1, (short)-87);
			th.fail("should throw an ArrayIndexOutOfBoundsException -- 2");
		    }
		catch (ArrayIndexOutOfBoundsException ae) { th.check(true); }

		Array.setShort(sa, 5, (short)44);
		th.check(sa[5] == 44, "sa[5] should be set");
		th.check(sa[0] == 0 && sa[1] == 0 && sa[2] == 0 && sa[3] == 0 && sa[4] == 0 && sa[6] == 0 && sa[7] == 0 && sa[8] == 0 && sa[9] == 0, "no other element of sa[] should be affected");

		Object[] oa = new Object[10];
		boolean[] za = new boolean[10];
		byte[] ba = new byte[10];
		char[] ca = new char[10];
		int[] ia = new int[10];
		long[] ja = new long[10];
		long[] la = new long[10];
		float[] fa = new float[10];
		double[] da = new double[10];

		try {
			Array.setShort(oa, 0, (short)-42);
			th.fail("should throw an IllegalArgumentException -- 1");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setShort(za, 1, (short)79);
			th.fail("should throw an IllegalArgumentException -- 2");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setShort(ba, 2, (short)79);
			th.fail("should throw an IllegalArgumentException -- 3");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		try {
			Array.setShort(ca, 1, (short)79);
			th.fail("should throw an IllegalArgumentException -- 4");
		}
		catch (IllegalArgumentException ae) { th.check(true); }

		Array.setShort(ia, 1, (short)-123);
		th.check(ia[1] == -123, "ia[1] should be set");

		Array.setShort(ja, 1, (short)-123);
		th.check(ja[1] == -123, "ja[1] should be set");

		Array.setShort(fa, 1, (short)123);
		th.check(fa[1] == 123.0F, "fa[1] should be set");

		Array.setShort(da, 2, (short)5310);
		th.check(da[2] == 5310.0D, "da[2] should be set");
	}

}
