package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

import org.geoserver.png.ng.ColorType;

/**
 * A scanline provider optimized for BufferedImage with {@link BufferedImage#TYPE_3BYTE_BGR} or
 * {@link BufferedImage#TYPE_4BYTE_ABGR} types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public final class BufferedImageABGRByteProvider implements ScanlineProvider {

    final BufferedImage image;

    final byte[] bytes;

    final byte[] row;

    final int rowLength;

    final boolean hasAlpha;

    final ColorType colorType;

    int currentRow = 0;

    final boolean bgrOrder;

    public BufferedImageABGRByteProvider(BufferedImage image) {
        this.image = image;
        bytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        hasAlpha = image.getColorModel().hasAlpha();
        if (hasAlpha) {
            rowLength = image.getWidth() * 4;
            colorType = ColorType.RGBA;
        } else {
            rowLength = image.getWidth() * 3;
            colorType = ColorType.RGB;
        }
        bgrOrder = ((ComponentSampleModel) image.getSampleModel()).getBandOffsets()[0] != 0;
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

        int bytesIdx = rowLength * currentRow;
        int i = 0;
        if (hasAlpha) {
            if (bgrOrder) {
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
                    row[i++] = bytes[bytesIdx++];
                    row[i++] = bytes[bytesIdx++];
                    row[i++] = bytes[bytesIdx++];
                    row[i++] = bytes[bytesIdx++];
                }
            }
        } else {
            if(bgrOrder) {
                while (i < rowLength) {
                    final byte b = bytes[bytesIdx++];
                    final byte g = bytes[bytesIdx++];
                    final byte r = bytes[bytesIdx++];
                    row[i++] = r;
                    row[i++] = g;
                    row[i++] = b;
                }
            } else {
                while (i < rowLength) {
                    row[i++] = bytes[bytesIdx++];
                    row[i++] = bytes[bytesIdx++];
                    row[i++] = bytes[bytesIdx++];
                }
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
