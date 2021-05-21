/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.javax.crypto;

// import dalvik.annotation.TestTargetClass;
// import dalvik.annotation.TestTargets;
// import dalvik.annotation.TestLevel;
// import dalvik.annotation.TestTargetNew;

import javax.crypto.SecretKey;

import junit.framework.TestCase;


// @TestTargetClass(SecretKey.class)
/**
 * Tests for <code>SecretKey</code> class field
 * 
 */
public class SecretKeyTest extends TestCase {

    /**
     * Test for <code>serialVersionUID</code> field
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!Constants",
        args = {}
    )
     */
    public void testField() {
        checkSecretKey sk = new checkSecretKey();
        assertEquals("Incorrect serialVersionUID", 
                sk.getSerVerUID(), //SecretKey.serialVersionUID
                -4795878709595146952L);
    }
    
    public class checkSecretKey implements SecretKey {
        public String getAlgorithm() {
            return "SecretKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
    }
}
