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

import java.util.Arrays;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.junit.Test;

public class RandomXVMTest {

    @Test
    public void testGetHash() {
        int length = 32;
        byte[] message = new byte[32];
        int flags = RandomXJNA.INSTANCE.randomx_get_flags();
        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        Pointer msgPointer = new Memory(message.length);
        Pointer hashPointer = new Memory(RandomXUtils.HASH_SIZE);

        RandomXVM vm = createVM(flags, cache, dataset, length);
        byte[] hash = vm.getHash(message);
        System.out.println(Arrays.toString(hash));
        msgPointer.clear(message.length);
        hashPointer.clear(RandomXUtils.HASH_SIZE);
    }

    @Test
    public void testDestroy() {
        int length = 32;
        int flags = RandomXJNA.INSTANCE.randomx_get_flags();
        PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
        PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

        RandomXVM vm = createVM(flags, cache, dataset, length);
        vm.destroy();
    }

    private RandomXVM createVM(int flags, PointerByReference cache, PointerByReference dataset, int length) {
        byte[] buffer = new byte[length];
        Memory memory = new Memory(length);
        memory.write(0, buffer, 0, length);
        INSTANCE.randomx_init_cache(cache, memory, new NativeSize(length));
        return new RandomXVM(RandomXJNA.INSTANCE.randomx_create_vm(flags, cache, dataset), new RandomXWrapper.Builder().build());
    }

}
