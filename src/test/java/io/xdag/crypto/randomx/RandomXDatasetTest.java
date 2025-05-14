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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RandomXDataset class.
 */
public class RandomXDatasetTest {

    // Member variables initialized in setUp()
    private Set<RandomXFlag> flags;
    private RandomXCache cache;
    private RandomXDataset dataset;

    /**
     * Sets up the necessary components before each test.
     * Allocates and initializes a cache, and allocates a dataset.
     */
    @BeforeEach
    void setUp() {
        // Get recommended flags
        flags = RandomXUtils.getRecommendedFlags(); // Use refactored method
        assertNotNull(flags, "Flags should not be null");

        // Initialize cache, as dataset initialization depends on it
        cache = new RandomXCache(flags);
        // Use a specific key for dataset tests
        cache.init("test_key_for_dataset".getBytes(StandardCharsets.UTF_8));
        assertNotNull(cache.getCachePointer(), "Cache pointer should not be null in setUp");

        // Allocate dataset
        dataset = new RandomXDataset(flags);
        assertNotNull(dataset.getDatasetPointer(), "Dataset pointer should not be null after allocation in setUp"); // Use getDatasetPointer()
    }

    /**
     * Cleans up resources after each test.
     * Releases the dataset and cache.
     */
    @AfterEach
    void tearDown() {
        if (dataset != null) {
            dataset.close();
            dataset = null;
        }
        if (cache != null) {
            cache.close();
            cache = null;
        }
    }

    /**
     * Tests if the dataset is allocated correctly in setUp and can be released.
     * Allocation happens in setUp, release happens in tearDown.
     */
    @Test
    void testDatasetAllocationAndRelease() {
        // Allocation is done in setUp, test existence and pointer validity here
        assertNotNull(dataset, "Dataset should have been allocated in setUp");
        assertNotNull(dataset.getDatasetPointer(), "Dataset pointer should not be null after allocation"); // Use getDatasetPointer()
    }

    /**
     * Tests the initialization of the dataset.
     * Assumes the dataset is allocated and cache is initialized in setUp.
     */
    @Test
    void testDatasetInit() {
        assertNotNull(cache, "Cache should be initialized in setUp");
        assertNotNull(dataset, "Dataset should be allocated in setUp");
        assertNotNull(dataset.getDatasetPointer(), "Dataset pointer should not be null before init"); // Use getDatasetPointer()

        // Attempt to initialize the dataset using the initialized cache
        assertDoesNotThrow(() -> dataset.init(cache), "Dataset initialization should not throw an exception");
    }

    /**
     * Tests that initializing the dataset with a null cache throws an exception.
     */
    @Test
    void testDatasetInitWithNullCache() {
        assertNotNull(dataset, "Dataset should be allocated in setUp");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataset.init(null); // Pass null cache
        });
        assertTrue(exception.getMessage().contains("Valid cache instance"), "Exception message should indicate invalid cache");
    }

    /**
     * Tests allocating a dataset with null flags throws an exception.
     */
    @Test
    void testAllocateWithNullFlags() {
        assertThrows(IllegalArgumentException.class, () -> new RandomXDataset(null));
    }

    /**
     * Tests allocating a dataset with empty flags throws an exception.
     */
    @Test
    void testAllocateWithEmptyFlags() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RandomXDataset(java.util.EnumSet.noneOf(RandomXFlag.class));
        });
    }
}
