/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package wonka.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ThreadSerializationTest extends Thread implements Serializable {

  public static void main(String[] args) {
    File f = new File(args[1]);
    if ("write".equals(args[0])) {
      ThreadSerializationTest tst = new ThreadSerializationTest();
      tst.start();
      try {
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tst);
        oos.close();
        fos.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    else if ("read".equals(args[0])) {
      try {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object foo = ois.readObject();
        ois.close();
        fis.close();
	System.gc();
	System.gc();
	System.gc();
	System.out.println("Read " + foo);
	System.gc();
	System.gc();
	System.gc();
	((Thread)foo).start();
	System.gc();
	System.gc();
	System.gc();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      catch (java.lang.ClassNotFoundException cnfe) {
        cnfe.printStackTrace();
      }
    }
  }

  public void run() {
    try {
      System.out.println("Starting " + this);
      sleep(60000);
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }
}

