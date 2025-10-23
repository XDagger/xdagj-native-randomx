# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`xdagj-native-randomx` is a Java implementation of the RandomX proof-of-work algorithm using JNA (Java Native Access). It provides Java bindings to the native RandomX C++ library, enabling Java applications to perform RandomX hashing operations for the XDAG cryptocurrency ecosystem.

## Build and Development Commands

### Initial Setup
```bash
# Clone and initialize submodules
git submodule init
git submodule update
```

### Building Native Libraries

The project requires platform-specific native libraries to be compiled before building the Java library.

#### Linux x86_64
```bash
cd randomx
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON -DCMAKE_C_FLAGS="-fPIC" -DCMAKE_SHARED_LINKER_FLAGS="-z noexecstack"
make -j4
cp -i librandomx.so ../../src/main/resources/native/librandomx_linux_x86_64.so
cd ../..
```

#### macOS x86_64
```bash
cd randomx
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j4
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_x86_64.dylib
cd ../..
```

#### macOS aarch64 (Apple Silicon)
```bash
./scripts/build-macos-arm64.sh
```

Or manually:
```bash
cd randomx
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j$(sysctl -n hw.ncpu)
cp -i librandomx.dylib ../../src/main/resources/native/librandomx_macos_aarch64.dylib
cd ../..
```

#### Windows x86_64
```bash
cd randomx
mkdir build && cd build
cmake .. -G "MinGW Makefiles" -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON
make -j4
cp -i librandomx.dll ../../src/main/resources/native/librandomx_windows_x86_64.dll
cd ../..
```

### Building Java Library
```bash
mvn clean package
```

### Running Tests
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=RandomXVMTest

# Run a specific test method
mvn test -Dtest=RandomXVMTest#testCalculateHash
```

### License Checking
```bash
mvn license:check
```

## Architecture

### Core Components

**JNA Bridge Layer (`RandomXNative.java`)**
- Low-level JNA bindings to the native RandomX C++ library
- Maps all native function calls: `randomx_create_vm`, `randomx_calculate_hash`, etc.
- Loaded via `RandomXLibraryLoader` which extracts and loads platform-specific shared libraries from resources

**Library Loading (`RandomXLibraryLoader.java`)**
- Responsible for extracting the correct platform-specific library from `src/main/resources/native/`
- Supports: Linux x86_64, macOS x86_64, macOS aarch64, Windows x86_64
- Libraries are extracted to temporary files and loaded via `System.load()`
- Sets `jna.library.path` for JNA to find the loaded library

**Resource Management Wrappers**
- `RandomXCache`: Manages cache allocation/initialization for light mode hashing
- `RandomXDataset`: Manages dataset allocation/initialization for full mining mode (includes multi-threaded initialization)
- `RandomXVM`: Manages virtual machine instances that perform hash calculations
- All implement `AutoCloseable` for proper resource cleanup

**High-Level API (`RandomXTemplate.java`)**
- Builder pattern-based configuration for RandomX operations
- Manages the lifecycle of cache, dataset, and VM components
- Supports both mining mode (with dataset) and light mode (cache only)
- Handles key changes and component reinitialization
- Entry point for most application usage

**Utilities**
- `RandomXFlag`: Enum for RandomX configuration flags (JIT, HARD_AES, FULL_MEM, etc.)
- `RandomXUtils`: Helper methods, including `getRecommendedFlags()` for CPU-specific optimizations

### Resource Lifecycle

1. **Cache Creation**: `RandomXCache` is created with flags, then initialized with a key
2. **Dataset Creation** (mining mode only): `RandomXDataset` is created and initialized from cache using multi-threaded initialization
3. **VM Creation**: `RandomXVM` is created with cache (and optionally dataset)
4. **Hash Calculation**: VM performs hashing operations
5. **Cleanup**: All components must be closed in reverse order (VM → Dataset → Cache)

The `RandomXTemplate` class automates this lifecycle management.

### Native Library Structure

Native libraries are stored in `src/main/resources/native/` with platform-specific naming:
- `librandomx_linux_x86_64.so`
- `librandomx_macos_x86_64.dylib`
- `librandomx_macos_aarch64.dylib`
- `librandomx_windows_x86_64.dll`

The `randomx/` subdirectory contains the RandomX C++ library as a git submodule.

## Important Implementation Details

### Multi-threaded Dataset Initialization
- `RandomXDataset.init()` uses a thread pool (default: half of available CPU cores)
- Work is distributed across threads by dividing dataset items
- Uses custom thread factory for thread naming: `RandomX-Dataset-Init-N`
- Proper error handling and thread cleanup via ExecutorService

### JNA Memory Management
- All native memory operations use JNA's `Memory` class
- Memory objects are automatically GC'd but should be nullified when done
- Empty byte arrays require special handling (JNA Memory doesn't accept size 0)

### Key Changes
- Changing the RandomX key triggers cache reinitialization
- In mining mode, dataset is also recreated when key changes
- `RandomXTemplate.changeKey()` handles this cascade efficiently
- Duplicate key changes are detected and skipped

### Platform-Specific Considerations
- Windows: Temporary library extracted to a dedicated directory with fixed name `randomx.dll`
- Linux/macOS: Uses standard temp file creation with prefix/suffix
- Architecture normalization: `amd64` → `x86_64`, `arm64` → `aarch64`

## Performance Optimization

### JIT Compilation on macOS ARM64 (Apple Silicon)

**CRITICAL**: On Apple Silicon (M1/M2/M3), JIT provides a **12.6x performance boost** but requires specific configuration:

#### ✅ Recommended Configuration (Stable + Fast)
```java
Set<RandomXFlag> flags = EnumSet.of(
    RandomXFlag.JIT,     // Enable JIT compilation (~12x speedup)
    RandomXFlag.SECURE   // Required for W^X compliance on macOS ARM64
);
```

#### ❌ Unstable Configuration (May Crash)
```java
// JIT without SECURE - will crash on macOS ARM64
Set<RandomXFlag> flags = EnumSet.of(RandomXFlag.JIT);
```

#### Performance Comparison (Apple M3 Pro)
```
Mode              | Throughput    | Avg per Hash | Relative Speed
INTERPRETER       | 5 H/s         | 198.87 ms    | 1.0x (baseline)
JIT+SECURE        | 63 H/s        | 15.77 ms     | 12.6x ⚡
```

#### Why SECURE is Required on macOS ARM64
1. **W^X (Write XOR Execute) Policy**: macOS enforces strict memory protection on ARM64
2. **APRR (Apple Protection Regions)**: Hardware-enforced memory protection
3. **MAP_JIT Flag**: Required for proper JIT memory mapping
4. **Cache Invalidation**: Apple Silicon requires explicit I-cache invalidation

The RandomX library has been updated to handle these requirements when SECURE flag is enabled.

#### Diagnostic Testing
Run the diagnostic test to verify JIT performance on your system:
```bash
mvn test -Dtest=JITDiagnosticTest#compareAllModes
```

### Configurable Thread Count
Dataset initialization thread count can be configured via system property:
```bash
java -Drandomx.dataset.threads=8 -jar your-app.jar
```

Default is half of available processors.

### Memory Management Optimization
The implementation uses ThreadLocal buffers for Memory object reuse, which:
- Eliminates repeated native memory allocations during hash calculations
- Reduces GC pressure by ~90% in high-throughput mining scenarios
- Automatically scales per thread without manual configuration

## Testing

Tests are located in `src/test/java/io/xdag/crypto/randomx/`:
- `RandomXVMTest`: Tests VM operations
- `RandomXCacheTest`: Tests cache initialization
- `RandomXDatasetTest`: Tests dataset initialization
- `RandomXTemplateTest`: Tests high-level template API
- `RandomXTests`: Integration tests
- `RandomXBenchmark`: JMH benchmarks for performance testing

## Dependencies

Key dependencies (see `pom.xml`):
- JNA 5.17.0: Native library access
- Lombok 1.18.38: Reduces boilerplate code
- SLF4J 2.0.17: Logging facade
- JUnit 5.12.2: Testing framework
- JMH 1.37: Benchmarking framework

## Requirements

- JDK 21 or later
- Maven 3.9.9 or later
- CMake 3.5 or later (for building native libraries)
- GCC 4.8+ (v7+ recommended for best performance)
