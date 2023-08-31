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

import static io.xdag.crypto.randomx.RandomXJNA.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

public class RandomXJNATest {

    @Test
    public void testRandomx_get_flags() {
        int flags = INSTANCE.randomx_get_flags();
        assertNotEquals(0, flags);
    }

    @Test
    public void testRandomx_alloc_cache() {
        int flags = RandomXFlag.JIT.getValue();
        Pointer newCache = INSTANCE.randomx_alloc_cache(flags);
        assertNotNull(newCache,"randomx alloc cache ");

        INSTANCE.randomx_release_cache(newCache);
    }

    @Test
    public void testRandomx_init_cache() {
        int length = 32;
        int flags = RandomXFlag.JIT.getValue();
        Pointer newCache = INSTANCE.randomx_alloc_cache(flags);
        assertNotNull(newCache, "randomx alloc cache ");

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);

        INSTANCE.randomx_init_cache(newCache, memory, new NativeSize(length));
        INSTANCE.randomx_release_cache(newCache);
    }

    @Test
    public void testRandomx_release_cache() {
        int length = 32;
        int flags = RandomXFlag.JIT.getValue();
        Pointer newCache = INSTANCE.randomx_alloc_cache(flags);
        assertNotNull(newCache, "randomx alloc cache ");

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);

        INSTANCE.randomx_init_cache(newCache, memory, new NativeSize(length));
        INSTANCE.randomx_release_cache(newCache);
    }

    @Test
    public void testRandomx_create_vm() {
        int length = 32;
        int flags = RandomXFlag.JIT.getValue();
        assertNotEquals(0, flags);

        Pointer cache = INSTANCE.randomx_alloc_cache(flags);
        Pointer dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(length));
        RandomXWrapper randomXWrapper = RandomXWrapper.builder().build();
        RandomXVM vm = new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), randomXWrapper);
        assertNotNull(vm);

        INSTANCE.randomx_release_cache(cache);
        INSTANCE.randomx_release_dataset(dataset);
        INSTANCE.randomx_destroy_vm(vm.getPointer());
    }

    @Test
    public void testRandomx_destroy_vm() {
        int length = 32;
        int flags = RandomXFlag.JIT.getValue();
        assertNotEquals(0, flags);

        Pointer cache = INSTANCE.randomx_alloc_cache(flags);
        Pointer dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(length));
        RandomXWrapper randomXWrapper = RandomXWrapper.builder().build();
        RandomXVM vm = new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), randomXWrapper);
        assertNotNull(vm);

        INSTANCE.randomx_release_cache(cache);
        INSTANCE.randomx_release_dataset(dataset);
        INSTANCE.randomx_destroy_vm(vm.getPointer());
    }

    @Test
    public void testRandomx_vm_set_cache() {
        int flags = RandomXFlag.JIT.getValue();
        Pointer cache = INSTANCE.randomx_alloc_cache(flags);
        Pointer dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        RandomXVM vm = createVM(flags, cache, dataset);
        assertNotNull(vm);

        RandomXJNA.INSTANCE.randomx_vm_set_cache(vm.getPointer(), cache);

        INSTANCE.randomx_release_cache(cache);
        INSTANCE.randomx_release_dataset(dataset);
        INSTANCE.randomx_destroy_vm(vm.getPointer());
    }

    @Test
    public void testRandomx_calculate_hash() {
        String key1 = "hello rx 1";
        String key2 = "hello world 1";
        byte[] key1Bytes = key1.getBytes();
        byte[] key2Bytes = key2.getBytes();

//        int flags = INSTANCE.randomx_get_flags() + RandomXFlag.LARGE_PAGES.getValue() + RandomXFlag.FULL_MEM.getValue();
        int flags = RandomXFlag.JIT.getValue();

        Pointer cache = INSTANCE.randomx_alloc_cache(flags);
        Pointer dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        Memory memory = new Memory(key1Bytes.length);
        memory.write(0, key1Bytes, 0, key1Bytes.length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(key1Bytes.length));
        INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());

        Memory msgPointer = new Memory(key2Bytes.length);
        Memory hashPointer = new Memory(RandomXUtils.HASH_SIZE);
        msgPointer.write(0, key2Bytes, 0, key2Bytes.length);

        RandomXVM vm = createVM(flags, cache, dataset);
        RandomXJNA.INSTANCE.randomx_calculate_hash(vm.getPointer(), msgPointer, new NativeSize(key2Bytes.length), hashPointer);
        byte[] hash = hashPointer.getByteArray(0, RandomXUtils.HASH_SIZE);

        HexFormat hex = HexFormat.of();
        assertEquals("781315d3e78dc16a5060cb87677ca548d8b9aabdef5221a2851b2cc72aa2875b", hex.formatHex(hash));

        INSTANCE.randomx_release_cache(cache);
        INSTANCE.randomx_release_dataset(dataset);
        INSTANCE.randomx_destroy_vm(vm.getPointer());
    }

    @Test
    public void testRandomx_alloc_dataset() {
        Pointer cache = INSTANCE.randomx_alloc_cache(0);
        assertNotNull(cache);
        INSTANCE.randomx_release_cache(cache);
    }

    @Test
    public void testRandomx_init_dataset() {
        int flags = RandomXFlag.JIT.getValue();
        byte[] seed = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4};
        Memory memory = new Memory(seed.length);
        memory.write(0, seed, 0, seed.length);
        Pointer cache = INSTANCE.randomx_alloc_cache(flags);
        Pointer dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(seed.length));
        INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());

        INSTANCE.randomx_release_cache(cache);
        INSTANCE.randomx_release_dataset(dataset);
    }

    @Test
    public void testRandomx_vm_set_dataset() {

    }

    @Test
    public void testRandomx_dataset_item_count() {
        assertNotNull(RandomXJNA.INSTANCE.randomx_dataset_item_count());
    }

    @Test
    public void testRandomx_release_dataset() {
    }

    private RandomXVM createVM(int flags, Pointer cache, Pointer dataset) {
        return new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), null);
    }

}
