/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: UARTDriver.java,v 1.5 2006/10/04 14:24:21 cvsroot Exp $
*/

package org.open_mika.device.uart;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.comm.CommDriver;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import wonka.vm.Wonka;

public class UARTDriver implements CommDriver {

  private static UARTDriver singleton = null;

  protected UARTDriver() {
    try {
      InputStream s = ClassLoader.getSystemResourceAsStream("device.config");
      BufferedReader r = new BufferedReader(new InputStreamReader(s));
      String line;
      while ((line = r.readLine()) != null) {
        int start;
        int end;
        while ((start = line.indexOf("  ")) != -1) {
          line = line.substring(0, start) + line.substring(start + 1);
        }
        while ((start = line.indexOf("( ")) != -1) {
          end = line.indexOf(")", start + 2);
          if (end > start) {
            line = line.substring(0, start) + line.substring(end + 1);
          }
          else {
            line = line.substring(0, start);
          }
        }
        StringTokenizer t = new StringTokenizer(line);
        while (t.hasMoreTokens()) {
          String command = t.nextToken();
          if (command.toLowerCase().equals("attach-serial-device")) {
            attachSerialDevice(t);
          }
          else if (command.toLowerCase().equals("register-serial-device")) {
            registerSerialDevice(t);
          }
        }
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void attachSerialDevice(StringTokenizer t) {
    try {
      String family = t.nextToken();
      String number = t.nextToken();
      String path = t.nextToken();
      attachSerialDevice0(family, Integer.parseInt(number), path);
    }
    catch (NoSuchElementException e) {
      System.err.println("attachSerialDevice : syntax is attach-serial-device <family> <number> <path>");
    }
    catch (NumberFormatException nfe) {
      System.err.println("attachSerialDevice : illegal device number");
    }
  }

  private void registerSerialDevice(StringTokenizer t) {
    try {
      String name = t.nextToken();
      String family = t.nextToken();
      String number = t.nextToken();
      registerSerialDevice0(name, family, Integer.parseInt(number));
    }
    catch (NoSuchElementException e) {
      System.err.println("registerSerialDevice : syntax is register-serial-device <name> <family> <number>");
    }
    catch (NumberFormatException nfe) {
      System.err.println("registerSerialDevice : illegal device number");
    }
  }

  private static native void registerSerialDevice0(String name, String family, int number);

  private static native void attachSerialDevice0(String family, int number, String path);

  private native static String firstUARTname();

  private native static String nextUARTname();

  public void initialize() {
    String portname = firstUARTname();

    while (portname != null) {
      CommPortIdentifier.addPortName(portname,CommPortIdentifier.PORT_SERIAL,this);
      portname = nextUARTname();
    }
  }

  public static synchronized UARTDriver getInstance() {
    if (singleton == null) {
      singleton = new UARTDriver();
    }
    return singleton;
  }

  public CommPort getCommPort (String portName, int portType) 
    throws IllegalArgumentException
  {
    if (portType == CommPortIdentifier.PORT_SERIAL) {
      try {
        return new UART(portName);
      }
      catch (IOException ioe) {
        return null;
      }

    }
    else {

      throw new IllegalArgumentException();

    }
  }
}

