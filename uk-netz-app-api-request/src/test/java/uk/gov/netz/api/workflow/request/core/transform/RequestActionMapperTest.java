package uk.gov.netz.api.workflow.request.core.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestActionDTO;
import uk.gov.netz.api.workflow.request.core.domain.dto.RequestActionInfoDTO;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelledRequestActionPayload;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

class RequestActionMapperTest {

    private RequestActionMapper mapper;
    
    @BeforeEach
    public void init() {
        mapper = Mappers.getMapper(RequestActionMapper.class);
    }
    
    @Test
    void toRequestActionDTO() {
        Long accountId = 100L;
        Request request = Request.builder()
        		.id("requestId")
        		.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build()).build();
        addResourcesToRequest(accountId, CompetentAuthorityEnum.ENGLAND, request);
        RequestActionPayload requestActionPayload = Mockito.mock(RequestActionPayload.class);
        RequestAction requestAction = RequestAction.builder()
            .id(1L)
            .type("REQUEST_TERMINATED")
            .submitter("fn ln")
            .payload(requestActionPayload)
            .request(request)
            .build();
        
        RequestActionDTO result = mapper.toRequestActionDTO(requestAction);
        
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("REQUEST_TERMINATED");
        assertThat(result.getSubmitter()).isEqualTo("fn ln");
        assertThat(result.getPayload()).isEqualTo(requestActionPayload);
        assertThat(result.getRequestAccountId()).isEqualTo(accountId);
        assertThat(result.getRequestAccountId()).isEqualTo(accountId);
        assertThat(result.getRequestType()).isEqualTo("DUMMY_REQUEST_TYPE");
        assertThat(result.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.ENGLAND);
    }
    
    @Test
    void toRequestActionInfoDTO() {
        RequestAction requestAction = RequestAction.builder()
                .id(1L)
                .type("REQUEST_TERMINATED")
                .submitter("fn ln")
                .build();
        
        RequestActionInfoDTO result = mapper.toRequestActionInfoDTO(requestAction);
        assertThat(result.getType()).isEqualTo("REQUEST_TERMINATED");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubmitter()).isEqualTo("fn ln");
    }
    
    @Test
    void toRequestActionDTOIgnorePayload() {
        Long accountId = 100L;
        Request request = Request.builder()
        		.id("requestId")
        		.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build()).build();
        addResourcesToRequest(accountId, CompetentAuthorityEnum.ENGLAND, request);
        RequestAction requestAction = RequestAction.builder()
                .id(1L)
                .payload(PaymentCancelledRequestActionPayload.builder().payloadType(RequestActionPayloadTypes.PAYMENT_CANCELLED_PAYLOAD).build())
                .type("PAYMENT_CANCELLED")
                .submitter("fn ln")
                .request(request)
                .build();
        
        RequestActionDTO result = mapper.toRequestActionDTOIgnorePayload(requestAction);
        assertThat(result.getPayload()).isNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubmitter()).isEqualTo("fn ln");
        assertThat(result.getRequestAccountId()).isEqualTo(accountId);
        assertThat(result.getRequestId()).isEqualTo("requestId");
        assertThat(result.getRequestType()).isEqualTo("DUMMY_REQUEST_TYPE");
        assertThat(result.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.ENGLAND);
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
