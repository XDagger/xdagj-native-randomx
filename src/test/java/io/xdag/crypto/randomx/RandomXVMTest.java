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

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RandomXVM class.
 * Focuses on VM creation, state changes (cache/dataset), and hash calculations.
 */
public class RandomXVMTest {

    /**
     * Tests the creation of a RandomX VM in light mode (no dataset).
     * Verifies that the VM pointer is not null after creation.
     */
    @Test
    void testVMCreationLightMode() {
        Set<RandomXFlag> testFlags = RandomXUtils.getRecommendedFlags();
        // Ensure FULL_MEM is not present for a true light mode test, 
        // though getRecommendedFlags might include it.
        // RandomXVM itself doesn't add/remove FULL_MEM; RandomXTemplate does.
        // For this test, we explicitly remove it if present to test light VM creation.
        testFlags.remove(RandomXFlag.FULL_MEM); 

        try (RandomXCache localCache = new RandomXCache(testFlags)) {
            localCache.init("test_key_vm_light".getBytes(StandardCharsets.UTF_8));
            assertNotNull(localCache.getCachePointer(), "Cache pointer should not be null for VM creation.");

            try (RandomXVM vm = new RandomXVM(testFlags, localCache, null /* No dataset for light mode */)) {
                assertNotNull(vm.getVmPointer(), "VM pointer should not be null in light mode.");
                assertEquals(testFlags, vm.getFlags(), "VM flags should match those used for creation.");
                assertSame(localCache, vm.getCache(), "VM should hold the provided cache instance.");
                assertNull(vm.getDataset(), "Dataset should be null in light mode.");
            }
        }
    }

    /**
     * Tests the creation of a RandomX VM in full mode (with a dataset).
     * Verifies that the VM pointer is not null and dataset is correctly associated.
     */
    @Test
    void testVMCreationFullMode() {
        Set<RandomXFlag> recommendedFlags = RandomXUtils.getRecommendedFlags();
        
        // For full mode, a VM operates with a dataset. 
        // The FULL_MEM flag is primarily for dataset allocation/initialization.
        // We ensure flags used for creating cache, dataset, and VM are consistent for this mode.
        Set<RandomXFlag> fullModeFlags = EnumSet.copyOf(recommendedFlags);
        fullModeFlags.add(RandomXFlag.FULL_MEM); // Ensure FULL_MEM is present for full mode context

        try (RandomXCache localCache = new RandomXCache(fullModeFlags);
             RandomXDataset localDataset = new RandomXDataset(fullModeFlags)) {
            
            localCache.init("test_key_vm_full".getBytes(StandardCharsets.UTF_8));
            assertNotNull(localCache.getCachePointer(), "Cache pointer should not be null.");
            
            localDataset.init(localCache); // Dataset needs initialized cache
            assertNotNull(localDataset.getDatasetPointer(), "Dataset pointer should not be null.");

            try (RandomXVM vm = new RandomXVM(fullModeFlags, localCache, localDataset)) {
                assertNotNull(vm.getVmPointer(), "VM pointer should not be null in full mode.");
                assertEquals(fullModeFlags, vm.getFlags(), "VM flags should match.");
                assertSame(localCache, vm.getCache(), "VM should use the provided cache.");
                assertSame(localDataset, vm.getDataset(), "VM should use the provided dataset.");
            }
        }
    }

    /**
     * Tests hash calculation using the RandomX VM.
     * Ensures that a hash can be calculated and is of the correct length.
     */
    @Test
    void testVMHashCalculation() {
        Set<RandomXFlag> testFlags = RandomXUtils.getRecommendedFlags();
        testFlags.remove(RandomXFlag.FULL_MEM); // Typically light mode for simple hash tests

        byte[] keyBytes = "test_key_hash_calc".getBytes(StandardCharsets.UTF_8);
        byte[] input = "test_input_for_hash".getBytes(StandardCharsets.UTF_8);

        try (RandomXCache localCache = new RandomXCache(testFlags)) {
            localCache.init(keyBytes);
            try (RandomXVM vm = new RandomXVM(testFlags, localCache, null)) {
                byte[] hash = vm.calculateHash(input);
                assertNotNull(hash, "Calculated hash should not be null.");
                assertEquals(32, hash.length, "Hash should be 32 bytes long.");
            }
        }
    }

    /**
     * Tests changing the cache in an existing VM.
     */
    @Test
    void testVMSetCache() {
        Set<RandomXFlag> initialFlags = RandomXUtils.getRecommendedFlags();
        initialFlags.remove(RandomXFlag.FULL_MEM);

        try (RandomXCache cache1 = new RandomXCache(initialFlags);
             RandomXCache cache2 = new RandomXCache(initialFlags)) {

            cache1.init("key_for_cache1".getBytes(StandardCharsets.UTF_8));
            cache2.init("key_for_cache2".getBytes(StandardCharsets.UTF_8));

            try (RandomXVM vm = new RandomXVM(initialFlags, cache1, null)) {
                assertSame(cache1, vm.getCache(), "Initial cache should be cache1.");
                byte[] hash1 = vm.calculateHash("input".getBytes(StandardCharsets.UTF_8));

                vm.setCache(cache2); // Change the cache
                assertSame(cache2, vm.getCache(), "Updated cache should be cache2.");
                byte[] hash2 = vm.calculateHash("input".getBytes(StandardCharsets.UTF_8));
                
                assertNotNull(hash1, "Hash1 should not be null.");
                assertNotNull(hash2, "Hash2 should not be null.");
                assertFalse(java.util.Arrays.equals(hash1, hash2), 
                    "Hashes from VMs with different caches (different keys) should differ.");
            }
        }
    }

    /**
     * Tests changing the dataset in an existing VM.
     * This test assumes the VM is created with flags compatible with dataset usage.
     */
    @Test
    // @Disabled("Dataset changes and interactions can be complex; needs careful C API review for direct VM manipulation if not using RandomXTemplate") // Re-enabling test
    void testVMSetDataset() {
        Set<RandomXFlag> fullModeFlags = RandomXUtils.getRecommendedFlags();
        fullModeFlags.add(RandomXFlag.FULL_MEM); // Ensure FULL_MEM for dataset context

        byte[] inputBytes = "test_input_for_dataset_change".getBytes(StandardCharsets.UTF_8);

        try (RandomXCache cacheForDs1 = new RandomXCache(fullModeFlags);
             RandomXDataset dataset1 = new RandomXDataset(fullModeFlags);
             RandomXCache cacheForDs2 = new RandomXCache(fullModeFlags); // Separate cache for dataset2
             RandomXDataset dataset2 = new RandomXDataset(fullModeFlags)) {

            cacheForDs1.init("key_for_dataset1".getBytes(StandardCharsets.UTF_8));
            dataset1.init(cacheForDs1);
            assertNotNull(dataset1.getDatasetPointer(), "Dataset1 pointer should not be null.");

            cacheForDs2.init("key_for_dataset2".getBytes(StandardCharsets.UTF_8)); // Different key for cache2
            dataset2.init(cacheForDs2);
            assertNotNull(dataset2.getDatasetPointer(), "Dataset2 pointer should not be null.");

            // VM initially uses dataset1 (via its cache, cacheForDs1)
            try (RandomXVM vm = new RandomXVM(fullModeFlags, cacheForDs1, dataset1)) {
                assertSame(dataset1, vm.getDataset(), "Initial dataset should be dataset1.");
                assertSame(cacheForDs1, vm.getCache(), "Initial cache for VM should be cacheForDs1.");

                byte[] hash1 = vm.calculateHash(inputBytes);
                assertNotNull(hash1, "Hash1 should not be null.");

                vm.setCache(cacheForDs2); // Change cache first
                vm.setDataset(dataset2); // Then change dataset
                
                assertSame(dataset2, vm.getDataset(), "Updated dataset should be dataset2.");
                assertSame(cacheForDs2, vm.getCache(), "Updated cache for VM should be cacheForDs2.");

                byte[] hash2 = vm.calculateHash(inputBytes);
                assertNotNull(hash2, "Hash2 should not be null.");

                assertFalse(java.util.Arrays.equals(hash1, hash2),
                        "Hashes from VMs with different datasets (and their associated caches) should differ.");
            }
        }
    }

    /**
     * Tests creation of VM with null flags (should throw IllegalArgumentException).
     */
    @Test
    void testVMCreationNullFlags() {
        try (RandomXCache localCache = new RandomXCache(RandomXUtils.getRecommendedFlags())) {
            localCache.init("key".getBytes(StandardCharsets.UTF_8));
            assertThrows(IllegalArgumentException.class, () -> {
                new RandomXVM(null, localCache, null);
            });
        }
    }

     /**
     * Tests creation of VM with null cache (should throw IllegalArgumentException).
     */
    @Test
    void testVMCreationNullCache() {
        Set<RandomXFlag> testFlags = RandomXUtils.getRecommendedFlags();
        assertThrows(IllegalArgumentException.class, () -> {
            new RandomXVM(testFlags, null, null);
        });
    }

    /**
     * Tests setting a null cache on an existing VM (should throw IllegalArgumentException).
     */
    @Test
    void testVMSetCacheWithNull() {
        Set<RandomXFlag> testFlags = RandomXUtils.getRecommendedFlags();
        testFlags.remove(RandomXFlag.FULL_MEM); // Light mode is sufficient

        try (RandomXCache initialCache = new RandomXCache(testFlags)) {
            initialCache.init("initial_key".getBytes(StandardCharsets.UTF_8));
            try (RandomXVM vm = new RandomXVM(testFlags, initialCache, null)) {
                assertThrows(IllegalArgumentException.class, () -> {
                    vm.setCache(null); // Attempt to set null cache
                }, "Setting a null cache should throw IllegalArgumentException.");
            }
        }
    }
}