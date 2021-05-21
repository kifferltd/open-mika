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

package org.apache.harmony.luni.tests.java.util;

//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

//@TestTargetClass(IdentityHashMap.class) 
public class IdentityHashMapTest extends junit.framework.TestCase {

    /**
     * @tests java.util.IdentityHashMap#containsKey(java.lang.Object)
     * @tests java.util.IdentityHashMap#containsValue(java.lang.Object)
     * @tests java.util.IdentityHashMap#put(java.lang.Object, java.lang.Object)
     * @tests java.util.IdentityHashMap#get(java.lang.Object)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks null as a parameter.",
            method = "containsKey",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks null as a parameter.",
            method = "containsValue",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks null as a parameter.",
            method = "put",
            args = {java.lang.Object.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Checks null as a parameter.",
            method = "get",
            args = {java.lang.Object.class}
        )
    })
     */
    public void test_null_Keys_and_Values() {
        // tests with null keys and values
        IdentityHashMap map = new IdentityHashMap();
        Object result;

        // null key and null value
        result = map.put(null, null);
        assertTrue("testA can not find null key", map.containsKey(null));
        assertTrue("testA can not find null value", map.containsValue(null));
        assertNull("testA can not get null value for null key",
                map.get(null));
        assertNull("testA put returned wrong value", result);

        // null value
        String value = "a value";
        result = map.put(null, value);
        assertTrue("testB can not find null key", map.containsKey(null));
        assertTrue("testB can not find a value with null key", map
                .containsValue(value));
        assertTrue("testB can not get value for null key",
                map.get(null) == value);
        assertNull("testB put returned wrong value", result);

        // a null key
        String key = "a key";
        result = map.put(key, null);
        assertTrue("testC can not find a key with null value", map
                .containsKey(key));
        assertTrue("testC can not find null value", map.containsValue(null));
        assertNull("testC can not get null value for key", map.get(key));
        assertNull("testC put returned wrong value", result);

        // another null key
        String anothervalue = "another value";
        result = map.put(null, anothervalue);
        assertTrue("testD can not find null key", map.containsKey(null));
        assertTrue("testD can not find a value with null key", map
                .containsValue(anothervalue));
        assertTrue("testD can not get value for null key",
                map.get(null) == anothervalue);
        assertTrue("testD put returned wrong value", result == value);

        // remove a null key
        result = map.remove(null);
        assertTrue("testE remove returned wrong value", result == anothervalue);
        assertTrue("testE should not find null key", !map.containsKey(null));
        assertTrue("testE should not find a value with null key", !map
                .containsValue(anothervalue));
        assertNull("testE should not get value for null key",
                map.get(null));
    }

    /**
     * @tests java.util.IdentityHashMap#put(java.lang.Object, java.lang.Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks null as a parameter.",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        IdentityHashMap<Object, Object> map = new IdentityHashMap<Object, Object>();
        
        // Test null as a key.
        Object value = "Some value";
        map.put(null, value);
        assertSame("Assert 0: Failure getting null key", value, map.get(null));
        
        // Test null as a value
        Object key = "Some key";
        map.put(key, null);
        assertNull("Assert 1: Failure getting null value", map.get(key));
    }

    /**
     * @tests java.util.IdentityHashMap#remove(java.lang.Object)
     * @tests java.util.IdentityHashMap#keySet()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify removed value.",
        method = "remove",
        args = {java.lang.Object.class}
    )
     */
    public void test_remove() {
        IdentityHashMap map = new IdentityHashMap();
        map.put(null, null);
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.remove("key1");

        assertTrue("Did not remove key1", !map.containsKey("key1"));
        assertTrue("Did not remove the value for key1", !map
                .containsValue("value1"));

        assertTrue("Modified key2", map.get("key2") != null
                && map.get("key2") == "value2");
        assertNull("Modified null entry", map.get(null));
    }

    /**
     * @tests java.util.IdentityHashMapTest#remove(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test.",
        method = "remove",
        args = {java.lang.Object.class}
    )
     */
    public void test_removeLjava_lang_Object() {
        // Regression for HARMONY-37
        IdentityHashMap<String, String> hashMap = new IdentityHashMap<String, String>();
        hashMap.remove("absent");
        assertEquals("Assert 0: Size is incorrect", 0, hashMap.size());

        hashMap.put("key", "value");
        hashMap.remove("key");
        assertEquals("Assert 1: After removing non-null element size is incorrect", 0, hashMap.size());

        hashMap.put(null, null);
        assertEquals("Assert 2: adding literal null failed", 1, hashMap.size());
        hashMap.remove(null);
        assertEquals("Assert 3: After removing null element size is incorrect", 0, hashMap.size());
    }

    /**
     * @tests java.util.IdentityHashMap#entrySet()
     * @tests java.util.IdentityHashMap#keySet()
     * @tests java.util.IdentityHashMap#values()
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "keySet",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "entrySet",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "values",
            args = {}
        )
    })
     */
    public void test_sets() {
        // tests with null keys and values
        IdentityHashMap map = new IdentityHashMap();

        // null key and null value
        map.put("key", "value");
        map.put(null, null);
        map.put("a key", null);
        map.put("another key", null);

        Set keyset = map.keySet();
        Collection valueset = map.values();
        Set entries = map.entrySet();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            assertTrue("EntrySetIterator can not find entry ", entries
                    .contains(entry));

            assertTrue("entry key not found in map", map.containsKey(entry
                    .getKey()));
            assertTrue("entry value not found in map", map.containsValue(entry
                    .getValue()));

            assertTrue("entry key not found in the keyset", keyset
                    .contains(entry.getKey()));
            assertTrue("entry value not found in the valueset", valueset
                    .contains(entry.getValue()));
        }
    }

    /**
     * @tests java.util.IdentityHashMap#entrySet()
     * @tests java.util.IdentityHashMap#remove(java.lang.Object)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies positive functionality. Doesn't verify that remove method returns null if there was no entry for key.",
            method = "entrySet",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies positive functionality. Doesn't verify that remove method returns null if there was no entry for key.",
            method = "remove",
            args = {java.lang.Object.class}
        )
    })
     */
    public void test_entrySet_removeAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.entrySet();

        set.removeAll(set);
        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the entryset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("entrySet iterator still has elements", !it.hasNext());
    }

    /**
     * @tests java.util.IdentityHashMap#keySet()
     * @tests java.util.IdentityHashMap#clear()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
     */
    public void test_keySet_clear() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();
        set.clear();

        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * @tests java.util.IdentityHashMap#values()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
     */
    public void test_values() {

        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 10; i++) {
            map.put(new Integer(i), new Integer(i));
        }

        Integer key = new Integer(20);
        Integer value = new Integer(40);
        map.put(key, value);

        Collection vals = map.values();
        boolean result = vals.remove(key);
        assertTrue("removed entries incorrectly", map.size() == 11 && !result);
        assertTrue("removed key incorrectly", map.containsKey(key));
        assertTrue("removed value incorrectly", map.containsValue(value));

        result = vals.remove(value);
        assertTrue("Did not remove entry as expected", map.size() == 10
                && result);
        assertTrue("Did not remove key as expected", !map.containsKey(key));
        assertTrue("Did not remove value as expected", !map
                .containsValue(value));

        // put an equivalent key to a value
        key = new Integer(1);
        value = new Integer(100);
        map.put(key, value);

        result = vals.remove(key);
        assertTrue("TestB. removed entries incorrectly", map.size() == 11
                && !result);
        assertTrue("TestB. removed key incorrectly", map.containsKey(key));
        assertTrue("TestB. removed value incorrectly", map.containsValue(value));

        result = vals.remove(value);
        assertTrue("TestB. Did not remove entry as expected", map.size() == 10
                && result);
        assertTrue("TestB. Did not remove key as expected", !map
                .containsKey(key));
        assertTrue("TestB. Did not remove value as expected", !map
                .containsValue(value));

        vals.clear();
        assertEquals("Did not remove all entries as expected", 0, map.size());
    }

    /**
     * @tests java.util.IdentityHashMap#keySet()
     * @tests java.util.IdentityHashMap#remove(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
     */
    public void test_keySet_removeAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();
        set.removeAll(set);

        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * @tests java.util.IdentityHashMap#keySet()
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
     */
    public void test_keySet_retainAll() {
        IdentityHashMap map = new IdentityHashMap();
        for (int i = 0; i < 1000; i++) {
            map.put(new Integer(i), new Integer(i));
        }
        Set set = map.keySet();

        // retain all the elements
        boolean result = set.retainAll(set);
        assertTrue("retain all should return false", !result);
        assertEquals("did not retain all", 1000, set.size());

        // send empty set to retainAll
        result = set.retainAll(new TreeSet());
        assertTrue("retain all should return true", result);
        assertEquals("did not remove all elements in the map", 0, map.size());
        assertTrue("did not remove all elements in the keyset", set.isEmpty());

        Iterator it = set.iterator();
        assertTrue("keySet iterator still has elements", !it.hasNext());
    }

    /**
     * @tests java.util.IdentityHashMap#keySet()
     * @tests java.util.IdentityHashMap#remove(java.lang.Object)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "keySet",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "remove",
            args = {java.lang.Object.class}
        )
    })
     */
    public void test_keyset_remove() {
        IdentityHashMap map = new IdentityHashMap();

        Integer key = new Integer(21);

        map.put(new Integer(1), null);
        map.put(new Integer(11), null);
        map.put(key, null);
        map.put(new Integer(31), null);
        map.put(new Integer(41), null);
        map.put(new Integer(51), null);
        map.put(new Integer(61), null);
        map.put(new Integer(71), null);
        map.put(new Integer(81), null);
        map.put(new Integer(91), null);

        Set set = map.keySet();

        Set newset = new HashSet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element == key) {
                it.remove();
            } else
                newset.add(element);
        }
        int size = newset.size();
        assertTrue("keyset and newset don't have same size",
                newset.size() == size);
        assertTrue("element is in newset ", !newset.contains(key));
        assertTrue("element not removed from keyset", !set.contains(key));
        assertTrue("element not removed from map", !map.containsKey(key));

        assertTrue("newset and keyset do not have same elements 1", newset
                .equals(set));
        assertTrue("newset and keyset do not have same elements 2", set
                .equals(newset));
    }

    // comparator for IdentityHashMap objects
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            IdentityHashMap init = (IdentityHashMap) initial;
            IdentityHashMap desr = (IdentityHashMap) deserialized;

            assertEquals("Size", init.size(), desr.size());
        }
    };

    /**
     * @tests serialization/deserialization compatibility with RI.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
     */
    public void testSerializationCompatibility() throws Exception {
        IdentityHashMap<String, String> identityHashMap = new IdentityHashMap<String, String>();
        identityHashMap.put("key1", "value1");
        identityHashMap.put("key2", "value2");
        identityHashMap.put("key3", "value3");

        SerializationTest.verifyGolden(this, identityHashMap, COMPARATOR);
    }
}
