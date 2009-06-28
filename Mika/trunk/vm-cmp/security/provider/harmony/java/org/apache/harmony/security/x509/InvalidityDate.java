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
import java.util.Date;

import org.apache.harmony.security.asn1.ASN1GeneralizedTime;
import org.apache.harmony.security.asn1.ASN1Type;

/**
 * CRL Entry's Invalidity Date Extension (OID = 2.5.29.24).
 * <pre>
 *   id-ce-invalidityDate OBJECT IDENTIFIER ::= { id-ce 24 }
 *
 *   invalidityDate ::=  GeneralizedTime
 * </pre>
 * (as specified in RFC 3280 http://www.ietf.org/rfc/rfc3280.txt)
 */
public class InvalidityDate extends ExtensionValue {

    // invalidity date value
    private final Date date;

    /**
     * Constructs the object on the base of the invalidity date value.
     */
    public InvalidityDate(Date date) {
        this.date = date;
    }

    /**
     * Constructs the object on the base of its encoded form.
     */
    public InvalidityDate(byte[] encoding) throws IOException {
        super(encoding);
        date = (Date) ASN1.decode(encoding);
    }

    /**
     * Returns the invalidity date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns ASN.1 encoded form of this X.509 InvalidityDate value.
     * @return a byte array containing ASN.1 encoded form.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(date);
        }
        return encoding;
    }

    /**
     * Places the string representation of extension value
     * into the StringBuffer object.
     */
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Invalidity Date: [ ") //$NON-NLS-1$
            .append(date).append(" ]\n"); //$NON-NLS-1$
    }

    /**
     * ASN.1 Encoder/Decoder.
     */
    public static final ASN1Type ASN1 = ASN1GeneralizedTime.getInstance();
}

