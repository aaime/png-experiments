package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for BufferedImage with {@link BufferedImage#TYPE_BYTE_INDEXED} or
 * {@link BufferedImage#TYPE_4BYTE_ABGR} types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteIndexedProvider extends RasterByteGrayProvider {

    private final IndexColorModel palette;

    public RasterByteIndexedProvider(Raster raster, IndexColorModel palette) {
        super(raster);
        this.palette = palette;
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Paletted;
    }

    @Override
    public IndexColorModel getPalette() {
        return palette;
    }

}
