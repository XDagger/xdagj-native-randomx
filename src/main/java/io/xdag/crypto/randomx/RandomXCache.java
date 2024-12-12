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
 * A class that encapsulates the RandomX cache functionality.
 * This class manages the allocation, initialization and release of RandomX cache memory.
 * It implements AutoCloseable to ensure proper resource cleanup.
 */
@Getter
public class RandomXCache implements AutoCloseable {
    
    /**
     * Pointer to the allocated RandomX cache memory
     */
    private final Pointer cachePointer;
    
    /**
     * Pointer to the key used for cache initialization
     */
    private Pointer keyPointer;

    /**
     * Constructs a new RandomXCache with the specified flags.
     * Allocates memory for the cache using the native RandomX library.
     *
     * @param flags Set of RandomXFlag values that configure the cache behavior
     * @throws IllegalStateException if cache allocation fails
     */
    public RandomXCache(Set<RandomXFlag> flags) {
        // Convert flags to integer value
        int combinedFlags = RandomXFlag.toValue(flags);

        // Allocate cache
        this.cachePointer = RandomXJNALoader.getInstance().randomx_alloc_cache(combinedFlags);
        if (this.cachePointer == null) {
            throw new IllegalStateException("Failed to allocate RandomX cache.");
        }
    }

    /**
     * Initializes the cache with the provided key.
     * Converts the key to a native pointer and initializes the cache using the RandomX library.
     *
     * @param key byte array containing the key data
     * @throws NullPointerException if the key is null
     */
    public void init(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null.");
        }

        // Convert key to JNA Pointer
        keyPointer = new Memory(key.length);
        keyPointer.write(0, key, 0, key.length);
        // Initialize cache
        RandomXJNALoader.getInstance().randomx_init_cache(this.cachePointer, keyPointer, key.length);
    }

    /**
     * Releases the allocated cache memory.
     * This method is called automatically when the object is closed.
     */
    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_release_cache(cachePointer);
    }
}