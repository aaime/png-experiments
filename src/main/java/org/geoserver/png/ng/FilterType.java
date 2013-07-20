package org.geoserver.png.ng;

/**
 * PNG filtering types
 * 
 * @author Andrea Aime - GeoSolutions
 */
public enum FilterType {

    None(0), Sub(1), Up(2), Average(3), Paeth(4);

    FilterType(int type) {
        this.type = (byte) type;
    }

    byte type;

    public byte getType() {
        return type;
    }

}
