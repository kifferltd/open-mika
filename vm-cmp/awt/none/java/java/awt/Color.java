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

public class Color implements java.io.Serializable {
  
  private static final long serialVersionUID = 118526816881161077L;
  
  /******************************/
  /**
   * Definitions
   * @status  Complient with java specs
   * @remark  Complient with java specs
   */

  public final static Color lightGray = new Color(192, 192, 192);
  public final static Color gray = new Color(128, 128, 128);
  public final static Color darkGray = new Color(64, 64, 64);
  public final static Color black = new Color(0, 0, 0);
  public final static Color red = new Color(255, 0, 0);
  public final static Color pink = new Color(255, 175, 175);
  public final static Color orange = new Color(255, 200, 0);
  public final static Color yellow = new Color(255, 255, 0);
  public final static Color green = new Color(0, 255, 0);
  public final static Color magenta = new Color(255, 0, 255);
  public final static Color cyan = new Color(0, 255, 255);
  public final static Color blue = new Color(0, 0, 255);
  public final static Color white = new Color(255, 255, 255);

  private static final double FILTER = 0.7;

  /******************************/
  /**
   * variable
   */
  int value;

  /******************************/
  /**
   * Constructors
   * @status  Constructors present are compliant with specs, though several constructors are missing
   * @remark  Complient with java specs
   */
  public Color(int r, int g, int b) {
    this(r, g, b, 255);
  }

  public Color(int r, int g, int b, int a) {
    value = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
  }

  public Color(int rgb) {
    value = 0xff000000 | rgb;
  }

  public Color(float r, float g, float b) {
    this((int)(r * 255 + 0.5), (int)(g * 255 + 0.5), (int)(b * 255 + 0.5));
  }

  /******************************/
  /**
   * get color components
   * @status  Implemented
   * @remark  Complient with java specs
   */
  public int getRed() {
    return (getRGB() >> 16) & 0xFF;
  }

  public int getGreen() {
    return (getRGB() >> 8) & 0xFF;
  }

  public int getBlue() {
    return (getRGB() >> 0) & 0xFF;
  }

  public int getAlpha() {
    return (getRGB() >> 24) & 0xFF;
  }

  public int getRGB() {
    return value;
  }

  /******************************/
  /**
   * Color brighter/darker
   * @status  Implemented
   * @remark  Complient with java specs
   */
  public Color brighter() {
    int r = getRed();
    int g = getGreen();
    int b = getBlue();

    int i = (int)(1.0 / (1.0 - FILTER));

    if (r == 0 && g == 0 && b == 0) {
       return new Color(i, i, i);
    }

    if (r > 0 && r < i) r = i;
    if (g > 0 && g < i) g = i;
    if (b > 0 && b < i) b = i;

    return new Color(Math.min((int)(r / FILTER), 255), Math.min((int)(g / FILTER), 255), Math.min((int)(b / FILTER), 255));
  }

  public Color darker() {
    return new Color(Math.max((int)(getRed() * FILTER), 0), Math.max((int)(getGreen() * FILTER), 0), Math.max((int)(getBlue() * FILTER), 0));
  }

  /******************************/
  /**
   * System color
   */
   public static Color getColor(String propertyname) {
     String colorstr = System.getProperty( propertyname, null);
     if(colorstr != null) {
       return new Color(Integer.parseInt(colorstr));
     }
     else {
       return null;
     }
   }

   public static Color getColor(String propertyname, Color defaultcolor) {
     String colorstr = System.getProperty( propertyname, null);
     if(colorstr != null) {
       return new Color(Integer.parseInt(colorstr));
     }
     else {
       return defaultcolor;
     }
   }

   public static Color getColor(String propertyname, int defaultcolorint) {
     String colorstr = System.getProperty( propertyname, null);
     if(colorstr != null) {
       return new Color(Integer.parseInt(colorstr));
     }
     else {
       return new Color(defaultcolorint);
     }
   }

  /******************************/
  /**
   * RGB to HSB (HSL)
   * /
   */

   public static float[] RGBtoHSB(int r, int g, int b, float hsb[]) {

     float min, max, delta;
     float h, s, v;
     float r2 = (float)r / 255;
     float g2 = (float)g / 255;
     float b2 = (float)b / 255;

     if(hsb == null) hsb = new float[3];
     
     min = Math.min(Math.min(r2, g2), b2);
     max = Math.max(Math.max(r2, g2), b2);
     v = max;

     delta = max - min;

     if(max != 0)
       s = delta / max;
     else {
       // r = g = b = 0    // s = 0, v is undefined
       hsb[0] = 0;
       hsb[1] = 0;
       hsb[2] = 0;
       return hsb;
     }

     if(r2 == max)
       h = (g2 - b2) / delta;      // between yellow & magenta
     else if(g2 == max)
       h = 2 + (b2 - r2) / delta;  // between cyan & yellow
     else
       h = 4 + (r2 - g2) / delta;  // between magenta & cyan

     if( h < 0 )
       h += 6;
     h /= 6;                       // 0.0 - 1.0

     hsb[0] = h;
     hsb[1] = s;
     hsb[2] = v;
     
     return hsb;

   }

   public static int HSBtoRGB(float h, float s, float v) {

     int i;
     float f, p, q, t;
     float r, g, b;

     if(s == 0) {
       // achromatic (grey)
       r = g = b = v;
       return (255 << 24) | ((int)(r*255) << 16) | ((int)(g*255) << 8) | (int)(b*255);
     }

     h *= 6;      // sector 0 to 5
     i = (int)Math.floor(h);
     f = h - i;      // factorial part of h
     p = v * (1 - s);
     q = v * (1 - s * f);
     t = v * (1 - s * (1 - f));

     switch(i) {
       case 0:  r = v;
                g = t;
                b = p;
                break;
       case 1:  r = q;
                g = v;
                b = p;
                break;
       case 2:  r = p;
                g = v;
                b = t;
                break;
       case 3:  r = p;
                g = q;
                b = v;
                break;
       case 4:  r = t;
                g = p;
                b = v;
                break;
       default: r = v;
                g = p;
                b = q;
                break;
     }

     return (255 << 24) | ((int)(r*255) << 16) | ((int)(g*255) << 8) | (int)(b*255);

   }

   public static Color getHSBColor(float hue, float saturation, float brightness){
     return new Color(HSBtoRGB(hue,saturation,brightness));
   }

  /**
   * @status  implemented
   * @remark  Currently, not compliant with SUN specs. Beside string representations of
   * one decimal, one hexadecimal or one octal number, it accepts strings representing
   * three decimal, three hexadecimal or three octal numbers separated by commas. <br>
   * Such strings cause a 'NumberFormatException' being thrown by sun's jdk1.2.2 implementation.
   * Accoding to the specs, wonka accepts '0x' and '0X' for hexadecimal radix.
   */

   public static Color decode(String nm) throws NumberFormatException {

     int idx = nm.indexOf(',');
     if (idx != -1) {
       int idx2 = nm.indexOf(',', idx+1);
       if (idx2 == -1){
         throw new NumberFormatException("String '"+nm+"' has the wrong number of commas to be a valid color");
       }
       int r = Integer.decode(nm.substring(0,idx)).intValue();
       int g = Integer.decode(nm.substring(idx+1,idx2)).intValue();
       int b = Integer.decode(nm.substring(idx2+1)).intValue();
       return new Color(r, g, b);
     }
     else {
       return new Color(Integer.decode(nm).intValue());
     }
   }
   
  /******************************/
  /**
   * Equals & hashcode
   * @status  Implemented
   * @remark  Complient with java specs
   */
  public boolean equals(Object object) {

    if (!(object instanceof Color))
      return false;
    else {
      Color c = (Color)object;
      return (this.getRed()==c.getRed() && this.getGreen()==c.getGreen() && this.getBlue()==c.getBlue());
    }
  }

  public int hashCode() {
    return value;
  }

  /******************************/
  /**
   * Diagnostics
   */
  public String toString() {
    return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]";
  }
  protected String paramString() {
    return getClass().getName() + "(" + getRed() + ", " + getGreen() + ", " + getBlue() + ",alfa "+getAlpha()+")";
  }
}
