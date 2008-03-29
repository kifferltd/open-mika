// Test simple forms of MessageFormat formatting.

// Copyright (c) 1999, 2001  Cygnus Solutions
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

package gnu.testlet.wonka.text.MessageFormat;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.text.MessageFormat;
import java.util.Locale;

public class format implements Testlet
{
  private final String checkFormat (MessageFormat mf, Object[] args,
			       StringBuffer buf)
    {
      try
	{
	  buf.setLength (0);
	  mf.format (args, buf, null);
	  return buf.toString ();
	}
      catch (IllegalArgumentException x)
	{
	  return "caught IllegalArgumentException";
	}
    }

  public void test (TestHarness harness)
    {
      harness.setclass("java.text.MessageFormat");
      harness.checkPoint("basic format tests");

      MessageFormat mf;

      // Just to be explicit: we're only testing the US locale here.
      Locale loc = Locale.US;
      Locale.setDefault (loc);

      mf = new MessageFormat ("no variables");
      mf.setLocale (loc);

      Object[] args = new Object[0];
      StringBuffer buf = new StringBuffer ();

      harness.checkPoint ("no variable format");
      harness.check (mf.format (args, buf, null) == buf);
      harness.check (buf.toString (), "no variables");
      buf.setLength (0);
      harness.check (mf.format (null, buf, null) == buf);
      harness.check (buf.toString (), "no variables");

      harness.check (MessageFormat.format ("no variables", args),
		     "no variables");

      harness.checkPoint ("quoted brace");
      mf.applyPattern ("no '{' variables");
      harness.check (checkFormat (mf, args, buf), "no { variables");
      harness.check (mf.toPattern (), "no '{' variables");

      harness.checkPoint ("one variable");
      mf.applyPattern ("the disk contains {0} files");
      args = new Object[1];
      args[0] = new Long (23);
      harness.check (checkFormat (mf, args, buf), "the disk contains 23 files");

      // Check to make sure excess args are ignored.
      args = new Object[10];
      args[0] = new Long (27);
      harness.check (checkFormat (mf, args, buf), "the disk contains 27 files");

      mf.applyPattern ("the disk contains {0,number} files");
      harness.check (checkFormat (mf, args, buf), "the disk contains 27 files");
      args[0] = "zap";
      harness.check (checkFormat (mf, args, buf),
		     "caught IllegalArgumentException");

      args[0] = new Double (.99);
      mf.applyPattern ("the disk is {0,number,percent} full");
      harness.check (checkFormat (mf, args, buf),
		     "the disk is 99% full");

      harness.checkPoint ("choice format");
      args[0] = new Long (5);
      mf.applyPattern ("There {0,choice,0#are no files|1#is one file|1<are {0, number, integer} files}.");
      harness.check (checkFormat (mf, args, buf), "There are 5 files.");

      args[0] = new Double (10.23);
      mf.applyPattern ("Got {0,number,'#'.##}");
      harness.check (checkFormat (mf, args, buf), "Got #10.23");

      // This tests for JDK compatibility.  Pointed out by libgcj
      // user.
      harness.checkPoint ("array argument");
      mf.applyPattern ("test Message = {0}");
      args = new Object[1];
      args[0] = "a jolly string";
      harness.check (mf.format ((Object) args),
		     "test Message = a jolly string");
    }
}
