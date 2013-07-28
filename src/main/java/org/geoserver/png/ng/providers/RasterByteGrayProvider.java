package org.geoserver.png.ng.providers;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with 8-bit gray channel
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class RasterByteGrayProvider implements ScanlineProvider {

    final Raster raster;

    final byte[] bytes;

    int currentRow = 0;

    final byte[] scanline;

    final ScanlineCursor cursor;

    public RasterByteGrayProvider(Raster raster) {
        this(raster, new byte[raster.getWidth()]);
    }

    protected RasterByteGrayProvider(Raster raster, byte[] scanline) {
        this.raster = raster;
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        this.scanline = scanline;
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
        return 8;
    }

    @Override
    public ColorType getColorType() {
        return ColorType.Grayscale;
    }

    @Override
    public byte[] next() {
        if (this.currentRow == this.raster.getHeight()) {
            return null;
        }

        System.arraycopy(bytes, cursor.next(), scanline, 0, raster.getWidth());

        currentRow++;
        return scanline;
    }

    @Override
    public IndexColorModel getPalette() {
        return null;
    }

}
