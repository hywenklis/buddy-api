package com.buddy.api.domains.terms.dtos;

import com.buddy.api.domains.account.dtos.AccountDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TermsAcceptanceDto(AccountDto account,
                                 TermsVersionDto termsVersion,
                                 String ipAddress,
                                 String userAgent,
                                 LocalDateTime acceptedAt
) { }
