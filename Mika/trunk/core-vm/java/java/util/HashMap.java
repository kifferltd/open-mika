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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * HashMap is an implementation of Map. All optional operations (adding and
 * removing) are supported. Keys and values can be any objects.
 */
public class HashMap extends AbstractMap implements Map,
        Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * Actual count of entries
     */
    transient int elementCount;

    /*
     * The internal data structure to hold Entries
     */
    transient Entry[] elementData;

    /*
     * modification count, to keep track of structural modifications between the
     * HashMap and the iterator
     */
    transient int modCount = 0;

    /*
     * default size that an HashMap created using the default constructor would
     * have.
     */
    private static final int DEFAULT_SIZE = 16;

    /*
     * maximum ratio of (stored elements)/(storage size) which does not lead to
     * rehash
     */
    final float loadFactor;

    /*
     * maximum number of elements that can be put in this map before having to
     * rehash
     */
    int threshold;

    static class Entry extends MapEntry {
        final int origKeyHash;

        Entry next;

        Entry(Object theKey, int hash) {
            super(theKey, null);
            this.origKeyHash = hash;
        }

        Entry(Object theKey, Object theValue) {
            super(theKey, theValue);
            origKeyHash = (theKey == null ? 0 : computeHashCode(theKey));
        }

        public Object clone() {
            Entry entry = (Entry) super.clone();
            if (next != null) {
                entry.next = (Entry) next.clone();
            }
            return entry;
        }
    }

    private static class AbstractMapIterator  {
        private int position = 0;
        int expectedModCount;
        Entry futureEntry;
        Entry currentEntry;
        Entry prevEntry;

        final HashMap associatedMap;

        AbstractMapIterator(HashMap hm) {
            associatedMap = hm;
            expectedModCount = hm.modCount;
            futureEntry = null;
        }

        public boolean hasNext() {
            if (futureEntry != null) {
                return true;
            }
            while (position < associatedMap.elementData.length) {
                if (associatedMap.elementData[position] == null) {
                    position++;
                } else {
                    return true;
                }
            }
            return false;
        }

        final void checkConcurrentMod() throws ConcurrentModificationException {
            if (expectedModCount != associatedMap.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        final void makeNext() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (futureEntry == null) {
                currentEntry = associatedMap.elementData[position++];
                futureEntry = currentEntry.next;
                prevEntry = null;
            } else {
                if(currentEntry!=null){
                    prevEntry = currentEntry;
                }
                currentEntry = futureEntry;
                futureEntry = futureEntry.next;
            }
        }

        public final void remove() {
            checkConcurrentMod();
            if (currentEntry==null) {
                throw new IllegalStateException();
            }
            if(prevEntry==null){
                int index = currentEntry.origKeyHash & (associatedMap.elementData.length - 1);
                associatedMap.elementData[index] = associatedMap.elementData[index].next;
            } else {
                prevEntry.next = currentEntry.next;
            }
            currentEntry = null;
            expectedModCount++;
            associatedMap.modCount++;
            associatedMap.elementCount--;

        }
    }


    private static class EntryIterator  extends AbstractMapIterator implements Iterator {

        EntryIterator (HashMap map) {
            super(map);
        }

        public Object next() {
            makeNext();
            return currentEntry;
        }
    }

    private static class KeyIterator  extends AbstractMapIterator implements Iterator {

        KeyIterator (HashMap map) {
            super(map);
        }

        public Object next() {
            makeNext();
            return currentEntry.key;
        }
    }

    private static class ValueIterator  extends AbstractMapIterator implements Iterator {

        ValueIterator (HashMap map) {
            super(map);
        }

        public Object next() {
            makeNext();
            return currentEntry.value;
        }
    }

    static class HashMapEntrySet extends AbstractSet {
        private final HashMap associatedMap;

        public HashMapEntrySet(HashMap hm) {
            associatedMap = hm;
        }

        HashMap hashMap() {
            return associatedMap;
        }

        public int size() {
            return associatedMap.elementCount;
        }

        public void clear() {
            associatedMap.clear();
        }

        public boolean remove(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry oEntry = (Map.Entry) object;
                Entry entry = associatedMap.getEntry(oEntry.getKey());
                if(valuesEq(entry, oEntry)) {
                    associatedMap.removeEntry(entry);
                    return true;
                }
            }
            return false;
        }

        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                Map.Entry oEntry = (Map.Entry) object;
                Entry entry = associatedMap.getEntry(oEntry.getKey());
                return valuesEq(entry, oEntry);
            }
            return false;
        }

        private static boolean valuesEq(Entry entry, Map.Entry oEntry) {
            return (entry != null) &&
                                   ((entry.value == null) ?
                                    (oEntry.getValue() == null) :
                                    (areEqualValues(entry.value, oEntry.getValue())));
        }

        public Iterator iterator() {
            return new EntryIterator (associatedMap);
        }
    }

    /**
     * Create a new element array
     *
     * @param s
     * @return Reference to the element array
     */
    Entry[] newElementArray(int s) {
        return new Entry[s];
    }

    /**
     * Constructs a new empty {@code HashMap} instance.
     */
    public HashMap() {
        this(DEFAULT_SIZE);
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero.
     */
    public HashMap(int capacity) {
        this(capacity, 0.75f);  // default load factor of 0.75
        }

    /**
     * Calculates the capacity of storage required for storing given number of
     * elements
     * 
     * @param x
     *            number of elements
     * @return storage size
     */
    private static final int calculateCapacity(int x) {
        if(x >= 1 << 30){
            return 1 << 30;
        }
        if(x == 0){
            return 16;
        }
        x = x -1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity and
     * load factor.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the initial load factor.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero or the load factor is
     *                less or equal to zero.
     */
    public HashMap(int capacity, float loadFactor) {
        if (capacity >= 0 && loadFactor > 0) {
            capacity = calculateCapacity(capacity);
            elementCount = 0;
            elementData = newElementArray(capacity);
            this.loadFactor = loadFactor;
            computeThreshold();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new {@code HashMap} instance containing the mappings from
     * the specified map.
     *
     * @param map
     *            the mappings to add.
     */
    public HashMap(Map map) {
        this(calculateCapacity(map.size()));
        putAllImpl(map);
    }

    /**
     * Removes all mappings from this hash map, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    public void clear() {
        if (elementCount > 0) {
            elementCount = 0;
            Arrays.fill(elementData, null);
            modCount++;
        }
    }

    /**
     * Returns a shallow copy of this map.
     *
     * @return a shallow copy of this map.
     */
    public Object clone() {
        try {
            HashMap map = (HashMap) super.clone();
            map.elementCount = 0;
            map.elementData = newElementArray(elementData.length);
            map.putAll(this);
            
            return map;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Computes the threshold for rehashing
     */
    private void computeThreshold() {
        threshold = (int) (elementData.length * loadFactor);
    }

    /**
     * Returns whether this map contains the specified key.
     *
     * @param key
     *            the key to search for.
     * @return {@code true} if this map contains the specified key,
     *         {@code false} otherwise.
     */
    public boolean containsKey(Object key) {
        Entry m = getEntry(key);
        return m != null;
    }

    /**
     * Returns whether this map contains the specified value.
     *
     * @param value
     *            the value to search for.
     * @return {@code true} if this map contains the specified value,
     *         {@code false} otherwise.
     */
    public boolean containsValue(Object value) {
        if (value != null) {
            for (int i = 0; i < elementData.length; i++) {
                Entry entry = elementData[i];
                while (entry != null) {
                    if (areEqualValues(value, entry.value)) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        } else {
            for (int i = 0; i < elementData.length; i++) {
                Entry entry = elementData[i];
                while (entry != null) {
                    if (entry.value == null) {
                        return true;
                    }
                    entry = entry.next;
                }
            }
        }
        return false;
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     *
     * @return a set of the mappings.
     */
    public Set entrySet() {
        return new HashMapEntrySet(this);
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    public Object get(Object key) {
        Entry m = getEntry(key);
        if (m != null) {
            return m.value;
        }
        return null;
    }

    final Entry getEntry(Object key) {
        Entry m;
        if (key == null) {
            m = findNullKeyEntry();
        } else {
            int hash = computeHashCode(key);
            int index = hash & (elementData.length - 1);
            m = findNonNullKeyEntry(key, index, hash);
        }
        return m;
    }

    final Entry findNonNullKeyEntry(Object key, int index, int keyHash) {
        Entry m = elementData[index];
        while (m != null
                && (m.origKeyHash != keyHash || !areEqualKeys(key, m.key))) {
            m = m.next;
        }
        return m;
    }

    final Entry findNullKeyEntry() {
        Entry m = elementData[0];
        while (m != null && m.key != null)
            m = m.next;
        return m;
    }

    /**
     * Returns whether this map is empty.
     *
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    public boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The set does not
     * support adding.
     *
     * @return a set of the keys.
     */
    public Set keySet() {
        if (keySet == null) {
            keySet = new AbstractSet() {
                public boolean contains(Object object) {
                    return containsKey(object);
                }

                public int size() {
                    return HashMap.this.size();
                }

                public void clear() {
                    HashMap.this.clear();
                }

                public boolean remove(Object key) {
                    Entry entry = HashMap.this.removeEntry(key);
                    return entry != null;
                }

                public Iterator iterator() {
                    return new KeyIterator (HashMap.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or
     *         {@code null} if there was no such mapping.
     */
    public Object put(Object key, Object value) {
        return putImpl(key, value);
    }

    Object putImpl(Object key, Object value) {
        Entry entry;
        if(key == null) {
            entry = findNullKeyEntry();
            if (entry == null) {
                modCount++;
                entry = createHashedEntry(null, 0, 0);
                if (++elementCount > threshold) {
                    rehash();
                }
            }
        } else {
            int hash = computeHashCode(key);
            int index = hash & (elementData.length - 1);
            entry = findNonNullKeyEntry(key, index, hash);
            if (entry == null) {
                modCount++;
                entry = createHashedEntry(key, index, hash);
                if (++elementCount > threshold) {
                    rehash();
                }
            }
        }

        Object result = entry.value;
        entry.value = value;
        return result;
    }

    Entry createEntry(Object key, int index, Object value) {
        Entry entry = new Entry(key, value);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    Entry createHashedEntry(Object key, int index, int hash) {
        Entry entry = new Entry(key,hash);
        entry.next = elementData[index];
        elementData[index] = entry;
        return entry;
    }

    /**
     * Copies all the mappings in the specified map to this map. These mappings
     * will replace all mappings that this map had for any of the keys currently
     * in the given map.
     *
     * @param map
     *            the map to copy mappings from.
     * @throws NullPointerException
     *             if {@code map} is {@code null}.
     */
    public void putAll(Map map) {
        if (!map.isEmpty()) {
            putAllImpl(map);
        }
    }

    private void putAllImpl(Map map) {
        int capacity = elementCount + map.size();
        if (capacity > threshold) {
            rehash(capacity);
        }
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            putImpl(entry.getKey(), entry.getValue());
        }
    }

    void rehash(int capacity) {
        int length = calculateCapacity((capacity == 0 ? 1 : capacity << 1));

        Entry[] newData = newElementArray(length);
        for (int i = 0; i < elementData.length; i++) {
            Entry entry = elementData[i];
            elementData[i] = null;
            while (entry != null) {
                int index = entry.origKeyHash & (length - 1);
                Entry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeThreshold();
    }

    void rehash() {
        rehash(elementData.length);
    }

    /**
     * Removes the mapping with the specified key from this map.
     *
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     */
    public Object remove(Object key) {
        Entry entry = removeEntry(key);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }

    /*
     * Remove the given entry from the hashmap.
     * Assumes that the entry is in the map.
     */
    final void removeEntry(Entry entry) {
        int index = entry.origKeyHash & (elementData.length - 1);
        Entry m = elementData[index];
        if (m == entry) {
            elementData[index] = entry.next;
        } else {
            while (m.next != entry) {
                m = m.next;
            }
            m.next = entry.next;

        }
        modCount++;
        elementCount--;
    }

    final Entry removeEntry(Object key) {
        int index = 0;
        Entry entry;
        Entry last = null;
        if (key != null) {
            int hash = computeHashCode(key);
            index = hash & (elementData.length - 1);
            entry = elementData[index];
            while (entry != null && !(entry.origKeyHash == hash && areEqualKeys(key, entry.key))) {
                last = entry;
                entry = entry.next;
            }
        } else {
            entry = elementData[0];
            while (entry != null && entry.key != null) {
                last = entry;
                entry = entry.next;
            }
        }
        if (entry == null) {
            return null;
        }
        if (last == null) {
            elementData[index] = entry.next;
        } else {
            last.next = entry.next;
        }
        modCount++;
        elementCount--;
        return entry;
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    public int size() {
        return elementCount;
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The {@code size}
     * method wraps the map's size method and the {@code contains} method wraps
     * the map's containsValue method.
     * <p>
     * The collection is created when this method is called for the first time
     * and returned in response to all subsequent calls. This method may return
     * different collections when multiple concurrent calls occur, since no
     * synchronization is performed.
     *
     * @return a collection of the values contained in this map.
     */
    public Collection values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection() {
                public boolean contains(Object object) {
                    return containsValue(object);
                }

                public int size() {
                    return HashMap.this.size();
                }

                public void clear() {
                    HashMap.this.clear();
                }

                public Iterator iterator() {
                    return new ValueIterator(HashMap.this);
                }
            };
        }
        return valuesCollection;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(elementData.length);
        stream.writeInt(elementCount);
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            stream.writeObject(entry.key);
            stream.writeObject(entry.value);
            entry = entry.next;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int length = stream.readInt();
        elementData = newElementArray(length);
        elementCount = stream.readInt();
        for (int i = elementCount; --i >= 0;) {
            Object key = stream.readObject();
            int index = (null == key) ? 0 : (computeHashCode(key) & (length - 1));
            createEntry(key, index, stream.readObject());
        }
    }

    /*
     * Contract-related functionality 
     */
    static int computeHashCode(Object key) {
        return key.hashCode();
}

    static boolean areEqualKeys(Object key1, Object key2) {
        return (key1 == key2) || key1.equals(key2);
    }
    
    static boolean areEqualValues(Object value1, Object value2) {
        return (value1 == value2) || value1.equals(value2);
    }
    
    
}
