package wonka.test;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.InterruptedException;

public class TimerTest {
	private static final int NB = 10;
	int index;
	long i[];
	long j[];

	private class TestTimerTask extends TimerTask {
		public void run() {
			j[TimerTest.this.index] = System.currentTimeMillis();
			System.out.println("delta = " + (j[TimerTest.this.index] - i[TimerTest.this.index]));
			synchronized(TimerTest.this) {
				TimerTest.this.notify();
			}
		}
	}

	public static void main(String[] argv) {
		System.out.println("Starting");
		int delta = 1000; 
		if(argv.length == 1)
			delta = Integer.parseInt(argv[0]);
		new TimerTest().test(delta);
		System.out.println("Stopping");
		System.exit(0);
	}
	
	private void test(int delta) {
		this.i = new long[NB];
		this.j = new long[NB];
		System.out.println(NB+" tries with delta = "+delta+"ms.");
		try {
		  Timer t = new Timer();
			for(index = 0; index < NB; index++) { 
				synchronized(this) {
					// was here : Timer t = new Timer();
					System.out.println("scheduling task # "+index);
					this.i[index] = System.currentTimeMillis();
					t.schedule(new TestTimerTask(), delta);
					this.wait();
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		long res = 0;
		for(index = 0; index < NB; index++) {
			res += this.j[index] - this.i[index];
		}
		System.out.println("average delta = " + res/NB);
	}
}
