package uk.gov.netz.api.workflow.request.application.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.ResourceAuthorityInfo;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RequestAuthorityInfoQueryServiceTest {
	
    @InjectMocks
    private RequestAuthorityInfoQueryService service;

    @Mock
    private RequestService requestService;

    @Test
    void getRequestInfo() {
        Request request = Request.builder().type(RequestType.builder().code("code").build()).build();
        addResourcesToRequest(1L, ENGLAND, request);

        when(requestService.findRequestById("1")).thenReturn(request);

        RequestAuthorityInfoDTO requestInfoDTO = service.getRequestInfo("1");

        RequestAuthorityInfoDTO expectedRequestInfoDTO = RequestAuthorityInfoDTO.builder()
        		.type("code")
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1", 
                				ResourceType.CA, ENGLAND.name()))
                        .build())
                .build();
        assertEquals(expectedRequestInfoDTO, requestInfoDTO);
    }

    @Test
    void getRequestInfo_does_not_exist() {
        when(requestService.findRequestById("1")).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        BusinessException be = assertThrows(BusinessException.class, () -> {
            service.getRequestInfo("1");
        });
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
    
    private void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Request request) {
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

        request.getRequestResources().addAll(List.of(caResource, accountResource));
	}
}
