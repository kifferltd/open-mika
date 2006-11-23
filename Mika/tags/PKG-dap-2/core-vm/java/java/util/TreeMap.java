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

/**
**	Implentation of a SortedMap based on tree-structure.
**	The internal tree is a RED-BLACK tree.  The basic Tree Algorithms were based on the
**	C-implementation found at:
**
**      http://www.people.fas.harvard.edu/~toub/cs50/projects/datastructs/redblack/redblack.html
**
**      So kind thanks for the people from Harvard for making this code public ...
**      This code has a small optimisation for find - remove operation (this is used by the Set Iterators)
**      A remove Operation doesn't need browse through the tree if the key was the last one used by a 'get',
** 	'contiansKey', 'lastKey' or 'firstKey' (this means removing the first key or the last key can be done
**	without comparing the keys to determine which key-value pair to remove.)
**	This optimisation is very usefull for all SortedMaps return by this TreeMap.
*/
public class TreeMap extends AbstractMap implements SortedMap, Cloneable, java.io.Serializable {
	
  static boolean RED = true;
  static boolean BLACK = false;
	private static final long serialVersionUID = 919286545866124006L;

  transient Node leaf = new Node();
  transient Node root = leaf;
  transient Node lastFind;
  transient int modcount;
  transient int size;

  Comparator comparator;
	
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    int count = s.readInt();
    Object key;
    for (int i=0; i < count ; i++){
    	key = s.readObject();
     	put(key, s.readObject());
    }
	}

	private void writeObject(ObjectOutputStream s) throws IOException{
   	s.defaultWriteObject();
   	s.writeInt(size);
   	Iterator it = entrySet().iterator();
   	Map.Entry me;
   	for (int i=0; i < size ; i++){
   	  me = (Map.Entry)it.next();
   	  s.writeObject(me.getKey());
   	  s.writeObject(me.getValue());
   	}
	}

  public TreeMap() {}

  public TreeMap(Map m) {
  	this.putAll(m);
  }

  public TreeMap(SortedMap s) {
  	comparator = s.comparator();
  	this.putAll(s);
  }
  public TreeMap(Comparator comp) {
 	  comparator = comp;   	
  }

  private void rotateLeft(Node x) {
   	Node y = x.right;
    x.right = y.left;
    if (y.left != leaf){ y.left.parent = x; }
    if (y != leaf){ y.parent = x.parent; }
    if (x.parent != null) {
     	if (x == x.parent.left) {
     		x.parent.left = y;
     	}
     	else {
     	 	x.parent.right = y;	
     	}		       	
    }
    else { root = y; }
    y.left = x;
    if (x != leaf) { x.parent = y; }                 	
  }

  private void rotateRight(Node n) {
    Node y = n.left;
    n.left = y.right;
    if (y.right != leaf){ y.right.parent = n; }
    if (y != leaf){ y.parent = n.parent; }
    if (n.parent != null) {
     	if (n == n.parent.right) {
     		n.parent.right = y;
     	}
     	else {
     	 	n.parent.left = y;	
     	}		       	
    }
    else { root = y; }
    y.right = n;
    if (n != leaf) { n.parent = y; }                 	
  }

  private void insertFixup(Node n) {
  	Node y;
   	while (n != root && n.parent.color == RED) {
   	 	if (n.parent == n.parent.parent.left){
   	 		y = n.parent.parent.right;
        if( y.color == RED ) {//uncle is red
        	n.parent.color = BLACK;
        	y.color = BLACK;
        	n.parent.parent.color = RED;
        	n = n.parent.parent;
        }
        else { // uncle is black
         	if (n == n.parent.right) {
         	 	n = n.parent;
         	 	rotateLeft(n);
         	}
         	n.parent.color = BLACK;
         	n.parent.parent.color = RED;
         	rotateRight(n.parent.parent);
        }
   		}
   		else {
   		 	y = n.parent.parent.left;
   		 	if (y.color == RED){//uncle is red
           n.parent.color = BLACK;
           y.color = BLACK;
           n.parent.parent.color = RED;
           n = n.parent.parent;
   		 	}
        else { // uncle is black
         	if (n == n.parent.left) {
         	 	n = n.parent;
         	 	rotateRight(n);
         	}
         	n.parent.color = BLACK;
         	n.parent.parent.color = RED;
         	rotateLeft(n.parent.parent);
        }
   		}
   	}	
   	root.color = BLACK;	
  }

  public Object put(Object key, Object value) {
   	Node current=root;
   	Node parent=null;
   	int comp;
   	while (current != leaf) {
   		comp = compare(key, current.key);
   		if (comp == 0) {
   			key = current.value;
   			current.value = value;
   			return key;
   		}
   		parent = current;
   		current = (comp < 0)? current.left : current.right;	
   	}
   	Node n = new Node(parent,key,value,leaf);
   	modcount++;
   	if (parent != null) {
     	if (compare(key,parent.key) < 0) {
     		parent.left = n; 	
     	}
     	else {
     	 	parent.right = n;
     	}
   	}
   	else {
   	 	root = n;
   	}
   	insertFixup(n);
   	lastFind=null;
   	size++;
   	return null;
  }

  private void deleteFixup(Node n){
  	Node y;
   	while (n != root && n.color == BLACK) {
   	 	if (n == n.parent.left){
   	 		y = n.parent.right;
        if ( y.color == RED ) {
        	y.color = BLACK;
        	n.parent.color = RED;
        	rotateLeft(n.parent);
        	y = n.parent.right;
        }
        if ( y.left.color == BLACK && y.right.color == BLACK ) {
         	y.color = RED;
         	n = n.parent;
        }
        else {
        	if (y.right.color == BLACK) {
        	 	y.left.color = BLACK;
        	 	y.color = RED;
        	 	rotateRight(y);
        	 	y = n.parent.right;
        	}
        	y.color = n.parent.color;
        	n.parent.color = BLACK;
        	y.right.color = BLACK;
        	rotateLeft(n.parent);
        	n = root;
        }
   		}
   		else {
   		 	y = n.parent.left;
   		 	if (y.color == RED){
          y.color = BLACK;
          n.parent.color = RED;
          rotateRight(n.parent);
          y = n.parent.left;
   		 	}
   		 	if ( y.left.color == BLACK && y.right.color == BLACK ) {
         	y.color = RED;
         	n = n.parent;         		 	
   		 	}
        else {
         	if (y.left.color == BLACK) {
         	 	y.right.color = BLACK;
         	 	y.color = RED;
         	 	rotateLeft(y);
         	 	y = n.parent.left;
         	}
         	y.color = n.parent.color;
         	n.parent.color = BLACK;
         	y.left.color = BLACK;
         	rotateRight(n.parent);
        	n = root;
        }
   		}
   	}	
   	n.color = BLACK;	

  }

  public Object remove(Object key){
   	Node n,y,x;
   	if (lastFind != null && compare(key, lastFind.key) == 0) {
   	 	n = lastFind;
   	}
   	else {
   	 	n = root;
   	 	int comp;
   	 	while (n != leaf) {
   	 	 	comp = compare(key , n.key);
   	 	 	if (comp == 0) { break; }
   	 	 	else {
   	 	 	 	n = (comp < 0) ? n.left : n.right;
   	 	 	}
   	 	}
   	 	if (n == leaf) { return null; }
   	}
   	key = n.value;
   	modcount++;
   	if (n.right == leaf || n.left == leaf) {
   	   	y = n;
   	}
   	else {
   	 	y = n.right;
   	 	while (y.left != leaf) { y = y.left; }
   	}
   	x = (y.left != leaf) ? y.left : y.right;
   	x.parent = y.parent;
   	if (y.parent != null) {
 	   	if (y == y.parent.left) {
 	   	 	y.parent.left = x;
 	   	}
 	   	else {
 	   	 	y.parent.right = x;
 	   	}
   	}
   	else {
   		root = x;
   	}
   	if ( y != n ) {
   	 	n.key = y.key;
   	 	n.value = y.value;
   	}
   	if (y.color == BLACK) { deleteFixup(x); }
   	lastFind = null;
   	size--;
   	return key;
           	
  }

  public Object get(Object key) {
    Node n = root;
    int comp;
    while ( n != leaf ) {
     	comp = compare(key , n.key);
      if (comp == 0) {
       	lastFind = n;
       	return n.value;
      }
      else {
      	n = (comp < 0 )? n.left : n.right;
      }
    }
    return null;         	
  }

  Node getNode(Object key) {
    Node n = root;
    while ( n != leaf ) {
     	int comp = compare(key , n.key);
      if (comp == 0) {
       	lastFind = n;
       	return n;
      }
      else {
      	n = (comp < 0 )? n.left : n.right;
      }
    }
    return null;         	
  }


  public boolean containsKey(Object key) {
   	Node n = root;
   	int comp;
   	while ( n != leaf ) {
   	 	comp = compare(key , n.key);
      if (comp == 0) {
       	lastFind = n;
       	return true;
      }
      else {
      	n = (comp < 0 )? n.left : n.right;
      }
   	}
   	return false;         	
  }

  /**
  ** copies the behaviour of the iterator but without generating all the extra objects
  */
  public boolean containsValue(Object o){
    if(size > 0){
      Node n = root;
      while(n.right != leaf){
        n = n.right;
      }
      if(o == null){
        while(n != null){
          if(n.value == null){
            return true;
          }
          n = nextNode(n);
        }
      }
      else {
        while(n != null){
          if(o.equals(n.value)){
            return true;
          }
          n = nextNode(n);
        }
      }
    }
    return false;
  }

  public Set entrySet(){
   	return new SubTreeSet(leaf,leaf);
  }

  public Set keySet() {
   	return new KeySubSet(leaf,leaf);
  }

  public Collection values(){
   	return new ValueSubSet(leaf,leaf);
  }

  public void putAll(Map m){
		Iterator it = m.entrySet().iterator();
		Map.Entry me;
		while (it.hasNext()){
     	me = (Map.Entry)it.next();
     	put(me.getKey(),me.getValue());
		}		         	
  }

  public void clear() {
  	lastFind = null;
   	leaf.parent=null;
   	root = leaf;
   	size = 0;
   	modcount++;
  }

  public Object clone(){
   	TreeMap s = null;
   	try { s = (TreeMap) super.clone(); }
   	catch(CloneNotSupportedException cnse){}
   	s.clear();
   	s.putAll(this);
   	return s;         	
  }

  public Comparator comparator() {
   	return comparator;
  }

  public Object firstKey(){
   	if (size == 0) {
   	 	throw new NoSuchElementException();
   	}
   	Node n = root;
   	while (n.left != leaf) { n = n.left; }
   	lastFind = n;
   	return n.key;         	
  }

  public Object lastKey(){
   	if (size == 0) {
   	 	throw new NoSuchElementException();
   	}
   	Node n = root;
   	while (n.right != leaf) { n = n.right; }
   	lastFind = n;
   	return n.key;         	
  }

  public boolean isEmpty(){
   	return (size == 0);
  }

  public int size(){
   	return size;
  }

  public SortedMap headMap(Object toV) {
   	return new SubTree(leaf,toV);
  }
  public SortedMap tailMap(Object fromV) {
   	return new SubTree(fromV,leaf);
  }
  public SortedMap subMap(Object fromV, Object toV) {
  	if ( compare(fromV,toV) > 0 ){
  	 	throw new IllegalArgumentException();
	  }
   	return new SubTree(fromV,toV);
  }

  int compare(Object nkey, Object key) {
   	int ret;
   	if (comparator != null) {
   		ret = comparator.compare(nkey, key);
   	}
   	else {
	    if (nkey == null) {
	    	if (key == null){
	            ret = 0;
	        }
	        else {
	            ret = -((Comparable)key).compareTo(nkey);
	        }
	    }
	    else {
	    	ret = ((Comparable)nkey).compareTo(key);
	    }         	
   	}
   	return ret;
  }

// InnerClass convenience Methods ...
  Node locateLowerNode(Object from) throws NoSuchElementException {
    Node next = root;
    if (next == leaf){
 		 throw new NoSuchElementException("Map is Empty");	 	
 	 }
    if (from == leaf){
      while (next.left != leaf){ next = next.left; }
      return next;
    }
    if (compare(from, next.key)<=0){
      while (next.left != leaf){ next = next.left; }
    }
    while (next != null){
      if (compare(from,next.key)<=0){
     	 return next;
      }  		  	
      next = nextNode(next);
    }
    throw new NoSuchElementException();	  	
  }

  Node locateUpperNode(Object to) throws NoSuchElementException {
    Node next = root;
    if (next == leaf){
     	throw new NoSuchElementException("Map is Empty");	 	
    }
    if (leaf == to){
    while (next.right != leaf){ next = next.right; }
     	return next;  	
    }
    if (compare(to, next.key)<=0){
      while (next.left != leaf){ next = next.left; }
    }
    if (compare(to, next.key)<=0){
      throw new NoSuchElementException();	  	
    }
    Node prev=next;
    next = nextNode(next);
    while (next != null){
      if (compare(to,next.key)<=0){
     	  return prev;
      }  		  	
      prev = next;
      next = nextNode(next);
    }
    return prev;
  }

  Node nextNode(Node next){
    Object key = next.key;
    while (next != null){
      if (compare(key,next.key)<0){
        return next;
      } 	  			
    	if (next.right != leaf && compare(key,next.right.key)<0){
     		next = next.right;
     		while (next.left != leaf){
     	 		next = next.left;
     		}
     		return next;
    	}
    	next = next.parent;
    }
    return next;
  }

  Object locateLowerKey(Object from, Object to) {
  	Object key = (from == leaf ? firstKey(): locateLowerNode(from).key);
  	if (to == leaf || compare(key,to)<0){
  	 	return key;
  	}
 	  throw new NoSuchElementException("Map is Empty");	 		
  }

  Object locateUpperKey(Object from, Object to) {
  	Object key = (to == leaf ? lastKey(): locateUpperNode(to).key);
  	if (from == leaf || compare(key,from)>=0){
  	 	return key;
  	}
 	 throw new NoSuchElementException("Map is Empty");	 		
  }

  int calculateSize(Object from,Object to){
    int size = 1;
    try {
  	  Node next = locateLowerNode(from);
  	  Object key = next.key;
 	    if (to != leaf && compare(next.key,to) >= 0){       			
		    return 0;		
	    }
    	while (next != null){   		
    		if (compare(key,next.key)<0){
    	 	 	if (to != leaf && compare(next.key,to) >= 0){       			
     				break;
     			}                   			
    	 	 	size++;
     			key = next.key;
    		}else {
    			if (next.right != leaf && compare(key,next.right.key)<0){
    				next = next.right;
    				while (next.left != leaf){
    	 				next = next.left;
    				}
    	 	 		if (to != leaf && compare(next.key,to) >= 0){       			
           		break;
           	}                   			
    		 	 	size++;
           	key = next.key;
     			}
      		else {
      			next = next.parent;
      		}
      	}
    	}
    }
    catch(NoSuchElementException nse){
      size = 0;
    }
    return size;
  }
// END of InnerClass convenience Methods ...

  static final class Node {

   	public Node parent;
 	  public Node left;
	  public Node right;
	
  	public boolean color;
	  public Object key;
	  public Object value;
	
	  public Node(){
  		parent = null;
  		left = this;
  		right = this;
  		color = BLACK;	 	
  	}
  	
	  public Node(Node p, Object k, Object v, Node leaf) {
	 	  key = k;
	 	  value = v;
	    parent = p;
		  color = RED;
		  left = leaf;
		  right = leaf;
	  }
  }

  private class SubTree extends AbstractMap implements SortedMap {
 	
 	  private Object from;
 	  private Object to;
 	
  	private void checkRange(Object k){
  	 	if ((from != leaf && compare(k,from) < 0)
  		      || (to != leaf && compare(k,to) >= 0)){
  		 	throw new IllegalArgumentException();
  		}
  	}
 	
  	public SubTree(Object f, Object t){
  	   	from = f;
  	   	to = t;
  	} 	
  	
  	public Object firstKey() {
 	 	  return locateLowerKey(from,to); 	 	
  	}	
  	 	
 	  public Object lastKey() {
	 	  return locateUpperKey(from,to); 	 	
 	  }
 	  	
 	  public Comparator comparator() {
 	 	  return comparator;
 	  }
 	   	
  	public SortedMap headMap(Object toV){
  	 	checkRange(toV);
  	 	return new SubTree(from,toV);
  	}
  	
  	public SortedMap subMap(Object fr, Object t){
 	   	if ( compare(fr,t) > 0 ){
 	   	 	throw new IllegalArgumentException();
 	   	}
  	 	checkRange(fr);
  	 	checkRange(t);
  	 	return new SubTree(fr,t);
  	}
 	
  	public SortedMap tailMap(Object fr){
  	 	checkRange(fr);
  	 	return new SubTree(fr,to);
  	}
 	
  	public Set entrySet(){
  	 	return new SubTreeSet(from,to);
  	}	

    public Set keySet() {
     	return new KeySubSet(from,to);
    }

    public Collection values(){
     	return new ValueSubSet(from,to);
    }
 	
   	public Object put(Object key, Object value){
   	 	checkRange(key);
   	 	return TreeMap.this.put(key,value);
   	}
   	public Object remove(Object key){
   	 	if ((from != leaf && compare(key,from) < 0)
   		      || (to != leaf && compare(key,to) >= 0)){
      	return null;
      }
      return TreeMap.this.remove(key);
   	}	
  }

  private class KeySubSet extends AbstractSet {
  	
  	private Object from;
  	private Object to;
  	private int smc;
  	private int size;
  	
  	public KeySubSet(Object fr, Object t){
  	 	from = fr;
  	 	to = t;
  	 	smc = -1;
  	}

  	public int size(){
  		if (smc != modcount) {
  			size = calculateSize(from,to);
  		}
  	 	return size;
  	}
  	 	
  	public Iterator iterator(){
      Node n = null;
      try {
        	n = locateLowerNode(from);
      }
      catch(NoSuchElementException nse){}//null will do in the constructor
  	 	return new SetIterator(n,to,1);
  	}
  }

  private class ValueSubSet extends AbstractCollection {
  	
  	private Object from;
  	private Object to;
  	private int smc;
  	private int size;
  	
  	public ValueSubSet(Object fr, Object t){
  	 	from = fr;
  	 	to = t;
  	 	smc = -1;
  	}

  	public synchronized int size(){
  		if (smc != modcount) {
  			size = calculateSize(from,to);
  		}
  	 	return size;
  	}
  	 	
  	public Iterator iterator(){
      Node n = null;
      try {
        	n = locateLowerNode(from);
      }
      catch(NoSuchElementException nse){}//null will do in the constructor
  	 	return new SetIterator(n,to,-1);
  	}

  }

  private class SubTreeSet extends AbstractSet {
 	
 	private Object from;
 	private Object to;
 	private int smc;
 	private int size;
 	
 	public SubTreeSet(Object fr, Object t){
 	 	from = fr;
 	 	to = t;
 	 	smc = -1;
 	}

 	public synchronized int size(){
 		if (smc != modcount) {
 			size = calculateSize(from,to);
 		}
 	 	return size;
 	}
 	 	
 	public Iterator iterator(){
    Node n = null;
    try {
     	n = locateLowerNode(from);
    }
    catch(NoSuchElementException nse){}//null will do in the constructor
 	 	return new SetIterator(n,to,0);
 	}
 }

  private class SetIterator implements Iterator{
  	
  	private Node node;
  	private int mc;
  	private Object end;	
  	private Node prevKey;
  	private int rType;
  	
  	public SetIterator(Node n, Object to, int returnType){
     	if (n == leaf){
     	 	n = null;
     	}
     	node = n;
     	end = to;
     	mc = modcount;
     	prevKey=leaf;
     	rType = returnType;
  	}

  	public boolean hasNext(){
  	  return (node != null);
  	}
  	 	
  	public Object next(){
  		if (node == null){
  		 	throw new NoSuchElementException();
  		}
  		if (mc != modcount) {
  		 	throw new ConcurrentModificationException();
  		}
  	 	prevKey = node;
  	 	Object ret;
  	 	if (rType == 0) {
  	 	 	ret = new MapEntry(node);
  	 	}
  	 	else if (rType == 1){
  	 	  ret = node.key;
  	 	}
  	 	else {
  	 	 	ret = node.value;
  	 	}
  	 	node = nextNode(node);
  	 	if (end != leaf && node != null && compare(node.key,end) >= 0){
  	 		node = null;
  	 	}
  	 	return ret;
  	}
  	
  	public void remove(){
  		if (prevKey == leaf){
  		 	throw new IllegalStateException();
  		}
  		if (mc != modcount) {
  		 	throw new ConcurrentModificationException();
  		}
  		lastFind = prevKey;
  		if(node != null){
    		Object key = node.key;
    		TreeMap.this.remove(prevKey.key);
  		  node = getNode(key);
  		}
  		else {
    		TreeMap.this.remove(prevKey.key);
  		}
  		prevKey = leaf; 		
  		mc++;
  	}
  }

  private class MapEntry implements Map.Entry {

    private Object key;
    private Object value;
  	
  	public MapEntry(Node n) {
     	key = n.key;
     	value = n.value;
    }

    public Object getKey() {
    	return key;
    }

    public Object getValue() {
    	return value;
    }

    public Object setValue(Object nv) {
    	Object old = value;
    	TreeMap.this.put(key,nv);
      value = nv;
      return old;
    }

    public boolean equals(Object o) {
    	if (!(o instanceof Map.Entry))return false;
    	Map.Entry e = (Map.Entry)o;        	
    	return ( (key == null ? e.getKey()==null : key.equals(e.getKey())) &&
              (value == null ? e.getValue()==null : value.equals(e.getValue())));
    }

    public int hashCode() {
    	int kc = key == null ? 0 : key.hashCode();
    	int vc = value == null ? 0 : value.hashCode();
    	return kc ^ vc;
    }
  }
}