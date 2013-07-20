package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;

import org.geoserver.png.ng.ColorType;

public interface ScanlineProvider {

    /**
     * Image width
     * 
     * @return
     */
    int getWidth();
    
    /**
     * Image height
     * @return
     */
    int getHeight();
    
    /**
     * The bit depth of this image, 1, 2, 4, 8 or 16
     * @return
     */
    public byte getBitDepth();
    
    /**
     * The color type of this image
     */
    public ColorType getColorType();

    /**
     * The next scanline, or null if not available
     * 
     * @return
     */
    byte[] next();
    
    /**
     * Returns the palette for this image, or null if the image does not have one 
     * @return
     */
    IndexColorModel getPalette();
}
