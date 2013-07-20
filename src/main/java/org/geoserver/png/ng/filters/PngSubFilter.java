package org.geoserver.png.ng.filters;

public class PngSubFilter implements PngFilter {
    
    private final int bytesPerPixel;
    private final byte[] output;

    public PngSubFilter(int bytesPerPixel, int width) {
        this.bytesPerPixel = bytesPerPixel;
        this.output = new byte[width * bytesPerPixel];
    }

    @Override
    public byte[] filter(byte[] row) {
        // the first pixel is unaltered
        for(int i = 0; i < bytesPerPixel; i++) {
            output[i] = row[i];
        }
        for (int i = bytesPerPixel, j = 0; i < row.length; i++, j++) {
            final byte curr = row[i];
            final byte prev = row[j]; 
            final byte filtered = (byte) ((curr - prev) & 0xFF);
            output[i] = filtered;
        }
        
        return output;
    }

}
