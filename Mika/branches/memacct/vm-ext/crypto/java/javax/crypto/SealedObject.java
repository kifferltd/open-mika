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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;


public class SealedObject implements Serializable {

  private static final long serialVersionUID = 4482838265551344752L;

  protected byte[] encodedParams;
  private byte[] encryptedContent;
  private String sealAlg;
  private String paramsAlg;

  protected SealedObject(SealedObject so){
    encodedParams = (so.encodedParams == null ? null : (byte[])so.encodedParams.clone());
    encryptedContent = (byte[]) so.encryptedContent.clone();
    sealAlg = so.sealAlg;
    paramsAlg = so.paramsAlg;
  }

  public SealedObject(Serializable object, Cipher c) throws IOException, IllegalBlockSizeException  {
    sealAlg = c.getAlgorithm();
    AlgorithmParameters params = c.getParameters();
    if(params != null){
      paramsAlg = params.getAlgorithm();
      encodedParams = params.getEncoded();
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
    ObjectOutputStream out = new ObjectOutputStream(new CipherOutputStream(bout, c));
    out.writeObject(object);
    out.flush();
    out.close();
    encryptedContent = bout.toByteArray();
  }

  public final String getAlgorithm(){
    return sealAlg;
  }

  public final Object getObject(Key key) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(sealAlg);
    if(paramsAlg != null){
      AlgorithmParameters params = AlgorithmParameters.getInstance(paramsAlg);
      params.init(encodedParams);
      try {
        cipher.init(Cipher.DECRYPT_MODE, key, params);
      }
      catch(InvalidAlgorithmParameterException iape){
        cipher.init(Cipher.DECRYPT_MODE, key);
      }
    }
    else {
      cipher.init(Cipher.DECRYPT_MODE, key);
    }
    try {
      return getObject(cipher);
    }
    catch(BadPaddingException bpe){
      return null;
    }
    catch(IllegalBlockSizeException ibse){
      return null;
    }
  }

  public final Object getObject(Cipher c) throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException {
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(c.doFinal(encryptedContent,0, encryptedContent.length)));
    return in.readObject();
  }

  public final Object getObject(Key key, String provider)
    throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

    Cipher cipher = Cipher.getInstance(sealAlg, provider);
    if(paramsAlg != null){
      AlgorithmParameters params = AlgorithmParameters.getInstance(paramsAlg, provider);
      params.init(encodedParams);
      try {
        cipher.init(Cipher.DECRYPT_MODE, key, params);
      }
      catch(InvalidAlgorithmParameterException iape){
        cipher.init(Cipher.DECRYPT_MODE, key);
      }
    }
    else {
      cipher.init(Cipher.DECRYPT_MODE, key);
    }
    try {
      return getObject(cipher);
    }
    catch(BadPaddingException bpe){
      return null;
    }
    catch(IllegalBlockSizeException ibse){
      return null;
    }
  }
}
