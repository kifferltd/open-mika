/* Copyright (C) 1999 Hewlett-Packard Company

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


// Edited on Thu Aug 24

package gnu.testlet.wonka.lang.Cloneable;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class CloneableTest	 implements Testlet,  Cloneable
{
	int   a = 20;
	char  b = 'b';
	Float c = new Float( 10.0f );
	
  protected TestHarness harness;
	public  void test_clone()
	{
			CloneableTest tst = null;
		try {
			tst = (CloneableTest)clone();
			harness.check(true);  
		}
		catch ( CloneNotSupportedException e )
		{
			harness.fail("$$java.lang.Cloneable$$Error: CloneNotSupportedException should not be thrown here");
		}

		if ( tst == null )
			harness.fail("Error: Clone method on Object did not work properly");
		else
		{
			harness.check(true);
			if (!( tst.a == a && tst.b == b && tst.c.floatValue() == c.floatValue()))
			{
				harness.fail("Error: Clone method on Object did not clone data properly");
			}
			else {harness.check(true);}
		}	
// extra test added here
			
		class tstc 
		{
			public Object clone() throws CloneNotSupportedException

			{
				return super.clone();
			}
		}		

		try 	{
			tstc bln=new tstc();
			bln.clone();
			harness.fail("Error: CloneNotSupportedException should be thrown here");
			}
	
		catch ( CloneNotSupportedException e ) {harness.check(true);}
	
	
	}


		
// end code


	public void test_array()
	{
		int []ia = new int[5];
		int i;

		for (i = 0; i < ia.length; i++) {
			ia[i] = i;
		}
		Cloneable c;
		Object o = ia;

		if (!(ia instanceof Cloneable)) {
			harness.fail("Error: arrays should implement Cloneable");
		}
		else { harness.check(true);}
	
		int []ib = (int[])ia.clone();
		Class cla = ia.getClass();
		Class clb = ib.getClass();
		if (cla != clb) {
			harness.fail("Error: array classes should be equal");
		}
		else { harness.check(true);}
	
		for (i = 0; i < ia.length; i++) {
			if (ib[i] != ia[i]) {
				harness.fail("Error: mismatch on cloned array at " + i);
			}
			else { harness.check(true);}
		}

	}

	public void testall()
	{
		test_clone();
		test_array();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

	
}
