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

package org.apache.harmony.nio_char.tests.java.nio.charset;

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestLevel;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

//@TestTargetClass(CharacterCodingException.class)
/**
 * Test CharacterCodingException
 */
public class CharacterCodingExceptionTest extends TestCase {

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CharacterCodingException",
        args = {}
    )
     */
    public void testConstructor() {
        CharacterCodingException ex = new CharacterCodingException();
        assertTrue(ex instanceof IOException);
        assertNull(ex.getCause());
        assertNull(ex.getMessage());
    }

    /**
     * @tests serialization/deserialization compatibility.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new CharacterCodingException());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
     */
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new CharacterCodingException());

    }
}
