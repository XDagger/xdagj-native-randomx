package io.xdag.crypto.jni;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;

import org.apache.commons.lang3.SystemUtils;

public final class RandomXUtils {

    static File nativeDir;
    static boolean enabled = false;

    /**
     * Initializes the native libraries
     */
    public static void init() {
        if(SystemUtils.IS_OS_LINUX) {
            enabled = loadLibrary("/librandomx.so");
        } else if(SystemUtils.IS_OS_MAC) {
            enabled = loadLibrary("/native/Darwin-x86_64/librandomx.dylib");
        } else if(SystemUtils.IS_OS_MAC) {
            loadLibrary("/native/Windows-x86_64/librandomx.dll");
        }
    }

    /**
     * Loads a library file from bundled resource.
     */
    protected static boolean loadLibrary(String resource) {
        try {
            if (nativeDir == null) {
                nativeDir = Files.createTempDirectory("native").toFile();
                nativeDir.deleteOnExit();
            }

            String name = resource.contains("/") ? resource.substring(resource.lastIndexOf('/') + 1) : resource;
            File file = new File(nativeDir, name);

            if (!file.exists()) {
                InputStream in = RandomX.class.getResourceAsStream(resource); // null pointer exception
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                for (int c; (c = Objects.requireNonNull(in).read()) != -1; ) {
                    out.write(c);
                }
                out.close();
                in.close();
            }

            System.load(file.getAbsolutePath());
            return true;
        } catch (Exception | UnsatisfiedLinkError e) {
            System.err.println("Failed to load native library:" + e.getMessage());
            return false;
        }
    }

}
