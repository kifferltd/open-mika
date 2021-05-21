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

//@TestTargetClass(Byte.class) 
public class ByteTest extends TestCase {

    /**
     * @tests java.lang.Byte#valueOf(byte)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {byte.class}
    )
     */
    public void test_valueOfB() {
        assertEquals(new Byte(Byte.MIN_VALUE), Byte.valueOf(Byte.MIN_VALUE));
        assertEquals(new Byte(Byte.MAX_VALUE), Byte.valueOf(Byte.MAX_VALUE));
        assertEquals(new Byte((byte) 0), Byte.valueOf((byte) 0));

        byte b = Byte.MIN_VALUE + 1;
        while (b < Byte.MAX_VALUE) {
            assertEquals(new Byte(b), Byte.valueOf(b));
            assertSame(Byte.valueOf(b), Byte.valueOf(b));
            b++;
        }
    }

    /**
     * @tests java.lang.Byte#hashCode()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
     */
    public void test_hashCode() {
        assertEquals(1, new Byte((byte) 1).hashCode());
        assertEquals(2, new Byte((byte) 2).hashCode());
        assertEquals(0, new Byte((byte) 0).hashCode());
        assertEquals(-1, new Byte((byte) -1).hashCode());
    }

    /**
     * @tests java.lang.Byte#Byte(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Byte",
        args = {java.lang.String.class}
    )
     */
    public void test_ConstructorLjava_lang_String() {
        assertEquals(new Byte((byte) 0), new Byte("0"));
        assertEquals(new Byte((byte) 1), new Byte("1"));
        assertEquals(new Byte((byte) -1), new Byte("-1"));

        try {
            new Byte("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Byte("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Byte("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            new Byte(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#Byte(byte)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Byte",
        args = {byte.class}
    )
     */
    public void test_ConstructorB() {
        assertEquals(1, new Byte((byte) 1).byteValue());
        assertEquals(2, new Byte((byte) 2).byteValue());
        assertEquals(0, new Byte((byte) 0).byteValue());
        assertEquals(-1, new Byte((byte) -1).byteValue());
    }

    /**
     * @tests java.lang.Byte#byteValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "byteValue",
        args = {}
    )
     */
    public void test_byteValue1() {
        assertEquals(1, new Byte((byte) 1).byteValue());
        assertEquals(2, new Byte((byte) 2).byteValue());
        assertEquals(0, new Byte((byte) 0).byteValue());
        assertEquals(-1, new Byte((byte) -1).byteValue());
    }

    /**
     * @tests java.lang.Byte#equals(Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void test_equalsLjava_lang_Object() {
        assertEquals(new Byte((byte) 0), Byte.valueOf((byte) 0));
        assertEquals(new Byte((byte) 1), Byte.valueOf((byte) 1));
        assertEquals(new Byte((byte) -1), Byte.valueOf((byte) -1));

        Byte fixture = new Byte((byte) 25);
        assertEquals(fixture, fixture);
        assertFalse(fixture.equals(null));
        assertFalse(fixture.equals("Not a Byte"));
    }

    /**
     * @tests java.lang.Byte#toString()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
     */
    public void test_toString() {
        assertEquals("-1", new Byte((byte) -1).toString());
        assertEquals("0", new Byte((byte) 0).toString());
        assertEquals("1", new Byte((byte) 1).toString());
        assertEquals("-1", new Byte((byte) 0xFF).toString());
    }

    /**
     * @tests java.lang.Byte#toString(byte)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {byte.class}
    )
     */
    public void test_toStringB() {
        assertEquals("-1", Byte.toString((byte) -1));
        assertEquals("0", Byte.toString((byte) 0));
        assertEquals("1", Byte.toString((byte) 1));
        assertEquals("-1", Byte.toString((byte) 0xFF));
    }

    /**
     * @tests java.lang.Byte#valueOf(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks only positive functionality.",
        method = "valueOf",
        args = {java.lang.String.class}
    )
     */
    public void test_valueOfLjava_lang_String() {
        assertEquals(new Byte((byte) 0), Byte.valueOf("0"));
        assertEquals(new Byte((byte) 1), Byte.valueOf("1"));
        assertEquals(new Byte((byte) -1), Byte.valueOf("-1"));

        try {
            Byte.valueOf("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#valueOf(String,int)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check boundary values.",
        method = "valueOf",
        args = {java.lang.String.class, int.class}
    )
     */
    public void test_valueOfLjava_lang_StringI() {
        assertEquals(new Byte((byte) 0), Byte.valueOf("0", 10));
        assertEquals(new Byte((byte) 1), Byte.valueOf("1", 10));
        assertEquals(new Byte((byte) -1), Byte.valueOf("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Byte.valueOf("1", 2).byteValue());
        assertEquals(Character.digit('F', 16), Byte.valueOf("F", 16).byteValue());

        try {
            Byte.valueOf("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.valueOf(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#parseByte(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parseByte",
        args = {java.lang.String.class}
    )
     */
    public void test_parseByteLjava_lang_String() {
        assertEquals(0, Byte.parseByte("0"));
        assertEquals(1, Byte.parseByte("1"));
        assertEquals(-1, Byte.parseByte("-1"));

        try {
            Byte.parseByte("0x1");
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte(null);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#parseByte(String,int)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check boundary values.",
        method = "parseByte",
        args = {java.lang.String.class, int.class}
    )
     */
    public void test_parseByteLjava_lang_StringI() {
        assertEquals(0, Byte.parseByte("0", 10));
        assertEquals(1, Byte.parseByte("1", 10));
        assertEquals(-1, Byte.parseByte("-1", 10));

        //must be consistent with Character.digit()
        assertEquals(Character.digit('1', 2), Byte.parseByte("1", 2));
        assertEquals(Character.digit('F', 16), Byte.parseByte("F", 16));

        try {
            Byte.parseByte("0x1", 10);
            fail("Expected NumberFormatException with hex string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("9.2", 10);
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("", 10);
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte(null, 10);
            fail("Expected NumberFormatException with null string.");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#decode(String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class}
    )
     */
    public void test_decodeLjava_lang_String() {
        assertEquals(new Byte((byte) 0), Byte.decode("0"));
        assertEquals(new Byte((byte) 1), Byte.decode("1"));
        assertEquals(new Byte((byte) -1), Byte.decode("-1"));
        assertEquals(new Byte((byte) 0xF), Byte.decode("0xF"));
        assertEquals(new Byte((byte) 0xF), Byte.decode("#F"));
        assertEquals(new Byte((byte) 0xF), Byte.decode("0XF"));
        assertEquals(new Byte((byte) 07), Byte.decode("07"));

        try {
            Byte.decode("9.2");
            fail("Expected NumberFormatException with floating point string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.decode("");
            fail("Expected NumberFormatException with empty string.");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.decode(null);
            //undocumented NPE, but seems consistent across JREs
            fail("Expected NullPointerException with null string.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.Byte#doubleValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "No boundary verification.",
        method = "doubleValue",
        args = {}
    )
     */
    public void test_doubleValue() {
        assertEquals(-1D, new Byte((byte) -1).doubleValue(), 0D);
        assertEquals(0D, new Byte((byte) 0).doubleValue(), 0D);
        assertEquals(1D, new Byte((byte) 1).doubleValue(), 0D);
    }

    /**
     * @tests java.lang.Byte#floatValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify boundary values.",
        method = "floatValue",
        args = {}
    )
     */
    public void test_floatValue() {
        assertEquals(-1F, new Byte((byte) -1).floatValue(), 0F);
        assertEquals(0F, new Byte((byte) 0).floatValue(), 0F);
        assertEquals(1F, new Byte((byte) 1).floatValue(), 0F);
    }

    /**
     * @tests java.lang.Byte#intValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "No boundary verification.",
        method = "intValue",
        args = {}
    )
     */
    public void test_intValue() {
        assertEquals(-1, new Byte((byte) -1).intValue());
        assertEquals(0, new Byte((byte) 0).intValue());
        assertEquals(1, new Byte((byte) 1).intValue());
    }

    /**
     * @tests java.lang.Byte#longValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "No boundary verification.",
        method = "longValue",
        args = {}
    )
     */
    public void test_longValue() {
        assertEquals(-1L, new Byte((byte) -1).longValue());
        assertEquals(0L, new Byte((byte) 0).longValue());
        assertEquals(1L, new Byte((byte) 1).longValue());
    }

    /**
     * @tests java.lang.Byte#shortValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't check boundary values.",
        method = "shortValue",
        args = {}
    )
     */
    public void test_shortValue() {
        assertEquals(-1, new Byte((byte) -1).shortValue());
        assertEquals(0, new Byte((byte) 0).shortValue());
        assertEquals(1, new Byte((byte) 1).shortValue());
    }

    /**
     * @tests java.lang.Byte#compareTo(Byte)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.Byte.class}
    )
     */
    public void test_compareToLjava_lang_Byte() {
        final Byte min = new Byte(Byte.MIN_VALUE);
        final Byte zero = new Byte((byte) 0);
        final Byte max = new Byte(Byte.MAX_VALUE);

        assertTrue(max.compareTo(max) == 0);
        assertTrue(min.compareTo(min) == 0);
        assertTrue(zero.compareTo(zero) == 0);

        assertTrue(max.compareTo(zero) > 0);
        assertTrue(max.compareTo(min) > 0);

        assertTrue(zero.compareTo(max) < 0);
        assertTrue(zero.compareTo(min) > 0);

        assertTrue(min.compareTo(zero) < 0);
        assertTrue(min.compareTo(max) < 0);

        try {
            min.compareTo(null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.lang.Byte#Byte(byte)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary test.",
        method = "Byte",
        args = {byte.class}
    )
     */
    public void test_ConstructorB2() {
        // Test for method java.lang.Byte(byte)

        Byte b = new Byte((byte) 127);
        assertTrue("Byte creation failed", b.byteValue() == (byte) 127);
    }

    /**
     * @tests java.lang.Byte#Byte(java.lang.String)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check empty string or null.",
        method = "Byte",
        args = {java.lang.String.class}
    )
     */
    public void test_ConstructorLjava_lang_String2() {
        // Test for method java.lang.Byte(java.lang.String)

        Byte b = new Byte("127");
        Byte nb = new Byte("-128");
        assertTrue("Incorrect Byte Object created", b.byteValue() == (byte) 127
                && (nb.byteValue() == (byte) -128));

    }

    /**
     * @tests java.lang.Byte#byteValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "byteValue",
        args = {}
    )
     */
    public void test_byteValue() {
        // Test for method byte java.lang.Byte.byteValue()
        assertTrue("Returned incorrect byte value",
                new Byte((byte) 127).byteValue() == (byte) (127));
    }

    /**
     * @tests java.lang.Byte#compareTo(java.lang.Byte)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "compareTo",
        args = {java.lang.Byte.class}
    )
     */
    public void test_compareToLjava_lang_Byte2() {
        // Test for method int java.lang.Byte.compareTo(java.lang.Byte)
        assertTrue("Comparison failed", new Byte((byte) 1).compareTo(new Byte((byte) 2)) < 0);
        assertTrue("Comparison failed", new Byte((byte) 1).compareTo(new Byte((byte) -2)) > 0);
        assertEquals("Comparison failed", 0, new Byte((byte) 1).compareTo(new Byte((byte) 1)));
    }

    /**
     * @tests java.lang.Byte#decode(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "decode",
        args = {java.lang.String.class}
    )
     */
    public void test_decodeLjava_lang_String2() {
        // Test for method java.lang.Byte
        // java.lang.Byte.decode(java.lang.String)
        assertTrue("String decoded incorrectly, wanted: 1 got: " + Byte.decode("1").toString(),
                Byte.decode("1").equals(new Byte((byte) 1)));
        assertTrue("String decoded incorrectly, wanted: -1 got: "
                + Byte.decode("-1").toString(), Byte.decode("-1").equals(new Byte((byte) -1)));
        assertTrue("String decoded incorrectly, wanted: 127 got: "
                + Byte.decode("127").toString(), Byte.decode("127")
                .equals(new Byte((byte) 127)));
        assertTrue("String decoded incorrectly, wanted: -128 got: "
                + Byte.decode("-128").toString(), Byte.decode("-128").equals(
                new Byte((byte) -128)));
        assertTrue("String decoded incorrectly, wanted: 127 got: "
                + Byte.decode("0x7f").toString(), Byte.decode("0x7f").equals(
                new Byte((byte) 127)));
        assertTrue("String decoded incorrectly, wanted: -128 got: "
                + Byte.decode("-0x80").toString(), Byte.decode("-0x80").equals(
                new Byte((byte) -128)));

        boolean exception = false;
        try {
            Byte.decode("128");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MAX_VALUE + 1", exception);

        exception = false;
        try {
            Byte.decode("-129");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for MIN_VALUE - 1", exception);

        exception = false;
        try {
            Byte.decode("0x80");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MAX_VALUE + 1", exception);

        exception = false;
        try {
            Byte.decode("-0x81");
        } catch (NumberFormatException e) {
            // Correct
            exception = true;
        }
        assertTrue("Failed to throw exception for hex MIN_VALUE - 1", exception);
    }

    /**
     * @tests java.lang.Byte#doubleValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks boundary value.",
        method = "doubleValue",
        args = {}
    )
     */
    public void test_doubleValue2() {
        assertEquals(127D, new Byte((byte) 127).doubleValue(), 0.0);
    }

    /**
     * @tests java.lang.Byte#equals(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Checks that negative value doesn't equal to positive.",
        method = "equals",
        args = {java.lang.Object.class}
    )
     */
    public void test_equalsLjava_lang_Object2() {
        // Test for method boolean java.lang.Byte.equals(java.lang.Object)
        Byte b1 = new Byte((byte) 90);
        Byte b2 = new Byte((byte) 90);
        Byte b3 = new Byte((byte) -90);
        assertTrue("Equality test failed", b1.equals(b2));
        assertTrue("Equality test failed", !b1.equals(b3));
    }

    /**
     * @tests java.lang.Byte#floatValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "floatValue",
        args = {}
    )
     */
    public void test_floatValue2() {
        assertEquals(127F, new Byte((byte) 127).floatValue(), 0.0);
    }

    /**
     * @tests java.lang.Byte#hashCode()
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary test.",
        method = "hashCode",
        args = {}
    )
     */
    public void test_hashCode2() {
        // Test for method int java.lang.Byte.hashCode()
        assertEquals("Incorrect hash returned", 127, new Byte((byte) 127).hashCode());
    }

    /**
     * @tests java.lang.Byte#intValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "intValue",
        args = {}
    )
     */
    public void test_intValue2() {
        // Test for method int java.lang.Byte.intValue()
        assertEquals("Returned incorrect int value", 127, new Byte((byte) 127).intValue());
    }

    /**
     * @tests java.lang.Byte#longValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies boundary values.",
        method = "longValue",
        args = {}
    )
     */
    public void test_longValue2() {
        // Test for method long java.lang.Byte.longValue()
        assertEquals("Returned incorrect long value", 127L, new Byte((byte) 127).longValue());
    }

    /**
     * @tests java.lang.Byte#parseByte(java.lang.String)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary verification.",
        method = "parseByte",
        args = {java.lang.String.class}
    )
     */
    public void test_parseByteLjava_lang_String2() {
        assertEquals((byte)127, Byte.parseByte("127"));
        assertEquals((byte)-128, Byte.parseByte("-128"));
        assertEquals((byte)0, Byte.parseByte("0"));
        assertEquals((byte)0x80, Byte.parseByte("-128"));
        assertEquals((byte)0x7F, Byte.parseByte("127"));

        try {
            Byte.parseByte("-1000");
            fail("No NumberFormatException");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("128");
            fail("No NumberFormatException");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("-129");
            fail("No NumberFormatException");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#parseByte(java.lang.String, int)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "parseByte",
        args = {java.lang.String.class, int.class}
    )
     */
    public void test_parseByteLjava_lang_StringI2() {
        // Test for method byte java.lang.Byte.parseByte(java.lang.String, int)
        byte b = Byte.parseByte("127", 10);
        byte bn = Byte.parseByte("-128", 10);
        assertTrue("Invalid parse of dec byte", b == (byte) 127 && (bn == (byte) -128));
        assertEquals("Failed to parse hex value", 10, Byte.parseByte("A", 16));
        assertEquals("Returned incorrect value for 0 hex", 0, Byte.parseByte("0", 16));
        assertTrue("Returned incorrect value for most negative value hex", Byte.parseByte(
                "-80", 16) == (byte) 0x80);
        assertTrue("Returned incorrect value for most positive value hex", Byte.parseByte("7f",
                16) == 0x7f);
        assertEquals("Returned incorrect value for 0 decimal", 0, Byte.parseByte("0", 10));
        assertTrue("Returned incorrect value for most negative value decimal", Byte.parseByte(
                "-128", 10) == (byte) 0x80);
        assertTrue("Returned incorrect value for most positive value decimal", Byte.parseByte(
                "127", 10) == 0x7f);

        try {
            Byte.parseByte("-1000", 10);
            fail("Failed to throw exception");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("128", 10);
            fail("Failed to throw exception for MAX_VALUE + 1");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("-129", 10);
            fail("Failed to throw exception for MIN_VALUE - 1");
        } catch (NumberFormatException e) {
        }

        try {
            Byte.parseByte("80", 16);
            fail("Failed to throw exception for hex MAX_VALUE + 1");
        } catch (NumberFormatException e) {
        }
        
        try {
            Byte.parseByte("-81", 16);
            fail("Failed to throw exception for hex MIN_VALUE + 1");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#shortValue()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "shortValue",
        args = {}
    )
     */
    public void test_shortValue2() {
        assertEquals((short)127, new Byte((byte)127).shortValue());
    }

    /**
     * @tests java.lang.Byte#toString()
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary test.",
        method = "toString",
        args = {}
    )
     */
    public void test_toString2() {
        assertEquals("Returned incorrect String", "127", new Byte((byte) 127).toString());
        assertEquals("Returned incorrect String", "-127", new Byte((byte) -127).toString());
        assertEquals("Returned incorrect String", "-128", new Byte((byte) -128).toString());
    }

    /**
     * @tests java.lang.Byte#toString(byte)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary test.",
        method = "toString",
        args = {byte.class}
    )
     */
    public void test_toStringB2() {
        assertEquals("Returned incorrect String", "127", Byte.toString((byte) 127));
        assertEquals("Returned incorrect String", "-127", Byte.toString((byte) -127));
        assertEquals("Returned incorrect String", "-128", Byte.toString((byte) -128));
    }

    /**
     * @tests java.lang.Byte#valueOf(java.lang.String)
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Boundary test.",
        method = "valueOf",
        args = {java.lang.String.class}
    )
     */
    public void test_valueOfLjava_lang_String2() {
        assertEquals("Returned incorrect byte", 0, Byte.valueOf("0").byteValue());
        assertEquals("Returned incorrect byte", 127, Byte.valueOf("127").byteValue());
        assertEquals("Returned incorrect byte", -127, Byte.valueOf("-127").byteValue());
        assertEquals("Returned incorrect byte", -128, Byte.valueOf("-128").byteValue());

        try {
            Byte.valueOf("128");
            fail("Failed to throw exception when passes value > byte");
        } catch (NumberFormatException e) {
        }
    }

    /**
     * @tests java.lang.Byte#valueOf(java.lang.String, int)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Boundary test.",
        method = "valueOf",
        args = {java.lang.String.class, int.class}
    )
     */
    public void test_valueOfLjava_lang_StringI2() {
        assertEquals("Returned incorrect byte", 10, Byte.valueOf("A", 16).byteValue());
        assertEquals("Returned incorrect byte", 127, Byte.valueOf("127", 10).byteValue());
        assertEquals("Returned incorrect byte", -127, Byte.valueOf("-127", 10).byteValue());
        assertEquals("Returned incorrect byte", -128, Byte.valueOf("-128", 10).byteValue());
        assertEquals("Returned incorrect byte", 127, Byte.valueOf("7f", 16).byteValue());
        assertEquals("Returned incorrect byte", -127, Byte.valueOf("-7f", 16).byteValue());
        assertEquals("Returned incorrect byte", -128, Byte.valueOf("-80", 16).byteValue());

        try {
            Byte.valueOf("128", 10);
            fail("Failed to throw exception when passes value > byte");
        } catch (NumberFormatException e) {
        }
    }
}
