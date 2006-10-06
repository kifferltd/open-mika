/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: RSAMultiPrimePrivateCrtKeySpec.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.spec;

import java.math.BigInteger;

/**
 * RSAMultiPrimePrivateCrtKeySpec:
 *
 * @author ruelens
 *
 * created: Apr 11, 2006
 */
public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {

  private RSAOtherPrimeInfo[] otherPrimeInfo;
  private BigInteger crtCoefficient;
  private BigInteger primeExponentQ;
  private BigInteger primeExponentP;
  private BigInteger primeP;
  private BigInteger primeQ;
  private BigInteger publicExponent;

  /**
   * @param modulus
   * @param privateExponent
   */
  public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus, 
      BigInteger publicExponent, BigInteger privateExponent, BigInteger primeP, 
      BigInteger primeQ, BigInteger primeExponentP, BigInteger primeExponentQ, 
      BigInteger crtCoefficient, RSAOtherPrimeInfo[] otherPrimeInfo) {
    
    super(modulus, privateExponent);
    if(publicExponent == null || primeP == null || crtCoefficient == null
      || primeExponentP == null || primeExponentQ == null || primeQ == null) {
      throw new NullPointerException();      
    }
    if(otherPrimeInfo != null && otherPrimeInfo.length == 0) {
      throw new IllegalArgumentException("otherPrimeInfo length == 0");
    }
    this.publicExponent =publicExponent;
    this.otherPrimeInfo = otherPrimeInfo;
    this.crtCoefficient = crtCoefficient;
    this.primeExponentQ = primeExponentQ;
    this.primeExponentP = primeExponentP;
    this.primeP = primeP;
    this.primeQ = primeQ;
  }

  /**
   * @return Returns the crtCoefficient.
   */
  public BigInteger getCrtCoefficient() {
    return crtCoefficient;
  }

  /**
   * @return Returns the otherPrimeInfo.
   */
  public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
    return otherPrimeInfo;
  }

  /**
   * @return Returns the primeExponentP.
   */
  public BigInteger getPrimeExponentP() {
    return primeExponentP;
  }

  /**
   * @return Returns the primeExponentQ.
   */
  public BigInteger getPrimeExponentQ() {
    return primeExponentQ;
  }

  /**
   * @return Returns the primeP.
   */
  public BigInteger getPrimeP() {
    return primeP;
  }

  /**
   * @return Returns the primeQ.
   */
  public BigInteger getPrimeQ() {
    return primeQ;
  }

  /**
   * @return Returns the publicExponent.
   */
  public BigInteger getPublicExponent() {
    return publicExponent;
  }
}
