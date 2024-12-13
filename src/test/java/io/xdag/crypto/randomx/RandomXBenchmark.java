package io.xdag.crypto.randomx;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive benchmark for RandomX operations.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 1, time = 10)
@Threads(8)
public class RandomXBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(RandomXBenchmark.class);

    private static final byte[] BLOCK_TEMPLATE = {
            (byte)0x07, (byte)0x07, (byte)0xf7, (byte)0xa4, (byte)0xf0, (byte)0xd6, (byte)0x05, (byte)0xb3,
            (byte)0x03, (byte)0x26, (byte)0x08, (byte)0x16, (byte)0xba, (byte)0x3f, (byte)0x10, (byte)0x90,
            (byte)0x2e, (byte)0x1a, (byte)0x14, (byte)0x5a, (byte)0xc5, (byte)0xfa, (byte)0xd3, (byte)0xaa,
            (byte)0x3a, (byte)0xf6, (byte)0xea, (byte)0x44, (byte)0xc1, (byte)0x18, (byte)0x69, (byte)0xdc,
            (byte)0x4f, (byte)0x85, (byte)0x3f, (byte)0x00, (byte)0x2b, (byte)0x2e, (byte)0xea, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00
    };

    private RandomXTemplate lightTemplate;
    private RandomXTemplate miningTemplate;
    private Set<RandomXFlag> flags;
    private RandomXCache cache;
    private RandomXDataset dataset;

    @Setup(Level.Trial)
    public void setup() {
        logger.info("Setting up benchmark with 8 threads");
        flags = RandomXUtils.getFlagsSet();
        initializeTemplates();
        logger.info("Benchmark setup completed with flags: {}", flags);
    }

    private void initializeTemplates() {
        cache = new RandomXCache(flags);
        cache.init(BLOCK_TEMPLATE);

        // Initialize light mode template
        lightTemplate = RandomXTemplate.builder()
                .miningMode(false)
                .flags(flags)
                .cache(cache)
                .build();
        lightTemplate.init();

        // Initialize mining mode template with dataset
        dataset = new RandomXDataset(flags);
        dataset.init(cache);
        miningTemplate = RandomXTemplate.builder()
                .miningMode(true)
                .flags(flags)
                .cache(cache)
                .dataset(dataset)
                .build();
        miningTemplate.init();
    }

    @TearDown
    public void tearDown() {
        logger.info("Cleaning up benchmark resources");
        if (lightTemplate != null) lightTemplate.close();
        if (miningTemplate != null) miningTemplate.close();
        if (dataset != null) dataset.close();
        if (cache != null) cache.close();
    }

    @Benchmark
    @Group("miningNoBatch")
    public byte[] miningModeNoBatchHash() {
        return miningTemplate.calculateHash(BLOCK_TEMPLATE);
    }

    @Benchmark
    @Group("miningBatch")
    public byte[] miningModeBatchHash() {
        miningTemplate.calculateHashFirst(BLOCK_TEMPLATE);
        return miningTemplate.calculateHashNext(BLOCK_TEMPLATE);
    }

    @Benchmark
    @Group("lightNoBatch")
    public byte[] lightModeNoBatchHash() {
        return lightTemplate.calculateHash(BLOCK_TEMPLATE);
    }

    @Benchmark
    @Group("lightBatch")
    public byte[] lightModeBatchHash() {
        lightTemplate.calculateHashFirst(BLOCK_TEMPLATE);
        return lightTemplate.calculateHashNext(BLOCK_TEMPLATE);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(RandomXBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.TEXT)
                .jvmArgs("-Xms2G", "-Xmx2G")
                .build();

        new Runner(opt).run();
    }
}