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


package java.security.cert;

import java.util.Arrays;

import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

/**
 *
 * @version $Id: Certificate.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 *
 */

public abstract class Certificate implements java.io.Serializable {

  private static final long serialVersionUID = -6751606818319535583L;

  private String type;

  protected Certificate ( String type) {
  	this.type = type;
  }

  public final String getType () {
  	return type;
  }

  public boolean equals (Object other) {
    if (!(other instanceof Certificate)){
      return false;
    }
    Certificate cert = (Certificate) other;
    try {
      return Arrays.equals(cert.getEncoded(), getEncoded());
    } catch(CertificateEncodingException e){
      return false;
    }
  }

  public int hashCode(){
    try {
      return getEncoded().hashCode();
    } catch(CertificateEncodingException e){
      return 0;
    }
  }

  public abstract byte [] getEncoded() throws CertificateEncodingException;
  public abstract void verify (PublicKey key)
  	throws CertificateException, NoSuchAlgorithmException,
  		InvalidKeyException, NoSuchProviderException,
  		SignatureException;

  public abstract void verify(PublicKey key, String sigProvider)
  	throws CertificateException, NoSuchAlgorithmException,
  		InvalidKeyException, NoSuchProviderException,
  		SignatureException;

  public abstract String toString();	
  public abstract PublicKey getPublicKey();

  protected Object writeReplace() throws java.io.ObjectStreamException {
    try {
      return new CertificateRep(type, getEncoded());
    }
    catch(CertificateEncodingException cee){
      throw new java.io.InvalidObjectException(cee.toString());
    }
  }

  protected static class CertificateRep implements java.io.Serializable {

    private String type;
    private byte[] data;

    protected CertificateRep(String type, byte[] data){
      this.type = type;
      this.data = data;
    }

    protected Object readResolve() throws java.io.ObjectStreamException {
      try {
        return CertificateFactory.getInstance(type).generateCertificate(new java.io.ByteArrayInputStream(data));
      }
      catch(Exception e){
        throw new java.io.InvalidObjectException(e.toString());
      }
    }
  }
}
