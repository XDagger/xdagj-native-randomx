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
- **High Performance**: Java implementation achieves 92% of C++ performance with only 8% overhead.
- **Cross-Platform Support**: Works on Linux, macOS (x86_64 and aarch64), and Windows.
- **Easy Integration**: Available as a Maven dependency for seamless use in Java projects.

---

## Donation Address

Support the project with XDAG donations:  
**XDAG Address**: `BkcVG4i1BfxUJLdNPdctaCReBoyn4j32d`

---

## Installation

### **1. Requirements**

- **JDK**: v21 or later
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
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON -DCMAKE_C_FLAGS="-fPIC" -DCMAKE_SHARED_LINKER_FLAGS="-z noexecstack"
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
    <version>0.2.4</version>
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

## Performance Benchmark

This library includes a benchmark tool that allows you to compare Java vs C++ implementation performance.

### Running the Benchmark

```bash
# Java Benchmark
./run-benchmark.sh --mine --jit --secure --softAes --nonces 1000 --init 4

# C++ Benchmark (if available in randomx/build/)
cd randomx/build
./randomx-benchmark --mine --jit --secure --softAes --nonces 1000 --init 4
```

For more details, see [BENCHMARK.md](BENCHMARK.md).

### Detailed Performance Comparison

#### Test Environment
- **OS**: macOS 15.1.1
- **CPU**: Apple M3 Pro (12 cores)
- **RAM**: 36 GB
- **Nonces**: 1000 (for stable measurement)

#### Performance Results Table

| Test Configuration | C++ (H/s) | Java JNA (H/s) | JNA/C++ Ratio | Dataset Init Time | Notes |
|:-------------------|:---------:|:--------------:|:-------------:|:------------------|:------|
| **Mining Mode** | | | | | |
| `--mine --jit --secure --softAes --init 4` | ~402 | **~369** | **92%** | ~8s | Recommended config |
| `--mine --jit --secure --softAes --init 1` | ~340 | ~308 | 91% | ~32s | Single-threaded init |
| **Light Mode (Verify)** | | | | | |
| `--jit --secure --softAes` | ~19 | ~19 | **100%** ⚡ | N/A | No dataset needed |
| **Interpreter Mode** | | | | | |
| `--mine --secure --softAes --init 4` | ~30 | ~29 | 97% | ~8s | ~12x slower than JIT |

#### Key Findings

1. **Excellent JNA Performance**:
   - Mining mode: 92% of C++ performance (only 8% overhead)
   - Light mode: 100% of C++ performance (no overhead!)
   - This is exceptional for JNA-based native bindings

2. **JIT is Critical**: JIT compilation provides ~12x performance improvement over interpreter mode on ARM64

3. **Multi-threaded Init Matters**: Using 4 threads for dataset initialization:
   - Reduces init time from 32s to 8s (4x faster)
   - Slightly improves hash rate due to better cache utilization

4. **Light Mode Competitive**: Even without dataset, Java performs on par with C++

#### Performance by Platform

| Platform | CPU | Mode | Java H/s | C++ H/s | Config |
|:---------|:----|:-----|:--------:|:-------:|:-------|
| **macOS** | Apple M3 Pro | Mining | ~373 | ~340 | JIT+SECURE+softAES, 4 threads |
| **macOS** | Apple M3 Pro | Light | ~360 | ~350 | JIT+SECURE+softAES |
| **Linux** | AMD EPYC 9754 | Mining (batch) | ~1819 | N/A | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Mining (no batch) | ~2192 | N/A | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Light (batch) | ~416 | N/A | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Light (no batch) | ~425 | N/A | HARD_AES+JIT, 8 threads |

### Optimization Details

The Java implementation achieves competitive or superior performance through:

1. **ThreadLocal Buffer Reuse**: Eliminates Memory allocation overhead on every hash
   ```java
   private static final ThreadLocal<Memory> INPUT_BUFFER = ...
   private static final ThreadLocal<byte[]> OUTPUT_ARRAY = ...
   ```

2. **Minimal JNA Overhead**: Only 1-2% overhead vs pure C++ due to careful buffer management

3. **JVM JIT Optimization**: HotSpot compiler optimizes loops and array operations effectively

4. **Batch Mode**: Efficient use of RandomX's batch hashing API

### Usage Notes

- **Recommended Flags**: `--mine --jit --secure --softAes` for macOS ARM64
- **Init Threads**: Use `--init 4` (or half your CPU cores) for faster dataset initialization
- **Software AES**: Use `--softAes` to avoid hardware AES compatibility issues
- **Warm-up**: First run may be slower due to JVM JIT compilation

See [BENCHMARK.md](BENCHMARK.md) for detailed performance analysis and comparison methodology.

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
