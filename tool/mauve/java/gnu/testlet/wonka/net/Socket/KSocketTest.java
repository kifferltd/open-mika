// Tags: JDK1.4

/*
   Copyright (C) 2006, 2016 Chris Gray, KIFFER Ltd.

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

package gnu.testlet.wonka.net.Socket;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.net.*;
import java.util.Vector;
import java.io.*;

public class KSocketTest implements Testlet {

  protected TestHarness th;

  private static String server = "www.k-embedded-java.com";
  private static int port = 80;

  private Object lock = new Object();
  private boolean reading;
  private boolean readInterrupted;
  private String ifconfig;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.Socket");
    ifconfig = getIfconfigOutput();
    test_closeSocket();
    // We run the following test several times to be sure that a hanging
    // connection does not interfere with subsequent attempts
    test_timedConnect(1);
    test_timedConnect(2);
    test_timedConnect(3);
    test_getLocalAddress();
    test_getLocalSocketAddress();
  }

  private String getIfconfigOutput() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final Runtime r = Runtime.getRuntime();
    try {
      final Process p = r.exec("ifconfig");
      new Thread(new Runnable() {
        public void run() {
          try {
            InputStream is = p.getInputStream();
            int ch = is.read();
            while (ch >= 0) {
              baos.write(ch);
              ch = is.read();
            }
          }
          catch (IOException ioe) {
            System.err.println("error when reading from ifconfig stdout");
            ioe.printStackTrace();
          }
        }
      }, "readStdout").start();
      new Thread(new Runnable() {
        public void run() {
          try {
            InputStream is = p.getErrorStream();
            int ch = is.read();
            while (ch >= 0) {
              ch = is.read();
            }
          }
          catch (IOException ioe) {
            System.err.println("error when reading from ifconfig stdout");
            ioe.printStackTrace();
          }
        }
      }, "readStderr").start();
      int rc = p.waitFor();
      if (rc != 0) {
        System.err.println("exec ifconfig failed with return code " + rc);
      }
    }
    catch (IOException ioe) {
      System.err.println("error when executing ifconfig");
      ioe.printStackTrace();
      return null;
    }
    catch (InterruptedException ie) {
      System.err.println("interrupted!");
    }
    System.out.println("ifconfig output:\n\n" + baos);
    return baos.toString();
  }

  private void test_closeSocket() {
    SocketTest test = new SocketTest(server, port);
    try {
        new Thread(new Runnable() {
          public void run() {
            try {
              long t0 = System.currentTimeMillis();
              synchronized(lock) {
                while (!readInterrupted && System.currentTimeMillis() - t0 < 2000) {
                  lock.wait();
                }
                if (!readInterrupted) {
                  th.fail("close() failed tu interrupt read()");
                }
              }
            } catch (InterruptedException ie) {
              ie.printStackTrace();
            }
          }
        }).start();
      synchronized(lock) {
        test.start();
        while (!reading) {
          lock.wait();
        }
        // wait a bit longer so the read() is really executing
        lock.wait(1000);
        test.closeSocket();
        while (!readInterrupted) {
          lock.wait();
        }
      }
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }

  }

  private void test_timedConnect(int n) {
    try {
      InetAddress inetAddress = InetAddress.getByName("google.com");
      SocketAddress socketAddress = new InetSocketAddress(inetAddress, 22);
      Socket s = new Socket();
      s.connect(socketAddress, 1000);
      th.fail("Apparently  google.com allowed us to connect to port 22?  Must be something wrong here. (Attempt #" + n + ")");
    }
    catch (UnknownHostException uhe) {
      th.fail("Unable to locate google.com - is DNS set up properly? (Attempt #" + n + ")");
    }
    catch (SocketTimeoutException ste) {
      // Expected result
    }
    catch (IOException ioe) {
        ioe.printStackTrace();
        th.fail("Unexpected IOException (Attempt #" + n + ")");
    }
  }

  private void test_getLocalAddress() {
    try {
      Socket s = new Socket();
      final InetAddress unboundLocalAddress = s.getLocalAddress();
System.out.println("unboundLocalAddress = " + unboundLocalAddress.getClass() + " " + unboundLocalAddress);
try {
System.out.println(unboundLocalAddress.getClass().getMethod("isAnyLocalAddress", new Class[0]));
} catch (Exception e) {e.printStackTrace();}
      if (!unboundLocalAddress.isAnyLocalAddress()) {
        th.fail("local address of an unbound socket should be the wildcard address");
      }
      s.connect(new InetSocketAddress("kiffer.ltd.uk", 80));
      final InetAddress localAddress = s.getLocalAddress();
      final byte[] localAddressBytes = localAddress.getAddress();
      String localNumericAddress = Integer.toString(localAddressBytes[0] & 0xff);
      for (int i = 1; i < localAddressBytes.length; ++i) {
        localNumericAddress += "." + Integer.toString(localAddressBytes[i] & 0xff);
      }
      System.out.println("getLocalAddress.getHostAddress().getAddress(): " + localNumericAddress);
      if (ifconfig.indexOf(localNumericAddress) < 0) {
        th.fail("could not find IP of socket local address (" + localNumericAddress + ") anywhere in ifconfig output");
      }
      // Not testing getHostName() as no reliable way to get own host name
    }
    catch (UnknownHostException uhe) {
      th.fail("Unable to locate kiffer.ltd.uk - is DNS set up properly?");
    }
    catch (IOException ioe) {
        ioe.printStackTrace();
        th.fail("Unexpected IOException");
    }
  }

  private void test_getLocalSocketAddress() {
    try {
      Socket s = new Socket();
      System.out.println(s.getLocalSocketAddress());
      if (s.getLocalSocketAddress() != null) {
        th.fail("local socket address of an unbound socket should be null");
      }
      s.connect(new InetSocketAddress("kiffer.ltd.uk", 80));
      final SocketAddress localSocketAddress = s.getLocalSocketAddress();
      // Not testing getHostName() as no reliable way to get own host name
    }
    catch (UnknownHostException uhe) {
      th.fail("Unable to locate kiffer.ltd.uk - is DNS set up properly?");
    }
    catch (IOException ioe) {
        ioe.printStackTrace();
        th.fail("Unexpected IOException");
    }
  }

  private class SocketTest extends Thread {
    private Socket socket;

    SocketTest(String server, int port) {
      try {
        this.socket = new Socket(server, port); 
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
        th.fail("Unable to create connection to " + socket + ":" + port + " - check your network settings");
      }
    }
  
    public void run() {
      try {
        while (!isInterrupted()) {
          InputStream i = this.socket.getInputStream();
  
          // Read incoming data and interpret it as a frame
          synchronized (lock) {
            reading = true;
            lock.notifyAll();
          }
          i.read();
        }
  
      } catch (IOException e) {
        synchronized (lock) {
          readInterrupted = true;
          lock.notifyAll();
        }
      }
    }

    public void closeSocket() {
      // close streams and socket
      if (this.socket != null) {
        try {
          InputStream in = this.socket.getInputStream();
          if (in != null) {
            in.close();
          }
        } catch (IOException e) {
          th.fail("Failed to close connection input stream : " + e.toString());
          e.printStackTrace();
        }

        try {
          this.socket.close();
        } catch (IOException e) {
          th.fail("Failed to close connection socket : " + e.toString());
          e.printStackTrace();
        }
      }
    }
  }
  
}

