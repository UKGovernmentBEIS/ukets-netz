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
import uk.gov.netz.api.workflow.request.core.repository.RequestNoteRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestNoteAuthorityInfoQueryServiceTest {

    @InjectMocks
    private RequestNoteAuthorityInfoQueryService service;

    @Mock
    private RequestNoteRepository requestNoteRepository;

    @Test
    void getRequestInfo() {

        final long noteId = 1L;
        final Long accountId = 2L;

        final Request request = Request.builder().type(RequestType.builder().code("TEST_REQUEST_TYPE").build()).build();
        addResourcesToRequest(accountId, ENGLAND, 1L, request);

        when(requestNoteRepository.getRequestByNoteId(noteId)).thenReturn(Optional.of(request));

        final RequestAuthorityInfoDTO requestInfoDTO = service.getRequestNoteInfo(noteId);

        final RequestAuthorityInfoDTO expectedRequestInfoDTO = RequestAuthorityInfoDTO.builder()
    		.type("TEST_REQUEST_TYPE")
            .authorityInfo(ResourceAuthorityInfo.builder()
            		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString(), 
            				ResourceType.CA, ENGLAND.name(),
            				ResourceType.VERIFICATION_BODY, "1"))
                    .build())
            .build();
        assertEquals(expectedRequestInfoDTO, requestInfoDTO);
    }

    @Test
    void getRequestInfo_does_not_exist() {

        final long noteId = 1L;
        when(requestNoteRepository.getRequestByNoteId(noteId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () -> service.getRequestNoteInfo(noteId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
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
