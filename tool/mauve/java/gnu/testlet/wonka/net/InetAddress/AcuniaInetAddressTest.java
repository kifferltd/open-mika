/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/



package gnu.testlet.wonka.net.InetAddress;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.net.*;


public class AcuniaInetAddressTest implements Testlet
{
  protected static TestHarness harness;

  private void testLocalhost()
  {
    try
    {
      harness.checkPoint("InetAddress building local host(getLocalHost)");
      InetAddress local = InetAddress.getLocalHost();
      harness.verbose("=> local adress (getLocalHost): "+local);
      harness.check(local == InetAddress.getLocalHost(),"local1.equals(InetAddress.getLocalHost())");
      harness.check(local,InetAddress.getLocalHost(),"implicit 'equals'");

      testAddressData(local);
    }
    catch(UnknownHostException uex)
    {
      harness.fail("Unable to resolve local host" + uex.toString() );
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }

  private void testLocalhostByName()
  {
    try
    {
      harness.checkPoint("InetAddress building local host(getByName(LocalHost))");
      InetAddress local = InetAddress.getByName("LocalHost");
      harness.verbose("=> local adress (getByName(LocalHost)): "+local);
      harness.check(local,InetAddress.getByName("LocalHost"));//reflexivity
      //harness.check(!local.equals(InetAddress.getLocalHost()));

      testAddressData(local);
    }
    catch(UnknownHostException uex)
    {
      harness.fail("Unable to resolve local host" + uex.toString() );
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }

  private void testLoopbackByName()
  {
    try
    {
      harness.checkPoint("InetAddress building local host(getByName(LocalHost))");
      InetAddress local = InetAddress.getByName("LocalHost");
      harness.verbose("=> loopback adress (getByName(null)): "+local);
      harness.check(local,InetAddress.getByName(null));//reflexivity
      //harness.check(!local.equals(InetAddress.getLocalHost()));

      testAddressData(local);
    }
    catch(UnknownHostException uex)
    {
      harness.fail("Unknown host" + uex.toString() );
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }


  private void testRandomByName()
  {
    harness.checkPoint("InetAddress getByName(random adress) for valid and invalid address strings");
    checkByName("www.kuleuven.be"); //valid adress
     failByName("www.sun","no web adress extension");
    checkByName("www.amazon.de");//valid address start
     failByName("www.amazon.de/bs975938","no extensions");
    checkByName("mail.yahoo.com");//valid address start
     failByName("www.yahoo.com/r/m1","no extensions (redirects to mail)");
     failByName("www.yah123.com","unexisting page");
    checkByName("127.0.0.1"      ); //self
     failByName("192.168.  8.  2","Name tag no spaces");
     failByName("195.0.76.33    ","Name tag no spaces");
     failByName("    195.0.76.33","Name tag no spaces");
     failByName("kiffer.ltd. uk","no spaces allowed");
     failByName(" kiffer.ltd.uk","no spaces allowed");
     failByName("kiffer.lt d.uk","no spaces allowed");
     failByName("kiffer.ltd.uk ","no spaces allowed");

    try
    {
      harness.checkPoint("InetAddress getByName(random adress))");
      testAddressData(InetAddress.getByName("127.0.0.1")); //self
      testAddressData(InetAddress.getByName("kiffer.ltd.uk")); //www
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }


  private void checkByName(String target)
  {
    try
    {
      InetAddress.getByName(target);
      harness.check(true,"InetAddress getByName("+target+"))loaded");
    }
    catch(Exception e)
    {
      harness.fail("Loading <"+target+"> : "+e.toString() );
    }
  }

  private void failByName(String target, String reason)
  {
    try
    {
      InetAddress.getByName(target);
      harness.fail("InetAddress getByName("+target+"))should fail for reason : "+reason);
    }
    catch(UnknownHostException uex)
    {
      harness.check(true,"InetAddress getByName("+target+")) failed for reason : "+reason );
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }

  private void testAddressData(InetAddress target)  throws Exception
  {
    harness.verbose("  testing address: "+target);

    InetAddress reverse;
    String name, address, fullname;
    String constructed = null;
    byte[] byteAddress = new byte[4];

    //host name
    //( InetAddress.getHostName(); )
    name = target.getHostName();
    harness.verbose("  => host name: "+name);

    //host address
    //( InetAddress.getHostAddress(); )
    address = target.getHostAddress();
    harness.verbose("  => host address : "+address);

    //addres in bytes:
    //( InetAddress.getAddress(); )
    byteAddress = target.getAddress();
    harness.verbose("  => host address (in bytes): "+byteAddress.toString());
    if (byteAddress.length == 4)
    {
      constructed=toUnsigned(byteAddress[0])+"."+toUnsigned(byteAddress[1])+"."+toUnsigned(byteAddress[2])+"."+toUnsigned(byteAddress[3]);
      harness.verbose("  => host address (getAddress()): "+constructed);
      harness.check(constructed,address);
    }
    else
      harness.fail("Host adress must consist out of 4 bytes, consists out of "+byteAddress.length);
    //full String
    //InetAddress.toString();
    fullname = target.toString();
    harness.verbose("  => InetAddress.toString() : "+fullname);
    harness.check(fullname,""+target); //inline promotion
    harness.check(fullname,name+"/"+address);

    // reverse engineering: searching by name
    // ( InetAddress.getByName(name); )
    reverse = InetAddress.getByName(name);
    harness.check(reverse,target, "getByName("+name+")");

    // reverse engineering: searching by address
    // ( InetAddress.getByName(address); )
    reverse = InetAddress.getByName(address);
    harness.check(reverse,target, "getByName("+address+"))");
  }

  private int toUnsigned(byte b) {
    return (b<0)?256+b:(int)b;
  }

  private void testHashCode()
  {
    try
    {
      InetAddress local = InetAddress.getLocalHost();

      harness.checkPoint("Testing HashCode : local to self");
      checkEquality(InetAddress.getLocalHost(), local, true);

/*
 * Doesn't work any more: one hostname yields 134.58.64.12 and the other 134.58.64.12
      harness.checkPoint("Testing HashCode : addresses to self");
      checkEquality(InetAddress.getByName("www.kuleuven.be"),InetAddress.getByName("www.kuleuven.be"),true );
      checkEquality(InetAddress.getByName("www.kuleuven.be"),InetAddress.getByName("www.kul.be"),false );
*/

      harness.checkPoint("Testing HashCode : local to definite other addresses");
      checkHashCodes(InetAddress.getByName("195.0.76.33"),local);
      checkHashCodes(InetAddress.getByName("kiffer.ltd.uk"),local );

      harness.checkPoint("Testing HashCode : definite different addresses");
      checkHashCodes(InetAddress.getByName("kiffer.ltd.uk"),InetAddress.getByName("www.kuleuven.be"));
      checkHashCodes(InetAddress.getByName("194.7.211.212"),InetAddress.getByName("195.0.76.33"));
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }

  }

  private void checkEquality(InetAddress target, InetAddress reference, boolean same_name)
  {
    //hashcode uniquity rules: same objects MUST have the same hashcode
    harness.debug("     Comparing ("+target+") and ("+reference+")");

    //check equality
    harness.check(target,reference);
    harness.check(target.getHostAddress(), reference.getHostAddress());
    //if asked, check for differnet address
    if(same_name)
      harness.check(target.toString(), reference.toString());
    else
      harness.check(!((target.toString()).equals(reference.toString())),
                "different names must have different addresses ("+target.toString()+" <--> "+reference.toString()+")" );

    // positive test: equal objects must have equal hashcode
    harness.check(target.hashCode(),reference.hashCode(),"equal addresses must have equal hashcode");
  }

  private void checkHashCodes(InetAddress target, InetAddress reference)
  {
    //hashcode uniquity rules: same objects MUST have the same hashcode
    int targetcode = target.hashCode();
    int referencecode = reference.hashCode();
    harness.verbose("     Comparing ("+target+") code: "+targetcode);
    harness.verbose("           and ("+reference+") code: "+referencecode);

    // positive test: equal objects must have equal code
    if(target.equals(reference))
      harness.check(targetcode,referencecode,"equal addresses must have equal hashcode");
    // negative test: different codes mean different objects
    if(targetcode != referencecode)
      harness.check(!(target.equals(reference)),"Different codes must come from different addresses");
  }

  private void testMulticastAddress()
  {
    try
    {
      harness.checkPoint("Testing multicast address");
      String should = "Class-C (224.0.0.0 to 139.225.225.225) must be Multicast Adress";
      String shouldnot = "Multicast Adress reserved for class-C (224.0.0.0 to 139.225.225.225)";
      harness.check(!(InetAddress.getByName("223.128.128.128").isMulticastAddress()),shouldnot);
        harness.check(InetAddress.getByName("224.000.000.000").isMulticastAddress(),should);
        harness.check(InetAddress.getByName("224.000.000.001").isMulticastAddress(),should);
        harness.check(InetAddress.getByName("230.128.128.128").isMulticastAddress(),should);
        harness.check(InetAddress.getByName("239.225.225.224").isMulticastAddress(),should);
        harness.check(InetAddress.getByName("239.225.225.225").isMulticastAddress(),should);
      harness.check(!(InetAddress.getByName("240.000.000.000").isMulticastAddress()),shouldnot);
      harness.check(!(InetAddress.getByName("247.128.128.128").isMulticastAddress()),shouldnot);

    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }
  }

  private void getAllByName()
  {
    harness.checkPoint("Testing multiple Inet adresses for one name");
    checkMultipleNames("224.0.0.4",false); //multicast address
    //TODO: FIND A REAL MULTIPLE ADDRESS NAME AND TEST THIS
  }


  private void checkMultipleNames(String target, boolean more_expected)
  {
    InetAddress first;
    InetAddress[] resolved;
    harness.checkPoint("Testing multiple Inet adresses for one name");
    try
    {
      resolved = InetAddress.getAllByName(target);
      first = InetAddress.getByName(target);
      harness.verbose("Found "+resolved.length+" addresses ");
      if(more_expected &&(+resolved.length<=1))
        harness.fail("Expected multiple addresses on <"+target+"> not found");
      for(int i = 0; i<resolved.length; i++)
      {
        harness.verbose("address "+resolved[i]);
        harness.check(resolved[i],first);
      }
    }
    catch(Exception e)
    {
      harness.fail(e.toString() );
    }

  }

/**
** The purpose of this test is to make sure we don't do unneeded name resolving ..
** It doesn't matter how your InetAddress get created ...
*/
  public void test_toString(){
    TestHarness th = harness;
    th.checkPoint("toString()java.lang.String");
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      th.check(InetAddress.getLocalHost(), laddr, "checking -- cache -- 1");
      th.check(!laddr.getHostName().equals(laddr.getHostAddress()), laddr + ": getHostName() returns " + laddr.getHostName() + ", getHostAddress returns " + laddr.getHostAddress());
      InetAddress local = InetAddress.getByName("127.0.0.1");
      th.check(InetAddress.getByName("127.0.0.1"), local, "checking -- cache -- 2");
      th.check(!local.getHostName().equals(local.getHostAddress()), local + ": getHostName() returns " + local.getHostName() + ", getHostAddress returns " + local.getHostAddress());
      InetAddress laddr2 = InetAddress.getByName(laddr.getHostAddress());
      th.check(laddr2, laddr, "checking -- cache -- 3");
      th.check(laddr2.toString(), laddr.toString(), "checking -- names -- 1");
      InetAddress lib = InetAddress.getByName("195.0.76.33");
      byte[] bytes = lib.getAddress();
      bytes[0] = (byte)127;
      th.check(lib.getHostAddress(), "195.0.76.33", "checking security -- 1");
      th.check(lib.getAddress() != bytes, "checking security -- 2");
      th.debug("hashCode of "+local+" = "+Integer.toHexString(local.hashCode()));
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
  }



  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("java.net.InetAddress");
		testLocalhost();
		testLocalhostByName();
		testLoopbackByName();
		testRandomByName();
		testHashCode();
	  testMulticastAddress();
		getAllByName();
		test_toString();
	}

}
