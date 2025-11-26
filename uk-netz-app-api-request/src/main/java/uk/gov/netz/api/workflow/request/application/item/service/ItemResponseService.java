package uk.gov.netz.api.workflow.request.application.item.service;

import java.util.Map;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;

public interface ItemResponseService {

    ItemDTOResponse toItemDTOResponse(ItemPage itemPage, Map<String, Map<String, String>> itemRequestResources, AppUser appUser);
}
