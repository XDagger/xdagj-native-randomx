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

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for RandomX constants and helper methods.
 * This class provides static methods to get RandomX flags.
 */
@Slf4j
public final class RandomXUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RandomXUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The size of a RandomX hash in bytes (usually 32 bytes).
     */
    public static final int RANDOMX_HASH_SIZE = 32;

    /**
     * Gets the recommended RandomX flags from the native library.
     *
     * @return An integer representing the combined RandomX flags from the native library.
     */
    public static int getNativeFlags() {
        // Ensure RandomXNative is loaded by accessing it before calling native method
        try {
            Class.forName(RandomXNative.class.getName());
        } catch (ClassNotFoundException e) {
             throw new RuntimeException("RandomXNative class not found", e);
        } catch (ExceptionInInitializerError e) {
            log.error("Failed to initialize RandomXNative: {}", e.getCause().getMessage(), e.getCause());
            throw e; // Re-throw to indicate failure
        }
        return RandomXNative.randomx_get_flags();
    }

    /**
     * Gets a set of RandomX flags recommended by the native library.
     * This method ensures that the DEFAULT flag is included if the native library
     * returns an empty set or a set that doesn't explicitly include optimizations
     * that would imply DEFAULT.
     *
     * On macOS ARM64 (Apple Silicon), this method automatically ensures SECURE flag
     * is enabled when JIT is present for stability and W^X compliance.
     *
     * @return A Set of RandomXFlag enums representing the enabled flags.
     */
    public static Set<RandomXFlag> getRecommendedFlags() {
        int nativeFlagsValue = getNativeFlags();
        log.info("Native recommended flags value: {}", nativeFlagsValue);

        Set<RandomXFlag> flagsSet = RandomXFlag.fromValue(nativeFlagsValue);
        log.info("Parsed native flags set: {}", flagsSet.stream().map(Enum::name).collect(Collectors.joining(", ")));

        // Detect platform
        String osName = System.getProperty("os.name", "").toLowerCase();
        String osArch = System.getProperty("os.arch", "").toLowerCase();
        boolean isMacOSARM64 = osName.contains("mac") &&
                               (osArch.contains("aarch64") || osArch.contains("arm64"));

        // macOS ARM64 specific handling: JIT requires SECURE for W^X compliance
        if (isMacOSARM64 && flagsSet.contains(RandomXFlag.JIT)) {
            if (!flagsSet.contains(RandomXFlag.SECURE)) {
                log.info("macOS ARM64 detected with JIT flag: Automatically enabling SECURE flag for W^X compliance and stability");
                flagsSet.add(RandomXFlag.SECURE);
            } else {
                log.info("macOS ARM64 detected: JIT and SECURE flags already present (correct configuration)");
            }
        }

        // Ensure a DEFAULT flag is present if the set is empty or only contains non-functional flags.
        // The native library should ideally always return DEFAULT (0) or a combination including it
        // if no other specific flags like JIT are set.
        // If JIT or other major flags are set, DEFAULT (0) might be implicitly part of the mode.
        // Let's ensure the set isn't empty and contains DEFAULT if no major operational flags are present.
        if (flagsSet.isEmpty()) {
            log.info("Native flags resulted in an empty set. Adding DEFAULT.");
            flagsSet.add(RandomXFlag.DEFAULT);
        } else if (!flagsSet.contains(RandomXFlag.DEFAULT) &&
                   flagsSet.stream().noneMatch(flag ->
                       flag == RandomXFlag.JIT ||
                       flag == RandomXFlag.FULL_MEM ||
                       flag == RandomXFlag.LARGE_PAGES)) {
            // If no major operational flags are set, and DEFAULT is also missing, add DEFAULT.
            log.info("No major operational flags (JIT, FULL_MEM, LARGE_PAGES) or DEFAULT found. Adding DEFAULT.");
            flagsSet.add(RandomXFlag.DEFAULT);
        }

        log.info("Final recommended flags set: {}", flagsSet.stream().map(Enum::name).collect(Collectors.joining(", ")));
        return flagsSet;
    }
}