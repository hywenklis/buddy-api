package com.buddy.api.commons.converters;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import com.buddy.api.commons.exceptions.AttributeEncryptorException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Converter
public abstract class AbstractAttributeEncryptor implements AttributeConverter<String, String> {

    private final BuddySecurityProperties properties;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String convertToDatabaseColumn(final String attribute) {
        return Optional.ofNullable(attribute)
            .map(this::encrypt)
            .orElse(null);
    }

    @Override
    public String convertToEntityAttribute(final String dbData) {
        return Optional.ofNullable(dbData)
            .map(this::decrypt)
            .orElse(null);
    }

    private String encrypt(final String data) {
        try {
            final byte[] iv = new byte[properties.ivLength()];
            SECURE_RANDOM.nextBytes(iv);

            final var cipher = initCipher(Cipher.ENCRYPT_MODE, iv);
            final byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            final var byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new AttributeEncryptorException("attribute", "Error encrypting data", e);
        }
    }

    private String decrypt(final String encryptedData) {
        try {
            final byte[] decoded = Base64.getDecoder().decode(encryptedData);
            final var byteBuffer = ByteBuffer.wrap(decoded);

            final byte[] iv = new byte[properties.ivLength()];
            byteBuffer.get(iv);

            final byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            final var cipher = initCipher(Cipher.DECRYPT_MODE, iv);

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AttributeEncryptorException("attribute", "Error decrypting data", e);
        }
    }

    private Cipher initCipher(final int mode, final byte[] iv) throws Exception {
        final var cipher = Cipher.getInstance(properties.algorithm());
        final var parameterSpec = new GCMParameterSpec(properties.tagLength(), iv);
        final var keySpec = new SecretKeySpec(properties.encryptionKey()
            .getBytes(StandardCharsets.UTF_8),
            "AES"
        );

        cipher.init(mode, keySpec, parameterSpec);
        return cipher;
    }
}
