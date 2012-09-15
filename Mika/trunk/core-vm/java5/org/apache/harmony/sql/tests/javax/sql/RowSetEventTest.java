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

package org.apache.harmony.sql.tests.javax.sql;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import javax.sql.ConnectionEvent;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import junit.framework.TestCase;

//@TestTargetClass(RowSetEvent.class)
public class RowSetEventTest extends TestCase {

    /**
     * @tests {@link javax.sql.RowSetEvent#RowSetEvent(javax.sql.RowSet)}.
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "functional test missing but not feasible: no implementation available.",
        method = "RowSetEvent",
        args = {javax.sql.RowSet.class}
    )    
     */
    public void testConstructor() {
        try {
            new RowSetEvent(null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }

        Impl_RowSet irs = new Impl_RowSet();
        RowSetEvent rse = new RowSetEvent(irs);
        assertSame(irs, rse.getSource());
    }
    
    
}
