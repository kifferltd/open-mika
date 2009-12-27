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


/**
 * This class represents ASN.1 Sequence type.
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public class ASN1Sequence extends ASN1TypeCollection {

    public ASN1Sequence(ASN1Type[] type) {
        super(TAG_SEQUENCE, type);

        //FIXME optional components must be checked for distinct identifiers
    }

    //
    //
    // Decode
    //
    //

    public Object decode(BerInputStream in) throws IOException {
        in.readSequence(this);

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
        out.encodeSequence(this);
    }

    public final void setEncodingContent(BerOutputStream out) {
        out.getSequenceLength(this);
    }
}

