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

package org.apache.harmony.luni.tests.java.io;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargetNew;
//@TestTargetClass(BufferedReader.class)
public class BufferedReaderTest extends TestCase {

    /**
     * @tests java.io.BufferedReader#read(char[], int, int)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks exceptions.",
        method = "read",
        args = {char[].class, int.class, int.class}
    )
     */
    public void test_read$CII() throws IOException {
        char[] in = {'L', 'o', 'r', 'e', 'm'};
        char[] ch = new char[3];
        BufferedReader reader = new BufferedReader(new CharArrayReader(in));
        
        try {
            reader.read(null, 1, 0);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
            // Expected.
        }

        try {
            reader.read(ch , -1, 1);
            fail("Test 2: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }
        
        try {
            reader.read(ch , 1, -1);
            fail("Test 3: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        try {
            reader.read(ch, 1, 3);
            fail("Test 4: IndexOutOfBoundsException expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected.
        }

        reader.close();
        try {
            reader.read(ch, 1, 1);
            fail("Test 5: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }
    
    /**
     * @tests java.io.BufferedReader#mark(int)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "mark",
        args = {int.class}
    )
     */
    public void test_markI() throws IOException {
        BufferedReader buf = new BufferedReader(new StringReader("01234"), 2);

        try {
            buf.mark(-1);
            fail("Test 1: IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Expected.
        }
               
        buf.mark(3);
        char[] chars = new char[3];
        int result = buf.read(chars);
        assertEquals(3, result);
        assertEquals("Assert 0:", '0', chars[0]);
        assertEquals("Assert 1:", '1', chars[1]);
        assertEquals("Assert 2:", '2', chars[2]);
        assertEquals("Assert 3:", '3', buf.read());

        buf = new BufferedReader(new StringReader("01234"), 2);
        buf.mark(3);
        chars = new char[4];
        result = buf.read(chars);
        assertEquals("Assert 4:", 4, result);
        assertEquals("Assert 5:", '0', chars[0]);
        assertEquals("Assert 6:", '1', chars[1]);
        assertEquals("Assert 7:", '2', chars[2]);
        assertEquals("Assert 8:", '3', chars[3]);
        assertEquals("Assert 9:", '4', buf.read());
        assertEquals("Assert 10:", -1, buf.read());

        BufferedReader reader = new BufferedReader(new StringReader("01234"));
        reader.mark(Integer.MAX_VALUE);
        reader.read();
        reader.close();
        
        try {
            reader.mark(1);
            fail("Test 2: IOException expected.");
        } catch (IOException e) {
            // Expected.
        }
    }

}
