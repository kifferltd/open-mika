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

package gnu.testlet.wonka.lang.System;

import java.util.*;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class arraycopy implements Testlet
{
  public void fill (int[] a)
  {
    for (int i = 0; i < a.length; ++i)
      a[i] = i;
  }

  public void check (TestHarness harness, int[] expect, int[] result)
    {
      boolean ok = expect.length == result.length;
      for (int i = 0; ok && i < expect.length; ++i)
	if (expect[i] != result[i])
	  ok = false;
      harness.check (ok);
    }

  public Object copy (Object from, int a, Object to, int b, int c)
    {
      try
	{
	  System.arraycopy (from, a, to, b, c);
	}
      catch (ArrayStoreException xa)
	{
	  return "caught ArrayStoreException";
	}
      catch (IndexOutOfBoundsException xb)
	{
	  return "caught IndexOutOfBoundsException";
	}
      catch (NullPointerException xc)
	{
	  return "caught NullPointerException";
	}
      catch (Throwable xd)
	{
	  return "caught unexpected exception";
	}

      return null;
    }

  public void test (TestHarness harness)
    {
      harness.setclass("java.lang.System");
      harness.checkPoint("arraycopy(java.lang.Object,int,java.lang.Object,int,int)void");
      int[] x, y;

      x = new int[5];
      y = new int[5];
      fill (x);

      harness.check (copy (x, 0, y, 0, x.length), null);
      int[] one = { 0, 1, 2, 3, 4 };
      check (harness, y, one);

      harness.check (copy (x, 1, y, 0, x.length - 1), null);
      harness.check (copy (x, 0, y, x.length - 1, 1), null);
      int[] two = { 1, 2, 3, 4, 0 };
      check (harness, y, two);

      Object[] z = new Object[5];
      harness.check (copy (x, 0, z, 0, x.length),
		     "caught ArrayStoreException");

      harness.check (copy (x, 0, y, 0, -23),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (null, 0, y, 0, -23),
		     "caught NullPointerException");

      harness.check (copy (x, 0, null, 0, -23),
		     "caught NullPointerException");

      String q = "metonymy";
      harness.check (copy (q, 0, y, 0, 19),
		     "caught ArrayStoreException");

      harness.check (copy (x, 0, q, 0, 19),
		     "caught ArrayStoreException");

      double[] v = new double[5];
      harness.check (copy (x, 0, v, 0, 5),
		     "caught ArrayStoreException");

      harness.check (copy (x, -1, y, 0, 1),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (x, 0, z, 0, x.length),
		     "caught ArrayStoreException");

      harness.check (copy (x, 0, y, -1, 1),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (x, 3, y, 0, 5),
		     "caught IndexOutOfBoundsException");

      harness.check (copy (x, 0, y, 3, 5),
		     "caught IndexOutOfBoundsException");
      // Regression test for missing check in libgcj.
      harness.check (copy (x, 4, y, 4, Integer.MAX_VALUE),
		     "caught IndexOutOfBoundsException");


      Object[] w = new Object[5];
      String[] ss = new String[5];
      for (int i = 0; i < 5; ++i)
	{
	  w[i] = i + "";
	  ss[i] = (i + 23) + "";
	}
      w[3] = new Integer (23);

      harness.check (copy (w, 0, ss, 0, 5),
		     "caught ArrayStoreException");
      harness.check (ss[0], "0");
      harness.check (ss[1], "1");
      harness.check (ss[2], "2");
      harness.check (ss[3], "26");
      harness.check (ss[4], "27");

      TestHarness th = harness;

      char [] chararray = new char[1];
      chararray[0] = 'a';
      Vector [] va = new Vector[5];
      List [] l = new List[5];
      ss = new String[5];
      w = new Object[5];
      for (int i = 0; i < 5; ++i)
	{
	  va[i] = new Vector();
	  l[i] = new ArrayList();
	  w[i] = i + "";
	  ss[i] = (i + 23) + "";
	}
      try {
          System.arraycopy ( null ,0 ,null ,0,1);
          harness.fail("where is the NullPointerException -- 1");
          }
      catch (NullPointerException ne) { th.check(true , "nr 1");}
      catch(Exception e) { th.fail("got wrong exception -- 1, got:"+e);}
      try {
          System.arraycopy ( l ,0 ,null ,0,1);
          harness.fail("where is the NullPointerException -- 2");
          }
      catch (NullPointerException ne) { th.check(true , "nr 2");}
      catch(Exception e) { th.fail("got wrong exception -- 2, got:"+e);}
      try {
          System.arraycopy ( null ,0 ,l ,0,1);
          harness.fail("where is the NullPointerException -- 3");
          }
      catch (NullPointerException ne) { th.check(true , "nr 3");}
      catch(Exception e) { th.fail("got wrong exception -- 3, got:"+e);}
      Object noarray = new Object();
      try {
          System.arraycopy (noarray,0 ,l ,0,1);
          harness.fail("where is the ArrayStoreException -- 4");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 4");}
      catch(Exception e) { th.fail("got wrong exception -- 4, got:"+e);}
      try {
          System.arraycopy (l,0,noarray,0,1);
          harness.fail("where is the ArrayStoreException -- 5");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 5");}
      catch(Exception e) { th.fail("got wrong exception -- 5, got:"+e);}
      try {
          System.arraycopy (x,0,chararray,0,1);
          harness.fail("where is the ArrayStoreException -- 6");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 6");}
      catch(Exception e) { th.fail("got wrong exception -- 6, got:"+e);}
      try {
          System.arraycopy (x,0,l,0,1);
          harness.fail("where is the ArrayStoreException -- 7");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 7");}
      catch(Exception e) { th.fail("got wrong exception -- 7, got:"+e);}
      try {
          System.arraycopy (l,0,x,0,1);
          harness.fail("where is the ArrayStoreException -- 8");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 8");}
      catch(Exception e) { th.fail("got wrong exception -- 8, got:"+e);}
      try {
          System.arraycopy (l,0,va,0,-1);
          harness.fail("where is the IndexOutOfBoundsException -- 9");
          }
      catch (IndexOutOfBoundsException ne) { th.check(true , "nr 9");}
      catch(Exception e) { th.fail("got wrong exception -- 9, got:"+e);}
      try {
          System.arraycopy (l,0,va,-1,1);
          harness.fail("where is the IndexOutOfBoundsException -- 10");
          }
      catch (IndexOutOfBoundsException ne) { th.check(true , "nr 10");}
      catch(Exception e) { th.fail("got wrong exception -- 10, got:"+e);}
      try {
          System.arraycopy (l,-1,va,1,1);
          harness.fail("where is the IndexOutOfBoundsException -- 11");
          }
      catch (IndexOutOfBoundsException ne) { th.check(true , "nr 11");}
      catch(Exception e) { th.fail("got wrong exception -- 11, got:"+e);}
      try {
          System.arraycopy (l,1,va,0,5);
          harness.fail("where is the IndexOutOfBoundsException -- 12");
          }
      catch (IndexOutOfBoundsException ne) { th.check(true , "nr 12");}
      catch(Exception e) { th.fail("got wrong exception -- 12, got:"+e);}
      try {
          System.arraycopy (l,0,va,1,5);
          harness.fail("where is the IndexOutOfBoundsException -- 13");
          }
      catch (IndexOutOfBoundsException ne) { th.check(true , "nr 13");}
      catch(Exception e) { th.fail("got wrong exception -- 13, got:"+e);}
      try {
          System.arraycopy (l,0,va,0,5);
          harness.fail("where is the ArrayStoreException -- 14");
          }
      catch (ArrayStoreException ne) { th.check(true , "nr 14");}
      catch(Exception e) { th.fail("got wrong exception -- 14, got:"+e);}
      try {
          System.arraycopy (va,0,l,0,5);
          th.check(true , "nr 15");
          }
      catch(Exception e) { th.fail("got wrong exception -- 15, got:"+e);}
      try {
          System.arraycopy (w,0,ss,0,5);
          th.check(true , "nr 16");
          harness.check (ss[0].equals("0"),"checking value -1- 0");
          harness.check (ss[1].equals("1"),"checking value -1- 1");
      	  harness.check (ss[2].equals("2"),"checking value -1- 2");
          harness.check (ss[3].equals("3"),"checking value -1- 3");
          harness.check (ss[4].equals("4"),"checking value -1- 4");
          }
      catch(Exception e) { th.fail("got wrong exception -- 16, got:"+e);}
      try {
          w[3] = null; w[0]="a";w[1]="b";w[2]="c";w[4]="e";
          System.arraycopy (w,0,ss,0,5);
          th.check(true , "nr 16");
          }
      catch(Exception e) { th.fail("got wrong exception -- 16, got:"+e);}
      harness.check (ss[0].equals("a"),"checking value -2- 0");
      harness.check (ss[1].equals("b"),"checking value -2- 1");
      harness.check (ss[2].equals("c"),"checking value -2- 2");
      harness.check (ss[3] == null,"checking value -2- 3");
      harness.check (ss[4].equals("e"),"checking value -2- 4");



    }
}
