// Test DecimalFormat.toPattern.

// Copyright (c) 1999  Cygnus Solutions
// Written by Tom Tromey <tromey@cygnus.com>

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
// Boston, MA 02111-1307, USA.

// Tags: JDK1.1

package gnu.testlet.wonka.text.DecimalFormat;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class topattern implements Testlet
{
  public void test (TestHarness harness)
    {
      // Just to be explicit: we're only testing the US locale here.
      Locale loc = Locale.US;
      Locale.setDefault (loc);

      // There aren't really many tests we can do, since it doesn't
      // seem like any canonical output format is documented.

      DecimalFormat df = new DecimalFormat ("0.##");
      harness.check (df.toPattern (), "0.##");
      harness.check (df.toLocalizedPattern (), "0.##");

      DecimalFormatSymbols dfs = df.getDecimalFormatSymbols ();
      dfs.setDecimalSeparator (',');
      dfs.setZeroDigit ('1');
      dfs.setDigit ('X');
      dfs.setGroupingSeparator ('!');
      harness.check (df.toLocalizedPattern (), "1,XX");

      df.applyPattern ("Fr #,##0.##");
      String x1 = df.toPattern ();
      String x2 = df.toLocalizedPattern ();
      harness.check (x1.length (), x2.length ());
      boolean ok = x1.length () == x2.length ();
      for (int i = 0; i < x1.length (); ++i)
	{
	  char c = x1.charAt(i);
	  if (c == '0')
	    c = '1';
	  else if (c == '#')
	    c = 'X';
	  else if (c == '.')
	    c = ',';
	  else if (c == ',')
	    c = '!';
	  if (c != x2.charAt (i))
	    {
	      ok = false;
	      harness.debug ("failure at char " + i);
	      harness.debug ("x1 = " + x1 + "\nx2 = " + x2);
	      break;
	    }
	}
      harness.check (ok);
    }
}
