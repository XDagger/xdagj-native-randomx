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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RandomXTemplate class.
 */
public class RandomXTemplateTest {

    @Test
    public void testCalculateHash() {
        String key1 = "hello rx 1";
        String key2 = "hello world 1";
        String key3 = "hello xdagj";
        String key4 = "hello xdagj-native-randomx";

        byte[] key1Bytes = key1.getBytes(StandardCharsets.UTF_8);
        byte[] key2Bytes = key2.getBytes(StandardCharsets.UTF_8);
        byte[] key3Bytes = key3.getBytes(StandardCharsets.UTF_8);
        byte[] key4Bytes = key4.getBytes(StandardCharsets.UTF_8);

        Set<RandomXFlag> flagSet = RandomXUtils.getRecommendedFlags();
        RandomXCache cache = new RandomXCache(flagSet);
        cache.init(key1Bytes);

        HexFormat hex = HexFormat.of();

        RandomXTemplate template = RandomXTemplate.builder()
            .cache(cache)
            .miningMode(false)
            .flags(flagSet)
            .build();
        template.init();
        byte[] hash = template.calculateHash(key2Bytes);
        assertEquals("781315d3e78dc16a5060cb87677ca548d8b9aabdef5221a2851b2cc72aa2875b", hex.formatHex(hash));

        cache = new RandomXCache(flagSet);
        cache.init(key3Bytes);
        template = RandomXTemplate.builder()
            .cache(cache)
            .miningMode(false)
            .flags(flagSet)
            .build();
        template.init();
        hash = template.calculateHash(key3Bytes);
        assertEquals("33e17472f3f691252d1f28a2e945b990c5878f514034006df5a06a23dc1cada0", hex.formatHex(hash));

        cache = new RandomXCache(flagSet);
        cache.init(key4Bytes);
        template = RandomXTemplate.builder()
            .cache(cache)
            .miningMode(false)
            .flags(flagSet)
            .build();
        template.init();
        hash = template.calculateHash(key4Bytes);
        assertEquals("5d4155322b69284bf45fa8ac182384490a87c55a6af47b7e72558cafa8832bd9", hex.formatHex(hash));

    }

    @Test
    public void testChangeKey() {
        String key1 = "hello xdagj-native-randomx";
        byte[] key1Bytes = key1.getBytes(StandardCharsets.UTF_8);
        String key2 = "world xdagj-native-randomx";
        byte[] key2Bytes = key2.getBytes(StandardCharsets.UTF_8);

        Set<RandomXFlag> flagSet = RandomXUtils.getRecommendedFlags();
        RandomXCache cache = new RandomXCache(flagSet);
        cache.init(key1Bytes);

        HexFormat hex = HexFormat.of();

        RandomXTemplate template = RandomXTemplate.builder()
            .cache(cache)
            .miningMode(false)
            .flags(flagSet)
            .build();
        template.init();
        byte[] hash = template.calculateHash(key1Bytes);
        assertEquals("5d4155322b69284bf45fa8ac182384490a87c55a6af47b7e72558cafa8832bd9", hex.formatHex(hash));

        template.changeKey(key2Bytes);
        hash = template.calculateHash(key2Bytes);
        assertEquals("3910d7b054df9ba920e2f7e103aa2c1fc4597b13d1793f1ab08c1c9c922709c0", hex.formatHex(hash));
    }

}
