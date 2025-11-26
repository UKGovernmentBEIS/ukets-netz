package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.service.AccountContactQueryService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestAccountContactQueryServiceTest {

    @InjectMocks
    private RequestAccountContactQueryService service;

    @Mock
    private AccountContactQueryService accountContactQueryService;
    
    @Mock
    private UserInfoApi userInfoApi;
    
    @Test
    void getRequestAccountContact() {
        Long accountId = 1L;
        Request request = Request.builder().build();
        addAccountResourceToRequest(accountId, request);
        String contactType = AccountContactType.PRIMARY;
        
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .firstName("fn").lastName("ln").email("email@email")
                .build();
        
        when(accountContactQueryService.findContactByAccountAndContactType(accountId, contactType))
            .thenReturn(Optional.of("primaryUserId"));
        
        when(userInfoApi.getUserByUserId("primaryUserId")).thenReturn(userInfoDTO);
        
        //invoke
        Optional<UserInfoDTO> result = service.getRequestAccountContact(request, contactType);
            
        assertThat(result.get()).isEqualTo(userInfoDTO);
        
        verify(accountContactQueryService, times(1)).findContactByAccountAndContactType(accountId, contactType);
        verify(userInfoApi, times(1)).getUserByUserId("primaryUserId");
    }
    
    @Test
    void getRequestAccountPrimaryContact() {
        Long accountId = 1L;
        Request request = Request.builder().build();
        addAccountResourceToRequest(accountId, request);
        
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .firstName("fn").lastName("ln").email("email@email")
                .build();
        
        when(accountContactQueryService.findContactByAccountAndContactType(accountId, AccountContactType.PRIMARY))
            .thenReturn(Optional.of("primaryUserId"));
        
        when(userInfoApi.getUserByUserId("primaryUserId")).thenReturn(userInfoDTO);
        
        //invoke
        Optional<UserInfoDTO> result = service.getRequestAccountPrimaryContact(request);
            
        assertThat(result.get()).isEqualTo(userInfoDTO);
        
        verify(accountContactQueryService, times(1)).findContactByAccountAndContactType(accountId, AccountContactType.PRIMARY);
        verify(userInfoApi, times(1)).getUserByUserId("primaryUserId");
    }
    
    @Test
    void getRequestAccountPrimaryContact_not_found() {
        Long accountId = 1L;
        Request request = Request.builder().build();
        addAccountResourceToRequest(accountId, request);

        when(accountContactQueryService.findContactByAccountAndContactType(accountId, AccountContactType.PRIMARY))
            .thenReturn(Optional.empty());

        Optional<UserInfoDTO> result = service.getRequestAccountPrimaryContact(request);

        //invoke
        assertFalse(result.isPresent());
        verify(accountContactQueryService, times(1)).findContactByAccountAndContactType(accountId, AccountContactType.PRIMARY);
        verifyNoInteractions(userInfoApi);
    }
    
    private void addAccountResourceToRequest(Long accountId, Request request) {
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();

        request.getRequestResources().add(accountResource);
	}  
}
