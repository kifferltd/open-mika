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

package org.apache.harmony.security.pkcs7;

import java.util.List;

import org.apache.harmony.security.asn1.ASN1Any;
import org.apache.harmony.security.asn1.ASN1Implicit;
import org.apache.harmony.security.asn1.ASN1Integer;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1SetOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x509.AlgorithmIdentifier;
import org.apache.harmony.security.x509.Certificate;
import org.apache.harmony.security.x509.CertificateList;


/**
 * As defined in PKCS #7: Cryptographic Message Syntax Standard
 * (http://www.ietf.org/rfc/rfc2315.txt)
 * 
 * SignedData ::= SEQUENCE { 
 *   version Version, 
 *   digestAlgorithms DigestAlgorithmIdentifiers,
 *   contentInfo ContentInfo,
 *   certificates
 *     [0] IMPLICIT ExtendedCertificatesAndCertificates OPTIONAL,
 *   crls 
 *     [1] IMPLICIT CertificateRevocationLists OPTIONAL,
 *   signerInfos SignerInfos }
 *  
 */

public class SignedData {

    private int version;

    private List digestAlgorithms;
    private ContentInfo contentInfo;
    private List certificates;
    private List crls;
    private List signerInfos;

    public SignedData(int version, List digestAlgorithms, ContentInfo contentInfo,
            List certificates, List crls, List signerInfos) {
        this.version = version;
        this.digestAlgorithms = digestAlgorithms;
        this.contentInfo = contentInfo;
        this.certificates = certificates;
        this.crls = crls;
        this.signerInfos = signerInfos;
    }

    public List getCertificates() {
        return certificates;
    }

    public List getCRLs() {
        return crls;
    }

    public List getSignerInfos() {
        return signerInfos;
    }

    /**
     * @return Returns the contentInfo.
     */
    public ContentInfo getContentInfo() {
        return contentInfo;
    }

    /**
     * @return Returns the digestAlgorithms.
     */
    public List getDigestAlgorithms() {
        return digestAlgorithms;
    }

    /**
     * @return Returns the version.
     */
    public int getVersion() {
        return version;
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("---- SignedData:"); //$NON-NLS-1$
        res.append("\nversion: "); //$NON-NLS-1$
        res.append(version);
        res.append("\ndigestAlgorithms: "); //$NON-NLS-1$
        res.append(digestAlgorithms.toString());
        res.append("\ncontentInfo: "); //$NON-NLS-1$
        res.append(contentInfo.toString());
        res.append("\ncertificates: "); //$NON-NLS-1$
        if (certificates != null) {
            res.append(certificates.toString());
        }
        res.append("\ncrls: "); //$NON-NLS-1$
        if (crls != null) {
            res.append(crls.toString());
        }
        res.append("\nsignerInfos:\n"); //$NON-NLS-1$
        res.append(signerInfos.toString());
        res.append("\n---- SignedData End\n]"); //$NON-NLS-1$
        return res.toString();
    }

    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(), 
            new ASN1SetOf(AlgorithmIdentifier.ASN1),
            ContentInfo.ASN1,
            new ASN1Implicit(0, new ASN1SetOf(Certificate.ASN1)),
            new ASN1Implicit(1, new ASN1SetOf(CertificateList.ASN1)),
            new ASN1SetOf(SignerInfo.ASN1) 
			}) {
        {
            setOptional(3); // certificates is optional
            setOptional(4); // crls is optional
        }

        protected void getValues(Object object, Object[] values) {
            SignedData sd = (SignedData) object;
            values[0] = new byte[] {(byte)sd.version};
            values[1] = sd.digestAlgorithms;
            values[2] = sd.contentInfo;
            values[3] = sd.certificates;
            values[4] = sd.crls;
            values[5] = sd.signerInfos;
        }

        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new SignedData(
                        ASN1Integer.toIntValue(values[0]),
                        (List) values[1], 
                        (ContentInfo) values[2],
                        (List) values[3], 
                        (List) values[4], 
                        (List) values[5]
                    );
        }
    };

}

