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

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.PointerByReference;
import java.util.HexFormat;

import org.junit.jupiter.api.Test;

public class RandomXVMTest {

    @Test
    public void testGetHash() {
        String key1 = "hello rx 1";
        String key2 = "hello world 1";
        byte[] key1Bytes = key1.getBytes();
        byte[] key2Bytes = key2.getBytes();

        int flags = RandomXFlag.JIT.getValue();

        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        Memory memory = new Memory(key1Bytes.length);
        memory.write(0, key1Bytes, 0, key1Bytes.length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(key1Bytes.length));
        INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());

        RandomXVM vm = createVM(flags, cache, dataset);
        byte[] hash = vm.getHash(key2Bytes);

        HexFormat hex = HexFormat.of();
        assertEquals("781315d3e78dc16a5060cb87677ca548d8b9aabdef5221a2851b2cc72aa2875b", hex.formatHex(hash));
    }

    @Test
    public void testDestroy() {
        int flags = RandomXFlag.JIT.getValue();
        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        RandomXVM vm = createVM(flags, cache, dataset);
        vm.destroy();
    }

    private RandomXVM createVM(int flags, PointerByReference cache, PointerByReference dataset) {
        return new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), null);
    }

}
