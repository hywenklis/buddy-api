package com.buddy.api.units.commons.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import com.buddy.api.commons.converters.IpAddressEncryptor;
import com.buddy.api.commons.exceptions.AttributeEncryptorException;
import com.buddy.api.units.UnitTestAbstract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class IpAddressEncryptorTest extends UnitTestAbstract {

    @Mock
    private BuddySecurityProperties properties;

    private IpAddressEncryptor encryptor;

    private static final String VALID_SECRET_KEY = "12345678901234567890123456789012";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    @BeforeEach
    void setUp() {
        lenient().when(properties.encryptionKey()).thenReturn(VALID_SECRET_KEY);
        lenient().when(properties.algorithm()).thenReturn(ALGORITHM);
        lenient().when(properties.tagLength()).thenReturn(TAG_LENGTH);
        lenient().when(properties.ivLength()).thenReturn(IV_LENGTH);

        this.encryptor = new IpAddressEncryptor(properties);
    }

    @Test
    @DisplayName("Should encrypt and decrypt an IPv4 address"
        + " successfully returning the original value")
    void should_encrypt_and_decrypt_ipv4_successfully() {
        final var originalIp = "192.168.0.1";
        final var encryptedData = encryptor.convertToDatabaseColumn(originalIp);
        final var decryptedData = encryptor.convertToEntityAttribute(encryptedData);

        assertThat(encryptedData)
            .isNotNull()
            .isNotEqualTo(originalIp)
            .isBase64();

        assertThat(decryptedData)
            .isNotNull()
            .isEqualTo(originalIp);
    }

    @Test
    @DisplayName("Should encrypt and decrypt an IPv6 address successfully")
    void should_encrypt_and_decrypt_ipv6_successfully() {
        final var originalIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        final var encryptedData = encryptor.convertToDatabaseColumn(originalIp);
        final var decryptedData = encryptor.convertToEntityAttribute(encryptedData);

        assertThat(decryptedData).isEqualTo(originalIp);
    }

    @Test
    @DisplayName("Should generate different ciphertexts for the same IP due to random IV strategy")
    void should_generate_random_iv_for_each_encryption() {
        final var ipAddress = "10.0.0.1";
        final var firstEncryption = encryptor.convertToDatabaseColumn(ipAddress);
        final var secondEncryption = encryptor.convertToDatabaseColumn(ipAddress);

        assertThat(firstEncryption).isNotEqualTo(secondEncryption);
        assertThat(encryptor.convertToEntityAttribute(firstEncryption)).isEqualTo(ipAddress);
        assertThat(encryptor.convertToEntityAttribute(secondEncryption)).isEqualTo(ipAddress);
    }

    @Test
    @DisplayName("Should return null when input is null to support optional JPA columns")
    void should_handle_null_values_gracefully() {
        assertThat(encryptor.convertToDatabaseColumn(null)).isNull();
        assertThat(encryptor.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Should throw AttributeEncryptorException "
        + "when database data is corrupted or tampered")
    void should_throw_exception_on_corrupted_data() {
        final var tamperedData = "UmTextoLongoEmBase64QueNaoEhCriptografiaValida==";

        assertThatThrownBy(() -> encryptor.convertToEntityAttribute(tamperedData))
            .isInstanceOf(AttributeEncryptorException.class)
            .hasMessage("Error decrypting data")
            .hasFieldOrPropertyWithValue("fieldName", "attribute");
    }

    @Test
    @DisplayName("Should throw AttributeEncryptorException when configuration is invalid")
    void should_throw_exception_on_encryption_error() {
        when(properties.algorithm()).thenReturn("ALGORITHM_INVALID");

        final var badConfigEncryptor = new IpAddressEncryptor(properties);
        final var ip = "127.0.0.1";

        assertThatThrownBy(() -> badConfigEncryptor.convertToDatabaseColumn(ip))
            .isInstanceOf(AttributeEncryptorException.class)
            .hasMessage("Error encrypting data");
    }
}
