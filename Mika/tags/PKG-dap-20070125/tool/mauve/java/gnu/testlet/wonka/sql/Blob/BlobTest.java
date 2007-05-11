/*************************************************************************
/* BlobTest.java - Test java.util.Blob interface
/*
/* Copyright (c) 1999 Aaron M. Renn (arenn@urbanophile.com)
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

// Tags: JDK1.2 JDBC2.0

package gnu.testlet.wonka.sql.Blob;

import java.sql.*;
import java.io.InputStream;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class BlobTest implements Blob, Testlet
{

public void
test(TestHarness harness)
{
  harness.check(true, "java.sql.Blob");
}

public long
length() throws SQLException
{
  return(0);
}

public byte[]
getBytes(long offset, int length) throws SQLException
{
  return(null);
}

public InputStream
getBinaryStream() throws SQLException
{
  return(null);
}

public long
position(byte[] pattern, long offset) throws SQLException
{
  return(0);
}

public long
position(Blob pattern, long offset) throws SQLException
{
  return(0);
}

} // class BlobTest

