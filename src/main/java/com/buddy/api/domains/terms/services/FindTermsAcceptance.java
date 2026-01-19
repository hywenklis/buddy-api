package com.buddy.api.domains.terms.services;

import java.util.UUID;

public interface FindTermsAcceptance {
    boolean exists(UUID accountId, UUID termsVersionId);
}
