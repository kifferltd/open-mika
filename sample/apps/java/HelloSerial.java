import java.io.*;
import javax.comm.*;

public class HelloSerial {

  public static void main(String[] args) {
    // Prints "Hello, Serial World" to the debug UART.
    CommPortIdentifier cpi;
    SerialPort sp = null;
        
    try {
      cpi = CommPortIdentifier.getPortIdentifier("COM3");
      sp = (SerialPort)cpi.open("HelloSerial app", 100);

      sp.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
      sp.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

      OutputStream os = sp.getOutputStream();
      PrintStream ps = new PrintStream(os);
      ps.println("Hello, Serial World");
    }
    catch (Exception e) {
      System.err.print("Exception thrown : " + e);
      e.printStackTrace();
    }
    finally {
      if (sp != null) {
        sp.close();
      }
    }
  }
}

