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


package gnu.testlet.wonka.net.DatagramSocket;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
** a test class for java.net.DatagramSocket Class.
**
** this class does NOT test get/setSoTimeout, get/setReceiveBufferSize and get/setSendBufferSize
** these methods are tested in the AcuniaSocketOptionsTest.
*/
public class AcuniaDatagramSocketTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.DatagramSocket");
    test_DatagramSocket();
    test_close();
    test_receive();
    test_send();
    test_connect();
    test_disconnect();
    test_getInetAddress();
    test_getPort();
    test_getLocalPort();
    test_getLocalAddress();
    test_behaviour();
  }

  private void close(DatagramSocket ds){
    if (ds != null){
      ds.close();
    }
  }

  private void checkConstructFails(int port, InetAddress addr, int count){
    try {
      new DatagramSocket(port,addr);
      th.fail("constructor must fail -- "+count);
    }
    catch(IOException ioe){
      th.fail("caught wrong Exception -- "+count+" --> "+ioe);
    }
    catch(IllegalArgumentException iae){
      th.check(true,"constructor failed with correct exception "+count);
    }
  }

  private void checkConstructFails(int port, int count){
    try {
      new DatagramSocket(port);
      th.fail("constructor must fail -- "+count);
    }
    catch(IOException ioe){
      th.fail("caught wrong Exception -- "+count+" --> "+ioe);
    }
    catch(IllegalArgumentException iae){
      th.check(true,"constructor failed with correct exception "+count);
    }
  }


/**
* implemented. <br>
*
*/
  public void test_DatagramSocket(){
    th.checkPoint("DatagramSocket()");
    DatagramSocket ds = null;
    try {
       ds = new DatagramSocket();
       th.check(ds.getLocalPort() >= 1024 ,"port should not be in the reserved area below 1024, got "+ds.getLocalPort());
       th.check(ds.getLocalAddress(), InetAddress.getByName("0.0.0.0"), "cheking local address");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);

    th.checkPoint("DatagramSocket(int)");
    checkConstructFails(-1, 1);
    checkConstructFails(65536, 2);

    try {
       ds = new DatagramSocket(11345);
       th.check(ds.getLocalPort(), 11345 ,"test value -- 1");
       th.check(ds.getLocalAddress(), InetAddress.getByName("0.0.0.0"), "cheking local address");
       try {
         new DatagramSocket(11345);
         th.fail("should throw exception -- port already in use");
       }
       catch(SocketException se){
         th.check(true);
         //se.printStackTrace();
       }
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);

    th.checkPoint("DatagramSocket(int,java.net.InetAddress)");
    try {
       InetAddress laddr = InetAddress.getLocalHost();
       checkConstructFails(-1, laddr, 1);
       checkConstructFails(65536, laddr, 2);
       ds = new DatagramSocket(11346,laddr);
       th.check(ds.getLocalPort(), 11346 ,"test value -- 1");
       try {
         new DatagramSocket(11346, laddr);
         th.fail("should throw exception -- port already in use");
       }
       catch(SocketException se){
         th.check(true);
       }
       th.check(ds.getLocalAddress(), laddr ,"test value -- 1");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    try {
       DatagramSocket ds = new DatagramSocket();
       ds.close();
       ds.close();
       ds.close();
       th.debug("local address = "+ds.getLocalAddress());
       InetAddress laddr = InetAddress.getLocalHost();
       ds.connect(laddr, 12345);
       th.check(ds.getPort() , -1);
       th.check(ds.getInetAddress() , null);
       ds = new DatagramSocket();
       ds.connect(laddr, 12345);
       th.check(ds.getPort() , 12345);
       th.check(ds.getInetAddress() , laddr);
       ds.disconnect();
       ds.close();
       ds.close();
       DatagramPacket p = new DatagramPacket(new byte[10],10);
       try {
         ds.send(p);
         th.fail("exception not thrown -- 1");
       } catch(Exception so){
         th.check(true);
       }
       try {
         ds.receive(p);
         th.fail("exception not thrown -- 2");
       } catch(Exception so){
         th.check(true);
       }
       try {
         ds.getSendBufferSize();
         th.fail("exception not thrown -- 3");
       } catch(Exception so){
         th.check(true);
       }
       try {
         ds.getReceiveBufferSize();
         th.fail("exception not thrown -- 4");
       } catch(Exception so){
         th.check(true);
       }
       try {
         ds.getSoTimeout();
         th.fail("exception not thrown -- 5");
       } catch(Exception so){
         th.check(true);
       }
       try {
         ds.setSoTimeout(4);
         th.fail("exception not thrown -- 6");
       } catch(Exception so){
         th.check(true);
       }
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_receive(){
    th.checkPoint("receive(java.net.DatagramPacket)void");
    DatagramSocket ds = null;
    try {
       InetAddress laddr = InetAddress.getLocalHost();
       DatagramPacket recvp = new DatagramPacket(new byte[128],20,20);
       String s = "01234MESSAGE: HELLOWORLD! HOW ARE YOU ALL TODAY ! END12345";
       DatagramPacket sendp = new DatagramPacket(s.getBytes(),5, "MESSAGE: HELLOWORLD!".length()-10, laddr, 11234);
       ds = new DatagramSocket(11234,laddr);
       ds.send(sendp);
       ds.receive(recvp);
       th.check(new String(recvp.getData(), recvp.getOffset(), recvp.getLength()), "MESSAGE: HELLOWORLD!", "checking data -- 1");
       th.check(recvp.getData()[19] , 0, "nothing written before offset");
       th.check(recvp.getAddress(), laddr);
       th.check(recvp.getPort(), 11234);

       ds.connect(laddr, 11234);
       try {
         DatagramPacket p = new DatagramPacket(s.getBytes(),0, s.length(), laddr, 11233);
         ds.send(p);
         th.fail("socket is connected -- 1");
       } catch(IllegalArgumentException se){
         th.check(true);
       }
       try {
         DatagramPacket p = new DatagramPacket(s.getBytes(),0, s.length(), InetAddress.getByName("0.0.0.0"), 11234);
         ds.send(p);
         th.fail("socket is connected -- 2");
       } catch(IllegalArgumentException se){
         th.check(true);
       }
       DatagramSocket ds1 = new DatagramSocket(11233,laddr);
       DatagramPacket p = new DatagramPacket(s.getBytes(),0, s.length(), laddr, 11234);
       recvp = new DatagramPacket(new byte[128],128);
       ds1.send(p);
       try {
         Thread.sleep(100);
       } catch(InterruptedException ie){}
       ds.send(p);
       ds.receive(recvp);
       th.check(new String(recvp.getData(), recvp.getOffset(), recvp.getLength()), s, "checking data -- 2");
       th.check(recvp.getAddress(), laddr);
       th.check(recvp.getPort(), 11234);
       th.check(recvp.getLength(), s.length());
       try {
         th.debug("DEBUGGING - receiving 'null'");
         ds.receive(null);
         th.fail("should throw an Exception");
       } catch(NullPointerException np){
         th.check(true);
       }
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
* implemented. <br>
* @see for more tests look at receive !
*/
  public void test_send(){
    th.checkPoint("send(java.net.DatagramPacket)void");
    DatagramSocket ds = null;
    try {
       ds = new DatagramSocket();
       try {
         ds.send(null);
         th.fail("should throw an Exception");
       } catch(NullPointerException np){
         th.check(true);
       }
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
*   not implemented. <br>
*
*/
  public void test_connect(){
    th.checkPoint("connect(java.net.InetAddress,int)void");
    DatagramSocket ds = null;
    try {
       ds = new DatagramSocket();
       InetAddress laddr = InetAddress.getLocalHost();
       th.check(ds.getInetAddress(), null ,"check value -- 1");
       ds.connect(laddr, 12345);
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
*   not implemented. <br>
*   @see connect, getPort and getInetAddress
*/
  public void test_disconnect(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
*
*/
  public void test_getInetAddress(){
    th.checkPoint("getInetAddress()java.net.InetAddress");
    DatagramSocket ds = null;
    try {
       ds = new DatagramSocket();
       InetAddress laddr = InetAddress.getLocalHost();
       th.check(ds.getInetAddress(), null ,"check value -- 1");
       ds.connect(laddr, 12345);
       th.check(ds.getInetAddress(), laddr ,"check value -- 2");
       ds.disconnect();
       th.check(ds.getInetAddress(), null ,"check value -- 3");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
* implemented. <br>
*
*/
  public void test_getPort(){
    th.checkPoint("getPort()int");
    DatagramSocket ds = null;
    try {
       ds = new DatagramSocket();
       InetAddress laddr = InetAddress.getLocalHost();
       th.check(ds.getPort(), -1 ,"check value -- 1");
       ds.connect(laddr, 12345);
       th.check(ds.getPort(), 12345 ,"check value -- 2");
       ds.disconnect();
       th.check(ds.getPort(), -1 ,"check value -- 3");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

/**
*  not implemented. <br>
*  @see test in constructor
*/
  public void test_getLocalAddress(){
    th.checkPoint("()");

  }

/**
*  not implemented. <br>
*  @see test in constructor
*/
  public void test_getLocalPort(){
    th.checkPoint("()");

  }

/**
* implemented. <br>
* - it is important to verify the address of the sender ...
* --> this is iportant since it can create security holes
*/

/**
* implemented. <br>
*
* The Socket will have 1 of three addresses:
*  1. 127.0.0.1
*  2. REAL IP address
*  3. 0.0.0.0
*  the goal of these test is to see if the Socket receives a packet sent to one of these addresses
*  2 combinations fail:
*    --> loopback to real address
*    --> real address to loopback
*/
  public void test_behaviour(){
    th.checkPoint("send/receive");
    th.debug("testing send/receive");
    DatagramSocket ds = null;
    try {
       int port = 11345;
       InetAddress laddr = InetAddress.getLocalHost();
       InetAddress loopb = InetAddress.getByName("127.0.0.1");
       InetAddress zeros = InetAddress.getByName("0.0.0.0");
       InetAddress multi = InetAddress.getByName("225.0.0.1");
       String la = "Send to ";
       String lb = la+loopb;
       String zr = la+zeros;
       String mc = la+multi;
       la = la+laddr;
       th.debug(laddr+", "+loopb+", "+zeros);
       DatagramPacket ladp = new DatagramPacket(la.getBytes(), la.length(), laddr, port);
       DatagramPacket lbdp = new DatagramPacket(lb.getBytes(), lb.length(), loopb, port);
       DatagramPacket zrdp = new DatagramPacket(zr.getBytes(), zr.length(), zeros, port);

       if(laddr.equals(loopb)){
         th.debug("getLocalHost returned the loopback address !");
       }
       ds = new DatagramSocket(port++,zeros);
       ds.setSoTimeout(1000);
       checkSendReceive(ds, ladp, la, 1,false);
       checkSendReceive(ds, lbdp, lb, 2,false);
       //checkSendReceive(ds, zrdp, zr, 3,false);
       close(ds);

       setPorts(ladp, lbdp, zrdp, port);
       ds = new DatagramSocket(port++,laddr);
       ds.setSoTimeout(1000);
       checkSendReceive(ds, ladp, la, 4,false);
       checkSendReceive(ds, lbdp, lb, 5,true);
       //checkSendReceive(ds, zrdp, zr, 6,false);
       close(ds);

       setPorts(ladp, lbdp, zrdp, port);
       ds = new DatagramSocket(port,loopb);
       ds.setSoTimeout(1000);
       checkSendReceive(ds, ladp, la, 7,true);
       checkSendReceive(ds, lbdp, lb, 8,false);
       //checkSendReceive(ds, zrdp, zr, 9,false);

       ds.setSoTimeout(10);
       try {
         ds.receive(new DatagramPacket(new byte[1],1));
         th.fail("should have thrown an InterruptedIOException");
       }
       catch(InterruptedIOException iioe){
         th.check(true);
       }
       catch(IOException ioe){
         th.fail("threw wrong exception "+ioe);
       }
       ds.close();

       th.checkPoint("send/receive -- 2");
       /*
       DatagramPacket mcdp = new DatagramPacket(mc.getBytes(), mc.length(), zeros, port);       
       ds = new DatagramSocket(port,multi);
       ds.setSoTimeout(50);
       checkSendReceive(ds, mcdp, mc, 10,true);
       checkSendReceive(ds, zrdp, zr, 11,true);
       */
    }
    catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ds);
  }

  private void checkSendReceive(DatagramSocket ds, DatagramPacket dp, String msg, int count, boolean bad){
    DatagramPacket recv = new DatagramPacket(new byte[256], 256);
    try {
      ds.send(dp);
      ds.receive(recv);
      th.check(recv.getLength(), msg.length(), "checking length -- "+count);
      th.check(new String(recv.getData(),0,recv.getLength()), msg, "checking bytes -- "+count);
      th.check(recv.getPort(), ds.getLocalPort(), "checking local port "+count);
    }
    catch(Exception e){
      th.check(bad,"SendReceive "+count+" failed due to "+e);
    }
  }

  private void setPorts(DatagramPacket dp1, DatagramPacket dp2, DatagramPacket dp3,int port){
    dp1.setPort(port);
    dp2.setPort(port);
    dp3.setPort(port);
  }
}
