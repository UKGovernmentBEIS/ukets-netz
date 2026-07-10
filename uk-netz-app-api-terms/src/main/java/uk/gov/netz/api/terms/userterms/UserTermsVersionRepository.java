package uk.gov.netz.api.terms.userterms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public interface UserTermsVersionRepository extends JpaRepository<UserTermsVersion, String> {


}
