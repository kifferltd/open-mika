/*
   Copyright (C) 2016 KIFFER Ltd.

   This file is part of Mauve.

   Mauve is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   Mauve is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Mauve; see the file COPYING.  If not, write to
   the Free Software Foundation, 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/

package gnu.testlet.wonka.net.URLConnection;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.net.*;
import java.io.InputStream;
import java.io.IOException; 
import java.util.zip.GZIPInputStream;

public class KifferURLConnectionTest implements Testlet
{
  protected static TestHarness harness;
  public void test_AutoGzip()
  {
    try {
      harness.checkPoint("AutoGzip - HEAD request");
      URL url = new URL("http", "kiffer.ltd.uk", "");
      int uncompressedLength = -1;
      HttpURLConnection c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("HEAD");
      c.setRequestProperty("Accept-Encoding", "identity");
      uncompressedLength = c.getContentLength();
      int rc = c.getResponseCode();
      harness.check(200, rc, "HEAD request did not return 200");
      c.disconnect();

      harness.checkPoint("AutoGzip - HEAD request");
      url = new URL("http", "kiffer.ltd.uk", "");
      int compressedLength = -1;
      c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("HEAD");
      c.setRequestProperty("Accept-Encoding", "gzip");
      compressedLength = c.getContentLength();
      rc = c.getResponseCode();
      harness.check(200, rc, "HEAD request did not return 200");
      harness.check(uncompressedLength > compressedLength, "uncompressed length should be greater than compressed");
      c.disconnect();

      harness.checkPoint("AutoGzip - GET request using autogzip");
      c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("GET");
      rc = c.getResponseCode();
      harness.check(200, rc, "GET request did not return 200");
      harness.check(compressedLength, c.getContentLength(), "Content-Length should be compressed length");
      InputStream is = c.getInputStream();
      harness.check("gzip".equalsIgnoreCase(c.getContentEncoding()), "received input is not zipped");
      byte[] content1 = new byte[compressedLength * 10];
      int readlen = is.read(content1);
      harness.check(uncompressedLength, readlen, "length of content should be uncompressed length");
      c.disconnect();

      harness.checkPoint("AutoGzip - GET request using explicit gzip");
      c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("GET");
      c.setRequestProperty("Accept-Encoding", "gzip");
      rc = c.getResponseCode();
      harness.check(200, rc, "GET request with Accept-Encoding: gzip did not return 200");
      harness.check(compressedLength, c.getContentLength(), "Content-Length returned by GET with Accept-Encoding: gzip different to Content-Length returned by HEAD");
      is = c.getInputStream();
      harness.check("gzip".equalsIgnoreCase(c.getContentEncoding()), "received input is not zipped");
      byte[] content2 = new byte[uncompressedLength];
      readlen = new GZIPInputStream(is).read(content2);
      harness.check(uncompressedLength, readlen, "length of content should be compressed length");
      c.disconnect();

      harness.checkPoint("AutoGzip - GET request with identity encoding");
      c = (HttpURLConnection) url.openConnection();
      c.setRequestMethod("GET");
      c.setRequestProperty("Accept-Encoding", "identity");
      rc = c.getResponseCode();
      harness.check(200, rc, "GET request with Accept-Encoding: identity did not return 200");
      harness.check(uncompressedLength, c.getContentLength(), "Content-Length returned by GET with Accept-Encoding: identity does not match Content-Length returned by HEAD");
      is = c.getInputStream();
      harness.check(!(is instanceof GZIPInputStream), "received input is zipped");
      byte[] content3 = new byte[uncompressedLength];
      readlen = is.read(content3);
      harness.check(uncompressedLength, readlen, "length of retrieved content should be compressed length");
      c.disconnect();
    }
    catch ( Throwable e )
      {
	e.printStackTrace();
	harness.fail("Error in test_AutoGzip " + 
			   " should not have raised Throwable" );
      }
		
  }


  public void testall()
  {
    test_AutoGzip();
  }

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}

