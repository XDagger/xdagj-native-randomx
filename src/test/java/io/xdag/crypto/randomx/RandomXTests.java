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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for RandomX hash calculation functionality.
 * Tests various hash operations including single hash, batch hash and string commitment.
 */
public class RandomXTests {
    private RandomXTemplate template;
    private HexFormat hex;

    /**
     * Sets up the test environment before each test.
     * Initializes RandomX template with cache and dataset.
     */
    @BeforeEach
    public void setUp() {
        Set<RandomXFlag> flagsSet = RandomXUtils.getFlagsSet();
        RandomXCache cache = new RandomXCache(flagsSet);
        RandomXDataset dataset = new RandomXDataset(flagsSet);
        template = RandomXTemplate.builder()
                .cache(cache)
                .dataset(dataset)
                .miningMode(false)
                .flags(flagsSet)
                .build();
        template.init();
        hex = HexFormat.of();
    }

    /**
     * Tests string hash calculation with different key-input pairs.
     * Verifies that the hash output matches expected values.
     */
    @ParameterizedTest(name = "key={0}, input={1}, output={2}")
    @CsvSource({
            "test key 000,This is a test,639183aae1bf4c9a35884cb46b09cad9175f04efd7684e7262a0ac1c2f0b4e3f",
            "test key 000,Lorem ipsum dolor sit amet,300a0adb47603dedb42228ccb2b211104f4da45af709cd7547cd049e9489c969",
            "test key 000,sed do eiusmod tempor incididunt ut labore et dolore magna aliqua,c36d4ed4191e617309867ed66a443be4075014e2b061bcdaf9ce7b721d2b77a8",
            "test key 001,sed do eiusmod tempor incididunt ut labore et dolore magna aliqua,e9ff4503201c0c2cca26d285c93ae883f9b1d30c9eb240b820756f2d5a7905fc",
    })
    void testCalcStringHash(String key, String input, String output) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

        template.changeKey(keyBytes);
        assertEquals(output, hex.formatHex(template.calculateHash(inputBytes)));
    }

    /**
     * Tests string commitment calculation.
     * Verifies the commitment hash output matches expected value.
     */
    @ParameterizedTest(name = "key={0}, input={1}, output={2}")
    @CsvSource({
            "test key 000,This is a test,d53ccf348b75291b7be76f0a7ac8208bbced734b912f6fca60539ab6f86be919",
    })
    void testCalcStringCommitment(String key, String input, String output) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

        template.changeKey(keyBytes);
        assertEquals(output, hex.formatHex(template.calcStringCommitment(inputBytes)));
    }

    /**
     * Tests hash calculation with hex input.
     * Verifies the hash output matches expected value.
     */
    @ParameterizedTest(name = "key={0}, input={1}, output={2}")
    @CsvSource({
            "test key 001,0b0b98bea7e805e0010a2126d287a2a0cc833d312cb786385a7c2f9de69d25537f584a9bc9977b00000000666fd8753bf61a8631f12984e3fd44f4014eca629276817b56f32e9b68bd82f416,c56414121acda1713c2f2a819d8ae38aed7c80c35c2a769298d34f03833cd5f1",
    })
    void testCalcHexHash(String key, String input, String output) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] inputBytes = hex.parseHex(input);

        template.changeKey(keyBytes);
        assertEquals(output, hex.formatHex(template.calculateHash(inputBytes)));
    }

    /**
     * Tests batch hash calculation functionality.
     * Processes multiple inputs in sequence and verifies each hash output.
     */
    @Test
    void testBatchHash() {
        byte[] keyBytes = "test key 000".getBytes(StandardCharsets.UTF_8);

        byte[] hash1;
        byte[] hash2;
        byte[] hash3;

        byte[] input1Bytes = "This is a test".getBytes(StandardCharsets.UTF_8);
        byte[] input2Bytes = "Lorem ipsum dolor sit amet".getBytes(StandardCharsets.UTF_8);
        byte[] input3Bytes = "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua".getBytes(StandardCharsets.UTF_8);

        template.changeKey(keyBytes);

        template.calculateHashFirst(input1Bytes);
        hash1 = template.calculateHashNext(input2Bytes);
        hash2 = template.calculateHashNext(input3Bytes);
        hash3 = template.calculateHashLast();

        assertEquals("639183aae1bf4c9a35884cb46b09cad9175f04efd7684e7262a0ac1c2f0b4e3f", hex.formatHex(hash1));
        assertEquals("300a0adb47603dedb42228ccb2b211104f4da45af709cd7547cd049e9489c969", hex.formatHex(hash2));
        assertEquals("c36d4ed4191e617309867ed66a443be4075014e2b061bcdaf9ce7b721d2b77a8", hex.formatHex(hash3));
    }

}
