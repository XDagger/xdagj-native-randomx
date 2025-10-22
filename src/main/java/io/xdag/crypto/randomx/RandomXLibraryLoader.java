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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles the loading of the RandomX native library.
 * This class is responsible for extracting the library from resources if necessary,
 * loading it into the JVM, and configuring JNA library paths.
 */
@Slf4j
final class RandomXLibraryLoader {

    private static volatile boolean isLoaded = false;
    private static volatile Path loadedLibraryPath = null; // Store the path of the loaded library

    // Private constructor to prevent instantiation
    private RandomXLibraryLoader() {}

    /**
     * Ensures that the RandomX native library is loaded.
     * This method is synchronized and will only attempt to load the library once.
     * It extracts the library from resources, loads it using System.load(),
     * and sets the jna.library.path.
     *
     * @throws UnsatisfiedLinkError if the library cannot be loaded for any reason.
     * @throws Exception for other unexpected errors during loading.
     * @return Path to the loaded native library file, or null if already loaded or failed before critical point.
     */
    public static synchronized Path load() throws Exception {
        if (isLoaded) {
            log.info("Native library already loaded from: {}", loadedLibraryPath);
            return loadedLibraryPath; // Return cached path
        }

        Path tempLibFilePath = null;
        try {
            File tempFile = extractAndLoadNativeLibrary(); // This method now returns File
            tempLibFilePath = tempFile.toPath();
            loadedLibraryPath = tempLibFilePath; // Cache the path

            String tempLibDir = tempFile.getParent();
            if (tempLibDir != null) {
                String currentJnaPath = System.getProperty("jna.library.path");
                if (currentJnaPath == null || currentJnaPath.isEmpty()) {
                    System.setProperty("jna.library.path", tempLibDir);
                } else if (!currentJnaPath.contains(tempLibDir)) {
                    System.setProperty("jna.library.path", currentJnaPath + File.pathSeparator + tempLibDir);
                }
                log.info("Set jna.library.path to include: {}", tempLibDir);
            } else {
                log.warn("Could not get parent directory for temporary library file: {}", loadedLibraryPath);
            }

            isLoaded = true;
            log.info("RandomX native library loaded successfully via RandomXLibraryLoader from: {}", loadedLibraryPath);
            return loadedLibraryPath;

        } catch (UnsatisfiedLinkError ule) { // Catch specifically from System.load()
            log.error("Failed to load native library (UnsatisfiedLinkError from System.load()): {}: {}", 
                     (loadedLibraryPath != null ? loadedLibraryPath : (tempLibFilePath != null ? tempLibFilePath : "<path not determined>")), 
                     ule.getMessage(), ule);
            logLibraryPaths(); // Log paths for diagnostics
            throw ule; // Re-throw to be handled by RandomXNative's static block
        } catch (Exception e) {
            log.error("An unexpected error occurred during native library loading: {}", e.getMessage(), e);
            logLibraryPaths(); // Log paths for diagnostics
            throw e; // Re-throw to be handled by RandomXNative's static block
        }
    }

    /**
     * Logs current JNA and Java library paths for diagnostic purposes.
     */
    public static void logLibraryPaths() {
        String libPath = System.getProperty("jna.library.path");
        log.error("Current jna.library.path: {}", (libPath != null ? libPath : "<not set>"));
        String javaLibPath = System.getProperty("java.library.path");
        log.error("Current java.library.path: {}", (javaLibPath != null ? javaLibPath : "<not set>"));
    }

    /**
     * Extracts the native library from resources to a temporary file and loads it using System.load().
     * @return The File object of the loaded temporary library.
     * @throws IOException if file operations fail.
     * @throws IllegalStateException if the resource is not found.
     * @throws UnsatisfiedLinkError if System.load() fails.
     */
    private static File extractAndLoadNativeLibrary() throws IOException, IllegalStateException, UnsatisfiedLinkError {
        String libraryLogicalName = "randomx";
        String resourceBaseName = "librandomx";

        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();
        
        String libFileNameInResources = getPlatformSpecificResourceName(resourceBaseName, os, arch);

        Path tempFilePath = null;
        try (InputStream libStream = RandomXLibraryLoader.class.getClassLoader().getResourceAsStream(libFileNameInResources)) {
            if (libStream == null) {
                log.error("Native library resource not found: {}", libFileNameInResources);
                try {
                    java.net.URL resourceUrl = RandomXLibraryLoader.class.getResource("");
                    if (resourceUrl != null) {
                         log.error("Checked relative to class location: {}", resourceUrl);
                    }
                     java.net.URL rootResourceUrl = RandomXLibraryLoader.class.getClassLoader().getResource("");
                     if (rootResourceUrl != null) {
                         log.error("Checked relative to classloader root: {}", rootResourceUrl);
                     }
                 } catch(Exception e) { /* ignore secondary errors */ }

                throw new IllegalStateException("Native library resource not found: " + libFileNameInResources +
                    ". Check classpath and resource packaging.");
            }
            
            String mappedLibName = System.mapLibraryName(libraryLogicalName);
            File tempFile;

            if (os.contains("win")) {
                // Use a fixed file name under Windows: randomx.dll
                Path tempDir = Files.createTempDirectory("randomx-");
                tempDir.toFile().deleteOnExit();
                tempFilePath = tempDir.resolve("randomx.dll"); // Fixed name
            } else {
                // Linux and macOS uses the default temporary files policy
                String tempFilePrefix = mappedLibName.substring(0, mappedLibName.lastIndexOf('.'));
                String tempFileSuffix = mappedLibName.substring(mappedLibName.lastIndexOf('.'));
                tempFilePath = Files.createTempFile(tempFilePrefix + "-", tempFileSuffix);
            }

            tempFile = tempFilePath.toFile();

            tempFile.deleteOnExit();
            
            Files.copy(libStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.load(tempFile.getAbsolutePath()); // This can throw UnsatisfiedLinkError
            log.info("Native library extracted to and loaded from: {} (resource: {})", tempFile.getAbsolutePath(), libFileNameInResources);
            return tempFile;

        } catch (IOException e) {
            log.error("IOException during library extraction from resource '{}': {}", libFileNameInResources, e.getMessage(), e);
             if (tempFilePath != null) {
                 log.error("Temporary file path was: {}", tempFilePath.toString());
             }
            throw e;
        } catch (UnsatisfiedLinkError ule) { // Catch from System.load()
            log.error("UnsatisfiedLinkError during System.load() for '{}': {}", 
                (tempFilePath != null ? tempFilePath.toString() : libFileNameInResources), ule.getMessage(), ule);
            throw ule; // Re-throw
        } catch (Exception e) { // Catch other unexpected errors
             log.error("Unexpected exception during library extraction/loading from resource '{}': {}", 
                libFileNameInResources, e.getMessage(), e);
            // Wrap in a more specific runtime exception if desired, or re-throw as is.
            throw new RuntimeException("Failed to load native library from resource: " + libFileNameInResources, e);
        }
    }

    /**
     * Constructs the platform-specific library file name as it exists in the resources.
     */
    private static String getPlatformSpecificResourceName(String resourceBaseName, String os, String arch) {
        String prefix = "native/";
        String suffix;
        String platformArch = arch;

        if (os.contains("win")) {
            suffix = "_windows_x86_64.dll";
        } else if (os.contains("mac")) {
            if ("arm64".equals(arch)) {
                platformArch = "aarch64";
            } else if (!"aarch64".equals(arch) && !"x86_64".equals(arch)){
                 log.warn("Uncommon macOS arch detected: {}, attempting to use it directly in resource path.", arch);
            }
            suffix = "_macos_" + platformArch + ".dylib";
        } else if (os.contains("linux")) {
            if ("amd64".equals(arch)) {
                 platformArch = "x86_64";
             }
            suffix = "_linux_" + platformArch + ".so";
        } else {
            throw new UnsupportedOperationException(
                String.format("Unsupported platform: OS=%s, Architecture=%s", os, arch));
        }
        String resourcePath = prefix + resourceBaseName + suffix;
        log.info("Determined resource path: {} (OS: {}, Arch: {} -> {})", 
                           resourcePath, os, arch, platformArch);
        return resourcePath;
    }
} 