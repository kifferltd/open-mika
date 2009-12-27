/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class ExemptionMechanismSpi {

  public ExemptionMechanismSpi(){}

  protected abstract byte[] engineGenExemptionBlob() throws ExemptionMechanismException;

  protected abstract int engineGenExemptionBlob(byte[] bytes, int offset)
    throws ShortBufferException, ExemptionMechanismException;

  protected abstract int engineGetOutputSize(int length);

  protected abstract void engineInit(Key key) throws InvalidKeyException, ExemptionMechanismException;

  protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
    throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

  protected abstract void engineInit(Key key, AlgorithmParameters params)
    throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

}
