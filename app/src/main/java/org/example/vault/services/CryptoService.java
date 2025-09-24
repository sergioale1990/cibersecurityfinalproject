package org.example.vault.services;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

//Clase responsable de cifrar y descifrar datos con AES
public class CryptoService {
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();
    private final byte[] kdfSalt;
    private final int iterations;
    private final char[] masterPassword;

    // Constructor
    public CryptoService(char[] masterPassword, byte[] kdfSalt, int iterations) {
        this.masterPassword = masterPassword;
        this.kdfSalt = kdfSalt;
        this.iterations = iterations;
    }

    // Deriva la clave de cifrado a partir del master password y el salt
    private SecretKeySpec deriveKey() throws Exception {
        PBEKeySpec spec = new PBEKeySpec(masterPassword, kdfSalt, iterations, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    // Cifra un texto plano y devuelve el resultado en un array de dos cadenas:
    // - Cadena cifrada (Base64)
    // - IV (Base64)
    public String[] encrypt(String plain) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] out = cipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return new String[]{
                Base64.getEncoder().encodeToString(out),
                Base64.getEncoder().encodeToString(iv)
        };
    }

    // Descifra un texto cifrado y devuelve el resultado como cadena plana
    public String decrypt(String cipherTextB64, String ivB64) throws Exception {
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] ct = Base64.getDecoder().decode(cipherTextB64);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] out = cipher.doFinal(ct);
        return new String(out, java.nio.charset.StandardCharsets.UTF_8);
    }
}