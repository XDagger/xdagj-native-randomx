# Welcome to xdagj-native-randomx

![](https://github.com/XDagger/xdagj-native-randomx/actions/workflows/maven.yml/badge.svg) ![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/XDagger/xdagj-native-randomx) ![GitHub](https://img.shields.io/github/license/XDagger/xdagj-native-randomx) ![GitHub issues](https://img.shields.io/github/issues/XDagger/xdagj-native-randomx)

## Donation address
XDAG：+89Zijf2XsXqbdVK7rdfR4F8+RkHkAPh

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
Maven : v3.8.3
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

#### 2.2.1 change cmake setting

```
cd randomx
vim CMakeLists.txt
```
modify CMakeList.txt in randomx folder.
change "add_library(randomx ${randomx_sources})" at line 178 to
"add_library(randomx SHARED ${randomx_sources})"

#### 2.2.2 compile c++ lib
```
mkdir build && cd build
cmake -DARCH=native ..
make
cp -i librandomx.dylib ../../src/main/resources
overwrite ../../src/main/resources/librandomx.dylib? (y/n [n]) y
```

#### 2.2.3 compile java lib

```
cd ../../
mvn package
```

### 3. maven system dependency

```xml
<dependency>
    <groupId>io.xdag</groupId>
    <artifactId>xdagj-native-randomx</artifactId>
    <version>0.1.3</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/xdagj-native-randomx-0.1.3.jar</systemPath>
</dependency>
```

### 4. example

```java
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

```

### 5. benchmark

JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targetting the JVM

```shell
mvn package
java -jar target/benchmarks.jar RandomXJNAPerformance
```

result of ubuntu:

```
system ：Ubuntu 22.04.1 LTS
CPU    ：Intel(R) Xeon(R) CPU E5-2640 v3 @ 2.60GHz   2.60 GHz
RAM    ：32.0 GB
```

|           Benchmark            | Mode  | Cnt  | Score  |  Error  | Units |
| :----------------------------: | :---: | :--: | :----: | :-----: | :---: |
| RandomXJNAPerformance.testHash | thrpt |  25  | 35.683 | ± 1.081 | ops/s |
| RandomXJNAPerformance.testHash | avgt  |  25  | 0.027  | ± 0.001 | s/op  |





