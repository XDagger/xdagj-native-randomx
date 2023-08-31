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
 * RandomX JNA Interface
 */
public interface RandomXJNA extends Library {

    RandomXJNA INSTANCE = RandomXUtils.loadJNALibrary();

    int randomx_get_flags();

    Pointer randomx_alloc_cache(int flags);

    void randomx_init_cache(Pointer cache, Pointer key, NativeSize keySize);

    void randomx_release_cache(Pointer cache);

    Pointer randomx_create_vm(int flags, Pointer cache, Pointer dataset);

    void randomx_destroy_vm(Pointer machine);

    void randomx_vm_set_cache(Pointer machine, Pointer cache);

    void randomx_calculate_hash(Pointer machine, Pointer input, NativeSize inputSize, Pointer output);

    Pointer randomx_alloc_dataset(int flags);

    void randomx_init_dataset(Pointer dataset, Pointer cache, NativeLong startItem, NativeLong itemCount);

    void randomx_vm_set_dataset(Pointer machine, Pointer dataset);

    NativeLong randomx_dataset_item_count();

    void randomx_release_dataset(Pointer dataset);

}
