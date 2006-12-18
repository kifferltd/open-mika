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


package gnu.testlet.wonka.net.MulticastSocket;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.io.IOException;
import java.io.InterruptedIOException;


public class AcuniaMulticastSocketTest implements Testlet {
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.MulticastSocket");
    test_MulticastSocket();
    test_joinGroup();
    test_leaveGroup();
    test_send();
    test_TimeToLive();
    test_getInterface();
    test_setInterface();
    test_behaviour();
  }

  /**
  * helper functions to make sure all sockets get closed ...
  */
  private void close(MulticastSocket ms){
    if (ms != null){
      ms.close();
    }
  }

  /**
  * checks if the constructor fails using the port parameter
  */
  private void checkConstructFails(int port, int count){
    try {
      new MulticastSocket(port);
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
  public void test_MulticastSocket(){
    th.checkPoint("MulticastSocket()");
    MulticastSocket ms = null;
    try {
       ms = new MulticastSocket();
       th.check(ms.getLocalPort() >= 1024 ,"port should not be in the reserved area below 1024, got "+ms.getLocalPort());
       th.check(ms.getLocalAddress(), InetAddress.getByName("0.0.0.0"), "cheking local address");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ms);

    th.checkPoint("MulticastSocket(int)");
    checkConstructFails(-1, 1);
    checkConstructFails(65536, 2);
    try {
       ms = new MulticastSocket(33333);
       th.check(ms.getLocalPort(), 33333 ,"checking port number");
       th.check(ms.getLocalAddress(), InetAddress.getByName("0.0.0.0"), "cheking local address");
    } catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ms);
  }

  /**
  * sends a packet to the specified socket and then tries to recieve a packet ...
  */
  private void checkSendReceive(MulticastSocket ms, DatagramPacket dp, String msg, int count, boolean bad){
    DatagramPacket recv = new DatagramPacket(new byte[256], 256);
    try {
      ms.send(dp);
      ms.receive(recv);
      th.check(recv.getLength(), msg.length(), "checking length -- "+count);
      th.check(new String(recv.getData(),0,recv.getLength()), msg, "checking bytes -- "+count);
      th.check(ms.getLocalPort(), recv.getPort(), "checking port -- "+count);
    }
    catch(Exception e){
      th.check(bad,"SendReceive "+count+" failed due to "+e);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_joinGroup(){
    th.checkPoint("joinGroup(java.net.InetAddress)void");
    MulticastSocket ms = null;
    int port = 33334;
    try {
       ms = new MulticastSocket(port);
       InetAddress multi = InetAddress.getByName("225.0.0.1");
       InetAddress multi2 = InetAddress.getByName("239.0.0.1");
       String mc = "Send to ";
       String mc2 = mc+multi2;
       mc = mc+multi;
       DatagramPacket mcdp = new DatagramPacket(mc.getBytes(), mc.length(), multi, port);
       DatagramPacket mc2dp = new DatagramPacket(mc2.getBytes(), mc2.length(), multi2, port);
       ms.joinGroup(multi);
       ms.setSoTimeout(1000);
       checkSendReceive(ms, mcdp, mc, 1, false);
       ms.joinGroup(multi2);
       ms.setSoTimeout(500);
       checkSendReceive(ms, mc2dp, mc2, 2, false);
       checkSendReceive(ms, mcdp, mc, 3, false);
       ms.setSoTimeout(50);
       mcdp.setPort(1+port);
       checkSendReceive(ms, mcdp, mc, 4, true);
       ms.leaveGroup(multi);
       mcdp.setPort(port);
       checkSendReceive(ms, mcdp, mc, 5, true);
       try {
         ms.joinGroup(null);
         th.fail("should throw a NullPointerException");
       }
       catch(NullPointerException np){
         th.check(true);
       }
       try {
         ms.joinGroup(InetAddress.getByName("223.255.255.255"));
         th.fail("should throw an Exception");
       }
       catch(SocketException np){
         th.check(true);
       }
       try {
         ms.joinGroup(InetAddress.getByName("240.0.0.0"));
         th.fail("should throw an Exception");
       }
       catch(SocketException np){
         th.check(true);
       }
    }
    catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ms);
  }

/**
* implemented. <br>
* @see also joinGroup
*/
  public void test_leaveGroup(){
    th.checkPoint("leaveGroup(java.net.InetAddress)void");
    MulticastSocket ms = null;
    int port = 33336;
    try {
       ms = new MulticastSocket(port);
       try {
         ms.leaveGroup(null);
         th.fail("should throw a NullPointerException");
       }
       catch(NullPointerException np){
         th.check(true);
       }
       try {
         ms.leaveGroup(InetAddress.getByName("223.255.255.255"));
         th.fail("should throw an Exception");
       }
       catch(SocketException np){
         th.check(true);
       }
       try {
         ms.leaveGroup(InetAddress.getByName("240.0.0.0"));
         th.fail("should throw an Exception");
       }
       catch(SocketException np){
         th.check(true);
       }
       try {
         ms.leaveGroup(InetAddress.getByName("234.0.0.0"));
         th.fail("should throw an Exception");
       }
       catch(SocketException np){
         th.check(true);
       }
    }
    catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ms);
  }

  private void checkSendReceive(MulticastSocket ms, DatagramPacket dp, String msg, int count, boolean bad, byte TTL){
    DatagramPacket recv = new DatagramPacket(new byte[256], 256);
    try {
      ms.send(dp,TTL);
      ms.receive(recv);
      th.check(recv.getLength(), msg.length(), "checking length -- "+count);
      th.check(new String(recv.getData(),0,recv.getLength()), msg, "checking bytes -- "+count);
    }
    catch(Exception e){
      th.check(bad,"SendReceive "+count+" failed due to "+e);
    }
  }

/**
* implemented. <br>
*
*/
  public void test_send(){
    th.checkPoint("send(java.net.DatagramPacket,byte)void");
    MulticastSocket ms = null;
    int port = 33335;
    try {
      ms = new MulticastSocket(port);
      InetAddress multi = InetAddress.getByName("225.0.0.1");
      InetAddress local = ms.getLocalAddress();
      String mc = "Send to ";
      String lc = mc+local;
      mc = mc+multi;
      DatagramPacket mcdp = new DatagramPacket(mc.getBytes(), mc.length(), multi, port);
      DatagramPacket lcdp = new DatagramPacket(lc.getBytes(), lc.length(), local, port);
      ms.setSoTimeout(1000);
      ms.setTimeToLive(2);
      ms.joinGroup(multi);
      checkSendReceive(ms, lcdp, lc, 1, false,(byte)3);
      th.check(ms.getTimeToLive(),2,"checking ttl -- 1");
      checkSendReceive(ms, mcdp, mc, 1, false,(byte)1);
      th.check(ms.getTimeToLive(),2,"checking ttl -- 2");
    }
    catch(Exception e){
      th.fail("unwanted exception caught :"+e);
    }
    close(ms);
  }



/**
* implemented. <br>
* - check get/setTimeToLive()
* - checks get/setTTL()
*/
  public void test_TimeToLive(){
    th.checkPoint("getTimeToLive()int");
    MulticastSocket ms = null;
    try {
      ms = new MulticastSocket();
      th.check(ms.getTimeToLive(), 1, "check default value");
      th.checkPoint("setTimeToLive(int)void");
      ms.setTimeToLive(10);
      th.check(ms.getTimeToLive(), 10, "check  value -- 1");
      ms.setTimeToLive(255);
      th.check(ms.getTimeToLive(), 255, "check value -- 2");
      ms.setTimeToLive(1);
      th.check(ms.getTimeToLive(), 1, "check value -- 3");
      ms.setTTL((byte)(-1));
      th.check(ms.getTimeToLive(), 255, "check value -- 4");
      th.check(ms.getTTL(), -1, "check value -- 5");
      ms.setTTL((byte)(1));
      th.check(ms.getTimeToLive(), 1, "check value -- 6");
      th.check(ms.getTTL(), 1, "check value -- 7");
      try {
        ms.setTimeToLive(-1);
        th.fail("illegal argument passed -- 1");
      }
      catch(IllegalArgumentException iae){
        th.check(true);
      }
      try {
        ms.setTimeToLive(256);
        th.fail("illegal argument passed -- 2");
      }
      catch(IllegalArgumentException iae){
        th.check(true);
      }

    } catch(Exception e){
      th.fail("got unwanted exception "+e);
    }
    close(ms);
  }

/**
* not implemented. <br>
* @see SocketOptions test
*/
  public void test_getInterface(){
  }

/**
* not implemented. <br>
* @see SocketOptions test
*/
  public void test_setInterface(){
  }





/**
* not implemented. <br>
*
*/
  public void test_behaviour(){
  }
}
