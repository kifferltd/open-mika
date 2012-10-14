package be.kiffer.vixen;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

public class DefaultGraphicsConfiguration extends GraphicsConfiguration {

  public GraphicsDevice getDevice() {
        throw new RuntimeException("not yet implemented");
  }

  public BufferedImage createCompatibleImage(int width, int height) {
        throw new RuntimeException("not yet implemented");
  }

  public BufferedImage createCompatibleImage(int width, int height, int transparency) {
        throw new RuntimeException("not yet implemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height) {
        throw new RuntimeException("not yet implemented");
  }

  public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
        throw new RuntimeException("not yet implemented");
  }

  public ColorModel getColorModel() {
        throw new RuntimeException("not yet implemented");
  }

  public ColorModel getColorModel(int transparency) {
        throw new RuntimeException("not yet implemented");
  }

  public AffineTransform getDefaultTransform() {
        throw new RuntimeException("not yet implemented");
  }

  public AffineTransform getNormalizingTransform() {
        throw new RuntimeException("not yet implemented");
  }

  public Rectangle getBounds() {
        throw new RuntimeException("not yet implemented");
  }

  public BufferCapabilities getBufferCapabilities() {
        throw new RuntimeException("not yet implemented");
  }

  public ImageCapabilities getImageCapabilities() {
        throw new RuntimeException("not yet implemented");
  }
}

