package org.geoserver.png;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.geoserver.png.ng.FilterType;
import org.geoserver.png.ng.PngEncoder;
import org.geoserver.png.ng.providers.ScanlineProvider;
import org.geoserver.png.ng.providers.ScanlineProviderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PngSuiteImagesTest {

    private File sourceFile;

    public PngSuiteImagesTest(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Parameters(name = "{0}")
    public static Collection parameters() {
        List result = new ArrayList();
        File source = new File("./src/test/resources/suite");
        for (File file : source.listFiles()) {
            if (file.getName().endsWith(".png")) {
                result.add(new Object[] { file });
            }
        }

        return result;
    }

    @Test
    public void testRoundTripFilterNone() throws Exception {
        BufferedImage input = ImageIO.read(sourceFile);

        Deflater deflater = new Deflater();
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(input);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, FilterType.None);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        BufferedImage image = ImageIO.read(bis);

        assertEquals(input.getWidth(), image.getWidth());
        assertEquals(input.getHeight(), image.getHeight());
        assertEquals(input.getSampleModel(), image.getSampleModel());
        assertEquals(input.getColorModel(), image.getColorModel());

        for (int x = 0; x < input.getWidth(); x++) {
            for (int y = 0; y < input.getHeight(); y++) {
                assertEquals(input.getRGB(x, y), image.getRGB(x, y));
            }
        }
    }
}
