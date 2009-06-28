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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * The class encapsulates the ASN.1 DER encoding/decoding work 
 * with the GeneralSubtrees structure which is a part of X.509 certificate:
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 * 
 * <pre>
 *   GeneralSubtrees ::= SEQUENCE SIZE (1..MAX) OF GeneralSubtree
 * </pre>
 * 
 * @see org.apache.harmony.security.x509.NameConstraints
 * @see org.apache.harmony.security.x509.GeneralSubtree
 */

public class GeneralSubtrees {

    // the list of values of GeneralSubtrees
    private List generalSubtrees;
    // the ASN.1 encoded form of GeneralSubtrees
    private byte[] encoding;
    
    /**
     * Constructs an object representing the value of GeneralSubtrees.
     */
    public GeneralSubtrees() {}
    
    /**
     * TODO
     * @param   generalSubtrees:    List
     */
    public GeneralSubtrees(List generalSubtrees) {
        // TODO: the size should not be less than one
        this.generalSubtrees = generalSubtrees;
    }
    
    /**
     * Returns the list of values of subtrees.
     * @return  subtrees
     */
    public List getSubtrees() {
        return generalSubtrees;
    }

    /**
     * TODO
     * @param   subtree:    GeneralSubtree
     * @return
     */
    public GeneralSubtrees addSubtree(GeneralSubtree subtree) {
        encoding = null;
        if (generalSubtrees == null) {
            generalSubtrees = new ArrayList();
        }
        generalSubtrees.add(subtree);
        return this;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 AlgorithmIdentifier value.
     * @return a byte array containing ASN.1 encode form.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    /**
     * ASN.1 DER X.509 GeneralSubtrees encoder/decoder class.
     */
    public static final ASN1Type ASN1 = new ASN1SequenceOf(GeneralSubtree.ASN1) {

        public Object getDecodedObject(BerInputStream in) {
            return new GeneralSubtrees((List)in.content);
        }

        public Collection getValues(Object object) {
            GeneralSubtrees gss = (GeneralSubtrees) object;
            return (gss.generalSubtrees == null) 
                ? new ArrayList() : gss.generalSubtrees;
        }
    };
}

