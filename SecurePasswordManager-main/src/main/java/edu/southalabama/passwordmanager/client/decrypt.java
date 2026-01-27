package edu.southalabama.passwordmanager.client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;

/**
 * Provides AES-GCM decryption utility.
 * Works with strings produced by encrypt.encrypt().
 *
 * Expected input format:
 *   Base64( IV[12] + ciphertext_and_tag )
 */
public class decrypt {

    // Must match encrypt.java
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    /**
     * Decrypts Base64(AES-GCM(plaintext)) values produced by encrypt.encrypt().
     *
     * @param base64IvAndCiphertext complete encrypted string
     * @param key                   same SecretKey used for encryption
     * @return decrypted plaintext
     */
    public static String decrypt(String base64IvAndCiphertext, SecretKey key) throws Exception {

        // 1. Decode Base64 string into raw bytes
        byte[] ivAndCiphertext = Base64.getDecoder().decode(base64IvAndCiphertext);

        if (ivAndCiphertext.length < IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data (too short).");
        }

        // 2. Split IV and ciphertext
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(ivAndCiphertext, 0, iv, 0, IV_LENGTH);

        byte[] ciphertext = new byte[ivAndCiphertext.length - IV_LENGTH];
        System.arraycopy(ivAndCiphertext, IV_LENGTH, ciphertext, 0, ciphertext.length);

        // 3. Configure AES-GCM cipher for decryption
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, params);

        // 4. Perform decryption
        byte[] plaintextBytes = cipher.doFinal(ciphertext);
        return new String(plaintextBytes, "UTF-8");
    }
}
