/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Boris Kuznetsov
* @version $Revision$
*/

/*
 * Imported by CG 20090319 based on Apache Harmony ("enhanced") revision 476395.
 */

package org.apache.harmony.security.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.pkcs7.ContentInfo;
import org.apache.harmony.security.pkcs7.SignedData;
import org.apache.harmony.security.pkcs7.SignerInfo;
import org.apache.harmony.security.provider.cert.X509CertImpl;
import org.apache.harmony.security.x501.AttributeTypeAndValue;


public class JarUtils {

	// as defined in PKCS #9: Selected Attribute Types:
	// http://www.ietf.org/rfc/rfc2985.txt
    private static final int[] MESSAGE_DIGEST_OID =
        new int[] {1, 2, 840, 113549, 1, 9, 4};

    /**
     * This method handle all the work with  PKCS7, ASN1 encoding, signature verifying, 
     * and certification path building. 
     * See also PKCS #7: Cryptographic Message Syntax Standard:
     * http://www.ietf.org/rfc/rfc2315.txt
     * @param signature - the input stream of signature file to be verified
     * @param signatureBlock - the input stream of corresponding signature block file
     * @return array of certificates used to verify the signature file
     * @throws IOException - if some errors occurs during reading from the stream
     * @throws GeneralSecurityException - if signature verification process fails
     */
    public static Certificate[] verifySignature(InputStream signature, InputStream 
            signatureBlock) throws IOException, GeneralSecurityException {

        BerInputStream bis = new BerInputStream(signatureBlock);
        ContentInfo info = (ContentInfo)ContentInfo.ASN1.decode(bis);      
        SignedData signedData = info.getSignedData();
        if (signedData == null) {
            throw new IOException("No SignedData found");
        }
        Collection encCerts = signedData.getCertificates();
        if (encCerts.isEmpty()) {
            return null;
        }
        X509Certificate[] certs = new X509Certificate[encCerts.size()];
        int i = 0;
        for (Iterator it = encCerts.iterator(); it.hasNext();) {
            certs[i++]= new X509CertImpl((org.apache.harmony.security.x509.Certificate)it.next());
        }

        List sigInfos = signedData.getSignerInfos();
        SignerInfo sigInfo;
        if (!sigInfos.isEmpty()) {
            sigInfo = (SignerInfo)sigInfos.get(0);
        } else {
            return null;
        }

        // Issuer
        X500Principal issuer = sigInfo.getIssuer();

        // Certificate serial number
        BigInteger snum = sigInfo.getSerialNumber();

        // Locate the certificate
        int issuerSertIndex = 0;
        for (i = 0; i < certs.length; i++) {
            if (issuer.equals(certs[i].getIssuerDN()) && 
                    snum.equals(certs[i].getSerialNumber())) {
                issuerSertIndex = i;
                break;
            }
        }
        if (i == certs.length) { // No issuer certificate found
            return null;
        }

        if (certs[issuerSertIndex].hasUnsupportedCriticalExtension()) {
            throw new SecurityException("Can not recognize a critical extension");
        }

        // Get Signature instance
        Signature sig = null;
        String da = sigInfo.getdigestAlgorithm();
        String dea = sigInfo.getDigestEncryptionAlgorithm();
        String alg = null;
        if (da != null && dea != null) {
            alg = da + "with" +  dea; //$NON-NLS-1$
            try{ 
                sig = Signature.getInstance(alg);
            } catch (NoSuchAlgorithmException e) {}
        }
        if (sig == null) {
            alg = da;
            if (alg == null) {
                return null;
            }
            try{ 
                sig = Signature.getInstance(alg);
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }
        sig.initVerify(certs[issuerSertIndex]);

        // If the authenticatedAttributes field of SignerInfo contains more than zero attributes,
        // compute the message digest on the ASN.1 DER encoding of the Attributes value.
        // Otherwise, compute the message digest on the data.
        List atr = sigInfo.getAuthenticatedAttributes();

        byte[] sfBytes = new byte[signature.available()];
        signature.read(sfBytes);

        if (atr == null) {
            sig.update(sfBytes);    
        } else {
            sig.update(sigInfo.getEncodedAuthenticatedAttributes());

            // If the authenticatedAttributes field contains the message-digest attribute,
            // verify that it equals the computed digest of the signature file
            byte[] existingDigest = null;
            for (Iterator it = atr.iterator(); it.hasNext();) {
                AttributeTypeAndValue a = (AttributeTypeAndValue)it.next();
                if (Arrays.equals(a.getType().getOid(), MESSAGE_DIGEST_OID) ){
//TODO value                    existingDigest = a.AttributeValue;
                }
            }
            if (existingDigest != null) {
                MessageDigest md = MessageDigest.getInstance(sigInfo.getDigestAlgorithm());
                byte[] computedDigest = md.digest(sfBytes);
                if (!Arrays.equals(existingDigest, computedDigest)) {
                    throw new SecurityException("Incorrect MD");
                }
            }
        }

        if (!sig.verify(sigInfo.getEncryptedDigest())) {
            throw new SecurityException("Incorrect signature");
        }

        return createChain(certs[issuerSertIndex], certs);
    }
    
    private static X509Certificate[] createChain(X509Certificate  signer, X509Certificate[] candidates) {
        LinkedList chain = new LinkedList();
        chain.add(0, signer);

        // Signer is self-signed
        if (signer.getSubjectDN().equals(signer.getIssuerDN())){
            return (X509Certificate[])chain.toArray(new X509Certificate[1]);
        }

        Principal issuer = signer.getIssuerDN();
        X509Certificate issuerCert;
        int count = 1;
        while (true) {
            issuerCert = findCert(issuer, candidates);
            if( issuerCert == null) {
                break;
            }
            chain.add(issuerCert);
            count++;
            if (issuerCert.getSubjectDN().equals(issuerCert.getIssuerDN())) {
                break;
            }
            issuer = issuerCert.getIssuerDN();
        }
        return (X509Certificate[])chain.toArray(new X509Certificate[count]);
    }

    private static X509Certificate findCert(Principal issuer, X509Certificate[] candidates) {
        for (int i = 0; i < candidates.length; i++) {
            if (issuer.equals(candidates[i].getSubjectDN())) {
                return candidates[i];
            }
        }
        return null;
    }

}

