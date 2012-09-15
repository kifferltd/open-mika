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
import java.util.MissingFormatArgumentException;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

//@TestTargetClass(MissingFormatArgumentException.class) 
public class MissingFormatArgumentExceptionTest extends TestCase {

    /**
     * @tests java.util.MissingFormatArgumentException#MissingFormatArgumentException(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MissingFormatArgumentException",
        args = {java.lang.String.class}
    )
     */
    public void test_missingFormatArgumentException() {
        assertNotNull(new MissingFormatArgumentException("String"));

        try {
            new MissingFormatArgumentException(null);
            fail("should throw NullPointerExcepiton.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /**
     * @tests java.util.MissingFormatArgumentException#getFormatSpecifier()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormatSpecifier",
        args = {}
    )
     */
    public void test_getFormatSpecifier() {
        String s = "MYTESTSTRING";
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertEquals(s, missingFormatArgumentException.getFormatSpecifier());
    }

    /**
     * @tests java.util.MissingFormatArgumentException#getMessage()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
     */
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertTrue(null != missingFormatArgumentException.getMessage());

    }

    // comparator for comparing MissingFormatArgumentException objects
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            MissingFormatArgumentException initEx = (MissingFormatArgumentException) initial;
            MissingFormatArgumentException desrEx = (MissingFormatArgumentException) deserialized;

            assertEquals("FormatSpecifier", initEx.getFormatSpecifier(), desrEx
                    .getFormatSpecifier());
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

        SerializationTest.verifySelf(new MissingFormatArgumentException(
                "MYTESTSTRING"), exComparator);
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
                new MissingFormatArgumentException("MYTESTSTRING"),
                exComparator);
    }
}
