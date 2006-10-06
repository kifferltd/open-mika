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

package java.awt.image;

public abstract class RGBImageFilter extends ImageFilter {

  protected boolean     canFilterIndexColorModel;
  protected ColorModel  newmodel = null;
  protected ColorModel  origmodel = null;

  public IndexColorModel filterIndexColorModel(IndexColorModel icm) {
    ColorModel RGBmodel = ColorModel.getRGBdefault();
    byte[] cmap = new byte[icm.getMapSize() * 4];
    int color;
    int j = 0;    
    
    for(int i=0; i<icm.getMapSize(); i++) {
      color = filterRGB(-1, -1, icm.getRGB(i));
      cmap[j++] = (byte)(RGBmodel.getRed(color));
      cmap[j++] = (byte)(RGBmodel.getGreen(color));
      cmap[j++] = (byte)(RGBmodel.getBlue(color));
      cmap[j++] = (byte)(RGBmodel.getAlpha(color));
    }

    return new IndexColorModel(icm.getPixelSize(), icm.getMapSize(), cmap, 0, true, icm.getTransparentPixel());
  }

  public abstract int filterRGB(int x, int y, int rgb);

  public void filterRGBPixels(int x, int y, int w, int h, int[] pixels, int offset, int scansize) {
    for(int i=0; i<h; i++) {
      for(int j=0; j<w; j++) {
        pixels[i * scansize + j + offset] = filterRGB(x + j, y + i, pixels[i * scansize + j + offset]);
      }
    }
  }
  
  public void setColorModel(ColorModel model) {
    if((model instanceof IndexColorModel) && canFilterIndexColorModel) {
      substituteColorModel(model, filterIndexColorModel((IndexColorModel)model));
      super.setColorModel(newmodel);
    } else {
      super.setColorModel(ColorModel.getRGBdefault());
    }
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    if(newmodel == null) {
      int[] newpixels = new int[pixels.length];
      for(int i=0; i<h; i++) {
        for(int j=0; j<w; j++) {
          newpixels[i * scansize + j + off] = model.getRGB(pixels[i * scansize + j + off]);
        }
      }
      filterRGBPixels(x, y, w, h, newpixels, off, scansize);
      super.setPixels(x, y, w, h, ColorModel.getRGBdefault(), newpixels, 0, w);
    } else {
      super.setPixels(x, y, w, h, newmodel, pixels, 0, w);
    }
  }
  
  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    int[] newpixels = pixels;
    if(newmodel == null) {
      newpixels = new int[pixels.length];
      for(int i=0; i<h; i++) {
        for(int j=0; j<w; j++) {
          newpixels[i * scansize + j + off] = model.getRGB(pixels[i * scansize + j + off]);
        }
      }
      filterRGBPixels(x, y, w, h, newpixels, off, scansize);
      super.setPixels(x, y, w, h, ColorModel.getRGBdefault(), newpixels, off, scansize);
    } else {
      super.setPixels(x, y, w, h, newmodel, newpixels, 0, w);
    }
  }

  public void substituteColorModel(ColorModel oldcm, ColorModel newcm) {
    origmodel = oldcm;
    newmodel = newcm;
  }
  
}

