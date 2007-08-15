/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of other           *
*    contributors may be used to endorse or promote products derived      *
*    from this software without specific prior written permission.        *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

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

