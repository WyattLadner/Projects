package edu.southalabama.passwordmanager.client;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.nio.file.attribute.PosixFilePermission;

/**
 * Provides the AES key used for encrypting/decrypting passwords.
 * If a key file (sspm.key) does not exist next to the running JAR/class,
 * a new secure random key is generated and written. Permissions are tightened
 * on POSIX systems when possible. Falls back to a bundled key on error.
 */
public final class KeyManager {

    private static final String KEY_FILE_NAME = "sspm.key";

    // fallback hardcoded key (kept for compatibility if file operations fail)
    private static final byte[] FALLBACK_KEY_BYTES = new byte[] {
            0x01, 0x23, 0x45, 0x67,
            (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
            0x10, 0x32, 0x54, 0x76,
            (byte) 0x98, (byte) 0xba, (byte) 0xdc, (byte) 0xfe
    };

    private static final SecretKey AES_KEY = loadOrCreateKey();

    private KeyManager() {}

    public static SecretKey getKey() {
        return AES_KEY;
    }

    private static SecretKey loadOrCreateKey() {
        try {
            // determine folder next to the running JAR / classes
            File codeLocation = new File(KeyManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File dir = codeLocation.isDirectory() ? codeLocation : codeLocation.getParentFile();
            if (dir == null) throw new IOException("Cannot determine application folder");

            Path keyPath = new File(dir, KEY_FILE_NAME).toPath();

            if (!Files.exists(keyPath)) {
                // generate secure random 16 byte (128-bit) key
                byte[] keyBytes = new byte[16];
                new SecureRandom().nextBytes(keyBytes);

                // write atomically
                Path tmp = Files.createTempFile(dir.toPath(), "sspm-key-", ".tmp");
                try (OutputStream os = Files.newOutputStream(tmp)) {
                    os.write(keyBytes);
                    os.flush();
                }
                Files.move(tmp, keyPath);

            
                try {
                    EnumSet<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                    Files.setPosixFilePermissions(keyPath, perms);
                } catch (UnsupportedOperationException | IOException ignored) {
                    // Not POSIX (Windows) or permission set failed — ignore
                }
            }

            byte[] loaded = Files.readAllBytes(keyPath);
            if (loaded.length != 16) throw new IOException("Invalid key length: " + loaded.length);
            return new SecretKeySpec(loaded, "AES");
        } catch (Throwable t) {
            // fallback to hardcoded key if anything goes wrong
            return new SecretKeySpec(FALLBACK_KEY_BYTES, "AES");
        }
    }
}