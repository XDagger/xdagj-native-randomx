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
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public final class RandomXWrapper {

    private PointerByReference cache;
    private PointerByReference dataset;

    final List<RandomXVM> vms = new ArrayList<>();

    private boolean fastInit;
    private boolean miningMode;

    private Pointer memory;
    private int keySize;

    private int flagsValue;
    private final List<RandomXFlag> flags;

    /**
     * Initialize randomX cache or dataset for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void init(byte[] key) {
        setCache(key);
        if(miningMode) {
            //Initialized cache is required to create dataset, first initialize it
            setDataset(key);
        }
    }

    /**
     * Create a new VM for the current randomX instance
     * RandomX must be initialized before calling this.
     * @return RandomX_VM an Object representing the resulting VM
     */
    public RandomXVM createVM() {
        if(flags.isEmpty()) {
            flagsValue = RandomXJNA.INSTANCE.randomx_get_flags();
        } else {
            for (RandomXFlag flag : flags) {
                flagsValue += flag.getValue();
            }

        }
        PointerByReference pointer = RandomXJNA.INSTANCE.randomx_create_vm(flagsValue, cache, dataset);
        if(pointer == null) {
            throw new RuntimeException("create randomx vm error.");
        }
        RandomXVM vm = new RandomXVM(pointer, this);
        vms.add(vm);
        return vm;
    }

    /**
     * Initialize randomX cache for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    private void setCache(byte[] key) {
        if(this.memory != null && Arrays.equals(key, this.memory.getByteArray(0, keySize)))
            return;

        PointerByReference newCache = RandomXJNA.INSTANCE.randomx_alloc_cache(flagsValue);

        this.memory = new Memory(key.length);
        this.memory.write(0, key, 0, key.length);
        keySize = key.length;

        RandomXJNA.INSTANCE.randomx_init_cache(newCache, this.memory, new NativeSize(key.length));

        if(cache != null) {
            RandomXJNA.INSTANCE.randomx_release_cache(cache);
        }

        cache = newCache;
    }

    /**
     * Initialize randomX dataset for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    private void setDataset(byte[] key) {
        if(this.memory != null && Arrays.equals(key, this.memory.getByteArray(0, keySize))) {
            return;
        }

        //Allocate memory for dataset
        PointerByReference newDataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flagsValue);

        if(fastInit) {
            /*
             * If fastInit enabled use all cores to create the dataset
             * by equally distributing work between them
             */
            int threadCount = Runtime.getRuntime().availableProcessors();
            long perThread = RandomXJNA.INSTANCE.randomx_dataset_item_count().longValue() / threadCount;
            long remainder = RandomXJNA.INSTANCE.randomx_dataset_item_count().longValue() % threadCount;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            long startItem = 0;
            for (int i = 0; i < threadCount; ++i) {
                long count = perThread + (i == threadCount - 1 ? remainder : 0);
                long start = startItem;
                executorService.submit(() -> RandomXJNA.INSTANCE.randomx_init_dataset(newDataset, cache, new NativeLong(start), new NativeLong(count)));
                startItem += count;
            }

            for (int i = 0; i < threadCount; ++i) {
                long count = perThread + (i == threadCount - 1 ? remainder : 0);
                long start = startItem;
                executorService.submit(() -> RandomXJNA.INSTANCE.randomx_init_dataset(newDataset, cache, new NativeLong(start), new NativeLong(count)));
                startItem += count;
            }


            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } else {
            RandomXJNA.INSTANCE.randomx_init_dataset(newDataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());
        }

        //Release the cache that was used to create the dataset
        RandomXJNA.INSTANCE.randomx_release_cache(cache);
        cache = null;

        //If there is an old dataset release it before remplacing by the new one
        if(dataset != null) {
            RandomXJNA.INSTANCE.randomx_release_dataset(dataset);
        }
        dataset = newDataset;
    }

    /**
     * Change current randomX key by reinitializing dataset or cache
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void changeKey(byte[] key) {
        setCache(key);
        for(RandomXVM vm : vms) {
            if(vm.getPointer() != null) {
                RandomXJNA.INSTANCE.randomx_vm_set_cache(vm.getPointer(), cache);
            }
        }
        if(miningMode) {
            setDataset(key);
            for(RandomXVM vm : vms) {
                if(vm.getPointer() != null) {
                    RandomXJNA.INSTANCE.randomx_vm_set_dataset(vm.getPointer(), dataset);
                }

            }
        }
    }

    /**
     * Destroy all VMs and clear cache and dataset
     */
    public void destroy() {
        for(RandomXVM vm : vms) {
            if(vm.getPointer() != null) {
                RandomXJNA.INSTANCE.randomx_destroy_vm(vm.getPointer());
            }
        }
        vms.clear();
        if(cache != null) {
            RandomXJNA.INSTANCE.randomx_release_cache(cache);
            cache = null;
        }
        if(dataset != null) {
            RandomXJNA.INSTANCE.randomx_release_dataset(dataset);
            dataset = null;
        }
    }

}
