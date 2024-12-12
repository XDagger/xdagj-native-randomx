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
    private static RandomXJNA instance;

    /**
     * Static initialization block to load the library when the class is loaded
     */
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
     * Thread-safe implementation using synchronized method.
     *
     * @return The singleton instance of RandomXJNA
     */
    public static synchronized RandomXJNA getInstance() {
        if (instance == null) {
            instance = Native.load("randomx", RandomXJNA.class);
        }
        return instance;
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
        String libFileName;

        if (os.contains("win")) {
            libFileName = "native/" + libraryName + "_windows_" + arch + ".dll";
        } else if (os.contains("mac")) {
            libFileName = "native/" + libraryName + "_macos_" + arch + ".dylib";
        }  else if (StringUtils.contains(os, "linux")) {
            if(StringUtils.containsAny(arch, "amd64", "x86_64")) {
                libFileName = "native/" + libraryName + "_linux_x86_64.so";
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        // Load from resources
        try (InputStream libStream = RandomXJNALoader.class.getClassLoader().getResourceAsStream(libFileName)) {
            if (libStream == null) {
                throw new IllegalStateException("Native library not found: " + libFileName);
            }
            File tempFile = File.createTempFile(libraryName, libFileName.substring(libFileName.lastIndexOf('.')));
            tempFile.deleteOnExit();
            Files.copy(libStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library: " + libraryName, e);
        }
    }
}