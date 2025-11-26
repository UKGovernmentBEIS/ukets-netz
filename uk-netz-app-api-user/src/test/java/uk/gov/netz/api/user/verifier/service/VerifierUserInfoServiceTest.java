package uk.gov.netz.api.user.verifier.service;

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
class VerifierUserInfoServiceTest {

    @InjectMocks
    private VerifierUserInfoService verifierUserInfoService;

    @Mock
    private UserInfoService userInfoService;

    @Test
    void getVerifierUsersInfo() {
        List<UserInfoDTO> expectedUserInfoList = List.of(UserInfoDTO.builder()
            .firstName("firstName")
            .lastName("lastName")
            .build());

        when(userInfoService.getUsersInfo(List.of("userId"))).thenReturn(expectedUserInfoList);

        List<UserInfoDTO> actualUserInfoList = verifierUserInfoService.getVerifierUsersInfo(List.of("userId"));

        assertEquals(expectedUserInfoList, actualUserInfoList);
    }
}