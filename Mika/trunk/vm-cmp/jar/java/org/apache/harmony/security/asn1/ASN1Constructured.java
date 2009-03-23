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
 * Imported by CG 20090319 based on Apache Harmony ("enhanced") revision 490473.
 */

package org.apache.harmony.security.asn1;


/**
 * This abstract class is the super class for all constructed ASN.1 types
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public abstract class ASN1Constructured extends ASN1Type {

    public ASN1Constructured(int tagNumber) {
        super(CLASS_UNIVERSAL, tagNumber);
    }

    public ASN1Constructured(int tagClass, int tagNumber) {
        super(tagClass, tagNumber);
    }
    
    /**
     * Tests provided identifier.
     *
     * @param identifier - identifier to be verified
     * @return - true if identifier correspond to constructed identifier of
     *           this ASN.1 type, otherwise false
     */
    public final boolean checkTag(int identifier) {
        return this.constrId == identifier;
    }
    
    /**
     *
     */
    public void encodeASN(BerOutputStream out) {
        out.encodeTag(constrId);
        encodeContent(out);
    }
}

