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
package org.apache.harmony.nio.tests.java.nio.channels;

//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import tests.support.Support_PortManager;

import junit.framework.TestCase;

/**
 * Note: the test case uses a temp text file named "test" which contains 31
 * characters : "P@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]"
 * 
 */
//@TestTargetClass(Channels.class)
public class ChannelsTest extends TestCase {
    private static final String CODE_SET = "GB2312"; //$NON-NLS-1$

    private static final String BAD_CODE_SET = "GB2313"; //$NON-NLS-1$

    private FileInputStream fins;

    private FileOutputStream fouts;

    private final int writebufSize = 60;

    private final int testNum = 10;

    private final int fileSize = 31;// the file size
    
    private File tmpFile;

    protected void setUp() throws Exception {
        super.setUp();
        // Make the test file same in every test
        tmpFile = File.createTempFile("test","tmp");
        tmpFile.deleteOnExit();
        this.writeFileSame();
    }

    protected void tearDown() throws Exception {
        if (null != this.fins) {
            this.fins.close();
            this.fins = null;
        }
        if (null != this.fouts) {
            this.fouts.close();
            this.fouts = null;
        }

        tmpFile.delete();
        super.tearDown();

    }

    private void writeFileSame() throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.fouts.flush();
        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        this.fouts.write(writebuf.getBytes());
    }

    /*
     * This private method is to assert if the file size is the same as the
     * compare Number in the test
     */
    private void assertFileSizeSame(File fileToTest, int compareNumber)
            throws IOException {
        FileInputStream file = new FileInputStream(fileToTest);
        assertEquals(file.available(), compareNumber);
        file.close();
    }

    // test if new Channel to input is null
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.InputStream.class}
    )
    */
    public void testNewChannelInputStream_InputNull() throws IOException {
        ByteBuffer byteBuf = ByteBuffer.allocate(this.testNum);
        this.fins = null;
        int readres = this.testNum;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        assertNotNull(rbChannel);
        try {
            readres = rbChannel.read(byteBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(this.testNum, readres);
    }

    // test if buffer to read is null
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.InputStream.class}
    )
    */
    public void testNewChannelInputStream_BufferNull() throws IOException {
        ByteBuffer byteBuf = ByteBuffer.allocate(this.testNum);
        int readres = this.testNum;
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        assertNotNull(rbChannel);
        try {
            readres = rbChannel.read(null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(this.testNum, readres);
        readres = 0;
        try {
            readres = rbChannel.read(byteBuf);
        } catch (NullPointerException e) {
            fail();
        }
        assertEquals(this.testNum, readres);
    }

    /*
     * Test method for 'java.nio.channels.Channels.NewChannel'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.InputStream.class}
    )
     */
    public void testNewChannelInputStream() throws IOException {
        int bufSize = 10;
        int readres = 0;
        byte[] byteArray = new byte[bufSize];
        ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        readres = this.fins.read(byteArray);

        assertEquals(bufSize, readres);
        assertFalse(0 == this.fins.available());

        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        // fins still reads.
        assertFalse(0 == this.fins.available());
        readres = this.fins.read(byteArray);
        assertEquals(bufSize, readres);

        // rbChannel also reads.
        assertNotNull(rbChannel);
        readres = rbChannel.read(byteBuf);

        assertEquals(bufSize, readres);
        InputStream ins = Channels.newInputStream(rbChannel);
        assertNotNull(ins);
        assertEquals(0, ins.available());
    }

    // test if fout to change is null
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.OutputStream.class}
    )
     */
    public void testNewChannelOutputStream_inputNull() throws IOException {
        int writeres = this.testNum;
        ByteBuffer writebuf = ByteBuffer.allocate(this.writebufSize);
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf.putChar((char) (val + 64));
        }
        this.fouts = null;
        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);
        writeres = rbChannel.write(writebuf);
        assertEquals(0, writeres);

        writebuf.flip();
        try {
            writeres = rbChannel.write(writebuf);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
    }

    // test if write buf is null
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.OutputStream.class}
    )
     */
    public void testNewChannelOutputStream_BufNull() throws IOException {
        int writeres = this.testNum;
        ByteBuffer writebuf = null;
        try {
            this.fouts = new FileOutputStream(tmpFile);
        } catch (FileNotFoundException e) {
            fail();
        }

        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);
        try {
            writeres = rbChannel.write(writebuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(this.testNum, writeres);
    }

    /*
     * Test method for 'java.nio.channels.Channels.NewChannel(OutputStream)'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newChannel",
        args = {java.io.OutputStream.class}
    )
     */
    public void testNewChannelOutputStream() throws IOException {
        int writeNum = 0;
        ByteBuffer writebuf = ByteBuffer.allocateDirect(this.writebufSize);
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf.putChar((char) (val + 64));
        }
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel testChannel = this.fouts.getChannel();
        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);

        assertTrue(testChannel.isOpen());
        assertTrue(rbChannel.isOpen());

        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.fouts.flush();
        this.fins = new FileInputStream(tmpFile);
        assertEquals(this.fins.available(), 1);
        this.fins.close();

        writeNum = rbChannel.write(writebuf);
        // write success ,but output null
        assertEquals(0, writeNum);
        // close of fouts does not affect on channel
        this.fouts.close();
        writeNum = rbChannel.write(writebuf);
        assertEquals(0, writeNum);
        try {
            writeNum = testChannel.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
            // correct
        }
        assertEquals(0, writeNum);
        // close of rbchannel does affect on testchannel(same channel)
        rbChannel.close();
        try {
            writeNum = testChannel.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newInputStream",
        args = {java.nio.channels.ReadableByteChannel.class}
    )
    */
    public void testNewInputStreamReadableByteChannel_InputNull()
            throws Exception {
        byte[] readbuf = new byte[this.testNum];
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel readbc = this.fins.getChannel();
        assertEquals(this.fileSize, this.fins.available());
        assertTrue(readbc.isOpen());
        InputStream testins = Channels.newInputStream(null);
        assertNotNull(testins);

        try {
            testins.read(readbuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, testins.available());
        try {
            testins.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }

    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newInputStream",
        args = {java.nio.channels.ReadableByteChannel.class}
    )
    */
    public void testNewInputStreamReadableByteChannel() throws Exception {
        ByteBuffer readbcbuf = ByteBuffer.allocateDirect(this.testNum);
        byte[] readbuf = new byte[this.testNum];
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel readbc = this.fins.getChannel();
        assertEquals(this.fileSize, this.fins.available());
        assertTrue(readbc.isOpen());
        InputStream testins = Channels.newInputStream(readbc);
        // read in testins and fins use the same pointer
        testins.read(readbuf);
        assertEquals(this.fins.available(), this.fileSize - this.testNum);
        int readNum = readbc.read(readbcbuf);
        assertEquals(readNum, this.testNum);
        assertEquals(this.fins.available(), this.fileSize - this.testNum * 2);
        testins.read(readbuf);
        assertEquals(this.fins.available(), this.fileSize - this.testNum * 3);

        assertFalse(testins.markSupported());
        try {
            testins.mark(10);
        } catch (UnsupportedOperationException e) {
            // expected;
        }

        try {
            testins.reset();
        } catch (IOException e) {
            // expected;
        }

        // readbc.close() affect testins
        readbc.close();
        assertFalse(readbc.isOpen());
        try {
            testins.read(readbuf);
            fail();
        } catch (ClosedChannelException e) {
            // correct
        }

        // Read methods throw IllegalBlockingModeException if underlying channel
        // is in non-blocking mode.
        SocketChannel chan = SocketChannel.open();
        chan.configureBlocking(false);
        testins = Channels.newInputStream(chan);
        try {
            testins.read();
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            testins.read(new byte[1]);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            testins.read(new byte[1], 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newOutputStream",
        args = {java.nio.channels.WritableByteChannel.class}
    )
    */
    public void testNewOutputStreamWritableByteChannel_InputNull()
            throws Exception {
        byte[] writebuf = new byte[this.testNum];
        OutputStream testouts = Channels.newOutputStream(null);
        assertNotNull(testouts);
        try {
            testouts.write(writebuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        testouts.flush();
        try {
            testouts.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        WritableByteChannel writebc = Channels.newChannel((OutputStream) null);
        assertTrue(writebc.isOpen());
        OutputStream testoutputS = Channels.newOutputStream(writebc);
        try {
            testoutputS.write(writebuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newOutputStream",
        args = {java.nio.channels.WritableByteChannel.class}
    )
    */
    public void testNewOutputStreamWritableByteChannel() throws Exception {
        byte[] writebuf = new byte[this.testNum];
        ByteBuffer writebcbuf = ByteBuffer.allocateDirect(this.testNum);
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel writebc = this.fouts.getChannel();

        assertTrue(writebc.isOpen());
        OutputStream testouts = Channels.newOutputStream(writebc);

        // read in testins and fins use the same pointer
        testouts.write(writebuf);
        this.assertFileSizeSame(tmpFile, this.testNum);
        writebc.write(writebcbuf);
        this.assertFileSizeSame(tmpFile, this.testNum * 2);
        testouts.write(writebuf);
        this.assertFileSizeSame(tmpFile, this.testNum * 3);
        // readbc.close() affect testins
        writebc.close();
        assertFalse(writebc.isOpen());
        try {
            testouts.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
            // correct
        }

        // Write methods throw IllegalBlockingModeException if underlying 
        // channel is in non-blocking mode.
        SocketChannel chan = SocketChannel.open();
        chan.configureBlocking(false);
        testouts = Channels.newOutputStream(chan);
        try {
            testouts.write(10);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            testouts.write(new byte[1]);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            testouts.write(new byte[1], 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedCharsetException.",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.nio.charset.CharsetDecoder.class, int.class}
    )
    */
    public void testnewReaderCharsetError() throws Exception {
        this.fins = new FileInputStream(tmpFile);

        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        try {
            Channels.newReader(rbChannel, Charset.forName(BAD_CODE_SET)
                    .newDecoder(), //$NON-NLS-1$
                    -1);
            fail();
        } catch (UnsupportedCharsetException e) {
            // correct
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedCharsetException.",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.lang.String.class}
    )
    */
    public void testnewReaderCharsetError2() throws Exception {
        this.fins = new FileInputStream(tmpFile);

        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        try {
            Channels.newReader(rbChannel, BAD_CODE_SET);
            fail();
        } catch (UnsupportedCharsetException e) {
            // correct
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedCharsetException.",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.nio.charset.CharsetEncoder.class, int.class}
    )
    */
    public void testnewWriterCharsetError() throws Exception {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        try {
            Channels.newWriter(wbChannel, Charset.forName(BAD_CODE_SET)
                    .newEncoder(), -1);
            fail();
        } catch (UnsupportedCharsetException e) {
            // correct
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies UnsupportedCharsetException.",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.lang.String.class}
    )
    */
    public void testnewWriterCharsetError2() throws Exception {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        try {
            Channels.newWriter(wbChannel, BAD_CODE_SET);
            fail();
        } catch (UnsupportedCharsetException e) {
            // correct
        }
    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newReader(ReadableByteChannel, String)'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.nio.charset.CharsetDecoder.class, int.class}
    )
     */
    public void testNewReaderReadableByteChannelCharsetDecoderI_InputNull()
            throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        // channel null
        Reader testReader = Channels.newReader(null, Charset.forName(CODE_SET)
                .newDecoder(), -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }

        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        // channel with null inputs
        testReader = Channels.newReader(rbChannel, Charset.forName(CODE_SET)
                .newDecoder(), //$NON-NLS-1$
                -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newReader(ReadableByteChannel, String)'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.lang.String.class}
    )
     */
    public void testNewReaderReadableByteChannelString_InputNull()
            throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        // channel null
        Reader testReader = Channels.newReader(null, CODE_SET);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }

        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        // channel with null inputs
        testReader = Channels.newReader(rbChannel, CODE_SET);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newReader(ReadableByteChannel, String)'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.nio.charset.CharsetDecoder.class, int.class}
    )
     */
    public void testNewReaderReadableByteChannelCharsetDecoderI_internalBufferZero()
            throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        // channel null
        Reader testReader = Channels.newReader(null, Charset.forName(CODE_SET)
                .newDecoder(), //$NON-NLS-1$
                0);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        // channel with null inputs
        testReader = Channels.newReader(rbChannel, Charset.forName(CODE_SET)
                .newDecoder(), //$NON-NLS-1$
                -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newReader(ReadableByteChannel, String)'
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newReader",
        args = {java.nio.channels.ReadableByteChannel.class, java.lang.String.class}
    )
     */
    public void testNewReaderReadableByteChannelString_internalBufferZero()
            throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        // channel null
        Reader testReader = Channels.newReader(null, CODE_SET);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        // channel with null inputs
        testReader = Channels.newReader(rbChannel, CODE_SET);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newReader(ReadableByteChannel, String)'
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "newReader",
            args = {java.nio.channels.ReadableByteChannel.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "newReader",
            args = {java.nio.channels.ReadableByteChannel.class, java.nio.charset.CharsetDecoder.class, int.class}
        )
    })
     */
    public void testNewReaderReadableByteChannel() throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        Reader testReader = Channels.newReader(rbChannel, Charset.forName(
                CODE_SET).newDecoder(), //$NON-NLS-1$
                -1);
        Reader testReader_s = Channels.newReader(rbChannel, CODE_SET); //$NON-NLS-1$

        assertEquals(this.fileSize, this.fins.available());
        // not ready...
        assertFalse(testReader.ready());
        assertFalse(testReader_s.ready());
        // still reads
        readres = testReader.read(charBuf);
        assertEquals(bufSize, readres);
        assertEquals(0, this.fins.available());

        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
            // correct
        }

        readres = testReader_s.read(charBuf);
        assertEquals(0, readres);
        assertTrue(testReader.ready());
        assertFalse(testReader_s.ready());
    }

    /*
     * Zero-Buffer
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.nio.charset.CharsetEncoder.class, int.class}
    )
     */
    public void testNewWriterWritableByteChannelCharsetEncoderI_internalBufZero()
            throws IOException {

        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        // null channel
        Writer testWriter = Channels.newWriter(null, Charset.forName(CODE_SET)
                .newEncoder(), //$NON-NLS-1$
                -1);
        // can write to buffer
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }

        // channel with null input
        this.fouts = null;
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        testWriter = Channels.newWriter(wbChannel, Charset.forName(CODE_SET)
                .newEncoder(), //$NON-NLS-1$
                -1);
        // can write to buffer
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Zero-Buffer
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.lang.String.class}
    )
     */
    public void testNewWriterWritableByteChannelString_internalBufZero()
            throws IOException {

        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        // null channel
        Writer testWriter = Channels.newWriter(null, CODE_SET);
        // can write to buffer
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }

        // channel with null input
        this.fouts = null;
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        testWriter = Channels.newWriter(wbChannel, CODE_SET);
        // can write to buffer
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * this test cannot be passed when buffer set to 0!
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.nio.charset.CharsetEncoder.class, int.class}
    )
     */
    public void testNewWriterWritableByteChannelCharsetEncoderI_InputNull()
            throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        Writer testWriter = Channels.newWriter(wbChannel, Charset.forName(
                CODE_SET).newEncoder(), //$NON-NLS-1$
                1);

        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        // can write to buffer
        testWriter.write(writebuf);
        testWriter.flush();
        testWriter.close();

    }

    /*
     * this test cannot be passed when buffer set to 0!
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "newWriter",
        args = {java.nio.channels.WritableByteChannel.class, java.lang.String.class}
    )
     */
    public void testNewWriterWritableByteChannelString_InputNull()
            throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        Writer testWriter = Channels.newWriter(wbChannel, CODE_SET);

        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        // can write to buffer
        testWriter.write(writebuf);
        testWriter.flush();
        testWriter.close();

    }

    /*
     * Test method for
     * 'java.nio.channels.Channels.newWriter(WritableByteChannel, String)'
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "newWriter",
            args = {java.nio.channels.WritableByteChannel.class, java.nio.charset.CharsetEncoder.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "newWriter",
            args = {java.nio.channels.WritableByteChannel.class, java.lang.String.class}
        )
    })
     */
    public void testNewWriterWritableByteChannelString() throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        Writer testWriter = Channels.newWriter(wbChannel, CODE_SET); //$NON-NLS-1$
        Writer testWriter_s = Channels.newWriter(wbChannel, Charset.forName(
                CODE_SET).newEncoder(), //$NON-NLS-1$
                -1);

        String writebuf = ""; //$NON-NLS-1$
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.assertFileSizeSame(tmpFile, 1);

        // writer continues to write after '1',what the fouts write
        testWriter.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        // testwriter_s does not know if testwrite writes
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        // testwriter_s even does not know if himself writes?
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);

        // close the fouts, no longer writable for testWriter
        for (int val = 0; val < this.writebufSize; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        this.fouts.close();
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        
        SocketChannel chan = SocketChannel.open();
        chan.configureBlocking(false);
        Writer writer = Channels.newWriter(chan, CODE_SET);
        try {
            writer.write(10);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write(new char[10]);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write("test");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write(new char[10], 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write("test", 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        
        writer = Channels.newWriter(chan, Charset.forName(
                CODE_SET).newEncoder(), //$NON-NLS-1$
                -1);
        try {
            writer.write(10);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write(new char[10]);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write("test");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write(new char[10], 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        try {
            writer.write("test", 0, 1);
        } catch (IllegalBlockingModeException e) {
            // expected
        }
    }
    
    /**
     * @tests java.nio.channels.Channels#newReader(ReadableByteChannel channel,
     *        String charsetName)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalBlockingModeException.",
        method = "newInputStream",
        args = {java.nio.channels.ReadableByteChannel.class}
    )
     */
    public void test_newInputStream_LReadableByteChannel()
            throws IOException {
        InetSocketAddress localAddr = new InetSocketAddress("127.0.0.1",
                Support_PortManager.getNextPort());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr);

        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr);
        sc.configureBlocking(false);
        assertFalse(sc.isBlocking());

        ssc.accept().close();
        ssc.close();
        assertFalse(sc.isBlocking());

        Reader reader = Channels.newReader(sc, "UTF16");
        int i = reader.read();
        assertEquals(-1, i);

        try {
            Channels.newInputStream(sc).read();
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }

        sc.close();
    }
}
