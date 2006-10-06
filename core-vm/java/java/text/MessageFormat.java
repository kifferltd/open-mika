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
** $Id: MessageFormat.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.StringTokenizer;

public class MessageFormat extends Format {

  private static final long serialVersionUID = 6479157306784022952L;
  private static final FieldPosition TRASHPOSITION = new FieldPosition(0);

  //dictated by the serialized form
  private int[] argumentNumbers = new int[10];
  private Format[] formats = new Format[10];
  private Locale locale = Locale.getDefault();
  private int maxOffset;
  private int[] offsets = new int[10];
  private String pattern;

  private transient String[] subPatterns;
  private transient String origPattern;

  public MessageFormat(String pattern){
    applyPattern(pattern);
  }

  private Format getDateTimeFormat(StringTokenizer st, boolean time){
    int style = DateFormat.DEFAULT;
    if(st.hasMoreTokens()){
      String token = st.nextToken();
      if(token.equals("short")){
        style = DateFormat.SHORT;
      }
      else if (token.equals("medium")){
        style = DateFormat.MEDIUM;
      }
      else if (token.equals("long")){
        style = DateFormat.LONG;
      }
      else if (token.equals("full")){
        style = DateFormat.FULL;
      }
      else{
        return new SimpleDateFormat(token, locale);
      }
    }
    if(time){
      return DateFormat.getTimeInstance(style, locale);
    }
    else {
      return DateFormat.getDateInstance(style, locale);
    }
  }

  private int applyFormat(int idx, String pattern){
    int end = pattern.indexOf('}', idx);
    int last = pattern.indexOf('{', idx);
    while (last < end && last != -1 && end != -1){
      //System.out.println("in while last = "+last+" <--> end = "+end);
      end = pattern.indexOf('}', end+1);
      last = pattern.indexOf('{', last+1);
    }
    //System.out.println("after while last = "+last+" <--> end = "+end);
    if(end == -1 || end == idx){
      throw new IllegalArgumentException("bad pattern");
    }
    StringTokenizer st = new StringTokenizer(pattern.substring(idx,end),",");
    try {
      argumentNumbers[++maxOffset] = Integer.parseInt(st.nextToken());
      if(argumentNumbers[maxOffset]<0 || argumentNumbers[maxOffset]>9){
        throw new IllegalArgumentException("bad pattern: bad argument number "+argumentNumbers[maxOffset]);
      }
      if(st.hasMoreTokens()){
        String token = st.nextToken().trim();
        //System.out.println("looking for '"+token+"'");
        if(token.equals("time")){
          formats[maxOffset] = getDateTimeFormat(st,true);
        }
        else if (token.equals("date")){
          formats[maxOffset] = getDateTimeFormat(st,true);
        }
        else if (token.equals("number")){
          if(st.hasMoreTokens()){
            token = st.nextToken();
            //System.out.println("looking for '"+token+"'");
            String trimmed = token.trim();
            if(trimmed.equals("currency")){
              formats[maxOffset] = NumberFormat.getCurrencyInstance(locale);
            }
            else if(trimmed.equals("percent")){
              formats[maxOffset] = NumberFormat.getPercentInstance(locale);
            }
            else if(trimmed.equals("integer")){
               NumberFormat nf = NumberFormat.getNumberInstance(locale);
               nf.setParseIntegerOnly(true);
               formats[maxOffset] = nf;
            }
            else {
              formats[maxOffset] = new DecimalFormat(token, new  DecimalFormatSymbols(locale));
            }
          }
          else {
            formats[maxOffset] = NumberFormat.getInstance(locale);
          }
        }
        else if (token.equals("choice")){
          if(st.hasMoreTokens()){
            formats[maxOffset] = new ChoiceFormat(st.nextToken());
          }
          else{
            formats[maxOffset] = new ChoiceFormat("0# ");
          }
        }
        else {
          throw new IllegalArgumentException("bad pattern");
        }
      }
      else{
        formats[maxOffset] = null;
      }
    }
    catch(RuntimeException rt){
      throw new IllegalArgumentException("bad pattern");
    }
    return end;
  }

  public void applyPattern(String pattern){
    //System.out.println("applying pattern '"+pattern+"'");
    int len = pattern.length();
    maxOffset = -1;
    StringBuffer pat = new StringBuffer(len);
    for (int i = 0 ; i < len ; i++){
      char ch = pattern.charAt(i);
      if(ch == '\'' && (++i) < len){
        pat.append(pattern.charAt(i));
      }
      else if(ch == '{'){
        i = applyFormat(i+1, pattern);
        offsets[maxOffset] = pat.length();
      }
      else
        pat.append(ch);
    }
    subPatterns = null;
    origPattern = pattern;
    this.pattern = pat.toString();
  }

  public Object clone() {
    MessageFormat mf = (MessageFormat) super.clone();
    mf.argumentNumbers = (int[])argumentNumbers.clone();
    mf.offsets = (int[]) offsets;
    mf.formats = (Format[]) formats.clone();
    return mf;
  }

  public boolean equals(Object o){
    if(!(o instanceof MessageFormat)){
      return false;
    }
    MessageFormat mf = (MessageFormat) o;
    return this.pattern.equals(mf.pattern)
        && this.maxOffset == mf.maxOffset
        && this.locale.equals(mf.locale)
        && Arrays.equals(this.formats ,mf.formats)
        && Arrays.equals(this.offsets ,mf.offsets)
        && Arrays.equals(this.argumentNumbers ,mf.argumentNumbers);
  }
  
  public static String format(String pattern, Object[] args){
    return new MessageFormat(pattern).format(args,new StringBuffer(),null).toString();
  }

  public final StringBuffer format(Object[] args, StringBuffer dest, FieldPosition pos){
    if(subPatterns == null){
      createSubPatterns();
    }
    for(int i = 0 ; i <= maxOffset ; i++){
      Format format = formats[i];
      dest.append(subPatterns[i]);
      if(format == null){
        Object o = args[argumentNumbers[i]];
        if(o instanceof Date){
          DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,locale).format((Date)o,dest,TRASHPOSITION);
        }
        else if(o instanceof Number){
          NumberFormat.getInstance(locale).format(((Number)o).doubleValue(), dest,TRASHPOSITION);
        }
        else {
          dest.append(o);
        }
      }
      else{
        //System.out.println("formatting '"+args[argumentNumbers[i]]+"'using "+format);
        /*
        if(format instanceof DecimalFormat){
          ((DecimalFormat)format).debug(origPattern);
        }*/
        format.format(args[argumentNumbers[i]], dest, TRASHPOSITION);
        //System.out.println("formatted '"+args[argumentNumbers[i]]+"'using "+format+" got '"+dest+"'");
      }
    }
    dest.append(subPatterns[maxOffset+1]);
    return dest;
  }


  public final StringBuffer format(Object obj, StringBuffer dest, FieldPosition pos){
    if(obj instanceof Object[]){
      return format((Object[])obj, dest, pos);
    }
    Object[] args = new Object[1];
    args[0] = obj;
    return format(args, dest, pos);
  }

  public Format[] getFormats(){
    return formats;
  }

  public Locale getLocale(){
    return locale;
  }

  public int hashCode(){
    return locale.hashCode() ^ pattern.hashCode() ^ maxOffset;
  }

  public Object[] parse(String src, ParsePosition pos){
    if(subPatterns == null){
      createSubPatterns();
    }
    Object[] res = new Object[10];
    int p = pos.getIndex();
    for(int i = 0 ; i <= maxOffset ; i++){
      String s = subPatterns[i];
      int len = s.length();
      if(!src.regionMatches(p, s, 0, len)){
        pos.setErrorIndex(p);
        //System.out.println("PARSE FAILED: missing subpattern "+subPatterns[i]);
        return null;
      }
      p += len;
      Format format = formats[i];
      if(format == null){
        int end = src.indexOf(subPatterns[i+1],p);
        if(end == -1){
          pos.setErrorIndex(p);
          //System.out.println("PARSE FAILED: missing subpattern after "+subPatterns[i]);
          return null;
        }
        res[argumentNumbers[i]] = src.substring(p,end);
        p = end;
      }
      else {
        pos.setIndex(p);
        Object o = format.parseObject(src, pos);
        if(o == null){
          //System.out.println("PARSE FAILED: parsing failed "+format);
          return null;
        }
        res[argumentNumbers[i]] = o;
        p = pos.getIndex();
      }
    }
    String s = subPatterns[maxOffset+1];
    int len = s.length();
    if(!src.regionMatches(p, s, 0, len)){
      pos.setErrorIndex(p);
      //System.out.println("PARSE FAILED: missing subpattern "+s);
      return null;
    }
    pos.setIndex(p+len);
    return res;
  }

  public Object[] parse(String src) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object[] res = parse(src,pos);
    if(res == null){
      throw new ParseException("parsing failed!",pos.getErrorIndex());
    }
    return res;
  }

  public Object parseObject(String source, ParsePosition pos){
    return parse(source, pos);
  }

  public void setFormat(int num, Format format){
    formats[num] = format;
  }

  public void setFormats(Format[] formats){
    this.formats = formats;
  }

  public void setLocale(Locale loc){
    locale = loc;
  }

  public String toPattern(){
    if(origPattern == null){
      if(subPatterns == null){
        createSubPatterns();
      }
      StringBuffer pattern = new StringBuffer();
      for(int i = 0 ; i <= maxOffset ; i++){
        addQuotes(subPatterns[i], pattern);
        pattern.append('{');
        pattern.append(argumentNumbers[i]);
        Format format = formats[i];
        if(format != null){
          if(format instanceof ChoiceFormat){
            pattern.append(",choice,");
            pattern.append(((ChoiceFormat)format).toPattern());
          }
          else if(format instanceof DecimalFormat){
            pattern.append(",number,");
            pattern.append(((DecimalFormat)format).toPattern());
          }
          else if(format instanceof SimpleDateFormat){
            pattern.append(",date,");
            pattern.append(((SimpleDateFormat)format).toPattern());
          }
        }
        pattern.append('}');
      }
      addQuotes(subPatterns[maxOffset+1], pattern);
      origPattern = pattern.toString();
    }
    return origPattern;
  }

  private void addQuotes(String pat, StringBuffer dest){
    String sep = "'{";
    StringTokenizer st = new StringTokenizer(pat, sep, true);
    while(st.hasMoreTokens()){
      String token = st.nextToken();
      if(sep.indexOf(token) != -1){
        dest.append('\'');
      }
      dest.append(token);
    }
  }

  private void createSubPatterns(){
    subPatterns = new String[maxOffset+2];
    int prev = 0;
    for(int i = 0 ; i <= maxOffset ; i++){
      int off = offsets[i];
      subPatterns[i] = pattern.substring(prev, off);
      prev = off;
    }
    subPatterns[maxOffset+1] = pattern.substring(prev);
  }

  private void debug(){
    System.out.println("ORIGINAL PATTERN = '"+origPattern+"'");
    for (int i=0 ; i <= maxOffset ; i++){
      System.out.println("argNr "+argumentNumbers[i]+" using "+formats[i]+" off "+offsets[i]);
    }
  }


}