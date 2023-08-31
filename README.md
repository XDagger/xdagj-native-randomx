# Welcome to xdagj-native-randomx

![](https://github.com/XDagger/xdagj-native-randomx/actions/workflows/maven.yml/badge.svg) ![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/XDagger/xdagj-native-randomx) ![GitHub](https://img.shields.io/github/license/XDagger/xdagj-native-randomx) ![GitHub issues](https://img.shields.io/github/issues/XDagger/xdagj-native-randomx)

## Donation address
XDAG：BkcVG4i1BfxUJLdNPdctaCReBoyn4j32d

Development is based on xdagj community-driven

## RandomX
RandomX is a proof-of-work (PoW) algorithm that is optimized for general-purpose CPUs. RandomX uses random code execution (hence the name) together with several memory-hard techniques to minimize the efficiency advantage of specialized hardware.

[RandomX](https://github.com/tevador/RandomX)

## Xdagj
The java version of xdag

[Xdagj](https://github.com/XDagger/xdagj)

## Xdagj-Native-RandomX
xdagj-native-randomx is an implements of RandomX use Java JNA.

## Install

### 1. system
```yaml
JDK   : v17
Maven : v3.9.1
```

### 2. compile

### 2.1 checkout 
```shell
git clone https://github.com/XDagger/xdagj-native-randomx.git
cd xdagj-native-randomx
git submodule init
git submodule update
```
### 2.2 compile randomx lib

#### 2.2.1 compile randomx share lib
```
cd randomx
mkdir build && cd build
cmake .. -DARCH=native -DBUILD_SHARED_LIBS=ON -DCMAKE_C_FLAGS="-fPIC"
make
cp -i librandomx.dylib ../../src/main/resources
overwrite ../../src/main/resources/librandomx.dylib? (y/n [n]) y
```

#### 2.2.2 compile java lib

```
cd ../../
mvn package
```

### 3. maven system dependency

```xml
<dependency>
    <groupId>io.xdag</groupId>
    <artifactId>xdagj-native-randomx</artifactId>
    <version>0.1.7</version>
</dependency>
```

### 4. example

```java
package io.xdag.crypto.randomx;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

public class Example {

        String key = "hello xdagj-native-randomx";
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
    
        // 1. build randomx jna wrapper
        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(List.of(RandomXFlag.JIT, RandomXFlag.HARD_AES, RandomXFlag.ARGON2))
                .fastInit(false)
                .miningMode(false)
                .build();
    
        byte[] seed = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4};
        // 2. init dataset or cache
            randomXWrapper.init(seed);
    
        // 3. create randomxVm
        RandomXVM randomxVm = randomXWrapper.createVM();
    
        // 4. calculate hash
        byte[] hash = randomxVm.getHash(keyBytes);
    
        // 5. print result
        HexFormat hex = HexFormat.of();
        System.out.println("message:" + key);
        System.out.println("hash:" + hex.formatHex(hash));
    }
}


```

### 5. benchmark

JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targetting the JVM

result of ubuntu:

```
system ：macOS 13.3.1
CPU    ：Intel(R) Core(TM) i7-9750H CPU @ 2.60GHz
RAM    ：16 GB 2667 MHz DDR4
```

|           Benchmark            | Mode  | Cnt  | Score  |  Error  | Units |
| :----------------------------: | :---: | :--: |:------:| :-----: | :---: |
| RandomXJNAPerformance.testHash | thrpt |  25  | 75.130 | ± 2.128 | ops/s |
| RandomXJNAPerformance.testHash | avgt  |  25  | 0.015  | ± 0.001 | s/op  |





