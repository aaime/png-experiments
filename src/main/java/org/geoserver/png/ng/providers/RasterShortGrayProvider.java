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
public class RasterShortGrayProvider implements ScanlineProvider {

    final Raster raster;

    final short[] shorts;

    final ScanlineCursor cursor;

    final byte[] scanline;

    public RasterShortGrayProvider(Raster raster) {
        this.raster = raster;
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
        this.scanline = new byte[raster.getWidth() * 2];
        this.cursor = new ScanlineCursor(raster);
    }

    @Override
    public int getWidth() {
        return raster.getWidth();
    }

    @Override
    public int getHeight() {
        return raster.getHeight();
    }

    @Override
    public byte getBitDepth() {
        return 16;
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Grayscale;
    }

    @Override
    public byte[] next() {
        final int width = raster.getWidth();
        int shortsIdx = cursor.next();
        int i = 0;
        while (i < scanline.length) {
            short gray = shorts[shortsIdx++];
            scanline[i++] = (byte) ((gray >> 8) & 0xFF);
            scanline[i++] = (byte) (gray & 0xFF);
        }

        return scanline;
    }

    @Override
    public IndexColorModel getPalette() {
        return null;
    }

}
