/* DERReader.java -- parses ASN.1 DER sequences
   Copyright (C) 2003 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.java.security.der;

import gnu.java.security.OID;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class decodes DER sequences into Java objects. The methods of
 * this class do not have knowledge of higher-levels of structure in the
 * DER stream -- such as ASN.1 constructions -- and it is therefore up
 * to the calling application to determine if the data are structured
 * properly by inspecting the {@link DERValue} that is returned.
 *
 * @author Casey Marshall (csm@gnu.org)
 */
public class DERReader 
{

  // Fields.
  // ------------------------------------------------------------------------

  protected InputStream in;

  protected ByteArrayOutputStream encBuf;

  // Constructor.
  // ------------------------------------------------------------------------

  /**
   * Create a new DER reader from a byte array.
   *
   * @param in The encoded bytes.
   */
  public DERReader(byte[] in)
  {
  }

  public DERReader (byte[] in, int off, int len)
  {
  }

  /**
   * Create a new DER readed from an input stream.
   *
   * @param in The encoded bytes.
   */
  public DERReader(InputStream in)
  {
  }

  // Class methods.
  // ------------------------------------------------------------------------

  /**
   * Convenience method for reading a single primitive value from the
   * given byte array.
   *
   * @param encoded The encoded bytes.
   * @throws IOException If the bytes do not represent an encoded
   * object.
   */
  public static DERValue read(byte[] encoded) throws IOException
  {
    throw new IOException();
  }

  // Instance methods.
  // ------------------------------------------------------------------------

  public void skip (int bytes) throws IOException
  {
  }

  /**
   * Decode a single value from the input stream, returning it in a new
   * {@link DERValue}. By "single value" we mean any single type in its
   * entirety -- including constructed types such as SEQUENCE and all
   * the values they contain. Usually it is sufficient to call this
   * method once to parse and return the top-level structure, then to
   * inspect the returned value for the proper contents.
   *
   * @return The parsed DER structure.
   * @throws IOException If an error occurs reading from the input
   * stream.
   * @throws DEREncodingException If the input does not represent a
   * valid DER stream.
   */
  public DERValue read() throws IOException
  {
    return null;
  }

  protected int readLength() throws IOException
  {
    throw new IOException();
  }

}
