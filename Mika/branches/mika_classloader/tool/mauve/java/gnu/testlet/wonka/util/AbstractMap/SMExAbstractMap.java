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

package gnu.testlet.wonka.util.AbstractMap; //complete the package name ...

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;


/**
*  this class extends AbstractMap and is used to test java.util.AbstractMap
*  (since it is an abstract class)
*  used by SMAbstractMapTest
*/
public class SMExAbstractMap extends AbstractMap
{
	Vector keys = new Vector();
	Vector values = new Vector();
	private boolean edit = true;
	
	boolean deleteInAM(Object e) {
//		System.out.println("removing in AM");
	 	if  (!keys.contains(e)) return false;
	 	values.remove(keys.indexOf(e));
	 	return keys.remove(e);
	}
	
	public Vector getKeyV() {
		return (Vector)keys.clone();
	}
	public Vector getValuesV() {
		return (Vector)values.clone();
	}
	
	public SMExAbstractMap(){
		super();
	}
	
	public Set entrySet() {
		return  new ESet();
	}

	public Object put(Object key, Object value) {
		if (edit) {
			if (keys.contains(key)) {
				return values.set(keys.indexOf(key),value);
			}
			values.add(value);
			keys.add(key);
			return null;
		}
		return super.put(key,value);
	}
	
	public void set_edit(boolean b) {
		edit = b;
	}
	
  private class ESet extends AbstractSet {
	
		public Iterator iterator() {
			return new EIterator();
		}
	
	        public int size() {
	        	return keys.size();
	        }
	

  }
	
  private class Entry implements java.util.Map.Entry {

  	private Object key;
  	private Object value;
  	
  	public Entry(Object k, Object v) {
         	key = k;
         	value = v;
        }

        public Object getKey() {
//        	System.out.println("DEBUG -- in HT MapEntry -- getKey is called ->"+key);
        	return key;
        }

        public Object getValue() {
//        	System.out.println("DEBUG -- in HT MapEntry -- getValue is called -> "+value);
        	return value;
        }

        public Object setValue(Object nv) {
//        	System.out.println("DEBUG -- in HT MapEntry -- setValue is called");
        	Object ov = value;
        	value = nv;
        	return ov;
        }

        public boolean equals(Object o) {
//        	System.out.println("DEBUG -- in HT MapEntry -- equals is called");

        	if (!(o instanceof java.util.Map.Entry))return false;
        	java.util.Map.Entry e = (java.util.Map.Entry)o;
        	if (  e == null ) return false;
        	return ( (key == null ? e.getKey()==null : key.equals(e.getKey())) &&
                  (value == null ? e.getValue()==null : key.equals(e.getValue())));
        }

        public int hashCode() {
//        	System.out.println("DEBUG -- in HT MapEntry -- hashCode is called");
        	int kc = key == null ? 0 : key.hashCode();
        	int vc = value == null ? 0 : value.hashCode();
        	return kc ^ vc;
        }

  }

        private class EIterator implements Iterator {
        	int pos=0;
                int status=0;

                public EIterator() {}

                public  boolean hasNext() {
//                	System.out.println("DEBUG -- in HT keySetIterator -- hasNext is called ->"+(pos < buf.size()));
                	return  pos < size();
                }

                public Object next() {
//                	System.out.println("DEBUG -- in HT keySetIterator -- next is called");
                 	status = 1;
                 	if (pos>= size()) throw new NoSuchElementException("no elements left");
                 	pos++;			
			return new Entry(keys.get(pos-1) ,values.get(pos-1));                 	
                }

                public void remove() {
//                	System.out.println("DEBUG -- in AM EIterator -- remove is called");
                        if (status != 1 ) throw new IllegalStateException("do a next() operation before remove()");
                      	deleteInAM(keys.get(pos-1));
                      	pos--;
                      	status=-1;
                }
        }
  	
}	
