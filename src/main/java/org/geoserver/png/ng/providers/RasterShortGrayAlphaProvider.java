package org.geoserver.png.ng.providers;

import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for a Raster with 16 bit gray + 16 bits alpha
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class RasterShortGrayAlphaProvider implements ScanlineProvider {

    final Raster raster;

    final short[] shorts;

    int currentRow = 0;

    final byte[] scanline;

    final boolean alphaFirst;

    public RasterShortGrayAlphaProvider(Raster raster) {
        this.raster = raster;
        this.shorts = ((DataBufferUShort) raster.getDataBuffer()).getData();
        int rowLength = raster.getWidth() * 4;
        this.scanline = new byte[rowLength];
        int[] bandOffsets = ((PixelInterleavedSampleModel) raster.getSampleModel()).getBandOffsets();
        this.alphaFirst = bandOffsets[0] != 0;
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
        return ColorType.GrayAlpha;
    }

    @Override
    public byte[] next() {
        if (this.currentRow == this.raster.getHeight()) {
            return null;
        }

        final int width = raster.getWidth();
        int shortsIdx = width * currentRow;
        int i = 0;
        if(alphaFirst) {
            while (i < scanline.length) {
                final short alpha = shorts[shortsIdx++];
                final short gray = shorts[shortsIdx++];
                scanline[i++] = (byte) ((gray >> 8) & 0xFF);
                scanline[i++] = (byte) (gray & 0xFF);
                scanline[i++] = (byte) ((alpha >> 8) & 0xFF);
                scanline[i++] = (byte) (alpha & 0xFF);
            } 
        } else {
            while (i < scanline.length) {
                final short gray = shorts[shortsIdx++];
                final short alpha = shorts[shortsIdx++];
                scanline[i++] = (byte) ((gray >> 8) & 0xFF);
                scanline[i++] = (byte) (gray & 0xFF);
                scanline[i++] = (byte) ((alpha >> 8) & 0xFF);
                scanline[i++] = (byte) (alpha & 0xFF);
            }
        }

        currentRow++;
        return scanline;
    }

    @Override
    public IndexColorModel getPalette() {
        return null;
    }

}
