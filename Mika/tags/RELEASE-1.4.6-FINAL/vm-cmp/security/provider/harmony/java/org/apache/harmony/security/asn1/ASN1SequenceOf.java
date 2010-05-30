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
* @author Vladimir N. Molotkov, Stepan M. Mishura
* @version $Revision$
*/

/*
 * Imported by CG 20090319 based on Apache Harmony ("enhanced") revision 476395.
 */

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * This class represents ASN.1 Sequence OF type.
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ASN1SequenceOf extends ASN1ValueCollection {

    public ASN1SequenceOf(ASN1Type type) {
        super(TAG_SEQUENCE, type);
    }

    //
    //
    // Decode
    //
    //

    public Object decode(BerInputStream in) throws IOException {
        in.readSequenceOf(this);

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    //
    //
    // Encode
    //
    //

    public final void encodeContent(BerOutputStream out) {
        out.encodeSequenceOf(this);
    }

    public final void setEncodingContent(BerOutputStream out) {
        out.getSequenceOfLength(this);
    }

    /**
     * Creates array wrapper of provided ASN1 type
     *
     * @param type - ASN1 type to be wrapped
     * @return - a wrapper for ASN1 set of type. 
     * @throws IOException
     * @see org.apache.harmony.security.asn1.ASN1ValueCollection
     */
    public static ASN1SequenceOf asArrayOf(ASN1Type type) {

        return new ASN1SequenceOf(type) {
            public Object getDecodedObject(BerInputStream in)
                    throws IOException {
                return ((List) in.content).toArray();
            }

            public Collection getValues(Object object) {
                return Arrays.asList((Object[]) object);
            }
        };
    }
}

