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

/*
** $Id: RuleBasedCollator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import wonka.vm.IntArrayList;
import wonka.vm.IntHashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class RuleBasedCollator extends Collator {

  private static final long serialVersionUID = 2822366911447564107L;
  
  static {
    /*Runtime rt = Runtime.getRuntime();
    System.out.println("freeMem "+rt.freeMemory());
    long time = System.currentTimeMillis();*/
    createHashtables();
    /*
    time = System.currentTimeMillis() -time;
    System.out.println("time = "+time+", freeMem "+rt.freeMemory());
    */
  }

  private IntHashtable orders = new IntHashtable();
  private String rules;

  private boolean accentInverseSorting; //defaults to false ...

  //the Rules form a linkedList
  static class Rule{
    int[] order;
    IntHashtable composed;
    Rule next;//to Support reset
  }

  static {
    createHashtables();
  }

  /**
  ** TODO: implement inverse accent ordering ('@')
  ** TODO: ignore chars if sequence start with ',' or ';'
  */
  public RuleBasedCollator(String rules) throws ParseException{

    this.rules = rules;
    rules = rules.trim();
    int primary = 0;
    int secondary = 0;
    int tertiary = 0;
    int len = rules.length();
    StringBuffer buf = new StringBuffer();
    Rule prev = new Rule();
    HashMap resetPointtable = new HashMap();
    Rule resetPoint = null;
    for(int i = 0 ; i < len ; i++){
      char ch = rules.charAt(i);
      //System.out.println("parsing char '"+ch+"'");
      if(!Character.isWhitespace(ch)){
        switch(ch){
          case '<':
            if(resetPoint != null){
              Rule rule = resetPoint;
              do {
                for(int k = 0 ; k < rule.order.length ; k++){
                  rule.order[k] += 0x00010000;
                }
                rule = rule.next;
              } while (rule != null);
              //secondary orders of next should be lowered
              rule = resetPoint;
              int order = (prev.order[0] & 0xffff0000)+0x00010000;
              while(order == (rule.order[0] & 0xffff0000)){
                //System.out.println("lowering secondary rule");
                rule.order[0] -= 0x00000100;
                for(int k = 1 ; k < rule.order.length ; k++){
                  if((rule.order[k] & 0xffff0000)== order){
                    rule.order[k] -= 0x00000100;
                  }
                }
                rule = rule.next;
                if(rule == null){
                  break;
                }
              }
            }
            primary++;
            secondary = 0;
            tertiary = 0;
            break;
          case ';':
            if(resetPoint != null){
              Rule rule = resetPoint;
              int order = prev.order[0] & 0xffff0000;
              while(order == (rule.order[0] & 0xffff0000)){
                rule.order[0] += 0x00000100;
                for(int k = 1 ; k < rule.order.length ; k++){
                  if((rule.order[k] & 0xffff0000)== order){
                    rule.order[k] += 0x00000100;
                  }
                }
                rule = rule.next;
                if(rule == null){
                  break;
                }
              }
              //lowering tertiary rule if needed
              rule = resetPoint;
              order = 0x00000100 + (prev.order[0] & 0xffffff00);
              while(order == (rule.order[0] & 0xffffff00)){
                rule.order[0] += 0x00000001;
                for(int k = 1 ; k < rule.order.length ; k++){
                  if((rule.order[k] & 0xffffff00)== order){
                    rule.order[k] += 0x00000001;
                  }
                }
                rule = rule.next;
                if(rule == null){
                  break;
                }
              }
            }
            secondary++;
            tertiary = 0;
            break;
          case ',':
            if(resetPoint != null){
              Rule rule = resetPoint;
              int order = prev.order[0] & 0xffffff00;
              while(order == (rule.order[0] & 0xffffff00)){
                rule.order[0] += 0x00000001;
                for(int k = 1 ; k < rule.order.length ; k++){
                  if((rule.order[k] & 0xffffff00)== order){
                    rule.order[k] += 0x00000001;
                  }
                }
                rule = rule.next;
                if(rule == null){
                  break;
                }
              }
            }
            tertiary ++;
          case '=':
            break;
          case '&':
            //step 1 clean up previous resets
            prev.next = resetPoint; //that was easy ...
            //step 2 do the reset
            // there are 2 reset cases
            i = findEndOfArgument(rules, i, buf, len);
            decomposeChars(buf);
            String pattern = buf.toString();
            Object o = resetPointtable.get(pattern);
            if(o != null){
              //1 plain reset to existing 'single rule'
              Rule rule = (Rule)o;
              resetPoint = rule.next;
              int order = rule.order[0];
              primary   = (order & 0xffff0000)>>>16;
              secondary = (order & 0x0000ff00)>>8;
              tertiary  = (order & 0x000000ff);
              //System.out.println("reset to '"+buf+" p = "+primary+" s = "+secondary+" t = "+tertiary);
              prev = rule;
            }
            else {
              //2 reset to a combined rule
              // first find a combination of existing chars
              LinkedList ll = findCombination(resetPointtable,pattern);
              if(ll == null) {
                throw new ParseException("invalid reset Point",i);
              }
              if(resetPoint != null){ //this is not the best way to locate the last rule!!!
                while(resetPoint.next != null){
                  resetPoint = resetPoint.next;
                }
                prev = resetPoint;
                int order = prev.order[0];
                primary   = (order & 0xffff0000)>>>16;
                secondary = (order & 0x0000ff00)>>8;
                tertiary  = (order & 0x000000ff);
                resetPoint = null;
              }
              IntArrayList newOrder = new IntArrayList(7);
              Iterator it =  ll.iterator();
              while(it.hasNext()){
                newOrder.add((int[])it.next());
              }
              int[] array = newOrder.toArray();
              while((++i) < len && Character.isWhitespace(rules.charAt(i)));
              if(i == len){
                continue;
              }
              ch = rules.charAt(i++);
              switch(ch){
                case '<':
                  array[array.length-1] += 0x00010000;
                   break;
                case ';':
                  array[array.length-1] += 0x00000100;
                   break;
                case ',':
                  array[array.length-1] += 0x00000001;
                   break;
                default:
                  throw new ParseException("invalid reset Point",i);
              }
              i = findEndOfArgument(rules, i, buf, len);
              decomposeChars(buf);
              //System.out.println("character is '"+buf+"' size = "+buf.length());
              Rule next = addToRuleTree(buf);
              next.order = array;
              resetPointtable.put(pattern, next);
            }
            continue;
          case '@':
            accentInverseSorting = true;
            continue;
          default:
            throw new ParseException("parsing of rules failed",i);
        }
        //create a new rule ...
        i = findEndOfArgument(rules, i, buf, len);
        decomposeChars(buf);
        //System.out.println("character is '"+buf+"' size = "+buf.length());
        Rule next = addToRuleTree(buf);
        next.order = new int[1];
        next.order[0] = (primary<<16) + (secondary<<8) + tertiary;
        prev.next = next;
        prev = next;
        resetPointtable.put(buf.toString(), next);
        //System.out.println("setting order to "+Integer.toHexString(next.order[0]));
      }
    }
  }

  private Rule addToRuleTree(StringBuffer buf) throws ParseException {
    int size = buf.length()-1;
    IntHashtable hash = orders;
    Rule next;
    for(int k = 0 ; k < size ; k++){
      char ch = buf.charAt(k);
      Object o = hash.get(ch);
      if(o == null){
        next = new Rule();
        hash.put(ch, next);
        //System.out.println("putting '"+ch+"' in "+hash+", with rule "+next);
        hash = new IntHashtable(5);
        next.composed = hash;
      }
      else{
        next = (Rule)o;
        if(next.composed == null){
          next.composed = new IntHashtable(5);
        }
        hash = next.composed;
      }
    }
    char ch = buf.charAt(size);
    Object o = hash.get(ch);
    if(o == null){
      next = new Rule();
      hash.put(ch, next);
      //System.out.println("putting '"+ch+"' in "+hash+", with rule "+next);
    }
    else{
      next = (Rule)o;
      if(next.order != null){
        throw new ParseException("duplicate character",0);
      }
    }
    return next;
  }

  private int findEndOfArgument(String rules, int pos, StringBuffer buf, int len){
    buf.setLength(0);
    while((++pos) < len && Character.isWhitespace(rules.charAt(pos)));
    for(; pos < len ; pos++){
      char ch = rules.charAt(pos);
      if('\'' == ch && pos < len-2){//skip
        if(rules.charAt(pos+2) == '\''){
          ch = rules.charAt(pos+1);
          pos += 2;
        }
        buf.append(ch);
      }
      else if((ch >= 0x0009 && ch <= 0x000d) || (ch >= 0x0020 && ch <= 0x002f)
          ||(ch >= 0x003a && ch <= 0x0040) || (ch >= 0x005b && ch <= 0x0060)
          ||(ch >= 0x007e && ch <= 0x007e) || ch == '@' || ch == '<' || ch == ';' || ch == ',' || ch == '&'){
        return pos-1;
      }
      else {
        buf.append(ch);
      }
    }
    return pos;
  }

  public Object clone(){
    return super.clone();
  }

  public int compare(String one, String two){
    CollationElementIterator cei1 = new CollationElementIterator(new StringCharacterIterator(one),this);
    CollationElementIterator cei2 = new CollationElementIterator(new StringCharacterIterator(two),this);
    int order1 = cei1.next();
    while(order1 != CollationElementIterator.NULLORDER){
      int order2 = cei2.next();
      //System.out.println("order1: "+Integer.toHexString(order1)+", order2:"+Integer.toHexString(order2));
      if(order2 == CollationElementIterator.NULLORDER){
        return 1;
      }
      int mask;
      if(strength == PRIMARY){
        mask = 0xffff0000;
      }
      else if(strength == SECONDARY){
        mask = 0xffffff00;
      }
      else {
        mask = 0xffffffff;
      }
      order1 = mask & order1;
      order2 = mask & order2;
      if(order2 > order1){
        return -1;
      }
      if(order1 > order2){
        return 1;
      }
      order1 = cei1.next();
    }
    return (CollationElementIterator.NULLORDER == cei2.next() ? 0 : -1);
  }

  public boolean equals(Object o){
    if(!(o instanceof RuleBasedCollator)){
      return false;
    }
    RuleBasedCollator rbc = (RuleBasedCollator) o;
    return this.decomposition == rbc.decomposition
        && this.strength == rbc.strength
        && this.rules.equals(rbc.rules);
  }

  public CollationElementIterator getCollationElementIterator(String src){
    return new CollationElementIterator(new StringCharacterIterator(src),this);
  }

  public CollationElementIterator getCollationElementIterator(CharacterIterator src){
    return new CollationElementIterator(src,this);
  }

  public CollationKey getCollationKey(String src){
    StringBuffer key = new StringBuffer(src);
    decomposeChars(key);
    return new CollationKey(src,key.toString());
  }

  public String getRules(){
    return rules;
  }

  public int hashCode(){
    return decomposition ^ strength ^ rules.hashCode();
  }

  //CollationElementIterator interface ...
  int[] getOrders(CharacterIterator src){
    //sort characters + check next based on Unicode properties
    StringBuffer buf = getOrderedCharacter(src);
    if(buf == null){
      return null;
    }
    IntArrayList temp = new IntArrayList(5);

    /**
    ** for every character in the buffer we need to get it's order. (and add it to the IntArrayList)
    ** This could mean combining some characters (and possibly get some more chars
    ** to make special ordering combination dictated by the Collator rule)
    */
    int len = buf.length();

    for(int pos = 0 ; pos < len ;){
      char ch = buf.charAt(pos++);
      //System.out.println("checking char '"+ch+"'");

      Rule r = (Rule) orders.get(ch);
      if(r != null){
        //combine chars if possible
        //System.out.println("char '"+ch+"' has a rule "+r);
        if(r.composed != null){
          //System.out.println("char '"+ch+"' can be combined");
          Rule resetRule = r;
          int resetPos = pos;
          StringBuffer resetBuf = buf;
          int resetIndex = src.getIndex();

          while(r.composed != null){
            if(pos >= len){
              buf = getOrderedCharacter(src);
              if(buf == null){
                break;
              }
              pos = 0;
              len = buf.length();
            }
            r = (Rule)r.composed.get(buf.charAt(pos++));
            if(r == null){
              break;
            }
            if(r.order != null){
              resetRule = r;
              resetPos = pos;
              resetBuf = buf;
              resetIndex = src.getIndex();
            }
          }
          buf = resetBuf;
          pos = resetPos;
          r = resetRule;
          src.setIndex(resetIndex);
          len = buf.length();
        }
        if (r.order != null){
          //System.out.println("char '"+ch+"' adding ordering values "+Integer.toHexString(r.order[0])+" from "+r);
          temp.add(r.order);
          continue;
        }
      }
      //System.out.println("char '"+ch+"' adding default values");
      temp.add(0x7fffffff);
      temp.add(ch<<16);
    }
    return temp.toArray();
  }

  /**
  ** should decompose the char ch and check if the next character can be grouped with
  ** the already decomposed character(s). the correct ordering should be applied.
  ** This is all based on Unicode character properties ...
  */
  private StringBuffer getOrderedCharacter(CharacterIterator src){
    char ch = src.current();
    if(ch == CharacterIterator.DONE){
      return null;
    }
    StringBuffer buf = new StringBuffer();
    buf.append(ch);
    decomposeChars(buf);
    ch = src.next();
    while (ch != CharacterIterator.DONE && getCombiningClass(ch) != 0){
      buf.append(ch);
      ch = src.next();
    }
    //sort the orders ...
    int len = buf.length();
    for(int i = 1 ; i < len ; i++){
      ch = buf.charAt(i);
      int order = getCombiningClass(ch);
      //System.out.println("ORDERING char '"+Integer.toHexString(ch)+"' order = "+order+" at pos "+i);
      for(int j = i-1 ; j >= 0 ; j--){
        char chr = buf.charAt(j);
        if(order >= getCombiningClass(chr)){
          break;
        }
        buf.setCharAt(j, ch);
        buf.setCharAt(j+1, chr);
      }
    }
    return buf;
  }

  private LinkedList findCombination(HashMap table, String pattern){
    //System.out.println("looking for '"+pattern+"' in "+table);
    int len = pattern.length();
    if(len == 1){
      //System.out.println("length == 1");
      Rule rule = (Rule)table.get(pattern);
      //System.out.println("length == 1 '"+pattern+"' got "+rule);
      if(rule == null){
        return null;
      }
      LinkedList ll = new LinkedList();
      ll.addFirst(rule.order);
      return ll;
    }
    for(int i = 1 ; i <= len ; i++){
      String sub = pattern.substring(0,i);
      Rule rule = (Rule)table.get(sub);
      //System.out.println("looking for '"+sub+"' got "+rule);
      if(rule != null){
        LinkedList ll = findCombination(table, pattern.substring(i));
        if(ll != null){
          ll.addFirst(rule.order);
          return ll;
        }
      }
    }
    return null;
  }

  /**
  ** should take decomposition into account...
  */
  private void decomposeChars(StringBuffer buf){
    if(decomposition != NO_DECOMPOSITION){
      for (int i = buf.length() - 1 ; i >= 0 ; i--){
        String chars = decomposeChar(buf.charAt(i));
        if(chars != null){
          buf.deleteCharAt(i);
          buf.insert(i,chars);
        }
      }
    }
  }

  /**
  ** should take decomposition into account...
  */
  private String decomposeChar(char ch){
    if(strength != IDENTICAL){
      /* TODO ...
      Object o = controls.get(ch);
      if(o != null){
        return EMPTYCHAR;
      }
      */
      //System.out.println("decomposeChar '"+ch+"'");
      if(strength == FULL_DECOMPOSITION || getCompatibility(ch) == null){
        return  getDecomposition(ch);
      }
    }
    return null;
  }

/*
** Set up the internal tables used by getCombiningClass, getDecomposition,
** and getCompatibility.
*/
private static native void createHashtables();

/*
** Get the canonical combining class for this character
*/
private static native int getCombiningClass(char c);

/*
** Get the decomposition of this character.
*/
private static native String getDecomposition(char c);

/*
** Get the compatibilty attribute of the decomposition.  If the decomposition 
** is canonical then this method returns null; else it returns a string such 
** as "isolated" or "noBreak".
*/
private static native String getCompatibility(char c);




}
