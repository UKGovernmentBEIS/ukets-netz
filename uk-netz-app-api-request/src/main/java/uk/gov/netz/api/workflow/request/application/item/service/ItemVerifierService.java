package uk.gov.netz.api.workflow.request.application.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.application.authorization.VerifierAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemByRequestVerifierRepository;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemVerifierService implements ItemService {

    private final VerifierAuthorityResourceAdapter verifierAuthorityResourceAdapter;
    private final ItemResponseService itemResponseService;
    private final ItemByRequestVerifierRepository itemByRequestVerifierRepository;
    private final ItemRequestResourcesService itemRequestResourcesService;

    @Override
    public ItemDTOResponse getItemsByRequest(AppUser appUser, String requestId) {
        Map<Long, Set<String>> scopedRequestTaskTypes = verifierAuthorityResourceAdapter
                .getUserScopedRequestTaskTypes(appUser);

        if (ObjectUtils.isEmpty(scopedRequestTaskTypes)) {
            return ItemDTOResponse.emptyItemDTOResponse();
        }

        ItemPage itemPage = itemByRequestVerifierRepository.findItemsByRequestId(scopedRequestTaskTypes, requestId);
        
        Map<String, Map<String, String>> itemRequestResources = 
        		itemRequestResourcesService.getItemRequestResources(itemPage);

        return itemResponseService.toItemDTOResponse(itemPage, itemRequestResources, appUser);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.VERIFIER;
    }
}
