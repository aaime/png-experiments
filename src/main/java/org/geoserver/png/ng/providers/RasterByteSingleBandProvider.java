package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider that can copy 1-1 data from the buffered image into the scanline without
 * performing any kind of transformation
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteSingleBandProvider extends AbstractScanlineProvider {

    final byte[] bytes;

    public RasterByteSingleBandProvider(Raster raster, int bitDepth, int scanlineLength, ColorType colorType) {
        super(raster, bitDepth, scanlineLength, colorType);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    public RasterByteSingleBandProvider(Raster raster, int bitDepth, int scanlineLength,
            IndexColorModel palette) {
        super(raster, bitDepth, scanlineLength, palette);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
    }

    @Override
    public void next(final byte[] scanline, final int offset, final int length) {
        if (this.currentRow == height) {
            throw new IllegalStateException("All scanlines have been read already");
        }

        System.arraycopy(bytes, cursor.next(), scanline, offset, length);
        currentRow++;
    }

}
