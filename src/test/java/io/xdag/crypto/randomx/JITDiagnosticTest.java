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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic test for JIT performance and stability issues on macOS/Apple Silicon.
 * This test helps identify JIT-related crashes and performance characteristics.
 */
public class JITDiagnosticTest {

    private static final Logger log = LoggerFactory.getLogger(JITDiagnosticTest.class);

    private static final byte[] TEST_KEY = "test_key_jit_diagnostic".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_INPUT = "test_input_for_hashing".getBytes(StandardCharsets.UTF_8);
    private static final int WARMUP_ITERATIONS = 10;
    private static final int TEST_ITERATIONS = 100;

    /**
     * Test without JIT flag - should work reliably on all platforms
     */
    @Test
    public void testPerformanceWithoutJIT() {
        log.info("=== Testing WITHOUT JIT (Interpreter Mode) ===");

        Set<RandomXFlag> flags = EnumSet.noneOf(RandomXFlag.class);
        flags.addAll(RandomXUtils.getRecommendedFlags());
        flags.remove(RandomXFlag.JIT); // Ensure JIT is disabled
        flags.remove(RandomXFlag.SECURE); // Remove SECURE for baseline

        // Ensure at least DEFAULT flag
        if (flags.isEmpty()) {
            flags.add(RandomXFlag.DEFAULT);
        }

        log.info("Flags: {}", flags);

        long duration = runHashingTest(flags, "NO_JIT");
        log.info("NO_JIT Mode - Total time: {} ms, Avg per hash: {} ms",
                duration, duration / (double) TEST_ITERATIONS);
    }

    /**
     * Test with JIT flag only - may crash on macOS ARM64
     */
    @Test
    @Disabled("May crash on macOS ARM64 - enable manually for testing")
    public void testPerformanceWithJITOnly() {
        log.info("=== Testing WITH JIT ONLY ===");

        Set<RandomXFlag> flags = EnumSet.noneOf(RandomXFlag.class);
        flags.addAll(RandomXUtils.getRecommendedFlags());
        flags.remove(RandomXFlag.SECURE); // Remove SECURE
        flags.add(RandomXFlag.JIT); // Enable JIT

        log.info("Flags: {}", flags);
        log.warn("This test may crash on macOS ARM64!");

        long duration = runHashingTest(flags, "JIT_ONLY");
        log.info("JIT_ONLY Mode - Total time: {} ms, Avg per hash: {} ms",
                duration, duration / (double) TEST_ITERATIONS);
    }

    /**
     * Test with JIT + SECURE flag - recommended for macOS ARM64
     */
    @Test
    public void testPerformanceWithJITAndSecure() {
        log.info("=== Testing WITH JIT + SECURE (macOS ARM64 Recommended) ===");

        Set<RandomXFlag> flags = EnumSet.noneOf(RandomXFlag.class);
        flags.addAll(RandomXUtils.getRecommendedFlags());
        flags.add(RandomXFlag.JIT); // Enable JIT
        flags.add(RandomXFlag.SECURE); // Enable SECURE for W^X compliance

        log.info("Flags: {}", flags);
        log.info("This configuration should work on macOS ARM64");

        long duration = runHashingTest(flags, "JIT_SECURE");
        log.info("JIT+SECURE Mode - Total time: {} ms, Avg per hash: {} ms",
                duration, duration / (double) TEST_ITERATIONS);
    }

    /**
     * Compare all available modes side-by-side
     */
    @Test
    public void compareAllModes() {
        log.info("\n" + "=".repeat(80));
        log.info("PERFORMANCE COMPARISON ON: {} / {}",
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
        log.info("=".repeat(80) + "\n");

        Set<RandomXFlag> baseFlags = RandomXUtils.getRecommendedFlags();
        log.info("Base recommended flags: {}", baseFlags);

        // Mode 1: NO JIT (Interpreter)
        Set<RandomXFlag> noJitFlags = EnumSet.noneOf(RandomXFlag.class);
        noJitFlags.addAll(baseFlags);
        noJitFlags.remove(RandomXFlag.JIT);
        noJitFlags.remove(RandomXFlag.SECURE);
        // Ensure at least DEFAULT flag
        if (noJitFlags.isEmpty()) {
            noJitFlags.add(RandomXFlag.DEFAULT);
        }
        long noJitDuration = runHashingTest(noJitFlags, "INTERPRETER");

        // Mode 2: JIT + SECURE (macOS safe mode)
        Set<RandomXFlag> jitSecureFlags = EnumSet.noneOf(RandomXFlag.class);
        jitSecureFlags.addAll(baseFlags);
        jitSecureFlags.add(RandomXFlag.JIT);
        jitSecureFlags.add(RandomXFlag.SECURE);
        jitSecureFlags.remove(RandomXFlag.DEFAULT); // Remove DEFAULT when using JIT
        long jitSecureDuration = runHashingTest(jitSecureFlags, "JIT+SECURE");

        // Results summary
        log.info("\n" + "=".repeat(80));
        log.info("RESULTS SUMMARY");
        log.info("=".repeat(80));
        log.info("Mode              | Total Time | Avg/Hash | Relative Speed");
        log.info("-".repeat(80));
        log.info("INTERPRETER       | {:8} ms | {:6.2f} ms | 1.00x (baseline)",
                noJitDuration,
                noJitDuration / (double) TEST_ITERATIONS);
        log.info("JIT+SECURE        | {:8} ms | {:6.2f} ms | {:.2f}x",
                jitSecureDuration,
                jitSecureDuration / (double) TEST_ITERATIONS,
                (double) noJitDuration / jitSecureDuration);
        log.info("=".repeat(80) + "\n");

        // Performance expectations
        double speedup = (double) noJitDuration / jitSecureDuration;
        if (speedup > 1.5) {
            log.info("✓ JIT provides significant speedup ({:.1f}x) - Working as expected!", speedup);
        } else if (speedup > 1.1) {
            log.warn("⚠ JIT provides modest speedup ({:.1f}x) - May be suboptimal", speedup);
        } else {
            log.error("✗ JIT provides no speedup ({:.1f}x) - JIT may not be functioning correctly!", speedup);
        }
    }

    /**
     * Run a hashing test with specified flags
     */
    private long runHashingTest(Set<RandomXFlag> flags, String modeName) {
        log.info("\n--- Testing {} Mode ---", modeName);
        log.info("Flags: {}", flags);

        try (RandomXCache cache = new RandomXCache(flags)) {
            cache.init(TEST_KEY);

            try (RandomXVM vm = new RandomXVM(flags, cache, null)) {
                log.info("VM created successfully");

                // Warmup
                log.info("Warming up ({} iterations)...", WARMUP_ITERATIONS);
                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    vm.calculateHash(TEST_INPUT);
                }

                // Actual test
                log.info("Running test ({} iterations)...", TEST_ITERATIONS);
                long startTime = System.nanoTime();

                for (int i = 0; i < TEST_ITERATIONS; i++) {
                    byte[] hash = vm.calculateHash(TEST_INPUT);
                    assertNotNull(hash);
                    assertEquals(32, hash.length);

                    if (i == 0) {
                        // Log first hash for verification
                        log.info("First hash: {}", HexFormat.of().formatHex(hash));
                    }
                }

                long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

                log.info("Completed {} hashes in {} ms", TEST_ITERATIONS, duration);
                log.info("Average: {:.2f} ms per hash", duration / (double) TEST_ITERATIONS);
                log.info("Throughput: {:.1f} hashes/second", TEST_ITERATIONS * 1000.0 / duration);

                return duration;

            } catch (Exception e) {
                log.error("CRASH or ERROR in {} mode!", modeName, e);
                fail("Test failed with exception: " + e.getMessage());
                return -1;
            }
        }
    }
}
