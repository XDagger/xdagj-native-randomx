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

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * JNA interface for the RandomX native library functions.
 * This interface provides direct access to the native RandomX functions through JNA.
 */
public interface RandomXJNA extends Library {

    /**
     * Gets the recommended flags for the current CPU.
     * These flags are determined based on CPU features and capabilities.
     *
     * @return Integer value representing the recommended flags
     */
    int randomx_get_flags();

    /**
     * Allocates memory for a RandomX cache.
     * The allocated memory must be released using randomx_release_cache.
     *
     * @param flags Configuration flags for the cache
     * @return Pointer to the allocated cache memory, or null if allocation fails
     */
    Pointer randomx_alloc_cache(int flags);

    /**
     * Initializes a RandomX cache with the provided key.
     * This operation is required before the cache can be used.
     *
     * @param cache Pointer to the allocated cache memory
     * @param key Pointer to the key data
     * @param keySize Size of the key in bytes
     */
    void randomx_init_cache(Pointer cache, Pointer key, long keySize);

    /**
     * Releases the memory allocated for a RandomX cache.
     * This function must be called to prevent memory leaks.
     *
     * @param cache Pointer to the cache memory to be released
     */
    void randomx_release_cache(Pointer cache);

    /**
     * Creates a new RandomX virtual machine instance.
     * The instance must be destroyed using randomx_destroy_vm when no longer needed.
     *
     * @param flags Configuration flags for the VM
     * @param cache Pointer to the initialized cache
     * @param dataset Pointer to the initialized dataset, or null if not using a dataset
     * @return Pointer to the created VM instance, or null if creation fails
     */
    Pointer randomx_create_vm(int flags, Pointer cache, Pointer dataset);

    /**
     * Destroys a RandomX virtual machine instance.
     * This function must be called to prevent memory leaks.
     *
     * @param machine Pointer to the VM instance to be destroyed
     */
    void randomx_destroy_vm(Pointer machine);

    /**
     * Allocates memory for a RandomX dataset.
     * The allocated memory must be released using randomx_release_dataset.
     *
     * @param flags Configuration flags for the dataset
     * @return Pointer to the allocated dataset memory, or null if allocation fails
     */
    Pointer randomx_alloc_dataset(int flags);

    /**
     * Initializes items in a RandomX dataset.
     * This operation can be performed in parallel by multiple threads.
     *
     * @param dataset Pointer to the dataset memory
     * @param cache Pointer to the initialized cache
     * @param startItem Index of the first item to initialize
     * @param itemCount Number of items to initialize
     */
    void randomx_init_dataset(Pointer dataset, Pointer cache, NativeLong startItem, NativeLong itemCount);

    /**
     * Gets the number of items in a RandomX dataset.
     *
     * @return Number of items in the dataset
     */
    NativeLong randomx_dataset_item_count();

    /**
     * Releases the memory allocated for a RandomX dataset.
     * This function must be called to prevent memory leaks.
     *
     * @param dataset Pointer to the dataset memory to be released
     */
    void randomx_release_dataset(Pointer dataset);

    /**
     * Updates the cache used by a RandomX VM instance.
     *
     * @param machine Pointer to the VM instance
     * @param cache Pointer to the new cache
     */
    void randomx_vm_set_cache(Pointer machine, Pointer cache);

    /**
     * Updates the dataset used by a RandomX VM instance.
     *
     * @param machine Pointer to the VM instance
     * @param dataset Pointer to the new dataset
     */
    void randomx_vm_set_dataset(Pointer machine, Pointer dataset);

    /**
     * Calculates a RandomX hash value.
     *
     * @param machine Pointer to the VM instance
     * @param input Pointer to the input data
     * @param inputSize Size of the input data in bytes
     * @param output Pointer to the output buffer (32 bytes)
     */
    void randomx_calculate_hash(Pointer machine, Pointer input, long inputSize, Pointer output);

    /**
     * Begins a multi-part RandomX hashing operation.
     *
     * @param machine Pointer to the VM instance
     * @param input Pointer to the input data
     * @param inputSize Size of the input data in bytes
     */
    void randomx_calculate_hash_first(Pointer machine, Pointer input, long inputSize);

    /**
     * Continues a multi-part RandomX hashing operation.
     *
     * @param machine Pointer to the VM instance
     * @param nextInput Pointer to the next input data
     * @param nextInputSize Size of the next input data in bytes
     * @param output Pointer to the output buffer (32 bytes)
     */
    void randomx_calculate_hash_next(Pointer machine, Pointer nextInput, long nextInputSize, Pointer output);

    /**
     * Finalizes a multi-part RandomX hashing operation.
     *
     * @param machine Pointer to the VM instance
     * @param output Pointer to the output buffer (32 bytes)
     */
    void randomx_calculate_hash_last(Pointer machine, Pointer output);

    /**
     * Calculates a RandomX commitment hash.
     * 
     * @param input Pointer to the input data
     * @param inputSize Size of the input data in bytes
     * @param hash_in Pointer to the input hash
     * @param com_out Pointer to the output commitment buffer
     */
    void randomx_calculate_commitment(Pointer input, long inputSize, Pointer hash_in, Pointer com_out);
}