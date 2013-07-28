package org.geoserver.png.ng.providers;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for Raster objects containing a 16bit BGR or ABGR image
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterShortABGRProvider implements ScanlineProvider {

    final Raster raster;

    final short[] shorts;

    final byte[] row;

    final int rowLength;

    final boolean hasAlpha;

    final ColorType colorType;

    final ScanlineCursor cursor;

    final boolean bgrOrder;

    public RasterShortABGRProvider(Raster raster, boolean hasAlpha) {
        this.raster = raster;
        shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
        this.hasAlpha = hasAlpha;
        if (hasAlpha) {
            rowLength = raster.getWidth() * 4;
            colorType = ColorType.RGBA;
        } else {
            rowLength = raster.getWidth() * 3;
            colorType = ColorType.RGB;
        }
        bgrOrder = ((ComponentSampleModel) raster.getSampleModel()).getBandOffsets()[0] != 0;
        row = new byte[rowLength * 2];
        cursor = new ScanlineCursor(raster);
    }

    @Override
    public int getWidth() {
        return  raster.getWidth();
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
        return colorType;
    }

    @Override
    public byte[] next() {
        int shortsIdx = cursor.next();
        int i = 0;
        if (hasAlpha) {
            if (bgrOrder) {
                while (i < row.length) {
                    final short a = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short r = shorts[shortsIdx++];
                    row[i++] = (byte) ((r >> 8) & 0xFF);
                    row[i++] = (byte) (r & 0xFF);
                    row[i++] = (byte) ((g >> 8) & 0xFF);
                    row[i++] = (byte) (g & 0xFF);
                    row[i++] = (byte) ((b >> 8) & 0xFF);
                    row[i++] = (byte) (b & 0xFF);
                    row[i++] = (byte) ((a >> 8) & 0xFF);
                    row[i++] = (byte) (a & 0xFF);
                }
            } else {
                while (i < row.length) {
                    final short r = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    final short a = shorts[shortsIdx++];
                    row[i++] = (byte) ((r >> 8) & 0xFF);
                    row[i++] = (byte) (r & 0xFF);
                    row[i++] = (byte) ((g >> 8) & 0xFF);
                    row[i++] = (byte) (g & 0xFF);
                    row[i++] = (byte) ((b >> 8) & 0xFF);
                    row[i++] = (byte) (b & 0xFF);
                    row[i++] = (byte) ((a >> 8) & 0xFF);
                    row[i++] = (byte) (a & 0xFF);
                }
            }
        } else {
            if(bgrOrder) {
                while (i < row.length) {
                    final short b = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short r = shorts[shortsIdx++];
                    row[i++] = (byte) ((r >> 8) & 0xFF);
                    row[i++] = (byte) (r & 0xFF);
                    row[i++] = (byte) ((g >> 8) & 0xFF);
                    row[i++] = (byte) (g & 0xFF);
                    row[i++] = (byte) ((b >> 8) & 0xFF);
                    row[i++] = (byte) (b & 0xFF);
                }
            } else {
                while (i < row.length) {
                    final short r = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    row[i++] = (byte) ((r >> 8) & 0xFF);
                    row[i++] = (byte) (r & 0xFF);
                    row[i++] = (byte) ((g >> 8) & 0xFF);
                    row[i++] = (byte) (g & 0xFF);
                    row[i++] = (byte) ((b >> 8) & 0xFF);
                    row[i++] = (byte) (b & 0xFF);
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
