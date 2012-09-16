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

//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

// import targets.KeyGenerator;

public class KeyGeneratorFunctionalTest extends TestCase {
/*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.AES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.DES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.DESede.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACMD5.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA1.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA256.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA384.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyGenerator.HMACSHA512.class,
            method = "method",
            args = {}
        )
    })
*/
    public void test_() throws Exception {
        String[] algArray = {"AES", "DES", "DESEDE", "DESede", 
                "HMACMD5", "HmacMD5", "HMACSHA1", "HmacSHA1", "HMACSHA256",
                "HmacSHA256", "HMACSHA384", "HmacSHA384", "HMACSHA512",
                "HmacSHA512"};

        KeyGeneratorThread kgt = new KeyGeneratorThread(algArray);
        kgt.launcher();
        
        assertEquals(kgt.getFailureMessages(), 0, kgt.getTotalFailuresNumber());
    }
}
