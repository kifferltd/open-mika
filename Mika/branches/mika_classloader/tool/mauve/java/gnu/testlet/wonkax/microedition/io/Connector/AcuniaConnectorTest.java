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


package gnu.testlet.wonkax.microedition.io.Connector;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import javax.microedition.io.*;
import java.net.*;
import java.io.*;

/**
**  this test only verifies the basic functionality of the connection schemes supported by wonka.
*/

public class AcuniaConnectorTest implements Testlet, Runnable {

  protected TestHarness th;
  private int port;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("javax.microedition.io.Connector");
    test_Datagram();
    test_Socket();
    test_ServerSocket();
    //test_File();
   //test_URL();
  }


/**
* implemented. <br>
*
*/
  public void test_Datagram(){
    th.checkPoint("datagram");
    String text = "This is a simple test for datagram protocol of the javax.microedition.io API";
    String response = "I got the Message. It all works fine";
    try {
      port = 29666;

      //server connection ...
      Connection c1 = Connector.open("datagram://:"+port+";timeout=5234");
      th.check(c1 instanceof  DatagramConnection, "checking connection type");

      //client connection ...
      Connection c2 = Connector.open("datagram://localhost:"+port+";timeout=1234");
      th.check(c2 instanceof  DatagramConnection, "checking connection type");

      DatagramConnection dc2 = (DatagramConnection)c2;
      Datagram dgram2 = dc2.newDatagram(128);
      dgram2.writeUTF(text);
      dc2.send(dgram2);
      DatagramConnection dc1 = (DatagramConnection)c1;
      Datagram dgram1 = dc1.newDatagram(128);
      dc1.receive(dgram1);
      th.check(dgram1.readUTF(),text, "checking message");
      dgram1.reset();
      dgram1.writeUTF(response);
      dc1.send(dgram1);
      dgram2.reset();
      dc2.receive(dgram2);
      th.check(dgram2.readUTF(),response,"checking response message");
      try {
        dgram2.readBoolean();
        th.fail("should throw an EOFException");
      }
      catch(EOFException eofe){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("unwanted exception "+e);
      e.printStackTrace();
    }
  }

/**
* implemented. <br>
*
*/
  public void test_Socket(){
    th.checkPoint("socket");
    Thread t = null;
    try {
      ServerSocket ss = new ServerSocket(29999);
      ss.setSoTimeout(1000);
      port = 29999;
      t = new Thread(this,this.toString()+" SOCKET");
      t.start();
      Socket s = ss.accept();
      s.setSoTimeout(1000);
      InputStream in = s.getInputStream();
      byte[] bytes = new byte[256];
      int len = in.read(bytes);
      OutputStream out = s.getOutputStream();
      out.write(bytes,0,len);
      s.close();
      t.join(1000);
    }
    catch(BindException se){
      th.debug("problems occured on serverside to create server");
    }
    catch(Exception e){
      th.fail("unwanted exception "+e);
      try {
        if(t != null){
          t.join(1000);
        }
      }
      catch(InterruptedException ie){}
    }
  }

  public void run(){
    try {
      Connection c = Connector.open("socket://localhost:"+port+";timeout=12345");
      th.check(c instanceof StreamConnection, "checking connection type");
      StreamConnection sc = (StreamConnection)c;
      OutputStream out = sc.openOutputStream();
      InputStream in = sc.openInputStream();
      String text = "testing the 'socket' connecting scheme via the javax.microedition.io API's";
      out.write(text.getBytes());
      byte[] bytes = new byte[256];
      int len = in.read(bytes);
      th.check(new String(bytes,0, len), text, "checking message");
    }
    catch(Exception e){
      th.fail("unwanted exception "+e);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_ServerSocket(){
    th.checkPoint("serversocket");
    Thread t = null;
    try {
      Connection c = Connector.open("serversocket://29998;timeout=1000",Connector.READ_WRITE,true);
      th.check(c instanceof StreamConnectionNotifier, "checking connection type -- 1");
      StreamConnectionNotifier scn = (StreamConnectionNotifier)c;
      port = 29998;
      t = new Thread(this,this.toString()+" SERVERSOCKET");
      t.start();
      c = scn.acceptAndOpen();
      th.check(c instanceof StreamConnection, "checking connection type -- 2");
      StreamConnection sc = (StreamConnection)c;
      InputStream in = sc.openInputStream();
      byte[] bytes = new byte[256];
      int len = in.read(bytes);
      OutputStream out = sc.openOutputStream();
      out.write(bytes,0,len);
      t.join(1000);
    }
    catch(ConnectionNotFoundException cnfe){
      th.debug("problems occured on serverside to create server");
    }
    catch(Exception e){
      th.fail("unwanted exception "+e);
      try {
        if(t != null){
          t.join(1000);
        }
      }
      catch(InterruptedException ie){}
    }
  }

/**
* implemented. <br>
*
*/
  public void test_File(){
    th.checkPoint("file");
    String name = "./test/AcuniaConnectorTestFile";
    String text = "This is a simple test string\n";
    try {
      Connection c = Connector.open("file:./system/system.properties",Connector.READ);
      th.check(c instanceof InputConnection);
      InputConnection in = (InputConnection) c;
      in.close();
      in.close();
      try {
        in.openInputStream();
        th.fail("connection is closed no streams may be created");
      }
      catch(IOException ioe){
        th.check(true);
      }
    }
    catch(IOException ioe){
      th.fail("unwanted Exception"+ioe+" -- 1");
    }

    File f = new File(name);
    if(f.isFile()){
      th.check(f.delete(),"Unable to remove testfile");
    }

    try {
      Connection c = Connector.open("file:"+name+";nosuchOption=OK",Connector.WRITE);
      th.check(c instanceof OutputConnection);
      OutputConnection out = (OutputConnection) c;
      DataOutputStream dos = out.openDataOutputStream();
      out.close();
      out.close();
      try {
        out.openOutputStream();
        th.fail("should throw an IOException");
      }
      catch(IOException ioe){
        th.check(true);
      }
      dos.writeUTF(text);
      dos.close();

      c = Connector.open("file:"+name+";append=true");
      th.check(c instanceof StreamConnection);
      StreamConnection stream = (StreamConnection) c;
      dos = stream.openDataOutputStream();
      DataInputStream dis = stream.openDataInputStream();
      stream.close();
      stream.close();
      try {
        stream.openOutputStream();
        th.fail("should throw an IOException");
      }
      catch(IOException ioe){
        th.check(true);
      }
      dos.writeUTF(text);
      dos.close();
      th.check(dis.readUTF(), text);
      th.check(dis.readUTF(), text);
      dis.close();

    }
    catch(IOException ioe){
      th.fail("unwanted Exception "+ioe+" -- 1");
    }
  }

/**
* implemented. <br>
*
*/
  public void test_URL(){
    th.checkPoint("URL");
    try {
      Connection c =
        Connector.open("jar:file:/test/mauve-suite.jar!/gnu/testlet/wonkax/microedition/io/Connector/AcuniaConnectorTest.class;a=b");
      th.check(c instanceof ContentConnection);
      ContentConnection cc = (ContentConnection)c;
      InputStream in = cc.openInputStream();
      th.check(in.read(new byte[128]),128, "reading bytes from entry");
    }
    catch(IOException ioe){
      th.fail("unwanted Exception "+ioe);
      ioe.printStackTrace();
    }
  }
}
