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

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

public class RandomXWrapperTest {

    @Test
    public void testInit() {
        String key1 = "hello rx 1";
        String key2 = "hello world 1";
        byte[] key1Bytes = key1.getBytes(StandardCharsets.UTF_8);
        byte[] key2Bytes = key2.getBytes(StandardCharsets.UTF_8);

        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
                .fastInit(true)
                .build();
        randomXWrapper.init(key1Bytes);
        RandomXVM randomxVm = randomXWrapper.createVM();
        byte[] hash = randomxVm.getHash(key2Bytes);

        assertEquals("781315d3e78dc16a5060cb87677ca548d8b9aabdef5221a2851b2cc72aa2875b", BaseEncoding.base16().lowerCase().encode(hash));
    }

    @Test
    public void testCreateVM() {
        String key = "hello xdagj";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
                .fastInit(true)
                .build();
        randomXWrapper.init(keyBytes);
        RandomXVM randomxVm = randomXWrapper.createVM();
        byte[] hash = randomxVm.getHash(keyBytes);

        assertEquals("33e17472f3f691252d1f28a2e945b990c5878f514034006df5a06a23dc1cada0", BaseEncoding.base16().lowerCase().encode(hash));
    }

    @Test
    public void testChangeKey() {
        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
                .fastInit(true)
                .build();
        randomXWrapper.init(keyBytes);
        RandomXVM randomxVm = randomXWrapper.createVM();
        byte[] hash = randomxVm.getHash(keyBytes);

        assertEquals("5d4155322b69284bf45fa8ac182384490a87c55a6af47b7e72558cafa8832bd9", BaseEncoding.base16().lowerCase().encode(hash));

        key = "world xdagj-native-randomx";
        keyBytes = key.getBytes(StandardCharsets.UTF_8);
        randomXWrapper.changeKey(key.getBytes(StandardCharsets.UTF_8));
        hash = randomxVm.getHash(keyBytes);

        assertEquals("3910d7b054df9ba920e2f7e103aa2c1fc4597b13d1793f1ab08c1c9c922709c0", BaseEncoding.base16().lowerCase().encode(hash));
    }

    @Test
    public void testDestroy() {
        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
                .fastInit(true)
                .build();
        byte[] cache = new byte[32];
        randomXWrapper.init(cache);
        randomXWrapper.createVM();

        byte[] buffer = new byte[32];
        randomXWrapper.changeKey(buffer);
        randomXWrapper.destroy();
    }

}
