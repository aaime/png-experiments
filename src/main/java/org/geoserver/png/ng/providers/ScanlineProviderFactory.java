package org.geoserver.png.ng.providers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;

public class ScanlineProviderFactory {

    public static ScanlineProvider getProvider(RenderedImage image) {
        if (image instanceof BufferedImage) {
            BufferedImage bi = (BufferedImage) image;
            ColorModel cm = image.getColorModel();
            SampleModel sm = image.getSampleModel();
            Raster raster = bi.getData();
            if (cm instanceof ComponentColorModel && sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                    return new RasterByteABGRProvider(raster, bi.getColorModel().hasAlpha());
                } else if(sm.getNumBands() == 2 && cm.hasAlpha()) {
                    return new RasterByteGrayAlphaProvider(raster);
                } else if (sm.getNumBands() == 1) {
                    if (cm.getPixelSize() == 8) {
                        return new RasterByteGrayProvider(raster);
                    } else if (cm.getPixelSize() == 4) {
                        return new RasterFourBitsGrayProvider(raster);
                    } else if (cm.getPixelSize() == 2) {
                        return new RasterTwoBitsGrayProvider(raster);
                    } else if (cm.getPixelSize() == 1) {
                        return new RasterOneBitGrayProvider(raster);
                    }

                }
            } else if (cm instanceof ComponentColorModel
                    && sm.getDataType() == DataBuffer.TYPE_USHORT) {
                if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                    return new RasterShortABGRProvider(raster, bi.getColorModel().hasAlpha());
                } else if (sm.getNumBands() == 2 && cm.hasAlpha()) {
                    return new RasterShortGrayAlphaProvider(raster);
                } else if (sm.getNumBands() == 1) {
                    return new RasterShortGrayProvider(raster);
                }
            } else if (cm instanceof DirectColorModel && sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (sm.getNumBands() == 3 || sm.getNumBands() == 4) {
                    return new RasterIntABGRProvider(raster, bi.getColorModel().hasAlpha());
                } else if (sm.getNumBands() == 1) {
                    return new RasterByteGrayProvider(bi.getRaster());
                }
            } else if (cm instanceof IndexColorModel) {
                IndexColorModel icm = (IndexColorModel) cm;
                if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    if (icm.getPixelSize() == 8) {
                        return new RasterByteIndexedProvider(raster, icm);
                    } else if (icm.getPixelSize() == 4) {
                        return new RasterFourBitsIndexedProvider(raster, icm);
                    } else if (icm.getPixelSize() == 2) {
                        return new RasterTwoBitsIndexedProvider(raster, icm);
                    } else if (icm.getPixelSize() == 1) {
                        return new RasterOneBitIndexedProvider(raster, icm);
                    }
                } else if (sm.getDataType() == DataBuffer.TYPE_USHORT) {
                    return new RasterShortIndexedProvider(raster,
                            (IndexColorModel) bi.getColorModel());
                }
            }
        }

        throw new IllegalArgumentException("Unsupported image type: " + image);

    }
}
