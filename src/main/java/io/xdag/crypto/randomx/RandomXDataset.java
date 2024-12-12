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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.util.Set;

/**
 * A class that encapsulates the RandomX dataset functionality with multi-threaded initialization support.
 * This class manages the allocation, initialization and release of RandomX dataset memory.
 * It implements AutoCloseable to ensure proper resource cleanup.
 */
public class RandomXDataset implements AutoCloseable {

    /**
     * Pointer to the allocated RandomX dataset memory
     */
    private final Pointer dataset;

    /**
     * Constructs a new RandomXDataset with the specified flags.
     * Allocates memory for the dataset using the native RandomX library.
     *
     * @param flags Set of RandomXFlag values that configure the dataset behavior
     * @throws IllegalStateException if dataset allocation fails
     */
    public RandomXDataset(Set<RandomXFlag> flags) {
        // Convert flags to integer value
        int combinedFlags = RandomXFlag.toValue(flags);

        this.dataset = RandomXJNALoader.getInstance().randomx_alloc_dataset(combinedFlags);
        if (dataset == null) {
            throw new IllegalStateException("Failed to allocate RandomX dataset.");
        }
    }

    /**
     * Initializes the dataset using multiple threads.
     * The initialization work is divided equally among threads based on available CPU cores.
     * Each thread initializes its assigned portion of the dataset items.
     *
     * @param cache The RandomXCache instance used to initialize the dataset
     * @throws RuntimeException if the initialization is interrupted
     */
    public void init(RandomXCache cache) {
        // Get the number of available processors (cores)
        int threads = Runtime.getRuntime().availableProcessors();

        // Calculate total items and items per thread
        long itemCount = RandomXJNALoader.getInstance().randomx_dataset_item_count().longValue();
        long itemsPerThread = itemCount / threads;

        // Create and start initialization threads
        Thread[] threadPool = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final long startItem = i * itemsPerThread;
            final long endItem = (i == threads - 1) ? itemCount : startItem + itemsPerThread;

            threadPool[i] = new Thread(() -> RandomXJNALoader.getInstance().randomx_init_dataset(dataset, cache.getCachePointer(), new NativeLong(startItem), new NativeLong(endItem - startItem)));
            threadPool[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threadPool) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Dataset initialization interrupted", e);
            }
        }
    }

    /**
     * Gets the pointer to the allocated dataset memory.
     *
     * @return Pointer to the dataset memory
     */
    public Pointer getPointer() {
        return dataset;
    }

    /**
     * Releases the allocated dataset memory.
     * This method is called automatically when using try-with-resources.
     */
    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_release_dataset(dataset);
    }
}