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
package io.xdag.crypto.jna;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface RandomXLib extends Library {

    String JNA_LIBRARY_NAME = "randomx";
    RandomXLib INSTANCE = loadLib();

    int randomx_get_flags();

    PointerByReference randomx_alloc_cache(int flags);

    void randomx_init_cache(PointerByReference cache, Pointer key, NativeSize keySize);

    PointerByReference randomx_create_vm(int flags, PointerByReference cache, PointerByReference dataset);

    void randomx_calculate_hash(PointerByReference machine, Pointer input, NativeSize inputSize, Pointer output);

    void randomx_release_cache(PointerByReference cache);

    void randomx_destroy_vm(PointerByReference machine);

    void randomx_vm_set_cache(PointerByReference machine, PointerByReference cache);

    PointerByReference randomx_alloc_dataset(int flags);

    NativeLong randomx_dataset_item_count();

    void randomx_init_dataset(PointerByReference dataset, PointerByReference cache, NativeLong startItem, NativeLong itemCount);

    void randomx_release_dataset(PointerByReference dataset);

    void randomx_vm_set_dataset(PointerByReference machine, PointerByReference dataset);

    /**
     * Extract library from jar to lib/ directory then load it
     */
    private static RandomXLib loadLib() {
        return Native.load(JNA_LIBRARY_NAME, RandomXLib.class);
    }
}
