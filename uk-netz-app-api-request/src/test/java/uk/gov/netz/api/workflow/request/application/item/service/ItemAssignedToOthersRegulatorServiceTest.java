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
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class ItemAssignedToOthersRegulatorServiceTest {

    @InjectMocks
    private ItemAssignedToOthersRegulatorService itemService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemRegulatorRepository itemRegulatorRepository;
    
    @Mock
    private RegulatorAuthorityResourceAdapter regulatorAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getItemsAssignedToOthers() {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
            Map.of(ENGLAND, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.CA, ENGLAND.name()));
        
        AppUser appUser = buildRegulatorUser("reg1Id");
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
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRegulatorRepository).findItems(appUser.getUserId(), ItemAssignmentType.OTHERS,
                scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);


        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);
        
        verify(regulatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser.getUserId());
    }
    
    @Test
    void getItemsAssignedToOthers_empty_scopes() {
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes = Map.of();
        
        AppUser appUser = buildRegulatorUser("reg1Id");

        // Mock
        when(regulatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser.getUserId()))
            .thenReturn(scopedRequestTaskTypes);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService.getItemsAssignedToOthers(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertThat(actualItemDTOResponse).isEqualTo(ItemDTOResponse.emptyItemDTOResponse());
        
        verify(regulatorAuthorityResourceAdapter, times(1))
            .getUserScopedRequestTaskTypes(appUser.getUserId());
        verifyNoInteractions(itemRegulatorRepository);
        verifyNoInteractions(itemResponseService);
        verifyNoInteractions(itemRequestResourcesService);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.REGULATOR, itemService.getRoleType());
    }

    private AppUser buildRegulatorUser(String userId) {
        return AppUser.builder()
                .userId(userId)
                .roleType(RoleTypeConstants.REGULATOR)
                .build();
    }
}
