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


package java.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class TreeSet extends AbstractSet implements SortedSet, Cloneable, java.io.Serializable {

	final static Object value = new Object();

	private transient TreeMap backMap;

	private static final long serialVersionUID = - 2479143000061671589L;
	
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
	        backMap = new TreeMap((Comparator)s.readObject());
	        int size = s.readInt();
	        for (int i=0; i < size ; i++){
	         	add(s.readObject());
	        }
	}

	private void writeObject(ObjectOutputStream s) throws IOException{
	     	s.writeObject(backMap.comparator());
	     	int size = backMap.size();
	     	s.writeInt(size);
	     	Iterator it = iterator();
	     	for (int i=0; i < size ; i++){
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
         	return (backMap.put(key,value) == null);
        }

        public boolean remove(Object key){
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

        public Object clone(){
           	TreeSet ts = null;
           	try {
           	 	ts = (TreeSet) super.clone();
           	}	
           	catch(CloneNotSupportedException cnse){}
           	ts.backMap = (TreeMap)this.backMap.clone();
        	return ts;
        }

        public Comparator comparator() {
         	return backMap.comparator();
        }

        public Object first(){
         	return backMap.firstKey();         	
        }

        public Object last(){
         	return backMap.lastKey();         	
        }

        public boolean isEmpty(){
         	return backMap.isEmpty();
        }

        public int size(){
         	return backMap.size();
        }

        public Iterator iterator() {
         	return backMap.keySet().iterator();
        }

        public SortedSet headSet(Object toV) {
         	return new SubTreeSet(toV,false,backMap);
        }
        public SortedSet tailSet(Object fromV) {
         	return new SubTreeSet(fromV,true,backMap);
        }
        public SortedSet subSet(Object fromV, Object toV) {
         	return new SubTreeSet(fromV,toV,backMap);
        }

 private static class SubTreeSet extends AbstractSet implements SortedSet {
	
 	private SortedMap back;
 	
 	public SubTreeSet(Object o, boolean tail, SortedMap m){
 	 		back = (tail ? m.tailMap(o): m.headMap(o));
 	}
 	public SubTreeSet(Object from, Object to, SortedMap m){
 	 		back = m.subMap(from, to);
 	}
 	
        public int size(){
         	return back.size();
        }

        public boolean add(Object key){
         	return (back.put(key,value) == null);
        }

        public Iterator iterator() {
         	return back.keySet().iterator();
        }

        public Object first(){
         	return back.firstKey();         	
        }

        public Object last(){
         	return back.lastKey();         	
        }
        public Comparator comparator() {
         	return back.comparator();
        }
        public SortedSet headSet(Object toV) {
         	return new SubTreeSet(toV,false,back);
        }
        public SortedSet tailSet(Object fromV) {
         	return new SubTreeSet(fromV,true,back);
        }
        public SortedSet subSet(Object fromV, Object toV) {
         	return new SubTreeSet(fromV,toV,back);
        }
 }
}