package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;

import org.geoserver.png.ng.ColorType;

import ar.com.hjg.pngj.IImageLine;

public interface ScanlineProvider extends IImageLine {

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
     * The number of byte[] elements in the scaline
     * @return
     */
    public int getScanlineLength();
    
    /**
     * The color type of this image
     */
    public ColorType getColorType();

    /**
     * The next scanline, or throws an exception if we got past the end of the image
     * 
     * @return
     */
    void next(byte[] scaline, int offset, int length);
    
    /**
     * Returns the palette for this image, or null if the image does not have one 
     * @return
     */
    IndexColorModel getPalette();
}
