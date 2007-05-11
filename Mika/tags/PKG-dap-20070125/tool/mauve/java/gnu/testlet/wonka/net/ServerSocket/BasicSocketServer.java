// Tags: not-a-test

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


class BasicSocketServer extends Thread {
	ServerSocket srvsock = null;
	
  protected static TestHarness harness;
	public void init()
	{
		try {
			srvsock = new ServerSocket( 20000 );
		}
		catch ( Exception e )
		{
			System.out.println("Error : BasicSocketServer::init failed " + 
				"Exception should not be thrown here " + e );
		}
	}
	
	public void run()
	{
		if ( srvsock == null )
		{
			System.out.println("Error : BasicSocketServer::run failed  - 1 " + 
				"server socket creation was not successful" );
			return;
		}

		int i = 0;
		while( i++ < 2 )
		{
			try {
				Socket clnt = srvsock.accept();	

				OutputStream os = clnt.getOutputStream();
				DataOutputStream dos = new DataOutputStream( os );
				dos.writeBytes("hello buddy");
				dos.close();
			}
			catch ( Exception e ){
				System.out.println("Error : BasicSocketServer::run failed - 2" + 
				"exception was thrown" );
			}
		}
	}
}
