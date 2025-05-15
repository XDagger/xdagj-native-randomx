/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
 * This benchmark tests both mining and light modes with batch and non-batch operations.
 * Uses JMH framework for accurate performance measurements.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 1, time = 10)
@Threads(8)
public class RandomXBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(RandomXBenchmark.class);

    @Param({"NO_JIT", "JIT"}) // Parameter for controlling JIT flag
    public String compilationMode;

    // Sample block template for hash calculation
    private static final byte[] BLOCK_TEMPLATE = {
            (byte)0x07, (byte)0x07, (byte)0xf7, (byte)0xa4, (byte)0xf0, (byte)0xd6, (byte)0x05, (byte)0xb3,
            (byte)0x03, (byte)0x26, (byte)0x08, (byte)0x16, (byte)0xba, (byte)0x3f, (byte)0x10, (byte)0x90,
            (byte)0x2e, (byte)0x1a, (byte)0x14, (byte)0x5a, (byte)0xc5, (byte)0xfa, (byte)0xd3, (byte)0xaa,
            (byte)0x3a, (byte)0xf6, (byte)0xea, (byte)0x44, (byte)0xc1, (byte)0x18, (byte)0x69, (byte)0xdc,
            (byte)0x4f, (byte)0x85, (byte)0x3f, (byte)0x00, (byte)0x2b, (byte)0x2e, (byte)0xea, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00
    };

    // Shared resources across all benchmark threads
    private Set<RandomXFlag> flags;
    private RandomXCache cache;
    private RandomXDataset dataset;

    /**
     * Thread-local state containing RandomX templates for both mining and light modes
     */
    @State(Scope.Thread)
    public static class ThreadState {
        RandomXTemplate lightTemplate;
        RandomXTemplate miningTemplate;

        /**
         * Initialize thread-local templates using shared benchmark resources
         */
        @Setup(Level.Trial)
        public void setup(RandomXBenchmark benchmark) {
            lightTemplate = RandomXTemplate.builder()
                    .miningMode(false)
                    .flags(benchmark.flags)
                    .cache(benchmark.cache)
                    .build();
            lightTemplate.init();

            miningTemplate = RandomXTemplate.builder()
                    .miningMode(true)
                    .flags(benchmark.flags)
                    .cache(benchmark.cache)
                    .dataset(benchmark.dataset)
                    .build();
            miningTemplate.init();
        }

        /**
         * Clean up thread-local resources
         */
        @TearDown(Level.Trial)
        public void tearDown() {
            if (lightTemplate != null) lightTemplate.close();
            if (miningTemplate != null) miningTemplate.close();
        }
    }

    /**
     * Initialize shared resources used across all benchmark threads
     */
    @Setup(Level.Trial)
    public void setup() {
        logger.info("Setting up shared resources for RandomXBenchmark");
        
        Set<RandomXFlag> baseFlags = RandomXUtils.getRecommendedFlags(); // Get base recommended flags

        logger.info("Base recommended flags: {}", baseFlags);

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        boolean isMac = osName.contains("mac");

        if ("JIT".equals(compilationMode)) {
            logger.info("Compilation Mode: JIT selected. Enabling JIT flag.");
            baseFlags.add(RandomXFlag.JIT);

            if (isMac) {
                logger.info("On macOS, ensuring SECURE flag is also enabled when JIT is selected, as per user observation.");
                baseFlags.add(RandomXFlag.SECURE); // Ensure SECURE is present for JIT on macOS
            } else {
                // For non-macOS systems, if SECURE is present and potentially conflicts with JIT,
                // it might be safer to remove SECURE when JIT is prioritized.
                if (baseFlags.contains(RandomXFlag.SECURE)) {
                    logger.warn("JIT and SECURE flags may be incompatible on non-macOS. Removing SECURE flag as JIT is prioritized for this mode.");
                    baseFlags.remove(RandomXFlag.SECURE);
                }
            }
        } else { // NO_JIT or any other value
            logger.info("Compilation Mode: NO_JIT selected. Ensuring JIT flag is disabled.");
            baseFlags.remove(RandomXFlag.JIT);
            // If not using JIT, SECURE flag (if recommended by getRecommendedFlags()) should typically be kept.
            // No specific action needed here for SECURE if getRecommendedFlags() already handles it well for NO_JIT.
        }

        this.flags = EnumSet.copyOf(baseFlags); // Use a defensive copy
        logger.info("Benchmark (compilationMode={}) will use final flags for VMs and Cache: {}", compilationMode, this.flags);

        cache = new RandomXCache(this.flags);
        cache.init(BLOCK_TEMPLATE); 
        logger.info("Shared RandomXCache initialized with BLOCK_TEMPLATE using flags: {}", this.flags);

        Set<RandomXFlag> datasetAllocFlags = EnumSet.noneOf(RandomXFlag.class);
        datasetAllocFlags.add(RandomXFlag.FULL_MEM); 

        if (this.flags.contains(RandomXFlag.LARGE_PAGES)) {
            datasetAllocFlags.add(RandomXFlag.LARGE_PAGES);
            logger.info("LARGE_PAGES flag detected in VM/Cache flags, adding it to dataset allocation flags.");
        }
        
        dataset = new RandomXDataset(datasetAllocFlags); 
        logger.info("Shared RandomXDataset allocated with specific dataset allocation flags: {}. It will be initialized by ThreadState if mining mode is used.", datasetAllocFlags);

        logger.info("Shared resources setup completed for RandomXBenchmark.");
    }

    /**
     * Clean up shared resources after benchmark completion
     */
    @TearDown(Level.Trial)
    public void tearDown() {
        logger.info("Cleaning up shared resources");
        if (dataset != null) dataset.close();
        if (cache != null) cache.close();
    }

    /**
     * Benchmark mining mode without batch processing
     */
    @Benchmark
    @Group("miningNoBatch")
    public byte[] miningModeNoBatchHash(ThreadState state) {
        return state.miningTemplate.calculateHash(BLOCK_TEMPLATE);
    }

    /**
     * Benchmark mining mode with batch processing
     */
    @Benchmark
    @Group("miningBatch")
    public byte[] miningModeBatchHash(ThreadState state) {
        state.miningTemplate.calculateHashFirst(BLOCK_TEMPLATE);
        return state.miningTemplate.calculateHashNext(BLOCK_TEMPLATE);
    }

    /**
     * Benchmark light mode without batch processing
     */
    @Benchmark
    @Group("lightNoBatch")
    public byte[] lightModeNoBatchHash(ThreadState state) {
        return state.lightTemplate.calculateHash(BLOCK_TEMPLATE);
    }

    /**
     * Benchmark light mode with batch processing
     */
    @Benchmark
    @Group("lightBatch")
    public byte[] lightModeBatchHash(ThreadState state) {
        state.lightTemplate.calculateHashFirst(BLOCK_TEMPLATE);
        return state.lightTemplate.calculateHashNext(BLOCK_TEMPLATE);
    }

    /**
     * Main method to run the benchmark
     * Configures JMH options and executes the benchmark suite
     */
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(RandomXBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.TEXT)
                .jvmArgs("-Xms2G", "-Xmx2G")
                .build();

        new Runner(opt).run();
    }
}
