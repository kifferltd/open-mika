/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: PortUnreachableException.java,v 1.1 2006/03/29 09:27:14 cvs Exp $
 */
package java.net;

/**
 * PortUnreachableException: new since 1.4
 *
 * @author ruelens
 *
 * created: Mar 28, 2006
 */
public class PortUnreachableException extends SocketException {
  public PortUnreachableException() {
    super();
  }

  /**
   * @param message the exception message
   */
  public PortUnreachableException(String message) {
    super(message);
  }
}
