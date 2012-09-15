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
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetClass;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import junit.framework.TestCase;
import tests.support.Support_PortManager;

/*
 * Tests for SelectionKey and its default implementation
@TestTargetClass(
    value = SelectionKey.class,
    untestedMethods = {
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "empty protected constructor",
            method = "SelectionKey",
            args = {}
        )
    }
)
 */
public class SelectionKeyTest extends TestCase {

    Selector selector;

    SocketChannel sc;

    SelectionKey selectionKey;

    private static String LOCAL_ADDR = "127.0.0.1";

    protected void setUp() throws Exception {
        super.setUp();
        selector = Selector.open();
        sc = SocketChannel.open();
        sc.configureBlocking(false);
        selectionKey = sc.register(selector, SelectionKey.OP_CONNECT);
    }

    protected void tearDown() throws Exception {
        selectionKey.cancel();
        selectionKey = null;
        selector.close();
        selector = null;
        super.tearDown();
    }

    static class MockSelectionKey extends SelectionKey {
        private int interestOps;

        MockSelectionKey(int ops) {
            interestOps = ops;
        }

        public void cancel() {
            // do nothing
        }

        public SelectableChannel channel() {
            return null;
        }

        public int interestOps() {
            return 0;
        }

        public SelectionKey interestOps(int operations) {
            return null;
        }

        public boolean isValid() {
            return true;
        }

        public int readyOps() {
            return interestOps;
        }

        public Selector selector() {
            return null;
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#attach(Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "attach",
        args = {java.lang.Object.class}
    )
     */
    public void test_attach() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        // no previous, return null
        Object o = new Object();
        Object check = mockSelectionKey.attach(o);
        assertNull(check);

        // null parameter is ok
        check = mockSelectionKey.attach(null);
        assertSame(o, check);

        check = mockSelectionKey.attach(o);
        assertNull(check);
    }

    /**
     * @tests java.nio.channels.SelectionKey#attachment()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "attachment",
        args = {}
    )
     */
    public void test_attachment() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertNull(mockSelectionKey.attachment());
        Object o = new Object();
        mockSelectionKey.attach(o);
        assertSame(o, mockSelectionKey.attachment());
    }

    /**
     * @tests java.nio.channels.SelectionKey#channel()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "channel",
        args = {}
    )
     */
    public void test_channel() {
        assertSame(sc, selectionKey.channel());
        // can be invoked even canceled
        selectionKey.cancel();
        assertSame(sc, selectionKey.channel());
    }

    /**
     * @tests java.nio.channels.SelectionKey#interestOps()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "interestOps",
        args = {}
    )
     */
    public void test_interestOps() {
        assertEquals(SelectionKey.OP_CONNECT, selectionKey.interestOps());
    }

    /**
     * @tests java.nio.channels.SelectionKey#interestOps(int)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Doesn't verify CancelledKeyException.",
        method = "interestOps",
        args = {int.class}
    )
     */
    public void test_interestOpsI() {
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        assertEquals(SelectionKey.OP_WRITE, selectionKey.interestOps());

        try {
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }

        try {
            selectionKey.interestOps(~sc.validOps());
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }
        try {
            selectionKey.interestOps(-1);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }

        selectionKey.cancel();
        try {
            selectionKey.interestOps(-1);
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )
     */
    public void test_isValid() {
        assertTrue(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
     */
    public void test_isValid_KeyCancelled() {
        selectionKey.cancel();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
     */
    public void test_isValid_ChannelColsed() throws IOException {
        sc.close();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isValid",
        args = {}
    )    
     */
    public void test_isValid_SelectorClosed() throws IOException {
        selector.close();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isAcceptable()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isAcceptable",
        args = {}
    )
     */
    public void test_isAcceptable() throws IOException {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertTrue(mockSelectionKey1.isAcceptable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertFalse(mockSelectionKey2.isAcceptable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isConnectable()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isConnectable",
        args = {}
    )
     */
    public void test_isConnectable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertTrue(mockSelectionKey1.isConnectable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isConnectable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isReadable()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isReadable",
        args = {}
    )
     */
    public void test_isReadable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_READ);
        assertTrue(mockSelectionKey1.isReadable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isReadable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isWritable()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isWritable",
        args = {}
    )
     */
    public void test_isWritable() throws Exception {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_WRITE);
        assertTrue(mockSelectionKey1.isWritable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isWritable());

        Selector selector = SelectorProvider.provider().openSelector();
        
        Pipe pipe = SelectorProvider.provider().openPipe();
        pipe.open();
        pipe.sink().configureBlocking(false);
        SelectionKey key = pipe.sink().register(selector, SelectionKey.OP_WRITE);
        
        key.cancel();
        try {
            key.isWritable();
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#cancel()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "cancel",
        args = {}
    )
     */
    public void test_cancel() {
        selectionKey.cancel();
        try {
            selectionKey.isAcceptable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isConnectable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isReadable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isWritable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.readyOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.interestOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#readyOps()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readyOps",
        args = {}
    )
     */
    public void test_readyOps() throws IOException {
        int port = Support_PortManager.getNextPort();
        ServerSocket ss = new ServerSocket(port);
        try {
            sc.connect(new InetSocketAddress(LOCAL_ADDR, port));
            assertEquals(0, selectionKey.readyOps());
            assertFalse(selectionKey.isConnectable());
            selector.select();
            assertEquals(SelectionKey.OP_CONNECT, selectionKey.readyOps());
        } finally {
            ss.close();
            ss = null;
        }

        selectionKey.cancel();
        try {
            selectionKey.readyOps();
            fail("should throw IAE.");
        } catch (CancelledKeyException ex) {
            // expected;
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#selector()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "selector",
        args = {}
    )
     */
    public void test_selector() {
        assertSame(selector, selectionKey.selector());
        selectionKey.cancel();
        assertSame(selector, selectionKey.selector());
    }
}
