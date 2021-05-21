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

package org.apache.harmony.logging.tests.java.util.logging;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.security.Permission;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.LoggingPermission;
import java.util.logging.MemoryHandler;
import java.util.logging.SimpleFormatter;

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.HandlerTest.NullOutputStream;
import org.apache.harmony.logging.tests.java.util.logging.util.EnvironmentHelper;

/**
 * 
 */
//@TestTargetClass(MemoryHandler.class)
public class MemoryHandlerTest extends TestCase {

    final static LogManager manager = LogManager.getLogManager();

    final static Properties props = new Properties();

    final static String baseClassName = MemoryHandlerTest.class.getName();

    final static StringWriter writer = new StringWriter();

    final static SecurityManager securityManager = new MockSecurityManager();
    
    private final PrintStream err = System.err;

    private OutputStream errSubstituteStream = null;    

    MemoryHandler handler;

    Handler target = new MockHandler();

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        manager.reset();
        initProps();
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));        
    }

    /**
     * 
     */
    private void initProps() {
        props.put("java.util.logging.MemoryHandler.level", "FINE");
        props.put("java.util.logging.MemoryHandler.filter", baseClassName
                + "$MockFilter");
        props.put("java.util.logging.MemoryHandler.size", "2");
        props.put("java.util.logging.MemoryHandler.push", "WARNING");
        props.put("java.util.logging.MemoryHandler.target", baseClassName
                + "$MockHandler");
        props.put("java.util.logging.MemoryHandler.formatter", baseClassName
                + "$MockFormatter");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        manager.readConfiguration();
        props.clear();
        System.setErr(err);        
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "setPushLevel",
            args = {java.util.logging.Level.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "flush",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "push",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "getPushLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "isLoggable",
            args = {java.util.logging.LogRecord.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "SecurityException",
            method = "publish",
            args = {java.util.logging.LogRecord.class}
        )
    })
     */
    public void testGlobalSecurity() {
        SecurityManager currentManager = System.getSecurityManager();
        System.setSecurityManager(securityManager);
        try {
            try {
                handler.close();
                fail("should throw security exception");
            } catch (SecurityException e) {
            }
            try {
                handler.setPushLevel(Level.CONFIG);
                fail("should throw security exception");
            } catch (SecurityException e) {
            }
            handler.flush();
            handler.push();
            handler.getPushLevel();
            handler.isLoggable(new LogRecord(Level.ALL, "message"));
            handler.publish(new LogRecord(Level.ALL, "message"));
        } finally {
            System.setSecurityManager(currentManager);
        }

    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
     */
    public void testClose() {
        Filter filter = handler.getFilter();
        Formatter formatter = handler.getFormatter();
        writer.getBuffer().setLength(0);
        handler.close();
        assertEquals(writer.toString(), "close");
        assertEquals(handler.getFilter(), filter);
        assertEquals(handler.getFormatter(), formatter);
        assertNull(handler.getEncoding());
        assertNotNull(handler.getErrorManager());
        assertEquals(handler.getLevel(), Level.OFF);
        assertEquals(handler.getPushLevel(), Level.WARNING);
        assertFalse(handler.isLoggable(new LogRecord(Level.SEVERE, "test")));
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "flush",
        args = {}
    )
     */
    public void testFlush() {
        Filter filter = handler.getFilter();
        Formatter formatter = handler.getFormatter();
        writer.getBuffer().setLength(0);
        handler.flush();
        assertEquals(writer.toString(), "flush");
        assertEquals(handler.getFilter(), filter);
        assertEquals(handler.getFormatter(), formatter);
        assertNull(handler.getEncoding());
        assertNotNull(handler.getErrorManager());
        assertEquals(handler.getLevel(), Level.FINE);
        assertEquals(handler.getPushLevel(), Level.WARNING);
        assertTrue(handler.isLoggable(new LogRecord(Level.SEVERE, "test")));
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
     */
    public void testIsLoggable() {
        try {
            handler.isLoggable(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        LogRecord record = new LogRecord(Level.FINER, "MSG1");
        assertFalse(handler.isLoggable(record));


        record = new LogRecord(Level.FINE, "MSG2");
        assertTrue(handler.isLoggable(record));
        assertTrue(handler.isLoggable(new LogRecord(Level.INFO, "1")));
        assertTrue(handler.isLoggable(new LogRecord(Level.WARNING, "2")));
        assertTrue(handler.isLoggable(new LogRecord(Level.SEVERE, "3")));
        
        record = new LogRecord(Level.CONFIG, "MSG3");
        assertTrue(handler.isLoggable(record));

        record = new LogRecord(Level.CONFIG, "false");
        assertFalse(handler.isLoggable(record));

        handler.setFilter(null);
        record = new LogRecord(Level.CONFIG, "false");
        assertTrue(handler.isLoggable(record));
    }

    /*
     * Class under test for void MemoryHandler()
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "check errors",
        method = "MemoryHandler",
        args = {}
    )
     */
    public void testMemoryHandler() throws IOException {
        assertNotNull("Filter should not be null", handler.getFilter());
        assertNotNull("Formatter should not be null", handler.getFormatter());
        assertNull("character encoding should be null", handler.getEncoding());
        assertNotNull("ErrorManager should not be null", handler
                .getErrorManager());
        assertEquals("Level should be FINE", Level.FINE, handler.getLevel());
        assertEquals("Level should be WARNING", Level.WARNING, handler
                .getPushLevel());

        props.clear();
        props.put("java.util.logging.MemoryHandler.target", baseClassName
                + "$MockHandler");

        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertNull(handler.getFilter());
        assertTrue(handler.getFormatter() instanceof SimpleFormatter);
        assertNull(handler.getEncoding());
        assertNotNull(handler.getErrorManager());
        assertEquals(handler.getLevel(), Level.ALL);
        assertEquals(handler.getPushLevel(), Level.SEVERE);
 
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MemoryHandler",
        args = {}
    )
     */
    public void testMemoryHandlerInvalidProps() throws IOException {
        // null target
        try {
            props.remove("java.util.logging.MemoryHandler.target");
            manager.readConfiguration(EnvironmentHelper
                    .PropertiesToInputStream(props));
            handler = new MemoryHandler();
            fail("should throw RuntimeException: target must be set");
        } catch (RuntimeException e) {
        }

        // invalid target
        try {
            props.put("java.util.logging.MemoryHandler.target", "badname");
            manager.readConfiguration(EnvironmentHelper
                    .PropertiesToInputStream(props));
            handler = new MemoryHandler();
            fail("should throw RuntimeException: target must be valid");
        } catch (RuntimeException e) {
        }

        // invalid formatter
        initProps();
        props.put("java.util.logging.MemoryHandler.formatter", "badname");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertTrue(handler.getFormatter() instanceof SimpleFormatter);

        // invalid level
        initProps();
        props.put("java.util.logging.MemoryHandler.level", "badname");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertEquals(handler.getLevel(), Level.ALL);

        // invalid pushlevel
        initProps();
        props.put("java.util.logging.MemoryHandler.push", "badname");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertEquals(handler.getPushLevel(), Level.SEVERE);

        // invalid filter
        initProps();
        props.put("java.util.logging.MemoryHandler.filter", "badname");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertNull(handler.getFilter());

        // invalid size
        initProps();
        props.put("java.util.logging.MemoryHandler.size", "-1");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        initProps();
        props.put("java.util.logging.MemoryHandler.size", "badsize");
        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();

    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MemoryHandler",
        args = {}
    )
     */
    public void testMemoryHandlerDefaultValue() throws SecurityException,
            IOException {
        props.clear();
        props.put("java.util.logging.MemoryHandler.target", baseClassName
                + "$MockHandler");

        manager.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        handler = new MemoryHandler();
        assertNull(handler.getFilter());
        assertTrue(handler.getFormatter() instanceof SimpleFormatter);
        assertNull(handler.getEncoding());
        assertNotNull(handler.getErrorManager());
        assertEquals(handler.getLevel(), Level.ALL);
        assertEquals(handler.getPushLevel(), Level.SEVERE);
    }

    /*
     * Class under test for void MemoryHandler(Handler, int, Level)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MemoryHandler",
        args = {java.util.logging.Handler.class, int.class, java.util.logging.Level.class}
    )
     */
    public void testMemoryHandlerHandlerintLevel() {
        handler = new MemoryHandler(target, 2, Level.FINEST);
        assertNotNull("Filter should not be null", handler.getFilter());
        assertNotNull("Formatter should not be null", handler.getFormatter());
        assertNull("character encoding should be null", handler.getEncoding());
        assertNotNull("ErrorManager should not be null", handler
                .getErrorManager());
        assertEquals("Level should be FINE", Level.FINE, handler.getLevel());
        assertEquals("Level should be FINEST", Level.FINEST, handler
                .getPushLevel());

        try {
            new MemoryHandler(null, 2, Level.FINEST);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new MemoryHandler(target, 2, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new MemoryHandler(target, 0, Level.FINEST);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new MemoryHandler(target, -1, Level.FINEST);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPushLevel",
        args = {}
    )
     */
    public void testGetPushLevel() {
        try {
            handler.setPushLevel(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        handler.setPushLevel(Level.parse("123"));
        assertEquals(handler.getPushLevel(), Level.parse("123"));
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPushLevel",
        args = {java.util.logging.Level.class}
    )
     */
    public void testSetPushLevel() {
        // change push level don't trigger push action
        writer.getBuffer().setLength(0);
        LogRecord lr = new LogRecord(Level.CONFIG, "lr");
        assertTrue(handler.isLoggable(lr));
        handler.publish(lr);
        assertEquals(writer.toString(), "");
        writer.getBuffer().setLength(0);
        handler.setPushLevel(Level.FINE);
        assertEquals(writer.toString(), "");
        handler.publish(lr);
        assertEquals(writer.toString(), lr.getMessage() + lr.getMessage());
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "push",
        args = {}
    )
     */
    public void testPushPublic() {
        writer.getBuffer().setLength(0);
        // loggable but don't trig push
        handler.publish(new LogRecord(Level.CONFIG, "MSG1"));
        assertEquals("", writer.toString());
        // trig push
        handler.publish(new LogRecord(Level.SEVERE, "MSG2"));
        assertEquals(writer.toString(), "MSG1MSG2");
        writer.getBuffer().setLength(0);
        
        // regression test for Harmony-1292
        handler.publish(new LogRecord(Level.WARNING, "MSG"));
        assertEquals("MSG",writer.toString());
        
        writer.getBuffer().setLength(0);
        // push nothing
        handler.push();
        assertEquals("", writer.toString());
        // loggable but not push
        handler.publish(new LogRecord(Level.CONFIG, "MSG3"));
        assertEquals("", writer.toString());
        // not loggable
        handler.publish(new LogRecord(Level.FINEST, "MSG4"));
        assertEquals("", writer.toString());
        // loggable but not push
        handler.publish(new LogRecord(Level.CONFIG, "MSG5"));
        assertEquals("", writer.toString());
        // not loggable
        handler.publish(new LogRecord(Level.FINER, "MSG6"));
        assertEquals("", writer.toString());
        // not loggable
        handler.publish(new LogRecord(Level.FINER, "false"));
        assertEquals("", writer.toString());
        // loggable but not push
        handler.publish(new LogRecord(Level.CONFIG, "MSG8"));
        assertEquals("", writer.toString());
        // push all
        handler.push();
        assertEquals(writer.toString(), "MSG5MSG8");
        writer.getBuffer().setLength(0);
        handler.push();
        assertEquals("", writer.toString());
    }

    /*
     * mock classes
     */
    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            return !record.getMessage().equals("false");
        }
    }

    public static class MockHandler extends Handler {
        public void close() {
            writer.write("close");
        }

        public void flush() {
            writer.write("flush");
        }

        public void publish(LogRecord record) {
            writer.write(record.getMessage());
        }

    }

    public static class MockFormatter extends Formatter {
        public String format(LogRecord r) {
            return r.getMessage();
        }
    }

    public static class MockSecurityManager extends SecurityManager {
        public void checkPermission(Permission perm) {
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
            return;
        }
    }

}
