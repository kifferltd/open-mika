/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written            *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS     *
* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,     *
* OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT    *
* OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR      *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

package java.util;

import java.io.Serializable;

/**
 * Currency:
 *
 * @author ruelens
 *
 * created: Apr 14, 2006
 */
public final class Currency implements Serializable {

  private static final String packageName = "resource.currency.";
  private static final String countryToCode = packageName + "countryMap";
  private static final String SYMBOL = "symbol";
  private static final String DIGITS = "fraction.digits";  
  private static final int UNSET = -2;

  private static final Hashtable currencies = new Hashtable(11){
    public synchronized Object put(Object key, Object newvalue) {
      Object current = super.put(key, newvalue);      
      if(current != null && current != newvalue) {
        super.put(key,current); 
        return current;
      }
      return newvalue;
    }
  };
  
  public static Currency getInstance(String code) {
    Currency currency = (Currency) currencies.get(code);
    if(currency == null) {
      currency = (Currency) currencies.put(code, new Currency(code));
    }
    return currency;
  }
  
  public static Currency getInstance(Locale locale) {
    try {
      String code = 
        ResourceBundle.getBundle(countryToCode).getString(locale.getCountry());
      if(code != null) {
        return getInstance(code);
      }
    } catch (MissingResourceException mre) { }
    return null;
  }
  
  private String currencyCode;
  private transient String baseName;
  private transient int digits;
  private transient String symbol;
  
  private Currency(String code) {
    this.currencyCode = code;
    this.baseName = packageName + code;
    try {
      ResourceBundle.getBundle(baseName);
    } catch (MissingResourceException mre) {
      IllegalArgumentException ia = new IllegalArgumentException(code);
      ia.initCause(mre);
      throw ia;
    }   
    digits = UNSET;
  }
  
  public String getCurrencyCode() {
    return currencyCode;
  }

  public int getDefaultFractionDigits() {
    if(digits == UNSET) {
      try {
        digits = Integer.parseInt(
            ResourceBundle.getBundle(baseName).getString(DIGITS).trim());
      } catch(RuntimeException rt) {
        digits = -1;
      }
    }
    return digits;
  }

  public String getSymbol() {
    return getSymbol(Locale.getDefault());
  }

  public String getSymbol(Locale locale) {
    if(symbol == null) {
      try {
        symbol = ResourceBundle.getBundle(baseName, locale).getString(SYMBOL);
      } catch(MissingResourceException mre) {
        symbol = currencyCode;
      }
    }
    return symbol;
  }

  public String toString() {
    return currencyCode;
  }
}
