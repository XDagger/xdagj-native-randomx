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

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class Benchmark {

    public static void main(String[] args) throws InterruptedException {
        // Benchmark configuration
        final boolean batch = true;
        final boolean commit = false;
        final boolean miningMode = true;
        final int counter = 10000;
        final int numThreads = 8; // Default to CPU cores

        final String key = "hello xdagj-native-randomx";
        final byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // Initialize RandomX components
        Set<RandomXFlag> flagsSet = RandomXUtils.getFlagsSet();
        RandomXCache cache = initCache(flagsSet, keyBytes);
        RandomXDataset dataset = new RandomXDataset(flagsSet);
        
        // Print thread configuration
        System.out.println("Running benchmark with " + numThreads + " threads");

        // Run multi-threaded benchmark
        byte[] hash = runMultiThreadedBenchmark(flagsSet, cache, dataset, keyBytes, batch, commit, counter, numThreads, miningMode);

        // Print results
        printResults(hash);
    }

    private static RandomXCache initCache(Set<RandomXFlag> flags, byte[] key) {
        RandomXCache cache = new RandomXCache(flags);
        cache.init(key);
        return cache;
    }

    private static RandomXTemplate initTemplate(Set<RandomXFlag> flags, RandomXCache cache, RandomXDataset dataset, boolean miningMode ) {
        RandomXTemplate template = RandomXTemplate.builder()
                .cache(cache)
                .dataset(dataset)
                .miningMode(miningMode)
                .flags(flags)
                .build();
        template.init();
        return template;
    }

    private static byte[] runMultiThreadedBenchmark(Set<RandomXFlag> flags, RandomXCache cache, 
            RandomXDataset dataset, byte[] input, boolean batch, boolean commit, 
            int iterations, int numThreads, boolean miningMode) throws InterruptedException {
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicLong totalHashes = new AtomicLong(0);
        long start = System.currentTimeMillis();
        byte[] finalHash = new byte[32];

        // Distribute work among threads
        int iterationsPerThread = iterations / numThreads;
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                RandomXTemplate template = initTemplate(flags, cache, dataset, miningMode);
                byte[] threadHash = runBenchmark(template, input, batch, commit, iterationsPerThread);
                totalHashes.addAndGet(iterationsPerThread);
                synchronized (finalHash) {
                    System.arraycopy(threadHash, 0, finalHash, 0, threadHash.length);
                }
            });
        }

        // Shutdown and wait for all threads
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        // Calculate and print statistics
        long end = System.currentTimeMillis();
        double duration = (end - start) / 1000.0;
        double hashRate = totalHashes.get() / duration;
        System.out.printf("Multi-threaded benchmark completed in %.2f seconds%n", duration);
        System.out.printf("Total hash rate: %.2f H/s%n", hashRate);
        System.out.printf("Average hash rate per thread: %.2f H/s%n", hashRate / numThreads);

        return finalHash;
    }

    private static byte[] runBenchmark(RandomXTemplate template, byte[] input, boolean batch, boolean commit, int iterations) {
        if(batch) {
            template.calculateHashFirst(input);
        }

        byte[] hash = new byte[32];
        
        for (int i = 0; i < iterations; i++) {
            hash = batch ? template.calculateHashNext(input) : template.calculateHash(input);
            if (commit) {
                hash = template.calcStringCommitment(input);
            }
        }
        
        return hash;
    }

    private static void printResults(byte[] hash) {
        HexFormat hex = HexFormat.of();
        System.out.printf("Final hash: %s%n", hex.formatHex(hash));
    }

}