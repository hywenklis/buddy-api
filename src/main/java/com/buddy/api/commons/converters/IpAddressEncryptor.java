package com.buddy.api.commons.converters;

import com.buddy.api.commons.configurations.properties.BuddySecurityProperties;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class IpAddressEncryptor extends AbstractAttributeEncryptor {
    public IpAddressEncryptor(final BuddySecurityProperties properties) {
        super(properties);
    }
}
