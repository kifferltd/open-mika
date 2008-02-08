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
