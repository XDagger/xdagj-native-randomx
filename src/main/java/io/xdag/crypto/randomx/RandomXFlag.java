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

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing the RandomX flags.
 */
@Getter
public enum RandomXFlag {

    DEFAULT(0),
    LARGE_PAGES(1),
    HARD_AES(2),
    FULL_MEM(4),
    JIT(8),
    SECURE(16),
    ARGON2_SSSE3(32),
    ARGON2_AVX2(64),
    ARGON2(96);

    private final int value;

    RandomXFlag(int value) {
        this.value = value;
    }

    /**
     * Converts an integer value into a set of corresponding RandomXFlags.
     *
     * @param flags the combined integer value of multiple flags.
     * @return a set of RandomXFlag enums.
     */
    public static Set<RandomXFlag> fromValue(int flags) {
        EnumSet<RandomXFlag> result = EnumSet.noneOf(RandomXFlag.class);
        for (RandomXFlag flag : values()) {
            if ((flags & flag.value) == flag.value) {
                result.add(flag);
            }
        }
        return result;
    }

    /**
     * Converts a set of RandomXFlags into their combined integer value.
     *
     * @param flagSet the set of RandomXFlags.
     * @return the combined integer value.
     */
    public static int toValue(Set<RandomXFlag> flagSet) {
        int result = 0;
        for (RandomXFlag flag : flagSet) {
            result |= flag.value;
        }
        return result;
    }
}