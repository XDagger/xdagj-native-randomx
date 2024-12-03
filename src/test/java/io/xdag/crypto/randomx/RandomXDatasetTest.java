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

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RandomXDataset with multi-threaded initialization based on CPU cores.
 */
public class RandomXDatasetTest {

    @Test
    public void testDatasetInitializationWithDynamicThreads() {
        Set<RandomXFlag> flags = RandomXUtils.getFlagsSet();
        byte[] key = "test_key".getBytes();

        try (RandomXCache cache = new RandomXCache(flags, key);
             RandomXDataset dataset = new RandomXDataset(flags)) {

            long startTime = System.currentTimeMillis();
            dataset.initDataset(cache.getCachePointer()); // Dynamically adjusts thread count
            long elapsedTime = System.currentTimeMillis() - startTime;

            System.out.println("Dataset initialized in " + elapsedTime + " ms.");
            assertNotNull(dataset.getPointer(), "Dataset pointer should not be null.");
        }
    }
}