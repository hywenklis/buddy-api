package com.buddy.api.units.domains.valueobjects;

import static com.buddy.api.utils.RandomEmailUtils.generateValidEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.buddy.api.commons.exceptions.InvalidEmailAddressException;
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
    @DisplayName("Should ignore leading and trailing white spaces")
    void should_ignore_leading_and_trailing_white_spaces() {
        var email = generateValidEmail();

        assertThat(new EmailAddress(email))
            .isEqualTo(new EmailAddress(" " + email + " "));
    }

    @Test
    @DisplayName("Should not instantiate with null email value")
    void should_not_instantiate_with_null_email_value() {
        assertThatThrownBy(() -> new EmailAddress(null))
            .isInstanceOf(InvalidEmailAddressException.class)
            .hasMessage("Email address value cannot be null or blank")
            .extracting("fieldName")
            .isEqualTo("email");
    }

    @Test
    @DisplayName("Should not instantiate with empty email value")
    void should_not_instantiate_with_empty_email_value() {
        assertThatThrownBy(() -> new EmailAddress(""))
            .isInstanceOf(InvalidEmailAddressException.class)
            .hasMessage("Email address value cannot be null or blank")
            .extracting("fieldName")
            .isEqualTo("email");
    }

    @Test
    @DisplayName("Should not instantiate with blank email value")
    void should_not_instantiate_with_blank_email_value() {
        assertThatThrownBy(() -> new EmailAddress(" "))
            .isInstanceOf(InvalidEmailAddressException.class)
            .hasMessage("Email address value cannot be null or blank")
            .extracting("fieldName")
            .isEqualTo("email");
    }
}
