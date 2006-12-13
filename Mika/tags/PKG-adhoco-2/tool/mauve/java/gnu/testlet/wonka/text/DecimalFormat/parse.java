// Test simple forms of DecimalFormat.parse.

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
import java.text.ParsePosition;
import java.util.Locale;

public class parse implements Testlet
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

  public Number parseIt (DecimalFormat df, String string, ParsePosition pos)
    {
      pos.setIndex (0);
      return df.parse (string, pos);
    }

  public void test (TestHarness harness)
    {
      // Just to be explicit: we're only testing the US locale here.
      Locale loc = Locale.US;
      Locale.setDefault (loc);

      Number num;
      ParsePosition pp = new ParsePosition (0);

      // Some tests taken from JCL book.
      DecimalFormat df = new DecimalFormat ("0.##;-0.##");
      num = parseIt (df, "-1234.56", pp);
      harness.check (num instanceof Double);
      harness.check (num.doubleValue(), -1234.56);

      apply (harness, df, "0.#");
      num = parseIt (df, "1234.6", pp);
      harness.check (num instanceof Double);
      harness.check (num.doubleValue(), 1234.6);

      apply (harness, df, "0");
      num = parseIt (df, "-1235", pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue (), -1235);

      num = parseIt (df, Long.toString (Long.MIN_VALUE), pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue(), Long.MIN_VALUE);

      apply (harness, df, "'#'#.#");
      num = parseIt (df, "#30", pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue (), 30);

      num = parseIt (df, "xx30", pp);
      harness.check (num, null);

      apply (harness, df, "0.0000E#");
      num = parseIt (df, "2.0001E+5", pp);
      harness.check (num instanceof Double);
      harness.check (num.longValue (), 200000);

      apply (harness, df, "###0.#;(###0.#)");
      num = parseIt (df, "201.2", pp);
      harness.check (num instanceof Double);
      harness.check (num.doubleValue(), 201.2);
      num = parseIt (df, "(201.2)", pp);
      harness.check (num instanceof Double);
      harness.check (num.doubleValue(), -201.2);

      apply (harness, df, "0.#;0.#-");
      num = parseIt (df, "303", pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue (), 303);
      num = parseIt (df, "303-", pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue (), -303);

      num = parseIt (df, "9223372036854775808-", pp);
      harness.check (num instanceof Long);
      harness.check (num.longValue(), Long.MIN_VALUE);
    }
}
