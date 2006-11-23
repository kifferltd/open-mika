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
** $Id: CommPort.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package javax.comm;

import java.io.*;

public abstract class CommPort {

  protected String name;
  protected boolean closed;

  CommPortIdentifier commPortIdentifier;

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  // getInputStream returns an InputStream (or null if the device
  // is write-only).  Only way for an application to read from the port.
 
  public abstract InputStream getInputStream() throws IOException;

  // ditto but for output.

  public abstract OutputStream getOutputStream() throws IOException;

  public synchronized void close () {
    commPortIdentifier.handle_close();
    try{
      getInputStream().close();
    } catch (IOException e) {}
    try{
      getOutputStream().close();
    } catch (IOException e) {}

    // [CG 20010201] Moved here from just before the two try{} clauses
    // above, as getXXputStream throw an IllegalStateException if the 
    // CommPort is already closed ...
    closed = true;
  }

  // if implemented, tells driver to block until thresh bytes in buffer
  // (or buffer full, or timeout if enabled).
  public abstract void enableReceiveThreshold(int thresh) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveThreshold();

  public abstract boolean isReceiveThresholdEnabled();

  public abstract int getReceiveThreshold();

  // if implemented, tells driver to block until rcvTimeout ms elapse
  // (or buffer full, or threshold if enabled).
  public abstract void enableReceiveTimeout(int rcvTimeout) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveTimeout();

  public abstract boolean isReceiveTimeoutEnabled();

  public abstract int getReceiveTimeout();

  // if implemented, tells driver to block until framingByte received
  // (or buffer full/thresholdl, or timeout if enabled).
  public abstract void enableReceiveFraming(int framingByte) 
    throws UnsupportedCommOperationException;

  public abstract void disableReceiveFraming();

  public abstract boolean isReceiveFramingEnabled();

  public abstract int getReceiveFramingByte();

  public abstract void setInputBufferSize(int size); // advisory only

  public abstract int getInputBufferSize(); // advisory only

  public abstract void setOutputBufferSize(int size); // advisory only

  public abstract int getOutputBufferSize(); // advisory only

 }
 

