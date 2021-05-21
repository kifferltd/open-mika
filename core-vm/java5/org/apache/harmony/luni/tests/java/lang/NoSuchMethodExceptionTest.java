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

package org.apache.harmony.luni.tests.java.lang;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

//@TestTargetClass(NoSuchMethodException.class) 
public class NoSuchMethodExceptionTest extends TestCase {

    /**
     * @tests java.lang.NoSuchMethodException#NoSuchMethodException()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodException",
        args = {}
    )
     */
    public void test_Constructor() {
        NoSuchMethodException e = new NoSuchMethodException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }

    /**
     * @tests java.lang.NoSuchMethodException#NoSuchMethodException(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoSuchMethodException",
        args = {java.lang.String.class}
    )
     */
    public void test_ConstructorLjava_lang_String() {
        NoSuchMethodException e = new NoSuchMethodException("fixture");
        assertEquals("fixture", e.getMessage());
        assertNull(e.getCause());
    }
}
