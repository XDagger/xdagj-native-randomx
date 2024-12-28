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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.util.Set;

/**
 * Utility class for RandomX constants and helper methods.
 * This class provides static methods to get RandomX flags and flag sets.
 */
public final class RandomXUtils {

    /**
     * Gets the recommended RandomX flags for the current CPU.
     * This method calls the native RandomX library to determine optimal flags.
     *
     * @return An integer representing the combined RandomX flags
     */
    public static int getFlags() {
        return RandomXJNALoader.getInstance().randomx_get_flags();
    }

    /**
     * Gets a set of RandomX flags appropriate for the current system.
     * This method converts the raw flags to a Set of RandomXFlag enums and
     * applies platform-specific adjustments (e.g., removing JIT flag on macOS).
     *
     * @return A Set of RandomXFlag enums representing the enabled flags
     */
    public static Set<RandomXFlag> getFlagsSet() {
        int flags = getFlags();
        Set<RandomXFlag> flagsSet = RandomXFlag.fromValue(flags);
        if (SystemUtils.IS_OS_MAC_OSX && StringUtils.containsIgnoreCase(SystemUtils.OS_ARCH, "aarch64")) {
            flagsSet.remove(RandomXFlag.JIT);
        }

        return flagsSet;
    }

}