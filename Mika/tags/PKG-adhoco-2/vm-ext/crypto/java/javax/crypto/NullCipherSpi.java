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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

final class NullCipherSpi extends CipherSpi {

  public NullCipherSpi(){}

  protected byte[] engineDoFinal(byte[] in, int offset, int length)
    throws IllegalBlockSizeException, BadPaddingException{

    return engineUpdate(in, offset, length);
  }

  protected int engineDoFinal(byte[] in, int offset, int length, byte[] bytes, int offbytes)
    throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {

    return engineUpdate(in, offset, length, bytes, offbytes);
  }

  protected int engineGetBlockSize(){
    return 1;
  }

  protected byte[] engineGetIV(){
    return null;
  }

  protected int engineGetKeySize(Key key)throws InvalidKeyException {
    throw new UnsupportedOperationException("engineGetKeySize(Key key) is not supported by "+this.getClass());
  }

  protected int engineGetOutputSize(int length){
    return length;
  }

  protected AlgorithmParameters engineGetParameters(){
    return null;
  }

  protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
    throws InvalidKeyException, InvalidAlgorithmParameterException {}

  protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
    throws InvalidKeyException, InvalidAlgorithmParameterException {}

  protected void engineInit(int opmode, Key key, SecureRandom srandom) throws InvalidKeyException {}

  protected void engineSetMode(String mode) throws NoSuchAlgorithmException {}

  protected void engineSetPadding(String padding)throws NoSuchPaddingException {}

  protected  Key engineUnwrap(byte[] key, String keyAlgorithm, int keyType)
    throws InvalidKeyException,NoSuchAlgorithmException {

    throw new UnsupportedOperationException("engineUnwrap is not supported by "+this.getClass());
  }

  protected byte[] engineUpdate(byte[] in, int offset, int length){
    if(in.length - length < offset || offset < 0 || length < 0){
      throw new IndexOutOfBoundsException();
    }
    byte[] newBuffer = new byte[length];
    System.arraycopy(in, offset, newBuffer, 0, length);
    return newBuffer;
  }

  protected int engineUpdate(byte[] in, int offset, int length, byte[] bytes, int offbytes)
    throws ShortBufferException {
    if(in.length - length < offset || offset < 0 || length < 0 || offbytes < 0){
      throw new IndexOutOfBoundsException();
    }
    if((bytes.length - length - offbytes) < 0){
      throw new ShortBufferException();
    }
    System.arraycopy(in, offset, bytes, offbytes, length);
    return length;
  }

  protected byte[] engineWrap(Key key)throws IllegalBlockSizeException, InvalidKeyException {
    throw new UnsupportedOperationException("engineWrap(Key key) is not supported by "+this.getClass());
  }
}
