package org.geoserver.png;

import java.awt.image.BufferedImage;
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

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

@BenchmarkOptions(callgc = false, benchmarkRounds = 20, warmupRounds = 5, concurrency = 8)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "deflater-benchmark")
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.JVM)
public class NGEncodingBenchmark {

    static BufferedImage image;

    private static Map<String, byte[]> pngs = new ConcurrentHashMap<String, byte[]>();

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private static int lastImageType = Integer.MIN_VALUE;

    private String name;

    private FilterType filterType;

    private int compression;

    static final int width = 1024;

    static final int height = 1024;

    static final int strokeWidth = 30;

    static final int lines = 200;

    public NGEncodingBenchmark(String name, int imageType, FilterType filterType, int compression) {
        this.name = name;
        this.filterType = filterType;
        this.compression = compression;
        if (imageType != lastImageType) {
            lastImageType = imageType;
            image = new BufferedImage(width, height, imageType);
            new SampleImagePainter().paintImage(image);
        }
    }

    @Parameters(name = "{0}")
    public static Collection parameters() throws Exception {
        String[] types = new String[] { "4BYTE_ABGR", /* "INT_ARGB", /* "3BYTE_BGR", "INT_BGR", 
                "INT_RGB", "BYTE_INDEXED", "BYTE_GRAY" */};
        FilterType[] filters = new FilterType[] {FilterType.None};
        int[] compressions = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        
        List<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            Field field = BufferedImage.class.getDeclaredField("TYPE_" + type);
            int imageType = (Integer) field.get(null);
            for (int j = 0; j < filters.length; j++) {
                FilterType filter = filters[j];
                for (int k = 0; k < compressions.length; k++) {
                    parameters.add(new Object[] {type.toLowerCase() + "_" + filter.toString() + "_" + compressions[k], imageType, filter, compressions[k]});
                }
                
            }
        }
        
        return parameters;
    }

    @AfterClass
    public static void writeSamples() throws Exception {
        File parent = new File("./target/" + NGEncodingBenchmark.class.getSimpleName());
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
    public void timeNGEncode() throws Exception {
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        Deflater deflater = new Deflater(compression);
        // deflater.setStrategy(Deflater.FILTERED);
        PngEncoder encoder = new PngEncoder(scanlines, deflater, filterType);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.encode(bos);
        byte[] bytes = bos.toByteArray();
        collectPng("ng", bytes);

        // System.out.println(bos.size());
    }


}
