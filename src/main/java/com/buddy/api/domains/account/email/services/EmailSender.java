package com.buddy.api.domains.account.email.services;

import java.util.UUID;

public interface EmailSender {
    void dispatchVerificationEmail(UUID accountId,
                                   String userEmail,
                                   String token);

}
