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

package org.apache.harmony.sql.tests.java.sql;

//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;

import java.sql.SQLPermission;

import junit.framework.TestCase;
//@TestTargetClass(SQLPermission.class)
/**
 * JUnit Testcase for the java.sql.SQLPermission class
 * 
 * Note that the SQLPermission class only defines 2 constructors and all other
 * methods are inherited. This testcase explicitly tets the constructors but also
 * implicitly tests some of the inherited query methods.
 * 
 */

public class SQLPermissionTest extends TestCase {

    /*
     * Constructor test
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "invalid parameters checking missed. not fully supported",
        method = "SQLPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testSQLPermissionStringString() {
        String validName = "setLog";
        String validActions = "theActions";

        SQLPermission thePermission = new SQLPermission(validName, validActions);

        assertNotNull(thePermission);
        assertEquals(validName, thePermission.getName());
        // System.out.println("The actions: " + thePermission.getActions() + "."
        // );
        assertEquals("", thePermission.getActions());
    } // end method testSQLPermissionStringString

    /*
     * Constructor test
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not fully supported",
        method = "SQLPermission",
        args = {java.lang.String.class}
    )
    public void testSQLPermissionString() {
        String validName = "setLog";

        SQLPermission thePermission = new SQLPermission(validName);

        assertNotNull(thePermission);
        assertEquals(validName, thePermission.getName());

        // Set an invalid name ... 
        String invalidName = "foo";

        thePermission = new SQLPermission(invalidName);

        assertNotNull(thePermission);
        assertEquals(invalidName, thePermission.getName());
        assertEquals("", thePermission.getActions());
    } // end method testSQLPermissionString

} // end class SQLPermissionTest


