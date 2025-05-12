#!/bin/bash

# Script to build RandomX for Apple Silicon (M series chips)
# This script should be run on an Apple Silicon Mac

# Check if running on ARM Mac
if [[ $(uname -m) != "arm64" ]]; then
  echo "❌ This script must be run on an Apple Silicon Mac (M1/M2/M3)"
  exit 1
fi

echo "🔍 Building RandomX library for Apple Silicon (ARM64)..."

# Navigate to randomx directory
cd "$(dirname "$0")/../randomx" || exit 1

# Create build directory if it doesn't exist
mkdir -p build
cd build || exit 1

echo "🔧 Running CMake..."
cmake .. -DCMAKE_BUILD_TYPE=Release -DARCH=native -DBUILD_SHARED_LIBS=ON

echo "🔨 Compiling RandomX..."
make -j$(sysctl -n hw.ncpu)

# Create native resources directory if it doesn't exist
mkdir -p ../../src/main/resources/native

echo "📦 Copying library to resources directory..."
cp -vf librandomx.dylib ../../src/main/resources/native/librandomx_macos_aarch64.dylib

echo "✅ Build complete!"
echo "Library location: src/main/resources/native/librandomx_macos_aarch64.dylib"
echo ""
echo "Please commit this file to your repository before pushing to GitHub."
echo "This will allow GitHub Actions to package it with the other platform libraries." 