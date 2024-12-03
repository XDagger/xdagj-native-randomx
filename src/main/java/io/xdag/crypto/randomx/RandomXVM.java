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
 * Encapsulation for managing RandomX Virtual Machine (VM).
 */
@Getter
public class RandomXVM implements AutoCloseable {
    private final Pointer vm;
    private RandomXCache cache;
    private RandomXDataset dataset;

    /**
     * Creates a VM instance with the given flags, cache, and dataset.
     *
     * @param flags Configuration flags for the VM.
     * @param cache Cache pointer.
     * @param dataset Dataset pointer (can be null).
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
     * Switches the cache used by this VM.
     *
     * @param cache New cache pointer.
     */
    public void setCache(RandomXCache cache) {
        this.cache = cache;
        RandomXJNALoader.getInstance().randomx_vm_set_cache(vm, cache.getCachePointer());
    }

    /**
     * Switches the dataset used by this VM.
     *
     * @param dataset New dataset pointer.
     */
    public void setDataset(RandomXDataset dataset) {
        this.dataset = dataset;
        RandomXJNALoader.getInstance().randomx_vm_set_dataset(vm, dataset.getPointer());
    }

    /**
     * Calculates a hash using the VM.
     *
     * @param input Input data as a byte array.
     * @param output Output buffer for the calculated hash.
     */
    public void calculateHash(byte[] input, byte[] output) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(vm, inputPointer, input.length, outputPointer);

        outputPointer.read(0, output, 0, output.length);
    }

    public Pointer getPoint() {
        return vm;
    }

    /**
     * Releases the allocated VM.
     */
    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_destroy_vm(vm);
    }
}