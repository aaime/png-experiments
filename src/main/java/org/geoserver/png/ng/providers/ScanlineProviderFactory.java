package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;

public class ScanlineProviderFactory {

    public static ScanlineProvider getProvider(RenderedImage image) {
        if (image instanceof BufferedImage) {
            BufferedImage bi = (BufferedImage) image;
            int type = bi.getType();
            if (type == BufferedImage.TYPE_3BYTE_BGR || type == BufferedImage.TYPE_4BYTE_ABGR) {
                return new RasterByteABGRProvider(bi.getData(), bi.getColorModel().hasAlpha());
            } else if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_BGR
                    || type == BufferedImage.TYPE_INT_RGB) {
                return new RasterIntABGRProvider(bi.getData(), bi.getColorModel().hasAlpha());
            } else if (type == BufferedImage.TYPE_BYTE_INDEXED) {
                return new RasterByteIndexedProvider(bi.getData(),
                        (IndexColorModel) bi.getColorModel());
            } else if (type == BufferedImage.TYPE_BYTE_GRAY) {
                return new RasterByteGrayProvider(bi.getData());
            } else if (type == BufferedImage.TYPE_USHORT_GRAY) {
                return new RasterShortGrayProvider(bi.getData());
            }

            if (type == BufferedImage.TYPE_CUSTOM) {
                ColorModel cm = image.getColorModel();
                SampleModel sm = image.getSampleModel();
                if (cm instanceof ComponentColorModel && sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                        return new RasterByteABGRProvider(bi.getData(), bi.getColorModel()
                                .hasAlpha());
                    } else if (sm.getNumBands() == 1 && cm instanceof IndexColorModel) {
                        return new RasterByteIndexedProvider(bi.getData(),
                                (IndexColorModel) bi.getColorModel());
                    }
                } else if (cm instanceof ComponentColorModel
                        && sm.getDataType() == DataBuffer.TYPE_USHORT) {
                    if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                        return new RasterShortABGRProvider(bi.getData(), bi.getColorModel()
                                .hasAlpha());
                    } else if (sm.getNumBands() == 1) {
                        return new RasterShortGrayProvider(bi.getData());
                    }
                } else if (cm instanceof DirectColorModel
                        && sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                        return new RasterIntABGRProvider(bi.getData(), bi.getColorModel()
                                .hasAlpha());
                    } else if (sm.getNumBands() == 1) {
                        return new RasterByteGrayProvider(bi.getRaster());
                    }
                } else if (cm instanceof IndexColorModel) {
                    IndexColorModel icm = (IndexColorModel) cm;
                    if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                        return new RasterByteIndexedProvider(bi.getData(),
                                (IndexColorModel) bi.getColorModel());
                    } else if(sm.getDataType() == DataBuffer.TYPE_USHORT) {
                        return new RasterShortIndexedProvider(bi.getData(),
                                (IndexColorModel) bi.getColorModel());
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unsupported image type: " + image);

    }
}
