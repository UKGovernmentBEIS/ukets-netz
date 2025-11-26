package uk.gov.netz.api.thirdpartydataprovider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvider;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;

import java.util.List;

@Repository
public interface ThirdPartyDataProviderRepository extends JpaRepository<ThirdPartyDataProvider, Long> {

    @Transactional(readOnly = true)
    boolean existsByNameIgnoreCase(String name);

    @Transactional(readOnly = true)
    @Query(name = ThirdPartyDataProvider.NAMED_QUERY_FIND_ALL_THIRD_PARTY_DATA_PROVIDERS)
    List<ThirdPartyDataProviderNameInfoDTO> findAllThirdPartyDataProviders();
}
