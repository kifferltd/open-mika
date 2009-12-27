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

/*
 * Imported by CG 20090321 based on Apache Harmony ("enhanced") revision 541025.
 */

package org.apache.harmony.security.x509;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.BerInputStream;
import org.apache.harmony.security.asn1.ObjectIdentifier;

/**
 * Extended Key Usage Extension (OID == 2.5.29.37).
 *
 * The ASN.1 definition for Extended Key Usage Extension is:
 *
 * <pre>
 *  id-ce-extKeyUsage OBJECT IDENTIFIER ::= { id-ce 37 }
 *
 *  ExtKeyUsageSyntax ::= SEQUENCE SIZE (1..MAX) OF KeyPurposeId
 *
 *  KeyPurposeId ::= OBJECT IDENTIFIER
 * </pre>
 * (as specified in RFC 3280  http://www.ietf.org/rfc/rfc3280.txt
 */
public class ExtendedKeyUsage extends ExtensionValue {

    // the value of extension
    private List keys;

    /**
     * Creates an object on the base of list of integer arrays representing
     * key purpose IDs.
     */
    public ExtendedKeyUsage(List keys) {
        this.keys = keys;
    }

    /**
     * Creates the extension object on the base of its encoded form.
     */
    public ExtendedKeyUsage(byte[] encoding) {
        super(encoding);
    }

    /**
     * Returns the list of string representation of OIDs corresponding
     * to key purpose IDs.
     */
    public List getExtendedKeyUsage() throws IOException {
        if (keys == null) {
            keys = (List) ASN1.decode(getEncoded());
        }
        return keys;
    }

    /**
     * Returns the encoded form of the object.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(keys);
        }
        return encoding;
    }

    /**
     * Places the string representation of extension value
     * into the StringBuffer object.
     */
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Extended Key Usage: "); //$NON-NLS-1$
        if (keys == null) {
            try {
                keys = getExtendedKeyUsage();
            } catch (IOException e) {
                // incorrect extension value encoding
                super.dumpValue(buffer);
                return;
            }
        }
        buffer.append('[');
        for (Iterator it=keys.iterator(); it.hasNext();) {
            buffer.append(" \"").append(it.next()).append('"'); //$NON-NLS-1$
            if (it.hasNext()) {
                buffer.append(',');
            }
        }
        buffer.append(" ]\n"); //$NON-NLS-1$
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 =
        new ASN1SequenceOf(new ASN1Oid() {

            public Object getDecodedObject(BerInputStream in)
                    throws IOException {
                int[] oid = (int[]) super.getDecodedObject(in);
                return ObjectIdentifier.toString(oid);
            }

        });
}

