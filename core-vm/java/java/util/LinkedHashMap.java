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

/*
 * Imported by CG 20101226 based on Apache Harmony ("enhanced") revision 934338.
 */

package java.util;

/**
 * LinkedHashMap is a variant of HashMap. Its entries are kept in a
 * doubly-linked list. The iteration order is, by default, the order in which
 * keys were inserted. Reinserting an already existing key doesn't change the
 * order. A key is existing if a call to {@code containsKey} would return true.
 * <p>
 * If the three argument constructor is used, and {@code order} is specified as
 * {@code true}, the iteration will be in the order that entries were accessed.
 * The access order gets affected by put(), get(), putAll() operations, but not
 * by operations on the collection views.
 * <p>
 * Null elements are allowed, and all the optional map operations are supported.
 * <p>
 * <b>Note:</b> The implementation of {@code LinkedHashMap} is not synchronized.
 * If one thread of several threads accessing an instance modifies the map
 * structurally, access to the map needs to be synchronized. For
 * insertion-ordered instances a structural modification is an operation that
 * removes or adds an entry. Access-ordered instances also are structurally
 * modified by put(), get() and putAll() since these methods change the order of
 * the entries. Changes in the value of an entry are not structural changes.
 * <p>
 * The Iterator that can be created by calling the {@code iterator} method
 * throws a {@code ConcurrentModificationException} if the map is structurally
 * changed while an iterator is used to iterate over the elements. Only the
 * {@code remove} method that is provided by the iterator allows for removal of
 * elements during iteration. It is not possible to guarantee that this
 * mechanism works in all cases of unsynchronized concurrent modification. It
 * should only be used for debugging purposes.
 *
 * @since 1.4
 */
public class LinkedHashMap extends HashMap implements Map {

    private static final long serialVersionUID = 3801124242820219131L;

    private final boolean accessOrder;

    transient private LinkedHashMapEntry head, tail;

    /**
     * Constructs a new empty {@code LinkedHashMap} instance.
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
        head = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity.
     * 
     * @param s
     *            the initial capacity of this map.
     * @throws IllegalArgumentException
     *                if the capacity is less than zero.
     */
    public LinkedHashMap(int s) {
        super(s);
        accessOrder = false;
        head = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity and load factor.
     * 
     * @param s
     *            the initial capacity of this map.
     * @param lf
     *            the initial load factor.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(int s, float lf) {
        super(s, lf);
        accessOrder = false;
        head = null;
        tail = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance with the specified
     * capacity, load factor and a flag specifying the ordering behavior.
     * 
     * @param s
     *            the initial capacity of this hash map.
     * @param lf
     *            the initial load factor.
     * @param order
     *            {@code true} if the ordering should be done based on the last
     *            access (from least-recently accessed to most-recently
     *            accessed), and {@code false} if the ordering should be the
     *            order in which the entries were inserted.
     * @throws IllegalArgumentException
     *             when the capacity is less than zero or the load factor is
     *             less or equal to zero.
     */
    public LinkedHashMap(int s, float lf, boolean order) {
        super(s, lf);
        accessOrder = order;
        head = null;
        tail = null;
    }

    /**
     * Constructs a new {@code LinkedHashMap} instance containing the mappings
     * from the specified map. The order of the elements is preserved.
     * 
     * @param m
     *            the mappings to add.
     */
    public LinkedHashMap(Map m) {
        accessOrder = false;
        head = null;
        tail = null;
        putAll(m);
    }

    private static class AbstractMapIterator  {
        int expectedModCount;
        LinkedHashMapEntry  futureEntry;
        LinkedHashMapEntry  currentEntry;
        final LinkedHashMap associatedMap;

        AbstractMapIterator(LinkedHashMap map) {
            expectedModCount = map.modCount;
            futureEntry = map.head;
            associatedMap = map;
        }

        public boolean hasNext() {
            return (futureEntry != null);
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
            currentEntry = futureEntry;
            futureEntry = futureEntry.chainForward;
        }

        public void remove() {
            checkConcurrentMod();
            if (currentEntry==null) {
                throw new IllegalStateException();
            }
            associatedMap.removeEntry(currentEntry);
            LinkedHashMapEntry lhme =  currentEntry;
            LinkedHashMapEntry p = lhme.chainBackward;
            LinkedHashMapEntry n = lhme.chainForward;
            LinkedHashMap lhm = associatedMap;
            if (p != null) {
                p.chainForward = n;
                if (n != null) {
                    n.chainBackward = p;
                } else {
                    lhm.tail = p;
                }
            } else {
                lhm.head = n;
                if (n != null) {
                    n.chainBackward = null;
                } else {
                    lhm.tail = null;
                }
            }
            currentEntry = null;
            expectedModCount++;
        }
    }

    private static class EntryIterator extends AbstractMapIterator implements Iterator {

        EntryIterator (LinkedHashMap map) {
            super(map);
        }

        public Map.Entry next() {
            makeNext();
            return currentEntry;
        }
    }

    private static class KeyIterator extends AbstractMapIterator implements Iterator {

        KeyIterator (LinkedHashMap map) {
            super(map);
        }

        public Object next() {
            makeNext();
            return currentEntry.key;
        }
    }

    private static class ValueIterator extends AbstractMapIterator implements Iterator {

        ValueIterator (LinkedHashMap map) {
            super(map);
        }

        public Object next() {
            makeNext();
            return currentEntry.value;
        }
    }

    static final class LinkedHashMapEntrySet extends
            HashMapEntrySet {
        public LinkedHashMapEntrySet(LinkedHashMap lhm) {
            super(lhm);
        }

        public Iterator iterator() {
            return new EntryIterator((LinkedHashMap) hashMap());
        }
    }

    static final class LinkedHashMapEntry extends HashMap.Entry {
        LinkedHashMapEntry chainForward, chainBackward;

        LinkedHashMapEntry(Object theKey, Object theValue) {
            super(theKey, theValue);
            chainForward = null;
            chainBackward = null;
        }

        LinkedHashMapEntry(Object theKey, int hash) {
            super(theKey, hash);
            chainForward = null;
            chainBackward = null;
        }

        public Object clone() {
            LinkedHashMapEntry entry = (LinkedHashMapEntry) super
                    .clone();
            entry.chainBackward = chainBackward;
            entry.chainForward = chainForward;
            LinkedHashMapEntry lnext = (LinkedHashMapEntry) entry.next;
            if (lnext != null) {
                entry.next = (LinkedHashMapEntry) lnext.clone();
            }
            return entry;
        }
    }

    public boolean containsValue(Object value) {
        LinkedHashMapEntry entry = head;
        if (null == value) {
            while (null != entry) {
                if (null == entry.value) {
                    return true;
                }
                entry = entry.chainForward;
            }
        } else {
            while (null != entry) {
                if (value.equals(entry.value)) {
                    return true;
                }
                entry = entry.chainForward;
            }
        }
        return false;
    }

    /**
     * Create a new element array
     * 
     * @param s
     * @return Reference to the element array
     */
    HashMap.Entry[] newElementArray(int s) {
        return new LinkedHashMapEntry[s];
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
        LinkedHashMapEntry m;
        if (key == null) {
            m = (LinkedHashMapEntry) findNullKeyEntry();
        } else {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % elementData.length;
            m = (LinkedHashMapEntry) findNonNullKeyEntry(key, index, hash);
        }
        if (m == null) {
            return null;
        }
        if (accessOrder && tail != m) {
            LinkedHashMapEntry p = m.chainBackward;
            LinkedHashMapEntry n = m.chainForward;
            n.chainBackward = p;
            if (p != null) {
                p.chainForward = n;
            } else {
                head = n;
            }
            m.chainForward = null;
            m.chainBackward = tail;
            tail.chainForward = m;
            tail = m;
        }
        return m.value;
    }

    /*
     * @param key @param index @return Entry
     */
    HashMap.Entry createEntry(Object key, int index, Object value) {
        LinkedHashMapEntry m = new LinkedHashMapEntry(key, value);
        m.next = elementData[index];
        elementData[index] = m;
        linkEntry(m);
        return m;
    }

    HashMap.Entry createHashedEntry(Object key, int index, int hash) {
        LinkedHashMapEntry m = new LinkedHashMapEntry(key, hash);
        m.next = elementData[index];
        elementData[index] = m;
        linkEntry(m);
        return m;
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
        Object result = putImpl(key, value);

        if (removeEldestEntry(head)) {
            remove(head.key);
        }

        return result;
    }

    Object putImpl(Object key, Object value) {
        LinkedHashMapEntry m;
        if (elementCount == 0) {
            head = tail = null;
        }
        if (key == null) {
            m = (LinkedHashMapEntry) findNullKeyEntry();
            if (m == null) {
                modCount++;
                // Check if we need to remove the oldest entry. The check
                // includes accessOrder since an accessOrder LinkedHashMap does
                // not record the oldest member in 'head'.
                if (++elementCount > threshold) {
                    rehash();
                }
                m = (LinkedHashMapEntry) createHashedEntry(null, 0, 0);
            } else {
                linkEntry(m);
            }
        } else {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % elementData.length;
            m = (LinkedHashMapEntry) findNonNullKeyEntry(key, index, hash);
            if (m == null) {
                modCount++;
                if (++elementCount > threshold) {
                    rehash();
                    index = (hash & 0x7FFFFFFF) % elementData.length;
                }
                m = (LinkedHashMapEntry) createHashedEntry(key, index,
                        hash);
            } else {
                linkEntry(m);
            }
        }

        Object result = m.value;
        m.value = value;
        return result;
    }

    /*
     * @param m
     */
    void linkEntry(LinkedHashMapEntry m) {
        if (tail == m) {
            return;
        }

        if (head == null) {
            // Check if the map is empty
            head = tail = m;
            return;
        }

        // we need to link the new entry into either the head or tail
        // of the chain depending on if the LinkedHashMap is accessOrder or not
        LinkedHashMapEntry p = m.chainBackward;
        LinkedHashMapEntry n = m.chainForward;
        if (p == null) {
            if (n != null) {
                // The entry must be the head but not the tail
                if (accessOrder) {
                    head = n;
                    n.chainBackward = null;
                    m.chainBackward = tail;
                    m.chainForward = null;
                    tail.chainForward = m;
                    tail = m;
                }
            } else {
                // This is a new entry
                m.chainBackward = tail;
                m.chainForward = null;
                tail.chainForward = m;
                tail = m;
            }
            return;
        }

        if (n == null) {
            // The entry must be the tail so we can't get here
            return;
        }

        // The entry is neither the head nor tail
        if (accessOrder) {
            p.chainForward = n;
            n.chainBackward = p;
            m.chainForward = null;
            m.chainBackward = tail;
            tail.chainForward = m;
            tail = m;
        }
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     * 
     * @return a set of the mappings.
     */
    public Set entrySet() {
        return new LinkedHashMapEntrySet(this);
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
                    return LinkedHashMap.this.size();
                }

                public void clear() {
                    LinkedHashMap.this.clear();
                }

                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        LinkedHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }

                public Iterator iterator() {
                    return new KeyIterator(LinkedHashMap.this);
                }
            };
        }
        return keySet;
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The size method
     * wraps the map's size method and the contains method wraps the map's
     * containsValue method.
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
                    return LinkedHashMap.this.size();
                }

                public void clear() {
                    LinkedHashMap.this.clear();
                }

                public Iterator iterator() {
                    return new ValueIterator(LinkedHashMap.this);
                }
            };
        }
        return valuesCollection;
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
        LinkedHashMapEntry m = (LinkedHashMapEntry) removeEntry(key);
        if (m == null) {
            return null;
        }
        LinkedHashMapEntry p = m.chainBackward;
        LinkedHashMapEntry n = m.chainForward;
        if (p != null) {
            p.chainForward = n;
        } else {
            head = n;
        }
        if (n != null) {
            n.chainBackward = p;
        } else {
            tail = p;
        }
        return m.value;
    }

    /**
     * This method is queried from the put and putAll methods to check if the
     * eldest member of the map should be deleted before adding the new member.
     * If this map was created with accessOrder = true, then the result of
     * removeEldestEntry is assumed to be false.
     * 
     * @param eldest
     *            the entry to check if it should be removed.
     * @return {@code true} if the eldest member should be removed.
     */
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return false;
    }

    /**
     * Removes all elements from this map, leaving it empty.
     * 
     * @see #isEmpty()
     * @see #size()
     */
    public void clear() {
        super.clear();
        head = tail = null;
    }
}
