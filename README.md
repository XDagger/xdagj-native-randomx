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
cp -i librandomx.dll ../../src/main/resources/native/librandomx_windows_x86_64.dll
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
    <version>0.2.5</version>
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
- **CPU**: Apple M3 Pro (12 cores, 3.0-4.05 GHz)
- **RAM**: 36 GB LPDDR5
- **Test Method**: 3 runs per configuration, averaged
- **RandomX Version**: v1.2.1

#### 1. Core Performance Comparison (Mining Mode)

| Configuration | C++ (H/s) | Java JNA (H/s) | Ratio | Init Time | Notes |
|:--------------|:---------:|:--------------:|:-----:|:---------:|:------|
| `--mine --jit --secure --softAes --init 4 --nonces 1000` | **402** | **369** | **92%** | ~8s | **Recommended** |
| `--mine --jit --secure --softAes --init 1 --nonces 1000` | 397 | 366 | 92% | ~31s | Single-thread init |
| `--mine --jit --secure --softAes --init 2 --nonces 500` | 392 | 365 | 93% | ~15s | 2 threads |
| `--mine --jit --secure --softAes --init 8 --nonces 500` | 399 | 372 | 93% | ~4s | 8 threads |

**Key Insights**:
- Java consistently achieves **92-93% of C++ performance**
- Only **~8% overhead** from JNA abstraction layer - exceptional for JNA bindings
- Multi-threaded initialization dramatically reduces startup time (31s → 4s with 8 threads)
- Hash rate is stable regardless of init thread count

#### 2. Light Mode Performance (Verification)

| Configuration | C++ (H/s) | Java JNA (H/s) | Ratio | Memory | Notes |
|:--------------|:---------:|:--------------:|:-----:|:------:|:------|
| `--jit --secure --softAes --nonces 1000` | **18.8** | **19.1** | **102%** | 256 MB | **Zero overhead!** |

**Key Insights**:
- Java matches or slightly exceeds C++ in light mode
- **No measurable JNA overhead** in cache-only mode
- This proves the 8% overhead in mining mode comes from dataset access patterns, not JNA

#### 3. Impact of Sample Size (Warm-up Effect)

| Nonces | C++ (H/s) | Java JNA (H/s) | C++ Variance | Java Variance |
|:------:|:---------:|:--------------:|:------------:|:-------------:|
| 100 | 335 | 321 | ± 15 H/s | ± 20 H/s |
| 500 | 398 | 359 | ± 5 H/s | ± 8 H/s |
| 1000 | 396 | 361 | ± 3 H/s | ± 5 H/s |
| 2000 | 401 | 370 | ± 2 H/s | ± 3 H/s |

**Key Insights**:
- Both implementations show warm-up effects
- Java requires slightly larger sample size due to JVM JIT compilation
- **Recommendation**: Use at least 1000 nonces for reliable benchmarks
- 2000+ nonces for production performance testing

#### 4. JIT Compilation Impact

| Mode | C++ (H/s) | Java JNA (H/s) | C++ Speedup | Java Speedup |
|:-----|:---------:|:--------------:|:-----------:|:------------:|
| With JIT (`--jit --secure`) | 380 | 370 | **4.2x** | **12.8x** |
| Without JIT (interpreter) | 90 | 29 | 1.0x | 1.0x |

**Key Insights**:
- JIT is **absolutely critical** for performance
- Java shows larger speedup because it measures both JVM JIT + RandomX JIT
- C++ only has RandomX JIT
- **Never run without JIT in production**

#### 5. Initialization Thread Scaling

| Threads | C++ Init Time | Java Init Time | C++ Hash Rate | Java Hash Rate |
|:-------:|:-------------:|:--------------:|:-------------:|:--------------:|
| 1 | 31.2s | 31.3s | 397 H/s | 366 H/s |
| 2 | 15.4s | 15.5s | 392 H/s | 365 H/s |
| 4 | 7.9s | 8.2s | 386 H/s | 370 H/s |
| 8 | 4.3s | 4.6s | 399 H/s | 372 H/s |

**Key Insights**:
- Init time scales almost linearly with thread count
- **Java and C++ init times are virtually identical**
- Hash rate is unaffected by thread count (within measurement error)
- **Recommendation**: Use 4-8 threads for optimal startup time

#### 6. Memory Configuration

| Mode | Memory Size | Configurable? | Java Performance | C++ Performance |
|:-----|:-----------:|:-------------:|:----------------:|:---------------:|
| Light Mode | 256 MB | ❌ No (Fixed) | 19.1 H/s | 18.8 H/s |
| Mining Mode | 2,080 MB | ❌ No (Fixed) | 369 H/s | 402 H/s |
| JVM Heap (Java only) | 512 MB - 4 GB | ✅ Yes | 365-366 H/s | N/A |

**Key Insights**:
- RandomX memory size is **algorithm-defined, not configurable**
- Cannot improve performance by allocating more memory
- JVM heap size has **no impact** on performance (core computation in native layer)
- Light mode: 256 MB cache only
- Mining mode: 256 MB cache + 2,048 MB dataset = 2,080 MB total

#### 7. Performance Summary

**Mining Mode (Full Dataset - Recommended for Production)**
- **C++ Performance**: ~402 H/s (baseline)
- **Java Performance**: ~369 H/s (92% of C++)
- **Absolute Difference**: -33 H/s
- **JNA Overhead**: 8% (exceptional for JNA-based bindings)

**Light Mode (Cache Only - For Verification)**
- **C++ Performance**: ~19 H/s (baseline)
- **Java Performance**: ~19 H/s (100% of C++)
- **Absolute Difference**: +0.3 H/s
- **JNA Overhead**: 0% (no measurable overhead!)

#### 8. Performance by Platform

| Platform | CPU | Mode | Java H/s | C++ H/s | Ratio | Config |
|:---------|:----|:-----|:--------:|:-------:|:-----:|:-------|
| **macOS** | Apple M3 Pro | Mining | 369 | 402 | 92% | JIT+SECURE+softAES, 4 threads |
| **macOS** | Apple M3 Pro | Light | 19 | 19 | 100% | JIT+SECURE+softAES |
| **Linux** | AMD EPYC 9754 | Mining (batch) | ~1819 | N/A | - | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Mining (no batch) | ~2192 | N/A | - | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Light (batch) | ~416 | N/A | - | HARD_AES+JIT, 8 threads |
| **Linux** | AMD EPYC 9754 | Light (no batch) | ~425 | N/A | - | HARD_AES+JIT, 8 threads |

#### 9. What Affects Performance?

| Factor | Impact on Performance | Notes |
|:-------|:---------------------:|:------|
| **CPU Speed** | ✅ **High** | Higher frequency = better performance |
| **CPU Cache** | ✅ **High** | Larger L3 cache helps with random memory access |
| **Memory Bandwidth** | ✅ **Medium** | Faster memory (DDR4-3200 vs DDR4-2400) helps |
| **JIT Compilation** | ✅ **Critical** | 4-13x speedup, must enable `--jit` |
| **Sample Size (nonces)** | ⚠️ **Warm-up only** | Larger sample = more accurate measurement |
| **Init Threads** | ⚠️ **Startup only** | More threads = faster startup, no hash rate impact |
| **JVM Heap Size** | ❌ **None** | 512MB to 4GB shows identical performance |
| **Allocating More Memory** | ❌ **Impossible** | Memory size is algorithm-fixed (256MB or 2080MB) |

#### 10. Performance Optimization Recommendations

**For Best Performance:**
```bash
# Mining mode (production)
./run-benchmark.sh --mine --jit --secure --softAes --init 4 --nonces 2000

# Configuration breakdown:
# --mine           : Use 2080 MB dataset for maximum speed
# --jit            : Enable JIT compilation (~4-13x faster)
# --secure         : Required for macOS ARM64, W^X compliance
# --softAes        : Software AES (more compatible than hardAes)
# --init 4         : 4 threads for dataset initialization (fast startup)
# --nonces 2000    : Large sample for accurate measurement
```

**What NOT to Do:**
- ❌ Don't allocate more memory (not possible, size is fixed)
- ❌ Don't increase JVM heap beyond default (no benefit)
- ❌ Don't run without JIT (12x slower)
- ❌ Don't use small sample sizes for benchmarking (< 1000 nonces)

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
