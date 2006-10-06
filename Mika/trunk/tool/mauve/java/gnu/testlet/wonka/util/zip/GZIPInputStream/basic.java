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

package gnu.testlet.wonka.util.zip.GZIPInputStream;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
        System.out.println("got exception while unzipping"+_);
        _.printStackTrace();
      }
    return sb.toString ();
  }

  public void test (TestHarness harness)
  {
    // First read the uncompressed file.
    harness.checkPoint ("reading gzip file");

    String plain = "";
    GZIPInputStream gzin;
    InputStream is = null;
    try
      {
	is = new FileInputStream("/test/reference.data");
	plain = readall (is);
      } catch (Exception e){
        return;
      }
     try {  
	gzin = new GZIPInputStream (new FileInputStream("/test/reference.gz"));
	String uncompressed = readall (gzin);

	harness.check (plain, uncompressed);
      }
    catch (IOException _2)
      {
	harness.check (false);
      }

    // Now compress some data into a buffer and then re-read it.
    harness.checkPoint ("compressing and re-reading");
    if (is == null)
      harness.check (false);
    else
      {
	try
	  {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream ();
	    GZIPOutputStream gzout = new GZIPOutputStream (bout);
	    gzout.write (plain.getBytes ("8859_1"));
	    gzout.close ();
	    gzin = new GZIPInputStream (new ByteArrayInputStream (bout.toByteArray()));
	    String full = readall (gzin);

	    harness.check (plain, full);
	  }
	catch (UnsupportedEncodingException _1)
	  {
	    harness.check (false);
	  }
	catch (IOException _2)
	  {
	    harness.check (false);
	  }
      }
  }
}
