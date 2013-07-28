package org.geoserver.png;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;

import javax.imageio.IIOImage;
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

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi;

@BenchmarkOptions(callgc = false, benchmarkRounds = 20, warmupRounds = 5, concurrency = 8)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "bufferedimage-benchmark")
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.JVM)
public class BufferedImageEncodingBenchmark {

    static BufferedImage image;

    private static Map<String, byte[]> pngs = new ConcurrentHashMap<String, byte[]>();

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static int lastImageType = Integer.MIN_VALUE;

    private String name;

    private FilterType filterType;

    static final int width = 1024;

    static final int height = 1024;

    static final int strokeWidth = 30;

    static final int lines = 200;

    public BufferedImageEncodingBenchmark(String name, int imageType, FilterType filterType) {
        this.name = name;
        this.filterType = filterType;
        if (imageType != lastImageType) {
            lastImageType = imageType;
            image = new BufferedImage(width, height, imageType);
            new SampleImagePainter().paintImage(image);
        }
    }

    @Parameters(name = "{0}")
    public static Collection parameters() throws Exception {
        String[] types = new String[] { "4BYTE_ABGR", "INT_ARGB", "3BYTE_BGR", "INT_BGR", 
                "INT_RGB", "BYTE_INDEXED", "BYTE_GRAY" };
        FilterType[] filters = new FilterType[] {FilterType.None, /* FilterType.Sub, /* FilterType.Up, */ /* FilterType.Average, FilterType.Paeth  */}; 
        
        List<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            Field field = BufferedImage.class.getDeclaredField("TYPE_" + type);
            int imageType = (Integer) field.get(null);
            for (int j = 0; j < filters.length; j++) {
                FilterType filter = filters[j];
                parameters.add(new Object[] {type.toLowerCase() + "_" + filter.toString(), imageType, filter});
            }
        }
        
        return parameters;
    }

    @AfterClass
    public static void writeSamples() throws Exception {
        File parent = new File("./target/bufferedImageBenchmark");
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
    public void timeJavaEncode() throws Exception {
        ImageWriter writer = new PNGImageWriterSpi().createWriterInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream mos = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(mos);
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        // iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // iwp.setCompressionQuality(0.75f);
        writer.write(null, new IIOImage(image, null, null), iwp);
        mos.flush();
        collectPng("java", bos.toByteArray());

        // System.out.println(bos.size());
    }

    @Test
    public void timeNGEncode() throws Exception {
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        Deflater deflater = new Deflater(4);
        deflater.setStrategy(Deflater.FILTERED);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, filterType);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);
        byte[] bytes = bos.toByteArray();
        collectPng("ng", bytes);

        // System.out.println(bos.size());
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
        iwp.setCompressionQuality(0.75f);
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

    private void collectPng(String encoder, byte[] png) {
        if (png == null) {
            return;
        }
        String key = name + "_" + encoder;
        if (pngs.get(key) == null) {
            pngs.put(key, png);
        }
    }

//    @Test
//    public void timeKeypointEncode() throws Exception {
//        PngEncoderB encoder = new PngEncoderB(image);
//        encoder.setEncodeAlpha(true);
//        encoder.setCompressionLevel(4);
//        int type = filterType.getType();
//        encoder.setFilter(type);
//        byte[] png = encoder.pngEncode(true);
//        collectPng("keypoint", png);
//    }
//
    @Test
    public void timePNGJEncode() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ColorModel colorModel = image.getColorModel();
        boolean indexed = colorModel instanceof IndexColorModel;
        boolean hasAlpha = colorModel.hasAlpha();
        boolean grayscale = !indexed && colorModel.getNumColorComponents() == 1;
        ImageInfo ii = new ImageInfo(image.getWidth(), image.getHeight(), 8, hasAlpha, grayscale,
                indexed);
        PngWriter pw = new PngWriter(bos, ii);
        pw.setCompLevel(4);
        // pw.setDeflaterStrategy(Deflater.NO_COMPRESSION);
        pw.setFilterType(ar.com.hjg.pngj.FilterType.getByVal(filterType.getType()));

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
        byte[] rebased = new byte[image.getWidth() * colorModel.getNumColorComponents()];
        for (int row = 0; row < image.getHeight(); row++) {
            byte[] bytes = scanlines.next();
            // final byte[] line = scanline.getBytes();
            // if(scanline.getOffset() > 0) {
            // System.arraycopy(line, scanline.getOffset(), rebased, 0, scanline.getLength());
            // pw.writeRowByte(rebased, row);
            // } else {
            pw.writeRow(new ImageLineByte(ii, bytes));
            // }

        }
        pw.end();
        byte[] png = bos.toByteArray();
        collectPng("pngj", png);
    }

}
