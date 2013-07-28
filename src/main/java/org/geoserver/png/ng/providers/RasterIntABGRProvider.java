package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with int packed RGB or RGBA pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterIntABGRProvider implements ScanlineProvider {

    final Raster raster;

    final int[] pixels;

    final byte[] row;

    final int rowLength;

    final boolean hasAlpha;

    final ColorType colorType;

    final boolean bgrOrder;

    final ScanlineCursor cursor;

    public RasterIntABGRProvider(Raster raster, boolean hasAlpha) {
        this.raster = raster;
        pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
        this.hasAlpha = hasAlpha;
        this.cursor = new ScanlineCursor(raster);
        final int bpp;
        if (hasAlpha) {
            bpp = 4;
            colorType = ColorType.RGBA;
            bgrOrder = false;
        } else {
            bpp = 3;
            colorType = ColorType.RGB;
            bgrOrder = ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitOffsets()[0] != 0;
        }
        rowLength = raster.getWidth() * bpp;
        row = new byte[rowLength];
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
        return colorType;
    }

    @Override
    public byte[] next() {
        int pxIdx = cursor.next();
        int i = 0;
        if (hasAlpha) {
            while (i < rowLength) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 24) & 0xff);
            }
        } else if (bgrOrder) {
            while (i < rowLength) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color >> 16) & 0xff);
            }
        } else {
            while (i < rowLength) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
            }
        }
        return row;
    }

    @Override
    public IndexColorModel getPalette() {
        // no palette
        return null;
    }

}
