package io.xdag.crypto.jna;

import java.util.ArrayList;
import java.util.Arrays;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class RandomXWrapper {

    public static final int HASH_SIZE = 32;

    private PointerByReference cache;
    private PointerByReference dataset;

    ArrayList<RandomXVM> vms = new ArrayList<>();

    boolean fastInit;

    private Pointer key;
    private int keySize;

    private int flagsValue = 0;
    private ArrayList<Flag> flags;

    /**
     * Create a randomX instance using builder provided informations
     */
    private RandomXWrapper(Builder builder) {
        fastInit = builder.fastInit;
        flags = builder.flags;
        if(flags.size() == 0) {
            flagsValue = RandomXLib.INSTANCE.randomx_get_flags();
        } else {
            if(builder.recommendedFlags) {
                flagsValue = RandomXLib.INSTANCE.randomx_get_flags();

                //Add flags not included by randomx_get_flags if present in flags list
                if(flags.contains(Flag.FULL_MEM))
                    flagsValue+=Flag.FULL_MEM.value;
                if(flags.contains(Flag.LARGE_PAGES))
                    flagsValue+=Flag.LARGE_PAGES.value;
                if(flags.contains(Flag.SECURE))
                    flagsValue+=Flag.SECURE.value;

            } else {
                for(Flag flag : flags) {
                    flagsValue += flag.value;
                }
            }
        }
    }

    /**
     * Initialize randomX cache or dataset for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void init(byte[] key) {
        if(flags.contains(Flag.FULL_MEM)) {
            setDataSet(key);
        } else {
            setCache(key);
        }
    }

    /**
     * Create a new VM for the current randomX instance
     * RandomX must be initialized before calling this.
     * @return RandomX_VM an Object representing the resulting VM
     */
    public RandomXVM createVM() {
        RandomXVM vm = new RandomXVM(RandomXLib.INSTANCE.randomx_create_vm(flagsValue, cache, dataset), this);
        vms.add(vm);
        return vm;
    }

    /**
     * Initialize randomX cache for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    private void setCache(byte[] key) {
        if(this.key != null && Arrays.equals(key, this.key.getByteArray(0, keySize)))
            return;

        PointerByReference newCache = RandomXLib.INSTANCE.randomx_alloc_cache(flagsValue);

        this.key = new Memory(key.length);
        this.key.write(0, key, 0, key.length);
        keySize = key.length;

        RandomXLib.INSTANCE.randomx_init_cache(newCache, this.key, new NativeSize(key.length));

        if(cache != null) {
            RandomXLib.INSTANCE.randomx_release_cache(cache);
        }

        cache = newCache;
    }

    /**
     * Initialize randomX dataset for a specific key
     * @param key The key to initialize randomX with. (generally a hash)
     */
    private void setDataSet(byte[] key) {
        if(this.key != null && Arrays.equals(key, this.key.getByteArray(0, keySize))) {
            return;
        }

        //Initialized cache is required to create dataset, first initialize it
        setCache(key);

        PointerByReference newDataset;

        //Allocate memory for dataset
        if(flags.contains(Flag.LARGE_PAGES)) {
            newDataset = RandomXLib.INSTANCE.randomx_alloc_dataset(Flag.LARGE_PAGES.getValue());
        } else {
            newDataset = RandomXLib.INSTANCE.randomx_alloc_dataset(0);
        }

        if(fastInit) {
            /*
             * If fastInit enabled use all cores to create the dataset
             * by equally distributing work between them
             */
            ArrayList<Thread> threads = new ArrayList<>();
            int threadCount = Runtime.getRuntime().availableProcessors();
            long perThread = RandomXLib.INSTANCE.randomx_dataset_item_count().longValue() / threadCount;
            long remainder = RandomXLib.INSTANCE.randomx_dataset_item_count().longValue() % threadCount;

            long startItem = 0;
            for (int i = 0; i < threadCount; ++i) {
                long count = perThread + (i == threadCount - 1 ? remainder : 0);
                long start = startItem;
                Thread thread = new Thread(() -> RandomXLib.INSTANCE.randomx_init_dataset(newDataset, cache, new NativeLong(start), new NativeLong(count)));
                thread.start();
                threads.add(thread);
                startItem += count;
            }

            //wait for every thread to terminate execution (ie: dataset is initialised)
            for(Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            }

        }   else {
            RandomXLib.INSTANCE.randomx_init_dataset(newDataset, cache, new NativeLong(0), RandomXLib.INSTANCE.randomx_dataset_item_count());
        }

        //Release the cache that was used to create the dataset
        RandomXLib.INSTANCE.randomx_release_cache(cache);
        cache = null;

        //If there is a old dataset release it before remplacing by the new one
        if(dataset != null) {
            RandomXLib.INSTANCE.randomx_release_dataset(dataset);
        }
        dataset = newDataset;
    }

    /**
     * Change current randomX key by reinitializing dataset or cache
     * @param key The key to initialize randomX with. (generally a hash)
     */
    public void changeKey(byte[] key) {
        if(flags.contains(Flag.FULL_MEM)) {
            setDataSet(key);
            for(RandomXVM vm : vms) {
                RandomXLib.INSTANCE.randomx_vm_set_dataset(vm.getPointer(), dataset);
            }
        }   else    {
            setCache(key);
            for(RandomXVM vm : vms) {
                RandomXLib.INSTANCE.randomx_vm_set_cache(vm.getPointer(), cache);
            }
        }

    }

    /**
     * Destroy all VMs and clear cache and dataset
     */
    public void destroy() {
        for(RandomXVM vm : vms) {
            RandomXLib.INSTANCE.randomx_destroy_vm(vm.getPointer());
        }
        vms.clear();
        if(cache != null) {
            RandomXLib.INSTANCE.randomx_release_cache(cache);
            cache = null;
        }
        if(dataset != null) {
            RandomXLib.INSTANCE.randomx_release_dataset(cache);
            dataset = null;
        }
    }

    /**
     * New RandomX instance builder
     *
     * <p>
     * Example:<br><br>
     * {@code RandomX randomX = new RandomX.Builder()}<br>
     * {@code .build();}
     *
     * {@code randomX.init(hash);}
     * {@code RandomX_VM vm = randomX.createVM();}
     * {@code byte[] hash = vm.getHash(bytes);}
     * <p>
     *
     */
    public static class Builder {

        private boolean recommendedFlags = false;
        private ArrayList<Flag> flags = new ArrayList<Flag>();

        private boolean fastInit = false;

        public RandomXWrapper build() {
            return new RandomXWrapper(this);
        }

        public Builder fastInit(boolean value) {
            fastInit = value;
            return this;
        }

        public Builder recommendedFlags() {
            recommendedFlags = true;
            return this;
        }

        public Builder flag(Flag flag) {
            flags.add(flag);
            return this;
        }
    }

    public enum Flag {
        DEFAULT(0),
        LARGE_PAGES(1),
        HARD_AES(2),
        FULL_MEM(4),
        JIT(8),
        SECURE(16),
        ARGON2_SSSE3(32),
        ARGON2_AVX2(64),
        ARGON2(96);

        private int value;

        Flag(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public static void main(String[] args) {
        RandomXWrapper.Builder builder = new Builder();
        builder.flag(Flag.JIT);
        builder.fastInit(true);
        RandomXWrapper randomx = new RandomXWrapper(builder);
        byte[] cache = new byte[32];
        randomx.init(cache);
    }

}
