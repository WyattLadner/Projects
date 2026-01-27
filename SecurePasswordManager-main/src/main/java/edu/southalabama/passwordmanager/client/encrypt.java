package edu.southalabama.passwordmanager.client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides AES-GCM encryption utilities.
 * AES/GCM/NoPadding is chosen for modern authenticated encryption.
 *
 * The output format is:
 *   Base64( IV[12] + ciphertext_and_tag )
 *
 * This allows you to store/transmit encrypted values as simple Base64 strings.
 */
public class encrypt {

    // AES-GCM authentication tag length (in bits)
    private static final int GCM_TAG_LENGTH = 128;

    // Recommended IV length for GCM: 12 bytes (96 bits)
    private static final int IV_LENGTH = 12;

    // Cryptographically secure RNG for IV generation
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts a plaintext string using AES-GCM.
     *
     * @param plaintext the raw plaintext to encrypt
     * @param key       the AES SecretKey
     * @return Base64 string containing IV + encrypted bytes
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {

        // 1. Generate random 12-byte IV
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        // 2. Configure AES-GCM cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // 3. Perform encryption
        byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // 4. Prepend IV to ciphertext output
        byte[] output = new byte[IV_LENGTH + cipherBytes.length];
        System.arraycopy(iv, 0, output, 0, IV_LENGTH);
        System.arraycopy(cipherBytes, 0, output, IV_LENGTH, cipherBytes.length);

        // 5. Encode for text storage
        return Base64.getEncoder().encodeToString(output);
    }
}
