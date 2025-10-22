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

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Template class for RandomX operations, providing a common workflow.
 * This class encapsulates the functionality for RandomX mining and hashing operations.
 */
@Builder
@ToString
@Slf4j
public class RandomXTemplate implements AutoCloseable {
    /**
     * Private constructor to be used by the Lombok generated builder.
     * Direct instantiation is discouraged; use the builder pattern.
     */
    private RandomXTemplate(boolean miningMode, Set<RandomXFlag> flags, RandomXCache cache, RandomXDataset dataset, RandomXVM vm, byte[] currentKey) {
        this.miningMode = miningMode;
        this.flags = flags;
        this.cache = cache;
        this.dataset = dataset;
        this.vm = vm;
        // Defensive copy to prevent external modification
        this.currentKey = currentKey != null ? Arrays.copyOf(currentKey, currentKey.length) : null;
    }

    /** Flag indicating if the template is in mining mode */
    @Getter
    private final boolean miningMode;

    /** Set of RandomX flags for configuring the algorithm behavior */
    private final Set<RandomXFlag> flags;

    /**
     * Gets the flags used to configure this RandomX template.
     *
     * @return An unmodifiable set of RandomX flags.
     */
    public Set<RandomXFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /** Cache for RandomX operations */
    @Getter
    private final RandomXCache cache;
    
    /** Dataset for RandomX mining operations */
    @Getter
    private RandomXDataset dataset;
    
    /** Virtual machine instance for RandomX operations */
    @Getter
    private RandomXVM vm;

    /** Stores the current key used for cache initialization to avoid redundant re-initializations. */
    private byte[] currentKey;

    /**
     * Gets a copy of the current key to prevent external modification of internal state.
     * @return A copy of the current key, or null if no key is set.
     */
    public byte[] getCurrentKey() {
        return currentKey != null ? Arrays.copyOf(currentKey, currentKey.length) : null;
    }

    /**
     * Initializes the RandomX virtual machine (VM) with the configured settings.
     * This method must be called before any hash calculation.
     * If in mining mode, the dataset should be initialized before calling this method,
     * and if in light mode, the cache should be initialized.
     */
    public void init() {
        Set<RandomXFlag> vmFlags = EnumSet.copyOf(flags);
        if (miningMode) {
            vmFlags.add(RandomXFlag.FULL_MEM);
            // Ensure cache is initialized with currentKey before creating dataset
            if (this.currentKey == null) {
                log.warn("Initializing RandomXTemplate without a key set for the cache. Dataset initialization might rely on an uninitialized cache if not subsequently set.");
            } else if (cache.getCachePointer() == null) { // Cache might be allocated but not initialized
                 log.warn("Cache pointer is null during init despite currentKey being set. This should not happen if cache is managed correctly.");
            }

            log.debug("Mining mode enabled. Creating and initializing dataset with flags: {}", vmFlags);
            dataset = new RandomXDataset(vmFlags); // Dataset uses its own flags, usually including FULL_MEM
            dataset.init(cache); // Dataset initialization depends on an initialized cache
        } else {
            vmFlags.remove(RandomXFlag.FULL_MEM);
            if (dataset != null) {
                dataset.close(); // Ensure previous dataset is closed if switching modes
            }
            dataset = null;
            log.debug("Light mode enabled. Dataset will not be used.");
        }

        log.debug("Creating RandomXVM with flags: {} (Cache: {}, Dataset: {})", 
            vmFlags, 
            cache != null ? "Present" : "Null", 
            dataset != null ? "Present" : "Null");
        vm = new RandomXVM(vmFlags, cache, dataset);
        log.info("RandomXTemplate initialized. VM created.");
    }

    /**
     * Changes the current RandomX key by reinitializing the cache and, if in mining mode, the dataset.
     * If the provided key is the same as the current key, this method returns without reinitialization.
     *
     * @param key The new key (typically a seed hash) to initialize RandomX components with.
     * @throws IllegalArgumentException if the key is null or empty.
     */
    public void changeKey(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty for changeKey operation.");
        }

        // Check if the new key is the same as the current key.
        if (Arrays.equals(this.currentKey, key)) {
            log.debug("Key is unchanged. Skipping reinitialization.");
            return;
        }

        log.info("Changing RandomX key. Old key hash (if any): {}, New key hash: {}", 
            (this.currentKey != null ? Arrays.hashCode(this.currentKey) : "N/A"), Arrays.hashCode(key));

        // Initialize the cache with the new key.
        // The cache instance itself is final, but its internal state is changed by init().
        cache.init(key);
        this.currentKey = Arrays.copyOf(key, key.length); // Store a copy of the new key.

        // If a VM instance exists, update its cache.
        // If init() hasn't been called yet, vm will be null. The new cache will be used when vm is created in init().
        if (vm != null) {
             log.debug("Updating VM with the new cache.");
            vm.setCache(cache);
        } else {
            log.warn("VM is null during changeKey. The new cache will be used upon VM creation in init().");
        }

        // If in mining mode, the dataset also needs to be reinitialized with the new cache.
        if (miningMode) {
            log.debug("Mining mode: Reinitializing dataset due to key change.");

            // Create new dataset first, then close old one to avoid state inconsistency
            RandomXDataset oldDataset = this.dataset;
            RandomXDataset newDataset = null;

            try {
                // The flags for the dataset should include FULL_MEM.
                Set<RandomXFlag> datasetFlags = EnumSet.copyOf(this.flags); // Start with base flags
                datasetFlags.add(RandomXFlag.FULL_MEM);

                newDataset = new RandomXDataset(datasetFlags);
                newDataset.init(cache); // Initialize with the cache that has the new key.

                // Successfully created and initialized, update reference
                this.dataset = newDataset;

                // Update VM with new dataset if VM exists
                if (vm != null) {
                    log.debug("Updating VM with the new dataset.");
                    vm.setDataset(newDataset);
                }

                // Now it's safe to close the old dataset
                if (oldDataset != null) {
                    try {
                        oldDataset.close();
                        log.debug("Old dataset closed successfully.");
                    } catch (Exception e) {
                        log.warn("Failed to close old dataset", e);
                        // Continue anyway since new dataset is already set
                    }
                }

            } catch (Exception e) {
                log.error("Failed to create/initialize new dataset during key change", e);
                // Cleanup the new dataset if it was created
                if (newDataset != null) {
                    try {
                        newDataset.close();
                    } catch (Exception cleanupEx) {
                        log.warn("Failed to cleanup new dataset after initialization failure", cleanupEx);
                    }
                }
                // Keep old dataset if it exists (don't set this.dataset to null)
                throw new RuntimeException("Failed to reinitialize dataset with new key", e);
            }
        } else {
            // In light mode, ensure dataset is null if it was somehow set
            if (vm != null && vm.getDataset() != null) {
                log.debug("Light mode: Ensuring VM dataset is null after key change.");
                vm.setDataset(null); 
            }
        }
        log.info("RandomX key changed and components reinitialized successfully.");
    }

    /**
     * Performs a single hash calculation using the RandomX VM.
     *
     * @param input Input data for the hash calculation.
     * @return A 32-byte array containing the calculated hash.
     * @throws IllegalStateException if the VM is not initialized.
     */
    public byte[] calculateHash(byte[] input) {
        if (vm == null) {
            throw new IllegalStateException("RandomX VM is not initialized. Call init() first or ensure key is set.");
        }
        return vm.calculateHash(input);
    }

    /**
     * Begins a multi-part hash calculation by processing the first input.
     * 
     * @param input Initial input data for the hash calculation.
     * @throws IllegalStateException if the VM is not initialized.
     */
    public void calculateHashFirst(byte[] input) {
        if (vm == null) {
            throw new IllegalStateException("RandomX VM is not initialized. Call init() first or ensure key is set.");
        }
        vm.calculateHashFirst(input);
    }

    /**
     * Continues a multi-part hash calculation by processing the next input.
     * 
     * @param nextInput Next chunk of input data for the hash calculation.
     * @return A 32-byte array containing the intermediate hash result.
     * @throws IllegalStateException if the VM is not initialized.
     */
    public byte[] calculateHashNext(byte[] nextInput) {
        if (vm == null) {
            throw new IllegalStateException("RandomX VM is not initialized. Call init() first or ensure key is set.");
        }
        return vm.calculateHashNext(nextInput);
    }

    /**
     * Finalizes a multi-part hash calculation.
     * 
     * @return A 32-byte array containing the final hash result.
     * @throws IllegalStateException if the VM is not initialized.
     */
    public byte[] calculateHashLast() {
        if (vm == null) {
            throw new IllegalStateException("RandomX VM is not initialized. Call init() first or ensure key is set.");
        }
        return vm.calculateHashLast();
    }

    /**
     * Calculates a commitment hash for the given input data.
     *
     * @param input The input byte array to calculate commitment for.
     * @return A byte array containing the calculated commitment hash.
     * @throws IllegalStateException if the VM is not initialized.
     */
    public byte[] calculateCommitment(byte[] input) {
        if (vm == null) {
            throw new IllegalStateException("RandomX VM is not initialized. Call init() first or ensure key is set.");
        }
        byte[] hashOfInput = vm.calculateHash(input);

        // Then, use the original input and this calculated hash to get the commitment.
        return vm.calculateCommitment(input, hashOfInput);
    }

    /**
     * Releases all allocated resources (VM and Dataset).
     * The Cache is managed externally if passed to the builder, or internally if created by this template.
     * The Current implementation assumes cache is provided via builder and its lifecycle is managed outside this close().
     * If RandomXTemplate were to create its own RandomXCache, it should also close it here.
     *
     * Note: This method attempts to close all resources independently, ensuring that failure
     * to close one resource does not prevent cleanup of others.
     */
    @Override
    public void close() {
        log.debug("Closing RandomXTemplate resources...");

        // Close VM first (highest level resource)
        if (vm != null) {
            try {
                log.debug("Closing RandomX VM...");
                vm.close();
            } catch (Exception e) {
                log.error("Failed to close RandomX VM", e);
            }
        }

        // Close dataset second
        if (dataset != null) {
            try {
                log.debug("Closing RandomX Dataset...");
                dataset.close();
            } catch (Exception e) {
                log.error("Failed to close RandomX Dataset", e);
            }
        }

        // currentKey does not need explicit closing.
        // Cache is not closed here as it's assumed to be managed externally (passed in via builder).
        log.info("RandomXTemplate resources closed.");
    }
}
