package io.xdag.crypto.randomx;

import lombok.ToString;

@ToString
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

    RandomXFlag(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
