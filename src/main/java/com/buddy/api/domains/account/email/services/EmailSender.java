package com.buddy.api.domains.account.email.services;

import com.buddy.api.domains.valueobjects.EmailAddress;
import java.util.UUID;

public interface EmailSender {
    void dispatchVerificationEmail(UUID accountId,
                                   String userEmail,
                                   String token);

    void dispatchPasswordRecoveryEmail(UUID accountId,
                                       String userEmail,
                                       String token);

    void dispatchPasswordChangedNotification(UUID accountId, EmailAddress userEmail);
}
