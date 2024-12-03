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
import lombok.ToString;

import java.util.*;

/**
 * Template class for RandomX operations, providing a common workflow.
 */
@Builder
@ToString
public class RandomXTemplate implements AutoCloseable {

    private final boolean miningMode;
    private final Set<RandomXFlag> flags;

    private ThreadLocal<RandomXVM> threadLocalVM;

    /**
     * Initialize randomX cache or dataset for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void init(byte[] key) {
        RandomXDataset dataset;

        if(this.miningMode) {
            dataset = new RandomXDataset(flags);
        } else {
            dataset = null;
        }

        RandomXCache cache = new RandomXCache(flags, key);
        RandomXVM vm = new RandomXVM(flags,  cache, dataset);
        this.threadLocalVM = new ThreadLocal<>();
        threadLocalVM.set(vm);
    }

    /**
     * Change current randomX key by reinitializing dataset or cache
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void changeKey(byte[] key) {
        RandomXCache cache =  threadLocalVM.get().getCache();
        if(key != null && cache!= null && cache.getCachePointer() != null && Arrays.equals(key, cache.getKeyPointer().getByteArray(0, key.length)))
            return;
        threadLocalVM.get().setCache(new RandomXCache(flags, key));
        if(miningMode) {
            threadLocalVM.get().setDataset(new RandomXDataset(flags));
        }
    }

    /**
     * Performs the hash calculation using the VM.
     *
     * @param input Input data for the hash.
     * @return Calculated hash as a byte array.
     */
    public byte[] calculateHash(byte[] input) {
        Pointer inputPointer = new Memory(input.length);
        inputPointer.write(0, input, 0, input.length);

        byte[] output = new byte[32];

        Pointer outputPointer = new Memory(output.length);
        RandomXJNALoader.getInstance().randomx_calculate_hash(this.threadLocalVM.get().getPoint(), inputPointer, input.length, outputPointer);

        outputPointer.read(0, output, 0, output.length);
        return output;
    }

    @Override
    public void close() {
        threadLocalVM.remove();
    }
}
