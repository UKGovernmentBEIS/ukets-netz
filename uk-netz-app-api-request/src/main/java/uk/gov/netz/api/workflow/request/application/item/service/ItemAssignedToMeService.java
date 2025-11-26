package uk.gov.netz.api.workflow.request.application.item.service;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;

public interface ItemAssignedToMeService {

    ItemDTOResponse getItemsAssignedToMe(AppUser appUser, PagingRequest paging);

    String getRoleType();
}
