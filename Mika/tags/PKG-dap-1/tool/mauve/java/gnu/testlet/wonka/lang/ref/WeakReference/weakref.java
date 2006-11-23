// Test of WeakReference

// Copyright (C) 2001 Red Hat, Inc.

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

// Tags: JDK1.2

// In this test we make some assumptions about how the GC operates
// that are probably not quite sound.  In particular we assume
// System.gc() will collect everything.

package gnu.testlet.wonka.lang.ref.WeakReference;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.ref.*;

public class weakref implements Testlet
{
  public WeakReference genRef (ReferenceQueue q, Object o)
  {
    return new WeakReference (o, q);
  }

  public WeakReference try1 (ReferenceQueue q, TestHarness harness)
  {
    Integer twt = new Integer (23);
    WeakReference wr = genRef (q, twt);

    System.gc ();

    Reference r = q.poll ();
    harness.check (r, null, "live reference");
    harness.check (wr.get (), twt);

    // Must keep the WeakReference live.
    return wr;
  }

  public void test (TestHarness harness)
  {
    ReferenceQueue q = new ReferenceQueue ();

    WeakReference wr = try1 (q, harness);
    System.gc ();

    Reference r = null;
    try
      {
	r = q.remove (5 * 1000); // 5 seconds.
      }
    catch (InterruptedException _)
      {
	harness.debug (_);
      }

    harness.check (r, wr, "unreachable");
    harness.check (wr.get (), null, "contents of weak reference");
  }
}
