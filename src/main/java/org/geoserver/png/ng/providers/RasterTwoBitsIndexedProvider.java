package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with two bits paletted pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterTwoBitsIndexedProvider extends RasterTwoBitsGrayProvider {

    private final IndexColorModel palette;

    public RasterTwoBitsIndexedProvider(Raster raster, IndexColorModel palette) {
        super(raster);
        this.palette = palette;
        int pixelSize = palette.getPixelSize();
        if (pixelSize != 2) {
            throw new IllegalArgumentException(
                    "Illegal index color mode, expected 2 bits per pixel but it's " + pixelSize);
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
        return 2;
    }

}
