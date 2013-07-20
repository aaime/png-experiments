package org.geoserver.png.ng.filters;

public class PngAverageFilter implements PngFilter {
    
    private final int bytesPerPixel;
    private final byte[] output;
    private final byte[][] buffers;
    int previousIdx;


    public PngAverageFilter(int bytesPerPixel, int width) {
        this.bytesPerPixel = bytesPerPixel;
        this.output = new byte[width * bytesPerPixel];
        this.buffers = new byte[2][width * bytesPerPixel];
    }

    @Override
    public byte[] filter(byte[] row) {
        if(buffers == null) {
            // special case for first row
            buffers[0] = new byte[row.length];
            buffers[1] = new byte[row.length];
            System.arraycopy(row, 0, buffers[0], 0, row.length);
            
            // the first pixel is a mere copy
            for(int i = 0; i < bytesPerPixel; i++) {
                output[i] = row[i];
            }
            // the others also only from the prev in the same row
            for (int i = bytesPerPixel, j = 0; i < row.length; i++, j++) {
                output[i] = (byte) ((row[i] - (row[j] / 2)) & 0xFF);
            }
        } else {
            byte[] prior = buffers[previousIdx];
            byte[] curr = buffers[(previousIdx + 1) % 2];
            System.arraycopy(row, 0, curr, 0, row.length);
            // the first pixel depends only on the previous row
            for(int i = 0; i < bytesPerPixel; i++) {
                output[i] = (byte) ((row[i] - prior[i] / 2) & 0xFF);
            }
            // the others also from the prev value
            for (int i = bytesPerPixel, j = 0; i < curr.length; i++, j++) {
                int sum = curr[j] + prior[i];
                row[i] = (byte) ((curr[i] - sum / 2) & 0xFF);
            }
            previousIdx = (previousIdx + 1) % 2;
        }
        
        return output;
    }

}
