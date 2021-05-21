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
package org.apache.harmony.text.tests.java.text;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;

//@TestTargetClass(MessageFormat.Field.class) 
public class MessageFormatFieldTest extends TestCase {
    /**
     * @tests java.text.MessageFormat$Field#Field(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class}
    )
     */
    public void test_ConstructorLjava_lang_String() {
        // protected constructor
        String name = "new Message format";
        MyMessageFormat field = new MyMessageFormat(name);
        assertEquals("field has wrong name", name, field.getName());

        field = new MyMessageFormat(null);
        assertEquals("field has wrong name", null, field.getName());
    }

    /**
     * @tests java.text.MessageFormat$Field#readResolve()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "readResolve",
        args = {}
    )
     */
    public void test_readResolve() {
        // test for method java.lang.Object readResolve()

        // see serialization stress tests:
        // implemented in
        // SerializationStressTest4.test_writeObject_MessageFormat_Field()
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);

            MessageFormat.Field mfield, mfield2;
            MyMessageFormat field;

            mfield = MessageFormat.Field.ARGUMENT;

            field = new MyMessageFormat(null);

            out.writeObject(mfield);
            out.writeObject(field);

            in = new ObjectInputStream(new ByteArrayInputStream(bytes
                    .toByteArray()));

            try {
                mfield2 = (MessageFormat.Field) in.readObject();
                assertSame("resolved incorrectly", mfield, mfield2);
            } catch (IllegalArgumentException e) {
                fail("Unexpected IllegalArgumentException: " + e);
            }

            try {
                in.readObject();
                fail("Expected InvalidObjectException for subclass instance with null name");
            } catch (InvalidObjectException e) {
            }

        } catch (IOException e) {
            fail("unexpected IOException" + e);
        } catch (ClassNotFoundException e) {
            fail("unexpected ClassNotFoundException" + e);
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
    }

    static class MyMessageFormat extends MessageFormat.Field {
        static final long serialVersionUID = 1L;

        protected MyMessageFormat(String attr) {
            super(attr);
        }

        protected String getName() {
            return super.getName();
        }
    }
}
