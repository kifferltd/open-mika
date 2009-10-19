/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class TreeSet extends AbstractSet implements SortedSet, Cloneable,
    java.io.Serializable {

  final static Object value = new Object();

  private transient TreeMap backMap;

  private static final long serialVersionUID = -2479143000061671589L;

  private void readObject(ObjectInputStream s) throws IOException,
      ClassNotFoundException {
    backMap = new TreeMap((Comparator) s.readObject());
    int size = s.readInt();
    for (int i = 0; i < size; i++) {
      add(s.readObject());
    }
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.writeObject(backMap.comparator());
    int size = backMap.size();
    s.writeInt(size);
    Iterator it = iterator();
    for (int i = 0; i < size; i++) {
      s.writeObject(it.next());
    }
  }

  public TreeSet() {
    backMap = new TreeMap();
  }

  public TreeSet(Collection c) {
    backMap = new TreeMap();
    this.addAll(c);
  }

  public TreeSet(SortedSet s) {
    backMap = new TreeMap(s.comparator());
    this.addAll(s);
  }

  public TreeSet(Comparator comp) {
    backMap = new TreeMap(comp);
  }

  public boolean add(Object key) {
    return (backMap.put(key, value) == null);
  }

  public boolean remove(Object key) {
    boolean ans = backMap.containsKey(key);
    backMap.remove(key);
    return ans;
  }

  public boolean contains(Object key) {
    return backMap.containsKey(key);
  }

  public void clear() {
    backMap.clear();
  }

  public Object clone() {
    TreeSet ts = null;
    try {
      ts = (TreeSet) super.clone();
    } catch (CloneNotSupportedException cnse) {
    }
    ts.backMap = (TreeMap) this.backMap.clone();
    return ts;
  }

  public Comparator comparator() {
    return backMap.comparator();
  }

  public Object first() {
    return backMap.firstKey();
  }

  public Object last() {
    return backMap.lastKey();
  }

  public boolean isEmpty() {
    return backMap.isEmpty();
  }

  public int size() {
    return backMap.size();
  }

  public Iterator iterator() {
    return backMap.keySet().iterator();
  }

  public SortedSet headSet(Object toV) {
    return new SubTreeSet(toV, false, backMap);
  }

  public SortedSet tailSet(Object fromV) {
    return new SubTreeSet(fromV, true, backMap);
  }

  public SortedSet subSet(Object fromV, Object toV) {
    return new SubTreeSet(fromV, toV, backMap);
  }

  private static class SubTreeSet extends AbstractSet implements SortedSet {

    private SortedMap back;

    public SubTreeSet(Object o, boolean tail, SortedMap m) {
      back = (tail ? m.tailMap(o) : m.headMap(o));
    }

    public SubTreeSet(Object from, Object to, SortedMap m) {
      back = m.subMap(from, to);
    }

    public int size() {
      return back.size();
    }

    public boolean add(Object key) {
      return (back.put(key, value) == null);
    }

    public Iterator iterator() {
      return back.keySet().iterator();
    }

    public Object first() {
      return back.firstKey();
    }

    public Object last() {
      return back.lastKey();
    }

    public Comparator comparator() {
      return back.comparator();
    }

    public SortedSet headSet(Object toV) {
      return new SubTreeSet(toV, false, back);
    }

    public SortedSet tailSet(Object fromV) {
      return new SubTreeSet(fromV, true, back);
    }

    public SortedSet subSet(Object fromV, Object toV) {
      return new SubTreeSet(fromV, toV, back);
    }
  }
}
