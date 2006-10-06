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


package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.math.BigInteger;

public class DHParameterSpec implements AlgorithmParameterSpec {

  private BigInteger p;
  private BigInteger g;
  private int l;

  public DHParameterSpec(BigInteger p, BigInteger g){
        this(p,g,0);
  }

  public DHParameterSpec(BigInteger p, BigInteger g, int l){
        this.p = p;
        this.g = g;
        this.l = l;
  }

  public BigInteger getG(){
        return g;
  }

  public int getL(){
        return l;
  }

  public BigInteger getP(){
        return p;
  }
}