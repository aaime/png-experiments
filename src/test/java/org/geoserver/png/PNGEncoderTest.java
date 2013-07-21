package org.geoserver.png;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import org.geoserver.png.ng.FilterType;
import org.geoserver.png.ng.PngEncoder;
import org.geoserver.png.ng.providers.RasterByteABGRProvider;
import org.geoserver.png.ng.providers.ScanlineProvider;
import org.junit.Test;

import com.keypoint.PngEncoderB;

public class PNGEncoderTest {

    @Test
    public void testWriteRGB() throws IOException {
        BufferedImage image = getImage(BufferedImage.TYPE_4BYTE_ABGR);
        ScanlineProvider scanlines = new RasterByteABGRProvider(image.getData(), true);
        Deflater deflater = new Deflater(4);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, FilterType.None);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);
        byte[] bytes = bos.toByteArray();
        File file = new File("./target/rgb-ng.png");
        writeBytes(bytes, file);
    }
    
    @Test
    public void testWriteRGBKeypoint() throws IOException {
        BufferedImage image = getImage(BufferedImage.TYPE_4BYTE_ABGR);
        PngEncoderB encoder = new PngEncoderB(image);
        encoder.setEncodeAlpha(true);
        encoder.setCompressionLevel(4);
        byte[] bytes = encoder.pngEncode(true);
        File file = new File("./target/rgb-keypoint.png");
        writeBytes(bytes, file);
    }

    private void writeBytes(byte[] bytes, File file) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private BufferedImage getImage(int imageType) {
        BufferedImage image = new BufferedImage(640, 480, imageType);
        SampleImagePainter sip = new SampleImagePainter();
        sip.paintImage(image);
        return image;
    }

}
