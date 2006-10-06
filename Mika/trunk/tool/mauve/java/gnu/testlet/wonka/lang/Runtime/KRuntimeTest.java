/* Copyright (C) 2005 Chris Gray, /k/ Embedded Java Solutions

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

// Tags: JLS1.0

package gnu.testlet.wonka.lang.Runtime;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class KRuntimeTest implements Testlet {
  
  protected static TestHarness th;
  private Process p;
  private boolean finished;

  public void test_basic() {
    Process p = null;
    BufferedReader br = null;
    String line = null;

    th.setclass("java.lang.Runtime");
    th.checkPoint("exec(java.lang.String)java.lang.Process - read, then wait");
    Runtime rt = Runtime.getRuntime();

    // System.out.println("Version 1: read first, then get exit code");
    try {
      p = rt.exec("echo Hello world");
    } catch (Exception e) {
      th.fail("Exception thrown when calling exec(): " + e);
      e.printStackTrace();
    }
    
    try {
      br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      line = br.readLine();
      th.check(line,"Hello world",
          "Expected output 'Hello world', but got '" + line + "'");

    } catch (Exception e) {
      th.fail("Exception thrown when reading input stream: " + e);
      e.printStackTrace();
    }
    try {
      while (line != null) {
        line = br.readLine();
        if (line != null) {
          th.fail("Unexpected extra output: '" + line + "'");
        }
        line = br.readLine();
      }
    } catch (IOException ioe) {
      th.check(true);
    } catch (Exception e) {
      th.fail("Exception thrown when reading input stream: " + e);
      e.printStackTrace();
    }
    try {
      int rc = p.waitFor();
      th.check(rc == 0,
          "Command 'echo Hello world' gave non-zero return code " + rc);
    } catch (Exception e) {
      th.fail("Exception thrown when waiting for Process " + p + ": " + e);
      e.printStackTrace();
    }
    
    th.checkPoint("exec(java.lang.String)java.lang.Process - wait, then read");
    // System.out.println("Version 2: get exit code first, then read");
    try {
      p = rt.exec("echo Hello world");
    } catch (Exception e) {
      th.fail("Exception thrown when calling exec(): " + e);
      e.printStackTrace();
    }
    try {
      int rc = p.waitFor();
      th.check(rc == 0,
          "Command 'echo Hello world' gave non-zero return code " + rc);
    } catch (Exception e) {
      th.fail("Exception thrown when waiting for Process " + p + ": " + e);
      e.printStackTrace();
    }
    try {
      br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      line = br.readLine();
      th.check(line.equals("Hello world"),
          "Expected output 'Hello world', but got '" + line + "'");
    } catch (Exception e) {
      th.fail("Exception thrown when reading input stream: " + e);
      e.printStackTrace();
    }
    try {
      while (line != null) {
        line = br.readLine();
        if (line != null) {
          th.fail("Unexpected extra output: '" + line + "'");
        }
        line = br.readLine();
      }
    } catch (IOException ioe) {
      th.check(true);
    } catch (Exception e) {
      th.fail("Exception thrown when reading input stream: " + e);
      e.printStackTrace();
    }
    
    th.checkPoint("exec(java.lang.String)java.lang.Process - bad command");
    p = null;
    try {
      p = rt.exec("jihlava");
      th.fail("Should not execute bad command !");
    } catch (Exception e) {
      th.check(true, "Exception thrown when calling exec(): " + e);
      // e.printStackTrace();
    }
    
  }

  public void test (TestHarness the_harness) {
    th = the_harness;
    test_basic ();
    try {
      test_destroy();
      test_write();
      test_read();
      test_waitfor();
      test_exitValue();
      test_execArgs();
      p = null;
      Runtime rt = Runtime.getRuntime(); 
      rt.gc();
      rt.runFinalization();
      Thread.sleep(100);
    } catch (Exception e) {
      th.fail(e.toString());
      e.printStackTrace();
    }
  }

  private void test_execArgs() throws Exception {
    th.checkPoint("test_execArgs");
    Runtime rt = Runtime.getRuntime();    
    try {
      String arg = null;
      rt.exec(arg);
      th.fail("'null' is not allowed");
    } catch(NullPointerException  npe) {
      th.check(true);
    }
    try {
      String arg = "";
      rt.exec(arg);
      th.fail("'' is not allowed");
    } catch(IllegalArgumentException  npe) {
      th.check(true);
    }
    try {
      String arg = "olekebolleke";
      Process proc = rt.exec(arg);
      th.fail("'olekebolleke' doesn't exist "+proc);
    } catch(IOException  npe) {
      th.check(true);
    }
    
    try {
      String arg = "echo hallo";
      rt.exec(arg, new String[]{null});
      th.fail("'null' is not allowed");
    } catch(NullPointerException  npe) {
      th.check(true);
    }

    try {
      rt.exec(new String[]{null});
      th.fail("'null' is not allowed");
    } catch(NullPointerException  npe) {
      th.check(true);
    }
    try {
      rt.exec(new String[]{"echo",null});
      th.fail("'null' is not allowed");
    } catch(NullPointerException  npe) {
      th.check(true);
    }
 }

  private void test_exitValue() throws Exception {
    th.checkPoint("test_exitValue");
    Runtime rt = Runtime.getRuntime();    
    Process p = rt.exec("echo Hallo");
    int value = p.waitFor();
    th.check(p.exitValue(), value);
    p = rt.exec("sleep 20");
    try {
      p.exitValue();
      th.fail("process is still runnig --> no exitValue");
    } catch(IllegalThreadStateException itse) {
      th.check(true);
    }
    p.destroy();
  }

  private synchronized void test_waitfor() throws Exception {
    th.checkPoint("test_waitfor");
    Runtime rt = Runtime.getRuntime();    
    p = rt.exec("echo Hallo");
    int value = p.waitFor();
    th.check(p.waitFor(), value);
    Thread t = new Thread(new Runnable(){
      public void run() {
        do_waifortest();        
      }         
    });
    t.start();
    p = rt.exec("sleep 10");
    t.interrupt();
    this.wait();
    t.join();
    p.destroy();
    t = Thread.currentThread();   
    p = rt.exec("sleep 10");
    t.interrupt();
    try {
      p.waitFor();
      th.fail("No InterruptedException thrown");
    } catch (InterruptedException e) {
      th.check(true);
    } 
    p.destroy();
  }

  void do_waifortest() {
    synchronized(this) {
      this.notifyAll();
    }
    try {
      p.waitFor();
      th.fail("No InterruptedException thrown");
    } catch (InterruptedException e) {
      th.check(true);
    }    
  }

  private void test_read() throws Exception {
    // TODO Auto-generated method stub
  }

  private void test_write() throws Exception {
    th.checkPoint("test_write");
    Runtime rt = Runtime.getRuntime();    
    p = rt.exec("echo Hallo");
    p.waitFor();
    OutputStream out = p.getOutputStream();
    out.write(1);
    out.write(new byte[]{1,2,3,4,5,6});
    p = rt.exec("sleep 1000");
    out = p.getOutputStream();
    try {
      out.write(null, 1, 2);
      th.fail("null");
    } catch(NullPointerException npe) {
      th.check(true);
    }
    try {
    out.write(new byte[]{1,2,3,4,5,6}, -1, 6);
    th.fail("index -1");
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      th.check(true);
    }
    try {
    out.write(new byte[]{1,2,3,4,5,6}, 4, 6);
    th.fail("index total");
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      th.check(true);
    }
    try {
    out.write(new byte[]{1,2,3,4,5,6}, 1, -6);
    th.fail("length -6");
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      th.check(true);
    }
    p.destroy();
    p = rt.exec("cat");
    out = p.getOutputStream();
    byte[] bytes = "abcdefghijklmnopqrstuvw".getBytes();
    out.write(bytes);
    out.close();
    p.waitFor();
    InputStream in = p.getInputStream();
    byte[] readed = new byte[bytes.length];
    int off=0;
    int read = in.read(readed);
    while(read < off) {
      if(read == -1) {
        th.fail("didn't get all bytes");
        break;
      }
      off += read;
      read = in.read(readed,off,readed.length -off);
    }
    th.check(Arrays.equals(readed,bytes));
  }

  private synchronized void test_destroy() throws Exception {
    th.checkPoint("test_destroy");
    Runtime rt = Runtime.getRuntime();    
    p = rt.exec("echo Hallo");
    p.waitFor();
    p.destroy();
    p.destroy();
    p.destroy();
    th.check(true);
    finished = false;
    p = rt.exec("sleep 1000");

    System.out.println("KRuntimeTest.test_destroy() "+p);
    
    Thread thread = new Thread(new Runnable(){

      public void run() {
        do_test_destroy();
        
      }
      
    });
    thread.start();
    
    this.wait();
    
    p.destroy();
    p.destroy();
    this.wait(5000);
    th.check(finished, "Process should have finished by now");
  }

   void do_test_destroy() {
     synchronized(this) {
       notifyAll();
     }
     try {
      p.waitFor();
      finished = true;
    } catch (InterruptedException e) {
    }
    synchronized(this) {
      notifyAll();
    }    
  }
}
