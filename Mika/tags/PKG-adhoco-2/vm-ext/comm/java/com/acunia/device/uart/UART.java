/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: UART.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package com.acunia.device.uart;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.TooManyListenersException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

public class UART extends SerialPort implements Runnable {

  private static final int BOGUS_ANSWER = 42;

  private int baudRate;
  private int dataBits;
  private int stopBits;
  private int parity;
  private int flowControlMode;

  private boolean rxThresholdEnabled = false;
  private boolean rxTimeoutEnabled = false;
  private boolean dtr  = false;
  private boolean rts  = false;
  private SerialPortEventListener eventlistener;
  private BitSet  notifyon;

  private UARTInputStream in;
  private UARTOutputStream out;
  private UARTControlStream control;
  private Thread eventthread;


  public UART(String name) 
    throws SecurityException, IOException
  {
    this.name = name;
    control = new UARTControlStream(name);
    try {
      baudRate = control.getbaudrate();
      dataBits = control.getdatabits();
      stopBits = control.getstopbits();
      parity   = control.getparity();
      flowControlMode = control.getflowcontrol();
    }
    catch (Exception e) {}
  }

  public int getBaudRate() {
    if (super.closed) throw new IllegalStateException();

    return baudRate;
  }

  public int getDataBits() {
    if (super.closed) throw new IllegalStateException();

    return dataBits;
  }

  public int getStopBits() {
    if (super.closed) throw new IllegalStateException();

    return stopBits;
  }

  public int getParity() {
    if (super.closed) throw new IllegalStateException();

    return parity;
  }

  public void sendBreak(int millis) {
    if (super.closed) throw new IllegalStateException();

    control.sendbreak(millis);
}


  public void setFlowControlMode (int flowcontrol)
    throws UnsupportedCommOperationException 
  {
    if (super.closed) throw new IllegalStateException();

    if ((flowcontrol & (FLOWCONTROL_NONE|FLOWCONTROL_RTSCTS_IN|FLOWCONTROL_RTSCTS_OUT))!= flowcontrol) {
      throw new UnsupportedCommOperationException();
    }

    control.setflowcontrol(flowcontrol);
    flowControlMode = flowcontrol;
  }


  public int getFlowControlMode() {
    if (super.closed) throw new IllegalStateException();

    return flowControlMode;
  }

  public void setSerialPortParams (int baudrate, int dataBits, int stopBits, int parity)
    throws UnsupportedCommOperationException 
  {
    if (super.closed) throw new IllegalStateException();

    if (dataBits > DATABITS_8 || dataBits < DATABITS_5) {
      throw new UnsupportedCommOperationException();
    }

    if (stopBits < STOPBITS_1 || stopBits > STOPBITS_1_5) {
      throw new UnsupportedCommOperationException();
    }

    if (parity < PARITY_NONE || parity > PARITY_SPACE) {
      throw new UnsupportedCommOperationException();
    }

    if (baudrate<=0) {
      throw new UnsupportedCommOperationException();
    }
    control.setdatabits(dataBits);
    this.dataBits = dataBits;
    control.setstopbits(stopBits);
    this.stopBits = stopBits;
    control.setparity(parity);
    this.parity = parity;
    baudRate = control.setbaudrate(baudrate);
  }

  public void setDTR(boolean newdtr) {
    if (super.closed) throw new IllegalStateException();

    control.setdtr(newdtr);
    dtr = newdtr;
  }

  public boolean isDTR() {
    if (super.closed) throw new IllegalStateException();

    return dtr;
  }

  public void setRTS(boolean newrts) {
    if (super.closed) throw new IllegalStateException();

    control.setrts(newrts);
    rts = newrts;
  }

  public boolean isRTS() {
    if (super.closed) throw new IllegalStateException();

    return rts;
  }

  public boolean isCTS() {
    if (super.closed) throw new IllegalStateException();

    return control.getcts();
  }

  public boolean isDSR() {
    if (super.closed) throw new IllegalStateException();

    return control.getdsr();
  }

  public boolean isRI() {
    if (super.closed) throw new IllegalStateException();

    return control.getri();
  }

  public boolean isCD() {
    if (super.closed) throw new IllegalStateException();

    return control.getcd();
  }

  public synchronized void addEventListener(SerialPortEventListener lsnr)
    throws TooManyListenersException 
  {
    if (super.closed) throw new IllegalStateException();

    if (eventlistener!=null) {
      throw new TooManyListenersException();
    }

    eventlistener = lsnr;
    if(notifyon == null){
      notifyon = new BitSet(11);
    }
    eventthread = new Thread(this,getName()+" event notifier");
    eventthread.start();
  }

  public void removeEventListener() {
    notifyon = null;
    eventthread = null;
    eventlistener = null;
  }

  public void notifyOnDataAvailable(boolean enable) {
    if (super.closed) throw new IllegalStateException();

    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.DATA_AVAILABLE);
    } else {
      notifyon.clear(SerialPortEvent.DATA_AVAILABLE);
    }
  }
  // will get DATA_AVAILABLE when buffer goes from empty to non-empty

  public void notifyOnOutputEmpty(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.OUTPUT_BUFFER_EMPTY);
    } else {
      notifyon.clear(SerialPortEvent.OUTPUT_BUFFER_EMPTY);
    }
  }
  // will get OUTPUT_BUFFER_EMPTY when buffer goes from non-empty to empty

  public void notifyOnCTS(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.CTS);
    } else {
      notifyon.clear(SerialPortEvent.CTS);
    }
  }

  public void notifyOnDSR(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.DSR);
    } else {
      notifyon.clear(SerialPortEvent.DSR);
    }
  }

  public void notifyOnRingIndicator(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.RI);
    } else {
      notifyon.clear(SerialPortEvent.RI);
    }
  }

  public void notifyOnCarrierDetect(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.CD);
    } else {
      notifyon.clear(SerialPortEvent.CD);
    }
  }

  public void notifyOnOverrunError(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.OE);
    } else {
      notifyon.clear(SerialPortEvent.OE);
    }
  }

  public void notifyOnParityError(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.PE);
    } else {
      notifyon.clear(SerialPortEvent.PE);
    }
  }

  public void notifyOnFramingError(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.FE);
    } else {
      notifyon.clear(SerialPortEvent.FE);
    }
  }

   public void notifyOnBreakInterrupt(boolean enable) {
    if (super.closed) throw new IllegalStateException();


    if(notifyon == null){
      notifyon = new BitSet(11);
    }

    if (enable) {
      notifyon.set(SerialPortEvent.BI);
    } else {
      notifyon.clear(SerialPortEvent.BI);
    }
  }

  public InputStream getInputStream() throws IOException {
    if (in == null) in = new UARTInputStream(name);
    return in;
  }

  public OutputStream getOutputStream() throws IOException {
    if (out == null) out = new UARTOutputStream(name);
    return out;
  }


  public void enableReceiveThreshold(int thresh)
    throws UnsupportedCommOperationException 
  {
    if (super.closed) throw new IllegalStateException();

    if(in != null) in.setRxThreshold(thresh);
    rxThresholdEnabled = true;
  }


  public void disableReceiveThreshold() {
    if (super.closed) throw new IllegalStateException();

    if(in != null) in.clearRxThreshold();
    rxThresholdEnabled = false;
  }

  public boolean isReceiveThresholdEnabled() {
    if (super.closed) throw new IllegalStateException();

    return rxThresholdEnabled;
  }

  public int getReceiveThreshold() {
    if (super.closed) throw new IllegalStateException();

    return BOGUS_ANSWER;
  }

  public void enableReceiveTimeout(int rcvTimeout)
    throws UnsupportedCommOperationException
  {
    if (super.closed) throw new IllegalStateException();

    if(in != null) in.setRxTimeout(rcvTimeout);
    rxTimeoutEnabled = true;
  }

  public void disableReceiveTimeout() {
    if (super.closed) throw new IllegalStateException();

    if(in != null) in.clearRxTimeout();
    rxTimeoutEnabled = false;
  }

  public boolean isReceiveTimeoutEnabled() {
    if (super.closed) throw new IllegalStateException();

    return rxTimeoutEnabled;
  }

  public int getReceiveTimeout() {
    if (super.closed) throw new IllegalStateException();

    return BOGUS_ANSWER;
  }

  public void enableReceiveFraming(int framingByte)
    throws UnsupportedCommOperationException
  {
    if (super.closed) throw new IllegalStateException();

    throw new UnsupportedCommOperationException();
  }

  public void disableReceiveFraming(){}

  public boolean isReceiveFramingEnabled() {
    if (super.closed) throw new IllegalStateException();

    return false;
  }

  public int getReceiveFramingByte() {
    if (super.closed) throw new IllegalStateException();

    return BOGUS_ANSWER;
  }

  public void setInputBufferSize(int i){} 

  public int getInputBufferSize() {
    if (super.closed) throw new IllegalStateException();

    return BOGUS_ANSWER;
  } 

  public void setOutputBufferSize(int i){}

  public int getOutputBufferSize() {
    if (super.closed) throw new IllegalStateException();

    return BOGUS_ANSWER;
  }

  public void run() {
    while (eventlistener!=null) {
      SerialPortEvent newevent = control.getevent(this);

      synchronized (this) {
        if (eventlistener == null) {
          break;
        }
        if (notifyon.get(newevent.getEventType())) {
          eventlistener.serialEvent(newevent);
        }
      }
    }
  }


  public synchronized void close() {
    super.close();
    control.close();
  }
}
 

  
