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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that encapsulates the RandomX dataset functionality with multi-threaded initialization support.
 * This class manages the allocation, initialization and release of RandomX dataset memory.
 * It implements AutoCloseable to ensure proper resource cleanup.
 */
public class RandomXDataset implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RandomXDataset.class);

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
        if (flags == null || flags.isEmpty()) {
            throw new IllegalArgumentException("Flags cannot be null or empty");
        }
        
        int combinedFlags = RandomXFlag.toValue(flags);
        this.dataset = RandomXJNALoader.getInstance().randomx_alloc_dataset(combinedFlags);
        
        if (dataset == null) {
            throw new IllegalStateException("Failed to allocate RandomX dataset");
        }
        
        logger.debug("RandomX dataset allocated successfully with flags: {}", flags);
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
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        
        long startTime = System.nanoTime();
        
        // Get total items count
        long totalItems = RandomXJNALoader.getInstance().randomx_dataset_item_count().longValue();
        
        // Calculate optimal thread count
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        // Default to half of available processors, but can be configured
        int initThreadCount = Math.max(1, availableProcessors / 2);
        
        logger.info("Initializing dataset with {} threads for {} items", initThreadCount, totalItems);
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(initThreadCount, new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("RandomX-Dataset-Init-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });

        try {
            // Calculate items per thread - ensure even distribution
            long perThread = totalItems / initThreadCount;
            long remainder = totalItems % initThreadCount;
            List<Future<?>> futures = new ArrayList<>(initThreadCount);
            
            // Submit initialization tasks
            long startItem = 0;
            for (int i = 0; i < initThreadCount; i++) {
                // Add remainder items to the last thread
                long itemCount = perThread + (i == initThreadCount - 1 ? remainder : 0);
                final long start = startItem;
                final long count = itemCount;
                
                futures.add(executor.submit(() -> {
                    try {
                        logger.debug("Thread {} initializing items [{}, {})", 
                                Thread.currentThread().getName(), start, start + count);

                        RandomXJNALoader.getInstance().randomx_init_dataset(
                                dataset,
                                cache.getCachePointer(),
                                new NativeLong(start),
                                new NativeLong(count)
                        );
                        
                        logger.debug("Thread {} completed initialization of {} items", 
                                Thread.currentThread().getName(), count);
                    } catch (Exception e) {
                        logger.error("Dataset initialization failed for range [{}, {})", start, start + count, e);
                        throw new RuntimeException("Dataset initialization failed", e);
                    }
                }));
                
                startItem += itemCount;
            }
            
            // Wait for all threads to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Dataset initialization interrupted", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("Dataset initialization failed", e.getCause());
                }
            }
            
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logger.info("Dataset initialization completed in {} ms", duration);
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
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
        if (dataset != null) {
            RandomXJNALoader.getInstance().randomx_release_dataset(dataset);
            logger.debug("RandomX dataset released successfully");
        }
    }
}