package org.geoserver.png.ng.providers;

import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for a Raster with a 16 bit indexed color model
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterShortIndexedProvider extends RasterShortGrayProvider {

    private final IndexColorModel palette;

    public RasterShortIndexedProvider(Raster raster, IndexColorModel palette) {
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
