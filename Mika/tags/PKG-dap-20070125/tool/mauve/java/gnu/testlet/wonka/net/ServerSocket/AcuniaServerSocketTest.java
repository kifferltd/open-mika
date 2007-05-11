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


package gnu.testlet.wonka.net.ServerSocket;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.io.IOException;
import java.io.InterruptedIOException;


public class AcuniaServerSocketTest implements Testlet {

  protected TestHarness th;
  protected InetAddress zeros;


  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.ServerSocket");
    try {
      zeros = InetAddress.getByName("0.0.0.0");
    }
    catch(UnknownHostException uhe){
      th.fail("couldn't create InetAddress '0.0.0.0'");
    }
    test_ServerSocket();
    test_accept();
    test_close();
    test_getInetAddress();
    test_getLocalPort();
    test_toString();
    test_SoTimeout();
    test_implAccept();
    test_setSocketFactory();
  }

  private void checkConstructFails(int port, int backlog, InetAddress addr, int count){
    try {
      if(addr == null){
        if(backlog == -1){
          new ServerSocket(port);
        }
        else {
          new ServerSocket(port, backlog);
        }
      }
      else {
        new ServerSocket(port, backlog, addr);
      }
      th.fail("constructor must fail -- "+count);
    }
    catch(IOException ioe){
      th.fail("caught wrong Exception -- "+count+" --> "+ioe);
    }
    catch(IllegalArgumentException iae){
      th.check(true,"constructor failed with correct exception "+count);
    }
  }

  private void close(ServerSocket srv){
    if(srv != null){
      try {
        srv.close();
      }
      catch(IOException ioe){}
    }
  }


/**
* implemented. <br>
*
*/
  public void test_ServerSocket(){
    th.checkPoint("ServerSocket(int)");
    ServerSocket srv = null;

    checkConstructFails(-1,-1,null,1);
    checkConstructFails(65536,-1,null,2);
    try {
      srv = new ServerSocket(55124);
      th.check(srv.getInetAddress(), zeros, "checking address");
      th.check(srv.getLocalPort(), 55124, "checking port");
      srv.close();
      srv = new ServerSocket(0);
      th.check(srv.getLocalPort() > 1024 ,"port should not be in the reserved area below 1024, got "+srv.getLocalPort());
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(srv);

    th.checkPoint("ServerSocket(int,int)");
    checkConstructFails(-1,1,null,1);
    checkConstructFails(65536,1,null,2);
    try {
      srv = new ServerSocket(55556,1);
      th.check(srv.getInetAddress(), zeros, "checking address");
      th.check(srv.getLocalPort(), 55556, "checking port");
      srv.close();
      srv = new ServerSocket(0);
      th.check(srv.getLocalPort() > 1024 ,"port should not be in the reserved area below 1024, got "+srv.getLocalPort());
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(srv);

    th.checkPoint("ServerSocket(int,int,java.net.InetAddress)");
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      checkConstructFails(-1,1,null,1);
      checkConstructFails(65536,1,null,2);
      srv = new ServerSocket(55557,1,laddr);
      th.check(srv.getInetAddress(), laddr, "checking address -- 1");
      th.check(srv.getLocalPort(), 55557, "checking port");
      srv.close();
      srv = new ServerSocket(0);
      th.check(srv.getLocalPort() > 1024 ,"port should not be in the reserved area below 1024, got "+srv.getLocalPort());
      srv.close();
      srv = new ServerSocket(55558,1,null);
      th.check(srv.getInetAddress(), zeros, "checking address -- 2");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(srv);
  }

/**
* implemented. <br>
*
*/
  public void test_accept(){
    th.checkPoint("accept()java.net.Socket");
    ServerSocket srv = null;

    try {
      srv = new ServerSocket(55559);
      connectedPort = -1;
      new Connector(srv,this);
      Socket sock = srv.accept();
      while(connectedPort == -1){
        Thread.yield();
      }
      InetAddress loopb = InetAddress.getByName("127.0.0.1");
      th.check(sock.getLocalPort() > 1024 ,"port should not be in the reserved area below 1024, got "+sock.getLocalPort());
      th.check(sock.getPort(), connectedPort, "checking port of connecting socket");
      th.check(sock.getInetAddress(), loopb, "checking address of connecting socket");
      InetAddress laddr = InetAddress.getLocalHost();
      InetAddress zeros = InetAddress.getByName("0.0.0.0");
      checkConnect(srv,zeros, true, 1);
      checkConnect(srv,laddr, true, 2);
      checkConnect(srv,loopb, true, 3);
      srv.close();
      srv = new ServerSocket(55550,10,laddr);
      checkConnect(srv,zeros, laddr.equals(loopb), 4);
      checkConnect(srv,laddr, true, 5);
      checkConnect(srv,loopb, laddr.equals(loopb), 6);
      srv.close();
      srv = new ServerSocket(55551,10,loopb);
      checkConnect(srv,zeros, true, 7);
      checkConnect(srv,laddr, laddr.equals(loopb), 8);
      checkConnect(srv,loopb, true, 9);
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(srv);
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    th.checkPoint("toString()java.lang.String");
    ServerSocket srv = null;

    try {
      srv = new ServerSocket(55561);
      srv.close();
      srv.close();
      srv.close();
      try {
        srv.accept();
        th.fail("Cannot Accept -- socket is closed");
      }
      catch(IOException i){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
  }

/**
* not implemented. <br>
* @see ServerSocket constructor tests
*/
  public void test_getInetAddress(){
    th.checkPoint("()");

  }

/**
* not implemented. <br>
* @see ServerSocket constructor tests
*/
  public void test_getLocalPort(){
    th.checkPoint("()");

  }

/**
* not implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    ServerSocket srv = null;

    try {
      srv = new ServerSocket(55560);
      th.check(srv.toString().indexOf("55560") != -1, "should contain localPort");
      th.check(srv.toString().indexOf(srv.getInetAddress().toString()) != -1, "should contain local address");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(srv);
  }

/**
* implemented. <br>
*
*/
  public void test_SoTimeout(){
    th.checkPoint("getSoTimeout()int");
    ServerSocket srv = null;

    try {
      srv = new ServerSocket(55560);
      th.check(srv.getSoTimeout(),0,"checking timeout value -- 1");
      th.checkPoint("setSoTimeout(int)void");
      srv.setSoTimeout(10);
      th.check(srv.getSoTimeout(),10,"checking timeout value -- 2");
      try {
        srv.accept();
        th.fail("'accept' should have timed out ...");
      }
      catch(InterruptedIOException iioe){
        th.check(true);
      }

    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(srv);

  }

/**
* implemented. <br>
*
*/
  public void test_implAccept(){
    th.checkPoint("implAccept(java.net.Socket)void");
    MyServerSocket msrv = null;
    try {
      connectedPort = -1;
      msrv = new MyServerSocket(55123);
      new Connector(msrv, this);
      Socket sock = msrv.accept();
      while(connectedPort == -1){
        Thread.yield();
      }
      th.check(sock instanceof MySocket, "checking class");
      th.check(sock.getLocalPort() > 1024, "checking localport is > 1024 --> "+sock.getLocalPort());
      th.check(sock.getPort(), connectedPort, "checking remote port");
      InetAddress loopb = InetAddress.getByName("127.0.0.1");
      th.check(sock.getLocalAddress(), loopb, "checking localAddress");
      th.check(sock.getInetAddress(), loopb, "checking remote address");


    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(msrv);
  }

/**
* implemented. <br>
*
*/
  public void test_setSocketFactory(){
    th.checkPoint("setSocketFactory(java.net.SocketImplFactory)void");
    try {
      ServerSocket.setSocketFactory(null);
      ServerSocket.setSocketFactory(null);
      ServerSocket.setSocketFactory(new TestFactory());
      try {
        ServerSocket.setSocketFactory(new TestFactory());
        th.fail("Factory can be set only once -- 1");
      }
      catch(SocketException se){
        th.check(true);
      }
      try {
        ServerSocket.setSocketFactory(null);
        th.fail("Factory can be set only once -- 2");
      }
      catch(SocketException se){
        th.check(true);
      }
      try {
        ServerSocket.setSocketFactory(new TestFactory());
        th.fail("Factory can be set only once -- 3");
      }
      catch(SocketException se){
        th.check(true);
      }
    }
    catch(IOException e){
      th.fail("caught unwanted exception "+e);
    }
  }

  private void checkConnect(ServerSocket srv, InetAddress from, boolean pass, int count) throws IOException {
    this.pass = pass;
    this.count = count;
    new Connector(from, srv.getLocalPort(), this);
    srv.setSoTimeout(250);
    try {
      srv.accept();
    } catch(InterruptedIOException iioe){
      th.check(!pass, "checkConnect -- "+count);
    }
  }

  protected boolean pass;
  protected int connectedPort = -1;
  protected int count = 0;

  static class Connector implements Runnable {
    private InetAddress address;
    private int port;
    private boolean report;
    private AcuniaServerSocketTest test;

    public Connector(ServerSocket srv, AcuniaServerSocketTest test){
      this.port = srv.getLocalPort();
      this.address = srv.getInetAddress();
      this.test = test;
      this.report = false;
      new Thread(this, "Connector Thread").start();
    }

    public Connector(InetAddress addr, int port, AcuniaServerSocketTest test){
      this.port = port;
      this.address = addr;
      this.test = test;
      this.report = true;
      new Thread(this, "Connector Thread").start();
    }

    public void run(){
       try {
         Socket sock = new Socket(address,port);
         if(report){
           test.th.check(test.pass, "check connecting -- "+test.count+" connection made");
         }
         else {
           test.th.check(sock.getInetAddress(), address,"checking address in Connector");
           test.th.check(sock.getPort(), port,"checking port in Connector");
           test.connectedPort = sock.getLocalPort();
         }
         sock.close();
       }
       catch(IOException ioe){
         if(report){
           test.th.check(!test.pass, "check connecting -- "+test.count+" threw exception ");
         }
         else{
           test.th.fail("got unwanted exception "+ioe+" while Connecting");
         }
       }
    }
  }

  static class TestFactory implements SocketImplFactory{

    private static java.lang.reflect.Constructor constructor;

    public TestFactory(){
      if (constructor == null){
        try {
          Class cl = Class.forName("java.net.PlainSocketImpl");
          constructor = cl.getDeclaredConstructor(null);
          constructor.setAccessible(true);
        }
        catch(Exception e){}
      }
    }

    public SocketImpl createSocketImpl(){
      try {
        return (SocketImpl)constructor.newInstance(null);
      }
      catch(Exception e){
        return null;
      }
    }
  }

  static class MySocket extends Socket{ }

  static class MyServerSocket extends ServerSocket{

    public MyServerSocket(int port)throws IOException {
      super(port);
    }

    public Socket accept()throws IOException {
      Socket s = new MySocket();
      implAccept(s);
      return s;
    }
  }
}
