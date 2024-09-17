package com.buddy.api.commons.configuration.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EncryptorComponent {

    private final TextEncryptor textEncryptor;

    public EncryptorComponent() {
        // Configuração do Encryptor usando uma chave secreta e salt fixo
        this.textEncryptor = Encryptors.delux("mySecretKey", "5c0744940b5c369b");
    }

    public String encrypt(String data) {
        return textEncryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return textEncryptor.decrypt(encryptedData);
    }
}
