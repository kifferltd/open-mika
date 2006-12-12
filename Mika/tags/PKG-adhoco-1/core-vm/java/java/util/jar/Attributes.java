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


package java.util.jar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Attributes implements Map, Cloneable {

/**
** Field map is the map containing all entries
*/
  protected Map map;

/*
** Constructors
*/
  public Attributes() {
    this.map = new HashMap();
  }

  public Attributes(int cap) {
    this.map = new HashMap(cap);
  }

  public Attributes(Attributes attr) {
  	this.map = new HashMap(attr.map);
  }

/*
** public methods
*/
  public void clear() {
    map.clear();
  }

  public Object clone() {
   	Attributes cloned=null;
   	try {
   	  cloned = (Attributes)super.clone();
   	  cloned.map = (Map) ((HashMap)map).clone();
   	}
   	catch(CloneNotSupportedException cnse){}
   	return cloned;
  }

  public boolean containsKey (Object name) {
    return map.containsKey(name);
  }

  public boolean containsValue (Object value) {
    return map.containsValue(value);
  }

  public Set entrySet() {
    return map.entrySet();
  }

  public boolean equals(Object o) {
   	if (!(o instanceof Attributes)){
   	 	return false;
   	}
   	return map.equals(((Attributes)o).map);
  }

  public Object get(Object name) {
    return map.get(name);
  }

  public String getValue(String name) {
    Name attName = new Name(name);
    return (String)get(attName);
  }

  public String getValue (Name name) {
    return (String)get(name);
  }

  public int hashCode() {
   	return map.hashCode();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set keySet() {
    return map.keySet();
  }

  public Object put (Object name, Object value) {
    //cast are ,needed to make sure correct objects are placed in.
    return map.put((Attributes.Name) name, (String) value);
  }

  public String putValue(String aname , String val) {
    return (String)map.put(new Attributes.Name(aname),val);	
  }

  public void putAll(Map attr) {
    map.putAll(attr);
  }

  public Object remove(Object name) {
    return map.remove(name);
  }

  public int size() {
    return map.size();
  }

  public Collection values() {
    return map.values();
  }


/**
** public static innerclass  Name ...
** this class can be called by everybody ...
**
*/
  public static class Name {
    public static final Name CLASS_PATH = new Name("Class-Path");
    public static final Name CONTENT_TYPE = new Name("Content-Type");
    public static final Name IMPLEMENTATION_TITLE = new Name("Implementation-Title");
    public static final Name IMPLEMENTATION_VERSION = new Name("Implementation-Version");
    public static final Name IMPLEMENTATION_VENDOR = new Name("Implementation-Vendor");
    public static final Name MAIN_CLASS = new Name("Main-Class");
    public static final Name MANIFEST_VERSION = new Name("Manifest-Version");
    public static final Name SEALED = new Name("Sealed");
    public static final Name SIGNATURE_VERSION = new Name("Signature-Version");
    public static final Name SPECIFICATION_TITLE = new Name("Specification-Title");
    public static final Name SPECIFICATION_VERSION = new Name("Specification-Version");
    public static final Name SPECIFICATION_VENDOR = new Name("Specification-Vendor");

    public static final Name EXTENSION_INSTALLATION = new Name("Extension-Installation");
    public static final Name IMPLEMENTATION_URL = new Name("Implementation-Vendor-URL");
    public static final Name EXTENSION_LIST = new Name("Extension-List");
    public static final Name EXTENSION_NAME = new Name("Extension-Name");
    public static final Name IMPLEMENTATION_VENDOR_ID = new Name("Implementation-Vendor-Id");

    private String name;

    public Name (String name) throws IllegalArgumentException, NullPointerException {
      int length = name.length();
      if(length == 0){
        throw new IllegalArgumentException("A name cannot have a length of '0'");
      }
      char str[] = name.toCharArray();
      for(int i = 0 ; i < length ; i++){
        char ch = str[i];
        if(Character.digit(ch,36) == -1 && ch != '-' && ch != '_'){
          throw new IllegalArgumentException("bad digit '"+ch+"'");
        }
      }
      this.name = name;
    }

    // overwritten Object methods ...

    public String toString() {
      return name;
    }

    public int hashCode() {
      return name.toLowerCase().hashCode();
    }

    public boolean equals (Object o) {
      boolean answer = false;
      if (o instanceof Name) {
        Name n2 = (Name)o;
        if (n2.name.equalsIgnoreCase(name)) {
          answer = true;
        }
      }
      return answer;
    }
  }
}
