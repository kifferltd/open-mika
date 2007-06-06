/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package java.awt;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.acunia.wonka.rudolph.FontMapping;

public class Font implements java.io.Serializable {

  static {
    Properties fontProperties = new Properties();
    String     name;
    String     defaultFont;

    try {
      InputStream propstream = ClassLoader.getSystemResourceAsStream("font.properties");
      fontProperties.load(propstream);
    }
    catch (IOException e) {}

    defaultFont = (String)fontProperties.get("default");
    if(defaultFont != null) {
      addFont(defaultFont, (String)fontProperties.get(defaultFont));
      fontProperties.remove("default");
      fontProperties.remove(defaultFont);
    }

    Enumeration names = fontProperties.propertyNames();
    while(names.hasMoreElements()) {
      name = (String)names.nextElement();
      addFont(name, (String)fontProperties.get(name));
    }

    Toolkit.getDefaultToolkit();
  }

  public static final int PLAIN  = 0x00000000;
  public static final int BOLD   = 0x00000001;
  public static final int ITALIC = 0x00000002;

  protected String name;
  protected int style;
  protected int size;
  private   String family;

  private native void create(String name, int style, int size);

  private static native void addFont(String fontname, String filename);
  
  /**
   * Addendum to the SUN specs.
   * The font name will be the 'name' specified, even if 'name' is ""; if 'name' is null however, a default font with name 'Default' is constructed.<br>
   * <DL>
   * <DT><B> Implementation status:</B><DD> implemented <DD>
   * <DT><B> Implementation remarks:</B><DD> not compliant with SUN specs. Supported fonts with their characteristics are listed below. No italic fonts are supported yet. <br>
   * <TABLE>
   * <TR> <TH> FAMILY </TH><TH> NAME </TH><TH> STYLE </TH><TH> SIZE </TH>
   * <TR> <TD> Helvetica </TD><TD> helvR08 </TD><TD> plain  </TD><TD> 8    </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR12 </TD><TD> plain  </TD><TD> 12   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR14 </TD><TD> plain  </TD><TD> 14   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR17 </TD><TD> plain  </TD><TD> 17   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR17 </TD><TD> plain  </TD><TD> 18   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR20 </TD><TD> plain  </TD><TD> 20   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR25 </TD><TD> plain  </TD><TD> 25   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvR34 </TD><TD> plain  </TD><TD> 34   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB08 </TD><TD> bold   </TD><TD> 8    </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB12 </TD><TD> bold   </TD><TD> 12   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB14 </TD><TD> bold   </TD><TD> 14   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB17 </TD><TD> bold   </TD><TD> 17   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB18 </TD><TD> bold   </TD><TD> 18   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB20 </TD><TD> bold   </TD><TD> 20   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB25 </TD><TD> bold   </TD><TD> 25   </TD>
   * <TR> <TD> Helvetica </TD><TD> helvB34 </TD><TD> bold   </TD><TD> 34   </TD>
   * <TR> <TD> Courier   </TD><TD> courR10 </TD><TD> plain  </TD><TD> 10   </TD>
   * <TR> <TD> Courier   </TD><TD> courR12 </TD><TD> plain  </TD><TD> 12   </TD>
   * <TR> <TD> Courier   </TD><TD> courR14 </TD><TD> plain  </TD><TD> 14   </TD>
   * <TR> <TD> Courier   </TD><TD> courR17 </TD><TD> plain  </TD><TD> 17   </TD>
   * <TR> <TD> Courier   </TD><TD> courR20 </TD><TD> plain  </TD><TD> 20   </TD>
   * <TR> <TD> Courier   </TD><TD> courR25 </TD><TD> plain  </TD><TD> 25   </TD>
   * <TR> <TD> Fixed   </TD><TD> nippon13 </TD><TD> plain  </TD><TD> 13   </TD>
   * <TR> <TD> Fixed   </TD><TD> nippon18 </TD><TD> plain  </TD><TD> 18  </TD>
   * <!-- <TR> <TD> Courier   </TD><TD> courB10 </TD><TD> bold   </TD><TD> 10   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB12 </TD><TD> bold   </TD><TD> 12   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB14 </TD><TD> bold   </TD><TD> 14   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB17 </TD><TD> bold   </TD><TD> 17   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB20 </TD><TD> bold   </TD><TD> 20   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB25 </TD><TD> bold   </TD><TD> 25   </TD> -->
   * <!-- <TR> <TD> Courier   </TD><TD> courB34 </TD><TD> bold   </TD><TD> 34   </TD> -->
   * </TABLE>
   * All fonts, except 'nippon13' and 'nippon18', contain only the first 256 glyphs of the ISO 8859-1 standard. The 'nippon' fonts contain the first 256 glyphs of the ISO 10646-1 standard, japanese 'katakana' glyphs in the range 0x3000 - 0x30ff, and
   * 'kanji' glyphs in the range 0x4e00 - 0x9fb0. <br>
   * If the requested font name does not exist, the first font found, in the sequence shown above, with similar characteristics (equal family and size, or smaller or equal size (style is ignored)), but with the name as requested, is returned. <br>
   * E.g. new Font("myFont", Font.PLAIN, 11) will construct font 'myFont' with characteristics of 'courP10'.</DD>
   * </DL>
   */

  public Font(String name, int style, int size) {
    if (name == null) {
      name = "Default";
    }
    this.create(FontMapping.map(name), style, size);
  }

  /**
   * Addendum to the SUN specs.
   * Parse following forms of font specification, and create the corresponding font:<br>
   * <UL>
   * <LI>'name-style-size',</LI>
   * <LI>'name-style',</LI>
   * <LI>'name--size',</LI>
   * <LI>'name'.</LI>
   * </UL>
   *  Default for 'name' is "", unless the spec is null, in which case the default
   *  name is "dialog".<br>
   *  Style is one of: 'plain', 'bold', 'italic' or 'bolditalic'; default value is
   *  'plain'.<br>
   *  Size is any positif integer; default value is 12.
   * @status  implemented
   * @remark  Currently, not compliant with SUN specs. See constructor details.<br>
   * Example: Font.decode("myFont-plain-11") will construct font 'myFont' with characteristics of 'courP10'.
   */
  public static Font decode(String spec) {

    String name  = "";
    int style = Font.PLAIN;
    int size  = 12;

    if (spec == null) {
      name = "dialog";  // conform with jdk1.2.2
    }
    else if (spec.length() == 0) {
      name = "";        // conform with jdk1.2.2
    }
    else {
      String styleStr;
      String sizeStr;

      char DELIM = '-';
      int n = 0;

      n = spec.indexOf(DELIM);
      // logical font name is required
      if (n != -1) { // first dash found
        name = spec.substring(0, n);
        if (spec.length() > n + 1) { // more characters after first dash
          int m = n + 1;
          n = spec.indexOf(DELIM, m);
          if (n == -1){ // no more dashes in spec
            n = spec.length();
          }
          if (n > m){

            styleStr=spec.substring(m,n);

            if (styleStr.equals("plain")) {
              style = Font.PLAIN;
            }
            else if (styleStr.equals("bold")) {
              style = Font.BOLD;
            }
            else if (styleStr.equals("italic")) {
              style = Font.ITALIC;
            }
            else if (styleStr.equals("bolditalic")) {
              style = Font.BOLD|Font.ITALIC;
            }
            else {
              style=Font.PLAIN;
            }
          }

          if (spec.length() > n + 1) { // more characters after second dash
            try {
              int intV = Integer.parseInt(spec.substring(n+1,spec.length()));
              if (intV > 0) {
                size=intV;
              }
              else {
                size = 12;
              }
            }
            catch (NumberFormatException e) { // thrown by Integer.parseInt()
              size = 12;
            }
          }
        }
        else { // no more characters after first dash
          style = Font.PLAIN;
          size = 12;
        }
      }
      else  { // no dash found
        name = spec;        // spec.length()==0 was excluded higher
        style = Font.PLAIN;
        size = 12;
      }
    }
  
    return new Font(name, style, size);
  }
  
  public boolean equals(Object object) {
    if (object instanceof Font) {
      Font fnt = (Font) object;
      if (fnt.name.equals(name) && fnt.style == style && fnt.size == size) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
  
  public String getFamily() {
    return family;
  }
  
   /**
   * Addendum to the SUN specs.
   * If system property 'propName' is not found, null is returned.
   */
  public static Font getFont(String propName) {
    return getFont(propName, null);
  }

   /**
   * Addendum to the SUN specs.
   * If system property 'propName' is not found, whatever was specified for 'defaultFont' is returned.
   */
  public static Font getFont(String propName, Font defaultFont) {
    String propValue = System.getProperty(propName);

    if (propValue!=null) {
      return Font.decode(propValue);
    }
    else {
      return defaultFont;
    }
  }
  
  public String getFontName() {
    System.out.println("[AWT warning] getFontName() not supported, use getName() instead");
    return getName();
  }

  public String getName() {
    return name;
  }
  
  public int getSize() {
    return size;
  }
  
  public int getStyle() {
    return style;
  }

  public int hashCode() {
    return (new String(name + style + size).hashCode());
  }  
  
  public boolean isBold() {
    if ((style & BOLD) != 0) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public boolean isItalic() {
    if ((style & ITALIC) != 0) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public boolean isPlain() {
    if (style == 0) {
      return true;
    }
    else {
      return false;
    }
  }
  
  public String toString() {

    String styleStr;
    switch (style) {
      case PLAIN:
        styleStr="plain";
        break;
      case BOLD:
        styleStr="bold";
        break;
      case ITALIC:
        styleStr="italic";
        break;
      case BOLD|ITALIC:
        styleStr="bolditalic";
        break;
      default:
        styleStr="plain";
        break;
    }
    return getClass().getName() + "[family = " + family + ", name = " + name + ", style = " + styleStr + ", size = " + size + "]";
  }

}
