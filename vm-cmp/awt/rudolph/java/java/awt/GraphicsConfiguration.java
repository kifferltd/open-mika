/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Alexey A. Petrenko
 * @author Chris Gray
 */
package java.awt;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

public abstract class GraphicsConfiguration {

    private static ImageCapabilities defaultImageCapabilities;

   /***************************************************************************
    *
    *  Constructors
    *
    ***************************************************************************/

    protected GraphicsConfiguration() {
    }

    public abstract GraphicsDevice getDevice();

    public abstract BufferedImage createCompatibleImage(int arg0, int arg1);

    public VolatileImage createCompatibleImage(int arg0, int arg1, int arg2) {
      throw new RuntimeException("not yet implemented");
    }

    public VolatileImage createCompatibleVolatileImage(int arg0, int arg1) {
      throw new RuntimeException("not yet implemented");
    }

    public VolatileImage createCompatibleVolatileImage(int arg0, int arg1, int arg2) {
      throw new RuntimeException("not yet implemented");
    }

    public VolatileImage createCompatibleVolatileImage(int arg0, int arg1, ImageCapabilities arg2) throws java.awt.AWTException {
      throw new RuntimeException("not yet implemented");
    }

    public VolatileImage createCompatibleVolatileImage(int arg0, int arg1, ImageCapabilities arg2, int arg3) throws java.awt.AWTException {
      throw new RuntimeException("not yet implemented");
    }

    public abstract ColorModel getColorModel(int i);

    public abstract ColorModel getColorModel();

    public abstract AffineTransform getDefaultTransform();

    public abstract AffineTransform getNormalizingTransform();

    public abstract Rectangle getBounds();

    public BufferCapabilities getBufferCapabilities() {
      throw new RuntimeException("not yet implemented");
    }

    public ImageCapabilities getImageCapabilities() {
      if (defaultImageCapabilities == null) {
//        defaultImageCapabilities = new ImageCapabilities(0);
      }
      
      return defaultImageCapabilities;
    }
}

