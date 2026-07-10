package uk.gov.netz.api.terms.userterms;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public class UserTermsService {
	
    private final UserTermsVersionRepository userTermsVersionRepository;

    @Transactional
    public void updateUserTerms(String userId, Short newTermsVersion) {
        userTermsVersionRepository.findById(userId)
                .ifPresentOrElse(userTermsVersion -> userTermsVersion.setVersion(newTermsVersion),
                        () -> userTermsVersionRepository.save(UserTermsVersion.builder()
                                .id(userId)
                                .version(newTermsVersion)
                                .build()));
    }

    @Transactional(readOnly = true)
    public Optional<Short> getUserTerms(String userId) {
        return userTermsVersionRepository.findById(userId)
                .map(UserTermsVersion::getVersion);
    }
    
    @Transactional
    public void deleteUserTerms(String userId) {
    	userTermsVersionRepository.deleteById(userId);
    }

}
