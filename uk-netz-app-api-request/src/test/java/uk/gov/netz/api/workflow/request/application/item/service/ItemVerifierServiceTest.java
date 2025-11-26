package uk.gov.netz.api.workflow.request.application.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.application.authorization.VerifierAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTO;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemByRequestVerifierRepository;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemVerifierServiceTest {

    @InjectMocks
    private ItemVerifierService itemVerifierService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemByRequestVerifierRepository itemByRequestVerifierRepository;

    @Mock
    private VerifierAuthorityResourceAdapter verifierAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Mock
    private RequestService requestService;

    @Test
    void getItemsByRequest() {
        final String requestId = "1";
        final Long verificationBodyId = 1L;
        String userId = "verifierUser";
        AppUser appUser = AppUser.builder().userId(userId).roleType(RoleTypeConstants.VERIFIER).build();
        Map<Long, Set<String>> scopedRequestTaskTypes =
            Map.of(verificationBodyId, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.ACCOUNT, "accountId"));

        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder()
            .items(List.of(expectedItem))
            .totalItems(1L).build();

        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
            .items(List.of(expectedItemDTO))
            .totalItems(1L).build();

        // Mock
        when(verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser))
            .thenReturn(scopedRequestTaskTypes);
        when(itemRequestResourcesService.getItemRequestResources(expectedItemPage)).thenReturn(itemRequestResources);
        when(itemByRequestVerifierRepository.findItemsByRequestId(scopedRequestTaskTypes, requestId)).thenReturn(expectedItemPage);
        when(itemResponseService.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser)).thenReturn(expectedItemDTOResponse);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemVerifierService.getItemsByRequest(appUser, requestId);

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);

        verify(verifierAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser);
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemByRequestVerifierRepository, times(1))
            .findItemsByRequestId(scopedRequestTaskTypes, requestId);
        verify(itemResponseService, times(1))
            .toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getItemsByRequest_no_scopes() {
        final String requestId = "1";
        String userId = "verifierUser";
        AppUser appUser = AppUser.builder().userId(userId).roleType(RoleTypeConstants.VERIFIER).build();
        Map<Long, Set<String>> scopedRequestTaskTypes = Map.of();

        // Mock
        when(verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser))
            .thenReturn(scopedRequestTaskTypes);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemVerifierService.getItemsByRequest(appUser, requestId);

        // Assert
        assertThat(actualItemDTOResponse).isEqualTo(ItemDTOResponse.emptyItemDTOResponse());

        verify(verifierAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser);
        verifyNoInteractions(itemByRequestVerifierRepository, itemResponseService, itemRequestResourcesService);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.VERIFIER, itemVerifierService.getRoleType());
    }
}