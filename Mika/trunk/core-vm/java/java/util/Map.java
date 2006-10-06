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
* $Id: Map.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/
package java.util;

public interface Map {

  public static interface Entry {

    public Object getKey();
    public Object getValue();
    public Object setValue(Object value) throws UnsupportedOperationException,
 	ClassCastException, IllegalArgumentException, NullPointerException;
    public boolean equals(Object o);
    public int hashCode();

  }

  public int size();
  public boolean isEmpty();
  public boolean containsKey(Object key) throws ClassCastException,
	NullPointerException;
  public boolean containsValue(Object value) throws NullPointerException;
  public Object get(Object key) throws ClassCastException, NullPointerException;
  public Object put(Object key, Object value) throws ClassCastException,
	NullPointerException, UnsupportedOperationException, IllegalArgumentException;
  public Object remove(Object key) throws UnsupportedOperationException;
  public void putAll(Map t)throws ClassCastException,NullPointerException, UnsupportedOperationException,
  	IllegalArgumentException,ConcurrentModificationException;
  public void clear() throws UnsupportedOperationException;
  public Set keySet();
  public Collection values();
  public Set entrySet();
  public boolean equals(Object o);
  public int hashCode();

}
