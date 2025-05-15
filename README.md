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
- **CMake**: v3.5 or later
- **GCC/Compiler**: GCC v4.8 or later (v7+ recommended for best performance)

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
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON -DCMAKE_C_FLAGS="-fPIC"
make -j4
cp -i librandomx.so ../../src/main/resources/native/librandomx_linux_x86_64.so
```

##### **macOS x86_64**
```bash
cd randomx
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j4
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_x86_64.dylib
```

##### **macOS aarch64 (Apple Silicon)**
For Apple Silicon Macs (M1, M2, M3), use the provided script:
```bash
# Run from the project root
./scripts/build-macos-arm64.sh
```

Or manually:
```bash
cd randomx
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j$(sysctl -n hw.ncpu)
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_aarch64.dylib
```

##### **Windows x86_64**
```bash
cd randomx
mkdir build && cd build
cmake .. -G "MinGW Makefiles" -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j4
cp -i randomx.dll ../../src/main/resources/native/librandomx_windows_x86_64.dll
```
You can also compile using Visual Studio, as the official RandomX repository provides solution files.

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
    <version>0.2.2</version>
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

        // Get supported RandomX flags for the current CPU
        Set<RandomXFlag> flags = RandomXUtils.getRecommendedFlags();
        System.out.println("Supported flags: " + flags);

        // Initialize RandomX cache (key will be set via template)
        RandomXCache cache = new RandomXCache(flags);

        // Create and configure RandomXTemplate using builder pattern
        byte[] hash;
        try (RandomXTemplate template = RandomXTemplate.builder()
                .cache(cache)
                .miningMode(false)
                .flags(flags)
                .build()) {

            // Set the key for RandomX operations. This will initialize the cache.
            template.changeKey(keyBytes);

            // Initialize the template's VM with the configured settings
            template.init();

            // Calculate hash of the input
            hash = template.calculateHash(keyBytes);
        }

        // Format and display the results
        HexFormat hex = HexFormat.of();
        System.out.printf("Message: %s%n", key);
        System.out.printf("Hash: %s%n", hex.formatHex(hash));
    }
}
```

---

## Benchmark Results

### Linux System Configuration
- **OS**: Linux 5.4.119
- **CPU**: AMD EPYC 9754 (16 cores)
- **RAM**: 32 GB
- **thread**: 8
- **RandomX Flags**: [DEFAULT, HARD_AES, JIT, ARGON2_SSSE3, ARGON2_AVX2, ARGON2]

### Linux Performance Results
|           Benchmark            | Mode  | Cnt | Score   | Error  | Units |
|:------------------------------:|:-----:|:---:|:-------:|:------:|:-----:|
| RandomXBenchmark.lightBatch    | thrpt |   | 416.114  |  | ops/s |
| RandomXBenchmark.lightNoBatch  | thrpt  |   | 424.865   |  | ops/s  |
| RandomXBenchmark.miningBatch   | thrpt  |   | 1818.991   |  | ops/s  |
| RandomXBenchmark.miningNoBatch | thrpt  |   | 2191.774   |  | ops/s  |

---

### MacOS System Configuration
- **OS**: macOS 15.1.1
- **CPU**: Apple M3 Pro
- **RAM**: 36 GB
- **thread**: 8
- **RandomX Flags**: [DEFAULT, JIT, SECURE]

### MacOS Performance Results
|           Benchmark            | Mode  | Cnt | Score    | Error  | Units |
|:------------------------------:|:-----:|:---:|:--------:|:------:|:-----:|
| RandomXBenchmark.lightBatch    | thrpt |   | 416.114  |        | ops/s |
| RandomXBenchmark.lightNoBatch  | thrpt |   | 424.865  |        | ops/s |
| RandomXBenchmark.miningBatch   | thrpt |   | 1818.991 |        | ops/s |
| RandomXBenchmark.miningNoBatch | thrpt |   | 2191.774 |        | ops/s |

---

## Contribution

We welcome contributions to improve the project! Please feel free to submit issues or pull requests.

### **Contributing to the library**

If you're submitting changes that might affect the native libraries:

1. For platform-specific changes, compile the native library for your platform:
   * Linux x86_64, macOS x86_64, or Windows x86_64: Follow the compilation steps above.
   * macOS aarch64 (Apple Silicon): Must be compiled on an Apple Silicon Mac using provided script.

2. Commit both your code changes and the updated native library files.

3. GitHub Actions will build for other platforms and generate a complete multi-platform JAR.

For discussions or questions, contact the [xdagj community](https://github.com/XDagger/xdagj).

---
