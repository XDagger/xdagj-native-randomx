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

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.jna.Pointer;

/**
 * Tests for RandomXDataset with multi-threaded initialization based on CPU cores.
 */
public class RandomXDatasetTest {

    private final Set<RandomXFlag> flagsSet = RandomXUtils.getFlagsSet();
    private final byte[] keyBytes = "test_key".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testAllocAndRelease() {
        try (RandomXDataset dataset = new RandomXDataset(flagsSet)) {
            assertNotNull(dataset.getPointer(), "Dataset pointer should not be null.");
        }
    }

    @Test
    void testInitialization() {
        try (RandomXCache cache = new RandomXCache(flagsSet);
             RandomXDataset dataset = new RandomXDataset(flagsSet)) {
            
            cache.init(keyBytes);
            long startTime = System.currentTimeMillis();
            
            assertDoesNotThrow(() -> dataset.init(cache));
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Dataset initialized in " + elapsedTime + " ms.");
            assertNotNull(dataset.getPointer(), "Dataset pointer should not be null.");
        }
    }

}