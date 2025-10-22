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

import java.util.*;

/**
 * RandomX benchmark program that mimics the C++ benchmark output format.
 * This allows for easy performance comparison between Java and C++ implementations.
 *
 * Usage: java Benchmark [--mine] [--jit] [--secure] [--softAes] [--nonces N] [--init T] [--threads T]
 */
public class Benchmark {

    // Default parameters
    private static boolean miningMode = false;
    private static boolean useJit = false;
    private static boolean useSecure = false;
    private static boolean useSoftAes = false;
    private static int nonces = 1000;
    private static int initThreads = 1;
    private static int benchThreads = 1;

    // Sample block template (same as C++ benchmark blockTemplate_)
    private static final byte[] BLOCK_TEMPLATE = {
            (byte)0x07, (byte)0x07, (byte)0xf7, (byte)0xa4, (byte)0xf0, (byte)0xd6, (byte)0x05, (byte)0xb3,
            (byte)0x03, (byte)0x26, (byte)0x08, (byte)0x16, (byte)0xba, (byte)0x3f, (byte)0x10, (byte)0x90,
            (byte)0x2e, (byte)0x1a, (byte)0x14, (byte)0x5a, (byte)0xc5, (byte)0xfa, (byte)0xd3, (byte)0xaa,
            (byte)0x3a, (byte)0xf6, (byte)0xea, (byte)0x44, (byte)0xc1, (byte)0x18, (byte)0x69, (byte)0xdc,
            (byte)0x4f, (byte)0x85, (byte)0x3f, (byte)0x00, (byte)0x2b, (byte)0x2e, (byte)0xea, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x77, (byte)0xb2, (byte)0x06, (byte)0xa0, (byte)0x2c,
            (byte)0xa5, (byte)0xb1, (byte)0xd4, (byte)0xce, (byte)0x6b, (byte)0xbf, (byte)0xdf, (byte)0x0a,
            (byte)0xca, (byte)0xc3, (byte)0x8b, (byte)0xde, (byte)0xd3, (byte)0x4d, (byte)0x2d, (byte)0xcd,
            (byte)0xee, (byte)0xf9, (byte)0x5c, (byte)0xd2, (byte)0x0c, (byte)0xef, (byte)0xc1, (byte)0x2f,
            (byte)0x61, (byte)0xd5, (byte)0x61, (byte)0x09
    };

    private static final int NONCE_OFFSET = 39;  // Position where nonce is stored in blockTemplate
    private static final String REFERENCE_RESULT = "10b649a3f15c7c7f88277812f2e74b337a0f20ce909af09199cccb960771cfa1";

    public static void main(String[] args) {
        // Parse command line arguments
        parseArgs(args);

        // Print banner
        System.out.println("RandomX benchmark v1.2.1 (Java)");

        // Configure flags
        Set<RandomXFlag> flags = buildFlags();

        // Print configuration - Argon2 first (like C++)
        System.out.println(" - Argon2 implementation: reference");
        printConfiguration(flags);

        try {
            // Initialize cache
            System.out.print("Initializing");
            if (miningMode) {
                System.out.printf(" (%d thread%s)", initThreads, initThreads > 1 ? "s" : "");
            }
            System.out.println(" ...");

            long initStart = System.nanoTime();

            // Initialize cache with seed=0 (default, same as C++ benchmark)
            byte[] seed = new byte[4];  // All zeros = seed 0
            // Arrays.fill(seed, (byte)0); // Already zero

            RandomXCache cache = new RandomXCache(flags);
            cache.init(seed);

            RandomXDataset dataset = null;
            if (miningMode) {
                // Dataset needs same flags as cache for compatibility
                Set<RandomXFlag> datasetFlags = EnumSet.copyOf(flags);
                datasetFlags.add(RandomXFlag.FULL_MEM);

                // Set dataset initialization threads via system property
                System.setProperty("randomx.dataset.threads", String.valueOf(initThreads));
                dataset = new RandomXDataset(datasetFlags);
                dataset.init(cache);
            }

            long initTime = System.nanoTime() - initStart;
            System.out.printf("Memory initialized in %.4f s%n", initTime / 1_000_000_000.0);

            // Initialize VMs
            System.out.printf("Initializing %d virtual machine%s ...%n", benchThreads, benchThreads > 1 ? "s" : "");

            // Create flags for VM (include FULL_MEM for mining mode)
            Set<RandomXFlag> vmFlags = EnumSet.copyOf(flags);
            if (miningMode) {
                vmFlags.add(RandomXFlag.FULL_MEM);
            }

            // Create VM directly instead of using RandomXTemplate
            RandomXVM vm = new RandomXVM(vmFlags, cache, dataset);

            // Run benchmark
            System.out.printf("Running benchmark (%d nonces) ...%n", nonces);
            long benchStart = System.nanoTime();

            // XOR accumulator for results (like C++ AtomicHash)
            long[] xorResult = new long[4]; // 32 bytes = 4 longs

            for (int nonce = 0; nonce < nonces; nonce++) {
                // Create block template with nonce (modify at offset 39, little-endian)
                byte[] input = BLOCK_TEMPLATE.clone();
                store32LE(input, NONCE_OFFSET, nonce);

                byte[] hash = vm.calculateHash(input);

                // XOR hash into result
                for (int i = 0; i < 4; i++) {
                    long hashPart = load64LE(hash, i * 8);
                    xorResult[i] ^= hashPart;
                }
            }

            long benchTime = System.nanoTime() - benchStart;
            double seconds = benchTime / 1_000_000_000.0;

            // Print result (convert XOR accumulator to hex)
            System.out.print("Calculated result: ");
            printHash(xorResult);

            // Show reference result if using default parameters
            if (nonces == 1000) {
                System.out.println("Reference result:  " + REFERENCE_RESULT);
            }

            // Print performance
            if (!miningMode) {
                System.out.printf("Performance: %.3f ms per hash%n", 1000 * seconds / nonces);
            } else {
                double hashesPerSecond = nonces / seconds;
                System.out.printf("Performance: %.3f hashes per second%n", hashesPerSecond);
            }

            // Cleanup
            vm.close();
            if (dataset != null) dataset.close();
            cache.close();

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mine":
                    miningMode = true;
                    break;
                case "--jit":
                    useJit = true;
                    break;
                case "--secure":
                    useSecure = true;
                    break;
                case "--softAes":
                    useSoftAes = true;
                    break;
                case "--nonces":
                    if (i + 1 < args.length) {
                        nonces = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--init":
                    if (i + 1 < args.length) {
                        initThreads = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--threads":
                    if (i + 1 < args.length) {
                        benchThreads = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
            }
        }
    }

    private static Set<RandomXFlag> buildFlags() {
        Set<RandomXFlag> flags = EnumSet.noneOf(RandomXFlag.class);

        if (useJit) {
            flags.add(RandomXFlag.JIT);
        }

        if (useSecure) {
            flags.add(RandomXFlag.SECURE);
        }

        // Add hardware AES by default unless softAes is specified
        if (!useSoftAes) {
            flags.add(RandomXFlag.HARD_AES);
        }

        // If no flags specified, use recommended flags
        if (flags.isEmpty()) {
            flags = RandomXUtils.getRecommendedFlags();
        }

        return flags;
    }

    private static void printConfiguration(Set<RandomXFlag> flags) {
        // Print mode
        if (miningMode) {
            System.out.println(" - full memory mode (2080 MiB)");
        } else {
            System.out.println(" - light memory mode (256 MiB)");
        }

        // Print compilation mode
        if (flags.contains(RandomXFlag.JIT)) {
            System.out.print(" - JIT compiled mode");
            if (flags.contains(RandomXFlag.SECURE)) {
                System.out.print(" (secure)");
            }
            System.out.println();
        } else {
            System.out.println(" - interpreted mode");
        }

        // Print AES mode
        if (flags.contains(RandomXFlag.HARD_AES)) {
            System.out.println(" - hardware AES mode");
        } else {
            System.out.println(" - software AES mode");
        }

        // Print pages mode
        if (flags.contains(RandomXFlag.LARGE_PAGES)) {
            System.out.println(" - large pages mode");
        } else {
            System.out.println(" - small pages mode");
        }

        // Print batch mode (always batch in Java version for now)
        System.out.println(" - batch mode");
    }

    private static void printHelp() {
        System.out.println("RandomX benchmark v1.2.1 (Java)");
        System.out.println("Usage: java Benchmark [OPTIONS]");
        System.out.println("Supported options:");
        System.out.println("  --help        shows this message");
        System.out.println("  --mine        mining mode: 2080 MiB");
        System.out.println("  --jit         JIT compiled mode (default: interpreter)");
        System.out.println("  --secure      W^X policy for JIT pages (default: off)");
        System.out.println("  --softAes     use software AES (default: hardware AES)");
        System.out.println("  --init T      initialize dataset with T threads (default: 1)");
        System.out.println("  --nonces N    run N nonces (default: 1000)");
        System.out.println("  --threads T   use T threads (default: 1, multi-threading not yet implemented)");
    }

    // Store 32-bit integer in little-endian format
    private static void store32LE(byte[] array, int offset, int value) {
        array[offset] = (byte) (value & 0xFF);
        array[offset + 1] = (byte) ((value >>> 8) & 0xFF);
        array[offset + 2] = (byte) ((value >>> 16) & 0xFF);
        array[offset + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    // Load 64-bit integer in little-endian format
    private static long load64LE(byte[] array, int offset) {
        return ((long) (array[offset] & 0xFF))
                | ((long) (array[offset + 1] & 0xFF) << 8)
                | ((long) (array[offset + 2] & 0xFF) << 16)
                | ((long) (array[offset + 3] & 0xFF) << 24)
                | ((long) (array[offset + 4] & 0xFF) << 32)
                | ((long) (array[offset + 5] & 0xFF) << 40)
                | ((long) (array[offset + 6] & 0xFF) << 48)
                | ((long) (array[offset + 7] & 0xFF) << 56);
    }

    // Print hash in little-endian format (like C++)
    private static void printHash(long[] hash) {
        for (long part : hash) {
            for (int i = 0; i < 8; i++) {
                System.out.printf("%02x", (part >>> (i * 8)) & 0xFF);
            }
        }
        System.out.println();
    }
}
