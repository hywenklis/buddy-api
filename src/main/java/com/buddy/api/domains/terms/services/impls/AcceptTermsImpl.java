package com.buddy.api.domains.terms.services.impls;

import com.buddy.api.domains.account.services.FindAccount;
import com.buddy.api.domains.terms.dtos.AcceptTermsDto;
import com.buddy.api.domains.terms.dtos.TermsAcceptanceDto;
import com.buddy.api.domains.terms.services.AcceptTerms;
import com.buddy.api.domains.terms.services.CreateTermsAcceptance;
import com.buddy.api.domains.terms.services.FindTermsAcceptance;
import com.buddy.api.domains.terms.services.FindTermsVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptTermsImpl implements AcceptTerms {

    private final FindAccount findAccount;
    private final FindTermsVersion findTermsVersion;
    private final CreateTermsAcceptance createTermsAcceptance;
    private final FindTermsAcceptance findTermsAcceptance;

    @Override
    @Transactional
    public void accept(final AcceptTermsDto acceptTermsDto) {
        log.info("processing acceptance of terms for user");
        final var account = findAccount.findByEmail(acceptTermsDto.email());
        final var activeVersion = findTermsVersion.findActive();

        if (findTermsAcceptance.exists(account.accountId(), activeVersion.termsVersionId())) {
            log.info("user {} already has the term {} accepted. ignoring.",
                account.accountId(), activeVersion.versionTag()
            );
            return;
        }

        final var acceptance = TermsAcceptanceDto.builder()
            .account(account)
            .termsVersion(activeVersion)
            .ipAddress(acceptTermsDto.ipAddress())
            .userAgent(acceptTermsDto.userAgent())
            .build();

        createTermsAcceptance.create(acceptance);
        log.info("term {} successfully accepted by the user. {}.",
            activeVersion.versionTag(), account.accountId()
        );
    }
}
