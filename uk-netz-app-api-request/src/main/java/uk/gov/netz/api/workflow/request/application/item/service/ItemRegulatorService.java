package uk.gov.netz.api.workflow.request.application.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.authorization.RegulatorAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemByRequestRegulatorRepository;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItemRegulatorService implements ItemService {

    private final RegulatorAuthorityResourceAdapter regulatorAuthorityResourceAdapter;
    private final ItemResponseService itemResponseService;
    private final ItemByRequestRegulatorRepository itemByRequestRegulatorRepository;
    private final ItemRequestResourcesService itemRequestResourcesService;

    @Override
    public ItemDTOResponse getItemsByRequest(AppUser appUser, String requestId) {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes = regulatorAuthorityResourceAdapter
                .getUserScopedRequestTaskTypes(appUser.getUserId());

        if (ObjectUtils.isEmpty(scopedRequestTaskTypes)) {
            return ItemDTOResponse.emptyItemDTOResponse();
        }

        ItemPage itemPage = itemByRequestRegulatorRepository.findItemsByRequestId(scopedRequestTaskTypes, requestId);
        
        Map<String, Map<String, String>> itemRequestResources = 
        		itemRequestResourcesService.getItemRequestResources(itemPage);

        return itemResponseService.toItemDTOResponse(itemPage, itemRequestResources, appUser);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.REGULATOR;
    }
}
