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
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
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
