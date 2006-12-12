/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: URISyntaxException.java,v 1.1 2006/03/29 09:27:14 cvs Exp $
 */
package java.net;

/**
 * URISyntaxException: new since 1.4 Api
 *
 * @author ruelens
 *
 * created: Mar 28, 2006
 */
public class URISyntaxException extends Exception {

  private String input;
  private int index;

  /**
   * @param message
   */
  public URISyntaxException(String message, String input) {
    this(message, input, -1);
  }

  /**
   * @param message
   * @param cause
   */
  public URISyntaxException(String message, String input, int index) {
    super(message);
    this.input = input;
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
  
  public String getInput() {
    return input;
  }
  
  public String getReason() {
    return super.getMessage();
  }

  public String getMessage() {
    StringBuffer buf = new StringBuffer(super.getMessage()).append(input);
    return (index == -1 ? buf : buf.append(':').append(index)).toString();
  }
  
  
}
