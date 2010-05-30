// Tags: JDK1.1

// Copyright (C) 1999 Cygnus Solutions

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

package gnu.testlet.wonka.util.zip.ZipInputStream;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class basic implements Testlet
{
  private String readall (InputStream in)
  {
    StringBuffer sb = new StringBuffer ();
    byte[] buf = new byte[512];
    int n;
    try
      {
	while ((n = in.read(buf)) > 0)
	  sb.append(new String (buf, 0, n, "8859_1"));
      }
    catch (IOException _)
      {
      }
    return sb.toString ();
  }

  public String read_a_file (ZipInputStream zis)
  {
    try
      {
	ZipEntry ze = zis.getNextEntry();
	if (ze == null)
	  return "done";
	return readall (zis);
      }
    catch (IOException _)
      {
	return "";
      }
  }

  public void read_contents (TestHarness harness, ZipInputStream zis)
  {
    String s = read_a_file (zis);
    harness.check (s, "Contents of file 1\n");
    s = read_a_file (zis);
    harness.check (s, "Contents of file 2\n");
    s = read_a_file (zis);
    harness.check (s, "done");
  }

  public void test (TestHarness harness)
  {
    harness.checkPoint ("reading zip file");
    FileInputStream fis =null;
    try
      {
	fis = new FileInputStream("/test/reference.zip");
	read_contents (harness,
		       new ZipInputStream (fis));
      }
    catch (IOException _1)
      {
      return;
	// FIXME: all tests should fail.
      }
    try{   fis.close();}
    catch (IOException _1){}

    harness.checkPoint ("writing and re-reading");
    
  }
}
