/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.tests.java.util;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.IllegalFormatConversionException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

//@TestTargetClass(IllegalFormatConversionException.class) 
public class IllegalFormatConversionExceptionTest extends TestCase {

    /**
     * @tests java.util.IllegalFormatConversionException#IllegalFormatConversionException(char,
     *        Class)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatConversionException",
        args = {char.class, java.lang.Class.class}
    )
     */
    public void test_illegalFormatConversionException() {
        try {
            new IllegalFormatConversionException(' ', null);
            fail("should throw NullPointerExcetpion.");
        } catch (NullPointerException e) {
            // desired
        }
        
        assertNotNull(new IllegalFormatConversionException(' ', String.class));
    }

    /**
     * @tests java.util.IllegalFormatConversionException#getArgumentClass()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getArgumentClass",
        args = {}
    )
     */
    public void test_getArgumentClass() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(argClass, illegalFormatConversionException
                .getArgumentClass());

    }

    /**
     * @tests java.util.IllegalFormatConversionException#getConversion()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConversion",
        args = {}
    )
     */
    public void test_getConversion() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(c, illegalFormatConversionException.getConversion());

    }

    /**
     * @tests java.util.IllegalFormatConversionException#getMessage()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
     */
    public void test_getMessage() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertTrue(null != illegalFormatConversionException.getMessage());

    }

    // comparator for IllegalFormatConversionException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            IllegalFormatConversionException initEx = (IllegalFormatConversionException) initial;
            IllegalFormatConversionException desrEx = (IllegalFormatConversionException) deserialized;

            assertEquals("ArgumentClass", initEx.getArgumentClass(), desrEx
                    .getArgumentClass());
            assertEquals("Conversion", initEx.getConversion(), desrEx
                    .getConversion());
        }
    };

    /**
     * @tests serialization/deserialization.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new IllegalFormatConversionException('*',
                String.class), exComparator);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new IllegalFormatConversionException('*', String.class),
                exComparator);
    }
}
