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


package gnu.testlet.wonka.net.SocketOptions;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.io.IOException;


public class AcuniaSocketOptionsTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.SocketOptions");
    test_constants();
    //test_MulticastIF();
    test_BindAddr();
    test_Linger();
    test_RcvBuf();
    test_ReuseAddr();
    test_SndBuf();
    test_Timeout();
    test_NoDelay();
  }

/**
*  implemented. <br>
*
*/
  public void test_constants(){
    th.checkPoint("interface constants");
    th.check(SocketOptions.IP_MULTICAST_IF,0x0010, "checking value IP_MULTICAST_IF");
    th.check(SocketOptions.SO_BINDADDR,    0x000F, "checking value SO_BINDADDR");
    th.check(SocketOptions.SO_LINGER,      0x0080, "checking value SO_LINGER");
    th.check(SocketOptions.SO_RCVBUF,      0x1002, "checking value SO_RCVBUF");
    th.check(SocketOptions.SO_REUSEADDR,   0x0004, "checking value SO_REUSEADDR");
    th.check(SocketOptions.SO_SNDBUF,      0x1001, "checking value SO_SNDBUF");
    th.check(SocketOptions.SO_TIMEOUT,     0x1006, "checking value SO_TIMEOUT");
    th.check(SocketOptions.TCP_NODELAY,    0x0001, "checking value TCP_NODELAY");
  }

/**
*  implemented. <br>
*  - called by MulticastSocket --> get/setInterface
*/
  public void test_MulticastIF(){
    th.checkPoint("MulticastIF");
    MulticastSocket ms = null;
    try {
      ms = new MulticastSocket();
      InetAddress addr = ms.getInterface();
      th.debug("Interface = "+addr);
      InetAddress local = InetAddress.getLocalHost();
      ms.setInterface(local);
      th.check(ms.getInterface(), local);

    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      if( ms != null){
        ms.close();
      }
    }
  }

/**
*  implemented. <br>
*  - called by DatagramSocket.getLocalAddress()
*  - called by ServerSocket.getLocalAddress()
*  - called by Socket.getLocalAddress() (same code as ServerSocket)
*/
  public void test_BindAddr(){
    th.checkPoint("BindAddr - DatagramSocket");
    DatagramSocket ds = null;
    try {
      InetAddress local = InetAddress.getLocalHost();
      ds = new DatagramSocket(12345, local);
      th.check(ds.getLocalAddress() , local, "checking bind address");
      ds.close();
      ds = new DatagramSocket(12345);
      th.check(ds.getLocalAddress() , InetAddress.getByName("0.0.0.0"), "checking bind address");
      th.debug(ds.getLocalAddress()+" <---> "+InetAddress.getByName("0.0.0.0"));


    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
      ioe.printStackTrace();
    }
    finally{
      if( ds != null){
        ds.close();
      }
    }

    ServerSocket srv = null;
    try {
      InetAddress local = InetAddress.getLocalHost();
      srv = new ServerSocket(11111, 1,local);
      th.check(srv.getInetAddress() , local, "checking bind address");
      th.debug(srv.getInetAddress()+" <---> "+local);
      srv.close();
      srv = new ServerSocket(11113);
      th.check(srv.getInetAddress() , InetAddress.getByName("0.0.0.0"), "checking bind address");
      th.debug(srv.getInetAddress()+" <---> "+InetAddress.getByName("0.0.0.0"));


    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
      ioe.printStackTrace();
    }
    finally{
      if( srv != null){
        try {
          srv.close();
        } catch (IOException e){}
      }
    }
  }

/**
*  implemented. <br>
*  - called by Socket.get/setSoLinger
*/
  public void test_Linger(){
    th.checkPoint("Linger");
    Socket sock = null;
    SimpleServer ssrv = new SimpleServer();

    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local, 12345);
      th.check(sock.getSoLinger(), -1, "default linger value is off");
      sock.setSoLinger(true, 100);
      th.check(sock.getSoLinger(), 100, "checking linger value -- 1");
      try {
        sock.setSoLinger(true, -1);
        th.fail("bad value specified");
      } catch (IllegalArgumentException se){
        th.check(true, "threw exception -- 1");
      }
      sock.setSoLinger(false, 100);
      th.check(sock.getSoLinger(), -1, "checking linger value -- 2");

    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      try {
        sock.close();
      } catch (IOException e){}
    }
    ssrv.close();
  }

/**
*  implemented. <br>
*  - DatagramSocket
*  - Socket
*  - On Linux it seems to multiply the value by 2 when set is called!
*/
  public void test_RcvBuf(){
    th.checkPoint("Receive Buffer");
    DatagramSocket ds = null;
    try {
      InetAddress local = InetAddress.getLocalHost();
      ds = new DatagramSocket(12345, local);
      ds.setReceiveBufferSize(2048);
// We can't check the result, as the API allows anything or nothing to happen ...
      ds.getReceiveBufferSize();
      ds.setReceiveBufferSize(1024);
      ds.getReceiveBufferSize();
      try {
        ds.setReceiveBufferSize(-1024);
        th.fail("should throw an excception -- 1");
      } catch(IllegalArgumentException ia){
        th.check(true, "threw exception -- 1");
      }

    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      if( ds != null){
        ds.close();
      }
    }
    Socket sock = null;
    SimpleServer ssrv =  new SimpleServer(12346);
    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local, 12346);
      sock.setReceiveBufferSize(2048);
      sock.getReceiveBufferSize();
      sock.setReceiveBufferSize(1024);
      sock.getReceiveBufferSize();
      try {
        sock.setReceiveBufferSize(-1024);
        th.fail("should throw an excception -- 2");
      } catch(IllegalArgumentException ia){
        th.check(true, "threw exception -- 2");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      try {
        sock.close();

      } catch (Exception e){}
    }
    ssrv.close();
  }

/**
*  implemented. <br>
*  - MulticastSocket (Constructors ...)
*/
  public void test_ReuseAddr(){
    th.checkPoint("Reuse Bind Address");
    MulticastSocket ms = null;
    try {
      ms = new MulticastSocket(12350);
      try {
        ServerSocket ds = new ServerSocket(12350);
        ds.close();
        th.check(true);
      } catch(IOException ioe){
        th.fail("address can be reused ...");
      }

    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      if( ms != null){
        ms.close();
      }
    }
  }

/**
*  implemented. <br>
*  - DatagramSocket
*  - Socket
*  - On Linux it seems to multiply the value by 2 when set is called!
*/
  public void test_SndBuf(){
    th.checkPoint("Send Buffer");
    DatagramSocket ds = null;
    try {
      InetAddress local = InetAddress.getLocalHost();
      ds = new DatagramSocket(12345, local);
      ds.setSendBufferSize(2048);
      ds.getSendBufferSize();
      ds.setSendBufferSize(1024);
      ds.getSendBufferSize();
      try {
        ds.setSendBufferSize(-1024);
        th.fail("should throw an excception -- 1");
      } catch(IllegalArgumentException ia){
        th.check(true, "threw exception -- 1");
      }

    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      if( ds != null){
        ds.close();
      }
    }
    Socket sock = null;
    SimpleServer ssrv =  new SimpleServer(12347);
    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local, 12347);
      sock.setSendBufferSize(2048);
      sock.getSendBufferSize();
      sock.setSendBufferSize(1024);
      sock.getSendBufferSize();
      try {
        sock.setSendBufferSize(-1024);
        th.fail("should throw an excception -- 2");
      } catch(IllegalArgumentException ia){
        th.check(true, "threw exception -- 2");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException");
      th.debug(ioe);
    }
    finally{
      try {
        sock.close();

      } catch (Exception e){}
    }
    ssrv.close();

  }

/**
*  implemented. <br>
*  - DatagramSocket
*  - MulticastSocket
*  - ServerSocket
*  - Socket
*/
  public void test_Timeout(){
    th.checkPoint("Timeout");
    DatagramSocket ds = null;
    try {
      InetAddress local = InetAddress.getLocalHost();
      ds = new DatagramSocket();
      th.debug("default timeout is = "+ds.getSoTimeout());
      ds.setSoTimeout(1000);
      th.check(ds.getSoTimeout(), 1000, "checking value -- 1");
      try {
        ds.setSoTimeout(-1000);
        th.fail("should throw an IllegalArgumentException -- 1");
      } catch(IllegalArgumentException iae){
        th.check(true, "caught Exception -- 1");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException -- 1");
      th.debug(ioe);
    }
    finally{
      if( ds != null){
        ds.close();
      }
    }

    MulticastSocket ms = null;
    try {
      ms = new MulticastSocket();
      ms.setSoTimeout(1000);
      th.check(ms.getSoTimeout(), 1000, "checking value -- 2");
      try {
        ms.setSoTimeout(-1000);
        th.fail("should throw an IllegalArgumentException -- 2");
      } catch(IllegalArgumentException iae){
        th.check(true, "caught Exception -- 2");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException -- 2");
      th.debug(ioe);
    }
    finally{
      if( ms != null){
        ms.close();
      }
    }
    ServerSocket srv = null;
    try {
      srv = new ServerSocket(12351);
      srv.setSoTimeout(1000);
      th.check(srv.getSoTimeout(), 1000, "checking value -- 3");
      try {
        srv.setSoTimeout(-1000);
        th.fail("should throw an IllegalArgumentException -- 3");
      } catch(IllegalArgumentException iae){
        th.check(true, "caught Exception -- 3");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException -- 3");
      th.debug(ioe);
    }
    finally{
      if( srv != null){
        try {
          srv.close();
        } catch (IOException e){}
      }
    }

    Socket sock = null;
    SimpleServer ssrv =  new SimpleServer(12352);
    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local, 12352);
      sock.setSoTimeout(1000);
      th.check(sock.getSoTimeout(), 1000, "checking value -- 4");
      try {
        sock.setSoTimeout(-1000);
        th.fail("should throw an IllegalArgumentException -- 4");
      } catch(IllegalArgumentException iae){
        th.check(true, "caught Exception -- 4");
      }
    } catch (IOException ioe){
      th.fail("got unwanted IOException -- 4");
      th.debug(ioe);
    }
    finally{
      try {
        sock.close();

      } catch (Exception e){}
    }
    ssrv.close();
  }
/**
*  implemented. <br>
*  - Socket
*/
  public void test_NoDelay(){
    th.checkPoint("No Delay");
    Socket sock = null;
    SimpleServer ssrv =  new SimpleServer(12353);
    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local, 12353);
      th.debug("default TCP delay = "+sock.getTcpNoDelay());
      sock.setTcpNoDelay(true);
      th.check( sock.getTcpNoDelay(), "checking value -- 1");
      sock.setTcpNoDelay(false);
      th.check(!sock.getTcpNoDelay(), "checking value -- 2");
      sock.setTcpNoDelay(true);
      th.check( sock.getTcpNoDelay(), "checking value -- 3");
    } catch (IOException ioe){
      th.fail("got unwanted IOException -- 4");
      th.debug(ioe);
    }
    finally{
      try {
        sock.close();

      } catch (Exception e){}
    }
    ssrv.close();


  }

  private static class SimpleServer implements Runnable {

    private int handshake;
    private ServerSocket srv;
    private int port = 12345;

    public SimpleServer() {
      handshake = 0;
      new Thread(this, "SimpleServer Thread").start();
      while(handshake == 0){
         Thread.yield();
      }
    }

    public SimpleServer(int port) {
      this.port = port;
      handshake = 0;
      new Thread(this, "SimpleServer Thread").start();
      while(handshake == 0){
         Thread.yield();
      }
    }

    public void close(){
        try {
          System.out.println("closing ServerSocket of SimpleServer ...");
          srv.close();
        } catch(Exception ign){}
    }


    public void run(){
      try {
        srv = new ServerSocket(port,1,InetAddress.getLocalHost());
        handshake = 1;
        srv.accept();
        srv.close();
      } catch(IOException ioe){
        handshake = -1;
      }
    }
  }
}
