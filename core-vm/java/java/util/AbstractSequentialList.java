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

/**
 * AbstractSequentialList is an abstract implementation of the List interface.
 * This implementation does not support adding. A subclass must implement the
 * abstract method listIterator().
 * 
 * @since 1.2
 */
public abstract class AbstractSequentialList extends AbstractList {

    /**
     * Constructs a new instance of this AbstractSequentialList.
     */
    protected AbstractSequentialList() {
        super();
    }

    public void add(int location, Object object) {
        listIterator(location).add(object);
    }

    public boolean addAll(int location, Collection collection) {
        ListIterator it = listIterator(location);
        Iterator colIt = collection.iterator();
        int next = it.nextIndex();
        while (colIt.hasNext()) {
            it.add(colIt.next());
        }
        return next != it.nextIndex();
    }

    public Object get(int location) {
        try {
            return listIterator(location).next();
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public Iterator iterator() {
        return listIterator(0);
    }

    public abstract ListIterator listIterator(int location);

    public Object remove(int location) {
        try {
            ListIterator it = listIterator(location);
            Object result = it.next();
            it.remove();
            return result;
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    public Object set(int location, Object object) {
        ListIterator it = listIterator(location);
        if (!it.hasNext()) {
            throw new IndexOutOfBoundsException();
        }
        Object result = it.next();
        it.set(object);
        return result;
    }
}
