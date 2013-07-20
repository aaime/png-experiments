package org.geoserver.png.ng.filters;

/**
 * A version of the Sub filter optimized for single byte data
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class PngSubFilterSingleByte implements PngFilter {
    
    public PngSubFilterSingleByte() {
    }

    @Override
    public byte[] filter(byte[] row) {
        byte prev = row[0];
        for (int i = 1; i < row.length; i++) {
            final byte curr = row[i];
            final byte filtered = (byte) ((curr - prev) & 0xFF);
            row[i] = filtered;
            prev = curr;
        }
        
        return row;
    }

}
