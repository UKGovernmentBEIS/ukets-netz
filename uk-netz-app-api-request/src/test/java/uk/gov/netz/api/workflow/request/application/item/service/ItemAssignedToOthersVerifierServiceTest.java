package uk.gov.netz.api.workflow.request.application.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemAssignedToOthersVerifierServiceTest {

    @InjectMocks
    private ItemAssignedToOthersVerifierService itemService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemVerifierRepository itemRepository;

    @Mock
    private VerifierAuthorityResourceAdapter verifierAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getItemsAssignedToOthers() {
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of(1L, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.ACCOUNT, "accountId"));

        AppUser appUser = AppUser.builder().userId("vb1Id").roleType(RoleTypeConstants.VERIFIER).build();
        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of(expectedItem))
                .totalItems(1L).build();
        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of(expectedItemDTO))
                .totalItems(1L).build();

        // Mock
        when(verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser)).thenReturn(scopedRequestTaskTypes);
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRepository).findItems(appUser.getUserId(), ItemAssignmentType.OTHERS,
                scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService)
        		.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);
        verify(verifierAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser);
    }

    @Test
    void getItemsAssignedToOthers_empty_scopes() {
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of();
        AppUser appUser = AppUser.builder().userId("vb1Id").roleType(RoleTypeConstants.VERIFIER).build();

        // Mock
        when(verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser))
                .thenReturn(scopedRequestTaskTypes);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertThat(actualItemDTOResponse).isEqualTo(ItemDTOResponse.emptyItemDTOResponse());

        verify(verifierAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser);
        verifyNoInteractions(itemRepository);
        verifyNoInteractions(itemResponseService);
        verifyNoInteractions(itemRequestResourcesService);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.VERIFIER, itemService.getRoleType());
    }
}
