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


/*
** $Id: Chris.java,v 1.2 2006/03/03 11:43:32 cvs Exp $
*/
package wonka.test;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

class Chris implements Runnable {

  public void run() {
    System.out.println("Farewell world");
  }

  public static void main(String[] args) {
    int count = 0;

    Runtime rt = Runtime.getRuntime();

    try {
      java.net.URL u = new java.net.URL("http://www.adhoco.com/time.php");
      while(true) {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection)u.openConnection();
        int rc = c.getResponseCode();
	if (rc == 200) {
        System.out.println("Connected, response code is 200");
        java.io.InputStream is = c.getInputStream();
        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
        String s = br.readLine();
        while (s != null) {
	    System.out.println(s);
	    s = br.readLine();
        }
        System.out.println("=============================================");
	}
	else {
          System.out.println("Connection failed, response code is " + rc);
	}
	Thread.sleep(1000);
      }
    } catch (Throwable t) {
	    t.printStackTrace();
    }
    /*
    java.io.File f = new java.io.File("{}/data/foo/");
    System.out.println("Test 1 returned " + f.mkdirs());
    f = new java.io.File("{}/data/sir/john");
    System.out.println("Test 2 returned " + f.mkdirs());
    rt.gc();
    rt.runFinalization();

    
    System.out.println("Version 1: read first, then get exit code");
    try {
      Process p = rt.exec("fsroot/script");
      java.io.InputStream is = p.getInputStream();
      java.io.BufferedReader br = new BufferedReader(new InputStreamReader(is));
      System.out.println("============================================");
      String line = br.readLine();
      while (line != null) {
        System.out.println(line);
        line = br.readLine();
      }
      System.out.println("============================================");
      System.out.println("subprocess returned " + p.waitFor());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Version 2: get exit code first, then read");
    try {
      Process p = rt.exec("fsroot/script");
      java.io.InputStream is = p.getInputStream();
      java.io.BufferedReader br = new BufferedReader(new InputStreamReader(is));
      System.out.println("subprocess returned " + p.waitFor());
      System.out.println("============================================");
      String line = br.readLine();
      while (line != null) {
        System.out.println(line);
        line = br.readLine();
      }
      System.out.println("============================================");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0);

    System.out.println("java.vm.version = "+System.getProperty("java.vm.version"));
    System.out.println("Heap size: "+rt.totalMemory());
    System.out.println("Memory available: "+rt.freeMemory());
    rt.gc();
    System.out.println("Memory available: "+rt.freeMemory());

    long j = 0;
    long t0 = System.currentTimeMillis();
    for (int i = 0; i < 100000; ++i) {
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
      ++j;
    }
    long t1 = System.currentTimeMillis();
    System.out.println("t0 = " + t0 + ", t1 = " + t1);
    System.out.println("Performed "+j+" increments in "+(t1-t0)+" milliseconds");
    */

    /*
    j = 0;
    t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000; ++i) {
      System.out.println("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
      ++j;
    }
    t1 = System.currentTimeMillis();
    System.out.println("Printed "+j+" lines ("+(j*64)+" bytes) in "+(t1-t0)+" milliseconds");
    */

    /*
    System.out.println("java.vm.vendor = "+System.getProperty("java.vm.vendor"));
    System.out.println("java.vm.name = "+System.getProperty("java.vm.name"));
    System.out.println("java.class.version = "+System.getProperty("java.class.version"));
    System.out.println("java.class.path = "+System.getProperty("java.class.path"));
    System.out.println("java.ext.dirs.path = "+System.getProperty("java.ext.dirs"));
    System.out.println("os.name = "+System.getProperty("os.name"));
    System.out.println("os.arch = "+System.getProperty("os.arch"));
    System.out.println("os.version = "+System.getProperty("os.version"));
    System.out.println("os.name = "+System.getProperty("user.name"));
    System.out.println("user.home = "+System.getProperty("user.home"));
    System.out.println("user.dir = "+System.getProperty("user.dir"));

    int jj = 0;
    t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000000; ++i) {
      synchronized(rt) {
        ++jj;
      }
    }
    t1 = System.currentTimeMillis();
    System.out.println("Performed "+jj+" synchronized blocks in "+(t1-t0)+" milliseconds");

    try {
      Thread.sleep(1000000);
    }
    catch (InterruptedException ie) {}

    for (int i = 0; i < 10; ++i) {
    byte[][] blob = new byte[10][1000000];
    System.out.println("Memory available: "+rt.freeMemory());
    long t2 = System.currentTimeMillis();
    blob = null;
    rt.gc();
    long t3 = System.currentTimeMillis();
    System.out.println("gc() took "+(t3-t2)+" milliseconds");
    System.out.println("Memory available: "+rt.freeMemory());
    }

    if (args.length > 0) {
      StringBuffer sentence = new StringBuffer();
      for (int i = 0; i < args.length - 1; ++i) {
        sentence.append(args[i]);
        sentence.append(' ');
      }
      sentence.append(args[args.length - 1]);
      Carillon.getInstance(sentence.toString()).setDaemon(true);
    }
    else {
      Carillon.getInstance("Multithreaded programming is difficult and error prone. It is easy to make a mistake in synchronization that produces a data race, yet it can be extremely hard to locate this mistake during debugging.").setDaemon(true);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(new Chris()));
    */

  }

}

