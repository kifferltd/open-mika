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
 * Imported by CG 20090322 based on Apache Harmony ("enhanced") revision 476395.
 */

package org.apache.harmony.security.asn1;


/**
 * Encodes ASN.1 types with DER (X.690)
 * 
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */

public final class DerOutputStream extends BerOutputStream {

    public DerOutputStream(ASN1Type asn1, Object object) {
        super();

        content = object;

        index = -1;
        asn1.setEncodingContent(this);

        encoded = new byte[asn1.getEncodedLength(this)];

        index = 0;
        asn1.encodeASN(this);
    }

    public void encodeChoice(ASN1Choice choice) {

        ASN1Type type = (ASN1Type) val[index][0];

        content = val[index][1];

        index++;

        type.encodeASN(this);
    }

    public void encodeExplicit(ASN1Explicit explicit) {

        content = val[index][0];
        length = len[index][0];

        index++;

        explicit.type.encodeASN(this);
    }

    public void encodeSequence(ASN1Sequence sequence) {

        ASN1Type[] type = sequence.type;

        Object[] values = val[index];
        int[] compLens = len[index];

        index++;
        for (int i = 0; i < type.length; i++) {

            if (values[i] == null) {
                continue;
            }

            content = values[i];
            length = compLens[i];

            type[i].encodeASN(this);
        }
    }

    public void encodeSequenceOf(ASN1SequenceOf sequenceOf) {
        encodeValueCollection(sequenceOf);
    }

    public void encodeSetOf(ASN1SetOf setOf) {
        encodeValueCollection(setOf);
    }

    private final void encodeValueCollection(ASN1ValueCollection collection) {

        Object[] values = val[index];
        int[] compLens = len[index];

        index++;
        for (int i = 0; i < values.length; i++) {

            content = values[i];
            length = compLens[i];

            collection.type.encodeASN(this);
        }
    }

    /*
     * DATA
     */

    private final static int initSize = 32;

    private int index;

    private int[][] len = new int[initSize][];

    private Object[][] val = new Object[initSize][];

    private void push(int[] lengths, Object[] values) {

        index++;
        if (index == val.length) {

            int[][] newLen = new int[val.length * 2][];
            System.arraycopy(len, 0, newLen, 0, val.length);
            len = newLen;

            Object[][] newVal = new Object[val.length * 2][];
            System.arraycopy(val, 0, newVal, 0, val.length);
            val = newVal;
        }
        len[index] = lengths;
        val[index] = values;
    }

    /*
     * LENGTH 
     */

    public void getChoiceLength(ASN1Choice choice) {

        int i = choice.getIndex(content);
        content = choice.getObjectToEncode(content);

        Object[] values = new Object[] { choice.type[i], content };

        push(null, values);

        choice.type[i].setEncodingContent(this);

        // in case if we get content bytes while getting its length
        // FIXME what about remove it: need redesign
        values[1] = content;
    }

    public void getExplicitLength(ASN1Explicit explicit) {

        Object[] values = new Object[1];
        int[] compLens = new int[1];

        values[0] = content;

        push(compLens, values);

        explicit.type.setEncodingContent(this);

        // in case if we get content bytes while getting its length
        // FIXME what about remove it: need redesign
        values[0] = content;
        compLens[0] = length;

        length = explicit.type.getEncodedLength(this);
    }

    public void getSequenceLength(ASN1Sequence sequence) {

        ASN1Type[] type = sequence.type;

        Object[] values = new Object[type.length];
        int[] compLens = new int[type.length];

        sequence.getValues(content, values);

        push(compLens, values);

        int seqLen = 0;
        for (int i = 0; i < type.length; i++) {
            // check optional types
            if (values[i] == null) {
                if (sequence.OPTIONAL[i]) {
                    continue;
                } else {
                    throw new RuntimeException();//FIXME type & message
                }
            }

            if (sequence.DEFAULT[i] != null
                    && sequence.DEFAULT[i].equals(values[i])) {
                values[i] = null;
                continue;
            }

            content = values[i];

            type[i].setEncodingContent(this);

            compLens[i] = length;

            // in case if we get content bytes while getting its length
            // FIXME what about remove it: need redesign
            values[i] = content;

            seqLen += type[i].getEncodedLength(this);
        }
        length = seqLen;
    }

    public void getSequenceOfLength(ASN1SequenceOf sequence) {
        getValueOfLength(sequence);
    }

    public void getSetOfLength(ASN1SetOf setOf) {
        getValueOfLength(setOf);
    }

    private void getValueOfLength(ASN1ValueCollection collection) {

        //FIXME what about another way?
        Object[] cv = collection.getValues(content).toArray();

        Object[] values = new Object[cv.length];
        int[] compLens = new int[values.length];

        push(compLens, values);
        int seqLen = 0;
        for (int i = 0; i < values.length; i++) {

            content = cv[i];

            collection.type.setEncodingContent(this);

            compLens[i] = length;

            // in case if we get content bytes while getting its length
            // FIXME what about remove it: need redesign
            values[i] = content;

            seqLen += collection.type.getEncodedLength(this);
        }
        length = seqLen;
    }
}

