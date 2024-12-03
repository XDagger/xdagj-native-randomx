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
 * Encapsulation for managing RandomX cache.
 */
@Getter
public class RandomXCache implements AutoCloseable {
    private final Pointer cachePointer;
    private final Pointer keyPointer;

    public RandomXCache(Set<RandomXFlag> flags, byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null.");
        }

        // Convert flags to integer value
        int combinedFlags = RandomXFlag.toValue(flags);

        // Allocate cache
        this.cachePointer = RandomXJNALoader.getInstance().randomx_alloc_cache(combinedFlags);
        if (this.cachePointer == null) {
            throw new IllegalStateException("Failed to allocate RandomX cache.");
        }

        // Convert key to JNA Pointer
        keyPointer = new Memory(key.length);
        keyPointer.write(0, key, 0, key.length);

        // Initialize cache
        RandomXJNALoader.getInstance().randomx_init_cache(this.cachePointer, keyPointer, key.length);
    }

    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_release_cache(cachePointer);
    }
}