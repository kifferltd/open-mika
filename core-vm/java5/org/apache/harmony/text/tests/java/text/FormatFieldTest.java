/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.text.tests.java.text;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.text.Format;


//@TestTargetClass(Format.Field.class) 
public class FormatFieldTest extends TestCase {
    private class MockFormatField extends Format.Field {

        private static final long serialVersionUID = 1L;

        public MockFormatField(String name) {
            super(name);
        }
    }

    /**
     * @tests java.text.Format.Field#FormatField(java.lang.String) Test of
     *        method java.text.Format.Field#FormatField(java.lang.String).
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class}
    )
     */
    public void test_Constructor() {
        try {
            new MockFormatField("test");
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
