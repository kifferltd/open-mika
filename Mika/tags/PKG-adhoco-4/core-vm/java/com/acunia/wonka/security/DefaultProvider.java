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



/**
 * $Id: DefaultProvider.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.Provider;
import java.util.Properties;

public final class DefaultProvider extends Provider {

  private static final DefaultProvider theDefaultProvider = new DefaultProvider();

  public static Provider getInstance(){
    // ADD SECURITY CHECK ???
    return theDefaultProvider;
  }


  public static final String INFO = "Wonka's own SecurityProvider";
  public static final String NAME = "Wonka's DefaultProvider";
  public static final double VERSION = 0.1;

  public DefaultProvider(){
    super(NAME, VERSION , INFO);
    defaults = new Properties();
    //ADD YOUR ALGORITHMS with thier classes here ...
    defaults.put("SecureRandom.notSoSecure", "com.acunia.wonka.security.SecureRandomImpl");
    defaults.put("MessageDigest.MD5", "com.acunia.wonka.security.MD5MessageDigest");
    defaults.put("MessageDigest.SHA", "com.acunia.wonka.security.ShaMessageDigest");

    /**
    ** When reading the documentation, you are guaranteed to have some aliases
    ** for some well known algorithm's. Well here they are ...
    */
    defaults.put("Alg.Alias.KeyFactory.1.2.840.10040.4.1","DSA");
    defaults.put("Alg.Alias.Signature.1.2.840.10040.4.3","SHA1withDSA");
    defaults.put("Alg.Alias.KeyPairGenerator.OID.1.2.840.10040.4.1","DSA");
    defaults.put("Alg.Alias.KeyFactory.1.3.14.3.2.12","DSA");
    defaults.put("Alg.Alias.KeyPairGenerator.1.3.14.3.2.12","DSA");
    defaults.put("Alg.Alias.Signature.SHA/DSA","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.1.3.14.3.2.13","SHA1withDSA");
    defaults.put("Alg.Alias.CertificateFactory.X.509","X509");
    defaults.put("Alg.Alias.Signature.DSS","SHA1withDSA");
    defaults.put("Alg.Alias.AlgorithmParameters.1.3.14.3.2.12","DSA");
    defaults.put("Alg.Alias.Signature.DSA","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.DSAWithSHA1","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.SHAwithDSA","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.OID.1.2.840.10040.4.3","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.SHA1/DSA","SHA1withDSA");
    defaults.put("Alg.Alias.KeyPairGenerator.1.2.840.10040.4.1","DSA");
    defaults.put("Alg.Alias.MessageDigest.SHA-1","SHA");
    defaults.put("Alg.Alias.MessageDigest.SHA1","SHA");
    defaults.put("Alg.Alias.AlgorithmParameters.1.2.840.10040.4.1","DSA");
    defaults.put("Alg.Alias.Signature.1.3.14.3.2.27","SHA1withDSA");
    defaults.put("Alg.Alias.Signature.SHA-1/DSA","SHA1withDSA");
  }

  //TODO make this provider immutable ...
}

