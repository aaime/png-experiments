package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

import sun.awt.image.BytePackedRaster;

/**
 * A scanline provider optimized for rasters with two bits gray pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class RasterTwoBitsGrayProvider implements ScanlineProvider {

    final Raster raster;

    final byte[] bytes;

    final ScanlineCursor cursor;

    final byte[] scanline;

    public RasterTwoBitsGrayProvider(Raster raster) {
        this.raster = raster;
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        int rowLength = (raster.getWidth() + 2) / 4;
        this.scanline = new byte[rowLength];
        if(!(raster instanceof BytePackedRaster)) {
            throw new IllegalArgumentException("The raster was supposed to have a byte packed raster type");
        }
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
        return 2;
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
