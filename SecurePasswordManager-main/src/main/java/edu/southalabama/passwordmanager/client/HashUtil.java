package edu.southalabama.passwordmanager.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class that provides strong hashing functions (SHA-256)
 * for non-password data such as usernames, identifiers, or
 * integrity checks. This class is stateless and thread-safe.
 */
public final class HashUtil {

    // Prevent instantiation
    private HashUtil() {}

    /**
     * Computes a SHA-256 hash of the given input string and returns
     * the result as a lowercase hex string to match server storage.
     *
     * @param input the raw input to hash
     * @return hex(SHA-256(input)) or null on error
     */
    public static String sha256(String input) {
        if (input == null) return null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported on this platform", e);
        }
    }
}
