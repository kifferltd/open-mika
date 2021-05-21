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

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;
import java.util.zip.GZIPOutputStream;

//@TestTargetClass(GZIPOutputStream.class)
public class GZIPOutputStreamTest extends junit.framework.TestCase {

    class TestGZIPOutputStream extends GZIPOutputStream {
        TestGZIPOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        TestGZIPOutputStream(OutputStream out, int size) throws IOException {
            super(out, size);
        }

        Checksum getChecksum() {
            return crc;
        }
    }

    /**
     * @tests java.util.zip.GZIPOutputStream#GZIPOutputStream(java.io.OutputStream)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPOutputStream",
        args = {java.io.OutputStream.class}
    )
     */
    public void test_ConstructorLjava_io_OutputStream() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0,
                    outGZIP.getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * @tests java.util.zip.GZIPOutputStream#GZIPOutputStream(java.io.OutputStream,
     *        int)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPOutputStream",
        args = {java.io.OutputStream.class, int.class}
    )
     */
    public void test_ConstructorLjava_io_OutputStreamI() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile,
                    100);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0,
                    outGZIP.getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * @tests java.util.zip.GZIPOutputStream#finish()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "finish",
        args = {}
    )
     */
    public void test_finish() {
        // test method java.util.zip.GZIPOutputStream.finish()
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        TestGZIPOutputStream outGZIP = null;
        FileOutputStream outFile = null;
        try {
            outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            outGZIP = new TestGZIPOutputStream(outFile);

            outGZIP.finish();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }

            assertEquals(
                    "GZIP instance can still be used after finish is called",
                    1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
        try {
            outFile = new FileOutputStream("GZIPOutFinish.txt");
            outGZIP = new TestGZIPOutputStream(outFile);
            outFile.close();

            outGZIP.finish();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    /**
     * @tests java.util.zip.GZIPOutputStream#close()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException checking missed.",
        method = "close",
        args = {}
    )
     */
    public void test_close() {
        // test method java.util.zip.GZIPOutputStream.close()
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.close();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals(
                    "GZIP instance can still be used after close is called", 1,
                    r);
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }

    /**
     * @tests java.util.zip.GZIPOutputStream#write(byte[], int, int)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
     */
    public void test_write$BII() {
        // test method java.util.zip.GZIPOutputStream.writeBII
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        TestGZIPOutputStream outGZIP = null;
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.write(byteArray, 0, 10);
            // ran JDK and found this CRC32 value is 3097700292
            // System.out.print(outGZIP.getChecksum().getValue());
            assertEquals(
                    "the checksum value was incorrect result of write from GZIP",
                    3097700292L, outGZIP.getChecksum().getValue());

            // test for boundary check
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 11);
            } catch (IndexOutOfBoundsException ee) {
                r = 1;
            }
            assertEquals("out of bounds exception is not present", 1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
        try {
            outGZIP.write(byteArray, 0, 10);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Override
    protected void setUp() {
    }

    @Override
    protected void tearDown() {

        try {
            File dFile = new File("GZIPOutCon.txt");
            dFile.delete();
            File dFile2 = new File("GZIPOutFinish.txt");
            dFile2.delete();
            File dFile3 = new File("GZIPOutWrite.txt");
            dFile3.delete();
            File dFile4 = new File("GZIPOutClose2.txt");
            dFile4.delete();
        } catch (SecurityException e) {
            fail("Cannot delete file for security reasons");
        }
    }

}
