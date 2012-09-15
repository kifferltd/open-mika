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

package org.apache.harmony.nio.tests.java.nio.channels.spi;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargetNew;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

import junit.framework.TestCase;

import org.apache.harmony.nio.tests.java.nio.channels.spi.AbstractSelectorTest.MockSelectorProvider;

/**
 * Tests for AbstractSelectableChannel 
 */
//@TestTargetClass(AbstractSelectableChannel.class)
public class AbstractSelectableChannelTest extends TestCase {

    private MockSelectableChannel testChannel;

    protected void setUp() throws Exception {
        super.setUp();
        testChannel = new MockSelectableChannel(SelectorProvider.provider());
    }

    protected void tearDown() throws Exception {
        if (testChannel.isOpen()) {
            testChannel.close();
        }
    }
    
    /**
     * @tests AbstractSelectableChannel()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AbstractSelectableChannel",
        args = {SelectorProvider.class}
    )
     */
    public void test_Constructor_LSelectorProvider() throws Exception {
        assertSame(SelectorProvider.provider(), testChannel.provider());
    }

    /**
     * @tests AbstractSelectableChannel#implCloseChannel()
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseChannel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implCloseSelectableChannel",
            args = {}
        )
    })
     */
    public void test_implClose() throws IOException {
        testChannel.isImplCloseSelectableChannelCalled = false;
        testChannel.implCloseSelectableChannelCount = 0;
        testChannel.close();
        assertFalse(testChannel.isOpen());
        assertTrue(testChannel.isImplCloseSelectableChannelCalled);
        assertEquals(1, testChannel.implCloseSelectableChannelCount);

        testChannel = new MockSelectableChannel(SelectorProvider.provider());
        testChannel.isImplCloseSelectableChannelCalled = false;
        testChannel.implCloseSelectableChannelCount = 0;
        // close twice.
        // make sure implCloseSelectableChannelCount is called only once.
        testChannel.close();
        testChannel.close();
        assertFalse(testChannel.isOpen());
        assertTrue(testChannel.isImplCloseSelectableChannelCalled);
        assertEquals(1, testChannel.implCloseSelectableChannelCount);
    }

    /**
     * @tests AbstractSelectableChannel#provider()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "provider",
        args = {}
    )
     */
    public void test_provider() {
        SelectorProvider provider = testChannel.provider();
        assertSame(SelectorProvider.provider(), provider);
        testChannel = new MockSelectableChannel(null);
        provider = testChannel.provider();
        assertNull(provider);
    }

    /**
     * @tests AbstractSelectableChannel#isBlocking()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "isBlocking",
        args = {}
    )
     */
    public void test_isBlocking() throws IOException {
        assertTrue(testChannel.isBlocking());
        testChannel.configureBlocking(false);
        assertFalse(testChannel.isBlocking());
        testChannel.configureBlocking(true);
        assertTrue(testChannel.isBlocking());
    }

    /**
     * 
     * @tests AbstractSelectableChannel#blockingLock()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "blockingLock",
        args = {}
    )
     */
    public void test_blockingLock() {
        Object gotObj = testChannel.blockingLock();
        assertNotNull(gotObj);
    }

    /**
     * @tests AbstractSelectableChannel#register(Selector, int, Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "register",
        args = {java.nio.channels.Selector.class, int.class, java.lang.Object.class}
    )
     */
    public void test_register_LSelectorILObject() throws IOException {
        assertFalse(testChannel.isRegistered());
        Selector acceptSelector1 = SelectorProvider.provider().openSelector();
        Selector acceptSelector2 = new MockAbstractSelector(SelectorProvider
                .provider());
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey acceptKey = sc.register(acceptSelector1,
                SelectionKey.OP_READ, null);
        assertNotNull(acceptKey);
        assertTrue(acceptKey.isValid());
        assertSame(sc, acceptKey.channel());

        //test that sc.register invokes Selector.register()
        sc.register(acceptSelector2, SelectionKey.OP_READ, null);
        assertTrue(((MockAbstractSelector)acceptSelector2).isRegisterCalled);
    }

    /**
     * @tests AbstractSelectableChannel#register(Selector, int, Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "register",
        args = {java.nio.channels.Selector.class, int.class, java.lang.Object.class}
    )
     */
    public void test_register_LSelectorILObject_IllegalArgument()
            throws IOException {
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        assertTrue(acceptSelector.isOpen());
        MockSelectableChannel msc = new MockSelectableChannel(SelectorProvider
                .provider());
        msc.configureBlocking(false);
        // in nonblocking mode
        try {
            //different SelectionKey with validOps
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // in nonblocking mode, if selector closed
        acceptSelector.close();
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            msc.register(acceptSelector, 0, null);
            fail("Should throw IllegalSelectorException");
        } catch (IllegalSelectorException e) {
            // expected
        }

        acceptSelector = SelectorProvider.provider().openSelector();
        // test in blocking mode
        msc.configureBlocking(true);
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        acceptSelector.close();
        // in blocking mode, if selector closed
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            msc.register(null, 0, null);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }

        // register with an object
        Object argObj = new Object();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        try {
            sc.register(null, SelectionKey.OP_READ, argObj);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        // if channel closed
        msc.close();
        try {
            msc.register(acceptSelector, SelectionKey.OP_READ, null);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }

        SelectorProvider prov1 = MockSelectorProvider.provider();
        SelectorProvider prov2 = MockSelectorProvider.provider();
        
        Selector sel = prov2.openSelector();
        
        sc = prov1.openSocketChannel();
        sc.configureBlocking(false);
        try {
            sc.register(sel, SelectionKey.OP_READ, null);
        } catch (IllegalSelectorException e) {
            // expected
        }
    }

    /**
     * @tests AbstractSelectableChannel#keyFor(Selector)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keyFor",
        args = {java.nio.channels.Selector.class}
    )
     */
    public void test_keyfor_LSelector() throws Exception {
        SocketChannel sc = SocketChannel.open();
        Object argObj = new Object();
        sc.configureBlocking(false);
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        Selector acceptSelectorOther = SelectorProvider.provider()
                .openSelector();
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, argObj);
        assertEquals(sc.keyFor(acceptSelector), acceptKey);
        SelectionKey acceptKeyObjNull = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertSame(sc.keyFor(acceptSelector), acceptKeyObjNull);
        assertSame(acceptKeyObjNull, acceptKey);
        SelectionKey acceptKeyOther = sc.register(acceptSelectorOther,
                SelectionKey.OP_READ, null);
        assertSame(sc.keyFor(acceptSelectorOther), acceptKeyOther);
    }

    /**
     * @tests AbstractSelectableChannel#configureBlocking(boolean)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "configureBlocking",
        args = {boolean.class}
    )
     */
    public void test_configureBlocking_Z_IllegalBlockingMode() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        Selector acceptSelector = SelectorProvider.provider().openSelector();
        SelectionKey acceptKey = sc.register(acceptSelector,
                SelectionKey.OP_READ, null);
        assertEquals(sc.keyFor(acceptSelector), acceptKey);
        SelectableChannel getChannel = sc.configureBlocking(false);
        assertEquals(getChannel, sc);
        try {
            sc.configureBlocking(true);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
    }

    /**
     * @tests AbstractSelectableChannel#configureBlocking(boolean)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "configureBlocking",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "implConfigureBlocking",
            args = {boolean.class}
        )
    })   
     */
    public void test_configureBlocking_Z() throws Exception {
        testChannel = new MockSelectableChannel(SelectorProvider
                .provider());
        // default blocking mode is true. The implConfigureBlocking is only
        // invoked if the given mode is different with current one.
        testChannel.configureBlocking(true);
        assertFalse(testChannel.implConfigureBlockingCalled);
        testChannel.configureBlocking(false);
        assertTrue(testChannel.implConfigureBlockingCalled);
        
        AbstractSelectableChannel channel = 
                SelectorProvider.provider().openDatagramChannel();
        channel.configureBlocking(false);
        channel.register(SelectorProvider.provider().openSelector(),
                SelectionKey.OP_READ);
        try {
            channel.configureBlocking(true);
            fail("Should have thrown IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // expected
        }
        
        testChannel.close();
        try {
            testChannel.configureBlocking(false);
            fail("Should have thrown ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    private class MockSelectableChannel extends AbstractSelectableChannel {

        private boolean isImplCloseSelectableChannelCalled = false;

        private int implCloseSelectableChannelCount = 0;

        private boolean implConfigureBlockingCalled = false;

        public MockSelectableChannel(SelectorProvider arg0) {
            super(arg0);
        }

        protected void implCloseSelectableChannel() throws IOException {
            isImplCloseSelectableChannelCalled = true;
            ++implCloseSelectableChannelCount;
        }

        protected void implConfigureBlocking(boolean arg0) throws IOException {
            implConfigureBlockingCalled = true;
        }

        public int validOps() {
            return SelectionKey.OP_ACCEPT;
        }

    }
}
