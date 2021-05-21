/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.archive.tests.java.util.zip;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.zip.DataFormatException;

//@TestTargetClass(DataFormatException.class)
public class DataFormatExceptionTest extends TestCase {

/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DataFormatException",
        args = {}
    )
*/
    public void testDataFormatException() {
        DataFormatException dfe = new DataFormatException();
        assertEquals(dfe.getMessage(), null);
    }

/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DataFormatException",
        args = {java.lang.String.class}
    )
*/
    public void testDataFormatExceptionString() {
        DataFormatException dfe = new DataFormatException("Test");
        assertEquals(dfe.getMessage(), "Test");
    }

}
