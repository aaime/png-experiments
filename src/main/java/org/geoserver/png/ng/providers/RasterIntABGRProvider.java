package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for rasters with int packed RGB or RGBA pixels
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class RasterIntABGRProvider extends AbstractScanlineProvider {

    final int[] pixels;

    final boolean bgrOrder;

    public RasterIntABGRProvider(Raster raster, boolean hasAlpha) {
        super(raster, 32, raster.getWidth() * (hasAlpha ? 4 : 3), hasAlpha ? ColorType.RGBA : ColorType.RGB);
        this.pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
        if (hasAlpha) {
            bgrOrder = false;
        } else {
            bgrOrder = ((SinglePixelPackedSampleModel) raster.getSampleModel()).getBitOffsets()[0] != 0;
        }
    }

    @Override
    public void next(final byte[] row, final int offset, final int length) {
        int pxIdx = cursor.next();
        int i = offset;
        final int max = offset + length;
        if (colorType == ColorType.RGBA) {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 24) & 0xff);
            }
        } else if (bgrOrder) {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color >> 16) & 0xff);
            }
        } else {
            while (i < max) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
            }
        }
    }

}
