package wonka.test;

import java.util.HashSet;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.Serializable;

public class UnserializeTest implements Serializable {

	private class DummyClass implements Serializable {
		int value;
	
		public DummyClass(int value) {
			this.value = value;
		}
	}

	public static void main(String[] argv) {
		System.out.println("Starting");
		new UnserializeTest().test();
		System.out.println("Stopping");
		System.exit(0);
	}
	
	private void test(){
		HashSet h = new HashSet();
		HashSet j = new HashSet();
		for(int i = 0; i<10; i++)
			h.add(new DummyClass(i));
			
		// Unserialize data
		try {
			FileOutputStream fo = new FileOutputStream("tmp");
			ObjectOutput oo = new ObjectOutputStream(fo);
			oo.writeObject(h);
			oo.close();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileInputStream fi = new FileInputStream("tmp");
			ObjectInput oi = new ObjectInputStream(fi);
			j = (HashSet) oi.readObject();
			oi.close();
			fi.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
