package uk.gov.netz.api.workflow.request.application.taskview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

class RequestInfoMapperTest {

    private RequestInfoMapper mapper;

    @BeforeEach
    public void init() {
        mapper = Mappers.getMapper(RequestInfoMapper.class);
    }

    @Test
    void toRequestInfoDTO() {
        final String requestId = "1";
        RequestType requestType = RequestType.builder().code("requestTypeCode").build();
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;

        Request request = Request.builder()
            .id(requestId)
            .type(requestType)
            .build();
        addResourcesToRequest(1L, ca, request);

        RequestInfoDTO result = mapper.toRequestInfoDTO(request);

        assertEquals(requestId, result.getId());
        assertEquals("requestTypeCode", result.getType());
        assertEquals(ca, result.getCompetentAuthority());
        assertEquals(1L, result.getAccountId());
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
