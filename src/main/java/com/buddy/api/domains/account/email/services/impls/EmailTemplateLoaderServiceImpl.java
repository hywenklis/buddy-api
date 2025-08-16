package com.buddy.api.domains.account.email.services.impls;

import com.buddy.api.commons.exceptions.ReadyIoException;
import com.buddy.api.domains.account.email.services.EmailTemplateLoaderService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailTemplateLoaderServiceImpl implements EmailTemplateLoaderService {

    private final ResourceLoader resourceLoader;

    @Override
    public String load(final String templatePath) {
        final Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template from path: {}", templatePath, e);
            throw new ReadyIoException(
                "Failed to load email template.",
                "email template",
                e.getCause()
            );
        }
    }
}

