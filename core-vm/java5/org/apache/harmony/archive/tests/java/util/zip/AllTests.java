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

package org.apache.harmony.archive.tests.java.util.zip;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for java.util.zip package.
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = tests.TestSuiteFactory.createTestSuite(
                "Suite org.apache.harmony.archive.tests.java.util.zip");
        // $JUnit-BEGIN$
        suite.addTestSuite(Adler32Test.class);
        suite.addTestSuite(CheckedInputStreamTest.class);
        suite.addTestSuite(CheckedOutputStreamTest.class);
        suite.addTestSuite(CRC32Test.class);
        suite.addTestSuite(DataFormatExceptionTest.class);
        suite.addTestSuite(DeflaterOutputStreamTest.class);
        suite.addTestSuite(DeflaterTest.class);
        suite.addTestSuite(GZIPInputStreamTest.class);
        suite.addTestSuite(GZIPOutputStreamTest.class);
        suite.addTestSuite(InflaterInputStreamTest.class);
        suite.addTestSuite(InflaterTest.class);
        suite.addTestSuite(ZipEntryTest.class);
        suite.addTestSuite(ZipExceptionTest.class);
        suite.addTestSuite(ZipFileTest.class);
        suite.addTestSuite(ZipInputStreamTest.class);
        suite.addTestSuite(ZipOutputStreamTest.class);
        // $JUnit-END$
        return suite;
    }
}
