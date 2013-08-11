package org.geoserver.png.ng.providers;

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for Raster objects containing a 16bit BGR or ABGR image
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterShortABGRProvider extends AbstractScanlineProvider {

    final short[] shorts;

    final boolean bgrOrder;

    public RasterShortABGRProvider(Raster raster, boolean hasAlpha) {
        super(raster, 16, (hasAlpha ? 8 : 6) * raster.getWidth(), hasAlpha ? ColorType.RGBA : ColorType.RGB);
        shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
        bgrOrder = ((ComponentSampleModel) raster.getSampleModel()).getBandOffsets()[0] != 0;
    }

    @Override
    public void next(final byte[] scanline, final int offset, final int length) {
        int shortsIdx = cursor.next();
        int i = offset;
        final int max = offset + length;
        if (colorType == ColorType.RGBA) {
            if (bgrOrder) {
                while (i < max) {
                    final short a = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short r = shorts[shortsIdx++];
                    scanline[i++] = (byte) ((r >> 8) & 0xFF);
                    scanline[i++] = (byte) (r & 0xFF);
                    scanline[i++] = (byte) ((g >> 8) & 0xFF);
                    scanline[i++] = (byte) (g & 0xFF);
                    scanline[i++] = (byte) ((b >> 8) & 0xFF);
                    scanline[i++] = (byte) (b & 0xFF);
                    scanline[i++] = (byte) ((a >> 8) & 0xFF);
                    scanline[i++] = (byte) (a & 0xFF);
                }
            } else {
                while (i < max) {
                    final short r = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    final short a = shorts[shortsIdx++];
                    scanline[i++] = (byte) ((r >> 8) & 0xFF);
                    scanline[i++] = (byte) (r & 0xFF);
                    scanline[i++] = (byte) ((g >> 8) & 0xFF);
                    scanline[i++] = (byte) (g & 0xFF);
                    scanline[i++] = (byte) ((b >> 8) & 0xFF);
                    scanline[i++] = (byte) (b & 0xFF);
                    scanline[i++] = (byte) ((a >> 8) & 0xFF);
                    scanline[i++] = (byte) (a & 0xFF);
                }
            }
        } else {
            if(bgrOrder) {
                while (i < max) {
                    final short b = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short r = shorts[shortsIdx++];
                    scanline[i++] = (byte) ((r >> 8) & 0xFF);
                    scanline[i++] = (byte) (r & 0xFF);
                    scanline[i++] = (byte) ((g >> 8) & 0xFF);
                    scanline[i++] = (byte) (g & 0xFF);
                    scanline[i++] = (byte) ((b >> 8) & 0xFF);
                    scanline[i++] = (byte) (b & 0xFF);
                }
            } else {
                while (i < max) {
                    final short r = shorts[shortsIdx++];
                    final short g = shorts[shortsIdx++];
                    final short b = shorts[shortsIdx++];
                    scanline[i++] = (byte) ((r >> 8) & 0xFF);
                    scanline[i++] = (byte) (r & 0xFF);
                    scanline[i++] = (byte) ((g >> 8) & 0xFF);
                    scanline[i++] = (byte) (g & 0xFF);
                    scanline[i++] = (byte) ((b >> 8) & 0xFF);
                    scanline[i++] = (byte) (b & 0xFF);
                }
            }

        }
    }

}
