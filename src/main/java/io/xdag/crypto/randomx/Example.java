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

import java.nio.charset.StandardCharsets;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

public class Example {

    public static void main(String[] args) {
        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // 1. build randomx jna wrapper
        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXFlag.JIT))
                .fastInit(true)
                .build();

        // 2. init dataset or cache
        randomXWrapper.init(keyBytes);

        // 3. create randomxVm
        RandomXVM randomxVm = randomXWrapper.createVM();

        // 4. calculate hash
        byte[] hash = randomxVm.getHash(keyBytes);

        // 5. print result
        System.out.println("message:" + key);
        System.out.println("hash:" + BaseEncoding.base16().lowerCase().encode(hash));
    }
}
