/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/**
 * $Id: Collections.java,v 1.3 2006/04/18 11:35:28 cvs Exp $
 */
package java.util;

import java.io.Serializable;

public class Collections {

  private Collections(){}
  
  public static final Set EMPTY_SET = new FinalSet(new HashSet(0));
  public static final List EMPTY_LIST = new FinalList(new ArrayList(0));
  public static final Map EMPTY_MAP = new FinalMap(new HashMap(3));

  public static int binarySearch(List ls, Object e){
  	return binarySearch(ls,e,new StdComparator());
  }

  public static int binarySearch(List ls, Object e, Comparator comp){
  	int size = ls.size();
  	for (int i=0 ; i < size ; i++){
  	 	int cmp = comp.compare(e,ls.get(i));
  	 	if (cmp == 0) {
  	 	 	return i;
  	 	}
  	 	if (cmp < 0){
  	 	 	size = i;
  	 	}
  	}
  	return -(size+1);
  }

  public static void copy(List dest, List src){
  	if (dest.size() < src.size()){
  	 	throw new IndexOutOfBoundsException();
  	}
  	int size = src.size();
  	for (int i=0; i <size ; i++){
  	 	dest.set(i,src.get(i));
  	}
  }

  public static int indexOfSubList(List list, List sub) {
    int sublength = sub.size();
    if(sublength == 0) {
      return 0;
    }
    int l = list.size() - sublength + 1;
    Object first = sub.get(0);
    if (first == null) {
      for(int i=0 ; i < l; i++) {
        if(first == list.get(i) && isSubList(list, sub, i, sublength)) {
          return i;
        }
      }
    } else {
      for(int i=0 ; i < l; i++) {
        if(first.equals(list.get(i)) && isSubList(list, sub, i, sublength)) {
          return i;
        }
      }      
    }
    return -1;
  }
  
  public static int lastIndexOfSubList(List list, List sub) {
    int sublength = sub.size();
    int l = list.size() - sublength;
    if(sublength == 0) {
      return l-1;
    }
    Object first = sub.get(0);
    if (first == null) {
      for(int i=l ; i >= 0; i--) {
        if(first == list.get(i) && isSubList(list, sub, i, sublength)) {
          return i;
        }
      }
    } else {
      for(int i=l ; i >= 0; i--) {
        if(first.equals(list.get(i)) && isSubList(list, sub, i, sublength)) {
          return i;
        }
      }      
    }
    return -1;
  }
  
  public static boolean replaceAll(List list, Object oldVal, Object newVal) {
    boolean changed = false;
    ListIterator iterator = list.listIterator();
    if(oldVal == null) {
      while(iterator.hasNext()) {
        if(oldVal == iterator.next()) {
          iterator.set(newVal);
          changed = true;
        }
      }
    } else {
      while(iterator.hasNext()) {
        if(oldVal.equals(iterator.next())) {
          iterator.set(newVal);
          changed = true;
        }
      }
    }
    return changed;
  }
  
  public static void rotate(List list, int distance) {
    int size = list.size();
    if(size == 0 ) {
      return;
    }
    int shift = distance % size;
    if(shift == 0) {
      return;
    }
    int max = size;
    int loops = shift;
    int remain = max % loops;
    while(remain != 0) {
      max = loops;
      loops = remain;
      remain = max % loops;
    }    
    
    for(int i=0 ; i < loops ; i++) {
      int position = i;
      Object place = list.get(position);
      do {
        position = (position + shift) % size;
        place = list.set(position, place);         
      } while(position != i);
    }
  }
  
  public static ArrayList list(Enumeration e) {
    ArrayList list = new ArrayList();
    while(e.hasMoreElements()) {
      list.add(e.nextElement());
    }
    return list;
  }
  
  private static boolean isSubList(List list, List sub, int pos, int len) {
    for(int i=1 ; i < len ; i++) {
      Object element = sub.get(i);
      Object listE = list.get(pos+1);
      if(element == null ? listE != null : !element.equals(listE)) {
        return false;
      }
    }
    return true;
  }

  public static Enumeration enumeration(Collection c){
   	return new Enum(c.iterator());
  }

  private static class Enum implements Enumeration {
   	 private Iterator it;
   	
   	 public Enum(Iterator i){
   	      	it = i;
   	 }
   	
   	 public boolean hasMoreElements(){
   	  	return it.hasNext();
   	 }
   	 public Object nextElement(){
   	  	return it.next();
   	 }
  }

  public static void fill(List ld, Object e){
	int size = ld.size();
	for (int i=0 ; i < size ; i++){
	 	ld.set(i,e);
	}
  }

  public static Object max(Collection c){
  	return max(c,new StdComparator());
  }

  public static Object max(Collection c, Comparator comp){
   	Iterator it = c.iterator();
   	Object max = it.next();
   	while (it.hasNext()){
        	Object next = it.next();
        	if (comp.compare(max,next)<0){
        	 	max = next;
        	}   	   	
   	}
   	return max;
  }

  public static Object min(Collection c){
  	return min(c,new StdComparator());
  }

  public static Object min(Collection c, Comparator comp){
   	Iterator it = c.iterator();
   	Object min = it.next();
   	while (it.hasNext()){
        	Object next = it.next();
        	if (comp.compare(min,next)>0){
        	 	min = next;
        	}   	   	
   	}
   	return min;
  }

  public static List nCopies(int n, Object e){
	 if ( n < 0){
	  	throw new IllegalArgumentException("negative size specified: "+n);
	 }
	 ArrayList al = new ArrayList(n);
	 for (int i=0; i < n ; i++){
	  	al.add(i,e);
	 }	
	return new FinalList(al);
  }

  public static void reverse(List ls){
  	int size = ls.size()-1;
  	Object o;
  	for (int i=0 ; i < size ; i++){
  	 	o = ls.set(i,ls.get(size));
  	 	ls.set(size--,o); 	 	
  	}
  }

  public static Comparator reverseOrder() {
    return new ReverseComparator();
  }

  static class ReverseComparator implements Comparator {

    public int compare (Object o1, Object o2) {
      Comparable c1 = (Comparable)o1;
      Comparable c2 = (Comparable)o2;
      if (c2 != null) {
      	return c2.compareTo(c1);
      }
      if (c1 != null) {
      	return - c1.compareTo(c2);
      }
      return 0;
    }
  }
/**
** calls shuffle(s, new Random());
*/
  public static void shuffle(List s){
   	shuffle(s,new Random());
  }
/**
** shuffle the list using the given random.
** this method run a for loop... (size times)
** it switches the current pos with a random one
** this means every index is at least one time involved in a
** involved in a switching operation
*/
  public static void shuffle(List ls, Random r){
  	int size = ls.size();
  	if (size > 1){  	 	
  	 	for (int i=0; i < size ; i++){
  	 	      int pos = r.nextInt(size);
  	 	      Object val  = ls.set(i,ls.get(pos));
  	 	      ls.set(pos,val);
  	 	}
  	}	  	
  }

  public static void sort (List list) {
  	qSort(list,0,list.size()-1,new StdComparator());
  }

  static class StdComparator implements Comparator{
   	public int compare(Object one, Object two){
   	 	Comparable c1 = (Comparable)one;
   	 	Comparable c2 = (Comparable)two;
   	 	if (one != null){
   	 		return c1.compareTo(c2);
   	 	}  	 	
 	 	if (two != null){
   	 	 	return (-c2.compareTo(c1)); 	 	
   	 	} 	 	
   		return 0;
   	}
  }

  public static void sort (List list, Comparator c) {
        	qSort(list,0,list.size()-1,c);
  }

  private static void qSort(List ls, int l, int h ,Comparator c){
  	if (h-l < 3) {
  	 	fastSort(ls,l,h-l+1,c);
  	}
  	else {
  	    int pivot = h;
  	    swap(ls,(l+h)/2,h);
  	    Object pvt = ls.get(pivot); 	
  	    for (int i=l ; i < pivot ; i++){
  	     	if (c.compare(pvt, ls.get(i)) < 0){
  	     	 	if (i+1 == pivot){
  	     	 		swap(ls,i,pivot);
  	     	 		pivot--;
  	     	 	}
  	     	 	else {
  	     	 		ls.set(pivot--,ls.get(i));
  	     			Object o = ls.set(pivot,pvt);
  	     			ls.set(i--,o);
  	     	 	}
  	     	}  	     	
  	    }
  	    qSort(ls,l,pivot-1,c);
  	    qSort(ls,pivot+1,h,c);
  	}
  }
  /**
  ** only useable if size 3 or less ...
  */
  private static void fastSort(List ls, int l, int s, Comparator c){
	if (s >= 2) {
		if (c.compare(ls.get(l), ls.get(l+1)) > 0){
			Object o = ls.set(l+1,ls.get(l));
			ls.set(l,o);   	
		}
	}
	if (s >= 3){
		if (c.compare(ls.get(l+1), ls.get(l+2)) > 0){
			Object o = ls.set(l+1,ls.get(l+2));
			ls.set(l+2,o);   	
			if (c.compare(ls.get(l), ls.get(l+1)) > 0){
				o = ls.set(l+1,ls.get(l));
				ls.set(l,o);   	
			}
		}		
	}
  }

  public static void swap(List ls, int i, int j){
    Object o = ls.set(i,ls.get(j));
    ls.set(j,o);   	
  }

  public static Set singleton(Object e){
   	HashSet hs = new HashSet(2);
   	hs.add(e);
   	return new FinalSet(hs);
  }

  public static List singletonList(Object e){
   	ArrayList hs = new ArrayList(2);
   	hs.add(e);
   	return new FinalList(hs);
  }

  public static Map singletonMap(Object k, Object v){
   	HashMap hs = new HashMap(3);
   	hs.put(k,v);
   	return new FinalMap(hs);
  }

  public static Collection synchronizedCollection(Collection c){
	return new SyncedCollection(c);
  }

  public static List synchronizedList(List c){
	return new SyncedList(c);
  }

  public static Map synchronizedMap(Map c){
	return new SyncedMap(c);
  }

  public static Set synchronizedSet(Set c){
	return new SyncedSet(c);
  }

  public static SortedMap synchronizedSortedMap(SortedMap c){
	return new SyncedSortedMap(c);
  }

  public static SortedSet synchronizedSortedSet(SortedSet c){
	return new SyncedSortedSet(c);
  }

  public static Collection unmodifiableCollection(Collection c){
	return new FinalCollection(c);
  }

  public static List unmodifiableList(List c){
	return new FinalList(c);
  }

  public static Map unmodifiableMap(Map c){
	return new FinalMap(c);
  }

  public static Set unmodifiableSet(Set c){
	return new FinalSet(c);
  }

  public static SortedMap unmodifiableSortedMap(SortedMap c){
	return new FinalSortedMap(c);
  }

  public static SortedSet unmodifiableSortedSet(SortedSet c){
	return new FinalSortedSet(c);
  }

// unmodifiable Helper Classes

  private static class UnmodIterator implements Iterator{
       	private Iterator it;
       	
       	public UnmodIterator(Iterator i){
       	    	it = i;       	    	
       	}
       	public boolean hasNext(){
       	 	return it.hasNext();
       	}
       	public Object next(){
       	 	return it.next();
       	}
       	public void remove(){
       	 	throw new UnsupportedOperationException();
       	}
  }

  private static class FinalCollection extends AbstractCollection implements Serializable {
    	private Collection back;
    	
    	public FinalCollection(Collection bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
    	
    	public int size(){
    	 	return back.size();
    	}    	
        public Iterator iterator(){
         	return new UnmodIterator(back.iterator());
        }
  }

  private static class FinalList extends AbstractList implements Serializable {
    	private List back;
    	
    	public FinalList(List bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
    	public int size(){
    	 	return back.size();
    	}
    	public Object get(int idx){
    	 	return back.get(idx);
    	}	    	
        public Iterator iterator(){
         	return new UnmodIterator(back.iterator());
        }	
  }

  private static class FinalSet extends AbstractSet implements Serializable {
    	private Set back;
    	
    	public FinalSet(Set bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
    	
    	public int size(){
    	 	return back.size();
    	}
	    	
        public Iterator iterator(){
         	return new UnmodIterator(back.iterator());
        }	

  }

  private static class FinalSortedSet extends AbstractSet implements SortedSet, Serializable {
    	private SortedSet back;
    	
    	public FinalSortedSet(SortedSet bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
    	public int size(){
    	 	return back.size();
    	}
	    	
        public Iterator iterator(){
         	return new UnmodIterator(back.iterator());
        }
        public Object first(){
         	return back.first();
        }	
        public Object last(){
         	return back.last();
        }
        public SortedSet headSet(Object toV){
         	return new FinalSortedSet(back.headSet(toV));
        }	
        public SortedSet subSet(Object fromV, Object toV){
         	return new FinalSortedSet(back.subSet(fromV, toV));
        }	
        public SortedSet tailSet(Object fromV){
         	return new FinalSortedSet(back.tailSet(fromV));
        }	
  	public Comparator comparator(){
  	 	return back.comparator();
  	}
  }

  private static class FinalMap extends AbstractMap implements Serializable {
    	private Map back;
    	
    	public FinalMap(Map bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
      	public int size(){
    	 	return back.size();
    	}
        public Set entrySet(){
         	return new FinalSet(back.entrySet());
        }
  }

  private static class FinalSortedMap extends AbstractMap implements SortedMap, Serializable {
    	private SortedMap back;
    	
    	public FinalSortedMap(SortedMap bck){
    	 	if (bck == null){
    	 	 	throw new NullPointerException();
    	 	}
    	 	back = bck;
    	}
    	public int size(){
    	 	return back.size();
    	}	    	
        public Set entrySet(){
         	return new FinalSet(back.entrySet());
        }
        public Object firstKey(){
         	return back.firstKey();
        }	
        public Object lastKey(){
         	return back.lastKey();
        }
        public SortedMap headMap(Object toV){
         	return new FinalSortedMap(back.headMap(toV));
        }	
        public SortedMap subMap(Object fromV, Object toV){
         	return new FinalSortedMap(back.subMap(fromV, toV));
        }	
        public SortedMap tailMap(Object fromV){
         	return new FinalSortedMap(back.tailMap(fromV));
        }	
  	public Comparator comparator(){
  	 	return back.comparator();
  	}
  }

// synchronized Helper Classes

  private static class SyncedCollection implements Collection, Serializable {
  	protected Collection back;

        public SyncedCollection(Collection c){
         	if (c == null) {
         	 	throw new NullPointerException();
         	}
         	back = c;
        }

  	public synchronized boolean add(Object e){
  	 	return back.add(e);
  	}
  	public synchronized boolean addAll(Collection e){
  	 	return back.addAll(e);
  	}
  	public synchronized void clear(){
  	 	back.clear();
  	}
  	public synchronized boolean contains(Object e){
  	 	return back.contains(e);
  	}
  	public synchronized boolean containsAll(Collection e){
  	 	return back.containsAll(e);
  	}
	public synchronized boolean isEmpty(){
	 	return back.isEmpty();
	}  	
	public synchronized Iterator iterator(){
	 	return back.iterator();
	}
	public synchronized boolean remove(Object e){
	 	return back.remove(e);
	}
	public synchronized boolean removeAll(Collection c){
	 	return back.removeAll(c);
	}
	public synchronized boolean retainAll(Collection c){
	 	return back.retainAll(c);
	}
	public synchronized int size(){
	 	return back.size();
	}
	public synchronized Object[] toArray(){
	 	return back.toArray();
	}
	public synchronized Object[] toArray(Object [] a){
	 	return back.toArray(a);
	}
  }

  private static class SyncedList extends SyncedCollection implements List, Serializable {
   	private List list;
  	
   	public SyncedList(List l){
   	 	super(l);
   	 	list = l;
   	}
   	public synchronized List subList(int f, int t){
   	 	return new SyncedList(list.subList(f,t));
   	}
   	public synchronized void add(int p, Object t){
   	 	list.add(p,t);
   	}
   	public synchronized boolean addAll(int p, Collection t){
   	 	return list.addAll(p,t);
   	}
   	public synchronized ListIterator listIterator(){
   		return list.listIterator();
   	}
   	public synchronized ListIterator listIterator(int p){
   		return list.listIterator(p);
   	}
   	public synchronized int indexOf(Object o){
   		return list.indexOf(o);
   	}
   	public synchronized int lastIndexOf(Object o){
   		return list.lastIndexOf(o);
   	}
   	public synchronized Object remove(int idx){
   		return list.remove(idx);
   	}   	
   	public synchronized Object get(int idx){
   		return list.get(idx);
   	}   	
   	public synchronized Object set(int idx,Object e){
   		return list.set(idx,e);
   	}   	
   	public synchronized boolean equals(Object e){
   	 	return list.equals(e);
   	}	
   	public synchronized int hashCode(){
   	 	return list.hashCode();
   	}

  }

  private static class SyncedSet extends SyncedCollection implements Set, Serializable {
   	public SyncedSet(Set s){
   	 	super(s);
   	}	
   	public synchronized boolean equals(Object e){
   	 	return back.equals(e);
   	}	
   	public synchronized int hashCode(){
   	 	return back.hashCode();
   	}
  }

  private static class SyncedSortedSet extends SyncedSet implements SortedSet, Serializable {
   	private SortedSet sset;
   	
   	public SyncedSortedSet(SortedSet s){
   	 	super(s);
   		sset = s;
   	}	

   	public synchronized Comparator comparator(){
   	 	return sset.comparator();
   	}
   	public synchronized Object first(){
   	 	return sset.first();
   	}
   	public synchronized Object last(){
   	 	return sset.last();
   	}
   	public synchronized SortedSet headSet(Object toV){
   	 	return new SyncedSortedSet(sset.headSet(toV));
   	}
   	public synchronized SortedSet subSet(Object fromV, Object toV){
   	 	return new SyncedSortedSet(sset.subSet(fromV,toV));
   	}
   	public synchronized SortedSet tailSet(Object fromV){
   	 	return new SyncedSortedSet(sset.tailSet(fromV));
   	}
  }

  private static class SyncedMap implements Map, Serializable {
  	protected Map back;

    public SyncedMap(Map c){
      if (c == null) {
        throw new NullPointerException();
      }
      back = c;
    }
   	
   	public synchronized boolean equals(Object e){
   	 	return back.equals(e);
   	}	
   	public synchronized int hashCode(){
   	 	return back.hashCode();
   	}
   	public synchronized Object get(Object idx){
   		return back.get(idx);
   	}   	
  	public synchronized Object put(Object key, Object val){
  	 	return back.put(key,val);
  	}
  	public synchronized void putAll(Map e){
  	 	back.putAll(e);
  	}
  	public synchronized void clear(){
  	 	back.clear();
  	}
  	public synchronized boolean containsKey(Object e){
  	 	return back.containsKey(e);
  	}
  	public synchronized boolean containsValue(Object e){
  	 	return back.containsValue(e);
  	}
  	public synchronized boolean isEmpty(){
	  	return back.isEmpty();
	  }  	
    public synchronized Object remove(Object e){
	    return back.remove(e);
	  }
	  public synchronized int size(){
	    return back.size();
	  }
	  public synchronized Set entrySet(){
	 	  return new SyncedSet(back.entrySet());
	  }
	  public synchronized Set keySet(){
	 	  return new SyncedSet(back.keySet());
	  }
	  public synchronized Collection values(){
	 	  return new SyncedCollection(back.values());
	  }
  }

  private static class SyncedSortedMap extends SyncedMap implements SortedMap, Serializable {
  	private SortedMap smap;
  	
  	public SyncedSortedMap(SortedMap m){
  	 	super(m);
  	 	smap =m;
  	}
  	
   	public synchronized Comparator comparator(){
   	 	return smap.comparator();
   	}
   	public synchronized Object firstKey(){
   	 	return smap.firstKey();
   	}
   	public synchronized Object lastKey(){
   	 	return smap.lastKey();
   	}
   	public synchronized SortedMap headMap(Object toV){
   	 	return new SyncedSortedMap(smap.headMap(toV));
   	}
   	public synchronized SortedMap subMap(Object fromV, Object toV){
   	 	return new SyncedSortedMap(smap.subMap(fromV,toV));
   	}
   	public synchronized SortedMap tailMap(Object fromV){
   	 	return new SyncedSortedMap(smap.tailMap(fromV));
   	}
  	
  }
}
                             	
