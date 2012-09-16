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

//import dalvik.annotation.BrokenTest;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

// import targets.KeyAgreement;

public class KeyAgreementFunctionalTest extends TestCase {
/*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = KeyAgreement.DH.class,
            method = "method",
            args = {}
        )
    })
    @BrokenTest("Too slow - disabling for now")
*/
    public void test_KeyAgreement() throws Exception {
        String[] algArray = {"DES", "DESede"};

        KeyAgreementThread kat = new KeyAgreementThread(algArray);
        kat.launcher();
        
        assertEquals(kat.getFailureMessages(), 0, kat.getTotalFailuresNumber());
    }
}
