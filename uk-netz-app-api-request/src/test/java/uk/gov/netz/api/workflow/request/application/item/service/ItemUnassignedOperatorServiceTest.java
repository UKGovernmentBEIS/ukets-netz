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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemUnassignedOperatorServiceTest {

    @InjectMocks
    private ItemUnassignedOperatorService service;

    @Mock
    private ItemOperatorRepository itemOperatorRepository;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private OperatorAuthorityResourceAdapter operatorAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getUnassignedItems() {
        Long accountId = 1L;
        AppUser appUser = AppUser.builder()
            .userId("userId")
            .authorities(List.of(AppAuthority.builder().accountId(accountId).build()))
            .roleType(RoleTypeConstants.OPERATOR)
            .build();
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(accountId, Set.of("requestTaskType1"));
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
        PagingRequest pagingRequest = PagingRequest.builder().pageNumber(0).pageSize(5).build();


        when(operatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser)).thenReturn(scopedRequestTaskTypes);
        when(itemOperatorRepository.findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, pagingRequest))
        		.thenReturn(expectedItemPage);
        when(itemRequestResourcesService.getItemRequestResources(expectedItemPage)).thenReturn(itemRequestResources);
        when(itemResponseService.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser))
        		.thenReturn(expectedItemDTOResponse);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = service.getUnassignedItems(appUser, pagingRequest);

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);

        verify(operatorAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser);
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemOperatorRepository, times(1))
            .findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, pagingRequest);
        verify(itemResponseService, times(1))
            .toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getUnassignedItems_no_user_scopes() {
        AppUser appUser = AppUser.builder()
            .userId("userId")
            .authorities(List.of())
            .roleType(RoleTypeConstants.OPERATOR)
            .build();

        PagingRequest pagingRequest = PagingRequest.builder().pageNumber(0).pageSize(5).build();
        ItemDTOResponse emptyItemDTOResponse = ItemDTOResponse.emptyItemDTOResponse();

        when(operatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser)).thenReturn(Map.of());

        // Invoke
        ItemDTOResponse actualItemDTOResponse = service.getUnassignedItems(appUser, pagingRequest);

        // Assert
        assertEquals(emptyItemDTOResponse, actualItemDTOResponse);

        verify(operatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser);
        verifyNoInteractions(itemOperatorRepository, itemResponseService, itemRequestResourcesService);
    }

    @Test
    void getUnassignedItems_no_items() {
        Long accountId = 1L;
        AppUser appUser = AppUser.builder()
            .userId("userId")
            .authorities(List.of(AppAuthority.builder().accountId(accountId).build()))
            .roleType(RoleTypeConstants.OPERATOR)
            .build();
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(accountId, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.ACCOUNT, "accountId"));

        ItemPage expectedItemPage = ItemPage.builder()
            .items(Collections.emptyList())
            .totalItems(0L).build();
        ItemDTOResponse emptyItemDTOResponse = ItemDTOResponse.emptyItemDTOResponse();
        PagingRequest pagingRequest = PagingRequest.builder().pageNumber(0).pageSize(5).build();


        when(operatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser)).thenReturn(scopedRequestTaskTypes);
        when(itemOperatorRepository.findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, pagingRequest))
        		.thenReturn(expectedItemPage);
        when(itemRequestResourcesService.getItemRequestResources(expectedItemPage)).thenReturn(itemRequestResources);
        when(itemResponseService.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser)).thenReturn(emptyItemDTOResponse);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = service.getUnassignedItems(appUser, pagingRequest);

        // Assert
        assertEquals(emptyItemDTOResponse, actualItemDTOResponse);

        verify(operatorAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser);
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemOperatorRepository, times(1))
            .findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, pagingRequest);
        verify(itemResponseService, times(1))
            .toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.OPERATOR, service.getRoleType());
    }
}