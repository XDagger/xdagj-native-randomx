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
 * Enumeration of flags used to configure RandomX behavior.
 * These flags control various aspects of RandomX operation including optimizations and security features.
 */
@Getter
public enum RandomXFlag {

    /**
     * Default configuration with no special features enabled.
     * This is the baseline configuration suitable for most use cases.
     */
    DEFAULT(0),
    
    /**
     * Enables large memory pages for improved performance.
     * When available, this can significantly improve memory access speed.
     * Note: Requires appropriate system configuration and permissions.
     */
    LARGE_PAGES(1),
    
    /**
     * Enables hardware AES instructions for faster encryption.
     * Uses CPU's built-in AES acceleration when available.
     * Falls back to software implementation if hardware support is not available.
     */
    HARD_AES(2),
    
    /**
     * Uses full memory mode for increased security.
     * Ensures all dataset memory is utilized, providing maximum security.
     * May impact performance compared to light memory mode.
     */
    FULL_MEM(4),
    
    /**
     * Enables Just-In-Time compilation for better performance.
     * Compiles RandomX programs to native code at runtime.
     * Requires executable memory permissions.
     */
    JIT(8),
    
    /**
     * Enables additional security features.
     * Implements extra protection against certain types of attacks.
     * May slightly reduce performance.
     */
    SECURE(16),
    
    /**
     * Enables SSSE3 optimizations for Argon2 algorithm.
     * Uses SSSE3 CPU instructions when available.
     * Improves Argon2 performance on supported processors.
     */
    ARGON2_SSSE3(32),
    
    /**
     * Enables AVX2 optimizations for Argon2 algorithm.
     * Uses AVX2 CPU instructions when available.
     * Provides best Argon2 performance on supported processors.
     */
    ARGON2_AVX2(64),
    
    /**
     * Combined flag for all Argon2 optimizations.
     * Enables both SSSE3 and AVX2 optimizations.
     * System will use the best available optimization level.
     */
    ARGON2(96);

    /**
     * The integer value associated with this flag.
     * Used in native function calls to specify configuration.
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
     * Note: This method handles the DEFAULT(0) flag specially since it has value 0.
     * For composite flags like ARGON2(96), individual component flags will also be included.
     *
     * @param flags The combined integer value of multiple flags
     * @return A set of RandomXFlag enums corresponding to the enabled bits.
     */
    public static Set<RandomXFlag> fromValue(int flags) {
        EnumSet<RandomXFlag> result = EnumSet.noneOf(RandomXFlag.class);

        // Special handling for DEFAULT(0) - only add if flags value is exactly 0
        if (flags == 0) {
            result.add(DEFAULT);
            return result;
        }

        // Check all other flags
        for (RandomXFlag flag : values()) {
            if (flag != DEFAULT && (flags & flag.value) == flag.value && flag.value != 0) {
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
     * @throws IllegalArgumentException if flagSet is null
     */
    public static int toValue(Set<RandomXFlag> flagSet) {
        if (flagSet == null) {
            throw new IllegalArgumentException("Flag set cannot be null");
        }
        
        return flagSet.stream()
                .mapToInt(RandomXFlag::getValue)
                .reduce(0, (a, b) -> a | b);
    }

}