/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/


/*
** $Id: CommPortIdentifier.java,v 1.2 2005/03/19 11:45:06 cvs Exp $
*/

package javax.comm;

import java.io.FileDescriptor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.open_mika.device.uart.UARTDriver;

public class CommPortIdentifier {

 /** The various kinds of comm port.
 */
//@{
///
  public static final int PORT_SERIAL = 1;
///
  public static final int PORT_PARALLEL = 2;
//@}

  /**@name Hashtables
  */
//@{
  /** maps port names (Strings) onto instances of CommPortIdentifier.
  */
  private static Hashtable idtable = new Hashtable();

  /**@name Instance fields of a CommPortIdentifier
  */
//@{
  /** A String which uniquely identifies this port.
   ** Names are allocated by the system (more 
   ** specifically, by the device driver in question).
   */
  private String  name;
  /** type is either PORT_SERIAL or PORT_PARALLEL.
   */
  private int     type;
  /** The device driver which controls this port.
   */
  private CommDriver driver;
  /** The instance of a subclass of CommPort which
   ** acts as a "control block" for this port.
   */
  private CommPort commport;
  /** The current "owner".  
   ** Owners are identified by simple String's; it is up to the developers of
   ** classes which make use of this class to agree on a naming convention.
   */
  private String  owner;
  /** ownershiplisteners    A list (Vector) of CommPortOwnershipListeners.
   ** Each member of the list is notified whenever the port is open()ed or 
   ** close()d - or rather ...
   */
  private Vector  ownershiplisteners;
  /** ... a change in ownership status results in  notification of all 
   ** ownershiplisteners only if ownershipcallback is zero.  
   ** Before notification begins, ownershipcallback is incremented: it is
   ** decremented again after notification is complete.
   ** This is intended to prevent a "notification storm"
   ** which could occur when many would-be owners are
   ** contending for ownership of a port.
   */
  private int     ownershipcallback;
//@}


  /**@name Static initializer
   ** The static initializer of this class has the duty of starting up
   ** all device drivers (it's a kludgey job, but someone has to do it).
   ** TODO: consider making this data-driven somehow ...
   */ 
  static {
    CommDriver driver = UARTDriver.getInstance();
    driver.initialize();
  }

  /**
   ** Constructor.  Boring as hell.  Get the underlying commport from
   ** the driver and record the mapping CommPort->CommPortIdentifier
   */
  private CommPortIdentifier(String portName, int portType, CommDriver driver) 
    throws NoSuchPortException
  {
    this.name = portName;
    this.type = portType;
    this.driver = driver;
    ownershiplisteners = new Vector();
 }

  /**
   ** getPortIdentifiers() returns an Enumeration of all instances
   ** of CommPortIdentifier.
   */
  public static Enumeration getPortIdentifiers() {
    return idtable.elements();
  }

  /**
   ** getPortIdentifier(String) retrieves a CommPortIdentifier by name.
   ** Throws NoSuchPortException if no such CommPortIdentifier exists.
   */
  public static CommPortIdentifier getPortIdentifier(String portName)
    throws NoSuchPortException
  {
    CommPortIdentifier result = (CommPortIdentifier)idtable.get(portName);

    if (result==null) {
      throw new NoSuchPortException("No such CommPort as "+portName);
    }

    return result;
  }

  /**
   ** getPortIdentifier(CommPort) retrieves a CommPortIdentifier by CommPort.
   ** Throws NoSuchPortException if no such CommPortIdentifier exists.
   */
  public static CommPortIdentifier getPortIdentifier(CommPort port)
    throws NoSuchPortException
  {
    CommPortIdentifier result = port.commPortIdentifier;

    if (result==null) {
      throw new NoSuchPortException();
    }

    return result;
  }

  /**
   ** addPortName(String,int,CommDriver) is used by a CommDriver to register
   ** a port with the system.
   */
  public static void addPortName (String portName, int portType, CommDriver driver) {
    try {
      CommPortIdentifier newportid = new CommPortIdentifier(portName, portType, driver);
      idtable.put(portName,newportid);
    } catch ( NoSuchPortException e) {
      // Now why would the driver call us with a non-existent port name?
    }
  }

  /**
   ** getName() retrieves the name of this port.
   */
  public String getName() {
    return this.name;
  }

  /**
   ** getPortType() retrieves the type of this port.
   */
  public int getPortType () {
    return type;
  }

  /**
   ** getCurrentOwner() retrieves the current owner of this port.
   ** Returns null if the port is currently ownerless.
   */
  public  String getCurrentOwner() {
    return this.owner;
  }

  /**
   ** isCurrentlyOwned() returns true if this port has an owner,.
   ** false if the port is currently ownerless.
   */
  public synchronized boolean isCurrentlyOwned() {
    return owner!=null;
  }

  /**
   ** open(String,int) attempts to open the port in the name of a given
   ** application, within a given period of time.
   ** If the port is not free (unowned) when open() is called, then we 
   ** notify all ownership listeners of our desire to open the port;
   ** if the current owner reacts to this by releasing the port, then
   ** we can now open it.  If the current owner did not release the port,
   ** then we hang around for a while to see whether it becomes free for
   ** whatever reason: for this we use wait/notify, as the OwnershipListener
   ** mechanism has no time dimension. 
   **
   ** Note that if an application does not register as an ownership
   ** listener then it will be deaf to the pleas of all other applications
   ** that wish to use the port: there is no way we can force it to relinquish
   ** control.  Nor is there any prioritization of ownership candidates, nor
   ** any pretence of fairness.
   **
   ** The "spec" is silent on such questions, but I (CG) decided 
   ** 1) to throw an exception if the appname is null or the timeout is negative
   ** 2) zero timeout means don't hang around, either return immediately
   **    or throw PortInUseException..
   */
  public synchronized CommPort open (String appname, int timeout) 
    throws PortInUseException 
  {
    if (appname==null) throw new NullPointerException("appname is null");

    if (timeout<0) throw new IllegalArgumentException("nonpositive timeout");

    Vector snapshot = (Vector)ownershiplisteners.clone();
    Enumeration lstnrz = snapshot.elements();
    CommPortOwnershipListener lstnr;

    if (this.owner!=null && this.owner!=appname) {
      long deadline = System.currentTimeMillis()+timeout;
      long remaining = timeout;

      while (lstnrz.hasMoreElements()) {
        lstnr = (CommPortOwnershipListener)lstnrz.nextElement();
        ++ownershipcallback;
        lstnr.ownershipChange(CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED);
        --ownershipcallback;
      }

      while (remaining>0 && this.owner!=null) {
        try {
          this.wait(remaining);
          remaining = deadline - System.currentTimeMillis();
        } catch (InterruptedException e) {}
      }

      if (this.owner!=null) {
        PortInUseException piue = new PortInUseException();
        piue.currentOwner = this.owner;
        throw piue;
      }
    }

    if(commport == null) {
      commport = this.driver.getCommPort(this.name, this.type);
      if(commport == null) {
        throw new PortInUseException();
      }
      commport. commPortIdentifier = this;
    }

    lstnrz = snapshot.elements();
    while (lstnrz.hasMoreElements()) {
      lstnr = (CommPortOwnershipListener)lstnrz.nextElement();
      ++ownershipcallback;
      lstnr.ownershipChange(CommPortOwnershipListener.PORT_OWNED);
      --ownershipcallback;
    }

    this.owner = appname;

    if(commport.closed){
      commport = this.driver.getCommPort(this.name, this.type);
      commport. commPortIdentifier = this;
    }
    return commport;
  }

  /**
   ** handle_close() is the counterpart to open(): it is called from
   ** inside the close() method of CommPort.  If not already within
   ** an ownership callback, we notify all ownership listeners that
   ** the port is now free; we then also issue a notify() for the
   ** benefit of any thread that may be wait()ing with a timeout.
   ** (Maybe this should happen the other way around, who knows ...)
   */
  synchronized void handle_close() {
    Vector snapshot = (Vector)ownershiplisteners.clone();
    Enumeration lstnrz = snapshot.elements();
    CommPortOwnershipListener lstnr;

    owner = null; // [CG 20000428] moved this up from just before notify()

    if (ownershipcallback==0) {
      while (lstnrz.hasMoreElements()) {
        lstnr = (CommPortOwnershipListener)lstnrz.nextElement();
        ++ownershipcallback;
        lstnr.ownershipChange(CommPortOwnershipListener.PORT_UNOWNED);
        --ownershipcallback;
      }
    }

    if (this.type==PORT_SERIAL) {
      ((SerialPort)this.commport).removeEventListener();
    }
// Parallel ports we do not have
//    else {
//      ((ParallelPort)this.commport).removeEventListener();
//    }

    this.notifyAll();
  }

  /**
   ** open(FileDescriptor) is not yet supported.
   */
  public CommPort open (FileDescriptor fd) 
    throws UnsupportedCommOperationException 
  {
  //  opens fd as a CommPort.  Not supported on platform. :0
    throw new UnsupportedCommOperationException();
  }

  /**
   ** addPortOwnershipListener(CommPortOwnershipListener) adds a
   ** CommPortOwnershipListener to ownershiplisteners.
   */
  public void addPortOwnershipListener(CommPortOwnershipListener listener) {
    ownershiplisteners.addElement(listener);
  }

  /**
   ** removePortOwnershipListener(CommPortOwnershipListener) removes a
   ** CommPortOwnershipListener from ownershiplisteners.
   */
  public void removePortOwnershipListener (CommPortOwnershipListener listener) {
    ownershiplisteners.removeElement(listener);
  }


}
