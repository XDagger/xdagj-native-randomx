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
 * Encapsulation for managing RandomX datasets with multi-threaded initialization.
 */
public class RandomXDataset implements AutoCloseable {
    private final Pointer dataset;

    /**
     * Allocates a RandomX dataset.
     *
     * @param flags Configuration flags for the dataset.
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
     * Multi-threaded initialization of the dataset.
     * The number of threads is determined dynamically based on the available CPU cores.
     *
     * @param cache Cache pointer.
     */
    public void initDataset(Pointer cache) {
        // Get the number of available processors (cores)
        int threads = Runtime.getRuntime().availableProcessors();

        long itemCount = RandomXJNALoader.getInstance().randomx_dataset_item_count().longValue();
        long itemsPerThread = itemCount / threads;

        Thread[] threadPool = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final long startItem = i * itemsPerThread;
            final long endItem = (i == threads - 1) ? itemCount : startItem + itemsPerThread;

            threadPool[i] = new Thread(() -> RandomXJNALoader.getInstance().randomx_init_dataset(dataset, cache, new NativeLong(startItem), new NativeLong(endItem - startItem)));
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
     * Returns the pointer to the allocated dataset.
     *
     * @return Dataset pointer.
     */
    public Pointer getPointer() {
        return dataset;
    }

    /**
     * Releases the allocated dataset.
     */
    @Override
    public void close() {
        RandomXJNALoader.getInstance().randomx_release_dataset(dataset);
    }
}