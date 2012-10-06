package be.kiffer.vixen;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsConfigTemplate;
import java.awt.Window;

public class DefaultScreenDevice extends GraphicsDevice {

public static final int TYPE_RASTER_SCREEN = 0;
public static final int TYPE_PRINTER = 1;
public static final int TYPE_IMAGE_BUFFER = 2;

  private GraphicsConfiguration defaultGraphicsConfiguration = new DefaultGraphicsConfiguration();

  public int getType() {
    return TYPE_RASTER_SCREEN;
  }

  public String getIDstring() {
    return "Vixen DefaultScreenDevice";
  }

  public GraphicsConfiguration[] getConfigurations() {
    return new GraphicsConfiguration[] { defaultGraphicsConfiguration };
  }

  public GraphicsConfiguration getDefaultConfiguration() {
    return defaultGraphicsConfiguration;
  }

  // TODO should this return null if gct is too ambitious?
  public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
    return defaultGraphicsConfiguration;
  }

  public boolean isFullScreenSupported() {
    // throw IllegalArgumentException if DisplayMode is null or is not available 
    // throw UnsupportedOperationException if isDisplayChangeSupported returns false
        throw new RuntimeException("not yet implemented");
  }

  public void setFullScreenWindow(Window w) {
        throw new RuntimeException("not yet implemented");
  }

  public Window getFullScreenWindow() {
        throw new RuntimeException("not yet implemented");
  }

  public boolean isDisplayChangeSupported() {
        throw new RuntimeException("not yet implemented");
  }

  public void setDisplayMode(DisplayMode dm) {
        throw new RuntimeException("not yet implemented");
  }

  public DisplayMode getDisplayMode() {
        throw new RuntimeException("not yet implemented");
  }

  public DisplayMode[] getDisplayModes() {
        throw new RuntimeException("not yet implemented");
  }

  public int getAvailableAcceleratedMemory() {
        throw new RuntimeException("not yet implemented");
  }
}

