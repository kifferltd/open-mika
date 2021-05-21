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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.javax.crypto.spec;

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;

import java.util.Arrays;

import javax.crypto.spec.PBEParameterSpec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//@TestTargetClass(PBEParameterSpec.class)
/**
 */

public class PBEParameterSpecTest extends TestCase {

    /**
     * PBEParameterSpec(byte[] salt, int iterationCount) method testing.
     * Tests the behavior of the method in the case of null input array
     * and tests that input array is copied during the object initialization.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PBEParameterSpec",
        args = {byte[].class, int.class}
    )
     */
    public void testPBEParameterSpec() {
        byte[] salt = {1, 2, 3, 4, 5};
        int iterationCount = 10;

        try {
            new PBEParameterSpec(null, iterationCount);
            fail("A NullPointerException should be was thrown "
                    + "in the case of null salt.");
        } catch (NullPointerException e) {
        }

        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        salt[0] ++;
        assertFalse("The change of salt specified in the constructor "
                    + "should not cause the change of internal array.",
                    salt[0] == pbeps.getSalt()[0]);
   }

    /**
     * getSalt() method testing. Tests that returned salt is equal
     * to the salt specified in the constructor and that the change of
     * returned array does not cause the change of internal array.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSalt",
        args = {}
    )
     */
    public void testGetSalt() {
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        byte[] result = pbeps.getSalt();
        if (! Arrays.equals(salt, result)) {
            fail("The returned salt is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getSalt() method salt"
                    + "should not cause the change of internal array.",
                    result[0] == pbeps.getSalt()[0]);
    }

    /**
     * getIterationCount() method testing. Tests that returned value is equal
     * to the value specified in the constructor.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterationCount",
        args = {}
    )
     */
    public void testGetIterationCount() {
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        assertTrue("The returned iterationCount is not equal to the specified "
                + "in the constructor.",
                pbeps.getIterationCount() == iterationCount);
    }

    public static Test suite() {
        return new TestSuite(PBEParameterSpecTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

