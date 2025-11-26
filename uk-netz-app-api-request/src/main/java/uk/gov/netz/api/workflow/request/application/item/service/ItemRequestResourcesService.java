package uk.gov.netz.api.workflow.request.application.item.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.repository.RequestRepository;

@Service
@RequiredArgsConstructor
public class ItemRequestResourcesService {

	private final RequestRepository requestRepository;
	
	public Map<String, Map<String, String>> getItemRequestResources(ItemPage itemPage) {
		List<String> requestIds = itemPage.getItems().stream()
        		.map(Item::getRequestId)
        		.distinct()
        		.collect(Collectors.toList());
        
        return requestRepository.findAllById(requestIds).stream()
        		.collect(Collectors.toMap(Request::getId, Request::getRequestResourcesMap));
	}
}
