package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with 8-bit indexed rasters
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteIndexedProvider extends RasterByteGrayProvider {

    private final IndexColorModel palette;

    public RasterByteIndexedProvider(Raster raster, IndexColorModel palette) {
        super(raster);
        this.palette = palette;
        int pixelSize = palette.getPixelSize();
        if (pixelSize != 8) {
            throw new IllegalArgumentException(
                    "Illegal index color mode, expected 8 bits per pixel but it's " + pixelSize);
        }
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Paletted;
    }

    @Override
    public IndexColorModel getPalette() {
        return palette;
    }

    @Override
    public byte getBitDepth() {
        byte pixelSize = (byte) palette.getPixelSize();
        return pixelSize;
    }

}
