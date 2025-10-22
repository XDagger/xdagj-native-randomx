#!/bin/bash
# Convenience script to run Benchmark with proper classpath

JAVA_HOME="/Users/reymondtu/Library/Java/JavaVirtualMachines/jdk-21.0.5.jdk/Contents/Home"
CP=$(JAVA_HOME="$JAVA_HOME" mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)

"$JAVA_HOME/bin/java" \
    -cp "target/test-classes:target/classes:$CP" \
    io.xdag.crypto.randomx.Benchmark "$@"
