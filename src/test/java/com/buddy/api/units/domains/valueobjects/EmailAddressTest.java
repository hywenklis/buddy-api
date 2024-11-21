package com.buddy.api.units.domains.valueobjects;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.valueobjects.EmailAddress;
import com.buddy.api.units.UnitTestAbstract;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EmailAddressTest extends UnitTestAbstract {
    @Test
    @DisplayName("Should recognize equal email addresses")
    void should_recognize_equal_email_adresses() {
        var emailString = generateValidEmail();
        var email = new EmailAddress(emailString);
        var equivalentEmail = new EmailAddress(emailString);

        assertThat(email).isEqualTo(equivalentEmail);
    }

    @Test
    @DisplayName("Should distinguish distinct emails")
    void should_distinguish_distinct_emails() {
        var emailString = generateValidEmail();
        var distinctEmailString = "a" + emailString;

        var email = new EmailAddress(emailString);
        var distinctEmail = new EmailAddress(distinctEmailString);

        assertThat(email).isNotEqualTo(distinctEmail);
    }

    @Test
    @DisplayName("Should consider email to be case insensitive")
    void should_consider_email_to_be_case_insensitive() {
        var lowerCaseEmailString = generateValidEmail().toLowerCase(Locale.ENGLISH);
        var upperCaseEmailString = lowerCaseEmailString.toUpperCase(Locale.ENGLISH);

        assertThat(new EmailAddress(lowerCaseEmailString))
            .isEqualTo(new EmailAddress(upperCaseEmailString));
    }

    @Test
    @DisplayName("Should resolve null emails to empty string emails")
    void should_resolve_null_emails_to_empty_string() {
        assertThat(new EmailAddress(null))
            .isEqualTo(new EmailAddress(""));
    }
}
