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

package com.acunia.wonka.test.awt.Image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class RGBImageFilter extends VisualTestImpl {

  public class GrayFilter extends java.awt.image.RGBImageFilter {

    private ColorModel model = ColorModel.getRGBdefault();
  
    public int filterRGB(int x, int y, int rgb) {
      float HSB[] = new float[3];
      Color.RGBtoHSB(model.getRed(rgb), model.getGreen(rgb), model.getBlue(rgb), HSB);
      return Color.HSBtoRGB(HSB[0], 0, HSB[2]); // Set saturation to 0.
    }

  }

  public class MaskFilter extends java.awt.image.RGBImageFilter {

    private int      mask;
    public MaskFilter(int mask) {
      super();
      this.mask = mask;
    }

    public int filterRGB(int x, int y, int rgb) {
      return (rgb & mask);
    }

  }
  
  public class ShadeFilter extends java.awt.image.RGBImageFilter {

    private int        w;
    private int        h;
    private ColorModel model = ColorModel.getRGBdefault();
  
    public void setDimensions(int w, int h) {
      super.setDimensions(w, h);
      this.w = w;
      this.h = h;
    }
    
    public int filterRGB(int x, int y, int rgb) {
      int f1 = ((x < w/2 ? x : w - x) << 8) / (w/2);
      int f2 = ((y < h/2 ? y : h - y) << 8) / (h/2);
      int r = (model.getRed(rgb) * f1 * f2) >>> 16;
      int g = (model.getGreen(rgb) * f1 * f2) >>> 16;
      int b = (model.getBlue(rgb) * f1 * f2) >>> 16;
      return (255 << 24) | (r << 16) | (g << 8) | b;
    }

  }

  public class InvertFilter extends java.awt.image.RGBImageFilter {
    public int filterRGB(int x, int y, int rgb) {
      return (0xFFFFFFFF - rgb) | 0xFF000000;
    }

  }

  public class MyPanel extends Panel {
    private Image image;
    private String label;
    public MyPanel(Image image, String label) {
      this.image = image;
      this.label = label;
      this.setBackground(Color.black);
    }
    public void paint(Graphics g) {
      g.drawImage(image, 1, 1, null);
      g.setColor(Color.white);
      g.drawString(label, 35, 60);
    }
  }


  public RGBImageFilter() {
    String path = System.getProperty("vte.image.path", "{}/test/image");
    Image original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");

    Image color1 = createImage(new FilteredImageSource(original.getSource(), new MaskFilter(0xFFE0E0E0)));
    Image color2 = createImage(new FilteredImageSource(original.getSource(), new MaskFilter(0xFF808080)));
    
    Image red = createImage(new FilteredImageSource(original.getSource(), new MaskFilter(0xFFFF0000)));
    Image green = createImage(new FilteredImageSource(original.getSource(), new MaskFilter(0xFF00FF00)));
    Image blue = createImage(new FilteredImageSource(original.getSource(), new MaskFilter(0xFF0000FF)));

    Image gray = createImage(new FilteredImageSource(original.getSource(), new GrayFilter()));
    Image invert = createImage(new FilteredImageSource(original.getSource(), new InvertFilter()));
    Image shaded = createImage(new FilteredImageSource(original.getSource(), new ShadeFilter()));
    
    setLayout(new GridLayout(3, 3));

    add(new MyPanel(original,  "   Original   "));
    add(new MyPanel(color1,    " 3 bit/color  "));
    add(new MyPanel(color2,    " 1 bit/color  "));

    add(new MyPanel(red,       "  red layer   "));
    add(new MyPanel(green,     " green layer  "));
    add(new MyPanel(blue,      "  blue layer  "));

    add(new MyPanel(gray,      "   grayscale  "));
    add(new MyPanel(invert,    "   inverted   "));
    add(new MyPanel(shaded,    "    shaded    "));

  }

  public String getHelpText(){
    return "A Simple ImageFilter / FilteredImageSource test\n" +
           "\n" +
           "You should see 9 images in a 3 by 3 grid. The first " +
           "image on the first row is the original. Then there are 2 " +
           "copies with less and even lesser colors.\n" +
           "The second row contains the red layer, the green layer " +
           "and the blue layer of the image.\n" +
           "The third row are mixtures. The first should look yellow, " +
           "the second should look cyan and the third should look " +
           "purple";
  }
}

