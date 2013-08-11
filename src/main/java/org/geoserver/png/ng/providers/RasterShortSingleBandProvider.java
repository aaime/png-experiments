package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for a Raster with 16 bit gray pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterShortSingleBandProvider extends AbstractScanlineProvider {

    final short[] shorts;

    public RasterShortSingleBandProvider(Raster raster) {
        super(raster, 16, raster.getWidth() * 2, ColorType.Grayscale);
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
    }
    
    public RasterShortSingleBandProvider(Raster raster, IndexColorModel palette) {
        super(raster, 16, raster.getWidth() * 2, palette);
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
    }

    @Override
    public void next(final byte[] scanline, final int offset, final int length) {
        int shortsIdx = cursor.next();
        int i = offset;
        int max = offset + length;
        while (i < max) {
            short gray = shorts[shortsIdx++];
            scanline[i++] = (byte) ((gray >> 8) & 0xFF);
            scanline[i++] = (byte) (gray & 0xFF);
        }
    }

}
