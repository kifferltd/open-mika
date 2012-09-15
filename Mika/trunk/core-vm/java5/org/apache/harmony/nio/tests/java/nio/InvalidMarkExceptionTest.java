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
package org.apache.harmony.nio.tests.java.nio;

//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import java.nio.InvalidMarkException;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;

//@TestTargetClass(InvalidMarkException.class)
public class InvalidMarkExceptionTest extends TestCase {

    /**
     * @tests serialization/deserialization compatibility.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
     */
    public void testSerializationSelf() throws Exception {

        SerializationTest.verifySelf(new InvalidMarkException());
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )    
     */
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this, new InvalidMarkException());
    }

    // BEGIN android-added
    // copied from newer version of harmony
    /**
     *@tests {@link java.nio.InvalidMarkException#InvalidMarkException()}
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidMarkException",
        args = {}
    )
     */
    public void test_Constructor() {
        InvalidMarkException exception = new InvalidMarkException();
        assertNull(exception.getMessage());
        assertNull(exception.getLocalizedMessage());
        assertNull(exception.getCause());
    }
    // END android-added
}
