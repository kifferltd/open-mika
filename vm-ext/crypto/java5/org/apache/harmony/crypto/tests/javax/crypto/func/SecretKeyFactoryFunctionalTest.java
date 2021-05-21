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

//import dalvik.annotation.KnownFailure;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

// import targets.SecretKeyFactory;

public class SecretKeyFactoryFunctionalTest extends TestCase {
/*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.DES.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.DESede.class,
            method = "method",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = SecretKeyFactory.PBEWITHMD5ANDDES.class,
            method = "method",
            args = {}
        )
    })
*/
    public void test_() throws Exception {
        String[] algArray = {"DES", "DESede", "PBEWITHMD5ANDDES"};

        SecretKeyFactoryThread skft = new SecretKeyFactoryThread(algArray);
        skft.launcher();
        
        assertEquals(skft.getFailureMessages(), 0, skft.getTotalFailuresNumber());
    }
}
