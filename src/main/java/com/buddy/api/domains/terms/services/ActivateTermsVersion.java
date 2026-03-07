package com.buddy.api.domains.terms.services;

import java.util.UUID;

public interface ActivateTermsVersion {
    void activate(UUID termsVersionId);
}
