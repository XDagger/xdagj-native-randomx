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
        // Key (or message) to be hashed
        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        System.out.println("Input message: " + key);

        // Get recommended RandomX flags for the current CPU
        Set<RandomXFlag> flags = RandomXUtils.getRecommendedFlags();

        // Allocate RandomX cache with the recommended flags
        RandomXCache cache = new RandomXCache(flags);
        
        // Initialize the cache with the key. This step is crucial before using the cache.
        cache.init(keyBytes);

        // Create and configure RandomXTemplate using a builder pattern
        byte[] hash;
        try (RandomXTemplate template = RandomXTemplate.builder()
                .cache(cache) // Provide the initialized cache
                .miningMode(false)  // Set to false for light hashing mode (no dataset)
                .flags(flags)       // Provide the base flags
                .build()) {
            
            // Initialize the template. This creates the VM.
            template.init();
            hash = template.calculateHash(keyBytes);
        } // try-with-resources automatically calls template.close()

        // Format and display the results
        HexFormat hex = HexFormat.of();
        System.out.printf("Message: %s%n", key);
        System.out.printf("Hash: %s%n", hex.formatHex(hash));
    }
}