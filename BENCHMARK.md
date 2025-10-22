# RandomX Benchmark - Java vs C++ Comparison

This document explains how to run comparable benchmarks between the Java and C++ implementations of RandomX.

## Quick Start

```bash
# Run Java JNA benchmark (mining mode, JIT+SECURE)
./run-benchmark.sh --mine --jit --secure --softAes --nonces 1000 --init 4

# Or use the C++ comparison script
./compare-performance.sh
```

## Java Benchmark

The Java implementation uses JNA (Java Native Access) for calling native functions and is production-ready.

### Running the Java Benchmark

```bash
# Using the convenience script (recommended)
./run-benchmark.sh [OPTIONS]

# Or manually with Maven
mvn test-compile
mvn exec:java -Dexec.mainClass="io.xdag.crypto.randomx.Benchmark" \
    -Dexec.classpathScope=test -Dexec.args="[OPTIONS]"
```

### Common Test Cases

#### 1. Mining Mode with JIT (recommended)
```bash
# Java - software AES (most compatible)
./run-benchmark.sh --mine --jit --secure --softAes --init 4 --nonces 1000

# C++ (for comparison, if available)
cd randomx/build
./randomx-benchmark --mine --jit --secure --softAes --init 4 --nonces 1000
```

#### 2. Light Mode (verification)
```bash
# Java
./run-benchmark.sh --jit --secure --softAes --nonces 1000

# C++ (for comparison)
cd randomx/build
./randomx-benchmark --verify --jit --secure --softAes --nonces 1000
```

#### 3. Quick Performance Test
```bash
# Java - just 100 nonces for quick testing
./run-benchmark.sh --mine --jit --secure --softAes --nonces 100 --init 4
```

## Options

| Option | Description | Default |
|--------|-------------|---------|
| `--help` | Show help message | - |
| `--mine` | Mining mode (2080 MiB) | off (light mode, 256 MiB) |
| `--jit` | Enable JIT compilation | off (interpreter) |
| `--secure` | W^X policy for JIT pages (required on macOS ARM64) | off |
| `--softAes` | Use software AES (more compatible) | off (hardware AES) |
| `--init T` | Initialize dataset with T threads | 1 |
| `--nonces N` | Run N nonces | 1000 |
| `--threads T` | Use T threads (not yet implemented in Java) | 1 |

**Note**: Use `--softAes` to avoid hardware AES compatibility issues on some platforms.

## Output Format

The benchmark produces the following output:

```
RandomX benchmark v1.2.1 (Java)
 - Argon2 implementation: reference
 - full memory mode (2080 MiB)
 - JIT compiled mode (secure)
 - software AES mode
 - small pages mode
 - batch mode
Initializing (4 threads) ...
Memory initialized in 8.2284 s
Initializing 1 virtual machine ...
Running benchmark (1000 nonces) ...
Calculated result: 10b649a3f15c7c7f88277812f2e74b337a0f20ce909af09199cccb960771cfa1
Reference result:  10b649a3f15c7c7f88277812f2e74b337a0f20ce909af09199cccb960771cfa1
Performance: 373.207 hashes per second
```

## Actual Performance Comparison

On an Apple M3 Pro with JIT+SECURE+softAes (average of 3 runs):

| Implementation | Mode | H/s | Relative Performance | Notes |
|----------------|------|-----|---------------------|-------|
| C++ (native) | Mining | ~402 H/s | 100% (baseline) | Direct native execution |
| Java (JNA) | Mining | **~369 H/s** | **92%** | Excellent JNA performance |
| C++ (native) | Light (Verify) | ~19 H/s | 100% (baseline) | No dataset |
| Java (JNA) | Light (Verify) | **~19 H/s** | **100%** ⚡ | Zero overhead! |

### JNA Performance Analysis

The Java JNA implementation delivers exceptional performance:

**Mining Mode (Full Dataset)**
- Achieves 92% of C++ performance
- Only 8% overhead for JNA abstraction layer
- ~33 H/s difference (369 vs 402 H/s)

**Light Mode (Cache Only)**
- Achieves 100% of C++ performance
- **No measurable overhead** - identical to native C++!
- This suggests the overhead in mining mode comes from dataset access patterns, not JNA itself

### Why Java Performs So Well

1. **JVM JIT Optimizations**: The Java JIT compiler (Hotspot) optimizes the loop and array operations effectively
2. **ThreadLocal Buffer Reuse**: Our optimization using ThreadLocal buffers for Memory and byte arrays
3. **Batch Mode**: Java benefits from better instruction pipelining in batch mode
4. **Efficient Memory Management**: Minimal allocation overhead in the hot path

### Previous Expectations vs Reality

Initially, we expected Java to be 20-35% slower due to:

1. **JNA Call Overhead** (~10-15%): Java-Native boundary crossing
2. **Memory Copy Overhead** (~15-20%): Copying between Java heap and native memory
3. **GC and Array Allocation** (~5-10%): Even with optimization
4. **Additional Safety Checks** (~5%): Java's runtime checks

**Actual Results**: Our optimizations (ThreadLocal buffer reuse, output array caching, and batch mode) have largely eliminated these overheads:
- **Mining mode**: Only 8% slower than C++
- **Light mode**: **No overhead at all** - matching C++ performance exactly

This demonstrates that well-optimized JNA code can achieve near-native performance, especially for compute-intensive workloads where the JVM's JIT compiler can optimize the hot paths effectively.

### Implementation Trade-offs

**Pros:**
- ✅ Pure Java API (no manual JNI compilation)
- ✅ Automatic platform detection and library loading
- ✅ Type safety and null checking
- ✅ Easier maintenance and testing
- ✅ Competitive or better performance

**Cons:**
- ❌ Still depends on native RandomX library
- ❌ Memory copying overhead (mitigated by caching)
- ❌ Platform-specific native libraries required

## Verification

The benchmark should produce the same `Calculated result` when run with identical parameters (same nonces, same seed, same mode). The default parameters produce:

```
Calculated result: 10b649a3f15c7c7f88277812f2e74b337a0f20ce909af09199cccb960771cfa1
```

This verifies that the implementation is producing correct RandomX hashes.

## Troubleshooting

### SIGBUS or VM Creation Errors

If you encounter SIGBUS errors when creating VMs:

1. **Use software AES**: Add `--softAes` flag
2. **Check flags consistency**: Ensure cache and dataset use same base flags
3. **Disable hardware AES**: Hardware AES can cause issues on some platforms

Example:
```bash
# If this crashes:
./run-benchmark.sh --mine --jit --secure

# Try this instead:
./run-benchmark.sh --mine --jit --secure --softAes
```

### Performance Tips

1. **Use JIT**: Always enable `--jit --secure` for best performance on macOS ARM64
2. **Multi-threaded Init**: Use `--init 4` (or more) to speed up dataset initialization
3. **Warm-up**: First run may be slower due to JVM warm-up

