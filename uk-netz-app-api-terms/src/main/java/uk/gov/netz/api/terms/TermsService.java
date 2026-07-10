package uk.gov.netz.api.terms;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;

    public Terms getLatestTerms() {
        return termsRepository.findLatestTerms();
    }

}
