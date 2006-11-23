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
** $Id: BreakIterator.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.text;

import java.util.Locale;

public abstract class BreakIterator implements Cloneable {

  private static final String wordBreaks = " \r\t\n";
  private static final String sentenceBreaks = ".?!";
  private static final String lineBreaks = "\n\r";

  public final static int DONE = -1;

  public static synchronized Locale[] getAvailableLocales(){
    return new Locale[0];
  }

  public static BreakIterator getCharacterInstance(){
    return getCharacterInstance(Locale.getDefault());
  }

  public static BreakIterator getCharacterInstance(Locale loc){
    return new SimpleBreakIterator(null);
  }

  public static BreakIterator getLineInstance(){
    return getLineInstance(Locale.getDefault());
  }

  public static BreakIterator getLineInstance(Locale loc){
    return new SimpleBreakIterator(lineBreaks);
  }

  public static BreakIterator getSentenceInstance(){
    return getSentenceInstance(Locale.getDefault());
  }

  public static BreakIterator getSentenceInstance(Locale loc){
    return new SimpleBreakIterator(sentenceBreaks);
  }

  public static BreakIterator getWordInstance(){
    return getWordInstance(Locale.getDefault());
  }

  public static BreakIterator getWordInstance(Locale loc){
    return new SimpleBreakIterator(wordBreaks);
  }

  protected BreakIterator(){}

  public Object clone() {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){}
    return null;
  }

  public abstract int current();
  public abstract int first();
  public abstract int following(int pos);
  public abstract CharacterIterator getText();
  public abstract int last();
  public abstract int next();
  public abstract int next(int pos);
  public abstract int previous();
  public abstract void setText(CharacterIterator newText);

  public boolean isBoundary(int pos){
     CharacterIterator cit = getText();
     int old = cit.getIndex();
     cit.setIndex(pos);
     cit.next();
     int ret = cit.previous();
     cit.setIndex(old);
     return ret == pos;
  }

  public int preceding(int pos){
     CharacterIterator cit = getText();
     int old = cit.getIndex();
     cit.setIndex(pos);
     int ret = cit.previous();
     cit.setIndex(old);
     return ret;
  }

  public void setText(String newText){
    setText(new StringCharacterIterator(newText));
  }
}
