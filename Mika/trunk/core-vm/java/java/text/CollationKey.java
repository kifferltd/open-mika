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
** $Id: CollationKey.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

public final class CollationKey implements Comparable {

  private String key;
  private String value;

  CollationKey(String val, String key){
    this.key = key;
    value = val;
  }

  public int compareTo(CollationKey ckey){
    return this.key.compareTo(ckey.key);
  }

  public int compareTo(Object key){
    return compareTo((CollationKey)key);
  }

  public boolean equals(Object o){
    if(!(o instanceof CollationKey)){
      return false;
    }
    CollationKey ckey = (CollationKey)o;
    return this.key.equals(ckey.key);
  }

  public String getSourceString(){
    return value;
  }

  public byte[] toByteArray(){
    try {
      return key.getBytes("UTF8");
    }
    catch(java.io.UnsupportedEncodingException uee){
      return key.getBytes();
    }
  }
}