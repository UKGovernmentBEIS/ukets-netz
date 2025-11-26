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
import uk.gov.netz.api.workflow.request.application.authorization.VerifierAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTO;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemVerifierRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemAssignedToMeVerifierServiceTest {

    @InjectMocks
    private ItemAssignedToMeVerifierService itemService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemVerifierRepository itemRepository;

    @Mock
    private VerifierAuthorityResourceAdapter verifierAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getItemsAssignedToMe() {
        final String userId = "vb1Id";
        final Long vbId = 1L;
        final AppUser appUser = buildVerifierUser(userId, "vb1", vbId);
        Map<Long, Set<String>> scopedRequestTaskTypes =
                Map.of(vbId, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.ACCOUNT, "accountId"));

        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder().items(List.of(expectedItem)).totalItems(1L).build();
        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder().items(List.of(expectedItemDTO)).totalItems(1L).build();

        // Mock
        when(verifierAuthorityResourceAdapter
            .getUserScopedRequestTaskTypes(appUser))
            .thenReturn(scopedRequestTaskTypes);
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRepository).findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService
                .getItemsAssignedToMe(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);

        verify(verifierAuthorityResourceAdapter, times(1))
                .getUserScopedRequestTaskTypes(appUser);
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemRepository, times(1)).findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        verify(itemResponseService, times(1)).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getItemsAssignedToMe_no_user_authorities() {
        final Long vbId = 1L;
        final AppUser appUser = buildVerifierUser("vb1Id", "vb1", vbId);
        Map<Long, Set<String>> scopedRequestTaskTypes = emptyMap();
        Map<String, Map<String, String>> itemRequestResources = emptyMap();
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of())
                .totalItems(0L).build();
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of())
                .totalItems(0L).build();

        // Mock
        doReturn(scopedRequestTaskTypes)
            .when(verifierAuthorityResourceAdapter).getUserScopedRequestTaskTypes(appUser);
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRepository).findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToMe(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(ItemDTOResponse.emptyItemDTOResponse(), actualItemDTOResponse);

        verify(verifierAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser);
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemRepository, times(1))
                .findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        verify(itemResponseService, times(1)).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.VERIFIER, itemService.getRoleType());
    }

    private AppUser buildVerifierUser(String userId, String username, Long vbId) {
        return AppUser.builder()
                .userId(userId)
                .firstName(username)
                .lastName(username)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build()))
                .roleType(RoleTypeConstants.VERIFIER)
                .build();
    }
}
