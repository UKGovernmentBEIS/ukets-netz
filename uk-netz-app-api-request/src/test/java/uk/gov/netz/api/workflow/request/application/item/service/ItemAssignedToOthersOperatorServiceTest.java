package uk.gov.netz.api.workflow.request.application.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.workflow.request.application.authorization.OperatorAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTO;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemOperatorRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ItemAssignedToOthersOperatorServiceTest {

    @InjectMocks
    private ItemAssignedToOthersOperatorService itemService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemOperatorRepository itemOperatorRepository;

    @Mock
    private OperatorAuthorityResourceAdapter operatorAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getItemsAssignedToOthers() {
        final Long accountId = 1L;
        AppUser appUser = buildOperatorUser("oper1Id", "oper1", "oper1", accountId);
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(accountId, Set.of());
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.ACCOUNT, "accountId"));

        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of(expectedItem))
                .totalItems(1L)
                .build();
        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of(expectedItemDTO))
                .totalItems(1L)
                .build();

        // Mock
        doReturn(scopedRequestTaskTypes).when(operatorAuthorityResourceAdapter).getUserScopedRequestTaskTypes(appUser);
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemOperatorRepository).findItems(appUser.getUserId(), ItemAssignmentType.OTHERS,
            scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService)
        		.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);
    }

    @Test
    void getItemsAssignedToOthers_no_user_authorities() {
        Long accountId = 1L;
        AppUser appUser = buildOperatorUser("oper1Id", "oper1", "oper1", accountId);
        Map<Long, Set<String>> scopedRequestTaskTypesAsString = emptyMap();

        // Mock
        doReturn(scopedRequestTaskTypesAsString)
            .when(operatorAuthorityResourceAdapter).getUserScopedRequestTaskTypes(appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(ItemDTOResponse.emptyItemDTOResponse(), actualItemDTOResponse);

        verifyNoInteractions(itemOperatorRepository);
        verifyNoInteractions(itemResponseService);
        verifyNoInteractions(itemRequestResourcesService);

        verify(operatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.OPERATOR, itemService.getRoleType());
    }

    private AppUser buildOperatorUser(String userId, String firstName, String lastName, Long accountId) {
        AppAuthority appAuthority = AppAuthority.builder()
                .accountId(accountId).build();

        return AppUser.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .authorities(List.of(appAuthority))
                .roleType(RoleTypeConstants.OPERATOR)
                .build();
    }
}
