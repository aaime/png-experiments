package org.geoserver.png.ng;

import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.geoserver.png.ng.filters.PngAverageFilter;
import org.geoserver.png.ng.filters.PngFilter;
import org.geoserver.png.ng.filters.PngPaethFilter;
import org.geoserver.png.ng.filters.PngSubFilter;
import org.geoserver.png.ng.filters.PngSubFilterSingleByte;
import org.geoserver.png.ng.filters.PngUpFilter;
import org.geoserver.png.ng.providers.ScanlineProvider;

/**
 * Low level PNG encoder
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class PngEncoder {

    /**
     * The first eight bytes of a PNG file always contain the following (decimal) values: <br>
     * 137 80 78 71 13 10 26 10
     */
    static final byte[] PNG_SIGNATURE = new byte[] { (byte) 137, (byte) 80, (byte) 78, (byte) 71,
            (byte) 13, (byte) 10, (byte) 26, (byte) 10 };
    
    static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    Deflater deflater;

    ScanlineProvider scanlines;

    FilterType filterType;

    public PngEncoder(ScanlineProvider scanlines, Deflater deflater, FilterType filterType) {
        this.scanlines = scanlines;
        this.deflater = deflater;
        this.filterType = filterType;
    }

    public void encode(OutputStream output) throws IOException {
        // write signature
        output.write(PNG_SIGNATURE);

        // write critical chunks
        writeIHDR(output);
        IndexColorModel palette = scanlines.getPalette();
        if (palette != null) {
            writePalette(palette, output);
        }
        writeIDAT(output);
        writeChunk("IEND", EMPTY_BYTE_ARRAY, output);
    }

    /**
     * Writes the PLTE chunk, and if the palette has transparency, writes the tRNS chunk too
     * @param cm
     * @param os
     * @throws IOException
     */
    private void writePalette(IndexColorModel cm, OutputStream os) throws IOException {
        // grab the RGB contents as a single array (the spec fixes this to 256 entries)
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        byte[] palette = new byte[256 * 3];
        cm.getReds(reds);
        cm.getGreens(greens);
        cm.getBlues(blues);
        int paletteIdx = 0;
        for (int i = 0; i < 256; i++) {
            palette[paletteIdx++] = reds[i];
            palette[paletteIdx++] = greens[i];
            palette[paletteIdx++] = blues[i];
        }
        writeChunk("PLTE", palette, os);
        
        // check for alpha, and if necessary, add the ancillary rRNS chunk
        if(cm.hasAlpha()) {
            byte[] alphas = new byte[cm.getNumComponents()];
            cm.getAlphas(alphas);
            writeChunk("tRNS", alphas, os);
        }
    }

    private void writeChunk(String chunkType, byte[] contents, OutputStream os) throws IOException {
        // write lenght (not part of the CRC)
        os.write(toMSB(contents.length));
        // write the chunk type
        CRC32 crc = new CRC32();
        byte[] ctype = toChunkType(chunkType);
        crc.update(ctype);
        os.write(ctype);
        // write the chunk contents
        crc.update(contents);
        os.write(contents);
        // write the CRC
        int sum = (int) crc.getValue();
        os.write(toMSB(sum));
    }

    /**
     * Writes the image header (IHDR) chunk
     * 
     * @throws IOException
     */
    private void writeIHDR(OutputStream os) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(13);
        bos.write(toMSB(scanlines.getWidth()));
        bos.write(toMSB(scanlines.getHeight()));
        bos.write((byte) scanlines.getBitDepth());
        bos.write((byte) scanlines.getColorType().getType());
        bos.write((byte) 0); // compression method, fixed by spec
        bos.write((byte) 0);
        bos.write((byte) 0); // no interlacing
        writeChunk("IHDR", bos.toByteArray(), os);
    }

    private void writeIDAT(OutputStream os) throws IOException {
        // we need to do the encoding in memory in order to be able to write
        // the IDAT size
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(bos, deflater);

        // prepare the filter object
        PngFilter filter = null;
        int bpp = scanlines.getColorType().getBytesPerPixel();
        if(filterType == FilterType.Sub) {
            if(bpp == 1) {
                filter = new PngSubFilterSingleByte();
            } else {
                filter = new PngSubFilter(bpp, scanlines.getWidth());
            }
        } else if(filterType == FilterType.Up) {
            filter = new PngUpFilter(scanlines.getWidth() * bpp);
        } else if(filterType == FilterType.Average) {
            filter = new PngAverageFilter(bpp, scanlines.getWidth());
        } else if(filterType == FilterType.Paeth) {
            filter = new PngPaethFilter(bpp, scanlines.getWidth());
        }


        // write the scanlines
        byte[] scanline;
        while ((scanline = scanlines.next()) != null) {
            dos.write(filterType.getType());
            if(filter != null) {
                scanline = filter.filter(scanline);
            }
            dos.write(scanline);
        }
        dos.flush();
        dos.close();

        // write out the IDAT chunk now that we know how long it is
        writeChunk("IDAT", bos.toByteArray(), os);
    }

    /**
     * Turns a Java int into a MSB representation (Most Significant Bit, see PNG spec)
     * 
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    private byte[] toMSB(int value) {
        return new byte[] { (byte) ((value >> 24) & 0xff), //
                (byte) ((value >> 16) & 0xff), //
                (byte) ((value >> 8) & 0xff), //
                (byte) (value & 0xff) };

    }

    private byte[] toChunkType(CharSequence chunkName) {
        byte[] result = new byte[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) chunkName.charAt(i);
        }

        return result;
    }
}
