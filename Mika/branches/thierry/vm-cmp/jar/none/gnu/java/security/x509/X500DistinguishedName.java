/* X500DistinguishedName.java -- X.500 distinguished name.
   Copyright (C) 2004  Free Software Foundation, Inc.

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


package gnu.java.security.x509;

import gnu.java.security.OID;
import gnu.java.security.der.DERReader;
import gnu.java.security.der.DERValue;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class X500DistinguishedName implements Principal
{

  // Constants and fields.
  // -------------------------------------------------------------------------

  public static final OID CN         = new OID("2.5.4.3");
  public static final OID C          = new OID("2.5.4.6");
  public static final OID L          = new OID("2.5.4.7");
  public static final OID ST         = new OID("2.5.4.8");
  public static final OID STREET     = new OID("2.5.4.9");
  public static final OID O          = new OID("2.5.4.10");
  public static final OID OU         = new OID("2.5.4.11");
  public static final OID T          = new OID("2.5.4.12");
  public static final OID DNQ        = new OID("2.5.4.46");
  public static final OID NAME       = new OID("2.5.4.41");
  public static final OID GIVENNAME  = new OID("2.5.4.42");
  public static final OID INITIALS   = new OID("2.5.4.43");
  public static final OID GENERATION = new OID("2.5.4.44");
  public static final OID EMAIL      = new OID("1.2.840.113549.1.9.1");
  public static final OID DC         = new OID("0.9.2342.19200300.100.1.25");
  public static final OID UID        = new OID("0.9.2342.19200300.100.1.1");

  // Constructors.
  // -------------------------------------------------------------------------

  public X500DistinguishedName()
  {
  }

  public X500DistinguishedName(String name)
  {
  }

  public X500DistinguishedName(byte[] encoded) throws IOException
  {
  }

  public X500DistinguishedName(InputStream encoded) throws IOException
  {
  }

  // Instance methods.
  // -------------------------------------------------------------------------

  public String getName()
  {
    return toString();
  }

  public void newRelativeDistinguishedName()
  {
  }

  public int size()
  {
    return -1;
  }

  public int countComponents()
  {
    return 0;
  }

  public boolean containsComponent(OID oid, String value)
  {
    return false;
  }

  public String getComponent(OID oid)
  {
    return null;
  }

  public String getComponent(OID oid, int rdn)
  {
    return null;
  }

  public void putComponent(OID oid, String value)
  {
  }

  public void putComponent(String name, String value)
  {
  }

  public void setUnmodifiable()
  {
  }


  public byte[] getDer()
  {
    return null;
  }

}
