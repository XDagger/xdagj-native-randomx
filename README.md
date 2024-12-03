# Welcome to xdagj-native-randomx

![Build Status](https://github.com/XDagger/xdagj-native-randomx/actions/workflows/maven.yml/badge.svg)
![Latest Release](https://img.shields.io/github/v/release/XDagger/xdagj-native-randomx)
![License](https://img.shields.io/github/license/XDagger/xdagj-native-randomx)
![Issues](https://img.shields.io/github/issues/XDagger/xdagj-native-randomx)

---

## Introduction

`xdagj-native-randomx` is a Java implementation of the RandomX proof-of-work algorithm using JNA (Java Native Access). This library is a community-driven project based on the [xdagj](https://github.com/XDagger/xdagj) ecosystem.

### **What is RandomX?**

RandomX is a proof-of-work (PoW) algorithm optimized for general-purpose CPUs. It uses random code execution and memory-hard techniques to minimize the efficiency advantage of specialized hardware like ASICs.

For more details, visit the [RandomX GitHub repository](https://github.com/tevador/RandomX).

---

## Features

- **Native Integration**: Leverages RandomX's native C++ library via JNA.
- **Cross-Platform Support**: Works on Linux, macOS (x86_64 and aarch64), and Windows.
- **Easy Integration**: Available as a Maven dependency for seamless use in Java projects.

---

## Donation Address

Support the project with XDAG donations:  
**XDAG Address**: `BkcVG4i1BfxUJLdNPdctaCReBoyn4j32d`

---

## Installation

### **1. Requirements**

- **JDK**: v17 or later
- **Maven**: v3.9.9 or later

### **2. Build Steps**

#### **2.1 Clone the Repository**
```bash
git clone https://github.com/XDagger/xdagj-native-randomx.git
cd xdagj-native-randomx
git submodule init
git submodule update
```

#### **2.2 Compile RandomX Native Library**

Compile and copy the appropriate shared library for your platform:

##### **Linux x86_64**
```bash
cd randomx
mkdir build && cd build
cmake .. -DARCH=x86_64 -DBUILD_SHARED_LIBS=ON -DCMAKE_C_FLAGS="-fPIC"
make
cp -i librandomx.so ../../src/main/resources/native/librandomx_linux_x86_64.so
```

##### **macOS x86_64**
```bash
cd randomx
mkdir build && cd build
cmake .. -DARCH=x86_64 -DBUILD_SHARED_LIBS=ON
make
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_x86_64.dylib
```

##### **macOS aarch64**
```bash
cd randomx
mkdir build && cd build
cmake .. -DARCH=arm64 -DBUILD_SHARED_LIBS=ON
make
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_aarch64.dylib
```

##### **Windows x86_64**
```bash
cd randomx
mkdir build && cd build
cmake .. -G "MinGW Makefiles" -DARCH=native -DBUILD_SHARED_LIBS=ON
make
cp -i randomx.dll ../../src/main/resources/native/librandomx_windows_x86_64.dll
```

#### **2.3 Compile Java Library**
```bash
cd ../../
mvn clean package
```

### **3. Add Maven Dependency**

To include `xdagj-native-randomx` in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.xdag</groupId>
    <artifactId>xdagj-native-randomx</artifactId>
    <version>0.1.8</version>
</dependency>
```

---

## Usage Example

```java
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
        Set<RandomXFlag> flags = RandomXUtils.getFlagsSet();

        // Initialize RandomXTemplate
        try (RandomXTemplate template = RandomXTemplate.builder()
                .miningMode(false)
                .flags(flags)
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
```

---

## Benchmark Results

### Linux System Configuration
- **OS**: Linux 5.4.119-19.0009.44 x86_64
- **CPU**: AMD EPYC 9754 (16 cores)
- **RAM**: 32 GB

### Linux Performance Results
|           Benchmark            | Mode  | Cnt | Score   | Error  | Units |
|:------------------------------:|:-----:|:---:|:-------:|:------:|:-----:|
| RandomXJNAPerformance.testCalculateHash | thrpt | 25  | 38.793  | ±1.132 | ops/s |
| RandomXJNAPerformance.testCalculateHash | avgt  | 25  | 0.027   | ±0.001 | s/op  |

---

### MacOS System Configuration
- **OS**: macOS 15.1.1
- **CPU**: Apple M3 Pro
- **RAM**: 36 GB

### MacOS Performance Results
|           Benchmark            | Mode  | Cnt | Score   | Error  | Units |
|:------------------------------:|:-----:|:---:|:-------:|:------:|:-----:|
| RandomXJNAPerformance.testCalculateHash | thrpt | 25  | 5.281  | ±0.118 | ops/s |
| RandomXJNAPerformance.testCalculateHash | avgt  | 25  | 0.199   | ±0.006 | s/op  |

---

## Contribution

We welcome contributions to improve the project! Please feel free to submit issues or pull requests.

For discussions or questions, contact the [xdagj community](https://github.com/XDagger/xdagj).

---