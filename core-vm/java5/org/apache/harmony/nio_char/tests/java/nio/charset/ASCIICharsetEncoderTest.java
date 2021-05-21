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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import junit.framework.TestCase;
//@TestTargetClass(CharsetEncoder.class)
public class ASCIICharsetEncoderTest extends TestCase {

    // charset for ascii
    private static final Charset cs = Charset.forName("ascii");
    private static final CharsetEncoder encoder = cs.newEncoder();
    private static final int MAXCODEPOINT = 0x7F; 
    /*
     * @see CharsetEncoderTest#setUp()
     */
    protected void setUp() throws Exception {
    }

    /*
     * @see CharsetEncoderTest#tearDown()
     */
    protected void tearDown() throws Exception {
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {java.lang.CharSequence.class}
    )
    */
    public void testCanEncodeCharSequence() {
        // normal case for ascCS
        assertTrue(encoder.canEncode("\u0077"));
        assertFalse(encoder.canEncode("\uc2a3"));
        assertFalse(encoder.canEncode("\ud800\udc00"));
        try {
            encoder.canEncode(null);
        } catch (NullPointerException e) {
        }
        assertTrue(encoder.canEncode(""));
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "IllegalStateException checking missed.",
            method = "canEncode",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "IllegalStateException checking missed.",
            method = "canEncode",
            args = {char.class}
        )
    })
    */
    public void testCanEncodeSurrogate () {
        assertFalse(encoder.canEncode('\ud800'));
        assertFalse(encoder.canEncode("\udc00"));
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed.",
        method = "canEncode",
        args = {char.class}
    )
    */
    public void testCanEncodechar() throws CharacterCodingException {
        assertTrue(encoder.canEncode('\u0077'));
        assertFalse(encoder.canEncode('\uc2a3'));
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "averageBytesPerChar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "maxBytesPerChar",
            args = {}
        )
    })
    */
    public void testSpecificDefaultValue() {
        assertEquals(1.0, encoder.averageBytesPerChar(), 0.0);
        assertEquals(1.0, encoder.maxBytesPerChar(), 0.0);
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
        )
    })
    */
    public void testMultiStepEncode() throws CharacterCodingException {
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            encoder.encode(CharBuffer.wrap("\ud800\udc00"));
            fail("should unmappable");
        } catch (UnmappableCharacterException e) {
        }
        encoder.reset();
        ByteBuffer out = ByteBuffer.allocate(10);
        assertTrue(encoder.encode(CharBuffer.wrap("\ud800"), out, true)
                .isMalformed());
        encoder.flush(out);
        encoder.reset();
        out = ByteBuffer.allocate(10);
        assertSame(CoderResult.UNDERFLOW, encoder.encode(CharBuffer
                .wrap("\ud800"), out, false));
        assertTrue(encoder.encode(CharBuffer.wrap("\udc00"), out, true)
                .isMalformed());
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "encode",
        args = {java.nio.CharBuffer.class}
    )
    */
    public void testEncodeMapping() throws CharacterCodingException {
        encoder.reset();
        
        for (int i =0; i <= MAXCODEPOINT; i++) {
            char[] chars = Character.toChars(i);
            CharBuffer cb = CharBuffer.wrap(chars);
            ByteBuffer bb = encoder.encode(cb);
            assertEquals(i, bb.get(0));
        }
        
        CharBuffer cb = CharBuffer.wrap("\u0080");
        try {
            encoder.encode(cb);
        } catch (UnmappableCharacterException e) {
            //expected
        }
        
        cb = CharBuffer.wrap("\ud800");
        try {
            encoder.encode(cb);
        } catch (MalformedInputException e) {
            //expected
        }

        ByteBuffer bb = ByteBuffer.allocate(0x10);
        cb = CharBuffer.wrap("A");
        encoder.reset();
        encoder.encode(cb, bb, false);
        try {
        encoder.encode(cb);
        } catch (IllegalStateException e) {
            //expected
        }
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "reset",
            args = {}
        )
    })
    */
    public void testInternalState() {
        CharBuffer in = CharBuffer.wrap("A");
        ByteBuffer out = ByteBuffer.allocate(0x10);
        
        //normal encoding process
        encoder.reset();
        encoder.encode(in, out, false);
        in = CharBuffer.wrap("B");
        encoder.encode(in, out, true);
        encoder.flush(out);
    }
    
    //reset could be called at any time
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Checks functionality. Exceptions checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        )
    })
    */
    public void testInternalState_Reset() {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > reset
        newEncoder.reset();
        
        //reset - > reset
        newEncoder.reset();

        //encoding - >reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            newEncoder.reset();
        }

        //encoding end -> reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
        }
        //flused -> reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            newEncoder.reset();
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    */
    public void testInternalState_Encoding() {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
        }
        
        //reset - > encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();            
            newEncoder.encode(in, out, false);
        }
        //reset - > encoding - > encoding
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, false);
        }
        
        //encoding_end - > encoding
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
        //flushed - > encoding
        {
            newEncoder.reset();            
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    */
    public void testInternalState_Encoding_END() {
        CharsetEncoder newEncoder = cs.newEncoder();

        //Init - >encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
        }
        
        //Reset -> encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.reset();
            newEncoder.encode(in, out, true);
        }

        //encoding -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }
        
        //Reset -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in, out, true);
        }
        
        //Flushed -> encoding_end
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            in = CharBuffer.wrap("BC");
            try {
                newEncoder.encode(in, out, true);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                //expected
            }
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CoderMalfunctionError checking missed.",
        method = "encode",
        args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
    )
    */
    public void testInternalState_Flushed() {
        CharsetEncoder newEncoder = cs.newEncoder();
        
        //init -> flushed
        {
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.flush(out);
        }
        
        //reset - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.reset();
            newEncoder.flush(out);
        }
        
        //encoding - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            try {

                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
        
        //encoding_end -> flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
        }
        
        //flushd - > flushed
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            newEncoder.flush(out);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Functional test.",
            method = "reset",
            args = {}
        )
    })
    */
    public void testInternalState_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();
        //Init - > encode
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }
        
        //Reset - > encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
        }
        
        //Encoding -> encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, false);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
        
        //Encoding_end -> encode
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
        
        //Flushed -> reset
        {
            newEncoder.reset();
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = ByteBuffer.allocate(0x10);
            newEncoder.encode(in, out, true);
            in = CharBuffer.wrap("BC");
            newEncoder.flush(out);
            out = newEncoder.encode(in);
        }
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "encode",
            args = {java.nio.CharBuffer.class, java.nio.ByteBuffer.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "flush",
            args = {java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "CoderMalfunctionError checking missed.",
            method = "reset",
            args = {}
        )
    })
    */
    public void testInternalState_from_Encode() throws CharacterCodingException {
        CharsetEncoder newEncoder = cs.newEncoder();
        
        //Encode -> Reset
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            newEncoder.reset();
        }
        
        // Encode -> encoding
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.encode(in, out, false);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
        
        //Encode -> Encoding_end
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            ByteBuffer out = ByteBuffer.allocate(0x10);
            try {
                newEncoder.encode(in, out, true);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
        //Encode -> Flushed
        {
            CharBuffer in = CharBuffer.wrap("A");
            ByteBuffer out = newEncoder.encode(in);
            try {
                newEncoder.flush(out);
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }
        
        //Encode - > encode
        {
            CharBuffer in = CharBuffer.wrap("A");
            newEncoder.encode(in);
            in = CharBuffer.wrap("BC");
            newEncoder.encode(in);
        }
    }
}
