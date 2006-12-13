/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: PSSParameterSpec.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.spec;

/**
 * PSSParameterSpec:
 *
 * @author ruelens
 *
 * created: Apr 11, 2006
 */
public class PSSParameterSpec implements AlgorithmParameterSpec {

  private int saltLength;

  public PSSParameterSpec(int length) {
    if(length < 0) {
      throw new IllegalArgumentException(String.valueOf(length));
    }
    this.saltLength = length;
  }

  /**
   * @return Returns the saltLength.
   */
  public int getSaltLength() {
    return saltLength;
  }
}
