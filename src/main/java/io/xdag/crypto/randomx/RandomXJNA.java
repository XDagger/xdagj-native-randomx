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
import com.sun.jna.ptr.PointerByReference;

/**
 * RandomX JNA Interface
 */
public interface RandomXJNA extends Library {


    RandomXJNA INSTANCE = RandomXUtils.loadJNALibrary();

    int randomx_get_flags();

    PointerByReference randomx_alloc_cache(int flags);

    void randomx_init_cache(PointerByReference cache, Pointer key, NativeSize keySize);

    void randomx_release_cache(PointerByReference cache);

    PointerByReference randomx_create_vm(int flags, PointerByReference cache, PointerByReference dataset);

    void randomx_destroy_vm(PointerByReference machine);

    void randomx_vm_set_cache(PointerByReference machine, PointerByReference cache);

    void randomx_calculate_hash(PointerByReference machine, Pointer input, NativeSize inputSize, Pointer output);

    PointerByReference randomx_alloc_dataset(int flags);

    void randomx_init_dataset(PointerByReference dataset, PointerByReference cache, NativeLong startItem, NativeLong itemCount);

    void randomx_vm_set_dataset(PointerByReference machine, PointerByReference dataset);

    NativeLong randomx_dataset_item_count();

    void randomx_release_dataset(PointerByReference dataset);

}
