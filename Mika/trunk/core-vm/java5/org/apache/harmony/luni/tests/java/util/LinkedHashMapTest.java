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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import tests.support.Support_MapTest2;
import tests.support.Support_UnmodifiableCollectionTest;

/**
 * @tests java.util.LinkedHashMap
 */
//@TestTargetClass(LinkedHashMap.class) 
public class LinkedHashMapTest extends junit.framework.TestCase {

    LinkedHashMap hm;

    final static int hmSize = 1000;

    static Object[] objArray;

    static Object[] objArray2;
    {
        objArray = new Object[hmSize];
        objArray2 = new Object[hmSize];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = new Integer(i);
            objArray2[i] = objArray[i].toString();
        }
    }

    static final class CacheMap extends LinkedHashMap {
        protected boolean removeEldestEntry(Map.Entry e) {
            return size() > 5;
        }
    }
    
    private static class MockMapNull extends AbstractMap {
        @Override
        public Set entrySet() {
            return null;
        }

        @Override
        public int size() {
            return 10;
        }
    }

    /**
     * @tests java.util.LinkedHashMap#LinkedHashMap()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashMap",
        args = {}
    )
     */
    public void test_Constructor() {
        // Test for method java.util.LinkedHashMap()
        new Support_MapTest2(new LinkedHashMap()).runTest();

        LinkedHashMap hm2 = new LinkedHashMap();
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
    }

    /**
     * @tests java.util.LinkedHashMap#LinkedHashMap(int)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashMap",
        args = {int.class}
    )
     */
    public void test_ConstructorI() {
        // Test for method java.util.LinkedHashMap(int)
        LinkedHashMap hm2 = new LinkedHashMap(5);
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
        try {
            new LinkedHashMap(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        LinkedHashMap empty = new LinkedHashMap(0);
        assertNull("Empty LinkedHashMap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * @tests java.util.LinkedHashMap#LinkedHashMap(int, float)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashMap",
        args = {int.class, float.class}
    )
     */
    public void test_ConstructorIF() {
        // Test for method java.util.LinkedHashMap(int, float)
        LinkedHashMap hm2 = new LinkedHashMap(5, (float) 0.5);
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
        try {
            new LinkedHashMap(0, 0);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        LinkedHashMap empty = new LinkedHashMap(0, 0.75f);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }

    /**
     * @tests java.util.LinkedHashMap#LinkedHashMap(java.util.Map)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashMap",
        args = {java.util.Map.class}
    )
     */
    public void test_ConstructorLjava_util_Map() {
        // Test for method java.util.LinkedHashMap(java.util.Map)
        Map myMap = new TreeMap();
        for (int counter = 0; counter < hmSize; counter++)
            myMap.put(objArray2[counter], objArray[counter]);
        LinkedHashMap hm2 = new LinkedHashMap(myMap);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Failed to construct correct LinkedHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));
        
        try {
            new LinkedHashMap(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            //expected
        }
    }

    /**
     * @tests java.util.LinkedHashMap#get(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {java.lang.Object.class}
    )
     */
    public void test_getLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.LinkedHashMap.get(java.lang.Object)
        assertNull("Get returned non-null for non existent key",
                hm.get("T"));
        hm.put("T", "HELLO");
        assertEquals("Get returned incorecct value for existing key", "HELLO", hm.get("T")
                );

        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertEquals("Failed with null key", "test", m.get(null));
        assertNull("Failed with missing key matching null hash", m
                .get(new Integer(0)));
    }

    /**
     * @tests java.util.LinkedHashMap#put(java.lang.Object, java.lang.Object)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
     */
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.LinkedHashMap.put(java.lang.Object, java.lang.Object)
        hm.put("KEY", "VALUE");
        assertEquals("Failed to install key/value pair", 
                "VALUE", hm.get("KEY"));

        LinkedHashMap m = new LinkedHashMap();
        m.put(new Short((short) 0), "short");
        m.put(null, "test");
        m.put(new Integer(0), "int");
        assertEquals("Failed adding to bucket containing null", "short", m.get(
                new Short((short) 0)));
        assertEquals("Failed adding to bucket containing null2", "int", m.get(
                new Integer(0)));
    }

    /**
     * @tests java.util.LinkedHashMap#putAll(java.util.Map)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies only positive functionality.",
        method = "putAll",
        args = {java.util.Map.class}
    )
     */
    public void test_putAllLjava_util_Map() {
        // Test for method void java.util.LinkedHashMap.putAll(java.util.Map)
        LinkedHashMap hm2 = new LinkedHashMap();
        hm2.putAll(hm);
        for (int i = 0; i < 1000; i++)
            assertTrue("Failed to clear all elements", hm2.get(
                    new Integer(i).toString()).equals((new Integer(i))));
    }

    /**
     * @tests java.util.LinkedHashMap#putAll(java.util.Map)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "putAll",
        args = {java.util.Map.class}
    )
     */
    public void test_putAll_Ljava_util_Map_Null() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            linkedHashMap.putAll(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected.
        }

        try {
            linkedHashMap = new LinkedHashMap(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected.
        }
    } 

    /**
     * @tests java.util.LinkedHashMap#entrySet()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
     */
    public void test_entrySet() {
        // Test for method java.util.Set java.util.LinkedHashMap.entrySet()
        Set s = hm.entrySet();
        Iterator i = s.iterator();
        assertTrue("Returned set of incorrect size", hm.size() == s.size());
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            assertTrue("Returned incorrect entry set", hm.containsKey(m
                    .getKey())
                    && hm.containsValue(m.getValue()));
        }
    }

    /**
     * @tests java.util.LinkedHashMap#keySet()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
     */
    public void test_keySet() {
        // Test for method java.util.Set java.util.LinkedHashMap.keySet()
        Set s = hm.keySet();
        assertTrue("Returned set of incorrect size()", s.size() == hm.size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned set does not contain all keys", s
                    .contains(objArray[i].toString()));

        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.keySet().contains(null));
        assertNull("Failed with null key", m.keySet().iterator().next());

        Map map = new LinkedHashMap(101);
        map.put(new Integer(1), "1");
        map.put(new Integer(102), "102");
        map.put(new Integer(203), "203");
        Iterator it = map.keySet().iterator();
        Integer remove1 = (Integer) it.next();
        it.hasNext();
        it.remove();
        Integer remove2 = (Integer) it.next();
        it.remove();
        ArrayList list = new ArrayList(Arrays.asList(new Integer[] {
                new Integer(1), new Integer(102), new Integer(203) }));
        list.remove(remove1);
        list.remove(remove2);
        assertTrue("Wrong result", it.next().equals(list.get(0)));
        assertEquals("Wrong size", 1, map.size());
        assertTrue("Wrong contents", map.keySet().iterator().next().equals(
                list.get(0)));

        Map map2 = new LinkedHashMap(101);
        map2.put(new Integer(1), "1");
        map2.put(new Integer(4), "4");
        Iterator it2 = map2.keySet().iterator();
        Integer remove3 = (Integer) it2.next();
        Integer next;
        if (remove3.intValue() == 1)
            next = new Integer(4);
        else
            next = new Integer(1);
        it2.hasNext();
        it2.remove();
        assertTrue("Wrong result 2", it2.next().equals(next));
        assertEquals("Wrong size 2", 1, map2.size());
        assertTrue("Wrong contents 2", map2.keySet().iterator().next().equals(
                next));
    }

    /**
     * @tests java.util.LinkedHashMap#values()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
     */
    public void test_values() {
        // Test for method java.util.Collection java.util.LinkedHashMap.values()
        Collection c = hm.values();
        assertTrue("Returned collection of incorrect size()", c.size() == hm
                .size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned collection does not contain all keys", c
                    .contains(objArray[i]));

        LinkedHashMap myLinkedHashMap = new LinkedHashMap();
        for (int i = 0; i < 100; i++)
            myLinkedHashMap.put(objArray2[i], objArray[i]);
        Collection values = myLinkedHashMap.values();
        new Support_UnmodifiableCollectionTest(
                "Test Returned Collection From LinkedHashMap.values()", values)
                .runTest();
        values.remove(new Integer(0));
        assertTrue(
                "Removing from the values collection should remove from the original map",
                !myLinkedHashMap.containsValue(new Integer(0)));

    }

    /**
     * @tests java.util.LinkedHashMap#remove(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
     */
    public void test_removeLjava_lang_Object() {
        // Test for method java.lang.Object
        // java.util.LinkedHashMap.remove(java.lang.Object)
        int size = hm.size();
        Integer y = new Integer(9);
        Integer x = ((Integer) hm.remove(y.toString()));
        assertTrue("Remove returned incorrect value", x.equals(new Integer(9)));
        assertNull("Failed to remove given key", hm.get(new Integer(9)));
        assertTrue("Failed to decrement size", hm.size() == (size - 1));
        assertNull("Remove of non-existent key returned non-null", hm
                .remove("LCLCLC"));

        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertNull("Failed with same hash as null",
                m.remove(new Integer(0)));
        assertEquals("Failed with null key", "test", m.remove(null));
    }

    /**
     * @tests java.util.LinkedHashMap#clear()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
     */
    public void test_clear() {
        // Test for method void java.util.LinkedHashMap.clear()
        hm.clear();
        assertEquals("Clear failed to reset size", 0, hm.size());
        for (int i = 0; i < hmSize; i++)
            assertNull("Failed to clear all elements",
                    hm.get(objArray2[i]));

    }

    /**
     * @tests java.util.LinkedHashMap#clone()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
     */
    public void test_clone() {
        // Test for method java.lang.Object java.util.LinkedHashMap.clone()
        LinkedHashMap hm2 = (LinkedHashMap) hm.clone();
        assertTrue("Clone answered equivalent LinkedHashMap", hm2 != hm);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Clone answered unequal LinkedHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));

        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        // get the keySet() and values() on the original Map
        Set keys = map.keySet();
        Collection values = map.values();
        assertEquals("values() does not work", 
                "value", values.iterator().next());
        assertEquals("keySet() does not work", 
                "key", keys.iterator().next());
        AbstractMap map2 = (AbstractMap) map.clone();
        map2.put("key", "value2");
        Collection values2 = map2.values();
        assertTrue("values() is identical", values2 != values);
        
        // values() and keySet() on the cloned() map should be different
        assertEquals("values() was not cloned", 
                "value2", values2.iterator().next());
        map2.clear();
        map2.put("key2", "value3");
        Set key2 = map2.keySet();
        assertTrue("keySet() is identical", key2 != keys);
        assertEquals("keySet() was not cloned", 
                "key2", key2.iterator().next());
    }

    /**
     * @tests java.util.LinkedHashMap#containsKey(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
     */
    public void test_containsKeyLjava_lang_Object() {
        // Test for method boolean
        // java.util.LinkedHashMap.containsKey(java.lang.Object)
        assertTrue("Returned false for valid key", hm.containsKey(new Integer(
                876).toString()));
        assertTrue("Returned true for invalid key", !hm.containsKey("KKDKDKD"));

        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.containsKey(null));
        assertTrue("Failed with missing key matching null hash", !m
                .containsKey(new Integer(0)));
    }

    /**
     * @tests java.util.LinkedHashMap#containsValue(java.lang.Object)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
     */
    public void test_containsValueLjava_lang_Object() {
        // Test for method boolean
        // java.util.LinkedHashMap.containsValue(java.lang.Object)
        assertTrue("Returned false for valid value", hm
                .containsValue(new Integer(875)));
        assertTrue("Returned true for invalid valie", !hm
                .containsValue(new Integer(-9)));
    }

    /**
     * @tests java.util.LinkedHashMap#isEmpty()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
     */
    public void test_isEmpty() {
        // Test for method boolean java.util.LinkedHashMap.isEmpty()
        assertTrue("Returned false for new map", new LinkedHashMap().isEmpty());
        assertTrue("Returned true for non-empty", !hm.isEmpty());
    }

    /**
     * @tests java.util.LinkedHashMap#size()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
     */
    public void test_size() {
        // Test for method int java.util.LinkedHashMap.size()
        assertTrue("Returned incorrect size",
                hm.size() == (objArray.length + 2));
    }

    /**
     * @tests java.util.LinkedHashMap#entrySet()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
     */
    public void test_ordered_entrySet() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, ii.toString());
        }

        Set s1 = lhm.entrySet();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Map.Entry m = (Map.Entry) it1.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i);
        }

        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, ii.toString());
        }

        Set s3 = lruhm.entrySet();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size 2", lruhm.size() == s3
                .size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Map.Entry m = (Map.Entry) it3.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 2", jj.intValue() == i);
        }

        /* fetch the even numbered entries to affect traversal order */
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            String ii = (String) lruhm.get(new Integer(i));
            p = p + Integer.parseInt(ii);
        }
        assertEquals("invalid sum of even numbers", 2450, p);

        Set s2 = lruhm.entrySet();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size 3", lruhm.size() == s2
                .size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Map.Entry m = (Map.Entry) it2.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 3", jj.intValue() == i);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Map.Entry m = (Map.Entry) it2.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 4", jj.intValue() == i);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }

    /**
     * @tests java.util.LinkedHashMap#keySet()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
     */
    public void test_ordered_keySet() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, ii.toString());
        }

        Set s1 = lhm.keySet();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }

        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, ii.toString());
        }

        Set s3 = lruhm.keySet();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s3.size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Integer jj = (Integer) it3.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }

        /* fetch the even numbered entries to affect traversal order */
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            String ii = (String) lruhm.get(new Integer(i));
            p = p + Integer.parseInt(ii);
        }
        assertEquals("invalid sum of even numbers", 2450, p);

        Set s2 = lruhm.keySet();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s2.size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }

    /**
     * @tests java.util.LinkedHashMap#values()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
     */
    public void test_ordered_values() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, new Integer(i * 2));
        }

        Collection s1 = lhm.values();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i * 2);
        }

        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, new Integer(i * 2));
        }

        Collection s3 = lruhm.values();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s3.size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Integer jj = (Integer) it3.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }

        // fetch the even numbered entries to affect traversal order
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            Integer ii = (Integer) lruhm.get(new Integer(i));
            p = p + ii.intValue();
        }
        assertTrue("invalid sum of even numbers", p == 2450 * 2);

        Collection s2 = lruhm.values();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s2.size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }

    /**
     * @tests java.util.LinkedHashMap#removeEldestEntry(java.util.Map$Entry)
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify null as a parameter.",
        method = "removeEldestEntry",
        args = {java.util.Map.Entry.class}
    )
     */
    public void test_remove_eldest() {
        int i;
        int sz = 10;
        CacheMap lhm = new CacheMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, new Integer(i * 2));
        }

        Collection s1 = lhm.values();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 5; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i * 2);
        }
        assertTrue("Entries left in map", !it1.hasNext());
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "LinkedHashMap",
            args = {int.class, float.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "put",
            args = {java.lang.Object.class, java.lang.Object.class}
        )
    })
    */
    // BEGIN android-added
    public void testLinkedHashMap() {
        // we want to test the LinkedHashMap in access ordering mode.
        LinkedHashMap map = new LinkedHashMap<String, String>(10, 0.75f, true);

        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        Iterator iterator = map.keySet().iterator();
        String id = (String) iterator.next();
        map.get(id);
        try {
            iterator.next();
            // A LinkedHashMap is supposed to throw this Exception when a 
            // iterator.next() Operation takes place after a get
            // Operation. This is because the get Operation is considered
            // a structural modification if the LinkedHashMap is in
            // access order mode.
            fail("expected ConcurrentModificationException was not thrown.");
        } catch(ConcurrentModificationException e) {
            // expected
        }
        
        LinkedHashMap mapClone = (LinkedHashMap) map.clone();
        
        iterator = map.keySet().iterator();
        id = (String) iterator.next();
        mapClone.get(id);
        try {
            iterator.next();
        } catch(ConcurrentModificationException e) {
            fail("expected ConcurrentModificationException was not thrown.");
        }
        
        try {
            new LinkedHashMap<String, String>(-10, 0.75f, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        try {
            new LinkedHashMap<String, String>(10, -0.75f, true);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }
    // END android-added

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        hm = new LinkedHashMap();
        for (int i = 0; i < objArray.length; i++)
            hm.put(objArray2[i], objArray[i]);
        hm.put("test", null);
        hm.put(null, "test");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
