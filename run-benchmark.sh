#!/bin/bash
# Convenience script to run Benchmark with proper classpath

# Auto-detect JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    # Try to find Java 21+
    if command -v java &> /dev/null; then
        JAVA_CMD="java"
    else
        echo "Error: Java not found. Please set JAVA_HOME or add java to PATH"
        exit 1
    fi
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

CP=$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)

$JAVA_CMD \
    -cp "target/test-classes:target/classes:$CP" \
    io.xdag.crypto.randomx.Benchmark "$@"
