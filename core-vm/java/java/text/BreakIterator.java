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
