/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.crypto.randomx;

import com.sun.jna.Native;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A singleton class responsible for loading and managing the RandomX native library using JNA.
 * This class handles the platform-specific library loading and provides access to the RandomX native functions.
 */
public final class RandomXJNALoader {

    /**
     * The singleton instance of the RandomX JNA interface
     */
    private static volatile RandomXJNA instance;

    /**
     * Lock object for thread synchronization
     */
    private static final Object LOCK = new Object();

    /**
     * Private constructor to prevent instantiation
     */
    private RandomXJNALoader() {
        // Prevent instantiation
    }

    static {
        init();
    }

    /**
     * Initializes the RandomX native library
     */
    public static void init() {
        loadLibrary("librandomx");
    }

    /**
     * Gets or creates the singleton instance of the RandomX JNA interface.
     * Uses double-checked locking for thread safety and better performance.
     *
     * @return The singleton instance of RandomXJNA
     */
    public static RandomXJNA getInstance() {
        RandomXJNA result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = Native.load("randomx", RandomXJNA.class);
                }
            }
        }
        return result;
    }

    /**
     * Loads the native library for the current platform and architecture.
     * Supports Windows, macOS, and Linux (x86_64) platforms.
     * The library is extracted from resources to a temporary file before loading.
     *
     * @param libraryName The base name of the library to load
     * @throws UnsupportedOperationException if the current platform is not supported
     * @throws RuntimeException if the library loading fails
     */
    public static void loadLibrary(String libraryName) {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String libFileName = getLibraryFileName(libraryName, os, arch);

        // Load from resources
        try (InputStream libStream = getLibraryStream(libFileName)) {
            File tempFile = createTempLibraryFile(libraryName, libFileName);
            Files.copy(libStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libraryName, e);
        }
    }

    /**
     * Gets the platform-specific library file name.
     *
     * @param libraryName Base name of the library
     * @param os Operating system name
     * @param arch System architecture
     * @return The complete library file name
     * @throws UnsupportedOperationException if the platform is not supported
     */
    private static String getLibraryFileName(String libraryName, String os, String arch) {
        if (os.contains("win")) {
            return String.format("native/%s_windows_%s.dll", libraryName, arch);
        } else if (os.contains("mac")) {
            return String.format("native/%s_macos_%s.dylib", libraryName, arch);
        } else if (StringUtils.contains(os, "linux")) {
            if (StringUtils.containsAny(arch, "amd64", "x86_64")) {
                return String.format("native/%s_linux_x86_64.so", libraryName);
            }
        }
        throw new UnsupportedOperationException(
                String.format("Unsupported platform: OS=%s, Architecture=%s", os, arch));
    }

    /**
     * Gets the input stream for the library resource.
     *
     * @param libFileName Library file name
     * @return InputStream for the library resource
     * @throws IllegalStateException if the library resource is not found
     */
    private static InputStream getLibraryStream(String libFileName) {
        InputStream libStream = RandomXJNALoader.class.getClassLoader().getResourceAsStream(libFileName);
        if (libStream == null) {
            throw new IllegalStateException("Native library not found: " + libFileName);
        }
        return libStream;
    }

    /**
     * Creates a temporary file for the native library.
     *
     * @param libraryName Base name of the library
     * @param libFileName Complete library file name
     * @return Temporary File object
     * @throws RuntimeException if file creation fails
     */
    private static File createTempLibraryFile(String libraryName, String libFileName) {
        try {
            File tempFile = File.createTempFile(
                    libraryName, 
                    libFileName.substring(libFileName.lastIndexOf('.')));
            tempFile.deleteOnExit();
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary library file", e);
        }
    }
}