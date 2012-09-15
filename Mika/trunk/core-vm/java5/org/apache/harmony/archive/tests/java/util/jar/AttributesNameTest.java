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

package org.apache.harmony.archive.tests.java.util.jar;

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;

import java.util.jar.Attributes;
import junit.framework.TestCase;

//@TestTargetClass(Attributes.Name.class)
public class AttributesNameTest extends TestCase {

    /**
     * @tests java.util.jar.Attributes.Name#Name(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Name",
        args = {java.lang.String.class}
    )
     */
    public void test_AttributesName_Constructor() {
        // Regression for HARMONY-85
        try {
            new Attributes.Name(
                    "01234567890123456789012345678901234567890123456789012345678901234567890");
            fail("Assert 0: should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new Attributes.Name((String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
            // expected
        }

        assertNotNull(new Attributes.Name("Attr"));
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void test_equalsLjava_lang_Object() {
        Attributes.Name attr1 = new Attributes.Name("Attr");
        Attributes.Name attr2 = new Attributes.Name("Attr");

        assertTrue(attr1.equals(attr2));
        attr2 = new Attributes.Name("Attr1");
        assertFalse(attr1.equals(attr2));
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    */
    public void test_hashCode() {
        Attributes.Name attr1 = new Attributes.Name("Attr1");
        Attributes.Name attr2 = new Attributes.Name("Attr2");

        assertNotSame(attr1.hashCode(), attr2.hashCode());
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    */
    public void test_toString() {
        String str1 = "Attr1";
        String str2 = "Attr2";
        Attributes.Name attr1 = new Attributes.Name(str1);
        Attributes.Name attr2 = new Attributes.Name("Attr2");

        assertTrue(attr1.toString().equals(str1));
        assertTrue(attr2.toString().equals(str2));
        assertFalse(attr2.toString().equals(str1));
    }
}
