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