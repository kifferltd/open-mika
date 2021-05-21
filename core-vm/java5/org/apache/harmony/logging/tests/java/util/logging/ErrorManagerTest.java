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

import junit.framework.TestCase;

import org.apache.harmony.logging.tests.java.util.logging.HandlerTest.NullOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.ErrorManager;

//@TestTargetClass(ErrorManager.class)
public class ErrorManagerTest extends TestCase {
    
    
    private final PrintStream err = System.err;
    private final PrintStream out = System.out;

    private OutputStream errSubstituteStream = null; 
    
    public void setUp() throws Exception{
        super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));           
    }
    
    public void tearDown() throws Exception{
        System.setErr(err);
        System.setOut(out);
        super.tearDown();
    }
    
    
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case only with ErrorManager.GENERIC_FAILURE, impove MockStream",
        method = "error",
        args = {java.lang.String.class, java.lang.Exception.class, int.class}
    )
    */
    public void test_errorCheck() {
        ErrorManager em = new ErrorManager();
        MockStream aos = new MockStream();
        PrintStream st = new PrintStream(aos);
        System.setErr(st);
        System.setOut(st);
        em.error("supertest", null, ErrorManager.GENERIC_FAILURE);
        st.flush();

       assertTrue("message appears (supertest)", aos.getWrittenData().indexOf("supertest") != -1);
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case only with ErrorManager.GENERIC_FAILURE",
        method = "error",
        args = {java.lang.String.class, java.lang.Exception.class, int.class}
    )
    */
    public void test_errorStringStringint() {
        ErrorManager em = new ErrorManager();
        em.error(null, new NullPointerException(),
                        ErrorManager.GENERIC_FAILURE);
        em.error("An error message.", null, ErrorManager.GENERIC_FAILURE);
        em.error(null, null, ErrorManager.GENERIC_FAILURE);
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ErrorManager",
        args = {}
    )
    */
    public void test_constants() {
        assertEquals(3, ErrorManager.CLOSE_FAILURE);
        assertEquals(2, ErrorManager.FLUSH_FAILURE);
        assertEquals(5, ErrorManager.FORMAT_FAILURE);
        assertEquals(0, ErrorManager.GENERIC_FAILURE);
        assertEquals(4, ErrorManager.OPEN_FAILURE);
        assertEquals(1, ErrorManager.WRITE_FAILURE);
    }
    
    public class MockStream extends ByteArrayOutputStream {

        private StringBuffer linesWritten = new StringBuffer();

        public void flush() {}
        public  void close() {}
        
        @Override
        public void write(byte[] buffer) {
            linesWritten.append(new String(buffer));
        }
        
        @Override
        public synchronized void write(byte[] buffer, int offset, int len) {
            linesWritten.append(new String(buffer, offset, len));
        }
                
        public String getWrittenData() {return linesWritten.toString();}

    }


}
