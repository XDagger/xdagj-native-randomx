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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Wrapper class for RandomX virtual machine operations.
 * Manages the lifecycle and state of a RandomX VM instance.
 */
@Getter
public class RandomXVM implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RandomXVM.class);

    /**
     * The RandomX flags used to configure this VM
     */
    private final Set<RandomXFlag> flags;

    /**
     * Pointer to the native VM instance
     */
    @Getter
    private final Pointer point;

    /**
     * The cache used by this VM
     */
    private RandomXCache cache;

    /**
     * The dataset used by this VM (may be null in light mode)
     */
    private RandomXDataset dataset;

    /**
     * Creates a new RandomX VM instance with the specified configuration.
     *
     * @param flags Configuration flags for the VM
     * @param cache The cache to use for VM operations
     * @param dataset The dataset to use for VM operations (may be null)
     * @throws IllegalStateException if VM creation fails
     */
    public RandomXVM(Set<RandomXFlag> flags, RandomXCache cache, RandomXDataset dataset) {
        if (flags == null || flags.isEmpty()) {
            throw new IllegalArgumentException("Flags cannot be null or empty");
        }
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }

        this.flags = flags;
        this.cache = cache;
        this.dataset = dataset;

        int flagsValue = RandomXFlag.toValue(flags);
        Pointer datasetPointer = dataset != null ? dataset.getPointer() : null;

        this.point = RandomXJNALoader.getInstance().randomx_create_vm(
                flagsValue,
                cache.getCachePointer(),
                datasetPointer
        );

        if (point == null) {
            throw new IllegalStateException("Failed to create RandomX VM");
        }

        logger.debug("RandomX VM created with flags: {}", flags);
    }

    /**
     * Updates the cache used by this VM.
     *
     * @param cache The new cache to use
     * @throws IllegalArgumentException if cache is null
     */
    public void setCache(RandomXCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        RandomXJNALoader.getInstance().randomx_vm_set_cache(point, cache.getCachePointer());
        this.cache = cache;
        logger.debug("VM cache updated");
    }

    /**
     * Updates the dataset used by this VM.
     *
     * @param dataset The new dataset to use (may be null in light mode)
     */
    public void setDataset(RandomXDataset dataset) {
        Pointer datasetPointer = dataset != null ? dataset.getPointer() : null;
        RandomXJNALoader.getInstance().randomx_vm_set_dataset(point, datasetPointer);
        this.dataset = dataset;
    }

    /**
     * Calculates a RandomX hash using the current VM configuration.
     *
     * @param input The input data to be hashed
     * @return A 32-byte array containing the calculated hash
     * @throws IllegalArgumentException if input is null
     */
    public byte[] calculateHash(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        byte[] output = new byte[32];

        try (Memory inputMem = new Memory(input.length);
             Memory outputMem = new Memory(32)) {
            
            inputMem.write(0, input, 0, input.length);
            RandomXJNALoader.getInstance().randomx_calculate_hash(point, inputMem, input.length, outputMem);
            outputMem.read(0, output, 0, output.length);
        }

        return output;
    }

    /**
     * Begins a multi-part hash calculation.
     *
     * @param input The input data
     * @throws IllegalArgumentException if input is null
     */
    public void calculateHashFirst(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        try (Memory inputMem = new Memory(input.length)) {
            inputMem.write(0, input, 0, input.length);
            RandomXJNALoader.getInstance().randomx_calculate_hash_first(point, inputMem, input.length);
        }
    }

    /**
     * Continues a multi-part hash calculation.
     *
     * @param input The input data
     * @return A 32-byte array containing the intermediate hash result
     * @throws IllegalArgumentException if input is null
     */
    public byte[] calculateHashNext(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        byte[] output = new byte[32];

        try (Memory inputMem = new Memory(input.length);
             Memory outputMem = new Memory(32)) {
            
            inputMem.write(0, input, 0, input.length);
            RandomXJNALoader.getInstance().randomx_calculate_hash_next(point, inputMem, input.length, outputMem);
            outputMem.read(0, output, 0, output.length);
        }
        return output;
    }

    /**
     * Finalizes a multi-part hash calculation.
     *
     * @return A 32-byte array containing the final hash result
     */
    public byte[] calculateHashLast() {
        byte[] output = new byte[32];

        try (Memory outputMem = new Memory(32)) {
            RandomXJNALoader.getInstance().randomx_calculate_hash_last(point, outputMem);
            outputMem.read(0, output, 0, output.length);
        }
        return output;
    }

    /**
     * Calculates a commitment hash for the given input data.
     *
     * @param input The input data to calculate commitment for
     * @return A 32-byte array containing the calculated commitment hash
     */
    public byte[] calcStringCommitment(byte[] input) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        byte[] output = new byte[32];
        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(point, inputPointer, input.length, outputPointer);
        outputPointer.read(0, output, 0, output.length);
        RandomXJNALoader.getInstance().randomx_calculate_commitment(inputPointer, input.length, outputPointer, outputPointer);
        outputPointer.read(0, output, 0, output.length);
        return output;
    }

    /**
     * Releases the native VM resources.
     * This method is idempotent and can be called multiple times safely.
     */
    @Override
    public void close() {
        if (point != null) {
            RandomXJNALoader.getInstance().randomx_destroy_vm(point);
            logger.debug("RandomX VM destroyed");
        }
    }
}