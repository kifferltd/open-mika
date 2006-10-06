/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: RSAOtherPrimeInfo.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.spec;

import java.math.BigInteger;

/**
 * RSAOtherPrimeInfo:
 *
 * @author ruelens
 *
 * created: Apr 11, 2006
 */
public class RSAOtherPrimeInfo {

  private BigInteger crtCoefficient;
  private BigInteger prime;
  private BigInteger exponent;

  public RSAOtherPrimeInfo(BigInteger prime,
      BigInteger primeExponent,  BigInteger crtCoefficient) {
    if(prime == null || primeExponent == null || crtCoefficient == null) {
      throw new NullPointerException();
    }
    this.prime = prime;
    this.exponent = primeExponent;
    this.crtCoefficient = crtCoefficient;
  }

  /**
   * @return Returns the crtCoefficient.
   */
  public final BigInteger getCrtCoefficient() {
    return crtCoefficient;
  }

  /**
   * @return Returns the prime.
   */
  public final BigInteger getPrime() {
    return prime;
  }

  /**
   * @return Returns the exponent.
   */
  public final BigInteger getExponent() {
    return exponent;
  }
}
