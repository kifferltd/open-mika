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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

/*
 * Imported by CG 20090321 based on Apache Harmony ("enhanced") revision 490473.
 */

package org.apache.harmony.security.x509;

import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.x501.DirectoryString;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work 
 * with the following structure which is a subpart of GeneralName
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *   
 * <pre>
 *   EDIPartyName ::= SEQUENCE {
 *        nameAssigner            [0]     DirectoryString OPTIONAL,
 *        partyName               [1]     DirectoryString 
 *   }
 * 
 *   DirectoryString ::= CHOICE {
 *        teletexString             TeletexString   (SIZE (1..MAX)),
 *        printableString           PrintableString (SIZE (1..MAX)),
 *        universalString           UniversalString (SIZE (1..MAX)),
 *        utf8String              UTF8String      (SIZE (1..MAX)),
 *        bmpString               BMPString       (SIZE (1..MAX)) 
 *   }
 * </pre>
 */
public class EDIPartyName {
    // the value of nameAssigner field of the structure
    private String nameAssigner;
    // the value of partyName field of the structure
    private String partyName;
    // the ASN.1 encoded form of EDIPartyName
    private byte[] encoding;
    
    /**
     * TODO
     * @param   nameAssigner:   String
     * @param   partyName:  String
     */
    public EDIPartyName(String nameAssigner, String partyName) {
        this.nameAssigner = nameAssigner;
        this.partyName = partyName;
    }
    
    // 
    // TODO
    // @param   nameAssigner:   String
    // @param   partyName:  String
    // @param   encoding:   byte[]
    // 
    private EDIPartyName(String nameAssigner, String partyName, 
                         byte[] encoding) {
        this.nameAssigner = nameAssigner;
        this.partyName = partyName;
        this.encoding = encoding;
    }
    
    /**
     * Returns the value of nameAssigner field of the structure.
     * @return  nameAssigner
     */
    public String getNameAssigner() {
        return nameAssigner;
    }
    
    /**
     * Returns the value of partyName field of the structure.
     * @return  partyName
     */
    public String getPartyName() {
        return partyName;
    }
    
    /**
     * Returns ASN.1 encoded form of this X.509 EDIPartyName value.
     * @return a byte array containing ASN.1 encode form.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * ASN.1 DER X.509 EDIPartyName encoder/decoder class.
     */
    public static final ASN1Sequence ASN1 = new ASN1Sequence(
            new ASN1Type[] {
                new ASN1Explicit(0, DirectoryString.ASN1), 
                new ASN1Explicit(1, DirectoryString.ASN1)
            }) {
        {
            setOptional(0);
        }

        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new EDIPartyName((String) values[0], (String) values[1],
                    in.getEncoded());
        }

        protected void getValues(Object object, Object[] values) {
            EDIPartyName epn = (EDIPartyName) object;
            values[0] = epn.nameAssigner;
            values[1] = epn.partyName;
        }
    };
}

