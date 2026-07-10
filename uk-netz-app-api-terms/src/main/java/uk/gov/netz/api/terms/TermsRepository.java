package uk.gov.netz.api.terms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public interface TermsRepository extends JpaRepository<Terms, Long> {

    @Query("select t from Terms t where t.version = (SELECT MAX(tt.version) from Terms tt)")
    Terms findLatestTerms();

}
