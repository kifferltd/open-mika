package be.kiffer.vixen;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

public class VixenGraphicsEnvironment extends GraphicsEnvironment {

    public Graphics2D createGraphics(BufferedImage bufferedImage) {
        throw new RuntimeException("not yet implemented");
    }

    public Font[] getAllFonts() {
        throw new RuntimeException("not yet implemented");
    }

    public String[] getAvailableFontFamilyNames() {
        throw new RuntimeException("not yet implemented");
    }

    public String[] getAvailableFontFamilyNames(Locale locale) {
        throw new RuntimeException("not yet implemented");
    }

    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
        throw new RuntimeException("not yet implemented");
    }

    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
        throw new RuntimeException("not yet implemented");
    }

}

