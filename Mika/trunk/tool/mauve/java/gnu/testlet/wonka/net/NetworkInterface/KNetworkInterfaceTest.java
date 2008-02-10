// Tags: JDK1.4

/*
   Copyright (C) Chris Gray, /k/ Embedded Java Solutions

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

package gnu.testlet.wonka.net.NetworkInterface;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.net.*;
import java.util.Enumeration;


public class KNetworkInterfaceTest implements Testlet
{
  protected static TestHarness harness;
  public void test_Basics ()
  {
    try {
      Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
      while(interfaces.hasMoreElements()) {
        NetworkInterface ni = (NetworkInterface)interfaces.nextElement();
        System.out.print(ni.getName() + ": " + ni.getDisplayName() + ", ");
        Enumeration addresses = ni.getInetAddresses();
        while(addresses.hasMoreElements()) {
            System.out.print(((InetAddress)addresses.nextElement()).getHostAddress());
        }
        System.out.println();
      }
    }
    catch (SocketException se) {
      se.printStackTrace();
      harness.fail("Error : test_Basics failed - 0 " +
			 " Should not throw SocketException here " );
    }
  }

  public void testall()
  {
    test_Basics();
  }

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    harness.setclass("java.net.NetworkInterface");
    testall ();
  }

}

