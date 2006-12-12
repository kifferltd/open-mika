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


package java.security.spec;

import java.math.BigInteger;

public class RSAPrivateKeySpec implements KeySpec {

  private BigInteger modulus;
  private BigInteger privateExponent;

  public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent){
        if (modulus == null || privateExponent == null){
                throw new NullPointerException();
        }
        this.modulus = modulus;
        this.privateExponent = privateExponent;
  }

  public BigInteger getModulus() {
        return modulus;
  }

  public BigInteger getPrivateExponent() {
        return privateExponent;
  }

}