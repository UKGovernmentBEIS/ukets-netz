package uk.gov.netz.api.user.core.service.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves contact email to account IDs via all user authority assignments. */
@Service
@RequiredArgsConstructor
public class ContactAccountIdsService {

    private final UserAuthService userAuthService;
    private final AuthorityRepository authorityRepository;

    public Set<Long> resolveAccountIdsByEmail(String contactEmail) {
        if (!StringUtils.hasText(contactEmail)) {
            return Set.of();
        }
        return userAuthService.getUserByEmail(contactEmail.trim())
                .map(user -> authorityRepository.findByUserId(user.getUserId())
                        .stream()
                        .map(Authority::getAccountId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }
}
