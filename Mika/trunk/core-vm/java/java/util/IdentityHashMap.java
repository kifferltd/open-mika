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
 * Imported by CG 20101225 based on Apache Harmony ("enhanced") revision 1043880.
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * IdentityHashMap is a variant on HashMap which tests equality by reference
 * instead of equality by value. Basically, keys and values are compared for
 * equality by checking if their references are equal rather than by calling the
 * "equals" function.
 * <p>
 * <b>Note: This class intentionally violates the general contract of {@code
 * Map}'s on comparing objects by their {@code equals} method.</b>
 * <p>
 * IdentityHashMap uses open addressing (linear probing in particular) for
 * collision resolution. This is different from HashMap which uses Chaining.
 * <p>
 * Like HashMap, IdentityHashMap is not thread safe, so access by multiple
 * threads must be synchronized by an external mechanism such as
 * Collections.synchronizedMap.
 * 
 * @since 1.4
 */
public class IdentityHashMap extends AbstractMap implements
        Map, Serializable, Cloneable {

    private static final long serialVersionUID = 8188218128353913216L;

    /*
     * The internal data structure to hold key value pairs This array holds keys
     * and values in an alternating fashion.
     */
    transient Object[] elementData;

    /* Actual number of key-value pairs. */
    int size;

    /*
     * maximum number of elements that can be put in this map before having to
     * rehash.
     */
    transient int threshold;

    /*
     * default threshold value that an IdentityHashMap created using the default
     * constructor would have.
     */
    private static final int DEFAULT_MAX_SIZE = 21;

    /* Default load factor of 0.75; */
    private static final int loadFactor = 7500;

    /*
     * modification count, to keep track of structural modifications between the
     * IdentityHashMap and the iterator
     */
    transient int modCount = 0;

    /*
     * Object used to represent null keys and values. This is used to
     * differentiate a literal 'null' key value pair from an empty spot in the
     * map.
     */
    private static final Object NULL_OBJECT = new Object();  //$NON-LOCK-1$

    static class IdentityHashMapEntry extends MapEntry {

        final Object iKey;

        final Object[] elementData;

        IdentityHashMapEntry(Object theKey, Object theValue, Object[] elementData) {
            super(theKey == NULL_OBJECT ? null : theKey,
                    theValue == NULL_OBJECT ? null : theValue);
            iKey = theKey;
            this.elementData = elementData;
        }

        public Object clone() {
            return super.clone();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) object;
                return (key == entry.getKey()) && (value == entry.getValue());
            }
            return false;
        }

        public int hashCode() {
            return System.identityHashCode(key)
                    ^ System.identityHashCode(value);
        }

        public String toString() {
            return key + "=" + value; //$NON-NLS-1$
        }

        public Object setValue(Object object) {
            int index = findIndex(iKey, elementData);
            if (elementData[index] == key) {
                elementData[index + 1] = object;
            }
            return super.setValue(object);
        }
    }

    static class IdentityHashMapIterator implements Iterator {
        private int position = 0; // the current position

        // the position of the entry that was last returned from next()
        private int lastPosition = 0;

        final IdentityHashMap associatedMap;

        int expectedModCount;

        final MapEntry.Type type;

        boolean canRemove = false;

        IdentityHashMapIterator(MapEntry.Type value,
                IdentityHashMap hm) {
            associatedMap = hm;
            type = value;
            expectedModCount = hm.modCount;
        }

        public boolean hasNext() {
            while (position < associatedMap.elementData.length) {
                // if this is an empty spot, go to the next one
                if (associatedMap.elementData[position] == null) {
                    position += 2;
                } else {
                    return true;
                }
            }
            return false;
        }

        void checkConcurrentMod() throws ConcurrentModificationException {
            if (expectedModCount != associatedMap.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        public Object next() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            IdentityHashMapEntry result = associatedMap
                    .getEntry(position);
            lastPosition = position;
            position += 2;

            canRemove = true;
            return type.get(result);
        }

        public void remove() {
            checkConcurrentMod();
            if (!canRemove) {
                throw new IllegalStateException();
            }

            canRemove = false;
            associatedMap.remove(associatedMap.elementData[lastPosition]);
            position = lastPosition;
            expectedModCount++;
        }
    }

    static class IdentityHashMapEntrySet extends AbstractSet {
        private final IdentityHashMap associatedMap;

        public IdentityHashMapEntrySet(IdentityHashMap hm) {
            associatedMap = hm;
        }

        IdentityHashMap hashMap() {
            return associatedMap;
        }

        public int size() {
            return associatedMap.size;
        }

        public void clear() {
            associatedMap.clear();
        }

        public boolean remove(Object object) {
            if (contains(object)) {
                associatedMap.remove(((Map.Entry) object).getKey());
                return true;
            }
            return false;
        }

        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                IdentityHashMapEntry entry = associatedMap
                        .getEntry(((Map.Entry) object).getKey());
                // we must call equals on the entry obtained from "this"
                return entry != null && entry.equals(object);
            }
            return false;
        }

        public Iterator iterator() {
            return new IdentityHashMapIterator(
                    new MapEntry.Type() {
                        public Map.Entry get(MapEntry entry) {
                            return entry;
                        }
                    }, associatedMap);
        }
    }

    /**
     * Creates an IdentityHashMap with default expected maximum size.
     */
    public IdentityHashMap() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Creates an IdentityHashMap with the specified maximum size parameter.
     * 
     * @param maxSize
     *            The estimated maximum number of entries that will be put in
     *            this map.
     */
    public IdentityHashMap(int maxSize) {
        if (maxSize >= 0) {
            this.size = 0;
            threshold = getThreshold(maxSize);
            elementData = newElementArray(computeElementArraySize());
        } else {
            throw new IllegalArgumentException();
        }
    }

    private int getThreshold(int maxSize) {
        // assign the threshold to maxSize initially, this will change to a
        // higher value if rehashing occurs.
        return maxSize > 3 ? maxSize : 3;
    }

    private int computeElementArraySize() {
        int arraySize = (int) (((long) threshold * 10000) / loadFactor) * 2;
        // ensure arraySize is positive, the above cast from long to int type
        // leads to overflow and negative arraySize if threshold is too big
        return arraySize < 0 ? -arraySize : arraySize;
    }

    /**
     * Create a new element array
     * 
     * @param s
     *            the number of elements
     * @return Reference to the element array
     */
    private Object[] newElementArray(int s) {
        return new Object[s];
    }

    /**
     * Creates an IdentityHashMap using the given map as initial values.
     * 
     * @param map
     *            A map of (key,value) pairs to copy into the IdentityHashMap.
     */
    public IdentityHashMap(Map map) {
        this(map.size() < 6 ? 11 : map.size() * 2);
        putAllImpl(map);
    }

    private Object massageValue(Object value) {
        return ((value == NULL_OBJECT) ? null : value);
    }

    /**
     * Removes all elements from this map, leaving it empty.
     * 
     * @see #isEmpty()
     * @see #size()
     */
    public void clear() {
        size = 0;
        for (int i = 0; i < elementData.length; i++) {
            elementData[i] = null;
        }
        modCount++;
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
        if (key == null) {
            key = NULL_OBJECT;
        }

        int index = findIndex(key, elementData);
        return elementData[index] == key;
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
        if (value == null) {
            value = NULL_OBJECT;
        }

        for (int i = 1; i < elementData.length; i = i + 2) {
            if (elementData[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of the mapping with the specified key.
     * 
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key.
     */
    public Object get(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }

        int index = findIndex(key, elementData);

        if (elementData[index] == key) {
            Object result = elementData[index + 1];
            return massageValue(result);
        }

        return null;
    }

    private IdentityHashMapEntry getEntry(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }

        int index = findIndex(key, elementData);
        if (elementData[index] == key) {
            return getEntry(index);
        }

        return null;
    }

    /**
     * Convenience method for getting the IdentityHashMapEntry without the
     * NULL_OBJECT elements
     */
    private IdentityHashMapEntry getEntry(int index) {
        return new IdentityHashMapEntry(elementData[index],
                elementData[index + 1], elementData);
    }

    /**
     * Returns the index where the key is found at, or the index of the next
     * empty spot if the key is not found in this table.
     */
    private static int findIndex(Object key, Object[] array) {
        int length = array.length;
        int index = getModuloHash(key, length);
        int last = (index + length - 2) % length;
        while (index != last) {
            if (array[index] == key || (array[index] == null)) {
                /*
                 * Found the key, or the next empty spot (which means key is not
                 * in the table)
                 */
                break;
            }
            index = (index + 2) % length;
        }
        return index;
    }

    private static int getModuloHash(Object key, int length) {
        return ((System.identityHashCode(key) & 0x7FFFFFFF) % (length / 2)) * 2;
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
        Object _key = key;
        Object _value = value;
        if (_key == null) {
            _key = NULL_OBJECT;
        }

        if (_value == null) {
            _value = NULL_OBJECT;
        }

        int index = findIndex(_key, elementData);

        // if the key doesn't exist in the table
        if (elementData[index] != _key) {
            modCount++;
            if (++size > threshold) {
                rehash();
                index = findIndex(_key, elementData);
            }

            // insert the key and assign the value to null initially
            elementData[index] = _key;
            elementData[index + 1] = null;
        }

        // insert value to where it needs to go, return the old value
        Object result = elementData[index + 1];
        elementData[index + 1] = _value;

        return massageValue(result);
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
        putAllImpl(map);
    }

    private void rehash() {
        int newlength = elementData.length << 1;
        if (newlength == 0) {
            newlength = 1;
        }
        Object[] newData = newElementArray(newlength);
        for (int i = 0; i < elementData.length; i = i + 2) {
            Object key = elementData[i];
            if (key != null) {
                // if not empty
                int index = findIndex(key, newData);
                newData[index] = key;
                newData[index + 1] = elementData[i + 1];
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    private void computeMaxSize() {
        threshold = (int) ((long) (elementData.length / 2) * loadFactor / 10000);
    }

    /**
     * Removes the mapping with the specified key from this map.
     * 
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping, or {@code null} if no mapping
     *         for the specified key was found.
     */
    public Object remove(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }

        boolean hashedOk;
        int index, next, hash;
        Object result, object;
        index = next = findIndex(key, elementData);

        if (elementData[index] != key) {
            return null;
        }

        // store the value for this key
        result = elementData[index + 1];

        // shift the following elements up if needed
        // until we reach an empty spot
        int length = elementData.length;
        while (true) {
            next = (next + 2) % length;
            object = elementData[next];
            if (object == null) {
                break;
            }

            hash = getModuloHash(object, length);
            hashedOk = hash > index;
            if (next < index) {
                hashedOk = hashedOk || (hash <= next);
            } else {
                hashedOk = hashedOk && (hash <= next);
            }
            if (!hashedOk) {
                elementData[index] = object;
                elementData[index + 1] = elementData[next + 1];
                index = next;
            }
        }

        size--;
        modCount++;

        // clear both the key and the value
        elementData[index] = null;
        elementData[index + 1] = null;

        return massageValue(result);
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     * 
     * @return a set of the mappings.
     */
    public Set entrySet() {
        return new IdentityHashMapEntrySet(this);
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
                    return IdentityHashMap.this.size();
                }

                public void clear() {
                    IdentityHashMap.this.clear();
                }

                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        IdentityHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }

                public Iterator iterator() {
                    return new IdentityHashMapIterator(
                            new MapEntry.Type() {
                                public Object get(MapEntry entry) {
                                    return entry.key;
                                }
                            }, IdentityHashMap.this);
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
                    return IdentityHashMap.this.size();
                }

                public void clear() {
                    IdentityHashMap.this.clear();
                }

                public Iterator iterator() {
                    return new IdentityHashMapIterator(
                            new MapEntry.Type() {
                                public Object get(MapEntry entry) {
                                    return entry.value;
                                }
                            }, IdentityHashMap.this);
                }

                public boolean remove(Object object) {
                    Iterator it = iterator();
                    while (it.hasNext()) {
                        if (object == it.next()) {
                            it.remove();
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
        return valuesCollection;
    }

    /**
     * Compares this map with other objects. This map is equal to another map is
     * it represents the same set of mappings. With this map, two mappings are
     * the same if both the key and the value are equal by reference. When
     * compared with a map that is not an IdentityHashMap, the equals method is
     * neither necessarily symmetric (a.equals(b) implies b.equals(a)) nor
     * transitive (a.equals(b) and b.equals(c) implies a.equals(c)).
     * 
     * @param object
     *            the object to compare to.
     * @return whether the argument object is equal to this object.
     */
    public boolean equals(Object object) {
        /*
         * We need to override the equals method in AbstractMap because
         * AbstractMap.equals will call ((Map) object).entrySet().contains() to
         * determine equality of the entries, so it will defer to the argument
         * for comparison, meaning that reference-based comparison will not take
         * place. We must ensure that all comparison is implemented by methods
         * in this class (or in one of our inner classes) for reference-based
         * comparison to take place.
         */
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            if (size() != map.size()) {
                return false;
            }

            Set set = entrySet();
            // ensure we use the equals method of the set created by "this"
            return set.equals(map.entrySet());
        }
        return false;
    }

    /**
     * Returns a new IdentityHashMap with the same mappings and size as this
     * one.
     * 
     * @return a shallow copy of this IdentityHashMap.
     * @see java.lang.Cloneable
     */
    public Object clone() {
        try {
            IdentityHashMap cloneHashMap = (IdentityHashMap) super
                    .clone();
            cloneHashMap.elementData = newElementArray(elementData.length);
            System.arraycopy(elementData, 0, cloneHashMap.elementData, 0,
                    elementData.length);
            return cloneHashMap;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns whether this IdentityHashMap has no elements.
     * 
     * @return {@code true} if this IdentityHashMap has no elements,
     *         {@code false} otherwise.
     * @see #size()
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of mappings in this IdentityHashMap.
     * 
     * @return the number of mappings in this IdentityHashMap.
     */
    public int size() {
        return size;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(size);
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            MapEntry entry = (MapEntry) iterator.next();
            stream.writeObject(entry.key);
            stream.writeObject(entry.value);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int savedSize = stream.readInt();
        threshold = getThreshold(DEFAULT_MAX_SIZE);
        elementData = newElementArray(computeElementArraySize());
        for (int i = savedSize; --i >= 0;) {
            Object key = stream.readObject();
            put(key, stream.readObject());
        }
        size = savedSize;
    }
    
    private void putAllImpl(Map map) {
        if (map.entrySet() != null) {
            super.putAll(map);
        }
    }
}
