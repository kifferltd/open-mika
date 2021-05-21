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

package org.apache.harmony.luni.tests.java.lang;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

//@TestTargetClass(Boolean.class) 
public class BooleanTest extends TestCase {

    /**
     * @tests java.lang.Boolean#hashCode()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
     */
    public void test_hashCode() {
        assertEquals(1231, Boolean.TRUE.hashCode());
        assertEquals(1237, Boolean.FALSE.hashCode());
    }

    /**
     * @tests java.lang.Boolean#Boolean(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Boolean",
        args = {java.lang.String.class}
    )
     */
    public void test_ConstructorLjava_lang_String() {
        assertEquals(Boolean.TRUE, new Boolean("TRUE"));
        assertEquals(Boolean.TRUE, new Boolean("true"));
        assertEquals(Boolean.TRUE, new Boolean("True"));

        assertEquals(Boolean.FALSE, new Boolean("yes"));
        assertEquals(Boolean.FALSE, new Boolean("false"));
    }

    /**
     * @tests java.lang.Boolean#Boolean(boolean)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Boolean",
        args = {boolean.class}
    )
     */
    public void test_ConstructorZ() {
        assertEquals(Boolean.TRUE, new Boolean(true));
        assertEquals(Boolean.FALSE, new Boolean(false));
    }

    /**
     * @tests java.lang.Boolean#booleanValue()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "booleanValue",
        args = {}
    )
     */
    public void test_booleanValue() {
        assertTrue(Boolean.TRUE.booleanValue());
        assertFalse(Boolean.FALSE.booleanValue());
    }

    /**
     * @tests java.lang.Boolean#equals(Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void test_equalsLjava_lang_Object() {
        assertTrue(Boolean.TRUE.equals(Boolean.TRUE));
        assertTrue(Boolean.TRUE.equals(new Boolean(true)));
        assertFalse(Boolean.TRUE.equals("true"));
        assertFalse(Boolean.TRUE.equals(null));
        assertFalse(Boolean.FALSE.equals(Boolean.TRUE));
        assertTrue(Boolean.FALSE.equals(Boolean.FALSE));
        assertTrue(Boolean.FALSE.equals(new Boolean(false)));
    }

    /**
     * @tests java.lang.Boolean#getBoolean(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBoolean",
        args = {java.lang.String.class}
    )
     */
    public void test_getBooleanLjava_lang_String() {
        System.setProperty(getClass().getName(), "true");
        assertTrue(Boolean.getBoolean(getClass().getName()));

        System.setProperty(getClass().getName(), "TRUE");
        assertTrue(Boolean.getBoolean(getClass().getName()));

        System.setProperty(getClass().getName(), "false");
        assertFalse(Boolean.getBoolean(getClass().getName()));
    }

    /**
     * @tests java.lang.Boolean#toString()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
     */
    public void test_toString() {
        assertEquals("true", Boolean.TRUE.toString());
        assertEquals("false", Boolean.FALSE.toString());
    }

    /**
     * @tests java.lang.Boolean#toString(boolean)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {boolean.class}
    )
     */
    public void test_toStringZ() {
        assertEquals("true", Boolean.toString(true));
        assertEquals("false", Boolean.toString(false));
    }

    /**
     * @tests java.lang.Boolean#valueOf(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
     */
    public void test_valueOfLjava_lang_String() {
        assertEquals(Boolean.TRUE, Boolean.valueOf("true"));
        assertEquals(Boolean.FALSE, Boolean.valueOf("false"));

        assertEquals(Boolean.TRUE, Boolean.valueOf("TRUE"));
        assertEquals(Boolean.FALSE, Boolean.valueOf("false"));

        assertEquals(Boolean.FALSE, Boolean.valueOf(null));
        assertEquals(Boolean.FALSE, Boolean.valueOf(""));
        assertEquals(Boolean.FALSE, Boolean.valueOf("invalid"));

        assertTrue("Failed to parse true to true", Boolean.valueOf("true").booleanValue());
        assertTrue("Failed to parse mixed case true to true", Boolean.valueOf("TrUe")
                .booleanValue());
        assertTrue("parsed non-true to true", !Boolean.valueOf("ddddd").booleanValue());
    }

    /**
     * @tests java.lang.Boolean#valueOf(boolean)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {boolean.class}
    )
     */
    public void test_valueOfZ() {
        assertEquals(Boolean.TRUE, Boolean.valueOf(true));
        assertEquals(Boolean.FALSE, Boolean.valueOf(false));
    }

    /**
     * @tests java.lang.Boolean#parseBoolean(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parseBoolean",
        args = {java.lang.String.class}
    )
     */
    public void test_parseBooleanLjava_lang_String() {
        assertTrue(Boolean.parseBoolean("true"));
        assertTrue(Boolean.parseBoolean("TRUE"));
        assertFalse(Boolean.parseBoolean("false"));
        assertFalse(Boolean.parseBoolean(null));
        assertFalse(Boolean.parseBoolean(""));
        assertFalse(Boolean.parseBoolean("invalid"));
    }

    /**
     * @tests java.lang.Boolean#compareTo(Boolean)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.Boolean.class}
    )
     */
    public void test_compareToLjava_lang_Boolean() {
        assertTrue(Boolean.TRUE.compareTo(Boolean.TRUE) == 0);
        assertTrue(Boolean.FALSE.compareTo(Boolean.FALSE) == 0);
        assertTrue(Boolean.TRUE.compareTo(Boolean.FALSE) > 0);
        assertTrue(Boolean.FALSE.compareTo(Boolean.TRUE) < 0);

        try {
            Boolean.TRUE.compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }
}
