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
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.application.authorization.RegulatorAuthorityResourceAdapter;
import uk.gov.netz.api.workflow.request.application.item.domain.Item;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemAssignmentType;
import uk.gov.netz.api.workflow.request.application.item.domain.ItemPage;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTO;
import uk.gov.netz.api.workflow.request.application.item.domain.dto.ItemDTOResponse;
import uk.gov.netz.api.workflow.request.application.item.repository.ItemRegulatorRepository;

import java.util.Collections;
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
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class ItemUnassignedRegulatorServiceTest {

    @InjectMocks
    private ItemUnassignedRegulatorService service;
    
    @Mock
    private ItemRegulatorRepository itemRegulatorRepository;

    @Mock
    private ItemResponseService itemResponseService;
    
    @Mock
    private RegulatorAuthorityResourceAdapter regulatorAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getUnassignedItems() {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
            Map.of(ENGLAND, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.CA, ENGLAND.name()));
        
        AppUser appUser = buildRegulatorUser("reg1");
        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of(expectedItem))
                .totalItems(1L).build();
        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of(expectedItemDTO))
                .totalItems(1L).build();

        // Mock
        when(regulatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser.getUserId()))
            .thenReturn(scopedRequestTaskTypes);
        when(itemRegulatorRepository.findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build()))
                .thenReturn(expectedItemPage);
        when(itemRequestResourcesService.getItemRequestResources(expectedItemPage)).thenReturn(itemRequestResources);
        when(itemResponseService.toItemDTOResponse(expectedItemPage, itemRequestResources, appUser))
                .thenReturn(expectedItemDTOResponse);

        // Invoke
        ItemDTOResponse actualResponse = service.getUnassignedItems(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertThat(actualResponse).isEqualTo(expectedItemDTOResponse);
        
        verify(regulatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser.getUserId());
    }
    
    @Test
    void getUnassignedItems_empty_scopes() {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes = Map.of();
        
        AppUser appUser = buildRegulatorUser("reg1");

        // Mock
        when(regulatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser.getUserId()))
            .thenReturn(scopedRequestTaskTypes);

        // Invoke
        ItemDTOResponse actualResponse = service.getUnassignedItems(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertThat(actualResponse).isEqualTo(ItemDTOResponse.emptyItemDTOResponse());
        
        verify(regulatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser.getUserId());
        verifyNoInteractions(itemRegulatorRepository, itemResponseService, itemRequestResourcesService);
    }

    @Test
    void getUnassignedItems_ReturnsEmptyResponseWhenNoItemsFetched() {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
            Map.of(ENGLAND, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.CA, ENGLAND.name()));
        
        AppUser appUser = buildRegulatorUser("reg1");
        ItemPage itemPage = ItemPage.builder()
                .items(Collections.emptyList())
                .totalItems(0L).build();
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.emptyItemDTOResponse();

        // Mock
        when(regulatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser.getUserId()))
            .thenReturn(scopedRequestTaskTypes);
        when(itemRequestResourcesService.getItemRequestResources(itemPage)).thenReturn(itemRequestResources);
        when(itemRegulatorRepository.findItems(appUser.getUserId(), ItemAssignmentType.UNASSIGNED, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build()))
                .thenReturn(itemPage);
        when(itemResponseService.toItemDTOResponse(itemPage, itemRequestResources, appUser))
                .thenReturn(expectedItemDTOResponse);

        // Invoke
        ItemDTOResponse actualResponse = service.getUnassignedItems(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertThat(actualResponse).isEqualTo(expectedItemDTOResponse);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.REGULATOR, service.getRoleType());
    }

    private AppUser buildRegulatorUser(String userId) {
        return AppUser.builder()
                .userId(userId)
                .roleType(RoleTypeConstants.REGULATOR)
                .build();
    }
}
