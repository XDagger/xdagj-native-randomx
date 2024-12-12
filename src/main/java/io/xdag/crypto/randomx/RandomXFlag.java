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
 * Enum representing the RandomX flags for configuring the RandomX algorithm behavior.
 * Each flag represents a specific feature or optimization that can be enabled.
 */
@Getter
public enum RandomXFlag {

    /**
     * Default flag with no special features enabled
     */
    DEFAULT(0),
    
    /**
     * Enables large memory pages for improved performance
     */
    LARGE_PAGES(1),
    
    /**
     * Enables hardware AES instructions for faster encryption
     */
    HARD_AES(2),
    
    /**
     * Uses full memory mode for increased security
     */
    FULL_MEM(4),
    
    /**
     * Enables Just-In-Time compilation for better performance
     */
    JIT(8),
    
    /**
     * Enables additional security features
     */
    SECURE(16),
    
    /**
     * Enables SSSE3 optimizations for Argon2 algorithm
     */
    ARGON2_SSSE3(32),
    
    /**
     * Enables AVX2 optimizations for Argon2 algorithm
     */
    ARGON2_AVX2(64),
    
    /**
     * Combined flag for all Argon2 optimizations
     */
    ARGON2(96);

    /**
     * The integer value associated with this flag
     */
    private final int value;

    /**
     * Constructs a new RandomXFlag with the specified value.
     *
     * @param value The integer value representing this flag
     */
    RandomXFlag(int value) {
        this.value = value;
    }

    /**
     * Converts an integer value into a set of corresponding RandomXFlags.
     * Each bit in the input value corresponds to a specific flag.
     *
     * @param flags The combined integer value of multiple flags
     * @return A set of RandomXFlag enums corresponding to the enabled bits
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
     * The resulting value has bits set corresponding to each flag in the set.
     *
     * @param flagSet The set of RandomXFlags to combine
     * @return The combined integer value representing all flags in the set
     */
    public static int toValue(Set<RandomXFlag> flagSet) {
        int result = 0;
        for (RandomXFlag flag : flagSet) {
            result |= flag.value;
        }
        return result;
    }
}