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
*                                                                         *
* Additions copyright (C) 2005 Chris Gray, /k/ Embedded Java Solutions.   *
* Permission is hereby granted to distribute these changes under the      *
* terms of the Wonka Public Licence.                                      *
**************************************************************************/

/*
** $Id: AttributedString.java,v 1.2 2005/09/13 09:01:22 cvs Exp $
*/

package java.text;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AttributedString {

  private String basetext;
  private int textbegin;
  private int textend;
  private Map range2attr = new HashMap(11);
  private AttributedCharacterIterator cachedIterator;

  private class Range {
    int start;
    int end;

    Range(int start, int end) {
      this.start = start;
      this.end = end;
    }

    public int hashCode() {
      return start + 32767 * end;
    }

    public boolean equals(Object o) {
      try {
        Range r = (Range)o;
        return r.start == this.start && r.end == this.end;
      }
      catch (ClassCastException cce) {
        return false;
      }
    }
  }

  public AttributedString(String text){
    basetext = text;
  }

  public AttributedString(String text, Map attr){
    basetext = text;
    range2attr.put (new Range(0, text.length()), attr);
  }

  public AttributedString(AttributedCharacterIterator text){
    cachedIterator = text;
    init(text, text.getBeginIndex(), text.getEndIndex());
  }

  public AttributedString(AttributedCharacterIterator text, int begin, int end){
    init(text, begin, end);
  }

  public AttributedString(AttributedCharacterIterator text, int begin, int end, AttributedCharacterIterator.Attribute[] attributes){
    textbegin = begin;
    textend = end;
    Set attrkeys = text.getAllAttributeKeys();
    Iterator akit = attrkeys.iterator();
    while (akit.hasNext()) {
      AttributedCharacterIterator.Attribute a = (AttributedCharacterIterator.Attribute) akit.next();
      boolean relevant = false;
      for (int i = 0; i < attributes.length; ++i) {
        if (attributes[i].equals(a)) {
          relevant = true;
	  break;
	}
      }
      if (!relevant) {
        continue;
      }
      AttributedCharacterIterator aci = (AttributedCharacterIterator) text.clone();
      int runstart = begin;
      aci.setIndex(begin);
      Object currvalue = aci.getAttribute(a);
      int runend = aci.getRunLimit(a);
      while (aci.getRunStart(a) < begin) {
        runstart = runend;
	runend = aci.getRunLimit(a);
      }
      while (runend <= end) {
        if (currvalue != null) {
          addAttribute(a, currvalue, runstart, runend);
	  runstart = runend;
          currvalue = aci.getAttribute(a);
          runend = aci.getRunLimit(a);
        }
      }
    }
  }

  /**
   * Essentially the same as the 4-args constructor, but skipping the wasteful
   * search through the array (we know it will succeeed, somehow).
   * */
  private void init(AttributedCharacterIterator text, int begin, int end) {
    textbegin = begin;
    textend = end;
    Set attrkeys = text.getAllAttributeKeys();
    Iterator akit = attrkeys.iterator();
    while (akit.hasNext()) {
      AttributedCharacterIterator.Attribute a = (AttributedCharacterIterator.Attribute) akit.next();
      AttributedCharacterIterator aci = (AttributedCharacterIterator) text.clone();
      int runstart = begin;
      aci.setIndex(begin);
      Object currvalue = aci.getAttribute(a);
      int runend = aci.getRunLimit(a);
      while (aci.getRunStart(a) < begin) {
        runstart = runend;
	runend = aci.getRunLimit(a);
      }
      while (runend <= end) {
        if (currvalue != null) {
          addattr(a, currvalue, runstart, runend);
	  runstart = runend;
          currvalue = aci.getAttribute(a);
          runend = aci.getRunLimit(a);
        }
      }
    }
  }

  public void addAttribute(AttributedCharacterIterator.Attribute key, Object value){
    cachedIterator = null;
    addattr(key, value, textbegin, textend);
  }

  public void addAttribute(AttributedCharacterIterator.Attribute key, Object value, int beginIdx, int endIdx){
    cachedIterator = null;
    addattr((AttributedCharacterIterator.Attribute) key, value, beginIdx, endIdx);
  }

  public void addAttributes(Map attrs, int beginIdx, int endIdx){
    cachedIterator = null;
    Set s = attrs.entrySet();
    Iterator it = s.iterator();
    while (it.hasNext()) {
      Map.Entry me = (Map.Entry) it.next();
      addattr((AttributedCharacterIterator.Attribute) me.getKey(), me.getValue(), beginIdx, endIdx);
    }
  }

  private void addattr(AttributedCharacterIterator.Attribute key, Object value, int beginIdx, int endIdx){
    Range r = new Range(beginIdx, endIdx);
    synchronized(range2attr) {
      Map currattrs = (Map) range2attr.get(r);
      if (currattrs == null) {
        currattrs = new HashMap(7);
	range2attr.put(r, currattrs);
      }
      currattrs.put(key, value);
    }
  }

  // TODO
  /*
  public AttributedCharacterIterator getIterator(){
    if (cachedIterator != null) {
      return cachedIterator;
    }
    return new ACI();
  }

  public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] matchingkeys){
    return null;
  }

  public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] matchingkeys, int beginIdx, int endIdx){
    return null;
  }

  private class ACI implements AttributedCharacterIterator {
    AttributedCharacterIterator() {
    }
  }
  */
}
