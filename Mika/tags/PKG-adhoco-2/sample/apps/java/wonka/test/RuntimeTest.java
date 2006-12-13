package wonka.test;

import java.lang.InterruptedException;

public class RuntimeTest {
	public static void main(String[] argv) {
		System.out.println("Starting");
		try {
			Runtime.getRuntime().exec("date 10101010");
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Stopping");
		System.exit(0);
	}
}
