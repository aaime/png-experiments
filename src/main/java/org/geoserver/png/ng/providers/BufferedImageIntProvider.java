package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.SinglePixelPackedSampleModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for BufferedImage with {@link BufferedImage#TYPE_INT_ARGB} or
 * {@link BufferedImage#TYPE_INT_RGB} or {@link BufferedImage#TYPE_INT_BGR} types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class BufferedImageIntProvider implements ScanlineProvider {

    final BufferedImage image;

    final int[] pixels;

    final byte[] row;

    final int rowLength;

    final boolean hasAlpha;

    final ColorType colorType;

    final boolean bgrOrder;

    int currentRow = 0;

    public BufferedImageIntProvider(BufferedImage image) {
        this.image = image;
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        hasAlpha = image.getColorModel().hasAlpha();
        final int bpp;
        if (hasAlpha) {
            bpp = 4;
            colorType = ColorType.RGBA;
            bgrOrder = false;
        } else {
            bpp = 3;
            colorType = ColorType.RGB;
            bgrOrder = ((SinglePixelPackedSampleModel) image.getSampleModel()).getBitOffsets()[0] != 0;
        }
        rowLength = image.getWidth() * bpp;
        row = new byte[rowLength];
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
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
        if (this.currentRow == this.image.getHeight()) {
            return null;
        }

        int pxIdx = image.getWidth() * currentRow;
        int i = 0;
        if (hasAlpha) {
            while (i < rowLength) {
                final int color = pixels[pxIdx++];

                row[i++] = (byte) ((color >> 16) & 0xff);
                row[i++] = (byte) ((color >> 8) & 0xff);
                row[i++] = (byte) ((color) & 0xff);
                row[i++] = (byte) ((color >> 24) & 0xff);
            }
        } else if(bgrOrder){
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
        currentRow++;
        return row;
    }
    
    @Override
    public IndexColorModel getPalette() {
        // no palette
        return null;
    }

}
