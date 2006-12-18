// Tags: JDK1.0
// Uses: BasicBacklogSocketServer BasicSocketServer MyBasicSocketServer MyServerSocket

/*
   Copyright (C) 1999 Hewlett-Packard Company

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

package gnu.testlet.wonka.net.ServerSocket;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.net.*;
import java.io.*;


public class ServerSocketTest implements Testlet
{
	
  protected static TestHarness harness;

	public void test_BasicBacklogServer()
	{
		BasicBacklogSocketServer srv = new BasicBacklogSocketServer();
		srv.init();
		srv.start();
		try {
		  Thread.sleep(100);
    } catch(InterruptedException ie){}
		
		try {
			Socket sock = new Socket("localhost" , 21000);
			DataInputStream dis = new DataInputStream(
									sock.getInputStream());
		}
		catch ( Exception e )
		{
			harness.fail("Error : test_BasicBacklogServer failed - 1" + 
				"exception was thrown" );
		}

		// second iteration
		try {
			Socket sock = new Socket("localhost" , 21000);
		}
		catch ( Exception e )
		{
		}

				// third iteration
		try {
			Socket sock = new Socket("localhost" , 21000);
		}
		catch ( Exception e )
		{
		}

	}
  
	public void test_BasicServer()
	{
		harness.checkPoint("BasicServer");
		BasicSocketServer srv = new BasicSocketServer();
		srv.init();
		srv.start();
		try {
		  Thread.sleep(100);
		} catch(InterruptedException ie){}

		try {
			Socket sock = new Socket("localhost" , 20000);
			DataInputStream dis = new DataInputStream(
									sock.getInputStream());
			String str = dis.readLine();
			
			if ( !str.equals("hello buddy" ))
				harness.fail("Error : test_BasicServer failed - 1" + 
					"string returned is not correct." );
			sock.close();
		}
		catch ( Exception e )
		{
			harness.fail("Error : test_BasicServer failed - 2" + 
				"exception was thrown: " + e.getMessage());
		}
		// System.out.println("BasicServer 5");

		// second iteration
		try {
			Socket sock = new Socket("localhost" , 20000);
			DataInputStream dis = new DataInputStream(
									sock.getInputStream());
			String str = dis.readLine();
			
			if ( !str.equals("hello buddy" ))
				harness.fail("Error : test_BasicServer failed - 3" + 
					"string returned is not correct." );
			sock.close();
		}
		catch ( Exception e )
		{
			harness.fail("Error : test_BasicServer failed - 4" + 
				"exception was thrown: " + e.getMessage());
		}
		// System.out.println("BasicServer 6");

                try {
			srv.srvsock.close();
		}catch ( Exception e )
		{
			harness.fail("Error : test_BasicServer failed - 5" + " should not throw exception in close " );
		}
		// System.out.println("BasicServer 7");
	}

	public void test_MyBasicServer()
	{
		MyBasicSocketServer srv = new MyBasicSocketServer();
		srv.init();
		srv.start();
		try {
		  Thread.sleep(100);
    } catch(InterruptedException ie){}

		try {
			Socket sock = new Socket("localhost" , 20000);
		} catch (IOException e) {}
	}

	public void test_params()
	{
		try {
			ServerSocket sock = new ServerSocket( 30000 );

			if ( sock.getLocalPort() != 30000 )
				harness.fail("Error : test_params failed - 1" + 
					"get port did not return proper values" );

//			if(false) { // set/getSoTimeout not there
			try {
			  sock.setSoTimeout( 100 );
			  harness.check(sock.getSoTimeout(), 100 ,"Error : test_params failed - 2" +
					"get /set timeout did not return proper values" );
			}
      catch ( Exception e ) {
        harness.fail("Error : setSoTimeout fails since vxWorks do not support the feature" );
        e.printStackTrace(System.out);
      }
//			}
		    
			try {
				ServerSocket sock1 = new ServerSocket( 30000 );
				harness.fail("Error : test_params failed - 3" + 
					"should have thrown bind exception here." );

			}
			catch ( Exception e ){}

			harness.check(sock.getLocalPort(), 30000,
				"Error : test_params failed - 4" + "toString did not return proper values " );

      harness.check(sock.getInetAddress().toString(),"0.0.0.0/0.0.0.0",
				"Error : test_params failed - 5" + "getInetAddress did not return proper values " );
			  
		}
		catch ( Exception e )
		{
			harness.fail("Error : test_params failed - 10" + 
				"exception was thrown" );
			e.printStackTrace(System.out);
		}

	}

	public void testall()
	{
		harness.verbose("DOING BasicServer Tests");
		test_BasicServer();
		harness.verbose("DOING MyBasicServer Tests");
		test_MyBasicServer();
		harness.verbose("DOING BasicBacklogServer Tests");
		test_BasicBacklogServer();
		harness.verbose("DOING param Tests");
		test_params();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    harness.setclass("java.net.ServerSocket");
    testall ();
  }

}
