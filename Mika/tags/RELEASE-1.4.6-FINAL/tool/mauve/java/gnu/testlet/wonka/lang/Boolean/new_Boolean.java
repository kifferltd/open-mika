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

package gnu.testlet.wonka.lang.Boolean;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class new_Boolean implements Testlet
{
  public void test (TestHarness harness)
    {
      Boolean a = new Boolean("true");
      Boolean b = new Boolean("TRUE");
      Boolean c = new Boolean("tRuE");
      Boolean d = new Boolean("false");
      Boolean e = new Boolean("foo");
      Boolean f = new Boolean("");
      Boolean g = new Boolean(true);
      Boolean h = new Boolean(false);

      harness.check(a.booleanValue());
      harness.check(b.booleanValue());
      harness.check(c.booleanValue());
      harness.check(! d.booleanValue());
      harness.check(! e.booleanValue());
      harness.check(! f.booleanValue());
      harness.check(g.booleanValue());
      harness.check(! h.booleanValue());
    }
}
