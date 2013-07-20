package org.geoserver.png.ng;

/**
 * The possible color types for a PNG Image
 * 
 * @author Andrea Aime - GeoSolutions
 */
public enum ColorType {

    Grayscale(0, 1), RGB(2, 3), Paletted(3, 1), GrayAlpha(4, 2), RGBA(6, 4);

    byte type;
    int bytesPerPixel;

    ColorType(int colorType, int bytesPerPixel) {
        this.type = (byte) colorType;
        this.bytesPerPixel = bytesPerPixel;
    }

    public byte getType() {
        return type;
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }
}
