package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for BufferedImage with {@link BufferedImage#TYPE_BYTE_INDEXED} or
 * {@link BufferedImage#TYPE_4BYTE_ABGR} types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class BufferedImageByteIndexedProvider extends BufferedImageGrayByteProvider {

    public BufferedImageByteIndexedProvider(BufferedImage image) {
        super(image);
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Paletted;
    }

    @Override
    public IndexColorModel getPalette() {
        return (IndexColorModel) image.getColorModel();
    }

}
