package uk.gov.netz.api.user.operator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.user.core.service.UserInfoService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@ExtendWith(MockitoExtension.class)
class OperatorUserInfoServiceTest {
    @InjectMocks
    private OperatorUserInfoService operatorUserInfoService;

    @Mock
    private UserInfoService userInfoService;

    @Test
    void getOperatorUsersInfo() {
        List<UserInfoDTO> userInfoDTOList = List.of(UserInfoDTO.builder()
                .firstName("firstName")
                .lastName("lastName")
                .build());

        when(userInfoService.getUsersInfo(List.of("userId"))).thenReturn(userInfoDTOList);

        List<UserInfoDTO> result = operatorUserInfoService.getOperatorUsersInfo(List.of("userId"));
        assertEquals(userInfoDTOList, result);
    }
}