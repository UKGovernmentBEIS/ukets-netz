package uk.gov.netz.api.thirdpartydataprovider.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.thirdpartydataprovider.repository.ThirdPartyDataProviderRepository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDataProviderQueryServiceTest {

    @InjectMocks
    private ThirdPartyDataProviderQueryService queryService;

    @Mock
    private ThirdPartyDataProviderRepository thirdPartyDataProviderRepository;

    @Test
    void existsByNameIgnoreCase(){
        String name = "name";
        queryService.existsByNameIgnoreCase(name);

        verify(thirdPartyDataProviderRepository).existsByNameIgnoreCase(name);
        verifyNoMoreInteractions(thirdPartyDataProviderRepository);
    }

    @Test
    void existsById(){
        Long id = 1L;
        queryService.existsById(id);

        verify(thirdPartyDataProviderRepository).existsById(id);
        verifyNoMoreInteractions(thirdPartyDataProviderRepository);
    }

    @Test
    void getAllThirdPartyDataProviders() {
        queryService.getAllThirdPartyDataProviders();

        verify(thirdPartyDataProviderRepository).findAllThirdPartyDataProviders();
        verifyNoMoreInteractions(thirdPartyDataProviderRepository);
    }
}