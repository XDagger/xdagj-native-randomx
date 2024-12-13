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

/**
 * Example class demonstrating the usage of RandomX hashing algorithm.
 * This class shows how to initialize and use the RandomX components to generate hashes.
 */
public class Example {

    /**
     * Main method demonstrating the RandomX hashing process.
     * Shows initialization of RandomX components and hash calculation.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Key to be hashed
        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // Get supported RandomX flags for the current CPU
        Set<RandomXFlag> flags = RandomXUtils.getFlagsSet();
        System.out.println("Supported flags: " + flags);
        int combinedFlags = RandomXFlag.toValue(flags);
        System.out.println("Combined flags value: " + combinedFlags);

        // Initialize RandomX cache with the supported flags
        RandomXCache cache = new RandomXCache(flags);
        cache.init(keyBytes);

        // Create and configure RandomXTemplate using builder pattern
        byte[] hash;
        try (RandomXTemplate template = RandomXTemplate.builder()
                .cache(cache)
                .miningMode(false)  // Set to false for normal hashing mode
                .flags(flags)
                .build()) {

            // Initialize the template with the configured settings
            template.init();

            // Calculate hash of the input key
            hash = template.calculateHash(keyBytes);
        }

        // Format and display the results
        HexFormat hex = HexFormat.of();
        System.out.printf("Message: %s%n", key);
        System.out.printf("Hash: %s%n", hex.formatHex(hash));
    }
}