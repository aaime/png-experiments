package org.geoserver.png.ng.providers;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for Raster objects containig a 8bit BGR or ABGR image
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterByteABGRProvider implements ScanlineProvider {

    final Raster raster;

    final byte[] bytes;

    final byte[] row;

    final int rowLength;

    final boolean hasAlpha;

    final ColorType colorType;

    final boolean bgrOrder;
    
    final ScanlineCursor cursor;
    
    public RasterByteABGRProvider(Raster raster, boolean hasAlpha) {
        this.raster = raster;
        bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        this.hasAlpha = hasAlpha;
        if (hasAlpha) {
            rowLength = raster.getWidth() * 4;
            colorType = ColorType.RGBA;
        } else {
            rowLength = raster.getWidth() * 3;
            colorType = ColorType.RGB;
        }
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        bgrOrder = sm.getBandOffsets()[0] != 0;
        cursor = new ScanlineCursor(raster);
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
        int bytesIdx = cursor.next();
        int i = 0;
        if (!bgrOrder) {
            System.arraycopy(bytes, bytesIdx, row, 0, rowLength);
        } else {
            if (hasAlpha) {
                while (i < rowLength) {
                    final byte a = bytes[bytesIdx++];
                    final byte b = bytes[bytesIdx++];
                    final byte g = bytes[bytesIdx++];
                    final byte r = bytes[bytesIdx++];
                    row[i++] = r;
                    row[i++] = g;
                    row[i++] = b;
                    row[i++] = a;
                }
            } else {
                while (i < rowLength) {
                    final byte b = bytes[bytesIdx++];
                    final byte g = bytes[bytesIdx++];
                    final byte r = bytes[bytesIdx++];
                    row[i++] = r;
                    row[i++] = g;
                    row[i++] = b;
                }
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
