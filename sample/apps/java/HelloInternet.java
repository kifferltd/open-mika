import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.ServerSocket;

public class HelloInternet {

  public static void main(String[] args) throws IOException {
    // Open a listening socket
    System.out.println("Listening to port 1234");
    ServerSocket ss = new ServerSocket(1234);
    while (true) {
      try {
        // Wait for a connection to come in
        Socket s = ss.accept();
        System.out.println("Accepted a connection");
        // Create a Writer to the socket, with autoflush=true
        PrintWriter w = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
        // Create a Reader from the socket
        BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
        // Print a greeting to the socket and invite input.
        w.println("Hello, Internet Being. What is your name?");
        // Read the response
	String name = r.readLine();
        // Echo it back
        w.println("Goodbye, " + name);
	s.close();
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}

