// Tags: JDK1.2

// Copyright (C) 2004 David Gilbert <david.gilbert@object-refinery.com>

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

package gnu.testlet.wonka.lang.Double;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

/**
 * Some checks for the parseDouble() method in the {@link Double} class.
 */
public class parseDouble implements Testlet 
{

  /**
   * Runs the test using the specified harness.
   * 
   * @param harness  the test harness (<code>null</code> not permitted).
   */
  public void test(TestHarness harness)      
  {
    testRegular(harness);
    testInfinities(harness);
    testNaN(harness);
  }

  /**
   * Tests some regular values.
   * 
   * @param harness  the test harness.
   */
  public void testRegular(TestHarness harness) 
  {
    harness.check(Double.parseDouble("1.0"), 1.0);
    harness.check(Double.parseDouble("+1.0"), 1.0);
    harness.check(Double.parseDouble("-1.0"), -1.0);
    harness.check(Double.parseDouble(" 1.0 "), 1.0);
    harness.check(Double.parseDouble(" -1.0 "), -1.0);
    
    harness.check(Double.parseDouble("2."), 2.0);
    harness.check(Double.parseDouble(".3"), 0.3);
    harness.check(Double.parseDouble("1e-9"), 1e-9);
    harness.check(Double.parseDouble("1e137"), 1e137);    

    // test some bad formats
    try
    {
      /* double d = */ Double.parseDouble("");
      harness.check(false);
    }
    catch (NumberFormatException e) 
    {
      harness.check(true);
    }

    try
    {
      /* double d = */ Double.parseDouble("X");
      harness.check(false);
    }
    catch (NumberFormatException e) 
    {
      harness.check(true);
    }

    try
    {
      /* double d = */ Double.parseDouble("e");
      harness.check(false);
    }
    catch (NumberFormatException e) 
    {
      harness.check(true);
    }

    try
    {
      /* double d = */ Double.parseDouble("+ 1.0");
      harness.check(false);
    }
    catch (NumberFormatException e) 
    {
      harness.check(true);
    }

    try
    {
      /* double d = */ Double.parseDouble("- 1.0");
      harness.check(false);
    }
    catch (NumberFormatException e) 
    {
      harness.check(true);
    }       
    
    // null argument should throw NullPointerException
    try
    {
      /* double d = */ Double.parseDouble(null);
      harness.check(false);        
    }
    catch (NullPointerException e) 
    {
      harness.check(true);
    }
  }
  
  /**
   * Some checks for values that should parse to Double.POSITIVE_INFINITY
   * or Double.NEGATIVE_INFINITY.
   * 
   * @param harness  the test harness.
   */
  public void testInfinities(TestHarness harness) 
  {
    try 
    {
      harness.check(Double.parseDouble("Infinity"), Double.POSITIVE_INFINITY);
      harness.check(Double.parseDouble("+Infinity"), Double.POSITIVE_INFINITY);
      harness.check(Double.parseDouble("-Infinity"), Double.NEGATIVE_INFINITY);
      harness.check(Double.parseDouble(" +Infinity "), Double.POSITIVE_INFINITY);
      harness.check(Double.parseDouble(" -Infinity "), Double.NEGATIVE_INFINITY);
    }
    catch (Exception e) 
    {
      harness.check(false);
      harness.debug(e);
    }
    harness.check(Double.parseDouble("1e1000"), Double.POSITIVE_INFINITY);
    harness.check(Double.parseDouble("-1e1000"), Double.NEGATIVE_INFINITY);
  }
  
  /**
   * Some checks for 'NaN' values.
   * 
   * @param harness  the test harness.
   */
  public void testNaN(TestHarness harness) 
  {
    try
    {
      harness.check(Double.isNaN(Double.parseDouble("NaN")));
      harness.check(Double.isNaN(Double.parseDouble("+NaN")));
      harness.check(Double.isNaN(Double.parseDouble("-NaN")));
      harness.check(Double.isNaN(Double.parseDouble(" +NaN ")));
      harness.check(Double.isNaN(Double.parseDouble(" -NaN ")));
    }
    catch (Exception e) 
    {
      harness.check(false);
      harness.debug(e);
    }
  }
  
}

