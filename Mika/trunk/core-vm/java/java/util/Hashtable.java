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
 * Hashtable associates keys with values. Both keys and values cannot be null.
 * The size of the Hashtable is the number of key/value pairs it contains. The
 * capacity is the number of key/value pairs the Hashtable can hold. The load
 * factor is a float value which determines how full the Hashtable gets before
 * expanding the capacity. If the load factor of the Hashtable is exceeded, the
 * capacity is doubled.
 * 
 * @see Enumeration
 * @see java.io.Serializable
 * @see java.lang.Object#equals
 * @see java.lang.Object#hashCode
 */

public class Hashtable extends Dictionary implements Map,
        Cloneable, Serializable {

    private static final long serialVersionUID = 1421746759512286392L;

    transient int elementCount;

    transient Entry[] elementData;

    private float loadFactor;

    private int threshold;

    transient int firstSlot;

    transient int lastSlot = -1;

    transient int modCount;

    private static final Enumeration EMPTY_ENUMERATION = new Enumeration() {
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException();
        }
    };

    private static final Iterator EMPTY_ITERATOR = new Iterator() {

        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new IllegalStateException();
        }
    };

    private static Entry newEntry(Object key, Object value, int hash) {
        return new Entry(key, value);
    }

    private static class Entry extends MapEntry {
        Entry next;

        final int hashcode;

        Entry(Object theKey, Object theValue) {
            super(theKey, theValue);
            hashcode = theKey.hashCode();
        }

        public Object clone() {
            Entry entry = (Entry) super.clone();
            if (next != null) {
                entry.next = (Entry) next.clone();
            }
            return entry;
        }

        public Object setValue(Object object) {
            if (object == null) {
                throw new NullPointerException();
            }
            Object result = value;
            value = object;
            return result;
        }

        public int getKeyHash() {
            return key.hashCode();
        }

        public boolean equalsKey(Object aKey, int hash) {
            return hashcode == aKey.hashCode() && key.equals(aKey);
        }

        public String toString() {
            return key + "=" + value; //$NON-NLS-1$
        }
    }

    private class HashIterator implements Iterator {
        int position, expectedModCount;

        final MapEntry.Type type;

        Entry lastEntry;

        int lastPosition;

        boolean canRemove = false;

        HashIterator(MapEntry.Type value) {
            type = value;
            position = lastSlot;
            expectedModCount = modCount;
        }

        public boolean hasNext() {
            if (lastEntry != null && lastEntry.next != null) {
                return true;
            }
            while (position >= firstSlot) {
                if (elementData[position] == null) {
                    position--;
                } else {
                    return true;
                }
            }
            return false;
        }

        public Object next() {
            if (expectedModCount == modCount) {
                if (lastEntry != null) {
                    lastEntry = lastEntry.next;
                }
                if (lastEntry == null) {
                    while (position >= firstSlot
                            && (lastEntry = elementData[position]) == null) {
                        position--;
                    }
                    if (lastEntry != null) {
                        lastPosition = position;
                        // decrement the position so we don't find the same slot
                        // next time
                        position--;
                    }
                }
                if (lastEntry != null) {
                    canRemove = true;
                    return (Entry)type.get(lastEntry);
                }
                throw new NoSuchElementException();
            }
            throw new ConcurrentModificationException();
        }

        public void remove() {
            if (expectedModCount == modCount) {
                if (canRemove) {
                    canRemove = false;
                    synchronized (Hashtable.this) {
                        boolean removed = false;
                        Entry entry = elementData[lastPosition];
                        if (entry == lastEntry) {
                            elementData[lastPosition] = entry.next;
                            removed = true;
                        } else {
                            while (entry != null && entry.next != lastEntry) {
                                entry = entry.next;
                            }
                            if (entry != null) {
                                entry.next = lastEntry.next;
                                removed = true;
                            }
                        }
                        if (removed) {
                            modCount++;
                            elementCount--;
                            expectedModCount++;
                            return;
                        }
                        // the entry must have been (re)moved outside of the
                        // iterator
                        // but this condition wasn't caught by the modCount
                        // check
                        // throw ConcurrentModificationException() outside of
                        // synchronized block
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Constructs a new {@code Hashtable} using the default capacity and load
     * factor.
     */
    public Hashtable() {
        this(11);
    }

    /**
     * Constructs a new {@code Hashtable} using the specified capacity and the
     * default load factor.
     * 
     * @param capacity
     *            the initial capacity.
     */
    public Hashtable(int capacity) {
        if (capacity >= 0) {
            elementCount = 0;
            elementData = newElementArray(capacity == 0 ? 1 : capacity);
            firstSlot = elementData.length;
            loadFactor = 0.75f;
            computeMaxSize();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new {@code Hashtable} using the specified capacity and load
     * factor.
     * 
     * @param capacity
     *            the initial capacity.
     * @param loadFactor
     *            the initial load factor.
     */
    public Hashtable(int capacity, float loadFactor) {
        if (capacity >= 0 && loadFactor > 0) {
            elementCount = 0;
            firstSlot = capacity;
            elementData = newElementArray(capacity == 0 ? 1 : capacity);
            this.loadFactor = loadFactor;
            computeMaxSize();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Constructs a new instance of {@code Hashtable} containing the mappings
     * from the specified map.
     * 
     * @param map
     *            the mappings to add.
     */
    public Hashtable(Map map) {
        this(map.size() < 6 ? 11 : (map.size() * 4 / 3) + 11);
        putAll(map);
    }

    private Entry[] newElementArray(int size) {
        return new Entry[size];
    }

    /**
     * Removes all key/value pairs from this {@code Hashtable}, leaving the
     * size zero and the capacity unchanged.
     * 
     * @see #isEmpty
     * @see #size
     */
    public synchronized void clear() {
        elementCount = 0;
        Arrays.fill(elementData, null);
        modCount++;
    }

    /**
     * Returns a new {@code Hashtable} with the same key/value pairs, capacity
     * and load factor.
     * 
     * @return a shallow copy of this {@code Hashtable}.
     * @see java.lang.Cloneable
     */
    public synchronized Object clone() {
        try {
            Hashtable hashtable = (Hashtable) super.clone();
            hashtable.elementData = new Entry[elementData.length];
            Entry entry;
            for (int i = elementData.length; --i >= 0;) {
                if ((entry = elementData[i]) != null) {
                    hashtable.elementData[i] = (Entry) entry.clone();
                }
            }
            return hashtable;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private void computeMaxSize() {
        threshold = (int) (elementData.length * loadFactor);
    }

    /**
     * Returns true if this {@code Hashtable} contains the specified object as
     * the value of at least one of the key/value pairs.
     * 
     * @param value
     *            the object to look for as a value in this {@code Hashtable}.
     * @return {@code true} if object is a value in this {@code Hashtable},
     *         {@code false} otherwise.
     * @see #containsKey
     * @see java.lang.Object#equals
     */
    public synchronized boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        for (int i = elementData.length; --i >= 0;) {
            Entry entry = elementData[i];
            while (entry != null) {
                if (entry.value.equals(value)) {
                    return true;
                }
                entry = entry.next;
            }
        }
        return false;
    }

    /**
     * Returns true if this {@code Hashtable} contains the specified object as a
     * key of one of the key/value pairs.
     * 
     * @param key
     *            the object to look for as a key in this {@code Hashtable}.
     * @return {@code true} if object is a key in this {@code Hashtable},
     *         {@code false} otherwise.
     * @see #contains
     * @see java.lang.Object#equals
     */
    public synchronized boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Searches this {@code Hashtable} for the specified value.
     * 
     * @param value
     *            the object to search for.
     * @return {@code true} if {@code value} is a value of this
     *         {@code Hashtable}, {@code false} otherwise.
     */
    public boolean containsValue(Object value) {
        return contains(value);
    }

    /**
     * Returns an enumeration on the values of this {@code Hashtable}. The
     * results of the Enumeration may be affected if the contents of this
     * {@code Hashtable} are modified.
     * 
     * @return an enumeration of the values of this {@code Hashtable}.
     * @see #keys
     * @see #size
     * @see Enumeration
     */
    public synchronized Enumeration elements() {
        if (elementCount == 0) {
            return (Enumeration) EMPTY_ENUMERATION;
        }
        return new HashEnumIterator(new MapEntry.Type() {
            public Object get(MapEntry entry) {
                return entry.value;
            }
        }, true);
    }

    /**
     * Returns a set of the mappings contained in this {@code Hashtable}. Each
     * element in the set is a {@link Map.Entry}. The set is backed by this
     * {@code Hashtable} so changes to one are reflected by the other. The set
     * does not support adding.
     * 
     * @return a set of the mappings.
     */
    public Set entrySet() {
        return new Collections.SynchronizedSet(
                new AbstractSet() {
                    public int size() {
                        return elementCount;
                    }

                    public void clear() {
                        Hashtable.this.clear();
                    }

                    public boolean remove(Object object) {
                        if (contains(object)) {
                            Hashtable.this.remove(((Map.Entry) object)
                                    .getKey());
                            return true;
                        }
                        return false;
                    }

                    public boolean contains(Object object) {
                        Entry entry = getEntry(((Map.Entry) object)
                                .getKey());
                        return object.equals(entry);
                    }

                    public Iterator iterator() {
                        return new HashIterator(
                                new MapEntry.Type() {
                                    public Object get(
                                            MapEntry entry) {
                                        return entry;
                                    }
                                });
                    }
                }, this);
    }

    /**
     * Compares this {@code Hashtable} with the specified object and indicates
     * if they are equal. In order to be equal, {@code object} must be an
     * instance of Map and contain the same key/value pairs.
     * 
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this Map,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    public synchronized boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            if (size() != map.size()) {
                return false;
            }

            Set entries = entrySet();
            Iterator entryIterator = map.entrySet().iterator();
            while (entryIterator.hasNext()) {
              Map.Entry e = (Map.Entry)entryIterator.next();
                if (!entries.contains(e)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value associated with the specified key in this
     * {@code Hashtable}.
     * 
     * @param key
     *            the key of the value returned.
     * @return the value associated with the specified key, or {@code null} if
     *         the specified key does not exist.
     * @see #put
     */
    public synchronized Object get(Object key) {
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementData.length;
        Entry entry = elementData[index];
        while (entry != null) {
            if (entry.equalsKey(key, hash)) {
                return entry.value;
            }
            entry = entry.next;
        }
        return null;
    }

    Entry getEntry(Object key) {
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementData.length;
        Entry entry = elementData[index];
        while (entry != null) {
            if (entry.equalsKey(key, hash)) {
                return entry;
            }
            entry = entry.next;
        }
        return null;
    }

    public synchronized int hashCode() {
        int result = 0;
        Iterator it = entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            if (key == this) {
                continue;
            }
            Object value = entry.getValue();
            if (value == this) {
                continue;
            }
            int hash = (key != null ? key.hashCode() : 0)
                    ^ (value != null ? value.hashCode() : 0);
            result += hash;
        }
        return result;
    }

    /**
     * Returns true if this {@code Hashtable} has no key/value pairs.
     * 
     * @return {@code true} if this {@code Hashtable} has no key/value pairs,
     *         {@code false} otherwise.
     * @see #size
     */
    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * Returns an enumeration on the keys of this {@code Hashtable} instance.
     * The results of the enumeration may be affected if the contents of this
     * {@code Hashtable} are modified.
     * 
     * @return an enumeration of the keys of this {@code Hashtable}.
     * @see #elements
     * @see #size
     * @see Enumeration
     */
    public synchronized Enumeration keys() {
        if (elementCount == 0) {
            return (Enumeration) EMPTY_ENUMERATION;
        }
        return new HashEnumIterator(new MapEntry.Type() {
            public Object get(MapEntry entry) {
                return entry.key;
            }
        }, true);
    }

    /**
     * Returns a set of the keys contained in this {@code Hashtable}. The set
     * is backed by this {@code Hashtable} so changes to one are reflected by
     * the other. The set does not support adding.
     * 
     * @return a set of the keys.
     */
    public Set keySet() {
        return new Collections.SynchronizedSet(new AbstractSet() {
            public boolean contains(Object object) {
                return containsKey(object);
            }

            public int size() {
                return elementCount;
            }

            public void clear() {
                Hashtable.this.clear();
            }

            public boolean remove(Object key) {
                if (containsKey(key)) {
                    Hashtable.this.remove(key);
                    return true;
                }
                return false;
            }

            public Iterator iterator() {
                if (this.size() == 0) {
                    return (Iterator) EMPTY_ITERATOR;
                }
                return new HashEnumIterator(new MapEntry.Type() {
                    public Object get(MapEntry entry) {
                        return entry.key;
                    }
                });
            }
        }, this);
    }

    class HashEnumIterator extends HashIterator implements Enumeration {

        private boolean isEnumeration = false;

        int start;

        Entry entry;

        HashEnumIterator(MapEntry.Type value) {
            super(value);
        }

        HashEnumIterator(MapEntry.Type value, boolean isEnumeration) {
            super(value);
            this.isEnumeration = isEnumeration;
            start = lastSlot + 1;
        }

        public boolean hasMoreElements() {
            if (isEnumeration) {
                if (entry != null) {
                    return true;
                }
                while (start > firstSlot) {
                    if (elementData[--start] != null) {
                        entry = elementData[start];
                        return true;
                    }
                }
                return false;
            }
            // iterator
            return super.hasNext();
        }

        public boolean hasNext() {
            if (isEnumeration) {
                return hasMoreElements();
            }
            // iterator
            return super.hasNext();
        }

        public Object next() {
            if (isEnumeration) {
                if (expectedModCount == modCount) {
                    return (Entry) nextElement();
                } else {
                    throw new ConcurrentModificationException();
                }
            }
            // iterator
            return super.next();
        }

        public Object nextElement() {
            if (isEnumeration) {
                if (hasMoreElements()) {
                    Object result = type.get(entry);
                    entry = entry.next;
                    return result;
                }
                throw new NoSuchElementException();
            }
            // iterator
            return super.next();
        }

        public void remove() {
            if (isEnumeration) {
                throw new UnsupportedOperationException();
            } else {
                super.remove();
            }
        }
    }

    /**
     * Associate the specified value with the specified key in this
     * {@code Hashtable}. If the key already exists, the old value is replaced.
     * The key and value cannot be null.
     * 
     * @param key
     *            the key to add.
     * @param value
     *            the value to add.
     * @return the old value associated with the specified key, or {@code null}
     *         if the key did not exist.
     * @see #elements
     * @see #get
     * @see #keys
     * @see java.lang.Object#equals
     */
    public synchronized Object put(Object key, Object value) {
        if (key != null && value != null) {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % elementData.length;
            Entry entry = elementData[index];
            while (entry != null && !entry.equalsKey(key, hash)) {
                entry = entry.next;
            }
            if (entry == null) {
                modCount++;
                if (++elementCount > threshold) {
                    rehash();
                    index = (hash & 0x7FFFFFFF) % elementData.length;
                }
                if (index < firstSlot) {
                    firstSlot = index;
                }
                if (index > lastSlot) {
                    lastSlot = index;
                }
                entry = newEntry(key, value, hash);
                entry.next = elementData[index];
                elementData[index] = entry;
                return null;
            }
            Object result = entry.value;
            entry.value = value;
            return result;
        }
        throw new NullPointerException();
    }

    /**
     * Copies every mapping to this {@code Hashtable} from the specified map.
     * 
     * @param map
     *            the map to copy mappings from.
     */
    public synchronized void putAll(Map map) {
      Iterator entryIterator = map.entrySet().iterator();
      while (entryIterator.hasNext()) {
        Map.Entry entry = (Map.Entry)entryIterator.next();
        put(entry.getKey(), entry.getValue());
      }
    }

    /**
     * Increases the capacity of this {@code Hashtable}. This method is called
     * when the size of this {@code Hashtable} exceeds the load factor.
     */
    protected void rehash() {
        int length = (elementData.length << 1) + 1;
        if (length == 0) {
            length = 1;
        }
        int newFirst = length;
        int newLast = -1;
        Entry[] newData = newElementArray(length);
        for (int i = lastSlot + 1; --i >= firstSlot;) {
            Entry entry = elementData[i];
            while (entry != null) {
                int index = (entry.getKeyHash() & 0x7FFFFFFF) % length;
                if (index < newFirst) {
                    newFirst = index;
                }
                if (index > newLast) {
                    newLast = index;
                }
                Entry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        firstSlot = newFirst;
        lastSlot = newLast;
        elementData = newData;
        computeMaxSize();
    }

    /**
     * Removes the key/value pair with the specified key from this
     * {@code Hashtable}.
     * 
     * @param key
     *            the key to remove.
     * @return the value associated with the specified key, or {@code null} if
     *         the specified key did not exist.
     * @see #get
     * @see #put
     */
    public synchronized Object remove(Object key) {
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % elementData.length;
        Entry last = null;
        Entry entry = elementData[index];
        while (entry != null && !entry.equalsKey(key, hash)) {
            last = entry;
            entry = entry.next;
        }
        if (entry != null) {
            modCount++;
            if (last == null) {
                elementData[index] = entry.next;
            } else {
                last.next = entry.next;
            }
            elementCount--;
            Object result = entry.value;
            entry.value = null;
            return result;
        }
        return null;
    }

    /**
     * Returns the number of key/value pairs in this {@code Hashtable}.
     * 
     * @return the number of key/value pairs in this {@code Hashtable}.
     * @see #elements
     * @see #keys
     */
    public synchronized int size() {
        return elementCount;
    }

    /**
     * Returns the string representation of this {@code Hashtable}.
     * 
     * @return the string representation of this {@code Hashtable}.
     */
    public synchronized String toString() {
        if (isEmpty()) {
            return "{}"; //$NON-NLS-1$
        }

        StringBuffer buffer = new StringBuffer(size() * 28);
        buffer.append('{');
        for (int i = lastSlot; i >= firstSlot; i--) {
            Entry entry = elementData[i];
            while (entry != null) {
                if (entry.key != this) {
                    buffer.append(entry.key);
                } else {
                    buffer.append("(this Map)");
                }
                buffer.append('=');
                if (entry.value != this) {
                    buffer.append(entry.value);
                } else {
                    buffer.append("(this Map)");
                }
                buffer.append(", ");
                entry = entry.next;
            }
        }
        // Remove the last ", "
        if (elementCount > 0) {
            buffer.setLength(buffer.length() - 2);
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * Returns a collection of the values contained in this {@code Hashtable}.
     * The collection is backed by this {@code Hashtable} so changes to one are
     * reflected by the other. The collection does not support adding.
     * 
     * @return a collection of the values.
     */
    public Collection values() {
        return new Collections.SynchronizedCollection(
                new AbstractCollection() {
                    public boolean contains(Object object) {
                        return Hashtable.this.contains(object);
                    }

                    public int size() {
                        return elementCount;
                    }

                    public void clear() {
                        Hashtable.this.clear();
                    }

                    public Iterator iterator() {
                        return new HashIterator(
                                new MapEntry.Type() {
                                    public Object get(MapEntry entry) {
                                        return entry.value;
                                    }
                                });
                    }
                }, this);
    }

    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(elementData.length);
        stream.writeInt(elementCount);
        for (int i = elementData.length; --i >= 0;) {
            Entry entry = elementData[i];
            while (entry != null) {
                stream.writeObject(entry.key);
                stream.writeObject(entry.value);
                entry = entry.next;
            }
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
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % length;
            if (index < firstSlot) {
                firstSlot = index;
            }
            if (index > lastSlot) {
                lastSlot = index;
            }
            Entry entry = newEntry(key, stream.readObject(), hash);
            entry.next = elementData[index];
            elementData[index] = entry;
        }
    }
}
