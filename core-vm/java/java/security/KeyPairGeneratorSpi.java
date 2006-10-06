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
** $Id: KeyPairGeneratorSpi.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyPairGeneratorSpi {

  public KeyPairGeneratorSpi(){}

  public abstract KeyPair generateKeyPair();
  public abstract void initialize(int keysize, SecureRandom random);

/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing.
**
** @remark should be overridden
*/
  public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {}
}