package org.geoserver.png;

import static org.junit.Assert.assertEquals;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;

import org.geoserver.png.ng.FilterType;
import org.geoserver.png.ng.PngEncoder;
import org.geoserver.png.ng.providers.ScanlineProvider;
import org.geoserver.png.ng.providers.ScanlineProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

@RunWith(Parameterized.class)
public class PngSuiteImagesTest {

    private File sourceFile;

    public PngSuiteImagesTest(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @BeforeClass
    public static void disableNativeCodecs() {
        System.setProperty("com.sun.media.imageio.disableCodecLib", "true");
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

        roundTripNG(input, input);
    }

    @Test
    public void testRoundTripTiledImage() throws Exception {
        BufferedImage input = ImageIO.read(sourceFile);

        // prepare a tiled image layout
        ImageLayout il = new ImageLayout(input);
        il.setTileWidth(8);
        il.setTileHeight(8);

        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
        RenderedOp tiled = FormatDescriptor.create(input, input.getSampleModel().getDataType(),
                hints);
        assertEquals(8, tiled.getTileWidth());
        assertEquals(8, tiled.getTileHeight());

        roundTripNG(input, tiled);
    }

    private void roundTripNG(BufferedImage original, RenderedImage source) throws IOException {
        Deflater deflater = new Deflater();
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(source);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, FilterType.None);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);

        byte[] bytes = bos.toByteArray();
        writeToFile(new File("./target/roundTripNone", sourceFile.getName()), bytes);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(bis);

        assertEquals(original.getWidth(), image.getWidth());
        assertEquals(original.getHeight(), image.getHeight());
        assertEquals(original.getSampleModel(), image.getSampleModel());
        assertEquals(original.getColorModel(), image.getColorModel());

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int rgbOriginal = original.getRGB(x, y);
                int rgbActual = image.getRGB(x, y);
                assertEquals("Comparison failed at " + x + "," + y, rgbOriginal, rgbActual);
            }
        }
    }

    private void roundTripPNGJ(BufferedImage original, RenderedImage source) throws IOException {
        ColorModel colorModel = source.getColorModel();
        boolean indexed = colorModel instanceof IndexColorModel;
        boolean hasAlpha = colorModel.hasAlpha();
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(source);
        int numColorComponents = colorModel.getNumColorComponents();
        boolean grayscale = !indexed && numColorComponents < 3;
        byte bitDepth = scanlines.getBitDepth();
        ImageInfo ii = new ImageInfo(source.getWidth(), source.getHeight(), bitDepth, hasAlpha, grayscale,
                indexed);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PngWriter pw = new PngWriter(bos, ii);
        pw.setCompLevel(4);
        pw.setFilterType(ar.com.hjg.pngj.FilterType.FILTER_NONE);

        if (indexed) {
            IndexColorModel icm = (IndexColorModel) colorModel;
            PngChunkPLTE palette = pw.getMetadata().createPLTEChunk();
            int ncolors = icm.getNumComponents();
            palette.setNentries(ncolors);
            for (int i = 0; i < ncolors; i++) {
                palette.setEntry(i, icm.getRed(i), icm.getGreen(i), icm.getBlue(i));
            }
            if (icm.hasAlpha()) {
                PngChunkTRNS transparent = new PngChunkTRNS(ii);
                int[] alpha = new int[ncolors];
                for (int i = 0; i < ncolors; i++) {
                    alpha[i] = icm.getAlpha(i);
                }
                transparent.setPalletteAlpha(alpha);
                pw.getChunksList().queue(transparent);

            }
        }

        for (int row = 0; row < source.getHeight(); row++) {
            byte[] bytes = scanlines.next();
            pw.writeRow(new ImageLineByte(ii, bytes));
        }
        pw.end();

        byte[] bytes = bos.toByteArray();
        writeToFile(new File("./target/roundTripNone", sourceFile.getName()), bytes);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(bis);

        assertEquals(original.getWidth(), image.getWidth());
        assertEquals(original.getHeight(), image.getHeight());
        assertEquals(original.getSampleModel(), image.getSampleModel());
        assertEquals(original.getColorModel(), image.getColorModel());

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int rgbOriginal = original.getRGB(x, y);
                int rgbActual = image.getRGB(x, y);
                assertEquals("Comparison failed at " + x + "," + y, rgbOriginal, rgbActual);
            }
        }
    }

    private void writeToFile(File file, byte[] bytes) throws IOException {
        File parent = file.getParentFile();
        parent.mkdirs();
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

}
