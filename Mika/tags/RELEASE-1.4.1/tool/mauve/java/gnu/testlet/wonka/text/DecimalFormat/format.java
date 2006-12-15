// Test simple forms of DecimalFormat.format.

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
import java.util.Locale;

public class format implements Testlet
{
  public void apply (TestHarness harness, DecimalFormat df, String pattern)
    {
      harness.checkPoint("pattern " + pattern);
      boolean ok = true;
      try
	{
	  df.applyPattern(pattern);
	}
      catch (IllegalArgumentException x)
	{
	  ok = false;
	}
      harness.check (ok);
    }

  public void test (TestHarness harness)
    {
      harness.setclass("java.text.DecimalFormat");
      harness.checkPoint("format(double)java.lang.Number");
      // Just to be explicit: we're only testing the US locale here.
      Locale loc = Locale.US;
      Locale.setDefault (loc);

      // Some tests taken from JCL book.
      DecimalFormat df = new DecimalFormat ("0.##;-0.##");
      harness.check (df.format (-1234.56), "-1234.56");
      harness.check (df.format (1234.56), "1234.56");

      apply (harness, df, "0.#");
      harness.check (df.format (-1234.56), "-1234.6");
      harness.check (df.format (1234.56), "1234.6");

      apply (harness, df, "#,##0.##;-#");
      harness.check (df.format (-1234.56), "-1,234.56");
      harness.check (df.format (1234.56), "1,234.56");

      apply (harness, df, "00,000.000;-00,000.000");
      harness.check (df.format (-1234.56), "-01,234.560");
      harness.check (df.format (1234.56), "01,234.560");

      apply (harness, df, "##,###,####.");
      df.setDecimalSeparatorAlwaysShown(true);
      harness.check (df.format (-1234.56), "-1235.");
      harness.check (df.format (1234.56), "1235.");

      apply (harness, df, "0");
      harness.check (df.format (-1234.56), "-1235");
      harness.check (df.format (1234.56), "1235");

      harness.check (df.format (Long.MIN_VALUE), "-9223372036854775808");

      apply (harness, df, "###0.#;(###0.#)");
      harness.check (df.format (-1234.56), "(1234.6)");
      harness.check (df.format (1234.56), "1234.6");

      apply (harness, df, "###0.#;###0.#-");
      harness.check (df.format (-1234.56), "1234.6-");
      harness.check (df.format (1234.56), "1234.6");

      apply (harness, df, "#,##0%;-#,##0%");
      harness.check (df.format (-1234.56), "-123,456%");
      harness.check (df.format (1234.56), "123,456%");

      apply (harness, df, "#.#");
      harness.check (df.format (0.2), ".2");

      apply (harness, df, "'#'#.#");
      harness.check (df.format (30), "#30");

      // FIXME: we don't actually know the right result here, because
      // neither the JCL book nor the JDK 1.2 docs explain what should
      // happen.  The below represents how I think things ought to
      // work.  However, Sun has a different (and more confusing)
      // idea.  E.g., JDK1.1 prints "200000.0000E" in the first case.
//       apply (harness, df, "0.0000E#");
//       harness.check (df.format (200000), "2.0000E+5");

//       apply (harness, df, "00.00E00");
//       harness.check (df.format (200000), "20.00E+04");
    }
}
