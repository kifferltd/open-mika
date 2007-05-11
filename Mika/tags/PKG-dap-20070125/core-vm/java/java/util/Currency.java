/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: Currency.java,v 1.1 2006/04/18 11:35:28 cvs Exp $
 */
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
