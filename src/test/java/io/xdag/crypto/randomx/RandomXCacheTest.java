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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RandomXCache class.
 * Tests the allocation, initialization and release of RandomX cache resources.
 */
public class RandomXCacheTest {

    private final byte[] keyBytes = "test_key".getBytes(StandardCharsets.UTF_8);

    /**
     * Tests the allocation and automatic release of RandomX cache resources.
     * Verifies that the cache pointer is properly initialized.
     */
    @Test
    public void testAllocAndRelease() {
        try (RandomXCache cache = new RandomXCache(RandomXUtils.getRecommendedFlags())) {
            assertNotNull(cache.getCachePointer(), "Cache pointer should not be null.");
        } // Cache is automatically released here.
    }

    /**
     * Tests the initialization of RandomX cache with a key.
     * Verifies that the cache can be properly initialized with test key bytes.
     */
    @Test
    public void testInit() {
        try (RandomXCache cache = new RandomXCache(RandomXUtils.getRecommendedFlags())) {
            assertNotNull(cache.getCachePointer(), "Cache pointer should not be null.");
            cache.init(keyBytes);
        } // Cache is automatically released here.
    }

}