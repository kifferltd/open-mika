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
    System.out.println("Connecting to port 587 of kiffer.ltd.uk");
    Socket s = new Socket("kiffer.ltd.uk", 587);
    try {
      // Create a Reader from the socket
      BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
      // Read the SMTP server banner
      String banner = r.readLine();
      System.out.println("Received: " + banner);
      System.out.println("Closing the connection");
      s.close();
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
    finally {
      s.close();
    }
    // Open a listening socket
    System.out.println("Listening to port 1234");
    ServerSocket ss = new ServerSocket(1234);
    while (true) {
      // Wait for a connection to come in
      Socket cs = ss.accept();
      System.out.println("Accepted a connection");
      try {
        // Create a Writer to the socket, with autoflush=true
        PrintWriter w = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()), true);
        // Create a Reader from the socket
        BufferedReader r = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        // Print a greeting to the socket and invite input.
        w.println("Hello, Internet Being. What is your name?");
        // Read the response
        String name = r.readLine();
        w.println("Nice to meet you, " + name + ".");
        w.println("I will now echo back everything you type (use ^C to exit);");
        while (true) {
          String line = r.readLine();
          w.println(line);
        }
      }
      catch (IOException ioe) {
        // ignore, we expect user to send ^C
      }
      finally {
        cs.close();
      }
    }
  }
}

