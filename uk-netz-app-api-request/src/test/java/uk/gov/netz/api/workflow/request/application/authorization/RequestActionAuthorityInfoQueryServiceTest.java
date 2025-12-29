package uk.gov.netz.api.workflow.request.application.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestActionAuthorityInfoDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestActionAuthorityInfoQueryServiceTest {

    @InjectMocks
    private RequestActionAuthorityInfoQueryService service;

    @Mock
    private RequestActionRepository requestActionRepository;

    @Test
    void getRequestActionAuthorityInfo() {
        Long requestActionId = 1L;
        Request request = Request.builder().type(RequestType.builder().code("TEST_REQUEST_TYPE").build()).build();
        addResourcesToRequest(1L, ENGLAND, 2L, request);
        
        RequestAction requestAction = RequestAction.builder()
            .id(requestActionId)
            .type("requestActionType1")
            .request(request)
            .build();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.of(requestAction));

        RequestActionAuthorityInfoDTO requestActionInfo = service.getRequestActionAuthorityInfo(requestActionId);

        assertThat(requestActionInfo.getId()).isEqualTo(requestActionId);
        assertThat(requestActionInfo.getType()).isEqualTo(requestAction.getType());
        assertEquals(request.getRequestResourcesMap(), requestActionInfo.getAuthorityInfo().getRequestResources());
    }

    @Test
    void getRequestActionAuthorityInfo_not_exists() {
        Long requestActionId = 1L;
        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.empty());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.getRequestActionAuthorityInfo(requestActionId));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
    }
    
    private void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Long vbId, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
		
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();
		
		RequestResource vbIdResource = RequestResource.builder()
				.resourceType(ResourceType.VERIFICATION_BODY)
				.resourceId(vbId.toString())
				.request(request)
				.build();
        request.getRequestResources().addAll(List.of(caResource, accountResource, vbIdResource));
	}
}