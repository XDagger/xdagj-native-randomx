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

```yaml
mvn package
```

### 3. example

```java
String key = "hello xdagj";
byte[] keyBytes = key.getBytes();

// 1. set flages 
int flags = INSTANCE.randomx_get_flags();

// 2. alloc cache and dataset 
PointerByReference cache = INSTANCE.randomx_alloc_cache(flags);
PointerByReference dataset = RandomXJNA.INSTANCE.randomx_alloc_dataset(flags);

// 3. init cache and dataset
Memory memory = new Memory(keyBytes.length);
memory.write(0, keyBytes, 0, keyBytes.length);
INSTANCE.randomx_init_cache(cache, memory, new NativeSize(keyBytes.length));
INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), RandomXJNA.INSTANCE.randomx_dataset_item_count());

// 4. alloc memory and set value
Pointer msgPointer = new Memory(keyBytes.length);
Pointer hashPointer = new Memory(RandomXUtils.HASH_SIZE);
msgPointer.write(0, keyBytes, 0, keyBytes.length);

// 5. create vm and calculate hash
RandomXVM vm = createVM(flags, cache, dataset);
RandomXJNA.INSTANCE.randomx_calculate_hash(vm.getPointer(), msgPointer, new NativeSize(keyBytes.length), hashPointer);

// 6. get hash value from memory
byte[] hash = hashPointer.getByteArray(0, RandomXUtils.HASH_SIZE);
```

