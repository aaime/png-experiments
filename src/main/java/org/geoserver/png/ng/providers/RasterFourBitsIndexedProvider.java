package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with 4-bit paletted rasters
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterFourBitsIndexedProvider extends RasterFourBitsGrayProvider {

    private final IndexColorModel palette;

    public RasterFourBitsIndexedProvider(Raster raster, IndexColorModel palette) {
        super(raster);
        this.palette = palette;
        int pixelSize = palette.getPixelSize();
        if (pixelSize != 4) {
            throw new IllegalArgumentException(
                    "Illegal index color mode, expected 4 bits per pixel but it's " + pixelSize);
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
        return 4;
    }

}
