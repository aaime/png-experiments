package org.geoserver.png.ng.filters;

public class PngUpFilter implements PngFilter {
    
    private final byte[][] buffers;
    int previousIdx;

    public PngUpFilter(int rowLength) {
        this.buffers = new byte[2][rowLength];
    }

    @Override
    public byte[] filter(byte[] row) {
        if(buffers == null) {
            buffers[0] = new byte[row.length];
            buffers[1] = new byte[row.length];
            System.arraycopy(row, 0, buffers[0], 0, row.length);
        } else {
            byte[] prev = buffers[previousIdx];
            byte[] curr = buffers[(previousIdx + 1) % 2];
            System.arraycopy(row, 0, curr, 0, row.length);
            for (int i = 0; i < curr.length; i++) {
                row[i] = (byte) ((curr[i] - prev[i]) & 0xFF);
            }
            previousIdx = (previousIdx + 1) % 2;
        }
        
        return row;
    }

}
