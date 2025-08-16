package com.buddy.api.units.domains.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.buddy.api.commons.exceptions.ReadyIoException;
import com.buddy.api.domains.account.email.services.impls.EmailTemplateLoaderServiceImpl;
import com.buddy.api.units.UnitTestAbstract;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class EmailTemplateLoaderServiceImplTest extends UnitTestAbstract {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private EmailTemplateLoaderServiceImpl emailTemplateLoaderService;

    @Test
    @DisplayName("Should load and return template content when file exists")
    void load_whenTemplateExists_shouldReturnContent() throws IOException {
        final String templatePath = "templates/test-template.html";
        final String expectedContent = "<html><body><h1>test template buddy 🐾</h1></body></html>";

        when(resourceLoader.getResource("classpath:" + templatePath)).thenReturn(resource);
        when(resource.getContentAsString(StandardCharsets.UTF_8)).thenReturn(expectedContent);

        final String actualContent = emailTemplateLoaderService.load(templatePath);

        assertThat(actualContent).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Should throw ReadyIoException when reading the resource fails")
    void load_whenResourceAccessFails_shouldThrowReadyIoException() throws IOException {
        final String nonExistentPath = "templates/non-existent-template.html";

        when(resourceLoader.getResource("classpath:" + nonExistentPath)).thenReturn(resource);
        when(resource.getContentAsString(StandardCharsets.UTF_8))
            .thenThrow(new IOException("Disk read error"));

        assertThatThrownBy(() -> emailTemplateLoaderService.load(nonExistentPath))
            .isInstanceOf(ReadyIoException.class)
            .hasMessageContaining("Failed to load email template.");
    }
}
