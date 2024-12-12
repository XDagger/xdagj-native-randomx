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
public class RandomXTemplate {

    /** Flag indicating if the template is in mining mode */
    private final boolean miningMode;
    
    /** Set of RandomX flags for configuring the algorithm behavior */
    @Getter
    private final Set<RandomXFlag> flags;
    
    /** Cache for RandomX operations */
    private RandomXCache cache;
    
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
        if(this.miningMode) {
            flags.add(RandomXFlag.FULL_MEM);
            dataset = new RandomXDataset(flags);
        } else {
            flags.remove(RandomXFlag.FULL_MEM);
            dataset = null;
        }
        vm = new RandomXVM(flags, cache, dataset);
    }

    /**
     * Changes the current RandomX key by reinitializing the dataset or cache.
     * If the key is unchanged, returns without performing reinitialization.
     * 
     * @param key The key to initialize RandomX with (generally a hash)
     */
    public void changeKey(byte[] key) {
        assert cache != null;
        RandomXCache cache = vm.getCache();
        if(key != null && cache!= null && cache.getCachePointer() != null && (cache.getKeyPointer() != null && Arrays.equals(key, cache.getKeyPointer().getByteArray(0, key.length))))
            return;

        assert cache != null;
        cache.init(key);
        vm.setCache(cache);
        if(miningMode) {
            RandomXDataset dataset = new RandomXDataset(flags);
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
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        byte[] output = new byte[32];

        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(vm.getPoint(), inputPointer, input.length, outputPointer);

        outputPointer.read(0, output, 0, output.length);
        return output;
    }

    /**
     * Begins a multi-part hash calculation by processing the first input.
     * 
     * @param input Initial input data for the hash calculation
     */
    public void calculateHashFirst(byte[] input) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash_first(vm.getPoint(), inputPointer, input.length);
    }

    /**
     * Continues a multi-part hash calculation by processing the next input.
     * 
     * @param nextInput Next chunk of input data for the hash calculation
     * @return A 32-byte array containing the intermediate hash result
     */
    public byte[] calculateHashNext(byte[] nextInput) {
        Pointer inputPointer = new Memory(nextInput.length);
        inputPointer.write(0, nextInput, 0, nextInput.length);

        byte[] output = new byte[32];
        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash_next(vm.getPoint(), inputPointer, nextInput.length, outputPointer);

        outputPointer.read(0, output, 0, output.length);
        return output;
    }

    /**
     * Finalizes a multi-part hash calculation.
     * 
     * @return A 32-byte array containing the final hash result
     */
    public byte[] calculateHashLast() {
        byte[] output = new byte[32];
        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash_last(vm.getPoint(), outputPointer);
        outputPointer.read(0, output, 0, output.length);
        return output;
    }

    /**
     * Calculates a commitment hash for the given input string.
     * This is a two-step process that first calculates a regular hash and then
     * generates a commitment from that hash.
     * 
     * @param input Input data for the commitment calculation
     * @return A 32-byte array containing the commitment hash
     */
    public byte[] calcStringCommitment(byte[] input) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        byte[] output = new byte[32];
        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(vm.getPoint(), inputPointer, input.length, outputPointer);
        outputPointer.read(0, output, 0, output.length);
        RandomXJNALoader.getInstance().randomx_calculate_commitment(inputPointer, input.length, outputPointer, outputPointer);
        outputPointer.read(0, output, 0, output.length);
        return output;
    }

}
