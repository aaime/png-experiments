package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for BufferedImage with {@link BufferedImage#TYPE_BYTE_INDEXED} or
 * {@link BufferedImage#TYPE_4BYTE_ABGR} types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class BufferedImageGrayByteProvider implements ScanlineProvider {

    final BufferedImage image;

    final byte[] bytes;

    int currentRow = 0;

    final byte[] scanline;

    public BufferedImageGrayByteProvider(BufferedImage image) {
        this.image = image;
        this.bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        this.scanline = new byte[image.getWidth()];
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
        return ColorType.Grayscale;
    }

    @Override
    public byte[] next() {
        if (this.currentRow == this.image.getHeight()) {
            return null;
        }

        final int width = image.getWidth();
        System.arraycopy(bytes, currentRow * width, scanline, 0, image.getWidth());

        currentRow++;
        return scanline;
    }

    @Override
    public IndexColorModel getPalette() {
        return null;
    }

}
