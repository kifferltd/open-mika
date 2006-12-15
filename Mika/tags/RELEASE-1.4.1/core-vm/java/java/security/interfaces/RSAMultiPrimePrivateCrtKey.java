/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: RSAMultiPrimePrivateCrtKey.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * RSAMultiPrimePrivateCrtKey:
 *
 * @author ruelens
 *
 * created: Apr 11, 2006
 */
public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {

  public BigInteger getPublicExponent();
  public BigInteger getPrimeP();
  public BigInteger getPrimeQ();
  public BigInteger getPrimeExponentP();
  public BigInteger getPrimeExponentQ();
  public BigInteger getCrtCoefficient();
  public RSAOtherPrimeInfo[] getOtherPrimeInfo(); 
}
