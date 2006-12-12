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
** $Id: SignedObject.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.security;

import java.io.*;

public final class SignedObject implements Serializable {

  private byte[] content;
  private byte[] signature;
  private String thealgorithm;

  private transient Object signedobject;

  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
  }

  public SignedObject(Serializable object, PrivateKey pkey, Signature sign)
                throws IOException, InvalidKeyException, SignatureException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream(baos).writeObject(object);
        content = baos.toByteArray();

        sign.engineInitSign(pkey);
        sign.engineUpdate(content, 0, content.length);
        signature = sign.engineSign();
        signedobject = object;
  }

  public String getAlgorithm(){
        return thealgorithm;
  }

  public Object getObject() throws ClassNotFoundException, IOException {
        return signedobject;
  }

  public byte[] getSignature(){
        return signature;
  }

  public boolean verify(PublicKey pkey, Signature sign)
                throws InvalidKeyException, SignatureException {

        sign.engineInitVerify(pkey);
        return sign.engineVerify(signature);
  }
}
