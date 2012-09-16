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
//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

// import targets.Cipher;

//@TestTargetClass(Cipher.AESWrap.class)
public class CipherAesWrapTest extends TestCase {
// 3 cases checked
/*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
*/
    public void test_AesWrap() {
        CipherWrapThread aesWrap = new CipherWrapThread("AESWrap", new int[] {
                128, 192, 256}, // Keysize must be 128, 192, 256.
                new String[] {"ECB"}, new String[] {"NoPadding"});

        aesWrap.launcher();

        assertEquals(aesWrap.getFailureMessages(), 0, aesWrap
                .getTotalFailuresNumber());
    }
}
