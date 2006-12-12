/*************************************************************************
/* SimpleRead.java -- CharArrayReader simple read test.
/*
/* Copyright (c) 1998 Free Software Foundation, Inc.
/* Written by Aaron M. Renn (arenn@urbanophile.com)
/*
/* This program is free software; you can redistribute it and/or modify
/* it under the terms of the GNU General Public License as published 
/* by the Free Software Foundation, either version 2 of the License, or
/* (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful, but
/* WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU General Public License for more details.
/*
/* You should have received a copy of the GNU General Public License
/* along with this program; if not, write to the Free Software Foundation
/* Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
/*************************************************************************/

// Tags: JDK1.1

package gnu.testlet.wonka.io.CharArrayReader;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.io.*;

public class SimpleRead implements Testlet
{

public void
test(TestHarness harness)
{
  String str = "In junior high, I did a lot of writing.  I wrote a science\n" +
     "fiction novel length story that was called 'The Destruction of\n" +
     "Planet Earth'.  All the characters in the story were my friends \n" +
     "from school because I couldn't think up any cool names.\n";

  char[] str_chars = new char[str.length()];
  str.getChars(0, str.length(), str_chars, 0);
  char[] read_buf = new char[12];

  CharArrayReader car = new CharArrayReader(str_chars);

  try
    {
      int chars_read, total_read = 0;
      while ((chars_read = car.read(read_buf, 0, read_buf.length)) != -1)
        {
          harness.debug(new String(read_buf, 0, chars_read), false);
          total_read += chars_read;
        }

      harness.check(total_read, str.length(), "total_read");
    }
  catch (IOException e)
    {
      harness.debug(e);
      harness.check(false);
    }
}

} // SimpleRead

