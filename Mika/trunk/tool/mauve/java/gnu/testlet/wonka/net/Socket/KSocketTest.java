// Tags: JDK1.4

/*
   Copyright (C) Chris Gray, /k/ Embedded Java Solutions

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

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.net.Socket");
    test_closeSocket();
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

