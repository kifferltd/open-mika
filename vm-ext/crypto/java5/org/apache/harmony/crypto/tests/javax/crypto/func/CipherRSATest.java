/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.crypto.tests.javax.crypto.func;

//import dalvik.annotation.AndroidOnly;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

// import targets.Cipher;

//@TestTargetClass(Cipher.RSA.class)
public class CipherRSATest extends TestCase {
// 3 cases checked
/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
*/
    public void test_RSAShortKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {512},
                new String[] {"ECB"}, new String[] {
                        "PKCS1PADDING", "OAEPWITHMD5ANDMGF1PADDING",
                        "OAEPWITHSHA1ANDMGF1PADDING"});

        rsa.launcher();

        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }

    // Not supported by Android
    public void disabled_test_RSALongKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {1024},
                new String[] {"ECB"},
                new String[] {"OAEPWITHSHA-384ANDMGF1PADDING",
                "OAEPWITHSHA-256ANDMGF1PADDING"});

        rsa.launcher();

        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }

    // Not supported by Android
   public void disabled_test_RSAXXXLKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {2048},
                new String[] {"ECB"},
                new String[] {"OAEPWITHSHA-512ANDMGF1PADDING"});

        rsa.launcher();

        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }

/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
*/
    @AndroidOnly("Fails on RI but succeeds on Android.")
    public void test_RSANoPadding() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {1024},
                new String[] {"ECB"}, new String[] {"NOPADDING"});

        rsa.launcher();

        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }
}
