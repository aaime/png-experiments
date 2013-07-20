package org.geoserver.png.ng.filters;

public class PngPaethFilter implements PngFilter {

    private final int bytesPerPixel;

    private final byte[] output;

    private final byte[][] buffers;

    int previousIdx;

    public PngPaethFilter(int bytesPerPixel, int width) {
        this.bytesPerPixel = bytesPerPixel;
        this.output = new byte[width * bytesPerPixel];
        this.buffers = new byte[2][width * bytesPerPixel];
    }

    @Override
    public byte[] filter(byte[] row) {
        if (buffers == null) {
            // special case for first row
            buffers[0] = new byte[row.length];
            buffers[1] = new byte[row.length];
            System.arraycopy(row, 0, buffers[0], 0, row.length);

            for (int i = 0, j = -bytesPerPixel; i < row.length; i++, j++) {
                final int prev = j >= 0 ? row[j] : 0;
                output[i] = (byte) ((row[i] - paeth(prev, 0, 0)) & 0xFF);
            }
        } else {
            byte[] prior = buffers[previousIdx];
            byte[] curr = buffers[(previousIdx + 1) % 2];
            System.arraycopy(row, 0, curr, 0, row.length);
            // the others also from the prev value
            for (int i = 0, j = -bytesPerPixel; i < row.length; i++, j++) {
                final int prev = j >= 0 ? row[j] : 0;
                final int up = prior[i];
                final int upLeft = j >= 0 ? prior[j] : 0;
                output[i] = (byte) ((row[i] - paeth(prev, up, upLeft)) & 0xFF);
            }
            previousIdx = (previousIdx + 1) % 2;
        }

        return output;
    }

    /**
     * The Paeth predictor function, straight from the spec
     * 
     * @param a
     * @param b
     * @param c
     * @return
     */
    private int paeth(int a, int b, int c) {
        // initial estimate
        int p = a + b - c;
        // distances to a, b, c
        final int pa = p >= a ? p - a : a - p;
        final int pb = p >= b ? p - b : b - p;
        final int pc = p >= c ? p - c : c - p;
        // return nearest of a,b,c,
        // breaking ties in order a,b,c.
        if (pa <= pb && pa <= pc) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }

}
