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
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that encapsulates the RandomX cache functionality.
 * This class manages the allocation, initialization and release of RandomX cache memory.
 * It implements AutoCloseable to ensure proper resource cleanup.
 */
@Getter
public class RandomXCache implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RandomXCache.class);
    
    /**
     * Pointer to the allocated RandomX cache memory.
     * -- GETTER --
     * Returns the pointer to the allocated cache memory.
     */
    @Getter
    private final Pointer cachePointer;
    
    /**
     * Pointer to the key used for cache initialization
     */
    private volatile Pointer keyPointer;
    private int keyLength;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a new RandomXCache with the specified flags.
     * Allocates memory for the cache using the native RandomX library.
     *
     * @param flags Set of RandomXFlag values that configure the cache behavior
     * @throws IllegalArgumentException if flags is null or empty
     * @throws IllegalStateException if cache allocation fails
     */
    public RandomXCache(Set<RandomXFlag> flags) {
        if (flags == null || flags.isEmpty()) {
            throw new IllegalArgumentException("Flags cannot be null or empty");
        }
        
        int combinedFlags = RandomXFlag.toValue(flags);
        logger.debug("Allocating RandomX cache with flags: {}", flags);
        
        this.cachePointer = RandomXJNALoader.getInstance().randomx_alloc_cache(combinedFlags);
        if (this.cachePointer == null) {
            throw new IllegalStateException("Failed to allocate RandomX cache");
        }
        
        logger.debug("RandomX cache allocated successfully");
    }

    /**
     * Initializes the cache with the provided key.
     * This method is thread-safe and can be called multiple times with different keys.
     *
     * @param key byte array containing the key data
     * @throws IllegalArgumentException if key is null or empty
     */
    public void init(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        lock.lock();
        try {
            long startTime = System.nanoTime();
            logger.debug("Initializing cache with key length: {}", key.length);
            
            // Free old key pointer if exists
            if (keyPointer != null) {
                keyPointer.clear(keyLength);
            }
            
            // Allocate and initialize new key
            keyLength = key.length;
            keyPointer = new Memory(key.length);
            keyPointer.write(0, key, 0, key.length);
            
            RandomXJNALoader.getInstance().randomx_init_cache(
                this.cachePointer,
                keyPointer,
                key.length
            );
            
            long endTime = System.nanoTime();
            logger.debug("Cache initialization completed in {} ms", (endTime - startTime) / 1_000_000);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Releases the allocated cache memory and key memory.
     * This method is thread-safe and idempotent.
     */
    @Override
    public void close() {
        lock.lock();
        try {
            if (keyPointer != null) {
                keyPointer.clear(keyLength);
                keyPointer = null;
            }
            
            if (cachePointer != null) {
                RandomXJNALoader.getInstance().randomx_release_cache(cachePointer);
                logger.debug("RandomX cache released successfully");
            }
        } finally {
            lock.unlock();
        }
    }
}