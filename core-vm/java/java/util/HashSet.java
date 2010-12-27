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
 * Imported by CG 20101225 based on Apache Harmony ("enhanced") revision 929253.
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * HashSet is an implementation of a Set. All optional operations (adding and
 * removing) are supported. The elements can be any objects.
 */
public class HashSet extends AbstractSet implements Set, Cloneable,
        Serializable {

    private static final long serialVersionUID = -5024744406713321676L;

    transient HashMap backingMap;

    /**
     * Constructs a new empty instance of {@code HashSet}.
     */
    public HashSet() {
        this(new HashMap());
    }

    /**
     * Constructs a new instance of {@code HashSet} with the specified capacity.
     * 
     * @param capacity
     *            the initial capacity of this {@code HashSet}.
     */
    public HashSet(int capacity) {
        this(new HashMap(capacity));
    }

    /**
     * Constructs a new instance of {@code HashSet} with the specified capacity
     * and load factor.
     * 
     * @param capacity
     *            the initial capacity.
     * @param loadFactor
     *            the initial load factor.
     */
    public HashSet(int capacity, float loadFactor) {
        this(new HashMap(capacity, loadFactor));
    }

    /**
     * Constructs a new instance of {@code HashSet} containing the unique
     * elements in the specified collection.
     * 
     * @param collection
     *            the collection of elements to add.
     */
    public HashSet(Collection collection) {
        this(new HashMap(collection.size() < 6 ? 11 : collection
                .size() * 2));
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Object e = iter.next();
            add(e);
        }
    }

    HashSet(HashMap backingMap) {
        this.backingMap = backingMap;
    }

    /**
     * Adds the specified object to this {@code HashSet} if not already present.
     * 
     * @param object
     *            the object to add.
     * @return {@code true} when this {@code HashSet} did not already contain
     *         the object, {@code false} otherwise
     */
    public boolean add(Object object) {
        return backingMap.put(object, this) == null;
    }

    /**
     * Removes all elements from this {@code HashSet}, leaving it empty.
     * 
     * @see #isEmpty
     * @see #size
     */
    public void clear() {
        backingMap.clear();
    }

    /**
     * Returns a new {@code HashSet} with the same elements and size as this
     * {@code HashSet}.
     * 
     * @return a shallow copy of this {@code HashSet}.
     * @see java.lang.Cloneable
     */
    public Object clone() {
        try {
            HashSet clone = (HashSet) super.clone();
            clone.backingMap = (HashMap) backingMap.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Searches this {@code HashSet} for the specified object.
     * 
     * @param object
     *            the object to search for.
     * @return {@code true} if {@code object} is an element of this
     *         {@code HashSet}, {@code false} otherwise.
     */
    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }

    /**
     * Returns true if this {@code HashSet} has no elements, false otherwise.
     * 
     * @return {@code true} if this {@code HashSet} has no elements,
     *         {@code false} otherwise.
     * @see #size
     */
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    /**
     * Returns an Iterator on the elements of this {@code HashSet}.
     * 
     * @return an Iterator on the elements of this {@code HashSet}.
     * @see Iterator
     */
    public Iterator iterator() {
        return backingMap.keySet().iterator();
    }

    /**
     * Removes the specified object from this {@code HashSet}.
     * 
     * @param object
     *            the object to remove.
     * @return {@code true} if the object was removed, {@code false} otherwise.
     */
    public boolean remove(Object object) {
        return backingMap.remove(object) != null;
    }

    /**
     * Returns the number of elements in this {@code HashSet}.
     * 
     * @return the number of elements in this {@code HashSet}.
     */
    public int size() {
        return backingMap.size();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(backingMap.elementData.length);
        stream.writeFloat(backingMap.loadFactor);
        stream.writeInt(backingMap.elementCount);
        for (int i = backingMap.elementData.length; --i >= 0;) {
            HashMap.Entry entry = backingMap.elementData[i];
            while (entry != null) {
                stream.writeObject(entry.key);
                entry = entry.next;
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int length = stream.readInt();
        float loadFactor = stream.readFloat();
        backingMap = createBackingMap(length, loadFactor);
        int elementCount = stream.readInt();
        for (int i = elementCount; --i >= 0;) {
            Object key = stream.readObject();
            backingMap.put(key, this);
        }
    }

    HashMap createBackingMap(int capacity, float loadFactor) {
        return new HashMap(capacity, loadFactor);
    }
}
