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

import java.util.*;

/**
 * Template class for RandomX operations, providing a common workflow.
 * This class encapsulates the functionality for RandomX mining and hashing operations.
 */
@Builder
@ToString
public class RandomXTemplate implements AutoCloseable {

    /** Flag indicating if the template is in mining mode */
    private final boolean miningMode;
    
    /** Set of RandomX flags for configuring the algorithm behavior */
    @Getter
    private final Set<RandomXFlag> flags;
    
    /** Cache for RandomX operations */
    private final RandomXCache cache;
    
    /** Dataset for RandomX mining operations */
    private RandomXDataset dataset;
    
    /** Virtual machine instance for RandomX operations */
    private RandomXVM vm;

    /**
     * Initializes the RandomX cache or dataset based on the mining mode.
     * If in mining mode, enables FULL_MEM flag and initializes dataset.
     * Otherwise, removes FULL_MEM flag and sets dataset to null.
     */
    public void init() {
        Set<RandomXFlag> vmFlags = EnumSet.copyOf(flags);
        if (miningMode) {
            vmFlags.add(RandomXFlag.FULL_MEM);
            dataset = new RandomXDataset(vmFlags);
            dataset.init(cache);
        } else {
            vmFlags.remove(RandomXFlag.FULL_MEM);
            dataset = null;
        }
        vm = new RandomXVM(vmFlags, cache, dataset);
    }

    /**
     * Changes the current RandomX key by reinitializing the dataset or cache.
     * If the key is unchanged, returns without performing reinitialization.
     * 
     * @param key The key to initialize RandomX with (generally a hash)
     */
    public void changeKey(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        RandomXCache currentCache = vm.getCache();
        // Check if key is unchanged
        if (currentCache != null && currentCache.getCachePointer() != null && 
            currentCache.getKeyPointer() != null && 
            Arrays.equals(key, currentCache.getKeyPointer().getByteArray(0, key.length))) {
            return;
        }

        cache.init(key);
        vm.setCache(cache);
        
        if (miningMode) {
            if (dataset != null) {
                dataset.close();
            }
            dataset = new RandomXDataset(flags);
            dataset.init(cache);
            vm.setDataset(dataset);
        }
    }

    /**
     * Performs a single hash calculation using the RandomX VM.
     *
     * @param input Input data for the hash calculation
     * @return A 32-byte array containing the calculated hash
     */
    public byte[] calculateHash(byte[] input) {
        return vm.calculateHash(input);
    }

    /**
     * Begins a multi-part hash calculation by processing the first input.
     * 
     * @param input Initial input data for the hash calculation
     */
    public void calculateHashFirst(byte[] input) {
        vm.calculateHashFirst(input);
    }

    /**
     * Continues a multi-part hash calculation by processing the next input.
     * 
     * @param nextInput Next chunk of input data for the hash calculation
     * @return A 32-byte array containing the intermediate hash result
     */
    public byte[] calculateHashNext(byte[] nextInput) {
        return vm.calculateHashNext(nextInput);
    }

    /**
     * Finalizes a multi-part hash calculation.
     * 
     * @return A 32-byte array containing the final hash result
     */
    public byte[] calculateHashLast() {
        return vm.calculateHashLast();
    }

    /**
     * Calculates a commitment hash for the given input string.
     *
     * @param input The input byte array to calculate commitment for
     * @return A byte array containing the calculated commitment hash
     */
    public byte[] calcStringCommitment(byte[] input) {
        return vm.calcStringCommitment(input);
    }

    /**
     * Releases all allocated resources.
     */
    @Override
    public void close() {
        if (vm != null) {
            vm.close();
            vm = null;
        }
        if (dataset != null) {
            dataset.close();
            dataset = null;
        }
    }
}
