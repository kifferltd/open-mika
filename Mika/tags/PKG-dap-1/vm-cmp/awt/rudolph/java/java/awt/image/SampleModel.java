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

public abstract class SampleModel {

  protected int dataType;
  protected int height;
  protected int numBands;
  protected int width;

  public SampleModel(int dataType, int w, int h, int numBands) {
    this.dataType = dataType;
    this.height = h;
    this.width = w;
    this.numBands = numBands;
  }

  public abstract SampleModel createCompatibleSampleModel(int w, int h);
  public abstract DataBuffer createDataBuffer();
  public abstract SampleModel createSubsetSampleModel(int[] bands);
  public abstract Object getDataElements(int x, int y, Object obj, DataBuffer data);
  public abstract int getNumDataElements();
  public abstract int getSample(int x, int y, int b, DataBuffer data);
  public abstract int[] getSampleSize();
  public abstract int getSampleSize(int band);
  public abstract void setDataElements(int x, int y, Object obj, DataBuffer data);
  public abstract void setSample(int x, int y, int b, int s, DataBuffer data);
  
  public Object getDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
   return null;
  }
  
  public final int getDataType() {
    return dataType;
  }
  
  public final int getHeight() {
    return height;
  }
  
  public final int getWidth() {
    return width;
  }
  
  public final int getNumBands() {
    return numBands;
  }
  
  public int getTransferType() {
    return dataType;
  }
  
  public double[] getPixel(int x, int y, double[] dArray, DataBuffer data) {
    return null;
  }
  
  public float[] getPixel(int x, int y, float[] fArray, DataBuffer data) {
    return null;
  }
  
  public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
    return null;
  }
  
  public double[] getPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
    return null;
  }
  
  public float[] getPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
    return null;
  }
  
  public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
    return null;
  }
  
  public double getSampleDouble(int x, int y, int b, DataBuffer data) {
    return 0;
  }
  
  public float getSampleFloat(int x, int y, int b, DataBuffer data) {
    return 0;
  }
  
  public double[] getSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
    return null;
  }
  
  public float[] getSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
    return null;
  }
  
  public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
    return null;
  }
  
  public void setDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
  }
  
  public void setPixel(int x, int y, double[] dArray, DataBuffer data) {
  }
  
  public void setPixel(int x, int y, float[] fArray, DataBuffer data) {
  }
  
  public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
  }
  
  public void setPixels(int x, int y, int w, int h, double[] dArray, DataBuffer data) {
  }
  
  public void setPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
  }
  
  public void setPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
  }
  
  public void setSample(int x, int y, int b, double s, DataBuffer data) {
  }
  
  public void setSample(int x, int y, int b, float s, DataBuffer data) {
  }
  
  public void setSamples(int x, int y, int w, int h, int b, double[] dArray, DataBuffer data) {
  }
  
  public void setSamples(int x, int y, int w, int h, int b, float[] fArray, DataBuffer data) {
  }
  
  public void setSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
  }

}

