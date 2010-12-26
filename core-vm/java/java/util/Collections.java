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
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Map.Entry;

/**
 * {@code Collections} contains static methods which operate on
 * {@code Collection} classes.
 *
 * @since 1.2
 */
public class Collections {


  /**
   * UnmodEntry:
   * 
   * @author ruelens created: Feb 5, 2007
   */
  public static class UnmodEntry implements Map.Entry {

    private Entry entry;

    public UnmodEntry(Entry entry) {
      this.entry = entry;
    }

    public Object getKey() {
      return entry.getKey();
    }

    public Object getValue() {
      return entry.getValue();
    }

    public boolean equals(Object obj) {
      return entry.equals(obj);
    }

    public int hashCode() {
      return entry.hashCode();
    }

    public Object setValue(Object value) throws UnsupportedOperationException,
        ClassCastException, IllegalArgumentException, NullPointerException {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * EntryIterator:
   * 
   * @author ruelens created: Feb 5, 2007
  public static class EntryIterator implements Iterator {

    private Iterator iterator;

    public EntryIterator(Iterator iterator) {
      this.iterator = iterator;
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    /.
     . (non-Javadoc)
     . 
     . @see java.util.Iterator#next()
     ./
    public Object next() throws NoSuchElementException,
        ConcurrentModificationException {
      return new UnmodEntry((Map.Entry) iterator.next());
    }

    public void remove() throws UnsupportedOperationException,
        IllegalStateException, ConcurrentModificationException {
      throw new UnsupportedOperationException();
    }

  }
   */

    private static final class CopiesList extends AbstractList implements Serializable {
      private static final long serialVersionUID = 2739099268398711800L;

      private final int n;

      private final Object element;

      CopiesList(int length, Object object) {
        if (length < 0) {
          throw new IllegalArgumentException();
        }
      n = length;
      element = object;
    }

    public boolean contains(Object object) {
      return element == null ? object == null : element.equals(object);
    }

    public int size() {
      return n;
    }

    public Object get(int location) {
      if (0 <= location && location < n) {
        return element;
      }
      throw new IndexOutOfBoundsException();
    }
  }

  private static final class EmptyList extends AbstractList implements RandomAccess, Serializable {
    private static final long serialVersionUID = 8842843931221139166L;

    public boolean contains(Object object) {
      return false;
    }

    public int size() {
      return 0;
    }

    public Object get(int location) {
      throw new IndexOutOfBoundsException();
    }

    private Object readResolve() {
      return Collections.EMPTY_LIST;
    }
  }

  private static final class EmptySet extends AbstractSet implements Serializable {
    private static final long serialVersionUID = 1582296315990362920L;

    public boolean contains(Object object) {
      return false;
    }

    public int size() {
      return 0;
    }

    public Iterator iterator() {
      return new Iterator() {
        public boolean hasNext() {
          return false;
        }

        public Object next() {
           throw new NoSuchElementException();
        }

        public void remove() {
           throw new UnsupportedOperationException();
        }
      };
    }

    private Object readResolve() {
      return Collections.EMPTY_SET;
    }
  }

  private static final class EmptyMap extends AbstractMap implements Serializable {
    private static final long serialVersionUID = 6428348081105594320L;

    public boolean containsKey(Object key) {
      return false;
    }

    public boolean containsValue(Object value) {
      return false;
    }

    public Set entrySet() {
      return EMPTY_SET;
    }

    public Object get(Object key) {
      return null;
    }

    public Set keySet() {
      return EMPTY_SET;
    }

    public Collection values() {
      return EMPTY_LIST;
    }

    private Object readResolve() {
      return Collections.EMPTY_MAP;
    }
  }
 
  /**
   * An empty immutable instance of {@link List}.
   */
  public static final List EMPTY_LIST = new EmptyList();

  /**
   * An empty immutable instance of {@link Set}.
   */
  public static final Set EMPTY_SET = new EmptySet();

  /**
   * An empty immutable instance of {@link Map}.
   */
  public static final Map EMPTY_MAP = new EmptyMap();

  /**
   * This class is a singleton so that equals() and hashCode() work properly.
   */
  private static final class ReverseComparator implements Comparator, Serializable {

    private static final ReverseComparator INSTANCE
                = new ReverseComparator();

    private static final long serialVersionUID = 7207038068494060240L;

    public int compare(Object o1, Object o2) {
      Comparable c2 = (Comparable) o2;
      return c2.compareTo(o1);
    }

    private Object readResolve() throws ObjectStreamException {
      return INSTANCE;
    }
  }

  private static final class ReverseComparatorWithComparator implements Comparator, Serializable {
    private static final long serialVersionUID = 4374092139857L;

    private final Comparator comparator;

    ReverseComparatorWithComparator(Comparator comparator) {
      super();
      this.comparator = comparator;
    }

    public int compare(Object o1, Object o2) {
      return comparator.compare(o2, o1);
    }

    public boolean equals(Object o) {
      return o instanceof ReverseComparatorWithComparator
            && ((ReverseComparatorWithComparator) o).comparator
                  .equals(comparator);
    }

    public int hashCode() {
      return ~comparator.hashCode();
    }
  }

 
  private static final class SingletonSet extends AbstractSet implements
            Serializable {
    private static final long serialVersionUID = 3193687207550431679L;

    final Object element;

    SingletonSet(Object object) {
      element = object;
    }

    public boolean contains(Object object) {
      return element == null ? object == null : element.equals(object);
    }

    public int size() {
      return 1;
    }

    public Iterator iterator() {
      return new Iterator() {
        boolean hasNext = true;

        public boolean hasNext() {
          return hasNext;
        }

        public Object next() {
          if (hasNext) {
            hasNext = false;
            return element;
          }
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private static final class SingletonList extends AbstractList
            implements Serializable {
      private static final long serialVersionUID = 3093736618740652951L;

    final Object element;

    SingletonList(Object object) {
      element = object;
    }

    public boolean contains(Object object) {
      return element == null ? object == null : element.equals(object);
    }

    public Object get(int location) {
      if (location == 0) {
        return element;
      }
      throw new IndexOutOfBoundsException();
    }

    public int size() {
      return 1;
    }
  }

  private static final class SingletonMap extends AbstractMap implements Serializable {
      private static final long serialVersionUID = -6979724477215052911L;

      final Object k;

      final Object v;

      SingletonMap(Object key, Object value) {
        k = key;
        v = value;
      }

      public boolean containsKey(Object key) {
        return k == null ? key == null : k.equals(key);
      }

      public boolean containsValue(Object value) {
        return v == null ? value == null : v.equals(value);
      }

      public Object get(Object key) {
        if (containsKey(key)) {
          return v;
        }
        return null;
      }

      public int size() {
        return 1;
      }

      public Set entrySet() {
          return new AbstractSet() {
              public boolean contains(Object object) {
                  if (object instanceof Map.Entry) {
                      Map.Entry entry = (Map.Entry) object;
                      return containsKey(entry.getKey())
                              && containsValue(entry.getValue());
                  }
                  return false;
              }

              public int size() {
                  return 1;
              }

              public Iterator iterator() {
                  return new Iterator() {
                      boolean hasNext = true;

                      public boolean hasNext() {
                          return hasNext;
                      }

                      public Map.Entry next() {
                          if (!hasNext) {
                              throw new NoSuchElementException();
                          }

                          hasNext = false;
                          return new MapEntry(k, v) {
                              public Object setValue(Object value) {
                                  throw new UnsupportedOperationException();
                              }
                          };
                      }

                      public void remove() {
                          throw new UnsupportedOperationException();
                      }
                  };
              }
          };
      }
  }

  static class SynchronizedCollection implements Collection, Serializable {

    private static final long serialVersionUID = 3053995032091335093L;

    final Collection back;

    final Object mutex;

    SynchronizedCollection(Collection c) {
      back = c;
      mutex = this;
    }

    SynchronizedCollection(Collection c, Object mutex) {
      back = c;
      this.mutex = mutex;
    }

    public boolean add(Object e) {
      synchronized(mutex) {
        return back.add(e);
      }
    }

    public boolean addAll(Collection e) {
      synchronized(mutex) {
        return back.addAll(e);
      }
    }

    public void clear() {
      synchronized(mutex) {
        back.clear();
      }
    }

    public boolean contains(Object e) {
      synchronized(mutex) {
        return back.contains(e);
      }
    }

    public boolean containsAll(Collection e) {
      synchronized(mutex) {
        return back.containsAll(e);
      }
    }

    public boolean isEmpty() {
      synchronized(mutex) {
        return back.isEmpty();
      }
    }

    public Iterator iterator() {
      synchronized(mutex) {
        return back.iterator();
      }
    }

    public boolean remove(Object e) {
      synchronized(mutex) {
        return back.remove(e);
      }
    }

    public boolean removeAll(Collection c) {
      synchronized(mutex) {
        return back.removeAll(c);
      }
    }

    public boolean retainAll(Collection c) {
      synchronized(mutex) {
        return back.retainAll(c);
      }
    }

    public int size() {
      synchronized(mutex) {
        return back.size();
      }
    }

    public Object[] toArray() {
      synchronized(mutex) {
        return back.toArray();
      }
    }

    public Object[] toArray(Object[] a) {
      synchronized(mutex) {
        return back.toArray(a);
      }
    }

    public String toString() {
      synchronized(mutex) {
        return back.toString();
      }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
      synchronized (mutex) {
        stream.defaultWriteObject();
      }
    }
  }

  static class SynchronizedRandomAccessList extends SynchronizedList implements RandomAccess {
    private static final long serialVersionUID = 1530674583602358482L;

    SynchronizedRandomAccessList(List l) {
        super(l);
    }

    SynchronizedRandomAccessList(List l, Object mutex) {
        super(l, mutex);
    }

    public List subList(int start, int end) {
        synchronized (mutex) {
            return new SynchronizedRandomAccessList(list.subList(start, end), mutex);
        }
    }

    /**
     * Replaces this SynchronizedRandomAccessList with a SynchronizedList so
     * that JREs before 1.4 can deserialize this object without any
     * problems. This is necessary since RandomAccess API was introduced
     * only in 1.4.
     * <p>
     * 
     * @return SynchronizedList
     * 
     * @see SynchronizedList#readResolve()
     */
    private Object writeReplace() {
      return new SynchronizedList(list);
    }
  }

  static class SynchronizedList extends SynchronizedCollection implements List {
    private static final long serialVersionUID = -7754090372962971524L;

    final List list;

    SynchronizedList(List l) {
      super(l);
      list = l;
    }

    SynchronizedList(List l, Object mutex) {
      super(l, mutex);
      list = l;
    }

    public void add(int location, Object object) {
      synchronized (mutex) {
        list.add(location, object);
      }
    }

    public boolean addAll(int location, Collection collection) {
      synchronized (mutex) {
        return list.addAll(location, collection);
      }
    }

    public boolean equals(Object object) {
      synchronized (mutex) {
        return list.equals(object);
      }
    }

    public Object get(int location) {
      synchronized (mutex) {
        return list.get(location);
      }
    }

    public int hashCode() {
      synchronized (mutex) {
        return list.hashCode();
      }
    }

    public int indexOf(Object object) {
      final int size;
      final Object[] array;
      synchronized (mutex) {
        size = list.size();
        array = new Object[size];
        list.toArray(array);
      }
      if (null != object) {
        for (int i = 0; i < size; i++) {
          if (object.equals(array[i])) {
            return i;
          }
        }
      }
      else {
        for (int i = 0; i < size; i++) {
          if (null == array[i]) {
            return i;
          }
        }
      }
      return -1;
    }

    public int lastIndexOf(Object object) {
      final int size;
      final Object[] array;
      synchronized (mutex) {
        size = list.size();
        array = new Object[size];
        list.toArray(array);
       }
       if (null != object) {
         for (int i = size - 1; i >= 0; i--) {
           if (object.equals(array[i])) {
             return i;
           }
         }
      }
      else {
        for (int i = size - 1; i >= 0; i--) {
          if (null == array[i]) {
            return i;
          }
        }
      }
      return -1;
    }

    public ListIterator listIterator() {
            synchronized (mutex) {
                return list.listIterator();
            }
    }

    public ListIterator listIterator(int location) {
            synchronized (mutex) {
                return list.listIterator(location);
            }
    }

    public Object remove(int location) {
            synchronized (mutex) {
                return list.remove(location);
            }
    }

    public Object set(int location, Object object) {
            synchronized (mutex) {
                return list.set(location, object);
            }
    }

    public List subList(int start, int end) {
            synchronized (mutex) {
                return new SynchronizedList(list.subList(start, end), mutex);
            }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
    }

    /**
     * Resolves SynchronizedList instances to SynchronizedRandomAccessList
     * instances if the underlying list is a Random Access list.
     * <p>
     * This is necessary since SynchronizedRandomAccessList instances are
     * replaced with SynchronizedList instances during serialization for
     * compliance with JREs before 1.4.
     * <p>
     * 
     * @return a SynchronizedList instance if the underlying list implements
     *         RandomAccess interface, or this same object if not.
     * 
     * @see SynchronizedRandomAccessList#writeReplace()
     */
    private Object readResolve() {
      if (list instanceof RandomAccess) {
        return new SynchronizedRandomAccessList(list, mutex);
      }
      return this;
    }
  }

  static class SynchronizedMap implements Map, Serializable {
    private static final long serialVersionUID = 1978198479659022715L;

    private final Map m;

    final Object mutex;

    SynchronizedMap(Map map) {
        m = map;
        mutex = this;
    }

    SynchronizedMap(Map map, Object mutex) {
        m = map;
        this.mutex = mutex;
    }

    public void clear() {
        synchronized (mutex) {
            m.clear();
        }
    }

    public boolean containsKey(Object key) {
        synchronized (mutex) {
            return m.containsKey(key);
        }
    }

    public boolean containsValue(Object value) {
        synchronized (mutex) {
            return m.containsValue(value);
        }
    }

    public Set entrySet() {
        synchronized (mutex) {
            return new SynchronizedSet(m.entrySet(), mutex);
        }
    }

    public boolean equals(Object object) {
        synchronized (mutex) {
            return m.equals(object);
        }
    }

    public Object get(Object key) {
        synchronized (mutex) {
            return m.get(key);
        }
    }

    public int hashCode() {
        synchronized (mutex) {
            return m.hashCode();
        }
    }

    public boolean isEmpty() {
        synchronized (mutex) {
            return m.isEmpty();
        }
    }

    public Set keySet() {
        synchronized (mutex) {
            return new SynchronizedSet(m.keySet(), mutex);
        }
    }

    public Object put(Object key, Object value) {
        synchronized (mutex) {
            return m.put(key, value);
        }
    }

    public void putAll(Map map) {
        synchronized (mutex) {
            m.putAll(map);
        }
    }

    public Object remove(Object key) {
        synchronized (mutex) {
            return m.remove(key);
        }
    }

    public int size() {
        synchronized (mutex) {
            return m.size();
        }
    }

    public Collection values() {
        synchronized (mutex) {
            return new SynchronizedCollection(m.values(), mutex);
        }
    }

    public String toString() {
        synchronized (mutex) {
            return m.toString();
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        synchronized (mutex) {
            stream.defaultWriteObject();
        }
    }
  }

  static class SynchronizedSet extends SynchronizedCollection implements Set {
      private static final long serialVersionUID = 487447009682186044L;

      SynchronizedSet(Set set) {
          super(set);
      }

      SynchronizedSet(Set set, Object mutex) {
          super(set, mutex);
      }

      public boolean equals(Object object) {
          synchronized (mutex) {
              return back.equals(object);
          }
      }

      public int hashCode() {
          synchronized (mutex) {
              return back.hashCode();
          }
      }

      private void writeObject(ObjectOutputStream stream) throws IOException {
          synchronized (mutex) {
              stream.defaultWriteObject();
          }
      }
  }

  static class SynchronizedSortedMap extends SynchronizedMap implements SortedMap {
        private static final long serialVersionUID = -8798146769416483793L;

        private final SortedMap sm;

        SynchronizedSortedMap(SortedMap map) {
            super(map);
            sm = map;
        }

        SynchronizedSortedMap(SortedMap map, Object mutex) {
            super(map, mutex);
            sm = map;
        }

        public Comparator comparator() {
            synchronized (mutex) {
                return sm.comparator();
            }
        }

        public Object firstKey() {
            synchronized (mutex) {
                return sm.firstKey();
            }
        }

        public SortedMap headMap(Object endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(sm.headMap(endKey), mutex);
            }
        }

        public Object lastKey() {
            synchronized (mutex) {
                return sm.lastKey();
            }
        }

        public SortedMap subMap(Object startKey, Object endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(sm.subMap(startKey,
                        endKey), mutex);
            }
        }

        public SortedMap tailMap(Object startKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap(sm.tailMap(startKey), mutex);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }

    static class SynchronizedSortedSet extends SynchronizedSet implements SortedSet {
    private static final long serialVersionUID = 8695801310862127406L;

    private final SortedSet ss;

    SynchronizedSortedSet(SortedSet set) {
      super(set);
      ss = set;
    }

    SynchronizedSortedSet(SortedSet set, Object mutex) {
      super(set, mutex);
      ss = set;
    }

    public Comparator comparator() {
      synchronized (mutex) {
        return ss.comparator();
      }
    }

    public Object first() {
      synchronized (mutex) {
        return ss.first();
      }
    }

    public SortedSet headSet(Object end) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.headSet(end), mutex);
      }
    }

    public Object last() {
      synchronized (mutex) {
        return ss.last();
      }
    }

    public SortedSet subSet(Object start, Object end) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.subSet(start, end), mutex);
      }
    }

    public SortedSet tailSet(Object start) {
      synchronized (mutex) {
        return new SynchronizedSortedSet(ss.tailSet(start), mutex);
      }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
      synchronized (mutex) {
        stream.defaultWriteObject();
      }
    }
  }

  private static class UnmodifiableList extends UnmodifiableCollection implements List {
    private static final long serialVersionUID = -283967356065247728L;

    final List list;

    UnmodifiableList(List l) {
      super(l);
      list = l;
    }

    public void add(int location, Object object) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int location, Collection collection) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object object) {
        return list.equals(object);
    }

    public Object get(int location) {
        return list.get(location);
    }

    public int hashCode() {
        return list.hashCode();
    }

    public int indexOf(Object object) {
        return list.indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return list.lastIndexOf(object);
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(final int location) {
        return new ListIterator() {
        ListIterator iterator = list.listIterator(location);

        public void add(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        public Object next() {
            return iterator.next();
        }

        public int nextIndex() {
            return iterator.nextIndex();
        }

        public Object previous() {
            return iterator.previous();
        }

        public int previousIndex() {
            return iterator.previousIndex();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(Object object) {
            throw new UnsupportedOperationException();
        }
      };
    }

    public Object remove(int location) {
        throw new UnsupportedOperationException();
    }

    public Object set(int location, Object object) {
        throw new UnsupportedOperationException();
    }

    public List subList(int start, int end) {
        return new UnmodifiableList(list.subList(start, end));
    }

    /**
     * Resolves UnmodifiableList instances to UnmodifiableRandomAccessList
     * instances if the underlying list is a Random Access list.
     * <p>
     * This is necessary since UnmodifiableRandomAccessList instances are
     * replaced with UnmodifiableList instances during serialization for
     * compliance with JREs before 1.4.
     * <p>
     * 
     * @return an UnmodifiableList instance if the underlying list
     *         implements RandomAccess interface, or this same object if
     *         not.
     * 
     * @see UnmodifiableRandomAccessList#writeReplace()
     */
    private Object readResolve() {
      if (list instanceof RandomAccess) {
        return new UnmodifiableRandomAccessList(list);
      }
      return this;
    }
  }

  private static class UnmodifiableMap implements Map, Serializable {
        private static final long serialVersionUID = -1034234728574286014L;

        private final Map m;

        private static class UnmodifiableEntrySet extends UnmodifiableSet {
            private static final long serialVersionUID = 7854390611657943733L;

            private static class UnmodifiableMapEntry implements
                    Map.Entry {
                Map.Entry mapEntry;

                UnmodifiableMapEntry(Map.Entry entry) {
                    mapEntry = entry;
                }

                public boolean equals(Object object) {
                    return mapEntry.equals(object);
                }

                public Object getKey() {
                    return mapEntry.getKey();
                }

                public Object getValue() {
                    return mapEntry.getValue();
                }

                public int hashCode() {
                    return mapEntry.hashCode();
                }

                public Object setValue(Object object) {
                    throw new UnsupportedOperationException();
                }

                public String toString() {
                    return mapEntry.toString();
                }
            }

            UnmodifiableEntrySet(Set set) {
                super(set);
            }

            public Iterator iterator() {
                return new Iterator() {
                    Iterator iterator = c.iterator();

                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public Map.Entry next() {
                        return new UnmodifiableMapEntry((Map.Entry)iterator.next());
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public Object[] toArray() {
                int length = c.size();
                Object[] result = new Object[length];
                Iterator it = iterator();
                for (int i = length; --i >= 0;) {
                    result[i] = it.next();
                }
                return result;
            }

            public Object[] toArray(Object[] contents) {
                int size = c.size(), index = 0;
                Iterator it = iterator();
                if (size > contents.length) {
                    Class ct = contents.getClass().getComponentType();
                    contents = (Object[])Array.newInstance(ct, size);
                }
                while (index < size) {
                    contents[index++] = it.next();
                }
                if (index < contents.length) {
                    contents[index] = null;
                }
                return contents;
            }
        }

        UnmodifiableMap(Map map) {
            m = map;
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            return m.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return m.containsValue(value);
        }

        public Set entrySet() {
            return new UnmodifiableEntrySet(m.entrySet());
        }

        public boolean equals(Object object) {
            return m.equals(object);
        }

        public Object get(Object key) {
            return m.get(key);
        }

        public int hashCode() {
            return m.hashCode();
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public Set keySet() {
            return new UnmodifiableSet(m.keySet());
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map map) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return m.size();
        }

        public Collection values() {
            return new UnmodifiableCollection(m.values());
        }

        public String toString() {
            return m.toString();
        }
    }

    private static class UnmodifiableSet extends UnmodifiableCollection
            implements Set {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set set) {
            super(set);
        }

        public boolean equals(Object object) {
            return c.equals(object);
        }

        public int hashCode() {
            return c.hashCode();
        }
    }

    private static class UnmodifiableSortedMap extends
            UnmodifiableMap implements SortedMap {
        private static final long serialVersionUID = -8806743815996713206L;

        private final SortedMap sm;

        UnmodifiableSortedMap(SortedMap map) {
            super(map);
            sm = map;
        }

        public Comparator comparator() {
            return sm.comparator();
        }

        public Object firstKey() {
            return sm.firstKey();
        }

        public SortedMap headMap(Object before) {
            return new UnmodifiableSortedMap(sm.headMap(before));
        }

        public Object lastKey() {
            return sm.lastKey();
        }

        public SortedMap subMap(Object start, Object end) {
            return new UnmodifiableSortedMap(sm.subMap(start, end));
        }

        public SortedMap tailMap(Object after) {
            return new UnmodifiableSortedMap(sm.tailMap(after));
        }
    }

    private static class UnmodifiableSortedSet extends UnmodifiableSet
            implements SortedSet {
        private static final long serialVersionUID = -4929149591599911165L;

        private final SortedSet ss;

        UnmodifiableSortedSet(SortedSet set) {
            super(set);
            ss = set;
        }

        public Comparator comparator() {
            return ss.comparator();
        }

        public Object first() {
            return ss.first();
        }

        public SortedSet headSet(Object before) {
            return new UnmodifiableSortedSet(ss.headSet(before));
        }

        public Object last() {
            return ss.last();
        }

        public SortedSet subSet(Object start, Object end) {
            return new UnmodifiableSortedSet(ss.subSet(start, end));
        }

    public SortedSet tailSet(Object after) {
      return new UnmodifiableSortedSet(ss.tailSet(after));
    }
  }

  private Collections() {
  }

  /**
   * Performs a binary search for the specified element in the specified
   * sorted list. The list needs to be already sorted in natural sorting
   * order. Searching in an unsorted array has an undefined result. It's also
   * undefined which element is found if there are multiple occurrences of the
   * same element.
   * 
   * @param list
   *            the sorted list to search.
   * @param object
   *            the element to find.
   * @return the non-negative index of the element, or a negative index which
   *         is the {@code -index - 1} where the element would be inserted
   * @throws ClassCastException
   *             if an element in the List or the search element does not
   *             implement Comparable, or cannot be compared to each other.
   */
  public static int binarySearch(List list, Object object) {
    if (list == null) {
        throw new NullPointerException();
    }
    if (list.isEmpty()) {
      return -1;
    }
           
    if (!(list instanceof RandomAccess)) {
        ListIterator it = list.listIterator();
        while (it.hasNext()) {
            int result;
            if ((result = -((Comparable)it.next()).compareTo(object)) <= 0) {    
                if (result == 0) {
                    return it.previousIndex();
                }
                return -it.previousIndex() - 1;
            }
        }
        return -list.size() - 1;
    }

    int low = 0, mid = list.size(), high = mid - 1, result = -1;
    while (low <= high) {
        mid = (low + high) >> 1;
        if ((result = -((Comparable)list.get(mid)).compareTo(object)) > 0) {
            low = mid + 1;
        } else if (result == 0) {
            return mid;
        } else {
            high = mid - 1;
        }
    }
    return -mid - (result < 0 ? 1 : 2);
  }

  /**
   * Performs a binary search for the specified element in the specified
   * sorted list using the specified comparator. The list needs to be already
   * sorted according to the comparator passed. Searching in an unsorted array
   * has an undefined result. It's also undefined which element is found if
   * there are multiple occurrences of the same element.
   * 
   * @param <T> The element type
   * @param list
   *            the sorted List to search.
   * @param object
   *            the element to find.
   * @param comparator
   *            the comparator. If the comparator is {@code null} then the
   *            search uses the objects' natural ordering.
   * @return the non-negative index of the element, or a negative index which
   *         is the {@code -index - 1} where the element would be inserted.
   * @throws ClassCastException
   *             when an element in the list and the searched element cannot
   *             be compared to each other using the comparator.
   */
  public static  int binarySearch(List list, Object object, Comparator comparator) {
    if (comparator == null) {
      return Collections.binarySearch(list, object);
    }
    if (!(list instanceof RandomAccess)) {
        ListIterator it = list.listIterator();
        while (it.hasNext()) {
            int result;
            if ((result = -comparator.compare(it.next(), object)) <= 0) {
                if (result == 0) {
                    return it.previousIndex();
                }
                return -it.previousIndex() - 1;
            }
        }
        return -list.size() - 1;
    }

    int low = 0, mid = list.size(), high = mid - 1, result = -1;
    while (low <= high) {
        mid = (low + high) >> 1;
        if ((result = -comparator.compare(list.get(mid),object)) > 0) {
            low = mid + 1;
        } else if (result == 0) {
            return mid;
        } else {
            high = mid - 1;
        }
    }
    return -mid - (result < 0 ? 1 : 2);
  }

  /**
   * Copies the elements from the source list to the destination list. At the
   * end both lists will have the same objects at the same index. If the
   * destination array is larger than the source list, the elements in the
   * destination list with {@code index >= source.size()} will be unchanged.
   * 
   * @param destination
   *            the list whose elements are set from the source list.
   * @param source
   *            the list with the elements to be copied into the destination.
   * @throws IndexOutOfBoundsException
   *             when the destination list is smaller than the source list.
   * @throws UnsupportedOperationException
   *             when replacing an element in the destination list is not
   *             supported.
   */
  public static void copy(List destination, List source) {
    if (destination.size() < source.size()) {
      throw new ArrayIndexOutOfBoundsException("Source size " + source.size() + " does not fit into destination"); 
    }
    Iterator srcIt = source.iterator();
    ListIterator destIt = destination.listIterator();
    while (srcIt.hasNext()) {
      try {
        destIt.next();
      } catch (NoSuchElementException e) {
        throw new ArrayIndexOutOfBoundsException("Source size " + source.size() + " does not fit into destination"); 
      }
      destIt.set(srcIt.next());
    }
  }

  /**
   * Returns an {@code Enumeration} on the specified collection.
   * 
   * @param collection
   *            the collection to enumerate.
   * @return an Enumeration.
   */
  public static Enumeration enumeration(Collection collection) {
    final Collection c = collection;
    return new Enumeration() {
      Iterator it = c.iterator();

      public boolean hasMoreElements() {
        return it.hasNext();
      }

      public Object nextElement() {
        return it.next();
      }
    };
  }

  /**
   * Fills the specified list with the specified element.
   * 
   * @param list
   *            the list to fill.
   * @param object
   *            the element to fill the list with.
   * @throws UnsupportedOperationException
   *             when replacing an element in the List is not supported.
   */
  public static void fill(List list, Object object) {
    ListIterator it = list.listIterator();
    while (it.hasNext()) {
      it.next();
      it.set(object);
    }
  }

  /**
   * Searches the specified collection for the maximum element.
   * 
   * @param collection
   *            the collection to search.
   * @return the maximum element in the Collection.
   * @throws ClassCastException
   *             when an element in the collection does not implement
   *             {@code Comparable} or elements cannot be compared to each
   *             other.
   */
  public static Object max(Collection collection) {
    Iterator it = collection.iterator();
    Comparable max = (Comparable)it.next();
    while (it.hasNext()) {
      Object next = it.next();
      if (max.compareTo(next) < 0) {
        max = (Comparable)next;
      }
    }
    return max;
  }

  /**
   * Searches the specified collection for the maximum element using the
   * specified comparator.
   * 
   * @param collection
   *            the collection to search.
   * @param comparator
   *            the comparator.
   * @return the maximum element in the Collection.
   * @throws ClassCastException
   *             when elements in the collection cannot be compared to each
   *             other using the {@code Comparator}.
   */
  public static Object max(Collection collection, Comparator comparator) {
    if (comparator == null) {
      Object result = max(collection);
      return result;
    }

    Iterator it = collection.iterator();
    Comparable max = (Comparable)it.next();
    while (it.hasNext()) {
      Object next = it.next();
      if (comparator.compare(max, next) < 0) {
        max = (Comparable)next;
      }
    }
    return max;
  }

  /**
   * Searches the specified collection for the minimum element.
   * 
   * @param collection
   *            the collection to search.
   * @return the minimum element in the collection.
   * @throws ClassCastException
   *             when an element in the collection does not implement
   *             {@code Comparable} or elements cannot be compared to each
   *             other.
   */
  public static Object min(Collection collection) {
    Iterator it = collection.iterator();
    Comparable min = (Comparable)it.next();
    while (it.hasNext()) {
      Object next = it.next();
      if (min.compareTo(next) > 0) {
        min = (Comparable)next;
      }
    }
    return min;
  }

  /**
   * Searches the specified collection for the minimum element using the
   * specified comparator.
   * 
   * @param collection
   *            the collection to search.
   * @param comparator
   *            the comparator.
   * @return the minimum element in the collection.
   * @throws ClassCastException
   *             when elements in the collection cannot be compared to each
   *             other using the {@code Comparator}.
   */
  public static Object min(Collection collection, Comparator comparator) {
    if (comparator == null) {
      Object result = min(collection);
      return result;
    }

    Iterator it = collection.iterator();
    Comparable min = (Comparable)it.next();
    while (it.hasNext()) {
      Object next = it.next();
      if (comparator.compare(min, next) > 0) {
        min = (Comparable)next;
      }
    }
    return min;
  }

  /**
   * Returns a list containing the specified number of the specified element.
   * The list cannot be modified. The list is serializable.
   * 
   * @param length
   *            the size of the returned list.
   * @param object
   *            the element to be added {@code length} times to a list.
   * @return a list containing {@code length} copies of the element.
   * @throws IllegalArgumentException
   *             when {@code length < 0}.
   */
  public static List nCopies(final int length, Object object) {
        return new CopiesList(length, object);
  }

  /**
   * Modifies the specified {@code List} by reversing the order of the
   * elements.
   * 
   * @param list
   *            the list to reverse.
   * @throws UnsupportedOperationException
   *             when replacing an element in the List is not supported.
   */
  public static void reverse(List list) {
    int size = list.size();
    ListIterator front = (ListIterator) list.listIterator();
    ListIterator back = (ListIterator) list
            .listIterator(size);
    for (int i = 0; i < size / 2; i++) {
      Object frontNext = front.next();
      Object backPrev = back.previous();
      front.set(backPrev);
      back.set(frontNext);
    }
  }

  /**
   * A comparator which reverses the natural order of the elements. The
   * {@code Comparator} that's returned is {@link Serializable}.
   *
   * @return a {@code Comparator} instance.
   * @see Comparator
   * @see Comparable
   * @see Serializable
   */
  public static Comparator reverseOrder() {
    return (Comparator) ReverseComparator.INSTANCE;
  }

  /**
   * Returns a {@link Comparator} that reverses the order of the
   * {@code Comparator} passed. If the {@code Comparator} passed is
   * {@code null}, then this method is equivalent to {@link #reverseOrder()}.
   * <p>
   * The {@code Comparator} that's returned is {@link Serializable} if the
   * {@code Comparator} passed is serializable or {@code null}.
   *
   * @param c
   *            the {@code Comparator} to reverse or {@code null}.
   * @return a {@code Comparator} instance.
   * @see Comparator
   * @since 1.5
   */
  public static Comparator reverseOrder(Comparator c) {
    if (c == null) {
      return reverseOrder();
    }
    if (c instanceof ReverseComparatorWithComparator) {
      return ((ReverseComparatorWithComparator) c).comparator;
    }
    return new ReverseComparatorWithComparator(c);
  }

  /**
   * Moves every element of the list to a random new position in the list.
   * 
   * @param list
   *            the List to shuffle.
   * 
   * @throws UnsupportedOperationException
   *             when replacing an element in the List is not supported.
   */
  public static void shuffle(List list) {
    shuffle(list, new Random());
  }

  /**
   * Moves every element of the list to a random new position in the list
   * using the specified random number generator.
   * 
   * @param list
   *            the list to shuffle.
   * @param random
   *            the random number generator.
   * @throws UnsupportedOperationException
   *             when replacing an element in the list is not supported.
   */
  public static void shuffle(List list, Random random) {
    final List objectList = list;

    if (list instanceof RandomAccess) {
      for (int i = objectList.size() - 1; i > 0; i--) {
        int index = random.nextInt(i + 1);
        objectList.set(index, objectList.set(i, objectList.get(index)));
      }
    } else {
      Object[] array = objectList.toArray();
      for (int i = array.length - 1; i > 0; i--) {
        int index = random.nextInt(i + 1);
        Object temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }

      int i = 0;
      ListIterator it = objectList.listIterator();
      while (it.hasNext()) {
        it.next();
        it.set(array[i++]);
      }
    }
  }

  /**
   * Returns a set containing the specified element. The set cannot be
   * modified. The set is serializable.
   * 
   * @param object
   *            the element.
   * @return a set containing the element.
   */
  public static Set singleton(Object object) {
    return new SingletonSet(object);
  }

  /**
   * Returns a list containing the specified element. The list cannot be
   * modified. The list is serializable.
   * 
   * @param object
   *            the element.
   * @return a list containing the element.
   */
  public static List singletonList(Object object) {
    return new SingletonList(object);
  }

  /**
   * Returns a Map containing the specified key and value. The map cannot be
   * modified. The map is serializable.
   * 
   * @param key
   *            the key.
   * @param value
   *            the value.
   * @return a Map containing the key and value.
   */
  public static Map singletonMap(Object key, Object value) {
    return new SingletonMap(key, value);
  }

  /**
   * Sorts the specified list in ascending natural order. The algorithm is
   * stable which means equal elements don't get reordered.
   * 
   * @param list
   *            the list to be sorted.
   * @throws ClassCastException
   *             when an element in the List does not implement Comparable or
   *             elements cannot be compared to each other.
   */
  public static void sort(List list) {
    Object[] array = list.toArray();
    Arrays.sort(array);
    int i = 0;
    ListIterator it = list.listIterator();
    while (it.hasNext()) {
      it.next();
      it.set(array[i++]);
    }
  }

  /**
   * Sorts the specified list using the specified comparator. The algorithm is
   * stable which means equal elements don't get reordered.
   * 
   * @param list
   *            the list to be sorted.
   * @param comparator
   *            the comparator.
   * @throws ClassCastException
   *             when elements in the list cannot be compared to each other
   *             using the comparator.
   */
  public static void sort(List list, Comparator comparator) {
    Object[] array = list.toArray((Object[]) new Object[list.size()]);
    Arrays.sort(array, comparator);
    int i = 0;
    ListIterator it = list.listIterator();
    while (it.hasNext()) {
      it.next();
      it.set(array[i++]);
    }
  }

  /**
   * Swaps the elements of list {@code list} at indices {@code index1} and
   * {@code index2}.
   * 
   * @param list
   *            the list to manipulate.
   * @param index1
   *            position of the first element to swap with the element in
   *            index2.
   * @param index2
   *            position of the other element.
   * 
   * @throws IndexOutOfBoundsException
   *             if index1 or index2 is out of range of this list.
   * @since 1.4
   */
  public static void swap(List list, int index1, int index2) {
    if (list == null) {
      throw new NullPointerException();
    }
    final int size = list.size();
    if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
      throw new IndexOutOfBoundsException();
    }
    if (index1 == index2) {
      return;
    }
    List rawList = list;
    rawList.set(index2, rawList.set(index1, rawList.get(index2)));
  }

  /**
   * Replaces all occurrences of Object {@code obj} in {@code list} with
   * {@code newObj}. If the {@code obj} is {@code null}, then all
   * occurrences of {@code null} are replaced with {@code newObj}.
   * 
   * @param list
   *            the list to modify.
   * @param obj
   *            the object to find and replace occurrences of.
   * @param obj2
   *            the object to replace all occurrences of {@code obj} in
   *            {@code list}.
   * @return true, if at least one occurrence of {@code obj} has been found in
   *         {@code list}.
   * @throws UnsupportedOperationException
   *             if the list does not support setting elements.
   */
  public static boolean replaceAll(List list, Object obj, Object obj2) {
    int index;
    boolean found = false;

    while ((index = list.indexOf(obj)) > -1) {
      found = true;
      list.set(index, obj2);
    }
    return found;
  }

  /**
   * Rotates the elements in {@code list} by the distance {@code dist}
   * <p>
   * e.g. for a given list with elements [1, 2, 3, 4, 5, 6, 7, 8, 9, 0],
   * calling rotate(list, 3) or rotate(list, -7) would modify the list to look
   * like this: [8, 9, 0, 1, 2, 3, 4, 5, 6, 7]
   *
   * @param lst
   *            the list whose elements are to be rotated.
   * @param dist
   *            is the distance the list is rotated. This can be any valid
   *            integer. Negative values rotate the list backwards.
   */
  public static void rotate(List lst, int dist) {
    List list = (List) lst;
    int size = list.size();

    // Can't sensibly rotate an empty collection
    if (size == 0) {
      return;
    }

     // normalize the distance
    int normdist;
    if (dist > 0) {
      normdist = dist % size;
    } else {
      normdist = size - ((dist % size) * (-1));
    }

    if (normdist == 0 || normdist == size) {
      return;
    }

    if (list instanceof RandomAccess) {
      // make sure each element gets juggled
      // with the element in the position it is supposed to go to
      Object temp = list.get(0);
      int index = 0, beginIndex = 0;
      for (int i = 0; i < size; i++) {
        index = (index + normdist) % size;
        temp = list.set(index, temp);
        if (index == beginIndex) {
          index = ++beginIndex;
          temp = list.get(beginIndex);
        }
      }
    } else {
      int divideIndex = (size - normdist) % size;
      List sublist1 = list.subList(0, divideIndex);
      List sublist2 = list.subList(divideIndex, size);
      reverse(sublist1);
      reverse(sublist2);
      reverse(list);
    }
  }

  /**
   * Searches the {@code list} for {@code sublist} and returns the beginning
   * index of the first occurrence.
   * <p>
   * -1 is returned if the {@code sublist} does not exist in {@code list}.
   * 
   * @param list
   *            the List to search {@code sublist} in.
   * @param sublist
   *            the List to search in {@code list}.
   * @return the beginning index of the first occurrence of {@code sublist} in
   *         {@code list}, or -1.
   */
  public static int indexOfSubList(List list, List sublist) {
    int size = list.size();
    int sublistSize = sublist.size();

    if (sublistSize > size) {
      return -1;
    }
 
    if (sublistSize == 0) {
      return 0;
    }

    // find the first element of sublist in the list to get a head start
    Object firstObj = sublist.get(0);
    int index = list.indexOf(firstObj);
    if (index == -1) {
      return -1;
    }

    while (index < size && (size - index >= sublistSize)) {
      ListIterator listIt = list.listIterator(index);

      if ((firstObj == null) ? listIt.next() == null : firstObj
             .equals(listIt.next())) {

      // iterate through the elements in sublist to see
      // if they are included in the same order in the list
      ListIterator sublistIt = sublist.listIterator(1);
      boolean difFound = false;
      while (sublistIt.hasNext()) {
        Object element = sublistIt.next();
        if (!listIt.hasNext()) {
          return -1;
        }
        if ((element == null) ? listIt.next() != null : !element.equals(listIt.next())) {
          difFound = true;
          break;
        }
      }
      // All elements of sublist are found in main list
      // starting from index.
      if (!difFound) {
        return index;
      }
    }
    // This was not the sequence we were looking for,
    // continue search for the firstObj in main list
    // at the position after index.
    index++;
  }
    return -1;
  }

  /**
   * Searches the {@code list} for {@code sublist} and returns the beginning
   * index of the last occurrence.
   * <p>
   * -1 is returned if the {@code sublist} does not exist in {@code list}.
   * 
   * @param list
   *            the list to search {@code sublist} in.
   * @param sublist
   *            the list to search in {@code list}.
   * @return the beginning index of the last occurrence of {@code sublist} in
   *         {@code list}, or -1.
   */
  public static int lastIndexOfSubList(List list, List sublist) {
    int sublistSize = sublist.size();
    int size = list.size();

    if (sublistSize > size) {
      return -1;
    }

    if (sublistSize == 0) {
      return size;
    }

    // find the last element of sublist in the list to get a head start
    Object lastObj = sublist.get(sublistSize - 1);
    int index = list.lastIndexOf(lastObj);

    while ((index > -1) && (index + 1 >= sublistSize)) {
    ListIterator listIt = list.listIterator(index + 1);

    if ((lastObj == null) ? listIt.previous() == null : lastObj.equals(listIt.previous())) {
      // iterate through the elements in sublist to see
      // if they are included in the same order in the list
      ListIterator sublistIt = sublist.listIterator(sublistSize - 1);
      boolean difFound = false;
      while (sublistIt.hasPrevious()) {
        Object element = sublistIt.previous();
        if (!listIt.hasPrevious()) {
          return -1;
        }
        if ((element == null) ? listIt.previous() != null : !element.equals(listIt.previous())) {
          difFound = true;
          break;
        }
      }
      // All elements of sublist are found in main list
      // starting from listIt.nextIndex().
      if (!difFound) {
        return listIt.nextIndex();
      }
    }
    // This was not the sequence we were looking for,
    // continue search for the lastObj in main list
    // at the position before index.
    index--;
    }
    return -1;
  }

  /**
   * Returns a wrapper on the specified collection which synchronizes all
   * access to the collection.
   * 
   * @param collection
   *            the Collection to wrap in a synchronized collection.
   * @return a synchronized Collection.
   */
  public static Collection synchronizedCollection(Collection collection) {
    if (collection == null) {
      throw new NullPointerException();
    }
    return new SynchronizedCollection(collection);
  }

  /**
   * Returns a wrapper on the specified List which synchronizes all access to
   * the List.
   * 
   * @param list
   *            the List to wrap in a synchronized list.
   * @return a synchronized List.
   */
  public static List synchronizedList(List list) {
    if (list == null) {
      throw new NullPointerException();
    }
    if (list instanceof RandomAccess) {
      return new SynchronizedRandomAccessList(list);
    }
    return new SynchronizedList(list);
  }

  /**
   * Returns a wrapper on the specified map which synchronizes all access to
   * the map.
   * 
   * @param map
   *            the map to wrap in a synchronized map.
   * @return a synchronized Map.
   */
  public static Map synchronizedMap(Map map) {
    if (map == null) {
      throw new NullPointerException();
    }
    return new SynchronizedMap(map);
  }

  /**
   * Returns a wrapper on the specified set which synchronizes all access to
   * the set.
   * 
   * @param set
   *            the set to wrap in a synchronized set.
   * @return a synchronized set.
   */
  public static Set synchronizedSet(Set set) {
    if (set == null) {
      throw new NullPointerException();
    }
    return new SynchronizedSet(set);
  }

  /**
   * Returns a wrapper on the specified sorted map which synchronizes all
   * access to the sorted map.
   * 
   * @param map
   *            the sorted map to wrap in a synchronized sorted map.
   * @return a synchronized sorted map.
   */
  public static SortedMap synchronizedSortedMap(SortedMap map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return new SynchronizedSortedMap(map);
    }

  /**
   * Returns a wrapper on the specified sorted set which synchronizes all
   * access to the sorted set.
   * 
   * @param set
   *            the sorted set to wrap in a synchronized sorted set.
   * @return a synchronized sorted set.
   */
  public static SortedSet synchronizedSortedSet(SortedSet set) {
    if (set == null) {
      throw new NullPointerException();
    }
    return new SynchronizedSortedSet(set);
  }

  /**
   * Returns a wrapper on the specified collection which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the collection.
   * 
   * @param collection
   *            the collection to wrap in an unmodifiable collection.
   * @return an unmodifiable collection.
   */
  public static Collection unmodifiableCollection(Collection collection) {
    if (collection == null) {
      throw new NullPointerException();
    }
    return new UnmodifiableCollection(collection);
  }

  /**
   * Returns a wrapper on the specified list which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the list.
   * 
   * @param list
   *            the list to wrap in an unmodifiable list.
   * @return an unmodifiable List.
   */
  public static List unmodifiableList(List list) {
    if (list == null) {
      throw new NullPointerException();
    }
    if (list instanceof RandomAccess) {
      return new UnmodifiableRandomAccessList(list);
    }
    return new UnmodifiableList(list);
  }

  /**
   * Returns a wrapper on the specified map which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the map.
   * 
   * @param map
   *            the map to wrap in an unmodifiable map.
   * @return a unmodifiable map.
   */
  public static Map unmodifiableMap( Map map) {
    if (map == null) {
      throw new NullPointerException();
    }
    return new UnmodifiableMap(map);
  }

  /**
   * Returns a wrapper on the specified set which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the set.
   * 
   * @param set
   *            the set to wrap in an unmodifiable set.
   * @return a unmodifiable set
   */
  public static Set unmodifiableSet(Set set) {
    if (set == null) {
      throw new NullPointerException();
    }
    return new UnmodifiableSet(set);
  }

  /**
   * Returns a wrapper on the specified sorted map which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the sorted map.
   * 
   * @param map
   *            the sorted map to wrap in an unmodifiable sorted map.
   * @return a unmodifiable sorted map
   */
  public static SortedMap unmodifiableSortedMap(SortedMap map) {
    if (map == null) {
      throw new NullPointerException();
    }
    return new UnmodifiableSortedMap(map);
  }

  /**
   * Returns a wrapper on the specified sorted set which throws an
   * {@code UnsupportedOperationException} whenever an attempt is made to
   * modify the sorted set.
   * 
   * @param set
   *            the sorted set to wrap in an unmodifiable sorted set.
   * @return a unmodifiable sorted set.
   */
  public static SortedSet unmodifiableSortedSet(SortedSet set) {
    if (set == null) {
      throw new NullPointerException();
    }
    return new UnmodifiableSortedSet(set);
  }

  /**
  * Returns an {@code ArrayList} with all the elements in the {@code
  * enumeration}. The elements in the returned {@code ArrayList} are in the
  * same order as in the {@code enumeration}.
  * 
  * @param enumeration
  *            the source {@link Enumeration}.
  * @return an {@code ArrayList} from {@code enumeration}.
  */
  public static ArrayList list(Enumeration enumeration) {
    ArrayList list = new ArrayList();
    while (enumeration.hasMoreElements()) {
      list.add(enumeration.nextElement());
    }
    return list;
  }

/*
  static class StdComparator implements Comparator {
    public int compare(Object one, Object two) {
      Comparable c1 = (Comparable) one;
      Comparable c2 = (Comparable) two;
      if (one != null) {
        return c1.compareTo(c2);
      }
      if (two != null) {
        return (-c2.compareTo(c1));
      }
      return 0;
    }
  }

  public static void sort(List list, Comparator c) {
    qSort(list, 0, list.size() - 1, c);
  }

  private static void qSort(List ls, int l, int h, Comparator c) {
    if (h - l < 3) {
      fastSort(ls, l, h - l + 1, c);
    } else {
      int pivot = h;
      swap(ls, (l + h) / 2, h);
      Object pvt = ls.get(pivot);
      for (int i = l; i < pivot; i++) {
        if (c.compare(pvt, ls.get(i)) < 0) {
          if (i + 1 == pivot) {
            swap(ls, i, pivot);
            pivot--;
          } else {
            ls.set(pivot--, ls.get(i));
            Object o = ls.set(pivot, pvt);
            ls.set(i--, o);
          }
        }
      }
      qSort(ls, l, pivot - 1, c);
      qSort(ls, pivot + 1, h, c);
    }
  }

  /..
   . only useable if size 3 or less ...
   ./
  private static void fastSort(List ls, int l, int s, Comparator c) {
    if (s >= 2) {
      if (c.compare(ls.get(l), ls.get(l + 1)) > 0) {
        Object o = ls.set(l + 1, ls.get(l));
        ls.set(l, o);
      }
    }
    if (s >= 3) {
      if (c.compare(ls.get(l + 1), ls.get(l + 2)) > 0) {
        Object o = ls.set(l + 1, ls.get(l + 2));
        ls.set(l + 2, o);
        if (c.compare(ls.get(l), ls.get(l + 1)) > 0) {
          o = ls.set(l + 1, ls.get(l));
          ls.set(l, o);
        }
      }
    }
  }
*/

  private static class UnmodifiableCollection implements Collection,
            Serializable {
    private static final long serialVersionUID = 1820017752578914078L;

    final Collection c;

    UnmodifiableCollection(Collection collection) {
      c = collection;
    }

    public boolean add(Object object) {
      throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection collection) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public boolean contains(Object object) {
      return c.contains(object);
    }

    public boolean containsAll(Collection collection) {
      return c.containsAll(collection);
    }

    public boolean isEmpty() {
      return c.isEmpty();
    }

    public Iterator iterator() {
      return new Iterator() {
        Iterator iterator = c.iterator();

        public boolean hasNext() {
          return iterator.hasNext();
        }

        public Object next() {
          return iterator.next();
        }

        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public boolean remove(Object object) {
      throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection collection) {
      throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection collection) {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return c.size();
    }

    public Object[] toArray() {
      return c.toArray();
    }

    public Object[] toArray(Object[] array) {
      return c.toArray(array);
    }

    public String toString() {
      return c.toString();
    }
  }

  private static class UnmodifiableRandomAccessList extends
            UnmodifiableList implements RandomAccess {
    private static final long serialVersionUID = -2542308836966382001L;

    UnmodifiableRandomAccessList(List l) {
      super(l);
    }

    public List subList(int start, int end) {
      return new UnmodifiableRandomAccessList(list.subList(start, end));
    }

    /**
     * Replaces this UnmodifiableRandomAccessList with an UnmodifiableList
     * so that JREs before 1.4 can deserialize this object without any
     * problems. This is necessary since RandomAccess API was introduced
     * only in 1.4.
     * <p>
     * 
     * @return UnmodifiableList
     * 
     * @see UnmodifiableList#readResolve()
     */
    private Object writeReplace() {
      return new UnmodifiableList(list);
      }
    }
  
/*
  private static class UnmodIterator implements Iterator {
    private Iterator it;

    public UnmodIterator(Iterator i) {
      it = i;
    }

    public boolean hasNext() {
      return it.hasNext();
    }

    public Object next() {
      return it.next();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class FinalCollection extends AbstractCollection implements
      Serializable {
    private Collection back;

    public FinalCollection(Collection bck) {
      if (bck == null) {
        throw new NullPointerException();
      }
      back = bck;
    }

    public int size() {
      return back.size();
    }

    public Iterator iterator() {
      return new UnmodIterator(back.iterator());
    }
  }

  private static class FinalList extends AbstractList implements Serializable {
    private List back;

    public FinalList(List bck) {
      if (bck == null) {
        throw new NullPointerException();
      }
      back = bck;
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return back.size();
    }

    public Object get(int idx) {
      return back.get(idx);
    }

    public Iterator iterator() {
      return new UnmodIterator(back.iterator());
    }

    public Object remove(int index) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

  private static class FinalSet extends AbstractSet implements Serializable {
    private Set back;

    public FinalSet(Set bck) {
      if (bck == null) {
        throw new NullPointerException();
      }
      back = bck;
    }

    public int size() {
      return back.size();
    }

    public Iterator iterator() {
      return new UnmodIterator(back.iterator());
    }

  }

  private static class FinalSortedSet extends AbstractSet implements SortedSet,
      Serializable {
    private SortedSet back;

    public FinalSortedSet(SortedSet bck) {
      if (bck == null) {
        throw new NullPointerException();
      }
      back = bck;
    }

    public int size() {
      return back.size();
    }

    public Iterator iterator() {
      return new UnmodIterator(back.iterator());
    }

    public Object first() {
      return back.first();
    }

    public Object last() {
      return back.last();
    }

    public SortedSet headSet(Object toV) {
      return new FinalSortedSet(back.headSet(toV));
    }

    public SortedSet subSet(Object fromV, Object toV) {
      return new FinalSortedSet(back.subSet(fromV, toV));
    }

    public SortedSet tailSet(Object fromV) {
      return new FinalSortedSet(back.tailSet(fromV));
    }

    public Comparator comparator() {
      return back.comparator();
    }
  }

  // synchronized Helper Classes

  private static class SyncedSet extends SynchronizedCollection implements Set,
      Serializable {
    public SyncedSet(Set s) {
      super(s);
    }

    public synchronized boolean equals(Object e) {
      return back.equals(e);
    }

    public synchronized int hashCode() {
      return back.hashCode();
    }
  }

  private static class SyncedSortedSet extends SyncedSet implements SortedSet,
      Serializable {
    private SortedSet sset;

    public SyncedSortedSet(SortedSet s) {
      super(s);
      sset = s;
    }

    public synchronized Comparator comparator() {
      return sset.comparator();
    }

    public synchronized Object first() {
      return sset.first();
    }

    public synchronized Object last() {
      return sset.last();
    }

    public synchronized SortedSet headSet(Object toV) {
      return new SyncedSortedSet(sset.headSet(toV));
    }

    public synchronized SortedSet subSet(Object fromV, Object toV) {
      return new SyncedSortedSet(sset.subSet(fromV, toV));
    }

    public synchronized SortedSet tailSet(Object fromV) {
      return new SyncedSortedSet(sset.tailSet(fromV));
    }
  }

  private static class SyncedMap implements Map, Serializable {
    protected Map back;

    public SyncedMap(Map c) {
      if (c == null) {
        throw new NullPointerException();
      }
      back = c;
    }

    public synchronized boolean equals(Object e) {
      return back.equals(e);
    }

    public synchronized int hashCode() {
      return back.hashCode();
    }

    public synchronized Object get(Object idx) {
      return back.get(idx);
    }

    public synchronized Object put(Object key, Object val) {
      return back.put(key, val);
    }

    public synchronized void putAll(Map e) {
      back.putAll(e);
    }

    public synchronized void clear() {
      back.clear();
    }

    public synchronized boolean containsKey(Object e) {
      return back.containsKey(e);
    }

    public synchronized boolean containsValue(Object e) {
      return back.containsValue(e);
    }

    public synchronized boolean isEmpty() {
      return back.isEmpty();
    }

    public synchronized Object remove(Object e) {
      return back.remove(e);
    }

    public synchronized int size() {
      return back.size();
    }

    public synchronized Set entrySet() {
      return new SyncedSet(back.entrySet());
    }

    public synchronized Set keySet() {
      return new SyncedSet(back.keySet());
    }

    public synchronized Collection values() {
      return new SynchronizedCollection(back.values());
    }
  }

  private static class SyncedSortedMap extends SyncedMap implements SortedMap,
      Serializable {
    private SortedMap smap;

    public SyncedSortedMap(SortedMap m) {
      super(m);
      smap = m;
    }

    public synchronized Comparator comparator() {
      return smap.comparator();
    }

    public synchronized Object firstKey() {
      return smap.firstKey();
    }

    public synchronized Object lastKey() {
      return smap.lastKey();
    }

    public synchronized SortedMap headMap(Object toV) {
      return new SyncedSortedMap(smap.headMap(toV));
    }

    public synchronized SortedMap subMap(Object fromV, Object toV) {
      return new SyncedSortedMap(smap.subMap(fromV, toV));
    }

    public synchronized SortedMap tailMap(Object fromV) {
      return new SyncedSortedMap(smap.tailMap(fromV));
    }

  }
*/
}
