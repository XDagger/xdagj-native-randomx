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

import com.google.common.io.BaseEncoding;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.junit.Test;
import static org.junit.Assert.*;
import static io.xdag.crypto.randomx.RandomXJNA.*;

public class RandomXJNATest {

    @Test
    public void testRandomx_get_flags() {
        int flagsValue = INSTANCE.randomx_get_flags();
        assertNotEquals(0, flagsValue);
    }

    @Test
    public void testRandomx_alloc_cache() {
        int flagsValue = 0;
        PointerByReference newCache = INSTANCE.randomx_alloc_cache(flagsValue);
        assertNotNull("randomx alloc cache ", newCache);
    }

    @Test
    public void testRandomx_init_cache() {
        int flagsValue = 0;
        int length = 32;
        PointerByReference newCache = INSTANCE.randomx_alloc_cache(flagsValue);
        assertNotNull("randomx alloc cache ", newCache);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(newCache, memory, new NativeSize(length));
    }

    @Test
    public void testRandomx_release_cache() {
        int flagsValue = 0;
        int length = 32;
        PointerByReference newCache = INSTANCE.randomx_alloc_cache(flagsValue);
        assertNotNull("randomx alloc cache ", newCache);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(newCache, memory, new NativeSize(length));
        INSTANCE.randomx_release_cache(newCache);
    }

    @Test
    public void testRandomx_create_vm() {
        int length = 32;
        int flagsValue = RandomXJNA.INSTANCE.randomx_get_flags();
        assertNotEquals(0, flagsValue);

        PointerByReference cache = INSTANCE.randomx_alloc_cache(flagsValue);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flagsValue);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(length));
        RandomXVM vm = new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flagsValue, cache, dataset), new RandomXWrapper.Builder().build());
        assertNotNull(vm);
    }

    @Test
    public void testRandomx_destroy_vm() {
        int length = 32;
        int flagsValue = RandomXJNA.INSTANCE.randomx_get_flags();
        assertNotEquals(0, flagsValue);

        PointerByReference cache = INSTANCE.randomx_alloc_cache(flagsValue);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flagsValue);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(length));
        RandomXVM vm = new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flagsValue, cache, dataset), new RandomXWrapper.Builder().build());
        assertNotNull(vm);

        INSTANCE.randomx_destroy_vm(vm.getPointer());
    }

    @Test
    public void testRandomx_vm_set_cache() {
        int flags = RandomXJNA.INSTANCE.randomx_get_flags();
        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        RandomXVM vm = createVM(flags, cache, dataset);
        assertNotNull(vm);

        RandomXJNA.INSTANCE.randomx_vm_set_cache(vm.getPointer(), cache);
    }

    @Test
    public void testRandomx_calculate_hash() {
        String key1 = "hello rx 1";
        String key2 = "hello world 1";
        byte[] key1Bytes = key1.getBytes();
        byte[] key2Bytes = key2.getBytes();

        //int flags = INSTANCE.randomx_get_flags() + RandomXWrapper.Flag.LARGE_PAGES.getValue() + RandomXWrapper.Flag.FULL_MEM.getValue();
        int flags = INSTANCE.randomx_get_flags();

        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        Memory memory = new Memory(key1Bytes.length);
        memory.write(0, key1Bytes, 0, key1Bytes.length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(key1Bytes.length));
        INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());

        Pointer msgPointer = new Memory(key2Bytes.length);
        Pointer hashPointer = new Memory(RandomXUtils.HASH_SIZE);
        msgPointer.write(0, key2Bytes, 0, key2Bytes.length);

        RandomXVM vm = createVM(flags, cache, dataset);
        RandomXJNA.INSTANCE.randomx_calculate_hash(vm.getPointer(), msgPointer, new NativeSize(key2Bytes.length), hashPointer);
        byte[] hash = hashPointer.getByteArray(0, RandomXUtils.HASH_SIZE);
        assertEquals("781315d3e78dc16a5060cb87677ca548d8b9aabdef5221a2851b2cc72aa2875b", BaseEncoding.base16().lowerCase().encode(hash));
        msgPointer.clear(key2Bytes.length);
        hashPointer.clear(RandomXUtils.HASH_SIZE);
    }

    @Test
    public void testRandomx_alloc_dataset() {
    }

    @Test
    public void testRandomx_init_dataset() {
    }

    @Test
    public void testRandomx_vm_set_dataset() {
    }

    @Test
    public void testRandomx_dataset_item_count() {
    }

    @Test
    public void testRandomx_release_dataset() {
    }

    private RandomXVM createVM(int flags, PointerByReference cache, PointerByReference dataset) {
        return new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), null);
    }

}
