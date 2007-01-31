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

