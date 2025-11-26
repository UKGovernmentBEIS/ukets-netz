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

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class ItemAssignedToMeRegulatorServiceTest {

    @InjectMocks
    private ItemAssignedToMeRegulatorService itemService;

    @Mock
    private ItemResponseService itemResponseService;

    @Mock
    private ItemRegulatorRepository itemRegulatorRepository;

    @Mock
    private RegulatorAuthorityResourceAdapter regulatorAuthorityResourceAdapter;
    
    @Mock
    private ItemRequestResourcesService itemRequestResourcesService;

    @Test
    void getItemsAssignedToMe() {
        AppUser appUser = buildRegulatorUser("reg1Id", "reg1");
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes =
            Map.of(ENGLAND, Set.of("requestTaskType1"));
        Map<String, Map<String, String>> itemRequestResources = 
        		Map.of("requestId", Map.of(ResourceType.CA, ENGLAND.name()));

        Item expectedItem = mock(Item.class);
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of(expectedItem))
                .totalItems(1L).build();
        ItemDTO expectedItemDTO = mock(ItemDTO.class);
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of(expectedItemDTO))
                .totalItems(1L).build();

        // Mock
        when(regulatorAuthorityResourceAdapter
            .getUserScopedRequestTaskTypes(appUser.getUserId()))
            .thenReturn(scopedRequestTaskTypes);
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRegulatorRepository).findItems(appUser.getUserId(), ItemAssignmentType.ME,
            scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService
                .getItemsAssignedToMe(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(expectedItemDTOResponse, actualItemDTOResponse);

        verify(regulatorAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser.getUserId());
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemRegulatorRepository, times(1)).findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        verify(itemResponseService, times(1)).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getItemsAssignedToMe_no_user_authorities() {
        AppUser appUser = buildRegulatorUser("reg1Id", "reg1");
        Map<CompetentAuthorityEnum, Set<String>> scopedRequestTaskTypes = emptyMap();
        Map<String, Map<String, String>> itemRequestResources = emptyMap();
        ItemPage expectedItemPage = ItemPage.builder()
                .items(List.of())
                .totalItems(0L).build();
        ItemDTOResponse expectedItemDTOResponse = ItemDTOResponse.builder()
                .items(List.of())
                .totalItems(0L).build();

        // Mock
        doReturn(scopedRequestTaskTypes)
            .when(regulatorAuthorityResourceAdapter)
            .getUserScopedRequestTaskTypes(appUser.getUserId());
        doReturn(itemRequestResources).when(itemRequestResourcesService).getItemRequestResources(expectedItemPage);
        doReturn(expectedItemPage).when(itemRegulatorRepository).findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        doReturn(expectedItemDTOResponse).when(itemResponseService).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);

        // Invoke
        ItemDTOResponse actualItemDTOResponse = itemService
            .getItemsAssignedToMe(appUser, PagingRequest.builder().pageNumber(0).pageSize(10).build());

        // Assert
        assertEquals(ItemDTOResponse.emptyItemDTOResponse(), actualItemDTOResponse);

        verify(regulatorAuthorityResourceAdapter, times(1)).getUserScopedRequestTaskTypes(appUser.getUserId());
        verify(itemRequestResourcesService, times(1)).getItemRequestResources(expectedItemPage);
        verify(itemRegulatorRepository, times(1))
                .findItems(appUser.getUserId(), ItemAssignmentType.ME, scopedRequestTaskTypes, PagingRequest.builder().pageNumber(0).pageSize(10).build());
        verify(itemResponseService, times(1)).toItemDTOResponse(expectedItemPage, itemRequestResources, appUser);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.REGULATOR, itemService.getRoleType());
    }

    private AppUser buildRegulatorUser(String userId, String username) {
        AppAuthority appAuthority = AppAuthority.builder()
                .competentAuthority(ENGLAND)
                .build();

        return AppUser.builder()
                .userId(userId)
                .firstName(username)
                .lastName(username)
                .authorities(List.of(appAuthority))
                .roleType(RoleTypeConstants.REGULATOR)
                .build();
    }
}
