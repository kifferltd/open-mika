/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.util;

public abstract class AbstractSet extends AbstractCollection implements Set {

  protected AbstractSet() {
  }

  public boolean equals (Object o) {
    if (!( o instanceof Set)) return false;
    // null is no instance of Set !!!
    if (o == this) return true;
    Set s = (Set)o;
    boolean answer = true;
    answer =(answer && (size() == s.size()));
    answer =(answer && containsAll(s));
    return answer;
  }

  public int hashCode() {
    int h = 0;
    Object o;
    Iterator it = iterator();
    while (it.hasNext()) {
      o = it.next();
      h = h +(o == null ? 0 :o.hashCode());
    }
    return h;
  }

  public boolean removeAll(Collection c) {
    Iterator it;
    Object o;
    boolean result = false;

    if (size() < c.size()) {
      // this set is smaller than c, iterate over this set and remove each 
      // element which is present in c
      it = iterator();
      while (it.hasNext()) {
        o = it.next();
	if (c.contains(o)) {
	  it.remove();
	  result = true;
	}
      }
    }
    else {
      // this set is at least as big as c, iterate over c and remove each 
      // element which is present in this set
      it = c.iterator();
      while (it.hasNext()) {
        o = it.next();
	if (this.contains(o)) {
	  it.remove();
	  result = true;
	}
      }
    }

    return result;
  }

}  
