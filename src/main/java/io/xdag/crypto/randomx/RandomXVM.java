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

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import lombok.Getter;

import java.util.Set;

/**
 * A class that encapsulates the RandomX Virtual Machine (VM) functionality.
 * This class manages the lifecycle and operations of a RandomX VM instance.
 */
@Getter
public class RandomXVM implements AutoCloseable {
    
    /**
     * The native pointer to the RandomX VM instance
     */
    private final Pointer vm;
    
    /**
     * The cache used by this VM instance
     */
    private RandomXCache cache;
    
    /**
     * The dataset used by this VM instance (optional)
     */
    private RandomXDataset dataset;

    /**
     * Creates a new RandomX VM instance with the specified configuration.
     *
     * @param flags The set of RandomX flags to configure the VM behavior
     * @param cache The RandomX cache to be used by the VM
     * @param dataset The RandomX dataset to be used by the VM (can be null)
     * @throws IllegalStateException if VM creation fails
     */
    public RandomXVM(Set<RandomXFlag> flags, RandomXCache cache, RandomXDataset dataset) {
        this.cache = cache;
        this.dataset = dataset;

        // Convert flags to integer value
        int combinedFlags = RandomXFlag.toValue(flags);
        this.vm = RandomXJNALoader.getInstance().randomx_create_vm(combinedFlags, cache.getCachePointer(), dataset == null?null:dataset.getPointer());
        if (vm == null) {
            throw new IllegalStateException("Failed to create RandomX VM.");
        }
    }

    /**
     * Updates the cache used by this VM instance.
     * This method allows switching to a different cache without recreating the VM.
     *
     * @param cache The new RandomX cache to be used
     */
    public void setCache(RandomXCache cache) {
        this.cache = cache;
        RandomXJNALoader.getInstance().randomx_vm_set_cache(vm, cache.getCachePointer());
    }

    /**
     * Updates the dataset used by this VM instance.
     * This method allows switching to a different dataset without recreating the VM.
     *
     * @param dataset The new RandomX dataset to be used
     */
    public void setDataset(RandomXDataset dataset) {
        this.dataset = dataset;
        RandomXJNALoader.getInstance().randomx_vm_set_dataset(vm, dataset.getPointer());
    }

    /**
     * Calculates a RandomX hash using the current VM configuration.
     *
     * @param input The input data to be hashed
     * @param output The buffer where the calculated hash will be stored
     */
    public void calculateHash(byte[] input, byte[] output) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(vm, inputPointer, input.length, outputPointer);

        outputPointer.read(0, output, 0, output.length);
    }

    /**
     * Gets the native pointer to the VM instance.
     *
     * @return The native pointer to the VM instance
     */
    public Pointer getPoint() {
        return vm;
    }

    /**
     * Releases the resources associated with this VM instance.
     * This method is called automatically when using try-with-resources.
     */
    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_destroy_vm(vm);
    }
}