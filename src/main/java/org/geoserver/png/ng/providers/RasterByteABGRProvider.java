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
public final class RasterByteABGRProvider extends AbstractScanlineProvider {

    final byte[] bytes;
    final boolean bgrOrder;

    public RasterByteABGRProvider(Raster raster, boolean hasAlpha) {
        super(raster, 8, raster.getWidth() * (hasAlpha ? 4 : 3), hasAlpha ? ColorType.RGBA : ColorType.RGB);
        this.bytes = ((DataBufferByte) raster.getDataBuffer()).getData();
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        this.bgrOrder = sm.getBandOffsets()[0] != 0;
    }

    @Override
    public void next(final byte[] row, final int offset, final int length) {
        int bytesIdx = cursor.next();
        int i = offset;
        final int max = offset + length;
        if (!bgrOrder) {
            System.arraycopy(bytes, bytesIdx, row, offset, length);
        } else {
            if (colorType == ColorType.RGBA) {
                while (i < max) {
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
                while (i < max) {
                    final byte b = bytes[bytesIdx++];
                    final byte g = bytes[bytesIdx++];
                    final byte r = bytes[bytesIdx++];
                    row[i++] = r;
                    row[i++] = g;
                    row[i++] = b;
                }
            }
        }
    }

}
