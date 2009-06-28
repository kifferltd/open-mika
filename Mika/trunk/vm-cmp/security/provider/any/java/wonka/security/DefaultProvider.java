/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package wonka.security;

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

