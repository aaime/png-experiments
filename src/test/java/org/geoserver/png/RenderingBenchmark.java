package org.geoserver.png;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

// @BenchmarkOptions(callgc = true, benchmarkRounds = 50, warmupRounds = 20)
@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "rendering-benchmark")
@RunWith(Parameterized.class)
public class RenderingBenchmark {

    BufferedImage image;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private int imageType;

    public RenderingBenchmark(String name, int imageType) {
        this.imageType = imageType;
    }

    @Parameters(name="{0}")
    public static Collection parameters() {
        return Arrays.asList(new Object[] {"4byte_abgr", BufferedImage.TYPE_4BYTE_ABGR },
                new Object[] { "int_argb", BufferedImage.TYPE_INT_ARGB },
                new Object[] { "3byte_bgr", BufferedImage.TYPE_3BYTE_BGR },
                new Object[] { "int_bgr", BufferedImage.TYPE_INT_BGR },
                new Object[] { "int_rgb", BufferedImage.TYPE_INT_RGB });
    }

    @Before
    public void setUp() throws Exception {
        image = new BufferedImage(1024, 1024, imageType);
    }

    @Test
    public void timeRendering() throws Exception {
        SampleImagePainter painter = new SampleImagePainter();
        painter.setLabels(2000);
        painter.setLines(10000);
        painter.setStrokeWidth(100);
        painter.paintImage(image);
    }



}
