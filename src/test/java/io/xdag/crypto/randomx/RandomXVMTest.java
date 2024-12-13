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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RandomXVM class.
 * Tests the hash calculation functionality using RandomX virtual machine.
 * Verifies the basic operations and output format of the VM implementation.
 */
public class RandomXVMTest {

    /**
     * Tests hash calculation using RandomXVM.
     * This test verifies:
     * 1. VM initialization with given flags and cache
     * 2. Hash calculation with test input
     * 3. Output validation:
     *    - Not null check
     *    - Correct length (32 bytes) check
     * 4. Proper resource cleanup using try-with-resources
     */
    @Test
    public void testVMHashCalculation() {
        Set<RandomXFlag> flags = RandomXUtils.getFlagsSet();
        byte[] keyBytes = "test_key".getBytes();
        byte[] input = "test_input".getBytes();

        try (RandomXCache cache = new RandomXCache(flags);
             RandomXVM vm = new RandomXVM(flags, cache, null)) {
            cache.init(keyBytes);
            byte[] output = vm.calculateHash(input);
            assertNotNull(output, "Output should not be null.");
            assertEquals(32, output.length, "Output size should be 32 bytes.");
        }
    }
}