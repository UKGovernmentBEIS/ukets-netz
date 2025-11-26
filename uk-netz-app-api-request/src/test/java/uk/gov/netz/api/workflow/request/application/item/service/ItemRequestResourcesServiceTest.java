package uk.gov.netz.api.workflow.request.application.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class ItemRequestResourcesServiceTest {

	@InjectMocks
    private ItemRequestResourcesService itemRequestResourcesService;

    @Mock
    private RequestRepository requestRepository;
    
    @Test
    void getItemsByRequest_empty_scopes() {
    	Request request1 = Request.builder()
    			.id("requestId1")
    			.requestResources(List.of(RequestResource.builder()
    					.resourceType(ResourceType.ACCOUNT)
    					.resourceId("accountId1")
    					.build(),
    					RequestResource.builder()
    					.resourceType(ResourceType.CA)
    					.resourceId("ENGLAND")
    					.build()))
    			.build();
    	Request request2 = Request.builder()
    			.id("requestId2")
    			.requestResources(List.of(RequestResource.builder()
    					.resourceType(ResourceType.ACCOUNT)
    					.resourceId("accountId2")
    					.build(),
    					RequestResource.builder()
    					.resourceType(ResourceType.CA)
    					.resourceId("ENGLAND")
    					.build()))
    			.build();
    	ItemPage itemPage = ItemPage.builder()
    			.items(List.of(Item.builder()
    					.requestId("requestId1")
    					.build(), 
    					Item.builder()
    					.requestId("requestId2")
    					.build()))
    			.build();

        // Mock
        when(requestRepository.findAllById(List.of("requestId1", "requestId2")))
                .thenReturn(List.of(request1, request2));

        Map<String, Map<String, String>> expectedResult = 
        		Map.of("requestId1", Map.of(ResourceType.ACCOUNT, "accountId1", ResourceType.CA, "ENGLAND"),
        				"requestId2", Map.of(ResourceType.ACCOUNT, "accountId2", ResourceType.CA, "ENGLAND"));
        		
        // Invoke
        Map<String, Map<String, String>> result = itemRequestResourcesService.getItemRequestResources(itemPage);

        // Assert
        assertThat(result).isEqualTo(expectedResult);

        verify(requestRepository, times(1)).findAllById(List.of("requestId1", "requestId2"));
    }
}
