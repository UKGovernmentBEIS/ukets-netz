package uk.gov.netz.api.workflow.request.application.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.workflow.request.application.authorization.OperatorAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemOperatorRepository;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemAssignedToMeOperatorService implements ItemAssignedToMeService {

    private final ItemOperatorRepository itemOperatorRepository;
    private final ItemResponseService itemResponseService;
    private final OperatorAuthorityResourceAdapter operatorAuthorityResourceAdapter;
    private final ItemRequestResourcesService itemRequestResourcesService;

    @Override
    public ItemDTOResponse getItemsAssignedToMe(AppUser appUser, PagingRequest paging) {
        Map<Long, Set<String>> userScopedRequestTaskTypes = operatorAuthorityResourceAdapter
                .getUserScopedRequestTaskTypes(appUser);

        ItemPage itemPage = itemOperatorRepository.findItems(
                appUser.getUserId(),
                ItemAssignmentType.ME,
                userScopedRequestTaskTypes,
                paging);
        
        Map<String, Map<String, String>> itemRequestResources = 
        		itemRequestResourcesService.getItemRequestResources(itemPage);

        return itemResponseService.toItemDTOResponse(itemPage, itemRequestResources, appUser);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.OPERATOR;
    }
}
