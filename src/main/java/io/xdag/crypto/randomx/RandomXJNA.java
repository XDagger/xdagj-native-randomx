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
 * JNA interface to map the RandomX native library functions.
 */
public interface RandomXJNA extends Library {
    /**
     * Returns the recommended flags to be used on the current CPU.
     * @return recommended flags for the current CPU
     */
    int randomx_get_flags();

    /**
     * Allocates memory for RandomX cache.
     * @param flags the initialization flags
     * @return pointer to the allocated memory or null if failed
     */
    Pointer randomx_alloc_cache(int flags);

    /**
     * Initializes the cache memory.
     * @param cache pointer to the cache memory
     * @param key pointer to the key
     * @param keySize size of the key in bytes
     */
    void randomx_init_cache(Pointer cache, Pointer key, long keySize);

    /**
     * Releases the cache memory.
     * @param cache pointer to the cache memory
     */
    void randomx_release_cache(Pointer cache);

    /**
     * Creates and initializes a RandomX virtual machine.
     * @param flags the initialization flags
     * @param cache pointer to initialized cache
     * @param dataset pointer to initialized dataset
     * @return pointer to the virtual machine or null if failed
     */
    Pointer randomx_create_vm(int flags, Pointer cache, Pointer dataset);

    /**
     * Destroys the virtual machine and frees its memory.
     * @param machine pointer to the virtual machine
     */
    void randomx_destroy_vm(Pointer machine);

    /**
     * Allocates memory for RandomX dataset.
     * @param flags the initialization flags
     * @return pointer to the allocated memory or null if failed
     */
    Pointer randomx_alloc_dataset(int flags);

    /**
     * Initializes dataset items.
     * @param dataset pointer to the dataset memory
     * @param cache pointer to initialized cache
     * @param startItem the first item to initialize
     * @param itemCount number of items to initialize
     */
    void randomx_init_dataset(Pointer dataset, Pointer cache, NativeLong startItem, NativeLong itemCount);

    /**
     * Releases the dataset memory.
     * @param dataset pointer to the dataset memory
     */
    void randomx_release_dataset(Pointer dataset);

    /**
     * Returns the number of items in the dataset.
     * @return number of dataset items
     */
    NativeLong randomx_dataset_item_count();

    /**
     * Updates the VM's cache.
     * @param machine pointer to the virtual machine
     * @param cache pointer to the new cache
     */
    void randomx_vm_set_cache(Pointer machine, Pointer cache);

    /**
     * Updates the VM's dataset.
     * @param machine pointer to the virtual machine
     * @param dataset pointer to the new dataset
     */
    void randomx_vm_set_dataset(Pointer machine, Pointer dataset);

    /**
     * Calculates a RandomX hash value.
     * @param machine pointer to the virtual machine
     * @param input pointer to the input data
     * @param inputSize size of the input data in bytes
     * @param output pointer to the output buffer
     */
    void randomx_calculate_hash(Pointer machine, Pointer input, long inputSize, Pointer output);

    /**
     * Begins a RandomX hashing operation.
     * @param machine pointer to the virtual machine
     * @param input pointer to the input data
     * @param inputSize size of the input data in bytes
     */
    void randomx_calculate_hash_first(Pointer machine, Pointer input, long inputSize);

    /**
     * Continues a RandomX hashing operation.
     * @param machine pointer to the virtual machine
     * @param nextInput pointer to the next input data
     * @param nextInputSize size of the next input data in bytes
     * @param output pointer to the output buffer
     */
    void randomx_calculate_hash_next(Pointer machine, Pointer nextInput, long nextInputSize, Pointer output);

    /**
     * Finalizes a RandomX hashing operation.
     * @param machine pointer to the virtual machine
     * @param output pointer to the output buffer
     */
    void randomx_calculate_hash_last(Pointer machine, Pointer output);

    /**
     * Calculates a RandomX commitment hash.
     * @param input pointer to the input data
     * @param inputSize size of the input data in bytes
     * @param hash_in pointer to the input hash
     * @param com_out pointer to the output commitment buffer
     */
    void randomx_calculate_commitment(Pointer input, long inputSize, Pointer hash_in, Pointer com_out);
}