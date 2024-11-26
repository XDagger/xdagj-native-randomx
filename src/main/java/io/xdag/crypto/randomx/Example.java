package io.xdag.crypto.randomx;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;

public class Example {

    public static void main(String[] args) {
        // Key to be hashed
        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // Get supported flags
        Set<RandomXFlag> supportedFlags = RandomXFlag.fromValue(RandomXUtils.getFlags());

        System.out.println(supportedFlags);

        Set<RandomXFlag> sss = Set.of(RandomXFlag.DEFAULT, RandomXFlag.HARD_AES, RandomXFlag.SECURE);

        // Initialize RandomXTemplate
        try (RandomXTemplate template = RandomXTemplate.builder()
                .miningMode(false)
                .flags(sss)
                .build()) {
            // Initialize the template with the key
            template.init(keyBytes);

            // Calculate hash
            byte[] hash = template.calculateHash(keyBytes);

            // Print the hash in hexadecimal format
            HexFormat hex = HexFormat.of();
            System.out.printf("Message: %s%n", key);
            System.out.printf("Hash: %s%n", hex.formatHex(hash));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}