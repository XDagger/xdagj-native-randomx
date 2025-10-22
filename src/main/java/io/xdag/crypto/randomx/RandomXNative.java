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

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * JNA direct mapping class for RandomX native library functions.
 * Native library loading is handled by {@link RandomXLibraryLoader}.
 * This class registers the native methods after the library is successfully loaded.
 */
public class RandomXNative {
    /**
     * Private constructor to prevent instantiation.
     * This class provides static JNA mappings and should not be instantiated.
     */
    private RandomXNative() {
        throw new UnsupportedOperationException("This is a JNA mapping class and cannot be instantiated");
    }

    static {
        try {
            // Step 1: Initialize the library using the loader.
            // This will handle extraction, loading (System.load), and setting jna.library.path.
            RandomXLibraryLoader.load();

            // Step 2: Register the native methods with JNA.
            // "randomx" is the logical name. JNA should find the loaded library
            // via the jna.library.path set by the loader, or because it's already loaded.
            Native.register("randomx");
            
            // Simple log to confirm registration attempt after loader success.
            // For more detailed logging, rely on RandomXLibraryLoader or a dedicated logging framework.
            System.out.println("[INFO] RandomXNative: Native methods registration attempted for 'randomx'.");

        } catch (UnsatisfiedLinkError ule) {
            // Logged by RandomXLibraryLoader, but good to catch and re-throw here
            // to ensure the class loading fails clearly if registration itself has an issue
            // or if the library loaded but methods can't be bound.
            System.err.println("[ERROR] RandomXNative: Failed to link native library or register methods. " +
                               "Ensure 'randomx' library was loaded correctly by RandomXLibraryLoader. " +
                               "Error: " + ule.getMessage());
            ule.printStackTrace(System.err); // Print stack trace for more details
            throw ule; // Re-throw to indicate critical failure
        } catch (Exception e) {
            System.err.println("[ERROR] RandomXNative: An unexpected error occurred during static initialization: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("Failed to initialize RandomXNative due to an unexpected error", e);
        }
    }

    // --- Native Method Declarations ---
    // Add all the necessary methods you intend to call directly
    
    /**
     * Retrieves the RandomX flags supported by the current CPU.
     * @return An integer value representing the combination of supported flags.
     * @see RandomXFlag
     */
    public static native int randomx_get_flags();

    /**
     * Allocates a RandomX cache object.
     * This cache is used to store precomputed data to speed up hash calculations, especially in "fast" mode.
     * It needs to be initialized using {@link #randomx_init_cache(Pointer, Pointer, long)}.
     * After use, it should be released via {@link #randomx_release_cache(Pointer)}.
     *
     * @param flags Flags used to initialize the cache. See {@link RandomXFlag}.
     * @return A pointer to the allocated RandomX cache. Behavior is undefined if allocation fails.
     */
    public static native Pointer randomx_alloc_cache(int flags);

    /**
     * Initializes a RandomX cache previously allocated by {@link #randomx_alloc_cache(int)}.
     *
     * @param cache Pointer to the cache object obtained from {@code randomx_alloc_cache}.
     * @param key   The key (seed) used to initialize the cache.
     * @param keySize The length of the key in bytes.
     */
    public static native void randomx_init_cache(Pointer cache, Pointer key, long keySize);

    /**
     * Returns a pointer to the internal memory buffer of the cache structure.
     * The size of the internal memory buffer is RANDOMX_ARGON_MEMORY KiB (typically 256 KiB).
     *
     * @param cache Pointer to an initialized RandomX cache. Must not be NULL.
     * @return Pointer to the internal memory buffer of the cache structure.
     */
    public static native Pointer randomx_get_cache_memory(Pointer cache);

    /**
     * Releases a RandomX cache previously allocated by {@link #randomx_alloc_cache(int)}.
     *
     * @param cache Pointer to the cache object to be released.
     */
    public static native void randomx_release_cache(Pointer cache);

    // --- Dataset related methods ---

    /**
     * Allocates a RandomX dataset object.
     * The dataset is used for "slow" hashing mode and requires a large amount of memory.
     * It needs to be initialized using {@link #randomx_init_dataset(Pointer, Pointer, NativeLong, NativeLong)}.
     * After use, it should be released via {@link #randomx_release_dataset(Pointer)}.
     *
     * @param flags Flags used to initialize the dataset. See {@link RandomXFlag}.
     * @return A pointer to the allocated RandomX dataset. Behavior is undefined if allocation fails.
     */
    public static native Pointer randomx_alloc_dataset(int flags);

    /**
     * Gets the number of items required to build a full dataset.
     * @return The total number of items in the dataset, as an unsigned long represented by {@link NativeLong}.
     */
    public static native NativeLong randomx_dataset_item_count(); // Returns unsigned long, use NativeLong

    /**
     * Initializes a portion of a RandomX dataset previously allocated by {@link #randomx_alloc_dataset(int)}.
     * This process is typically done in chunks, as the full dataset can be very large.
     *
     * @param dataset   Pointer to the dataset object obtained from {@code randomx_alloc_dataset}.
     * @param cache     Pointer to an initialized RandomX cache.
     * @param startItem The starting item index (0-based) of the dataset to initialize.
     * @param itemCount The number of items to initialize, starting from {@code startItem}.
     */
    public static native void randomx_init_dataset(Pointer dataset, Pointer cache, NativeLong startItem, NativeLong itemCount);

    /**
     * Releases a RandomX dataset previously allocated by {@link #randomx_alloc_dataset(int)}.
     *
     * @param dataset Pointer to the dataset object to be released.
     */
    public static native void randomx_release_dataset(Pointer dataset);

    // --- VM related methods ---

    /**
     * Creates a RandomX virtual machine (VM).
     * The VM is used to perform hash calculations.
     * It can be configured for "fast" mode (using a cache) or "slow" mode (using a dataset).
     * After use, it should be destroyed via {@link #randomx_destroy_vm(Pointer)}.
     *
     * @param flags   Flags used to create the VM. See {@link RandomXFlag}.
     * @param cache   Pointer to an initialized RandomX cache. Can be {@code null} if using "slow" mode.
     * @param dataset Pointer to an initialized RandomX dataset. Can be {@code null} if using "fast" mode.
     * @return A pointer to the created RandomX VM. Behavior is undefined if creation fails.
     */
    public static native Pointer randomx_create_vm(int flags, Pointer cache, Pointer dataset); // dataset can be NULL for light VM

    /**
     * Changes the cache used by an existing RandomX VM.
     *
     * @param vm    Pointer to the VM to be modified.
     * @param cache Pointer to the new initialized RandomX cache.
     */
    public static native void randomx_vm_set_cache(Pointer vm, Pointer cache);

    /**
     * Changes the dataset used by an existing RandomX VM.
     *
     * @param vm      Pointer to the VM to be modified.
     * @param dataset Pointer to the new initialized RandomX dataset. Can be {@code null} to switch to "fast" mode (if supported by the VM).
     */
    public static native void randomx_vm_set_dataset(Pointer vm, Pointer dataset); // dataset can be NULL

    /**
     * Destroys a RandomX VM previously created by {@link #randomx_create_vm(int, Pointer, Pointer)}.
     *
     * @param vm Pointer to the VM to be destroyed.
     */
    public static native void randomx_destroy_vm(Pointer vm);

    /**
     * Calculates the RandomX hash of the input data using the specified VM.
     *
     * @param vm        Pointer to an initialized VM.
     * @param input     Pointer to the input data to be hashed.
     * @param inputSize Size of the input data in bytes.
     * @param output    Pointer to a buffer where the calculated hash will be stored. The buffer size should be {@link RandomXUtils#RANDOMX_HASH_SIZE}.
     */
    public static native void randomx_calculate_hash(Pointer vm, Pointer input, long inputSize, Pointer output);

    // --- Multi-part hashing methods ---

    /**
     * Starts a multi-part RandomX hash calculation.
     * This is the first step in a multi-part hash, typically used for streaming data or very large inputs.
     *
     * @param vm        Pointer to an initialized VM.
     * @param input     Pointer to the first part of the input data.
     * @param inputSize Size of the first part of the input data in bytes.
     */
    public static native void randomx_calculate_hash_first(Pointer vm, Pointer input, long inputSize);

    /**
     * Continues a multi-part RandomX hash calculation, processing the next chunk of data.
     * Must be called after {@link #randomx_calculate_hash_first(Pointer, Pointer, long)}.
     * Can be called multiple times for sequential data chunks.
     *
     * @param vm        Pointer to the VM undergoing multi-part hashing.
     * @param input     Pointer to the next block of input data.
     * @param inputSize Size of the next block of input data in bytes.
     * @param output    Pointer to a buffer for storing an intermediate (or final, if no subsequent 'last' call) hash.
     *                  Note: The RandomX C API's randomx_calculate_hash_next doesn't always output the full hash directly;
     *                  its behavior may depend on the specific implementation. 
     *                  Typically, the final hash is obtained via {@link #randomx_calculate_hash_last(Pointer, Pointer)}.
     */
    public static native void randomx_calculate_hash_next(Pointer vm, Pointer input, long inputSize, Pointer output);

    /**
     * Finalizes a multi-part RandomX hash calculation and retrieves the final hash value.
     * Must be called after {@link #randomx_calculate_hash_first(Pointer, Pointer, long)} and any number of
     * {@link #randomx_calculate_hash_next(Pointer, Pointer, long, Pointer)} calls.
     *
     * @param vm     Pointer to the VM undergoing multi-part hashing.
     * @param output Pointer to a buffer where the final calculated hash will be stored. The buffer size should be {@link RandomXUtils#RANDOMX_HASH_SIZE}.
     */
    public static native void randomx_calculate_hash_last(Pointer vm, Pointer output);

    // --- Commitment hash method (assuming this C API exists as used) ---

    /**
     * Calculates a commitment value for a two-phase commit scheme.
     * This is an optimization specific to certain PoW variants, allowing pre-computation of part of the work
     * without knowing the full input.
     * Note: This method operates directly on input data and a pre-calculated hash, not through a VM.
     *
     * @param input     Pointer to the original input data (e.g., the first part of a block header).
     * @param inputSize Size of the original input data in bytes.
     * @param hash_in   Pointer to a pre-calculated hash of some transformation of the original input 
     *                  (e.g., a hash of the first part of the block header).
     *                  This hash will be part of the commitment calculation.
     * @param com_out   Pointer to a buffer where the calculated commitment will be stored. The buffer size should be {@link RandomXUtils#RANDOMX_HASH_SIZE}.
     */
    public static native void randomx_calculate_commitment(Pointer input, long inputSize, Pointer hash_in, Pointer com_out);

} 