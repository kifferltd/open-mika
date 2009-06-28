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
* @author Alexander V. Esin
* @version $Revision$
*/

/*
 * Imported by CG 20090321 based on Apache Harmony ("enhanced") revision 541025.
 */

package org.apache.harmony.security.x501;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.harmony.security.utils.ObjectIdentifier;

/**
 * AttributeTypeAndValue comparator
 * 
 */
public class AttributeTypeAndValueComparator implements Comparator, Serializable {

	private static final long serialVersionUID = -1286471842007103132L;

	/**
     * compares two AttributeTypeAndValues
     * 
     * @param obj1
     *            first AttributeTypeAndValue
     * @param obj2
     *            second AttributeTypeAndValue
     * @return -1 of first AttributeTypeAndValue "less" than second
     *         AttributeTypeAndValue 1 otherwise, 0 if they are equal
     */
    public int compare(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return 0;
        }

        AttributeTypeAndValue atav1 = (AttributeTypeAndValue) obj1;
        AttributeTypeAndValue atav2 = (AttributeTypeAndValue) obj2;
        String kw1 = atav1.getType().getName();
        String kw2 = atav2.getType().getName();
        if (kw1 != null && kw2 == null) {
            return -1;
        }
        if (kw1 == null && kw2 != null) {
            return 1;
        }
        if (kw1 != null && kw2 != null) {
            return kw1.compareTo(kw2);
        }

        return compateOids(atav1.getType(), atav2.getType());
    }
    
    /**
     * compares two Object identifiers
     * 
     * @param oid1
     *            first OID
     * @param oid2
     *            second OID
     * @return -1 of first OID "less" than second OID 1 otherwise, 0 if they are
     *         equal
     */
    private static int compateOids(ObjectIdentifier oid1, ObjectIdentifier oid2) {
        if (oid1 == oid2) {
            return 0;
        }

        int[] ioid1 = oid1.getOid();
        int[] ioid2 = oid2.getOid();
        int min = ioid1.length < ioid2.length ? ioid1.length : ioid2.length;
        for (int i = 0; i < min; ++i) {
            if (ioid1[i] < ioid2[i]) {
                return -1;
            }
            if (ioid1[i] > ioid2[i]) {
                return 1;
            }
            if ((i + 1) == ioid1.length && (i + 1) < ioid2.length) {
                return -1;
            }
            if ((i + 1) < ioid1.length && (i + 1) == ioid2.length) {
                return 1;
            }
        }
        return 0;
    }
}

