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
 * @author Elena Semukhina
 * @version $Revision$
 */

package org.apache.harmony.math.tests.java.math;

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;

import java.math.BigInteger;

import junit.framework.TestCase;
//@TestTargetClass(BigInteger.class)
/**
 * Class:   java.math.BigInteger
 * Methods: abs, compareTo, equals, max, min, negate, signum
 */
public class BigIntegerCompareTest extends TestCase {
    /**
     * abs() for a positive number
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for abs method.",
        method = "abs",
        args = {}
    )
     */
    public void testAbsPositive() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = 1;
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.abs();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * abs() for a negative number
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for abs method.",
        method = "abs",
        args = {}
    )
     */
    public void testAbsNegative() {
        byte aBytes[] = {1, 2, 3, 4, 5, 6, 7};
        int aSign = -1;
        byte rBytes[] = {1, 2, 3, 4, 5, 6, 7};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.abs();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * compareTo(BigInteger a).
     * Compare two positive numbers.
     * The first is greater.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToPosPos1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(1, aNumber.compareTo(bNumber));
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two positive numbers.
     * The first is less.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToPosPos2() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(-1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare two equal positive numbers.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToEqualPos() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(0, aNumber.compareTo(bNumber));
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two negative numbers.
     * The first is greater in absolute value.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToNegNeg1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(-1, aNumber.compareTo(bNumber));
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two negative numbers.
     * The first is less  in absolute value.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareNegNeg2() {
        byte aBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare two equal negative numbers.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToEqualNeg() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(0, aNumber.compareTo(bNumber));
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare two numbers of different signs.
     * The first is positive.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToDiffSigns1() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = 1;
        int bSign = -1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare two numbers of different signs.
     * The first is negative.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToDiffSigns2() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {10, 20, 30, 40, 50, 60, 70, 10, 20, 30};
        int aSign = -1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(-1, aNumber.compareTo(bNumber));
    }
    
    /**
     * compareTo(BigInteger a).
     * Compare a positive number to ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToPosZero() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        assertEquals(1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to a positive number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToZeroPos() {
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int bSign = 1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(-1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare a negative number to ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToNegZero() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        assertEquals(-1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to a negative number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToZeroNeg() {
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int bSign = -1;
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        assertEquals(1, aNumber.compareTo(bNumber));
    }

    /**
     * compareTo(BigInteger a).
     * Compare ZERO to ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for compareTo method.",
        method = "compareTo",
        args = {java.math.BigInteger.class}
    )
     */
    public void testCompareToZeroZero() {
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger bNumber = BigInteger.ZERO;
        assertEquals(0, aNumber.compareTo(bNumber));
    }

    /**
     * equals(Object obj).
     * obj is not a BigInteger
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void testEqualsObject() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object obj = new Object();
        assertFalse(aNumber.equals(obj));
    }

    /**
     * equals(null).
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void testEqualsNull() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertFalse(aNumber.equals(null));
    }

    /**
     * equals(Object obj).
     * obj is a BigInteger.
     * numbers are equal.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void testEqualsBigIntegerTrue() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object bNumber = new BigInteger(bSign, bBytes);
        assertTrue(aNumber.equals(bNumber));
    }

    /**
     * equals(Object obj).
     * obj is a BigInteger.
     * numbers are not equal.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for equals method.",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void testEqualsBigIntegerFalse() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        Object bNumber = new BigInteger(bSign, bBytes);
        assertFalse(aNumber.equals(bNumber));
    }

    /**
     * max(BigInteger val).
     * the first is greater.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMaxGreater() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * the first is less.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMaxLess() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * numbers are equal.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMaxEqual() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * max(BigInteger val).
     * max of negative and ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for max method.",
        method = "max",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMaxNegZero() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.max(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 0);
    }

    /**
     * min(BigInteger val).
     * the first is greater.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for mix method.",
        method = "min",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMinGreater() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * min(BigInteger val).
     * the first is less.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for mix method.",
        method = "min",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMinLess() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertEquals("incorrect sign", 1, result.signum());
    }

    /**
     * min(BigInteger val).
     * numbers are equal.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for mix method.",
        method = "min",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMinEqual() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        byte bBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        int bSign = 1;        
        byte rBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = new BigInteger(bSign, bBytes);
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * max(BigInteger val).
     * min of positive and ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for mix method.",
        method = "min",
        args = {java.math.BigInteger.class}
    )
     */
    public void testMinPosZero() {
        byte aBytes[] = {45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        byte rBytes[] = {0};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger bNumber = BigInteger.ZERO;
        BigInteger result = aNumber.min(bNumber);
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 0);
    }
    
    /**
     * negate() a positive number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for negate method.",
        method = "negate",
        args = {}
    )
     */
    public void testNegatePositive() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        byte rBytes[] = {-13, -57, -101, 1, 75, -90, -46, -92, -4, 14, -36, -27, -4, -91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == -1);
    }

    /**
     * negate() a negative number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for negate method.",
        method = "negate",
        args = {}
    )
     */
    public void testNegateNegative() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        byte rBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertTrue("incorrect sign", result.signum() == 1);
    }

    /**
     * negate() ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for negate method.",
        method = "negate",
        args = {}
    )
     */
    public void testNegateZero() {
        byte rBytes[] = {0};
        BigInteger aNumber = BigInteger.ZERO;
        BigInteger result = aNumber.negate();
        byte resBytes[] = new byte[rBytes.length];
        resBytes = result.toByteArray();
        for(int i = 0; i < resBytes.length; i++) {
            assertTrue(resBytes[i] == rBytes[i]);
        }    
        assertEquals("incorrect sign", 0, result.signum());
    }

    /**
     * signum() of a positive number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
     */
    public void testSignumPositive() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = 1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals("incorrect sign", 1, aNumber.signum());
    }
    
    /**
     * signum() of a negative number.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
     */
    public void testSignumNegative() {
        byte aBytes[] = {12, 56, 100, -2, -76, 89, 45, 91, 3, -15, 35, 26, 3, 91};
        int aSign = -1;
        BigInteger aNumber = new BigInteger(aSign, aBytes);
        assertEquals("incorrect sign", -1, aNumber.signum());
    }
    
    /**
     * signum() of ZERO.
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for signum method.",
        method = "signum",
        args = {}
    )
     */
    public void testSignumZero() {
        BigInteger aNumber = BigInteger.ZERO;
        assertEquals("incorrect sign", 0, aNumber.signum());
    }
}
