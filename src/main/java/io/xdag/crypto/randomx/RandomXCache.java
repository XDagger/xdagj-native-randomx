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
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a RandomX Cache object.
 * This class manages the allocation, initialization, and release of the native RandomX cache structure.
 */
@Slf4j
public class RandomXCache implements Closeable {
    private final Pointer cachePointer;
    private final Set<RandomXFlag> flags;

    /**
     * Gets the flags used to configure this cache.
     *
     * @return An unmodifiable set of RandomX flags.
     */
    public Set<RandomXFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    /**
     * Allocates a new RandomX cache.
     *
     * @param flags Flags used to initialize the cache.
     * @throws RuntimeException if cache allocation fails.
     */
    public RandomXCache(Set<RandomXFlag> flags) {
        this.flags = flags;
        int combinedFlags = RandomXFlag.toValue(flags);
        log.debug("Allocating RandomX cache with flags: {} ({})", flags, combinedFlags);
        // Use RandomXNative for allocation
        this.cachePointer = RandomXNative.randomx_alloc_cache(combinedFlags);
        if (this.cachePointer == null) {
            String errorMsg = String.format("Failed to allocate RandomX cache with flags: %s (%d)", flags, combinedFlags);
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        log.debug("RandomX cache allocated successfully at pointer: {}", Pointer.nativeValue(this.cachePointer));
    }

    /**
     * Initializes the RandomX cache with the specified key.
     *
     * @param key Key (seed) used to initialize the cache.
     * @throws RuntimeException if cache initialization fails.
     * @throws IllegalStateException if the cache is not allocated.
     */
    public void init(byte[] key) {
        if (cachePointer == null) {
            throw new IllegalStateException("Cache is not allocated.");
        }
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be null or empty for cache initialization.");
        }

        // Use JNA Memory to manage native memory
        Memory keyPointer = new Memory(key.length);
        try {
            keyPointer.write(0, key, 0, key.length);
            log.debug("Initializing RandomX cache with key of length: {}", key.length);
            // Use RandomXNative for initialization
            RandomXNative.randomx_init_cache(
                    this.cachePointer,
                    keyPointer,
                    key.length
            );
            log.debug("RandomX cache initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize RandomX cache", e);
            // Note: We don't call close() here to avoid double-free.
            // The caller is responsible for cleanup using try-with-resources.
            throw new RuntimeException("Failed to initialize RandomX cache", e);
        } finally {
            // Memory objects do not need to be manually released; JNA's GC will handle it,
            // but nullifying the reference immediately might help GC reclaim it faster.
            keyPointer = null; // Help GC
        }
    }

    /**
     * Gets the pointer to the underlying native RandomX cache structure.
     *
     * @return Pointer to the native cache.
     * @throws IllegalStateException if the cache is not allocated.
     */
    public Pointer getCachePointer() {
        if (cachePointer == null) {
            throw new IllegalStateException("Cache is not allocated.");
        }
        return cachePointer;
    }

    /**
     * Releases the resources occupied by the native RandomX cache.
     * This method should be called after finishing with the cache to prevent memory leaks.
     */
    @Override
    public void close() {
        if (cachePointer != null) {
            log.debug("Releasing RandomX cache at pointer: {}", Pointer.nativeValue(cachePointer));
            try {
                // Use RandomXNative for release
                RandomXNative.randomx_release_cache(cachePointer);
                log.info("RandomX cache released successfully");
            } catch (Throwable t) {
                log.error("Error occurred while releasing RandomX cache. Pointer: {}", Pointer.nativeValue(cachePointer), t);
            }
        }
    }
}