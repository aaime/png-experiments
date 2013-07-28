package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

import sun.awt.image.BytePackedRaster;

/**
 * A scanline provider optimized rasters with one bit b/w pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class RasterOneBitGrayProvider implements ScanlineProvider {

    final Raster raster;

    final byte[] bytes;

    final ScanlineCursor cursor;

    final byte[] scanline;

    public RasterOneBitGrayProvider(Raster raster) {
        this.raster = raster;
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        int rowLength = (raster.getWidth() + 4) / 8;
        this.scanline = new byte[rowLength];
        this.cursor = new ScanlineCursor(raster);
        if (!(raster instanceof BytePackedRaster)) {
            throw new IllegalArgumentException(
                    "The raster was supposed to have a byte packed raster type");
        }
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
        return 1;
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Grayscale;
    }

    @Override
    public IndexColorModel getPalette() {
        return null;
    }

    @Override
    public byte[] next() {
        final int rowLength = scanline.length;
        System.arraycopy(bytes, cursor.next(), scanline, 0, rowLength);
        return scanline;
    }

}
