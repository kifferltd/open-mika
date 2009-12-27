// Tags: JDK1.1

// Copyright (C) 2002 Free Software Foundation, Inc.
// Written by Mark Wielaard (mark@klomp.org)

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

package gnu.testlet.wonka.io.FileInputStream;

import java.io.File;
import java.io.FileInputStream;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class read implements Testlet
{
  public void test (TestHarness harness)
  {
    String tmpfile = harness.getTempDirectory()
	    + File.separator + "mauve-filein.tst";
    File f = new File(tmpfile);
    f.delete();
    try
      {
        harness.check(f.createNewFile(), "Empty file created");
	harness.check(new FileInputStream(tmpfile).read(new byte[0]), 0,
			"empty byte[] read");
      }
    catch(Throwable t)
      {
	harness.fail("Empty file created or empty byte[] read");
	harness.debug(t);
      }
    finally
      {
	// Cleanup
	f.delete();
      }
  }
}

