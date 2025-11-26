package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderQueryService;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountThirdPartyDataProviderServiceTest {

    @InjectMocks
    private AccountThirdPartyDataProviderService service;

    @Mock
    private AccountQueryService accountQueryService;
    @Mock
    private ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;
    @Mock
    private ThirdPartyDataProviderService thirdPartyDataProviderService;


    @Test
    void getThirdPartyDataProviderNameInfoByAccount() {
        Long accountId = 1L;
        Long thirdPartyDataProviderId = 2L;
        ThirdPartyDataProviderNameInfoDTO dto = mock(ThirdPartyDataProviderNameInfoDTO.class);

        when(accountQueryService.getThirdPartyDataProviderId(accountId))
            .thenReturn(Optional.of(thirdPartyDataProviderId));
        when(thirdPartyDataProviderService.getThirdPartyDataProviderNameInfoById(thirdPartyDataProviderId))
            .thenReturn(dto);

        Optional<ThirdPartyDataProviderNameInfoDTO> response =
            service.getThirdPartyDataProviderNameInfoByAccount(accountId);

        assertEquals(Optional.of(dto), response);
        verify(accountQueryService).getThirdPartyDataProviderId(accountId);
        verify(thirdPartyDataProviderService).getThirdPartyDataProviderNameInfoById(thirdPartyDataProviderId);

        verifyNoMoreInteractions(accountQueryService, thirdPartyDataProviderService);
        verifyNoMoreInteractions(thirdPartyDataProviderQueryService);
    }

    @Test
    void getAllThirdPartyDataProviders() {
        ThirdPartyDataProviderNameInfoDTO  dto = mock(ThirdPartyDataProviderNameInfoDTO.class);
        when(thirdPartyDataProviderQueryService.getAllThirdPartyDataProviders()).thenReturn(List.of(dto));

        List<ThirdPartyDataProviderNameInfoDTO> allThirdPartyDataProviders = service.getAllThirdPartyDataProviders();

        assertEquals(List.of(dto), allThirdPartyDataProviders);

        verify(thirdPartyDataProviderQueryService).getAllThirdPartyDataProviders();
        verifyNoMoreInteractions(thirdPartyDataProviderQueryService);
        verifyNoInteractions(accountQueryService,  thirdPartyDataProviderService);
    }
}