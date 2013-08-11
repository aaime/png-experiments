package org.geoserver.png;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.geoserver.png.ng.FilterType;
import org.geoserver.png.ng.PngEncoder;
import org.geoserver.png.ng.providers.ScanlineProvider;
import org.geoserver.png.ng.providers.ScanlineProviderFactory;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi;

@BenchmarkOptions(callgc = false, benchmarkRounds = 20, warmupRounds = 5, concurrency = 8)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "samples-benchmark")
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.JVM)
public class SamplesEncodingBenchmark {

    static BufferedImage image;

    private static Map<String, byte[]> pngs = new ConcurrentHashMap<String, byte[]>();

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static String lastFileName = null;

    private String name;

    private int compression;

    private boolean raster;

    public SamplesEncodingBenchmark(String name, String fileName, int compression) throws IOException {
        this.name = name;
        this.compression = compression;
        this.raster = fileName.endsWith("jpg");
        if (fileName != lastFileName) {
            lastFileName = fileName;
            image = null;
            image = ImageIO.read(new File("./src/test/resources/samples/" + fileName));
        }
    }

    @Parameters(name = "{0}")
    public static Collection parameters() throws Exception {
        String[] files = new String[] { "osmbox1.png", "osmbox2.png", "osmbox3.png", "osmbox4.png", "italy.jpg", "LandiscorSample2.jpg" };
        int[] compressions = new int[] {5 /*, 7, 9*/ };
        
        List<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
                for (int k = 0; k < compressions.length; k++) {
                    int compression = compressions[k];
                    parameters.add(new Object[] {file + "_" + compression, file, compression});
                }
        }
        
        return parameters;
    }

    @AfterClass
    public static void writeSamples() throws Exception {
        File parent = new File("./target/" + SamplesEncodingBenchmark.class.getSimpleName());
        parent.mkdirs();
        List<String> keys = new ArrayList(pngs.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            byte[] bytes = pngs.get(key);
            System.out.println(key + ":\t" + (bytes.length / 1024));
            File file = new File(parent, key + ".png");
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

    @Test
    public void timeCLibEncode() throws Exception {
        ImageWriter writer = new CLibPNGImageWriterSpi().createWriterInstance();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        // Define compression mode
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // best compression
        // iwp.setCompressionType("FILTERED");
        // we can control quality here
        iwp.setCompressionQuality((1 - compression / 9f));
        // destination image type
        iwp.setDestinationType(new ImageTypeSpecifier(image.getColorModel(), image.getSampleModel()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream mos = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(mos);
        writer.write(null, new IIOImage(image, null, null), iwp);
        mos.flush();
        collectPng("clib", bos.toByteArray());
        // System.out.println(bos.size());
    }
    
    @Test
    public void timeNGEncode() throws Exception {
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        Deflater deflater = new Deflater(compression);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, raster ? FilterType.Sub : FilterType.None);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);
        byte[] bytes = bos.toByteArray();
        collectPng("ng", bytes);

        // System.out.println(bos.size());
    }

    private void collectPng(String encoder, byte[] png) {
        if (png == null) {
            return;
        }
        String key = name + "_" + encoder;
        if (pngs.get(key) == null) {
            pngs.put(key, png);
        }
    }
    
    @Test
    public void timePNGJEncodeAuto() throws Exception {
        ar.com.hjg.pngj.FilterType filterType = raster ? ar.com.hjg.pngj.FilterType.FILTER_SUB : ar.com.hjg.pngj.FilterType.FILTER_NONE;
        timePNGJEncode(filterType, "pngj_auto");
    }
    
//    @Test
//    public void timePNGJEncodeDefault() throws Exception {
//        timePNGJEncode(ar.com.hjg.pngj.FilterType.FILTER_DEFAULT, "pngj_default");
//    }
//    
//    @Test
//    public void timePNGJEncodeAggressive() throws Exception {
//        timePNGJEncode(ar.com.hjg.pngj.FilterType.FILTER_AGGRESSIVE, "pngj_aggressive");
//    }

    public void timePNGJEncode(ar.com.hjg.pngj.FilterType filterType, String name) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ColorModel colorModel = image.getColorModel();
        boolean indexed = colorModel instanceof IndexColorModel;
        boolean hasAlpha = colorModel.hasAlpha();
        boolean grayscale = !indexed && colorModel.getNumColorComponents() == 1;
        ImageInfo ii = new ImageInfo(image.getWidth(), image.getHeight(), 8, hasAlpha, grayscale,
                indexed);
        PngWriter pw = new PngWriter(bos, ii);
        pw.setCompLevel(compression);
        pw.setFilterType(filterType);

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

        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        for (int row = 0; row < image.getHeight(); row++) {
            pw.writeRow(scanlines);
        }
        pw.end();
        byte[] png = bos.toByteArray();
        collectPng(name, png);
    }

}
