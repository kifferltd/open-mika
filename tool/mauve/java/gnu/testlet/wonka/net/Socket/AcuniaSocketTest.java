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


package gnu.testlet.wonka.net.Socket;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.util.Vector;
import java.io.*;

/**
** This test class doesn't test methods which call get/setSocketOption.
** these methods are tested AcuniaSocketOptions
**
*/
public class AcuniaSocketTest implements Testlet {

  protected TestHarness th;
  protected static final int port = 54321;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.Socket");
    MyServer msrv = new MyServer(port);
    if(!msrv.start()){
      th.fail("failed to setup server");
      return;
    }
//    test_Socket();
    test_close();
    test_getInputStream();
    test_getOutputStream();
    test_getInetAddress();
    test_getLocalAddress();
    test_getLocalPort();
    test_getPort();
    test_toString();
    test_setSocketImplFactory();
    test_keepalive();
    test_shutdownInput();
    test_shutdownOutput();
    msrv.stop();
  }

  private void close(Socket s){
    if(s != null){
      try{
        s.close();
      }
      catch(IOException e){}
    }
  }

  private void constructMustFail(InetAddress addr, int p, String excType, int count){
    try{
      Socket s = new Socket(addr,p);
      th.fail("constructor didn't fail -- "+count);
      s.close();
    }
    catch(Exception e){
      th.check(e.toString().startsWith(excType) ,"checking exception type -- "+count);
      if(!e.toString().startsWith(excType)){
        th.debug("got "+e.getClass().getName()+", but expected "+excType);
      }
    }
  }

  private void constructMustFail(String addr, int p, String excType, int count){
    try{
      Socket s = new Socket(addr,p);
      th.fail("constructor didn't fail -- "+count);
      s.close();
    }
    catch(Exception e){
      th.check(e.toString().startsWith(excType) ,"checking exception type -- "+count);
      if(!e.toString().startsWith(excType)){
        th.debug("got "+e.getClass().getName()+", but expected "+excType);
      }
    }
  }

  private void constructMustFail(String addr, int p, InetAddress laddr, int lp, String excType, int count){
    try{
      Socket s = new Socket(addr,p,laddr,lp);
      th.fail("constructor didn't fail -- "+count);
      s.close();
    }
    catch(Exception e){
      th.check(e.toString().startsWith(excType) ,"checking exception type -- "+count);
      if(!e.toString().startsWith(excType)){
        th.debug("got "+e.getClass().getName()+", but expected "+excType);
      }
    }
  }

  private void constructMustFail(InetAddress addr, int p, InetAddress laddr, int lp, String excType, int count){
    try{
      Socket s = new Socket(addr,p,laddr,lp);
      th.fail("constructor didn't fail -- "+count);
      s.close();
    }
    catch(Exception e){
      th.check(e.toString().startsWith(excType) ,"checking exception type -- "+count);
      if(!e.toString().startsWith(excType)){
        th.debug("got "+e.getClass().getName()+", but expected "+excType);
      }
    }
  }

/**
*  CG 20160414 This is all BOGUS!  The InetAddress could be anything - loopback, 
*  address on WLAN or LAN, ...
*
  public void test_Socket(){
    Socket sock = null;
    th.checkPoint("Socket(java.net.InetAddress,int)");
    constructMustFail((InetAddress)null, 1,"java.lang.NullPointerException",1);
    try {
      InetAddress local = InetAddress.getLocalHost();
      constructMustFail(local, -1,"java.lang.IllegalArgumentException",2);
      constructMustFail(local, 65536,"java.lang.IllegalArgumentException",3);
      constructMustFail(local, 65535,"java.net.ConnectException",4);
      sock = new Socket(local,port);
      th.check(sock.getInetAddress(),  local, "checking remote address");
      th.check(sock.getLocalAddress().getHostAddress(), local.getHostAddress(), "checking local address");
      th.check(sock.getPort(), port, "checking remote port");
      th.check(sock.getLocalPort() > 1024, "checking local port > 1024, got "+sock.getLocalPort());
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(sock);
    th.checkPoint("Socket(java.net.InetAddress,int,java.net.InetAddress,int)");
    try {
      InetAddress local = InetAddress.getLocalHost();
      constructMustFail((InetAddress)null, 1, local, 10300, "java.lang.NullPointerException",1);
      constructMustFail(local, -1, local, 10301,    "java.lang.IllegalArgumentException",2);
      constructMustFail(local, 65536, local, 10302, "java.lang.IllegalArgumentException",3);
      constructMustFail(local, 65535, local, 10303, "java.net.ConnectException",4);
      constructMustFail(local, port, local, -1,     "java.lang.IllegalArgumentException",5);
      constructMustFail(local, port, local, 65536,  "java.lang.IllegalArgumentException",6);

      sock = new Socket(local,port,local,10304);
      th.check(sock.getInetAddress(),  local, "checking remote address -- 1");
      th.check(sock.getLocalAddress(), local, "checking local address -- 1");
      th.check(sock.getPort(), port, "checking remote port -- 1");
      th.check(sock.getLocalPort(), 10304, "checking local port -- 1");
      sock.close();
      sock = new Socket(local,port,null,10305);
      th.check(sock.getInetAddress(),  local, "checking remote address -- 2");
      th.check(sock.getLocalAddress().getHostAddress(), local.getHostAddress(), "checking local address -- 2");
      th.check(sock.getPort(), port, "checking remote port -- 2");
      th.check(sock.getLocalPort(), 10305, "checking local port -- 2");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);

    th.checkPoint("Socket(java.lang.String,int)");
    constructMustFail((String)null, 1,"java.net.ConnectException",1);
    constructMustFail("localhost", -1,"java.lang.IllegalArgumentException",2);
    constructMustFail("localhost", 65536,"java.lang.IllegalArgumentException",3);
    constructMustFail("localhost", 65535,"java.net.ConnectException",4);
    try {
      InetAddress local = InetAddress.getLocalHost();
      sock = new Socket(local,port);
      th.check(sock.getInetAddress(),  local, "checking remote address");
      th.check(sock.getLocalAddress().getHostName(), local.getHostName(), "checking local address");
      th.check(sock.getPort(), port, "checking remote port");
      th.check(sock.getLocalPort() > 1024, "checking local port > 1024, got "+sock.getLocalPort());
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(sock);

    th.checkPoint("Socket(java.lang.String,int,java.net.InetAddress,int)");
    try {
      InetAddress local = InetAddress.getLocalHost();
      constructMustFail((String)null, 1, local, 10306,    "java.net.ConnectException",1);
      constructMustFail("localhost", -1, local, 10307,    "java.lang.IllegalArgumentException",2);
      constructMustFail("localhost", 65536, local, 10312, "java.lang.IllegalArgumentException",3);
      constructMustFail("localhost", 65535, local, 10313, "java.net.ConnectException",4);
      constructMustFail("localhost", port, local, -1,     "java.lang.IllegalArgumentException",5);
      constructMustFail("localhost", port, local, 65536,  "java.lang.IllegalArgumentException",6);

      sock = new Socket("localhost",port,local,10314);
      InetAddress loopb = InetAddress.getByName("127.0.0.1");
      th.check(sock.getInetAddress(),  loopb, "checking remote address -- 1");
      th.check(sock.getLocalAddress(), local, "checking local address -- 1");
      th.check(sock.getPort(), port, "checking remote port -- 1");
      th.check(sock.getLocalPort(), 10314, "checking local port -- 1");
      sock.close();
      sock = new Socket("localhost",port,null,10315);
      th.check(sock.getInetAddress(),  loopb, "checking remote address -- 2");
      th.check(sock.getLocalAddress(), loopb, "checking local address -- 2");
      th.check(sock.getPort(), port, "checking remote port -- 2");
      th.check(sock.getLocalPort(), 10315, "checking local port -- 2");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
    }
    close(sock);
  }
*/

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port,null,10335);
      sock.close();
      sock.close();
      sock.close();
      try {
        sock.getReceiveBufferSize();
        th.fail("should throw an SocketException -- 1");
      }
      catch(IOException e){
        th.check(true);
      }
      try {
        sock.getOutputStream();
        th.fail("should throw an Exception -- 1");
      }
      catch(Exception e){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
  }

/**
* implemented. <br>
*
*/
  public void test_getInputStream(){
    th.checkPoint("getInputStream()java.io.InputStream");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port,null,10345);
      InputStream in = sock.getInputStream();
      th.check(in.available() ,0 ,"checking available -- 1");
      in.close();
      try {
        sock.getSendBufferSize();
        th.fail("should throw an SocketException -- 1");
      }
      catch(IOException e){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);
  }

/**
*   not implemented. <br>
*
*/
  public void test_getOutputStream(){
    th.checkPoint("getOutputStream()java.io.OutputStream");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port,null,10355);
      OutputStream out = sock.getOutputStream();
      out.close();
      try {
        sock.getReceiveBufferSize();
        th.fail("should throw an SocketException -- 1");
      }
      catch(IOException e){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);

  }

/**
* not implemented. <br>
* @see Socket constructor
*/
  public void test_getInetAddress(){
    th.checkPoint("()");

  }

/**
* not implemented. <br>
* @see Socket constructor
*/
  public void test_getLocalAddress(){
    th.checkPoint("()");

  }

/**
* not implemented. <br>
* @see Socket constructor
*/
  public void test_getLocalPort(){
    th.checkPoint("()");

  }

/**
* not implemented. <br>
* @see Socket constructor
*/
  public void test_getPort(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port,null,10325);
      th.check(sock.toString().indexOf(laddr.toString()) != -1 , "should contain remote address");
      th.check(sock.toString().indexOf(""+10325) != -1 , "should contain local port");
      th.check(sock.toString().indexOf(""+port) != -1 , "should contain remote port");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);
  }

/**
* implemented. <br>
*
*/
  public void test_keepalive(){
    th.checkPoint("getKeepAlive()boolean");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port);
      th.check(!sock.getKeepAlive(), "default is false");
      sock.setKeepAlive(true);
      th.check(sock.getKeepAlive(), "set to true");
      sock.setKeepAlive(false);
      th.check(!sock.getKeepAlive(), "set to false");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);
  }

/**
* implemented. <br>
*
*/
  public void test_shutdownInput(){
    th.checkPoint("shutdownInput()void");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port);
      InputStream in = sock.getInputStream();
      sock.shutdownInput();
      th.check(in.read(), -1, "EOF reached");
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);
  }

/**
* implemented. <br>
*
*/
  public void test_shutdownOutput(){
    th.checkPoint("shutdownOutput()void");
    Socket sock = null;
    try {
      InetAddress laddr = InetAddress.getLocalHost();
      sock = new Socket(laddr,port);
      OutputStream out = sock.getOutputStream();
      sock.shutdownOutput();
      try {
        out.write(new byte[256]);
        th.fail("after shutdownOutput IOException should be thrown");
      }
      catch(IOException e){
        th.check(true);
      }
    }
    catch(Exception e){
      th.fail("caught unwanted exception "+e);
      e.printStackTrace();
    }
    close(sock);
  }






/**
* implemented. <br>
*
*/
  public void test_setSocketImplFactory(){
    th.checkPoint("setSocketImplFactory(java.net.SocketImplFactory)void");
    try {
      Socket.setSocketImplFactory(null);
      Socket.setSocketImplFactory(null);
      Socket.setSocketImplFactory(new TestFactory());
      try {
        Socket.setSocketImplFactory(new TestFactory());
        th.fail("Factory can be set only once -- 1");
      }
      catch(SocketException se){
        th.check(true);
      }
      try {
        Socket.setSocketImplFactory(null);
        th.fail("Factory can be set only once -- 2");
      }
      catch(SocketException se){
        th.check(true);
      }
      try {
        Socket.setSocketImplFactory(new TestFactory());
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

  static class MyServer implements Runnable {
     private int port;
     private ServerSocket srv;
     private boolean stop;

     public MyServer(int port){
       this.port = port;
     }

     public void stop(){
       stop = true;
       try{
         srv.close();
       }
       catch (IOException e){ }
       while(stop){
         Thread.yield();
       }
     }

     public boolean start(){
       try {
         srv = new ServerSocket(port);
         srv.setSoTimeout(150);
         new Thread(this,"MyServer thread").start();
         return true;
       }
       catch(Exception e){
         return false;
       }
     }

     public void run(){
       try {
         Vector v = new Vector();
         while(!stop){
           try {
             v.add(srv.accept());
           }
           catch(Exception e){}
         }
         for(int i = 0 ; i < v.size() ; i++){
           try {((Socket)v.get(i)).close();
           } catch(IOException ioe){}
         }
         srv.close();
       }
       catch(Exception e1){}
       stop = false;
     }
  }

}
